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

export enum TipoCertificadoEvento {
  CURRICULAR = 'CURRICULAR',
  NO_CURRICULAR = 'NO_CURRICULAR'
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
  costo: number | null;
  fechaInicio: string;
  fechaFin: string;
  horaInicio: string;
  horaFin: string;
  ubicacion?: string | null;
  direccion?: string | null;
  latitud?: number | null;
  longitud?: number | null;
  radioMetros?: number | null;
  enlaceVirtual?: string | null;
  requiereInscripcion: boolean;
  cupoLimitado: boolean;
  cupoMaximo: number | null;
  cupoDisponible: number | null;
  emiteCertificado: boolean;
  tipoCertificado?: TipoCertificadoEvento | null;
  horasAcademicas?: number | null;
  publicoObjetivo: PublicoObjetivo;
  estado: EstadoEvento;
  imagenPortada?: string | null;
  telefonoContacto?: string | null;
  emailContacto?: string | null;
  whatsappContacto?: string | null;
  qrPagoUrl?: string | null;
  instruccionesPago?: string | null;
  motivoRechazo?: string | null;
  motivoCancelacion?: string | null;
  organizadorId: string;
  organizadorNombre: string;
}

/** Respuesta de GET /eventos/{id}; amplía el resumen con datos del detalle. */
export type EventoDetalle = Evento;

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
  latitud: number | null;
  longitud: number | null;
  radioMetros: number | null;
  enlaceVirtual?: string;
  requiereInscripcion: boolean;
  cupoLimitado: boolean;
  cupoMaximo: number | null;
  emiteCertificado: boolean;
  tipoCertificado: TipoCertificadoEvento | null;
  horasAcademicas: number | null;
  publicoObjetivo: PublicoObjetivo;
  imagenPortada?: string;
  telefonoContacto?: string;
  emailContacto?: string;
  whatsappContacto?: string;
  instruccionesPago?: string;
}
