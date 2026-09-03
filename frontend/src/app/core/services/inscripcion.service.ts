import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { Inscripcion, DetalleInscripcion, CrearInscripcionRequest } from '../models/inscripcion.model';

@Injectable({
  providedIn: 'root'
})
export class InscripcionService {
  private apiUrl = `${environment.apiUrl}/inscripciones`;

  constructor(private http: HttpClient) {}

  inscribir(request: CrearInscripcionRequest): Observable<DetalleInscripcion> {
    return this.http.post<DetalleInscripcion>(this.apiUrl, request);
  }

  listarMisInscripciones(): Observable<Inscripcion[]> {
    return this.http.get<Inscripcion[]>(`${this.apiUrl}/mis-inscripciones`);
  }

  obtenerPorId(id: string): Observable<DetalleInscripcion> {
    return this.http.get<DetalleInscripcion>(`${this.apiUrl}/${id}`);
  }

  cancelar(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/cancelar`, {});
  }

  listarInscritosEvento(eventoId: string): Observable<Inscripcion[]> {
    return this.http.get<Inscripcion[]>(`${this.apiUrl}/evento/${eventoId}`);
  }
}
