# E3.2 — Matriz normativa Monografía → Implementación

**Auditoría de solo lectura de código y documentos, con excepción de este informe.** No se modificaron código ni documentos normativos; no se crearon commits ni se hizo push. El único archivo nuevo de esta tarea es este informe.

## 1. Fuente normativa identificada

### Fuente principal

`docs/monografia/Lamas-monografia-F.docx` es la fuente normativa seleccionada: su portada identifica el trabajo de Vidia para la UAJMS y sus secciones 1.5, 2.3.2 y 2.3.3 contienen objetivos, trece RF con CA/MoSCoW y cinco RNF con métricas. El usuario designa esta versión como la actual. Las propiedades internas indican revisión 3 y modificación 2026-10-01; el documento no declara una etiqueta de versión “final”. Sus propiedades todavía muestran como título “Plantilla del Trabajo Final de Diplomado”; por tanto se lo toma como **versión normativa suministrada para E3.2**, no como confirmación editorial de que sea un documento final listo para defensa.

### Fuentes complementarias encontradas

| Archivo | Uso y condición |
|---|---|
| `docs/monografia/Lineamientos-Tecnicos-Trabajo-Final-Diplomado-Desarrollo-Web-y-Apps-Moviles.docx` | Complementario académico; extracción legible. Define contenidos por capítulo, ocho condiciones mínimas del producto y anexos obligatorios. Creado/modificado 2026-07-22 en propiedades. |
| `docs/monografia/Lineamientos-Tecnicos-Trabajo-Final-Diplomado-Desarrollo-Web-y-Apps-Moviles.pdf` | Duplicado PDF del lineamiento; se verificó su existencia, pero no se pudo extraer el contenido en este entorno. |
| `docs/monografia/Formato-Trabajo-Final-Diplomado-Distintas-Areas (1).pdf` | Formato institucional referenciado por los lineamientos; existe, pero no se pudo extraer su contenido en este entorno. |
| `docs/monografia/Plantilla_Monografia_Diplomado (1).docx` | Plantilla auxiliar; no es fuente de RF del proyecto. |
| `docs/monografia/P3_Arquitectura_API_Seguridad_Modulo4.pdf` | Auxiliar académico P3; existencia comprobada, contenido no extraído en este entorno. |
| `docs/monografia/P4_Pruebas_Despliegue_Defensa_Modulo4.pdf` | Auxiliar académico P4; existencia comprobada, contenido no extraído en este entorno. |
| `docs/plan_maestro/03_requisitos.md` | Archivo **no versionado**. Fuente complementaria solamente; no reemplaza a la monografía. Enumera 12 RF y 8 MUST, frente a 13 RF y 7 MUST de la fuente normativa actual. |
| `docs/07_Requerimientos_Funcionales.md`, `docs/08_Requerimientos_No_Funcionales.md`, `docs/12_Matriz_de_Trazabilidad.md` | Documentos auxiliares versionados, de una especificación anterior. Sirven para localizar discrepancias, no para corregir ni sustituir el contenido de la monografía. |

La extracción textual de `pdftotext` no funcionó: el ejecutable disponible es el iniciador de MiKTeX y detuvo la operación solicitando completar su configuración. No se instaló ninguna dependencia ni se generaron archivos intermedios. Por eso los contenidos particulares de Formato PDF, P3 y P4 quedan pendientes de cotejo; sí se usó el DOCX complementario de lineamientos.

**Hallazgo editorial importante:** el documento suministrado aún contiene marcadores de plantilla y resultados sin completar: resumen con marcador, casos CP-01 a CP-15 con resultado “Pendiente de registrar”, campos de despliegue por completar y conclusiones/recomendaciones en plantilla. En la sección 2.9 aparecen credenciales de demostración en texto plano; por seguridad no se reproducen aquí. Esto contradice la instrucción de 2.7.5 del propio documento de no incluir credenciales en la monografía y debe corregirse en el documento normativo bajo control académico antes de entregarlo.

## 2. Requisitos funcionales extraídos

La siguiente lista reproduce todos los RF identificados en la Tabla 4 de 2.3.2, con criterios resumidos fielmente y sus IDs CA conservados. La monografía no asigna una columna de plataforma por RF; la asignación web/móvil posterior se deriva de la arquitectura descrita en 2.4.1 y se marca como análisis, no como texto normativo.

| RF | Nombre | Prioridad | Criterios de aceptación | Actor |
|---|---|---|---|---|
| RF-01 | Registro e inicio de sesión | MUST | CA-01.1: datos válidos y únicos crean la cuenta y permiten login. CA-01.2: correo/RU/CI duplicado se rechaza. CA-01.3: credenciales válidas autentican y entregan mecanismo de autorización. CA-01.4: credenciales incorrectas son rechazadas. | Usuario/Participante |
| RF-02 | Solicitar condición de organizador | SHOULD | CA-02.1: usuario sin solicitud pendiente puede enviarla para revisión administrativa. CA-02.2: solicitud pendiente duplicada se rechaza. CA-02.3: aprobación concede condición de organizador. CA-02.4: rechazo queda registrado. | Participante autenticado; Administrador revisa |
| RF-03 | Crear, gestionar y enviar eventos a revisión | MUST | CA-03.1: organizador autorizado crea evento asociado a sí mismo. CA-03.2: se permiten cambios en los estados editables y se persisten. CA-03.3: evento válido cambia al proceso de revisión al enviarse. CA-03.4: datos obligatorios ausentes o inválidos impiden el envío. | Organizador |
| RF-04 | Revisar y gestionar eventos enviados a revisión | MUST | CA-04.1: aprobación de evento en revisión lo publica. CA-04.2: rechazo registra la decisión e impide publicar. CA-04.3: evento rechazado puede corregirse y volver a revisión cuando la regla lo permita. | Administrador |
| RF-05 | Consultar y buscar eventos publicados | MUST | CA-05.1: listado muestra eventos publicados existentes. CA-05.2: búsqueda devuelve resultados coincidentes. CA-05.3: búsqueda sin coincidencias informa que no hay resultados. CA-05.4: detalle por ID válido muestra información pública. | Participante |
| RF-06 | Gestionar inscripción a eventos | MUST | CA-06.1: evento abierto y con cupo cuando aplique registra una única inscripción. CA-06.2: inscripción duplicada se rechaza. CA-06.3: falta de cupo impide inscribir. CA-06.4: inscripción válida aparece en consulta propia. | Participante |
| RF-07 | Registrar y cargar comprobante de pago | MUST | CA-07.1: evento pagado permite registrar pago y cargar comprobante. CA-07.2: comprobante cargado deja el pago disponible para validación. CA-07.3: administrador u organizador autorizado cambia el pago al estado que corresponda al validar. CA-07.4: participante identifica rechazo y puede volver a presentar documentación cuando corresponda. | Participante; Administrador/Organizador valida |
| RF-08 | Registrar asistencia mediante QR y geolocalización | MUST | CA-08.1: inscripción válida + QR vigente + ubicación aceptada registra asistencia. CA-08.2: QR inválido/expirado se rechaza. CA-08.3: ubicación no válida para la sesión se rechaza. CA-08.4: no duplica asistencia de la misma inscripción/sesión. | Participante |
| RF-09 | Consultar certificados de participación | SHOULD | CA-09.1: administrador/organizador autorizado genera certificado de inscripción elegible. CA-09.2: certificado generado se muestra al participante. CA-09.3: certificado existente se descarga. CA-09.4: inscripción no elegible no genera certificado. | Participante; Administrador/Organizador genera |
| RF-10 | Consultar historial de participación | SHOULD | CA-10.1: inscripciones del participante se muestran. CA-10.2: pagos, asistencias y certificados asociados se consultan en sus módulos. CA-10.3: ausencia de registros se comunica como lista sin datos. | Participante |
| RF-11 | Gestionar categorías de eventos | SHOULD | CA-11.1: datos válidos crean categoría. CA-11.2: nombre duplicado se rechaza. CA-11.3: cambios válidos se persisten. CA-11.4: restricción de borrado por uso informa la condición. | Administrador |
| RF-12 | Consultar reportes y estadísticas | COULD | CA-12.1: dashboard autorizado muestra estadísticas disponibles. CA-12.2: consultas de eventos/participantes/pagos/certificados retornan los datos permitidos. CA-12.3: exportación disponible se procesa en formato seleccionado. | Administrador u Organizador |
| RF-13 | Verificar públicamente certificado por código | COULD | CA-13.1: código registrado devuelve información y estado. CA-13.2: código desconocido informa que no está registrado. CA-13.3: la consulta pública no exige autenticación. | Tercero |

