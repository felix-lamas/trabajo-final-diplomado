import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Subject, of, throwError } from 'rxjs';
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
      resolverEnlaceGoogleMaps: vi.fn(() => of({ latitud: -21.5, longitud: -64.7 })),
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

  it('actualiza automáticamente la imagen cuando cambia el QR del backend', async () => {
    const nuevoQr = { id: 'qr-1', sesionId: 'sesion-1', token: 'token-segundo',
      emitidoEn: '2099-10-01T10:01:00', expiraEn: '2099-10-01T10:03:00', activo: true, revocadoEn: null };
    service['generarQr'].mockReturnValue(of({ ...nuevoQr, id: 'qr-0', token: 'token-inicial' }));
    service['obtenerQrActivo'].mockReturnValue(of(nuevoQr));
    component.generarQr({ id: 'sesion-1', activa: true, requiereAsistencia: true } as never);
    await fixture.whenStable();
    expect(component.qrActual()?.id).toBe('qr-0');
    const imagenAnterior = component.qrDataUrl();
    await new Promise(resolve => setTimeout(resolve, 3100));
    await fixture.whenStable();
    expect(service['obtenerQrActivo']).toHaveBeenCalledWith('sesion-1');
    expect(component.qrActual()?.id).toBe('qr-1');
    expect(component.qrDataUrl()).not.toBe(imagenAnterior);
    const imagenActual = fixture.nativeElement.querySelector('img');
    expect(imagenActual?.getAttribute('alt')).toContain('QR temporal');
    expect(imagenActual?.getAttribute('src')).toBe(component.qrDataUrl());
  });

  it('oculta el QR ante errores de red y no conserva la imagen anterior como válida', async () => {
    const qr = { id: 'qr-vigente', sesionId: 'sesion-1', token: 'token-vigente',
      emitidoEn: '2099-10-01T10:00:00', expiraEn: '2099-10-01T10:02:00', activo: true, revocadoEn: null };
    service['generarQr'].mockReturnValue(of(qr));
    service['obtenerQrActivo'].mockReturnValue(throwError(() => new Error('sin conexión')));

    component.generarQr({ id: 'sesion-1', activa: true, requiereAsistencia: true } as never);
    await new Promise(resolve => setTimeout(resolve, 1800));
    await fixture.whenStable();
    expect(service['obtenerQrActivo']).toHaveBeenCalledWith('sesion-1');
    expect(component.qrDataUrl()).toBeNull();
    expect(component.qrEstado()).toContain('Sin conexión');
    expect(fixture.nativeElement.querySelector('img')).toBeNull();
  });

  it('retira de la plantilla un QR que vence mientras espera la renovación del backend', async () => {
    const qrVencido = { id: 'qr-vencido', sesionId: 'sesion-1', token: 'token-vencido',
      emitidoEn: new Date().toISOString(), expiraEn: new Date(Date.now() + 250).toISOString(),
      activo: true, revocadoEn: null };
    service['generarQr'].mockReturnValue(of(qrVencido));
    service['obtenerQrActivo'].mockReturnValue(throwError(() => new Error('renovación pendiente')));

    component.generarQr({ id: 'sesion-1', activa: true, requiereAsistencia: true } as never);
    await new Promise(resolve => setTimeout(resolve, 1800));
    await fixture.whenStable();

    expect(Date.parse(component.qrActual()!.expiraEn)).toBeLessThanOrEqual(Date.now());
    expect(component.qrDataUrl()).toBeNull();
    expect(fixture.nativeElement.querySelector('img')).toBeNull();
  });

  it('evita doble envio mientras crea una sesion', () => {
    component.form.patchValue({ nombre: 'Sesion 1' });
    component.guardar();
    component.guardar();
    expect(service['crearSesion']).toHaveBeenCalledTimes(1);
    expect(component.saving()).toBe(true);
    const request = service['crearSesion'].mock.calls[0][1] as Record<string, unknown>;
    expect(request).toMatchObject({ nombre: 'Sesion 1', latitud: -21.53, longitud: -64.72 });
    expect(request).not.toHaveProperty('mapsLink');
  });

  it('extrae coordenadas explícitas desde q y desde marcadores de lugar de Google Maps', () => {
    component.mapsLink.setValue('https://www.google.com/maps?q=-21.538558%2C-64.764673');
    component.obtenerCoordenadas();
    expect(component.form.value).toMatchObject({ latitud: -21.538558, longitud: -64.764673 });

    component.mapsLink.setValue('https://maps.google.com/maps?query=12.25%2C-68.5');
    component.obtenerCoordenadas();
    expect(component.form.value).toMatchObject({ latitud: 12.25, longitud: -68.5 });

    component.mapsLink.setValue('https://www.google.com/maps/search/-21.538538,+-64.764671');
    component.obtenerCoordenadas();
    expect(component.form.value).toMatchObject({ latitud: -21.538538, longitud: -64.764671 });

    component.mapsLink.setValue('https://www.google.com/maps/place/Lugar/@-21.5,-64.7,17z/data=!3m1!4b1!4m6!3m5!1sabc!8m2!3d-21.538558!4d-64.764673');
    component.obtenerCoordenadas();
    expect(component.form.value).toMatchObject({ latitud: -21.538558, longitud: -64.764673 });
  });

  it.each([
    ['URL que requiere resolución', 'https://www.google.com/maps/place/Lugar', 'No se encontraron coordenadas explícitas'],
    ['URL de otro dominio', 'https://example.com/maps?q=-21%2C-64', 'Use una URL HTTPS de Google Maps'],
    ['coordenadas fuera de rango', 'https://www.google.com/maps?q=91%2C-181', 'fuera de rango'],
    ['coordenadas ambiguas', 'https://www.google.com/maps?q=-21%2C-64&query=-22%2C-65', 'es ambiguo']
  ])('rechaza %s sin modificar ninguna coordenada', (_case, link, expectedMessage) => {
    component.form.patchValue({ latitud: -10.25, longitud: -68.15 });
    component.mapsLink.setValue(link);
    component.obtenerCoordenadas();
    expect(component.form.value).toMatchObject({ latitud: -10.25, longitud: -68.15 });
    expect(component.mapsLinkMessage()).toContain(expectedMessage);
  });

  it('obtener coordenadas no inicia el guardado de la sesión', () => {
    component.mapsLink.setValue('https://www.google.com/maps?q=-21.5%2C-64.7');
    component.obtenerCoordenadas();
    expect(service['crearSesion']).not.toHaveBeenCalled();
    expect(service['actualizarSesion']).not.toHaveBeenCalled();
  });

  it('resuelve enlaces cortos con el backend y actualiza ambos campos GPS', () => {
    component.mapsLink.setValue('https://maps.app.goo.gl/9aeGck7kXVKpCMDb7');
    component.obtenerCoordenadas();
    expect(service['resolverEnlaceGoogleMaps']).toHaveBeenCalledWith('https://maps.app.goo.gl/9aeGck7kXVKpCMDb7');
    expect(component.form.value).toMatchObject({ latitud: -21.5, longitud: -64.7 });
    expect(service['crearSesion']).not.toHaveBeenCalled();
    expect(service['actualizarSesion']).not.toHaveBeenCalled();
  });

  it('deshabilita el control del enlace durante la petición y lo habilita al terminar', () => {
    const resolver$ = new Subject<{ latitud: number; longitud: number }>();
    service['resolverEnlaceGoogleMaps'].mockReturnValue(resolver$.asObservable());
    component.mapsLink.setValue('https://maps.app.goo.gl/no-resuelve');

    component.obtenerCoordenadas();
    expect(component.mapsLink.disabled).toBe(true);

    resolver$.error(new Error('fallo'));
    expect(component.mapsLink.enabled).toBe(true);
  });

  it('conserva las coordenadas actuales si falla la resolución del enlace corto', () => {
    service['resolverEnlaceGoogleMaps'].mockReturnValue(throwError(() => new Error('fallo')));
    component.form.patchValue({ latitud: -10.25, longitud: -68.15 });
    component.mapsLink.setValue('https://maps.app.goo.gl/no-resuelve');
    component.obtenerCoordenadas();
    expect(component.form.value).toMatchObject({ latitud: -10.25, longitud: -68.15 });
    expect(component.mapsLinkMessage()).toContain('No se pudieron obtener coordenadas');
  });
});
