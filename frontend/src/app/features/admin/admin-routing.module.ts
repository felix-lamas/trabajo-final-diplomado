import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CategoriaListComponent } from './categorias/categoria-list/categoria-list.component';
import { CategoriaFormComponent } from './categorias/categoria-form/categoria-form.component';
import { EventoListComponent } from './eventos/evento-list/evento-list.component';
import { EventoDetailComponent } from './eventos/evento-detail/evento-detail.component';
import { EventoFormComponent } from './eventos/evento-form/evento-form.component';
import { ValidarPagosListComponent } from './pagos/validar-pagos-list/validar-pagos-list.component';
import { AdminDashboardComponent } from './dashboard/admin-dashboard.component';

const routes: Routes = [
  { path: 'administradores/invitar', loadComponent: () => import('./administradores/invitar-administrador.component').then(m => m.InvitarAdministradorComponent) },
  { path: 'administradores', loadComponent: () => import('./administradores/administradores.component').then(m => m.AdministradoresComponent) },
  {
    path: '',
    pathMatch: 'full',
    component: AdminDashboardComponent
  },
  {
    path: 'usuarios',
    children: [
      { path: '', loadComponent: () => import('./usuarios/usuario-list.component').then((m) => m.UsuarioListComponent) },
      { path: ':id', loadComponent: () => import('./usuarios/usuario-detail.component').then((m) => m.UsuarioDetailComponent) }
    ]
  },
  {
    path: 'categorias',
    children: [
      { path: '', component: CategoriaListComponent },
      { path: 'nuevo', component: CategoriaFormComponent },
      { path: 'editar/:id', component: CategoriaFormComponent }
    ]
  },
  {
    path: 'eventos',
    children: [
      { path: '', component: EventoListComponent },
      { path: 'nuevo', pathMatch: 'full', redirectTo: '' },
      { path: 'editar/:id', component: EventoFormComponent },
      { path: ':id', component: EventoDetailComponent }
    ]
  },
  {
    path: 'pagos',
    children: [
      { path: 'validar', component: ValidarPagosListComponent }
    ]
  },
  {
    path: 'asistencias',
    loadChildren: () => import('../asistencias/asistencias.module').then((m) => m.AsistenciasModule)
  },
  {
    path: 'solicitudes-organizador',
    loadComponent: () => import('./usuarios/solicitudes-organizador/solicitudes-organizador.component')
      .then((m) => m.SolicitudesOrganizadorComponent)
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
