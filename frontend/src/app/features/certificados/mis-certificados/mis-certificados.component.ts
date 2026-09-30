import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CertificadoService } from '../../../core/services/certificado.service';
import { CertificadoResponse } from '../../../core/models/certificado.model';

@Component({
  selector: 'app-mis-certificados',
  templateUrl: './mis-certificados.component.html',
  styleUrls: [],
  standalone: false
})
export class MisCertificadosComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  readonly certificados = signal<CertificadoResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly certificadoSeleccionado = signal<CertificadoResponse | null>(null);
  readonly descargandoId = signal<string | null>(null);
  readonly displayedColumns = ['codigo', 'evento', 'horas', 'fecha', 'estado', 'acciones'];

  constructor(
    private readonly certificadoService: CertificadoService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cargarCertificados();
  }

  cargarCertificados(): void {
    this.loading.set(true);
    this.error.set(null);
    this.certificadoService.listarMisCertificados()
      .pipe(finalize(() => this.loading.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => this.certificados.set(data),
        error: () => this.error.set('No fue posible recuperar sus certificados.')
      });
  }

  verVistaPrevia(certificado: CertificadoResponse): void {
    this.certificadoSeleccionado.set(certificado);
  }

  descargar(certificado: CertificadoResponse): void {
    if (this.descargandoId() !== null) return;
    this.descargandoId.set(certificado.id);
    this.certificadoService.descargar(certificado.id)
      .pipe(finalize(() => this.descargandoId.set(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pdf) => {
          const url = URL.createObjectURL(pdf);
          const enlace = document.createElement('a');
          enlace.href = url;
          enlace.download = `certificado-${certificado.codigoCertificado}.pdf`;
          enlace.click();
          URL.revokeObjectURL(url);
          this.snackBar.open('Certificado descargado correctamente', 'Cerrar', { duration: 2000 });
          this.cargarCertificados();
        },
        error: () => this.snackBar.open('No fue posible descargar el certificado', 'Cerrar', { duration: 3000 })
      });
  }

  cerrarVistaPrevia(): void {
    this.certificadoSeleccionado.set(null);
  }
}
