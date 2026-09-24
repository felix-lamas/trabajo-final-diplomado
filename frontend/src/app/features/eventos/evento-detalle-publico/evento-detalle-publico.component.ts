import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EventoService } from '../../../core/services/evento.service';
import { Evento } from '../../../core/models/evento.model';

@Component({
  selector: 'app-evento-detalle-publico',
  templateUrl: './evento-detalle-publico.component.html',
  standalone: false
})
export class EventoDetallePublicoComponent implements OnInit {
  evento?: Evento;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventoService: EventoService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.cargarEvento(id);
  }

  cargarEvento(id: string): void {
    this.eventoService.obtenerPorId(id).subscribe({
      next: (data) => {
        this.evento = data;
        this.loading = false;
      },
      error: (err) => {
        this.snackBar.open(err.error?.mensaje || 'Error al cargar el evento', 'Cerrar', { duration: 3000 });
        this.router.navigate(['/eventos']);
      }
    });
  }
}
