import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { EncuestasModule } from '../encuestas/encuestas.module';

// Material
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule } from '@angular/material/dialog';
import { BreadcrumbsComponent } from '../../shared/ui/breadcrumbs/breadcrumbs.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';
import { SkeletonComponent } from '../../shared/ui/skeleton/skeleton.component';
import { ConfirmDialogComponent } from '../../shared/ui/confirm-dialog/confirm-dialog.component';

// Components
import { MisInscripcionesComponent } from './inscripciones/mis-inscripciones/mis-inscripciones.component';
import { InscripcionDetailComponent } from './inscripciones/inscripcion-detail/inscripcion-detail.component';

// Material
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

const routes: Routes = [
  { path: '', component: MisInscripcionesComponent },
  { path: ':id', component: InscripcionDetailComponent }
];

@NgModule({
  declarations: [
    MisInscripcionesComponent,
    InscripcionDetailComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    
    // Material
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    BreadcrumbsComponent,
    EmptyStateComponent,
    SkeletonComponent,
    ConfirmDialogComponent,
    EncuestasModule
  ]
})
export class PrivadoInscripcionesModule { }
