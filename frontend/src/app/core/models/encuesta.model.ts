export interface ResponderEncuestaRequest {
  eventoId: string;
  calificacion: number;
  comentario?: string | null;
}

export interface EncuestaResponse {
  id: string;
  eventoId: string;
  eventoTitulo: string;
  usuarioId: string;
  participante: string;
  calificacion: number;
  comentario?: string | null;
  fechaRespuesta: string;
}

export interface ComentarioEncuestaResponse {
  participante: string;
  comentario: string;
  fechaRespuesta: string;
}

export interface EstadisticasEncuestaResponse {
  eventoId: string;
  eventoTitulo: string;
  calificacionPromedio: number;
  totalAsistentes: number;
  totalRespuestas: number;
  participacion: number;
  comentarios: ComentarioEncuestaResponse[];
}
