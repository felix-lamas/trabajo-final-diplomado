-- Additive migration. Existing accounts remain active; no roles or seed accounts are changed.
ALTER TABLE public.usuario ADD COLUMN IF NOT EXISTS activo boolean NOT NULL DEFAULT true;
CREATE TABLE public.invitacion_administrador (
    id uuid PRIMARY KEY,
    fecha_creacion timestamp(6), fecha_actualizacion timestamp(6), fecha_eliminacion timestamp(6),
    creado_por varchar(255), actualizado_por varchar(255),
    correo varchar(100) NOT NULL,
    token_hash varchar(64) NOT NULL UNIQUE,
    nombres varchar(50) NOT NULL, apellidos varchar(50) NOT NULL,
    ci varchar(20) NOT NULL, celular varchar(20) NOT NULL,
    estado varchar(20) NOT NULL CHECK (estado IN ('PENDIENTE','ACEPTADA','EXPIRADA','REVOCADA')),
    fecha_expiracion timestamp(6) NOT NULL, fecha_ultimo_envio timestamp(6) NOT NULL,
    fecha_aceptacion timestamp(6),
    invitante_id uuid NOT NULL REFERENCES public.usuario(id),
    usuario_id uuid REFERENCES public.usuario(id)
);
CREATE UNIQUE INDEX uk_invitacion_admin_correo_pendiente ON public.invitacion_administrador (lower(correo)) WHERE estado = 'PENDIENTE';
CREATE UNIQUE INDEX uk_invitacion_admin_ci_pendiente ON public.invitacion_administrador (ci) WHERE estado = 'PENDIENTE';
