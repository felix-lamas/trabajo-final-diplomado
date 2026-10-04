import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { DetalleInscripcion, EstadoInscripcion } from '../../../core/models/inscripcion.model';
import { EstadoEvento, Evento, Modalidad, PublicoObjetivo, TipoInscripcion } from '../../../core/models/evento.model';
import { EventoService } from '../../../core/services/evento.service';
import { InscripcionService } from '../../../core/services/inscripcion.service';
import { PublicoModule } from '../publico.module';
import { InscripcionPublicaComponent } from './inscripcion-publica.component';

describe('InscripcionPublicaComponent', () => {
  let fixture: ComponentFixture<InscripcionPublicaComponent>;
  let eventoResponse: Subject<Evento>;
  let inscripcionResponse: Subject<DetalleInscripcion>;
  const router = { navigate: vi.fn() };
  const evento: Evento = {
    id: 'evento', titulo: 'Taller pagado', descripcion: 'Descripcion', objetivos: 'Aprender', categoriaId: 'cat',
    categoriaNombre: 'Taller', modalidad: Modalidad.VIRTUAL, tipoInscripcion: TipoInscripcion.PAGO,
    costo: 50, fechaInicio: '2026-10-10', fechaFin: '2026-10-10', horaInicio: '09:00',
    horaFin: '11:00', requiereInscripcion: true, cupoLimitado: true, cupoMaximo: 10,
    cupoDisponible: 2, emiteCertificado: false, publicoObjetivo: PublicoObjetivo.AMBOS,
    estado: EstadoEvento.PUBLICADO, organizadorId: 'org', organizadorNombre: 'Organizador'
  };

  beforeEach(async () => {
    eventoResponse = new Subject<Evento>();
    inscripcionResponse = new Subject<DetalleInscripcion>();
    await TestBed.configureTestingModule({
      imports: [PublicoModule],
      providers: [
        provideZonelessChangeDetection(), provideNoopAnimations(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => 'evento' } } } },
        { provide: Router, useValue: router },
        { provide: EventoService, useValue: { obtenerPorId: vi.fn(() => eventoResponse.asObservable()), normalizarQrPagoUrl: vi.fn((url: string) => url) } },
        { provide: InscripcionService, useValue: { inscribir: vi.fn(() => inscripcionResponse.asObservable()) } }
      ]
    }).compileComponents();
    router.navigate.mockClear();
    fixture = TestBed.createComponent(InscripcionPublicaComponent);
    fixture.detectChanges();
  });

  it('muestra loading hasta que responde el evento', () => {
    expect(fixture.componentInstance.loading).toBe(true);
    expect(fixture.nativeElement.querySelector('mat-spinner')).not.toBeNull();
  });

  it('admite evento pagado y explica que el pago es externo con comprobante', async () => {
    eventoResponse.next(evento); eventoResponse.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('realiza el pago externamente');
    expect(fixture.nativeElement.textContent).toContain('presenta tu comprobante');
    expect(router.navigate).not.toHaveBeenCalledWith(['/eventos']);
  });

  it('muestra el evento gratuito sin pedir comprobante', async () => {
    eventoResponse.next({ ...evento, tipoInscripcion: TipoInscripcion.GRATUITO, costo: 0 }); eventoResponse.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No se requiere comprobante de pago');
    expect(fixture.nativeElement.textContent).toContain('Gratuito');
  });

  it('evita doble envio mientras confirma', async () => {
    eventoResponse.next(evento); eventoResponse.complete();
    await fixture.whenStable();
    const service = TestBed.inject(InscripcionService);
    fixture.componentInstance.confirmar();
    fixture.componentInstance.confirmar();
    expect(service.inscribir).toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.confirmando).toBe(true);
  });

  it('navega al detalle con estado pendiente de pago', async () => {
    eventoResponse.next(evento); eventoResponse.complete();
    await fixture.whenStable();
    fixture.componentInstance.confirmar();
    inscripcionResponse.next({
      id: 'ins', eventoId: 'evento', eventoTitulo: 'Taller pagado', fechaInscripcion: '2026-09-30T10:00:00',
      estado: EstadoInscripcion.PENDIENTE_PAGO, usuarioId: 'user', usuarioNombre: 'Ana',
      codigoInscripcion: 'INS-ABC', modalidadEvento: 'VIRTUAL'
    });
    inscripcionResponse.complete();
    await fixture.whenStable();
    expect(router.navigate).toHaveBeenCalledWith(['/privado/inscripciones', 'ins']);
  });
});
