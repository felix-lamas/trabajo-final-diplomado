# E3.3 — Reconciliación académica final: Monografía ↔ Sistema

**Tipo:** auditoría estática de coherencia y propuesta de decisiones. No se modificaron código, monografía ni configuración; no se ejecutaron pruebas en esta fase. El único archivo creado es este informe. La implementación se contrastó en `fc9ce15` (`main`). “Existe en código” no equivale a “se probó en producción”.

## 1. Fuente y criterio de reconciliación

La fuente académica de referencia es `docs/monografia/Lamas-monografia-F.docx`, suministrada como versión actual. Contiene el problema, objetivos, RF-01…RF-13, criterios de aceptación, prioridades, RNF-01…RNF-05 y la arquitectura prevista. No declara una etiqueta editorial “final”; conserva marcadores de plantilla y pruebas/despliegue pendientes. Se la trata como norma para esta revisión, no como texto ya listo para entregar.

Se consultaron también `docs/auditoria/E3_2_MATRIZ_NORMATIVA.md`, la estructura y servicios actuales de `backend/`, `frontend/` y `flutter/VidiaApp/`, `docs/07_Requerimientos_Funcionales.md`, `docs/08_Requerimientos_No_Funcionales.md`, `docs/12_Matriz_de_Trazabilidad.md`, `docs/plan_maestro/`, `docs/release_1_0/` y documentación de instalación/alcance. Los auxiliares no sustituyen la monografía: se usan para detectar inconsistencias. Los contenidos de los PDF P3/P4/formato institucional no se vuelven a atribuir aquí como examinados; E3.2 dejó registrada la limitación de extracción de esos archivos.

Prioridad de interpretación aplicada: monografía actual → lineamientos académicos legibles y complementos aprobados → planes y documentos técnicos auxiliares → código para determinar implementación real. Si una funcionalidad deseada no existe en el cliente/backend, se señala como brecha; no se rebaja su requisito para hacerla parecer cumplida.

## 2. Problema, contexto y formulación

La monografía identifica una dispersión de información y variedad de mecanismos para difundir eventos, inscribir participantes, registrar asistencia y consultar certificación. El problema central es la dificultad de seguimiento integral de la participación universitaria por falta de centralización e integración. La propuesta cubre actividades UAJMS gratuitas y pagadas, desde consulta/inscripción hasta asistencia y certificación.

**Evaluación: COHERENTE, con implementación parcial del resultado integral.** El backend integra esos dominios en una API REST y modelo común; Angular ya proporciona catálogo, inscripción/pagos, gestión de asistencia y certificados; el portal de verificación es público. Flutter, que la arquitectura académica asigna al participante, solo cubre autenticación de acceso, catálogo/detalle e inscripción/mis inscripciones; el propio detalle móvil declara que el flujo de pagos llegará posteriormente y no se encontraron captura QR/GPS ni consulta móvil de certificados. Por ello el sistema construido apunta al problema, pero todavía no demuestra en el canal móvil los procesos centrales de asistencia y certificación planteados.

La base del problema está formulada desde observación del autor y anuncia una encuesta exploratoria; la monografía aún dice que sus resultados se añadirán luego. No se debe presentar como resultado de investigación una percepción que aún no tenga instrumento, muestra, respuestas y análisis incorporados. Completar esa evidencia sin cambiar artificialmente la formulación.

## 3. Justificación

La justificación técnica (Spring Boot/Java, PostgreSQL, Angular y Flutter), la pertinencia institucional y el impacto esperado de centralización son compatibles con el sistema y con su dirección. Mantener como expectativa —no como resultado medido— la reducción de tareas manuales y mejor seguimiento.

Debe actualizarse para reflejar lo que existe: API REST común bajo `/api/v1`; backend monolítico modular; Spring Security/JWT; PostgreSQL/JPA; cliente web Angular; cliente móvil Flutter; SMTP/Brevo para correo; Supabase Storage S3-compatible para objetos privados de comprobantes y QR de pago. La base relacional sigue siendo PostgreSQL; Supabase Auth no se utiliza. El QR de pago es una imagen cargada por el organizador y servida por Vidia, diferente del comprobante privado del participante y del token QR temporal de asistencia. `Evento.imagenPortada` continúa siendo una URL, no una carga a Supabase.

Hay dos límites que la justificación/documentación no debe disimular: no hay evidencia de despliegue productivo y las funciones móviles principales siguen incompletas. Además, existen valores/estadísticas placeholder en DTO de dashboard (p. ej. satisfacción/encuestas y QR utilizados devueltos como cero); no son inteligencia de negocio real y no deben justificarse ni describirse como resultados. Las pantallas Angular revisadas no los presentan como indicadores.

## 4. Objetivos

### Objetivo general

El objetivo general de la monografía —desarrollar una plataforma web y móvil UAJMS para centralizar información y gestionar inscripción, pago, asistencia y certificación— es **COHERENTE y debe CONSERVARSE**. El producto está diseñado con esos canales y el backend contiene esas reglas; su grado de cumplimiento final es **PARCIAL** hasta terminar Flutter y obtener evidencia de despliegue/pruebas. No conviene cambiar el objetivo a “web solamente” para acomodarlo al estado incompleto del móvil: hacerlo contradiría la arquitectura y objetivos específicos actuales.

### Objetivos específicos

| Objetivo | Evidencia real | Estado | Acción recomendada |
|---|---|---|---|
| 1. Analizar procesos y requerimientos de administradores, organizadores y participantes. | Monografía contiene actores, RF/CA, casos de uso y contexto; la encuesta exploratoria sigue anunciada como pendiente. | PARCIAL | Conservar; completar evidencia del análisis (instrumento, participantes, resultados y cómo informaron requisitos) o precisar que la observación es cualitativa si esa fue la metodología aprobada. |
| 2. Diseñar arquitectura, modelo de datos, interfaces y contratos para integrar web, móvil y backend. | Arquitectura ejecutada: monolito modular Spring Boot por módulos, Angular, Flutter y API REST. DTO/OpenAPI y entidades reales existen. El modelo declarado “14 entidades/tablas” discrepa del código actual. Hay diagramas/documentos auxiliares históricos. | PARCIAL | Conservar; actualizar diagramas, arquitectura de canales/integraciones, modelo y API con estado actual, distinguir entidad de dominio y entidad técnica. |
| 3. Implementar gestión de eventos, inscripción, pagos, asistencia QR y geolocalización, certificación. | Backend implementa todos esos dominios; Angular cubre gestión web; Flutter no tiene pagos/comprobantes, escaneo QR/GPS ni certificados y reconoce pendiente de pagos en `event_detail_screen.dart`. | PARCIAL | Conservar y cerrar brechas móviles. No declarar completo por la existencia de endpoints backend. |
| 4. Aplicar autenticación, autorización y protección de información por rol. | BCrypt, JWT, tres roles, anotaciones/autorización de servidor, ownership, sesiones de usuario y revocación aparecen en backend y sus tests. El texto de 2.7.1 dice lo contrario sobre revocación. | COHERENTE en implementación; documentación parcial | Conservar; corregir sección de sesiones conforme al mecanismo real y describir sus límites. |
| 5. Validar con pruebas funcionales y no funcionales. | A E3.2: backend 460 tests, 0 fallidos, 1 omitido; Angular 271/271 PASS; Flutter test se interrumpió sin resultado. No hay evidencia satisfactoria de pruebas RNF de carga, usabilidad, compatibilidad o uptime. | PARCIAL | Conservar; ejecutar casos de aceptación y protocolos RNF; documentar datos/entorno, resultados y artefactos sin secretos. |
| 6. Desplegar y documentar la plataforma web y móvil para instalación, configuración, uso y mantenimiento. | Configuración y documentación existen; frontend/backend/productivo, conexiones externas, APK/AAB y disponibilidad actual no fueron verificados. | PARCIAL | Conservar; completar y probar despliegue web/API, distribución Flutter, manuales y resultados con URLs/versión verificadas. |

