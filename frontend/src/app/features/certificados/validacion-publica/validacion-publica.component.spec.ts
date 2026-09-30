import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CertificadoService } from '../../../core/services/certificado.service';
import { ValidacionPublicaComponent } from './validacion-publica.component';

describe('ValidacionPublicaComponent', () => {
  let fixture: ComponentFixture<ValidacionPublicaComponent>;
  let component: ValidacionPublicaComponent;
  const service = { verificarPublicamente: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [ValidacionPublicaComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: CertificadoService, useValue: service },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => null } } } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ValidacionPublicaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('muestra un certificado válido sin datos sensibles', () => {
    service.verificarPublicamente.mockReturnValue(of({
      valido: true,
      mensaje: 'Certificado válido',
      institucion: 'Universidad Autónoma Juan Misael Saracho - UAJMS',
      nombreCompleto: 'Persona de Prueba',
      evento: 'Evento de Prueba',
      tipoCertificado: 'NO_CURRICULAR',
      horasAcademicas: null,
      porcentajeAsistencia: 50,
      fechaEmision: '2026-09-30T12:00:00',
      codigoCertificado: 'UAJMS-CODIGO',
      estado: 'GENERADO'
    }));

    component.verificar(' UAJMS-CODIGO ');

    expect(service.verificarPublicamente).toHaveBeenCalledWith('UAJMS-CODIGO');
    expect(component.resultado()?.valido).toBe(true);
    expect(component.error()).toBeNull();
  });

  it('representa un código inexistente como resultado no válido', () => {
    service.verificarPublicamente.mockReturnValue(of({
      valido: false, mensaje: 'Certificado no registrado', institucion: 'UAJMS', nombreCompleto: null,
      evento: null, tipoCertificado: null, horasAcademicas: null, porcentajeAsistencia: null,
      fechaEmision: null, codigoCertificado: 'NO-EXISTE', estado: 'NO_REGISTRADO'
    }));

    component.verificar('NO-EXISTE');

    expect(component.resultado()?.estado).toBe('NO_REGISTRADO');
  });

  it('expone estado de error sin conservar un resultado anterior', () => {
    service.verificarPublicamente.mockReturnValue(throwError(() => new Error('red')));

    component.verificar('UAJMS-CODIGO');

    expect(component.resultado()).toBeNull();
    expect(component.error()).toContain('No fue posible');
    expect(component.loading()).toBe(false);
  });
});
