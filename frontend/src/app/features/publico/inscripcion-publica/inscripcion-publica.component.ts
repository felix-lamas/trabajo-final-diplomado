import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventoService } from '../../../core/services/evento.service';
import { InscripcionService } from '../../../core/services/inscripcion.service';
import { Evento } from '../../../core/models/evento.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-inscripcion-publica',
  templateUrl: './inscripcion-publica.component.html',
  standalone: false
})
export class InscripcionPublicaComponent implements OnInit {
  evento?: Evento;
  loading = true;
  confirmando = false;
  completado = false;

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

    this.eventoService.obtenerPorId(id).subscribe({
      next: (data) => {
        this.evento = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.router.navigate(['/eventos']);
      }
    });
  }

  confirmar(): void {
    if (!this.evento) return;

    this.confirmando = true;
    this.inscripcionService.inscribir({ eventoId: this.evento.id }).subscribe({
      next: (inscripcion) => {
        this.confirmando = false;
        this.completado = true;
        this.snackBar.open('Inscripcion realizada con exito', 'Cerrar', { duration: 3500 });
        setTimeout(() => this.router.navigate(['/privado/inscripciones', inscripcion.id]), 900);
      },
      error: (err) => {
        this.confirmando = false;
        this.snackBar.open(err.error?.mensaje || 'No fue posible completar la inscripcion', 'Cerrar', { duration: 4500 });
      }
    });
  }
}
