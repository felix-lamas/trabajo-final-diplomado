import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SurfaceComponent } from './surface.component';

describe('SurfaceComponent', () => {
  let fixture: ComponentFixture<SurfaceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [SurfaceComponent] }).compileComponents();
    fixture = TestBed.createComponent(SurfaceComponent);
    fixture.detectChanges();
  });

  it('usa la superficie base por defecto', () => {
    expect(fixture.nativeElement.querySelector('.app-surface')).not.toBeNull();
  });

  it('aplica la variante elevada', () => {
    fixture.componentInstance.variant = 'elevated';
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.app-surface').classList).toContain('app-surface--elevated');
  });
});
