import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { DashboardService } from '../../../core/services/dashboard.service';
import { EventoService } from '../../../core/services/evento.service';
import { PagoService } from '../../../core/services/pago.service';
import { DashboardAcademicoResponse, DashboardEjecutivoResponse, DashboardOperativoResponse } from '../../../core/models/dashboard.model';
import { Evento } from '../../../core/models/evento.model';
import { Pago } from '../../../core/models/pago.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatProgressBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  loading = true;
  ejecutivo?: DashboardEjecutivoResponse;
  academico?: DashboardAcademicoResponse;
  operativo?: DashboardOperativoResponse;
  eventos: Evento[] = [];
  pagosPendientes: Pago[] = [];

  constructor(
    private readonly dashboardService: DashboardService,
    private readonly eventoService: EventoService,
    private readonly pagoService: PagoService
  ) {}

  ngOnInit(): void {
    forkJoin({
      ejecutivo: this.dashboardService.obtenerEjecutivo(),
      academico: this.dashboardService.obtenerAcademico(),
      operativo: this.dashboardService.obtenerOperativo(),
      eventos: this.eventoService.listar(),
      pagos: this.pagoService.listarPendientes()
    }).subscribe({
      next: (data) => {
        this.ejecutivo = data.ejecutivo;
        this.academico = data.academico;
        this.operativo = data.operativo;
        this.eventos = data.eventos;
        this.pagosPendientes = data.pagos;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get kpis(): Array<{ label: string; value: number | string; icon: string; tone: string }> {
    if (!this.ejecutivo || !this.operativo) {
      return [];
    }

    return [
      { label: 'Eventos', value: this.ejecutivo.totalEventos, icon: 'event', tone: 'blue' },
      { label: 'Usuarios', value: this.ejecutivo.totalUsuarios, icon: 'groups', tone: 'teal' },
      { label: 'Inscripciones', value: this.ejecutivo.totalInscripciones, icon: 'how_to_reg', tone: 'violet' },
      { label: 'Pagos validados', value: this.operativo.pagosValidados, icon: 'payments', tone: 'green' },
      { label: 'QR utilizados', value: this.operativo.qrUtilizados, icon: 'qr_code_scanner', tone: 'amber' },
      { label: 'Satisfaccion', value: `${this.ejecutivo.nivelSatisfaccion}%`, icon: 'favorite', tone: 'pink' }
    ];
  }

  get alerts(): Array<{ label: string; value: number; tone: string }> {
    if (!this.operativo) {
      return [];
    }

    return [
      { label: 'Pagos pendientes', value: this.operativo.pagosPendientes, tone: 'warning' },
      { label: 'Eventos activos', value: this.operativo.eventosActivos, tone: 'info' },
      { label: 'Eventos finalizados', value: this.operativo.eventosFinalizados, tone: 'success' }
    ];
  }

  get recommendedActions(): Array<{ label: string; icon: string; route: string; description: string }> {
    return [
      { label: 'Gestionar eventos', icon: 'event_note', route: '/admin/eventos', description: 'Publicacion, edicion y cierre operativo.' },
      { label: 'Validar pagos', icon: 'verified', route: '/admin/pagos/validar', description: 'Revision rapida de comprobantes.' },
      { label: 'Facultades', icon: 'domain', route: '/admin/facultades', description: 'Estructura academica institucional.' },
      { label: 'Carreras', icon: 'account_tree', route: '/admin/carreras', description: 'Catalogo academico actualizado.' },
      { label: 'Categorias', icon: 'label', route: '/admin/categorias', description: 'Clasificacion de eventos.' }
    ];
  }

  get recentEvents(): Evento[] {
    return this.eventos.slice(0, 4);
  }

  get recentPayments(): Pago[] {
    return this.pagosPendientes.slice(0, 4);
  }

  progress(value: number, max: number): number {
    if (max <= 0) {
      return 0;
    }

    return Math.min(100, (value / max) * 100);
  }
}
