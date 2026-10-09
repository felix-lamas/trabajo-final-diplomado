import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdministradorService } from './administrador.service';
import { environment } from '../../../environments/environment';
import { jwtInterceptor } from '../interceptors/jwt.interceptor';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('AdministradorService', () => {
  let service: AdministradorService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: { getToken: () => 'session-only', clearLocalSession: vi.fn() },
        },
        { provide: Router, useValue: { navigate: vi.fn() } },
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
      ],
    });
    service = TestBed.inject(AdministradorService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('env?a invitaci?n administrativa autenticada sin permitir roles', () => {
    const datos = {
      nombres: 'E2E',
      apellidos: 'Admin',
      correo: 'e2e@example.test',
      ci: 'E2E-01',
      celular: '00000000',
    };
    service.invitar(datos).subscribe();
    const r = http.expectOne(`${environment.apiUrl}/administradores/invitaciones`);
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(datos);
    expect(r.request.headers.get('Authorization')).toBe('Bearer session-only');
    r.flush({ estado: 'PENDIENTE' });
  });
  it('consulta token solo en body, sin Authorization ni URL con token', () => {
    service.consultar('secret-test').subscribe();
    const r = http.expectOne(`${environment.apiUrl}/auth/invitaciones-administrador/consultar`);
    expect(r.request.body).toEqual({ token: 'secret-test' });
    expect(r.request.headers.has('Authorization')).toBe(false);
    r.flush({ estado: 'PENDIENTE' });
  });
  it('activa sin guardar token o crear sesi?n autom?ticamente', () => {
    const datos = {
      nombres: 'E2E',
      apellidos: 'Admin',
      ci: 'E2E-01',
      celular: '00000000',
      contrasena: 'TestOnly9!',
      confirmacionContrasena: 'TestOnly9!',
    };
    service.aceptar('secret-test', datos).subscribe();
    const r = http.expectOne(`${environment.apiUrl}/auth/invitaciones-administrador/aceptar`);
    expect(r.request.body).toEqual({ ...datos, token: 'secret-test' });
    expect(r.request.headers.has('Authorization')).toBe(false);
    r.flush(null, { status: 204, statusText: 'No Content' });
  });
  it.each(['reenviar', 'revocar', 'desactivar'] as const)(
    'env?a acci?n administrativa %s',
    (accion) => {
      const result: import('rxjs').Observable<unknown> = service[accion]('id-test');
      result.subscribe();
      const path =
        accion === 'desactivar' ? 'id-test/desactivar' : `invitaciones/id-test/${accion}`;
      const r = http.expectOne(`${environment.apiUrl}/administradores/${path}`);
      expect(r.request.headers.has('Authorization')).toBe(true);
      r.flush(null);
    },
  );
});
