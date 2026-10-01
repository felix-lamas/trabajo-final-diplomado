import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService, RegistroUsuarioRequest } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-registro',
  templateUrl: './registro.component.html',
  standalone: false
})
export class RegistroComponent {
  form: FormGroup;
  private readonly viewState = signal({ currentStep: 1, loading: false });
  readonly submitError = signal('');
  readonly passwordVisible = signal(false);
  readonly confirmationVisible = signal(false);
  get currentStep(): number { return this.viewState().currentStep; }
  private set currentStep(value: number) { this.viewState.update((state) => ({ ...state, currentStep: value })); }
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      nombres: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      apellidos: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      ci: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9-]{4,20}$/)]],
      celular: ['', [Validators.required, Validators.pattern(/^[0-9+ -]{7,20}$/)]],
      tipoUsuario: ['INTERNO', [Validators.required]],
      ru: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9-]{4,20}$/)]],
      correoElectronico: ['', [Validators.required, Validators.email]],
      contrasena: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/)
      ]],
      confirmacionContrasena: ['', [Validators.required]]
    }, { validators: this.checkPasswords });
  }

  isInterno(): boolean {
    return this.form.get('tipoUsuario')?.value === 'INTERNO';
  }

  nextStep(): void {
    if (!this.canAdvance()) {
      this.markStepTouched();
      return;
    }

    this.currentStep = Math.min(3, this.currentStep + 1);
  }

  previousStep(): void {
    this.currentStep = Math.max(1, this.currentStep - 1);
  }

  goToStep(step: number): void {
    if (step < this.currentStep) {
      this.currentStep = step;
      return;
    }

    if (step === this.currentStep + 1) {
      this.nextStep();
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.submitError.set('');
    const value = this.form.getRawValue();
    const request: RegistroUsuarioRequest = {
      nombres: value.nombres,
      apellidos: value.apellidos,
      correoElectronico: value.correoElectronico,
      ci: value.ci,
      ru: this.isInterno() ? String(value.ru).trim() : null,
      celular: value.celular,
      contrasena: value.contrasena,
      confirmacionContrasena: value.confirmacionContrasena,
      tipoUsuario: value.tipoUsuario
    };

    this.authService.registro(request).pipe(finalize(() => this.loading = false)).subscribe({
      next: (response) => {
        this.snackBar.open('Cuenta creada. Verifique su correo para poder ingresar.', 'Cerrar', { duration: 4500 });
        this.router.navigate(['/auth/verificar-correo'], {
          queryParams: { correo: response.correoElectronico }
        });
      },
      error: (err) => {
        this.submitError.set(apiErrorMessage(err, 'No fue posible completar el registro. Revise los datos e intente nuevamente.'));
      }
    });
  }

  togglePasswordVisibility(field: 'password' | 'confirmation'): void {
    if (field === 'password') this.passwordVisible.update((visible) => !visible);
    else this.confirmationVisible.update((visible) => !visible);
  }

  get passwordStrength(): number {
    const password = String(this.form.get('contrasena')?.value || '');
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

  get progressPercent(): number {
    return (this.currentStep / 3) * 100;
  }

  private canAdvance(): boolean {
    if (this.currentStep === 1) {
      return ['nombres', 'apellidos', 'ci', 'celular'].every((control) => this.form.get(control)?.valid);
    }

    if (this.currentStep === 2) {
      if (!this.isInterno()) {
        return true;
      }

      return this.form.get('ru')?.valid ?? false;
    }

    return true;
  }

  private markStepTouched(): void {
    if (this.currentStep === 1) {
      ['nombres', 'apellidos', 'ci', 'celular'].forEach((control) => this.form.get(control)?.markAsTouched());
      return;
    }

    if (this.currentStep === 2 && this.isInterno()) {
      this.form.get('ru')?.markAsTouched();
    }
  }

  syncAcademicValidators(): void {
    const tipoUsuario = this.form.get('tipoUsuario')?.value;
    const ruCtrl = this.form.get('ru');

    if (tipoUsuario === 'INTERNO') {
      ruCtrl?.setValidators([Validators.required, Validators.pattern(/^[A-Za-z0-9-]{4,20}$/)]);
      ruCtrl?.updateValueAndValidity({ emitEvent: false });
      return;
    }

    ruCtrl?.clearValidators();
    ruCtrl?.reset('', { emitEvent: false });
    ruCtrl?.updateValueAndValidity({ emitEvent: false });
  }

  private checkPasswords(group: FormGroup) {
    const pass = group.get('contrasena')?.value;
    const confirmPass = group.get('confirmacionContrasena')?.value;
    return pass === confirmPass ? null : { notSame: true };
  }
}
