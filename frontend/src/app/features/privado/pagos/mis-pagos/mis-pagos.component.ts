import { Component, OnInit, signal } from '@angular/core';
import { PagoService } from '../../../../core/services/pago.service';
import { EstadoPago, Pago } from '../../../../core/models/pago.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-mis-pagos',
  templateUrl: './mis-pagos.component.html',
  standalone: false
})
export class MisPagosComponent implements OnInit {
  private readonly viewState = signal({ pagos: [] as Pago[], loading: true, error: '', downloadingId: '' });
  get pagos(): Pago[] { return this.viewState().pagos; }
  private set pagos(value: Pago[]) { this.viewState.update((state) => ({ ...state, pagos: value })); }
  get loading(): boolean { return this.viewState().loading; }
  get error(): string { return this.viewState().error; }
  get downloadingId(): string { return this.viewState().downloadingId; }
  displayedColumns: string[] = ['evento', 'monto', 'fecha', 'estado', 'acciones'];
  estadoFiltro = 'TODOS';

  constructor(
    private pagoService: PagoService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cargarPagos();
  }

  cargarPagos(): void {
    this.viewState.update((state) => ({ ...state, loading: true, error: '' }));
    this.pagoService.listarMisPagos().pipe(
      finalize(() => this.viewState.update((state) => ({ ...state, loading: false })))
    ).subscribe({
      next: (data) => this.pagos = data,
      error: (err) => this.viewState.update((state) => ({ ...state, error: apiErrorMessage(err, 'No fue posible cargar sus pagos.') }))
    });
  }

  get pagosFiltrados(): Pago[] {
    if (this.estadoFiltro === 'TODOS') {
      return this.pagos;
    }

    return this.pagos.filter((pago) => pago.estado === this.estadoFiltro);
  }

  get totalPagos(): number {
    return this.pagos.length;
  }

  get totalValidados(): number {
    return this.pagos.filter((pago) => pago.estado === EstadoPago.APROBADO).length;
  }

  get totalPendientes(): number {
    return this.pagos.filter((pago) =>
      pago.estado === EstadoPago.PENDIENTE_PAGO || pago.estado === EstadoPago.PENDIENTE_VALIDACION).length;
  }

  cambiarFiltro(estado: string): void {
    this.estadoFiltro = estado;
  }

  getEstadoClass(estado: string): string {
    switch (estado) {
      case 'APROBADO': return 'bg-green-100 text-green-800';
      case 'PENDIENTE_PAGO':
      case 'PENDIENTE_VALIDACION': return 'bg-yellow-100 text-yellow-800';
      case 'RECHAZADO': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getEstadoLabel(estado: string): string {
    switch (estado) {
      case 'APROBADO': return 'Aprobado';
      case 'PENDIENTE_PAGO': return 'Pendiente de pago';
      case 'PENDIENTE_VALIDACION': return 'Pendiente de validación';
      case 'RECHAZADO': return 'Rechazado';
      default: return estado;
    }
  }

  verComprobante(pago: Pago): void {
    if (!pago.comprobante || this.downloadingId) return;
    this.viewState.update((state) => ({ ...state, downloadingId: pago.id }));
    this.pagoService.descargarComprobante(pago.id).pipe(
      finalize(() => this.viewState.update((state) => ({ ...state, downloadingId: '' })))
    ).subscribe({
      next: (archivo) => {
        const contentType = archivo.type || pago.comprobante?.tipoContenido;
        const extension = contentType === 'application/pdf' ? 'pdf' : contentType === 'image/png' ? 'png' : contentType === 'image/jpeg' ? 'jpg' : 'bin';
        const url = URL.createObjectURL(archivo);
        const link = document.createElement('a');
        link.href = url;
        link.download = `comprobante-${pago.id}.${extension}`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => this.snackBar.open(apiErrorMessage(err, 'No fue posible descargar el comprobante'), 'Cerrar')
    });
  }

  puedePresentar(pago: Pago): boolean {
    return pago.estado === EstadoPago.PENDIENTE_PAGO || pago.estado === EstadoPago.RECHAZADO;
  }
}
