import { Component, OnInit } from '@angular/core';

// Service & Model
import { DashboardService } from '../../../core/services/dashboard.service';
import { ReporteDataResponse } from '../../../core/models/dashboard.model';
import { ToastService } from '../../../shared/ui/toast.service';

@Component({
  selector: 'app-reportes-panel',
  templateUrl: './reportes-panel.component.html',
  styleUrls: [],
  standalone: false
})
export class ReportesPanelComponent implements OnInit {
  reporte: ReporteDataResponse | null = null;
  tipoActual: 'eventos' | 'participantes' | 'pagos' | 'certificados' = 'eventos';
  loading = false;
  columnas: string[] = [];

  constructor(
    private dashboardService: DashboardService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarReporte('eventos');
  }

  cargarReporte(tipo: 'eventos' | 'participantes' | 'pagos' | 'certificados'): void {
    this.loading = true;
    this.tipoActual = tipo;
    this.reporte = null;

    this.dashboardService.obtenerReporte(tipo).subscribe({
      next: (data) => {
        this.reporte = data;
        if (data.filas.length > 0) {
          this.columnas = Object.keys(data.filas[0]);
        }
        this.loading = false;
        this.toast.success(`Reporte ${tipo.toUpperCase()} cargado`);
      },
      error: () => {
        this.toast.error('Error al generar reporte de datos');
        this.loading = false;
      }
    });
  }

  exportar(formato: 'pdf' | 'excel'): void {
    const url = formato === 'pdf' 
      ? this.dashboardService.exportarPdf(this.tipoActual)
      : this.dashboardService.exportarExcel(this.tipoActual);
    
    window.open(url, '_blank');
    this.toast.info(`Iniciando descarga institucional de ${formato.toUpperCase()}...`, 'Cerrar', 2000);
  }
}