## 3. Cálculo MoSCoW

Conteo directo de Tabla 4, sección 2.3.2:

- **TOTAL_RF = 13**
- **TOTAL_MUST = 7**: RF-01, RF-03, RF-04, RF-05, RF-06, RF-07 y RF-08.
- **SHOULD = 4**: RF-02, RF-09, RF-10 y RF-11.
- **COULD = 2**: RF-12 y RF-13.
- **WON'T = 0** (no aparece esa prioridad en la tabla).
- **PORCENTAJE_MUST = 7 / 13 × 100 = 53,846… % ≈ 53,85 %.**

La monografía no establece por sí misma un máximo de MUST en el texto extraído. Si se aplica el umbral de 60 % usado en el checkpoint E3, 53,85 % queda por debajo. No se halló un umbral de 60 % en los DOCX leídos; el PDF P4 no se pudo extraer, así que no se atribuye ese límite a P4.

**Discrepancia con Plan Maestro:** el no versionado `docs/plan_maestro/03_requisitos.md` enumera RF-01…RF-12, con RF-01…RF-08 MUST y RF-09…RF-11 SHOULD, RF-12 COULD. La monografía vigente tiene RF-13 y distintas prioridades/alcances —en particular RF-02 SHOULD, RF-09 SHOULD y RF-13 COULD para verificación pública—. Se conserva la monografía como autoridad y no se normalizan IDs ni prioridades.

## 4. Requisitos no funcionales

Las métricas siguientes son las de la Tabla 5, sección 2.3.3, sin sustitución por las métricas del documento auxiliar anterior.

| RNF | Tipo | Requisito | Métrica normativa | Evidencia necesaria | Estado de evidencia |
|---|---|---|---|---|---|
| RNF-01 | Rendimiento | Responder operaciones principales de consulta, autenticación, inscripción y consulta de información en tiempo adecuado para interacción. | En pruebas de rendimiento, ≥95 % de solicitudes principales ≤3 segundos con 20 usuarios concurrentes. | Escenario/carga reproducible, dataset, herramienta/versiones, percentiles/resultado por endpoint y reporte fechado. | No hay prueba de carga ejecutada/versionada localizada. |
| RNF-02 | Seguridad | Proteger información/operaciones con autenticación, autorización por rol, contraseñas seguras y validación de solicitudes protegidas. | 100 % de endpoints protegidos rechazan autenticación ausente/inválida y operaciones restringidas rechazan rol insuficiente. | Inventario exhaustivo de endpoints y ejecución con/sin JWT y con roles insuficientes/suficientes. | Código y tests parciales disponibles; cobertura completa/producción no demostrada. |
| RNF-03 | Usabilidad | Interfaces comprensibles/consistentes para operaciones principales de consulta, inscripción, gestión y seguimiento. | ≥80 % de usuarios de prueba completan tareas principales sin asistencia externa. | Protocolo, tareas, participantes, observaciones, denominador/resultados y evidencia anonimizada. | No hay estudio de usabilidad localizado. |
| RNF-04 | Compatibilidad | Funcionar en navegadores web y dispositivos móviles definidos manteniendo funcionalidades principales. | Chrome, Edge y Firefox en versiones estables disponibles durante pruebas; Android 12 o superior. | Matriz de versiones/dispositivo, ejecución de flujos principales y evidencia por navegador/Android. | No hay matriz de compatibilidad ejecutada/versionada. |
| RNF-05 | Disponibilidad | Sistema desplegado disponible para funcionalidades principales durante periodo de prueba. | Disponibilidad mínima 95 % durante el periodo de prueba, excluyendo mantenimiento programado. | Periodo y ventana definidos, monitoreo/uptime, incidencias, cálculo reproducible y URL/versión probada. | No hay serie de disponibilidad ni prueba productiva versionada. |

## 5. Objetivos y trazabilidad

Objetivo general de 1.5.1: desarrollar una plataforma web y móvil UAJMS para centralizar información y gestionar inscripción, pago, asistencia y certificación. La asignación RF de esta tabla es una interpretación trazable, no una renumeración normativa.

| Objetivo específico (1.5.2) | RF relacionados | Implementación real | Evidencia | Estado |
|---|---|---|---|---|
| OE-1 Analizar procesos/requerimientos de administradores, organizadores y participantes. | RF-01…RF-13 | La monografía contiene actores, RF/CA/MoSCoW y casos de uso. Falta completar resultados exploratorios y evidencias de validación del problema que aún estén como pendientes. | Tabla 3/4, casos CU; `docs/07_Requerimientos_Funcionales.md` es auxiliar no normativo. | PARCIAL |
| OE-2 Diseñar arquitectura, datos, interfaces y contratos para web/móvil/backend. | RF-01…RF-13; RNF-01/RNF-02/RNF-04 | Backend modular, Angular, Flutter y API presentes; modelo y diseños se describen. Diagramas/anexos y concordancia del contrato requieren comprobación documental integral. | 2.4; módulos backend, rutas Angular, repositorios Flutter; pruebas de contrato OpenAPI. | PARCIAL |
| OE-3 Implementar gestión de eventos, inscripción, pagos, asistencia/QR y certificados. | RF-03…RF-09 | Backend y web cubren gran parte. Flutter implementa auth, catálogo/detalle, inscripción y mis inscripciones; no se localizan flujos móviles de pago, asistencia QR/GPS ni certificados, aunque el diseño atribuye pagos/asistencia/certificados a móvil. | Controladores/servicios por módulo; tests backend y Angular ejecutados; cuatro archivos de test Flutter existentes, ejecución no concluida. | PARCIAL |
| OE-4 Aplicar autenticación/autorización/protección según rol. | RF-01…RF-04, RF-07…RF-13; RNF-02 | BCrypt, JWT, filtros, roles, `@PreAuthorize`, ownership, validación y formatos de error implementados. Monografía contiene una afirmación de revocación desactualizada y evidencia real de producción falta. | `SecurityConfig`, `JwtService`, servicios/controladores; tests de filtros/autorización. | PARCIAL |
| OE-5 Validar con pruebas funcionales y no funcionales. | Todos; RNF-01…RNF-05 | Suites backend y Angular aprobadas localmente. No se acreditan métricas de carga, usabilidad, compatibilidad ni uptime; Flutter no dio resultado. | `mvn test`, `npm test`; no pruebas NFR ejecutadas/versionadas. | PARCIAL |
| OE-6 Desplegar y documentar solución web/móvil, instalación, configuración, uso y mantenimiento. | Todos; RNF-05 | Configuración de API/Render, PostgreSQL, Flyway, Brevo/Supabase y documentación local existen; no se verificó despliegue actual ni APK distribuible. La monografía aún tiene placeholders y una URL no comprobada. | `environment.ts`, `application*.yml`, Docker, documentación release; pruebas en línea pendientes. | PARCIAL |

## 6. RF → Backend

“Tests PASS” en esta sección significa parte de una suite local efectivamente aprobada; no implica que cada criterio CA tenga un test individual ni una prueba productiva.

