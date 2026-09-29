import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-evento-motivo-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>{{ data.titulo }}</h2>
    <mat-dialog-content>
      <p>{{ data.mensaje }}</p>
      <mat-form-field appearance="outline" style="width:100%">
        <mat-label>Motivo</mat-label>
        <textarea matInput rows="4" [formControl]="motivo"></textarea>
        <mat-error *ngIf="motivo.invalid">Ingrese entre 3 y 1000 caracteres.</mat-error>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="ref.close()">Cancelar</button>
      <button mat-flat-button color="warn" type="button" [disabled]="motivo.invalid" (click)="ref.close(motivo.value?.trim())">Confirmar</button>
    </mat-dialog-actions>
  `
})
export class EventoMotivoDialogComponent {
  readonly motivo = new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3), Validators.maxLength(1000)] });

  constructor(
    readonly ref: MatDialogRef<EventoMotivoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) readonly data: { titulo: string; mensaje: string }
  ) {}
}
