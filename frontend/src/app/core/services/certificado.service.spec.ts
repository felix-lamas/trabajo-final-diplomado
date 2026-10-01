import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CertificadoService } from './certificado.service';

describe('CertificadoService contrato canónico', () => {
  let service: CertificadoService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/certificados`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CertificadoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista exclusivamente los certificados del usuario autenticado', () => {
    service.listarMisCertificados().subscribe();
    expect(http.expectOne(`${base}/mis-certificados`).request.method).toBe('GET');
  });

  it('lista certificados usando el endpoint real del evento', () => {
    service.listarPorEvento('evento').subscribe();
    const request = http.expectOne(`${environment.apiUrl}/eventos/evento/certificados`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('solicita emisión sin fabricar un request body no definido por el backend', () => {
    service.generar('inscripcion').subscribe();
    const request = http.expectOne(`${base}/generar/inscripcion`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();
    request.flush({});
  });

  it('descarga el PDF como blob y no como JSON', () => {
    service.descargar('certificado').subscribe();
    const request = http.expectOne(`${base}/certificado/descargar`);
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
  });

  it('usa la única ruta pública canónica y codifica el código', () => {
    service.verificarPublicamente('UAJMS/CÓDIGO').subscribe();
    const request = http.expectOne(`${base}/verificar/UAJMS%2FC%C3%93DIGO`);
    expect(request.request.method).toBe('GET');
  });
});
