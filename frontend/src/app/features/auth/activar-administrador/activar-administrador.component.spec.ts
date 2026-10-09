import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { Location } from '@angular/common';
import { of, throwError } from 'rxjs';
import { AdministradorService } from '../../../core/services/administrador.service';
import { ActivarAdministradorComponent } from './activar-administrador.component';

describe('ActivarAdministradorComponent', () => {
  let service: any;
  let location: any;
  let component: ActivarAdministradorComponent;
  let query: any;
  beforeEach(() => {
    service = {
      consultar: vi.fn(() => of({ estado: 'PENDIENTE' })),
      aceptar: vi.fn(() => of(undefined)),
    };
    location = { replaceState: vi.fn() };
    query = convertToParamMap({ token: 'secret-test' });
    component = new ActivarAdministradorComponent(
      new FormBuilder(),
      { snapshot: { queryParamMap: query } } as ActivatedRoute,
      location,
      service,
    );
  });
  const datos = {
    nombres: 'E2E',
    apellidos: 'Admin',
    ci: 'E2E-01',
    celular: '00000000',
    contrasena: 'TestOnly9!',
    confirmacionContrasena: 'TestOnly9!',
  };
  it('consulta autom?ticamente y retira token del historial de URL', () => {
    component.ngOnInit();
    expect(service.consultar).toHaveBeenCalledWith('secret-test');
    expect(location.replaceState).toHaveBeenCalledWith('/auth/activar-administrador');
    expect(component.estado()).toBe('PENDIENTE');
  });
  it.each(['EXPIRADA', 'REVOCADA', 'ACEPTADA'])(
    'muestra estado %s sin permitir aceptaci?n',
    (estado) => {
      service.consultar.mockReturnValue(of({ estado }));
      component.ngOnInit();
      expect(component.estado()).toBe(estado);
      component.form.setValue(datos);
      component.aceptar();
      expect(service.aceptar).not.toHaveBeenCalled();
    },
  );
  it('rechaza enlace sin token', () => {
    component = new ActivarAdministradorComponent(
      new FormBuilder(),
      { snapshot: { queryParamMap: convertToParamMap({}) } } as ActivatedRoute,
      location,
      service,
    );
    component.ngOnInit();
    expect(component.estado()).toBe('error');
    expect(service.consultar).not.toHaveBeenCalled();
  });
  it('token inválido no conserva posibilidad de reintento', () => {
    service.consultar.mockReturnValue(
      throwError(() => ({ status: 400, error: { mensaje: 'Invitación inválida' } })),
    );
    component.ngOnInit();
    expect(component.estado()).toBe('error');
    expect(component.tokenDisponible()).toBe(false);
  });
  it('error de red permite reintentar sin persistir token', () => {
    service.consultar.mockReturnValue(throwError(() => ({ status: 0 })));
    component.ngOnInit();
    expect(component.tokenDisponible()).toBe(true);
    component.consultar();
    expect(service.consultar).toHaveBeenCalledTimes(2);
  });
  it('validaciones de contraseña y confirmación impiden env?o', () => {
    component.ngOnInit();
    component.form.setValue({ ...datos, contrasena: 'debil' });
    component.aceptar();
    expect(service.aceptar).not.toHaveBeenCalled();
    component.form.setValue({ ...datos, confirmacionContrasena: 'OtraTest9!' });
    component.aceptar();
    expect(service.aceptar).not.toHaveBeenCalled();
  });
  it('activaci?n exitosa limpia token y contraseña', () => {
    component.ngOnInit();
    component.form.setValue(datos);
    component.aceptar();
    expect(service.aceptar).toHaveBeenCalledWith('secret-test', datos);
    expect(component.estado()).toBe('success');
    expect(component.tokenDisponible()).toBe(false);
    expect(component.form.controls.contrasena.value).toBe('');
    component.aceptar();
    expect(service.aceptar).toHaveBeenCalledTimes(1);
  });
  it('muestra error backend sin ocultarlo', () => {
    component.ngOnInit();
    component.form.setValue(datos);
    service.aceptar.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 400, error: { mensaje: 'Los datos no coinciden' } }),
      ),
    );
    component.aceptar();
    expect(component.error()).toBe('Los datos no coinciden');
    expect(component.busy()).toBe(false);
  });
  it('no renderiza el secreto en pantalla', async () => {
    TestBed.configureTestingModule({
      imports: [ActivarAdministradorComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: query } } },
        { provide: Location, useValue: location },
        { provide: AdministradorService, useValue: service },
      ],
    });
    const fixture = TestBed.createComponent(ActivarAdministradorComponent);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).not.toContain('secret-test');
    expect(fixture.nativeElement.textContent).toContain('Activar cuenta administrativa');
  });
});
