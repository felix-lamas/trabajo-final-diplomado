import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { finalize } from 'rxjs';

import { FacultadService } from '../../../../core/services/facultad.service';
import { Facultad } from '../../../../core/models/facultad.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';

@Component({
  selector: 'app-facultad-list',
  templateUrl: './facultad-list.component.html',
  standalone: false
})
export class FacultadListComponent implements OnInit {
  facultades: Facultad[] = [];
  displayedColumns: string[] = ['nombre', 'descripcion', 'estado', 'fechaCreacion', 'acciones'];
  loading = true;

  constructor(
    private facultadService: FacultadService,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarFacultades();
  }

  cargarFacultades(): void {
    this.loading = true;
    this.facultadService.listar()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (data) => this.facultades = data,
        error: () => this.toast.error('Error al cargar facultades')
      });
  }

  get activas(): number {
    return this.facultades.filter((facultad) => facultad.estado === 'ACTIVO').length;
  }

  get inactivas(): number {
    return this.facultades.filter((facultad) => facultad.estado !== 'ACTIVO').length;
  }

  desactivar(facultad: Facultad): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Desactivar facultad',
        message: `La facultad "${facultad.nombre}" dejara de estar disponible para nuevos registros.`,
        confirmText: 'Desactivar',
        tone: 'warning'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      this.facultadService.eliminar(facultad.id).subscribe({
        next: () => {
          this.toast.success('Facultad desactivada correctamente');
          this.cargarFacultades();
        },
        error: () => this.toast.error('Error al desactivar facultad')
      });
    });
  }
}
