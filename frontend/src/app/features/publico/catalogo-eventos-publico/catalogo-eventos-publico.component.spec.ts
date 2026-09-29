import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject } from 'rxjs';

import { Evento, EstadoEvento, Modalidad, PublicoObjetivo, TipoInscripcion } from '../../../core/models/evento.model';
import { EventoService } from '../../../core/services/evento.service';
import { PublicoModule } from '../publico.module';
import { CatalogoEventosPublicoComponent } from './catalogo-eventos-publico.component';

describe('CatalogoEventosPublicoComponent reactive HTTP state', () => {
  let fixture: ComponentFixture<CatalogoEventosPublicoComponent>;
  let responses: Subject<Evento[]>[];

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

  it('muestra success inmediatamente al resolver HTTP sin otra deteccion manual', async () => {
    responses[0].next([evento]);
    responses[0].complete();
    await fixture.whenStable();

    expect(fixture.componentInstance.loading).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Evento reactivo');
    expect(fixture.nativeElement.querySelector('.public-event-card')).not.toBeNull();
  });

  it('incluye eventos publicados de pago en el catalogo', async () => {
    responses[0].next([{ ...evento, id: 'pago', titulo: 'Evento pagado', tipoInscripcion: TipoInscripcion.PAGO, costo: 30 }]);
    responses[0].complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Evento pagado');
  });

  it('muestra empty inmediatamente cuando HTTP devuelve una lista vacia', async () => {
    responses[0].next([]);
    responses[0].complete();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('No hay eventos publicados disponibles');
  });

  it('muestra error con reintento cuando HTTP falla', async () => {
    responses[0].error(new Error('fallo de red'));
    await fixture.whenStable();

    expect(fixture.componentInstance.errorCarga).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('No fue posible cargar los eventos');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });

  it('actualiza la vista al reintentar y resolver HTTP sin interaccion adicional', async () => {
    responses[0].error(new Error('fallo inicial'));
    await fixture.whenStable();

    fixture.componentInstance.cargarEventos();
    responses[1].next([evento]);
    responses[1].complete();
    await fixture.whenStable();

    expect(fixture.componentInstance.errorCarga).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Evento reactivo');
  });
});
