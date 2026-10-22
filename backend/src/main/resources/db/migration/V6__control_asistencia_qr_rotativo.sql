ALTER TABLE sesiones_evento
    ADD COLUMN IF NOT EXISTS control_asistencia_activo BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE qr_asistencia
    ADD COLUMN IF NOT EXISTS token_cifrado VARCHAR(512);
