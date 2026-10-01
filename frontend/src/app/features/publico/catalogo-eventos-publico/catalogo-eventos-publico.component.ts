import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup } from '@angular/forms';
import { EventoService } from '../../../core/services/evento.service';
import { Evento, Modalidad } from '../../../core/models/evento.model';

@Component({
  selector: 'app-catalogo-eventos-publico',
  templateUrl: './catalogo-eventos-publico.component.html',
  styleUrl: './catalogo-eventos-publico.component.css',
  standalone: false
})
export class CatalogoEventosPublicoComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly viewState = signal({
    eventos: [] as Evento[],
    filtrados: [] as Evento[],
    loading: true,
    errorCarga: false
  });
  get eventos(): Evento[] { return this.viewState().eventos; }
  private set eventos(value: Evento[]) { this.viewState.update((state) => ({ ...state, eventos: value })); }
  get filtrados(): Evento[] { return this.viewState().filtrados; }
  private set filtrados(value: Evento[]) { this.viewState.update((state) => ({ ...state, filtrados: value })); }
  get loading(): boolean { return this.viewState().loading; }
  private set loading(value: boolean) { this.viewState.update((state) => ({ ...state, loading: value })); }
  get errorCarga(): boolean { return this.viewState().errorCarga; }
  private set errorCarga(value: boolean) { this.viewState.update((state) => ({ ...state, errorCarga: value })); }
  pageIndex = 0;
  pageSize = 6;
  filtros: FormGroup;

  constructor(
    private fb: FormBuilder,
    private eventoService: EventoService
  ) {
    this.filtros = this.fb.group({
      buscar: [''],
      categoria: ['TODAS'],
      tipoInscripcion: ['TODOS'],
      modalidad: ['TODAS']
    });
  }

  ngOnInit(): void {
    this.cargarEventos();

    this.filtros.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.aplicarFiltros());
  }

  cargarEventos(): void {
    this.loading = true;
    this.errorCarga = false;
    this.eventoService.listarPublicados().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => {
        this.eventos = data;
        this.loading = false;
        this.aplicarFiltros();
      },
      error: () => {
        this.loading = false;
        this.errorCarga = true;
        this.eventos = [];
        this.filtrados = [];
      }
    });
  }

  get categorias(): string[] {
    return Array.from(new Set(this.eventos.map((evento) => evento.categoriaNombre).filter(Boolean)));
  }

  get paginados(): Evento[] {
    const start = this.pageIndex * this.pageSize;
    return this.filtrados.slice(start, start + this.pageSize);
  }

  get totalResultados(): number {
    return this.filtrados.length;
  }

  get busqueda(): string {
    return String(this.filtros.get('buscar')?.value || '');
  }

  get modalidadOpciones(): Array<{ value: string; label: string }> {
    return [
      { value: Modalidad.PRESENCIAL, label: 'Presencial' },
      { value: Modalidad.VIRTUAL, label: 'Virtual' }
    ];
  }

  cambiarPagina(event: { pageIndex: number; pageSize: number }): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  limpiarFiltros(): void {
    this.filtros.reset({ buscar: '', categoria: 'TODAS', tipoInscripcion: 'TODOS', modalidad: 'TODAS' }, { emitEvent: false });
    this.pageIndex = 0;
    this.aplicarFiltros();
  }

  private aplicarFiltros(): void {
    const buscar = String(this.filtros.get('buscar')?.value || '').toLowerCase().trim();
    const categoria = String(this.filtros.get('categoria')?.value || 'TODAS');
    const tipoInscripcion = String(this.filtros.get('tipoInscripcion')?.value || 'TODOS');
    const modalidad = String(this.filtros.get('modalidad')?.value || 'TODAS');

    this.filtrados = this.eventos.filter((evento) => {
      const coincideBusqueda = !buscar ||
        evento.titulo.toLowerCase().includes(buscar) ||
        evento.descripcion.toLowerCase().includes(buscar) ||
        evento.categoriaNombre.toLowerCase().includes(buscar);

      const coincideCategoria = categoria === 'TODAS' || evento.categoriaNombre === categoria;
      const coincideTipo = tipoInscripcion === 'TODOS' || evento.tipoInscripcion === tipoInscripcion;
      const coincideModalidad = modalidad === 'TODAS' || evento.modalidad === modalidad;
      return coincideBusqueda && coincideCategoria && coincideTipo && coincideModalidad;
    });

    this.pageIndex = 0;
  }
}
