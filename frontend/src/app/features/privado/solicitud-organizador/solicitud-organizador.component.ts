import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { switchMap } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { SolicitudOrganizadorService } from '../../../core/services/solicitud-organizador.service';
import { apiErrorMessage } from '../../../core/utils/api-error.util';

type RequestStatus = 'idle' | 'loading' | 'success' | 'error';

@Component({
  selector: 'app-solicitud-organizador',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatCardModule, MatIconModule, MatProgressBarModule],
  templateUrl: './solicitud-organizador.component.html'
})
export class SolicitudOrganizadorComponent implements OnInit {
  readonly loading = signal(true);
  readonly loadError = signal('');
  readonly requestStatus = signal<RequestStatus>('idle');
  readonly message = signal('');
  readonly estado = computed(() => {
    if (this.authService.roles().includes('ORGANIZADOR')) {
      return 'APROBADA';
    }
    return this.authService.usuarioActual()?.estadoSolicitudOrganizador ?? 'NINGUNA';
  });
  readonly puedeSolicitar = computed(() =>
    this.authService.roles().includes('USUARIO')
      && ['NINGUNA', 'RECHAZADA'].includes(this.estado())
      && this.requestStatus() !== 'loading'
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
    this.authService.refrescarPerfil().subscribe({
      next: () => this.loading.set(false),
      error: (error) => {
        this.loading.set(false);
        this.loadError.set(apiErrorMessage(error, 'No fue posible consultar el estado de la solicitud.'));
      }
    });
  }

  solicitar(): void {
    if (!this.puedeSolicitar()) {
      return;
    }

    this.requestStatus.set('loading');
    this.message.set('');
    this.solicitudes.solicitar().pipe(
      switchMap(() => this.authService.refrescarPerfil())
    ).subscribe({
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
