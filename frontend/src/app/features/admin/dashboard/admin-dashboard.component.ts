import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { DashboardService } from '../../../core/services/dashboard.service';
import { EventoService } from '../../../core/services/evento.service';
import { PagoService } from '../../../core/services/pago.service';
import { DashboardAcademicoResponse, DashboardEjecutivoResponse, DashboardOperativoResponse } from '../../../core/models/dashboard.model';
import { Evento } from '../../../core/models/evento.model';
import { Pago } from '../../../core/models/pago.model';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { SkeletonComponent } from '../../../shared/ui/skeleton/skeleton.component';
import { StatCardComponent } from '../../../shared/ui/stat-card/stat-card.component';
import { SurfaceComponent } from '../../../shared/ui/surface/surface.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule, RouterLink, MatButtonModule, MatIconModule, AlertComponent,
    EmptyStateComponent, PageHeaderComponent, SkeletonComponent, StatCardComponent, SurfaceComponent
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly dashboardService = inject(DashboardService);
  private readonly eventoService = inject(EventoService);
  private readonly pagoService = inject(PagoService);

  readonly loading = signal(true);
  readonly error = signal(false);
  readonly ejecutivo = signal<DashboardEjecutivoResponse | null>(null);
  readonly academico = signal<DashboardAcademicoResponse | null>(null);
  readonly operativo = signal<DashboardOperativoResponse | null>(null);
  readonly eventos = signal<Evento[]>([]);
  readonly pagosPendientes = signal<Pago[]>([]);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(false);
    forkJoin({
      ejecutivo: this.dashboardService.obtenerEjecutivo(),
      academico: this.dashboardService.obtenerAcademico(),
      operativo: this.dashboardService.obtenerOperativo(),
      eventos: this.eventoService.listar(),
      pagos: this.pagoService.listarPendientes()
    }).pipe(
      finalize(() => this.loading.set(false)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (data) => {
        this.ejecutivo.set(data.ejecutivo);
        this.academico.set(data.academico);
        this.operativo.set(data.operativo);
        this.eventos.set(data.eventos);
        this.pagosPendientes.set(data.pagos);
      },
      error: () => this.error.set(true)
    });
  }

  get eventosMostrados(): Evento[] {
    return this.eventos().slice(0, 4);
  }

  get pagosMostrados(): Pago[] {
    return this.pagosPendientes().slice(0, 4);
  }
}
