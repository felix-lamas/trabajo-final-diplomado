import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type VidiaButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger' | 'success';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './button.component.html',
  styleUrl: './button.component.css'
})
export class ButtonComponent {
  @Input() variant: VidiaButtonVariant = 'primary';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() label = '';
  @Input() icon?: string;
  @Input() iconPosition: 'start' | 'end' = 'start';
  @Input() fullWidth = false;
  @Input() disabled = false;
  @Input() loading = false;
  @Output() pressed = new EventEmitter<MouseEvent>();

  get unavailable(): boolean {
    return this.disabled || this.loading;
  }
}
