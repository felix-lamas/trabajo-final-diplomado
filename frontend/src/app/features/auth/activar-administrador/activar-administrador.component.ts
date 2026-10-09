import { CommonModule, Location } from '@angular/common';
import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AdministradorService } from '../../../core/services/administrador.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

@Component({
  selector: 'app-activar-administrador',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  template: `<mat-card class="activation"
    ><mat-card-content
      ><h1>Activar cuenta administrativa</h1>
      @if (estado() === 'loading') {
        <p role="status">Comprobando invitación?</p>
      }
      @if (estado() === 'error') {
        <p role="alert">{{ error() }}</p>
        @if (tokenDisponible()) {
          <button mat-button (click)="consultar()">Reintentar</button>
        }
      }
      @if (estado() === 'success') {
        <p role="status">Cuenta administrativa activada. Ya puede iniciar sesión.</p>
      }
      @if (estado() === 'PENDIENTE') {
        <p>
          Confirme sus datos de invitación y establezca su contraseña. El correo será el que recibió
          este enlace.
        </p>
        <form [formGroup]="form" (ngSubmit)="aceptar()">
          <mat-form-field
            ><mat-label>Nombres</mat-label
            ><input
              matInput
              formControlName="nombres"
              maxlength="50"
              autocomplete="given-name"
              required
          /></mat-form-field>
          <mat-form-field
            ><mat-label>Apellidos</mat-label
            ><input
              matInput
              formControlName="apellidos"
              maxlength="50"
              autocomplete="family-name"
              required
          /></mat-form-field>
          <mat-form-field
            ><mat-label>CI de la invitación</mat-label
            ><input matInput formControlName="ci" maxlength="20" required
          /></mat-form-field>
          <mat-form-field
            ><mat-label>Celular</mat-label
            ><input
              matInput
              type="tel"
              formControlName="celular"
              maxlength="20"
              autocomplete="tel"
              required
          /></mat-form-field>
          <mat-form-field
            ><mat-label>Contraseña</mat-label
            ><input
              matInput
              type="password"
              formControlName="contrasena"
              autocomplete="new-password"
              maxlength="100"
              required
            /><mat-hint
              >Mínimo 8 caracteres; mayúscula, minúscula, número y símbolo.</mat-hint
            ></mat-form-field
          >
          <mat-form-field
            ><mat-label>Confirmar contraseña</mat-label
            ><input
              matInput
              type="password"
              formControlName="confirmacionContrasena"
              autocomplete="new-password"
              maxlength="100"
              required
          /></mat-form-field>
          @if (form.invalid && form.touched) {
            <p role="alert">Revise los datos y confirme que las contraseñas coincidan.</p>
          }
          @if (error()) {
            <p role="alert">{{ error() }}</p>
          }
          <button mat-flat-button type="submit" [disabled]="busy()">
            {{ busy() ? 'Activando?' : 'Activar cuenta' }}
          </button>
        </form>
      }
      @if (estado() === 'EXPIRADA') {
        <p role="alert">La invitación expiró. Solicite un nuevo enlace al administrador.</p>
      }
      @if (estado() === 'REVOCADA') {
        <p role="alert">La invitación fue revocada.</p>
      }
      @if (estado() === 'ACEPTADA') {
        <p role="status">La invitación ya fue utilizada. Puede iniciar sesión.</p>
      }
      <a mat-button routerLink="/auth/login">Ir al login</a>
    </mat-card-content></mat-card
  >`,
  styles: [
    `
      .activation {
        max-width: 36rem;
        margin: 2rem auto;
      }
      form {
        display: grid;
        gap: 1rem;
      }
      mat-form-field {
        width: 100%;
      }
    `,
  ],
})
export class ActivarAdministradorComponent implements OnInit, OnDestroy {
  private token = '';
  readonly tokenDisponible = signal(false);
  readonly estado = signal('loading');
  readonly error = signal('');
  readonly busy = signal(false);
  readonly form;
  constructor(
    fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly location: Location,
    private readonly service: AdministradorService,
  ) {
    this.form = fb.nonNullable.group(
      {
        nombres: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/\S/)]],
        apellidos: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/\S/)]],
        ci: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9-]{4,20}$/)]],
        celular: ['', [Validators.required, Validators.pattern(/^[0-9+ -]{7,20}$/)]],
        contrasena: [
          '',
          [
            Validators.required,
            Validators.minLength(8),
            Validators.maxLength(100),
            Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).+$/),
          ],
        ],
        confirmacionContrasena: ['', Validators.required],
      },
      {
        validators: (c: AbstractControl) =>
          c.get('contrasena')?.value === c.get('confirmacionContrasena')?.value
            ? null
            : { contrasenasDistintas: true },
      },
    );
  }
  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token')?.trim() ?? '';
    this.tokenDisponible.set(!!this.token);
    this.location.replaceState('/auth/activar-administrador'); // Remove secret from browser history; memory only.
    this.consultar();
  }
  ngOnDestroy() {
    this.token = '';
    this.form.reset();
  }
  consultar() {
    if (!this.token) {
      this.estado.set('error');
      this.error.set('El enlace no contiene una invitación válida.');
      return;
    }
    this.estado.set('loading');
    this.error.set('');
    this.service.consultar(this.token).subscribe({
      next: (r) => {
        this.estado.set(r.estado);
        if (r.estado !== 'PENDIENTE') this.limpiarToken();
      },
      error: (e) => {
        this.estado.set('error');
        this.error.set(apiErrorMessage(e, 'No fue posible comprobar la invitación.'));
        if (e.status !== 0 && e.status < 500) this.limpiarToken();
      },
    });
  }
  aceptar() {
    if (this.busy()) return;
    if (this.form.invalid || !this.token) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.service.aceptar(this.token, this.form.getRawValue()).subscribe({
      next: () => {
        this.busy.set(false);
        this.estado.set('success');
        this.limpiarToken();
        this.form.reset();
      },
      error: (e) => {
        this.busy.set(false);
        this.error.set(apiErrorMessage(e, 'No fue posible activar la cuenta.'));
        if (e.error?.codigo === 'ADMIN_INVITATION_INVALID') {
          this.estado.set('error');
          this.limpiarToken();
        }
      },
    });
  }
  private limpiarToken() {
    this.token = '';
    this.tokenDisponible.set(false);
  }
}
