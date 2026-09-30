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
  {
    path: '',
    pathMatch: 'full',
    component: AdminDashboardComponent
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
