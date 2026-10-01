import { HttpErrorResponse } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { AuthModule } from '../auth.module';
import { VerificarCorreoComponent } from './verificar-correo.component';

describe('VerificarCorreoComponent reactive HTTP state', () => {
  let fixture: ComponentFixture<VerificarCorreoComponent>;
  let verifyResponse: Subject<void>;
  let resendResponse: Subject<void>;
  let queryParams: Record<string, string>;
  const auth = {
    verificarCorreo: vi.fn(() => verifyResponse.asObservable()),
    reenviarVerificacion: vi.fn(() => resendResponse.asObservable())
  };

  beforeEach(async () => {
    verifyResponse = new Subject<void>();
    resendResponse = new Subject<void>();
    queryParams = { token: 'verification-token' };
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [AuthModule],
      providers: [
        provideZonelessChangeDetection(),
        { provide: AuthService, useValue: auth },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: (key: string) => queryParams[key] ?? null } } }
        }
      ]
    }).compileComponents();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(VerificarCorreoComponent);
    fixture.detectChanges();
  }

  it('muestra loading en la carga inicial con token', () => {
    createComponent();

    expect(auth.verificarCorreo).toHaveBeenCalledWith('verification-token');
    expect(fixture.nativeElement.textContent).toContain('Verificando correo...');
  });

  it('muestra exito inmediatamente al resolver HTTP sin deteccion manual adicional', async () => {
    createComponent();
    verifyResponse.next();
    verifyResponse.complete();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Correo verificado');
    expect(fixture.nativeElement.textContent).toContain('Ir al inicio de sesion');
  });

  it.each([
    ['EMAIL_VERIFICATION_TOKEN_INVALID', 'no es valido'],
    ['EMAIL_VERIFICATION_TOKEN_EXPIRED', 'expiro'],
    ['EMAIL_VERIFICATION_TOKEN_USED', 'ya fue utilizado']
  ])('muestra el error contractual %s', async (codigo, texto) => {
    createComponent();
    verifyResponse.error(new HttpErrorResponse({
      status: 400,
      error: { codigo, mensaje: 'Error de verificacion' }
    }));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain(texto);
    expect(fixture.nativeElement.textContent).not.toContain('Reintentar');
    expect(fixture.nativeElement.textContent).toContain('Solicitar otro enlace');
  });

  it('permite estado inicial sin token y reenvio con respuesta generica', async () => {
    queryParams = { correo: 'ana@example.test' };
    createComponent();
    expect(fixture.nativeElement.textContent).toContain('Revisa tu bandeja de entrada');

    fixture.componentInstance.reenviar();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Procesando...');
    resendResponse.next();
    resendResponse.complete();
    await fixture.whenStable();

    expect(auth.reenviarVerificacion).toHaveBeenCalledWith('ana@example.test');
    expect(fixture.nativeElement.textContent).toContain('Si la cuenta existe y requiere verificacion');
  });

  it('muestra error HTTP de reenvio y rehabilita el formulario', async () => {
    queryParams = { correo: 'ana@example.test' };
    createComponent();
    fixture.componentInstance.reenviar();
    resendResponse.error(new HttpErrorResponse({ status: 503, error: {} }));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('No fue posible procesar la solicitud');
    expect(fixture.componentInstance.reenvioEstado()).toBe('error');
  });
});
