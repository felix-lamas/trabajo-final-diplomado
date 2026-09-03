import { Component, OnInit } from '@angular/core';
import { CredencialService } from '../../../../core/services/credencial.service';
import { Credencial } from '../../../../core/models/credencial.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-mis-credenciales',
  templateUrl: './mis-credenciales.component.html',
  standalone: false
})
export class MisCredencialesComponent implements OnInit {
  credenciales: Credencial[] = [];
  displayedColumns: string[] = ['evento', 'codigo', 'fecha', 'estado', 'acciones'];

  constructor(
    private credencialService: CredencialService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cargarCredenciales();
  }

  cargarCredenciales(): void {
    this.credencialService.listarMisCredenciales().subscribe({
      next: (data) => this.credenciales = data,
      error: () => this.snackBar.open('Error al cargar tus credenciales', 'Cerrar', { duration: 3000 })
    });
  }

  get totalCredenciales(): number {
    return this.credenciales.length;
  }

  get totalActivas(): number {
    return this.credenciales.filter((credencial) => credencial.estado === 'ACTIVA').length;
  }

  get totalInactivas(): number {
    return this.credenciales.filter((credencial) => credencial.estado !== 'ACTIVA').length;
  }

  descargar(id: string): void {
    this.credencialService.descargarPdf(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `credencial_${id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.snackBar.open('Error al descargar la credencial', 'Cerrar')
    });
  }
}
