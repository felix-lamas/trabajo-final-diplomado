import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { of } from 'rxjs';
import { AsistenciaService } from '../../../core/services/asistencia.service';
import { EventoService } from '../../../core/services/evento.service';
import { AsistenciaListComponent } from './asistencia-list.component';
import { AlertComponent } from '../../../shared/ui/alert/alert.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { SkeletonComponent } from '../../../shared/ui/skeleton/skeleton.component';

describe('AsistenciaListComponent', () => {
  let fixture: ComponentFixture<AsistenciaListComponent>;
  const evento = {
    id: 'evento-1', titulo: 'Evento ficticio', descripcion: '', objetivos: '', categoriaId: 'cat', categoriaNombre: 'Académico',
    modalidad: 'PRESENCIAL', tipoInscripcion: 'GRATUITO', costo: 0, fechaInicio: '2026-09-30', fechaFin: '2026-09-30',
    horaInicio: '09:00', horaFin: '10:00', requiereInscripcion: true, cupoLimitado: false, cupoMaximo: null,
    cupoDisponible: null, emiteCertificado: false, publicoObjetivo: 'UAJMS', estado: 'FINALIZADO',
    organizadorId: 'organizador', organizadorNombre: 'Organizador Ficticio'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AsistenciaListComponent],
      imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule,
        MatIconModule, MatOptionModule, MatSelectModule, MatProgressSpinnerModule, MatTableModule,
        AlertComponent, EmptyStateComponent, SkeletonComponent],
      providers: [
        { provide: AsistenciaService, useValue: { listarPorEvento: vi.fn(() => of([{
          id: 'asistencia-1', nombreParticipante: 'Participante Ficticio', documentoIdentidad: 'CI-FICTICIO',
          codigoParticipante: 'PART-FICTICIO', evento: 'Evento ficticio', sesionEventoId: 'sesion-1',
          sesion: 'Sesión 1', fechaHoraRegistro: '2026-09-30T10:00:00', registradoPor: null,
          distanciaMetros: null, precisionGpsMetros: null, resultadoValidacion: 'VALIDADO', observacion: null
        }])) } },
        { provide: EventoService, useValue: { listar: () => of([evento]) } },
        { provide: MatSnackBar, useValue: { open: vi.fn() } }
      ]
    });
    fixture = TestBed.createComponent(AsistenciaListComponent);
    fixture.detectChanges();
  });

  it('renderiza una fila del DTO actual cuando registradoPor es null', () => {
    fixture.componentInstance.filtroForm.setValue({ eventoId: evento.id });
    fixture.componentInstance.cargarAsistencias();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Participante Ficticio');
    expect(fixture.nativeElement.textContent).toContain('No requerida');
    expect(fixture.nativeElement.textContent).toContain('No informado');
  });

  it('muestra información de validación de asistencia devuelta por backend', () => {
    fixture.componentInstance.filtroForm.setValue({ eventoId: evento.id });
    fixture.componentInstance.cargarAsistencias();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('VALIDADO');
    expect(fixture.nativeElement.textContent).toContain('Código');
  });
});
