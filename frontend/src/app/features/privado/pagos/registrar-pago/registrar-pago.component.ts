import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PagoService } from '../../../../core/services/pago.service';
import { InscripcionService } from '../../../../core/services/inscripcion.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-registrar-pago',
  templateUrl: './registrar-pago.component.html',
  standalone: false
})
export class RegistrarPagoComponent implements OnInit {
  pagoForm: FormGroup;
  inscripcionId = '';
  eventoTitulo = '';
  montoSugerido = 0;
  archivoComprobante: File | null = null;
  enviando = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private pagoService: PagoService,
    private inscripcionService: InscripcionService,
    private snackBar: MatSnackBar
  ) {
    this.pagoForm = this.fb.group({
      monto: [0, [Validators.required, Validators.min(0.01)]],
      observacion: ['']
    });
  }

  ngOnInit(): void {
    this.inscripcionId = this.route.snapshot.queryParams['inscripcionId'] || '';
    if (this.inscripcionId) {
      this.cargarDetalleInscripcion();
    } else {
      this.router.navigate(['/privado/inscripciones']);
    }
  }

  cargarDetalleInscripcion(): void {
    this.inscripcionService.obtenerPorId(this.inscripcionId).subscribe({
      next: (ins) => {
        this.eventoTitulo = ins.eventoTitulo;
      },
      error: () => this.snackBar.open('Error al cargar datos de la inscripcion', 'Cerrar')
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.archivoComprobante = file;
    }
  }

  guardar(): void {
    if (this.pagoForm.invalid || !this.archivoComprobante) {
      this.snackBar.open('Por favor complete el formulario y suba un comprobante', 'Cerrar');
      return;
    }

    this.enviando = true;
    const request = {
      inscripcionId: this.inscripcionId,
      monto: this.pagoForm.value.monto,
      observacion: this.pagoForm.value.observacion
    };

    this.pagoService.registrarPago(request).subscribe({
      next: (pago) => {
        this.subirArchivo(pago.id);
      },
      error: (err) => {
        this.enviando = false;
        this.snackBar.open(err.error?.mensaje || 'Error al registrar el pago', 'Cerrar');
      }
    });
  }

  subirArchivo(pagoId: string): void {
    if (this.archivoComprobante) {
      this.pagoService.subirComprobante(pagoId, this.archivoComprobante).subscribe({
        next: () => {
          this.snackBar.open('Pago y comprobante registrados correctamente', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/privado/pagos']);
        },
        error: () => {
          this.enviando = false;
          this.snackBar.open('El pago se registro pero hubo un error al subir el archivo', 'Cerrar');
          this.router.navigate(['/privado/pagos']);
        }
      });
    }
  }
}
