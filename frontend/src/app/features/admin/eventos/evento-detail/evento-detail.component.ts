import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';

import { EventoService } from '../../../../core/services/evento.service';
import { Evento } from '../../../../core/models/evento.model';
import { finalize } from 'rxjs';
import { ToastService } from '../../../../shared/ui/toast.service';
import { AuthService } from '../../../../core/services/auth.service';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';
import { EventoMotivoDialogComponent } from '../evento-motivo-dialog.component';
import { InscripcionService } from '../../../../core/services/inscripcion.service';
import { Inscripcion } from '../../../../core/models/inscripcion.model';

@Component({
  selector: 'app-evento-detail',
  templateUrl: './evento-detail.component.html',
  standalone: false
})
export class EventoDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  readonly evento = signal<Evento | undefined>(undefined);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly procesando = signal(false);
  readonly inscripciones = signal<Inscripcion[]>([]);
  readonly cargandoInscripciones = signal(false);
  readonly errorInscripciones = signal('');

  constructor(
    private eventoService: EventoService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private toast: ToastService,
    private inscripcionService: InscripcionService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarEvento(id);
    }
  }

  cargarEvento(id: string): void {
    this.loading.set(true);
    this.error.set(false);
    this.eventoService.obtenerPorId(id).pipe(finalize(() => this.loading.set(false)), takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => {
        this.evento.set(data);
        if (this.esAdministrador || this.esOrganizador) this.cargarInscripciones(data.id);
      },
      error: (err) => { this.error.set(true); this.toast.error(apiErrorMessage(err, 'Error al cargar detalle')); }
    });
  }

  private cargarInscripciones(eventoId: string): void {
    this.cargandoInscripciones.set(true);
    this.errorInscripciones.set('');
    this.inscripcionService.listarInscritosEvento(eventoId).pipe(
      finalize(() => this.cargandoInscripciones.set(false)), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (data) => this.inscripciones.set(data),
      error: (err) => this.errorInscripciones.set(apiErrorMessage(err, 'No fue posible cargar las inscripciones del evento.'))
    });
  }

  publicar(): void {
    if (!this.evento()) return;
    this.procesar(this.eventoService.publicar(this.evento()!.id), 'Evento publicado con exito', 'Error al publicar');
  }

  rechazar(): void {
    const evento = this.evento();
    if (!evento) return;
    this.solicitarMotivo('Rechazar evento', 'Indique la correccion requerida al organizador', (motivo) =>
      this.procesar(this.eventoService.rechazar(evento.id, motivo), 'Evento rechazado', 'Error al rechazar'));
  }

  volverABorrador(): void {
    if (!this.evento()) return;
    this.procesar(this.eventoService.volverABorrador(this.evento()!.id), 'Evento devuelto a borrador', 'Error al volver a borrador');
  }

  enviarARevision(): void {
    if (!this.evento()) return;
    this.procesar(this.eventoService.enviarARevision(this.evento()!.id), 'Evento enviado a revision', 'Error al enviar a revision');
  }

  private procesar(operacion$: ReturnType<EventoService['publicar']>, exito: string, error: string): void {
    if (this.procesando()) return;
    this.procesando.set(true);
    operacion$.pipe(finalize(() => this.procesando.set(false)), takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toast.success(exito);
        this.cargarEvento(this.evento()!.id);
      },
      error: (err) => this.toast.error(apiErrorMessage(err, error))
    });
  }

  get esAdministrador(): boolean {
    return this.authService.hasAnyRole(['ADMINISTRADOR']);
  }

  get esOrganizador(): boolean {
    return this.authService.hasAnyRole(['ORGANIZADOR']);
  }

  get rutaGestion(): string {
    return this.esOrganizador ? '/organizador/eventos' : '/admin/eventos';
  }

  cancelar(): void {
    const evento = this.evento();
    if (!evento) return;
    this.solicitarMotivo('Cancelar evento', 'Esta transicion es definitiva. Indique el motivo', (motivo) =>
      this.procesar(this.eventoService.cancelar(evento.id, motivo), 'Evento cancelado', 'Error al cancelar'));
  }

  finalizar(): void {
    if (!this.evento()) return;
    this.procesar(this.eventoService.finalizar(this.evento()!.id), 'Evento finalizado correctamente', 'Error al finalizar');
  }

  private solicitarMotivo(titulo: string, mensaje: string, aceptar: (motivo: string) => void): void {
    this.dialog.open(EventoMotivoDialogComponent, { width: 'min(520px, 92vw)', data: { titulo, mensaje } })
      .afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe((motivo?: string) => { if (motivo) aceptar(motivo); });
  }
}
