import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';

import { EventoService } from '../../../../core/services/evento.service';
import { Evento } from '../../../../core/models/evento.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';
import { AuthService } from '../../../../core/services/auth.service';
import { apiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-evento-detail',
  templateUrl: './evento-detail.component.html',
  standalone: false
})
export class EventoDetailComponent implements OnInit {
  evento?: Evento;

  constructor(
    private eventoService: EventoService,
    private authService: AuthService,
    private route: ActivatedRoute,
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
      error: (err) => this.toast.error(apiErrorMessage(err, 'Error al cargar detalle'))
    });
  }

  publicar(): void {
    if (!this.evento) return;
    this.eventoService.publicar(this.evento.id).subscribe({
      next: () => {
        this.toast.success('Evento publicado con exito');
        this.cargarEvento(this.evento!.id);
      },
      error: (err) => this.toast.error(apiErrorMessage(err, 'Error al publicar'))
    });
  }

  enviarARevision(): void {
    if (!this.evento) return;
    this.eventoService.enviarARevision(this.evento.id).subscribe({
      next: () => {
        this.toast.success('Evento enviado a revision');
        this.cargarEvento(this.evento!.id);
      },
      error: (err) => this.toast.error(apiErrorMessage(err, 'Error al enviar a revision'))
    });
  }

  get esAdministrador(): boolean {
    return this.authService.hasAnyRole(['ADMINISTRADOR']);
  }

  get esOrganizador(): boolean {
    return this.authService.hasAnyRole(['ORGANIZADOR']);
  }

  get rutaGestion(): string {
    return this.esOrganizador ? '/organizador/eventos' : '/admin/eventos';
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

      this.eventoService.cancelar(this.evento.id, 'Cancelado desde el panel web').subscribe({
        next: () => {
          this.toast.warning('Evento cancelado');
          this.cargarEvento(this.evento!.id);
        },
        error: (err) => this.toast.error(apiErrorMessage(err, 'Error al cancelar'))
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
      error: (err) => this.toast.error(apiErrorMessage(err, 'Error al finalizar'))
    });
  }
}
