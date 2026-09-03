# Flujos funcionales

## Objetivo del documento
Expresar secuencias reconstruidas desde rutas, servicios y entidades.

## Información encontrada
1. **Autenticación (EXISTENTE):** formulario Angular → POST auth/login o registro → `AutenticacionServicio` → BCrypt/JWT → token recibido por `AuthService` → interceptor Bearer.
2. **Inscripción (EXISTENTE):** usuario autenticado → catálogo/detalle → POST `/inscripciones` con `CrearInscripcionRequest` → `InscripcionService` → entidad `inscripciones` → respuesta DTO.
3. **Pago (EXISTENTE):** participante → registrar pago o adjuntar comprobante → `PagoService`/`ArchivoSeguroServicio` → pago/comprobante → organizador/admin valida o rechaza.
4. **Acceso (EXISTENTE):** personal de control → escáner o código/documento → validar QR → autorizar/denegar → `control_acceso`; asistencia se consulta por evento.
5. **Certificación (EXISTENTE):** organizador/admin genera por inscripción → `CertificadoService` → certificado con código → consulta pública por `/verificacion-certificados/{codigo}`.
6. **Encuesta (EXISTENTE):** autenticado → responder → encuesta/respuestas; administrador/organizador consulta estadísticas.

## Evidencias
`AutenticacionControlador`, `InscripcionController`, `PagoController`, `ControlAccesoController`, `CertificadoController`, `EncuestaController`; diagramas `docs/design/03_*`, `04_*`.

## Estado
EXISTENTE como flujo de código.

## Observaciones
La condición de emisión de certificado se delega a servicio; debe verificarse en `CertificadoService` antes de documentar reglas académicas exactas.

## Inconsistencias
Los diagramas son fuente secundaria y requieren contraste individual antes de usarlos en tesis.

## Información faltante
NO VERIFICABLE: confirmaciones de correo, pasarela bancaria y comportamiento ante fallos de infraestructura.
