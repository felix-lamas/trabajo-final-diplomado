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
    confirmando: boolean;
    completado: boolean;
    estadoInscripcion?: EstadoInscripcion;
  }>({ loading: true, confirmando: false, completado: false });
  get evento(): Evento | undefined { return this.viewState().evento; }
  private set evento(value: Evento | undefined) { this.viewState.update((state) => ({ ...state, evento: value })); }
  get loading(): boolean { return this.viewState().loading; }
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
      this.router.navigate(['/eventos']);
      return;
    }

    this.eventoService.obtenerPorId(id).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.loading = false)
    ).subscribe({
      next: (data) => {
        this.evento = data;
      },
      error: (err) => {
        this.snackBar.open(apiErrorMessage(err, 'No fue posible cargar el evento'), 'Cerrar', { duration: 4500 });
        this.router.navigate(['/eventos']);
      }
    });
  }

  confirmar(): void {
    if (!this.evento || this.confirmando) return;

    this.confirmando = true;
    this.inscripcionService.inscribir({ eventoId: this.evento.id }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.confirmando = false)
    ).subscribe({
      next: (inscripcion) => {
        this.completado = true;
        this.estadoInscripcion = inscripcion.estado;
        this.snackBar.open(`Inscripcion realizada con exito. Estado: ${inscripcion.estado}`, 'Cerrar', { duration: 3500 });
        this.router.navigate(['/privado/inscripciones', inscripcion.id]);
      },
      error: (err) => {
        const mensaje = err.status === 409
          ? 'Ya existe una inscripcion para este evento'
          : apiErrorMessage(err, 'No fue posible completar la inscripcion');
        this.snackBar.open(mensaje, 'Cerrar', { duration: 4500 });
      }
    });
  }
}
