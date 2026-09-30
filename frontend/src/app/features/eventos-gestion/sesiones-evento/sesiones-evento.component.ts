import { Component, DestroyRef, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import QRCode from 'qrcode';

import { Evento, Modalidad } from '../../../core/models/evento.model';
import { QrAsistencia, SesionEvento, SesionEventoRequest } from '../../../core/models/asistencia.model';
import { AsistenciaService } from '../../../core/services/asistencia.service';
import { ToastService } from '../../../shared/ui/toast.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

@Component({
  selector: 'app-sesiones-evento',
  templateUrl: './sesiones-evento.component.html',
  standalone: false
})
export class SesionesEventoComponent implements OnChanges {
  @Input({ required: true }) evento!: Evento;

  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  readonly sesiones = signal<SesionEvento[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal(false);
  readonly editandoId = signal<string | null>(null);
  readonly generandoQrId = signal<string | null>(null);
  readonly qrActual = signal<QrAsistencia | null>(null);
  readonly qrDataUrl = signal<string | null>(null);

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: [''],
    fecha: ['', Validators.required],
    horaInicio: ['', Validators.required],
    horaFin: ['', Validators.required],
    requiereAsistencia: [true, Validators.required],
    latitud: [null as number | null],
    longitud: [null as number | null],
    radioMetros: [100, [Validators.required, Validators.min(1), Validators.max(500)]],
    activa: [true, Validators.required]
  });

  constructor(
    private readonly asistenciaService: AsistenciaService,
    private readonly toast: ToastService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['evento'] && this.evento?.id) {
      this.inicializarFormulario();
      this.cargar();
    }
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(false);
    this.asistenciaService.listarSesiones(this.evento.id).pipe(
      finalize(() => this.loading.set(false)), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (sesiones) => this.sesiones.set(sesiones),
      error: (err) => {
        this.error.set(true);
        this.toast.error(apiErrorMessage(err, 'No fue posible cargar las sesiones'));
      }
    });
  }

  guardar(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    const request = this.form.getRawValue() as SesionEventoRequest;
    const operacion = this.editandoId()
      ? this.asistenciaService.actualizarSesion(this.editandoId()!, request)
      : this.asistenciaService.crearSesion(this.evento.id, request);
    this.saving.set(true);
    operacion.pipe(finalize(() => this.saving.set(false)), takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toast.success(this.editandoId() ? 'Sesion actualizada' : 'Sesion creada');
        this.cancelarEdicion();
        this.cargar();
      },
      error: (err) => this.toast.error(apiErrorMessage(err, 'No fue posible guardar la sesion'))
    });
  }

  editar(sesion: SesionEvento): void {
    this.editandoId.set(sesion.id);
    this.form.reset({
      nombre: sesion.nombre,
      descripcion: sesion.descripcion ?? '',
      fecha: sesion.fecha,
      horaInicio: sesion.horaInicio.substring(0, 5),
      horaFin: sesion.horaFin.substring(0, 5),
      requiereAsistencia: sesion.requiereAsistencia,
      latitud: sesion.latitud,
      longitud: sesion.longitud,
      radioMetros: sesion.radioMetros,
      activa: sesion.activa
    });
  }

  cancelarEdicion(): void {
    this.editandoId.set(null);
    this.inicializarFormulario();
  }

  cambiarEstado(sesion: SesionEvento): void {
    if (this.saving()) return;
    this.saving.set(true);
    this.asistenciaService.cambiarEstado(sesion.id, !sesion.activa).pipe(
      finalize(() => this.saving.set(false)), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => this.cargar(),
      error: (err) => this.toast.error(apiErrorMessage(err, 'No fue posible cambiar el estado'))
    });
  }

  generarQr(sesion: SesionEvento): void {
    if (this.generandoQrId()) return;
    this.generandoQrId.set(sesion.id);
    this.qrActual.set(null);
    this.qrDataUrl.set(null);
    this.asistenciaService.generarQr(sesion.id).pipe(
      finalize(() => this.generandoQrId.set(null)), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: async (qr) => {
        if (!qr.token) return;
        this.qrActual.set(qr);
        this.qrDataUrl.set(await QRCode.toDataURL(qr.token, { width: 280, margin: 2, errorCorrectionLevel: 'M' }));
      },
      error: (err) => this.toast.error(apiErrorMessage(err, 'No fue posible generar el QR'))
    });
  }

  private inicializarFormulario(): void {
    const presencial = this.evento.modalidad === Modalidad.PRESENCIAL;
    this.form.reset({
      nombre: '', descripcion: '', fecha: this.evento.fechaInicio,
      horaInicio: this.evento.horaInicio?.substring(0, 5) ?? '',
      horaFin: this.evento.horaFin?.substring(0, 5) ?? '',
      requiereAsistencia: true,
      latitud: presencial ? (this.evento.latitud ?? null) : null,
      longitud: presencial ? (this.evento.longitud ?? null) : null,
      radioMetros: this.evento.radioMetros ?? 100,
      activa: true
    });
  }
}
