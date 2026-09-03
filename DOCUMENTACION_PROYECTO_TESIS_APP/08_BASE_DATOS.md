# Base de datos

## Objetivo del documento
Reconstruir el modelo persistente desde entidades JPA.

## Información encontrada
Motor configurado: PostgreSQL (`application.yml`, Docker PostgreSQL 16). Tablas de entidades: `usuario`, `rol`, `permiso`, `usuario_rol`, `rol_permiso`, `token_recuperacion`, `facultades`, `carreras`, `categorias_evento`, `eventos`, `inscripciones`, `pagos`, `comprobantes_pago`, `credenciales`, `codigos_qr`, `control_acceso`, `asistencias`, `certificados`, `encuestas`, `preguntas_encuesta`, `respuestas_encuesta`.

Relaciones verificables: Facultad 1:N Carrera; Categoría 1:N Evento; Usuario/Evento 1:N Inscripción; Inscripción 1:1 Credencial, Certificado y Encuesta (restricciones `unique=true`); Credencial 1:1 Código QR; Pago y comprobante se relacionan con inscripción; asistencia y control de acceso referencian inscripción/credencial y usuario de control. `EntidadBase` aporta UUID y campos de auditoría a las entidades que la extienden.

## Evidencias
`backend/src/main/java/bo/uajms/eventos/modulos/**/entidades/*.java`; `comun/EntidadBase.java`.

## Estado
EXISTENTE como modelo JPA.

## Observaciones
Las columnas, tipos Java, nulabilidad, `unique` y enums están en las anotaciones de cada entidad; constituyen la fuente primaria sobre el esquema generado.

## Inconsistencias
NO ENCONTRADO EN EL PROYECTO: archivos SQL en `database/scripts`; por tanto no se pudo contrastar el DDL físico, índices creados ni versión de esquema.

## Información faltante
NO VERIFICABLE: datos iniciales reales, tamaño/volumen, planes de consulta, procedimientos, triggers y backup.
