import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
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
  private apiUrl = environment.apiUrl;

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

  exportarPdf(tipo: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/reportes/exportar/pdf`, {
      params: { tipo },
      responseType: 'blob'
    });
  }

  exportarExcel(tipo: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/reportes/exportar/excel`, {
      params: { tipo },
      responseType: 'blob'
    });
  }
}
