import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Subject, of } from 'rxjs';
import { EventosGestionModule } from '../eventos-gestion.module';
import { SesionesEventoComponent } from './sesiones-evento.component';
import { AsistenciaService } from '../../../core/services/asistencia.service';
import { Evento, EstadoEvento, Modalidad, PublicoObjetivo, TipoInscripcion } from '../../../core/models/evento.model';
import { ToastService } from '../../../shared/ui/toast.service';

describe('SesionesEventoComponent', () => {
  let fixture: ComponentFixture<SesionesEventoComponent>;
  let component: SesionesEventoComponent;
  let crear$: Subject<never>;
  let service: Record<string, ReturnType<typeof vi.fn>>;

  const evento: Evento = {
    id: 'evento', titulo: 'Evento', descripcion: '', objetivos: '', categoriaId: 'cat', categoriaNombre: 'Categoria',
    modalidad: Modalidad.PRESENCIAL, tipoInscripcion: TipoInscripcion.GRATUITO, costo: 0,
    fechaInicio: '2026-10-01', fechaFin: '2026-10-02', horaInicio: '08:00', horaFin: '18:00',
    latitud: -21.53, longitud: -64.72, radioMetros: 100, requiereInscripcion: true, cupoLimitado: false,
    cupoMaximo: null, cupoDisponible: null, emiteCertificado: false, publicoObjetivo: PublicoObjetivo.AMBOS,
    estado: EstadoEvento.PUBLICADO, organizadorId: 'org', organizadorNombre: 'Organizador'
  };

  beforeEach(async () => {
    crear$ = new Subject<never>();
    service = {
      listarSesiones: vi.fn(() => of([])), crearSesion: vi.fn(() => crear$.asObservable()),
      actualizarSesion: vi.fn(), cambiarEstado: vi.fn(), generarQr: vi.fn(), obtenerQrActivo: vi.fn()
    };
    await TestBed.configureTestingModule({
      imports: [EventosGestionModule],
      providers: [
        provideZonelessChangeDetection(), provideNoopAnimations(),
        { provide: AsistenciaService, useValue: service },
        { provide: ToastService, useValue: { success: vi.fn(), error: vi.fn() } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(SesionesEventoComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('evento', evento);
    fixture.detectChanges();
  });

  it('carga sesiones y usa Signals para loading y empty', async () => {
    await fixture.whenStable();
    expect(service['listarSesiones']).toHaveBeenCalledWith('evento');
    expect(component.loading()).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Sin sesiones');
  });

  it('evita doble envio mientras crea una sesion', () => {
    component.form.patchValue({ nombre: 'Sesion 1' });
    component.guardar();
    component.guardar();
    expect(service['crearSesion']).toHaveBeenCalledTimes(1);
    expect(component.saving()).toBe(true);
  });
});
