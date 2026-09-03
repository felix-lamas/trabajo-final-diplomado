import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { finalize } from 'rxjs';

import { PagoService } from '../../../../core/services/pago.service';
import { Pago } from '../../../../core/models/pago.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';

@Component({
  selector: 'app-validar-pagos-list',
  templateUrl: './validar-pagos-list.component.html',
  standalone: false
})
export class ValidarPagosListComponent implements OnInit {
  pagos: Pago[] = [];
  displayedColumns: string[] = ['usuario', 'evento', 'monto', 'fecha', 'acciones'];
  loading = true;

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
    this.pagoService.listarPendientes()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (data) => this.pagos = data,
        error: () => this.toast.error('Error al cargar pagos pendientes')
      });
  }

  validar(pago: Pago): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Validar pago',
        message: `Confirme que el comprobante de ${pago.usuarioNombre} coincide con el monto y el evento.`,
        confirmText: 'Validar',
        tone: 'primary'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      const obs = prompt('Observacion (opcional):');
      if (obs === null) return;

      this.pagoService.validarPago(pago.id, { observacion: obs }).subscribe({
        next: () => {
          this.toast.success('Pago validado correctamente');
          this.cargarPendientes();
        },
        error: (err) => this.toast.error(err.error?.mensaje || 'Error al validar')
      });
    });
  }

  rechazar(pago: Pago): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Rechazar pago',
        message: `El participante recibira el rechazo del comprobante asociado a "${pago.eventoTitulo}".`,
        confirmText: 'Rechazar',
        tone: 'danger'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      const obs = prompt('Motivo del rechazo (obligatorio):');
      if (obs) {
        this.pagoService.rechazarPago(pago.id, { observacion: obs }).subscribe({
          next: () => {
            this.toast.warning('Pago rechazado');
            this.cargarPendientes();
          },
          error: (err) => this.toast.error(err.error?.mensaje || 'Error al rechazar')
        });
      } else if (obs === '') {
        this.toast.warning('Debe indicar un motivo para rechazar el pago');
      }
    });
  }

  verComprobante(pago: Pago): void {
    if (pago.comprobante) {
      window.open(pago.comprobante.urlArchivo, '_blank', 'noopener,noreferrer');
    }
  }
}
