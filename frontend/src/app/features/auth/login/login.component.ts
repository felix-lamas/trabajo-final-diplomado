import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: false
})
export class LoginComponent {
  form: FormGroup;
  loading = false;

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

    this.authService.login(this.form.getRawValue()).pipe(
      finalize(() => setTimeout(() => this.loading = false))
    ).subscribe({
      next: () => {
        this.snackBar.open('Sesion iniciada correctamente', 'Cerrar', { duration: 3000 });
        this.router.navigate(['/privado/dashboard']);
      },
      error: (err) => {
        this.snackBar.open(err.error?.mensaje || 'No fue posible iniciar sesion', 'Cerrar', { duration: 4000 });
      }
    });
  }
}
