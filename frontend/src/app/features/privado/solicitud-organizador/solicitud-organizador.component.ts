import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { switchMap } from 'rxjs';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  SolicitudOrganizador,
  TipoEventoSolicitud
} from '../../../core/services/solicitud-organizador.service';
import { AuthService } from '../../../core/services/auth.service';
import { SolicitudOrganizadorService } from '../../../core/services/solicitud-organizador.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

type RequestStatus = 'idle' | 'loading' | 'success' | 'error';

@Component({
  selector: 'app-solicitud-organizador',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule
  ],
  templateUrl: './solicitud-organizador.component.html'
})
export class SolicitudOrganizadorComponent implements OnInit {
  readonly solicitud = signal<SolicitudOrganizador | null>(null);
  readonly tipos = signal<TipoEventoSolicitud[]>([]);
  readonly form = new FormGroup({
    motivoSolicitud: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(30),
        Validators.maxLength(1000),
        (control) => ((control.value?.trim().length ?? 0) < 30 ? { motivoCorto: true } : null)
      ]
    }),
    tiposEventos: new FormControl<string[]>([], {
      nonNullable: true,
      validators: [Validators.required]
    }),
    informacionAdicional: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(1000)]
    })
  });
  readonly loading = signal(true);
  readonly loadError = signal('');
  readonly requestStatus = signal<RequestStatus>('idle');
  readonly message = signal('');
  readonly estado = computed(() => {
    if (this.solicitud()) return this.solicitud()!.estado;
    if (this.authService.roles().includes('ORGANIZADOR')) {
      return 'APROBADA';
    }
    return this.authService.usuarioActual()?.estadoSolicitudOrganizador ?? 'NINGUNA';
  });
  readonly puedeSolicitar = computed(
    () => this.solicitud()?.puedeSolicitar === true && this.requestStatus() !== 'loading'
  );

  constructor(
    private readonly authService: AuthService,
    private readonly solicitudes: SolicitudOrganizadorService
  ) {}

  ngOnInit(): void {
    this.actualizarEstado();
  }

  actualizarEstado(): void {
    this.loading.set(true);
    this.loadError.set('');
    this.authService
      .refrescarPerfil()
      .pipe(
        switchMap(() => this.solicitudes.tiposEventos()),
        switchMap((tipos) => {
          this.tipos.set(tipos);
          return this.solicitudes.obtenerMiSolicitud();
        })
      )
      .subscribe({
        next: (solicitud) => {
          this.solicitud.set(solicitud);
          this.loading.set(false);
        },
        error: (error) => {
          this.loading.set(false);
          this.loadError.set(
            apiErrorMessage(error, 'No fue posible consultar el estado de la solicitud.')
          );
        }
      });
  }

  solicitar(): void {
    this.form.markAllAsTouched();
    if (!this.puedeSolicitar() || this.form.invalid) {
      return;
    }

    this.requestStatus.set('loading');
    this.message.set('');
    this.solicitudes
      .solicitar({
        ...this.form.getRawValue(),
        motivoSolicitud: this.form.controls.motivoSolicitud.value.trim()
      })
      .pipe(
        switchMap((solicitud) => {
          this.solicitud.set(solicitud);
          return this.authService.refrescarPerfil();
        })
      )
      .subscribe({
        next: () => {
          this.requestStatus.set('success');
          this.message.set('Solicitud enviada correctamente. Un administrador debe revisarla.');
        },
        error: (error) => {
          this.requestStatus.set('error');
          this.message.set(apiErrorMessage(error, 'No fue posible enviar la solicitud.'));
        }
      });
  }
}
