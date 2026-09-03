import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { CertificadoResponse, VerificacionCertificadoResponse } from '../models/certificado.model';

@Injectable({
  providedIn: 'root'
})
export class CertificadoService {
  private apiUrl = `${environment.apiUrl}/certificados`;
  private publicUrl = `${environment.apiUrl}/verificacion-certificados`;

  constructor(private http: HttpClient) {}

  generar(inscripcionId: string): Observable<CertificadoResponse> {
    return this.http.post<CertificadoResponse>(`${this.apiUrl}/generar/${inscripcionId}`, {});
  }

  obtenerPorId(id: string): Observable<CertificadoResponse> {
    return this.http.get<CertificadoResponse>(`${this.apiUrl}/${id}`);
  }

  listarMisCertificados(): Observable<CertificadoResponse[]> {
    return this.http.get<CertificadoResponse[]>(`${this.apiUrl}/mis-certificados`);
  }

  descargar(id: string): Observable<CertificadoResponse> {
    return this.http.get<CertificadoResponse>(`${this.apiUrl}/${id}/descargar`);
  }

  verificarPublicamente(codigo: string): Observable<VerificacionCertificadoResponse> {
    return this.http.get<VerificacionCertificadoResponse>(`${this.publicUrl}/${codigo}`);
  }
}
