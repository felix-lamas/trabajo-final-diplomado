import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';

import { ResponderEncuestaComponent } from './responder-encuesta/responder-encuesta.component';
import { ResultadosEncuestaComponent } from './resultados-encuesta/resultados-encuesta.component';

const routes: Routes = [
  { path: 'responder/:eventoId', component: ResponderEncuestaComponent },
  { path: 'resultados/:eventoId', component: ResultadosEncuestaComponent }
];

@NgModule({
  declarations: [
    ResponderEncuestaComponent,
    ResultadosEncuestaComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ]
})
export class EncuestasModule {}
