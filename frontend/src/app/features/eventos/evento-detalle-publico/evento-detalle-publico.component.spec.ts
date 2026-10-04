import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { EventoService } from '../../../core/services/evento.service';
import { InscripcionService } from '../../../core/services/inscripcion.service';
import { EstadoEvento, EventoDetalle, Modalidad, PublicoObjetivo, TipoCertificadoEvento, TipoInscripcion } from '../../../core/models/evento.model';
import { EstadoInscripcion, Inscripcion } from '../../../core/models/inscripcion.model';
import { ThemeService } from '../../../core/services/theme.service';
import { PublicoModule } from '../../publico/publico.module';
import { EventoDetallePublicoComponent } from './evento-detalle-publico.component';

describe('EventoDetallePublicoComponent', () => {
  let fixture: ComponentFixture<EventoDetallePublicoComponent>;
  let eventoResponses: Subject<EventoDetalle>[];
  let inscripcionResponses: Subject<Inscripcion[]>[];
  let authUser: { authenticated: boolean; roles: string[] };
  let eventoApi: { obtenerPorId: ReturnType<typeof vi.fn>; descargarQrPago: ReturnType<typeof vi.fn> };
  let inscripcionApi: { listarMisInscripciones: ReturnType<typeof vi.fn> };

  const evento: EventoDetalle = {
    id: 'evento-1', titulo: 'Encuentro de innovación', descripcion: 'Una jornada universitaria para compartir experiencias.',
    objetivos: 'Promover el intercambio académico.', categoriaId: 'categoria-1', categoriaNombre: 'Académico',
    modalidad: Modalidad.PRESENCIAL, tipoInscripcion: TipoInscripcion.GRATUITO, costo: null,
    fechaInicio: '2026-11-20', fechaFin: '2026-11-21', horaInicio: '09:00:00', horaFin: '17:00:00',
    ubicacion: 'Campus Central', direccion: 'Avenida principal', latitud: -21.53, longitud: -64.73,
    radioMetros: 100, enlaceVirtual: null, requiereInscripcion: true, cupoLimitado: true,
    cupoMaximo: 120, cupoDisponible: 30, emiteCertificado: true,
    tipoCertificado: TipoCertificadoEvento.CURRICULAR, horasAcademicas: 16,
    publicoObjetivo: PublicoObjetivo.AMBOS, estado: EstadoEvento.PUBLICADO,
    imagenPortada: null, telefonoContacto: '46600000', emailContacto: 'eventos@demo.local',
    whatsappContacto: null, qrPagoUrl: null, instruccionesPago: null, motivoRechazo: null,
    motivoCancelacion: null, organizadorId: 'organizador-1', organizadorNombre: 'Organizador Demo'
  };

  const inscripcion: Inscripcion = {
    id: 'inscripcion-1', eventoId: evento.id, eventoTitulo: evento.titulo,
    fechaInscripcion: '2026-10-01T10:00:00', estado: EstadoInscripcion.PENDIENTE_PAGO
  };

  beforeEach(async () => {
    eventoResponses = [];
    inscripcionResponses = [];
    authUser = { authenticated: false, roles: [] };
    eventoApi = {
      descargarQrPago: vi.fn(() => of(new Blob())),
      obtenerPorId: vi.fn(() => {
        const response = new Subject<EventoDetalle>();
        eventoResponses.push(response);
        return response.asObservable();
      })
    };
    inscripcionApi = {
      listarMisInscripciones: vi.fn(() => {
        const response = new Subject<Inscripcion[]>();
        inscripcionResponses.push(response);
        return response.asObservable();
      })
    };

    await TestBed.configureTestingModule({
      imports: [PublicoModule],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: evento.id }) } } },
        { provide: EventoService, useValue: eventoApi },
        { provide: InscripcionService, useValue: inscripcionApi },
        { provide: AuthService, useValue: {
          isAuthenticated: () => authUser.authenticated,
          hasAnyRole: (roles: readonly string[]) => roles.some((role) => authUser.roles.includes(role))
        } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EventoDetallePublicoComponent);
    fixture.detectChanges();
  });

  function resolveEvent(data: EventoDetalle = evento): void {
    eventoResponses[0].next(data);
    eventoResponses[0].complete();
    fixture.detectChanges();
  }

  function reloadEvent(data: EventoDetalle): void {
    fixture.componentInstance.cargarEvento();
    const response = eventoResponses[eventoResponses.length - 1];
    response.next(data);
    response.complete();
    fixture.detectChanges();
  }

  function resolveInscription(data: Inscripcion[] = []): void {
    inscripcionResponses[0].next(data);
    inscripcionResponses[0].complete();
    fixture.detectChanges();
  }

  it('muestra un skeleton mientras consulta el detalle', () => {
    expect(fixture.componentInstance.loading).toBe(true);
    expect(fixture.nativeElement.querySelector('app-skeleton')).not.toBeNull();
  });

  it('representa sin límite cuando el DTO indica que no hay cupo limitado', () => {
    resolveEvent({ ...evento, cupoLimitado: false, cupoMaximo: null, cupoDisponible: null });
    expect(fixture.nativeElement.textContent).toContain('Sin límite de cupos');
  });

  it('presenta el DTO de detalle real sin inventar una imagen', () => {
    resolveEvent();
    const content = fixture.nativeElement.textContent;
    expect(content).toContain(evento.titulo);
    expect(content).toContain(evento.objetivos);
    expect(content).toContain('Organizador Demo');
    expect(content).toContain('30 cupos disponibles de 120');
    expect(content).toContain('Campus Central');
    expect(content).toContain('20 Nov 2026');
    expect(fixture.nativeElement.querySelector('.vidia-event-detail__image-placeholder')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('img[src*="unsplash"]')).toBeNull();
  });

  it('muestra el estado no encontrado para una respuesta 404', () => {
    eventoResponses[0].error(new HttpErrorResponse({ status: 404 }));
    fixture.detectChanges();
    expect(fixture.componentInstance.notFound).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('No encontramos este evento');
    expect(fixture.nativeElement.querySelector('app-empty-state')).not.toBeNull();
  });

  it('muestra error de API y permite reintentar en la misma pantalla', () => {
    eventoResponses[0].error(new HttpErrorResponse({ status: 503 }));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No pudimos cargar el evento');
    expect(fixture.nativeElement.querySelector('app-alert')).not.toBeNull();
    fixture.componentInstance.cargarEvento();
    expect(eventoApi.obtenerPorId).toHaveBeenCalledTimes(2);
    expect(fixture.componentInstance.loading).toBe(true);
  });

  it('expresa que un evento gratuito no tiene costo y permite continuar a inscripción', () => {
    resolveEvent();
    expect(fixture.nativeElement.textContent).toContain('Gratuito');
    expect(fixture.nativeElement.querySelector('app-button')).not.toBeNull();
  });

  it('muestra el monto e instrucciones reales de un evento pagado sin simular checkout', () => {
    resolveEvent({ ...evento, tipoInscripcion: TipoInscripcion.PAGO, costo: 125,
      instruccionesPago: 'Paga externamente y adjunta tu comprobante.', qrPagoUrl: 'https://example.test/qr.png' });
    const content = fixture.nativeElement.textContent;
    expect(content).toContain('Bs.');
    expect(content).toContain('125');
    expect(content).toContain('Paga externamente y adjunta tu comprobante.');
    expect(content).toContain('no se procesa mediante una pasarela');
    expect(fixture.nativeElement.querySelector('img[alt*="Código QR"]')).not.toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Pagar ahora');
  });

  it('muestra tipo y horas del certificado solo cuando el DTO lo habilita', () => {
    resolveEvent();
    expect(fixture.nativeElement.textContent).toContain('curricular');
    expect(fixture.nativeElement.textContent).toContain('16 horas académicas');
    reloadEvent({ ...evento, emiteCertificado: false, tipoCertificado: null, horasAcademicas: null });
    expect(fixture.nativeElement.textContent).not.toContain('Certificación');
    expect(fixture.nativeElement.textContent).not.toContain('horas académicas');
  });

  it('representa presencial y virtual; solo muestra enlace virtual a participante confirmado', () => {
    authUser = { authenticated: true, roles: ['USUARIO'] };
    resolveEvent({ ...evento, modalidad: Modalidad.VIRTUAL, ubicacion: null, direccion: null,
      enlaceVirtual: 'https://meet.example.test/evento' });
    resolveInscription([]);
    expect(fixture.nativeElement.textContent).toContain('Virtual');
    expect(fixture.nativeElement.querySelector('a[href="https://meet.example.test/evento"]')).toBeNull();
  });

  it('muestra el enlace virtual cuando la inscripción propia está confirmada', () => {
    authUser = { authenticated: true, roles: ['USUARIO'] };
    resolveEvent({ ...evento, modalidad: Modalidad.VIRTUAL, ubicacion: null, direccion: null,
      enlaceVirtual: 'https://meet.example.test/evento' });
    resolveInscription([{ ...inscripcion, estado: EstadoInscripcion.CONFIRMADA }]);
    expect(fixture.nativeElement.querySelector('a[href="https://meet.example.test/evento"]')).not.toBeNull();
  });

  it('muestra la audiencia usando los valores del contrato', () => {
    resolveEvent({ ...evento, publicoObjetivo: PublicoObjetivo.UAJMS });
    expect(fixture.nativeElement.textContent).toContain('Comunidad UAJMS');
    reloadEvent({ ...evento, publicoObjetivo: PublicoObjetivo.EXTERNO });
    expect(fixture.nativeElement.textContent).toContain('Participantes externos');
    reloadEvent({ ...evento, publicoObjetivo: PublicoObjetivo.AMBOS });
    expect(fixture.nativeElement.textContent).toContain('Comunidad UAJMS y público externo');
  });

  it('consulta inscripción propia y representa estado pendiente de pago', () => {
    authUser = { authenticated: true, roles: ['USUARIO'] };
    resolveEvent();
    expect(inscripcionApi.listarMisInscripciones).toHaveBeenCalledOnce();
    resolveInscription([inscripcion]);
    expect(fixture.nativeElement.textContent).toContain('Pendiente de pago');
    expect(fixture.nativeElement.textContent).toContain('continuar y presentar el comprobante');
  });

  it('representa el estado pendiente de validación sin ofrecer una inscripción nueva', () => {
    authUser = { authenticated: true, roles: ['USUARIO'] };
    resolveEvent();
    resolveInscription([{ ...inscripcion, estado: EstadoInscripcion.PENDIENTE_VALIDACION }]);
    expect(fixture.nativeElement.textContent).toContain('Comprobante en revisión');
    expect(fixture.nativeElement.textContent).toContain('pendiente de validación por el organizador');
    expect(fixture.nativeElement.querySelector('app-button button').textContent).toContain('Ver mi inscripción');
  });

  it('mantiene el botón de inscripción para evento libre y dirige al flujo existente', () => {
    resolveEvent();
    const router = TestBed.inject(Router);
    const navigation = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.nativeElement.querySelector('app-button button').click();
    expect(navigation).toHaveBeenCalledWith(['/eventos', evento.id, 'inscripcion']);
  });

  it('dirige una inscripción existente al detalle propio sin ofrecer una duplicada', () => {
    authUser = { authenticated: true, roles: ['USUARIO'] };
    resolveEvent();
    resolveInscription([{ ...inscripcion, estado: EstadoInscripcion.CANCELADA }]);
    expect(fixture.nativeElement.textContent).toContain('Esta inscripción fue cancelada');
    expect(fixture.nativeElement.querySelector('app-button button').textContent).toContain('Ver mi inscripción');
    const router = TestBed.inject(Router);
    const navigation = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.nativeElement.querySelector('app-button button').click();
    expect(navigation).toHaveBeenCalledWith(['/privado/inscripciones', inscripcion.id]);
  });

  it('no muestra inscripción para eventos que no la requieren ni para roles administrativos', () => {
    resolveEvent({ ...evento, requiereInscripcion: false });
    expect(fixture.nativeElement.querySelector('app-button')).toBeNull();
    authUser = { authenticated: true, roles: ['ADMINISTRADOR'] };
    reloadEvent(evento);
    expect(fixture.nativeElement.querySelector('app-button')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('disponible para participantes con rol Usuario');
    expect(inscripcionApi.listarMisInscripciones).not.toHaveBeenCalled();
  });

  it('deshabilita la acción si no pudo consultar el estado personal y ofrece reintento', () => {
    authUser = { authenticated: true, roles: ['USUARIO'] };
    resolveEvent();
    inscripcionResponses[0].error(new HttpErrorResponse({ status: 503 }));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No pudimos consultar tu inscripción');
    expect(fixture.nativeElement.querySelector('app-button button').disabled).toBe(true);
  });

  it('hereda Light y Dark mediante los tokens, sin fondos inline', () => {
    resolveEvent();
    const theme = TestBed.inject(ThemeService);
    theme.setTheme('light');
    expect(document.documentElement.dataset['theme']).toBe('light');
    theme.setTheme('dark');
    fixture.detectChanges();
    expect(document.documentElement.dataset['theme']).toBe('dark');
    expect(fixture.nativeElement.querySelector('.vidia-event-detail__hero').getAttribute('style')).toBeNull();
    expect(fixture.nativeElement.querySelector('.vidia-event-detail__action-card')).not.toBeNull();
  });
});
