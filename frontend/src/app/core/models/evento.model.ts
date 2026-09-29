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
  EXTERNA = 'EXTERNA',
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
  costo: number;
  fechaInicio: string;
  fechaFin: string;
  horaInicio: string;
  horaFin: string;
  ubicacion?: string;
  direccion?: string;
  latitud?: number;
  longitud?: number;
  radioMetros?: number;
  enlaceVirtual?: string;
  requiereInscripcion: boolean;
  cupoLimitado: boolean;
  cupoMaximo: number | null;
  cupoDisponible: number | null;
  emiteCertificado: boolean;
  tipoCertificado?: TipoCertificadoEvento;
  horasAcademicas?: number;
  publicoObjetivo: PublicoObjetivo;
  estado: EstadoEvento;
  imagenPortada?: string;
  telefonoContacto?: string;
  emailContacto?: string;
  whatsappContacto?: string;
  qrPagoUrl?: string;
  instruccionesPago?: string;
  motivoRechazo?: string;
  motivoCancelacion?: string;
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
  qrPagoUrl?: string;
  instruccionesPago?: string;
}
