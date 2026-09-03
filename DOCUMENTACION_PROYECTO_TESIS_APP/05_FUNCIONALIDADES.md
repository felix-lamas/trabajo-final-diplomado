# Funcionalidades verificadas

## Objetivo del documento
Inventariar funcionalidades con estado y evidencia de código.

## Información encontrada
| Funcionalidad | Actor/evidencia | Endpoint o interfaz | Estado |
|---|---|---|---|
| Registro, login, recuperación y restablecimiento | público; `AutenticacionControlador` | `/api/v1/auth/*`; `features/auth` | IMPLEMENTADO |
| Perfil y cambio de contraseña | autenticado; `UsuarioControlador` | `/usuarios/perfil`, `/cambiar-contrasena` | IMPLEMENTADO |
| CRUD facultades/carreras/categorías | administrador | controladores respectivos; `features/admin` | IMPLEMENTADO |
| Catálogo público y detalle de eventos | público | `/eventos/publicados`, `/eventos/{id}`; `features/publico` | IMPLEMENTADO |
| CRUD y ciclo publicar/cancelar/finalizar evento | administrador/organizador | `EventoController` | IMPLEMENTADO |
| Inscripción, consulta y cancelación | estudiante/external/admin | `InscripcionController`; pantallas privadas/públicas | IMPLEMENTADO |
| Registro, comprobante y validación de pago | participante, administrador/organizador | `PagoController`; `pagos` | IMPLEMENTADO |
| Credencial, QR y PDF | participante/admin | `CredencialController` | IMPLEMENTADO |
| Control por QR, autorización/denegación e historial | personal de control | `ControlAccesoController`; `asistencias` | IMPLEMENTADO |
| Asistencias por evento | control/organizador/admin | `AsistenciaController` | IMPLEMENTADO |
| Certificados y validación pública | organizador/admin/participante | `CertificadoController`; `certificados` | IMPLEMENTADO |
| Encuestas y estadísticas | autenticado/admin/organizador | `EncuestaController`; `encuestas` | IMPLEMENTADO |
| Dashboard, reportes y exportación | admin/organizador | `DashboardController`; `reportes` | IMPLEMENTADO |

## Evidencias
Controladores en `backend/src/main/java/bo/uajms/eventos/modulos/**/controladores`; rutas/componentes en `frontend/src/app/features/`.

## Estado
IMPLEMENTADO a nivel de código; ejecución integral NO VERIFICABLE.

## Observaciones
Entradas se validan con DTOs `@Valid` donde se declara; salida suele ser `ResponseEntity` con DTO.

## Inconsistencias
La pantalla pública de inscripción existe, pero el endpoint de inscripción exige autenticación por `@PreAuthorize`; no es un registro anónimo verificable.

## Información faltante
NO ENCONTRADO EN EL PROYECTO: integración de pago electrónico, notificaciones push/SMS, carga de imágenes de eventos y auditoría funcional automatizada.
