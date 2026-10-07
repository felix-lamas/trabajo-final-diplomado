-- Reconciles legacy schemas with the current JPA model without deleting business data.
-- Existing rows receive only explicitly defined defaults; ambiguous historic values remain NULL.

CREATE TABLE IF NOT EXISTS public.asistencias
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    distancia_metros numeric(10, 2),
    fecha_hora_registro timestamp(6) without time zone NOT NULL,
    observacion text COLLATE pg_catalog."default",
    precision_gps_metros numeric(10, 2),
    resultado_validacion character varying(20) COLLATE pg_catalog."default" NOT NULL,
    inscripcion_id uuid NOT NULL,
    registrado_por_id uuid,
    sesion_evento_id uuid NOT NULL,
    CONSTRAINT asistencias_pkey PRIMARY KEY (id),
    CONSTRAINT uk_asistencia_inscripcion_sesion UNIQUE (inscripcion_id, sesion_evento_id)
);

CREATE TABLE IF NOT EXISTS public.categorias_evento
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    descripcion text COLLATE pg_catalog."default",
    estado character varying(255) COLLATE pg_catalog."default" NOT NULL,
    nombre character varying(100) COLLATE pg_catalog."default" NOT NULL,
    nombre_normalizado character varying(100) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT categorias_evento_pkey PRIMARY KEY (id),
    CONSTRAINT uk9dcouuesv8ant9k1jaiaihdu4 UNIQUE (nombre),
    CONSTRAINT ukobteg7chcc396i7hljq0xx152 UNIQUE (nombre_normalizado)
);

CREATE TABLE IF NOT EXISTS public.certificados
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    archivo_pdf_url character varying(255) COLLATE pg_catalog."default",
    codigo_certificado character varying(50) COLLATE pg_catalog."default" NOT NULL,
    estado character varying(20) COLLATE pg_catalog."default" NOT NULL,
    fecha_emision timestamp(6) without time zone NOT NULL,
    horas_academicas integer,
    porcentaje_asistencia numeric(5, 2) NOT NULL,
    tipo_certificado character varying(20) COLLATE pg_catalog."default" NOT NULL,
    url_verificacion character varying(255) COLLATE pg_catalog."default" NOT NULL,
    evento_id uuid NOT NULL,
    inscripcion_id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    CONSTRAINT certificados_pkey PRIMARY KEY (id),
    CONSTRAINT uk_certificado_codigo UNIQUE (codigo_certificado),
    CONSTRAINT uk_certificado_inscripcion UNIQUE (inscripcion_id)
);

CREATE TABLE IF NOT EXISTS public.eventos
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    costo numeric(12, 2) NOT NULL,
    cupo_disponible integer,
    cupo_limitado boolean NOT NULL,
    cupo_maximo integer,
    descripcion text COLLATE pg_catalog."default",
    direccion character varying(500) COLLATE pg_catalog."default",
    email_contacto character varying(100) COLLATE pg_catalog."default",
    emite_certificado boolean NOT NULL,
    enlace_virtual character varying(500) COLLATE pg_catalog."default",
    estado character varying(30) COLLATE pg_catalog."default" NOT NULL,
    fecha_envio_revision timestamp(6) without time zone,
    fecha_fin date NOT NULL,
    fecha_inicio date NOT NULL,
    fecha_resolucion timestamp(6) without time zone,
    hora_fin time(6) without time zone,
    hora_inicio time(6) without time zone,
    horas_academicas integer,
    imagen_portada character varying(500) COLLATE pg_catalog."default",
    instrucciones_pago text COLLATE pg_catalog."default",
    latitud numeric(10, 7),
    longitud numeric(10, 7),
    modalidad character varying(20) COLLATE pg_catalog."default" NOT NULL,
    motivo_cancelacion character varying(1000) COLLATE pg_catalog."default",
    motivo_rechazo character varying(1000) COLLATE pg_catalog."default",
    objetivos text COLLATE pg_catalog."default",
    publico_objetivo character varying(20) COLLATE pg_catalog."default" NOT NULL,
    qr_pago_url character varying(500) COLLATE pg_catalog."default",
    radio_metros integer,
    requiere_inscripcion boolean NOT NULL,
    telefono_contacto character varying(20) COLLATE pg_catalog."default",
    tipo_certificado character varying(20) COLLATE pg_catalog."default",
    tipo_inscripcion character varying(20) COLLATE pg_catalog."default" NOT NULL,
    titulo character varying(200) COLLATE pg_catalog."default" NOT NULL,
    ubicacion character varying(255) COLLATE pg_catalog."default",
    whatsapp_contacto character varying(20) COLLATE pg_catalog."default",
    categoria_id uuid NOT NULL,
    organizador_id uuid NOT NULL,
    resuelto_por_id uuid,
    qr_pago_storage_key character varying(500) COLLATE pg_catalog."default",
    CONSTRAINT eventos_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.inscripciones
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    codigo_participante character varying(30) COLLATE pg_catalog."default",
    estado character varying(30) COLLATE pg_catalog."default" NOT NULL,
    fecha_inscripcion timestamp(6) without time zone NOT NULL,
    observacion text COLLATE pg_catalog."default",
    evento_id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    CONSTRAINT inscripciones_pkey PRIMARY KEY (id),
    CONSTRAINT uk_inscripcion_usuario_evento UNIQUE (usuario_id, evento_id),
    CONSTRAINT ukmey22k67agj6dplhkmixk9uf3 UNIQUE (codigo_participante)
);

CREATE TABLE IF NOT EXISTS public.pagos
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    comprobante_nombre_archivo character varying(255) COLLATE pg_catalog."default",
    comprobante_tipo_contenido character varying(100) COLLATE pg_catalog."default",
    comprobante_url character varying(500) COLLATE pg_catalog."default",
    estado character varying(20) COLLATE pg_catalog."default" NOT NULL,
    fecha_carga_comprobante timestamp(6) without time zone,
    fecha_pago timestamp(6) without time zone NOT NULL,
    fecha_resolucion timestamp(6) without time zone,
    intentos_comprobante integer NOT NULL,
    monto numeric(12, 2) NOT NULL,
    motivo_rechazo character varying(1000) COLLATE pg_catalog."default",
    observacion text COLLATE pg_catalog."default",
    inscripcion_id uuid NOT NULL,
    resuelto_por_id uuid,
    CONSTRAINT pagos_pkey PRIMARY KEY (id),
    CONSTRAINT ukcmeveyyrpuai5j5frvbigmkd4 UNIQUE (inscripcion_id)
);

CREATE TABLE IF NOT EXISTS public.permiso
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    descripcion character varying(100) COLLATE pg_catalog."default",
    nombre character varying(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT permiso_pkey PRIMARY KEY (id),
    CONSTRAINT uknwe6lkk7x7sbw94xcmbwgvycu UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS public.qr_asistencia
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    activo boolean NOT NULL,
    emitido_en timestamp(6) without time zone NOT NULL,
    expira_en timestamp(6) without time zone NOT NULL,
    revocado_en timestamp(6) without time zone,
    token_hash character varying(64) COLLATE pg_catalog."default" NOT NULL,
    version bigint,
    generado_por_id uuid NOT NULL,
    sesion_evento_id uuid NOT NULL,
    CONSTRAINT qr_asistencia_pkey PRIMARY KEY (id),
    CONSTRAINT uk6s2onaib0e7w9xlsb1s9hdlhc UNIQUE (token_hash)
);

