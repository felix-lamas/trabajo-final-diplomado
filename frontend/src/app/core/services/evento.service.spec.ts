import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CrearEventoRequest, Modalidad, PublicoObjetivo, TipoInscripcion } from '../models/evento.model';
import { EventoService } from './evento.service';

describe('EventoService contrato canonico', () => {
  let service: EventoService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/eventos`;
  const request = {
    titulo: 'Evento', descripcion: 'Descripcion', objetivos: 'Objetivos', categoriaId: 'cat',
    modalidad: Modalidad.VIRTUAL, tipoInscripcion: TipoInscripcion.GRATUITO, costo: 0,
    fechaInicio: '2026-10-01', fechaFin: '2026-10-01', horaInicio: '08:00', horaFin: '09:00',
    enlaceVirtual: 'https://meet.example.test/a', latitud: null, longitud: null, radioMetros: null,
    requiereInscripcion: true, cupoLimitado: false, cupoMaximo: null, emiteCertificado: false,
    tipoCertificado: null, horasAcademicas: null, publicoObjetivo: PublicoObjetivo.AMBOS
  } satisfies CrearEventoRequest;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(EventoService); http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('lista con GET /eventos', () => { service.listar().subscribe(); expect(http.expectOne(base).request.method).toBe('GET'); });
  it('lista revision por la ruta canonica', () => { service.listarEnRevision().subscribe(); expect(http.expectOne(`${base}/revision`).request.method).toBe('GET'); });
  it('lista publicados por la ruta canonica', () => { service.listarPublicados().subscribe(); expect(http.expectOne(`${base}/publicados`).request.method).toBe('GET'); });
  it('obtiene detalle por id', () => { service.obtenerPorId('evt').subscribe(); expect(http.expectOne(`${base}/evt`).request.method).toBe('GET'); });
  it('crea el borrador sin organizador enviado', () => { service.crear(request).subscribe(); const r = http.expectOne(base); expect(r.request.method).toBe('POST'); expect(r.request.body.organizadorId).toBeUndefined(); });
  it('actualiza por PUT', () => { service.actualizar('evt', request).subscribe(); expect(http.expectOne(`${base}/evt`).request.method).toBe('PUT'); });
  it('elimina por DELETE', () => { service.eliminar('evt').subscribe(); expect(http.expectOne(`${base}/evt`).request.method).toBe('DELETE'); });
  it('envia a revision por PATCH', () => { service.enviarARevision('evt').subscribe(); expect(http.expectOne(`${base}/evt/enviar-revision`).request.method).toBe('PATCH'); });
  it('publica por PATCH', () => { service.publicar('evt').subscribe(); expect(http.expectOne(`${base}/evt/publicar`).request.method).toBe('PATCH'); });
  it('rechaza con motivo', () => { service.rechazar('evt', 'Corregir').subscribe(); const r = http.expectOne(`${base}/evt/rechazar`); expect(r.request.body).toEqual({ motivo: 'Corregir' }); });
  it('retorna rechazado a borrador', () => { service.volverABorrador('evt').subscribe(); expect(http.expectOne(`${base}/evt/volver-borrador`).request.method).toBe('PATCH'); });
  it('cancela con motivo', () => { service.cancelar('evt', 'Fuerza mayor').subscribe(); const r = http.expectOne(`${base}/evt/cancelar`); expect(r.request.body.motivo).toBe('Fuerza mayor'); });
  it('finaliza por PATCH', () => { service.finalizar('evt').subscribe(); expect(http.expectOne(`${base}/evt/finalizar`).request.method).toBe('PATCH'); });
});
