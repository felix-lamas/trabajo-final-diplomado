import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { InvitarAdministradorComponent } from './invitar-administrador.component';
describe('InvitarAdministradorComponent', () => {
  let c: InvitarAdministradorComponent;
  let service: any;
  const datos = {
    nombres: 'E2E',
    apellidos: 'Admin',
    correo: 'e2e@example.test',
    ci: 'E2E-01',
    celular: '00000000',
  };
  beforeEach(() => {
    service = { invitar: vi.fn(() => of({ estado: 'PENDIENTE' })) };
    c = new InvitarAdministradorComponent(new FormBuilder(), service);
  });
  it('requiere todos los campos y no env?a formulario vac?o', () => {
    c.enviar();
    expect(service.invitar).not.toHaveBeenCalled();
    expect(c.form.touched).toBe(true);
  });
  it.each([{ correo: 'incorrecto' }, { ci: '!' }, { celular: 'abc' }, { nombres: '   ' }])(
    'rechaza formatos inválidos %j',
    (cambio) => {
      c.form.setValue({ ...datos, ...cambio });
      c.enviar();
      expect(service.invitar).not.toHaveBeenCalled();
    },
  );
  it('env?a formulario correcto y confirma correo enviado', () => {
    c.form.setValue(datos);
    c.enviar();
    expect(service.invitar).toHaveBeenCalledWith(datos);
    expect(c.success()).toBe(true);
    expect(c.busy()).toBe(false);
  });
  it('muestra rechazo del backend por cuenta existente', () => {
    c.form.setValue(datos);
    service.invitar.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { mensaje: 'La dirección de correo ya está asociada a una cuenta.' },
          }),
      ),
    );
    c.enviar();
    expect(c.success()).toBe(false);
    expect(c.error()).toContain('asociada');
  });
});
