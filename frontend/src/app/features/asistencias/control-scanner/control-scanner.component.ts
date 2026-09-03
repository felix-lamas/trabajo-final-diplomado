import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

// Material
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

// Service & Model
import { ControlAccesoService } from '../../../core/services/control-acceso.service';
import { ValidarQrResponse } from '../../../core/models/control-acceso.model';
import { EventoService } from '../../../core/services/evento.service';
import { Evento } from '../../../core/models/evento.model';

@Component({
  selector: 'app-control-scanner',
  templateUrl: './control-scanner.component.html',
  styleUrls: [],
  standalone: false
})
export class ControlScannerComponent implements OnInit {
  searchForm: FormGroup;
  qrForm: FormGroup;
  ficha: ValidarQrResponse | null = null;
  loading = false;
  eventos: Evento[] = [];
  observacion = '';

  constructor(
    private fb: FormBuilder,
    private controlAccesoService: ControlAccesoService,
    private eventoService: EventoService,
    private snackBar: MatSnackBar
  ) {
    this.searchForm = this.fb.group({
      tipoBusqueda: ['documento', Validators.required],
      valor: ['', Validators.required],
      eventoId: ['', Validators.required]
    });

    this.qrForm = this.fb.group({
      tokenQr: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loading = true;
    this.eventoService.listar().subscribe({
      next: (data) => {
        this.eventos = data;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Error al cargar eventos', 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });

    // Cambiar validaciones dinámicamente si es por código
    this.searchForm.get('tipoBusqueda')?.valueChanges.subscribe(tipo => {
      const eventoCtrl = this.searchForm.get('eventoId');
      if (tipo === 'codigo') {
        eventoCtrl?.clearValidators();
      } else {
        eventoCtrl?.setValidators([Validators.required]);
      }
      eventoCtrl?.updateValueAndValidity();
    });
  }

  escanearQr(): void {
    if (this.qrForm.invalid) return;

    this.loading = true;
    this.ficha = null;
    this.controlAccesoService.validarQr(this.qrForm.value.tokenQr).subscribe({
      next: (res) => {
        this.ficha = res;
        this.loading = false;
        if (!res.puedeIngresar) {
          this.snackBar.open(res.mensajeValidacion, 'Cerrar', { duration: 5000, panelClass: ['bg-red-600', 'text-white'] });
        }
      },
      error: (err) => {
        this.snackBar.open(err.error?.mensaje || 'QR Inválido', 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  buscarManual(): void {
    if (this.searchForm.invalid) return;

    this.loading = true;
    this.ficha = null;
    const { tipoBusqueda, valor, eventoId } = this.searchForm.value;

    const req$ = tipoBusqueda === 'codigo'
      ? this.controlAccesoService.buscarPorCodigo(valor)
      : this.controlAccesoService.buscarPorDocumento(valor, eventoId);

    req$.subscribe({
      next: (res) => {
        this.ficha = res;
        this.loading = false;
      },
      error: (err) => {
        this.snackBar.open(err.error?.mensaje || 'No encontrado', 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  autorizar(): void {
    if (!this.ficha) return;

    this.loading = true;
    this.controlAccesoService.autorizar(this.ficha.credencialId, this.observacion).subscribe({
      next: () => {
        this.snackBar.open('INGRESO AUTORIZADO CORRECTAMENTE', 'Cerrar', { duration: 3000, panelClass: ['bg-green-600', 'text-white'] });
        this.limpiar();
      },
      error: (err) => {
        this.snackBar.open(err.error?.mensaje || 'Error al autorizar', 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  denegar(): void {
    if (!this.ficha) return;

    this.loading = true;
    this.controlAccesoService.denegar(this.ficha.credencialId, this.observacion || 'Acceso denegado por personal de control').subscribe({
      next: () => {
        this.snackBar.open('INGRESO RECHAZADO Y REGISTRADO', 'Cerrar', { duration: 3000, panelClass: ['bg-orange-600', 'text-white'] });
        this.limpiar();
      },
      error: (err) => {
        this.snackBar.open(err.error?.mensaje || 'Error', 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  limpiar(): void {
    this.ficha = null;
    this.observacion = '';
    this.qrForm.reset();
    this.loading = false;
  }
}
