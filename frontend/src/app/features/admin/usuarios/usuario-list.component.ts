import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';

import { UsuarioAdmin } from '../../../core/models/usuario-admin.model';
import { UsuarioAdminService } from '../../../core/services/usuario-admin.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { SkeletonComponent } from '../../../shared/ui/skeleton/skeleton.component';

@Component({
  selector: 'app-usuario-list',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule,
    MatTableModule, AlertComponent, EmptyStateComponent, PageHeaderComponent, SkeletonComponent],
  templateUrl: './usuario-list.component.html',
  styleUrl: './usuario-list.component.css'
})
export class UsuarioListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly service = inject(UsuarioAdminService);

  readonly usuarios = signal<UsuarioAdmin[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly busqueda = signal('');
  readonly usuariosFiltrados = computed(() => {
    const query = this.busqueda().trim().toLocaleLowerCase();
    if (!query) return this.usuarios();
    return this.usuarios().filter((usuario) => [
      usuario.nombres, usuario.apellidos, usuario.correoElectronico, usuario.ci ?? '', usuario.ru ?? '',
      ...usuario.roles
    ].some((value) => value.toLocaleLowerCase().includes(query)));
  });
  readonly columns = ['usuario', 'tipo', 'correo', 'rol', 'solicitud', 'acciones'];

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set('');
    this.service.listar().pipe(
      finalize(() => this.loading.set(false)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (usuarios) => this.usuarios.set(usuarios),
      error: (error) => this.error.set(apiErrorMessage(error, 'No fue posible cargar el directorio de usuarios.'))
    });
  }
}
