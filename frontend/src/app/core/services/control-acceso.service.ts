import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { ValidarQrResponse, ControlAccesoResponse, AsistenciaResponse } from '../models/control-acceso.model';

@Injectable({
  providedIn: 'root'
})
export class ControlAccesoService {
  private apiUrl = `${environment.apiUrl}/control-acceso`;
  private asistenciaUrl = `${environment.apiUrl}/asistencias`;

  constructor(private http: HttpClient) {}

  validarQr(tokenQr: string): Observable<ValidarQrResponse> {
    return this.http.post<ValidarQrResponse>(`${this.apiUrl}/validar-qr`, { tokenQr });
  }

  autorizar(credencialId: string, observacion: string): Observable<ControlAccesoResponse> {
    return this.http.post<ControlAccesoResponse>(`${this.apiUrl}/autorizar`, { credencialId, observacion });
  }

  denegar(credencialId: string, observacion: string): Observable<ControlAccesoResponse> {
    return this.http.post<ControlAccesoResponse>(`${this.apiUrl}/denegar`, { credencialId, observacion });
  }

  obtenerHistorial(eventoId: string): Observable<ControlAccesoResponse[]> {
    return this.http.get<ControlAccesoResponse[]>(`${this.apiUrl}/historial/${eventoId}`);
  }

  buscarPorCodigo(codigoParticipante: string): Observable<ValidarQrResponse> {
    return this.http.get<ValidarQrResponse>(`${this.apiUrl}/codigo/${codigoParticipante}`);
  }

  buscarPorDocumento(documento: string, eventoId: string): Observable<ValidarQrResponse> {
    return this.http.get<ValidarQrResponse>(`${this.apiUrl}/documento/${documento}?eventoId=${eventoId}`);
  }

  listarAsistenciasPorEvento(eventoId: string): Observable<AsistenciaResponse[]> {
    return this.http.get<AsistenciaResponse[]>(`${this.asistenciaUrl}/evento/${eventoId}`);
  }
}
