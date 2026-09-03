import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { AuthService, RegistroUsuarioRequest } from '../../../core/services/auth.service';
import { FacultadService } from '../../../core/services/facultad.service';
import { CarreraService } from '../../../core/services/carrera.service';
import { Facultad } from '../../../core/models/facultad.model';
import { Carrera } from '../../../core/models/carrera.model';

@Component({
  selector: 'app-registro',
  templateUrl: './registro.component.html',
  standalone: false
})
export class RegistroComponent implements OnInit, OnDestroy {
  form: FormGroup;
  currentStep = 1;
  loading = false;
  facultades: Facultad[] = [];
  carreras: Carrera[] = [];
  private readonly destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private facultadService: FacultadService,
    private carreraService: CarreraService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      nombres: ['', [Validators.required, Validators.minLength(2)]],
      apellidos: ['', [Validators.required, Validators.minLength(2)]],
      ci: ['', [Validators.required, Validators.minLength(5)]],
      celular: ['', [Validators.required, Validators.minLength(7)]],
      tipoUsuario: ['INTERNO', [Validators.required]],
      facultadId: [null],
      carreraId: [null],
      correoElectronico: ['', [Validators.required, Validators.email]],
      contrasena: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/)
      ]],
      confirmacion: ['', [Validators.required]]
    }, { validators: this.checkPasswords });
  }

  ngOnInit(): void {
    this.cargarFacultades();
    this.syncAcademicValidators();

    this.form.get('tipoUsuario')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.syncAcademicValidators());

    this.form.get('facultadId')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((facultadId) => {
        if (this.isInterno() && facultadId) {
          this.cargarCarreras(facultadId);
        } else {
          this.carreras = [];
          this.form.get('carreraId')?.reset(null, { emitEvent: false });
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  isInterno(): boolean {
    return this.form.get('tipoUsuario')?.value === 'INTERNO';
  }

  nextStep(): void {
    if (!this.canAdvance()) {
      this.markStepTouched();
      return;
    }

    this.currentStep = Math.min(3, this.currentStep + 1);
  }

  previousStep(): void {
    this.currentStep = Math.max(1, this.currentStep - 1);
  }

  goToStep(step: number): void {
    if (step < this.currentStep) {
      this.currentStep = step;
      return;
    }

    if (step === this.currentStep + 1) {
      this.nextStep();
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    const value = this.form.getRawValue();
    const request: RegistroUsuarioRequest = {
      correoElectronico: value.correoElectronico,
      contrasena: value.contrasena,
      nombres: value.nombres,
      apellidos: value.apellidos,
      ci: value.ci,
      celular: value.celular,
      tipoUsuario: value.tipoUsuario,
      carreraId: this.isInterno() ? value.carreraId ?? undefined : undefined
    };

    this.authService.registro(request).subscribe({
      next: () => {
        this.loading = false;
        this.snackBar.open('Registro completado correctamente', 'Cerrar', { duration: 3500 });
        this.router.navigate(['/privado/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.mensaje || 'No fue posible completar el registro', 'Cerrar', { duration: 4500 });
      }
    });
  }

  get passwordStrength(): number {
    const password = String(this.form.get('contrasena')?.value || '');
    let score = 0;

    if (password.length >= 8) score += 25;
    if (/[a-z]/.test(password)) score += 20;
    if (/[A-Z]/.test(password)) score += 20;
    if (/\d/.test(password)) score += 20;
    if (/[^A-Za-z0-9]/.test(password)) score += 15;

    return score;
  }

  get passwordLabel(): string {
    const score = this.passwordStrength;
    if (score < 40) return 'Debil';
    if (score < 70) return 'Aceptable';
    return 'Fuerte';
  }

  get passwordTone(): 'danger' | 'warning' | 'success' {
    const score = this.passwordStrength;
    if (score < 40) return 'danger';
    if (score < 70) return 'warning';
    return 'success';
  }

  get progressPercent(): number {
    return (this.currentStep / 3) * 100;
  }

  private canAdvance(): boolean {
    if (this.currentStep === 1) {
      return ['nombres', 'apellidos', 'ci', 'celular'].every((control) => this.form.get(control)?.valid);
    }

    if (this.currentStep === 2) {
      if (!this.isInterno()) {
        return true;
      }

      return ['facultadId', 'carreraId'].every((control) => this.form.get(control)?.valid);
    }

    return true;
  }

  private markStepTouched(): void {
    if (this.currentStep === 1) {
      ['nombres', 'apellidos', 'ci', 'celular'].forEach((control) => this.form.get(control)?.markAsTouched());
      return;
    }

    if (this.currentStep === 2 && this.isInterno()) {
      ['facultadId', 'carreraId'].forEach((control) => this.form.get(control)?.markAsTouched());
    }
  }

  private cargarFacultades(): void {
    this.facultadService.listar().subscribe({
      next: (data) => this.facultades = data.filter((facultad) => facultad.estado === 'ACTIVO'),
      error: () => this.snackBar.open('No fue posible cargar las facultades', 'Cerrar', { duration: 3000 })
    });
  }

  private cargarCarreras(facultadId: string): void {
    this.carreraService.listar().subscribe({
      next: (data) => {
        this.carreras = data.filter((carrera) => carrera.estado === 'ACTIVO' && carrera.facultadId === facultadId);
        if (this.carreras.length === 0) {
          this.form.get('carreraId')?.reset(null, { emitEvent: false });
        }
      },
      error: () => this.snackBar.open('No fue posible cargar las carreras', 'Cerrar', { duration: 3000 })
    });
  }

  syncAcademicValidators(): void {
    const tipoUsuario = this.form.get('tipoUsuario')?.value;
    const facultadCtrl = this.form.get('facultadId');
    const carreraCtrl = this.form.get('carreraId');

    if (tipoUsuario === 'INTERNO') {
      facultadCtrl?.setValidators([Validators.required]);
      carreraCtrl?.setValidators([Validators.required]);
      facultadCtrl?.updateValueAndValidity({ emitEvent: false });
      carreraCtrl?.updateValueAndValidity({ emitEvent: false });
      return;
    }

    facultadCtrl?.clearValidators();
    carreraCtrl?.clearValidators();
    facultadCtrl?.reset(null, { emitEvent: false });
    carreraCtrl?.reset(null, { emitEvent: false });
    facultadCtrl?.updateValueAndValidity({ emitEvent: false });
    carreraCtrl?.updateValueAndValidity({ emitEvent: false });
    this.carreras = [];
  }

  private checkPasswords(group: FormGroup) {
    const pass = group.get('contrasena')?.value;
    const confirmPass = group.get('confirmacion')?.value;
    return pass === confirmPass ? null : { notSame: true };
  }
}
