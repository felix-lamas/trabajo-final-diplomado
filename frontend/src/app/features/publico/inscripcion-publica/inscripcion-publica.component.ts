import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventoService } from '../../../core/services/evento.service';
import { InscripcionService } from '../../../core/services/inscripcion.service';
import { Evento } from '../../../core/models/evento.model';
import { EstadoInscripcion } from '../../../core/models/inscripcion.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { apiErrorMessage } from '../../../core/utils/api-error.util';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-inscripcion-publica',
  templateUrl: './inscripcion-publica.component.html',
  standalone: false
})
export class InscripcionPublicaComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly viewState = signal<{
    evento?: Evento;
    loading: boolean;
    error: string;
    confirmando: boolean;
    completado: boolean;
    estadoInscripcion?: EstadoInscripcion;
  }>({ loading: true, error: '', confirmando: false, completado: false });
  get evento(): Evento | undefined { return this.viewState().evento; }
  private set evento(value: Evento | undefined) { this.viewState.update((state) => ({ ...state, evento: value })); }
  get loading(): boolean { return this.viewState().loading; }
  get error(): string { return this.viewState().error; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
  get confirmando(): boolean { return this.viewState().confirmando; }
  private set confirmando(value: boolean) { this.viewState.update((state) => ({ ...state, confirmando: value })); }
  get completado(): boolean { return this.viewState().completado; }
  private set completado(value: boolean) { this.viewState.update((state) => ({ ...state, completado: value })); }
  get estadoInscripcion(): EstadoInscripcion | undefined { return this.viewState().estadoInscripcion; }
  private set estadoInscripcion(value: EstadoInscripcion | undefined) { this.viewState.update((state) => ({ ...state, estadoInscripcion: value })); }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventoService: EventoService,
    private inscripcionService: InscripcionService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.viewState.update((state) => ({ ...state, loading: false, error: 'No se encontro el evento solicitado.' }));
      return;
    }
    this.cargarEvento(id);
  }

  cargarEvento(id: string): void {
    this.viewState.update((state) => ({ ...state, loading: true, error: '' }));
    this.eventoService.obtenerPorId(id).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.viewState.update((state) => ({ ...state, loading: false })))
    ).subscribe({
      next: (data) => { this.evento = data; this.qrImagenFallida = false; },
      error: (err) => this.viewState.update((state) => ({ ...state, error: apiErrorMessage(err, 'No fue posible cargar el evento.') }))
    });
  }

  confirmar(): void {
    if (!this.evento || this.confirmando || !this.evento.requiereInscripcion) return;

    this.confirmando = true;
    this.inscripcionService.inscribir({ eventoId: this.evento.id }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.confirmando = false)
    ).subscribe({
      next: (inscripcion) => {
        this.completado = true;
        this.estadoInscripcion = inscripcion.estado;
        this.snackBar.open('Inscripcion realizada correctamente.', 'Cerrar', { duration: 3500 });
        this.router.navigate(['/privado/inscripciones', inscripcion.id]);
      },
      error: (err) => {
        this.viewState.update((state) => ({
          ...state,
          error: err.status === 409
            ? apiErrorMessage(err, 'La inscripcion entro en conflicto con el estado actual del evento.')
            : apiErrorMessage(err, 'No fue posible completar la inscripcion.')
        }));
      }
    });
  }

  get sinCupo(): boolean {
    return Boolean(this.evento?.cupoLimitado && (this.evento.cupoDisponible == null || this.evento.cupoDisponible <= 0));
  }

  get qrPagoImagenUrl(): string | null {
    const url = this.evento?.qrPagoUrl;
    return url ? this.eventoService.normalizarQrPagoUrl(url) : null;
  }
  qrImagenFallida = false;
  ocultarQr(): void { this.qrImagenFallida = true; }
}
