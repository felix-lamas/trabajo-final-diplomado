import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

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
    path: 'verificar-certificado',
    loadComponent: () => import('./features/certificados/validacion-publica/validacion-publica.component').then((m) => m.ValidacionPublicaComponent)
  },
  {
    path: 'verificar-certificado/:codigo',
    loadComponent: () => import('./features/certificados/validacion-publica/validacion-publica.component').then((m) => m.ValidacionPublicaComponent)
  },
  {
    path: '',
    loadComponent: () => import('./core/layout/app-shell.component').then((m) => m.AppShellComponent),
    children: [
      {
        path: 'admin',
        canMatch: [authGuard, roleGuard],
        data: { roles: ['ADMINISTRADOR'] },
        loadChildren: () => import('./features/admin/admin.module').then((m) => m.AdminModule)
      },
      {
        path: 'organizador',
        canMatch: [authGuard, roleGuard],
        data: { roles: ['ORGANIZADOR'] },
        loadChildren: () => import('./features/organizador/organizador.module').then((m) => m.OrganizadorModule)
      },
      {
        path: 'certificados',
        canMatch: [authGuard],
        loadChildren: () => import('./features/certificados/certificados.module').then((m) => m.CertificadosModule)
      },
      {
        path: 'privado',
        pathMatch: 'full',
        redirectTo: '/eventos'
      },
      {
        path: 'privado/dashboard',
        pathMatch: 'full',
        redirectTo: '/eventos'
      },
      {
        path: 'privado/perfil',
        canMatch: [authGuard],
        loadComponent: () => import('./features/privado/perfil-usuario/perfil-usuario.component').then((m) => m.PerfilUsuarioComponent)
      },
      {
        path: 'privado/solicitud-organizador',
        canMatch: [authGuard, roleGuard],
        data: { roles: ['USUARIO', 'ORGANIZADOR'] },
        loadComponent: () => import('./features/privado/solicitud-organizador/solicitud-organizador.component').then((m) => m.SolicitudOrganizadorComponent)
      },
      {
        path: 'privado/inscripciones',
        canMatch: [authGuard, roleGuard],
        data: { roles: ['USUARIO'] },
        loadChildren: () => import('./features/privado/privado.module').then((m) => m.PrivadoInscripcionesModule)
      },
      {
        path: 'privado/pagos',
        canMatch: [authGuard, roleGuard],
        data: { roles: ['USUARIO'] },
        loadChildren: () => import('./features/privado/pagos/pagos.module').then((m) => m.PrivadoPagosModule)
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