CREATE TABLE IF NOT EXISTS public.rol
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    descripcion character varying(100) COLLATE pg_catalog."default",
    nombre character varying(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT rol_pkey PRIMARY KEY (id),
    CONSTRAINT uk43kr6s7bts1wqfv43f7jd87kp UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS public.rol_permiso
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    permiso_id uuid NOT NULL,
    rol_id uuid NOT NULL,
    CONSTRAINT rol_permiso_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.sesion_usuario
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    fecha_expiracion timestamp(6) without time zone NOT NULL,
    fecha_revocacion timestamp(6) without time zone,
    usuario_id uuid NOT NULL,
    CONSTRAINT sesion_usuario_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.sesiones_evento
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    activa boolean NOT NULL,
    descripcion text COLLATE pg_catalog."default",
    fecha date NOT NULL,
    historica boolean NOT NULL,
    hora_fin time(6) without time zone NOT NULL,
    hora_inicio time(6) without time zone NOT NULL,
    latitud numeric(10, 7),
    longitud numeric(10, 7),
    nombre character varying(150) COLLATE pg_catalog."default" NOT NULL,
    radio_metros integer,
    requiere_asistencia boolean NOT NULL,
    evento_id uuid NOT NULL,
    CONSTRAINT sesiones_evento_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.token_recuperacion
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    fecha_expiracion timestamp(6) without time zone NOT NULL,
    token character varying(255) COLLATE pg_catalog."default" NOT NULL,
    utilizado boolean NOT NULL,
    usuario_id uuid NOT NULL,
    CONSTRAINT token_recuperacion_pkey PRIMARY KEY (id),
    CONSTRAINT uk33lrkaqshli5r4svsvil7yfc0 UNIQUE (token)
);

CREATE TABLE IF NOT EXISTS public.token_verificacion_correo
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    fecha_expiracion timestamp(6) without time zone NOT NULL,
    token_hash character varying(64) COLLATE pg_catalog."default" NOT NULL,
    utilizado boolean NOT NULL,
    usuario_id uuid NOT NULL,
    CONSTRAINT token_verificacion_correo_pkey PRIMARY KEY (id),
    CONSTRAINT uka184aisgc1eaapsoky0n6vdu3 UNIQUE (token_hash)
);

CREATE TABLE IF NOT EXISTS public.usuario
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    apellidos character varying(50) COLLATE pg_catalog."default" NOT NULL,
    celular character varying(20) COLLATE pg_catalog."default",
    ci character varying(20) COLLATE pg_catalog."default" NOT NULL,
    contrasena character varying(255) COLLATE pg_catalog."default" NOT NULL,
    correo_electronico character varying(100) COLLATE pg_catalog."default" NOT NULL,
    correo_verificado boolean NOT NULL,
    estado_solicitud_organizador character varying(20) COLLATE pg_catalog."default" NOT NULL,
    fecha_carga_fotografia timestamp(6) without time zone,
    fecha_resolucion_organizador timestamp(6) without time zone,
    fecha_solicitud_organizador timestamp(6) without time zone,
    fotografia_url character varying(255) COLLATE pg_catalog."default",
    motivo_rechazo_organizador character varying(500) COLLATE pg_catalog."default",
    nombre_archivo_fotografia character varying(100) COLLATE pg_catalog."default",
    nombres character varying(50) COLLATE pg_catalog."default" NOT NULL,
    ru character varying(20) COLLATE pg_catalog."default",
    tipo_usuario character varying(20) COLLATE pg_catalog."default" NOT NULL,
    solicitud_resuelta_por_id uuid,
    CONSTRAINT usuario_pkey PRIMARY KEY (id),
    CONSTRAINT ukdf45etfnvb4mcw8j04h8h93vd UNIQUE (ci),
    CONSTRAINT ukf7w2jekriedf7k6a4kaclt9t7 UNIQUE (correo_electronico),
    CONSTRAINT ukq8gbcdu6kstk7m1fs1cqxc0od UNIQUE (ru)
);

CREATE TABLE IF NOT EXISTS public.usuario_rol
(
    id uuid NOT NULL,
    actualizado_por character varying(255) COLLATE pg_catalog."default",
    creado_por character varying(255) COLLATE pg_catalog."default",
    fecha_actualizacion timestamp(6) without time zone,
    fecha_creacion timestamp(6) without time zone,
    fecha_eliminacion timestamp(6) without time zone,
    rol_id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    CONSTRAINT usuario_rol_pkey PRIMARY KEY (id)
);

