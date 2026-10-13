# Plan propuesto de cuatro sprints

## Períodos y regla histórica

Fechas proporcionadas por el usuario, no verificadas como fechas de milestones vía API:

| Sprint | Período | Foco propuesto | Filas |
|---|---|---|---|
| 0 | 11–20 septiembre 2026 | Análisis, requisitos, arquitectura, modelo y planificación | 10 existentes |
| 1 | 21–27 septiembre 2026 | Acceso, perfil, roles, solicitud y eventos/revisión | 6 nuevas |
| 2 | 28 septiembre–4 octubre 2026 | Catálogo, inscripción, pagos, asistencia, certificados, historial, categorías/reportes | 8 nuevas |
| 3 | 5–11 octubre 2026 | Pruebas, seguridad, RNF, despliegue y evoluciones posteriores | 10 nuevas |

Agrupación temática para revisión, NO registro retrospectivo de tareas ejecutadas. A 10/10 los sprints 0–2 terminaron; tareas abiertas son remanentes y deben planificarse fuera del período vencido. No falsificar inicio/fin/aceptación ni asignar vencimientos pasados automáticamente. due_date queda vacío. Solo el título de milestone Sprint 0 fue observado.

Git registra pagos/certificados/asistencia el 23/09 (Sprint 1), aunque la propuesta los agrupa en Sprint 2. Sesiones revocables/verificación tienen commit 29/09 (Sprint 2), aunque pertenecen temáticamente a autenticación. Dual 08/10 e invitaciones/formulario 09/10 son evoluciones Sprint 3. V2/V3 son 06/10 y V4/V5 09/10. Fecha de commit no acredita fecha de prueba/aceptación ni carga de trabajo del sprint. Las versiones finales no deben atribuirse como completas al sprint conceptual.

No hay evidencia de capacidad/velocidad para comprometer 33 pendientes antes del 11/10. Priorizar E2E dual, IDOR, Android QR/GPS, suites y release; conservar remanente posterior explícito. Documentación #8/#9 sigue asociada a Sprint 0 observado, con revisión transversal de cierre en Sprint 3, sin crear duplicado.

## Detalle de incidencias

## Sprint 0

### G01 — Definir requisitos funcionales y no funcionales

Equivalente: Sí: #1. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Reconciliar requisitos y alcance sin renumerar.

Requisitos: RF-01 a RF-13, RNF-01 a RNF-05.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Mantener 13 RF y 5 RNF
- [ ] Vincular cada criterio con código/prueba/pendiente

Evidencia disponible o faltante: docs/07,08,12 y DOCX existen, desactualización Flutter y falta aceptación integral.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): Ninguna.
Historial observado: 19e46a1 (2026-09-13): docs: organizar trazabilidad de requisitos del proyecto.
Clave: G01. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### G02 — Diseñar arquitectura de la plataforma

Equivalente: Sí: #2. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Documentar monolito modular y proveedores reales.

Requisitos: Transversal.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Diagrama coincide con módulos y clientes
- [ ] Registrar versiones y límites Web/Móvil
- [ ] Documentar Brevo API y Storage privado

Evidencia disponible o faltante: pom, package-lock, pubspec y Dockerfile, docs SMTP desactualizadas.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): G01.
Historial observado: Sin ejecución histórica atribuida a esta tarea.
Clave: G02. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### G03 — Diseñar modelo de datos

Equivalente: Sí: #3. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Contrastar entidades y migraciones sin modificar BD.

Requisitos: RF-02, RF-06, RF-07, RF-08, RF-09.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Mapear FK y unicidades
- [ ] Distinguir esquema versionado/desplegado
- [ ] Verificar restricción usuario_rol pendiente

Evidencia disponible o faltante: V1–V5 y repositorios, esquema producción no verificado.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): G02.
Historial observado: 6aadfd9 (2026-09-22): refactor: alinear backend con modelo oficial de entidades; cc09100 (2026-10-06): fix(db): reconciliar esquema actual con flyway.
Clave: G03. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### G04 — Diseñar interfaces y wireframes