**Conclusión sobre objetivos:** ninguno debe eliminarse. El tercero requiere trabajo de producto, no solo ajuste verbal; el segundo y sexto requieren reconciliar evidencias y configuración real. Las conclusiones del Capítulo 3 deben redactarse después de ejecutar esas evidencias y expresar el grado verdadero de cumplimiento.

## 5. RF de la monografía: evaluación y decisión

Se conservan las prioridades normativas halladas en E3.2: **13 RF; 7 MUST (53,85 %), 4 SHOULD y 2 COULD**. No se renumeran ni se toma la lista incompatible de `docs/plan_maestro/03_requisitos.md` como autoridad. La columna Flutter indica implementación comprobada estáticamente en el árbol actual, no potencial del API.

| RF | Monografía (resumen fiel) | Backend | Angular | Flutter | Estado integral actual | Decisión recomendada |
|---|---|---|---|---|---|---|
| RF-01 MUST | Registro e inicio de sesión | Implementado: registro, login, verificación de correo, recuperación/reset, perfil y sesiones. | Registro, login, verificación, recuperación/reset y sesión. | Parcial: login/sesión; no se localizaron registro/verificación/reset. | Parcial multiplataforma | **AMPLIAR** criterios para mencionar verificación/recuperación como flujos de acceso; completar móvil si se espera autonomía de alta. La cuenta puede registrarse hoy por web y autenticarse en móvil, pero debe declararse esa frontera. |
| RF-02 SHOULD | Solicitar condición de organizador | Solicitud y decisión admin, roles servidor. | Solicitud usuario; listado/aprobación/rechazo admin. | No aplica al cliente participante actual; operación administrativa web. | Implementado web/backend | **CONSERVAR**; reemplazar nomenclatura antigua de actores por Administrador, Organizador y Participante (rol técnico USUARIO). |
| RF-03 MUST | Crear/gestionar eventos y enviarlos a revisión | Estados y transiciones, ownership, imagen QR de pago almacenada por backend. | Formularios, gestión de evento y carga/reemplazo/eliminación del QR como operación separada del JSON. | Gestión administrativa no aplica al cliente móvil actual. | Implementado web/backend; no productivamente verificado | **AMPLIAR** criterios con modalidad/estado y configuración opcional de imagen QR para eventos pagados. QR no es obligatorio si negocio actual no lo exige. |
| RF-04 MUST | Revisar, aprobar o rechazar eventos | Admin revisa EN_REVISION, publica/rechaza y decisiones de ciclo de vida. | UI administrativa. | No aplica. | Implementado web/backend | **CONSERVAR**; usar estado real `EN_REVISION` y transiciones implementadas, no estados antiguos como `PENDIENTE`. |
| RF-05 MUST | Consultar/buscar eventos publicados, filtros, detalle público | Listado/búsqueda/detalle público; el resumen no necesita incluir imagen privada del QR. | Catálogo, filtros y detalle; presenta información/instrucciones/QR de pago a través de URL controlada por backend. | Catálogo y detalle presentes; verificar la representación actual del QR en el modelo móvil (no se encontró flujo de pago). | Implementado web; móvil parcial | **AMPLIAR** CA para mostrar información de pago/QR real cuando exista y evento sea de pago; no incorporar fecha/filtro no soportado como resultado actual. |
| RF-06 MUST | Inscripción: unicidad, disponibilidad/cupo y consulta propia | Gratis y pagado, duplicados/cupo/estados/ownership. | Inscripción, mis inscripciones, cancelación/comprobante según backend. | Inscripción y mis inscripciones implementadas; pantallas/repositorios presentes. | Implementado en código, producción no verificada | **CONSERVAR**; explicar estados reales y la diferencia entre inscripción gratuita confirmada y pago pendiente. |
| RF-07 MUST | Registro/carga de pago, comprobante y validación/rechazo | Flujo externo, pago/comprobante privado, reemplazo, validación/rechazo admin u organizador de su evento, estados del backend. | Carga Blob/FormData, mis pagos y revisión; no checkout/gateway. | **Pendiente**: la pantalla de detalle informa que el pago llegará después; no hay repositorio/modelo/pantalla de pagos identificados. | Parcial; MUST incompleto en móvil | **AMPLIAR** criterios para explícitamente pago externo, QR/instrucciones del evento, carga/reemplazo de comprobante y revisión con estados reales. Implementar cliente Flutter. No gateway. |
| RF-08 MUST | Registro de asistencia QR + geolocalización; rechazos y no duplicidad | Sesión y QR temporal; valida inscripción/ventana/GPS/radio/duplicado; se registra desde API. | Consulta/gestión/listados del organizador, sin captura GPS de participante. | **Pendiente**: no camera/scanner, geolocation, request/model/repository de asistencia ni pruebas móviles localizadas. | Parcial; MUST incompleto en móvil | **CONSERVAR**; implementar flujo participante Flutter con permisos, escaneo, ubicación y errores del backend. Precisar que QR es temporal por sesión y puede servir a varios inscritos: no afirmar “un solo uso global” si el código no lo establece. |
| RF-09 SHOULD | Consultar/generar certificados elegibles y descargar | Emisión autorizada según evento finalizado, inscripción confirmada, pago aprobado cuando aplica y regla de asistencia; PDF se produce bajo demanda/descarga, código público. | Mis certificados, listado de evento, emisión y descarga/verificación en vistas autorizadas. | **Pendiente**: no modelo/repo/pantalla de certificado localizada. | Parcial en canal móvil | **REFORMULAR** título a “Generar y consultar certificados de participación” y criterios a emisión autorizada/descarga bajo demanda. Incorporar la regla curricular real (80 %) y tipo/horas del evento; no llamarlo emisión automática al cerrar evento. Implementar Flutter. |
| RF-10 SHOULD | Consultar historial de participación: inscripciones, pagos, asistencias, certificados | Endpoints separados `mis-*`; no existe un agregado único demostrado. | Inscripciones, pagos, certificados son módulos/vistas distintas. | Solo “Mis inscripciones”; pagos/asistencia/certificados ausentes. | Parcial | **REFORMULAR** para dejar claro que es consulta del historial por dominios/vistas, no un endpoint único ni una estadística agregada. Completar móvil para sus elementos incluidos. |
| RF-11 SHOULD | Gestionar categorías | CRUD y restricciones admin. | CRUD admin. | No aplica. | Implementado web/backend | **CONSERVAR**; alinear actor al rol ADMINISTRADOR. |
| RF-12 COULD | Dashboards, consultas y exportaciones | Métricas por alcance y reportes JSON; existen generadores PDF/XLSX. Algunas filas/campos de áreas/carreras no se rellenan; varias métricas se devuelven como listas vacías o ceros. | Dashboards y reportes/exportación. | No aplica según arquitectura participante. | Parcial respecto a todas las afirmaciones posibles; reportes operativos reales implementados | **REFORMULAR** para enumerar solo métricas/dimensiones efectivamente calculadas y exportaciones PDF/XLSX reales; no afirmar participación por facultad/carrera/encuestas si no se obtiene. Mantener COULD. |
| RF-13 COULD | Verificación pública de certificado por código, sin login | Endpoint público real de verificación; DTO público acotado. | Ruta pública sin guard. | No aplica. | Implementado en código; producción pendiente | **CONSERVAR**; documentar los datos públicos expuestos y distinguir consulta pública de cuenta/rol. |

