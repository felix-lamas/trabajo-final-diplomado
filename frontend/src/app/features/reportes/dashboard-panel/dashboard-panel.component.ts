import { Component, OnInit } from '@angular/core';

import { DashboardService } from '../../../core/services/dashboard.service';
import {
  DashboardAcademicoResponse,
  DashboardEjecutivoResponse,
  DashboardOperativoResponse
} from '../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard-panel',
  templateUrl: './dashboard-panel.component.html',
  styleUrls: [],
  standalone: false
})
export class DashboardPanelComponent implements OnInit {
  ejecutivo: DashboardEjecutivoResponse | null = null;
  academico: DashboardAcademicoResponse | null = null;
  operativo: DashboardOperativoResponse | null = null;
  loading = false;

  filtroFechaInicio: Date | null = null;
  filtroFechaFin: Date | null = null;
  filtroFacultad = 'Todas';
  filtroCarrera = 'Todas';
  filtroCategoria = 'Todas';

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loading = true;

    this.dashboardService.obtenerEjecutivo().subscribe(data => this.ejecutivo = data);
    this.dashboardService.obtenerAcademico().subscribe(data => this.academico = data);
    this.dashboardService.obtenerOperativo().subscribe(data => {
      this.operativo = data;
      this.loading = false;
    });
  }

  get facultades(): string[] {
    return ['Todas', ...(this.academico?.participacionPorFacultad ?? []).map(item => item.name)];
  }

  get carreras(): string[] {
    return ['Todas', ...(this.academico?.participacionPorCarrera ?? []).map(item => item.name)];
  }

  get categorias(): string[] {
    return ['Todas', ...(this.academico?.participacionPorCategoria ?? []).map(item => item.name)];
  }

  maxValue(items: Array<{ value: number }>): number {
    return Math.max(...items.map(item => item.value), 1);
  }

  progress(value: number, max: number): number {
    return Math.min(100, Math.round((value / Math.max(max, 1)) * 100));
  }

  total(items: Array<{ value: number }>): number {
    return items.reduce((sum, item) => sum + item.value, 0);
  }

  trendPath(items: Array<{ periodo: string; inscritos: number }>): string {
    if (!items.length) {
      return 'M0 96 L360 96';
    }

    const max = Math.max(...items.map(item => item.inscritos), 1);
    const step = items.length > 1 ? 360 / (items.length - 1) : 360;
    return items
      .map((item, index) => {
        const x = index * step;
        const y = 104 - (item.inscritos / max) * 88;
        return `${index === 0 ? 'M' : 'L'}${x.toFixed(1)} ${y.toFixed(1)}`;
      })
      .join(' ');
  }

  trendArea(items: Array<{ periodo: string; inscritos: number }>): string {
    return `${this.trendPath(items)} L360 112 L0 112 Z`;
  }

  donutBackground(percent: number): string {
    const value = Math.max(0, Math.min(100, percent || 0));
    return `conic-gradient(var(--app-primary) 0 ${value}%, #dfe8f2 ${value}% 100%)`;
  }

  paymentValidationRate(): number {
    if (!this.operativo) {
      return 0;
    }

    const totalPagos = this.operativo.pagosValidados + this.operativo.pagosPendientes;
    return this.progress(this.operativo.pagosValidados, totalPagos);
  }

  asistenciaRate(): number {
    if (!this.operativo || !this.ejecutivo) {
      return 0;
    }

    return this.progress(this.operativo.asistenciasRegistradas, this.ejecutivo.totalInscripciones);
  }

  certificadoRate(): number {
    if (!this.ejecutivo) {
      return 0;
    }

    return this.progress(this.ejecutivo.totalCertificados, this.ejecutivo.totalParticipantes);
  }

  ingresoPromedio(): number {
    if (!this.ejecutivo) {
      return 0;
    }

    return this.ejecutivo.totalInscripciones
      ? this.ejecutivo.ingresosGenerados / this.ejecutivo.totalInscripciones
      : 0;
  }
}