-- Add any column missing from an existing table. New columns stay nullable unless a
-- deterministic model default is defined below; existing values are never overwritten.
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='id') THEN ALTER TABLE public.asistencias ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='actualizado_por') THEN ALTER TABLE public.asistencias ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='creado_por') THEN ALTER TABLE public.asistencias ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.asistencias ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='fecha_creacion') THEN ALTER TABLE public.asistencias ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.asistencias ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='distancia_metros') THEN ALTER TABLE public.asistencias ADD COLUMN distancia_metros NUMERIC(10,2); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='fecha_hora_registro') THEN ALTER TABLE public.asistencias ADD COLUMN fecha_hora_registro TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='observacion') THEN ALTER TABLE public.asistencias ADD COLUMN observacion TEXT; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='precision_gps_metros') THEN ALTER TABLE public.asistencias ADD COLUMN precision_gps_metros NUMERIC(10,2); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='resultado_validacion') THEN ALTER TABLE public.asistencias ADD COLUMN resultado_validacion VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='inscripcion_id') THEN ALTER TABLE public.asistencias ADD COLUMN inscripcion_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='registrado_por_id') THEN ALTER TABLE public.asistencias ADD COLUMN registrado_por_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='sesion_evento_id') THEN ALTER TABLE public.asistencias ADD COLUMN sesion_evento_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='id') THEN ALTER TABLE public.categorias_evento ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='actualizado_por') THEN ALTER TABLE public.categorias_evento ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='creado_por') THEN ALTER TABLE public.categorias_evento ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.categorias_evento ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='fecha_creacion') THEN ALTER TABLE public.categorias_evento ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.categorias_evento ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='descripcion') THEN ALTER TABLE public.categorias_evento ADD COLUMN descripcion TEXT; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='estado') THEN ALTER TABLE public.categorias_evento ADD COLUMN estado VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='nombre') THEN ALTER TABLE public.categorias_evento ADD COLUMN nombre VARCHAR(100); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='categorias_evento' AND column_name='nombre_normalizado') THEN ALTER TABLE public.categorias_evento ADD COLUMN nombre_normalizado VARCHAR(100); END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='id') THEN ALTER TABLE public.certificados ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='actualizado_por') THEN ALTER TABLE public.certificados ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='creado_por') THEN ALTER TABLE public.certificados ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.certificados ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='fecha_creacion') THEN ALTER TABLE public.certificados ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.certificados ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='archivo_pdf_url') THEN ALTER TABLE public.certificados ADD COLUMN archivo_pdf_url VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='codigo_certificado') THEN ALTER TABLE public.certificados ADD COLUMN codigo_certificado VARCHAR(50); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='estado') THEN ALTER TABLE public.certificados ADD COLUMN estado VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='fecha_emision') THEN ALTER TABLE public.certificados ADD COLUMN fecha_emision TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='horas_academicas') THEN ALTER TABLE public.certificados ADD COLUMN horas_academicas INTEGER; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='porcentaje_asistencia') THEN ALTER TABLE public.certificados ADD COLUMN porcentaje_asistencia NUMERIC(5,2); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='tipo_certificado') THEN ALTER TABLE public.certificados ADD COLUMN tipo_certificado VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='url_verificacion') THEN ALTER TABLE public.certificados ADD COLUMN url_verificacion VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='evento_id') THEN ALTER TABLE public.certificados ADD COLUMN evento_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='inscripcion_id') THEN ALTER TABLE public.certificados ADD COLUMN inscripcion_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='certificados' AND column_name='usuario_id') THEN ALTER TABLE public.certificados ADD COLUMN usuario_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='id') THEN ALTER TABLE public.eventos ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='actualizado_por') THEN ALTER TABLE public.eventos ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='creado_por') THEN ALTER TABLE public.eventos ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.eventos ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='fecha_creacion') THEN ALTER TABLE public.eventos ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.eventos ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='costo') THEN ALTER TABLE public.eventos ADD COLUMN costo NUMERIC(12,2); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='cupo_disponible') THEN ALTER TABLE public.eventos ADD COLUMN cupo_disponible INTEGER; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='cupo_limitado') THEN ALTER TABLE public.eventos ADD COLUMN cupo_limitado BOOLEAN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='cupo_maximo') THEN ALTER TABLE public.eventos ADD COLUMN cupo_maximo INTEGER; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='descripcion') THEN ALTER TABLE public.eventos ADD COLUMN descripcion TEXT; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='direccion') THEN ALTER TABLE public.eventos ADD COLUMN direccion VARCHAR(500); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='email_contacto') THEN ALTER TABLE public.eventos ADD COLUMN email_contacto VARCHAR(100); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='emite_certificado') THEN ALTER TABLE public.eventos ADD COLUMN emite_certificado BOOLEAN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='enlace_virtual') THEN ALTER TABLE public.eventos ADD COLUMN enlace_virtual VARCHAR(500); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='estado') THEN ALTER TABLE public.eventos ADD COLUMN estado VARCHAR(30); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='fecha_envio_revision') THEN ALTER TABLE public.eventos ADD COLUMN fecha_envio_revision TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='fecha_fin') THEN ALTER TABLE public.eventos ADD COLUMN fecha_fin DATE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='fecha_inicio') THEN ALTER TABLE public.eventos ADD COLUMN fecha_inicio DATE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='fecha_resolucion') THEN ALTER TABLE public.eventos ADD COLUMN fecha_resolucion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='hora_fin') THEN ALTER TABLE public.eventos ADD COLUMN hora_fin TIME WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='hora_inicio') THEN ALTER TABLE public.eventos ADD COLUMN hora_inicio TIME WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='horas_academicas') THEN ALTER TABLE public.eventos ADD COLUMN horas_academicas INTEGER; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='imagen_portada') THEN ALTER TABLE public.eventos ADD COLUMN imagen_portada VARCHAR(500); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='instrucciones_pago') THEN ALTER TABLE public.eventos ADD COLUMN instrucciones_pago TEXT; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='latitud') THEN ALTER TABLE public.eventos ADD COLUMN latitud NUMERIC(10,7); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='longitud') THEN ALTER TABLE public.eventos ADD COLUMN longitud NUMERIC(10,7); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='modalidad') THEN ALTER TABLE public.eventos ADD COLUMN modalidad VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='motivo_cancelacion') THEN ALTER TABLE public.eventos ADD COLUMN motivo_cancelacion VARCHAR(1000); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='motivo_rechazo') THEN ALTER TABLE public.eventos ADD COLUMN motivo_rechazo VARCHAR(1000); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='objetivos') THEN ALTER TABLE public.eventos ADD COLUMN objetivos TEXT; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='publico_objetivo') THEN ALTER TABLE public.eventos ADD COLUMN publico_objetivo VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='qr_pago_url') THEN ALTER TABLE public.eventos ADD COLUMN qr_pago_url VARCHAR(500); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='radio_metros') THEN ALTER TABLE public.eventos ADD COLUMN radio_metros INTEGER; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='requiere_inscripcion') THEN ALTER TABLE public.eventos ADD COLUMN requiere_inscripcion BOOLEAN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='telefono_contacto') THEN ALTER TABLE public.eventos ADD COLUMN telefono_contacto VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='tipo_certificado') THEN ALTER TABLE public.eventos ADD COLUMN tipo_certificado VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='tipo_inscripcion') THEN ALTER TABLE public.eventos ADD COLUMN tipo_inscripcion VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='titulo') THEN ALTER TABLE public.eventos ADD COLUMN titulo VARCHAR(200); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='ubicacion') THEN ALTER TABLE public.eventos ADD COLUMN ubicacion VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='whatsapp_contacto') THEN ALTER TABLE public.eventos ADD COLUMN whatsapp_contacto VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='categoria_id') THEN ALTER TABLE public.eventos ADD COLUMN categoria_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='organizador_id') THEN ALTER TABLE public.eventos ADD COLUMN organizador_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='resuelto_por_id') THEN ALTER TABLE public.eventos ADD COLUMN resuelto_por_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='eventos' AND column_name='qr_pago_storage_key') THEN ALTER TABLE public.eventos ADD COLUMN qr_pago_storage_key VARCHAR(500); END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='id') THEN ALTER TABLE public.inscripciones ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='actualizado_por') THEN ALTER TABLE public.inscripciones ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='creado_por') THEN ALTER TABLE public.inscripciones ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.inscripciones ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='fecha_creacion') THEN ALTER TABLE public.inscripciones ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.inscripciones ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='codigo_participante') THEN ALTER TABLE public.inscripciones ADD COLUMN codigo_participante VARCHAR(30); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='estado') THEN ALTER TABLE public.inscripciones ADD COLUMN estado VARCHAR(30); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='fecha_inscripcion') THEN ALTER TABLE public.inscripciones ADD COLUMN fecha_inscripcion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='observacion') THEN ALTER TABLE public.inscripciones ADD COLUMN observacion TEXT; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='evento_id') THEN ALTER TABLE public.inscripciones ADD COLUMN evento_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='inscripciones' AND column_name='usuario_id') THEN ALTER TABLE public.inscripciones ADD COLUMN usuario_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='id') THEN ALTER TABLE public.pagos ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='actualizado_por') THEN ALTER TABLE public.pagos ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='creado_por') THEN ALTER TABLE public.pagos ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.pagos ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='fecha_creacion') THEN ALTER TABLE public.pagos ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.pagos ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='comprobante_nombre_archivo') THEN ALTER TABLE public.pagos ADD COLUMN comprobante_nombre_archivo VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='comprobante_tipo_contenido') THEN ALTER TABLE public.pagos ADD COLUMN comprobante_tipo_contenido VARCHAR(100); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='comprobante_url') THEN ALTER TABLE public.pagos ADD COLUMN comprobante_url VARCHAR(500); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='estado') THEN ALTER TABLE public.pagos ADD COLUMN estado VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='fecha_carga_comprobante') THEN ALTER TABLE public.pagos ADD COLUMN fecha_carga_comprobante TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='fecha_pago') THEN ALTER TABLE public.pagos ADD COLUMN fecha_pago TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='fecha_resolucion') THEN ALTER TABLE public.pagos ADD COLUMN fecha_resolucion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='intentos_comprobante') THEN ALTER TABLE public.pagos ADD COLUMN intentos_comprobante INTEGER; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='monto') THEN ALTER TABLE public.pagos ADD COLUMN monto NUMERIC(12,2); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='motivo_rechazo') THEN ALTER TABLE public.pagos ADD COLUMN motivo_rechazo VARCHAR(1000); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='observacion') THEN ALTER TABLE public.pagos ADD COLUMN observacion TEXT; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='inscripcion_id') THEN ALTER TABLE public.pagos ADD COLUMN inscripcion_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='pagos' AND column_name='resuelto_por_id') THEN ALTER TABLE public.pagos ADD COLUMN resuelto_por_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='permiso' AND column_name='id') THEN ALTER TABLE public.permiso ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='permiso' AND column_name='actualizado_por') THEN ALTER TABLE public.permiso ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='permiso' AND column_name='creado_por') THEN ALTER TABLE public.permiso ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='permiso' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.permiso ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='permiso' AND column_name='fecha_creacion') THEN ALTER TABLE public.permiso ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='permiso' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.permiso ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='permiso' AND column_name='descripcion') THEN ALTER TABLE public.permiso ADD COLUMN descripcion VARCHAR(100); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='permiso' AND column_name='nombre') THEN ALTER TABLE public.permiso ADD COLUMN nombre VARCHAR(50); END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='id') THEN ALTER TABLE public.qr_asistencia ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='actualizado_por') THEN ALTER TABLE public.qr_asistencia ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='creado_por') THEN ALTER TABLE public.qr_asistencia ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.qr_asistencia ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='fecha_creacion') THEN ALTER TABLE public.qr_asistencia ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.qr_asistencia ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='activo') THEN ALTER TABLE public.qr_asistencia ADD COLUMN activo BOOLEAN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='emitido_en') THEN ALTER TABLE public.qr_asistencia ADD COLUMN emitido_en TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='expira_en') THEN ALTER TABLE public.qr_asistencia ADD COLUMN expira_en TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='revocado_en') THEN ALTER TABLE public.qr_asistencia ADD COLUMN revocado_en TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='token_hash') THEN ALTER TABLE public.qr_asistencia ADD COLUMN token_hash VARCHAR(64); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='version') THEN ALTER TABLE public.qr_asistencia ADD COLUMN version BIGINT; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='generado_por_id') THEN ALTER TABLE public.qr_asistencia ADD COLUMN generado_por_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='qr_asistencia' AND column_name='sesion_evento_id') THEN ALTER TABLE public.qr_asistencia ADD COLUMN sesion_evento_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol' AND column_name='id') THEN ALTER TABLE public.rol ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol' AND column_name='actualizado_por') THEN ALTER TABLE public.rol ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol' AND column_name='creado_por') THEN ALTER TABLE public.rol ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.rol ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol' AND column_name='fecha_creacion') THEN ALTER TABLE public.rol ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.rol ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol' AND column_name='descripcion') THEN ALTER TABLE public.rol ADD COLUMN descripcion VARCHAR(100); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol' AND column_name='nombre') THEN ALTER TABLE public.rol ADD COLUMN nombre VARCHAR(50); END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol_permiso' AND column_name='id') THEN ALTER TABLE public.rol_permiso ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol_permiso' AND column_name='actualizado_por') THEN ALTER TABLE public.rol_permiso ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol_permiso' AND column_name='creado_por') THEN ALTER TABLE public.rol_permiso ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol_permiso' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.rol_permiso ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol_permiso' AND column_name='fecha_creacion') THEN ALTER TABLE public.rol_permiso ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol_permiso' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.rol_permiso ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol_permiso' AND column_name='permiso_id') THEN ALTER TABLE public.rol_permiso ADD COLUMN permiso_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='rol_permiso' AND column_name='rol_id') THEN ALTER TABLE public.rol_permiso ADD COLUMN rol_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesion_usuario' AND column_name='id') THEN ALTER TABLE public.sesion_usuario ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesion_usuario' AND column_name='actualizado_por') THEN ALTER TABLE public.sesion_usuario ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesion_usuario' AND column_name='creado_por') THEN ALTER TABLE public.sesion_usuario ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesion_usuario' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.sesion_usuario ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesion_usuario' AND column_name='fecha_creacion') THEN ALTER TABLE public.sesion_usuario ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesion_usuario' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.sesion_usuario ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesion_usuario' AND column_name='fecha_expiracion') THEN ALTER TABLE public.sesion_usuario ADD COLUMN fecha_expiracion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesion_usuario' AND column_name='fecha_revocacion') THEN ALTER TABLE public.sesion_usuario ADD COLUMN fecha_revocacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesion_usuario' AND column_name='usuario_id') THEN ALTER TABLE public.sesion_usuario ADD COLUMN usuario_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='id') THEN ALTER TABLE public.sesiones_evento ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='actualizado_por') THEN ALTER TABLE public.sesiones_evento ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='creado_por') THEN ALTER TABLE public.sesiones_evento ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.sesiones_evento ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='fecha_creacion') THEN ALTER TABLE public.sesiones_evento ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.sesiones_evento ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='activa') THEN ALTER TABLE public.sesiones_evento ADD COLUMN activa BOOLEAN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='descripcion') THEN ALTER TABLE public.sesiones_evento ADD COLUMN descripcion TEXT; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='fecha') THEN ALTER TABLE public.sesiones_evento ADD COLUMN fecha DATE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='historica') THEN ALTER TABLE public.sesiones_evento ADD COLUMN historica BOOLEAN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='hora_fin') THEN ALTER TABLE public.sesiones_evento ADD COLUMN hora_fin TIME WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='hora_inicio') THEN ALTER TABLE public.sesiones_evento ADD COLUMN hora_inicio TIME WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='latitud') THEN ALTER TABLE public.sesiones_evento ADD COLUMN latitud NUMERIC(10,7); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='longitud') THEN ALTER TABLE public.sesiones_evento ADD COLUMN longitud NUMERIC(10,7); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='nombre') THEN ALTER TABLE public.sesiones_evento ADD COLUMN nombre VARCHAR(150); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='radio_metros') THEN ALTER TABLE public.sesiones_evento ADD COLUMN radio_metros INTEGER; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='requiere_asistencia') THEN ALTER TABLE public.sesiones_evento ADD COLUMN requiere_asistencia BOOLEAN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='sesiones_evento' AND column_name='evento_id') THEN ALTER TABLE public.sesiones_evento ADD COLUMN evento_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='id') THEN ALTER TABLE public.token_recuperacion ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='actualizado_por') THEN ALTER TABLE public.token_recuperacion ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='creado_por') THEN ALTER TABLE public.token_recuperacion ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.token_recuperacion ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='fecha_creacion') THEN ALTER TABLE public.token_recuperacion ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.token_recuperacion ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='fecha_expiracion') THEN ALTER TABLE public.token_recuperacion ADD COLUMN fecha_expiracion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='token') THEN ALTER TABLE public.token_recuperacion ADD COLUMN token VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='utilizado') THEN ALTER TABLE public.token_recuperacion ADD COLUMN utilizado BOOLEAN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_recuperacion' AND column_name='usuario_id') THEN ALTER TABLE public.token_recuperacion ADD COLUMN usuario_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='id') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='actualizado_por') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='creado_por') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='fecha_creacion') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='fecha_expiracion') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN fecha_expiracion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='token_hash') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN token_hash VARCHAR(64); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='utilizado') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN utilizado BOOLEAN; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='token_verificacion_correo' AND column_name='usuario_id') THEN ALTER TABLE public.token_verificacion_correo ADD COLUMN usuario_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='id') THEN ALTER TABLE public.usuario ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='actualizado_por') THEN ALTER TABLE public.usuario ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='creado_por') THEN ALTER TABLE public.usuario ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.usuario ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='fecha_creacion') THEN ALTER TABLE public.usuario ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.usuario ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='apellidos') THEN ALTER TABLE public.usuario ADD COLUMN apellidos VARCHAR(50); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='celular') THEN ALTER TABLE public.usuario ADD COLUMN celular VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='ci') THEN ALTER TABLE public.usuario ADD COLUMN ci VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='contrasena') THEN ALTER TABLE public.usuario ADD COLUMN contrasena VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='correo_electronico') THEN ALTER TABLE public.usuario ADD COLUMN correo_electronico VARCHAR(100); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='correo_verificado') THEN ALTER TABLE public.usuario ADD COLUMN correo_verificado BOOLEAN NOT NULL DEFAULT FALSE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='estado_solicitud_organizador') THEN ALTER TABLE public.usuario ADD COLUMN estado_solicitud_organizador VARCHAR(20) NOT NULL DEFAULT 'NINGUNA'; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='fecha_carga_fotografia') THEN ALTER TABLE public.usuario ADD COLUMN fecha_carga_fotografia TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='fecha_resolucion_organizador') THEN ALTER TABLE public.usuario ADD COLUMN fecha_resolucion_organizador TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='fecha_solicitud_organizador') THEN ALTER TABLE public.usuario ADD COLUMN fecha_solicitud_organizador TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='fotografia_url') THEN ALTER TABLE public.usuario ADD COLUMN fotografia_url VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='motivo_rechazo_organizador') THEN ALTER TABLE public.usuario ADD COLUMN motivo_rechazo_organizador VARCHAR(500); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='nombre_archivo_fotografia') THEN ALTER TABLE public.usuario ADD COLUMN nombre_archivo_fotografia VARCHAR(100); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='nombres') THEN ALTER TABLE public.usuario ADD COLUMN nombres VARCHAR(50); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='ru') THEN ALTER TABLE public.usuario ADD COLUMN ru VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='tipo_usuario') THEN ALTER TABLE public.usuario ADD COLUMN tipo_usuario VARCHAR(20); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario' AND column_name='solicitud_resuelta_por_id') THEN ALTER TABLE public.usuario ADD COLUMN solicitud_resuelta_por_id UUID; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario_rol' AND column_name='id') THEN ALTER TABLE public.usuario_rol ADD COLUMN id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario_rol' AND column_name='actualizado_por') THEN ALTER TABLE public.usuario_rol ADD COLUMN actualizado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario_rol' AND column_name='creado_por') THEN ALTER TABLE public.usuario_rol ADD COLUMN creado_por VARCHAR(255); END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario_rol' AND column_name='fecha_actualizacion') THEN ALTER TABLE public.usuario_rol ADD COLUMN fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario_rol' AND column_name='fecha_creacion') THEN ALTER TABLE public.usuario_rol ADD COLUMN fecha_creacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario_rol' AND column_name='fecha_eliminacion') THEN ALTER TABLE public.usuario_rol ADD COLUMN fecha_eliminacion TIMESTAMP WITHOUT TIME ZONE; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario_rol' AND column_name='rol_id') THEN ALTER TABLE public.usuario_rol ADD COLUMN rol_id UUID; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='usuario_rol' AND column_name='usuario_id') THEN ALTER TABLE public.usuario_rol ADD COLUMN usuario_id UUID; END IF; END $$;

