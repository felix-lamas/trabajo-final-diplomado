import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { EventoService } from '../../../core/services/evento.service';
import { InscripcionService } from '../../../core/services/inscripcion.service';
import { PagoService } from '../../../core/services/pago.service';
import { CredencialService } from '../../../core/services/credencial.service';
import { Evento } from '../../../core/models/evento.model';
import { Inscripcion } from '../../../core/models/inscripcion.model';
import { EstadoPago, Pago } from '../../../core/models/pago.model';
import { Credencial } from '../../../core/models/credencial.model';
import { DashboardEjecutivoResponse } from '../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard-privado',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard-privado.component.html'
})
export class DashboardPrivadoComponent implements OnInit {
  private readonly viewState = signal<{
    loading: boolean;
    ejecutivo?: DashboardEjecutivoResponse;
    inscripciones: Inscripcion[];
    pagos: Pago[];
    credenciales: Credencial[];
    recomendados: Evento[];
  }>({ loading: true, inscripciones: [], pagos: [], credenciales: [], recomendados: [] });
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
  get ejecutivo(): DashboardEjecutivoResponse | undefined { return this.viewState().ejecutivo; }
  private set ejecutivo(value: DashboardEjecutivoResponse | undefined) { this.viewState.update((state) => ({ ...state, ejecutivo: value })); }
  get inscripciones(): Inscripcion[] { return this.viewState().inscripciones; }
  private set inscripciones(value: Inscripcion[]) { this.viewState.update((state) => ({ ...state, inscripciones: value })); }
  get pagos(): Pago[] { return this.viewState().pagos; }
  private set pagos(value: Pago[]) { this.viewState.update((state) => ({ ...state, pagos: value })); }
  get credenciales(): Credencial[] { return this.viewState().credenciales; }
  private set credenciales(value: Credencial[]) { this.viewState.update((state) => ({ ...state, credenciales: value })); }
  get recomendados(): Evento[] { return this.viewState().recomendados; }
  private set recomendados(value: Evento[]) { this.viewState.update((state) => ({ ...state, recomendados: value })); }

  constructor(
    private readonly authService: AuthService,
    private readonly dashboardService: DashboardService,
    private readonly eventoService: EventoService,
    private readonly inscripcionService: InscripcionService,
    private readonly pagoService: PagoService,
    private readonly credencialService: CredencialService
  ) {}

  ngOnInit(): void {
    forkJoin({
      ejecutivo: this.dashboardService.obtenerEjecutivo(),
      eventos: this.eventoService.listarPublicados(),
      inscripciones: this.inscripcionService.listarMisInscripciones(),
      pagos: this.pagoService.listarMisPagos(),
      credenciales: this.credencialService.listarMisCredenciales()
    }).subscribe({
      next: (data) => {
        this.ejecutivo = data.ejecutivo;
        this.recomendados = data.eventos.slice(0, 3);
        this.inscripciones = data.inscripciones;
        this.pagos = data.pagos;
        this.credenciales = data.credenciales;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get userName(): string {
    const user = this.authService.getUser();
    return user ? `${user.nombres} ${user.apellidos}`.trim() : 'Estudiante';
  }

  get userRole(): string {
    return this.authService.getRoles()[0] ?? 'Usuario';
  }

  get initial(): string {
    return this.userName.charAt(0).toUpperCase();
  }

  get pagosValidados(): number {
    return this.pagos.filter((pago) => pago.estado === EstadoPago.APROBADO).length;
  }

  get pagosPendientes(): number {
    return this.pagos.filter((pago) =>
      pago.estado === EstadoPago.PENDIENTE_PAGO || pago.estado === EstadoPago.PENDIENTE_VALIDACION).length;
  }

  get siguienteEvento(): Evento | undefined {
    return this.recomendados[0];
  }

  get actividad(): Array<{ titulo: string; detalle: string; tipo: string; fecha: Date | string }> {
    const actividadInscripciones = this.inscripciones.slice(0, 2).map((inscripcion) => ({
      titulo: inscripcion.eventoTitulo,
      detalle: `Inscripcion ${inscripcion.estado.toLowerCase()}`,
      tipo: 'Inscripcion',
      fecha: inscripcion.fechaInscripcion
    }));

    const actividadPagos = this.pagos.slice(0, 2).map((pago) => ({
      titulo: pago.eventoTitulo,
      detalle: `Pago ${pago.estado.toLowerCase()} por Bs. ${pago.monto}`,
      tipo: 'Pago',
      fecha: pago.fechaPago
    }));

    return [...actividadInscripciones, ...actividadPagos].slice(0, 4);
  }
}
