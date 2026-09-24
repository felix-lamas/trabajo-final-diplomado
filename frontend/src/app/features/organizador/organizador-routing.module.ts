import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EventoDetailComponent } from '../admin/eventos/evento-detail/evento-detail.component';
import { EventoFormComponent } from '../admin/eventos/evento-form/evento-form.component';
import { EventoListComponent } from '../admin/eventos/evento-list/evento-list.component';

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
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OrganizadorRoutingModule {}
