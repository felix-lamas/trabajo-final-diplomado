import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.dev';
import { Pago, RegistrarPagoRequest, ValidarPagoRequest } from '../models/pago.model';

@Injectable({
  providedIn: 'root'
})
export class PagoService {
  private apiUrl = `${environment.apiUrl}/pagos`;

  constructor(private http: HttpClient) {}

  registrarPago(request: RegistrarPagoRequest): Observable<Pago> {
    return this.http.post<Pago>(this.apiUrl, request);
  }

  listarMisPagos(): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${this.apiUrl}/mis-pagos`);
  }

  listarPendientes(): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${this.apiUrl}/pendientes`);
  }

  listarTodos(): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.apiUrl);
  }

  obtenerPorId(id: string): Observable<Pago> {
    return this.http.get<Pago>(`${this.apiUrl}/${id}`);
  }

  validarPago(id: string, request: ValidarPagoRequest): Observable<Pago> {
    return this.http.patch<Pago>(`${this.apiUrl}/${id}/validar`, request);
  }

  rechazarPago(id: string, request: ValidarPagoRequest): Observable<Pago> {
    return this.http.patch<Pago>(`${this.apiUrl}/${id}/rechazar`, request);
  }

  subirComprobante(id: string, archivo: File): Observable<Pago> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<Pago>(`${this.apiUrl}/${id}/comprobante`, formData);
  }
}
