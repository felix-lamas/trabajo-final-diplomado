export interface DashboardEjecutivoResponse {
  totalEventos: number;
  totalUsuarios: number;
  totalParticipantes: number;
  totalInscripciones: number;
  totalCertificados: number;
  ingresosGenerados: number;
  nivelSatisfaccion: number;
  participacionEncuestas: number;
  promedioSatisfaccionPorEvento: Array<{ eventoId: string; evento: string; promedio: number }>;
}

export interface DashboardAcademicoResponse {
  participacionPorFacultad: Array<{ name: string; value: number }>;
  participacionPorCarrera: Array<{ name: string; value: number }>;
  participacionPorCategoria: Array<{ name: string; value: number }>;
  participacionPorPeriodo: Array<{ periodo: string; inscritos: number }>;
}

export interface DashboardOperativoResponse {
  eventosActivos: number;
  eventosFinalizados: number;
  pagosPendientes: number;
  pagosValidados: number;
  qrUtilizados: number;
  asistenciasRegistradas: number;
}

export interface ReporteDataResponse {
  tipoReporte: string;
  totalRegistros: number;
  filas: Array<Record<string, any>>;
}
