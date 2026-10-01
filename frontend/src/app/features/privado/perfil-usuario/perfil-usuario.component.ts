import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

type LoadStatus = 'loading' | 'ready' | 'error';
type SubmitStatus = 'idle' | 'loading' | 'success' | 'error';

@Component({
  selector: 'app-perfil-usuario',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule
  ],
  templateUrl: './perfil-usuario.component.html'
})
export class PerfilUsuarioComponent implements OnInit {
  private static readonly PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;
  private readonly fb = inject(FormBuilder);

  readonly loadStatus = signal<LoadStatus>('loading');
  readonly loadError = signal('');
  readonly profileSubmitStatus = signal<SubmitStatus>('idle');
  readonly profileMessage = signal('');
  readonly passwordSubmitStatus = signal<SubmitStatus>('idle');
  readonly passwordMessage = signal('');
  readonly loggingOut = signal(false);
  readonly currentPasswordVisible = signal(false);
  readonly newPasswordVisible = signal(false);
  readonly passwordConfirmationVisible = signal(false);

  readonly profileForm = this.fb.group({
    nombres: ['', [Validators.required, Validators.maxLength(50)]],
    apellidos: ['', [Validators.required, Validators.maxLength(50)]],
    celular: ['', [Validators.required, Validators.pattern(/^[0-9+ -]{7,20}$/)]]
  });

  readonly passwordForm = this.fb.group({
    contrasenaActual: ['', Validators.required],
    nuevaContrasena: ['', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(PerfilUsuarioComponent.PASSWORD_PATTERN)
    ]],
    confirmacion: ['', Validators.required]
  }, { validators: [this.passwordsMatch, this.passwordIsDifferent] });

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cargarPerfil();
  }

  get user() {
    return this.authService.usuarioActual();
  }

  get fullName(): string {
    return this.user ? `${this.user.nombres} ${this.user.apellidos}`.trim() : 'Usuario';
  }

  get email(): string {
    return this.user?.correoElectronico || 'Sin correo disponible';
  }

  get roles(): string[] {
    return this.authService.roles();
  }

  get correoVerificado(): boolean {
    return this.user?.correoVerificado ?? false;
  }

  get tipoUsuario(): string | null {
    return this.user?.tipoUsuario ?? null;
  }

  get estadoSolicitudOrganizador(): string | null {
    return this.user?.estadoSolicitudOrganizador ?? null;
  }

  get initial(): string {
    return this.fullName.charAt(0).toUpperCase();
  }

  cargarPerfil(): void {
    if (this.loadStatus() === 'loading' && this.profileForm.dirty) {
      return;
    }
    this.loadStatus.set('loading');
    this.loadError.set('');
    this.authService.refrescarPerfil().subscribe({
      next: (usuario) => {
        this.profileForm.reset({
          nombres: usuario.nombres,
          apellidos: usuario.apellidos,
          celular: usuario.celular ?? ''
        });
        this.loadStatus.set('ready');
      },
      error: (error) => {
        this.loadStatus.set('error');
        this.loadError.set(apiErrorMessage(error, 'No fue posible cargar el perfil.'));
      }
    });
  }

  guardarPerfil(): void {
    if (this.profileForm.invalid || this.profileSubmitStatus() === 'loading') {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.profileSubmitStatus.set('loading');
    this.profileMessage.set('');
    const value = this.profileForm.getRawValue();
    this.authService.actualizarPerfil({
      nombres: value.nombres!.trim(),
      apellidos: value.apellidos!.trim(),
      celular: value.celular!.trim()
    }).subscribe({
      next: (usuario) => {
        this.profileForm.reset({
          nombres: usuario.nombres,
          apellidos: usuario.apellidos,
          celular: usuario.celular ?? ''
        });
        this.profileSubmitStatus.set('success');
        this.profileMessage.set('Perfil actualizado correctamente.');
      },
      error: (error) => {
        this.profileSubmitStatus.set('error');
        this.profileMessage.set(apiErrorMessage(error, 'No fue posible actualizar el perfil.'));
      }
    });
  }

  cambiarContrasena(): void {
    if (this.passwordForm.invalid || this.passwordSubmitStatus() === 'loading') {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.passwordSubmitStatus.set('loading');
    this.passwordMessage.set('');
    const value = this.passwordForm.getRawValue();
    this.authService.cambiarContrasena({
      contrasenaActual: value.contrasenaActual!,
      nuevaContrasena: value.nuevaContrasena!,
      confirmacion: value.confirmacion!
    }).subscribe({
      next: () => {
        this.passwordForm.reset();
        this.passwordSubmitStatus.set('success');
        this.snackBar.open('Contrasena actualizada. Inicie sesion nuevamente.', 'Cerrar', { duration: 5000 });
        void this.router.navigate(['/auth/login']);
      },
      error: (error) => {
        this.passwordSubmitStatus.set('error');
        this.passwordMessage.set(apiErrorMessage(error, 'No fue posible cambiar la contrasena.'));
      }
    });
  }

  cerrarSesion(): void {
    if (this.loggingOut()) {
      return;
    }
    this.loggingOut.set(true);
    this.authService.logout().subscribe({
      next: () => void this.router.navigate(['/auth/login']),
      error: () => void this.router.navigate(['/auth/login'])
    });
  }

  togglePasswordVisibility(field: 'current' | 'new' | 'confirmation'): void {
    if (field === 'current') this.currentPasswordVisible.update((visible) => !visible);
    if (field === 'new') this.newPasswordVisible.update((visible) => !visible);
    if (field === 'confirmation') this.passwordConfirmationVisible.update((visible) => !visible);
  }

  private passwordsMatch(control: AbstractControl): ValidationErrors | null {
    const nueva = control.get('nuevaContrasena')?.value;
    const confirmacion = control.get('confirmacion')?.value;
    return !nueva || !confirmacion || nueva === confirmacion ? null : { passwordMismatch: true };
  }

  private passwordIsDifferent(control: AbstractControl): ValidationErrors | null {
    const actual = control.get('contrasenaActual')?.value;
    const nueva = control.get('nuevaContrasena')?.value;
    return !actual || !nueva || actual !== nueva ? null : { passwordUnchanged: true };
  }
}
