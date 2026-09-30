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
