import { Component, OnInit } from '@angular/core';
import { PagoService } from '../../../../core/services/pago.service';
import { Pago, EstadoPago } from '../../../../core/models/pago.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-mis-pagos',
  templateUrl: './mis-pagos.component.html',
  standalone: false
})
export class MisPagosComponent implements OnInit {
  pagos: Pago[] = [];
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
    this.pagoService.listarMisPagos().subscribe({
      next: (data) => this.pagos = data,
      error: () => this.snackBar.open('Error al cargar tus pagos', 'Cerrar', { duration: 3000 })
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
    return this.pagos.filter((pago) => pago.estado === 'VALIDADO').length;
  }

  get totalPendientes(): number {
    return this.pagos.filter((pago) => pago.estado === 'PENDIENTE').length;
  }

  cambiarFiltro(estado: string): void {
    this.estadoFiltro = estado;
  }

  getEstadoClass(estado: string): string {
    switch (estado) {
      case 'VALIDADO': return 'bg-green-100 text-green-800';
      case 'PENDIENTE': return 'bg-yellow-100 text-yellow-800';
      case 'RECHAZADO': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getEstadoLabel(estado: string): string {
    switch (estado) {
      case 'VALIDADO': return 'Validado';
      case 'PENDIENTE': return 'Pendiente';
      case 'RECHAZADO': return 'Rechazado';
      default: return estado;
    }
  }

  verComprobante(pago: Pago): void {
    if (pago.comprobante) {
      window.open(pago.comprobante.urlArchivo, '_blank');
    }
  }
}
