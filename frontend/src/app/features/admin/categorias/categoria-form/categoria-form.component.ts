import { Component, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';

import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';

function nombreCategoriaValido(control: AbstractControl): ValidationErrors | null {
  const longitud = typeof control.value === 'string' ? control.value.trim().length : 0;
  if (longitud < 3) return { minlength: true };
  if (longitud > 100) return { maxlength: true };
  return null;
}

@Component({
  selector: 'app-categoria-form',
  templateUrl: './categoria-form.component.html',
  standalone: false
})
export class CategoriaFormComponent implements OnInit {
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly categoriaForm: FormGroup;
  esEdicion = false;
  id: string | null = null;

  constructor(
    private fb: FormBuilder,
    private categoriaService: CategoriaEventoService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    this.categoriaForm = this.fb.group({
      nombre: ['', [Validators.required, nombreCategoriaValido]],
      descripcion: [''],
      estado: ['ACTIVO', Validators.required]
    });
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id');
    if (this.id) {
      this.esEdicion = true;
      this.cargarCategoria(this.id);
    }
  }

  cargarCategoria(id: string): void {
    this.cargando.set(true);
    this.error.set(null);
    this.categoriaService.obtenerPorId(id)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (categoria) => this.categoriaForm.patchValue(categoria),
        error: (error) => this.error.set(apiErrorMessage(error, 'No fue posible cargar la categoría.'))
      });
  }

  guardar(): void {
    if (this.categoriaForm.invalid || this.guardando() || this.cargando()) {
      this.categoriaForm.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set(null);
    const raw = this.categoriaForm.getRawValue();
    const request = { ...raw, nombre: raw.nombre.trim(), descripcion: raw.descripcion?.trim() || undefined };
    const operacion = this.esEdicion && this.id
      ? this.categoriaService.actualizar(this.id, request)
      : this.categoriaService.crear({ nombre: request.nombre, descripcion: request.descripcion });

    operacion.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: () => {
        this.snackBar.open(
          this.esEdicion ? 'Categoría actualizada correctamente' : 'Categoría creada correctamente',
          'Cerrar',
          { duration: 3000 }
        );
        this.router.navigate(['/admin/categorias']);
      },
      error: (error) => {
        const mensaje = apiErrorMessage(error,
          this.esEdicion ? 'No fue posible actualizar la categoría.' : 'No fue posible crear la categoría.');
        this.error.set(mensaje);
        this.snackBar.open(mensaje, 'Cerrar', { duration: 4500 });
      }
    });
  }
}
