import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { EmptyStateComponent } from '../../../../shared/ui/empty-state/empty-state.component';
import {
  EstadoSolicitudOrganizador,
  SolicitudOrganizador,
  SolicitudOrganizadorService
} from '../../../../core/services/solicitud-organizador.service';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';
import { RechazarSolicitudDialogComponent } from '../rechazar-solicitud-dialog/rechazar-solicitud-dialog.component';

@Component({
  selector: 'app-solicitudes-organizador',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTooltipModule,
    EmptyStateComponent
  ],
  templateUrl: './solicitudes-organizador.component.html'
})
export class SolicitudesOrganizadorComponent implements OnInit {
  readonly solicitudes = signal<SolicitudOrganizador[]>([]);
  readonly filtro = signal<EstadoSolicitudOrganizador>('PENDIENTE');
  readonly loading = signal(true);
  readonly error = signal('');
  readonly procesandoId = signal<string | null>(null);
  readonly estados: EstadoSolicitudOrganizador[] = ['PENDIENTE', 'APROBADA', 'RECHAZADA'];

  constructor(
    private readonly service: SolicitudOrganizadorService,
    private readonly dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cambiarFiltro(estado: EstadoSolicitudOrganizador): void {
    this.filtro.set(estado);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set('');
    this.service.listar(this.filtro()).subscribe({
      next: (solicitudes) => {
        this.solicitudes.set(solicitudes);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.error.set(apiErrorMessage(error, 'No fue posible cargar las solicitudes.'));
      }
    });
  }

  aprobar(solicitud: SolicitudOrganizador): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Aprobar organizador',
        message: `Se agregara el rol ORGANIZADOR a ${solicitud.nombres} ${solicitud.apellidos}, conservando USUARIO y su actividad como participante.`,
        confirmText: 'Aprobar',
        tone: 'primary'
      }
    }).afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.procesandoId.set(solicitud.usuarioId);
      this.service.aprobar(solicitud.usuarioId).subscribe({
        next: (resultado) => this.aplicarResultado(resultado),
        error: (error) => this.finalizarConError(error)
      });
    });
  }

  rechazar(solicitud: SolicitudOrganizador): void {
    this.dialog.open(RechazarSolicitudDialogComponent, {
      width: 'min(480px, 92vw)',
      data: { nombre: `${solicitud.nombres} ${solicitud.apellidos}` }
    }).afterClosed().subscribe((motivo) => {
      if (!motivo) {
        return;
      }
      this.procesandoId.set(solicitud.usuarioId);
      this.service.rechazar(solicitud.usuarioId, motivo).subscribe({
        next: (resultado) => this.aplicarResultado(resultado),
        error: (error) => this.finalizarConError(error)
      });
    });
  }

  private aplicarResultado(resultado: SolicitudOrganizador): void {
    this.solicitudes.update((actuales) => resultado.estado === this.filtro()
      ? actuales.map((item) => item.usuarioId === resultado.usuarioId ? resultado : item)
      : actuales.filter((item) => item.usuarioId !== resultado.usuarioId));
    this.procesandoId.set(null);
  }

  private finalizarConError(error: unknown): void {
    this.procesandoId.set(null);
    this.error.set(apiErrorMessage(error, 'No fue posible resolver la solicitud.'));
  }
}
