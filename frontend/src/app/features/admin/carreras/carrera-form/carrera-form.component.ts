import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CarreraService } from '../../../../core/services/carrera.service';
import { FacultadService } from '../../../../core/services/facultad.service';
import { Facultad } from '../../../../core/models/facultad.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-carrera-form',
  templateUrl: './carrera-form.component.html',
  standalone: false
})
export class CarreraFormComponent implements OnInit {
  carreraForm: FormGroup;
  esEdicion = false;
  id: string | null = null;
  facultades: Facultad[] = [];

  constructor(
    private fb: FormBuilder,
    private carreraService: CarreraService,
    private facultadService: FacultadService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    this.carreraForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: [''],
      facultadId: ['', Validators.required],
      estado: ['ACTIVO']
    });
  }

  ngOnInit(): void {
    this.cargarFacultades();
    this.id = this.route.snapshot.paramMap.get('id');
    if (this.id) {
      this.esEdicion = true;
      this.cargarCarrera(this.id);
    }
  }

  cargarFacultades(): void {
    this.facultadService.listar().subscribe({
      next: (data) => this.facultades = data.filter(f => f.estado === 'ACTIVO'),
      error: () => this.snackBar.open('Error al cargar facultades', 'Cerrar', { duration: 3000 })
    });
  }

  cargarCarrera(id: string): void {
    this.carreraService.obtenerPorId(id).subscribe({
      next: (carrera) => {
        this.carreraForm.patchValue({
          nombre: carrera.nombre,
          descripcion: carrera.descripcion,
          facultadId: carrera.facultadId,
          estado: carrera.estado
        });
      },
      error: () => this.snackBar.open('Error al cargar carrera', 'Cerrar', { duration: 3000 })
    });
  }

  guardar(): void {
    if (this.carreraForm.invalid) return;

    const request = this.carreraForm.value;

    if (this.esEdicion && this.id) {
      this.carreraService.actualizar(this.id, request).subscribe({
        next: () => {
          this.snackBar.open('Carrera actualizada correctamente', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/admin/carreras']);
        },
        error: () => this.snackBar.open('Error al actualizar carrera', 'Cerrar', { duration: 3000 })
      });
    } else {
      this.carreraService.crear(request).subscribe({
        next: () => {
          this.snackBar.open('Carrera creada correctamente', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/admin/carreras']);
        },
        error: () => this.snackBar.open('Error al crear carrera', 'Cerrar', { duration: 3000 })
      });
    }
  }
}
