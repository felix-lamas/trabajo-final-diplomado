export enum Modalidad {
  PRESENCIAL = 'PRESENCIAL',
  VIRTUAL = 'VIRTUAL',
  HIBRIDO = 'HIBRIDO'
}

export enum EstadoEvento {
  BORRADOR = 'BORRADOR',
  PUBLICADO = 'PUBLICADO',
  INSCRIPCIONES_CERRADAS = 'INSCRIPCIONES_CERRADAS',
  EN_CURSO = 'EN_CURSO',
  FINALIZADO = 'FINALIZADO',
  CANCELADO = 'CANCELADO'
}

export enum TipoInscripcion {
  GRATUITO = 'GRATUITO',
  PAGO = 'PAGO'
}

export interface Evento {
  id: string;
  titulo: string;
  descripcion: string;
  objetivos: string;
  categoriaId: string;
  categoriaNombre: string;
  modalidad: Modalidad;
  tipoInscripcion: TipoInscripcion;
  costo: number;
  fechaInicio: Date;
  fechaFin: Date;
  horaInicio: string;
  horaFin: string;
  ubicacion?: string;
  enlaceVirtual?: string;
  cupoMaximo: number;
  cupoDisponible: number;
  estado: EstadoEvento;
  imagenPortada?: string;
  organizadorId: string;
  organizadorNombre: string;
}

export interface CrearEventoRequest {
  titulo: string;
  descripcion?: string;
  objetivos?: string;
  categoriaId: string;
  modalidad: Modalidad;
  tipoInscripcion: TipoInscripcion;
  costo: number;
  fechaInicio: string;
  fechaFin: string;
  horaInicio?: string;
  horaFin?: string;
  ubicacion?: string;
  enlaceVirtual?: string;
  cupoMaximo: number;
  imagenPortada?: string;
}
