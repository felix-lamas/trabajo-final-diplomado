import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-recuperar-contrasena',
  templateUrl: './recuperar-contrasena.component.html',
  standalone: false
})
export class RecuperarContrasenaComponent {
  recuperarForm: FormGroup;
  private readonly viewState = signal({ loading: false, enviado: false });
  readonly errorMessage = signal('');
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
  get enviado(): boolean { return this.viewState().enviado; }
  private set enviado(value: boolean) { this.viewState.update((state) => ({ ...state, enviado: value })); }

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
    this.errorMessage.set('');
    this.authService.recuperarContrasena(String(this.recuperarForm.value.correoElectronico).trim()).pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: () => {
        this.enviado = true;
        this.snackBar.open('Se han enviado instrucciones a su correo', 'Cerrar', { duration: 5000 });
      },
      error: (err) => {
        this.errorMessage.set(err.status === 0 || err.status >= 500
          ? 'No fue posible procesar la solicitud. Intente nuevamente más tarde.'
          : 'No fue posible procesar la solicitud. Revise el correo e intente nuevamente.');
      }
    });
  }
}
