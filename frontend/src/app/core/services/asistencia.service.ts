import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AsistenciaResponse, QrAsistencia, SesionEvento, SesionEventoRequest } from '../models/asistencia.model';

@Injectable({ providedIn: 'root' })
export class AsistenciaService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  listarPorEvento(eventoId: string): Observable<AsistenciaResponse[]> {
    return this.http.get<AsistenciaResponse[]>(`${this.apiUrl}/asistencias/evento/${eventoId}`);
  }

  listarSesiones(eventoId: string): Observable<SesionEvento[]> {
    return this.http.get<SesionEvento[]>(`${this.apiUrl}/eventos/${eventoId}/sesiones`);
  }

  crearSesion(eventoId: string, request: SesionEventoRequest): Observable<SesionEvento> {
    return this.http.post<SesionEvento>(`${this.apiUrl}/eventos/${eventoId}/sesiones`, request);
  }

  actualizarSesion(sesionId: string, request: SesionEventoRequest): Observable<SesionEvento> {
    return this.http.put<SesionEvento>(`${this.apiUrl}/sesiones/${sesionId}`, request);
  }

  resolverEnlaceGoogleMaps(url: string): Observable<{ latitud: number; longitud: number }> {
    return this.http.post<{ latitud: number; longitud: number }>(`${this.apiUrl}/maps/coordenadas-compartidas`, { url });
  }

  cambiarEstado(sesionId: string, activa: boolean): Observable<SesionEvento> {
    return this.http.patch<SesionEvento>(`${this.apiUrl}/sesiones/${sesionId}/estado`, { activa });
  }

  obtenerQrActivo(sesionId: string): Observable<QrAsistencia> {
    // El QR cambia sin cambiar la URL. Evita que el navegador o un proxy
    // reutilice una respuesta anterior durante el sondeo del organizador.
    return this.http.get<QrAsistencia>(`${this.apiUrl}/sesiones/${sesionId}/qr`, {
      params: new HttpParams().set('_', Date.now().toString())
    });
  }

  generarQr(sesionId: string): Observable<QrAsistencia> {
    return this.http.post<QrAsistencia>(`${this.apiUrl}/sesiones/${sesionId}/qr/generar`, {});
  }

  detenerControlQr(sesionId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/sesiones/${sesionId}/qr/detener`, {});
  }
}
