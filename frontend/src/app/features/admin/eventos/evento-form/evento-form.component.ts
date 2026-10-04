import { Component, DestroyRef, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize, map, of, switchMap } from 'rxjs';

import { CategoriaEvento } from '../../../../core/models/categoria-evento.model';
import { CrearEventoRequest, Modalidad, PublicoObjetivo, TipoCertificadoEvento, TipoInscripcion } from '../../../../core/models/evento.model';
import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { EventoService } from '../../../../core/services/evento.service';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({ selector: 'app-evento-form', templateUrl: './evento-form.component.html', styleUrl: './evento-form.component.css', standalone: false })
export class EventoFormComponent implements OnInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  readonly categorias = signal<CategoriaEvento[]>([]);
  readonly cargandoCategorias = signal(true);
  readonly cargandoEvento = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly modalidad = signal(Modalidad.PRESENCIAL);
  readonly tipoInscripcion = signal(TipoInscripcion.GRATUITO);
  readonly requiereInscripcion = signal(true);
  readonly cupoLimitado = signal(true);
  readonly emiteCertificado = signal(false);
  readonly tipoCertificado = signal<TipoCertificadoEvento | null>(null);
  readonly esPresencial = computed(() => this.modalidad() === Modalidad.PRESENCIAL);
  readonly esPagado = computed(() => this.tipoInscripcion() === TipoInscripcion.PAGO);
  readonly mostrarCupo = computed(() => this.requiereInscripcion() && this.cupoLimitado());
  readonly mostrarHoras = computed(() => this.emiteCertificado() && this.tipoCertificado() === TipoCertificadoEvento.CURRICULAR);
  readonly qrPreviewUrl = signal<string | null>(null);
  readonly qrFileName = signal<string | null>(null);
  readonly qrError = signal<string | null>(null);
  readonly qrSaved = signal(false);
  readonly qrRemoveRequested = signal(false);

  readonly eventoForm: FormGroup;
  esEdicion = false;
  id: string | null = null;
  private qrFile: File | null = null;
  private qrObjectUrl: string | null = null;
  private qrSavedUrl: string | null = null;
  private static readonly QR_MAX_BYTES = 5 * 1024 * 1024;
  private static readonly QR_MIMES = new Set(['image/png', 'image/jpeg']);

  constructor(
    private readonly fb: FormBuilder,
    private readonly eventoService: EventoService,
    private readonly categoriaService: CategoriaEventoService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly snackBar: MatSnackBar
  ) {
    this.eventoForm = this.fb.group({
      titulo: ['', [Validators.required, Validators.maxLength(200)]], descripcion: ['', Validators.required],
      objetivos: ['', Validators.required], categoriaId: ['', Validators.required],
      modalidad: [Modalidad.PRESENCIAL, Validators.required], tipoInscripcion: [TipoInscripcion.GRATUITO, Validators.required],
      costo: [0], fechaInicio: ['', Validators.required], fechaFin: ['', Validators.required],
      horaInicio: ['', Validators.required], horaFin: ['', Validators.required], ubicacion: [''], direccion: [''],
      latitud: [null], longitud: [null], radioMetros: [null], enlaceVirtual: [''],
      requiereInscripcion: [true, Validators.required], cupoLimitado: [true, Validators.required], cupoMaximo: [100],
      emiteCertificado: [false, Validators.required], tipoCertificado: [null], horasAcademicas: [null],
      publicoObjetivo: [PublicoObjetivo.AMBOS, Validators.required],
      telefonoContacto: ['', Validators.pattern(/^[0-9+() -]{7,20}$/)], emailContacto: ['', Validators.email],
      whatsappContacto: ['', Validators.pattern(/^[0-9+() -]{7,20}$/)],
      imagenPortada: ['', Validators.pattern(/^https?:\/\/.+/i)],
      instruccionesPago: ['']
    }, { validators: this.rangoTemporalValido });
    this.conectarEstadoCondicional();
  }

  ngOnInit(): void {
    this.actualizarValidadores();
    this.cargarCategorias();
    this.id = this.route.snapshot.paramMap.get('id');
    if (this.id) { this.esEdicion = true; this.cargarEvento(this.id); }
  }

  cargarCategorias(): void {
    this.cargandoCategorias.set(true);
    this.categoriaService.listarActivas().pipe(
      finalize(() => this.cargandoCategorias.set(false)), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (data) => this.categorias.set(data),
      error: (err) => this.error.set(apiErrorMessage(err, 'Error al cargar categorias activas'))
    });
  }

  cargarEvento(id: string): void {
    this.cargandoEvento.set(true);
    this.eventoService.obtenerPorId(id).pipe(
      finalize(() => this.cargandoEvento.set(false)), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (evento) => {
        this.eventoForm.patchValue(evento); this.sincronizarSignals(); this.actualizarValidadores();
        this.qrSavedUrl = evento.qrPagoUrl ?? null;
        this.qrSaved.set(Boolean(this.qrSavedUrl));
        if (this.qrSavedUrl) this.cargarPreviewQr(id, this.qrSavedUrl);
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'Error al cargar evento'))
    });
  }

  guardar(): void {
    this.eventoForm.markAllAsTouched();
    if (this.eventoForm.invalid || this.guardando()) return;
    this.guardando.set(true);
    this.error.set(null);
    const raw = this.eventoForm.getRawValue();
    const presencial = raw.modalidad === Modalidad.PRESENCIAL;
    const pagado = raw.tipoInscripcion === TipoInscripcion.PAGO;
    const certificado = Boolean(raw.emiteCertificado);
    const curricular = certificado && raw.tipoCertificado === TipoCertificadoEvento.CURRICULAR;
    const limitado = Boolean(raw.requiereInscripcion && raw.cupoLimitado);
    const request: CrearEventoRequest = {
      ...raw, costo: pagado ? Number(raw.costo) : 0,
      ubicacion: presencial ? raw.ubicacion : undefined, direccion: presencial ? raw.direccion : undefined,
      latitud: presencial ? Number(raw.latitud) : null, longitud: presencial ? Number(raw.longitud) : null,
      radioMetros: presencial ? Number(raw.radioMetros) : null, enlaceVirtual: presencial ? undefined : raw.enlaceVirtual,
      cupoLimitado: limitado, cupoMaximo: limitado ? Number(raw.cupoMaximo) : null,
      tipoCertificado: certificado ? raw.tipoCertificado : null, horasAcademicas: curricular ? Number(raw.horasAcademicas) : null,
      instruccionesPago: pagado ? raw.instruccionesPago : undefined
    };
    const operacion$ = this.esEdicion && this.id ? this.eventoService.actualizar(this.id, request) : this.eventoService.crear(request);
    operacion$.pipe(
      switchMap((evento) => {
        this.id = evento.id;
        this.esEdicion = true;
        if (pagado && this.qrFile) return this.eventoService.subirQrPago(evento.id, this.qrFile).pipe(map(() => {
          this.qrSaved.set(true); this.qrRemoveRequested.set(false); return evento;
        }));
        if (pagado && this.qrRemoveRequested()) return this.eventoService.eliminarQrPago(evento.id).pipe(map(() => {
          this.qrSaved.set(false); this.qrSavedUrl = null; return evento;
        }));
        return of(evento);
      }),
      finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: () => {
        this.qrFile = null; this.qrFileName.set(null); this.qrRemoveRequested.set(false);
        this.snackBar.open(this.esEdicion ? 'Evento actualizado' : 'Evento creado correctamente', 'Cerrar', { duration: 3000 });
        this.router.navigate([this.rutaGestion]);
      },
      error: (err) => this.error.set(apiErrorMessage(err, this.esEdicion ? 'Error al actualizar' : 'Error al crear'))
    });
  }

  onQrSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.qrError.set(null);
    if (!file) return;
    const extension = file.name.split('.').pop()?.toLowerCase();
    if (!['png', 'jpg', 'jpeg'].includes(extension ?? '') || (file.type && !EventoFormComponent.QR_MIMES.has(file.type))) {
      this.qrError.set('Selecciona una imagen PNG, JPG o JPEG válida.'); input.value = ''; return;
    }
    if (file.size <= 0 || file.size > EventoFormComponent.QR_MAX_BYTES) {
      this.qrError.set('La imagen debe pesar más de 0 y como máximo 5 MB.'); input.value = ''; return;
    }
    this.qrFile = file; this.qrFileName.set(file.name); this.qrRemoveRequested.set(false);
    this.reemplazarPreview(URL.createObjectURL(file));
  }

  quitarQr(): void {
    this.qrError.set(null);
    if (this.qrFile) {
      this.qrFile = null; this.qrFileName.set(null);
      if (this.qrSaved() && this.qrSavedUrl && this.id) this.cargarPreviewQr(this.id, this.qrSavedUrl);
      else this.reemplazarPreview(null);
      return;
    }
    this.qrFileName.set(null); this.qrRemoveRequested.set(this.qrSaved()); this.qrSaved.set(false);
    this.reemplazarPreview(null);
  }

  ngOnDestroy(): void { this.reemplazarPreview(null); }

  private cargarPreviewQr(eventoId: string, qrUrl: string): void {
    if (/^https?:\/\//i.test(qrUrl)) { this.reemplazarPreview(qrUrl); return; }
    this.eventoService.descargarQrPago(eventoId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (blob) => this.reemplazarPreview(URL.createObjectURL(blob)),
      error: () => this.qrError.set('No fue posible cargar la imagen QR guardada.')
    });
  }

  private reemplazarPreview(url: string | null): void {
    if (this.qrObjectUrl) URL.revokeObjectURL(this.qrObjectUrl);
    this.qrObjectUrl = url?.startsWith('blob:') ? url : null;
    this.qrPreviewUrl.set(url);
  }

  get rutaGestion(): string { return this.router.url.startsWith('/organizador') ? '/organizador/eventos' : '/admin/eventos'; }

  private conectarEstadoCondicional(): void {
    const observar = (control: string, actualizar: (valor: any) => void) => this.eventoForm.get(control)?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe((valor) => { actualizar(valor); this.actualizarValidadores(); });
    observar('modalidad', (v) => this.modalidad.set(v));
    observar('tipoInscripcion', (v) => this.tipoInscripcion.set(v));
    observar('requiereInscripcion', (v) => this.requiereInscripcion.set(Boolean(v)));
    observar('cupoLimitado', (v) => this.cupoLimitado.set(Boolean(v)));
    observar('emiteCertificado', (v) => this.emiteCertificado.set(Boolean(v)));
    observar('tipoCertificado', (v) => this.tipoCertificado.set(v));
  }

  private sincronizarSignals(): void {
    this.modalidad.set(this.eventoForm.get('modalidad')?.value); this.tipoInscripcion.set(this.eventoForm.get('tipoInscripcion')?.value);
    this.requiereInscripcion.set(Boolean(this.eventoForm.get('requiereInscripcion')?.value));
    this.cupoLimitado.set(Boolean(this.eventoForm.get('cupoLimitado')?.value));
    this.emiteCertificado.set(Boolean(this.eventoForm.get('emiteCertificado')?.value));
    this.tipoCertificado.set(this.eventoForm.get('tipoCertificado')?.value);
  }

  private actualizarValidadores(): void {
    this.definir('ubicacion', this.esPresencial(), [Validators.required, Validators.maxLength(255)]);
    this.definir('direccion', this.esPresencial(), [Validators.required, Validators.maxLength(500)]);
    this.definir('latitud', this.esPresencial(), [Validators.required, Validators.min(-90), Validators.max(90)]);
    this.definir('longitud', this.esPresencial(), [Validators.required, Validators.min(-180), Validators.max(180)]);
    this.definir('radioMetros', this.esPresencial(), [Validators.required, Validators.min(1), Validators.max(500)]);
    this.definir('enlaceVirtual', !this.esPresencial(), [Validators.required, Validators.pattern(/^https?:\/\/.+/i)]);
    this.definir('costo', this.esPagado(), [Validators.required, Validators.min(0.01)]);
    this.definir('instruccionesPago', this.esPagado(), [Validators.maxLength(2000)]);
    this.definir('cupoMaximo', this.mostrarCupo(), [Validators.required, Validators.min(1)]);
    this.definir('tipoCertificado', this.emiteCertificado(), [Validators.required]);
    this.definir('horasAcademicas', this.mostrarHoras(), [Validators.required, Validators.min(1)]);
  }

  private definir(nombre: string, requerido: boolean, validadores: any[]): void {
    const control = this.eventoForm.get(nombre); control?.setValidators(requerido ? validadores : []);
    control?.updateValueAndValidity({ emitEvent: false });
  }

  private rangoTemporalValido(group: AbstractControl): ValidationErrors | null {
    const inicio = `${group.get('fechaInicio')?.value || ''}T${group.get('horaInicio')?.value || ''}`;
    const fin = `${group.get('fechaFin')?.value || ''}T${group.get('horaFin')?.value || ''}`;
    return inicio.length > 2 && fin.length > 2 && inicio >= fin ? { rangoTemporal: true } : null;
  }
}
