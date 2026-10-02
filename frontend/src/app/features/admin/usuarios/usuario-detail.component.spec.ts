import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { UsuarioAdminService } from '../../../core/services/usuario-admin.service';
import { UsuarioDetailComponent } from './usuario-detail.component';

describe('UsuarioDetailComponent', () => {
  let fixture: ComponentFixture<UsuarioDetailComponent>;
  let service: any;
  const usuario = { id: 'u-2', nombres: 'Grace', apellidos: 'Hopper', correoElectronico: 'grace@example.test', correoVerificado: true, ci: null, ru: 'RU-1', celular: null, tipoUsuario: 'INTERNO', estadoSolicitudOrganizador: null, roles: ['ADMINISTRADOR'] };

  beforeEach(async () => {
    service = { obtenerPorId: vi.fn(() => of(usuario)) };
    await TestBed.configureTestingModule({
      imports: [UsuarioDetailComponent],
      providers: [
        provideZonelessChangeDetection(), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: 'u-2' }) } } },
        { provide: UsuarioAdminService, useValue: service }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(UsuarioDetailComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('consulta el identificador de ruta y presenta solo campos del DTO', () => {
    expect(service.obtenerPorId).toHaveBeenCalledWith('u-2');
    expect(fixture.nativeElement.textContent).toContain('Grace Hopper');
    expect(fixture.nativeElement.textContent).toContain('RU-1');
    expect(fixture.nativeElement.textContent).not.toContain('Contraseña');
  });

  it('muestra un error cuando el backend rechaza la consulta', async () => {
    service.obtenerPorId.mockReturnValueOnce(throwError(() => ({ status: 404 })));
    const second = TestBed.createComponent(UsuarioDetailComponent);
    second.detectChanges();
    await second.whenStable();
    second.detectChanges();
    expect(second.nativeElement.textContent).toContain('No se pudo cargar el usuario');
  });
});
