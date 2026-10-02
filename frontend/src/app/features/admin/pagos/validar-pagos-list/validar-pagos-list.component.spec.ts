import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';

import { AuthService } from '../../../../core/services/auth.service';
import { PagoService } from '../../../../core/services/pago.service';
import { ValidarPagosListComponent } from './validar-pagos-list.component';

describe('ValidarPagosListComponent admin scope', () => {
  let fixture: ComponentFixture<ValidarPagosListComponent>;
  let pagos: any;
  let roles: string[];
  const pendiente = { id: 'p-1', inscripcionId: 'i-1', eventoTitulo: 'Jornada', usuarioNombre: 'Persona', monto: 20, fechaPago: '2026-09-01T10:00:00', estado: 'PENDIENTE_VALIDACION', intentosComprobante: 1, comprobante: null };
  const aprobado = { ...pendiente, id: 'p-2', estado: 'APROBADO' };

  async function create(role: string): Promise<void> {
    roles = [role];
    await TestBed.configureTestingModule({
      imports: [ValidarPagosListComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: AuthService, useValue: { hasAnyRole: (expected: string[]) => expected.some((item) => roles.includes(item)) } },
        { provide: PagoService, useValue: pagos },
        { provide: MatDialog, useValue: { open: vi.fn(() => ({ afterClosed: () => of(false) })) } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ValidarPagosListComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(() => {
    pagos = {
      listarTodos: vi.fn(() => of([pendiente, aprobado])),
      listarPendientes: vi.fn(() => of([pendiente])),
      validarPago: vi.fn(), rechazarPago: vi.fn(), descargarComprobante: vi.fn()
    };
  });

  it('ADMINISTRADOR consume el listado global y solo puede resolver pagos pendientes de validación', async () => {
    await create('ADMINISTRADOR');
    expect(pagos.listarTodos).toHaveBeenCalledOnce();
    expect(pagos.listarPendientes).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Pagos registrados');
    expect(fixture.nativeElement.textContent).toContain('APROBADO');
    const actionButtons = Array.from(fixture.nativeElement.querySelectorAll('tr.mat-mdc-row')) as HTMLElement[];
    expect(actionButtons).toHaveLength(2);
    expect(actionButtons[0].querySelectorAll('button').length).toBe(2);
    expect(actionButtons[1].querySelectorAll('button').length).toBe(0);
  });

  it('ORGANIZADOR conserva la bandeja de pagos pendientes', async () => {
    await create('ORGANIZADOR');
    expect(pagos.listarPendientes).toHaveBeenCalledOnce();
    expect(pagos.listarTodos).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Comprobantes pendientes');
  });
});
