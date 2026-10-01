import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AlertComponent } from './alert.component';

describe('AlertComponent', () => {
  let fixture: ComponentFixture<AlertComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [AlertComponent] }).compileComponents();
    fixture = TestBed.createComponent(AlertComponent);
    fixture.detectChanges();
  });

  it('usa role status para mensajes informativos', () => {
    expect(fixture.nativeElement.querySelector('[role="status"]')).not.toBeNull();
  });

  it('anuncia errores con prioridad assertive', () => {
    fixture.componentInstance.variant = 'error';
    fixture.detectChanges();
    const alert = fixture.nativeElement.querySelector('[role="alert"]');
    expect(alert.getAttribute('aria-live')).toBe('assertive');
  });
});
