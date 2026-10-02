import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { UsuarioAdminService } from './usuario-admin.service';

describe('UsuarioAdminService', () => {
  let service: UsuarioAdminService;
  let http: HttpTestingController;
  const endpoint = `${environment.apiUrl}/usuarios`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(UsuarioAdminService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista usuarios mediante el endpoint administrativo de solo lectura', () => {
    service.listar().subscribe();
    const request = http.expectOne(endpoint);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('obtiene usuario por id mediante el endpoint documentado', () => {
    service.obtenerPorId('usuario-1').subscribe();
    const request = http.expectOne(`${endpoint}/usuario-1`);
    expect(request.request.method).toBe('GET');
    request.flush({ id: 'usuario-1' });
  });
});