Equivalente: Sí: #4. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Contrastar diseño con pantallas actuales.

Requisitos: RNF-03, RNF-04.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Capturas por SHA
- [ ] Loading/errores/vacíos/accesibilidad
- [ ] Mockups no son evidencia funcional

Evidencia disponible o faltante: UI y tests Web/Móvil, estudio humano pendiente.
Estado recomendado: En revisión. Prioridad: Media.
Dependencias (claves de planificación): G01.
Historial observado: e832f0e (2026-10-08): feat(flutter): reorganizar navegacion y experiencia de usuario.
Clave: G04. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### G05 — Configurar repositorio GitLab

Equivalente: Sí: #5. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Acreditar configuración básica actual del repositorio.

Requisitos: Transversal.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Repositorio accesible
- [ ] main y remoto identificados
- [ ] Registrar SHA sin secretos

Evidencia disponible o faltante: API pública proyecto y Git inspeccionados, no acredita CI/despliegue.
Estado recomendado: Hecho. Prioridad: Alta.
Dependencias (claves de planificación): Ninguna.
Historial observado: Sin ejecución histórica atribuida a esta tarea.
Clave: G05. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### G06 — Configurar tablero Scrum

Equivalente: Sí: #6. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Definir política de estados y cierre.

Requisitos: Transversal.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Una etiqueta de estado por issue
- [ ] RF/aceptación/evidencia por tarea
- [ ] Verificar columnas y permisos

Evidencia disponible o faltante: 10 issues públicos, listas tablero 401.
Estado recomendado: Por hacer. Prioridad: Alta.
Dependencias (claves de planificación): G01,G05.
Historial observado: Sin ejecución histórica atribuida a esta tarea.
Clave: G06. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### G07 — Definir actores y casos de uso

Equivalente: Sí: #7. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Reconciliar dual y administrador exclusivo.

Requisitos: RF-01, RF-02, RF-03, RF-04, RNF-02.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Tres roles exactos
- [ ] USUARIO+ORGANIZADOR explícitos
- [ ] ADMINISTRADOR no participa
- [ ] Ownership y actor público diferenciados

Evidencia disponible o faltante: SecurityConfig, UsuarioServicio, AdministradorServicio y guards, diagramas pendientes.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): G01,G02.
Historial observado: 141ccc6 (2026-10-08): feat(auth): conservar rol usuario al aprobar organizador; 9b1c66f (2026-10-09): feat(admin): implementar invitacion y activacion de administradores.
Clave: G07. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### G08 — Elaborar Perfil de Proyecto

Equivalente: Sí: #8. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Reconciliar objetivos y alcance documental.

Requisitos: Transversal.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Alcance coincide con código
- [ ] No afirmar RNF/producción sin prueba
- [ ] Referenciar versiones

Evidencia disponible o faltante: DOCX y docs disponibles, aprobación editorial pendiente.
Estado recomendado: En revisión. Prioridad: Media.
Dependencias (claves de planificación): G01,G02,G07.
Historial observado: Sin ejecución histórica atribuida a esta tarea.
Clave: G08. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### G09 — Revisar documentación del Perfil

Equivalente: Sí: #9. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Revisar discrepancias actuales y evidencia final.

Requisitos: Transversal.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Actualizar mediante autorización Flutter/correo/pruebas
- [ ] Conservar revisión documental
- [ ] No inventar fechas

Evidencia disponible o faltante: DOCX corte E3.5 desactualizado, esta tarea no lo modifica.
Estado recomendado: Por hacer. Prioridad: Alta.
Dependencias (claves de planificación): G08,V01,V04,V05,V06.
Historial observado: Sin ejecución histórica atribuida a esta tarea.
Clave: G09. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### G10 — Analizar el sistema existente

Equivalente: Sí: #10. Acción: REVISAR_EXISTENTE.

Objetivo y alcance: Reutilizar análisis y agregar diagnóstico actual.

Requisitos: Transversal.
Sprint propuesto: 0; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Separar implementado/probado/desplegado
- [ ] Registrar carencias y cambios ajenos
- [ ] Vincular SHA