| RF | Backend: módulos/flujo real | Endpoints representativos | Seguridad/validación | Tests relevantes y resultado | Estado backend |
|---|---|---|---|---|---|
| RF-01 | `Usuario`, `UsuarioRol`, tokens verificación/reset, `AutenticacionServicio`, `SesionUsuarioServicio` | `/api/v1/auth/registro`, `/login`, `/verificar-correo`, `/recuperar-contrasena`, `/restablecer-contrasena`, `/logout` | BCrypt; token JWT expirable; nuevo registro asigna `USUARIO`; token de recuperación hash/expiración/uso único. | `AutenticacionServicioTest`, `VerificacionCorreoServicioTest`, `RecuperacionContrasenaHashTest`, filtros JWT: PASS en suite. | IMPLEMENTADO; producción no verificada |
| RF-02 | `UsuarioControlador`/`UsuarioServicio`, solicitud y decisión admin | `POST /usuarios/solicitud-organizador`, `GET /usuarios/solicitudes-organizador`, `PATCH .../{usuarioId}/aprobar|rechazar` | Usuario solicita; administrador decide; reglas de duplicidad/estado y rol. | `UsuarioServicioTest`, `UsuarioControladorAuthorizationTest`: PASS. | IMPLEMENTADO; flujo real productivo no verificado |
| RF-03 | `Evento`, `EventoRepository`, `EventoService`, DTO de datos/crear/actualizar | `POST/PUT /api/v1/eventos`, `PATCH /{id}/enviar-revision`, `PATCH /{id}/volver-borrador` | Organizador aprobado, ownership; Bean Validation; estado editable y transición de revisión. | `EventoServiceLifecycleTest`, `EventoServiceAuthorizationTest`, validación de request: PASS. | IMPLEMENTADO en backend local |
| RF-04 | `EventoService` y operaciones administrativas | `GET /eventos/revision`, `PATCH /{id}/publicar`, `PATCH /{id}/rechazar` | ADMINISTRADOR; flujo de revisión y decisión; organizador no publica directamente. | `EventoControllerTest`, `EventoControllerSecurityContractTest`, lifecycle: PASS. | IMPLEMENTADO en backend local |
| RF-05 | Catálogo y búsqueda mediante `EventoController`/`EventoService` | `GET /eventos/publicados`, `/publicados/buscar`, `/{id}`, `/categoria/{id}` | Endpoints públicos limitados a eventos visibles; validación de filtros/ID. | tests de controlador/servicio y Angular; backend suite PASS. | IMPLEMENTADO localmente |
| RF-06 | `Inscripcion`, `InscripcionRepository`, `InscripcionService` | `POST /inscripciones`, `GET /mis-inscripciones`, `GET /evento/{eventoId}` | Autenticación `USUARIO`; duplicado/cupo/estado y acceso por scope. | `InscripcionControllerTest`, `InscripcionServiceAuthorizationTest`: PASS. | IMPLEMENTADO localmente |
| RF-07 | `Pago`, `PagoRepository`, `PagoService`, `ArchivoSeguroServicio`, `AlmacenamientoArchivos` y Supabase S3 | `POST /pagos`, `POST /pagos/{id}/comprobante`, `GET /mis-pagos`, `/pendientes`, `PATCH /{id}/validar|rechazar` | Ownership/scoping, tipo real/Tika, límites, autorización de aprobación por evento y estado. | `PagoServiceFlowTest`, `PagoServiceAuthorizationTest`, almacenamiento/archivo: PASS; smoke Supabase omitido sin credenciales habilitadas. | IMPLEMENTADO en backend; operación en Render no verificada |
| RF-08 | `SesionEvento`, `QrAsistenciaService`, `AsistenciaService`, entidades/repositorios QR y asistencia | `GET/POST /sesiones/{id}/qr`, `POST /asistencias`, `GET /asistencias/mis-asistencias`, `/evento/{id}` | Rol/ownership; inscripción válida; QR temporal/uso; geodistancia/radio; unicidad de asistencia. | `QrAsistenciaServiceTest`, `AsistenciaRegistroTest`, `AsistenciaApiSecurityContractTest`: PASS. | IMPLEMENTADO en backend; cliente móvil ausente |
| RF-09 | `CertificadoService`, `CertificadoDocumentoService`, `Certificado` | `POST /certificados/generar/{inscripcionId}`, `GET /certificados/mis-certificados`, `GET /eventos/{eventoId}/certificados`, `/certificados/{id}/descargar` | Elegibilidad resuelta en backend; ownership/scope de consulta/descarga. | `CertificadoServiceTest`, `CertificadoDocumentoServiceTest`, autorización: PASS. | IMPLEMENTADO en backend local |
| RF-10 | Consultas propias de inscripciones, pagos, asistencia y certificados | endpoints `mis-inscripciones`, `mis-pagos`, `mis-asistencias`, `mis-certificados` | `USUARIO` y scope de titular. | Tests por dominio: PASS en suite; la composición de “historial” no es un endpoint único. | PARCIAL como historial unificado; datos por módulo implementados |
| RF-11 | `CategoriaEvento`, repository/service/controller | `/categorias-evento`, `/activas`, CRUD admin | Lectura autenticada; mutaciones ADMINISTRADOR; validación/duplicado/restricción de eliminación. | `CategoriaEventoControllerTest`, `CategoriaEventoServiceTest`: PASS. | IMPLEMENTADO en backend local |
| RF-12 | `DashboardService`, DTOs ejecutivo/académico/operativo y `ReporteArchivoService` | `/dashboard`, `/dashboard/ejecutivo`, `/academico`, `/operativo`, `/reportes/*`, exportaciones PDF/Excel | Alcance admin/organizador según servicio y evento propio. | `DashboardService*AuthorizationTest`, `DashboardServiceScopeTest`, `ReporteArchivoServiceTest`: PASS. | IMPLEMENTADO en backend local |
| RF-13 | `CertificadoController`/`CertificadoService` consulta pública de verificación | `GET /api/v1/certificados/verificar/{codigo}` | Público intencional; expone DTO de verificación y no el registro interno completo. | Tests de servicio/controlador de certificados: PASS; respuesta online no verificada. | IMPLEMENTADO en backend local |

Los estados se limitan a existencia del flujo backend y tests locales. No implican integración completa con DB/servicios reales: no se localizaron tests Testcontainers ni suite end-to-end contra una base real para este recuento.

## 7. RF → Angular

| RF | Ruta/pantallas y servicios | Formularios/HTTP/errores | Evidencia de test | Estado Angular |
|---|---|---|---|---|
| RF-01 | `features/auth` registro/login/verificación/recuperación/reset; `AuthService` | formularios reactivos; `jwtInterceptor`; navegación 401→login | Specs auth, interceptor y guards incluidos en 271 PASS | IMPLEMENTADO en web |
| RF-02 | Solicitud organizador privada y pantalla administrativa de solicitudes; `SolicitudOrganizadorService` | POST/GET/PATCH con `ApiError`/mensajes | specs de solicitud/lista/admin incluidas en 271 PASS | IMPLEMENTADO en web |
| RF-03 | Módulo organizador/eventos y `evento-form`; `EventoService` | JSON para crear/editar; validaciones Angular; submit a revisión | specs de formulario/servicio en 271 PASS | IMPLEMENTADO en web |
| RF-04 | Rutas admin de eventos, lista/detalle/dialogo de motivo | acciones publican/rechazan por API y refrescan datos | specs de eventos admin en 271 PASS | IMPLEMENTADO en web |
| RF-05 | `/eventos`, detalle público y `EventoService` | búsqueda/filtros catálogo; estados empty/error/loading | catálogo y detalle specs en 271 PASS | IMPLEMENTADO en web; sin prueba de navegador productiva |
| RF-06 | inscripción pública y mis inscripciones | POST inscripción, consulta y cancelación según API | inscripcion-publica/mis-inscripciones/service specs en 271 PASS | IMPLEMENTADO en web |
| RF-07 | registro/presentación comprobante, mis pagos y validación admin/org | POST multipart `archivo`, GET Blob, mensajes de error | `registrar-pago`, `pago.service`, `mis-pagos`, validación pagos specs; 271 PASS. Bug de submit fue corregido en commit `8a06f56`. | IMPLEMENTADO en web |
| RF-08 | sesiones/QR y listado de asistencia de organizador; `AsistenciaService` | web administra/genera/consulta; no es el cliente GPS del participante | specs sesiones/asistencia; no prueba E2E de dispositivo | PARCIAL frente al flujo participante QR+GPS |
| RF-09 | mis certificados, validación pública; `CertificadoService` | lista, descarga Blob y consulta pública; genera desde funciones autorizadas | specs certificados/validación pública en 271 PASS | IMPLEMENTADO en web para consulta/descarga/verificación; emisión bajo endpoint autorizado |
| RF-10 | mis inscripciones, mis pagos, mis certificados y asistencia | vistas separadas, no historial agregado | specs por módulo en 271 PASS | PARCIAL: cobertura distribuida y no unificada |
| RF-11 | admin categorías/lista/form; `CategoriaEventoService` | CRUD y errores de duplicidad/restricción | specs de categorías en 271 PASS | IMPLEMENTADO en web |
| RF-12 | paneles admin/organizador/reportes; `DashboardService` | JSON y descargas PDF/Excel | dashboard/reportes specs en 271 PASS | IMPLEMENTADO en web, ejecución productiva no verificada |
| RF-13 | ruta pública de verificación de certificado | consulta por código sin JWT a API pública | `validacion-publica.component.spec.ts` pasó dentro de 271 | IMPLEMENTADO en web |

