import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { ComprobanteInscripcion, DetalleInscripcion, EstadoInscripcion } from '../../../../core/models/inscripcion.model';
import { EstadoPago, Pago } from '../../../../core/models/pago.model';
import { InscripcionService } from '../../../../core/services/inscripcion.service';
import { PagoService } from '../../../../core/services/pago.service';
import { PrivadoPagosModule } from '../pagos.module';
import { RegistrarPagoComponent } from './registrar-pago.component';

describe('RegistrarPagoComponent', () => {
  let fixture: ComponentFixture<RegistrarPagoComponent>;
  let inscripcionResponse: Subject<DetalleInscripcion>;
  let pagosResponse: Subject<Pago[]>;
  let comprobanteResponse: Subject<ComprobanteInscripcion>;
  let uploadResponse: Subject<Pago>;
  const router = { navigate: vi.fn() };
  const pago: Pago = {
    id: 'pago', inscripcionId: 'inscripcion', eventoTitulo: 'Evento pagado', usuarioNombre: 'Ana',
    monto: 80, fechaPago: '2026-09-30T10:00:00', estado: EstadoPago.RECHAZADO,
    motivoRechazo: 'Comprobante ilegible', intentosComprobante: 1
  };

  beforeEach(async () => {
    inscripcionResponse = new Subject<DetalleInscripcion>();
    pagosResponse = new Subject<Pago[]>();
    comprobanteResponse = new Subject<ComprobanteInscripcion>();
    uploadResponse = new Subject<Pago>();
    await TestBed.configureTestingModule({
      imports: [PrivadoPagosModule],
      providers: [
        provideZonelessChangeDetection(), provideNoopAnimations(),
        { provide: ActivatedRoute, useValue: { snapshot: { queryParams: { inscripcionId: 'inscripcion' } } } },
        { provide: Router, useValue: router },
        { provide: InscripcionService, useValue: {
          obtenerPorId: vi.fn(() => inscripcionResponse.asObservable()),
          obtenerComprobante: vi.fn(() => comprobanteResponse.asObservable())
        } },
        { provide: PagoService, useValue: {
          listarMisPagos: vi.fn(() => pagosResponse.asObservable()),
          registrarPago: vi.fn(),
          subirComprobante: vi.fn(() => uploadResponse.asObservable())
        } }
      ]
    }).compileComponents();
    router.navigate.mockClear();
    fixture = TestBed.createComponent(RegistrarPagoComponent);
    fixture.detectChanges();
  });

  it('mantiene loading hasta resolver inscripción y pago', () => {
    expect(fixture.componentInstance.loading).toBe(true);
    expect(fixture.nativeElement.querySelector('mat-spinner')).not.toBeNull();
  });

  it('muestra el monto oficial y el motivo para permitir el reenvío', async () => {
    resolverContexto();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Bs.');
    expect(fixture.nativeElement.textContent).toContain('Comprobante ilegible');
    expect(fixture.nativeElement.textContent).toContain('Presentar comprobante');
  });

  it('evita el doble envío y reutiliza el mismo pago rechazado', async () => {
    resolverContexto();
    await fixture.whenStable();
    fixture.componentInstance.archivoComprobante = new File(['pdf'], 'comprobante.pdf', { type: 'application/pdf' });
    fixture.componentInstance.guardar();
    fixture.componentInstance.guardar();
    const service = TestBed.inject(PagoService);
    expect(service.subirComprobante).toHaveBeenCalledTimes(1);
    expect(service.registrarPago).not.toHaveBeenCalled();
  });

  function resolverContexto(): void {
    inscripcionResponse.next({
      id: 'inscripcion', eventoId: 'evento', eventoTitulo: 'Evento pagado', fechaInscripcion: '2026-09-30T10:00:00',
      estado: EstadoInscripcion.PENDIENTE_PAGO, usuarioId: 'usuario', usuarioNombre: 'Ana',
      codigoInscripcion: 'INS-1', modalidadEvento: 'PRESENCIAL'
    });
    inscripcionResponse.complete();
    pagosResponse.next([pago]);
    pagosResponse.complete();
    comprobanteResponse.next({
      inscripcionId: 'inscripcion', codigoInscripcion: 'INS-1', eventoId: 'evento',
      eventoTitulo: 'Evento pagado', participante: 'Ana', ci: '123', monto: 80,
      fechaInscripcion: '2026-09-30T10:00:00', estadoInscripcion: EstadoInscripcion.PENDIENTE_PAGO,
      estadoPago: 'RECHAZADO', codigoVerificacion: 'INS-1'
    });
    comprobanteResponse.complete();
  }
});
