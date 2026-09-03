export interface CategoriaEvento {
  id: string;
  nombre: string;
  descripcion?: string;
  estado: 'ACTIVO' | 'INACTIVO';
}

export interface CrearCategoriaEventoRequest {
  nombre: string;
  descripcion?: string;
}

export interface ActualizarCategoriaEventoRequest {
  nombre: string;
  descripcion?: string;
  estado: string;
}
