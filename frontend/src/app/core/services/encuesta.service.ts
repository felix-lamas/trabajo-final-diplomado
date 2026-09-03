import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { EncuestaResponse, EstadisticasEncuestaResponse, ResponderEncuestaRequest } from '../models/encuesta.model';

@Injectable({
  providedIn: 'root'
})
export class EncuestaService {
  private apiUrl = `${environment.apiUrl.replace(/\/v1$/, '')}/encuestas`;

  constructor(private http: HttpClient) {}

  responder(request: ResponderEncuestaRequest): Observable<EncuestaResponse> {
    return this.http.post<EncuestaResponse>(`${this.apiUrl}/responder`, request);
  }

  obtenerPorEvento(eventoId: string): Observable<EncuestaResponse[]> {
    return this.http.get<EncuestaResponse[]>(`${this.apiUrl}/evento/${eventoId}`);
  }

  obtenerEstadisticas(eventoId: string): Observable<EstadisticasEncuestaResponse> {
    return this.http.get<EstadisticasEncuestaResponse>(`${this.apiUrl}/estadisticas/${eventoId}`);
  }
}
