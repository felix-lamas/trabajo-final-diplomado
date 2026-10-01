import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
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
  get loading(): boolean { return this.loadingState(); }
  private set loading(value: boolean) { this.loadingState.set(value); }
  get correoNoVerificado(): boolean { return this.unverifiedState(); }
  get reenviando(): boolean { return this.resendState() === 'loading'; }
  get reenvioEnviado(): boolean { return this.resendState() === 'success'; }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
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

    this.authService.login(this.form.getRawValue()).pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: (response) => {
        this.snackBar.open('Sesion iniciada correctamente', 'Cerrar', { duration: 3000 });
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
        this.snackBar.open(apiErrorMessage(err, 'No fue posible iniciar sesion'), 'Cerrar', { duration: 4000 });
      }
    });
  }

  reenviarVerificacion(): void {
    const correo = String(this.form.get('correoElectronico')?.value ?? '').trim();
    if (!correo || this.reenviando) {
      return;
    }

    this.resendState.set('loading');
    this.authService.reenviarVerificacion(correo).subscribe({
      next: () => this.resendState.set('success'),
      error: (error) => {
        this.resendState.set('idle');
        this.snackBar.open(
          apiErrorMessage(error, 'No fue posible procesar el reenvio. Intente nuevamente.'),
          'Cerrar',
          { duration: 4000 }
        );
      }
    });
  }
}
