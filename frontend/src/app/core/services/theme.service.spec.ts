import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.removeItem('app-theme');
    document.documentElement.removeAttribute('data-theme');
    TestBed.configureTestingModule({ providers: [ThemeService] });
  });

  afterEach(() => {
    localStorage.removeItem('app-theme');
    document.documentElement.removeAttribute('data-theme');
  });

  it('aplica el tema claro por defecto sin sobrescribir la preferencia al inicializar', () => {
    const service = TestBed.inject(ThemeService);
    service.initialize();

    expect(service.theme()).toBe('light');
    expect(document.documentElement.dataset['theme']).toBe('light');
    expect(localStorage.getItem('app-theme')).toBeNull();
  });

  it('restaura el tema oscuro persistido al iniciar la aplicacion', () => {
    localStorage.setItem('app-theme', 'dark');
    const service = TestBed.inject(ThemeService);
    service.initialize();

    expect(service.theme()).toBe('dark');
    expect(document.documentElement.dataset['theme']).toBe('dark');
  });

  it('cambia y persiste el tema sin depender del shell de navegacion', () => {
    const service = TestBed.inject(ThemeService);
    service.initialize();
    service.toggle();

    expect(service.theme()).toBe('dark');
    expect(document.documentElement.dataset['theme']).toBe('dark');
    expect(localStorage.getItem('app-theme')).toBe('dark');
  });
});
