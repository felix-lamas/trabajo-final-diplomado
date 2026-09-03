export interface Facultad {
  id: string;
  nombre: string;
  descripcion?: string;
  estado: 'ACTIVO' | 'INACTIVO';
  cantidadCarreras?: number;
  fechaCreacion?: Date;
}

export interface CrearFacultadRequest {
  nombre: string;
  descripcion?: string;
}

export interface ActualizarFacultadRequest {
  nombre: string;
  descripcion?: string;
  estado: string;
}
