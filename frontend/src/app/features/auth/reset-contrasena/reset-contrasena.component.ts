import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-reset-contrasena',
  templateUrl: './reset-contrasena.component.html',
  standalone: false
})
export class ResetContrasenaComponent implements OnInit {
  resetForm: FormGroup;
  private readonly loadingState = signal(false);
  readonly errorMessage = signal('');
  readonly passwordVisible = signal(false);
  readonly confirmationVisible = signal(false);
  readonly tokenMissing = signal(false);
  get loading(): boolean { return this.loadingState(); }
  private set loading(value: boolean) { this.loadingState.set(value); }
  token = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.resetForm = this.fb.group({
      nuevaContrasena: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/)
      ]],
      confirmacion: ['', [Validators.required]]
    }, { validators: this.checkPasswords });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.tokenMissing.set(true);
    }
  }

  onPasswordChange(): void {
    const confirmacion = this.resetForm.get('confirmacion');
    if (confirmacion?.touched) {
      confirmacion.updateValueAndValidity({ onlySelf: true });
    }
  }

  checkPasswords(group: FormGroup) {
    const pass = group.get('nuevaContrasena')?.value;
    const confirmPass = group.get('confirmacion')?.value;
    return pass === confirmPass ? null : { notSame: true };
  }

  onSubmit(): void {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage.set('');
    const { nuevaContrasena, confirmacion } = this.resetForm.value;

    this.authService.restablecerContrasena(this.token, nuevaContrasena, confirmacion).pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: () => {
        this.router.navigate(['/auth/login']);
      },
      error: (err) => {
        const code = err.error?.codigo;
        this.errorMessage.set(code === 'PASSWORD_RESET_TOKEN_EXPIRED'
          ? 'El enlace de recuperación expiró. Solicite uno nuevo.'
          : code === 'PASSWORD_RESET_TOKEN_USED'
            ? 'Este enlace ya fue utilizado. Solicite una nueva recuperación si aún necesita cambiar la contraseña.'
            : code === 'PASSWORD_RESET_TOKEN_INVALID'
              ? 'El enlace de recuperación no es válido. Solicite uno nuevo.'
              : err.status === 0 || err.status >= 500
                ? 'No fue posible actualizar la contraseña. Intente nuevamente más tarde.'
                : apiErrorMessage(err, 'No fue posible actualizar la contraseña. Intente nuevamente.'));
      }
    });
  }

  togglePasswordVisibility(field: 'password' | 'confirmation'): void {
    if (field === 'password') this.passwordVisible.update((visible) => !visible);
    else this.confirmationVisible.update((visible) => !visible);
  }

  get passwordStrength(): number {
    const password = String(this.resetForm.get('nuevaContrasena')?.value || '');
    let score = 0;

    if (password.length >= 8) score += 25;
    if (/[a-z]/.test(password)) score += 20;
    if (/[A-Z]/.test(password)) score += 20;
    if (/\d/.test(password)) score += 20;
    if (/[^A-Za-z0-9]/.test(password)) score += 15;

    return score;
  }

  get passwordLabel(): string {
    const score = this.passwordStrength;
    if (score < 40) return 'Debil';
    if (score < 70) return 'Aceptable';
    return 'Fuerte';
  }

  get passwordTone(): 'danger' | 'warning' | 'success' {
    const score = this.passwordStrength;
    if (score < 40) return 'danger';
    if (score < 70) return 'warning';
    return 'success';
  }
}
