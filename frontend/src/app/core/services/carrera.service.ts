import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { Carrera, CrearCarreraRequest, ActualizarCarreraRequest } from '../models/carrera.model';

@Injectable({
  providedIn: 'root'
})
export class CarreraService {
  private apiUrl = `${environment.apiUrl}/carreras`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Carrera[]> {
    return this.http.get<Carrera[]>(this.apiUrl);
  }

  obtenerPorId(id: string): Observable<Carrera> {
    return this.http.get<Carrera>(`${this.apiUrl}/${id}`);
  }

  crear(request: CrearCarreraRequest): Observable<Carrera> {
    return this.http.post<Carrera>(this.apiUrl, request);
  }

  actualizar(id: string, request: ActualizarCarreraRequest): Observable<Carrera> {
    return this.http.put<Carrera>(`${this.apiUrl}/${id}`, request);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
