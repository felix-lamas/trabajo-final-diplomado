import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BadgeComponent } from './badge.component';

describe('BadgeComponent', () => {
  let fixture: ComponentFixture<BadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [BadgeComponent] }).compileComponents();
    fixture = TestBed.createComponent(BadgeComponent);
    fixture.detectChanges();
  });

  it('usa variante neutral por defecto', () => {
    expect(fixture.nativeElement.querySelector('.app-badge')).not.toBeNull();
  });

  it('aplica variante semántica', () => {
    fixture.componentInstance.variant = 'warning';
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.app-badge').classList).toContain('app-badge--warning');
  });
});