**Decisión de estructura:** no recomiendo crear un RF independiente para QR de pago por evento en este corte. El flujo ya se integra naturalmente como CA de RF-03 (organizador configura/reemplaza) y RF-05/RF-07 (participante lo consulta y paga externamente). Añadir un RF-14 duplicaría el proceso salvo que el tutor exija seguimiento separado; si se decide separarlo, conservar IDs existentes y agregarlo con prioridad acordada, nunca renumerar en silencio.

**Trabajo que permanece:** RF-07 y RF-08 son MUST e implementados en backend/web en distinta medida, pero no completos en móvil según arquitectura académica; RF-09 SHOULD y RF-10 SHOULD también están incompletos en móvil. RF-01 es parcial en Flutter. No marcar la plataforma web+móvil completamente terminada antes de decidir y cerrar estas brechas.

## 6. Funcionalidades reales del sistema y tratamiento documental

| Funcionalidad encontrada | Estado/documentación actual | Decisión para versión final |
|---|---|---|
| Verificación de correo, recuperación y restablecimiento | Código backend/Angular; no desarrollados como flujos separados en el resumen de RF | **Agregar** a 2.6/2.7 y asociar como criterios de RF-01 o soporte de seguridad. No inventar RF autónomo si no es necesario. |
| Perfil, cambio de contraseña y revocación/sesión única | Endpoints/servicios existentes; `SesionUsuarioServicio` revoca sesiones activas y el logout revoca sesión | **Documentar** en 2.6/2.7. Corregir afirmación de revocación ausente en 2.7.1. Explicar exactamente el modelo de sesión y límites de JWT. |
| Solicitud/aprobación/rechazo de organizador | Backend y Angular | **Documentar** con flujo y estados reales, mapear bajo tres roles definitivos. |
| Ciclo de eventos y QR de pago | Gestión/revisión/publicación; QR real de imagen, Supabase privado, endpoint Vidia; eventos mantienen portada como URL | **Agregar** QR pago a diseño/implementación; distinguir de portada, QR asistencia y comprobante. No documentar `qrPagoUrl` como almacenamiento externo directo si ahora la respuesta apunta al endpoint backend. |
| Pago externo, carga/revisión de comprobante | Implementado backend/Angular; Flutter incompleto | **Conservar/ampliar** RF-07; explicitar que no hay gateway ni tarjeta/checkout. Documentar almacenamiento privado. |
| Sesiones, QR temporal y geolocalización | Backend organiza sesión y valida token de QR, inscripción, ventana temporal, coordenadas, precisión/radio y duplicados. Angular de organizador consulta/gestiona; captura participante desde móvil falta. | **Documentar** mecanismo real y **completar Flutter**. No describir escaneo web del participante ni QR “one-use global”. No confundir QR de pago con QR asistencia. |
| Certificación curricular/no curricular, PDF, código y verificación pública | Backend calcula elegibilidad; generación por operación autorizada; descarga genera PDF; verificación pública; Angular integrado | **Documentar** que no es automáticamente emitido al final del evento. Detallar 80 % curricular como regla de implementación actual y no promesa de entrega automática. Completar consulta móvil. |
| Dashboards/reportes/PDF/XLSX | Código y Angular generan reportes; hay campos DTO con ceros/listas vacías | **Documentar** solo datos calculados y exports que fueron comprobados; **desconectar o implementar de forma real** placeholders de satisfacción/encuestas y contador QR antes de exponerlos como métricas. No decir “encuestas” como módulo activo. |
| QR público de verificación de certificados | Ruta pública de Angular y API abierta | **Documentar** tercero verificador como actor externo/público, no como rol adicional. |
| Brevo y Supabase Storage | Configuración/código presentes; SMTP real local reportado probado en fase anterior; Supabase smoke test automatizado omitido y producción no verificada (E3.2). | **Actualizar** arquitectura y configuración con nombres de proveedores y límites. Separar “integrado/configurado” de “probado en producción”. |
| Encuestas de satisfacción | No hay módulo/controller de producción actual; el test arquitectónico comprueba clases antiguas ausentes. Quedan DTO placeholders y documentos/colección Postman históricos. | **No documentar como feature actual.** Recomendar retirar/desconectar placeholders y etiquetar/archivear documentos y colección legacy. Solo reintroducir si se aprueba alcance y se implementa de extremo a extremo. |
| Credenciales digitales permanentes, ControlAcceso, Facultades/Carreras como módulos, roles legacy | No existen en módulos fuente actuales; `ModeloConceptualOficialTest` verifica explícitamente ausencia de varias entidades retiradas. | **Excluir** de arquitectura/modelo/APIs actuales. Mantener esa prueba de ausencia; marcar manuales, diagramas y Postman antiguos como históricos/sustituidos antes de publicación. |
| Portada de evento | `imagenPortada` continúa URL, no objeto subido mediante el nuevo flujo QR | **Documentar** como URL si se incluye; no afirmar almacenamiento Supabase ni upload de portadas. |

## 7. Actores y roles finales

