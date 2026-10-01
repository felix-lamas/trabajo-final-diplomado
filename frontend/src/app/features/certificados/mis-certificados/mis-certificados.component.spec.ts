import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, Subject, throwError } from 'rxjs';
import { CertificadoService } from '../../../core/services/certificado.service';
import { MisCertificadosComponent } from './mis-certificados.component';
import { CertificadosModule } from '../certificados.module';

describe('MisCertificadosComponent', () => {
  let fixture: ComponentFixture<MisCertificadosComponent>;
  let component: MisCertificadosComponent;
  const service = { listarMisCertificados: vi.fn(), descargar: vi.fn() };
  const snackBar = { open: vi.fn() };
  const certificado = {
    id: 'certificado', nombreCompleto: 'Persona Ficticia', ru: null, ci: 'CI-TEST', evento: 'Jornada Demo',
    cargaHoraria: 8, tipoCertificado: 'CURRICULAR' as const, horasAcademicas: 8, porcentajeAsistencia: 100,
    codigoCertificado: 'UAJMS-TEST', fechaEmision: '2026-09-30T12:00:00',
    urlVerificacion: '/verificar-certificado/UAJMS-TEST', estado: 'GENERADO' as const, archivoPdfUrl: '/api/v1/certificados/certificado/descargar'
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    service.listarMisCertificados.mockReturnValue(of([]));
    await TestBed.configureTestingModule({
      imports: [CertificadosModule],
      providers: [
        provideZonelessChangeDetection(),
        { provide: CertificadoService, useValue: service },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(MisCertificadosComponent);
    component = fixture.componentInstance;
  });

  it('representa el estado vacío al terminar la carga', () => {
    fixture.detectChanges();

    expect(component.certificados()).toEqual([]);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBeNull();
  });

  it('representa errores de listado', () => {
    service.listarMisCertificados.mockReturnValue(throwError(() => new Error('red')));

    fixture.detectChanges();

    expect(component.error()).toContain('No fue posible');
    expect(component.loading()).toBe(false);
  });

  it('muestra los campos reales de un certificado curricular y su enlace público', () => {
    service.listarMisCertificados.mockReturnValue(of([certificado]));
    fixture.detectChanges();
    component.verDetalle(certificado);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Jornada Demo');
    expect(fixture.nativeElement.textContent).toContain('Curricular');
    expect(fixture.nativeElement.textContent).toContain('8');
    expect(fixture.nativeElement.textContent).toContain('UAJMS-TEST');
    expect(fixture.nativeElement.querySelector('a[href="/verificar-certificado/UAJMS-TEST"]')).not.toBeNull();
  });

  it('no presenta porcentaje u horas cuando el backend los devuelve nulos', () => {
    const noCurricular = { ...certificado, tipoCertificado: 'NO_CURRICULAR' as const, horasAcademicas: null, cargaHoraria: null, porcentajeAsistencia: null };
    service.listarMisCertificados.mockReturnValue(of([noCurricular]));
    fixture.detectChanges();
    component.verDetalle(noCurricular);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No curricular');
    expect(fixture.nativeElement.textContent).not.toContain('Asistencia registrada');
    expect(fixture.nativeElement.textContent).not.toContain('Carga horaria del evento');
  });

  it('impide una segunda descarga mientras la primera sigue en curso', () => {
    const descarga = new Subject<Blob>();
    service.descargar.mockReturnValue(descarga.asObservable());
    fixture.detectChanges();
    const certificado = { id: 'certificado', codigoCertificado: 'UAJMS-CODIGO', estado: 'GENERADO' } as never;

    component.descargar(certificado);
    component.descargar(certificado);

    expect(service.descargar).toHaveBeenCalledOnce();
    expect(component.descargandoId()).toBe('certificado');
  });
});
