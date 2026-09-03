import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EventoService } from '../../../../core/services/evento.service';
import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { CategoriaEvento } from '../../../../core/models/categoria-evento.model';
import { Modalidad, TipoInscripcion } from '../../../../core/models/evento.model';
import { MatSnackBar } from '@angular/material/snack-bar';

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
      descripcion: [''],
      objetivos: [''],
      categoriaId: ['', Validators.required],
      modalidad: [Modalidad.PRESENCIAL, Validators.required],
      tipoInscripcion: [TipoInscripcion.GRATUITO, Validators.required],
      costo: [0, [Validators.required, Validators.min(0)]],
      fechaInicio: ['', Validators.required],
      fechaFin: ['', Validators.required],
      horaInicio: [''],
      horaFin: [''],
      ubicacion: [''],
      enlaceVirtual: [''],
      cupoMaximo: [100, [Validators.required, Validators.min(1)]],
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
    this.categoriaService.listarActivas().subscribe(data => this.categorias = data);
  }

  cargarEvento(id: string): void {
    this.eventoService.obtenerPorId(id).subscribe({
      next: (ev) => this.eventoForm.patchValue(ev),
      error: () => this.snackBar.open('Error al cargar evento', 'Cerrar', { duration: 3000 })
    });
  }

  guardar(): void {
    if (this.eventoForm.invalid) return;

    const request = this.eventoForm.getRawValue();

    if (this.esEdicion && this.id) {
      this.eventoService.actualizar(this.id, request).subscribe({
        next: () => {
          this.snackBar.open('Evento actualizado', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/admin/eventos']);
        },
        error: (err) => this.snackBar.open(err.error?.mensaje || 'Error al actualizar', 'Cerrar')
      });
    } else {
      this.eventoService.crear(request).subscribe({
        next: () => {
          this.snackBar.open('Evento creado correctamente', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/admin/eventos']);
        },
        error: (err) => this.snackBar.open(err.error?.mensaje || 'Error al crear', 'Cerrar')
      });
    }
  }
}
