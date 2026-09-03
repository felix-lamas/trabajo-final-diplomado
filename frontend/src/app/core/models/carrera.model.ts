export interface Carrera {
  id: string;
  nombre: string;
  descripcion?: string;
  estado: 'ACTIVO' | 'INACTIVO';
  facultadId: string;
  facultadNombre?: string;
  fechaCreacion?: Date;
}

export interface CrearCarreraRequest {
  nombre: string;
  descripcion?: string;
  facultadId: string;
}

export interface ActualizarCarreraRequest {
  nombre: string;
  descripcion?: string;
  estado: string;
  facultadId: string;
}
