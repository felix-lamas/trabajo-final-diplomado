import { Component, OnInit, signal } from '@angular/core';
import { EventoService } from '../../../core/services/evento.service';
import { Evento } from '../../../core/models/evento.model';

@Component({
  selector: 'app-landing-publico',
  templateUrl: './landing-publico.component.html',
  standalone: false
})
export class LandingPublicoComponent implements OnInit {
  private readonly viewState = signal({ eventos: [] as Evento[], loading: true });
  get eventos(): Evento[] { return this.viewState().eventos; }
  private set eventos(value: Evento[]) { this.viewState.update((state) => ({ ...state, eventos: value })); }
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }

  constructor(private eventoService: EventoService) {}

  ngOnInit(): void {
    this.eventoService.listarPublicados().subscribe({
      next: (data) => {
        this.eventos = data;
        this.loading = false;
      },
      error: () => {
        this.eventos = [];
        this.loading = false;
      }
    });
  }

  get destacados(): Evento[] {
    return this.eventos.slice(0, 3);
  }

  get categorias(): string[] {
    return Array.from(new Set(this.eventos.map((evento) => evento.categoriaNombre).filter(Boolean)));
  }

  get totalEventos(): number {
    return this.eventos.length;
  }

  get totalCupos(): number {
    return this.eventos.reduce((acc, evento) => acc + (evento.cupoDisponible || 0), 0);
  }

  get totalCategorias(): number {
    return this.categorias.length;
  }

  get modalidadPresencial(): number {
    return this.eventos.filter((evento) => evento.modalidad === 'PRESENCIAL').length;
  }
}
