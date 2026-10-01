import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-icon-button',
  standalone: true,
  imports: [MatIconModule, MatTooltipModule],
  templateUrl: './icon-button.component.html',
  styleUrl: './icon-button.component.css'
})
export class IconButtonComponent {
  @Input({ required: true }) ariaLabel!: string;
  @Input({ required: true }) icon!: string;
  @Input() tooltip?: string;
  @Input() disabled = false;
  @Output() pressed = new EventEmitter<MouseEvent>();
}