El último `npm run build` terminó correctamente; warnings de presupuesto: chunk inicial 889,33 kB frente a 500 kB, y tres CSS de componente superan presupuesto. No es fallo funcional, pero es deuda de rendimiento/carga inicial.

## 8. RF → Flutter

El README de `flutter/VidiaApp` declara alcance E2 (login/logout, catálogo, detalle, inscripción gratuita y mis inscripciones). La sección 2.4.1 de la monografía actual atribuye a Flutter funciones más amplias: participante puede consultar, inscribirse, gestionar pagos, registrar asistencia y consultar certificados. Por tanto E2 no limita esta evaluación.

| RF | Pantalla/servicio/modelo localizado | Endpoint/auth/dispositivo | Pruebas | Estado Flutter frente a arquitectura actual |
|---|---|---|---|---|
| RF-01 | `LoginScreen`, `AuthGate`, `AuthService`, `AuthSession/AuthUser` | login REST; token en almacenamiento seguro; no se localizó pantalla de registro | `auth_service_test.dart` | PARCIAL: login/sesión sí; registro desde móvil no localizado; backend y web sí |
| RF-02 | No se localizó solicitud/revisión de organizador | No aplica al participante móvil según arquitectura prevista | Sin test | NO APLICA a móvil participante; web/backend implementan |
| RF-03 | No se localizó gestión/creación de eventos | Arquitectura asigna gestión principalmente web | Sin test | NO APLICA a móvil participante |
| RF-04 | No se localizó revisión administrativa de eventos | Sin pantalla/admin móvil | Sin test | NO APLICA |
| RF-05 | `EventsScreen`, `EventDetailScreen`, `BackendEventoRepository`, `Evento` | `/eventos/publicados`, `/eventos/{id}` sin auth | `evento_repository_test.dart` | IMPLEMENTADO (catálogo/detalle) |
| RF-06 | detalle/confirmación, `InscripcionRepository`, `BackendInscripcionRepository`, `MyRegistrationsScreen` | POST `/inscripciones`; GET `/mis-inscripciones`, con JWT; según README, inscripción gratuita | `inscripcion_repository_test.dart` | PARCIAL: cubre alta/consulta básica; paid/full UI y flujos móviles extensos no demostrados |
| RF-07 | No se localizaron pantallas/repositorios de pago, upload comprobante ni revisión | Backend endpoints existen; no integración Flutter multipart/Blob localizada | Sin test | PENDIENTE en Flutter |
| RF-08 | No se localizaron pantalla de escaneo/captura GPS ni repository de asistencia | Backend API exige QR/ubicación; permisos de cámara/localización no configurados en flujo app localizado | Sin test | PENDIENTE en Flutter; crítico para completar flujo móvil descrito |
| RF-09 | No se localizó pantalla/repositorio de certificados | Backend consulta/descarga y Angular implementados | Sin test | PENDIENTE en Flutter |
| RF-10 | `MyRegistrationsScreen` | Consulta mis inscripciones; sin historial unificado de pagos/asistencias/certificados | Inscription repository tests | PARCIAL |
| RF-11 | No se localizó administración de categorías | Función admin web | Sin test | NO APLICA |
| RF-12 | No se localizaron dashboards/reportes Flutter | Función web admin/organizador | Sin test | NO APLICA según arquitectura |
| RF-13 | No se localizó pantalla de verificación pública Flutter | Endpoint público y Angular disponibles | Sin test móvil | NO APLICA por cliente móvil declarado; no invalida endpoint público |

Se localizaron 4 archivos Flutter de pruebas con **6 casos `test()`** en el texto fuente. `flutter test --no-pub --reporter expanded` se intentó, quedó sin salida y fue interrumpido; resultado **NO VERIFICADO**, no se declara PASS ni FAIL.

## 9. Seguridad

| Control | Implementado | Evidencia | Riesgo/pendiente |
|---|---|---|---|
| Autenticación y contraseñas | BCrypt, login y JWT | `SecurityConfig`, `AutenticacionServicio`, tests auth | Verificar política/variables en Render; Angular conserva token en `localStorage` (impacto XSS a evaluar). |
| JWT/sesiones/expiración | JWT con vencimiento configurable; servicio de sesión y revocación | `JwtService`, `JwtPropiedades`, `SesionUsuarioServicio`; tests de filtro/sesión | La monografía 2.7.1 afirma que no existe revocación de servidor; contradice el código actual. Actualizar la descripción documental con evidencia, no cambiar código para coincidir. |
| Rol de menor privilegio | Registro asigna `USUARIO`; roles técnicos finales ADMINISTRADOR/ORGANIZADOR/USUARIO | `AutenticacionServicio`, `RolSistema`, tests | Probar con cuentas reales de rol en ambiente desplegado. |
| Autorización 401/403 | `anyRequest().authenticated()`, `@PreAuthorize`, handler 401/403 | `SecurityConfig`, tests controller/service | Suites no prueban exhaustivamente 100 % endpoint en producción: RNF-02 sigue sin acreditación completa. |
| Ownership/IDOR | Servicios verifican titular/evento/organizador en pagos, inscripciones, eventos, asistencia/certificados | `EventoService`, `InscripcionService`, `PagoService`, `CertificadoService` y tests Authorization/Scope | Convertir la cobertura actual en una matriz endpoint×rol×titular; hacer 403/404 según contrato con dos identidades reales. |
| Validación/errores | `@Valid`, validadores de archivo, formato `ErrorRespuesta` | DTOs, `ArchivoSeguroServicio`, `ManejadorGlobalExcepciones` | Verificar todos los status y mensajes en contrato desplegado; tests unitarios no cubren every path. |
| Recuperación/verificación | Hash de token, caducidad/uso, respuestas no enumerativas | repositorios/servicios de token y pruebas hash/flujo | Prueba real de emails de producción pendiente; no exponer datos/token en evidencia. |
| Archivos privados | Comprobantes vía adapter de almacenamiento y API autenticada; QR de asistencia aparte del QR de pago | `AlmacenamientoArchivos`, `SupabaseS3StorageService`, `ArchivoSeguroServicio`, servicios QR | Smoke Supabase está omitido en tests; permisos/credenciales del proyecto productivo no verificados. |
| Certificados | Descarga protegida y verificación pública separada | `CertificadoController`, `CertificadoService`, DTO público | Comprobar que DTO público exponga únicamente campos permitidos en despliegue. |
| CORS/HTTPS/secretos | CORS parametrizado, secretos en variables de entorno; HTTPS esperado en despliegue | `application.yml`, `.env.example`, `.gitignore`, interceptor Angular | Configuración efectiva de Render no inspeccionada; escaneo histórico de secretos no se realizó. Monografía expone credenciales de demostración (ver §1). |

### Pruebas 401/403 seleccionadas

