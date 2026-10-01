import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PageHeaderComponent } from './page-header.component';

describe('PageHeaderComponent', () => {
  let fixture: ComponentFixture<PageHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [PageHeaderComponent] }).compileComponents();
    fixture = TestBed.createComponent(PageHeaderComponent);
    fixture.componentRef.setInput('title', 'Mis eventos');
    fixture.detectChanges();
  });

  it('muestra el título y su descripción cuando existe', () => {
    fixture.componentInstance.description = 'Gestiona las actividades.';
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('h1').textContent).toContain('Mis eventos');
    expect(fixture.nativeElement.querySelector('p').textContent).toContain('Gestiona las actividades.');
  });

  it('admite breadcrumb', () => {
    fixture.componentInstance.breadcrumbs = [{ label: 'Inicio', route: '/' }];
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-breadcrumbs')).not.toBeNull();
  });
});
