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
  });
});
