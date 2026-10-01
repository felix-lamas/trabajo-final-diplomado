import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

// Material Modules
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

// Components
import { MisCertificadosComponent } from './mis-certificados/mis-certificados.component';
import { AlertComponent } from '../../shared/ui/alert/alert.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../shared/ui/page-header/page-header.component';
import { SkeletonComponent } from '../../shared/ui/skeleton/skeleton.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'mis-certificados' },
  { path: 'mis-certificados', component: MisCertificadosComponent }
];

@NgModule({
  declarations: [
    MisCertificadosComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    
    // Material
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
    AlertComponent,
    EmptyStateComponent,
    PageHeaderComponent,
    SkeletonComponent
  ]
})
export class CertificadosModule { }
