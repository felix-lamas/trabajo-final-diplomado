export interface SesionEvento {
  id: string;
  eventoId: string;
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
  historica: boolean;
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
