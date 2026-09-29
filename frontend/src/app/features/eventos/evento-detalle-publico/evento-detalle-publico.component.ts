import { Component, OnInit, signal } from '@angular/core';
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
  private readonly viewState = signal<{ evento?: Evento; loading: boolean }>({ loading: true });
  get evento(): Evento | undefined { return this.viewState().evento; }
  private set evento(value: Evento | undefined) { this.viewState.update((state) => ({ ...state, evento: value })); }
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }

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
