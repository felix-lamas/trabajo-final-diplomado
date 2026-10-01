import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { Observable, of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { CertificadoService } from '../../../core/services/certificado.service';
import { EventoService } from '../../../core/services/evento.service';
import { InscripcionService } from '../../../core/services/inscripcion.service';
import { PagoService } from '../../../core/services/pago.service';
import { AuthService } from '../../../core/services/auth.service';
import { EstadoEvento, Evento, Modalidad, PublicoObjetivo, TipoInscripcion } from '../../../core/models/evento.model';
import { EstadoInscripcion, Inscripcion } from '../../../core/models/inscripcion.model';
import { EstadoPago, Pago } from '../../../core/models/pago.model';
import { CertificadoResponse } from '../../../core/models/certificado.model';
import { DashboardPrivadoComponent } from './dashboard-privado.component';

describe('DashboardPrivadoComponent', () => {
  const publishedEvent = (id: string, title: string, start: string): Evento => ({
    id,
    titulo: title,
    descripcion: 'Descripción del evento',
    objetivos: '',
    categoriaId: 'category-1',
    categoriaNombre: 'Académico',
    modalidad: Modalidad.PRESENCIAL,
    tipoInscripcion: TipoInscripcion.GRATUITO,
    costo: null,
    fechaInicio: start,
    fechaFin: start,
    horaInicio: '10:00',
    horaFin: '12:00',
    requiereInscripcion: true,
    cupoLimitado: false,
    cupoMaximo: null,
    cupoDisponible: null,
    emiteCertificado: false,
    publicoObjetivo: PublicoObjetivo.AMBOS,
    estado: EstadoEvento.PUBLICADO,
    organizadorId: 'organizer-1',
    organizadorNombre: 'Organizador'
  });

  const registration: Inscripcion = {
    id: 'registration-1',
    eventoId: 'event-1',
    eventoTitulo: 'Encuentro universitario',
    fechaInscripcion: '2026-01-10T10:00:00',
    estado: EstadoInscripcion.CONFIRMADA
  };

  const payment: Pago = {
    id: 'payment-1',
    inscripcionId: 'registration-1',
    eventoTitulo: 'Encuentro universitario',
    usuarioNombre: 'Usuario Demo',
    monto: 50,
    fechaPago: '2026-01-11T10:00:00',
    estado: EstadoPago.APROBADO,
    intentosComprobante: 1
  };

  const certificate: CertificadoResponse = {
    id: 'certificate-1',
    nombreCompleto: 'Usuario Demo',
    ru: null,
    ci: '0000000',
    evento: 'Encuentro universitario',
    cargaHoraria: null,
    tipoCertificado: 'NO_CURRICULAR',
    horasAcademicas: null,
    porcentajeAsistencia: null,
    codigoCertificado: 'CODIGO-TEST',
    fechaEmision: '2026-01-12T10:00:00',
    urlVerificacion: '/verificar-certificado/CODIGO-TEST',
    estado: 'GENERADO',
    archivoPdfUrl: ''
  };

  const eventService = { listarPublicados: vi.fn() };
  const registrationService = { listarMisInscripciones: vi.fn() };
  const paymentService = { listarMisPagos: vi.fn() };
  const certificateService = { listarMisCertificados: vi.fn() };
  const authService = { getUser: vi.fn(() => ({ nombres: 'Usuario', apellidos: 'Demo' })) };

  beforeEach(() => vi.clearAllMocks());

  async function render(overrides?: {
    events?: Observable<Evento[] | null>;
    registrations?: Observable<Inscripcion[] | null>;
    payments?: Observable<Pago[] | null>;
    certificates?: Observable<CertificadoResponse[] | null>;
  }) {
    eventService.listarPublicados.mockReturnValue(overrides?.events ?? of([]));
    registrationService.listarMisInscripciones.mockReturnValue(overrides?.registrations ?? of([]));
    paymentService.listarMisPagos.mockReturnValue(overrides?.payments ?? of([]));
    certificateService.listarMisCertificados.mockReturnValue(overrides?.certificates ?? of([]));

    await TestBed.configureTestingModule({
      imports: [DashboardPrivadoComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: EventoService, useValue: eventService },
        { provide: InscripcionService, useValue: registrationService },
        { provide: PagoService, useValue: paymentService },
        { provide: CertificadoService, useValue: certificateService }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DashboardPrivadoComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('loads the authenticated user name and real personal indicators', async () => {
    const fixture = await render({
      registrations: of([registration]),
      payments: of([payment]),
      certificates: of([certificate])
    });

    expect(fixture.nativeElement.textContent).toContain('Hola, Usuario Demo');
    expect(fixture.nativeElement.querySelector('[aria-label="Mis inscripciones: 1"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('[aria-label="Pagos aprobados: 1"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('[aria-label="Mis certificados: 1"]')).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Inscripción confirmada');
    expect(registrationService.listarMisInscripciones).toHaveBeenCalledOnce();
    expect(paymentService.listarMisPagos).toHaveBeenCalledOnce();
    expect(certificateService.listarMisCertificados).toHaveBeenCalledOnce();
  });

  it('shows published upcoming events ordered by their actual start date without calling them recommendations', async () => {
    const fixture = await render({
      events: of([
        publishedEvent('event-later', 'Evento más lejano', '2099-08-20'),
        publishedEvent('event-soon', 'Evento más próximo', '2099-07-10'),
        publishedEvent('event-past', 'Evento pasado', '2020-07-10')
      ]),
      registrations: of([{ ...registration, eventoId: 'event-soon' }])
    });

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Evento más próximo');
    expect(text.indexOf('Evento más próximo')).toBeLessThan(text.indexOf('Evento más lejano'));
    expect(text).not.toContain('Evento pasado');
    expect(text).toContain('Eventos disponibles');
    expect(text).toContain('Tu inscripción: confirmada');
    expect(text).not.toContain('recomendados');
    expect(fixture.nativeElement.querySelector('img')).toBeNull();
    expect(eventService.listarPublicados).toHaveBeenCalledOnce();
  });

  it('provides empty states for real empty responses and keeps the existing navigation actions', async () => {
    const fixture = await render();
    const text = fixture.nativeElement.textContent as string;
    const links = Array.from(fixture.nativeElement.querySelectorAll('a')) as HTMLAnchorElement[];

    expect(text).toContain('No hay próximos eventos publicados');
    expect(text).toContain('Tu actividad aparecerá aquí');
    expect(text).toContain('Aún no tienes inscripciones');
    expect(text).toContain('Aún no tienes pagos');
    expect(text).toContain('Aún no tienes certificados');
    expect(links.some((link) => link.getAttribute('href') === '/eventos')).toBe(true);
    expect(links.some((link) => link.getAttribute('href') === '/privado/perfil')).toBe(true);
    expect(links.some((link) => link.getAttribute('href') === '/privado/inscripciones')).toBe(true);
    expect(links.some((link) => link.getAttribute('href') === '/privado/pagos')).toBe(true);
    expect(links.some((link) => link.getAttribute('href') === '/certificados/mis-certificados')).toBe(true);
  });

  it('shows an error instead of presenting failed requests as empty or zero data', async () => {
    const fixture = await render({
      events: throwError(() => new Error('network')),
      payments: throwError(() => new Error('network'))
    });
    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('No pudimos cargar los eventos');
    expect(text).toContain('Algunos datos no están disponibles');
    expect(fixture.nativeElement.querySelector('[aria-label="Pagos aprobados: —"]')).not.toBeNull();
    expect(eventService.listarPublicados).toHaveBeenCalledOnce();
  });

  it('shows loading skeletons before dashboard requests complete', async () => {
    eventService.listarPublicados.mockReturnValue(new Observable(() => undefined));
    registrationService.listarMisInscripciones.mockReturnValue(new Observable(() => undefined));
    paymentService.listarMisPagos.mockReturnValue(new Observable(() => undefined));
    certificateService.listarMisCertificados.mockReturnValue(new Observable(() => undefined));

    await TestBed.configureTestingModule({
      imports: [DashboardPrivadoComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: EventoService, useValue: eventService },
        { provide: InscripcionService, useValue: registrationService },
        { provide: PagoService, useValue: paymentService },
        { provide: CertificadoService, useValue: certificateService }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DashboardPrivadoComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('app-skeleton').length).toBeGreaterThan(0);
    expect(fixture.nativeElement.textContent).not.toContain('Mis inscripciones: 0');
  });
});
