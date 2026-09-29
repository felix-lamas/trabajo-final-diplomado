import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { SolicitudOrganizador, SolicitudOrganizadorService } from './solicitud-organizador.service';

describe('SolicitudOrganizadorService', () => {
  let service: SolicitudOrganizadorService;
  let http: HttpTestingController;
  const solicitud: SolicitudOrganizador = {
    usuarioId: '10000000-0000-0000-0000-000000000001',
    nombres: 'Ana',
    apellidos: 'Perez',
    correoElectronico: 'ana@example.test',
    estado: 'PENDIENTE',
    fechaSolicitud: '2026-09-29T12:00:00'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(SolicitudOrganizadorService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('solicita para el usuario autenticado sin enviar usuarioId', () => {
    service.solicitar().subscribe();
    const request = http.expectOne(`${environment.apiUrl}/usuarios/solicitud-organizador`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush(solicitud);
  });

  it('lista por estado contractual', () => {
    service.listar('RECHAZADA').subscribe();
    const request = http.expectOne((req) =>
      req.url === `${environment.apiUrl}/usuarios/solicitudes-organizador`
      && req.params.get('estado') === 'RECHAZADA');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('aprueba por PATCH', () => {
    service.aprobar(solicitud.usuarioId).subscribe();
    const request = http.expectOne(
      `${environment.apiUrl}/usuarios/solicitudes-organizador/${solicitud.usuarioId}/aprobar`
    );
    expect(request.request.method).toBe('PATCH');
    request.flush({ ...solicitud, estado: 'APROBADA' });
  });

  it('rechaza por PATCH con motivo', () => {
    service.rechazar(solicitud.usuarioId, 'Informacion incompleta').subscribe();
    const request = http.expectOne(
      `${environment.apiUrl}/usuarios/solicitudes-organizador/${solicitud.usuarioId}/rechazar`
    );
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ motivo: 'Informacion incompleta' });
    request.flush({ ...solicitud, estado: 'RECHAZADA' });
  });
});
