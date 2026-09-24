import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EventoService } from '../../../../core/services/evento.service';
import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { CategoriaEvento } from '../../../../core/models/categoria-evento.model';
import { CrearEventoRequest, Modalidad, PublicoObjetivo, TipoInscripcion } from '../../../../core/models/evento.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-evento-form',
  templateUrl: './evento-form.component.html',
  standalone: false
})
export class EventoFormComponent implements OnInit {
  eventoForm: FormGroup;
  esEdicion = false;
  id: string | null = null;
  categorias: CategoriaEvento[] = [];

  constructor(
    private fb: FormBuilder,
    private eventoService: EventoService,
    private categoriaService: CategoriaEventoService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    this.eventoForm = this.fb.group({
      titulo: ['', [Validators.required, Validators.maxLength(200)]],
      descripcion: ['', Validators.required],
      objetivos: ['', Validators.required],
      categoriaId: ['', Validators.required],
      modalidad: [Modalidad.PRESENCIAL, Validators.required],
      tipoInscripcion: [TipoInscripcion.GRATUITO, Validators.required],
      costo: [0, [Validators.required, Validators.min(0)]],
      fechaInicio: ['', Validators.required],
      fechaFin: ['', Validators.required],
      horaInicio: ['', Validators.required],
      horaFin: ['', Validators.required],
      ubicacion: ['', Validators.required],
      direccion: [''],
      enlaceVirtual: [''],
      requiereInscripcion: [true, Validators.required],
      cupoLimitado: [true, Validators.required],
      cupoMaximo: [100, [Validators.required, Validators.min(1)]],
      emiteCertificado: [false, Validators.required],
      publicoObjetivo: [PublicoObjetivo.AMBOS, Validators.required],
      imagenPortada: ['']
    });

    // Validar costo según tipo de inscripción
    this.eventoForm.get('tipoInscripcion')?.valueChanges.subscribe(tipo => {
      const costoControl = this.eventoForm.get('costo');
      if (tipo === TipoInscripcion.GRATUITO) {
        costoControl?.setValue(0);
        costoControl?.disable();
      } else {
        costoControl?.enable();
      }
    });

    this.eventoForm.get('modalidad')?.valueChanges.subscribe(() => this.actualizarValidadoresModalidad());
    this.eventoForm.get('cupoLimitado')?.valueChanges.subscribe(() => this.actualizarValidadorCupo());
  }

  ngOnInit(): void {
    this.cargarCategorias();
    this.id = this.route.snapshot.paramMap.get('id');
    if (this.id) {
      this.esEdicion = true;
      this.cargarEvento(this.id);
    }
  }

  cargarCategorias(): void {
    this.categoriaService.listarActivas().subscribe({
      next: (data) => this.categorias = data,
      error: (err) => this.snackBar.open(apiErrorMessage(err, 'Error al cargar categorias activas'), 'Cerrar', { duration: 4500 })
    });
  }

  cargarEvento(id: string): void {
    this.eventoService.obtenerPorId(id).subscribe({
      next: (ev) => this.eventoForm.patchValue(ev),
      error: (err) => this.snackBar.open(apiErrorMessage(err, 'Error al cargar evento'), 'Cerrar', { duration: 4500 })
    });
  }

  guardar(): void {
    if (this.eventoForm.invalid) return;

    const raw = this.eventoForm.getRawValue();
    const request: CrearEventoRequest = {
      ...raw,
      costo: 0,
      cupoMaximo: raw.cupoLimitado ? raw.cupoMaximo : null,
      emiteCertificado: false
    };

    if (this.esEdicion && this.id) {
      this.eventoService.actualizar(this.id, request).subscribe({
        next: () => {
          this.snackBar.open('Evento actualizado', 'Cerrar', { duration: 3000 });
          this.router.navigate([this.rutaGestion]);
        },
        error: (err) => this.snackBar.open(apiErrorMessage(err, 'Error al actualizar'), 'Cerrar', { duration: 4500 })
      });
    } else {
      this.eventoService.crear(request).subscribe({
        next: () => {
          this.snackBar.open('Evento creado correctamente', 'Cerrar', { duration: 3000 });
          this.router.navigate([this.rutaGestion]);
        },
        error: (err) => this.snackBar.open(apiErrorMessage(err, 'Error al crear'), 'Cerrar', { duration: 4500 })
      });
    }
  }

  get rutaGestion(): string {
    return this.router.url.startsWith('/organizador') ? '/organizador/eventos' : '/admin/eventos';
  }

  private actualizarValidadoresModalidad(): void {
    const presencial = this.eventoForm.get('modalidad')?.value === Modalidad.PRESENCIAL;
    const ubicacion = this.eventoForm.get('ubicacion');
    const enlaceVirtual = this.eventoForm.get('enlaceVirtual');

    presencial ? ubicacion?.setValidators([Validators.required]) : ubicacion?.clearValidators();
    presencial ? enlaceVirtual?.clearValidators() : enlaceVirtual?.setValidators([Validators.required]);
    ubicacion?.updateValueAndValidity({ emitEvent: false });
    enlaceVirtual?.updateValueAndValidity({ emitEvent: false });
  }

  private actualizarValidadorCupo(): void {
    const cupo = this.eventoForm.get('cupoMaximo');
    if (this.eventoForm.get('cupoLimitado')?.value) {
      cupo?.setValidators([Validators.required, Validators.min(1)]);
    } else {
      cupo?.clearValidators();
      cupo?.setValue(null, { emitEvent: false });
    }
    cupo?.updateValueAndValidity({ emitEvent: false });
  }
}
