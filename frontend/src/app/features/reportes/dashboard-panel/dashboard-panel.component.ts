import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import {
  DashboardAcademicoResponse,
  DashboardEjecutivoResponse,
  DashboardOperativoResponse
} from '../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard-panel',
  templateUrl: './dashboard-panel.component.html',
  styleUrl: './dashboard-panel.component.css',
  standalone: false
})
export class DashboardPanelComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly dashboardService = inject(DashboardService);
  private readonly authService = inject(AuthService);

  readonly ejecutivo = signal<DashboardEjecutivoResponse | null>(null);
  readonly academico = signal<DashboardAcademicoResponse | null>(null);
  readonly operativo = signal<DashboardOperativoResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly esOrganizador = this.authService.hasAnyRole(['ORGANIZADOR']);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(false);
    forkJoin({
      ejecutivo: this.dashboardService.obtenerEjecutivo(),
      academico: this.dashboardService.obtenerAcademico(),
      operativo: this.dashboardService.obtenerOperativo()
    }).pipe(
      finalize(() => this.loading.set(false)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ({ ejecutivo, academico, operativo }) => {
        this.ejecutivo.set(ejecutivo);
        this.academico.set(academico);
        this.operativo.set(operativo);
      },
      error: () => this.error.set(true)
    });
  }

  get rutaEventos(): string {
    return this.esOrganizador ? '/organizador/eventos' : '/admin/eventos';
  }

  get rutaPagos(): string {
    return this.esOrganizador ? '/organizador/pagos/validar' : '/admin/pagos/validar';
  }
}
