import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { EventoService } from '../../../core/services/evento.service';
import { InscripcionService } from '../../../core/services/inscripcion.service';
import { EstadoEvento, EventoDetalle, Modalidad, PublicoObjetivo, TipoCertificadoEvento } from '../../../core/models/evento.model';
import { EstadoInscripcion, Inscripcion } from '../../../core/models/inscripcion.model';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

interface DetalleState {
  evento?: EventoDetalle;
  loading: boolean;
  notFound: boolean;
  loadError?: string;
  inscripcion?: Inscripcion;
  loadingInscripcion: boolean;
  errorInscripcion?: string;
}

@Component({
  selector: 'app-evento-detalle-publico',
  templateUrl: './evento-detalle-publico.component.html',
  styleUrl: './evento-detalle-publico.component.css',
  standalone: false
})
export class EventoDetallePublicoComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly state = signal<DetalleState>({ loading: true, notFound: false, loadingInscripcion: false });
  private readonly eventoId = inject(ActivatedRoute).snapshot.paramMap.get('id');

  get evento(): EventoDetalle | undefined { return this.state().evento; }
  get loading(): boolean { return this.state().loading; }
  get notFound(): boolean { return this.state().notFound; }
  get loadError(): string | undefined { return this.state().loadError; }
  get inscripcion(): Inscripcion | undefined { return this.state().inscripcion; }
  get loadingInscripcion(): boolean { return this.state().loadingInscripcion; }
  get errorInscripcion(): string | undefined { return this.state().errorInscripcion; }

  constructor(
    private readonly router: Router,
    private readonly eventoService: EventoService,
    private readonly inscripcionService: InscripcionService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    if (!this.eventoId) {
      this.state.update((state) => ({ ...state, loading: false, notFound: true }));
      return;
    }
    this.cargarEvento();
  }

  cargarEvento(): void {
    if (!this.eventoId) return;
    this.state.set({ loading: true, notFound: false, loadingInscripcion: false });
    this.eventoService.obtenerPorId(this.eventoId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (evento) => {
        this.state.update((state) => ({ ...state, evento, loading: false }));
        if (this.esParticipante) this.cargarInscripcion();
      },
      error: (error: { status?: number }) => {
        this.state.update((state) => ({
          ...state,
          loading: false,
          notFound: error.status === 404,
          loadError: error.status === 404 ? undefined : apiErrorMessage(error, 'No fue posible cargar el evento')
        }));
      }
    });
  }

  cargarInscripcion(): void {
    if (!this.esParticipante || !this.evento) return;
    this.state.update((state) => ({ ...state, loadingInscripcion: true, errorInscripcion: undefined }));
    this.inscripcionService.listarMisInscripciones().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (inscripciones) => this.state.update((state) => ({
        ...state,
        inscripcion: inscripciones.find((inscripcion) => inscripcion.eventoId === this.evento?.id),
        loadingInscripcion: false
      })),
      error: (error) => this.state.update((state) => ({
        ...state,
        loadingInscripcion: false,
        errorInscripcion: apiErrorMessage(error, 'No fue posible consultar tu inscripción')
      }))
    });
  }

  get esParticipante(): boolean {
    return this.authService.isAuthenticated() && this.authService.hasAnyRole(['USUARIO']);
  }

  get esOtroRolAutenticado(): boolean {
    return this.authService.isAuthenticated() && !this.esParticipante;
  }

  get estadoEventoLabel(): string {
    return this.evento ? this.etiquetasEstadoEvento[this.evento.estado] ?? this.evento.estado : '';
  }

  get estadoEventoVariant(): 'neutral' | 'success' | 'warning' | 'danger' | 'info' {
    switch (this.evento?.estado) {
      case EstadoEvento.PUBLICADO: return 'success';
      case EstadoEvento.FINALIZADO: return 'info';
      case EstadoEvento.CANCELADO:
      case EstadoEvento.RECHAZADO: return 'danger';
      case EstadoEvento.EN_REVISION: return 'warning';
      default: return 'neutral';
    }
  }

  get modalidadLabel(): string {
    return this.evento?.modalidad === Modalidad.VIRTUAL ? 'Virtual' : 'Presencial';
  }

  get audienciaLabel(): string {
    if (!this.evento) return '';
    return this.etiquetasAudiencia[this.evento.publicoObjetivo] ?? this.evento.publicoObjetivo;
  }

  get certificadoLabel(): string | undefined {
    if (!this.evento?.emiteCertificado || !this.evento.tipoCertificado) return undefined;
    return this.evento.tipoCertificado === TipoCertificadoEvento.CURRICULAR ? 'Curricular' : 'No curricular';
  }

  get disponibilidad(): string | undefined {
    if (!this.evento) return undefined;
    if (!this.evento.cupoLimitado) return 'Sin límite de cupos';
    if (this.evento.cupoDisponible === null || this.evento.cupoDisponible === undefined) return 'Cupo limitado';
    if (this.evento.cupoMaximo === null || this.evento.cupoMaximo === undefined) {
      return this.evento.cupoDisponible === 0 ? 'Sin cupos disponibles' : `${this.evento.cupoDisponible} cupos disponibles`;
    }
    return this.evento.cupoDisponible === 0
      ? 'Sin cupos disponibles'
      : `${this.evento.cupoDisponible} cupos disponibles de ${this.evento.cupoMaximo}`;
  }

  get puedeVerEnlaceVirtual(): boolean {
    return Boolean(this.evento?.modalidad === Modalidad.VIRTUAL && this.evento.enlaceVirtual &&
      (!this.evento.requiereInscripcion || this.inscripcion?.estado === EstadoInscripcion.CONFIRMADA));
  }

  get accionInscripcionLabel(): string {
    return this.inscripcion ? 'Ver mi inscripción' : 'Inscribirme';
  }

  get estadoInscripcionLabel(): string {
    switch (this.inscripcion?.estado) {
      case EstadoInscripcion.PENDIENTE_PAGO: return 'Pendiente de pago';
      case EstadoInscripcion.PENDIENTE_VALIDACION: return 'Comprobante en revisión';
      case EstadoInscripcion.CONFIRMADA: return 'Confirmada';
      case EstadoInscripcion.CANCELADA: return 'Cancelada';
      default: return '';
    }
  }

  get varianteInscripcion(): 'success' | 'warning' | 'danger' | 'info' | undefined {
    switch (this.inscripcion?.estado) {
      case EstadoInscripcion.CONFIRMADA: return 'success';
      case EstadoInscripcion.PENDIENTE_PAGO:
      case EstadoInscripcion.PENDIENTE_VALIDACION: return 'warning';
      case EstadoInscripcion.CANCELADA: return 'danger';
      default: return undefined;
    }
  }

  abrirAccionInscripcion(): void {
    if (!this.evento) return;
    if (this.inscripcion) {
      void this.router.navigate(['/privado/inscripciones', this.inscripcion.id]);
      return;
    }
    void this.router.navigate(['/eventos', this.evento.id, 'inscripcion']);
  }

  volverAlCatalogo(): void {
    void this.router.navigate(['/eventos']);
  }

  private readonly etiquetasEstadoEvento: Record<EstadoEvento, string> = {
    [EstadoEvento.BORRADOR]: 'Borrador',
    [EstadoEvento.EN_REVISION]: 'En revisión',
    [EstadoEvento.PUBLICADO]: 'Publicado',
    [EstadoEvento.FINALIZADO]: 'Finalizado',
    [EstadoEvento.RECHAZADO]: 'Rechazado',
    [EstadoEvento.CANCELADO]: 'Cancelado'
  };

  private readonly etiquetasAudiencia: Record<PublicoObjetivo, string> = {
    [PublicoObjetivo.UAJMS]: 'Comunidad UAJMS',
    [PublicoObjetivo.EXTERNO]: 'Participantes externos',
    [PublicoObjetivo.AMBOS]: 'Comunidad UAJMS y público externo'
  };
}
