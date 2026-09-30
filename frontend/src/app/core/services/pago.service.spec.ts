import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { PagoService } from './pago.service';

describe('PagoService contrato canónico', () => {
  let service: PagoService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/pagos`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(PagoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('registra compatibilidad histórica sin aceptar monto, usuario, evento ni estado', () => {
    service.registrarPago({ inscripcionId: 'inscripcion' }).subscribe();
    const request = http.expectOne(base);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ inscripcionId: 'inscripcion' });
  });

  it('presenta el comprobante como multipart sobre el pago', () => {
    const archivo = new File(['contenido'], 'comprobante.pdf', { type: 'application/pdf' });
    service.subirComprobante('pago', archivo).subscribe();
    const request = http.expectOne(`${base}/pago/comprobante`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.get('archivo')).toBe(archivo);
  });

  it('descarga el comprobante autenticado como blob', () => {
    service.descargarComprobante('pago').subscribe();
    const request = http.expectOne(`${base}/pago/comprobante`);
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
  });

  it('usa las rutas canónicas de consulta y resolución', () => {
    service.listarMisPagos().subscribe();
    expect(http.expectOne(`${base}/mis-pagos`).request.method).toBe('GET');
    service.listarPendientes().subscribe();
    expect(http.expectOne(`${base}/pendientes`).request.method).toBe('GET');
    service.validarPago('pago', {}).subscribe();
    expect(http.expectOne(`${base}/pago/validar`).request.method).toBe('PATCH');
    service.rechazarPago('pago', { observacion: 'Ilegible' }).subscribe();
    expect(http.expectOne(`${base}/pago/rechazar`).request.method).toBe('PATCH');
  });
});
