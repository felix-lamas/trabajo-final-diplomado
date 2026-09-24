export enum Modalidad {
  PRESENCIAL = 'PRESENCIAL',
  VIRTUAL = 'VIRTUAL'
}

export enum EstadoEvento {
  BORRADOR = 'BORRADOR',
  EN_REVISION = 'EN_REVISION',
  PUBLICADO = 'PUBLICADO',
  FINALIZADO = 'FINALIZADO',
  RECHAZADO = 'RECHAZADO',
  CANCELADO = 'CANCELADO'
}

export enum TipoInscripcion {
  GRATUITO = 'GRATUITO',
  PAGO = 'PAGO'
}

export enum PublicoObjetivo {
  UAJMS = 'UAJMS',
  EXTERNO = 'EXTERNO',
  AMBOS = 'AMBOS'
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
  fechaInicio: string;
  fechaFin: string;
  horaInicio: string;
  horaFin: string;
  ubicacion?: string;
  enlaceVirtual?: string;
  requiereInscripcion: boolean;
  cupoLimitado: boolean;
  cupoMaximo: number | null;
  cupoDisponible: number | null;
  emiteCertificado: boolean;
  publicoObjetivo: PublicoObjetivo;
  estado: EstadoEvento;
  imagenPortada?: string;
  organizadorId: string;
  organizadorNombre: string;
}

export interface CrearEventoRequest {
  titulo: string;
  descripcion: string;
  objetivos: string;
  categoriaId: string;
  modalidad: Modalidad;
  tipoInscripcion: TipoInscripcion;
  costo: number | null;
  fechaInicio: string;
  fechaFin: string;
  horaInicio: string;
  horaFin: string;
  ubicacion?: string;
  direccion?: string;
  enlaceVirtual?: string;
  requiereInscripcion: boolean;
  cupoLimitado: boolean;
  cupoMaximo: number | null;
  emiteCertificado: boolean;
  publicoObjetivo: PublicoObjetivo;
  imagenPortada?: string;
}
