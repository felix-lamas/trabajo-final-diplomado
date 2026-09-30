import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { Subject, of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { EventoService } from '../../../../core/services/evento.service';
import { Evento, EstadoEvento, Modalidad, PublicoObjetivo, TipoInscripcion } from '../../../../core/models/evento.model';
import { ToastService } from '../../../../shared/ui/toast.service';
import { EventosGestionModule } from '../../../eventos-gestion/eventos-gestion.module';
import { EventoDetailComponent } from './evento-detail.component';
import { AsistenciaService } from '../../../../core/services/asistencia.service';

describe('EventoDetailComponent', () => {
  let fixture: ComponentFixture<EventoDetailComponent>;
  let component: EventoDetailComponent;
  let response: Subject<Evento>;
  let roles: string[];
  let service: Record<string, ReturnType<typeof vi.fn>>;

  const evento: Evento = {
    id: 'evt', titulo: 'Evento', descripcion: 'Descripcion', objetivos: 'Objetivos', categoriaId: 'cat', categoriaNombre: 'Taller',
    modalidad: Modalidad.VIRTUAL, tipoInscripcion: TipoInscripcion.GRATUITO, costo: 0,
    fechaInicio: '2026-10-01', fechaFin: '2026-10-01', horaInicio: '08:00', horaFin: '10:00',
    enlaceVirtual: 'https://meet.example.test/x', requiereInscripcion: true, cupoLimitado: false,
    cupoMaximo: null, cupoDisponible: null, emiteCertificado: false, publicoObjetivo: PublicoObjetivo.AMBOS,
    estado: EstadoEvento.EN_REVISION, organizadorId: 'org', organizadorNombre: 'Organizador Demo'
  };

  beforeEach(async () => {
    response = new Subject<Evento>(); roles = ['ADMINISTRADOR'];
    service = {
      obtenerPorId: vi.fn(() => response.asObservable()), publicar: vi.fn(() => of(void 0)), rechazar: vi.fn(() => of(void 0)),
      enviarARevision: vi.fn(() => of(void 0)), volverABorrador: vi.fn(() => of(void 0)),
      cancelar: vi.fn(() => of(void 0)), finalizar: vi.fn(() => of(void 0))
    };
    await TestBed.configureTestingModule({
      imports: [EventosGestionModule],
      providers: [
        provideZonelessChangeDetection(), provideNoopAnimations(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => 'evt' } } } },
        { provide: EventoService, useValue: service },
        { provide: AsistenciaService, useValue: { listarSesiones: vi.fn(() => of([])) } },
        { provide: AuthService, useValue: { hasAnyRole: (esperados: string[]) => esperados.some((r) => roles.includes(r)) } },
        { provide: MatDialog, useValue: { open: vi.fn(() => ({ afterClosed: () => of('Motivo valido') })) } },
        { provide: ToastService, useValue: { success: vi.fn(), warning: vi.fn(), error: vi.fn() } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(EventoDetailComponent); component = fixture.componentInstance; fixture.detectChanges();
  });

  it('muestra loading y actualiza el DOM al resolver HTTP', async () => {
    expect(component.loading()).toBe(true);
    response.next(evento); response.complete(); await fixture.whenStable();
    expect(component.loading()).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Evento');
  });

  it('administrador puede publicar y rechazar EN_REVISION', async () => {
    response.next(evento); response.complete(); await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Publicar');
    component.publicar(); expect(service['publicar']).toHaveBeenCalledWith('evt');
    component.rechazar(); expect(service['rechazar']).toHaveBeenCalledWith('evt', 'Motivo valido');
  });

  it('organizador rechazado puede volver a borrador y no cancelar', async () => {
    roles = ['ORGANIZADOR']; response.next({ ...evento, estado: EstadoEvento.RECHAZADO }); response.complete(); await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Volver a borrador');
    expect(fixture.nativeElement.textContent).not.toContain('Cancelar evento');
    component.volverABorrador(); expect(service['volverABorrador']).toHaveBeenCalledWith('evt');
  });

  it('administrador publicado puede cancelar y finalizar', async () => {
    response.next({ ...evento, estado: EstadoEvento.PUBLICADO }); response.complete(); await fixture.whenStable();
    component.cancelar(); component.finalizar();
    expect(service['cancelar']).toHaveBeenCalledWith('evt', 'Motivo valido');
    expect(service['finalizar']).toHaveBeenCalledWith('evt');
  });

  it('muestra estado de error de carga', async () => {
    response.error(new Error('red')); await fixture.whenStable();
    expect(component.error()).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('No fue posible cargar el evento');
  });
});
