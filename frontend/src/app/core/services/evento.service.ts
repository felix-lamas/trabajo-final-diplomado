import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Evento, EventoDetalle, CrearEventoRequest } from '../models/evento.model';

@Injectable({
  providedIn: 'root'
})
export class EventoService {
  private apiUrl = `${environment.apiUrl}/eventos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Evento[]> {
    return this.http.get<Evento[]>(this.apiUrl);
  }

  listarEnRevision(): Observable<Evento[]> {
    return this.http.get<Evento[]>(`${this.apiUrl}/revision`);
  }

  listarPublicados(): Observable<Evento[]> {
    return this.http.get<Evento[]>(`${this.apiUrl}/publicados`);
  }

  obtenerPorId(id: string): Observable<EventoDetalle> {
    return this.http.get<EventoDetalle>(`${this.apiUrl}/${id}`);
  }

  crear(request: CrearEventoRequest): Observable<EventoDetalle> {
    return this.http.post<EventoDetalle>(this.apiUrl, request);
  }

  actualizar(id: string, request: CrearEventoRequest): Observable<EventoDetalle> {
    return this.http.put<EventoDetalle>(`${this.apiUrl}/${id}`, request);
  }

  subirQrPago(id: string, archivo: File): Observable<void> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.put<void>(`${this.apiUrl}/${id}/qr-pago`, formData);
  }

  eliminarQrPago(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/qr-pago`);
  }

  descargarQrPago(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/qr-pago`, { responseType: 'blob' });
  }

  normalizarQrPagoUrl(url: string): string {
    return /^https?:\/\//i.test(url) ? url : new URL(url, environment.apiUrl).toString();
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  publicar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/publicar`, {});
  }

  enviarARevision(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/enviar-revision`, {});
  }

  rechazar(id: string, motivo: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/rechazar`, { motivo });
  }

  volverABorrador(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/volver-borrador`, {});
  }

  cancelar(id: string, motivo: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/cancelar`, { motivo });
  }

  finalizar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/finalizar`, {});
  }
}
