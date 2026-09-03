import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment.dev';

export interface LoginRequest {
  correoElectronico: string;
  contrasena: string;
}

export interface RegistroUsuarioRequest {
  correoElectronico: string;
  contrasena: string;
  nombres: string;
  apellidos: string;
  ci: string;
  celular: string;
  tipoUsuario: 'INTERNO' | 'EXTERNO';
  carreraId?: string;
}

export interface LoginResponse {
  token: string;
  usuario: {
    id: string;
    correoElectronico: string;
    nombres: string;
    apellidos: string;
    roles: string[];
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly tokenKey = 'token';
  private readonly userKey = 'usuario';
  private apiUrl = `${environment.apiUrl.replace(/\/v1$/, '')}/auth`;

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((response) => this.setSession(response))
    );
  }

  registro(request: RegistroUsuarioRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/registro`, request).pipe(
      tap((response) => this.setSession(response))
    );
  }

  recuperarContrasena(correoElectronico: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/recuperar-contrasena`, { correoElectronico });
  }

  restablecerContrasena(token: string, nuevaContrasena: string, confirmacion: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/restablecer-contrasena`, { token, nuevaContrasena, confirmacion });
  }

  resetearContrasena(token: string, nuevaContrasena: string, confirmacion: string): Observable<void> {
    return this.restablecerContrasena(token, nuevaContrasena, confirmacion);
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  getUser(): LoginResponse['usuario'] | null {
    const raw = localStorage.getItem(this.userKey);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as LoginResponse['usuario'];
    } catch {
      return null;
    }
  }

  getRoles(): string[] {
    return this.getUser()?.roles ?? [];
  }

  hasAnyRole(roles: readonly string[]): boolean {
    if (roles.length === 0) {
      return true;
    }

    const currentRoles = this.getRoles();
    return roles.some((role) => currentRoles.includes(role));
  }

  setSession(response: LoginResponse): void {
    localStorage.setItem(this.tokenKey, response.token);
    localStorage.setItem(this.userKey, JSON.stringify(response.usuario));
  }
}
