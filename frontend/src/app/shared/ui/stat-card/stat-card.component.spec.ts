import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatCardComponent } from './stat-card.component';

describe('StatCardComponent', () => {
  let fixture: ComponentFixture<StatCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [StatCardComponent] }).compileComponents();
    fixture = TestBed.createComponent(StatCardComponent);
    fixture.componentRef.setInput('label', 'Inscripciones');
    fixture.componentRef.setInput('value', 3);
    fixture.detectChanges();
  });

  it('presenta el label y el valor recibidos sin añadir métricas', () => {
    expect(fixture.nativeElement.textContent).toContain('Inscripciones');
    expect(fixture.nativeElement.querySelector('strong').textContent).toContain('3');
  });

  it('oculta el icono opcional cuando no se proporciona', () => {
    expect(fixture.nativeElement.querySelector('mat-icon')).toBeNull();
  });
});
