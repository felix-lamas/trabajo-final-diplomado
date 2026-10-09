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
  fechaSolicitud: string | null;
  fechaResolucion?: string | null;
  motivoRechazo?: string | null;
  resueltaPorId?: string | null;
  motivoSolicitud?: string | null;
  tiposEventos?: string[];
  nombresTiposEventos?: string[];
  informacionAdicional?: string | null;
  puedeSolicitar?: boolean;
}

export interface TipoEventoSolicitud {
  codigo: string;
  nombre: string;
}
export interface SolicitarOrganizadorRequest {
  motivoSolicitud: string;
  tiposEventos: string[];
  informacionAdicional?: string;
}

@Injectable({ providedIn: 'root' })
export class SolicitudOrganizadorService {
  private readonly apiUrl = `${environment.apiUrl}/usuarios`;

  constructor(private readonly http: HttpClient) {}

  solicitar(request: SolicitarOrganizadorRequest): Observable<SolicitudOrganizador> {
    return this.http.post<SolicitudOrganizador>(`${this.apiUrl}/solicitud-organizador`, request);
  }

  obtenerMiSolicitud(): Observable<SolicitudOrganizador> {
    return this.http.get<SolicitudOrganizador>(`${this.apiUrl}/solicitud-organizador`);
  }

  tiposEventos(): Observable<TipoEventoSolicitud[]> {
    return this.http.get<TipoEventoSolicitud[]>(
      `${this.apiUrl}/solicitud-organizador/tipos-eventos`
    );
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
