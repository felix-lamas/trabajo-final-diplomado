import { NgModule } from '@angular/core';
import { EventosGestionModule } from '../eventos-gestion/eventos-gestion.module';
import { OrganizadorRoutingModule } from './organizador-routing.module';

@NgModule({
  imports: [EventosGestionModule, OrganizadorRoutingModule]
})
export class OrganizadorModule {}
