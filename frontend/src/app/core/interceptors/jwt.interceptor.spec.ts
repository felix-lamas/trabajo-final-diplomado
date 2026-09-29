import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { jwtInterceptor } from './jwt.interceptor';

describe('jwtInterceptor', () => {
  let client: HttpClient;
  let http: HttpTestingController;
  const auth = {
    getToken: vi.fn(() => 'jwt-session'),
    clearLocalSession: vi.fn()
  };
  const router = { navigate: vi.fn(() => Promise.resolve(true)) };
  const snackBar = { open: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    });
    client = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('anade JWT solo a peticiones de la API canonica protegida', () => {
    client.get(`${environment.apiUrl}/usuarios/perfil`).subscribe();
    const request = http.expectOne(`${environment.apiUrl}/usuarios/perfil`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-session');
    request.flush({});

    client.get('https://example.test/recurso').subscribe();
    const external = http.expectOne('https://example.test/recurso');
    expect(external.request.headers.has('Authorization')).toBe(false);
    external.flush({});
  });

  it('no anade JWT a endpoints publicos de identidad', () => {
    client.post(`${environment.apiUrl}/auth/verificar-correo`, { token: 'token' }).subscribe();
    const request = http.expectOne(`${environment.apiUrl}/auth/verificar-correo`);
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush(null);
  });

  it('un 401 protegido limpia sesion, navega una sola vez y no reintenta', () => {
    client.get(`${environment.apiUrl}/usuarios/perfil`).subscribe({ error: () => undefined });
    const request = http.expectOne(`${environment.apiUrl}/usuarios/perfil`);
    request.flush({ codigo: 'AUTH_INVALID_SESSION' }, { status: 401, statusText: 'Unauthorized' });

    expect(auth.clearLocalSession).toHaveBeenCalledWith(true);
    expect(router.navigate).toHaveBeenCalledOnce();
    expect(snackBar.open).toHaveBeenCalledOnce();
    http.expectNone(`${environment.apiUrl}/usuarios/perfil`);
  });

  it('un 401 durante logout limpia sin loop ni navegacion duplicada', () => {
    client.post(`${environment.apiUrl}/auth/logout`, {}).subscribe({ error: () => undefined });
    const request = http.expectOne(`${environment.apiUrl}/auth/logout`);
    request.flush({ codigo: 'AUTH_INVALID_SESSION' }, { status: 401, statusText: 'Unauthorized' });

    expect(auth.clearLocalSession).toHaveBeenCalledWith(true);
    expect(router.navigate).not.toHaveBeenCalled();
    expect(snackBar.open).not.toHaveBeenCalled();
    http.expectNone(`${environment.apiUrl}/auth/logout`);
  });
});
