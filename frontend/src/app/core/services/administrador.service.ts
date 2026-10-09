import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface Administrador {
  id: string;
  nombres: string;
  apellidos: string;
  correo: string;
  activo: boolean;
  fechaCreacion: string;
}
export interface InvitacionAdministrador {
  id: string;
  correo: string;
  nombres: string;
  apellidos: string;
  estado: string;
  fechaCreacion: string;
  fechaExpiracion: string;
  fechaAceptacion: string | null;
}
export interface DatosInvitacion {
  nombres: string;
  apellidos: string;
  correo: string;
  ci: string;
  celular: string;
}
export interface DatosActivacion {
  nombres: string;
  apellidos: string;
  ci: string;
  celular: string;
  contrasena: string;
  confirmacionContrasena: string;
}
@Injectable({ providedIn: 'root' })
export class AdministradorService {
  private readonly adminUrl = `${environment.apiUrl}/administradores`;
  private readonly activationUrl = `${environment.apiUrl}/auth/invitaciones-administrador`;
  constructor(private readonly http: HttpClient) {}
  listar() {
    return this.http.get<Administrador[]>(this.adminUrl);
  }
  invitaciones() {
    return this.http.get<InvitacionAdministrador[]>(`${this.adminUrl}/invitaciones`);
  }
  invitar(datos: DatosInvitacion) {
    return this.http.post<InvitacionAdministrador>(`${this.adminUrl}/invitaciones`, datos);
  }
  reenviar(id: string) {
    return this.http.post<InvitacionAdministrador>(
      `${this.adminUrl}/invitaciones/${id}/reenviar`,
      {},
    );
  }
  revocar(id: string) {
    return this.http.patch<InvitacionAdministrador>(
      `${this.adminUrl}/invitaciones/${id}/revocar`,
      {},
    );
  }
  desactivar(id: string) {
    return this.http.patch<void>(`${this.adminUrl}/${id}/desactivar`, {});
  }
  consultar(token: string) {
    return this.http.post<{ estado: string }>(`${this.activationUrl}/consultar`, { token });
  }
  aceptar(token: string, datos: DatosActivacion) {
    return this.http.post<void>(`${this.activationUrl}/aceptar`, { ...datos, token });
  }
}
