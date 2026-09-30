import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin, Observable, of, switchMap } from 'rxjs';
import { EstadoInscripcion } from '../../../../core/models/inscripcion.model';
import { EstadoPago, Pago } from '../../../../core/models/pago.model';
import { InscripcionService } from '../../../../core/services/inscripcion.service';
import { PagoService } from '../../../../core/services/pago.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-registrar-pago',
  templateUrl: './registrar-pago.component.html',
  standalone: false
})
export class RegistrarPagoComponent implements OnInit {
  private static readonly MAX_FILE_SIZE = 5 * 1024 * 1024;
  private static readonly ALLOWED_TYPES = new Set(['image/jpeg', 'image/png', 'application/pdf']);

  inscripcionId = '';
  archivoComprobante: File | null = null;
  private pago?: Pago;
  private readonly viewState = signal({
    eventoTitulo: '', monto: 0, loading: true, enviando: false, error: ''
  });
  get eventoTitulo(): string { return this.viewState().eventoTitulo; }
  get monto(): number { return this.viewState().monto; }
  get loading(): boolean { return this.viewState().loading; }
  get enviando(): boolean { return this.viewState().enviando; }
  get error(): string { return this.viewState().error; }
  get motivoRechazo(): string | undefined { return this.pago?.motivoRechazo; }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pagoService: PagoService,
    private inscripcionService: InscripcionService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.inscripcionId = this.route.snapshot.queryParams['inscripcionId'] || '';
    if (!this.inscripcionId) {
      void this.router.navigate(['/privado/inscripciones']);
      return;
    }
    this.cargarContexto();
  }

  cargarContexto(): void {
    this.patchState({ loading: true, error: '' });
    forkJoin({
      inscripcion: this.inscripcionService.obtenerPorId(this.inscripcionId),
      comprobanteInscripcion: this.inscripcionService.obtenerComprobante(this.inscripcionId),
      pagos: this.pagoService.listarMisPagos()
    }).pipe(finalize(() => this.patchState({ loading: false }))).subscribe({
      next: ({ inscripcion, comprobanteInscripcion, pagos }) => {
        this.pago = pagos.find((pago) => pago.inscripcionId === this.inscripcionId);
        const estadoPagoValido = !this.pago
          || this.pago.estado === EstadoPago.PENDIENTE_PAGO
          || this.pago.estado === EstadoPago.RECHAZADO;
        if (inscripcion.estado !== EstadoInscripcion.PENDIENTE_PAGO || !estadoPagoValido) {
          this.patchState({ error: 'Esta inscripción no admite una nueva presentación de comprobante.' });
          return;
        }
        this.patchState({ eventoTitulo: inscripcion.eventoTitulo, monto: this.pago?.monto ?? comprobanteInscripcion.monto });
      },
      error: () => this.patchState({ error: 'No fue posible cargar el pago asociado a la inscripción.' })
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) return;
    if (!RegistrarPagoComponent.ALLOWED_TYPES.has(file.type)) {
      this.archivoComprobante = null;
      input.value = '';
      this.snackBar.open('Solo se permiten archivos JPG, PNG o PDF', 'Cerrar');
      return;
    }
    if (file.size > RegistrarPagoComponent.MAX_FILE_SIZE) {
      this.archivoComprobante = null;
      input.value = '';
      this.snackBar.open('El archivo supera el tamaño máximo de 5 MB', 'Cerrar');
      return;
    }
    this.archivoComprobante = file;
  }

  guardar(): void {
    if (this.enviando || !this.archivoComprobante || this.error) return;
    this.patchState({ enviando: true });
    const archivo = this.archivoComprobante;
    this.obtenerOCrearPago().pipe(
      switchMap((pago) => this.pagoService.subirComprobante(pago.id, archivo)),
      finalize(() => this.patchState({ enviando: false }))
    ).subscribe({
      next: () => {
        this.snackBar.open('Comprobante presentado para validación', 'Cerrar', { duration: 3000 });
        void this.router.navigate(['/privado/pagos']);
      },
      error: (err) => this.snackBar.open(err.error?.mensaje || 'No fue posible presentar el comprobante', 'Cerrar')
    });
  }

  private obtenerOCrearPago(): Observable<Pago> {
    return this.pago ? of(this.pago) : this.pagoService.registrarPago({ inscripcionId: this.inscripcionId });
  }

  private patchState(patch: Partial<ReturnType<typeof this.viewState>>): void {
    this.viewState.update((state) => ({ ...state, ...patch }));
  }
}
