import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

// Material
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatDividerModule } from '@angular/material/divider';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';
import { SkeletonComponent } from '../../shared/ui/skeleton/skeleton.component';
import { EventCardComponent } from '../../shared/ui/event-card/event-card.component';
import { AlertComponent } from '../../shared/ui/alert/alert.component';
import { SurfaceComponent } from '../../shared/ui/surface/surface.component';
import { BadgeComponent } from '../../shared/ui/badge/badge.component';
import { ButtonComponent } from '../../shared/ui/button/button.component';
import { PageHeaderComponent } from '../../shared/ui/page-header/page-header.component';

// Components
import { LandingPublicoComponent } from './landing-publico/landing-publico.component';
import { CatalogoEventosPublicoComponent } from './catalogo-eventos-publico/catalogo-eventos-publico.component';
import { EventoDetallePublicoComponent } from '../eventos/evento-detalle-publico/evento-detalle-publico.component';
import { InscripcionPublicaComponent } from './inscripcion-publica/inscripcion-publica.component';
import { authGuard } from '../../core/guards/auth.guard';
import { roleGuard } from '../../core/guards/role.guard';

const routes: Routes = [
  { path: '', component: LandingPublicoComponent },
  { path: 'eventos', component: CatalogoEventosPublicoComponent },
  { path: 'eventos/:id', component: EventoDetallePublicoComponent },
  {
    path: 'eventos/:id/inscripcion',
    component: InscripcionPublicaComponent,
    canMatch: [authGuard, roleGuard],
    data: { roles: ['USUARIO'] }
  }
];

@NgModule({
  declarations: [
    LandingPublicoComponent,
    CatalogoEventosPublicoComponent,
    EventoDetallePublicoComponent,
    InscripcionPublicaComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    
    // Material
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatPaginatorModule,
    MatDividerModule,
    MatCheckboxModule,
    EmptyStateComponent,
    SkeletonComponent,
    EventCardComponent,
    AlertComponent,
    SurfaceComponent,
    BadgeComponent,
    ButtonComponent,
    PageHeaderComponent
  ]
})
export class PublicoModule { }