| Actor funcional | Rol técnico | Uso actual |
|---|---|---|
| Administrador | `ADMINISTRADOR` | Usuarios/solicitudes, categorías, revisión global de eventos y operaciones globales permitidas de pagos, asistencia, certificados y reportes. |
| Organizador | `ORGANIZADOR` | Eventos propios, sesiones, consulta de inscritos/asistencia y validación de pagos/certificados dentro de su alcance. |
| Participante | `USUARIO` | Consulta pública; inscripción, pagos/comprobantes, asistencia y certificados propios. “Participante” es el nombre académico de `USUARIO`, no un cuarto rol. |
| Verificador externo | Sin rol/cuenta requerida para verificación pública | Actor secundario de RF-13; solo recibe los campos que el DTO público expone. No es rol de seguridad. |

Los nombres “Super Administrador”, “Coordinador de Facultad/Carrera”, “Validador Financiero”, “Registrador de Asistencia”, `ESTUDIANTE`, `PARTICIPANTE_EXTERNO` y `PERSONAL_CONTROL` aparecen en documentos viejos/auxiliares, pero no son roles funcionales finales. Interno/externo puede ser clasificación de usuario, no autoridad. Actualizar tablas de actores, casos de uso, diagramas, glosario, permisos y manuales para que acciones financieras tengan los ámbitos ADMIN global / ORGANIZADOR propietario descritos por código.

## 8. Arquitectura final propuesta

**Conservar y actualizar**, no reemplazar:

- Monolito modular por dominios en una aplicación Spring Boot 3.3 / Java 21. Módulos vigentes: usuarios, categorías, eventos, inscripciones, pagos, sesiones, asistencias, certificados y reportes; además `core`/`comun` transversales.
- API REST `/api/v1`, JSON para recursos y multipart/binario solo en operaciones de archivo; Angular 21 para administración/organización y flujos web públicos/participante; Flutter para participante; JWT/Spring Security controla el acceso en backend.
- PostgreSQL persiste datos de negocio y metadatos; Flyway está habilitado, pero solo se localizó `V1__agregar_qr_pago_storage_key_eventos.sql`; Hibernate aún tiene `ddl-auto:update` como valor por defecto. La monografía no debe describir un historial completo de migraciones ni esquema productivo validado hasta comprobarlo y definir la estrategia real.
- SMTP/Brevo envía correo transaccional. Supabase Storage por S3 guarda archivos privados, servido/autorizado por Spring; no reemplaza PostgreSQL, JWT/Spring Security ni las reglas. Comprobantes y QR de pago comparten adapter/servicio con namespaces separados; bucket no público. No hay upload de portada actualmente.
- PDFs de certificados se generan backend al descargar y los reportes disponen de salida PDF/XLSX. No describir exportaciones o dimensiones que no se calculen.
- Render/dominio/DB se consideran despliegue solo después de smoke test fechado del commit y evidencia de URL, salud, CORS, PostgreSQL/migración, correo y Storage. La configuración presente es CONFIGURADA, no prueba de ejecución.

### Modelo de datos

Conservar las ocho entidades conceptuales principales: `Usuario`, `CategoriaEvento`, `Evento`, `Inscripcion`, `Pago`, `SesionEvento`, `Asistencia`, `Certificado`. Añadir/explicar estructuras técnicas necesarias: `Rol`, `Permiso`, `UsuarioRol`, `RolPermiso`, `SesionUsuario`, `TokenVerificacionCorreo`, `TokenRecuperacion`, `QrAsistenciaTemporal`. En el backend se encontraron 16 clases `@Entity` (ocho de dominio anteriores más ocho de soporte/técnicas); eso no demuestra por sí solo que la base desplegada tenga exactamente 16 tablas ni resuelve diferencias de esquema. La monografía afirma correspondencia de 14 entidades/14 tablas; reconciliarla con el inventario efectivo y el esquema real antes de describir conteos. El QR temporal puede documentarse como entidad técnica persistida; no como entidad del dominio central si el diagrama conceptual busca abstraerla.

La relación QR pago/evento se apoya en referencia privada `qr_pago_storage_key`; no exponer la clave Supabase en DTO. `qrPagoUrl` en DTO es una URL controlada por Vidia para GET de imagen. Pago conserva metadatos/referencia del comprobante. Precisar esos campos desde entidad/esquema vigente al actualizar diccionario.

## 9. API y documentación de contratos

La monografía enumera la API de forma general; deben actualizarse 2.4.1/2.4.4 y anexo OpenAPI a operaciones vigentes, sin copiar listas viejas de `docs/plan_maestro/08_api.md`.

Operaciones representativas que deben quedar trazables por RF: `/api/v1/auth/*`; `/usuarios/perfil`, solicitudes de organizador; `/eventos/publicados`, `/eventos/publicados/buscar`, detalle y CRUD/revisión; `PUT/DELETE/GET /eventos/{id}/qr-pago`; `/inscripciones` y `/mis-inscripciones`; `/pagos`, `/mis-pagos`, comprobante, pendientes/validación/rechazo; `/eventos/{eventoId}/sesiones`, `/sesiones/{id}/qr`, `/asistencias`, `/mis-asistencias`; `/certificados/generar/{inscripcionId}`, `/mis-certificados`, descarga y `/certificados/verificar/{codigo}`; dashboards y reportes/export PDF/XLSX. Los métodos, parámetros, códigos, seguridad y DTO se deben copiar de OpenAPI actual, no inventar mediante esta lista resumida.

Actualizar/eliminar referencias legacy a `/api` sin versión, `codigos-qr`, credenciales, control de acceso antiguo, facultades/carreras, encuestas, `/verificacion-certificados` si contradicen el controlador vigente. `docs/12_Matriz_de_Trazabilidad.md`, casos de uso, Postman y release docs deben cotejarse uno por uno.

## 10. Seguridad: reconciliación

