import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { EstadoEvento, Evento } from '../../../core/models/evento.model';
import { BadgeComponent } from '../badge/badge.component';

@Component({
  selector: 'app-event-card',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatIconModule, BadgeComponent],
  templateUrl: './event-card.component.html',
  styleUrl: './event-card.component.css'
})
export class EventCardComponent {
  @Input({ required: true }) event!: Evento;
  @Input() actionLabel = 'Ver evento';
  @Input() showStatus = false;

  protected readonly EstadoEvento = EstadoEvento;

  get statusVariant(): 'neutral' | 'success' | 'warning' | 'danger' {
    switch (this.event?.estado) {
      case EstadoEvento.PUBLICADO:
      case EstadoEvento.FINALIZADO:
        return 'success';
      case EstadoEvento.EN_REVISION:
        return 'warning';
      case EstadoEvento.RECHAZADO:
      case EstadoEvento.CANCELADO:
        return 'danger';
      default:
        return 'neutral';
    }
  }
}
