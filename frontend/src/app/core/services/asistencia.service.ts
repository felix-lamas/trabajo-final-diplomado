import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { QrAsistencia, SesionEvento, SesionEventoRequest } from '../models/asistencia.model';

@Injectable({ providedIn: 'root' })
export class AsistenciaService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  listarSesiones(eventoId: string): Observable<SesionEvento[]> {
    return this.http.get<SesionEvento[]>(`${this.apiUrl}/eventos/${eventoId}/sesiones`);
  }

  crearSesion(eventoId: string, request: SesionEventoRequest): Observable<SesionEvento> {
    return this.http.post<SesionEvento>(`${this.apiUrl}/eventos/${eventoId}/sesiones`, request);
  }

  actualizarSesion(sesionId: string, request: SesionEventoRequest): Observable<SesionEvento> {
    return this.http.put<SesionEvento>(`${this.apiUrl}/sesiones/${sesionId}`, request);
  }

  cambiarEstado(sesionId: string, activa: boolean): Observable<SesionEvento> {
    return this.http.patch<SesionEvento>(`${this.apiUrl}/sesiones/${sesionId}/estado`, { activa });
  }

  obtenerQrActivo(sesionId: string): Observable<QrAsistencia> {
    return this.http.get<QrAsistencia>(`${this.apiUrl}/sesiones/${sesionId}/qr`);
  }

  generarQr(sesionId: string): Observable<QrAsistencia> {
    return this.http.post<QrAsistencia>(`${this.apiUrl}/sesiones/${sesionId}/qr/generar`, {});
  }
}
