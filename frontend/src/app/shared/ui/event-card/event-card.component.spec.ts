import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EventCardComponent } from './event-card.component';
import { EstadoEvento, Evento, Modalidad, PublicoObjetivo, TipoInscripcion } from '../../../core/models/evento.model';

const eventFixture: Evento = {
  id: 'event-1', titulo: 'Jornada universitaria', descripcion: 'Descripción del evento universitario.', objetivos: '',
  categoriaId: 'cat-1', categoriaNombre: 'Académico', modalidad: Modalidad.PRESENCIAL, tipoInscripcion: TipoInscripcion.GRATUITO,
  costo: null, fechaInicio: '2026-10-15', fechaFin: '2026-10-15', horaInicio: '09:00', horaFin: '12:00',
  ubicacion: 'Campus Central', direccion: null, latitud: null, longitud: null, radioMetros: null, enlaceVirtual: null,
  requiereInscripcion: true, cupoLimitado: true, cupoMaximo: 100, cupoDisponible: 42, emiteCertificado: false,
  tipoCertificado: null, horasAcademicas: null, publicoObjetivo: PublicoObjetivo.UAJMS, estado: EstadoEvento.PUBLICADO,
  imagenPortada: null, telefonoContacto: null, emailContacto: null, whatsappContacto: null, qrPagoUrl: null,
  instruccionesPago: null, motivoRechazo: null, motivoCancelacion: null, organizadorId: 'org-1', organizadorNombre: 'Organización'
};

describe('EventCardComponent', () => {
  let fixture: ComponentFixture<EventCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [EventCardComponent], providers: [provideRouter([])] }).compileComponents();
    fixture = TestBed.createComponent(EventCardComponent);
    fixture.componentRef.setInput('event', eventFixture);
    fixture.detectChanges();
  });

  it('presenta datos reales del DTO y una acción accesible', () => {
    expect(fixture.nativeElement.textContent).toContain('Jornada universitaria');
    expect(fixture.nativeElement.textContent).toContain('Campus Central');
    expect(fixture.nativeElement.textContent).toContain('Gratis');
    expect(fixture.nativeElement.querySelector('a').getAttribute('aria-label')).toContain('Jornada universitaria');
  });

  it('representa cupos solo cuando el evento los limita y hay valor disponible', () => {
    expect(fixture.nativeElement.textContent).toContain('42 cupos');
    fixture.componentInstance.event = { ...eventFixture, cupoLimitado: false, cupoDisponible: null };
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('42 cupos');
  });

  it('no inventa una imagen cuando no existe en el modelo', () => {
    expect(fixture.nativeElement.querySelector('.vidia-event-card__placeholder')).not.toBeNull();
  });
});
