import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';

import { EventoService } from '../../../../core/services/evento.service';
import { Evento } from '../../../../core/models/evento.model';
import { InscripcionService } from '../../../../core/services/inscripcion.service';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';

@Component({
  selector: 'app-evento-detail',
  templateUrl: './evento-detail.component.html',
  standalone: false
})
export class EventoDetailComponent implements OnInit {
  evento?: Evento;

  constructor(
    private eventoService: EventoService,
    private inscripcionService: InscripcionService,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarEvento(id);
    }
  }

  cargarEvento(id: string): void {
    this.eventoService.obtenerPorId(id).subscribe({
      next: (data) => this.evento = data,
      error: () => this.toast.error('Error al cargar detalle')
    });
  }

  inscribirse(): void {
    if (!this.evento) return;
    this.inscripcionService.inscribir({ eventoId: this.evento.id }).subscribe({
      next: () => {
        this.toast.success('Inscripcion exitosa');
        this.router.navigate(['/privado/inscripciones']);
      },
      error: (err) => this.toast.error(err.error?.mensaje || 'Error al inscribirse')
    });
  }

  publicar(): void {
    if (!this.evento) return;
    this.eventoService.publicar(this.evento.id).subscribe({
      next: () => {
        this.toast.success('Evento publicado con exito');
        this.cargarEvento(this.evento!.id);
      },
      error: (err) => this.toast.error(err.error?.mensaje || 'Error al publicar')
    });
  }

  cancelar(): void {
    if (!this.evento) return;

    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Cancelar evento',
        message: 'Esta accion no se puede deshacer y detendra la operacion del evento.',
        confirmText: 'Cancelar evento',
        tone: 'danger'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed || !this.evento) return;

      this.eventoService.cancelar(this.evento.id).subscribe({
        next: () => {
          this.toast.warning('Evento cancelado');
          this.cargarEvento(this.evento!.id);
        },
        error: (err) => this.toast.error(err.error?.mensaje || 'Error al cancelar')
      });
    });
  }

  finalizar(): void {
    if (!this.evento) return;
    this.eventoService.finalizar(this.evento.id).subscribe({
      next: () => {
        this.toast.success('Evento finalizado correctamente');
        this.cargarEvento(this.evento!.id);
      },
      error: (err) => this.toast.error(err.error?.mensaje || 'Error al finalizar')
    });
  }
}
