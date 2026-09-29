import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { Subject, of } from 'rxjs';
import { CategoriaEvento } from '../../../../core/models/categoria-evento.model';
import { CategoriaEventoService } from '../../../../core/services/categoria-evento.service';
import { ToastService } from '../../../../shared/ui/toast.service';
import { AdminModule } from '../../admin.module';
import { CategoriaListComponent } from './categoria-list.component';

describe('CategoriaListComponent', () => {
  let fixture: ComponentFixture<CategoriaListComponent>;
  let listResponse: Subject<CategoriaEvento[]>;
  let deleteResponse: Subject<void>;
  let service: any;
  let dialog: any;
  let toast: any;
  const categoria: CategoriaEvento = { id: 'cat-1', nombre: 'Conferencia', descripcion: 'Académica', estado: 'ACTIVO' };

  beforeEach(async () => {
    listResponse = new Subject<CategoriaEvento[]>();
    deleteResponse = new Subject<void>();
    service = {
      listar: vi.fn(() => listResponse.asObservable()),
      eliminar: vi.fn(() => deleteResponse.asObservable())
    };
    dialog = { open: vi.fn(() => ({ afterClosed: () => of(true) })) };
    toast = { success: vi.fn(), error: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [AdminModule],
      providers: [
        provideZonelessChangeDetection(),
        provideNoopAnimations(),
        provideRouter([]),
        { provide: CategoriaEventoService, useValue: service },
        { provide: MatDialog, useValue: dialog },
        { provide: ToastService, useValue: toast }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(CategoriaListComponent);
    fixture.detectChanges();
  });

  it('muestra loading y luego el listado de forma reactiva', async () => {
    expect(fixture.componentInstance.loading()).toBe(true);
    listResponse.next([categoria]);
    listResponse.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Conferencia');
    expect(fixture.componentInstance.activas()).toBe(1);
  });

  it('muestra estado vacio', async () => {
    listResponse.next([]);
    listResponse.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No se encontraron categorias');
  });

  it('muestra error y permite reintentar', async () => {
    listResponse.error({ error: { mensaje: 'Servidor no disponible' } });
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).toContain('No fue posible cargar');
    expect(fixture.nativeElement.textContent).toContain('Reintentar');
  });

  it('cancelar el dialogo no elimina', async () => {
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });
    fixture.componentInstance.eliminar(categoria);
    expect(service.eliminar).not.toHaveBeenCalled();
  });

  it('elimina y actualiza el DOM sin recargar', async () => {
    listResponse.next([categoria]);
    listResponse.complete();
    await fixture.whenStable();
    fixture.componentInstance.eliminar(categoria);
    deleteResponse.next();
    deleteResponse.complete();
    await fixture.whenStable();
    expect(fixture.componentInstance.categorias()).toEqual([]);
    expect(fixture.nativeElement.textContent).toContain('No se encontraron categorias');
    expect(toast.success).toHaveBeenCalled();
  });

  it('conserva la categoria y muestra el error backend', async () => {
    listResponse.next([categoria]);
    listResponse.complete();
    await fixture.whenStable();
    fixture.componentInstance.eliminar(categoria);
    deleteResponse.error({ error: { mensaje: 'La categoría tiene eventos' } });
    await fixture.whenStable();
    expect(fixture.componentInstance.categorias()).toHaveLength(1);
    expect(fixture.componentInstance.error()).toContain('No fue posible eliminar');
    expect(toast.error).toHaveBeenCalled();
  });
});
