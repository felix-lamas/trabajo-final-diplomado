import { CommonModule } from '@angular/common';
import { Component, Inject, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-rechazar-solicitud-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './rechazar-solicitud-dialog.component.html'
})
export class RechazarSolicitudDialogComponent {
  private readonly fb = inject(FormBuilder);
  readonly form = this.fb.group({
    motivo: ['', [Validators.required, Validators.maxLength(500)]]
  });

  constructor(
    readonly dialogRef: MatDialogRef<RechazarSolicitudDialogComponent, string>,
    @Inject(MAT_DIALOG_DATA) readonly data: { nombre: string }
  ) {}

  confirmar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.getRawValue().motivo!.trim());
  }
}