| Endpoint | Request de prueba | Resultado esperado | Evidencia automatizada |
|---|---|---|---|
| `GET /api/v1/usuarios/perfil` | Sin Authorization | 401 uniforme | `SecurityErrorResponseWriterTest`, filtros JWT y contrato de seguridad |
| `GET /api/v1/inscripciones/mis-inscripciones` | Sin Authorization | 401 | `InscripcionControllerTest` prueba operación protegida sin autenticación (su caso es POST); agregar/verificar request GET exacto en colección E3 |
| `GET /api/v1/pagos/mis-pagos` | Sin Authorization | 401 | cobertura general de filtro; request exacto productivo pendiente |
| `POST /api/v1/eventos` | JWT de `USUARIO` | 403; mismo caso con ORGANIZADOR autorizado: 201 | `EventoControllerTest.usuarioNoCreaEvento` y `organizadorCreaEvento` |
| `POST /api/v1/categorias-evento` | JWT `ORGANIZADOR`/`USUARIO` | 403; admin autorizado | `CategoriaEventoControllerTest`, autorización de servicio |
| `GET /api/v1/pagos` | JWT de `ORGANIZADOR` | 403; admin autorizado | `PagoControllerAuthorizationTest` |

Son casos automatizados locales. Las pruebas de producción con JWT real para todos los roles siguen pendientes.

## 10. Validaciones y errores

| Escenario | Backend / respuesta | Angular | Flutter | Evidencia de prueba local |
|---|---|---|---|---|
| Campos obligatorios/formato inválidos | Bean Validation / `MethodArgumentNotValidException` → 400 `VALIDATION_ERROR` | Validators reactivos en formularios | En pantallas presentes, validación puntual; no toda la matriz | `EventoDatosRequestValidationTest`, `EventoControllerTest`; Angular specs de formularios. |
| Registro duplicado correo/RU/CI | Restricciones/servicio rechazan | Errores API mostrados en registro | No se localiza registro móvil | Tests `AutenticacionServicioTest`/`UsuarioServicioTest`; cotejar cada identificador con CA-01.2. |
| Recurso inexistente | `RecursoNoEncontradoException` → 404 | Estado de error visible en vistas | tratamiento `ApiError` existente | `ManejadorGlobalExcepcionesTest` y tests de servicios. |
| Sin autenticación / rol insuficiente | 401 / 403 uniformes | interceptor redirige en 401 y muestra 403 | test Dart de 401; backend filter tests | Backend y Angular PASS; producción pendiente. |
| Conflicto estado / duplicado | `ConflictoException` o negocio → 409/400 según regla | mensajes mediante `ApiError` | `ApiError` mapea 409 | `InscripcionServiceAuthorizationTest`, Flutter `api_error_test.dart`; confirmar status endpoint por endpoint. |
| Archivo inválido / contenido incoherente | `ArchivoSeguroServicio` inspecciona MIME/extensión/contenido real | valida antes de enviar y representa error | sin upload Flutter | tests de servicio validan tipos/contenido. |
| Tamaño excedido | Spring multipart y handler → 413 `FILE_TOO_LARGE` | error visible en carga | no hay flujo móvil | handler y pruebas de archivo; prueba API productiva pendiente. |
| Capacidad agotada/inscripción duplicada | lógica de `InscripcionService` evita cupo/duplicado | muestra error backend | repository llama al endpoint; UI de error a confirmar | Tests de inscripción; Dart `api_error_test` caso 409. |
| Pago rechazado/reenvío | estado/observación decide nuevo comprobante | vista pagos y registrar comprobante | sin flujo de pago móvil | `PagoServiceFlowTest` y Angular specs PASS; Flutter pendiente. |
| QR inválido/expirado, asistencia duplicada/fuera de radio | validación QR/sesión/geolocalización en backend | Angular solo administra/consulta | cliente de captura móvil no localizado | `QrAsistenciaServiceTest`, `AsistenciaRegistroTest`: PASS backend; no E2E dispositivo. |
| Certificado no elegible/no disponible | Servicio decide elegibilidad y 404/error de dominio | Empty/error y descarga | sin vista móvil | `CertificadoServiceTest` y specs web PASS. |

`ErrorRespuesta` se compone de `codigo`, `mensaje`, `detalles`, `timestamp` y `ruta`. No se encontró uso contractual de HTTP 422; no debe agregarse a la colección como expectativa sin contrato.

## 11. Evidencia de pruebas

### Resultados ejecutados en esta fase

| Suite | Comando | Resultado real | Naturaleza/limitaciones |
|---|---|---|---|
| Backend | `mvn test` con JDK 21.0.11 | **460 ejecutados; 0 fallos; 0 errores; 1 omitido; BUILD SUCCESS.** | JUnit 5/Surefire, Mockito y MockMvc slice más tests de servicio/contrato. No es prueba de producción ni una suite completa con PostgreSQL real. Smoke test Supabase omitido por configuración no habilitada. Primera ejecución sandbox no pudo descargar el parent POM por restricción de red; reintentado con autorización de red y aprobado. |
| Angular | `npm test -- --watch=false` | **53 archivos; 271 tests PASS; exit 0.** | Vitest/Angular. Tests unitarios/componentes/servicios. No hay suite E2E identificada. |
| Angular build | `npm run build` | **PASS.** | Bundle inicial 889,33 kB; budget 500 kB excedido por 389,33 kB. Warnings CSS: AppShell 7,88 kB, detalle 7,84 kB, dashboard 7,84 kB, catálogo 5,19 kB contra límite de 4 kB. |
| Flutter | `flutter test --no-pub --reporter expanded` | **No concluido:** el proceso no imprimió salida y fue interrumpido; no se declara PASS/FAIL. | 4 archivos y 6 llamadas `test()` encontrados. Framework `flutter_test`. Falta ejecución observable y `flutter analyze`. |

### Clasificación

- **Unitarias:** abundantes en backend Angular Flutter; PASS actual backend/Angular, Flutter indeterminado.
- **Integración/API:** MockMvc slice y contratos automatizados; no hay evidencia de una batería Newman contra Render ni de DB real/Testcontainers.
- **Funcionales/E2E:** no se localizó Playwright/Cypress; escenarios documentados en monografía aún tienen resultado pendiente.
- **Rendimiento:** no se ejecutó la carga de 20 usuarios/95 % ≤3 s.
- **Producción:** no se efectuó prueba contra despliegue en esta fase.

La monografía incluye CP-01…CP-15, pero todos los resultados obtenidos se dejaron como marcador/pendiente. Los informes de sprints y Surefire no llenan automáticamente la columna académica “Resultado obtenido”.

### Matriz de evidencia para completar 2.8

La tabla exigida por la monografía pide identificador, escenario, esperado, obtenido y estado. Para trazabilidad completa debe añadirse RF/RNF, entorno y evidencia enlazada:

| ID | RF/RNF | Escenario | Esperado | Obtenido | Estado | Evidencia/entorno |
|---|---|---|---|---|---|---|
| CP-RFxx-H | RF MUST correspondiente | Camino feliz del CA | Resultado literal del CA | Completar tras ejecución | Pendiente | JUnit/Newman/captura sanitizada + commit/URL |
| CP-RFxx-E | RF MUST correspondiente | Camino de error del CA | Rechazo/mensaje/status del CA | Completar tras ejecución | Pendiente | JUnit/Newman/captura sanitizada + commit/URL |
| CP-401 | RNF-02 | Endpoint protegido sin JWT | 401 estándar | Automatizado local; producción pendiente | Parcial | Test + request/response anonimizada |
| CP-403 | RNF-02 | JWT válido con rol insuficiente | 403 estándar | Automatizado local; producción pendiente | Parcial | Test + rol/endpoint/status |
| CP-RNF01 | RNF-01 | 20 usuarios concurrentes en operaciones principales | ≥95 % ≤3 s | Sin resultado | Pendiente | Reporte de carga fechado |

Cada MUST debe tener una fila H y E por cada CA aplicable; no hay evidencia normativa para generar esas filas por ahora sin volver a la Tabla 4.

