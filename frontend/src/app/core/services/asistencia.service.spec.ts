import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AsistenciaService } from './asistencia.service';

describe('AsistenciaService contrato canonico', () => {
  let service: AsistenciaService;
  let http: HttpTestingController;
  const request = {
    nombre: 'Sesion', fecha: '2026-10-01', horaInicio: '08:00', horaFin: '10:00',
    requiereAsistencia: true, latitud: -21.53, longitud: -64.72, radioMetros: 100, activa: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AsistenciaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('usa rutas canonicas para CRUD y estado de sesiones', () => {
    service.listarSesiones('evento').subscribe();
    expect(http.expectOne(`${environment.apiUrl}/eventos/evento/sesiones`).request.method).toBe('GET');
    service.crearSesion('evento', request).subscribe();
    expect(http.expectOne(`${environment.apiUrl}/eventos/evento/sesiones`).request.method).toBe('POST');
    service.actualizarSesion('sesion', request).subscribe();
    expect(http.expectOne(`${environment.apiUrl}/sesiones/sesion`).request.method).toBe('PUT');
    service.cambiarEstado('sesion', false).subscribe();
    const estado = http.expectOne(`${environment.apiUrl}/sesiones/sesion/estado`);
    expect(estado.request.method).toBe('PATCH');
    expect(estado.request.body).toEqual({ activa: false });
  });

  it('genera y consulta QR exclusivamente en la sesion indicada', () => {
    service.obtenerQrActivo('sesion').subscribe();
    expect(http.expectOne(`${environment.apiUrl}/sesiones/sesion/qr`).request.method).toBe('GET');
    service.generarQr('sesion').subscribe();
    expect(http.expectOne(`${environment.apiUrl}/sesiones/sesion/qr/generar`).request.method).toBe('POST');
    service.detenerControlQr('sesion').subscribe();
    expect(http.expectOne(`${environment.apiUrl}/sesiones/sesion/qr/detener`).request.method).toBe('POST');
  });

  it('resuelve enlaces compartidos de Maps mediante el endpoint protegido de coordenadas', () => {
    service.resolverEnlaceGoogleMaps('https://maps.app.goo.gl/example').subscribe();
    const req = http.expectOne(`${environment.apiUrl}/maps/coordenadas-compartidas`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ url: 'https://maps.app.goo.gl/example' });
    req.flush({ latitud: -21.5, longitud: -64.7 });
  });

  it('consulta asistencia del evento con el DTO oficial, incluido registradoPor nulo', () => {
    const registrada = vi.fn();
    service.listarPorEvento('evento').subscribe(registrada);
    const req = http.expectOne(`${environment.apiUrl}/asistencias/evento/evento`);
    expect(req.request.method).toBe('GET');
    req.flush([{
      id: 'asistencia', nombreParticipante: 'Participante Ficticio', documentoIdentidad: 'CI-FICTICIO',
      codigoParticipante: 'PART-FICTICIO', evento: 'Evento de prueba', sesionEventoId: 'sesion',
      sesion: 'Sesión 1', fechaHoraRegistro: '2026-09-30T10:00:00', registradoPor: null,
      distanciaMetros: null, precisionGpsMetros: null, resultadoValidacion: 'VALIDADO', observacion: null
    }]);
    expect(registrada).toHaveBeenCalledWith(expect.arrayContaining([
      expect.objectContaining({ registradoPor: null })
    ]));
  });
});
