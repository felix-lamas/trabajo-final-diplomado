import { Component, DestroyRef, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
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
  readonly mapsLink = new FormControl('', { nonNullable: true });
  readonly mapsLinkMessage = signal('');
  readonly mapsLinkResolving = signal(false);

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

  obtenerCoordenadas(): void {
    const result = this.extraerCoordenadas(this.mapsLink.value);
    if (result.ok === false) {
      if (this.esEnlaceCortoGoogleMaps(this.mapsLink.value)) {
        this.resolverEnlaceCortoGoogleMaps();
        return;
      }
      this.mapsLinkMessage.set(result.message);
      return;
    }

    this.form.patchValue({ latitud: result.latitud, longitud: result.longitud });
    this.mapsLinkMessage.set('Coordenadas obtenidas. Revise los valores antes de guardar.');
  }

  private resolverEnlaceCortoGoogleMaps(): void {
    this.mapsLink.disable({ emitEvent: false });
    this.mapsLinkResolving.set(true);
    this.mapsLinkMessage.set('Resolviendo el enlace compartido de Google Maps…');
    this.asistenciaService.resolverEnlaceGoogleMaps(this.mapsLink.value).pipe(
      finalize(() => {
        this.mapsLink.enable({ emitEvent: false });
        this.mapsLinkResolving.set(false);
      }), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ({ latitud, longitud }) => {
        if (!this.coordenadasValidas(latitud, longitud)) {
          this.mapsLinkMessage.set('Google Maps devolvió coordenadas inválidas. Revise el enlace e intente nuevamente.');
          return;
        }
        this.form.patchValue({ latitud, longitud });
        this.mapsLinkMessage.set('Coordenadas obtenidas. Revise los valores antes de guardar.');
      },
      error: (err) => this.mapsLinkMessage.set(apiErrorMessage(
        err, 'No se pudieron obtener coordenadas de este enlace. Revise el enlace e intente nuevamente.'
      ))
    });
  }

  private esEnlaceCortoGoogleMaps(value: string): boolean {
    try {
      const url = new URL(value.trim());
      if (url.protocol !== 'https:' || url.username || url.password || url.port) return false;
      const host = url.hostname.toLowerCase();
      return (host === 'maps.app.goo.gl' && url.pathname.length > 1)
        || (host === 'goo.gl' && url.pathname.startsWith('/maps/'));
    } catch {
      return false;
    }
  }

  private coordenadasValidas(latitud: number, longitud: number): boolean {
    return Number.isFinite(latitud) && Number.isFinite(longitud)
      && latitud >= -90 && latitud <= 90 && longitud >= -180 && longitud <= 180;
  }

  private extraerCoordenadas(value: string):
    | { ok: true; latitud: number; longitud: number }
    | { ok: false; message: string } {
    let url: URL;
    try {
      url = new URL(value.trim());
    } catch {
      return { ok: false, message: 'Ingrese un enlace válido de Google Maps.' };
    }

    const host = url.hostname.toLowerCase();
    if (host === 'maps.app.goo.gl' || host === 'goo.gl') {
      return { ok: false, message: 'Los enlaces cortos requieren resolver una redirección y no se procesan automáticamente.' };
    }
    if (url.protocol !== 'https:' || url.username || url.password || url.port ||
        !(host === 'google.com' || host.endsWith('.google.com')) ||
        !(url.pathname === '/maps' || url.pathname.startsWith('/maps/'))) {
      return { ok: false, message: 'Use una URL HTTPS de Google Maps que contenga coordenadas explícitas.' };
    }

    const pairs: Array<[number, number]> = [];
    const coordinatePattern = '([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))';
    const explicitPair = new RegExp(`^${coordinatePattern},\\s*${coordinatePattern}$`);
    const parameters = [...url.searchParams.entries()].filter(([key]) => key === 'q' || key === 'query');
    for (const [, value] of parameters) {
      const match = value.trim().match(explicitPair);
      if (match) pairs.push([Number(match[1]), Number(match[2])]);
    }
    const searchPath = url.pathname.match(/\/maps\/search\/([+-]?(?:\d+(?:\.\d*)?|\.\d+)),\+([+-]?(?:\d+(?:\.\d*)?|\.\d+))(?:\/|$)/);
    if (searchPath) pairs.push([Number(searchPath[1]), Number(searchPath[2])]);
    for (const match of url.pathname.matchAll(/!3d([+-]?(?:\d+(?:\.\d*)?|\.\d+))!4d([+-]?(?:\d+(?:\.\d*)?|\.\d+))/g)) {
      pairs.push([Number(match[1]), Number(match[2])]);
    }

    const distinctPairs = [...new Map(pairs.map(([latitud, longitud]) =>
      [`${latitud},${longitud}`, [latitud, longitud] as [number, number]])).values()];
    if (distinctPairs.length > 1) {
      return { ok: false, message: 'El enlace contiene coordenadas distintas y es ambiguo.' };
    }
    const pair = distinctPairs[0];
    if (!pair) {
      return { ok: false, message: 'No se encontraron coordenadas explícitas. Los enlaces que necesitan redirección no se resuelven.' };
    }
    const [latitud, longitud] = pair;
    if (!Number.isFinite(latitud) || !Number.isFinite(longitud) ||
        latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
      return { ok: false, message: 'Las coordenadas del enlace están fuera de rango.' };
    }
    return { ok: true, latitud, longitud };
  }

  editar(sesion: SesionEvento): void {
    this.limpiarEnlaceMaps();
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
    this.limpiarEnlaceMaps();
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

  private limpiarEnlaceMaps(): void {
    this.mapsLink.enable({ emitEvent: false });
    this.mapsLink.reset('');
    this.mapsLinkMessage.set('');
  }
}
