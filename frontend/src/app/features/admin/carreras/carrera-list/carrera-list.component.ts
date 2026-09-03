import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { finalize } from 'rxjs';

import { CarreraService } from '../../../../core/services/carrera.service';
import { Carrera } from '../../../../core/models/carrera.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';

@Component({
  selector: 'app-carrera-list',
  templateUrl: './carrera-list.component.html',
  standalone: false
})
export class CarreraListComponent implements OnInit {
  carreras: Carrera[] = [];
  displayedColumns: string[] = ['nombre', 'facultad', 'estado', 'acciones'];
  loading = true;

  constructor(
    private carreraService: CarreraService,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarCarreras();
  }

  cargarCarreras(): void {
    this.loading = true;
    this.carreraService.listar()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (data) => this.carreras = data,
        error: () => this.toast.error('Error al cargar carreras')
      });
  }

  get activas(): number {
    return this.carreras.filter((carrera) => carrera.estado === 'ACTIVO').length;
  }

  get inactivas(): number {
    return this.carreras.filter((carrera) => carrera.estado !== 'ACTIVO').length;
  }

  desactivar(carrera: Carrera): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Desactivar carrera',
        message: `La carrera "${carrera.nombre}" dejara de estar disponible para nuevos registros.`,
        confirmText: 'Desactivar',
        tone: 'warning'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      this.carreraService.eliminar(carrera.id).subscribe({
        next: () => {
          this.toast.success('Carrera desactivada correctamente');
          this.cargarCarreras();
        },
        error: () => this.toast.error('Error al desactivar carrera')
      });
    });
  }
}
