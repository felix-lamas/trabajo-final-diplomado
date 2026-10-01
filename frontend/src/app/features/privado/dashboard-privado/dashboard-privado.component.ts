import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { MatIconModule } from '@angular/material/icon';

import { AuthService } from '../../../core/services/auth.service';
import { CertificadoService } from '../../../core/services/certificado.service';
import { EventoService } from '../../../core/services/evento.service';
import { InscripcionService } from '../../../core/services/inscripcion.service';
import { PagoService } from '../../../core/services/pago.service';
import { CertificadoResponse } from '../../../core/models/certificado.model';
import { Evento } from '../../../core/models/evento.model';
import { EstadoInscripcion, Inscripcion } from '../../../core/models/inscripcion.model';
import { EstadoPago, Pago } from '../../../core/models/pago.model';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { EventCardComponent } from '../../../shared/ui/event-card/event-card.component';
import { SkeletonComponent } from '../../../shared/ui/skeleton/skeleton.component';
import { StatCardComponent } from '../../../shared/ui/stat-card/stat-card.component';
import { SurfaceComponent } from '../../../shared/ui/surface/surface.component';

interface DashboardActivity {
  id: string;
  title: string;
  detail: string;
  type: 'Inscripción' | 'Pago' | 'Certificado';
  date: string;
  icon: string;
  route: string;
}

interface DashboardData {
  eventos: Evento[] | null;
  inscripciones: Inscripcion[] | null;
  pagos: Pago[] | null;
  certificados: CertificadoResponse[] | null;
}

@Component({
  selector: 'app-dashboard-privado',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatIconModule,
    AlertComponent,
    EmptyStateComponent,
    EventCardComponent,
    SkeletonComponent,
    StatCardComponent,
    SurfaceComponent
  ],
  templateUrl: './dashboard-privado.component.html',
  styleUrl: './dashboard-privado.component.css'
})
export class DashboardPrivadoComponent implements OnInit {
  readonly loading = signal(true);
  readonly eventsError = signal(false);
  readonly registrationsError = signal(false);
  readonly paymentsError = signal(false);
  readonly certificatesError = signal(false);
  readonly eventos = signal<Evento[]>([]);
  readonly inscripciones = signal<Inscripcion[]>([]);
  readonly pagos = signal<Pago[]>([]);
  readonly certificados = signal<CertificadoResponse[]>([]);

  readonly proximosEventos = computed(() => {
    const now = Date.now();
    return this.eventos()
      .filter((event) => new Date(`${event.fechaInicio}T${event.horaInicio || '00:00'}`).getTime() >= now)
      .sort((a, b) => this.eventStart(a) - this.eventStart(b));
  });
  readonly proximoEvento = computed(() => this.proximosEventos()[0]);
  readonly otrosEventos = computed(() => this.proximosEventos().slice(1, 4));
  readonly certificadosVigentes = computed(() => this.certificados().filter((certificate) => certificate.estado !== 'ANULADO'));
  readonly activity = computed(() => this.buildActivity());

  constructor(
    private readonly authService: AuthService,
    private readonly eventoService: EventoService,
    private readonly inscripcionService: InscripcionService,
    private readonly pagoService: PagoService,
    private readonly certificadoService: CertificadoService
  ) {}

  ngOnInit(): void {
    forkJoin({
      eventos: this.eventoService.listarPublicados().pipe(catchError(() => of(null))),
      inscripciones: this.inscripcionService.listarMisInscripciones().pipe(catchError(() => of(null))),
      pagos: this.pagoService.listarMisPagos().pipe(catchError(() => of(null))),
      certificados: this.certificadoService.listarMisCertificados().pipe(catchError(() => of(null)))
    }).subscribe((data: DashboardData) => {
      this.eventsError.set(data.eventos === null);
      this.registrationsError.set(data.inscripciones === null);
      this.paymentsError.set(data.pagos === null);
      this.certificatesError.set(data.certificados === null);
      this.eventos.set(data.eventos ?? []);
      this.inscripciones.set(data.inscripciones ?? []);
      this.pagos.set(data.pagos ?? []);
      this.certificados.set(data.certificados ?? []);
      this.loading.set(false);
    });
  }

  get userName(): string {
    const user = this.authService.getUser();
    return user ? `${user.nombres} ${user.apellidos}`.trim() : 'Usuario';
  }

  get approvedPayments(): number {
    return this.pagos().filter((payment) => payment.estado === EstadoPago.APROBADO).length;
  }

  get registrationCount(): number | string {
    return this.registrationsError() ? '—' : this.inscripciones().length;
  }

  get paymentCount(): number | string {
    return this.paymentsError() ? '—' : this.approvedPayments;
  }

  get certificateCount(): number | string {
    return this.certificatesError() ? '—' : this.certificadosVigentes().length;
  }

  registrationFor(eventId: string): Inscripcion | undefined {
    return this.inscripciones().find((registration) => registration.eventoId === eventId);
  }

  get hasActivityError(): boolean {
    return this.registrationsError() || this.paymentsError() || this.certificatesError();
  }

  private eventStart(event: Evento): number {
    return new Date(`${event.fechaInicio}T${event.horaInicio || '00:00'}`).getTime();
  }

  private buildActivity(): DashboardActivity[] {
    const registrationItems = this.inscripciones().map((registration) => ({
      id: `registration-${registration.id}`,
      title: registration.eventoTitulo,
      detail: `Inscripción ${this.registrationStatus(registration.estado)}`,
      type: 'Inscripción' as const,
      date: registration.fechaInscripcion,
      icon: 'event_available',
      route: `/privado/inscripciones/${registration.id}`
    }));
    const paymentItems = this.pagos().map((payment) => ({
      id: `payment-${payment.id}`,
      title: payment.eventoTitulo,
      detail: `Pago ${payment.estado.toLowerCase().replaceAll('_', ' ')}`,
      type: 'Pago' as const,
      date: payment.fechaPago,
      icon: 'payments',
      route: '/privado/pagos'
    }));
    const certificateItems = this.certificados()
      .filter((certificate) => certificate.estado !== 'ANULADO')
      .map((certificate) => ({
        id: `certificate-${certificate.id}`,
        title: certificate.evento,
        detail: `Certificado ${certificate.tipoCertificado === 'CURRICULAR' ? 'curricular' : 'no curricular'} emitido`,
        type: 'Certificado' as const,
        date: certificate.fechaEmision,
        icon: 'workspace_premium',
        route: '/certificados/mis-certificados'
      }));

    return [...registrationItems, ...paymentItems, ...certificateItems]
      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
      .slice(0, 5);
  }

  private registrationStatus(status: EstadoInscripcion): string {
    return status.toLowerCase().replaceAll('_', ' ');
  }
}
