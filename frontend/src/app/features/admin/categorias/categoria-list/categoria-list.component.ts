import { Component, OnInit, computed, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { finalize } from 'rxjs';

import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { CategoriaEvento } from '../../../../core/models/categoria-evento.model';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';

@Component({
  selector: 'app-categoria-list',
  templateUrl: './categoria-list.component.html',
  standalone: false
})
export class CategoriaListComponent implements OnInit {
  readonly categorias = signal<CategoriaEvento[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly eliminandoId = signal<string | null>(null);
  readonly activas = computed(() => this.categorias().filter((categoria) => categoria.estado === 'ACTIVO').length);
  readonly inactivas = computed(() => this.categorias().length - this.activas());
  readonly displayedColumns: string[] = ['nombre', 'descripcion', 'estado', 'acciones'];

  constructor(
    private categoriaService: CategoriaEventoService,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarCategorias();
  }

  cargarCategorias(): void {
    this.loading.set(true);
    this.error.set(null);
    this.categoriaService.listar()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (data) => this.categorias.set(data),
        error: (error) => this.error.set(apiErrorMessage(error, 'No fue posible cargar las categorías.'))
      });
  }

  eliminar(categoria: CategoriaEvento): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Eliminar categoría',
        message: `La categoría "${categoria.nombre}" dejará de estar disponible. No podrá eliminarse si tiene eventos asociados.`,
        confirmText: 'Eliminar',
        tone: 'warning'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      this.eliminandoId.set(categoria.id);
      this.error.set(null);
      this.categoriaService.eliminar(categoria.id)
        .pipe(finalize(() => this.eliminandoId.set(null)))
        .subscribe({
          next: () => {
            this.categorias.update((categorias) => categorias.filter((item) => item.id !== categoria.id));
            this.toast.success('Categoría eliminada correctamente');
          },
          error: (error) => {
            const mensaje = apiErrorMessage(error, 'No fue posible eliminar la categoría.');
            this.error.set(mensaje);
            this.toast.error(mensaje);
          }
        });
    });
  }
}
