import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

// Material
import { MatSnackBar } from '@angular/material/snack-bar';

// Service & Model
import { ControlAccesoService } from '../../../core/services/control-acceso.service';
import { EventoService } from '../../../core/services/evento.service';
import { ControlAccesoResponse } from '../../../core/models/control-acceso.model';
import { Evento } from '../../../core/models/evento.model';

@Component({
  selector: 'app-historial-acceso',
  templateUrl: './historial-acceso.component.html',
  styleUrls: [],
  standalone: false
})
export class HistorialAccesoComponent implements OnInit {
  filtroForm: FormGroup;
  eventos: Evento[] = [];
  historial: ControlAccesoResponse[] = [];
  loading = false;
  displayedColumns: string[] = ['fecha', 'participante', 'evento', 'estado', 'operador', 'observacion'];

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

  cargarHistorial(): void {
    if (this.filtroForm.invalid) return;

    this.loading = true;
    const eventoId = this.filtroForm.value.eventoId;

    this.controlAccesoService.obtenerHistorial(eventoId).subscribe({
      next: (data) => {
        this.historial = data;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Error al recuperar el historial', 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });
  }
}
