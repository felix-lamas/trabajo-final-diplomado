export interface SesionEvento {
  id: string;
  eventoId: string;
  nombre: string;
  descripcion?: string | null;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  requiereAsistencia: boolean | null;
  latitud: number | null;
  longitud: number | null;
  radioMetros: number | null;
  activa: boolean | null;
  historica: boolean | null;
}

export interface SesionEventoRequest {
  nombre: string;
  descripcion?: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  requiereAsistencia: boolean;
  latitud: number | null;
  longitud: number | null;
  radioMetros: number;
  activa: boolean;
}

export interface QrAsistencia {
  id: string;
  sesionId: string;
  token: string | null;
  emitidoEn: string;
  expiraEn: string;
  activo: boolean;
  revocadoEn: string | null;
}

/** Respuesta del endpoint GET /api/v1/asistencias/evento/{id}. */
export interface AsistenciaResponse {
  id: string;
  nombreParticipante: string;
  documentoIdentidad: string;
  codigoParticipante: string;
  evento: string;
  sesionEventoId: string;
  sesion: string;
  fechaHoraRegistro: string;
  registradoPor: string | null;
  distanciaMetros: number | null;
  precisionGpsMetros: number | null;
  resultadoValidacion: string;
  observacion: string | null;
}
