import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { Facultad, CrearFacultadRequest, ActualizarFacultadRequest } from '../models/facultad.model';
import { Carrera } from '../models/carrera.model';

@Injectable({
  providedIn: 'root'
})
export class FacultadService {
  private apiUrl = `${environment.apiUrl}/facultades`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Facultad[]> {
    return this.http.get<Facultad[]>(this.apiUrl);
  }

  obtenerPorId(id: string): Observable<Facultad> {
    return this.http.get<Facultad>(`${this.apiUrl}/${id}`);
  }

  crear(request: CrearFacultadRequest): Observable<Facultad> {
    return this.http.post<Facultad>(this.apiUrl, request);
  }

  actualizar(id: string, request: ActualizarFacultadRequest): Observable<Facultad> {
    return this.http.put<Facultad>(`${this.apiUrl}/${id}`, request);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  listarCarreras(id: string): Observable<Carrera[]> {
    return this.http.get<Carrera[]>(`${this.apiUrl}/${id}/carreras`);
  }
}