Evidencia disponible o faltante: Auditoría precedente y estos archivos, análisis no acredita aceptación integral.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): G01.
Historial observado: Sin ejecución histórica atribuida a esta tarea.
Clave: G10. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

## Sprint 1

### F01 — Acreditar autenticación y recuperación de acceso

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Registro/verificación/login/reset/sesiones Web y Flutter.

Requisitos: RF-01.
Sprint propuesto: 1; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Registro asigna USUARIO
- [ ] Correo no verificado bloqueado
- [ ] Expirado/reutilizado rechazado
- [ ] Logout revoca sesión

Evidencia disponible o faltante: AutenticacionServicioTest, VerificacionCorreoServicioTest, RecuperacionContrasenaHashTest, Angular auth, Flutter auth_service/email_auth, correo E2E pendiente.
Estado recomendado: En revisión. Prioridad: Crítica.
Dependencias (claves de planificación): G07.
Historial observado: ed00c09 (2026-09-29): feat(auth): implementar verificacion de correo y sesiones revocables; aa7cbe9 (2026-10-07): feat(auth): integrar correo transaccional con Brevo API; 7457098 (2026-10-08): feat(flutter): separar verificacion y recuperacion de cuenta.
Clave: F01. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F02 — Acreditar perfil y cambio de contraseña

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Recursos de cuenta propios con servicios actuales.

Requisitos: RF-01, RNF-02.
Sprint propuesto: 1; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Leer/editar perfil propio
- [ ] Validar contraseña actual
- [ ] Bloquear datos personales ajenos

Evidencia disponible o faltante: PerfilYCambioContrasenaValidacionTest, Angular perfil y Flutter profile_screen, E2E pendiente.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): F01.
Historial observado: cb3bf15 (2026-09-22): feat: completar usuarios roles y flujo de organizador.
Clave: F02. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F03 — Acreditar roles y cuenta USUARIO + ORGANIZADOR

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Preservar identidad e historial sin jerarquía de roles.

Requisitos: RF-02, RNF-02.
Sprint propuesto: 1; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Agregar ORGANIZADOR sin eliminar USUARIO
- [ ] No duplicar rol
- [ ] Conservar ID/historial
- [ ] No adquirir ADMINISTRADOR
- [ ] Mantener ownership

Evidencia disponible o faltante: CuentaDualHistorialTest y autorización de servicios, E2E dual/IDOR pendiente.
Estado recomendado: En revisión. Prioridad: Crítica.
Dependencias (claves de planificación): F01,G03,G07.
Historial observado: 141ccc6 (2026-10-08): feat(auth): conservar rol usuario al aprobar organizador.
Clave: F03. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F04 — Acreditar solicitud enriquecida de organizador

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Formulario existente Web/Flutter y resolución administrativa.

Requisitos: RF-02.
Sprint propuesto: 1; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Motivo 30–1000 y categorías controladas
- [ ] Opcional máximo 1000
- [ ] Bloquear duplicado pendiente/ADMIN
- [ ] Rechazo y reintento autorizados

Evidencia disponible o faltante: SolicitudOrganizadorIntegrationTest 22 históricos, UsuarioServicioTest y organizer_application_test, E2E pendiente.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): F03.
Historial observado: cb3bf15 (2026-09-22): feat: completar usuarios roles y flujo de organizador; 74ff848 (2026-10-09): feat(organizador): completar solicitud en web y flutter.
Clave: F04. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F05 — Acreditar creación y gestión de eventos propios

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Gestión organizador y QR pago opcional.

Requisitos: RF-03.
Sprint propuesto: 1; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Crear BORRADOR propio
- [ ] Edición ajena/estado inválido denegada
- [ ] Revisión con datos completos
- [ ] Archivo QR pago validado

Evidencia disponible o faltante: EventoServiceLifecycleTest, AuthorizationTest, EventoQrPagoTest, CP-04 PASS histórico.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): F03.
Historial observado: e0000e7 (2026-09-23): feat: completar ciclo de gestion de eventos; 8862ee9 (2026-09-30): fix(event): corregir auditoria del dominio evento; 4fa0054 (2026-10-03): feat(event): permitir cargar qr de pago.
Clave: F05. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F06 — Acreditar revisión y ciclo administrativo de eventos

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Publicación/rechazo y transiciones existentes.

