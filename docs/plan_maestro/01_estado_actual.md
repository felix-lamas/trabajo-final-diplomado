# 01. Estado actual del sistema — corte E3.4

Documento auxiliar de estado; la monografía vigente y `docs/auditoria/E3_3_RECONCILIACION_DOCUMENTO_SISTEMA.md` definen el alcance. No confundir código localizado con operación productiva probada.

| Área | Implementación estática observada | Pendiente / límite |
|---|---|---|
| Backend | Java 21, Spring Boot 3.3, JPA, Spring Security/JWT, PostgreSQL, módulos de usuario, categoría, evento, inscripción, pago, sesión, asistencia, certificado y reportes. | Verificar esquema/despliegue real. |
| Angular | Angular 21; catálogo/detalle, auth, solicitud organizador, admin, organización, inscripciones/pagos, sesiones/asistencia, certificados y reportes. QR pago se carga/se sirve con API Vidia. | Ejecutar E2E/visual y limpiar campos API no funcionales. |
| Flutter | Login, catálogo/detalle, inscripción y mis inscripciones. | RF-07 pagos/comprobantes, RF-08 scanner/GPS, RF-09 certificados e historial completo pendientes; test no concluyente en E3.2. |
| QR/archivos | QR pago de evento y comprobantes en Storage privado mediante adapter backend. QR asistencia token temporal por sesión. | Smoke real en hosting no verificado. |
| Correo | Spring Mail configurado para SMTP/Brevo por variables. | Envío desde producción no verificado. |
| Reportes | Dashboard y JSON/PDF/XLSX disponibles según controladores/servicios. | No tratar encuestas, satisfacción ni `qrUtilizados=0` como estadísticas reales. |
| Evidencia | Última referencia E3.2: backend 460 tests, 1 skip; Angular 271 PASS. | Flutter resultado no concluyente; no se midieron RNF ni uptime. |

Las clases legacy de encuestas, facultades/carreras, credenciales permanentes y control de acceso antiguo no forman parte de módulos backend actuales; existen documentos/colecciones históricas que deben etiquetarse como tales.
