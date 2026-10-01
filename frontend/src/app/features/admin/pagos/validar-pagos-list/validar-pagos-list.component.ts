import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { finalize } from 'rxjs';

import { PagoService } from '../../../../core/services/pago.service';
import { Pago } from '../../../../core/models/pago.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { EmptyStateComponent } from '../../../../shared/ui/empty-state/empty-state.component';
import { SkeletonComponent } from '../../../../shared/ui/skeleton/skeleton.component';
import { ToastService } from '../../../../shared/ui/toast.service';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-validar-pagos-list',
  templateUrl: './validar-pagos-list.component.html',
  standalone: true,
  imports: [
    CommonModule, MatButtonModule, MatCardModule, MatIconModule, MatTableModule, MatTooltipModule,
    EmptyStateComponent, SkeletonComponent
  ]
})
export class ValidarPagosListComponent implements OnInit {
  private readonly viewState = signal({ pagos: [] as Pago[], loading: true, error: '', procesandoId: '' });
  get pagos(): Pago[] { return this.viewState().pagos; }
  private set pagos(value: Pago[]) { this.viewState.update((state) => ({ ...state, pagos: value })); }
  displayedColumns: string[] = ['usuario', 'evento', 'monto', 'fecha', 'acciones'];
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
  get error(): string { return this.viewState().error; }
  get procesandoId(): string { return this.viewState().procesandoId; }

  constructor(
    private pagoService: PagoService,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarPendientes();
  }

  cargarPendientes(): void {
    this.loading = true;
    this.viewState.update((state) => ({ ...state, error: '' }));
    this.pagoService.listarPendientes()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (data) => this.pagos = data,
        error: (err) => this.viewState.update((state) => ({ ...state, error: apiErrorMessage(err, 'No fue posible cargar los pagos pendientes.') }))
      });
  }

  validar(pago: Pago): void {
    if (this.procesandoId) return;
    this.viewState.update((state) => ({ ...state, procesandoId: pago.id }));
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Validar pago',
        message: `Confirme que el comprobante de ${pago.usuarioNombre} coincide con el monto y el evento.`,
        confirmText: 'Validar',
        tone: 'primary'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        this.liberarPago(pago.id);
        return;
      }

      const entrada = prompt('Observación (opcional, máximo 1000 caracteres):');
      if (entrada === null) {
        this.liberarPago(pago.id);
        return;
      }
      const obs = entrada.trim();
      if (obs.length > 1000) {
        this.toast.warning('La observación no puede superar 1000 caracteres.');
        this.liberarPago(pago.id);
        return;
      }

      this.pagoService.validarPago(pago.id, { observacion: obs })
        .pipe(finalize(() => this.liberarPago(pago.id))).subscribe({
        next: () => {
          this.toast.success('Pago validado correctamente');
          this.cargarPendientes();
        },
        error: (err) => this.toast.error(apiErrorMessage(err, 'No fue posible validar el pago.'))
      });
    });
  }

  rechazar(pago: Pago): void {
    if (this.procesandoId) return;
    this.viewState.update((state) => ({ ...state, procesandoId: pago.id }));
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Rechazar pago',
        message: `El participante recibira el rechazo del comprobante asociado a "${pago.eventoTitulo}".`,
        confirmText: 'Rechazar',
        tone: 'danger'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        this.liberarPago(pago.id);
        return;
      }

      const entrada = prompt('Motivo del rechazo (obligatorio, máximo 1000 caracteres):');
      const obs = entrada?.trim();
      if (obs && obs.length <= 1000) {
        this.pagoService.rechazarPago(pago.id, { observacion: obs })
          .pipe(finalize(() => this.liberarPago(pago.id))).subscribe({
          next: () => {
            this.toast.warning('Pago rechazado');
            this.cargarPendientes();
          },
          error: (err) => this.toast.error(apiErrorMessage(err, 'No fue posible rechazar el pago.'))
        });
      } else if (entrada !== null) {
        this.toast.warning('Debe indicar un motivo para rechazar el pago');
        this.liberarPago(pago.id);
      } else {
        this.liberarPago(pago.id);
      }
    });
  }

  verComprobante(pago: Pago): void {
    if (pago.comprobante) {
      this.pagoService.descargarComprobante(pago.id).subscribe({
        next: (archivo) => {
          const url = URL.createObjectURL(archivo);
          const contentType = archivo.type || pago.comprobante?.tipoContenido;
          const extension = contentType === 'application/pdf' ? 'pdf' : contentType === 'image/png' ? 'png' : contentType === 'image/jpeg' ? 'jpg' : 'bin';
          const link = document.createElement('a');
          link.href = url;
          link.download = `comprobante-${pago.id}.${extension}`;
          link.click();
          URL.revokeObjectURL(url);
        },
        error: () => this.toast.error('No fue posible descargar el comprobante')
      });
    }
  }

  private liberarPago(id: string): void {
    if (this.procesandoId === id) {
      this.viewState.update((state) => ({ ...state, procesandoId: '' }));
    }
  }
}
