import { HttpErrorResponse } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { Subject, of } from 'rxjs';
import { SolicitudOrganizador, SolicitudOrganizadorService } from '../../../../core/services/solicitud-organizador.service';
import { SolicitudesOrganizadorComponent } from './solicitudes-organizador.component';

describe('SolicitudesOrganizadorComponent', () => {
  let fixture: ComponentFixture<SolicitudesOrganizadorComponent>;
  let listResponses: Subject<SolicitudOrganizador[]>[];
  let approveResponse: Subject<SolicitudOrganizador>;
  let rejectResponse: Subject<SolicitudOrganizador>;
  let service: any;
  let dialog: any;

  const solicitud: SolicitudOrganizador = {
    usuarioId: '10000000-0000-0000-0000-000000000001',
    nombres: 'Ana',
    apellidos: 'Perez',
    correoElectronico: 'ana@example.test',
    estado: 'PENDIENTE',
    fechaSolicitud: '2026-09-29T12:00:00'
  };

  beforeEach(async () => {
    listResponses = [];
    approveResponse = new Subject<SolicitudOrganizador>();
    rejectResponse = new Subject<SolicitudOrganizador>();
    service = {
      listar: vi.fn(() => {
        const response = new Subject<SolicitudOrganizador[]>();
        listResponses.push(response);
        return response.asObservable();
      }),
      aprobar: vi.fn(() => approveResponse.asObservable()),
      rechazar: vi.fn(() => rejectResponse.asObservable())
    };
    dialog = { open: vi.fn(() => ({ afterClosed: () => of(true) })) };

    await TestBed.configureTestingModule({
      imports: [SolicitudesOrganizadorComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: SolicitudOrganizadorService, useValue: service },
        { provide: MatDialog, useValue: dialog }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(SolicitudesOrganizadorComponent);
    fixture.detectChanges();
  });

  async function resolveList(items: SolicitudOrganizador[]): Promise<void> {
    listResponses.at(-1)!.next(items);
    listResponses.at(-1)!.complete();
    await fixture.whenStable();
  }

  it('muestra loading y listado inmediatamente al resolver HTTP', async () => {
    expect(fixture.nativeElement.querySelector('mat-progress-bar')).not.toBeNull();
    await resolveList([solicitud]);

    expect(fixture.nativeElement.textContent).toContain('Ana Perez');
    expect(fixture.nativeElement.textContent).toContain('PENDIENTE');
  });

  it('muestra estado vacio', async () => {
    await resolveList([]);

    expect(fixture.nativeElement.textContent).toContain('Sin solicitudes');
  });

  it('muestra error y reintento', async () => {
    listResponses[0].error(new HttpErrorResponse({ status: 500, error: {} }));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('No fue posible cargar las solicitudes');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });

  it('confirmacion cancelada no aprueba', async () => {
    await resolveList([solicitud]);
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });
    fixture.componentInstance.aprobar(solicitud);

    expect(service.aprobar).not.toHaveBeenCalled();
  });

  it('aprueba y retira inmediatamente la solicitud de pendientes', async () => {
    await resolveList([solicitud]);
    fixture.componentInstance.aprobar(solicitud);
    expect(service.aprobar).toHaveBeenCalledWith(solicitud.usuarioId);
    expect(fixture.componentInstance.procesandoId()).toBe(solicitud.usuarioId);

    approveResponse.next({ ...solicitud, estado: 'APROBADA' });
    approveResponse.complete();
    await fixture.whenStable();

    expect(fixture.componentInstance.solicitudes()).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('Sin solicitudes');
  });

  it('rechaza con motivo y actualiza inmediatamente', async () => {
    await resolveList([solicitud]);
    dialog.open.mockReturnValue({ afterClosed: () => of('Informacion incompleta') });
    fixture.componentInstance.rechazar(solicitud);

    expect(service.rechazar).toHaveBeenCalledWith(solicitud.usuarioId, 'Informacion incompleta');
    rejectResponse.next({ ...solicitud, estado: 'RECHAZADA', motivoRechazo: 'Informacion incompleta' });
    rejectResponse.complete();
    await fixture.whenStable();

    expect(fixture.componentInstance.solicitudes()).toEqual([]);
  });

  it('cambia filtro y solicita el estado seleccionado', async () => {
    await resolveList([]);
    fixture.componentInstance.cambiarFiltro('RECHAZADA');

    expect(service.listar).toHaveBeenLastCalledWith('RECHAZADA');
    expect(fixture.componentInstance.loading()).toBe(true);
    await resolveList([{ ...solicitud, estado: 'RECHAZADA' }]);
    expect(fixture.nativeElement.textContent).toContain('Ana Perez');
  });

  it('error al resolver conserva la lista y rehabilita acciones', async () => {
    await resolveList([solicitud]);
    fixture.componentInstance.aprobar(solicitud);
    approveResponse.error(new HttpErrorResponse({ status: 400, error: { mensaje: 'La solicitud no esta pendiente' } }));
    await fixture.whenStable();

    expect(fixture.componentInstance.procesandoId()).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('La solicitud no esta pendiente');
    expect(fixture.componentInstance.solicitudes()).toHaveLength(1);
  });
  it('muestra motivo, tipos y datos adicionales en la bandeja', async () => {
    await resolveList([{ ...solicitud, motivoSolicitud: 'Deseo organizar cursos educativos universitarios',
      nombresTiposEventos: ['Cursos y talleres', 'Actividades culturales'], informacionAdicional: 'Experiencia docente' }]);
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Deseo organizar cursos educativos universitarios');
    expect(text).toContain('Cursos y talleres, Actividades culturales');
    expect(text).toContain('Experiencia docente');
  });
  it('muestra solicitudes historicas sin inventar motivos', async () => {
    await resolveList([{ ...solicitud, estado: 'RECHAZADA', motivoRechazo: 'Motivo anterior' }]);
    expect(fixture.nativeElement.textContent).toContain('Solicitud anterior sin motivo registrado');
    expect(fixture.nativeElement.textContent).toContain('Motivo anterior');
  });

});
