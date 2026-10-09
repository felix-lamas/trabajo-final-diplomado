import { BreakpointObserver } from '@angular/cdk/layout';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { AppShellComponent } from './app-shell.component';

describe('AppShellComponent navigation by role', () => {
  let fixture: ComponentFixture<AppShellComponent>;
  const currentRoles = signal<string[]>(['USUARIO']);

  beforeEach(async () => {
    currentRoles.set(['USUARIO']);
    await TestBed.configureTestingModule({
      imports: [AppShellComponent],
      providers: [
        provideRouter([]),
        { provide: BreakpointObserver, useValue: { observe: vi.fn(() => of({ matches: false })) } },
        {
          provide: AuthService,
          useValue: {
            roles: () => currentRoles(),
            usuarioActual: () => null,
            hasAnyRole: (roles: string[]) => roles.some((role) => currentRoles().includes(role)),
            refrescarPerfil: () => of(null),
            logout: vi.fn(() => of(void 0))
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppShellComponent);
  });

  it('USUARIO ve Inicio y Buscar eventos, no Eventos publicados', () => {
    const items = fixture.componentInstance.navItems();
    const labels = items.map((item) => item.label);

    expect(labels).toEqual([
      'Inicio',
      'Buscar eventos',
      'Solicitar ser organizador',
      'Mis inscripciones',
      'Mis pagos',
      'Mis certificados',
      'Mi perfil'
    ]);
    expect(labels).not.toContain('Eventos publicados');
    expect(items.find((item) => item.label === 'Inicio')?.route).toBe('/privado/dashboard');
    expect(items.find((item) => item.label === 'Buscar eventos')?.route).toBe('/eventos');
  });

  it('ADMINISTRADOR conserva los accesos de gestión de eventos', () => {
    currentRoles.set(['ADMINISTRADOR']);
    const labels = fixture.componentInstance.navItems().map((item) => item.label);

    expect(labels).toContain('Revisar eventos');
    expect(labels).toContain('Panel administrativo');
    expect(labels).toContain('Usuarios');
    expect(fixture.componentInstance.navGroups().some((group) => group.label === 'Administración' && group.items.some((item) => item.route === '/admin/usuarios'))).toBe(true);
  });

  it('ORGANIZADOR conserva el acceso a sus eventos', () => {
    currentRoles.set(['ORGANIZADOR']);
    const items = fixture.componentInstance.navItems();

    expect(items.some((item) => item.label === 'Mis eventos' && item.route === '/organizador/eventos')).toBe(true);
  });

  it('agrupa las opciones visibles sin cambiar sus rutas y conserva nombre accesible al colapsar', () => {
    const shell = fixture.componentInstance;
    expect(shell.navGroups().map((group) => group.label)).toEqual(['Explorar', 'Cuenta', 'Mi actividad']);
    shell.collapsed.set(true);
    fixture.detectChanges();
    const links = Array.from(fixture.nativeElement.querySelectorAll('.shell__nav a')) as HTMLAnchorElement[];
    expect(links.length).toBeGreaterThan(0);
    expect(links.every((link) => Boolean(link.getAttribute('aria-label')))).toBe(true);
  });
  it('cuenta dual conserva actividad personal y gestion sin duplicar solicitud', () => {
    currentRoles.set(['USUARIO', 'ORGANIZADOR']);
    const shell = fixture.componentInstance;
    const items = shell.navItems();
    expect(items.some((item) => item.route === '/organizador/eventos')).toBe(true);
    for (const route of ['/privado/dashboard', '/privado/inscripciones', '/privado/pagos', '/certificados/mis-certificados']) {
      expect(items.some((item) => item.route === route)).toBe(true);
    }
    expect(items.filter((item) => item.route === '/privado/solicitud-organizador')).toHaveLength(1);
    expect(items.some((item) => item.route.startsWith('/admin'))).toBe(false);
    expect(shell.userRole()).toBe('Usuario · Organizador');
  });

});
