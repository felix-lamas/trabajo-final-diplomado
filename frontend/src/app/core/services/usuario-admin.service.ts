import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { UsuarioAdmin } from '../models/usuario-admin.model';

@Injectable({ providedIn: 'root' })
export class UsuarioAdminService {
  private readonly apiUrl = `${environment.apiUrl}/usuarios`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<UsuarioAdmin[]> {
    return this.http.get<UsuarioAdmin[]>(this.apiUrl);
  }

  obtenerPorId(id: string): Observable<UsuarioAdmin> {
    return this.http.get<UsuarioAdmin>(`${this.apiUrl}/${id}`);
  }
}
