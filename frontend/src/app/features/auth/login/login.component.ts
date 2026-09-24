import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

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
      next: (response) => {
        this.snackBar.open('Sesion iniciada correctamente', 'Cerrar', { duration: 3000 });
        const roles = response.usuario.roles;
        if (roles.includes('ADMINISTRADOR')) {
          this.router.navigate(['/admin']);
        } else if (roles.includes('ORGANIZADOR')) {
          this.router.navigate(['/organizador/eventos']);
        } else {
          this.router.navigate(['/eventos']);
        }
      },
      error: (err) => {
        this.snackBar.open(apiErrorMessage(err, 'No fue posible iniciar sesion'), 'Cerrar', { duration: 4000 });
      }
    });
  }
}
