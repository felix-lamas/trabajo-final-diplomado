# Matriz de trazabilidad E3.4

**Alcance:** relaciona los 13 RF acordados con evidencia estática actual. Los estados no prueban por sí solos ejecución, aceptación ni producción. Última ejecución local citada en E3.2; esta matriz no vuelve a ejecutar pruebas.

| RF | Prioridad | Backend | Angular | Flutter | Estado global al corte | Evidencia/prueba por completar |
|---|---|---|---|---|---|---|
| RF-01 | MUST | Implementado registro, correo, login, reset y sesión | Implementado | Parcial: login/sesión; no registro/verificación/reset | PARCIAL multiplataforma | Flujos de email/session por cliente, 401/errores y evidencia producción. |
| RF-02 | SHOULD | Solicitud y decisión admin | Web implementada | No aplica al cliente participante actual | Implementado backend/web | E2E solicitud pendiente, aprobación/rechazo y perfil/rol posterior. |
| RF-03 | MUST | Ciclo, ownership, carga de QR pago | Formulario y gestión QR implementados | No aplica a gestión por actor | Implementado backend/web | Carga/reemplazo/borrado por estados y roles; producción Storage no verificada. |
| RF-04 | MUST | Revisión/publicación/rechazo | Administración web implementada | No aplica | Implementado backend/web | Prueba de transición/estado y rechazo con motivo. |
| RF-05 | MUST | Catálogo, búsqueda, detalle y QR controlado | Catálogo/detalle implementados | Catálogo/detalle implementados; pago móvil pendiente | Implementado web; parcial móvil | Pruebas de filtros reales, QR para evento publicado y producción. |
| RF-06 | MUST | Inscripción gratis/pagada, cupo y duplicidad | Implementado | Inscripción/mis inscripciones; pago todavía no | Parcial por modalidad pagada móvil | Pruebas integrado de cupo, duplicidad, gratis y pagado. |
| RF-07 | MUST | Pagos, comprobante, revisión, Storage privado | Flujo de usuario/admin/organizador implementado | Pendiente; detalle dice pago llegará después | Parcial; cliente móvil MUST pendiente | Upload/review/retry/Blob y E2E móvil; no gateway. |
| RF-08 | MUST | Sesiones, QR temporal, GPS/radio/duplicidad; endpoints | Gestión/consulta de organizador; no captura GPS de participante | Pendiente (no scanner/location) | Parcial; cliente móvil MUST pendiente | Prueba Android real de QR, permisos GPS, ventana/radio/precisión y duplicado. |
| RF-09 | SHOULD | Elegibilidad, código, PDF y verificación | Mis certificados/emisión/descarga | Pendiente | Parcial móvil | Elegibilidad curricular/no curricular, PDF, acceso propio y público. |
| RF-10 | SHOULD | Consultas propias por recursos separados | Vistas por módulo, no vista integral única | Mis inscripciones únicamente | Parcial | Completar historial móvil por endpoints reales; no inventar agregado. |
| RF-11 | SHOULD | CRUD categorías y restricciones | CRUD admin implementado | No aplica | Implementado backend/web | Casos restricciones/errores y despliegue. |
| RF-12 | COULD | Dashboards/reportes disponibles; PDF/XLSX | Dashboards y reportes | No aplica | Parcial respecto a dimensiones del requisito | Retirar placeholders (encuestas/satisfacción/QR usados) o calcularlos; mantener solo métricas reales. |
| RF-13 | COULD | Endpoint público por código | Ruta pública sin guard | No aplica | Implementado en código | Verificación pública 404/500/datos mínimos en prod. |

## Roles y alcance

Los roles exactos son `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO`. ADMIN tiene alcance global donde el contrato lo permite; ORGANIZADOR está limitado por ownership del evento; USUARIO consulta/gestiona su propio ámbito. Verificador de certificado no autenticado es actor público, no rol. Recursos fuera de ownership pueden devolver 404; un rol no autorizado 403.

## QR de pago, QR de asistencia y verificación

| Elemento | Proveedor/función | Almacenamiento/consulta | Estado cliente |
|---|---|---|---|
| QR de pago del evento | Imagen opcional que entrega ORGANIZADOR para facilitar pago externo | Objeto privado Supabase, key solo backend; lectura mediante `/api/v1/eventos/{id}/qr-pago` | Web implementado; verificar detalle Flutter/pago cuando se complete. |
| QR de asistencia | Token temporal por sesión | Hash/estado en backend; API sesión/QR; server valida GPS y asistencia | Cliente de captura Flutter pendiente. |
| Verificación certificado | Código/QR que conduce a comprobación pública | Endpoint público `/api/v1/certificados/verificar/{codigo}`; no usar para asistencia | Página Web pública; Flutter no requerido. |

## Evidencia de pruebas disponible

E3.2 reportó backend 460 tests (0 fallos/errores, 1 skip) y Angular 271/271 PASS; Flutter test se interrumpió sin resultado concluyente. Son resultados locales de ese checkpoint, no evidencia productiva ni cobertura íntegra de todos los CA. En E3.4 no se ejecutaron pruebas.
