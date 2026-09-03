import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { EventoService } from '../../../core/services/evento.service';
import { Evento } from '../../../core/models/evento.model';

@Component({
  selector: 'app-catalogo-eventos-publico',
  templateUrl: './catalogo-eventos-publico.component.html',
  standalone: false
})
export class CatalogoEventosPublicoComponent implements OnInit {
  eventos: Evento[] = [];
  filtrados: Evento[] = [];
  loading = true;
  pageIndex = 0;
  pageSize = 6;
  filtros: FormGroup;

  constructor(
    private fb: FormBuilder,
    private eventoService: EventoService
  ) {
    this.filtros = this.fb.group({
      buscar: [''],
      categoria: ['TODAS']
    });
  }

  ngOnInit(): void {
    this.eventoService.listarPublicados().subscribe({
      next: (data) => {
        this.eventos = data;
        this.loading = false;
        this.aplicarFiltros();
      },
      error: () => {
        this.loading = false;
        this.eventos = [];
        this.filtrados = [];
      }
    });

    this.filtros.valueChanges.subscribe(() => this.aplicarFiltros());
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

  cambiarPagina(event: { pageIndex: number; pageSize: number }): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  limpiarFiltros(): void {
    this.filtros.reset({ buscar: '', categoria: 'TODAS' }, { emitEvent: false });
    this.pageIndex = 0;
    this.aplicarFiltros();
  }

  private aplicarFiltros(): void {
    const buscar = String(this.filtros.get('buscar')?.value || '').toLowerCase().trim();
    const categoria = String(this.filtros.get('categoria')?.value || 'TODAS');

    this.filtrados = this.eventos.filter((evento) => {
      const coincideBusqueda = !buscar ||
        evento.titulo.toLowerCase().includes(buscar) ||
        evento.descripcion.toLowerCase().includes(buscar) ||
        evento.categoriaNombre.toLowerCase().includes(buscar);

      const coincideCategoria = categoria === 'TODAS' || evento.categoriaNombre === categoria;
      return coincideBusqueda && coincideCategoria;
    });

    this.pageIndex = 0;
  }
}