## 12. Estado de producción

| Elemento | Clasificación | Evidencia y limitación |
|---|---|---|
| Web frontend | CONFIGURADO, no verificado | Angular tiene build PASS. La URL web del DOCX no fue verificada; environment de producción configura API, no certifica que frontend esté publicado. |
| Backend/API | CONFIGURADO, no verificado | `environment.ts` apunta a `https://trabajo-final-diplomado.onrender.com/api/v1`; no se confirmó servicio activo/version desplegada. |
| URL pública en monografía | NO VERIFICADO | 2.9 consigna `https://trabajo-final-diplomado-web.onrender.com/`; no se comprobó DNS/HTTP/login/flujo. Puede ser dominio frontend distinto del API, pero falta confirmarlo. |
| Salud | PENDIENTE | No se localizó `/api/v1/salud` ni endpoint de salud propio en controladores. No afirmar health check operacional. |
| PostgreSQL | CONFIGURADO, no verificado | JDBC por variables `DB_*`/`DB_URL`; Docker compose de desarrollo. No hay evidencia de conexión al PostgreSQL productivo. |
| Flyway | CONFIGURADO, ejecución productiva no verificada | Habilitado en `application.yml`, location `classpath:db/migration`, baseline 0. `ddl-auto` conserva default `update`; Render debe tener perfil/valor explícito. |
| Render/perfil `prod` | CONFIGURADO parcialmente | `application-prod.yml` selecciona Supabase. `SPRING_PROFILES_ACTIVE` default en YAML base es `dev`; no se leyó configuración privada del servicio Render. |
| CORS | CONFIGURADO por variable | `CORS_ALLOWED_ORIGINS`, default local `http://localhost:4200`; allowlist productiva desconocida. |
| JWT | CONFIGURADO por variable | `JWT_SECRET`, `JWT_EXPIRATION`; default de expiración del YAML es 86.400.000 ms. Valor y rotación productiva no verificados. |
| Brevo | CONFIGURADO por variables | SMTP host/user/from en env sample y properties. Envío real local fue reportado en fases anteriores; prueba desde despliegue actual no verificada. |
| Supabase Storage | CONFIGURADO por variables | Perfil prod fuerza provider Supabase y bucket privado configurado. Credenciales/permisos/probe del servicio Render no verificados; smoke-test backend omitido. |
| Docker/instalable móvil | CONFIGURADO parcialmente | Compose para PostgreSQL/pgAdmin; no se encontró prueba de Docker end-to-end ni APK/AAB distribuible en la auditoría. |
| Monitoreo de disponibilidad | PENDIENTE | No existe evidencia de ventana de medición/uptime para 95 %. |

La monografía 2.9 aún contiene marcador para procedimiento/configuración y direcciones, además de credenciales de demo; el Anexo D requiere dirección pública o instalable. No presentar estos datos como prueba de que el deployment está activo.

### Condiciones mínimas del producto (Lineamientos, §4)

| N.º | Condición del lineamiento | Evidencia actual | Estado |
|---|---|---|---|
| 1 | Despliegue accesible por URL pública o instalable funcional móvil | URL indicada/configurada, pero no verificada; APK/AAB no localizado | CONFIGURADO / NO VERIFICADO |
| 2 | Autenticación y, cuando aplique, control por rol | Backend, Angular y pruebas locales de seguridad | IMPLEMENTADO localmente; producción no verificada |
| 3 | Persistencia CRUD según dominio | JPA/PostgreSQL y módulos/repositorios; Flyway habilitado | IMPLEMENTADO en código; BD productiva no verificada |
| 4 | Interfaz web adaptable | Design tokens/layout responsive en código | NO VERIFICADO visualmente contra navegadores/dispositivos en esta fase |
| 5 | Repositorio con control de versiones e historial progresivo | 69 commits, 14 fechas diferentes al consultar `git log` | VERIFICADO en repositorio local; acceso de tribunal pendiente |
| 6 | README con descripción e instrucciones locales | README raíz/backend/frontend/Flutter existen | IMPLEMENTADO; cotejar que comandos/config correspondan al estado final |
| 7 | Variables sensibles por entorno, no en repositorio | `.env` ignorado, `.env.example` con secretos vacíos; no se hizo barrido histórico exhaustivo. Monografía incluye credenciales demo en claro | PARCIAL / riesgo documental crítico |
| 8 | Validación de entrada en cliente y servidor | Angular Reactive Forms; DTO/Bean Validation y validación de archivos | IMPLEMENTADO localmente; faltan pruebas de aceptación integrales |

El resultado del ítem 5 refleja el estado local revisado, no prueba accesibilidad pública del repositorio para el tribunal. La condición 1 también requiere decidir y demostrar si la entrega móvil exige APK, URL pública web o ambos, conforme al anexo aplicable.

## 13. E2 como antecedente histórico

E2 fue un incremento intermedio: login, organizador crea evento, participante se inscribe a evento gratuito desde web y URL pública. No restringe el alcance de la versión normativa E3 ni permite marcar como “no aplica” RF-07/RF-08/RF-09 para Flutter.

Historial y código muestran trabajo posterior: el submit HTML nativo de “Presentar comprobante” se corrigió (commit `8a06f56`) y hay spec Angular que verifica la ruta de guardado; la estabilidad de opciones de modalidad del catálogo se corrigió en `fc9ce15`. Son antecedentes de corrección de bugs del frontend, no resultados actuales de E2 ni pruebas de producción. `flutter/VidiaApp/README.md` aún describe alcance E2, lo que constituye documentación de alcance móvil desactualizada frente a 2.4.1 de la monografía actual.

## 14. Matriz maestra de trazabilidad

Estados: backend y web evaluados por código/tests locales; producción no se colapsa dentro de “implementado”. Flutter refleja lo que se encontró en pantallas/repositorios, no el potencial del endpoint.

| RF | Prioridad | Backend | Angular | Flutter | Tests ejecutados | Estado global | Evidencia principal |
|---|---|---|---|---|---|---|---|
| RF-01 | MUST | IMPLEMENTADO | IMPLEMENTADO | PARCIAL (login, no registro) | backend/Angular PASS; Flutter no ejecutado | PARCIAL multiplataforma; prod no verificada | Auth controllers/services; `features/auth`; `LoginScreen/AuthService` |
| RF-02 | SHOULD | IMPLEMENTADO | IMPLEMENTADO | NO APLICA | backend/Angular PASS | IMPLEMENTADO web/backend | `UsuarioControlador/Servicio`; solicitud y admin UI |
| RF-03 | MUST | IMPLEMENTADO | IMPLEMENTADO | NO APLICA | backend/Angular PASS | IMPLEMENTADO web/backend | `EventoService`; organizer evento form |
| RF-04 | MUST | IMPLEMENTADO | IMPLEMENTADO | NO APLICA | backend/Angular PASS | IMPLEMENTADO web/backend | publish/reject endpoints; admin events |
| RF-05 | MUST | IMPLEMENTADO | IMPLEMENTADO | IMPLEMENTADO catálogo/detalle | backend/Angular PASS; Flutter resultado no verificado | IMPLEMENTADO en código; despliegue no verificado | `EventoController`; catálogo; `BackendEventoRepository` |
| RF-06 | MUST | IMPLEMENTADO | IMPLEMENTADO | PARCIAL (caso gratuito documentado) | backend/Angular PASS; Flutter no ejecutado | PARCIAL multiplataforma | `InscripcionService`; Flutter registration repositories |
| RF-07 | MUST | IMPLEMENTADO | IMPLEMENTADO | PENDIENTE | backend/Angular PASS; smoke Supabase omitido; Flutter no ejecutado | PARCIAL; web funcional en tests, móvil incompleto | `PagoController/Service`; comprobante component; no Flutter upload |
| RF-08 | MUST | IMPLEMENTADO | PARCIAL (gestión/listado, no captura participante GPS) | PENDIENTE | backend PASS; sin E2E dispositivo | PARCIAL crítica de cliente móvil | `AsistenciaService/QrAsistenciaService`; no Flutter attendance UI |
| RF-09 | SHOULD | IMPLEMENTADO | IMPLEMENTADO | PENDIENTE | backend/Angular PASS; Flutter no ejecutado | PARCIAL multiplataforma | `CertificadoService/DocumentoService`; mis-certificados UI |
| RF-10 | SHOULD | IMPLEMENTADO por vistas separadas | PARCIAL/no agregado | PARCIAL (mis inscripciones) | backend/Angular PASS; Flutter no ejecutado | PARCIAL | endpoints `mis-*`; no historial integrado |
| RF-11 | SHOULD | IMPLEMENTADO | IMPLEMENTADO | NO APLICA | backend/Angular PASS | IMPLEMENTADO web/backend | categoría CRUD/admin UI |
| RF-12 | COULD | IMPLEMENTADO | IMPLEMENTADO | NO APLICA | backend/Angular PASS | IMPLEMENTADO en código; métricas de producción no verificadas | Dashboard/reportes/export |
| RF-13 | COULD | IMPLEMENTADO | IMPLEMENTADO | NO APLICA | backend/Angular PASS; no llamada productiva | IMPLEMENTADO en código; producción no verificada | `/certificados/verificar/{codigo}`; `validacion-publica` |