| Afirmación/control | Implementación observada | Acción documental |
|---|---|---|
| BCrypt | Configuración PasswordEncoder/autenticación y tests backend del módulo usuario. | Conservar y citar mecanismo. |
| JWT con vencimiento | Configurable en propiedades/env; protección por filtro. | Conservar; documentar vencimiento configurado sin afirmar valor productivo no verificado. |
| Revocación de sesión | `SesionUsuarioServicio` crea sesión única, revoca anteriores y logout/revocación; JWT incluye sesión validada en servidor según diseño actual. | Corregir 2.7.1 que dice que no existe revocación. Describir alcance exacto, sin llamar “stateless puro” al comportamiento real. |
| Roles y autorización | Solo `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO`; Spring Security, `@PreAuthorize`, scope/ownership en servicios. Público de certificado limitado. | Conservar; incluir matriz por operación, admin global solo donde código lo concede, organizer por evento propio. |
| 401/403/404 | 401 autenticación ausente/inválida, 403 rol insuficiente; recursos fuera de scope pueden ocultarse como 404. | Describir diferencias con ejemplos reales/ejecutados; no afirmar que todo IDOR devuelve 403. |
| Tokens de email/reset y contraseñas | Tokens aleatorios/hash/expiración/uso único según E3.2; correo por SMTP externo; reset con PasswordEncoder. | Añadir flujo. No incluir token ni URL tokenizada en capturas/documento. La carrera concurrente de consumo reset quedó como deuda técnica en auditoría previa. |
| Archivos | Comprobante y QR pago privados; validar autorización antes de Storage; nombres aleatorios; Spring intermedia; Tika/imagen. | Explicar bucket privado, scopes y no exposición de key/credenciales. Diferenciar archivo recibo del QR del organizador. |
| Comunicaciones | HTTPS requerido en producción en diseño; entorno local HTTP. HTTPS productivo no verificado en esta auditoría. | Marcar requisito y evidencia pendiente; no decir cifrado productivo comprobado. |
| Secretos | Env para JWT, DB, SMTP y storage; E3.2 encontró credenciales de demo en el DOCX. | Retirar/anonimizar secretos del documento y valorar rotación de cuentas si siguen activas; verificar en repo/historial con procedimiento seguro. Nunca reproducir valores. |
| QR asistencia | Token temporal/hash, ventana por sesión y geolocalización validada servidor; coordenadas del participante no se persisten según servicio observado. | Documentar tratamiento de ubicación, retención y errores; no afirmar QR firmado criptográficamente sin fuente específica. |

## 11. RNF finales y coherencia

Los cinco RNF de la monografía se conservan; son demostrables, pero algunas métricas requieren protocolo operacional. No reducir umbrales para que el resultado parezca alcanzado.

| RNF | Métrica normativa actual (E3.2) | Coherencia/estado | Acción para versión final |
|---|---|---|---|
| RNF-01 Rendimiento | ≥95 % de solicitudes principales en ≤3 s con 20 usuarios concurrentes. | Medible; no hay reporte de carga. Build inicial Angular reportó 889,33 kB sobre presupuesto 500 kB en E3.2, pero no demuestra incumplir tiempo. | Conservar literalmente; definir endpoints/dataset, herramienta, entorno y duración; ejecutar prueba y anexar resultado. |
| RNF-02 Seguridad | 100 % de protegidos rechazan sin auth/ inválida; operaciones restringidas rechazan rol insuficiente. | Coherente; mecanismos/tests backend existen, cobertura exhaustiva/productiva no está probada. | Conservar; inventario endpoint×rol y pruebas 401/403 con evidencia anonimizada. |
| RNF-03 Usabilidad | ≥80 % de usuarios de prueba completan tareas principales sin asistencia. | Medible pero protocolo/participantes no existen en evidencia revisada. | Conservar; protocolo de tareas, muestra, denominador y resultados. |
| RNF-04 Compatibilidad | Chrome/Edge/Firefox estables y Android 12+. | Demostrable; no hay matriz actualizada ejecutada. | Conservar; registrar versión de navegador/dispositivo y flows. Aplicar Android ≥12 a Flutter instalado. |
| RNF-05 Disponibilidad | ≥95 % durante período de prueba, excluido mantenimiento programado. | Medible una vez fijados período y sonda; no hay uptime productivo localizado. | Conservar; definir ventana y monitoreo reproducible. |

**Contradicción auxiliar importante:** `docs/03_Objetivos_Especificos.md` promete 99,5 % de disponibilidad, frente al RNF-05 del documento normativo con 95 %. Mantener la norma actual en este informe; solicitar aprobación del tutor para resolver la diferencia y alinear el auxiliar, no cambiar la métrica por conveniencia. El objetivo específico auxiliar “Aseguramiento tecnológico” tampoco corresponde a los seis objetivos de la monografía actual.

## 12. Pruebas, resultados y evidencia requerida

Resultados conocidos del checkpoint E3.2, en HEAD `fc9ce15` (no repetidos en E3.3): backend `mvn test`: 460 tests, 0 failures, 0 errors, 1 skipped; Angular `npm test -- --watch=false`: 271/271 PASS; Angular build PASS con warnings de presupuesto; Flutter `flutter test --no-pub --reporter expanded` fue interrumpido sin resultado concluyente. El smoke test de Supabase estaba omitido/deshabilitado. Estos son resultados locales de esa auditoría, no ejecución productiva ni verificación visual de cada flujo.

La evidencia final debe tener relación explícita `RF/CA/RNF → pasos → resultado esperado/obtenido → artefacto`. Priorizar: cada CA feliz/error de RF MUST; 401 sin token; 403 token de rol insuficiente; errores de validación, duplicidad, cupo, archivo y estado; más RNF-01…RNF-05. Para RF-07 y RF-08 añadir E2E real web/móvil con cuentas y datos autorizados de prueba. Para certificado, verificar evento finalizado/condiciones y URL pública. Automatización unit/integración no sustituye ejecución de aceptación ni producción.

## 13. Alcance final y fuera de alcance

La monografía actual describe producto web+móvil. `docs/05_Fuera_de_Alcance.md` afirma “Aplicación Móvil Nativa: No se desarrollarán apps Android o iOS” y plantea escaneo web móvil; esto contradice objetivo general y arquitectura de la monografía y debe **reformularse**. El QR/GPS del participante debe resolverse en Flutter, no trasladarse a Angular solo para evitar terminar la aplicación móvil.

Conservar fuera de alcance si tutor/problema lo aprueba: gateway de pagos, tarjeta/checkout, facturación electrónica, blockchain, reconocimiento facial, LMS, streaming/videollamadas, compras/viáticos y hardware industrial. El código observado no implementa un gateway ni LMS. No describirlos como capacidad existente.

La aplicación móvil de organizadores/administradores no está contemplada en arquitectura: Flutter es cliente participante; los workflows de gestión continúan web. No afirmar integración con SIA/finanzas: en visión aparece como meta futura.

## 14. Propuesta de sistema final

### Actores

Administrador, Organizador y Participante (`USUARIO` en JWT); tercero verificador público sin cuenta para RF-13. No se crean autoridades nuevas por tipo interno/externo ni por función financiera.

### Canales

- **Web Angular:** catálogo/detalle/inscripción pública; autenticación y espacio participante; administración y organización según roles. Esto extiende sin contradicción el alcance funcional de usuario a la web que ya existe.
- **Móvil Flutter:** canal de participante para catálogo, detalle, autenticación, inscripción e historial; debe completarse con pagos/comprobante, escaneo QR/GPS y certificados para corresponder al diseño académico.
- **Backend REST:** única autoridad de estados, reglas, ownership, elegibilidad y permisos.

### Funcionalidades principales

Autenticación/verificación/reset; promoción de usuario a organizador por solicitud aprobada; ciclo de evento borrador/revisión/publicación/finalización/cancelación; búsqueda y detalle; inscripción gratuita y pagada; consulta externa de pago mediante instrucciones/QR del organizador; presentación y revisión manual de comprobantes; sesiones y QR asistencia temporal con validación GPS en servidor; emisión elegible y descarga de certificados; verificación pública; dashboards/reportes acotados por rol. Presentar solo métricas reales.

