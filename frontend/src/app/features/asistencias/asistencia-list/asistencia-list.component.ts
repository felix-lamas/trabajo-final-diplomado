import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

// Material
import { MatSnackBar } from '@angular/material/snack-bar';

// Service & Model
import { AsistenciaService } from '../../../core/services/asistencia.service';
import { EventoService } from '../../../core/services/evento.service';
import { AsistenciaResponse } from '../../../core/models/asistencia.model';
import { Evento } from '../../../core/models/evento.model';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

@Component({
  selector: 'app-asistencia-list',
  templateUrl: './asistencia-list.component.html',
  styleUrl: './asistencia-list.component.css',
  standalone: false
})
export class AsistenciaListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  filtroForm: FormGroup;
  private readonly viewState = signal({
    eventos: [] as Evento[],
    asistencias: [] as AsistenciaResponse[],
    eventosLoading: true,
    loading: false,
    error: '',
    eventosError: ''
  });
  get eventos(): Evento[] { return this.viewState().eventos; }
  private set eventos(value: Evento[]) { this.viewState.update((state) => ({ ...state, eventos: value })); }
  get asistencias(): AsistenciaResponse[] { return this.viewState().asistencias; }
  private set asistencias(value: AsistenciaResponse[]) { this.viewState.update((state) => ({ ...state, asistencias: value })); }
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
  get eventosLoading(): boolean { return this.viewState().eventosLoading; }
  private set eventosLoading(value: boolean) { this.viewState.update((state) => ({ ...state, eventosLoading: value })); }
  get error(): string { return this.viewState().error; }
  private set error(value: string) { this.viewState.update((state) => ({ ...state, error: value })); }
  get eventosError(): string { return this.viewState().eventosError; }
  private set eventosError(value: string) { this.viewState.update((state) => ({ ...state, eventosError: value })); }
  displayedColumns: string[] = ['indice', 'codigo', 'participante', 'sesion', 'fechaHora', 'ubicacion', 'operador'];

  constructor(
    private fb: FormBuilder,
    private asistenciaService: AsistenciaService,
    private eventoService: EventoService,
    private snackBar: MatSnackBar
  ) {
    this.filtroForm = this.fb.group({
      eventoId: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarEventos();
  }

  cargarEventos(): void {
    this.eventosLoading = true;
    this.eventosError = '';
    this.eventoService.listar().pipe(
      finalize(() => this.eventosLoading = false), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (data) => this.eventos = data,
      error: (err) => {
        this.eventosError = apiErrorMessage(err, 'No fue posible cargar tus eventos.');
        this.snackBar.open(this.eventosError, 'Cerrar', { duration: 4000 });
      }
    });
  }

  cargarAsistencias(): void {
    if (this.filtroForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.asistencias = [];
    const eventoId = this.filtroForm.value.eventoId;

    this.asistenciaService.listarPorEvento(eventoId).pipe(
      finalize(() => this.loading = false), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (data) => {
        this.asistencias = data;
      },
      error: (err) => {
        this.error = apiErrorMessage(err, 'No fue posible cargar la lista de asistencia.');
        this.snackBar.open(this.error, 'Cerrar', { duration: 4000 });
      }
    });
  }
}
