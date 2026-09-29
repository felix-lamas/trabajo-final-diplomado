import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AuthService, AuthUser, LoginResponse, RegistroUsuarioRequest } from './auth.service';

describe('AuthService identity contract', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const usuario: AuthUser = {
    id: '10000000-0000-0000-0000-000000000001',
    nombres: 'Ana',
    apellidos: 'Perez',
    correoElectronico: 'ana@example.test',
    correoVerificado: true,
    roles: ['USUARIO']
  };
  const token = createToken({ exp: Math.floor(Date.now() / 1000) + 3600, jti: 'session-1' });
  const loginResponse: LoginResponse = { token, usuario };
  const registro: RegistroUsuarioRequest = {
    nombres: 'Ana',
    apellidos: 'Perez',
    correoElectronico: 'ana@example.test',
    ci: '1234567',
    ru: 'RU-100',
    celular: '70000000',
    contrasena: 'Segura1!',
    confirmacionContrasena: 'Segura1!',
    tipoUsuario: 'INTERNO'
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('registra sin crear ni persistir una sesion JWT', () => {
    let correo = '';
    service.registro(registro).subscribe((response) => correo = response.correoElectronico);

    expect(service.loading()).toBe(true);
    const request = http.expectOne(`${environment.apiUrl}/auth/registro`);
    expect(request.request.body).toEqual(registro);
    request.flush({ correoElectronico: registro.correoElectronico, correoVerificado: false, mensaje: 'Verifique su correo' });

    expect(correo).toBe(registro.correoElectronico);
    expect(service.loading()).toBe(false);
    expect(service.autenticado()).toBe(false);
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('login exitoso persiste JWT y actualiza todos los Signals', () => {
    service.login({ correoElectronico: usuario.correoElectronico, contrasena: 'Segura1!' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(loginResponse);

    expect(service.token()).toBe(token);
    expect(service.usuarioActual()).toEqual(usuario);
    expect(service.autenticado()).toBe(true);
    expect(service.roles()).toEqual(['USUARIO']);
    expect(service.correoVerificado()).toBe(true);
    expect(localStorage.getItem('token')).toBe(token);
  });

  it('login rechazado por correo no verificado no crea sesion', () => {
    service.login({ correoElectronico: usuario.correoElectronico, contrasena: 'Segura1!' }).subscribe({ error: () => undefined });
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(
      { codigo: 'EMAIL_NOT_VERIFIED', mensaje: 'Correo no verificado' },
      { status: 400, statusText: 'Bad Request' }
    );

    expect(service.autenticado()).toBe(false);
    expect(service.getToken()).toBeNull();
  });

  it('consume exclusivamente los endpoints canonicos de verificacion, reenvio y recuperacion', () => {
    service.verificarCorreo('token-verificacion').subscribe();
    const verify = http.expectOne(`${environment.apiUrl}/auth/verificar-correo`);
    expect(verify.request.body).toEqual({ token: 'token-verificacion' });
    verify.flush(null);

    service.reenviarVerificacion(usuario.correoElectronico).subscribe();
    const resend = http.expectOne(`${environment.apiUrl}/auth/reenviar-verificacion`);
    expect(resend.request.body).toEqual({ correoElectronico: usuario.correoElectronico });
    resend.flush(null);

    service.recuperarContrasena(usuario.correoElectronico).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/recuperar-contrasena`).flush(null);

    service.restablecerContrasena('reset-token', 'Nueva1!', 'Nueva1!').subscribe();
    http.expectOne(`${environment.apiUrl}/auth/restablecer-contrasena`).flush(null);
  });

  it('logout revoca en backend y limpia almacenamiento y Signals', () => {
    service.setSession(loginResponse);
    service.logout().subscribe();
    const request = http.expectOne(`${environment.apiUrl}/auth/logout`);
    expect(request.request.method).toBe('POST');
    request.flush(null, { status: 204, statusText: 'No Content' });

    expect(service.autenticado()).toBe(false);
    expect(service.usuarioActual()).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('logout con 401 tambien limpia la sesion local', () => {
    service.setSession(loginResponse);
    service.logout().subscribe();
    http.expectOne(`${environment.apiUrl}/auth/logout`).flush(
      { codigo: 'AUTH_INVALID_SESSION' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(service.autenticado()).toBe(false);
    expect(service.getToken()).toBeNull();
  });

  it('refresca perfil y roles sin reemplazar el JWT', () => {
    service.setSession(loginResponse);
    const organizado = { ...usuario, roles: ['USUARIO', 'ORGANIZADOR'] };

    service.refrescarPerfil().subscribe();
    http.expectOne(`${environment.apiUrl}/usuarios/perfil`).flush(organizado);

    expect(service.roles()).toEqual(['USUARIO', 'ORGANIZADOR']);
    expect(service.token()).toBe(token);
  });

  it('actualiza perfil y publica inmediatamente el usuario recibido', () => {
    service.setSession(loginResponse);
    const actualizado = { ...usuario, nombres: 'Maria', apellidos: 'Rojas', celular: '71111111' };

    service.actualizarPerfil({ nombres: 'Maria', apellidos: 'Rojas', celular: '71111111' }).subscribe();
    const request = http.expectOne(`${environment.apiUrl}/usuarios/perfil`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ nombres: 'Maria', apellidos: 'Rojas', celular: '71111111' });
    request.flush(actualizado);

    expect(service.usuarioActual()).toEqual(actualizado);
    expect(JSON.parse(localStorage.getItem('usuario')!)).toEqual(actualizado);
  });

  it('cambio autenticado de contrasena limpia sesion despues del exito', () => {
    service.setSession(loginResponse);
    const body = {
      contrasenaActual: 'Actual9!',
      nuevaContrasena: 'NuevaClave9!',
      confirmacion: 'NuevaClave9!'
    };

    service.cambiarContrasena(body).subscribe();
    const request = http.expectOne(`${environment.apiUrl}/usuarios/cambiar-contrasena`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush(null);

    expect(service.autenticado()).toBe(false);
    expect(service.usuarioActual()).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });
});

function createToken(payload: object): string {
  const encode = (value: object) => btoa(JSON.stringify(value)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}
