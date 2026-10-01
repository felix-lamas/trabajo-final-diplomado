import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResponsiveTableComponent } from './responsive-table.component';

describe('ResponsiveTableComponent', () => {
  let fixture: ComponentFixture<ResponsiveTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ResponsiveTableComponent] }).compileComponents();
    fixture = TestBed.createComponent(ResponsiveTableComponent);
    fixture.componentRef.setInput('label', 'Inscripciones');
    fixture.detectChanges();
  });

  it('ofrece una region con nombre y foco de teclado', () => {
    const region = fixture.nativeElement.querySelector('[role="region"]');
    expect(region.getAttribute('aria-label')).toBe('Inscripciones');
    expect(region.getAttribute('tabindex')).toBe('0');
  });
});
