import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CertificadoService } from '../../../core/services/certificado.service';
import { VerificacionCertificadoResponse } from '../../../core/models/certificado.model';

@Component({
  selector: 'app-validacion-publica',
  templateUrl: './validacion-publica.component.html',
  styleUrls: [],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatProgressSpinnerModule]
})
export class ValidacionPublicaComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  readonly resultado = signal<VerificacionCertificadoResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly valForm;

  constructor(
    formBuilder: FormBuilder,
    private readonly certificadoService: CertificadoService,
    private readonly route: ActivatedRoute
  ) {
    this.valForm = formBuilder.nonNullable.group({ codigo: ['', [Validators.required, Validators.maxLength(50)]] });
  }

  ngOnInit(): void {
    const codigo = this.route.snapshot.paramMap.get('codigo')?.trim();
    if (codigo) {
      this.valForm.setValue({ codigo });
      this.verificar(codigo);
    }
  }

  onSubmit(): void {
    if (this.valForm.invalid || this.loading()) return;
    this.verificar(this.valForm.controls.codigo.value);
  }

  verificar(codigo: string): void {
    const codigoNormalizado = codigo.trim();
    if (!codigoNormalizado) return;
    this.loading.set(true);
    this.error.set(null);
    this.resultado.set(null);
    this.certificadoService.verificarPublicamente(codigoNormalizado)
      .pipe(finalize(() => this.loading.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultado) => this.resultado.set(resultado),
        error: () => this.error.set('No fue posible consultar el certificado. Intente nuevamente.')
      });
  }

  limpiar(): void {
    this.resultado.set(null);
    this.error.set(null);
    this.valForm.reset();
  }
}