Requisitos: RF-04.
Sprint propuesto: 1; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Solo ADMIN resuelve
- [ ] Rechazo con motivo
- [ ] Corrección/reenvío
- [ ] Cancelación/finalización respetan estado/fecha

Evidencia disponible o faltante: Lifecycle 40 históricos y CP-05 PASS, negativos E2E incompletos.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): F05.
Historial observado: 9679b96 (2026-09-24): feat: habilitar flujo de organizador y revision administrativa; 8862ee9 (2026-09-30): fix(event): corregir auditoria del dominio evento.
Clave: F06. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

## Sprint 2

### F07 — Acreditar catálogo y búsqueda publicados

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Catálogo Web/Móvil con filtros reales sin inventar filtro fecha.

Requisitos: RF-05.
Sprint propuesto: 2; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Solo publicados visibles
- [ ] Texto/categoría/tipo/modalidad
- [ ] Vacío/error distinguibles
- [ ] Pago/QR cuando existen

Evidencia disponible o faltante: EventoControllerTest, Angular catálogo/detalle, Flutter evento_repository_test, multibrowser incompleto.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): F06.
Historial observado: f0ba10c (2026-10-01): feat(frontend): rediseñar catalogo de eventos Vidia; fc9ce15 (2026-10-03): fix(frontend): evitar ciclo de render en catalogo.
Clave: F07. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F08 — Acreditar inscripciones y cupos

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Inscripción gratuita/pagada y cancelación propia.

Requisitos: RF-06.
Sprint propuesto: 2; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Gratis CONFIRMADA
- [ ] Pagado PENDIENTE_PAGO y monto oficial
- [ ] Duplicado/cupo rechazados
- [ ] Carrera último cupo verificada

Evidencia disponible o faltante: InscripcionPagoFlowTest 6, EligibilityTest 7, AuthorizationTest 18 históricos, CP-06 BLOCKED.
Estado recomendado: En revisión. Prioridad: Crítica.
Dependencias (claves de planificación): F07,F01.
Historial observado: 0799a5c (2026-09-23): feat: completar flujo de pagos e inscripciones; f34f426 (2026-09-30): fix(inscripcion): corregir auditoria del dominio inscripcion.
Clave: F08. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F09 — Acreditar pagos y comprobantes privados

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Pago externo con revisión y reenvío sin gateway.

Requisitos: RF-07.
Sprint propuesto: 2; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Validar MIME/tamaño
- [ ] Aprobación confirma inscripción
- [ ] Rechazo/reenvío según estado
- [ ] Comprobante ajeno denegado
- [ ] Storage real probado

Evidencia disponible o faltante: PagoServiceFlowTest 12, AuthorizationTest 19, DescargaTest 7 históricos, smoke Supabase omitido y CP-08 BLOCKED.
Estado recomendado: En revisión. Prioridad: Crítica.
Dependencias (claves de planificación): F08.
Historial observado: 8e6737b (2026-09-30): fix(pago): corregir auditoria del dominio pago; 7f023e6 (2026-10-02): feat(storage): integrar almacenamiento de comprobantes con Supabase; 8a06f56 (2026-10-03): fix(frontend): corregir submit de comprobante de pago.
Clave: F09. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F10 — Acreditar sesiones y asistencia QR/GPS Android

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Reutilizar scanner y validación backend participante.

Requisitos: RF-08.
Sprint propuesto: 2; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Inscripción propia CONFIRMADA
- [ ] QR vigente y ventana/sesión válidas
- [ ] Distancia más precisión dentro del radio
- [ ] Duplicado rechazado
- [ ] Cuenta dual registra asistencia

