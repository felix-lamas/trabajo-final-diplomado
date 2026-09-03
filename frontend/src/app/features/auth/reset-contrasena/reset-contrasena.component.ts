import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-contrasena',
  templateUrl: './reset-contrasena.component.html',
  standalone: false
})
export class ResetContrasenaComponent implements OnInit {
  resetForm: FormGroup;
  loading = false;
  token = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
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
      this.snackBar.open('Token de seguridad ausente', 'Cerrar', { duration: 3000 });
      this.router.navigate(['/auth/login']);
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
    const { nuevaContrasena, confirmacion } = this.resetForm.value;

    this.authService.restablecerContrasena(this.token, nuevaContrasena, confirmacion).subscribe({
      next: () => {
        this.snackBar.open('Contrasena actualizada correctamente', 'Cerrar', { duration: 3000 });
        this.router.navigate(['/auth/login']);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.mensaje || 'Error al actualizar contrasena', 'Cerrar', { duration: 3000 });
      }
    });
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
