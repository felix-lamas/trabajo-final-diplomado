# API REST

## Objetivo del documento
Inventariar endpoints implementados desde controladores.

## Información encontrada
| Módulo | Métodos/rutas principales | Acceso |
|---|---|---|
| Auth | POST `/api[/v1]/auth/registro`, `login`, `recuperar-contrasena`, `restablecer-contrasena`, `resetear-contrasena` | público |
| Usuarios | GET `/api/v1/usuarios`, `/{id}`, `/perfil`; PUT perfil; POST cambiar contraseña | autenticado; lista/ID admin |
| Catálogos | CRUD `/facultades`, `/carreras`, `/categorias-evento` | lectura por roles; escritura admin |
| Eventos | GET publicados, `/{id}`, categoría; CRUD y PATCH estado | público/roles según ruta |
| Inscripciones | POST, GET propias, por id/evento, PATCH cancelar | roles definidos |
| Pagos | POST pago/comprobante; GET propios/pendientes/todos/id; PATCH validar/rechazar | roles definidos |
| Credenciales/QR | generar, consultar, QR, descarga, validar QR | roles definidos |
| Acceso/asistencia | validar QR, autorizar, denegar, historial, búsquedas; asistencias por evento | control/admin/organizador |
| Certificados/encuestas/reportes | generar/verificar/descargar; responder/estadísticas; dashboard/exportación | según controlador |

Cada endpoint usa `ResponseEntity`; los bodies se declaran por DTO (`*Request`, `*Response`) y los errores se gestionan en `core/excepciones`.

## Evidencias
Todos los archivos bajo `backend/src/main/java/bo/uajms/eventos/modulos/**/controladores/`; `core/configuracion/OpenAPIConfig.java`.

## Estado
IMPLEMENTADO en código.

## Observaciones
Existe springdoc y Swagger en `/swagger-ui.html`; no se halló contrato OpenAPI exportado para compararlo con ejecución.

## Inconsistencias
Controladores de certificados, encuestas, acceso y asistencia admiten dos prefijos, y reportes usa `/api`, no `/api/v1`.

## Información faltante
NO VERIFICABLE: respuestas reales, códigos de error efectivos, límites, paginación y disponibilidad de Swagger en ejecución.
