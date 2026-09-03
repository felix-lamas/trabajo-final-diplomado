import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-recuperar-contrasena',
  templateUrl: './recuperar-contrasena.component.html',
  standalone: false
})
export class RecuperarContrasenaComponent {
  recuperarForm: FormGroup;
  loading = false;
  enviado = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.recuperarForm = this.fb.group({
      correoElectronico: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.recuperarForm.invalid) {
      this.recuperarForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.authService.recuperarContrasena(this.recuperarForm.value.correoElectronico).subscribe({
      next: () => {
        this.enviado = true;
        this.loading = false;
        this.snackBar.open('Se han enviado instrucciones a su correo', 'Cerrar', { duration: 5000 });
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.mensaje || 'Error al procesar solicitud', 'Cerrar', { duration: 3000 });
      }
    });
  }
}