### Arquitectura e integraciones

Monolito modular Java 21/Spring Boot, Angular 21, Flutter 3.44.8, PostgreSQL/JPA, API REST `/api/v1`; Brevo por SMTP para correo; Supabase Storage privado S3 para comprobantes/QR pagos, accedido exclusivamente por backend. Portada de evento sigue URL HTTP(S). TLS, CORS, variables y deploy deben confirmarse en URL real.

### Entidades y seguridad

Ocho entidades de dominio (`Usuario`, `CategoriaEvento`, `Evento`, `Inscripcion`, `Pago`, `SesionEvento`, `Asistencia`, `Certificado`) y estructuras de soporte para RBAC, sesiones/tokens y QR temporal. Los datos y ownership quedan en backend; no delegar auth ni reglas a Supabase o clientes.

### Pagos

Sin gateway: evento pagado → participante paga externamente con datos/QR opcional del organizador → envía comprobante privado → revisor autorizado acepta/rechaza → backend actualiza estados. La imagen QR del organizador no es el recibo del participante.

### Asistencia

Sesión de evento; organizador emite/rota QR temporal; participante Flutter escanea, solicita ubicación y envía datos; backend valida autenticación, inscripción, sesión/ventana, QR, precisión/radio y duplicidad. En Angular se administra/consulta, no se simula GPS participante.

### Certificados

Backend evalúa condiciones; organizador/admin autorizado inicia generación bajo las reglas existentes; PDF se entrega al titular/roles permitidos. Curricular: regla de asistencia codificada (80 %); no curricular: regla de sesiones requeridas del backend. Código verificable por ruta pública devuelve solo datos públicos. No afirmar automático al finalizar ni elegibilidad calculada en Flutter.

### Reportes y verificación pública

Reportes y PDF/XLSX con datos/alcance realmente soportados; no satisfacción de encuestas ni contador QR si no hay cálculo real. Verificación por código pública, independiente de login y sin información personal no permitida.

## 15. Propuesta de RF final

La recomendación es **mantener 13 IDs y prioridades**, ajustar descripciones/CA sin reescribir IDs y completar Flutter. La estructura actual abarca el dominio; lo que cambia es hacer verificables los flujos verdaderos y añadir QR de pago como aceptación de RF existentes.

| ID actual | Decisión | ID propuesto | Nombre final recomendado | Justificación |
|---|---|---|---|---|
| RF-01 | AMPLIAR | RF-01 | Registro, autenticación y recuperación segura de acceso | Captura flujo real email-verification/reset/sesión; mantiene MUST. |
| RF-02 | CONSERVAR | RF-02 | Solicitar condición de organizador | Alinea el flujo user→admin→organizer actual. |
| RF-03 | AMPLIAR | RF-03 | Crear, gestionar y enviar eventos a revisión | Agregar carga/reemplazo de QR de pago opcional y transición/ownership exactos. |
| RF-04 | CONSERVAR | RF-04 | Revisar y gestionar eventos enviados a revisión | Estado/transiciones respaldadas y rol admin. |
| RF-05 | AMPLIAR | RF-05 | Consultar, buscar y ver detalle de eventos publicados | Añadir información real de pago/QR cuando esté presente, sin QR para gratuitos. |
| RF-06 | CONSERVAR | RF-06 | Gestionar inscripción a eventos gratuitos y pagados | Conservar unicidad, capacidad/estado y consulta propia. |
| RF-07 | AMPLIAR | RF-07 | Registrar pagos, presentar comprobantes y validar pagos | Declarar pago externo, QR/instrucciones del evento, comprobante y estado/revisor. |
| RF-08 | CONSERVAR | RF-08 | Registrar asistencia por sesión mediante QR y geolocalización | Backend satisface reglas; falta cliente Flutter. CA no debe decir que el QR global se consume una sola vez. |
| RF-09 | REFORMULAR | RF-09 | Generar, consultar y descargar certificados elegibles | Nombre actual “consultar” no refleja emisión real; decir generación bajo demanda, PDF y reglas backend. |
| RF-10 | REFORMULAR | RF-10 | Consultar historial propio de inscripciones, pagos, asistencias y certificados | Aclarar consulta por dominios/endpoints, no dashboard único; completar móvil. |
| RF-11 | CONSERVAR | RF-11 | Gestionar categorías de eventos | CRUD administrador real. |
| RF-12 | REFORMULAR | RF-12 | Consultar dashboards y exportar reportes disponibles | Limitar dimensiones/métricas a las calculadas, mantener COULD y PDF/XLSX. |
| RF-13 | CONSERVAR | RF-13 | Verificar públicamente un certificado por código | Endpoint/API y página pública coinciden. |

No se recomienda “ELIMINAR” RF solo por ausencia de Flutter: RF-07 y RF-08 son MUST y guardan coherencia con objetivo/arquitectura. La decisión correcta es cerrar brecha móvil o, solo con aprobación académica, revisar formalmente alcance, objetivos y criterios; no ocultar la brecha en documentación.

## 16. Propuesta RNF

Conservar los cinco RNF y sus umbrales. No hay base para reducirlos. Reformular únicamente precisión operativa con visto bueno académico: qué solicitudes cuentan como “principales”, cómo medir 20 concurrentes, protocolo de usabilidad, versión estable congelada durante compatibilidad y período exacto de uptime. No alterar 95 %/3 s/20 usuarios, 100 %, 80 %, Chrome/Edge/Firefox/Android 12+, ni disponibilidad ≥95 % sin autorización del tutor.

## 17. Matriz documento → sistema final

