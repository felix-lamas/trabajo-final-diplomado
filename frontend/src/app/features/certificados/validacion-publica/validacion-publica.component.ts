import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

// Service & Model
import { CertificadoService } from '../../../core/services/certificado.service';
import { VerificacionCertificadoResponse } from '../../../core/models/certificado.model';

@Component({
  selector: 'app-validacion-publica',
  templateUrl: './validacion-publica.component.html',
  styleUrls: [],
  standalone: false
})
export class ValidacionPublicaComponent implements OnInit {
  valForm: FormGroup;
  resultado: VerificacionCertificadoResponse | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private certificadoService: CertificadoService,
    private route: ActivatedRoute
  ) {
    this.valForm = this.fb.group({
      codigo: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    // Si viene el código por la URL (escaneo de QR directo)
    const codigoUrl = this.route.snapshot.paramMap.get('codigo');
    if (codigoUrl) {
      this.valForm.patchValue({ codigo: codigoUrl });
      this.verificar(codigoUrl);
    }
  }

  onSubmit(): void {
    if (this.valForm.invalid) return;
    this.verificar(this.valForm.value.codigo);
  }

  verificar(codigo: string): void {
    this.loading = true;
    this.resultado = null;

    this.certificadoService.verificarPublicamente(codigo).subscribe({
      next: (res) => {
        this.resultado = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  limpiar(): void {
    this.resultado = null;
    this.valForm.reset();
  }
}
