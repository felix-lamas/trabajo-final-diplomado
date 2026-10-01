import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: false
})
export class LoginComponent {
  form: FormGroup;
  private readonly loadingState = signal(false);
  private readonly unverifiedState = signal(false);
  private readonly resendState = signal<'idle' | 'loading' | 'success'>('idle');
  readonly errorMessage = signal('');
  readonly resendError = signal('');
  readonly passwordVisible = signal(false);
  get loading(): boolean { return this.loadingState(); }
  private set loading(value: boolean) { this.loadingState.set(value); }
  get correoNoVerificado(): boolean { return this.unverifiedState(); }
  get reenviando(): boolean { return this.resendState() === 'loading'; }
  get reenvioEnviado(): boolean { return this.resendState() === 'success'; }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      correoElectronico: ['', [Validators.required, Validators.email]],
      contrasena: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.unverifiedState.set(false);
    this.resendState.set('idle');
    this.errorMessage.set('');

    this.authService.login(this.form.getRawValue()).pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: (response) => {
        const roles = response.usuario.roles;
        if (roles.includes('ADMINISTRADOR')) {
          this.router.navigate(['/admin']);
        } else if (roles.includes('ORGANIZADOR')) {
          this.router.navigate(['/organizador/eventos']);
        } else {
          this.router.navigate(['/privado/dashboard']);
        }
      },
      error: (err: HttpErrorResponse) => {
        if (err.error?.codigo === 'EMAIL_NOT_VERIFIED') {
          this.unverifiedState.set(true);
          return;
        }
        this.errorMessage.set(err.status === 401
          ? 'El correo o la contraseña no coinciden.'
          : apiErrorMessage(err, 'No fue posible iniciar sesión. Intente nuevamente.'));
      }
    });
  }

  reenviarVerificacion(): void {
    const correo = String(this.form.get('correoElectronico')?.value ?? '').trim();
    if (!correo || this.reenviando) {
      return;
    }

    this.resendState.set('loading');
    this.resendError.set('');
    this.authService.reenviarVerificacion(correo).pipe(
      finalize(() => {
        if (this.resendState() === 'loading') this.resendState.set('idle');
      })
    ).subscribe({
      next: () => this.resendState.set('success'),
      error: (error) => {
        this.resendState.set('idle');
        this.resendError.set(apiErrorMessage(error, 'No fue posible procesar la solicitud. Intente nuevamente.'));
      }
    });
  }

  togglePasswordVisibility(): void {
    this.passwordVisible.update((visible) => !visible);
  }
}
