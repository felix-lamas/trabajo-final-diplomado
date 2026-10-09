import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialog } from '@angular/material/dialog';
import { forkJoin } from 'rxjs';
import {
  Administrador,
  InvitacionAdministrador,
  AdministradorService,
} from '../../../core/services/administrador.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';
import { ConfirmDialogComponent } from '../../../shared/ui/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-administradores',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatCardModule, MatProgressBarModule],
  template: `
    <header class="page-header">
      <div>
        <h1>Administradores</h1>
        <p>Cuentas administrativas e invitaciones de Vidia.</p>
      </div>
      <a mat-flat-button routerLink="invitar">Invitar administrador</a>
    </header>
    @if (loading()) {
      <mat-progress-bar mode="indeterminate" aria-label="Cargando administradores" />
    }
    @if (error()) {
      <p role="alert">{{ error() }}</p>
      <button mat-button (click)="cargar()">Reintentar</button>
    }
    @if (mensaje()) {
      <p role="status">{{ mensaje() }}</p>
    }
    <h2>Cuentas administrativas</h2>
    @for (admin of administradores(); track admin.id) {
      <mat-card class="account"
        ><mat-card-content>
          <h3>{{ admin.nombres }} {{ admin.apellidos }}</h3>
          <p>{{ admin.correo }}</p>
          <p>
            Estado: {{ admin.activo ? 'Activo' : 'Inactivo' }} · Creación:
            {{ admin.fechaCreacion | date: 'short' }}
          </p>
          @if (admin.activo && admin.correo !== 'admin@demo.local') {
            <button mat-button [disabled]="!!procesando()" (click)="desactivar(admin)">
              Desactivar
            </button>
          }
        </mat-card-content></mat-card
      >
    } @empty {
      @if (!loading() && !error()) {
        <p>No hay administradores para mostrar.</p>
      }
    }
    <h2>Invitaciones</h2>
    @for (i of invitaciones(); track i.id) {
      <mat-card class="account"
        ><mat-card-content
          ><h3>{{ i.nombres }} {{ i.apellidos }}</h3>
          <p>{{ i.correo }}</p>
          <p>
            Estado: {{ i.estado }} · Creación: {{ i.fechaCreacion | date: 'short' }} · Expira:
            {{ i.fechaExpiracion | date: 'short' }}
          </p>
          @if (i.estado === 'PENDIENTE' || i.estado === 'EXPIRADA') {
            <button mat-button [disabled]="!!procesando()" (click)="reenviar(i)">
              Reenviar invitación
            </button>
          }
          @if (i.estado === 'PENDIENTE') {
            <button mat-button [disabled]="!!procesando()" (click)="revocar(i)">Revocar</button>
          }
        </mat-card-content></mat-card
      >
    } @empty {
      @if (!loading() && !error()) {
        <p>No hay invitaciones.</p>
      }
    }
  `,
  styles: [
    `
      .page-header {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        flex-wrap: wrap;
        margin-bottom: 1.5rem;
      }
      .account {
        margin-bottom: 1rem;
        overflow-wrap: anywhere;
      }
      h2 {
        margin-top: 2rem;
      }
    `,
  ],
})
export class AdministradoresComponent implements OnInit {
  readonly administradores = signal<Administrador[]>([]);
  readonly invitaciones = signal<InvitacionAdministrador[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly mensaje = signal('');
  readonly procesando = signal('');
  constructor(
    private readonly service: AdministradorService,
    private readonly dialog: MatDialog,
  ) {}
  ngOnInit() {
    this.cargar();
  }
  cargar() {
    this.loading.set(true);
    this.error.set('');
    forkJoin({
      admins: this.service.listar(),
      invitaciones: this.service.invitaciones(),
    }).subscribe({
      next: (r) => {
        this.administradores.set(r.admins);
        this.invitaciones.set(r.invitaciones);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(apiErrorMessage(e, 'No fue posible cargar administradores.'));
        this.loading.set(false);
      },
    });
  }
  reenviar(i: InvitacionAdministrador) {
    this.ejecutar(
      i.id,
      () => this.service.reenviar(i.id),
      'Invitación reenviada. El enlace anterior ya no es válido.',
    );
  }
  revocar(i: InvitacionAdministrador) {
    this.confirmar('Revocar invitación', 'El enlace dejar? de ser válido.', () =>
      this.ejecutar(i.id, () => this.service.revocar(i.id), 'Invitación revocada.'),
    );
  }
  desactivar(a: Administrador) {
    this.confirmar(
      'Desactivar administrador',
      'Se cerrarán sus sesiones y no podrá acceder. El último administrador activo está protegido.',
      () => this.ejecutar(a.id, () => this.service.desactivar(a.id), 'Administrador desactivado.'),
    );
  }
  private confirmar(title: string, message: string, accion: () => void) {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: { title, message, confirmText: 'Confirmar', tone: 'primary' },
      })
      .afterClosed()
      .subscribe((ok) => {
        if (ok) accion();
      });
  }
  private ejecutar(id: string, accion: () => import('rxjs').Observable<unknown>, mensaje: string) {
    if (this.procesando()) return;
    this.procesando.set(id);
    this.error.set('');
    this.mensaje.set('');
    accion().subscribe({
      next: () => {
        this.procesando.set('');
        this.mensaje.set(mensaje);
        this.cargar();
      },
      error: (e) => {
        this.procesando.set('');
        this.error.set(apiErrorMessage(e, 'No fue posible completar la operación.'));
      },
    });
  }
}
