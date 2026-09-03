import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './skeleton.component.html',
  styleUrl: './skeleton.component.css'
})
export class SkeletonComponent {
  @Input() variant: 'card' | 'table' | 'dashboard' | 'list' = 'card';
  @Input() count = 1;

  get items(): unknown[] {
    return Array.from({ length: this.count });
  }
}
