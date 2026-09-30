import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { InscripcionService } from './inscripcion.service';

describe('InscripcionService contrato canonico', () => {
  let service: InscripcionService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/inscripciones`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(InscripcionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('inscribe enviando solo eventoId', () => {
    service.inscribir({ eventoId: 'evento' }).subscribe();
    const request = http.expectOne(base);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ eventoId: 'evento' });
    expect(request.request.body.usuarioId).toBeUndefined();
  });

  it('lista exclusivamente por la ruta personal', () => {
    service.listarMisInscripciones().subscribe();
    expect(http.expectOne(`${base}/mis-inscripciones`).request.method).toBe('GET');
  });

  it('obtiene detalle canonico', () => {
    service.obtenerPorId('ins').subscribe();
    expect(http.expectOne(`${base}/ins`).request.method).toBe('GET');
  });

  it('cancela por PATCH sin campos controlados por cliente', () => {
    service.cancelar('ins').subscribe();
    const request = http.expectOne(`${base}/ins/cancelar`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({});
  });

  it('obtiene el comprobante por endpoint canonico', () => {
    service.obtenerComprobante('ins').subscribe();
    expect(http.expectOne(`${base}/ins/comprobante`).request.method).toBe('GET');
  });

  it('lista inscritos de un evento por endpoint de gestion', () => {
    service.listarInscritosEvento('evento').subscribe();
    expect(http.expectOne(`${base}/evento/evento`).request.method).toBe('GET');
  });
});
