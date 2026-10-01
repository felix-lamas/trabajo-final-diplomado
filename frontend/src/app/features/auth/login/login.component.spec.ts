import { HttpErrorResponse } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthService, LoginResponse } from '../../../core/services/auth.service';
import { AuthModule } from '../auth.module';
import { LoginComponent } from './login.component';

describe('LoginComponent identity flow', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let loginResponse: Subject<LoginResponse>;
  let resendResponse: Subject<void>;
  const auth = {
    login: vi.fn(() => loginResponse.asObservable()),
    reenviarVerificacion: vi.fn(() => resendResponse.asObservable())
  };
  const snackBar = { open: vi.fn() };
  let router: Router;

  beforeEach(async () => {
    loginResponse = new Subject<LoginResponse>();
    resendResponse = new Subject<void>();
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [AuthModule],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthService, useValue: auth },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ correoElectronico: 'ana@example.test', contrasena: 'Segura1!' });
  });

  it('muestra loading y navega segun rol al resolver login', async () => {
    fixture.componentInstance.onSubmit();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Ingresando...');

    loginResponse.next(successResponse(['ORGANIZADOR']));
    loginResponse.complete();
    await fixture.whenStable();

    expect(fixture.componentInstance.loading).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/organizador/eventos']);
  });

  it('ADMINISTRADOR conserva la navegacion a su panel', () => {
    fixture.componentInstance.onSubmit();
    loginResponse.next(successResponse(['ADMINISTRADOR']));
    loginResponse.complete();
    expect(router.navigate).toHaveBeenCalledWith(['/admin']);
  });

  it('USUARIO llega a su panel principal', () => {
    fixture.componentInstance.onSubmit();
    loginResponse.next(successResponse(['USUARIO']));
    loginResponse.complete();
    expect(router.navigate).toHaveBeenCalledWith(['/privado/dashboard']);
  });

  it('correo no verificado aparece inmediatamente y ofrece reenvio generico', async () => {
    fixture.componentInstance.onSubmit();
    loginResponse.error(new HttpErrorResponse({
      status: 400,
      error: { codigo: 'EMAIL_NOT_VERIFIED', mensaje: 'Correo no verificado' }
    }));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Correo pendiente de verificacion');
    expect(fixture.nativeElement.textContent).toContain('Reenviar verificacion');

    fixture.componentInstance.reenviarVerificacion();
    resendResponse.next();
    resendResponse.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Si la cuenta existe y requiere verificacion');
  });

  it.each([
    [401, 'AUTH_INVALID_CREDENTIALS'],
    [400, 'VALIDATION_ERROR']
  ])('muestra error de credenciales para respuesta %s', async (status, codigo) => {
    fixture.componentInstance.onSubmit();
    loginResponse.error(new HttpErrorResponse({
      status,
      error: { codigo, mensaje: 'Correo electronico o contrasena incorrectos' }
    }));
    await fixture.whenStable();

    expect(fixture.componentInstance.errorMessage()).toBe(status === 401
      ? 'El correo o la contraseña no coinciden.'
      : 'Correo electronico o contrasena incorrectos');
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(fixture.componentInstance.errorMessage());
    expect(fixture.componentInstance.correoNoVerificado).toBe(false);
  });

  it('distingue 403 de credenciales incorrectas', async () => {
    fixture.componentInstance.onSubmit();
    loginResponse.error(new HttpErrorResponse({ status: 403, error: {} }));
    await fixture.whenStable();
    expect(fixture.componentInstance.errorMessage()).toBe('No tiene permisos para realizar esta operacion.');
    expect(fixture.componentInstance.errorMessage()).not.toContain('no coinciden');
  });

  it('permite mostrar y ocultar localmente la contrasena', () => {
    expect(fixture.componentInstance.passwordVisible()).toBe(false);
    fixture.componentInstance.togglePasswordVisibility();
    expect(fixture.componentInstance.passwordVisible()).toBe(true);
    fixture.componentInstance.togglePasswordVisibility();
    expect(fixture.componentInstance.passwordVisible()).toBe(false);
  });
});

function successResponse(roles: string[]): LoginResponse {
  return {
    token: 'jwt',
    usuario: {
      id: '10000000-0000-0000-0000-000000000001',
      nombres: 'Ana',
      apellidos: 'Perez',
      correoElectronico: 'ana@example.test',
      correoVerificado: true,
      roles
    }
  };
}
