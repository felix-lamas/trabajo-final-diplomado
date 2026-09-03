import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EncuestaService } from '../../../core/services/encuesta.service';

@Component({
  selector: 'app-responder-encuesta',
  templateUrl: './responder-encuesta.component.html',
  standalone: false
})
export class ResponderEncuestaComponent implements OnInit {
  form: FormGroup;
  eventoId = '';
  loading = false;
  submitted = false;
  estrellas = [1, 2, 3, 4, 5];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private encuestaService: EncuestaService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      calificacion: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comentario: ['']
    });
  }

  ngOnInit(): void {
    this.eventoId = this.route.snapshot.paramMap.get('eventoId') || '';
    if (!this.eventoId) {
      this.snackBar.open('Falta el evento', 'Cerrar', { duration: 3000 });
      this.router.navigate(['/']);
    }
  }

  seleccionar(valor: number): void {
    this.form.patchValue({ calificacion: valor });
  }

  onSubmit(): void {
    if (this.form.invalid || !this.eventoId) return;
    this.loading = true;

    this.encuestaService.responder({
      eventoId: this.eventoId,
      calificacion: this.form.value.calificacion,
      comentario: this.form.value.comentario || null
    }).subscribe({
      next: () => {
        this.loading = false;
        this.submitted = true;
        this.snackBar.open('Encuesta enviada', 'Cerrar', { duration: 3000 });
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.mensaje || 'No fue posible enviar la encuesta', 'Cerrar', { duration: 4000 });
      }
    });
  }
}
