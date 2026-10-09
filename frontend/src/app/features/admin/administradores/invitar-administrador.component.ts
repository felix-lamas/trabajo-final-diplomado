import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AdministradorService } from '../../../core/services/administrador.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

@Component({
  selector: 'app-invitar-administrador',
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
  template: `<h1>Invitar administrador</h1>
    <p>
      Una cuenta independiente, exclusivamente administrativa. El destinatario establecerá su
      contraseña.
    </p>
    <mat-card
      ><mat-card-content>
        @if (success()) {
          <p role="status">Invitación enviada. El destinatario recibirá el enlace de activaci?n.</p>
        } @else {
          <form [formGroup]="form" (ngSubmit)="enviar()">
            <mat-form-field
              ><mat-label>Nombres</mat-label
              ><input
                matInput
                formControlName="nombres"
                autocomplete="given-name"
                maxlength="50"
                required
            /></mat-form-field>
            <mat-form-field
              ><mat-label>Apellidos</mat-label
              ><input
                matInput
                formControlName="apellidos"
                autocomplete="family-name"
                maxlength="50"
                required
            /></mat-form-field>
            <mat-form-field
              ><mat-label>Correo</mat-label
              ><input
                matInput
                type="email"
                formControlName="correo"
                autocomplete="email"
                maxlength="100"
                required
            /></mat-form-field>
            <mat-form-field
              ><mat-label>CI</mat-label
              ><input matInput formControlName="ci" maxlength="20" required /><mat-hint
                >4 a 20 caracteres: letras, números o guiones.</mat-hint
              ></mat-form-field
            >
            <mat-form-field
              ><mat-label>Celular</mat-label
              ><input
                matInput
                type="tel"
                formControlName="celular"
                autocomplete="tel"
                maxlength="20"
                required
            /></mat-form-field>
            @if (form.invalid && form.touched) {
              <p role="alert">Revise los campos obligatorios y sus formatos.</p>
            }
            @if (error()) {
              <p role="alert">{{ error() }}</p>
            }
            <button mat-flat-button type="submit" [disabled]="busy()">
              {{ busy() ? 'Enviando?' : 'Enviar invitación' }}
            </button>
          </form>
        }
        <a mat-button routerLink="/admin/administradores">Volver a administradores</a>
      </mat-card-content></mat-card
    >`,
  styles: [
    `
      mat-card {
        max-width: 44rem;
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
export class InvitarAdministradorComponent {
  readonly form;
  readonly busy = signal(false);
  readonly success = signal(false);
  readonly error = signal('');
  constructor(
    fb: FormBuilder,
    private readonly service: AdministradorService,
  ) {
    this.form = fb.nonNullable.group({
      nombres: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/\S/)]],
      apellidos: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/\S/)]],
      correo: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      ci: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9-]{4,20}$/)]],
      celular: ['', [Validators.required, Validators.pattern(/^[0-9+ -]{7,20}$/)]],
    });
  }
  enviar() {
    if (this.busy()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.service.invitar(this.form.getRawValue()).subscribe({
      next: () => {
        this.busy.set(false);
        this.success.set(true);
      },
      error: (e) => {
        this.busy.set(false);
        this.error.set(apiErrorMessage(e, 'No fue posible enviar la invitación.'));
      },
    });
  }
}
