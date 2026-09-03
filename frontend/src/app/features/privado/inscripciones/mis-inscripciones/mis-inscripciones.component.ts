import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { InscripcionService } from '../../../../core/services/inscripcion.service';
import { CredencialService } from '../../../../core/services/credencial.service';
import { Inscripcion } from '../../../../core/models/inscripcion.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';

@Component({
  selector: 'app-mis-inscripciones',
  templateUrl: './mis-inscripciones.component.html',
  standalone: false
})
export class MisInscripcionesComponent implements OnInit {
  inscripciones: Inscripcion[] = [];
  displayedColumns: string[] = ['evento', 'fecha', 'estado', 'acciones'];
  estadoFiltro = 'TODAS';
  loading = true;

  constructor(
    private inscripcionService: InscripcionService,
    private credencialService: CredencialService,
    private dialog: MatDialog,
    private toast: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarInscripciones();
  }

  cargarInscripciones(): void {
    this.loading = true;
    this.inscripcionService.listarMisInscripciones()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (data) => this.inscripciones = data,
        error: () => this.toast.error('Error al cargar tus inscripciones')
      });
  }

  get inscripcionesFiltradas(): Inscripcion[] {
    if (this.estadoFiltro === 'TODAS') {
      return this.inscripciones;
    }

    return this.inscripciones.filter((inscripcion) => inscripcion.estado === this.estadoFiltro);
  }

  get totalInscripciones(): number {
    return this.inscripciones.length;
  }

  get totalConfirmadas(): number {
    return this.inscripciones.filter((inscripcion) => inscripcion.estado === 'CONFIRMADA').length;
  }

  get totalCredenciales(): number {
    return this.inscripciones.filter((inscripcion) => inscripcion.estado === 'CONFIRMADA' || inscripcion.estado === 'ASISTIO').length;
  }

  cambiarFiltro(estado: string): void {
    this.estadoFiltro = estado;
  }

  getEstadoClass(estado: string): string {
    switch (estado) {
      case 'CONFIRMADA': return 'bg-green-100 text-green-800';
      case 'PENDIENTE_PAGO': return 'bg-yellow-100 text-yellow-800';
      case 'PENDIENTE_VALIDACION': return 'bg-blue-100 text-blue-800';
      case 'CANCELADA': return 'bg-red-100 text-red-800';
      case 'RECHAZADA': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  puedeCancelar(estado: string): boolean {
    return estado === 'PENDIENTE_PAGO' || estado === 'PENDIENTE_VALIDACION' || estado === 'CONFIRMADA';
  }

  getEstadoLabel(estado: string): string {
    switch (estado) {
      case 'CONFIRMADA': return 'Confirmada';
      case 'PENDIENTE_PAGO': return 'Pendiente de pago';
      case 'PENDIENTE_VALIDACION': return 'Pendiente de validacion';
      case 'CANCELADA': return 'Cancelada';
      case 'RECHAZADA': return 'Rechazada';
      case 'ASISTIO': return 'Asistio';
      default: return estado;
    }
  }

  cancelar(inscripcion: Inscripcion): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Cancelar inscripcion',
        message: `Se cancelara tu inscripcion al evento "${inscripcion.eventoTitulo}".`,
        confirmText: 'Cancelar inscripcion',
        tone: 'danger'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      this.inscripcionService.cancelar(inscripcion.id).subscribe({
        next: () => {
          this.toast.success('Inscripcion cancelada correctamente');
          this.cargarInscripciones();
        },
        error: (err) => this.toast.error(err.error?.mensaje || 'Error al cancelar')
      });
    });
  }

  generarCredencial(inscripcion: Inscripcion): void {
    this.credencialService.generarCredencial(inscripcion.id).subscribe({
      next: (credencial) => {
        this.toast.success('Credencial generada con exito');
        this.router.navigate(['/privado/credenciales', credencial.id]);
      },
      error: (err) => {
        if (err.status === 400 && err.error?.mensaje?.includes('Ya existe')) {
          this.toast.info('La credencial ya fue generada. Puedes verla en Mis Credenciales', 'Ir', 5000)
            .onAction()
            .subscribe(() => this.router.navigate(['/privado/credenciales']));
          return;
        }

        this.toast.error(err.error?.mensaje || 'Error al generar credencial');
      }
    });
  }
}
