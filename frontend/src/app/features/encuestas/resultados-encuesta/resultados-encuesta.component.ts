import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EncuestaService } from '../../../core/services/encuesta.service';
import { EstadisticasEncuestaResponse, EncuestaResponse } from '../../../core/models/encuesta.model';

@Component({
  selector: 'app-resultados-encuesta',
  templateUrl: './resultados-encuesta.component.html',
  standalone: false
})
export class ResultadosEncuestaComponent implements OnInit {
  eventoId = '';
  private readonly viewState = signal<{
    loading: boolean;
    estadisticas: EstadisticasEncuestaResponse | null;
    respuestas: EncuestaResponse[];
  }>({ loading: false, estadisticas: null, respuestas: [] });
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
  get estadisticas(): EstadisticasEncuestaResponse | null { return this.viewState().estadisticas; }
  private set estadisticas(value: EstadisticasEncuestaResponse | null) { this.viewState.update((state) => ({ ...state, estadisticas: value })); }
  get respuestas(): EncuestaResponse[] { return this.viewState().respuestas; }
  private set respuestas(value: EncuestaResponse[]) { this.viewState.update((state) => ({ ...state, respuestas: value })); }

  constructor(
    private route: ActivatedRoute,
    private encuestaService: EncuestaService
  ) {}

  ngOnInit(): void {
    this.eventoId = this.route.snapshot.paramMap.get('eventoId') || '';
    if (this.eventoId) {
      this.cargar();
    }
  }

  cargar(): void {
    this.loading = true;
    this.encuestaService.obtenerEstadisticas(this.eventoId).subscribe({
      next: (data) => {
        this.estadisticas = data;
        this.encuestaService.obtenerPorEvento(this.eventoId).subscribe({
          next: (detalle) => {
            this.respuestas = detalle;
            this.loading = false;
          },
          error: () => this.loading = false
        });
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
