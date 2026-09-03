import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { AppShellComponent } from './core/layout/app-shell.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'auth/login'
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then((m) => m.AuthModule)
  },
  {
    path: '',
    component: AppShellComponent,
    children: [
      {
        path: 'admin',
        canMatch: [authGuard, roleGuard],
        data: { roles: ['ADMINISTRADOR'] },
        loadChildren: () => import('./features/admin/admin.module').then((m) => m.AdminModule)
      },
      {
        path: 'asistencias',
        canMatch: [authGuard, roleGuard],
        data: { roles: ['ADMINISTRADOR', 'PERSONAL_CONTROL'] },
        loadChildren: () => import('./features/asistencias/asistencias.module').then((m) => m.AsistenciasModule)
      },
      {
        path: 'certificados',
        canMatch: [authGuard],
        loadChildren: () => import('./features/certificados/certificados.module').then((m) => m.CertificadosModule)
      },
      {
        path: 'encuestas',
        canMatch: [authGuard],
        loadChildren: () => import('./features/encuestas/encuestas.module').then((m) => m.EncuestasModule)
      },
      {
        path: 'privado',
        pathMatch: 'full',
        redirectTo: '/privado/dashboard'
      },
      {
        path: 'privado/dashboard',
        canMatch: [authGuard],
        loadComponent: () => import('./features/privado/dashboard-privado/dashboard-privado.component').then((m) => m.DashboardPrivadoComponent)
      },
      {
        path: 'privado/perfil',
        canMatch: [authGuard],
        loadComponent: () => import('./features/privado/perfil-usuario/perfil-usuario.component').then((m) => m.PerfilUsuarioComponent)
      },
      {
        path: 'privado/inscripciones',
        canMatch: [authGuard],
        loadChildren: () => import('./features/privado/privado.module').then((m) => m.PrivadoInscripcionesModule)
      },
      {
        path: 'privado/pagos',
        canMatch: [authGuard],
        loadChildren: () => import('./features/privado/pagos/pagos.module').then((m) => m.PrivadoPagosModule)
      },
      {
        path: 'privado/credenciales',
        canMatch: [authGuard],
        loadChildren: () => import('./features/privado/credenciales/credenciales.module').then((m) => m.CredencialesModule)
      },
      {
        path: 'reportes',
        canMatch: [authGuard, roleGuard],
        data: { roles: ['ADMINISTRADOR', 'ORGANIZADOR'] },
        loadChildren: () => import('./features/reportes/reportes.module').then((m) => m.ReportesAnaliticaModule)
      }
    ]
  },
  {
    path: '',
    loadChildren: () => import('./features/publico/publico.module').then((m) => m.PublicoModule)
  },
  { path: '**', redirectTo: 'auth/login' }
];
