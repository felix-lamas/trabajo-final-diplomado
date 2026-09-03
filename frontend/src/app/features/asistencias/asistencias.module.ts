import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Material Imports
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';

// Components
import { ControlScannerComponent } from './control-scanner/control-scanner.component';
import { HistorialAccesoComponent } from './historial-acceso/historial-acceso.component';
import { AsistenciaListComponent } from './asistencia-list/asistencia-list.component';

const routes: Routes = [
  { path: 'escaneo', component: ControlScannerComponent },
  { path: 'historial', component: HistorialAccesoComponent },
  { path: 'lista', component: AsistenciaListComponent }
];

@NgModule({
  declarations: [
    ControlScannerComponent,
    HistorialAccesoComponent,
    AsistenciaListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    
    // Material Modules
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule
  ]
})
export class AsistenciasModule { }
