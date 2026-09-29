import { Component, OnInit, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { finalize } from 'rxjs';

import { EventoService } from '../../../../core/services/evento.service';
import { Evento } from '../../../../core/models/evento.model';
import { ConfirmDialogComponent } from '../../../../shared/ui/confirm-dialog/confirm-dialog.component';
import { ToastService } from '../../../../shared/ui/toast.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-evento-list',
  templateUrl: './evento-list.component.html',
  standalone: false
})
export class EventoListComponent implements OnInit {
  private readonly viewState = signal({ eventos: [] as Evento[], loading: true });
  get eventos(): Evento[] { return this.viewState().eventos; }
  private set eventos(value: Evento[]) { this.viewState.update((state) => ({ ...state, eventos: value })); }
  displayedColumns: string[] = ['titulo', 'categoria', 'fecha', 'cupo', 'estado', 'acciones'];
  busqueda = '';
  estadoFiltro = 'TODOS';
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }

  constructor(
    private eventoService: EventoService,
    private authService: AuthService,
    private dialog: MatDialog,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarEventos();
  }

  cargarEventos(): void {
    this.loading = true;
    const eventos$ = this.esAdministrador
      ? this.eventoService.listarEnRevision()
      : this.eventoService.listar();

    eventos$
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (data) => this.eventos = data,
        error: () => this.toast.error('Error al cargar eventos')
      });
  }

  get eventosFiltrados(): Evento[] {
    const busqueda = this.busqueda.trim().toLowerCase();
    return this.eventos.filter((evento) => {
      const coincideBusqueda = !busqueda
        || evento.titulo.toLowerCase().includes(busqueda)
        || evento.categoriaNombre.toLowerCase().includes(busqueda)
        || evento.descripcion.toLowerCase().includes(busqueda);
      const coincideEstado = this.estadoFiltro === 'TODOS' || evento.estado === this.estadoFiltro;
      return coincideBusqueda && coincideEstado;
    });
  }

  get totalEventos(): number {
    return this.eventos.length;
  }

  get totalPublicados(): number {
    return this.eventos.filter((evento) => evento.estado === 'PUBLICADO').length;
  }

  get totalBorradores(): number {
    return this.eventos.filter((evento) => evento.estado === 'BORRADOR').length;
  }

  get esOrganizador(): boolean {
    return this.authService.hasAnyRole(['ORGANIZADOR']);
  }

  get esAdministrador(): boolean {
    return this.authService.hasAnyRole(['ADMINISTRADOR']);
  }

  cambiarFiltro(estado: string): void {
    this.estadoFiltro = estado;
  }

  getEstadoClass(estado: string): string {
    switch (estado) {
      case 'BORRADOR': return 'bg-gray-100 text-gray-800';
      case 'EN_REVISION': return 'bg-yellow-100 text-yellow-800';
      case 'PUBLICADO': return 'bg-green-100 text-green-800';
      case 'FINALIZADO': return 'bg-purple-100 text-purple-800';
      case 'RECHAZADO': return 'bg-orange-100 text-orange-800';
      case 'CANCELADO': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getEstadoLabel(estado: string): string {
    switch (estado) {
      case 'BORRADOR': return 'Borrador';
      case 'EN_REVISION': return 'En revision';
      case 'PUBLICADO': return 'Publicado';
      case 'FINALIZADO': return 'Finalizado';
      case 'RECHAZADO': return 'Rechazado';
      case 'CANCELADO': return 'Cancelado';
      default: return estado;
    }
  }

  eliminar(evento: Evento): void {
    this.dialog.open(ConfirmDialogComponent, {
      width: 'min(440px, 92vw)',
      data: {
        title: 'Eliminar evento',
        message: `Esta accion quitara el borrador "${evento.titulo}" del catalogo.`,
        confirmText: 'Eliminar',
        tone: 'danger'
      }
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      this.eventoService.eliminar(evento.id).subscribe({
        next: () => {
          this.toast.success('Evento eliminado correctamente');
          this.cargarEventos();
        },
        error: () => this.toast.error('Error al eliminar evento')
      });
    });
  }
}