-- Existing nullable columns are normalized only where Java defines deterministic defaults.
UPDATE public.usuario SET correo_verificado = FALSE WHERE correo_verificado IS NULL;
ALTER TABLE public.usuario ALTER COLUMN correo_verificado SET DEFAULT FALSE;
ALTER TABLE public.usuario ALTER COLUMN correo_verificado SET NOT NULL;
UPDATE public.usuario SET estado_solicitud_organizador = 'NINGUNA' WHERE estado_solicitud_organizador IS NULL;
ALTER TABLE public.usuario ALTER COLUMN estado_solicitud_organizador SET DEFAULT 'NINGUNA';
ALTER TABLE public.usuario ALTER COLUMN estado_solicitud_organizador SET NOT NULL;
-- Keep legacy attendance rows without an unambiguous session association.
ALTER TABLE public.asistencias ALTER COLUMN sesion_evento_id DROP NOT NULL;
DO $$ BEGIN IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='asistencias' AND column_name='usuario_control_id') THEN ALTER TABLE public.asistencias ALTER COLUMN usuario_control_id DROP NOT NULL; END IF; END $$;
-- Existing certificates may lack academic/verifiable metadata. New rows are populated by the service.
ALTER TABLE public.certificados ALTER COLUMN porcentaje_asistencia DROP NOT NULL;
ALTER TABLE public.certificados ALTER COLUMN tipo_certificado DROP NOT NULL;
ALTER TABLE public.certificados ALTER COLUMN url_verificacion DROP NOT NULL;
-- These fields are nullable in the current Evento entity.
ALTER TABLE public.eventos ALTER COLUMN cupo_maximo DROP NOT NULL;
ALTER TABLE public.eventos ALTER COLUMN cupo_disponible DROP NOT NULL;

