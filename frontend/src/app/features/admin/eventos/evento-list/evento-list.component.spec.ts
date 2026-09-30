import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { Subject, of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { EventoService } from '../../../../core/services/evento.service';
import { Evento, EstadoEvento, Modalidad, PublicoObjetivo, TipoInscripcion } from '../../../../core/models/evento.model';
import { ToastService } from '../../../../shared/ui/toast.service';
import { EventosGestionModule } from '../../../eventos-gestion/eventos-gestion.module';
import { EventoListComponent } from './evento-list.component';

describe('EventoListComponent', () => {
  let fixture: ComponentFixture<EventoListComponent>;
  let component: EventoListComponent;
  let response: Subject<Evento[]>;
  let confirmar: boolean;
  let rolActual: 'ORGANIZADOR' | 'ADMINISTRADOR';
  let service: { listar: ReturnType<typeof vi.fn>; eliminar: ReturnType<typeof vi.fn> };
  const evento: Evento = {
    id: 'evt', titulo: 'Borrador propio', descripcion: 'Descripcion', objetivos: '', categoriaId: 'cat', categoriaNombre: 'Taller',
    modalidad: Modalidad.PRESENCIAL, tipoInscripcion: TipoInscripcion.GRATUITO, costo: 0,
    fechaInicio: '2026-10-01', fechaFin: '2026-10-01', horaInicio: '08:00', horaFin: '10:00',
    requiereInscripcion: false, cupoLimitado: false, cupoMaximo: null, cupoDisponible: null,
    emiteCertificado: false, publicoObjetivo: PublicoObjetivo.UAJMS, estado: EstadoEvento.BORRADOR,
    organizadorId: 'org', organizadorNombre: 'Organizador Demo'
  };

  beforeEach(async () => {
    response = new Subject<Evento[]>(); confirmar = false; rolActual = 'ORGANIZADOR';
    service = { listar: vi.fn(() => response.asObservable()), eliminar: vi.fn(() => of(void 0)) };
    await TestBed.configureTestingModule({
      imports: [EventosGestionModule],
      providers: [
        provideZonelessChangeDetection(), provideNoopAnimations(), provideRouter([]),
        { provide: EventoService, useValue: service },
        { provide: AuthService, useValue: { hasAnyRole: (roles: string[]) => roles.includes(rolActual) } },
        { provide: MatDialog, useValue: { open: vi.fn(() => ({ afterClosed: () => of(confirmar) })) } },
        { provide: ToastService, useValue: { success: vi.fn(), error: vi.fn() } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(EventoListComponent); component = fixture.componentInstance; fixture.detectChanges();
  });

  it('carga por el endpoint con alcance y actualiza el listado', async () => {
    expect(service.listar).toHaveBeenCalled(); expect(component.loading()).toBe(true);
    response.next([evento]); response.complete(); await fixture.whenStable();
    expect(component.eventosFiltrados()).toHaveLength(1);
    expect(fixture.nativeElement.textContent).toContain('Borrador propio');
  });

  it('muestra empty cuando no existen eventos', async () => {
    response.next([]); response.complete(); await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No hay eventos para mostrar');
  });

  it('no ofrece crear evento al administrador', async () => {
    rolActual = 'ADMINISTRADOR'; response.next([]); response.complete(); await fixture.whenStable();
    expect(fixture.nativeElement.textContent).not.toContain('Nuevo evento');
  });

  it('muestra error y permite reintentar', async () => {
    response.error(new Error('red')); await fixture.whenStable();
    expect(component.error()).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('No fue posible cargar los eventos');
  });

  it('cancelar el dialogo no elimina', () => {
    component.eliminar(evento); expect(service.eliminar).not.toHaveBeenCalled();
  });

  it('confirmar el dialogo elimina el borrador', () => {
    confirmar = true; component.eliminar(evento); expect(service.eliminar).toHaveBeenCalledWith('evt');
  });
});
