import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CertificadoResponse, VerificacionCertificadoResponse } from '../models/certificado.model';

@Injectable({
  providedIn: 'root'
})
export class CertificadoService {
  private apiUrl = `${environment.apiUrl}/certificados`;
  private publicUrl = `${environment.apiUrl}/certificados/verificar`;

  constructor(private http: HttpClient) {}

  generar(inscripcionId: string): Observable<CertificadoResponse> {
    return this.http.post<CertificadoResponse>(`${this.apiUrl}/generar/${inscripcionId}`, null);
  }

  obtenerPorId(id: string): Observable<CertificadoResponse> {
    return this.http.get<CertificadoResponse>(`${this.apiUrl}/${id}`);
  }

  listarMisCertificados(): Observable<CertificadoResponse[]> {
    return this.http.get<CertificadoResponse[]>(`${this.apiUrl}/mis-certificados`);
  }

  listarPorEvento(eventoId: string): Observable<CertificadoResponse[]> {
    return this.http.get<CertificadoResponse[]>(`${environment.apiUrl}/eventos/${eventoId}/certificados`);
  }

  descargar(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/descargar`, { responseType: 'blob' });
  }

  verificarPublicamente(codigo: string): Observable<VerificacionCertificadoResponse> {
    return this.http.get<VerificacionCertificadoResponse>(`${this.publicUrl}/${encodeURIComponent(codigo)}`);
  }
}
