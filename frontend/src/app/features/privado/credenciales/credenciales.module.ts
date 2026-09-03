import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

// Material
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

// Components
import { MisCredencialesComponent } from './mis-credenciales/mis-credenciales.component';
import { CredencialDetailComponent } from './credencial-detail/credencial-detail.component';

const routes: Routes = [
  { path: '', component: MisCredencialesComponent },
  { path: ':id', component: CredencialDetailComponent }
];

@NgModule({
  declarations: [
    MisCredencialesComponent,
    CredencialDetailComponent
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
    MatProgressSpinnerModule
  ]
})
export class CredencialesModule { }
