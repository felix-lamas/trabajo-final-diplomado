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
  readonly navItems: NavItem[] = [
    { label: 'Panel ejecutivo', icon: 'space_dashboard', route: '/admin' },
    { label: 'Dashboard', icon: 'analytics', route: '/reportes/dashboard' },
    { label: 'Eventos', icon: 'event', route: '/admin/eventos' },
    { label: 'Pagos', icon: 'payments', route: '/admin/pagos/validar' },
    { label: 'Participantes', icon: 'groups', route: '/privado/inscripciones' },
    { label: 'Asistencias', icon: 'qr_code_scanner', route: '/asistencias/escaneo' },
    { label: 'Certificados', icon: 'workspace_premium', route: '/certificados/mis-certificados' },
    { label: 'Facultades', icon: 'domain', route: '/admin/facultades' },
    { label: 'Carreras', icon: 'account_tree', route: '/admin/carreras' },
    { label: 'Categorias', icon: 'category', route: '/admin/categorias' },
    { label: 'Reportes', icon: 'bar_chart', route: '/reportes/generador' }
  ];
  readonly quickActions: NavItem[] = [
    { label: 'Nuevo evento', icon: 'add_circle', route: '/admin/eventos/nuevo' },
    { label: 'Validar pagos', icon: 'fact_check', route: '/admin/pagos/validar' },
    { label: 'Escanear QR', icon: 'qr_code_scanner', route: '/asistencias/escaneo' },
    { label: 'Generar reporte', icon: 'bar_chart', route: '/reportes/generador' }
  ];

  readonly userName = signal('Usuario');
  readonly userRole = signal('Acceso institucional');

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('app-theme');
    const isDark = savedTheme === 'dark';
    this.darkMode.set(isDark);
    document.documentElement.dataset['theme'] = isDark ? 'dark' : 'light';

    const user = this.authService.getUser();
    if (user) {
      this.userName.set(`${user.nombres} ${user.apellidos}`.trim());
      this.userRole.set(user.roles?.[0] ?? 'Usuario');
    }

    this.breakpointObserver
      .observe(['(max-width: 1024px)'])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((state) => this.mobile.set(state.matches));

    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((event) => this.breadcrumbs.set(this.buildBreadcrumbs(event.urlAfterRedirects)));

    this.breadcrumbs.set(this.buildBreadcrumbs(this.router.url));
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
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
      verificacion: 'Verificacion'
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
}