### Matriz maestra RNF

| RNF | Tipo | Métrica | Implementación | Evidencia | Estado |
|---|---|---|---|---|---|
| RNF-01 | Rendimiento | 95 % ≤3 s, 20 concurrentes | Endpoint y cliente existen; no hay demostración de carga | No load report encontrado | PENDIENTE |
| RNF-02 | Seguridad | 100 % endpoints protegidos; rol insuficiente rechazado | Mecanismos backend existen y suite incluye casos | JUnit/MockMvc local; no cobertura integral ni producción | PARCIAL |
| RNF-03 | Usabilidad | 80 % usuarios completan tareas sin asistencia | UX desarrollada; no prueba con usuarios localizada | Sin protocolo/resultados | PENDIENTE |
| RNF-04 | Compatibilidad | Chrome/Edge/Firefox estables; Android ≥12 | Angular build; configuración Flutter | Sin matriz de navegador/dispositivo ni APK probado | PENDIENTE |
| RNF-05 | Disponibilidad | ≥95 % durante periodo definido | Render está configurado en URL/env | Sin uptime/periodo medido | PENDIENTE |

### Objetivos específicos → RF/RNF

| Objetivo específico | RF | RNF | Evidencia | Estado |
|---|---|---|---|---|
| OE-1 Análisis de procesos/requisitos | RF-01…RF-13 | — | Tablas de actores, RF/CA/MoSCoW y casos de uso | PARCIAL: faltan resultados exploratorios/validación si siguen como marcadores |
| OE-2 Diseño arquitectura/datos/interfaz/API | RF-01…RF-13 | RNF-01, 02, 04 | 2.4, modelo, rutas/DTO/API, tests de contrato | PARCIAL: cotejo visual/documental y P3 pendiente |
| OE-3 Implementar funcionalidades de dominio | RF-03…RF-09 | — | Backend y Angular; Flutter con catálogo/inscripción básica | PARCIAL: pagos, asistencia y certificados móviles ausentes |
| OE-4 Seguridad | RF-01…RF-04, 07…13 | RNF-02 | BCrypt/JWT/roles/ownership/validación/tests | PARCIAL: 100 % y producción no demostrados; texto 2.7 desactualizado |
| OE-5 Pruebas funcionales/no funcionales | RF-01…RF-13 | RNF-01…RNF-05 | Suites locales ejecutadas; matriz 2.8 vacía de resultados | PARCIAL; NFR y Flutter pendientes |
| OE-6 Despliegue/documentación web/móvil | RF-01…RF-13 | RNF-05 | config Render/env/Docker y docs | PARCIAL; no verificación online ni instalable móvil |

## 15. Bloqueadores E3

| Severidad | Problema | Impacto demostrable | Próxima evidencia/corrección documental o técnica |
|---|---|---|---|
| CRÍTICO | La monografía declara 2.8 sin resultados: CP-01…CP-15 siguen “Pendiente de registrar”. | No se puede demostrar formalmente cada camino feliz/error ni cumplir el formato de resultados de pruebas. | Ejecutar escenarios, enlazar artefactos y completar “obtenido/estado” sin cambiar CA. |
| CRÍTICO | RF-07 y RF-08 MUST no tienen cliente Flutter localizado; la arquitectura de la monografía asigna gestión de pagos/asistencia al móvil. | MUST y objetivo OE-3 incompletos para la plataforma web/móvil prevista. | Acordar implementación móvil y pruebas de upload/QR+GPS antes de marcar completo. |
| CRÍTICO | Credenciales de demostración en texto plano dentro de la monografía. | Contradice 2.7.5 y expone acceso de demo si las cuentas siguen activas. | Retirar/anonimizar en documento y rotar/deshabilitar cuentas si siguen válidas; no publicar valores. |
| ALTO | RNF-01 rendimiento no probado; RNF-03 usabilidad sin estudio; RNF-04 compatibilidad sin matriz; RNF-05 sin uptime. | No hay demostración de objetivos NFR medibles. | Diseñar protocolos exactamente con métricas normativas y conservar reportes. |
| ALTO | URL pública, backend, frontend, DB, correo y Storage productivos solo configurados/no verificados; salud no localizada. | No se acredita despliegue accesible actual ni demo reproducible. | Smoke tests autorizados contra URL/commit productivo; identificar endpoint de salud real, revisar Render env sin revelar secretos. |
| ALTO | README Flutter conserva límite E2 y no describe flujos móviles exigidos por arquitectura actual. | Documentación contradice 2.4.1 y oculta brechas RF-07/08/09. | Actualizar manual/README solo después de decidir alcance e implementar; E2 permanece histórico. |
| ALTO | La afirmación de monografía 2.7.1 sobre ausencia de revocación contradice `SesionUsuarioServicio` actual. | Documento no refleja controles implementados y puede llevar a defensa incorrecta. | Corregir redacción con evidencia del código y un caso de test. |
| MEDIO | RF-10 “historial” está distribuido entre módulos, no como vista integral; Flutter solo muestra inscripciones. | CA-10.2 queda parcial en móvil y UX no ofrece resumen unificado. | Verificar si vistas por módulo bastan para CA o completar la experiencia según contrato; no inventar endpoint. |
| MEDIO | Backend JPA default `ddl-auto:update`; perfil `dev` es default si no se configura perfil explícito. | Riesgo de despliegue accidental con perfil/esquema inadecuado; no sabemos el valor real Render. | Verificar settings productivos y aplicar migraciones de forma controlada. |
| MEDIO | Build Angular inicial 889,33 kB excede presupuesto 500 kB; warnings CSS. | Riesgo de carga inicial relacionado con RNF-01, pero no demuestra incumplimiento de la métrica ≤3 s. | Medir primero y optimizar con evidencia, sin declarar causalidad solo por budget. |
| BAJO | RF-13 y prioridades difieren del plan maestro no versionado y matriz anterior. | Posible inconsistencia de documentación auxiliar; la monografía sigue siendo norma. | Mantener esta precedencia y alinear documentación auxiliar, sin editar monografía de forma silenciosa. |
| BAJO | Formato PDF, P3 y P4 no pudieron extraerse. | Requisitos complementarios específicos de esos PDFs no quedaron cotejados. | Proporcionar texto accesible/versión DOCX o habilitar lector PDF local para completar esta parte. |

## 16. Recomendación de siguiente fase

