import { HttpErrorResponse } from '@angular/common/http';
import { provideZonelessChangeDetection, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject, tap } from 'rxjs';
import { AuthService, AuthUser } from '../../../core/services/auth.service';
import { SolicitudOrganizador, SolicitudOrganizadorService } from '../../../core/services/solicitud-organizador.service';
import { SolicitudOrganizadorComponent } from './solicitud-organizador.component';

describe('SolicitudOrganizadorComponent', () => {
  let fixture: ComponentFixture<SolicitudOrganizadorComponent>;
  let refreshResponses: Subject<AuthUser>[];
  let requestResponse: Subject<SolicitudOrganizador>;
  let userState: ReturnType<typeof signal<AuthUser | null>>;
  let rolesState: ReturnType<typeof signal<string[]>>;
  let auth: any;
  let service: any;

  const user = (estado = 'NINGUNA', roles = ['USUARIO']): AuthUser => ({
    id: '10000000-0000-0000-0000-000000000001',
    nombres: 'Ana',
    apellidos: 'Perez',
    correoElectronico: 'ana@example.test',
    correoVerificado: true,
    estadoSolicitudOrganizador: estado,
    roles
  });
  const solicitud: SolicitudOrganizador = {
    usuarioId: '10000000-0000-0000-0000-000000000001',
    nombres: 'Ana',
    apellidos: 'Perez',
    correoElectronico: 'ana@example.test',
    estado: 'PENDIENTE',
    fechaSolicitud: '2026-09-29T12:00:00'
  };

  beforeEach(async () => {
    refreshResponses = [];
    requestResponse = new Subject<SolicitudOrganizador>();
    userState = signal<AuthUser | null>(user());
    rolesState = signal(['USUARIO']);
    auth = {
      usuarioActual: userState,
      roles: rolesState,
      refrescarPerfil: vi.fn(() => {
        const response = new Subject<AuthUser>();
        refreshResponses.push(response);
        return response.pipe(tap((profile) => {
          userState.set(profile);
          rolesState.set(profile.roles);
        }));
      })
    };
    service = { solicitar: vi.fn(() => requestResponse.asObservable()) };

    await TestBed.configureTestingModule({
      imports: [SolicitudOrganizadorComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthService, useValue: auth },
        { provide: SolicitudOrganizadorService, useValue: service }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(SolicitudOrganizadorComponent);
    fixture.detectChanges();
  });

  async function resolveInitial(profile: AuthUser): Promise<void> {
    refreshResponses[0].next(profile);
    refreshResponses[0].complete();
    await fixture.whenStable();
  }

  it('muestra loading y estado sin solicitud al resolver perfil', async () => {
    expect(fixture.nativeElement.querySelector('mat-progress-bar')).not.toBeNull();
    await resolveInitial(user());

    expect(fixture.nativeElement.textContent).toContain('NINGUNA');
    expect(fixture.nativeElement.textContent).toContain('Solicitar ser organizador');
  });

  it('envia solicitud y muestra PENDIENTE inmediatamente sin interaccion adicional', async () => {
    await resolveInitial(user());
    fixture.componentInstance.solicitar();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Enviando...');

    requestResponse.next(solicitud);
    requestResponse.complete();
    const pending = user('PENDIENTE');
    refreshResponses[1].next(pending);
    refreshResponses[1].complete();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('PENDIENTE');
    expect(fixture.nativeElement.textContent).toContain('Solicitud enviada correctamente');
  });

  it('estado pendiente impide solicitud duplicada', async () => {
    await resolveInitial(user('PENDIENTE'));
    fixture.componentInstance.solicitar();

    expect(service.solicitar).not.toHaveBeenCalled();
    expect(fixture.componentInstance.puedeSolicitar()).toBe(false);
  });

  it('estado rechazado se muestra y permite nueva solicitud', async () => {
    await resolveInitial(user('RECHAZADA'));

    expect(fixture.nativeElement.textContent).toContain('Solicitud rechazada');
    expect(fixture.componentInstance.puedeSolicitar()).toBe(true);
  });

  it('estado aprobado actualiza rol y ofrece acceso a eventos', async () => {
    await resolveInitial(user('APROBADA', ['USUARIO', 'ORGANIZADOR']));

    expect(fixture.nativeElement.textContent).toContain('Solicitud aprobada');
    expect(fixture.nativeElement.textContent).toContain('Ir a mis eventos');
    expect(fixture.componentInstance.puedeSolicitar()).toBe(false);
  });

  it('muestra error de carga con reintento', async () => {
    refreshResponses[0].error(new HttpErrorResponse({ status: 500, error: {} }));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('No se pudo consultar el estado');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });

  it('muestra error de solicitud y rehabilita el boton', async () => {
    await resolveInitial(user());
    fixture.componentInstance.solicitar();
    requestResponse.error(new HttpErrorResponse({
      status: 400,
      error: { mensaje: 'Ya existe una solicitud pendiente' }
    }));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Ya existe una solicitud pendiente');
    expect(fixture.componentInstance.requestStatus()).toBe('error');
    expect(fixture.componentInstance.puedeSolicitar()).toBe(true);
  });
});
