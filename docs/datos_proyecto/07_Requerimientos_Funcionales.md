# Requisitos funcionales de Vidia

**Base reconciliada E3.4:** conserva los 13 RF, IDs y prioridades de la monografía actual. Los criterios marcados aquí aclaran el sistema acordado; deben integrarse formalmente al DOCX solo en E3.5. Estado técnico al corte: implementación backend/web no equivale a implementación móvil ni a producción verificada.

| ID | Requisito | Prioridad | Actores |
|---|---|---|---|
| RF-01 | Registro, autenticación y recuperación segura de acceso | MUST | USUARIO |
| RF-02 | Solicitar condición de organizador | SHOULD | USUARIO, ADMINISTRADOR |
| RF-03 | Crear, gestionar y enviar eventos a revisión | MUST | ORGANIZADOR |
| RF-04 | Revisar y gestionar eventos enviados a revisión | MUST | ADMINISTRADOR |
| RF-05 | Consultar, buscar y ver detalle de eventos publicados | MUST | USUARIO / público |
| RF-06 | Gestionar inscripción a eventos gratuitos y pagados | MUST | USUARIO; ADMINISTRADOR/ORGANIZADOR según consulta permitida |
| RF-07 | Registrar pagos, presentar comprobantes y validar pagos | MUST | USUARIO, ORGANIZADOR propietario, ADMINISTRADOR |
| RF-08 | Registrar asistencia por sesión mediante QR y geolocalización | MUST | USUARIO; ORGANIZADOR/ADMINISTRADOR consultan en su ámbito |
| RF-09 | Generar, consultar y descargar certificados elegibles | SHOULD | ORGANIZADOR, ADMINISTRADOR, USUARIO |
| RF-10 | Consultar historial propio de inscripciones, pagos, asistencias y certificados | SHOULD | USUARIO |
| RF-11 | Gestionar categorías de eventos | SHOULD | ADMINISTRADOR |
| RF-12 | Consultar dashboards y exportar reportes disponibles | COULD | ADMINISTRADOR, ORGANIZADOR dentro de su alcance |
| RF-13 | Verificar públicamente un certificado por código | COULD | Tercero público (sin rol ni cuenta) |

## Criterios de aceptación reconciliados

### RF-01 — MUST

- CA-01.1 Datos válidos/únicos crean cuenta con privilegio inicial mínimo `USUARIO`.
- CA-01.2 Correo/RU/CI duplicado (según el tipo de registro) se rechaza con error entendible.
- CA-01.3 Credenciales válidas autentican y entregan mecanismo expirable de autorización; credenciales inválidas se rechazan.
- CA-01.4 El correo se verifica mediante la operación confirmada por backend; recuperación y reset respetan expiración/uso único y no revelan enumeración de cuentas.
- CA-01.5 Logout/sesión se comportan según la revocación implementada por backend; cliente no altera roles.

### RF-02 — SHOULD

- CA-02.1 USUARIO sin solicitud pendiente puede solicitar condición de organizador.
- CA-02.2 La solicitud duplicada pendiente se rechaza.
- CA-02.3 Solo ADMINISTRADOR resuelve; aprobación asigna ORGANIZADOR y rechazo conserva USUARIO, según backend.
- CA-02.4 El resultado se vuelve a consultar al backend; no se representa solo mediante cambio local.

### RF-03 — MUST

- CA-03.1 ORGANIZADOR aprobado crea un evento BORRADOR vinculado al usuario autenticado; no envía organizador/estado como autoridad del cliente.
- CA-03.2 ORGANIZADOR edita solo sus eventos en estados editables definidos por backend.
- CA-03.3 Datos obligatorios/combos inválidos impiden guardar o enviar a revisión.
- CA-03.4 Un evento de pago puede guardar monto/instrucciones y, opcionalmente, cargar, reemplazar o eliminar una imagen QR real en estados permitidos. La imagen es proporcionada por ORGANIZADOR y la clave privada no se expone.
- CA-03.5 Envío válido transiciona a EN_REVISION; propiedad y estado se verifican en servidor.

### RF-04 — MUST

- CA-04.1 Solo ADMINISTRADOR lista/resuelve eventos EN_REVISION.
- CA-04.2 Aprobación publica; rechazo registra el motivo/decisión conforme al contrato.
- CA-04.3 El organizador puede corregir y reenviar un evento RECHAZADO según las transiciones disponibles.
- CA-04.4 Una operación no permitida por estado devuelve error de negocio; interfaz no simula éxito.

### RF-05 — MUST

