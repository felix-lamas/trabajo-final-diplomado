import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-categoria-form',
  templateUrl: './categoria-form.component.html',
  standalone: false
})
export class CategoriaFormComponent implements OnInit {
  categoriaForm: FormGroup;
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
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: [''],
      estado: ['ACTIVO']
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
    this.categoriaService.obtenerPorId(id).subscribe({
      next: (cat) => {
        this.categoriaForm.patchValue({
          nombre: cat.nombre,
          descripcion: cat.descripcion,
          estado: cat.estado
        });
      },
      error: () => this.snackBar.open('Error al cargar categoría', 'Cerrar', { duration: 3000 })
    });
  }

  guardar(): void {
    if (this.categoriaForm.invalid) return;

    const request = this.categoriaForm.value;

    if (this.esEdicion && this.id) {
      this.categoriaService.actualizar(this.id, request).subscribe({
        next: () => {
          this.snackBar.open('Categoría actualizada correctamente', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/admin/categorias']);
        },
        error: () => this.snackBar.open('Error al actualizar categoría', 'Cerrar', { duration: 3000 })
      });
    } else {
      this.categoriaService.crear(request).subscribe({
        next: () => {
          this.snackBar.open('Categoría creada correctamente', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/admin/categorias']);
        },
        error: () => this.snackBar.open('Error al crear categoría', 'Cerrar', { duration: 3000 })
      });
    }
  }
}