Evidencia disponible o faltante: AsistenciaRegistroTest 29, QrAsistenciaServiceTest 12 históricos y mocks móviles, CP-11/22 BLOCKED por dispositivo.
Estado recomendado: En revisión. Prioridad: Crítica.
Dependencias (claves de planificación): F08,F03.
Historial observado: a8386d4 (2026-09-23): feat: implementar sesiones asistencia qr temporal y gps; 785bc3f (2026-09-30): fix(attendance): corregir auditoria de sesiones y asistencia; 141ccc6 (2026-10-08): feat(auth): conservar rol usuario al aprobar organizador.
Clave: F10. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F11 — Acreditar certificados y verificación pública

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Elegibilidad/PDF/consulta propia y código público.

Requisitos: RF-09, RF-13.
Sprint propuesto: 2; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] FINALIZADO/CONFIRMADA
- [ ] Pago aprobado cuando aplica
- [ ] Curricular ≥80 %
- [ ] PDF válido
- [ ] Código válido/inexistente y privacidad

Evidencia disponible o faltante: CertificadoServiceTest 41, DocumentoTest 2 históricos, Angular y Flutter, código válido E2E pendiente.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): F09,F10.
Historial observado: 9bebc0a (2026-09-23): feat: completar certificados y verificacion publica; 81c3116 (2026-09-30): feat(certificado): cerrar gestion y verificacion de certificados; 000a3e8 (2026-10-01): feat(frontend): completar certificados y verificacion de Vidia.
Clave: F11. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F12 — Acreditar historial y navegación participante

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Historia por cuatro dominios y navegación móvil existente.

Requisitos: RF-10, RNF-03.
Sprint propuesto: 2; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Datos propios reales
- [ ] Conservar ID/historial después de aprobación
- [ ] Back/logout correctos
- [ ] No agregar organizador móvil ni contadores ficticios

Evidencia disponible o faltante: history_screen, main_navigation_test, certificate_history_screen_test, validación integral pendiente.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): F08,F09,F10,F11.
Historial observado: e832f0e (2026-10-08): feat(flutter): reorganizar navegacion y experiencia de usuario; 141ccc6 (2026-10-08): feat(auth): conservar rol usuario al aprobar organizador.
Clave: F12. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F13 — Acreditar categorías administrativas

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: CRUD/normalización/restricciones existentes.

Requisitos: RF-11.
Sprint propuesto: 2; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Solo ADMIN modifica
- [ ] Duplicado normalizado bloqueado
- [ ] Referenciada no eliminable
- [ ] Errores servidor visibles

Evidencia disponible o faltante: CategoriaEventoServiceTest 16 y ControllerTest 18 históricos, formularios Angular.
Estado recomendado: En revisión. Prioridad: Media.
Dependencias (claves de planificación): F01,G03.
Historial observado: a26b228 (2026-09-29): feat(category): cerrar gestion de categorias; 82c76e3 (2026-10-06): fix(db): agregar migracion para nombre normalizado de categorias.
Clave: F13. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### F14 — Delimitar y acreditar reportes reales

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Dashboards por ownership y PDF/XLSX.

Requisitos: RF-12.
Sprint propuesto: 2; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Contrastar dataset y métricas
- [ ] Scope organizador correcto
- [ ] Exportaciones válidas
- [ ] No presentar placeholders como mediciones

Evidencia disponible o faltante: DashboardServiceScope/AuthorizationTests y ReporteArchivoServiceTest, satisfacción/encuestas/QR con placeholders.
Estado recomendado: Por hacer. Prioridad: Media.
Dependencias (claves de planificación): F08,F09,F10,F11.
Historial observado: b82580f (2026-09-30): feat(integracion): cerrar contratos backend angular y openapi; 6636a16 (2026-10-01): feat(frontend): completar experiencia del administrador Vidia.
Clave: F14. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

## Sprint 3

### V01 — Ejecutar regresión integral y E2E reproducible

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Suites en checkout limpio y entorno aislado.

Requisitos: RF-01 a RF-13.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] BE/Web/Móvil sin reducir tests
- [ ] SHA/fixture/entorno identificados
- [ ] PASS/FAIL/BLOCKED separados
- [ ] Cuenta dual E2E y evidencia sanitizada

