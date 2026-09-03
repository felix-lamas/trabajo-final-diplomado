import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { CategoriaEvento, CrearCategoriaEventoRequest, ActualizarCategoriaEventoRequest } from '../models/categoria-evento.model';

@Injectable({
  providedIn: 'root'
})
export class CategoriaEventoService {
  private apiUrl = `${environment.apiUrl}/categorias-evento`;

  constructor(private http: HttpClient) {}

  listar(): Observable<CategoriaEvento[]> {
    return this.http.get<CategoriaEvento[]>(this.apiUrl);
  }

  listarActivas(): Observable<CategoriaEvento[]> {
    return this.http.get<CategoriaEvento[]>(`${this.apiUrl}/activas`);
  }

  obtenerPorId(id: string): Observable<CategoriaEvento> {
    return this.http.get<CategoriaEvento>(`${this.apiUrl}/${id}`);
  }

  crear(request: CrearCategoriaEventoRequest): Observable<CategoriaEvento> {
    return this.http.post<CategoriaEvento>(this.apiUrl, request);
  }

  actualizar(id: string, request: ActualizarCategoriaEventoRequest): Observable<CategoriaEvento> {
    return this.http.put<CategoriaEvento>(`${this.apiUrl}/${id}`, request);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
