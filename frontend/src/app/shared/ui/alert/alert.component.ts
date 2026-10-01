import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type AlertVariant = 'info' | 'success' | 'warning' | 'error';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.css'
})
export class AlertComponent {
  @Input() variant: AlertVariant = 'info';
  @Input() title = '';
  @Input() icon?: string;

  get role(): 'alert' | 'status' {
    return this.variant === 'error' || this.variant === 'warning' ? 'alert' : 'status';
  }

  get live(): 'assertive' | 'polite' {
    return this.variant === 'error' ? 'assertive' : 'polite';
  }
}