Evidencia disponible o faltante: Backend 581 con 1 skip, Angular 310 y Flutter 137 históricos, Playwright 22 esperados/17 omitidos, sin rerun actual.
Estado recomendado: Por hacer. Prioridad: Crítica.
Dependencias (claves de planificación): F01,F02,F03,F04,F05,F06,F07,F08,F09,F10,F11,F12,F13,F14.
Historial observado: dd6e23f (2026-10-07): test(e3): incorporar pruebas automatizadas y evidencias; 74ff848 (2026-10-09): feat(organizador): completar solicitud en web y flutter; f6891ef (2026-10-10): fix(health): permitir monitorizacion HEAD sin autenticacion.
Clave: V01. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### V02 — Completar matriz de seguridad e IDOR

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Inventario endpoint por rol y propiedad.

Requisitos: RNF-02.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Ausencia/token inválido rechazados
- [ ] Rol insuficiente 403
- [ ] UUID ajeno 403/404 sin datos
- [ ] Dual no amplía permisos y ADMIN exclusivo

Evidencia disponible o faltante: JWT/sesiones/ownership tests y CP-25 PASS histórico, exhaustividad pendiente.
Estado recomendado: Por hacer. Prioridad: Crítica.
Dependencias (claves de planificación): V01,F03.
Historial observado: 5b3b664 (2026-09-22): security: corregir ownership y asistencia/control de acceso; 141ccc6 (2026-10-08): feat(auth): conservar rol usuario al aprobar organizador; 9b1c66f (2026-10-09): feat(admin): implementar invitacion y activacion de administradores.
Clave: V02. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### V03 — Medir rendimiento con 20 usuarios concurrentes

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Medir operaciones principales en entorno representativo.

Requisitos: RNF-01.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] ≥95 % solicitudes ≤3 s
- [ ] Dataset/duración/versiones
- [ ] Incluir auth/inscripción además de consultas
- [ ] Reportar percentiles

Evidencia disponible o faltante: CP-24 limitado a salud/catálogo, informes BLOCKED, umbral no acreditado.
Estado recomendado: Por hacer. Prioridad: Alta.
Dependencias (claves de planificación): V01.
Historial observado: dd6e23f (2026-10-07): test(e3): incorporar pruebas automatizadas y evidencias.
Clave: V03. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### V04 — Ejecutar protocolo de usabilidad anonimizado

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Evaluación humana Web/Móvil.

Requisitos: RNF-03.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] ≥80 % completa tareas sin asistencia
- [ ] Denominador/protocolo
- [ ] Evidencia anonimizada

Evidencia disponible o faltante: PROTOCOLO_CP26_USABILIDAD.csv y CP-26 NOT_RUN.
Estado recomendado: Por hacer. Prioridad: Media.
Dependencias (claves de planificación): F12,V01.
Historial observado: dd6e23f (2026-10-07): test(e3): incorporar pruebas automatizadas y evidencias.
Clave: V04. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### V05 — Completar compatibilidad Web y Android 12+

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Chrome/Edge/Firefox estables y Android identificado.

Requisitos: RNF-04.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Flujos por plataforma y versiones
- [ ] Sin overflow pequeña/estándar
- [ ] QR/GPS físico probado

Evidencia disponible o faltante: Chromium/WebKit parciales, Firefox BLOCKED histórico, Edge/Android sin matriz concluyente.
Estado recomendado: Por hacer. Prioridad: Alta.
Dependencias (claves de planificación): V01,F10.
Historial observado: dd6e23f (2026-10-07): test(e3): incorporar pruebas automatizadas y evidencias; e832f0e (2026-10-08): feat(flutter): reorganizar navegacion y experiencia de usuario.
Clave: V05. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### V06 — Medir disponibilidad durante ventana definida

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Sonda y registro de incidentes.

Requisitos: RNF-05.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Ventana/mantenimiento definidos
- [ ] ≥95 % calculado
- [ ] Registro exportable
- [ ] Salud no presentada como readiness BD/Storage/correo

