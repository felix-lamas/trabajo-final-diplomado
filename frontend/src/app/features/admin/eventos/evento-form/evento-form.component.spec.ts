import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { EventoService } from '../../../../core/services/evento.service';
import { Modalidad, TipoCertificadoEvento, TipoInscripcion } from '../../../../core/models/evento.model';
import { EventosGestionModule } from '../../../eventos-gestion/eventos-gestion.module';
import { EventoFormComponent } from './evento-form.component';

describe('EventoFormComponent', () => {
  let fixture: ComponentFixture<EventoFormComponent>;
  let component: EventoFormComponent;
  let eventoService: { crear: ReturnType<typeof vi.fn>; actualizar: ReturnType<typeof vi.fn>; obtenerPorId: ReturnType<typeof vi.fn>; subirQrPago: ReturnType<typeof vi.fn>; eliminarQrPago: ReturnType<typeof vi.fn>; descargarQrPago: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    eventoService = { crear: vi.fn(() => of({})), actualizar: vi.fn(() => of({})), obtenerPorId: vi.fn(), subirQrPago: vi.fn(() => of(undefined)), eliminarQrPago: vi.fn(() => of(undefined)), descargarQrPago: vi.fn(() => of(new Blob())) };
    await TestBed.configureTestingModule({
      imports: [EventosGestionModule],
      providers: [
        provideZonelessChangeDetection(), provideNoopAnimations(), provideRouter([]),
        { provide: EventoService, useValue: eventoService },
        { provide: CategoriaEventoService, useValue: { listarActivas: vi.fn(() => of([{ id: 'cat', nombre: 'Taller', descripcion: '', estado: 'ACTIVO' }])) } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(EventoFormComponent); component = fixture.componentInstance; fixture.detectChanges();
  });

  it('usa Signals para resolver la carga de categorias', async () => {
    await fixture.whenStable();
    expect(component.categorias().length).toBe(1);
    expect(component.cargandoCategorias()).toBe(false);
  });

  it('muestra campos presenciales y exige geolocalizacion', async () => {
    await fixture.whenStable();
    expect(component.esPresencial()).toBe(true);
    expect(component.eventoForm.get('latitud')?.hasError('required')).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Radio de asistencia');
  });

  it('muestra enlace y deja de exigir campos fisicos para VIRTUAL', async () => {
    component.eventoForm.get('modalidad')?.setValue(Modalidad.VIRTUAL); await fixture.whenStable();
    expect(component.esPresencial()).toBe(false);
    expect(component.eventoForm.get('ubicacion')?.hasError('required')).toBe(false);
    expect(component.eventoForm.get('enlaceVirtual')?.hasError('required')).toBe(true);
  });

  it('PAGO exige monto y permite omitir instrucciones para adjuntar un QR', () => {
    component.eventoForm.get('cupoLimitado')?.setValue(false);
    component.eventoForm.get('tipoInscripcion')?.setValue(TipoInscripcion.PAGO);
    expect(component.esPagado()).toBe(true);
    expect(component.cupoLimitado()).toBe(false);
    expect(component.eventoForm.get('costo')?.hasError('min')).toBe(true);
    expect(component.eventoForm.get('instruccionesPago')?.hasError('required')).toBe(false);
  });

  it('capacidad limitada exige un valor positivo', () => {
    component.eventoForm.get('cupoMaximo')?.setValue(0);
    expect(component.eventoForm.get('cupoMaximo')?.hasError('min')).toBe(true);
    component.eventoForm.get('cupoLimitado')?.setValue(false);
    expect(component.eventoForm.get('cupoMaximo')?.hasError('min')).toBe(false);
  });

  it('certificado CURRICULAR exige horas y NO_CURRICULAR no', () => {
    component.eventoForm.get('emiteCertificado')?.setValue(true);
    component.eventoForm.get('tipoCertificado')?.setValue(TipoCertificadoEvento.CURRICULAR);
    expect(component.eventoForm.get('horasAcademicas')?.hasError('required')).toBe(true);
    component.eventoForm.get('tipoCertificado')?.setValue(TipoCertificadoEvento.NO_CURRICULAR);
    expect(component.eventoForm.get('horasAcademicas')?.hasError('required')).toBe(false);
  });

  it('rechaza un rango temporal invertido', () => {
    component.eventoForm.patchValue({ fechaInicio: '2026-10-02', horaInicio: '10:00', fechaFin: '2026-10-01', horaFin: '10:00' });
    expect(component.eventoForm.hasError('rangoTemporal')).toBe(true);
  });

  it('crea con el modelo completo y limpia campos de modalidad contraria', () => {
    component.eventoForm.patchValue({
      titulo: 'Evento', descripcion: 'Descripcion', objetivos: 'Objetivos', categoriaId: 'cat',
      modalidad: Modalidad.VIRTUAL, enlaceVirtual: 'https://meet.example.test/x',
      fechaInicio: '2026-10-01', horaInicio: '08:00', fechaFin: '2026-10-01', horaFin: '10:00',
      requiereInscripcion: false, cupoLimitado: true, tipoInscripcion: TipoInscripcion.GRATUITO,
      emiteCertificado: false
    });
    component.guardar();
    expect(eventoService.crear).toHaveBeenCalled();
    const request = eventoService.crear.mock.calls[0][0];
    expect(request.ubicacion).toBeUndefined();
    expect(request.cupoLimitado).toBe(false);
    expect(request.costo).toBe(0);
  });

  it('crea el evento primero como JSON y luego carga la imagen QR multipart', () => {
    eventoService.crear.mockReturnValue(of({ id: 'evento-qr' }));
    component.eventoForm.patchValue({
      titulo: 'Evento pagado', descripcion: 'Descripcion', objetivos: 'Objetivos', categoriaId: 'cat',
      modalidad: Modalidad.VIRTUAL, enlaceVirtual: 'https://meet.example.test/x', tipoInscripcion: TipoInscripcion.PAGO,
      costo: 50, fechaInicio: '2026-10-01', horaInicio: '08:00', fechaFin: '2026-10-01', horaFin: '10:00',
      requiereInscripcion: false, cupoLimitado: false, emiteCertificado: false, instruccionesPago: ''
    });
    const file = new File(['qr'], 'pago.png', { type: 'image/png' });
    component.onQrSelected({ target: { files: [file], value: '' } } as unknown as Event);
    component.guardar();
    expect(eventoService.crear).toHaveBeenCalledOnce();
    expect(eventoService.crear.mock.calls[0][0].qrPagoUrl).toBeUndefined();
    expect(eventoService.subirQrPago).toHaveBeenCalledWith('evento-qr', file);
  });

  it('valida formato y tamaño antes de permitir la carga', () => {
    component.onQrSelected({ target: { files: [new File(['x'], 'qr.gif', { type: 'image/gif' })], value: 'bad' } } as unknown as Event);
    expect(component.qrError()).toContain('PNG, JPG o JPEG');
    const oversized = new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'qr.png', { type: 'image/png' });
    component.onQrSelected({ target: { files: [oversized], value: 'large' } } as unknown as Event);
    expect(component.qrError()).toContain('5 MB');
    expect(component.qrFileName()).toBeNull();
  });

  it('limpia selección local sin llamar eliminación remota', () => {
    const file = new File(['x'], 'qr.jpg', { type: 'image/jpeg' });
    component.onQrSelected({ target: { files: [file], value: '' } } as unknown as Event);
    expect(component.qrPreviewUrl()).toContain('blob:');
    component.quitarQr();
    expect(component.qrPreviewUrl()).toBeNull();
    expect(eventoService.eliminarQrPago).not.toHaveBeenCalled();
  });

  it('elimina el QR ya guardado al guardar una edición', () => {
    eventoService.actualizar.mockReturnValue(of({ id: 'evento-edit' }));
    component.esEdicion = true; component.id = 'evento-edit'; component.qrSaved.set(true);
    component.quitarQr();
    component.eventoForm.patchValue({
      titulo: 'Evento', descripcion: 'Descripcion', objetivos: 'Objetivos', categoriaId: 'cat',
      modalidad: Modalidad.VIRTUAL, enlaceVirtual: 'https://meet.example.test/x', tipoInscripcion: TipoInscripcion.PAGO,
      costo: 50, instruccionesPago: 'Transferencia', fechaInicio: '2026-10-01', horaInicio: '08:00',
      fechaFin: '2026-10-01', horaFin: '10:00', requiereInscripcion: false, cupoLimitado: false,
      emiteCertificado: false
    });
    component.guardar();
    expect(eventoService.eliminarQrPago).toHaveBeenCalledWith('evento-edit');
    expect(eventoService.subirQrPago).not.toHaveBeenCalled();
  });

  it('mantiene el error y no considera exitoso un fallo al subir el QR', () => {
    eventoService.crear.mockReturnValue(of({ id: 'evento-error' }));
    eventoService.subirQrPago.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 503 })));
    component.eventoForm.patchValue({
      titulo: 'Evento pagado', descripcion: 'Descripcion', objetivos: 'Objetivos', categoriaId: 'cat',
      modalidad: Modalidad.VIRTUAL, enlaceVirtual: 'https://meet.example.test/x', tipoInscripcion: TipoInscripcion.PAGO,
      costo: 50, instruccionesPago: 'Pago externo', fechaInicio: '2026-10-01', horaInicio: '08:00',
      fechaFin: '2026-10-01', horaFin: '10:00', requiereInscripcion: false, cupoLimitado: false,
      emiteCertificado: false
    });
    const file = new File(['x'], 'qr.png', { type: 'image/png' });
    component.onQrSelected({ target: { files: [file], value: '' } } as unknown as Event);
    component.guardar();
    expect(component.error()).toBeTruthy();
    expect(component.guardando()).toBe(false);
  });

  it('expone el error backend y libera el doble envio', () => {
    eventoService.crear.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 400, error: { mensaje: 'Solicitud invalida' } })));
    component.eventoForm.patchValue({
      titulo: 'Evento', descripcion: 'Descripcion', objetivos: 'Objetivos', categoriaId: 'cat', ubicacion: 'Campus',
      direccion: 'Direccion', latitud: -21.5, longitud: -64.7, radioMetros: 100,
      fechaInicio: '2026-10-01', horaInicio: '08:00', fechaFin: '2026-10-01', horaFin: '10:00'
    });
    component.guardar();
    expect(component.error()).toContain('Solicitud invalida');
    expect(component.guardando()).toBe(false);
  });
});
