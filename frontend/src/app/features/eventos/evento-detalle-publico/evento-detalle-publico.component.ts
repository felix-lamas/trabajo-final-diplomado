import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventoService } from '../../../core/services/evento.service';
import { InscripcionService } from '../../../core/services/inscripcion.service';
import { Evento } from '../../../core/models/evento.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-evento-detalle-publico',
  templateUrl: './evento-detalle-publico.component.html',
  standalone: false
})
export class EventoDetallePublicoComponent implements OnInit {
  evento?: Evento;
  loading = true;
  inscribiendo = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventoService: EventoService,
    private inscripcionService: InscripcionService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarEvento(id);
    }
  }

  cargarEvento(id: string): void {
    this.eventoService.obtenerPorId(id).subscribe({
      next: (data) => {
        this.evento = data;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Error al cargar el evento', 'Cerrar', { duration: 3000 });
        this.router.navigate(['/']);
      }
    });
  }

  inscribirse(): void {
    if (!this.evento) return;

    this.inscribiendo = true;
    this.inscripcionService.inscribir({ eventoId: this.evento.id }).subscribe({
      next: (inscripcion) => {
        this.snackBar.open('Inscripción realizada con éxito', 'Cerrar', { duration: 3000 });
        this.router.navigate(['/privado/inscripciones', inscripcion.id]);
      },
      error: (err) => {
        this.inscribiendo = false;
        this.snackBar.open(err.error?.mensaje || 'Error al inscribirse', 'Cerrar', { duration: 5000 });
      }
    });
  }
}