| Elemento académico | Sistema real | Acción documental | Acción técnica posterior | Prioridad |
|---|---|---|---|---|
| Problema y evidencia exploratoria | El sistema aborda dispersión; resultados encuesta no incorporados. | Completar evidencia sin exagerar muestra/conclusiones. | Ninguna derivada hasta análisis. | ALTA |
| Objetivos 1, 2, 5, 6 | Evidencia de análisis/diseño/pruebas/deploy incompleta. | Actualizar capítulos/diagramas, completar Cap. 3 solo tras resultados. | Pruebas NFR y deploy reproducible. | CRÍTICA |
| RF-07 y objetivo 3, pagos | Backend + Angular; Flutter presenta UI de “disponible después”. | Reflejar flujo real y arquitectura móvil deseada. | Completar Flutter pago externo, QR pago visualizado, upload/blob, mis pagos/errores. | CRÍTICA |
| RF-08 y objetivo 3, asistencia | Backend QR/GPS y web organizador; participante móvil no implementado. | Documentar validaciones del backend y papel de Flutter. | Cámara/QR, GPS/permisos, flujo y pruebas en dispositivo. | CRÍTICA |
| RF-09/RF-10, certificados/historial | Backend + Angular por vistas; móvil incompleto. | Precisar política y consulta fragmentada. | Pantallas/repositorios Flutter; pruebas elegibilidad/descarga/historial. | ALTA |
| RF-03/05/07 QR de pago | Implementación web/backend reciente, no parte explícita CA actual. | Añadir CA diferenciadas para carga del organizador y presentación del participante. | Probar PNG/JPG, ownership, evento público y pago end-to-end; revisar Flutter. | ALTA |
| 2.4 arquitectura/canales | Web+móvil monolito modular; servicios Brevo/Supabase. | Actualizar diagrama/tabla de protocolos y responsabilidades. | Verificación de servicios en deployment. | ALTA |
| 2.4.2 modelo | 16 `@Entity` visibles; docx declara 14 entidades/tablas. | Reconciliar con inventario DB real; distinguir dominio/técnico. | Validar schema de cada entorno y estrategia Flyway/DDL. | ALTA |
| 2.4.4 API | Controladores y Swagger `/api/v1`; auxiliares listan rutas legacy. | Sustituir inventario por contrato generado actual. | Probar colección API contra build y prod. | ALTA |
| 2.7 seguridad | BCrypt/JWT/RBAC/ownership/sesiones/archivos; prosa obsoleta de revocación. | Actualizar y retirar credenciales demo; sanitizar evidencia. | Evaluar deuda concurrente de reset y rotación de cuentas si corresponde. | CRÍTICA |
| 2.8 pruebas | Suites localmente ejecutadas; resultados de monografía siguen placeholders; Flutter inconcluso. | Completar casos con resultado real, fecha, versión y evidencia. | CI/reporte reproducible; API/E2E y NFR. | CRÍTICA |
| 2.9 despliegue/anexo móvil | Env/config presentes, producción y APK no verificados. | Escribir procedimiento probado y URLs/artifact real. | Desplegar/verificar; producir y probar APK/AAB si requisito. | CRÍTICA |
| Actores/modelo de permisos | Tres roles reales; auxiliares mantienen cinco roles antiguos. | Unificar tablas, casos de uso y glosario; tercero verificador no es rol. | Preservar tests que garantizan ausencia de módulos retirados. | ALTA |
| Dashboard/reportes | Datos reales mezclados con campos placeholder. | Documentar solo métricas existentes. | Quitar campos falsos/inactivos o implementar medición real antes de devolverlos. | ALTA |
| Fuera de alcance | Auxiliar excluye app móvil pese a que monografía la incluye. | Corregir contradicción; confirmar gateway/no gateway y otras exclusiones. | Ninguna para cosas explícitamente fuera, salvo evitar enlaces UI ficticios. | ALTA |

## 18. Documentos y contenido que deben incorporarse/retirarse

### Agregar o actualizar en la monografía

- Verificación de correo, recuperación/reset, sesión/revocación y roles reales.
- Flujo de solicitud/aprobación de organizador, estados de evento y ownership.
- QR de pago de evento (imagen real y su endpoint protegido/controlado), almacenamiento privado, instrucciones/pago externo; diferencia con comprobante y QR temporal de asistencia.
- Brevo SMTP y Supabase S3 como integraciones externas; PostgreSQL conserva datos/metadatos. Sin credenciales, URLs privadas ni tokens.
- Asistencia por sesión con token temporal, tiempo/radio/precisión según código y su cliente esperado Flutter.
- Elegibilidad de certificados, porcentaje curricular de 80 % conforme al backend, PDF bajo demanda y endpoint de verificación pública.
- Operaciones reales de reportes y productos de exportación.
- Resultados de pruebas y despliegue únicamente después de ejecutarlos.

### Reformular, archivar o eliminar referencias obsoletas

- `docs/05_Fuera_de_Alcance.md`: excluir aplicación móvil contradice la fuente actual; no puede permanecer así. Decidir alcance móvil con tutor y alinear.
- `docs/06_Actores_del_Sistema.md`, glosario, casos de uso y diagramas: reemplazar `Super Administrador`, Coordinador, Validador, Registrador y roles participantes legacy por los tres roles finales/actor público según corresponda.
- `docs/07_Requerimientos_Funcionales.md`, `docs/08_Requerimientos_No_Funcionales.md`, `docs/12_Matriz_de_Trazabilidad.md`, `docs/09_Reglas_De_Negocio.md`, `docs/10_Casos_de_Uso.md`, backlog, diagramas PlantUML, `docs/release_1_0/` y Postman: marcarlos como históricos o alinearlos. Contienen facultades/carreras, encuestas, credenciales, control acceso legado, lista espera, QR permanente/cifrado y estados que ya no coinciden. No copiarlos en la nueva monografía sin inspección.
- `flutter/VidiaApp/README.md`: quitar su exclusión E2 como afirmación de alcance final; mantener E2 solo como antecedente de etapa.
- Campos `nivelSatisfaccion`, `participacionEncuestas`, `promedioSatisfaccionPorEvento` y `qrUtilizados` que salen en DTO como 0/lista vacía: no documentarlos como estadísticas funcionales; recomendar retirarlos de contrato/modelos si no se implementan, o implementar cálculo real con alcance aprobado.
- Reconciliar “14 entidades/14 tablas” y el inventario actual. No declarar 16 tablas en producción solo por encontrar 16 clases JPA; verificar esquema desplegado.
- Corregir texto de revocación, marcadores de resumen/encuesta, casos CP-01…CP-15 “Pendiente de registrar”, despliegue con placeholder y conclusiones de plantilla. Retirar credenciales demo en claro y comprobar si las cuentas deben rotarse/desactivarse.

## 19. Trabajo técnico pendiente priorizado

1. **Completar Flutter MUST:** RF-07 pago externo y carga/consulta de comprobante; RF-08 escáner y geolocalización; agregar QR pago de evento al flujo donde corresponda. Mantener backend como árbitro y reuse de `/api/v1`.
2. **Completar Flutter SHOULD:** RF-09 mis certificados/descarga y RF-10 pagos/asistencias/certificados en consultas de historial; definir si alta/recuperación móvil es necesaria o si cuenta se crea en Web.
3. **Cerrar observabilidad falsa:** retirar o hacer reales campos estadísticos hardcodeados; comprobar si la respuesta de dashboard expone datos engañosos aunque UI no los muestre.
4. **Pruebas cruzadas:** Flutter unitario/widget + API real local/integración; RF-07/08 end-to-end en dispositivo Android 12+ con permisos denegados/aceptados y errores backend; RF-13 verificación pública.
5. **Datos/BD:** inventariar tabla por tabla en PostgreSQL local y productivo; resolver Flyway baseline/ddl-auto y documentar sin afirmar cobertura de migraciones no existente.
6. **Producción:** probar commit/URLs reales, HTTPS, CORS, login/roles, health endpoint disponible, PostgreSQL, Brevo, Supabase privado, descarga/up/down y política de datos; guardar evidencias sin secretos.
7. **NFR:** prueba de carga, usabilidad, navegadores Android y uptime exactamente con criterios normativos.

## 20. Orden recomendado de reconciliación

1. Acordar con tutor que el alcance final es web+móvil (o aprobar formalmente otra cosa) y congelar los tres actores/roles.
2. Aprobar edición de RF: conservar IDs/prioridades; aprobar CA ampliados; confirmar RF-09/RF-10/RF-12 reformulados y alcance de QR pago. No editar la monografía hasta aprobación.
3. Completar Flutter de RF-07/08 primero; luego RF-09/10 y onboarding requerido; mantener pagos manuales sin gateway.
4. Quitar placeholders/no-op de dashboard o implementar solo si se decide funcionalidad real; revisar residuos legacy sin borrar pruebas de ausencia.
5. Estabilizar/verificar esquema y deployment (local staging/productivo), contratos OpenAPI y seguridad/ownership.
6. Ejecutar pruebas unitarias, integración, API/E2E, errores 401/403 y criterios CA; después NFR RNF-01…05.
7. Reconciliar modelo entidad/tabla y diagramas; actualizar arquitectura/API/seguridad/actores/alcance y manuales a versión de sistema realmente probada.
8. Completar resultados 2.8 y procedimiento 2.9/anexo instalable; solo entonces redactar resumen/conclusiones y cerrar objetivos con evidencia.
9. Realizar una última auditoría cruzada documento↔OpenAPI↔clientes↔tests↔deployment y revisar secretos del DOCX/repositorio.

## 21. Riesgos y decisiones que requieren aprobación

- **Alcance móvil:** monografía actual lo incluye de manera inequívoca; eliminarlo afectaría objetivo general, OE-3, RF-07/08/09/10, arquitectura, compatibilidad y defensa. Recomendación: implementarlo, salvo decisión formal tutor/autor de rebaselinar también esos elementos.
- **Criterios nuevos de QR pago:** actualizar CA existentes parece más simple que crear RF-14; tutor debe aprobar si conserva 13 requisitos o requiere trazabilidad separada.
- **Métrica de disponibilidad:** auxiliar dice 99,5 %, norma actual E3.2 95 %. Requiere arbitraje académico; no se debe escoger por conveniencia.
- **Integridad de documentos:** existen credenciales de demo en monografía y marcadores de plantilla. Esto es una entrega no segura/no terminada aunque el código de auth sea bueno.
- **Persistencia/migraciones:** hay una migración localizada y `ddl-auto:update` como default; la correspondencia de producción no está acreditada. Riesgo de que diagramas/afirmación de schema no reflejen la BD ejecutada.
- **Estadísticas placeholder:** valores fijos pueden degradar credibilidad académica y confundirse con métricas implementadas.
- **Pruebas/producción:** los resultados locales no acreditan acceso de tribunal ni disponibilidad real; Flutter permanece sin resultado automatizado concluyente en E3.2.

## 22. Conclusión y recomendación

La monografía describe una dirección de producto coherente con el backend y con buena parte del frontend web. **No conviene reducirla al alcance E2 ni declarar sistema completo basándose en endpoints.** La versión final recomendada conserva el producto web+móvil, 3 roles funcionales, 13 RF y cinco RNF; añade como criterios verificables las funciones nuevas y reales (verificación/reset, QR de pago, Storage/correo) y corrige nombres, estados, modelo y seguridad. El trabajo técnico prioritario es completar en Flutter los MUST de pago/comprobante y QR+GPS, más certificados/historial SHOULD, eliminar métricas ficticias y verificar producción. Después se actualiza la monografía con evidencia, no con expectativas.

**Conclusión de auditoría:** el problema, objetivo general y los seis objetivos específicos se conservan; los RF-01/03/05/07 se amplían, RF-09/10/12 se reformulan, y RF-02/04/06/08/11/13 se conservan. No se recomienda eliminar RF. La documentación auxiliar sí requiere una depuración/alineación considerable, y existen brechas técnicas móviles y de evidencia que impiden afirmar cumplimiento académico completo hoy.

### Archivos/fuentes consultados

- `docs/monografia/Lamas-monografia-F.docx`; `docs/auditoria/E3_2_MATRIZ_NORMATIVA.md`.
- `docs/01_Vision_del_Proyecto.md`, `02_Objetivos_Generales.md`, `03_Objetivos_Especificos.md`, `04_Alcance.md`, `05_Fuera_de_Alcance.md`, `06_Actores_del_Sistema.md`, `07_Requerimientos_Funcionales.md`, `08_Requerimientos_No_Funcionales.md`, `09_Reglas_De_Negocio.md`, `10_Casos_de_Uso.md`, `12_Matriz_de_Trazabilidad.md`, `docs/plan_maestro/03_requisitos.md`, `05_flujos_funcionales.md`, `06_arquitectura.md`, `07_modelo_datos.md`, `08_api.md`, `09_frontend_web.md`, `10_aplicacion_movil.md`, `11_seguridad.md`, `12_pruebas.md`, `13_despliegue.md`, y `docs/release_1_0/` (arquitectura/despliegue/manuales).
- Backend: `EventoController`, `EventoService`, `Evento`/`EventoResponse`/`EventoDetalleResponse`; `AlmacenamientoArchivos`, `SupabaseS3StorageService`, `PagoService`, `PagoController`; `QrAsistenciaService`, `AsistenciaService`, `SesionEventoService`; `CertificadoService`, `CertificadoDocumentoService`, `DashboardService`, `ReporteArchivoService`; `SesionUsuarioServicio`, `AutenticacionServicio`; `application.yml`, `application-prod.yml`, `db/migration/V1__agregar_qr_pago_storage_key_eventos.sql`, `ModeloConceptualOficialTest`.
- Angular: `app.routes.ts`, `EventoService`, `evento.model.ts`, módulos/formularios de evento, catálogo/detalle/inscripción, pagos, asistencia, certificados, dashboards/reportes.
- Flutter: `pubspec.yaml`, `README.md`, `main.dart`, `screens/` (auth_gate, login, events, event_detail, my_registrations), modelos/repositories de evento e inscripción y servicios API/auth. No se encontró paquete/cliente/código de cámara/ubicación/pagos/certificados al inventariar `lib/`.
- Git: `git log -8 --oneline --decorate`; `git status --short`. No se ejecutaron pruebas, builds, APIs ni navegador durante E3.3.

**Estado Git al auditar:** `main`, HEAD `fc9ce15`. Working tree ya tenía cambios y documentos sin commit, incluidos `docs/monografia/`, `docs/auditoria/E3_2_MATRIZ_NORMATIVA.md` y otros archivos listados por `git status --short`; todos se preservaron. El único archivo creado por E3.3 es `docs/auditoria/E3_3_RECONCILIACION_DOCUMENTO_SISTEMA.md`. Sin commit y sin push.
