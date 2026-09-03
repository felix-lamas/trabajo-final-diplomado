import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { Credencial, CodigoQr } from '../models/credencial.model';

@Injectable({
  providedIn: 'root'
})
export class CredencialService {
  private apiUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  generarCredencial(inscripcionId: string): Observable<Credencial> {
    return this.http.post<Credencial>(`${this.apiUrl}/credenciales/generar/${inscripcionId}`, {});
  }

  obtenerPorId(id: string): Observable<Credencial> {
    return this.http.get<Credencial>(`${this.apiUrl}/credenciales/${id}`);
  }

  listarMisCredenciales(): Observable<Credencial[]> {
    return this.http.get<Credencial[]>(`${this.apiUrl}/usuarios/mis-credenciales`);
  }

  obtenerQr(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/credenciales/${id}/qr`, { responseType: 'blob' });
  }

  descargarPdf(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/credenciales/${id}/descargar`, { responseType: 'blob' });
  }

  validarQr(contenido: string): Observable<CodigoQr> {
    return this.http.get<CodigoQr>(`${this.apiUrl}/codigos-qr/validar/${contenido}`);
  }
}
