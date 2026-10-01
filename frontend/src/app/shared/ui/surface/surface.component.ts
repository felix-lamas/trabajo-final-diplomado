import { Component, Input } from '@angular/core';

export type SurfaceVariant = 'default' | 'elevated' | 'interactive';

@Component({
  selector: 'app-surface',
  standalone: true,
  template: '<div class="app-surface" [class.app-surface--elevated]="variant === \'elevated\'" [class.app-surface--interactive]="variant === \'interactive\'"><ng-content></ng-content></div>',
  styleUrl: './surface.component.css'
})
export class SurfaceComponent {
  @Input() variant: SurfaceVariant = 'default';
}
