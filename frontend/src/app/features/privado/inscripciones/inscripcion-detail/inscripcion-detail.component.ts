import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { finalize } from 'rxjs';

import { InscripcionService } from '../../../../core/services/inscripcion.service';
import { DetalleInscripcion } from '../../../../core/models/inscripcion.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';

@Component({
  selector: 'app-inscripcion-detail',
  templateUrl: './inscripcion-detail.component.html',
  standalone: false
})
export class InscripcionDetailComponent implements OnInit {
  inscripcion?: DetalleInscripcion;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private inscripcionService: InscripcionService,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarDetalle(id);
    }
  }

  cargarDetalle(id: string): void {
    this.loading = true;
    this.inscripcionService.obtenerPorId(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (data) => this.inscripcion = data,
        error: () => {
          this.toast.error('Error al cargar el detalle de la inscripcion');
          this.router.navigate(['/privado/inscripciones']);
        }
      });
  }

  cancelar(): void {
    if (!this.inscripcion) return;

    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Cancelar inscripcion',
        message: `Se cancelara tu inscripcion al evento "${this.inscripcion.eventoTitulo}".`,
        confirmText: 'Cancelar inscripcion',
        tone: 'danger'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed || !this.inscripcion) return;

      this.inscripcionService.cancelar(this.inscripcion.id).subscribe({
        next: () => {
          this.toast.success('Inscripcion cancelada correctamente');
          this.cargarDetalle(this.inscripcion!.id);
        },
        error: (err) => this.toast.error(err.error?.mensaje || 'Error al cancelar')
      });
    });
  }
}