1. Congelar esta extracción normativa: 13 RF, 7 MUST, 4 SHOULD, 2 COULD; 53,85 % MUST. No fusionar RF-13 ni tomar la lista del Plan Maestro como sustituto.
2. Resolver primero el riesgo documental de credenciales, completar placeholders y corregir la frase obsoleta de revocación en la monografía con aprobación del autor/tutor.
3. Completar ejecución del cliente Flutter para RF-07, RF-08 y RF-09 conforme a la arquitectura acordada; repetir pruebas funcionales y agregar integración de cámara/ubicación con permisos y manejo de denegación.
4. Construir la matriz 2.8 por CA: feliz/error de cada MUST, 401, 403 y las cinco pruebas RNF; agregar request/response o artefacto sanitizado, versión/commit y entorno.
5. Verificar un despliegue concreto: URL frontend, API, perfil activo, health check real, conexión PostgreSQL/Flyway, CORS, JWT, correo Brevo y Storage Supabase. Guardar evidencia sin secretos.
6. Ejecutar rendimiento con 20 usuarios y umbral literal RNF-01; prueba de usabilidad RNF-03; matriz Chrome/Edge/Firefox y Android 12+ RNF-04; uptime del periodo RNF-05.
7. Cotejar P3, P4 y Formato institucional directamente cuando exista extracción legible. No declarar su cumplimiento basándose únicamente en el DOCX de lineamientos.

### Registro de auditoría

- **Commit inspeccionado:** `fc9ce15` en `main`.
- **Comandos de pruebas/build ejecutados:** backend `mvn test` con JDK 21; Angular `npm test -- --watch=false`; Angular `npm run build`; Flutter `flutter test --no-pub --reporter expanded` (interrumpido sin salida concluyente).
- **Otras inspecciones:** extracción OOXML de monografía y lineamientos DOCX en memoria; inventario `docs/monografia`; búsquedas de controllers/endpoints/tests; lectura de configuraciones y estado Git; intento `pdftotext -layout <PDF> -` sin extracción por inicialización pendiente de MiKTeX.
- **Estado Git previo y tras pruebas:** existían cambios sin commit en `.gitignore`, eliminaciones de archivos tracked y varios documentos untracked, incluidos `docs/monografia/` y `docs/plan_maestro/`. Se preservaron. El único archivo añadido por esta fase es `docs/auditoria/E3_2_MATRIZ_NORMATIVA.md`; no se crearon commits ni se hizo push.

### Inventario de fuentes/archivos consultados

**Normativos y auxiliares:**

- `docs/monografia/Lamas-monografia-F.docx`
- `docs/monografia/Lineamientos-Tecnicos-Trabajo-Final-Diplomado-Desarrollo-Web-y-Apps-Moviles.docx`
- `docs/monografia/Plantilla_Monografia_Diplomado (1).docx`
- `docs/monografia/Formato-Trabajo-Final-Diplomado-Distintas-Areas (1).pdf` (extracción intentada, fallida)
- `docs/monografia/Lineamientos-Tecnicos-Trabajo-Final-Diplomado-Desarrollo-Web-y-Apps-Moviles.pdf` (extracción intentada, fallida)
- `docs/monografia/P3_Arquitectura_API_Seguridad_Modulo4.pdf` (extracción intentada, fallida)
- `docs/monografia/P4_Pruebas_Despliegue_Defensa_Modulo4.pdf` (extracción intentada, fallida)
- `docs/07_Requerimientos_Funcionales.md`, `docs/08_Requerimientos_No_Funcionales.md`, `docs/12_Matriz_de_Trazabilidad.md`
- `docs/plan_maestro/03_requisitos.md` (no versionado; solo comparación)
- `flutter/VidiaApp/README.md`

**Configuración, seguridad y backend consultados:**

- `frontend/src/environments/environment.ts`, `frontend/src/app/core/interceptors/jwt.interceptor.ts`
- `.env.example`, `.gitignore`, `backend/src/main/resources/application.yml`, `backend/src/main/resources/application-prod.yml`, `docker/docker-compose.yml`
- `backend/src/main/java/bo/uajms/eventos/core/seguridad/SecurityConfig.java`, `JwtService.java`, `JwtPropiedades.java`, `JwtAuthenticationFilter.java`, `SesionUsuarioServicio.java`
- `backend/src/main/java/bo/uajms/eventos/modulos/usuarios/controladores/AutenticacionControlador.java`, `UsuarioControlador.java`; servicios `AutenticacionServicio.java`, `UsuarioServicio.java`, `CorreoServicio.java`, `SesionUsuarioServicio.java`
- `backend/src/main/java/bo/uajms/eventos/modulos/eventos/controladores/EventoController.java`, `servicios/EventoService.java`, `repositorios/EventoRepository.java`, `entidades/Evento.java`, `dtos/EventoDatosRequest.java`
- `backend/src/main/java/bo/uajms/eventos/modulos/inscripciones/controladores/InscripcionController.java`, `servicios/InscripcionService.java`, `repositorios/InscripcionRepository.java`
- `backend/src/main/java/bo/uajms/eventos/modulos/pagos/controladores/PagoController.java`, `servicios/PagoService.java`, `ArchivoSeguroServicio.java`, `AlmacenamientoArchivos.java`, `SupabaseS3StorageService.java`
- `backend/src/main/java/bo/uajms/eventos/modulos/asistencias/controladores/AsistenciaController.java`, `QrAsistenciaController.java`, `servicios/AsistenciaService.java`, `QrAsistenciaService.java`
- `backend/src/main/java/bo/uajms/eventos/modulos/certificados/controladores/CertificadoController.java`, `servicios/CertificadoService.java`, `CertificadoDocumentoService.java`
- `backend/src/main/java/bo/uajms/eventos/modulos/categorias/controladores/CategoriaEventoController.java`, `servicios/CategoriaEventoService.java`
- `backend/src/main/java/bo/uajms/eventos/modulos/reportes/controladores/DashboardController.java`, `servicios/DashboardService.java`, `ReporteArchivoService.java`
- `backend/src/main/java/bo/uajms/eventos/core/excepciones/ErrorRespuesta.java`, `ManejadorGlobalExcepciones.java`

**Pruebas y clientes:**

- Backend tests: `EventoControllerTest`, `EventoControllerSecurityContractTest`, `EventoServiceLifecycleTest`, `EventoServiceAuthorizationTest`, `EventoDatosRequestValidationTest`, `InscripcionControllerTest`, `InscripcionServiceAuthorizationTest`, `PagoServiceFlowTest`, `PagoServiceAuthorizationTest`, `ArchivoSeguroServicioTest`, `AsistenciaRegistroTest`, `QrAsistenciaServiceTest`, `AsistenciaApiSecurityContractTest`, `CertificadoServiceTest`, `CertificadoDocumentoServiceTest`, `CertificadoControllerAuthorizationTest`, `CategoriaEventoControllerTest`, `CategoriaEventoServiceTest`, `DashboardServiceScopeTest`, `DashboardServicePagoAuthorizationTest`, `DashboardServiceAsistenciaAuthorizationTest`, `DashboardServiceCertificadoAuthorizationTest`, `AutenticacionServicioTest`, `SesionUsuarioServicioTest`, `JwtAuthenticationFilterTest`, `SecurityErrorResponseWriterTest`, `ManejadorGlobalExcepcionesTest`, `SupabaseStorageSmokeTest`.
- Angular: `frontend/package.json`; servicios `auth.service.ts`, `evento.service.ts`, `inscripcion.service.ts`, `pago.service.ts`, `asistencia.service.ts`, `certificado.service.ts`, `dashboard.service.ts`, `categoria-evento.service.ts`, `solicitud-organizador.service.ts`; pantallas de auth, catálogo/detalle, evento-form, inscripción, pago, mis certificados, validación pública, dashboard/reportes, asistencia y solicitud/revisión de organizador; sus correspondientes `.spec.ts` incluidos por `npm test`.
- Flutter: `lib/screens/login_screen.dart`, `auth_gate.dart`, `events_screen.dart`, `event_detail_screen.dart`, `my_registrations_screen.dart`; `lib/repositories/backend_evento_repository.dart`, `backend_inscripcion_repository.dart`, `lib/services/auth_service.dart`, `lib/config/app_config.dart`; `test/auth_service_test.dart`, `evento_repository_test.dart`, `inscripcion_repository_test.dart`, `api_error_test.dart`.