Evidencia disponible o faltante: CP-28 BLOCKED, GET/HEAD 200 previo no acredita período.
Estado recomendado: Por hacer. Prioridad: Alta.
Dependencias (claves de planificación): V07.
Historial observado: c70c3fa (2026-10-06): feat(backend): agregar endpoint de salud de la API; f6891ef (2026-10-10): fix(health): permitir monitorizacion HEAD sin autenticacion.
Clave: V06. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### V07 — Acreditar despliegue seguro por versión

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Builds/Render/API_BASE_URL y SHA sin secretos.

Requisitos: RNF-02, RNF-05.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Builds por SHA
- [ ] API HTTPS release
- [ ] Fallback SPA
- [ ] Secretos fuera Git
- [ ] Diferencia YAML/servicio real
- [ ] Registrar 500 ruta secundaria OpenAPI

Evidencia disponible o faltante: Docker omite tests, YAML no prueba Blueprint, OpenAPI/Swagger 200 previo y api/v1/api-docs 500, SHA producción desconocido.
Estado recomendado: Por hacer. Prioridad: Crítica.
Dependencias (claves de planificación): V01,V02.
Historial observado: 393eb6f (2026-10-08): fix(frontend): configurar fallback SPA para rutas de autenticacion; bd3bada (2026-10-08): feat(android): configurar firma release y app links; f6891ef (2026-10-10): fix(health): permitir monitorizacion HEAD sin autenticacion.
Clave: V07. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### V08 — Acreditar migraciones limpias e históricas V1–V5

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Validación forward-only PostgreSQL aislado.

Requisitos: RF-02, RNF-02.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Base limpia/histórica
- [ ] No modificar aplicadas
- [ ] FK/unicidades
- [ ] Históricos nulos sin motivos inventados
- [ ] Evaluar usuario_rol sin actualización ciega

Evidencia disponible o faltante: IntegrationTests organizador/admin con migraciones históricas, esquema desplegado/duplicados no verificados.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): G03.
Historial observado: cc09100 (2026-10-06): fix(db): reconciliar esquema actual con flyway; 9b1c66f (2026-10-09): feat(admin): implementar invitacion y activacion de administradores; 74ff848 (2026-10-09): feat(organizador): completar solicitud en web y flutter.
Clave: V08. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### V09 — Acreditar invitación y activación de administradores

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Cuenta independiente y token hash de un uso.

Requisitos: RNF-02, adicional administrativo.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Solo ADMIN invita
- [ ] No convertir cuenta existente
- [ ] Resultado solo ADMIN
- [ ] Expirado/usado/revocado bloqueados
- [ ] Último activo protegido
- [ ] Correo/login reales

Evidencia disponible o faltante: AdministradorServicioTest 34 e IntegrationTest 16 históricos, Brevo real pendiente, seed intacto.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): F01,V02.
Historial observado: 9b1c66f (2026-10-09): feat(admin): implementar invitacion y activacion de administradores.
Clave: V09. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.

### V10 — Acreditar App Links de autenticación Android

Equivalente: No localizado entre diez issues públicos. Acción: PROPONER_NUEVA.

Objetivo y alcance: Verificación/reset por enlaces existentes.

Requisitos: RF-01, RNF-04.
Sprint propuesto: 3; agrupación temática, no aceptación histórica acreditada.

Criterios de aceptación:
- [ ] Host/rutas exactos
- [ ] No persistir/loguear token
- [ ] Abierta/background/cerrada
- [ ] Assetlinks/certificado correctos
- [ ] Asociación Android verificada

Evidencia disponible o faltante: AuthLinkCoordinator tests, Manifest/assetlinks publicados previo, asociación dispositivo pendiente.
Estado recomendado: En revisión. Prioridad: Alta.
Dependencias (claves de planificación): F01,V05,V07.
Historial observado: 7457098 (2026-10-08): feat(flutter): separar verificacion y recuperacion de cuenta; bd3bada (2026-10-08): feat(android): configurar firma release y app links.
Clave: V10. Corte: 2026-10-10, HEAD f6891ef3ffb5a89a31e323d37a7ae9d2f734725c.
Resultados locales no acreditan producción ni cierre del sprint.
