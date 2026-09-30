import { Component, OnInit, signal } from '@angular/core';

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
  private readonly viewState = signal<{
    reporte: ReporteDataResponse | null;
    loading: boolean;
    columnas: string[];
  }>({ reporte: null, loading: false, columnas: [] });
  get reporte(): ReporteDataResponse | null { return this.viewState().reporte; }
  private set reporte(value: ReporteDataResponse | null) { this.viewState.update((state) => ({ ...state, reporte: value })); }
  tipoActual: 'eventos' | 'participantes' | 'pagos' | 'certificados' = 'eventos';
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
  get columnas(): string[] { return this.viewState().columnas; }
  private set columnas(value: string[]) { this.viewState.update((state) => ({ ...state, columnas: value })); }

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
    const descarga = formato === 'pdf'
      ? this.dashboardService.exportarPdf(this.tipoActual)
      : this.dashboardService.exportarExcel(this.tipoActual);

    descarga.subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `reporte_${this.tipoActual}.${formato === 'excel' ? 'xlsx' : 'pdf'}`;
        document.body.appendChild(enlace);
        enlace.click();
        enlace.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
        this.toast.info(`Descarga de ${formato.toUpperCase()} iniciada`, 'Cerrar', 2000);
      },
      error: () => this.toast.error(`No se pudo descargar el reporte ${formato.toUpperCase()}`)
    });
  }
}
