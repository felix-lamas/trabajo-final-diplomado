import { ComponentFixture, TestBed } from '@angular/core/testing';
import { IconButtonComponent } from './icon-button.component';

describe('IconButtonComponent', () => {
  let fixture: ComponentFixture<IconButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [IconButtonComponent] }).compileComponents();
    fixture = TestBed.createComponent(IconButtonComponent);
    fixture.componentRef.setInput('ariaLabel', 'Cerrar');
    fixture.componentRef.setInput('icon', 'close');
    fixture.detectChanges();
  });

  it('expone un nombre accesible obligatorio', () => {
    expect(fixture.nativeElement.querySelector('button').getAttribute('aria-label')).toBe('Cerrar');
  });

  it('respeta el estado disabled', () => {
    fixture.componentInstance.disabled = true;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(true);
  });

  it('refleja estado expanded y pressed en el control nativo', () => {
    fixture.componentInstance.ariaExpanded = true;
    fixture.componentInstance.ariaPressed = true;
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(button.getAttribute('aria-expanded')).toBe('true');
    expect(button.getAttribute('aria-pressed')).toBe('true');
  });
});
