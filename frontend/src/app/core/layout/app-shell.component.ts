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
import { BreadcrumbItem, BreadcrumbsComponent } from '../../shared/ui/breadcrumbs/breadcrumbs.component';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
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
    BreadcrumbsComponent
  ],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.css'
})
export class AppShellComponent implements OnInit {
  private readonly breakpointObserver = inject(BreakpointObserver);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  @ViewChild('drawer') drawer?: MatSidenav;

  readonly mobile = signal(false);
  readonly collapsed = signal(false);
  readonly darkMode = signal(false);
  readonly breadcrumbs = signal<BreadcrumbItem[]>([]);
  readonly currentSection = computed(() => this.breadcrumbs().at(-1)?.label ?? 'Inicio');
  readonly navItems = computed(() => [
    { label: 'Panel administrativo', icon: 'space_dashboard', route: '/admin', roles: ['ADMINISTRADOR'] },
    { label: 'Revisar eventos', icon: 'fact_check', route: '/admin/eventos', roles: ['ADMINISTRADOR'] },
    { label: 'Categorías', icon: 'category', route: '/admin/categorias', roles: ['ADMINISTRADOR'] },
    { label: 'Solicitudes de organizador', icon: 'manage_accounts', route: '/admin/solicitudes-organizador', roles: ['ADMINISTRADOR'] },
    { label: 'Reportes', icon: 'analytics', route: '/reportes/dashboard', roles: ['ADMINISTRADOR', 'ORGANIZADOR'] },
    { label: 'Mis eventos', icon: 'event', route: '/organizador/eventos', roles: ['ORGANIZADOR'] },
    { label: 'Validar pagos', icon: 'fact_check', route: '/organizador/pagos/validar', roles: ['ORGANIZADOR'] },
    { label: 'Validar pagos', icon: 'fact_check', route: '/admin/pagos/validar', roles: ['ADMINISTRADOR'] },
    { label: 'Asistencias', icon: 'how_to_reg', route: '/organizador/asistencias', roles: ['ORGANIZADOR'] },
    { label: 'Asistencias', icon: 'how_to_reg', route: '/admin/asistencias', roles: ['ADMINISTRADOR'] },
    { label: 'Estado de organizador', icon: 'workspace_premium', route: '/privado/solicitud-organizador', roles: ['USUARIO', 'ORGANIZADOR'] },
    { label: 'Eventos publicados', icon: 'explore', route: '/eventos', roles: ['USUARIO'] },
    { label: 'Mis inscripciones', icon: 'how_to_reg', route: '/privado/inscripciones', roles: ['USUARIO'] },
    { label: 'Mis pagos', icon: 'payments', route: '/privado/pagos', roles: ['USUARIO'] },
    { label: 'Mis certificados', icon: 'workspace_premium', route: '/certificados/mis-certificados', roles: ['USUARIO'] },
    { label: 'Mi perfil', icon: 'account_circle', route: '/privado/perfil', roles: ['USUARIO', 'ORGANIZADOR', 'ADMINISTRADOR'] }
  ].filter((item) => !item.roles || this.authService.hasAnyRole(item.roles)));
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
    const savedTheme = localStorage.getItem('app-theme');
    const isDark = savedTheme === 'dark';
    this.darkMode.set(isDark);
    document.documentElement.dataset['theme'] = isDark ? 'dark' : 'light';

    this.breakpointObserver
      .observe(['(max-width: 1024px)'])
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
    const nextTheme = this.darkMode() ? 'light' : 'dark';
    this.darkMode.set(!this.darkMode());
    document.documentElement.dataset['theme'] = nextTheme;
    localStorage.setItem('app-theme', nextTheme);
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
      credenciales: 'Credenciales',
      inscripciones: 'Inscripciones',
      perfil: 'Perfil',
      dashboard: 'Dashboard',
      facultades: 'Facultades',
      carreras: 'Carreras',
      categorias: 'Categorias',
      validar: 'Validacion',
      escaneo: 'Escaneo QR',
      historial: 'Historial',
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
