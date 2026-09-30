import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

// Material
import { MatSnackBar } from '@angular/material/snack-bar';

// Service & Model
import { AsistenciaService } from '../../../core/services/asistencia.service';
import { EventoService } from '../../../core/services/evento.service';
import { AsistenciaResponse } from '../../../core/models/asistencia.model';
import { Evento } from '../../../core/models/evento.model';

@Component({
  selector: 'app-asistencia-list',
  templateUrl: './asistencia-list.component.html',
  styleUrls: [],
  standalone: false
})
export class AsistenciaListComponent implements OnInit {
  filtroForm: FormGroup;
  private readonly viewState = signal({
    eventos: [] as Evento[],
    asistencias: [] as AsistenciaResponse[],
    loading: false
  });
  get eventos(): Evento[] { return this.viewState().eventos; }
  private set eventos(value: Evento[]) { this.viewState.update((state) => ({ ...state, eventos: value })); }
  get asistencias(): AsistenciaResponse[] { return this.viewState().asistencias; }
  private set asistencias(value: AsistenciaResponse[]) { this.viewState.update((state) => ({ ...state, asistencias: value })); }
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
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
    this.eventoService.listar().subscribe({
      next: (data) => this.eventos = data,
      error: () => this.snackBar.open('Error al cargar eventos', 'Cerrar', { duration: 3000 })
    });
  }

  cargarAsistencias(): void {
    if (this.filtroForm.invalid) return;

    this.loading = true;
    const eventoId = this.filtroForm.value.eventoId;

    this.asistenciaService.listarPorEvento(eventoId).subscribe({
      next: (data) => {
        this.asistencias = data;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Error al cargar la lista de asistencia', 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });
  }
}
