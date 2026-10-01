import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { finalize, forkJoin } from 'rxjs';

import { InscripcionService } from '../../../../core/services/inscripcion.service';
import { ComprobanteInscripcion, DetalleInscripcion } from '../../../../core/models/inscripcion.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-inscripcion-detail',
  templateUrl: './inscripcion-detail.component.html',
  standalone: false
})
export class InscripcionDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly viewState = signal<{
    inscripcion?: DetalleInscripcion;
    comprobante?: ComprobanteInscripcion;
    loading: boolean;
    procesando: boolean;
  }>({ loading: true, procesando: false });
  get inscripcion(): DetalleInscripcion | undefined { return this.viewState().inscripcion; }
  private set inscripcion(value: DetalleInscripcion | undefined) { this.viewState.update((state) => ({ ...state, inscripcion: value })); }
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
  get comprobante(): ComprobanteInscripcion | undefined { return this.viewState().comprobante; }
  private set comprobante(value: ComprobanteInscripcion | undefined) { this.viewState.update((state) => ({ ...state, comprobante: value })); }
  get procesando(): boolean { return this.viewState().procesando; }
  private set procesando(value: boolean) { this.viewState.update((state) => ({ ...state, procesando: value })); }

  estadoPagoLabel(estado?: string): string {
    switch (estado) {
      case 'NO_APLICA': return 'No aplica';
      case 'PENDIENTE_PAGO': return 'Pendiente de pago';
      case 'PENDIENTE_VALIDACION': return 'Pendiente de validación';
      case 'APROBADO': return 'Aprobado';
      case 'RECHAZADO': return 'Rechazado';
      default: return estado || 'Sin información de pago';
    }
  }

  estadoInscripcionLabel(estado: string): string {
    switch (estado) {
      case 'PENDIENTE_PAGO': return 'Pendiente de pago';
      case 'PENDIENTE_VALIDACION': return 'Pendiente de validación';
      case 'CONFIRMADA': return 'Confirmada';
      case 'CANCELADA': return 'Cancelada';
      default: return estado;
    }
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private inscripcionService: InscripcionService,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarDetalle(id);
    }
  }

  cargarDetalle(id: string): void {
    this.loading = true;
    forkJoin({
      inscripcion: this.inscripcionService.obtenerPorId(id),
      comprobante: this.inscripcionService.obtenerComprobante(id)
    }).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.loading = false))
      .subscribe({
        next: ({ inscripcion, comprobante }) => {
          this.inscripcion = inscripcion;
          this.comprobante = comprobante;
        },
        error: (err) => {
          this.toast.error(apiErrorMessage(err, 'Error al cargar el detalle de la inscripcion'));
          this.router.navigate(['/privado/inscripciones']);
        }
      });
  }

  cancelar(): void {
    if (!this.inscripcion || this.procesando) return;

    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Cancelar inscripcion',
        message: `Se cancelara tu inscripcion al evento "${this.inscripcion.eventoTitulo}".`,
        confirmText: 'Cancelar inscripcion',
        tone: 'danger'
      }
    }).afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe((confirmed) => {
      if (!confirmed || !this.inscripcion) return;

      this.procesando = true;
      this.inscripcionService.cancelar(this.inscripcion.id).pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.procesando = false)
      ).subscribe({
        next: () => {
          this.toast.success('Inscripcion cancelada correctamente');
          this.cargarDetalle(this.inscripcion!.id);
        },
        error: (err) => this.toast.error(err.error?.mensaje || 'Error al cancelar')
      });
    });
  }
}
