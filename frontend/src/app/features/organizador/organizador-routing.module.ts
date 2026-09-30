import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EventoDetailComponent } from '../admin/eventos/evento-detail/evento-detail.component';
import { EventoFormComponent } from '../admin/eventos/evento-form/evento-form.component';
import { EventoListComponent } from '../admin/eventos/evento-list/evento-list.component';
import { ValidarPagosListComponent } from '../admin/pagos/validar-pagos-list/validar-pagos-list.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'eventos' },
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
  },
  {
    path: 'asistencias',
    loadChildren: () => import('../asistencias/asistencias.module').then((m) => m.AsistenciasModule)
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OrganizadorRoutingModule {}
