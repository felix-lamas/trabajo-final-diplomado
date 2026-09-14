# 17. Planificación progresiva del desarrollo por sprints

## Propósito

Esta planificación organiza el desarrollo real de la Plataforma Web para la Gestión Integral de Eventos Universitarios UAJMS en ocho sprints. Sustituye el backlog inicial de cuatro sprints, que era una propuesta de MVP, por una secuencia trazable a los requisitos, historias, casos de uso, reglas de negocio, matriz de trazabilidad y evidencia del código disponible.

No define requisitos nuevos ni presupone funcionalidades no documentadas. Los estados se basan en la evidencia de `12_Matriz_de_Trazabilidad.md` y en los módulos actuales de backend y frontend; por tanto, **implementado** significa que existe evidencia en el código, no que se haya completado una aceptación o una prueba de calidad.

## Fuentes revisadas y criterio de organización

- Requisitos funcionales: `07_Requerimientos_Funcionales.md`.
- Requisitos no funcionales: `08_Requerimientos_No_Funcionales.md`.
- Reglas de negocio: `09_Reglas_De_Negocio.md`.
- Casos de uso e historias: `10_Casos_de_Uso.md` y `11_Historias_de_Usuario.md`.
- Trazabilidad y estado de evidencia: `12_Matriz_de_Trazabilidad.md`.
- Evidencia funcional y de pruebas: módulos `usuarios`, `eventos`, `inscripciones`, `pagos`, `credenciales`, `codigo_qr`, `control_acceso`, `asistencias`, `certificados`, `encuestas` y `reportes`, sus módulos Angular equivalentes, y los documentos `sprint_*_pruebas.md`.

La secuencia respeta las dependencias existentes: la identidad y el catálogo preceden a la inscripción; la validación de pago precede al acceso; la asistencia precede a la certificación; y los datos operativos alimentan reportes y estadísticas.

## Estado consolidado

| Clasificación | Funcionalidades respaldadas | Situación actual |
|---|---|---|
| Ya implementadas | Registro, inicio de sesión JWT, perfil y recuperación de contraseña (RF-01); creación de eventos y cupos (RF-02.01 y RF-02.02); inscripción gratuita y de pago (RF-03.01 y RF-03.02); escaneo QR y registro manual (RF-04.03 y RF-04.04); verificación pública de certificados (RF-05.03); reporte de participación (RF-06.01). También hay evidencia de catálogos académicos, cancelación de inscripción, encuestas y exportación de reportes. | Implementadas según la matriz; las pruebas y aceptación no son completas para todos los casos. |
| Pendientes | Filtro de fecha del catálogo; lista de espera opcional; notificación de pago; QR firmado; credencial PDF confirmable; certificado PDF automático y masivo; porcentaje configurable de asistencia; filtros completos de reportes; métricas de rendimiento, capacidad, responsividad, accesibilidad y logging. | Parciales o no verificados según la matriz. |
| Mejora o corrección | Alinear los estados documentados de eventos con el código; vincular la aprobación de pago con la habilitación de credencial/QR y su notificación; aplicar las reglas de tiempo de asistencia, cierre/liberación e inmutabilidad de certificados cuando corresponda; ampliar la cobertura de pruebas. | Requieren corrección o validación porque la evidencia actual es parcial o inconsistente. |

## Sprints planificados

### Sprint 1 — Preparación técnica, seguridad y usuarios

- **Objetivo:** disponer de la base técnica y del acceso seguro para los actores del sistema.
- **Funcionalidades o historias relacionadas:** RF-01.01 a RF-01.04, RNF-02.01, RNF-02.02, RNF-04.01; CU-01.
- **Tareas principales:** preparar y verificar la estructura modular backend/frontend y PostgreSQL ya definida; integrar registro, autenticación JWT, perfil y recuperación de contraseña; comprobar BCrypt, expiración configurable de JWT y controles de acceso por rol.
- **Resultado esperado:** usuarios externos pueden registrarse, autenticarse, recuperar su contraseña y gestionar su perfil dentro de la arquitectura modular.
- **Estado actual:** **implementado**. Persisten actividades de prueba y verificación de seguridad; no se modifica la configuración de seguridad en esta planificación.

### Sprint 2 — Catálogos académicos y gestión de eventos

- **Objetivo:** habilitar la publicación y consulta de eventos con sus datos académicos y control de cupos.
- **Funcionalidades o historias relacionadas:** RF-02.01 a RF-02.04; HU-01, HU-02 y HU-03; CU-02 y CU-07.
- **Tareas principales:** gestionar facultades, carreras y categorías existentes; crear y administrar eventos, cupos y publicación; exponer el catálogo público por categoría; corregir o documentar la divergencia entre los estados de evento requeridos y los implementados; completar el filtro por fecha documentado.
- **Resultado esperado:** el coordinador administra eventos y cupos, y el participante consulta el catálogo por categoría y fecha conforme al requisito.
- **Estado actual:** **implementado parcialmente**. Creación, cupos y catálogo por categoría tienen evidencia; filtro por fecha y la alineación de estados son pendientes de corrección.

### Sprint 3 — Inscripciones y control de cupos

- **Objetivo:** permitir la inscripción a eventos gratuitos y preparar la inscripción de pago con sus reglas de negocio.
- **Funcionalidades o historias relacionadas:** RF-03.01, RF-03.02 y RF-03.04; RN-01 y RN-03; CU-03 y CU-04; HU-04.
- **Tareas principales:** confirmar inscripciones gratuitas, registrar inscripciones de pago como pendientes, impedir duplicados y administrar cupos; mantener la cancelación de inscripción ya evidenciada; evaluar la lista de espera únicamente si se aprueba como requisito opcional documentado.
- **Resultado esperado:** una inscripción queda confirmada o pendiente de pago según el tipo de evento, sin duplicidad y sin exceder cupos.
- **Estado actual:** **implementado** para inscripciones gratuitas, de pago, unicidad, cupos y cancelación. La lista de espera permanece **no verificada** y no se asume como entregable sin validación del requisito opcional.

### Sprint 4 — Pagos, credenciales y códigos QR

- **Objetivo:** completar el flujo posterior a la inscripción de pago y asegurar la credencial de acceso.
- **Funcionalidades o historias relacionadas:** RF-03.02 y RF-03.03; RF-04.01 y RF-04.02; RNF-02.03; RN-02; CU-05, CU-08, CU-12 y CU-13; HU-05, HU-06 y HU-07.
- **Tareas principales:** registrar comprobantes y validar o rechazar pagos; notificar al participante la aprobación o rechazo; habilitar credencial y QR solamente después de la aprobación; confirmar la descarga o representación de la credencial; firmar el contenido del QR para evitar generación fraudulenta.
- **Resultado esperado:** el validador procesa pagos pendientes y el participante recibe la comunicación y su credencial/QR únicamente cuando corresponde.
- **Estado actual:** **implementado parcialmente**. Carga y validación de pagos, credencial y QR tienen evidencia; faltan la notificación del pago, la vinculación completa con la habilitación tras aprobación, la firma del QR y la confirmación del formato PDF de la credencial.

### Sprint 5 — Control de acceso y asistencia

- **Objetivo:** registrar asistencia fiable mediante QR y por contingencia manual.
- **Funcionalidades o historias relacionadas:** RF-04.03 y RF-04.04; RN-04 y RN-05; CU-09; HU-08.
- **Tareas principales:** validar y consumir QR, registrar asistencia, buscar por CI o código de inscripción e impedir el reuso; comprobar la ventana de ingreso documentada desde 30 minutos antes del inicio hasta el final del evento.
- **Resultado esperado:** el personal de control registra una sola asistencia válida por participante y cuenta con una alternativa manual de identificación.
- **Estado actual:** **implementado** para escaneo, registro y búsqueda manual. La verificación de la tolerancia horaria documentada permanece como actividad de mejora o corrección.

### Sprint 6 — Certificación y validación pública

- **Objetivo:** emitir certificados conforme a la asistencia y habilitar su verificación pública.
- **Funcionalidades o historias relacionadas:** RF-05.01 a RF-05.03; RNF-01.02; RN-06, RN-07 y RN-08; CU-06, CU-10 y CU-14; HU-09 y HU-10.
- **Tareas principales:** finalizar y liberar la certificación del evento; calcular el porcentaje de asistencia configurable; generar certificados PDF de forma automática y masiva sin bloquear la interfaz; preservar la inmutabilidad, anulación y regeneración documentadas; mantener la verificación pública por código.
- **Resultado esperado:** solo participantes elegibles reciben un certificado PDF verificable tras el cierre y liberación del evento.
- **Estado actual:** **implementado parcialmente**. Existe generación de registro y verificación pública; están pendientes el PDF, la automatización masiva/asíncrona, el porcentaje requerido y la evidencia completa de las reglas de liberación e inmutabilidad.

### Sprint 7 — Encuestas, estadísticas y reportes

- **Objetivo:** consolidar la información posterior al evento para seguimiento institucional.
- **Funcionalidades o historias relacionadas:** RF-06.01 a RF-06.03; CU-11. Se incluye encuesta de satisfacción porque existe evidencia en el módulo `encuestas`, aunque no tiene RF, CU o HU explícito.
- **Tareas principales:** conservar y validar encuestas y estadísticas existentes; elaborar reportes de participación por facultad, pagos y asistencia; completar los filtros de reporte económico por evento y rango de fechas, y el reporte de asistencia con hora de registro; verificar las exportaciones PDF y Excel ya evidenciadas.
- **Resultado esperado:** administración dispone de estadísticas, reportes consultables y exportables, con el detalle exigido por los RF.
- **Estado actual:** **implementado parcialmente**. Participación, paneles, encuestas y exportaciones tienen evidencia; los filtros económicos y el reporte específico de asistencia requieren completarse o verificarse.

### Sprint 8 — Calidad, pruebas y preparación de liberación

- **Objetivo:** verificar los requisitos de calidad antes de una liberación del sistema web existente.
- **Funcionalidades o historias relacionadas:** RNF-01.01, RNF-03.01, RNF-03.02, RNF-04.02 y RNF-05.01; y las pruebas documentadas en `sprint_1_3_pruebas.md`, `sprint_2_eventos_pruebas.md`, `sprint_3_inscripciones_pruebas.md`, `sprint_6_pruebas.md`, `sprint_7_pruebas.md`, `sprint_8_pruebas.md`, además de las colecciones Postman existentes.
- **Tareas principales:** ejecutar y registrar pruebas funcionales e integrales de los flujos anteriores; medir el catálogo bajo 100 usuarios concurrentes y el objetivo de respuesta; verificar responsividad y accesibilidad WCAG 2.1 básica; comprobar logging de errores y capacidad de certificados; corregir únicamente hallazgos vinculados a los requisitos anteriores.
- **Resultado esperado:** evidencia verificable de calidad y una relación clara de hallazgos pendientes para la liberación.
- **Estado actual:** **pendiente de verificación**. Hay documentación de pruebas y una prueba frontend localizada, pero la evidencia de pruebas automatizadas, carga, accesibilidad, logging y capacidad es parcial o inexistente.

## Alcance explícitamente no planificado

- **Despliegue productivo:** no se programa como sprint de implementación porque la documentación revisada no aporta requisitos formales de despliegue productivo, SLA, RPO/RTO o CI/CD. Solo se contempla la preparación de evidencia de calidad del Sprint 8.
- **Aplicación móvil Flutter:** no se programa. `DOCUMENTACION_PROYECTO_TESIS_APP/19_ANALISIS_APP_MOVIL.md` confirma que no existe proyecto móvil ni requisitos móviles aprobados; Flutter, notificaciones push, modo offline y sus servicios son propuestas futuras, no funcionalidades vigentes.

## Uso como evidencia metodológica

Cada sprint debe registrar posteriormente sus pruebas, hallazgos y cambios aprobados sin alterar esta línea base de alcance. La ejecución debe priorizar los pendientes y correcciones identificados por la matriz, manteniendo la distinción entre evidencia de implementación y evidencia de validación.
