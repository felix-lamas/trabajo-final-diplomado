import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, catchError, defer, finalize, of, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginRequest {
  correoElectronico: string;
  contrasena: string;
}

export interface RegistroUsuarioRequest {
  nombres: string;
  apellidos: string;
  correoElectronico: string;
  ci: string;
  ru: string | null;
  celular: string;
  contrasena: string;
  confirmacionContrasena: string;
  tipoUsuario: 'INTERNO' | 'EXTERNO';
}

export interface RegistroResponse {
  correoElectronico: string;
  correoVerificado: boolean;
  mensaje: string;
}

export interface AuthUser {
  id: string;
  correoElectronico: string;
  correoVerificado: boolean;
  nombres: string;
  apellidos: string;
  ci?: string | null;
  ru?: string | null;
  celular?: string | null;
  tipoUsuario?: string | null;
  estadoSolicitudOrganizador?: string | null;
  roles: string[];
}

export interface LoginResponse {
  token: string;
  usuario: AuthUser;
}

export interface ActualizarPerfilRequest {
  nombres: string;
  apellidos: string;
  celular: string;
}

export interface CambioContrasenaRequest {
  contrasenaActual: string;
  nuevaContrasena: string;
  confirmacion: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'token';
  private readonly userKey = 'usuario';
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly tokenState = signal<string | null>(null);
  private readonly userState = signal<AuthUser | null>(null);
  private readonly pendingRequestsState = signal(0);
  private readonly sessionRevokedState = signal(false);

  readonly token = this.tokenState.asReadonly();
  readonly usuarioActual = this.userState.asReadonly();
  readonly autenticado = computed(() => this.tokenState() !== null && this.userState() !== null);
  readonly roles = computed(() => this.userState()?.roles ?? []);
  readonly loading = computed(() => this.pendingRequestsState() > 0);
  readonly sesionRevocada = this.sessionRevokedState.asReadonly();
  readonly correoVerificado = computed(() => this.userState()?.correoVerificado ?? false);

  constructor(private readonly http: HttpClient) {
    this.restoreSession();
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.trackRequest(
      this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
        tap((response) => this.setSession(response))
      )
    );
  }

  registro(request: RegistroUsuarioRequest): Observable<RegistroResponse> {
    return this.trackRequest(this.http.post<RegistroResponse>(`${this.apiUrl}/registro`, request));
  }

  verificarCorreo(token: string): Observable<void> {
    return this.trackRequest(this.http.post<void>(`${this.apiUrl}/verificar-correo`, { token }));
  }

  reenviarVerificacion(correoElectronico: string): Observable<void> {
    return this.trackRequest(
      this.http.post<void>(`${this.apiUrl}/reenviar-verificacion`, { correoElectronico })
    );
  }

  recuperarContrasena(correoElectronico: string): Observable<void> {
    return this.trackRequest(
      this.http.post<void>(`${this.apiUrl}/recuperar-contrasena`, { correoElectronico })
    );
  }

  restablecerContrasena(token: string, nuevaContrasena: string, confirmacion: string): Observable<void> {
    return this.trackRequest(
      this.http.post<void>(`${this.apiUrl}/restablecer-contrasena`, {
        token,
        nuevaContrasena,
        confirmacion
      }).pipe(tap(() => this.clearLocalSession()))
    );
  }

  logout(): Observable<void> {
    if (!this.tokenState()) {
      this.clearLocalSession();
      return of(void 0);
    }

    return this.trackRequest(
      this.http.post<void>(`${this.apiUrl}/logout`, {}).pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 401) {
            return of(void 0);
          }
          return throwError(() => error);
        }),
        finalize(() => this.clearLocalSession())
      )
    );
  }

  refrescarPerfil(): Observable<AuthUser> {
    return this.trackRequest(
      this.http.get<AuthUser>(`${environment.apiUrl}/usuarios/perfil`).pipe(
        tap((usuario) => {
          this.userState.set(usuario);
          this.storage?.setItem(this.userKey, JSON.stringify(usuario));
        })
      )
    );
  }

  actualizarPerfil(request: ActualizarPerfilRequest): Observable<AuthUser> {
    return this.trackRequest(
      this.http.put<AuthUser>(`${environment.apiUrl}/usuarios/perfil`, request).pipe(
        tap((usuario) => {
          this.userState.set(usuario);
          this.storage?.setItem(this.userKey, JSON.stringify(usuario));
        })
      )
    );
  }

  cambiarContrasena(request: CambioContrasenaRequest): Observable<void> {
    return this.trackRequest(
      this.http.post<void>(`${environment.apiUrl}/usuarios/cambiar-contrasena`, request).pipe(
        tap(() => this.clearLocalSession())
      )
    );
  }

  getToken(): string | null {
    return this.tokenState();
  }

  isAuthenticated(): boolean {
    const token = this.tokenState();
    if (!token || !this.userState() || !this.isTokenCurrent(token)) {
      if (token) {
        this.clearLocalSession();
      }
      return false;
    }
    return true;
  }

  getUser(): AuthUser | null {
    return this.userState();
  }

  getRoles(): string[] {
    return this.roles();
  }

  hasAnyRole(roles: readonly string[]): boolean {
    return roles.length === 0 || roles.some((role) => this.roles().includes(role));
  }

  setSession(response: LoginResponse): void {
    this.tokenState.set(response.token);
    this.userState.set(response.usuario);
    this.sessionRevokedState.set(false);
    this.storage?.setItem(this.tokenKey, response.token);
    this.storage?.setItem(this.userKey, JSON.stringify(response.usuario));
  }

  clearLocalSession(sessionRevoked = false): void {
    this.tokenState.set(null);
    this.userState.set(null);
    this.sessionRevokedState.set(sessionRevoked);
    this.storage?.removeItem(this.tokenKey);
    this.storage?.removeItem(this.userKey);
  }

  private restoreSession(): void {
    const token = this.storage?.getItem(this.tokenKey) ?? null;
    const rawUser = this.storage?.getItem(this.userKey) ?? null;
    if (!token || !rawUser || !this.isTokenCurrent(token)) {
      this.clearLocalSession();
      return;
    }

    try {
      this.tokenState.set(token);
      this.userState.set(JSON.parse(rawUser) as AuthUser);
    } catch {
      this.clearLocalSession();
    }
  }

  private isTokenCurrent(token: string): boolean {
    try {
      const encodedPayload = token.split('.')[1];
      if (!encodedPayload) {
        return false;
      }
      const normalized = encodedPayload.replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(normalized)) as { exp?: number };
      return Boolean(payload.exp && payload.exp * 1000 > Date.now());
    } catch {
      return false;
    }
  }

  private trackRequest<T>(request: Observable<T>): Observable<T> {
    return defer(() => {
      this.pendingRequestsState.update((pending) => pending + 1);
      return request.pipe(
        finalize(() => this.pendingRequestsState.update((pending) => Math.max(0, pending - 1)))
      );
    });
  }

  private get storage(): Storage | null {
    return typeof localStorage === 'undefined' ? null : localStorage;
  }
}
