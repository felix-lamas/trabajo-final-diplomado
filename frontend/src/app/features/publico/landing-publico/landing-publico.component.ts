import { Component, OnInit } from '@angular/core';
import { EventoService } from '../../../core/services/evento.service';
import { Evento } from '../../../core/models/evento.model';

@Component({
  selector: 'app-landing-publico',
  templateUrl: './landing-publico.component.html',
  standalone: false
})
export class LandingPublicoComponent implements OnInit {
  eventos: Evento[] = [];
  loading = true;

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