DO $$ BEGIN IF to_regclass('public.asistencias') IS NOT NULL AND to_regclass('public.sesiones_evento') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.asistencias'::regclass AND conname = 'fkei2ce73jqpguetylbwlo79otc') THEN ALTER TABLE public.asistencias ADD CONSTRAINT fkei2ce73jqpguetylbwlo79otc FOREIGN KEY (sesion_evento_id) REFERENCES public.sesiones_evento (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.asistencias') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.asistencias'::regclass AND conname = 'fkh8c30cqr5gg9xal53qcf4ovvp') THEN ALTER TABLE public.asistencias ADD CONSTRAINT fkh8c30cqr5gg9xal53qcf4ovvp FOREIGN KEY (registrado_por_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.asistencias') IS NOT NULL AND to_regclass('public.inscripciones') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.asistencias'::regclass AND conname = 'fkqo2amobg079bmu7405c18dx6u') THEN ALTER TABLE public.asistencias ADD CONSTRAINT fkqo2amobg079bmu7405c18dx6u FOREIGN KEY (inscripcion_id) REFERENCES public.inscripciones (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.certificados') IS NOT NULL AND to_regclass('public.eventos') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.certificados'::regclass AND conname = 'fk2pem0o1ubr6wsa65e8imh7lkc') THEN ALTER TABLE public.certificados ADD CONSTRAINT fk2pem0o1ubr6wsa65e8imh7lkc FOREIGN KEY (evento_id) REFERENCES public.eventos (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.certificados') IS NOT NULL AND to_regclass('public.inscripciones') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.certificados'::regclass AND conname = 'fkhrwlb8vemddq9h0xc2w1s1bnr') THEN ALTER TABLE public.certificados ADD CONSTRAINT fkhrwlb8vemddq9h0xc2w1s1bnr FOREIGN KEY (inscripcion_id) REFERENCES public.inscripciones (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.certificados') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.certificados'::regclass AND conname = 'fko0m9bk7jdif67t4c44qoi4ld7') THEN ALTER TABLE public.certificados ADD CONSTRAINT fko0m9bk7jdif67t4c44qoi4ld7 FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.eventos') IS NOT NULL AND to_regclass('public.categorias_evento') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.eventos'::regclass AND conname = 'fk2fli88ukt4xqvt216s6j8ysmo') THEN ALTER TABLE public.eventos ADD CONSTRAINT fk2fli88ukt4xqvt216s6j8ysmo FOREIGN KEY (categoria_id) REFERENCES public.categorias_evento (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.eventos') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.eventos'::regclass AND conname = 'fkh1h7x29wfjquom85fq8pipfxo') THEN ALTER TABLE public.eventos ADD CONSTRAINT fkh1h7x29wfjquom85fq8pipfxo FOREIGN KEY (organizador_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.eventos') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.eventos'::regclass AND conname = 'fktgx6iv1f89tnbarujgxefu3w') THEN ALTER TABLE public.eventos ADD CONSTRAINT fktgx6iv1f89tnbarujgxefu3w FOREIGN KEY (resuelto_por_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.inscripciones') IS NOT NULL AND to_regclass('public.eventos') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.inscripciones'::regclass AND conname = 'fkjyiki674rogcdbpj2cq7e92mi') THEN ALTER TABLE public.inscripciones ADD CONSTRAINT fkjyiki674rogcdbpj2cq7e92mi FOREIGN KEY (evento_id) REFERENCES public.eventos (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.inscripciones') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.inscripciones'::regclass AND conname = 'fkp3mu4l8ajsn8a3bluvw6qxrt0') THEN ALTER TABLE public.inscripciones ADD CONSTRAINT fkp3mu4l8ajsn8a3bluvw6qxrt0 FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.pagos') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.pagos'::regclass AND conname = 'fkdw9v2v5volhqkxapk8wmtwhy2') THEN ALTER TABLE public.pagos ADD CONSTRAINT fkdw9v2v5volhqkxapk8wmtwhy2 FOREIGN KEY (resuelto_por_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.pagos') IS NOT NULL AND to_regclass('public.inscripciones') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.pagos'::regclass AND conname = 'fks6n733v96i99c52hi4t0jm1sx') THEN ALTER TABLE public.pagos ADD CONSTRAINT fks6n733v96i99c52hi4t0jm1sx FOREIGN KEY (inscripcion_id) REFERENCES public.inscripciones (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.qr_asistencia') IS NOT NULL AND to_regclass('public.sesiones_evento') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.qr_asistencia'::regclass AND conname = 'fkf7fmktgu2yfy6x0ks3otic3kd') THEN ALTER TABLE public.qr_asistencia ADD CONSTRAINT fkf7fmktgu2yfy6x0ks3otic3kd FOREIGN KEY (sesion_evento_id) REFERENCES public.sesiones_evento (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.qr_asistencia') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.qr_asistencia'::regclass AND conname = 'fkpte5dnv10d2pgfq2xjwynit6l') THEN ALTER TABLE public.qr_asistencia ADD CONSTRAINT fkpte5dnv10d2pgfq2xjwynit6l FOREIGN KEY (generado_por_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.rol_permiso') IS NOT NULL AND to_regclass('public.rol') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.rol_permiso'::regclass AND conname = 'fk6o522368i97la9m9cqn0gul2e') THEN ALTER TABLE public.rol_permiso ADD CONSTRAINT fk6o522368i97la9m9cqn0gul2e FOREIGN KEY (rol_id) REFERENCES public.rol (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.rol_permiso') IS NOT NULL AND to_regclass('public.permiso') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.rol_permiso'::regclass AND conname = 'fkfyao8wd0o5tsyem1w55s3141k') THEN ALTER TABLE public.rol_permiso ADD CONSTRAINT fkfyao8wd0o5tsyem1w55s3141k FOREIGN KEY (permiso_id) REFERENCES public.permiso (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.sesion_usuario') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.sesion_usuario'::regclass AND conname = 'fkje7oqp5w4b7cjpkpdj9aoxqsc') THEN ALTER TABLE public.sesion_usuario ADD CONSTRAINT fkje7oqp5w4b7cjpkpdj9aoxqsc FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.sesiones_evento') IS NOT NULL AND to_regclass('public.eventos') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.sesiones_evento'::regclass AND conname = 'fkabcjn6ad08ffyihivik49k116') THEN ALTER TABLE public.sesiones_evento ADD CONSTRAINT fkabcjn6ad08ffyihivik49k116 FOREIGN KEY (evento_id) REFERENCES public.eventos (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.token_recuperacion') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.token_recuperacion'::regclass AND conname = 'fkayarsat3jko2nh63j568v55j4') THEN ALTER TABLE public.token_recuperacion ADD CONSTRAINT fkayarsat3jko2nh63j568v55j4 FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.token_verificacion_correo') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.token_verificacion_correo'::regclass AND conname = 'fk1tji3iryhiuuyeg4nw55b4lng') THEN ALTER TABLE public.token_verificacion_correo ADD CONSTRAINT fk1tji3iryhiuuyeg4nw55b4lng FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.usuario') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.usuario'::regclass AND conname = 'fkde3utysya7uk4xtla1wyojrpj') THEN ALTER TABLE public.usuario ADD CONSTRAINT fkde3utysya7uk4xtla1wyojrpj FOREIGN KEY (solicitud_resuelta_por_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.usuario_rol') IS NOT NULL AND to_regclass('public.rol') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.usuario_rol'::regclass AND conname = 'fk610kvhkwcqk2pxeewur4l7bd1') THEN ALTER TABLE public.usuario_rol ADD CONSTRAINT fk610kvhkwcqk2pxeewur4l7bd1 FOREIGN KEY (rol_id) REFERENCES public.rol (id) NOT VALID; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.usuario_rol') IS NOT NULL AND to_regclass('public.usuario') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'public.usuario_rol'::regclass AND conname = 'fkbyfgloj439r9wr9smrms9u33r') THEN ALTER TABLE public.usuario_rol ADD CONSTRAINT fkbyfgloj439r9wr9smrms9u33r FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) NOT VALID; END IF; END $$;

