-- Los registros anteriores conservan campos nulos y tipos vacios, sin motivos inventados.
ALTER TABLE usuario ADD COLUMN motivo_solicitud_organizador varchar(1000);
ALTER TABLE usuario ADD COLUMN informacion_adicional_organizador varchar(1000);
CREATE TABLE usuario_solicitud_tipos_evento (
    usuario_id uuid NOT NULL REFERENCES usuario(id),
    tipo_evento varchar(40) NOT NULL CHECK (tipo_evento IN (
        'CONFERENCIAS_CHARLAS', 'CURSOS_TALLERES', 'DIPLOMADOS_CAPACITACION',
        'ACTIVIDADES_CULTURALES', 'ACTIVIDADES_DEPORTIVAS', 'OTROS_EVENTOS_UNIVERSITARIOS')),
    PRIMARY KEY (usuario_id, tipo_evento)
);
-- Snapshot del intento resuelto antes de volver a solicitar; no sobrescribe el historial.
CREATE TABLE solicitud_organizador_historial (
    id uuid PRIMARY KEY,
    usuario_id uuid NOT NULL REFERENCES usuario(id),
    estado varchar(20) NOT NULL CHECK (estado = 'RECHAZADA'),
    detalle jsonb NOT NULL
);
CREATE INDEX idx_solicitud_organizador_historial_estado ON solicitud_organizador_historial(estado);
