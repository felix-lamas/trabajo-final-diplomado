import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { FacultadService } from '../../../../core/services/facultad.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-facultad-form',
  templateUrl: './facultad-form.component.html',
  standalone: false
})
export class FacultadFormComponent implements OnInit {
  facultadForm: FormGroup;
  esEdicion = false;
  id: string | null = null;

  constructor(
    private fb: FormBuilder,
    private facultadService: FacultadService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    this.facultadForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: [''],
      estado: ['ACTIVO']
    });
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id');
    if (this.id) {
      this.esEdicion = true;
      this.cargarFacultad(this.id);
    }
  }

  cargarFacultad(id: string): void {
    this.facultadService.obtenerPorId(id).subscribe({
      next: (facultad) => {
        this.facultadForm.patchValue({
          nombre: facultad.nombre,
          descripcion: facultad.descripcion,
          estado: facultad.estado
        });
      },
      error: () => this.snackBar.open('Error al cargar facultad', 'Cerrar', { duration: 3000 })
    });
  }

  guardar(): void {
    if (this.facultadForm.invalid) return;

    const request = this.facultadForm.value;

    if (this.esEdicion && this.id) {
      this.facultadService.actualizar(this.id, request).subscribe({
        next: () => {
          this.snackBar.open('Facultad actualizada correctamente', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/admin/facultades']);
        },
        error: () => this.snackBar.open('Error al actualizar facultad', 'Cerrar', { duration: 3000 })
      });
    } else {
      this.facultadService.crear(request).subscribe({
        next: () => {
          this.snackBar.open('Facultad creada correctamente', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/admin/facultades']);
        },
        error: () => this.snackBar.open('Error al crear facultad', 'Cerrar', { duration: 3000 })
      });
    }
  }
}
