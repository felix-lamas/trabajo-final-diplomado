import { Component, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

type VerificationStatus = 'instructions' | 'loading' | 'success' | 'error';

@Component({
  selector: 'app-verificar-correo',
  templateUrl: './verificar-correo.component.html',
  standalone: false
})
export class VerificarCorreoComponent implements OnInit {
  readonly estado = signal<VerificationStatus>('instructions');
  readonly mensajeError = signal('');
  readonly reenvioEstado = signal<'idle' | 'loading' | 'success' | 'error'>('idle');
  readonly reenvioError = signal('');
  readonly reenvioForm: FormGroup;
  private readonly token = signal('');

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly authService: AuthService
  ) {
    this.reenvioForm = this.fb.group({
      correoElectronico: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token')?.trim() ?? '';
    const correo = this.route.snapshot.queryParamMap.get('correo')?.trim() ?? '';
    this.reenvioForm.patchValue({ correoElectronico: correo });

    if (token) {
      this.token.set(token);
      this.verificar();
    }
  }

  verificar(): void {
    const token = this.token();
    if (!token || this.estado() === 'loading') {
      if (!token) {
        this.estado.set('error');
        this.mensajeError.set('El enlace no contiene un token de verificacion valido.');
      }
      return;
    }

    this.estado.set('loading');
    this.mensajeError.set('');
    this.authService.verificarCorreo(token).subscribe({
      next: () => this.estado.set('success'),
      error: (error: HttpErrorResponse) => {
        this.estado.set('error');
        this.mensajeError.set(this.verificationErrorMessage(error));
      }
    });
  }

  reenviar(): void {
    if (this.reenvioForm.invalid || this.reenvioEstado() === 'loading') {
      this.reenvioForm.markAllAsTouched();
      return;
    }

    this.reenvioEstado.set('loading');
    this.reenvioError.set('');
    const correo = String(this.reenvioForm.value.correoElectronico).trim();
    this.authService.reenviarVerificacion(correo).subscribe({
      next: () => this.reenvioEstado.set('success'),
      error: (error) => {
        this.reenvioEstado.set('error');
        this.reenvioError.set(apiErrorMessage(error, 'No fue posible procesar la solicitud. Intente nuevamente.'));
      }
    });
  }

  private verificationErrorMessage(error: HttpErrorResponse): string {
    switch (error.error?.codigo) {
      case 'EMAIL_VERIFICATION_TOKEN_EXPIRED':
        return 'El enlace de verificacion expiro. Solicite uno nuevo.';
      case 'EMAIL_VERIFICATION_TOKEN_USED':
        return 'Este enlace ya fue utilizado. Si su cuenta ya esta verificada, puede iniciar sesion.';
      case 'EMAIL_VERIFICATION_TOKEN_INVALID':
        return 'El enlace de verificacion no es valido.';
      default:
        return apiErrorMessage(error, 'No fue posible verificar el correo. Intente nuevamente.');
    }
  }
}
