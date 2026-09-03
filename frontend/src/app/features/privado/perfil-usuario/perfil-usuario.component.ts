import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-perfil-usuario',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './perfil-usuario.component.html'
})
export class PerfilUsuarioComponent {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  get user() {
    return this.authService.getUser();
  }

  get fullName(): string {
    return this.user ? `${this.user.nombres} ${this.user.apellidos}`.trim() : 'Usuario';
  }

  get email(): string {
    return this.user?.correoElectronico || 'sin-correo@uajms.edu.bo';
  }

  get roles(): string[] {
    return this.authService.getRoles();
  }

  get initial(): string {
    return this.fullName.charAt(0).toUpperCase();
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
