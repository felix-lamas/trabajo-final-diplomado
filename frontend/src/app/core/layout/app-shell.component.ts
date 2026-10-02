import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { BreakpointObserver } from '@angular/cdk/layout';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatToolbarModule } from '@angular/material/toolbar';

import { AuthService } from '../services/auth.service';
import { ThemeService } from '../services/theme.service';
import { BreadcrumbItem, BreadcrumbsComponent } from '../../shared/ui/breadcrumbs/breadcrumbs.component';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { IconButtonComponent } from '../../shared/ui/icon-button/icon-button.component';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}

interface NavGroup {
  label: string;
  items: NavItem[];
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatButtonModule,
    MatDividerModule,
    MatIconModule,
    MatTooltipModule,
    BreadcrumbsComponent,
    ButtonComponent,
    IconButtonComponent
  ],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.css'
})
export class AppShellComponent implements OnInit {
  private readonly breakpointObserver = inject(BreakpointObserver);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authService = inject(AuthService);
  private readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);

  @ViewChild('drawer') drawer?: MatSidenav;

  readonly mobile = signal(false);
  readonly collapsed = signal(false);
  readonly darkMode = computed(() => this.themeService.theme() === 'dark');
  readonly themeActionLabel = computed(() => this.darkMode() ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro');
  readonly breadcrumbs = signal<BreadcrumbItem[]>([]);
  readonly currentSection = computed(() => this.breadcrumbs().at(-1)?.label ?? 'Inicio');
  readonly navItems = computed(() => [
    { label: 'Panel administrativo', icon: 'space_dashboard', route: '/admin', roles: ['ADMINISTRADOR'] },
    { label: 'Revisar eventos', icon: 'fact_check', route: '/admin/eventos', roles: ['ADMINISTRADOR'] },
    { label: 'Usuarios', icon: 'groups', route: '/admin/usuarios', roles: ['ADMINISTRADOR'] },
    { label: 'Categorías', icon: 'category', route: '/admin/categorias', roles: ['ADMINISTRADOR'] },
    { label: 'Solicitudes de organizador', icon: 'manage_accounts', route: '/admin/solicitudes-organizador', roles: ['ADMINISTRADOR'] },
    { label: 'Reportes', icon: 'analytics', route: '/reportes/dashboard', roles: ['ADMINISTRADOR', 'ORGANIZADOR'] },
    { label: 'Mis eventos', icon: 'event', route: '/organizador/eventos', roles: ['ORGANIZADOR'] },
    { label: 'Validar pagos', icon: 'fact_check', route: '/organizador/pagos/validar', roles: ['ORGANIZADOR'] },
    { label: 'Validar pagos', icon: 'fact_check', route: '/admin/pagos/validar', roles: ['ADMINISTRADOR'] },
    { label: 'Asistencias', icon: 'how_to_reg', route: '/organizador/asistencias', roles: ['ORGANIZADOR'] },
    { label: 'Asistencias', icon: 'how_to_reg', route: '/admin/asistencias', roles: ['ADMINISTRADOR'] },
    { label: 'Inicio', icon: 'home', route: '/privado/dashboard', roles: ['USUARIO'] },
    { label: 'Buscar eventos', icon: 'search', route: '/eventos', roles: ['USUARIO'] },
    { label: 'Solicitar ser organizador', icon: 'workspace_premium', route: '/privado/solicitud-organizador', roles: ['USUARIO'] },
    { label: 'Estado de organizador', icon: 'workspace_premium', route: '/privado/solicitud-organizador', roles: ['ORGANIZADOR'] },
    { label: 'Mis inscripciones', icon: 'how_to_reg', route: '/privado/inscripciones', roles: ['USUARIO'] },
    { label: 'Mis pagos', icon: 'payments', route: '/privado/pagos', roles: ['USUARIO'] },
    { label: 'Mis certificados', icon: 'workspace_premium', route: '/certificados/mis-certificados', roles: ['USUARIO'] },
    { label: 'Mi perfil', icon: 'account_circle', route: '/privado/perfil', roles: ['USUARIO', 'ORGANIZADOR', 'ADMINISTRADOR'] }
  ].filter((item) => !item.roles || this.authService.hasAnyRole(item.roles)));
  readonly navGroups = computed<NavGroup[]>(() => {
    const groups = new Map<string, NavItem[]>();
    for (const item of this.navItems()) {
      const itemRoles = item.roles ?? [];
      const group = item.route === '/privado/perfil' || item.route === '/privado/solicitud-organizador'
        ? 'Cuenta'
        : itemRoles.includes('USUARIO')
          ? (item.route === '/privado/inscripciones' || item.route === '/privado/pagos' || item.route.startsWith('/certificados/') ? 'Mi actividad' : 'Explorar')
          : item.route === '/admin' || item.route.startsWith('/admin/eventos') || item.route.startsWith('/admin/usuarios') || item.route.startsWith('/admin/categorias') || item.route.startsWith('/admin/solicitudes-organizador')
            ? 'Administración'
            : item.route === '/organizador/eventos'
              ? 'Gestión de eventos'
              : 'Operaciones';
      const groupItems = groups.get(group) ?? [];
      groupItems.push(item);
      groups.set(group, groupItems);
    }
    return Array.from(groups, ([label, items]) => ({ label, items }));
  });
  readonly quickActions = computed(() => [
    { label: 'Nuevo evento', icon: 'add_circle', route: '/organizador/eventos/nuevo', roles: ['ORGANIZADOR'] },
    { label: 'Revisar eventos', icon: 'fact_check', route: '/admin/eventos', roles: ['ADMINISTRADOR'] },
    { label: 'Explorar eventos', icon: 'explore', route: '/eventos', roles: ['USUARIO'] }
  ].filter((item) => !item.roles || this.authService.hasAnyRole(item.roles)));

  readonly userName = computed(() => {
    const user = this.authService.usuarioActual();
    return user ? `${user.nombres} ${user.apellidos}`.trim() : 'Usuario';
  });
  readonly userRole = computed(() => this.authService.roles()[0] ?? 'Acceso institucional');
  readonly loggingOut = signal(false);

  ngOnInit(): void {
    this.breakpointObserver
      .observe(['(max-width: 64rem)'])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((state) => this.mobile.set(state.matches));

    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((event) => {
        this.breadcrumbs.set(this.buildBreadcrumbs(event.urlAfterRedirects));
        this.refrescarIdentidad();
      });

    this.breadcrumbs.set(this.buildBreadcrumbs(this.router.url));
    this.refrescarIdentidad();
  }

  cerrarSesion(): void {
    if (this.loggingOut()) {
      return;
    }
    this.loggingOut.set(true);
    this.authService.logout().subscribe({
      next: () => void this.router.navigate(['/auth/login']),
      error: () => void this.router.navigate(['/auth/login'])
    });
  }

  toggleDrawer(): void {
    if (this.mobile()) {
      this.drawer?.toggle();
      return;
    }

    this.collapsed.update((value) => !value);
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  closeDrawerOnMobile(): void {
    if (this.mobile()) this.drawer?.close();
  }

  private buildBreadcrumbs(url: string): BreadcrumbItem[] {
    const cleanUrl = url.split('?')[0].split('#')[0];
    const segments = cleanUrl.split('/').filter(Boolean);

    if (!segments.length || segments[0] === 'auth') {
      return [];
    }

    const labels: Record<string, string> = {
      admin: 'Administracion',
      asistencias: 'Asistencias',
      certificados: 'Certificados',
      privado: 'Portal estudiante',
      reportes: 'Analitica',
      eventos: 'Eventos',
      pagos: 'Pagos',
      inscripciones: 'Inscripciones',
      perfil: 'Perfil',
      dashboard: 'Dashboard',
      facultades: 'Facultades',
      carreras: 'Carreras',
      categorias: 'Categorias',
      validar: 'Validacion',
      lista: 'Lista',
      generador: 'Generador',
      nuevo: 'Nuevo',
      editar: 'Editar',
      verificacion: 'Verificacion',
      'solicitudes-organizador': 'Solicitudes de organizador'
    };

    const iconByRoot: Record<string, string> = {
      admin: 'space_dashboard',
      asistencias: 'qr_code_scanner',
      certificados: 'workspace_premium',
      privado: 'account_circle',
      reportes: 'analytics',
      eventos: 'event'
    };

    let route = '';
    return segments.map((segment, index) => {
      route += `/${segment}`;
      const isId = /^[0-9a-f-]{8,}$/i.test(segment) || /^\d+$/.test(segment);
      return {
        label: isId ? 'Detalle' : labels[segment] ?? this.toTitle(segment),
        route: index === segments.length - 1 ? undefined : route,
        icon: index === 0 ? iconByRoot[segment] : undefined
      };
    });
  }

  private toTitle(value: string): string {
    return value
      .replace(/-/g, ' ')
      .replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  private refrescarIdentidad(): void {
    this.authService.refrescarPerfil().subscribe({ error: () => undefined });
  }
}
