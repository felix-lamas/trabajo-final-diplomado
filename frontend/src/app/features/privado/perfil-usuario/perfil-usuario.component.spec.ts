import { HttpErrorResponse } from '@angular/common/http';
import { provideZonelessChangeDetection, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter, Router } from '@angular/router';
import { Subject, of, tap } from 'rxjs';
import { AuthService, AuthUser } from '../../../core/services/auth.service';
import { PerfilUsuarioComponent } from './perfil-usuario.component';

describe('PerfilUsuarioComponent reactive profile', () => {
  let fixture: ComponentFixture<PerfilUsuarioComponent>;
  let loadResponse: Subject<AuthUser>;
  let updateResponse: Subject<AuthUser>;
  let passwordResponse: Subject<void>;
  let userState: ReturnType<typeof signal<AuthUser | null>>;
  let router: Router;

  const usuario: AuthUser = {
    id: '10000000-0000-0000-0000-000000000001',
    nombres: 'Ana',
    apellidos: 'Perez',
    correoElectronico: 'ana@example.test',
    correoVerificado: true,
    celular: '70000000',
    ci: '1234567',
    ru: 'RU-100',
    roles: ['USUARIO']
  };
  const snackBar = { open: vi.fn() };
  let auth: {
    usuarioActual: ReturnType<typeof signal<AuthUser | null>>;
    roles: ReturnType<typeof signal<string[]>>;
    refrescarPerfil: ReturnType<typeof vi.fn>;
    actualizarPerfil: ReturnType<typeof vi.fn>;
    cambiarContrasena: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    loadResponse = new Subject<AuthUser>();
    updateResponse = new Subject<AuthUser>();
    passwordResponse = new Subject<void>();
    userState = signal<AuthUser | null>(usuario);
    auth = {
      usuarioActual: userState,
      roles: signal(['USUARIO']),
      refrescarPerfil: vi.fn(() => loadResponse.pipe(tap((user) => userState.set(user)))),
      actualizarPerfil: vi.fn(() => updateResponse.pipe(tap((user) => userState.set(user)))),
      cambiarContrasena: vi.fn(() => passwordResponse.pipe(tap(() => userState.set(null)))),
      logout: vi.fn(() => of(void 0))
    };
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [PerfilUsuarioComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthService, useValue: auth },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(PerfilUsuarioComponent);
    fixture.detectChanges();
  });

  it('muestra loading y luego el perfil real inmediatamente al resolver HTTP', async () => {
    expect(fixture.nativeElement.querySelector('mat-progress-bar')).not.toBeNull();

    loadResponse.next(usuario);
    loadResponse.complete();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Ana Perez');
    expect(fixture.nativeElement.textContent).toContain('ana@example.test');
    expect(fixture.componentInstance.profileForm.value.celular).toBe('70000000');
    expect(fixture.nativeElement.textContent).toContain('Correo verificado');
    expect(fixture.nativeElement.textContent).toContain('CI: 1234567');
    expect(fixture.nativeElement.textContent).toContain('RU: RU-100');
  });

  it('no presenta correo verificado cuando el backend lo informa como pendiente', async () => {
    loadResponse.next({ ...usuario, correoVerificado: false });
    loadResponse.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Correo pendiente de verificacion');
    expect(fixture.nativeElement.textContent).not.toContain('Correo verificado');
  });

  it('muestra error de carga y permite reintentar', async () => {
    loadResponse.error(new HttpErrorResponse({ status: 500, error: {} }));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('No se pudo cargar el perfil');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });

  it('valida campos obligatorios, longitud y telefono antes de submit', () => {
    fixture.componentInstance.profileForm.setValue({ nombres: '', apellidos: 'A'.repeat(51), celular: 'invalido' });
    fixture.componentInstance.guardarPerfil();

    expect(fixture.componentInstance.profileForm.invalid).toBe(true);
    expect(auth.actualizarPerfil).not.toHaveBeenCalled();
  });

  it('muestra loading y actualiza Signals y DOM al guardar perfil', async () => {
    loadResponse.next(usuario);
    loadResponse.complete();
    await fixture.whenStable();
    fixture.componentInstance.profileForm.setValue({
      nombres: 'Maria', apellidos: 'Rojas', celular: '71111111'
    });

    fixture.componentInstance.guardarPerfil();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Guardando...');

    const actualizado = { ...usuario, nombres: 'Maria', apellidos: 'Rojas', celular: '71111111' };
    updateResponse.next(actualizado);
    updateResponse.complete();
    await fixture.whenStable();

    expect(auth.actualizarPerfil).toHaveBeenCalledWith({
      nombres: 'Maria', apellidos: 'Rojas', celular: '71111111'
    });
    expect(fixture.nativeElement.textContent).toContain('Maria Rojas');
    expect(fixture.nativeElement.textContent).toContain('Perfil actualizado correctamente');
  });

  it('muestra error de actualizacion y rehabilita el formulario', async () => {
    loadResponse.next(usuario);
    loadResponse.complete();
    await fixture.whenStable();
    fixture.componentInstance.guardarPerfil();
    updateResponse.error(new HttpErrorResponse({
      status: 400,
      error: { mensaje: 'El telefono tiene un formato invalido' }
    }));
    await fixture.whenStable();

    expect(fixture.componentInstance.profileSubmitStatus()).toBe('error');
    expect(fixture.nativeElement.textContent).toContain('El telefono tiene un formato invalido');
  });

  it('rechaza nueva contrasena debil y confirmacion diferente sin llamar API', () => {
    fixture.componentInstance.passwordForm.setValue({
      contrasenaActual: 'Actual9!', nuevaContrasena: 'debil', confirmacion: 'otra'
    });
    fixture.componentInstance.cambiarContrasena();

    expect(fixture.componentInstance.passwordForm.invalid).toBe(true);
    expect(auth.cambiarContrasena).not.toHaveBeenCalled();
  });

  it('muestra contraseña actual incorrecta como error controlado', async () => {
    loadResponse.next(usuario);
    loadResponse.complete();
    await fixture.whenStable();
    fixture.componentInstance.passwordForm.setValue({
      contrasenaActual: 'Incorrecta9!', nuevaContrasena: 'NuevaClave9!', confirmacion: 'NuevaClave9!'
    });
    fixture.componentInstance.cambiarContrasena();
    passwordResponse.error(new HttpErrorResponse({
      status: 400,
      error: { codigo: 'PASSWORD_INVALID', mensaje: 'La contrasena actual es incorrecta' }
    }));
    await fixture.whenStable();

    expect(fixture.componentInstance.passwordSubmitStatus()).toBe('error');
    expect(fixture.nativeElement.textContent).toContain('La contrasena actual es incorrecta');
  });

  it('cambio exitoso limpia formulario, sesion y navega al login', async () => {
    fixture.componentInstance.passwordForm.setValue({
      contrasenaActual: 'Actual9!', nuevaContrasena: 'NuevaClave9!', confirmacion: 'NuevaClave9!'
    });
    fixture.componentInstance.cambiarContrasena();
    expect(fixture.componentInstance.passwordSubmitStatus()).toBe('loading');

    passwordResponse.next();
    passwordResponse.complete();
    await fixture.whenStable();

    expect(userState()).toBeNull();
    expect(fixture.componentInstance.passwordForm.value.contrasenaActual).toBeNull();
    expect(snackBar.open).toHaveBeenCalledOnce();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
