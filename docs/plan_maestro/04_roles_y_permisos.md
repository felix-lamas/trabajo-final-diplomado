# 04. Roles y límites de acceso

Los únicos roles funcionales son `ADMINISTRADOR`, `ORGANIZADOR` y `USUARIO`. “Participante” es descripción funcional de USUARIO; perfil interno/externo no concede autoridad separada. Tercero que verifica un certificado es público y no autenticado.

| Operación | ADMINISTRADOR | ORGANIZADOR | USUARIO |
|---|---|---|---|
| Solicitud de condición organizador | Listar/aprobar/rechazar | No | Crear solicitud propia |
| Evento | Global solo en rutas administrativas habilitadas | Crear/editar/enviar y operar eventos propios según estado | Consultar publicados |
| QR pago evento | Según permisos de gestión | Evento propio y estados permitidos | No cargar; ver si evento visible |
| Inscripción | Consulta global solo donde endpoint lo autoriza | Consulta de inscritos de eventos propios | Inscripción y consulta/cancelación propia según reglas |
| Pago/comprobante | Consulta/revisión global donde el contrato lo permite | Validar/rechazar pagos de eventos propios | Consultar y cargar comprobante de pago propio |
| Sesiones/asistencia | Operaciones habilitadas globalmente | Gestionar sesiones y consultar asistencia de eventos propios | Registrar propia mediante flujo Flutter pendiente; consultar propias |
| Certificado | Generación/consulta de eventos autorizados | Eventos propios | Consultar/descargar propios |
| Reportes | Datos globales según API | Datos limitados a eventos propios | Sin dashboard administrativo |

El backend controla autorización y ownership. Angular/Flutter solo ofrecen UX. Falta de JWT/invalidación se trata como 401; rol insuficiente 403; un objeto ajeno puede ocultarse como 404 de acuerdo con cada servicio.
