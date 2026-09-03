import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CredencialService } from '../../../../core/services/credencial.service';
import { Credencial } from '../../../../core/models/credencial.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-credencial-detail',
  templateUrl: './credencial-detail.component.html',
  standalone: false
})
export class CredencialDetailComponent implements OnInit {
  credencial?: Credencial;
  qrImage: string | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private credencialService: CredencialService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarDetalle(id);
    }
  }

  cargarDetalle(id: string): void {
    this.credencialService.obtenerPorId(id).subscribe({
      next: (data) => {
        this.credencial = data;
        this.cargarQr(id);
      },
      error: () => {
        this.snackBar.open('Error al cargar la credencial', 'Cerrar');
        this.loading = false;
      }
    });
  }

  cargarQr(id: string): void {
    this.credencialService.obtenerQr(id).subscribe({
      next: (blob) => {
        const reader = new FileReader();
        reader.onload = () => {
          this.qrImage = reader.result as string;
          this.loading = false;
        };
        reader.readAsDataURL(blob);
      },
      error: () => {
        this.snackBar.open('Error al cargar el codigo QR', 'Cerrar');
        this.loading = false;
      }
    });
  }

  descargar(): void {
    if (!this.credencial) return;
    this.credencialService.descargarPdf(this.credencial.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `credencial_${this.credencial!.id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.snackBar.open('Error al descargar la credencial', 'Cerrar')
    });
  }
}
