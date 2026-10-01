import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { BreadcrumbItem, BreadcrumbsComponent } from '../breadcrumbs/breadcrumbs.component';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule, BreadcrumbsComponent],
  templateUrl: './page-header.component.html',
  styleUrl: './page-header.component.css'
})
export class PageHeaderComponent {
  @Input({ required: true }) title = '';
  @Input() description = '';
  @Input() breadcrumbs: BreadcrumbItem[] = [];
}
