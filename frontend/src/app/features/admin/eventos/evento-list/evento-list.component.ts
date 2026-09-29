import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
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
  private readonly destroyRef = inject(DestroyRef);
  readonly eventos = signal<Evento[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly busqueda = signal('');
  readonly estadoFiltro = signal('TODOS');
  readonly eventosFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    return this.eventos().filter((evento) => {
      const coincideBusqueda = !texto || evento.titulo.toLowerCase().includes(texto)
        || evento.categoriaNombre.toLowerCase().includes(texto) || (evento.descripcion || '').toLowerCase().includes(texto);
      return coincideBusqueda && (this.estadoFiltro() === 'TODOS' || evento.estado === this.estadoFiltro());
    });
  });
  readonly totalEventos = computed(() => this.eventos().length);
  readonly totalPublicados = computed(() => this.eventos().filter((e) => e.estado === 'PUBLICADO').length);
  readonly totalBorradores = computed(() => this.eventos().filter((e) => e.estado === 'BORRADOR').length);
  displayedColumns: string[] = ['titulo', 'categoria', 'fecha', 'cupo', 'estado', 'acciones'];

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
    this.loading.set(true);
    this.error.set(false);
    const eventos$ = this.eventoService.listar();

    eventos$
      .pipe(finalize(() => this.loading.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => this.eventos.set(data),
        error: () => { this.error.set(true); this.eventos.set([]); this.toast.error('Error al cargar eventos'); }
      });
  }

  get esOrganizador(): boolean {
    return this.authService.hasAnyRole(['ORGANIZADOR']);
  }

  get esAdministrador(): boolean {
    return this.authService.hasAnyRole(['ADMINISTRADOR']);
  }

  cambiarFiltro(estado: string): void {
    this.estadoFiltro.set(estado);
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
    }).afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe((confirmed) => {
      if (!confirmed) return;

      this.eventoService.eliminar(evento.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.toast.success('Evento eliminado correctamente');
          this.cargarEventos();
        },
        error: () => this.toast.error('Error al eliminar evento')
      });
    });
  }
}
