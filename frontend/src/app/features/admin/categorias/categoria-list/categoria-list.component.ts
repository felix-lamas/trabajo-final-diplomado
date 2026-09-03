import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { finalize } from 'rxjs';

import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { CategoriaEvento } from '../../../../core/models/categoria-evento.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';

@Component({
  selector: 'app-categoria-list',
  templateUrl: './categoria-list.component.html',
  standalone: false
})
export class CategoriaListComponent implements OnInit {
  categorias: CategoriaEvento[] = [];
  displayedColumns: string[] = ['nombre', 'descripcion', 'estado', 'acciones'];
  loading = true;

  constructor(
    private categoriaService: CategoriaEventoService,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarCategorias();
  }

  cargarCategorias(): void {
    this.loading = true;
    this.categoriaService.listar()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (data) => this.categorias = data,
        error: () => this.toast.error('Error al cargar categorias')
      });
  }

  get activas(): number {
    return this.categorias.filter((categoria) => categoria.estado === 'ACTIVO').length;
  }

  get inactivas(): number {
    return this.categorias.filter((categoria) => categoria.estado !== 'ACTIVO').length;
  }

  desactivar(cat: CategoriaEvento): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Desactivar categoria',
        message: `La categoria "${cat.nombre}" dejara de estar disponible para clasificar nuevos eventos.`,
        confirmText: 'Desactivar',
        tone: 'warning'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      this.categoriaService.eliminar(cat.id).subscribe({
        next: () => {
          this.toast.success('Categoria desactivada correctamente');
          this.cargarCategorias();
        },
        error: () => this.toast.error('Error al desactivar categoria')
      });
    });
  }
}
