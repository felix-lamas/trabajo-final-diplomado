import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ButtonComponent } from './button.component';

describe('ButtonComponent', () => {
  let fixture: ComponentFixture<ButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ButtonComponent] }).compileComponents();
    fixture = TestBed.createComponent(ButtonComponent);
    fixture.detectChanges();
  });

  it('usa por defecto la variante primary y el tipo button', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(button.classList).toContain('vidia-button--primary');
    expect(button.type).toBe('button');
  });

  it('deshabilita el control mientras está cargando e informa aria-busy', () => {
    fixture.componentInstance.loading = true;
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(button.disabled).toBe(true);
    expect(button.getAttribute('aria-busy')).toBe('true');
    expect(fixture.nativeElement.querySelector('.vidia-button__spinner')).not.toBeNull();
  });

  it('refleja variantes y propaga la acción', () => {
    const action = vi.fn();
    fixture.componentInstance.variant = 'danger';
    fixture.componentInstance.pressed.subscribe(action);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('button').click();
    expect(fixture.nativeElement.querySelector('button').classList).toContain('vidia-button--danger');
    expect(action).toHaveBeenCalledOnce();
  });

  it('permite ocupar el ancho disponible cuando se usa en una barra lateral', () => {
    fixture.componentInstance.fullWidth = true;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('button').classList).toContain('vidia-button--full-width');
  });
});
