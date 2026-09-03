import { Component, OnInit } from '@angular/core';

// Material
import { MatSnackBar } from '@angular/material/snack-bar';

// Service & Model
import { CertificadoService } from '../../../core/services/certificado.service';
import { CertificadoResponse } from '../../../core/models/certificado.model';

@Component({
  selector: 'app-mis-certificados',
  templateUrl: './mis-certificados.component.html',
  styleUrls: [],
  standalone: false
})
export class MisCertificadosComponent implements OnInit {
  certificados: CertificadoResponse[] = [];
  loading = false;
  certificadoSeleccionado: CertificadoResponse | null = null;
  displayedColumns: string[] = ['codigo', 'evento', 'horas', 'fecha', 'estado', 'acciones'];

  constructor(
    private certificadoService: CertificadoService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cargarCertificados();
  }

  cargarCertificados(): void {
    this.loading = true;
    this.certificadoService.listarMisCertificados().subscribe({
      next: (data) => {
        this.certificados = data;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Error al recuperar certificados personales', 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  verVistaPrevia(cert: CertificadoResponse): void {
    this.certificadoSeleccionado = cert;
  }

  descargar(cert: CertificadoResponse): void {
    this.certificadoService.descargar(cert.id).subscribe({
      next: (res) => {
        this.snackBar.open('Certificado descargado correctamente', 'Cerrar', { duration: 2000 });
        // Simular descarga de archivo abriendo en pestaña nueva
        window.open(res.archivoPdfUrl, '_blank');
        this.cargarCertificados(); // Actualizar estado a DESCARGADO
      },
      error: () => {
        this.snackBar.open('Error al procesar la descarga', 'Cerrar', { duration: 3000 });
      }
    });
  }

  cerrarVistaPrevia(): void {
    this.certificadoSeleccionado = null;
  }
}
