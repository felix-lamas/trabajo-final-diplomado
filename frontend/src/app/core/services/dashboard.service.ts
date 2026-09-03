import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import {
  DashboardEjecutivoResponse,
  DashboardAcademicoResponse,
  DashboardOperativoResponse,
  ReporteDataResponse
} from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = environment.apiUrl.replace(/\/v1$/, '');

  constructor(private http: HttpClient) {}

  obtenerEjecutivo(): Observable<DashboardEjecutivoResponse> {
    return this.http.get<DashboardEjecutivoResponse>(`${this.apiUrl}/dashboard/ejecutivo`);
  }

  obtenerAcademico(): Observable<DashboardAcademicoResponse> {
    return this.http.get<DashboardAcademicoResponse>(`${this.apiUrl}/dashboard/academico`);
  }

  obtenerOperativo(): Observable<DashboardOperativoResponse> {
    return this.http.get<DashboardOperativoResponse>(`${this.apiUrl}/dashboard/operativo`);
  }

  obtenerReporte(tipo: 'eventos' | 'participantes' | 'pagos' | 'certificados'): Observable<ReporteDataResponse> {
    return this.http.get<ReporteDataResponse>(`${this.apiUrl}/reportes/${tipo}`);
  }

  obtenerSatisfaccionEvento(eventoId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/encuestas/estadisticas/${eventoId}`);
  }

  exportarPdf(tipo: string): string {
    return `${this.apiUrl}/reportes/exportar/pdf?tipo=${tipo}`;
  }

  exportarExcel(tipo: string): string {
    return `${this.apiUrl}/reportes/exportar/excel?tipo=${tipo}`;
  }
}
