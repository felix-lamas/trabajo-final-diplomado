# 08. API vigente

Contrato canónico REST bajo `/api/v1`; OpenAPI/Swagger del código es fuente para método, payload, status y autorización. Lista resumida no reemplaza OpenAPI. Ignorar como API vigente rutas legacy de `/api`, credenciales, `codigos-qr`, control de acceso antiguo, encuestas y facultades/carreras.

| Dominio | Rutas representativas actuales |
|---|---|
| Auth | `/auth/registro`, `/auth/login`, `/auth/verificar-correo`, `/auth/reenviar-verificacion`, `/auth/recuperar-contrasena`, `/auth/restablecer-contrasena`, `/auth/logout` |
| Usuario | `/usuarios/perfil`, `/usuarios/cambiar-contrasena`, `/usuarios/solicitud-organizador`, `/usuarios/solicitudes-organizador` y decisiones admin |
| Categorías | `/categorias-evento`, `/categorias-evento/activas` |
| Eventos | `/eventos`, `/eventos/publicados`, `/eventos/publicados/buscar`, `/eventos/revision`, `/eventos/{id}` y transiciones de revisión/estado |
| QR pago evento | `PUT`, `DELETE`, `GET /eventos/{id}/qr-pago`; carga multipart campo `archivo`, lectura autorizada por backend |
| Inscripciones | `/inscripciones`, `/inscripciones/mis-inscripciones`, `/inscripciones/evento/{eventoId}`, recurso/cancelación/comprobante según controller |
| Pagos | `/pagos`, `/pagos/mis-pagos`, `/pagos/pendientes`, `/pagos/{id}`, comprobante, validar/rechazar |
| Sesión/asistencia | `/eventos/{eventoId}/sesiones`, `/sesiones/{id}`, `/sesiones/{id}/estado`, `/sesiones/{sesionId}/qr`, `/asistencias`, `/asistencias/mis-asistencias`, `/asistencias/evento/{id}` |
| Certificado | `/certificados/generar/{inscripcionId}`, `/certificados/mis-certificados`, `/eventos/{eventoId}/certificados`, `/certificados/{id}/descargar`, `/certificados/verificar/{codigo}` |
| Dashboard/reportes | `/dashboard*`, `/reportes/eventos`, `/reportes/participantes`, `/reportes/pagos`, `/reportes/certificados`, exportaciones PDF/XLSX |

No se encontró agregado único para historial. El endpoint QR de asistencia no es el endpoint QR pago. El usuario Flutter no debe llamar Supabase directo.
