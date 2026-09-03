import { Component, OnInit } from '@angular/core';
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
  loading = false;
  estadisticas: EstadisticasEncuestaResponse | null = null;
  respuestas: EncuestaResponse[] = [];

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
