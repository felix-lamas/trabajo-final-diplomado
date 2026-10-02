import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { ReportesAnaliticaModule } from '../reportes.module';
import { DashboardPanelComponent } from './dashboard-panel.component';

describe('DashboardPanelComponent', () => {
  let fixture: ComponentFixture<DashboardPanelComponent>;
  const auth = { hasAnyRole: vi.fn((roles: string[]) => roles.includes('ORGANIZADOR')) };
  const dashboard = {
    obtenerEjecutivo: vi.fn(() => of({
      totalEventos: 2, totalUsuarios: 3, totalParticipantes: 3, totalInscripciones: 4,
      totalCertificados: 1, ingresosGenerados: 125, nivelSatisfaccion: 0,
      participacionEncuestas: 0, promedioSatisfaccionPorEvento: []
    })),
    obtenerAcademico: vi.fn(() => of({
      participacionPorFacultad: [], participacionPorCarrera: [], participacionPorCategoria: [{ name: 'Académico', value: 2 }], participacionPorPeriodo: []
    })),
    obtenerOperativo: vi.fn(() => of({
      eventosActivos: 1, eventosFinalizados: 1, pagosPendientes: 2, pagosValidados: 1,
      qrUtilizados: 0, asistenciasRegistradas: 3
    }))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportesAnaliticaModule],
      providers: [
        provideZonelessChangeDetection(), provideNoopAnimations(), provideRouter([]),
        { provide: AuthService, useValue: auth },
        { provide: DashboardService, useValue: dashboard }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardPanelComponent);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('muestra métricas reales devueltas por el backend y no tendencias simuladas', () => {
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Participantes inscritos');
    expect(text).toContain('Pagos pendientes de validación');
    expect(text).toContain('Eventos por categoría');
    expect(text).not.toContain('+12%');
    expect(text).not.toContain('Satisfacción global');
    expect(text).not.toContain('QR utilizados');
  });

  it('dirige accesos rápidos de organizador a rutas de organizador, no administrativas', () => {
    const links = Array.from(fixture.nativeElement.querySelectorAll('a')) as HTMLAnchorElement[];
    const targets = links.map((link) => link.getAttribute('href'));
    expect(targets).toContain('/organizador/eventos');
    expect(targets).toContain('/organizador/pagos/validar');
    expect(targets).not.toContain('/admin/eventos');
    expect(targets).not.toContain('/admin/pagos/validar');
  });

  it('conserva los accesos administrativos para ADMINISTRADOR', async () => {
    auth.hasAnyRole.mockImplementation((roles) => roles.includes('ADMINISTRADOR'));
    const adminFixture = TestBed.createComponent(DashboardPanelComponent);
    adminFixture.detectChanges();
    await adminFixture.whenStable();

    const links = Array.from(adminFixture.nativeElement.querySelectorAll('a')) as HTMLAnchorElement[];
    const targets = links.map((link) => link.getAttribute('href'));
    expect(adminFixture.nativeElement.textContent).toContain('Panel de gestión');
    expect(targets).toContain('/admin/eventos');
    expect(targets).toContain('/admin/pagos/validar');
  });

  it('muestra una recuperación cuando falla la consulta del panel', async () => {
    dashboard.obtenerEjecutivo.mockReturnValue(throwError(() => new Error('API unavailable')) as never);
    const failedFixture = TestBed.createComponent(DashboardPanelComponent);
    failedFixture.detectChanges();
    await failedFixture.whenStable();

    expect(failedFixture.nativeElement.textContent).toContain('No se pudo cargar el panel');
    expect(failedFixture.nativeElement.textContent).toContain('Reintentar');
  });
});
