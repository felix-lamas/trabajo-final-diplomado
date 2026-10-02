import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';

import { UsuarioAdmin } from '../../../core/models/usuario-admin.model';
import { UsuarioAdminService } from '../../../core/services/usuario-admin.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { SkeletonComponent } from '../../../shared/ui/skeleton/skeleton.component';
import { SurfaceComponent } from '../../../shared/ui/surface/surface.component';

@Component({
  selector: 'app-usuario-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, AlertComponent, EmptyStateComponent, PageHeaderComponent, SkeletonComponent, SurfaceComponent],
  templateUrl: './usuario-detail.component.html',
  styleUrl: './usuario-detail.component.css'
})
export class UsuarioDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(UsuarioAdminService);

  readonly usuario = signal<UsuarioAdmin | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.loading.set(false);
      this.error.set('No se indicó un identificador de usuario válido.');
      return;
    }
    this.loading.set(true);
    this.error.set('');
    this.service.obtenerPorId(id).pipe(
      finalize(() => this.loading.set(false)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (usuario) => this.usuario.set(usuario),
      error: (err) => this.error.set(apiErrorMessage(err, 'No fue posible cargar los datos del usuario.'))
    });
  }
}
