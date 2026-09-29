import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CategoriaEventoService } from './categoria-evento.service';

describe('CategoriaEventoService', () => {
  let service: CategoriaEventoService;
  let http: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/categorias-evento`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CategoriaEventoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista mediante el endpoint canonico', () => {
    service.listar().subscribe();
    expect(http.expectOne(baseUrl).request.method).toBe('GET');
  });

  it('lista activas mediante el endpoint canonico', () => {
    service.listarActivas().subscribe();
    expect(http.expectOne(`${baseUrl}/activas`).request.method).toBe('GET');
  });

  it('obtiene una categoria', () => {
    service.obtenerPorId('cat-1').subscribe();
    expect(http.expectOne(`${baseUrl}/cat-1`).request.method).toBe('GET');
  });

  it('crea una categoria', () => {
    service.crear({ nombre: 'Taller' }).subscribe();
    const request = http.expectOne(baseUrl);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ nombre: 'Taller' });
  });

  it('actualiza una categoria', () => {
    service.actualizar('cat-1', { nombre: 'Taller', estado: 'ACTIVO' }).subscribe();
    expect(http.expectOne(`${baseUrl}/cat-1`).request.method).toBe('PUT');
  });

  it('elimina una categoria', () => {
    service.eliminar('cat-1').subscribe();
    expect(http.expectOne(`${baseUrl}/cat-1`).request.method).toBe('DELETE');
  });
});
