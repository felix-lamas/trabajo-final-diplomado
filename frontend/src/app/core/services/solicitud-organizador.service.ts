import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type EstadoSolicitudOrganizador = 'NINGUNA' | 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';

export interface SolicitudOrganizador {
  usuarioId: string;
  nombres: string;
  apellidos: string;
  correoElectronico: string;
  estado: EstadoSolicitudOrganizador;
  fechaSolicitud: string;
  fechaResolucion?: string | null;
  motivoRechazo?: string | null;
  resueltaPorId?: string | null;
}

@Injectable({ providedIn: 'root' })
export class SolicitudOrganizadorService {
  private readonly apiUrl = `${environment.apiUrl}/usuarios`;

  constructor(private readonly http: HttpClient) {}

  solicitar(): Observable<SolicitudOrganizador> {
    return this.http.post<SolicitudOrganizador>(`${this.apiUrl}/solicitud-organizador`, {});
  }

  listar(estado: EstadoSolicitudOrganizador): Observable<SolicitudOrganizador[]> {
    return this.http.get<SolicitudOrganizador[]>(`${this.apiUrl}/solicitudes-organizador`, {
      params: new HttpParams().set('estado', estado)
    });
  }

  aprobar(usuarioId: string): Observable<SolicitudOrganizador> {
    return this.http.patch<SolicitudOrganizador>(
      `${this.apiUrl}/solicitudes-organizador/${usuarioId}/aprobar`,
      {}
    );
  }

  rechazar(usuarioId: string, motivo: string): Observable<SolicitudOrganizador> {
    return this.http.patch<SolicitudOrganizador>(
      `${this.apiUrl}/solicitudes-organizador/${usuarioId}/rechazar`,
      { motivo }
    );
  }
}