- CA-05.1 Catálogo y detalle público muestran solo eventos publicados visibles.
- CA-05.2 La búsqueda/filtros soportados son texto, categoría, tipo de inscripción y modalidad en API; la experiencia web también filtra precio de acuerdo con sus datos. No afirmar filtro de fecha, que no se encontró en la API revisada.
- CA-05.3 Resultado vacío se informa sin datos de demostración.
- CA-05.4 Evento de pago muestra monto/instrucciones reales y QR solo cuando el evento lo tiene. QR se sirve por backend; evento gratuito no obliga a QR ni muestra sección de pago.

### RF-06 — MUST

- CA-06.1 USUARIO puede inscribirse una vez en evento publicado, abierto y con capacidad disponible cuando aplica.
- CA-06.2 Inscripción gratuita resulta CONFIRMADA según backend.
- CA-06.3 Inscripción pagada crea el pago con monto de backend y estado inicial previsto; no acepta monto del participante.
- CA-06.4 Duplicidad, falta de cupo, estado inválido o falta de autorización se rechazan; mis inscripciones devuelve recursos propios.

### RF-07 — MUST

- CA-07.1 No hay checkout ni gateway: participante paga externamente según instrucciones/QR opcional del evento.
- CA-07.2 USUARIO presenta archivo en su pago propio; se valida contenido/tamaño/tipo y se guarda privado.
- CA-07.3 Carga válida deja pago e inscripción en PENDIENTE_VALIDACION.
- CA-07.4 ADMINISTRADOR u ORGANIZADOR propietario revisa; aprobación produce APROBADO/CONFIRMADA y rechazo produce RECHAZADO y permite reenvío cuando el estado/regla backend lo permite.
- CA-07.5 El participante consulta estado y comprobante asociado; recursos ajenos no se exponen.

### RF-08 — MUST

- CA-08.1 Una sesión requiere configuración/estado admisible; QR de asistencia es temporal y vinculado a sesión.
- CA-08.2 USUARIO con inscripción confirmada escanea desde Flutter y envía token/ubicación; backend verifica vigencia y ventana horaria.
- CA-08.3 Ubicación/precisión fuera de regla del backend se rechaza; la ubicación no se confía a validación cliente.
- CA-08.4 Se registra como máximo una asistencia por inscripción y sesión.
- CA-08.5 El QR temporal puede usarse por múltiples participantes elegibles; no es de un solo uso global. Reemisión/rotación se rige por API.

### RF-09 — SHOULD

- CA-09.1 ORGANIZADOR propietario o ADMINISTRADOR puede iniciar generación solo para inscripción/evento elegible conforme a backend.
- CA-09.2 El backend exige evento FINALIZADO, inscripción CONFIRMADA y pago APROBADO cuando corresponda, más asistencia según tipo.
- CA-09.3 Certificado curricular aplica el umbral actual de 80 % de sesiones requeridas y muestra horas solo si están definidas; tipo no curricular sigue su regla backend.
- CA-09.4 Usuario autorizado consulta y descarga PDF; se genera bajo operación de descarga, no automáticamente al cerrar el evento.
- CA-09.5 Código de verificación es el emitido por backend, no uno creado por cliente.

### RF-10 — SHOULD

- CA-10.1 USUARIO consulta sus inscripciones y estados.
- CA-10.2 Pagos, asistencias y certificados se consultan mediante los recursos propios disponibles por módulo; no se presupone endpoint agregado.
- CA-10.3 Listas vacías/errores se presentan diferenciadamente; el cliente no inventa progreso o métricas.

### RF-11 — SHOULD

- CA-11.1 ADMINISTRADOR crea/edita categoría con datos válidos.
- CA-11.2 Duplicidad y restricciones de eliminación se reportan conforme a respuesta backend.
- CA-11.3 Lectura y mutación se restringen según permisos backend.

### RF-12 — COULD

- CA-12.1 Dashboard entrega únicamente métricas calculadas por backend y acotadas por rol/ownership.
- CA-12.2 Reportes disponibles incluyen tipos implementados por backend; exportaciones se sirven como archivos reales PDF/XLSX.
- CA-12.3 No se presentan encuestas/satisfacción ni conteo de QR utilizados mientras esos campos sean placeholders.

### RF-13 — COULD

- CA-13.1 Código existente responde con la información de verificación pública permitida.
- CA-13.2 Código desconocido responde como no registrado, diferenciándolo de error de servidor.
- CA-13.3 Consulta pública no exige autenticación y no expone datos privados ajenos a DTO.