CREATE INDEX IF NOT EXISTS idx_asistencia_sesion
    ON public.asistencias(sesion_evento_id);
CREATE INDEX IF NOT EXISTS idx_certificado_evento
    ON public.certificados(evento_id);
CREATE INDEX IF NOT EXISTS uk_certificado_inscripcion
    ON public.certificados(inscripcion_id);
CREATE INDEX IF NOT EXISTS idx_certificado_usuario
    ON public.certificados(usuario_id);
CREATE INDEX IF NOT EXISTS idx_inscripcion_evento
    ON public.inscripciones(evento_id);
CREATE INDEX IF NOT EXISTS idx_inscripcion_usuario
    ON public.inscripciones(usuario_id);
CREATE INDEX IF NOT EXISTS ukcmeveyyrpuai5j5frvbigmkd4
    ON public.pagos(inscripcion_id);


-- Add primary/unique constraints only when an equivalent key is not already present.
-- Conflicting legacy data is preserved and reported; no rows are rewritten.
DO $$ BEGIN IF to_regclass('public.asistencias') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.asistencias'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.asistencias'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.asistencias'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.asistencias WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint asistencias_pkey omitted; conflicting historical rows exist in public.asistencias (id).'; ELSE ALTER TABLE public.asistencias ADD CONSTRAINT asistencias_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.asistencias') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.asistencias'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['inscripcion_id','sesion_evento_id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.asistencias'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.asistencias'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 2 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['inscripcion_id','sesion_evento_id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.asistencias WHERE inscripcion_id IS NOT NULL AND sesion_evento_id IS NOT NULL GROUP BY inscripcion_id, sesion_evento_id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uk_asistencia_inscripcion_sesion omitted; conflicting historical rows exist in public.asistencias (inscripcion_id, sesion_evento_id).'; ELSE ALTER TABLE public.asistencias ADD CONSTRAINT uk_asistencia_inscripcion_sesion UNIQUE (inscripcion_id, sesion_evento_id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.categorias_evento') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.categorias_evento'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.categorias_evento'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.categorias_evento'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.categorias_evento WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint categorias_evento_pkey omitted; conflicting historical rows exist in public.categorias_evento (id).'; ELSE ALTER TABLE public.categorias_evento ADD CONSTRAINT categorias_evento_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.categorias_evento') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.categorias_evento'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['nombre']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.categorias_evento'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.categorias_evento'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['nombre']::name[])) THEN IF EXISTS (SELECT 1 FROM public.categorias_evento WHERE nombre IS NOT NULL GROUP BY nombre HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uk9dcouuesv8ant9k1jaiaihdu4 omitted; conflicting historical rows exist in public.categorias_evento (nombre).'; ELSE ALTER TABLE public.categorias_evento ADD CONSTRAINT uk9dcouuesv8ant9k1jaiaihdu4 UNIQUE (nombre); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.categorias_evento') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.categorias_evento'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['nombre_normalizado']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.categorias_evento'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.categorias_evento'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['nombre_normalizado']::name[])) THEN IF EXISTS (SELECT 1 FROM public.categorias_evento WHERE nombre_normalizado IS NOT NULL GROUP BY nombre_normalizado HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint ukobteg7chcc396i7hljq0xx152 omitted; conflicting historical rows exist in public.categorias_evento (nombre_normalizado).'; ELSE ALTER TABLE public.categorias_evento ADD CONSTRAINT ukobteg7chcc396i7hljq0xx152 UNIQUE (nombre_normalizado); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.certificados') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.certificados'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.certificados'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.certificados'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.certificados WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint certificados_pkey omitted; conflicting historical rows exist in public.certificados (id).'; ELSE ALTER TABLE public.certificados ADD CONSTRAINT certificados_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.certificados') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.certificados'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['codigo_certificado']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.certificados'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.certificados'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['codigo_certificado']::name[])) THEN IF EXISTS (SELECT 1 FROM public.certificados WHERE codigo_certificado IS NOT NULL GROUP BY codigo_certificado HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uk_certificado_codigo omitted; conflicting historical rows exist in public.certificados (codigo_certificado).'; ELSE ALTER TABLE public.certificados ADD CONSTRAINT uk_certificado_codigo UNIQUE (codigo_certificado); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.certificados') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.certificados'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['inscripcion_id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.certificados'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.certificados'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['inscripcion_id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.certificados WHERE inscripcion_id IS NOT NULL GROUP BY inscripcion_id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uk_certificado_inscripcion omitted; conflicting historical rows exist in public.certificados (inscripcion_id).'; ELSE ALTER TABLE public.certificados ADD CONSTRAINT uk_certificado_inscripcion UNIQUE (inscripcion_id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.eventos') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.eventos'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.eventos'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.eventos'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.eventos WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint eventos_pkey omitted; conflicting historical rows exist in public.eventos (id).'; ELSE ALTER TABLE public.eventos ADD CONSTRAINT eventos_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.inscripciones') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.inscripciones'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.inscripciones'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.inscripciones'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.inscripciones WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint inscripciones_pkey omitted; conflicting historical rows exist in public.inscripciones (id).'; ELSE ALTER TABLE public.inscripciones ADD CONSTRAINT inscripciones_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.inscripciones') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.inscripciones'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['usuario_id','evento_id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.inscripciones'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.inscripciones'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 2 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['usuario_id','evento_id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.inscripciones WHERE usuario_id IS NOT NULL AND evento_id IS NOT NULL GROUP BY usuario_id, evento_id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uk_inscripcion_usuario_evento omitted; conflicting historical rows exist in public.inscripciones (usuario_id, evento_id).'; ELSE ALTER TABLE public.inscripciones ADD CONSTRAINT uk_inscripcion_usuario_evento UNIQUE (usuario_id, evento_id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.inscripciones') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.inscripciones'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['codigo_participante']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.inscripciones'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.inscripciones'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['codigo_participante']::name[])) THEN IF EXISTS (SELECT 1 FROM public.inscripciones WHERE codigo_participante IS NOT NULL GROUP BY codigo_participante HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint ukmey22k67agj6dplhkmixk9uf3 omitted; conflicting historical rows exist in public.inscripciones (codigo_participante).'; ELSE ALTER TABLE public.inscripciones ADD CONSTRAINT ukmey22k67agj6dplhkmixk9uf3 UNIQUE (codigo_participante); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.pagos') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.pagos'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.pagos'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.pagos'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.pagos WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint pagos_pkey omitted; conflicting historical rows exist in public.pagos (id).'; ELSE ALTER TABLE public.pagos ADD CONSTRAINT pagos_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.pagos') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.pagos'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['inscripcion_id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.pagos'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.pagos'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['inscripcion_id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.pagos WHERE inscripcion_id IS NOT NULL GROUP BY inscripcion_id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint ukcmeveyyrpuai5j5frvbigmkd4 omitted; conflicting historical rows exist in public.pagos (inscripcion_id).'; ELSE ALTER TABLE public.pagos ADD CONSTRAINT ukcmeveyyrpuai5j5frvbigmkd4 UNIQUE (inscripcion_id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.permiso') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.permiso'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.permiso'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.permiso'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.permiso WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint permiso_pkey omitted; conflicting historical rows exist in public.permiso (id).'; ELSE ALTER TABLE public.permiso ADD CONSTRAINT permiso_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.permiso') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.permiso'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['nombre']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.permiso'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.permiso'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['nombre']::name[])) THEN IF EXISTS (SELECT 1 FROM public.permiso WHERE nombre IS NOT NULL GROUP BY nombre HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uknwe6lkk7x7sbw94xcmbwgvycu omitted; conflicting historical rows exist in public.permiso (nombre).'; ELSE ALTER TABLE public.permiso ADD CONSTRAINT uknwe6lkk7x7sbw94xcmbwgvycu UNIQUE (nombre); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.qr_asistencia') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.qr_asistencia'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.qr_asistencia'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.qr_asistencia'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.qr_asistencia WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint qr_asistencia_pkey omitted; conflicting historical rows exist in public.qr_asistencia (id).'; ELSE ALTER TABLE public.qr_asistencia ADD CONSTRAINT qr_asistencia_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.qr_asistencia') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.qr_asistencia'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['token_hash']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.qr_asistencia'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.qr_asistencia'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['token_hash']::name[])) THEN IF EXISTS (SELECT 1 FROM public.qr_asistencia WHERE token_hash IS NOT NULL GROUP BY token_hash HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uk6s2onaib0e7w9xlsb1s9hdlhc omitted; conflicting historical rows exist in public.qr_asistencia (token_hash).'; ELSE ALTER TABLE public.qr_asistencia ADD CONSTRAINT uk6s2onaib0e7w9xlsb1s9hdlhc UNIQUE (token_hash); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.rol') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.rol'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.rol'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.rol'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.rol WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint rol_pkey omitted; conflicting historical rows exist in public.rol (id).'; ELSE ALTER TABLE public.rol ADD CONSTRAINT rol_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.rol') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.rol'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['nombre']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.rol'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.rol'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['nombre']::name[])) THEN IF EXISTS (SELECT 1 FROM public.rol WHERE nombre IS NOT NULL GROUP BY nombre HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uk43kr6s7bts1wqfv43f7jd87kp omitted; conflicting historical rows exist in public.rol (nombre).'; ELSE ALTER TABLE public.rol ADD CONSTRAINT uk43kr6s7bts1wqfv43f7jd87kp UNIQUE (nombre); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.rol_permiso') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.rol_permiso'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.rol_permiso'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.rol_permiso'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.rol_permiso WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint rol_permiso_pkey omitted; conflicting historical rows exist in public.rol_permiso (id).'; ELSE ALTER TABLE public.rol_permiso ADD CONSTRAINT rol_permiso_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.sesion_usuario') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.sesion_usuario'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.sesion_usuario'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.sesion_usuario'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.sesion_usuario WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint sesion_usuario_pkey omitted; conflicting historical rows exist in public.sesion_usuario (id).'; ELSE ALTER TABLE public.sesion_usuario ADD CONSTRAINT sesion_usuario_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.sesiones_evento') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.sesiones_evento'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.sesiones_evento'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.sesiones_evento'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.sesiones_evento WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint sesiones_evento_pkey omitted; conflicting historical rows exist in public.sesiones_evento (id).'; ELSE ALTER TABLE public.sesiones_evento ADD CONSTRAINT sesiones_evento_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.token_recuperacion') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.token_recuperacion'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.token_recuperacion'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.token_recuperacion'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.token_recuperacion WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint token_recuperacion_pkey omitted; conflicting historical rows exist in public.token_recuperacion (id).'; ELSE ALTER TABLE public.token_recuperacion ADD CONSTRAINT token_recuperacion_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.token_recuperacion') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.token_recuperacion'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['token']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.token_recuperacion'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.token_recuperacion'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['token']::name[])) THEN IF EXISTS (SELECT 1 FROM public.token_recuperacion WHERE token IS NOT NULL GROUP BY token HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uk33lrkaqshli5r4svsvil7yfc0 omitted; conflicting historical rows exist in public.token_recuperacion (token).'; ELSE ALTER TABLE public.token_recuperacion ADD CONSTRAINT uk33lrkaqshli5r4svsvil7yfc0 UNIQUE (token); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.token_verificacion_correo') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.token_verificacion_correo'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.token_verificacion_correo'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.token_verificacion_correo'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.token_verificacion_correo WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint token_verificacion_correo_pkey omitted; conflicting historical rows exist in public.token_verificacion_correo (id).'; ELSE ALTER TABLE public.token_verificacion_correo ADD CONSTRAINT token_verificacion_correo_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.token_verificacion_correo') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.token_verificacion_correo'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['token_hash']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.token_verificacion_correo'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.token_verificacion_correo'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['token_hash']::name[])) THEN IF EXISTS (SELECT 1 FROM public.token_verificacion_correo WHERE token_hash IS NOT NULL GROUP BY token_hash HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint uka184aisgc1eaapsoky0n6vdu3 omitted; conflicting historical rows exist in public.token_verificacion_correo (token_hash).'; ELSE ALTER TABLE public.token_verificacion_correo ADD CONSTRAINT uka184aisgc1eaapsoky0n6vdu3 UNIQUE (token_hash); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.usuario') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.usuario'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.usuario'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.usuario'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.usuario WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint usuario_pkey omitted; conflicting historical rows exist in public.usuario (id).'; ELSE ALTER TABLE public.usuario ADD CONSTRAINT usuario_pkey PRIMARY KEY (id); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.usuario') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.usuario'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['ci']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.usuario'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.usuario'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['ci']::name[])) THEN IF EXISTS (SELECT 1 FROM public.usuario WHERE ci IS NOT NULL GROUP BY ci HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint ukdf45etfnvb4mcw8j04h8h93vd omitted; conflicting historical rows exist in public.usuario (ci).'; ELSE ALTER TABLE public.usuario ADD CONSTRAINT ukdf45etfnvb4mcw8j04h8h93vd UNIQUE (ci); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.usuario') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.usuario'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['correo_electronico']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.usuario'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.usuario'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['correo_electronico']::name[])) THEN IF EXISTS (SELECT 1 FROM public.usuario WHERE correo_electronico IS NOT NULL GROUP BY correo_electronico HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint ukf7w2jekriedf7k6a4kaclt9t7 omitted; conflicting historical rows exist in public.usuario (correo_electronico).'; ELSE ALTER TABLE public.usuario ADD CONSTRAINT ukf7w2jekriedf7k6a4kaclt9t7 UNIQUE (correo_electronico); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.usuario') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.usuario'::regclass AND c.contype = 'u' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['ru']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.usuario'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.usuario'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['ru']::name[])) THEN IF EXISTS (SELECT 1 FROM public.usuario WHERE ru IS NOT NULL GROUP BY ru HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint ukq8gbcdu6kstk7m1fs1cqxc0od omitted; conflicting historical rows exist in public.usuario (ru).'; ELSE ALTER TABLE public.usuario ADD CONSTRAINT ukq8gbcdu6kstk7m1fs1cqxc0od UNIQUE (ru); END IF; END IF; END $$;
DO $$ BEGIN IF to_regclass('public.usuario_rol') IS NOT NULL AND NOT (EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conrelid = 'public.usuario_rol'::regclass AND c.contype = 'p' AND c.conkey = ARRAY(SELECT a.attnum::smallint FROM unnest(ARRAY['id']::name[]) WITH ORDINALITY AS wanted(col, ord) JOIN pg_attribute a ON a.attrelid = 'public.usuario_rol'::regclass AND a.attname = wanted.col ORDER BY wanted.ord))) AND NOT (EXISTS (SELECT 1 FROM pg_index i WHERE i.indrelid = 'public.usuario_rol'::regclass AND i.indisunique AND i.indisvalid AND i.indpred IS NULL AND i.indexprs IS NULL AND i.indnkeyatts = 1 AND ARRAY(SELECT a.attname::name FROM unnest(i.indkey::smallint[]) WITH ORDINALITY AS keys(attnum, ord) JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=keys.attnum WHERE keys.ord <= i.indnkeyatts ORDER BY keys.ord) = ARRAY['id']::name[])) THEN IF EXISTS (SELECT 1 FROM public.usuario_rol WHERE id IS NOT NULL GROUP BY id HAVING count(*) > 1) THEN RAISE NOTICE 'V3: constraint usuario_rol_pkey omitted; conflicting historical rows exist in public.usuario_rol (id).'; ELSE ALTER TABLE public.usuario_rol ADD CONSTRAINT usuario_rol_pkey PRIMARY KEY (id); END IF; END IF; END $$;
