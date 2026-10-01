import { Component, Input } from '@angular/core';

export type BadgeVariant = 'neutral' | 'success' | 'warning' | 'danger' | 'info' | 'primary';

@Component({
  selector: 'app-badge',
  standalone: true,
  template: '<span class="app-badge" [class.app-badge--success]="variant === \'success\'" [class.app-badge--warning]="variant === \'warning\'" [class.app-badge--danger]="variant === \'danger\'" [class.app-badge--info]="variant === \'info\'" [class.app-badge--primary]="variant === \'primary\'"><ng-content></ng-content>{{ label }}</span>',
  styleUrl: './badge.component.css'
})
export class BadgeComponent {
  @Input() variant: BadgeVariant = 'neutral';
  @Input() label = '';
}
