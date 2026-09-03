import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Material Modules
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

// Components
import { MisCertificadosComponent } from './mis-certificados/mis-certificados.component';
import { ValidacionPublicaComponent } from './validacion-publica/validacion-publica.component';

const routes: Routes = [
  { path: 'mis-certificados', component: MisCertificadosComponent },
  { path: 'verificacion', component: ValidacionPublicaComponent },
  { path: 'verificacion/:codigo', component: ValidacionPublicaComponent }
];

@NgModule({
  declarations: [
    MisCertificadosComponent,
    ValidacionPublicaComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    
    // Material
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule
  ]
})
export class CertificadosModule { }
