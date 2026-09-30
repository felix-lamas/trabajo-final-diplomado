import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { of, Subject } from 'rxjs';
import { EstadoInscripcion, Inscripcion } from '../../../../core/models/inscripcion.model';
import { InscripcionService } from '../../../../core/services/inscripcion.service';
import { ToastService } from '../../../../shared/ui/toast.service';
import { PrivadoInscripcionesModule } from '../../privado.module';
import { MisInscripcionesComponent } from './mis-inscripciones.component';

describe('MisInscripcionesComponent', () => {
  let fixture: ComponentFixture<MisInscripcionesComponent>;
  let responses: Subject<Inscripcion[]>[];
  const service = {
    listarMisInscripciones: vi.fn(),
    cancelar: vi.fn(() => of(void 0))
  };
  const dialog = { open: vi.fn(() => ({ afterClosed: () => of(false) })) };
  const toast = { success: vi.fn(), error: vi.fn() };
  const inscripcion: Inscripcion = {
    id: 'ins', eventoId: 'evento', eventoTitulo: 'Seminario',
    fechaInscripcion: '2026-09-30T12:00:00', estado: EstadoInscripcion.CONFIRMADA
  };

  beforeEach(async () => {
    responses = [];
    service.listarMisInscripciones.mockImplementation(() => {
      const response = new Subject<Inscripcion[]>();
      responses.push(response);
      return response.asObservable();
    });
    service.listarMisInscripciones.mockClear(); service.cancelar.mockClear();
    dialog.open.mockClear(); toast.success.mockClear(); toast.error.mockClear();
    await TestBed.configureTestingModule({
      imports: [PrivadoInscripcionesModule],
      providers: [
        provideZonelessChangeDetection(), provideNoopAnimations(), provideRouter([]),
        { provide: InscripcionService, useValue: service },
        { provide: MatDialog, useValue: dialog },
        { provide: ToastService, useValue: toast }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(MisInscripcionesComponent);
    fixture.detectChanges();
  });

  it('muestra loading durante la carga', () => {
    expect(fixture.componentInstance.loading).toBe(true);
    expect(fixture.nativeElement.querySelector('app-skeleton')).not.toBeNull();
  });

  it('actualiza el DOM al recibir inscripciones', async () => {
    responses[0].next([inscripcion]); responses[0].complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Seminario');
    expect(fixture.nativeElement.textContent).toContain('Confirmada');
  });

  it('muestra estado vacio', async () => {
    responses[0].next([]); responses[0].complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No hay inscripciones en este filtro');
  });

  it('muestra error con reintento', async () => {
    responses[0].error({ error: { mensaje: 'Servicio no disponible' } });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Error al cargar tus inscripciones');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });

  it('cancelar dialogo no invoca backend', async () => {
    responses[0].next([inscripcion]); responses[0].complete();
    await fixture.whenStable();
    fixture.componentInstance.cancelar(inscripcion);
    expect(service.cancelar).not.toHaveBeenCalled();
  });

  it('cancelacion confirmada actualiza inmediatamente', async () => {
    dialog.open.mockReturnValueOnce({ afterClosed: () => of(true) });
    responses[0].next([inscripcion]); responses[0].complete();
    await fixture.whenStable();
    fixture.componentInstance.cancelar(inscripcion);
    expect(service.cancelar).toHaveBeenCalledWith('ins');
    expect(toast.success).toHaveBeenCalled();
    expect(service.listarMisInscripciones).toHaveBeenCalledTimes(2);
  });
});
