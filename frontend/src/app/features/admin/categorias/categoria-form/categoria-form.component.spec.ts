import { HttpErrorResponse } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { Subject } from 'rxjs';
import { CategoriaEvento } from '../../../../core/models/categoria-evento.model';
import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { AdminModule } from '../../admin.module';
import { CategoriaFormComponent } from './categoria-form.component';

describe('CategoriaFormComponent', () => {
  let fixture: ComponentFixture<CategoriaFormComponent>;
  let service: any;
  let createResponse: Subject<CategoriaEvento>;
  let updateResponse: Subject<CategoriaEvento>;
  let getResponse: Subject<CategoriaEvento>;
  let snackBar: any;
  const categoria: CategoriaEvento = { id: 'cat-1', nombre: 'Taller', descripcion: 'Práctico', estado: 'ACTIVO' };

  async function configurar(id: string | null = null): Promise<void> {
    createResponse = new Subject<CategoriaEvento>();
    updateResponse = new Subject<CategoriaEvento>();
    getResponse = new Subject<CategoriaEvento>();
    service = {
      crear: vi.fn(() => createResponse.asObservable()),
      actualizar: vi.fn(() => updateResponse.asObservable()),
      obtenerPorId: vi.fn(() => getResponse.asObservable())
    };
    snackBar = { open: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [AdminModule],
      providers: [
        provideZonelessChangeDetection(), provideNoopAnimations(), provideRouter([]),
        { provide: CategoriaEventoService, useValue: service },
        { provide: MatSnackBar, useValue: snackBar },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap(id ? { id } : {}) } } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(CategoriaFormComponent);
    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('crea con nombre recortado y navega al listado', async () => {
    await configurar();
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.componentInstance.categoriaForm.patchValue({ nombre: '  Jornada  ', descripcion: '  Académica  ' });
    fixture.componentInstance.guardar();

    expect(service.crear).toHaveBeenCalledWith({ nombre: 'Jornada', descripcion: 'Académica' });
    expect(fixture.componentInstance.guardando()).toBe(true);
    createResponse.next(categoria);
    createResponse.complete();
    await fixture.whenStable();
    expect(navigate).toHaveBeenCalledWith(['/admin/categorias']);
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('no envia un formulario invalido', async () => {
    await configurar();
    fixture.componentInstance.categoriaForm.patchValue({ nombre: '  ' });
    fixture.componentInstance.guardar();
    expect(service.crear).not.toHaveBeenCalled();
  });

  it('muestra el error backend al crear', async () => {
    await configurar();
    fixture.componentInstance.categoriaForm.patchValue({ nombre: 'Conferencia' });
    fixture.componentInstance.guardar();
    createResponse.error(new HttpErrorResponse({ status: 409, error: { mensaje: 'Ya existe una categoría' } }));
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).toBe('Ya existe una categoría');
    expect(fixture.nativeElement.textContent).toContain('Ya existe una categoría');
  });

  it('carga una categoria para edicion de forma reactiva', async () => {
    await configurar('cat-1');
    expect(fixture.componentInstance.cargando()).toBe(true);
    getResponse.next(categoria);
    getResponse.complete();
    await fixture.whenStable();
    expect(fixture.componentInstance.categoriaForm.value.nombre).toBe('Taller');
    expect(fixture.nativeElement.querySelector('input').value).toBe('Taller');
  });

  it('actualiza una categoria existente', async () => {
    await configurar('cat-1');
    getResponse.next(categoria);
    getResponse.complete();
    await fixture.whenStable();
    fixture.componentInstance.categoriaForm.patchValue({ nombre: 'Seminario' });
    fixture.componentInstance.guardar();
    expect(service.actualizar).toHaveBeenCalledWith('cat-1', expect.objectContaining({ nombre: 'Seminario', estado: 'ACTIVO' }));
  });

  it('muestra error y reintento cuando falla la carga', async () => {
    await configurar('cat-1');
    getResponse.error(new HttpErrorResponse({ status: 404, error: { mensaje: 'Categoría inexistente' } }));
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).toBe('Categoría inexistente');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });
});
