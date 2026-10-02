import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { DashboardService } from '../../../core/services/dashboard.service';
import { EventoService } from '../../../core/services/evento.service';
import { PagoService } from '../../../core/services/pago.service';
import { AdminDashboardComponent } from './admin-dashboard.component';

describe('AdminDashboardComponent', () => {
  let fixture: ComponentFixture<AdminDashboardComponent>;
  let dashboard: any;

  beforeEach(async () => {
    dashboard = {
      obtenerEjecutivo: vi.fn(() => of({ totalEventos: 4, totalUsuarios: 21, totalParticipantes: 13, totalInscripciones: 30, totalCertificados: 7, ingresosGenerados: 850, nivelSatisfaccion: 0, participacionEncuestas: 0, promedioSatisfaccionPorEvento: [] })),
      obtenerAcademico: vi.fn(() => of({ participacionPorFacultad: [], participacionPorCarrera: [], participacionPorCategoria: [{ name: 'Ciencia', value: 2 }], participacionPorPeriodo: [] })),
      obtenerOperativo: vi.fn(() => of({ eventosActivos: 2, eventosFinalizados: 2, pagosPendientes: 1, pagosValidados: 5, qrUtilizados: 0, asistenciasRegistradas: 9 }))
    };
    await TestBed.configureTestingModule({
      imports: [AdminDashboardComponent],
      providers: [
        provideZonelessChangeDetection(), provideRouter([]),
        { provide: DashboardService, useValue: dashboard },
        { provide: EventoService, useValue: { listar: () => of([]) } },
        { provide: PagoService, useValue: { listarPendientes: () => of([]) } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(AdminDashboardComponent);
  });

  it('muestra estadísticas obtenidas y evita bloques ficticios de satisfacción o QR', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('21');
    expect(text).toContain('850');
    expect(text).toContain('Asistencias registradas');
    expect(text).not.toContain('Satisfacción');
    expect(text).not.toContain('Encuestas');
    expect(text).not.toContain('QR utilizados');
    expect(text).not.toContain('tiempo real');
  });

  it('representa el error de consulta y permite reintentar', async () => {
    dashboard.obtenerEjecutivo.mockReturnValueOnce(throwError(() => new Error('fallo')));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No se pudo cargar el panel');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });
});
