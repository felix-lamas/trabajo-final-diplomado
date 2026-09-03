import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { FacultadListComponent } from './facultades/facultad-list/facultad-list.component';
import { FacultadFormComponent } from './facultades/facultad-form/facultad-form.component';
import { CarreraListComponent } from './carreras/carrera-list/carrera-list.component';
import { CarreraFormComponent } from './carreras/carrera-form/carrera-form.component';
import { CategoriaListComponent } from './categorias/categoria-list/categoria-list.component';
import { CategoriaFormComponent } from './categorias/categoria-form/categoria-form.component';
import { EventoListComponent } from './eventos/evento-list/evento-list.component';
import { EventoFormComponent } from './eventos/evento-form/evento-form.component';
import { EventoDetailComponent } from './eventos/evento-detail/evento-detail.component';
import { ValidarPagosListComponent } from './pagos/validar-pagos-list/validar-pagos-list.component';
import { AdminDashboardComponent } from './dashboard/admin-dashboard.component';

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: AdminDashboardComponent
  },
  {
    path: 'facultades',
    children: [
      { path: '', component: FacultadListComponent },
      { path: 'nuevo', component: FacultadFormComponent },
      { path: 'editar/:id', component: FacultadFormComponent }
    ]
  },
  {
    path: 'carreras',
    children: [
      { path: '', component: CarreraListComponent },
      { path: 'nuevo', component: CarreraFormComponent },
      { path: 'editar/:id', component: CarreraFormComponent }
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
      { path: 'nuevo', component: EventoFormComponent },
      { path: 'editar/:id', component: EventoFormComponent },
      { path: ':id', component: EventoDetailComponent }
    ]
  },
  {
    path: 'pagos',
    children: [
      { path: 'validar', component: ValidarPagosListComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
