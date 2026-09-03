import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

// Material
import { MatSnackBar } from '@angular/material/snack-bar';

// Service & Model
import { ControlAccesoService } from '../../../core/services/control-acceso.service';
import { EventoService } from '../../../core/services/evento.service';
import { AsistenciaResponse } from '../../../core/models/control-acceso.model';
import { Evento } from '../../../core/models/evento.model';

@Component({
  selector: 'app-asistencia-list',
  templateUrl: './asistencia-list.component.html',
  styleUrls: [],
  standalone: false
})
export class AsistenciaListComponent implements OnInit {
  filtroForm: FormGroup;
  eventos: Evento[] = [];
  asistencias: AsistenciaResponse[] = [];
  loading = false;
  displayedColumns: string[] = ['indice', 'codigo', 'participante', 'documento', 'fechaHora', 'operador'];

  constructor(
    private fb: FormBuilder,
    private controlAccesoService: ControlAccesoService,
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

    this.controlAccesoService.listarAsistenciasPorEvento(eventoId).subscribe({
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
