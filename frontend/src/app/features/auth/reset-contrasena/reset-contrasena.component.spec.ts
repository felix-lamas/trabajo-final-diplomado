import { HttpErrorResponse } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { AuthModule } from '../auth.module';
import { ResetContrasenaComponent } from './reset-contrasena.component';

describe('ResetContrasenaComponent', () => {
  let fixture: ComponentFixture<ResetContrasenaComponent>;
  let response: Subject<void>;
  let query: Record<string, string>;
  let router: Router;
  const auth = { restablecerContrasena: vi.fn(() => response.asObservable()) };

  beforeEach(async () => {
    response = new Subject<void>();
    query = { token: 'temporary-token' };
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [AuthModule],
      providers: [provideZonelessChangeDetection(), provideRouter([]), { provide: AuthService, useValue: auth }, {
        provide: ActivatedRoute,
        useValue: { snapshot: { queryParamMap: { get: (key: string) => query[key] ?? null } } }
      }]
    }).compileComponents();
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(ResetContrasenaComponent);
    fixture.detectChanges();
  });

  it('no guarda el token y explica cuando falta el enlace', () => {
    query = {};
    fixture = TestBed.createComponent(ResetContrasenaComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.tokenMissing()).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('No se encontro el enlace de recuperacion');
  });

  it('valida contrasena y confirmacion antes de enviar', () => {
    fixture.componentInstance.resetForm.setValue({ nuevaContrasena: 'weak', confirmacion: 'other' });
    fixture.componentInstance.onSubmit();
    expect(fixture.componentInstance.resetForm.invalid).toBe(true);
    expect(auth.restablecerContrasena).not.toHaveBeenCalled();
  });

  it.each([
    ['PASSWORD_RESET_TOKEN_INVALID', 'no es'],
    ['PASSWORD_RESET_TOKEN_EXPIRED', 'expiró'],
    ['PASSWORD_RESET_TOKEN_USED', 'ya fue utilizado']
  ])('representa el error contractual %s', async (codigo, mensaje) => {
    fixture.componentInstance.resetForm.setValue({ nuevaContrasena: 'NuevaClave9!', confirmacion: 'NuevaClave9!' });
    fixture.componentInstance.onSubmit();
    response.error(new HttpErrorResponse({ status: 400, error: { codigo, mensaje: 'Detalle interno' } }));
    await fixture.whenStable();
    expect(fixture.componentInstance.errorMessage()).toContain(mensaje);
    expect(fixture.componentInstance.loading).toBe(false);
  });

  it('en exito envia el token solo al endpoint y regresa a login', async () => {
    fixture.componentInstance.resetForm.setValue({ nuevaContrasena: 'NuevaClave9!', confirmacion: 'NuevaClave9!' });
    fixture.componentInstance.onSubmit();
    expect(auth.restablecerContrasena).toHaveBeenCalledWith('temporary-token', 'NuevaClave9!', 'NuevaClave9!');
    response.next();
    response.complete();
    await fixture.whenStable();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
