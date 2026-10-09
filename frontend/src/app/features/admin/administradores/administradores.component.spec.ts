import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { AdministradoresComponent } from './administradores.component';
describe('AdministradoresComponent', () => {
  let c: AdministradoresComponent;
  let service: any;
  beforeEach(() => {
    service = {
      listar: vi.fn(() => of([{ id: 'admin-1', activo: true }])),
      invitaciones: vi.fn(() => of([{ id: 'invite-1', estado: 'PENDIENTE' }])),
      reenviar: vi.fn(() => of({})),
      revocar: vi.fn(() => of({})),
      desactivar: vi.fn(() => of(undefined)),
    };
    c = new AdministradoresComponent(service, {
      open: () => ({ afterClosed: () => of(true) }),
    } as any);
  });
  it('lista cuentas e invitaciones y finaliza loading', () => {
    c.ngOnInit();
    expect(c.administradores()).toHaveLength(1);
    expect(c.invitaciones()[0].estado).toBe('PENDIENTE');
    expect(c.loading()).toBe(false);
  });
  it('muestra error y permite recargar', () => {
    service.listar.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 503, error: { mensaje: 'No disponible' } })),
    );
    c.cargar();
    expect(c.error()).toBe('No disponible');
    expect(c.loading()).toBe(false);
  });
  it('revocaci?n confirmada recarga los estados', () => {
    c.revocar({ id: 'invite-1' } as any);
    expect(service.revocar).toHaveBeenCalledWith('invite-1');
    expect(c.mensaje()).toContain('revocada');
  });
  it('muestra rechazo del último administrador', () => {
    service.desactivar.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { mensaje: 'No se puede desactivar al último administrador activo.' },
          }),
      ),
    );
    c.desactivar({ id: 'admin-1' } as any);
    expect(c.error()).toContain('último administrador');
  });
});
