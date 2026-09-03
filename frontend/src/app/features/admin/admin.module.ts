import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// Material
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule } from '@angular/material/dialog';
import { BreadcrumbsComponent } from '../../shared/ui/breadcrumbs/breadcrumbs.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';
import { SkeletonComponent } from '../../shared/ui/skeleton/skeleton.component';
import { ConfirmDialogComponent } from '../../shared/ui/confirm-dialog/confirm-dialog.component';

// Components
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

// Routing
import { AdminRoutingModule } from './admin-routing.module';

@NgModule({
  declarations: [
    FacultadListComponent,
    FacultadFormComponent,
    CarreraListComponent,
    CarreraFormComponent,
    CategoriaListComponent,
    CategoriaFormComponent,
    EventoListComponent,
    EventoFormComponent,
    EventoDetailComponent,
    ValidarPagosListComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    AdminRoutingModule,
    
    // Material
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressBarModule,
    MatDividerModule,
    MatDialogModule,
    BreadcrumbsComponent,
    EmptyStateComponent,
    SkeletonComponent,
    ConfirmDialogComponent
  ]
})
export class AdminModule { }
