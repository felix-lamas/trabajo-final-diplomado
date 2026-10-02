import { HttpErrorResponse } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { AuthModule } from '../auth.module';
import { RecuperarContrasenaComponent } from './recuperar-contrasena.component';

describe('RecuperarContrasenaComponent', () => {
  let fixture: ComponentFixture<RecuperarContrasenaComponent>;
  let response: Subject<void>;
  const auth = { recuperarContrasena: vi.fn(() => response.asObservable()) };
  const snackBar = { open: vi.fn() };

  beforeEach(async () => {
    response = new Subject<void>();
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [AuthModule],
      providers: [provideZonelessChangeDetection(), provideRouter([]), { provide: AuthService, useValue: auth }, { provide: MatSnackBar, useValue: snackBar }]
    }).compileComponents();
    fixture = TestBed.createComponent(RecuperarContrasenaComponent);
    fixture.detectChanges();
  });

  it('valida el correo antes de llamar al backend', () => {
    fixture.componentInstance.onSubmit();
    expect(fixture.componentInstance.recuperarForm.invalid).toBe(true);
    expect(auth.recuperarContrasena).not.toHaveBeenCalled();
  });

  it('muestra estado generico de exito sin revelar si existe la cuenta', async () => {
    fixture.componentInstance.recuperarForm.setValue({ correoElectronico: 'demo@example.test' });
    fixture.componentInstance.onSubmit();
    expect(fixture.componentInstance.loading).toBe(true);
    expect(auth.recuperarContrasena).toHaveBeenCalledWith('demo@example.test');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Procesando...');
    response.next();
    response.complete();
    await fixture.whenStable();
    expect(auth.recuperarContrasena).toHaveBeenCalledWith('demo@example.test');
    expect(fixture.nativeElement.textContent).toContain('Si existe una cuenta asociada');
    expect(snackBar.open).toHaveBeenCalledWith(
      'Si la dirección está registrada, recibirás instrucciones para recuperar tu contraseña.',
      'Cerrar',
      { duration: 5000 }
    );
    expect(fixture.componentInstance.loading).toBe(false);
  });

  it('muestra un error util sin filtrar detalles tecnicos del servidor', async () => {
    fixture.componentInstance.recuperarForm.setValue({ correoElectronico: 'demo@example.test' });
    fixture.componentInstance.onSubmit();
    response.error(new HttpErrorResponse({ status: 503, error: { mensaje: 'SQL internal failure' } }));
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No fue posible procesar la solicitud');
    expect(fixture.nativeElement.textContent).not.toContain('SQL internal failure');
  });
});
