import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { UsuarioAdminService } from '../../../core/services/usuario-admin.service';
import { UsuarioListComponent } from './usuario-list.component';

describe('UsuarioListComponent', () => {
  let fixture: ComponentFixture<UsuarioListComponent>;
  let service: any;
  const usuario = { id: 'u-1', nombres: 'Ada', apellidos: 'Lovelace', correoElectronico: 'ada@example.test', correoVerificado: true, ci: '123', ru: null, celular: null, tipoUsuario: 'INTERNO', estadoSolicitudOrganizador: 'PENDIENTE', roles: ['USUARIO'] };

  beforeEach(async () => {
    service = { listar: vi.fn(() => of([usuario])) };
    await TestBed.configureTestingModule({
      imports: [UsuarioListComponent],
      providers: [provideZonelessChangeDetection(), provideRouter([]), { provide: UsuarioAdminService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(UsuarioListComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('presenta listado de consulta y enlace al detalle sin acciones de modificación', () => {
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Ada Lovelace');
    expect(text).toContain('PENDIENTE');
    expect(text).not.toContain('Cambiar rol');
    expect(text).not.toContain('Desactivar');
    expect(fixture.nativeElement.querySelector('a[aria-label="Ver detalles de Ada Lovelace"]')).not.toBeNull();
  });

  it('filtra localmente por consulta sin alterar el contrato de listado', () => {
    fixture.componentInstance.busqueda.set('123');
    expect(fixture.componentInstance.usuariosFiltrados()).toEqual([usuario]);
    fixture.componentInstance.busqueda.set('no existe');
    expect(fixture.componentInstance.usuariosFiltrados()).toEqual([]);
    expect(service.listar).toHaveBeenCalledTimes(1);
  });

  it('muestra error de carga', async () => {
    service.listar.mockReturnValueOnce(throwError(() => ({ status: 500 })));
    const second = TestBed.createComponent(UsuarioListComponent);
    second.detectChanges();
    await second.whenStable();
    second.detectChanges();
    expect(second.nativeElement.textContent).toContain('No se pudo cargar el directorio');
  });
});
