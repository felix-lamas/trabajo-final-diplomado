import { HttpErrorResponse } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthService, RegistroResponse } from '../../../core/services/auth.service';
import { AuthModule } from '../auth.module';
import { RegistroComponent } from './registro.component';

describe('RegistroComponent verification flow', () => {
  let fixture: ComponentFixture<RegistroComponent>;
  let response: Subject<RegistroResponse>;
  const auth = { registro: vi.fn(() => response.asObservable()) };
  const snackBar = { open: vi.fn() };
  let router: Router;

  beforeEach(async () => {
    response = new Subject<RegistroResponse>();
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
    fixture = TestBed.createComponent(RegistroComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      nombres: 'Ana',
      apellidos: 'Perez',
      ci: '1234567',
      celular: '70000000',
      tipoUsuario: 'INTERNO',
      ru: 'RU-100',
      correoElectronico: 'ana@example.test',
      contrasena: 'Segura1!',
      confirmacionContrasena: 'Segura1!'
    });
  });

  it('registro exitoso no navega al dashboard sino a verificacion', async () => {
    fixture.componentInstance.onSubmit();
    expect(fixture.componentInstance.loading).toBe(true);

    response.next({ correoElectronico: 'ana@example.test', correoVerificado: false, mensaje: 'Verifique su correo' });
    response.complete();
    await fixture.whenStable();

    expect(router.navigate).toHaveBeenCalledWith(['/auth/verificar-correo'], {
      queryParams: { correo: 'ana@example.test' }
    });
    expect(router.navigate).not.toHaveBeenCalledWith(['/eventos']);
    expect(fixture.componentInstance.loading).toBe(false);
  });

  it('error de registro incluido correo duplicado rehabilita submit', async () => {
    fixture.componentInstance.onSubmit();
    response.error(new HttpErrorResponse({
      status: 409,
      error: { codigo: 'CONFLICT', mensaje: 'El correo ya esta registrado' }
    }));
    await fixture.whenStable();

    expect(fixture.componentInstance.loading).toBe(false);
    expect(fixture.componentInstance.submitError()).toBe('El correo ya esta registrado');
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain('El correo ya esta registrado');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('envia tipo externo sin RU y conserva el flujo de verificacion', async () => {
    fixture.componentInstance.form.patchValue({ tipoUsuario: 'EXTERNO', ru: '' });
    fixture.componentInstance.syncAcademicValidators();
    expect(fixture.componentInstance.form.get('ru')?.valid).toBe(true);
    fixture.componentInstance.onSubmit();
    expect(auth.registro).toHaveBeenCalledWith(expect.objectContaining({ tipoUsuario: 'EXTERNO', ru: null }));
  });
});
