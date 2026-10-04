import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Subject } from 'rxjs';

import { Evento, EstadoEvento, Modalidad, PublicoObjetivo, TipoInscripcion } from '../../../core/models/evento.model';
import { EventoService } from '../../../core/services/evento.service';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { PublicoModule } from '../publico.module';
import { CatalogoEventosPublicoComponent } from './catalogo-eventos-publico.component';

describe('CatalogoEventosPublicoComponent reactive HTTP state', () => {
  let fixture: ComponentFixture<CatalogoEventosPublicoComponent>;
  let responses: Subject<Evento[]>[];
  const auth = { isAuthenticated: vi.fn(() => true), logout: vi.fn() };

  const evento: Evento = {
    id: '10000000-0000-0000-0000-000000000001',
    titulo: 'Evento reactivo',
    descripcion: 'Visible inmediatamente al resolver HTTP',
    objetivos: 'Probar la vista',
    categoriaId: '20000000-0000-0000-0000-000000000001',
    categoriaNombre: 'Tecnologia',
    modalidad: Modalidad.PRESENCIAL,
    tipoInscripcion: TipoInscripcion.GRATUITO,
    costo: 0,
    fechaInicio: '2026-10-10',
    fechaFin: '2026-10-10',
    horaInicio: '09:00:00',
    horaFin: '11:00:00',
    requiereInscripcion: true,
    cupoLimitado: false,
    cupoMaximo: null,
    cupoDisponible: null,
    emiteCertificado: false,
    publicoObjetivo: PublicoObjetivo.AMBOS,
    estado: EstadoEvento.PUBLICADO,
    organizadorId: '30000000-0000-0000-0000-000000000001',
    organizadorNombre: 'Organizador Demo'
  };

  beforeEach(async () => {
    responses = [];
    vi.clearAllMocks();
    const eventoService = {
      listarPublicados: vi.fn(() => {
        const response = new Subject<Evento[]>();
        responses.push(response);
        return response.asObservable();
      })
    };

    await TestBed.configureTestingModule({
      imports: [PublicoModule],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthService, useValue: auth },
        { provide: EventoService, useValue: eventoService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CatalogoEventosPublicoComponent);
    fixture.detectChanges();
  });

  it('muestra loading durante la carga inicial', () => {
    expect(fixture.componentInstance.loading).toBe(true);
    expect(fixture.nativeElement.querySelector('app-skeleton')).not.toBeNull();
  });

  it('mantiene opciones de modalidad estables y con identidad por valor', () => {
    const component = fixture.componentInstance;
    const opciones = component.modalidadOpciones;

    expect(opciones).toEqual([
      { value: Modalidad.PRESENCIAL, label: 'Presencial' },
      { value: Modalidad.VIRTUAL, label: 'Virtual' }
    ]);
    expect(component.modalidadOpciones).toBe(opciones);
    expect(component.modalidadOpciones).toBe(opciones);
    expect(component.trackByModalidad(0, opciones[0])).toBe(Modalidad.PRESENCIAL);
    expect(component.trackByModalidad(1, opciones[1])).toBe(Modalidad.VIRTUAL);
  });

  it('muestra success al resolver HTTP', () => {
    responses[0].next([evento]);
    responses[0].complete();
    fixture.detectChanges();

    expect(fixture.componentInstance.loading).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Evento reactivo');
    expect(fixture.nativeElement.querySelector('app-event-card .vidia-event-card')).not.toBeNull();
  });

  it('incluye eventos publicados de pago en el catalogo', () => {
    responses[0].next([{ ...evento, id: 'pago', titulo: 'Evento pagado', tipoInscripcion: TipoInscripcion.PAGO, costo: 30 }]);
    responses[0].complete();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Evento pagado');
  });

  it('el enlace Volver al inicio vuelve al panel sin cerrar la sesión ni ir al login', () => {
    const inicio = fixture.nativeElement.querySelector('a[routerLink="/privado/dashboard"]') as HTMLAnchorElement;
    const router = TestBed.inject(Router);
    const navigation = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

    expect(inicio).not.toBeNull();
    expect(inicio.textContent).toContain('Volver al inicio');
    expect(inicio.getAttribute('href')).toBe('/privado/dashboard');
    inicio.click();

    expect(navigation).toHaveBeenCalledOnce();
    const target = navigation.mock.calls[0][0];
    expect(typeof target === 'string' ? target : router.serializeUrl(target)).toBe('/privado/dashboard');
    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.logout).not.toHaveBeenCalled();
  });

  it('inicia con precio Todos y muestra publicados gratuitos y pagados juntos', () => {
    const eventoPagado = {
      ...evento,
      id: 'pago',
      titulo: 'Evento pagado',
      tipoInscripcion: TipoInscripcion.PAGO,
      costo: 30
    };
    responses[0].next([evento, eventoPagado]);
    responses[0].complete();
    fixture.detectChanges();

    expect(fixture.componentInstance.filtros.get('tipoInscripcion')?.value).toBe('TODOS');
    expect(fixture.nativeElement.textContent).toContain('Evento reactivo');
    expect(fixture.nativeElement.textContent).toContain('Evento pagado');
  });

  it('permite filtrar solo gratuitos y luego solo pagados', () => {
    const eventoPagado = {
      ...evento,
      id: 'pago',
      titulo: 'Evento pagado',
      tipoInscripcion: TipoInscripcion.PAGO,
      costo: 30
    };
    responses[0].next([evento, eventoPagado]);
    responses[0].complete();
    fixture.detectChanges();

    fixture.componentInstance.filtros.get('tipoInscripcion')?.setValue('GRATUITO');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Evento reactivo');
    expect(fixture.nativeElement.textContent).not.toContain('Evento pagado');

    fixture.componentInstance.filtros.get('tipoInscripcion')?.setValue('PAGO');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Evento pagado');
    expect(fixture.nativeElement.textContent).not.toContain('Evento reactivo');
  });

  it('muestra empty inmediatamente cuando HTTP devuelve una lista vacia', () => {
    responses[0].next([]);
    responses[0].complete();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No encontramos eventos');
  });

  it('muestra error con reintento cuando HTTP falla', () => {
    responses[0].error(new Error('fallo de red'));
    fixture.detectChanges();

    expect(fixture.componentInstance.errorCarga).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('No pudimos cargar los eventos');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });

  it('actualiza la vista al reintentar y resolver HTTP', () => {
    responses[0].error(new Error('fallo inicial'));
    fixture.detectChanges();

    fixture.componentInstance.cargarEventos();
    responses[1].next([evento]);
    responses[1].complete();
    fixture.detectChanges();

    expect(fixture.componentInstance.errorCarga).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Evento reactivo');
  });

  it('combina búsqueda, categoría, precio y modalidad sin cambiar el catálogo de publicación', () => {
    const eventoVirtualPagado: Evento = {
      ...evento,
      id: 'virtual-pago',
      titulo: 'Jornada de innovación',
      categoriaNombre: 'Cultura',
      modalidad: Modalidad.VIRTUAL,
      tipoInscripcion: TipoInscripcion.PAGO,
      costo: 30
    };
    responses[0].next([evento, eventoVirtualPagado]);
    responses[0].complete();
    fixture.detectChanges();

    const filtros = fixture.componentInstance.filtros;
    filtros.patchValue({
      buscar: 'innovación',
      categoria: 'Cultura',
      tipoInscripcion: TipoInscripcion.PAGO,
      modalidad: Modalidad.VIRTUAL
    }, { emitEvent: false });
    filtros.get('modalidad')?.setValue(Modalidad.VIRTUAL);
    fixture.detectChanges();

    expect(fixture.componentInstance.totalResultados).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Jornada de innovación');
    expect(fixture.nativeElement.textContent).not.toContain('Evento reactivo');
    expect(fixture.componentInstance.filtros.get('categoria')?.value).toBe('Cultura');
  });

  it('limpia texto y todos los filtros y vuelve a la primera página', () => {
    const eventoPagado = { ...evento, id: 'pago', titulo: 'Evento pagado', tipoInscripcion: TipoInscripcion.PAGO };
    responses[0].next([evento, eventoPagado]);
    responses[0].complete();
    fixture.detectChanges();

    fixture.componentInstance.filtros.patchValue({
      buscar: 'Evento pagado',
      categoria: 'Tecnologia',
      tipoInscripcion: TipoInscripcion.PAGO,
      modalidad: Modalidad.VIRTUAL
    });
    fixture.componentInstance.pageIndex = 2;
    fixture.componentInstance.limpiarFiltros();

    expect(fixture.componentInstance.filtros.getRawValue()).toEqual({
      buscar: '', categoria: 'TODAS', tipoInscripcion: 'TODOS', modalidad: 'TODAS'
    });
    expect(fixture.componentInstance.pageIndex).toBe(0);
    expect(fixture.componentInstance.totalResultados).toBe(2);
  });

  it('mantiene la paginación local y conserva el orden recibido del backend', () => {
    const eventos = Array.from({ length: 13 }, (_, index) => ({
      ...evento,
      id: `evento-${index}`,
      titulo: `Evento ${index}`
    }));
    responses[0].next(eventos);
    responses[0].complete();

    expect(fixture.componentInstance.totalResultados).toBe(13);
    expect(fixture.componentInstance.paginados.map((item) => item.titulo)).toEqual(eventos.slice(0, 6).map((item) => item.titulo));
    fixture.componentInstance.cambiarPagina({ pageIndex: 1, pageSize: 6 });
    fixture.detectChanges();

    expect(fixture.componentInstance.paginados.map((item) => item.titulo)).toEqual(eventos.slice(6, 12).map((item) => item.titulo));
    expect(fixture.nativeElement.querySelectorAll('app-event-card').length).toBe(6);
  });

  it('continúa heredando la estrategia Light/Dark basada en tokens del sistema', async () => {
    const theme = TestBed.inject(ThemeService);
    theme.setTheme('light');
    expect(document.documentElement.dataset['theme']).toBe('light');
    expect(fixture.nativeElement.querySelector('.vidia-catalog__hero')).not.toBeNull();

    theme.setTheme('dark');
    fixture.detectChanges();
    expect(document.documentElement.dataset['theme']).toBe('dark');
    expect(fixture.nativeElement.querySelector('.vidia-catalog__filters')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.vidia-catalog__hero').getAttribute('style')).toBeNull();
  });
});
