import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { Evento, CrearEventoRequest } from '../models/evento.model';

@Injectable({
  providedIn: 'root'
})
export class EventoService {
  private apiUrl = `${environment.apiUrl}/eventos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Evento[]> {
    return this.http.get<Evento[]>(this.apiUrl);
  }

  listarPublicados(): Observable<Evento[]> {
    return this.http.get<Evento[]>(`${this.apiUrl}/publicados`);
  }

  obtenerPorId(id: string): Observable<Evento> {
    return this.http.get<Evento>(`${this.apiUrl}/${id}`);
  }

  crear(request: CrearEventoRequest): Observable<Evento> {
    return this.http.post<Evento>(this.apiUrl, request);
  }

  actualizar(id: string, request: CrearEventoRequest): Observable<Evento> {
    return this.http.put<Evento>(`${this.apiUrl}/${id}`, request);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  publicar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/publicar`, {});
  }

  cancelar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/cancelar`, {});
  }

  finalizar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/finalizar`, {});
  }
}
