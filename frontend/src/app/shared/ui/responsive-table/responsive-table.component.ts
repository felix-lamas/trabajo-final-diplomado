import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-responsive-table',
  standalone: true,
  template: '<div class="vidia-responsive-table" role="region" tabindex="0" [attr.aria-label]="label"><ng-content></ng-content></div>',
  styleUrl: './responsive-table.component.css'
})
export class ResponsiveTableComponent {
  @Input({ required: true }) label = '';
}
