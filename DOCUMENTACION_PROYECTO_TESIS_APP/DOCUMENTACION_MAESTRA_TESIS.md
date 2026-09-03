# DOCUMENTACIÓN MAESTRA DEL PROYECTO

> Documento consolidado a partir de los 27 archivos Markdown preexistentes en esta carpeta. La fuente primaria para implementación es el código/configuración; la documentación narrativa y diagramas se tratan como fuentes secundarias. Estados: **IMPLEMENTADO**, **PARCIALMENTE IMPLEMENTADO**, **PROPUESTO**, **NO IMPLEMENTADO** y **NO VERIFICADO**.

## 1. Identificación del proyecto

| Campo | Información | Estado/evidencia |
|---|---|---|
| Nombre | Plataforma Eventos UAJMS | IMPLEMENTADO: `backend/pom.xml` |
| Propósito | Gestión integral de eventos universitarios | IMPLEMENTADO: `README.md`, módulos de código |
| Tipo | Aplicación web SPA con API REST | IMPLEMENTADO: `frontend/`, `backend/` |
| Estado actual | Código de frontend/backend y configuración presentes; operación real no comprobada | IMPLEMENTADO/NO VERIFICADO |

## 2. Resumen ejecutivo

El sistema centraliza el ciclo de gestión de eventos: usuarios, catálogos académicos, eventos, inscripciones, pagos con comprobante, credenciales/QR, control de acceso, asistencias, certificados, encuestas y reportes. Está implementado como una SPA Angular que consume una API Spring Boot y persiste mediante JPA en PostgreSQL configurado. La auditoría confirma el código, rutas y entidades, pero no ejecutó el sistema ni verificó datos, producción, métricas o aceptación de usuarios.

## 3. Contexto del proyecto

El repositorio contiene `backend/`, `frontend/`, `database/scripts/`, `docker/`, `docs/`, `documentacion/`, `diagramas/`, `postman/` y `prompts/`. `docs/` reúne visión, objetivos, alcance, actores, requisitos, reglas, historias, backlog, pruebas y documentación release. Docker configura PostgreSQL y pgAdmin. El historial visible contiene tres commits entre 2026-06-17 y 2026-06-20. No se encontró evidencia de uso institucional o producción.

**Evidencia:** `README.md`; `docs/01_Vision_del_Proyecto.md`; `docker/docker-compose.yml`; `14_GIT_HISTORIAL.md`.

## 4. Problema identificado

La evidencia disponible describe la necesidad de una plataforma para gestionar eventos universitarios y sus procesos asociados, evitando que esos procesos permanezcan dispersos. La implementación cubre inscripción, pagos, acceso y certificación, lo que respalda ese propósito. No se encontraron mediciones del problema previo, entrevistas, población afectada ni indicadores de impacto; por tanto, no deben afirmarse como hechos.

**Evidencia:** `README.md`, `docs/01_Vision_del_Proyecto.md`, módulos en `backend/src/main/java/bo/uajms/eventos/modulos/`.

## 5. Objetivos identificados

| Categoría | Información | Estado |
|---|---|---|
| General | Gestionar integralmente eventos universitarios | IMPLEMENTADO como propósito documentado |
| Específicos derivados | Gestionar eventos, participantes, inscripciones, pagos, accesos, certificados, encuestas y reportes | IMPLEMENTADO en código por módulos |
| Propuestos | Aplicación móvil, push, offline, pago electrónico, analítica ampliada | PROPUESTO; no existen módulos/endpoints correspondientes |
| No verificables | Objetivos institucionales medibles, indicadores y metas aprobadas | NO VERIFICADO |

**Evidencia:** `docs/02_Objetivos_Generales.md`, `docs/03_Objetivos_Especificos.md`, `05_FUNCIONALIDADES.md`, `19_ANALISIS_APP_MOVIL.md`.

## 6. Alcance

Dentro del alcance implementado están autenticación, usuarios, catálogos, eventos, inscripciones, pagos manuales con comprobante, credenciales, QR, control de acceso, asistencia, certificados, encuestas y reportes. Fuera del alcance verificable están pasarela de pago en línea, aplicación móvil, push/SMS, sincronización offline, migraciones SQL versionadas, CI/CD visible y despliegue productivo.

**Evidencia:** `docs/04_Alcance.md`, `docs/05_Fuera_de_Alcance.md`, `05_FUNCIONALIDADES.md`, `21_REQUISITOS_FALTANTES.md`.

## 7. Arquitectura

**IMPLEMENTADO:** arquitectura cliente-servidor, monolito modular por capas. Angular es el cliente SPA; HttpClient y el interceptor JWT invocan controladores REST de Spring Boot; estos delegan a servicios de negocio, repositorios JPA y entidades PostgreSQL. No hay evidencia de microservicios, broker, gateway, caché distribuida ni almacenamiento externo.

`Angular componente/ruta → servicio TypeScript → interceptor Bearer → controlador REST → servicio → repositorio JPA → PostgreSQL → DTO/HTTP`.

| Capa/componente | Responsabilidad | Evidencia |
|---|---|---|
| SPA | páginas, rutas, formularios, guardas y servicios | `frontend/src/app/` |
| API | HTTP/DTO y control de acceso | `modulos/**/controladores/` |
| Dominio | reglas y coordinación de operaciones | `modulos/**/servicios/` |
| Persistencia | consultas y almacenamiento | `repositorios/`, `entidades/` |
| Transversal | JWT, excepciones, OpenAPI, JPA y auditoría | `core/`, `comun/EntidadBase.java` |
| Infraestructura | PostgreSQL 16 y pgAdmin en Docker | `docker/docker-compose.yml` |

## 8. Tecnologías

| Tecnología | Versión | Uso | Evidencia | Estado |
|---|---:|---|---|---|
| Java | 21 | backend | `backend/pom.xml` | IMPLEMENTADO |
| Spring Boot | 3.3.0 | API REST | `backend/pom.xml` | IMPLEMENTADO |
| Spring Security, Data JPA, Validation, Mail | gestionada por Boot | seguridad, ORM, validación y correo | `pom.xml` | IMPLEMENTADO |
| PostgreSQL | 16-alpine | BD Docker | `docker/docker-compose.yml` | IMPLEMENTADO |
| Hibernate/JPA | gestionada por Boot | persistencia | `pom.xml`, entidades | IMPLEMENTADO |
| JJWT | 0.12.5 | JWT | `pom.xml` | IMPLEMENTADO |
| springdoc OpenAPI | 2.5.0 | Swagger/OpenAPI | `pom.xml` | IMPLEMENTADO |
| ZXing | 3.5.3 | QR | `pom.xml` | IMPLEMENTADO |
| iText 7 | 8.0.4 | PDFs de credencial | `pom.xml` | IMPLEMENTADO |
| Apache Tika | 2.9.2 | validación de comprobantes | `pom.xml`, `ArchivoSeguroServicio.java` | IMPLEMENTADO |
| Angular | ^21.0.0 | SPA | `frontend/package.json` | IMPLEMENTADO |
| Angular Material/CDK | ^21.0.0 | UI | `package.json` | IMPLEMENTADO |
| Tailwind CSS | ^4.1.12 | estilos | `package.json` | IMPLEMENTADO |
| TypeScript | ~5.9.3 | frontend | `package.json` | IMPLEMENTADO |
| Vitest | ^4.0.8 | pruebas declaradas | `package.json` | PARCIALMENTE IMPLEMENTADO |

Los rangos `^`/`~` son declarados; las versiones instaladas en ejecución son **NO VERIFICADAS**.

## 9. Funcionalidades

| ID | Funcionalidad | Actor | Estado | Backend | Frontend | BD | Evidencia |
|---|---|---|---|---|---|---|---|
| F01 | Registro, login y recuperación | público | IMPLEMENTADO | auth/usuarios | auth | usuario, token_recuperacion | `AutenticacionControlador` |
| F02 | Perfil/cambio de contraseña | autenticado | IMPLEMENTADO | usuarios | privado/perfil | usuario | `UsuarioControlador` |
| F03 | Facultades, carreras y categorías | admin | IMPLEMENTADO | catálogos | admin | facultades, carreras, categorias_evento | controladores CRUD |
| F04 | Catálogo y detalle de eventos | público | IMPLEMENTADO | eventos | público | eventos | `EventoController` |
| F05 | CRUD y ciclo de evento | admin/organizador | IMPLEMENTADO | eventos | admin/eventos | eventos | publicar/cancelar/finalizar |
| F06 | Inscripción y cancelación | estudiante/external/admin | IMPLEMENTADO | inscripciones | público/privado | inscripciones | `InscripcionController` |
| F07 | Pago y comprobante | participante/admin/organizador | IMPLEMENTADO | pagos | privado/admin | pagos, comprobantes_pago | `PagoController` |
| F08 | Credencial, QR y PDF | participante/admin | IMPLEMENTADO | credenciales/QR | privado/credenciales | credenciales, codigos_qr | controladores |
| F09 | Control de acceso QR | personal_control/admin/organizador | IMPLEMENTADO | control_acceso | asistencias | control_acceso | `ControlAccesoController` |
| F10 | Asistencias por evento | control/admin/organizador | IMPLEMENTADO | asistencias | lista asistencia | asistencias | `AsistenciaController` |
| F11 | Certificados y verificación | organizador/admin/participante | IMPLEMENTADO | certificados | certificados | certificados | `CertificadoController` |
| F12 | Encuestas y estadísticas | autenticado/admin/organizador | IMPLEMENTADO | encuestas | encuestas | encuestas/preguntas/respuestas | `EncuestaController` |
| F13 | Dashboard, reportes/exportación | admin/organizador | IMPLEMENTADO | reportes | reportes | consultas de dominio | `DashboardController` |

La ejecución integral de F01–F13 es **NO VERIFICADA**.

## 10. Usuarios y roles

| Rol | Capacidades verificables |
|---|---|
| ADMINISTRADOR | usuarios, catálogos, eventos, pagos, reportes y operaciones protegidas |
| ORGANIZADOR | eventos, inscritos, pagos pendientes, certificados, reportes y control |
| ESTUDIANTE | inscripción, pagos propios, credenciales y perfil |
| PARTICIPANTE_EXTERNO | registro, inscripción, pagos y credenciales |
| PARTICIPANTE | certificados |
| PERSONAL_CONTROL | QR, control, asistencias y dashboard operativo |

Modelo RBAC: entidades `Rol`, `Permiso`, `UsuarioRol`, `RolPermiso`. La matriz poblada y gestión administrativa de roles son **NO VERIFICADAS**.

## 11. Requisitos funcionales

RF-01 autenticar/registrar usuarios; RF-02 recuperar/restablecer contraseña; RF-03 gestionar perfil; RF-04 administrar facultades/carreras/categorías; RF-05 publicar/consultar/administrar eventos; RF-06 inscribir y cancelar; RF-07 registrar/adjuntar/validar/rechazar pagos; RF-08 generar/consultar/descargar credenciales y QR; RF-09 validar/autorizar/denegar acceso; RF-10 consultar asistencias; RF-11 generar/consultar/verificar certificados; RF-12 responder/analizar encuestas; RF-13 consultar/exportar dashboard/reportes. Todos son **IMPLEMENTADOS en código**, con pruebas de ejecución **NO VERIFICADAS**. 

**Evidencia:** `docs/07_Requerimientos_Funcionales.md`, controladores y `20_REQUISITOS_EXISTENTES.md`.

## 12. Requisitos no funcionales

| Área | Evidencia/estado |
|---|---|
| Seguridad | JWT stateless, BCrypt, roles, Bean Validation, restricciones de archivo: IMPLEMENTADO |
| Interoperabilidad | API REST/OpenAPI configurado: IMPLEMENTADO |
| Persistencia | PostgreSQL/JPA: IMPLEMENTADO |
| Carga de archivo | máximo 5 MB; jpg/jpeg/png/pdf: IMPLEMENTADO |
| Mantenibilidad | módulos/capas/DTO/mappers: IMPLEMENTADO como estructura |
| Rendimiento, disponibilidad, escalabilidad | NO VERIFICADO; no hay métricas/SLA/pruebas |
| Accesibilidad, compatibilidad, responsive | NO VERIFICADO |
| Push, offline, RPO/RTO | NO IMPLEMENTADO; solo PROPUESTO |

## 13. Base de datos

**Motor configurado:** PostgreSQL; Docker declara 16-alpine. Modelo JPA con 21 tablas: `usuario`, `rol`, `permiso`, `usuario_rol`, `rol_permiso`, `token_recuperacion`, `facultades`, `carreras`, `categorias_evento`, `eventos`, `inscripciones`, `pagos`, `comprobantes_pago`, `credenciales`, `codigos_qr`, `control_acceso`, `asistencias`, `certificados`, `encuestas`, `preguntas_encuesta`, `respuestas_encuesta`.

| Relación verificable | Restricción/evidencia |
|---|---|
| Facultad 1:N Carrera | `Carrera.facultad_id` |
| Categoría 1:N Evento | `Evento.categoria_id` |
| Usuario y Evento 1:N Inscripción | entidad `Inscripcion` |
| Inscripción 1:1 Credencial/Certificado/Encuesta | `unique=true` en asociaciones |
| Credencial 1:1 QR | `CodigoQr.credencial_id` único |
| Asistencia → Inscripción y usuario control | `Asistencia` |
| Control acceso → Credencial y usuario control | `ControlAcceso` |

Las entidades especifican UUID y, cuando heredan `EntidadBase`, auditoría temporal/usuario. Campos exactos, tipos, nulabilidad, enums y restricciones deben tomarse de cada clase de entidad. No se encontraron scripts SQL, índices físicos, procedimientos, triggers, datos ni backup: **NO VERIFICADOS**.

## 14. API

Los controladores definen 81 anotaciones de mapeo HTTP; algunos producen más de una URL por prefijos alternos. Hay DTOs `*Request`/`*Response` y `ResponseEntity`; OpenAPI/Swagger está configurado en `/swagger-ui.html`.

| Módulo | Endpoints consolidados | Acceso |
|---|---|---|
| Auth | POST `/api[/v1]/auth/{registro,login,recuperar-contrasena,restablecer-contrasena,resetear-contrasena}` | público |
| Usuarios | GET `/api/v1/usuarios`, `/{id}`, `/perfil`; PUT `/perfil`; POST `/cambiar-contrasena` | autenticado; lista/id admin |
| Facultades | GET, GET `/{id}`, GET `/{id}/carreras`, POST, PUT/DELETE `/{id}` | lectura por roles; escritura admin |
| Carreras | GET, GET `/{id}`, POST, PUT/DELETE `/{id}` | lectura por roles; escritura admin |
| Categorías | GET, GET `/activas`, GET `/{id}`, POST, PUT/DELETE `/{id}` | lectura por roles; escritura admin |
| Eventos | GET, GET `/publicados`, GET `/{id}`, GET `/categoria/{id}`, POST, PUT/DELETE `/{id}`, PATCH `/{id}/{publicar,cancelar,finalizar}` | público o roles |
| Inscripciones | POST; GET `/mis-inscripciones`, `/{id}`, `/evento/{eventoId}`; PATCH `/{id}/cancelar` | roles definidos |
| Pagos | POST, POST `/{id}/comprobante`; GET, `/mis-pagos`, `/pendientes`, `/{id}`; PATCH `/{id}/{validar,rechazar}` | roles definidos |
| Credenciales/QR | POST `/credenciales/generar/{inscripcionId}`; GET credencial, QR, descarga, propias; GET `/codigos-qr/validar/{contenido}` | roles definidos |
| Acceso/asistencia | POST validar-qr/autorizar/denegar; GET historial/código/documento; GET asistencias por evento | control/admin/organizador |
| Certificados | POST generar; GET por id, propios, descargar, verificación pública | roles/público según ruta |
| Encuestas | POST responder; GET por evento y estadísticas | autenticado/admin/organizador |
| Reportes | GET dashboard general/ejecutivo/academico/operativo; reportes y exportar PDF/Excel | admin/organizador/control según ruta |

Parámetros y bodies se identifican por `@PathVariable`, `@RequestParam` y DTOs en los controladores. Respuestas, errores y Swagger en ejecución son **NO VERIFICADOS**.

## 15. Frontend

Angular usa rutas diferidas y áreas: `auth` (login, registro, recuperación), públicas (landing, catálogo, detalle, inscripción), privadas (dashboard, perfil, inscripciones, pagos, credenciales), administrativas (catálogos, eventos, validación de pagos), asistencias (escaneo/historial/lista), certificados, encuestas y reportes. Servicios de dominio están en `frontend/src/app/core/services/`; `authGuard`, `roleGuard` y `jwtInterceptor` controlan navegación y Bearer. UI real, responsive, accesibilidad y manejo en ejecución son **NO VERIFICADOS**.

**Evidencia:** `app.routes.ts`, `features/**`, `core/guards/**`, `core/interceptors/jwt.interceptor.ts`.

## 16. Backend

Módulos localizados: usuarios, facultades, carreras, categorías, eventos, inscripciones, pagos, credenciales, codigo_qr, control_acceso, asistencias, certificados, encuestas y reportes (más núcleo transversal). Cada dominio dispone, según corresponda, de controlador, servicio, repositorio, entidad, DTO y mapper. Lógica relevante: `AutenticacionServicio` para credenciales/tokens; `ArchivoSeguroServicio` para comprobantes; `CertificadoService`, `CredencialService`, `ControlAccesoService`, `PagoService`, `EncuestaService` y `DashboardService` para operaciones de dominio. 

**Evidencia:** `backend/src/main/java/bo/uajms/eventos/modulos/`, `core/`, `comun/`.

## 17. Seguridad

**IMPLEMENTADO:** JWT firmado con clave Base64 y expiración configurada; sesión stateless; filtro previo al filtro estándar; BCrypt; `@PreAuthorize`; endpoints públicos explícitos; recuperación mediante token (anotación declara 30 minutos); `@Valid`; comprobantes limitados a 5 MB, extensiones/MIME permitidos y Tika.

**Riesgos evidenciados:** secreto JWT y credenciales/defaults en `.env` y `application.yml` (alto); `ddl-auto:update` y SQL visible (medio); CORS no explícito (medio); CSRF deshabilitado, coherente condicionalmente con Bearer stateless; almacenamiento local de archivos configurable.

**NO ENCONTRADO:** MFA, limitación de tasa, refresh/revocación JWT, pruebas de penetración, cabeceras explícitas y análisis de dependencias.

## 18. Flujos principales

1. Registro/login: formulario → Auth API → BCrypt/autenticación → JWT → almacenamiento/uso por interceptor.
2. Inscripción: usuario autenticado → catálogo/detalle → POST inscripción → servicio/repositorio → respuesta.
3. Pago: registro o comprobante → validación de archivo/pago → admin/organizador valida o rechaza.
4. Acceso: personal_control escanea/busca QR/código/documento → valida → autoriza/deniega → registra control; asistencia consultable por evento.
5. Certificado: admin/organizador genera por inscripción → código certificado → verificación pública.
6. Encuesta: autenticado responde → encuesta/respuestas → admin/organizador consulta estadísticas.

Los flujos son **IMPLEMENTADOS como código**; comportamiento operativo es **NO VERIFICADO**.

## 19. Diagramas existentes

14 fuentes PlantUML/WSd en `docs/design/`: 01 casos de uso general; 02 casos por módulos; 03 secuencias Auth, Certificado, Inscripción-Pago; 04 actividades Certificación, Control de Acceso, Inscripción-Pago; 05 modelo conceptual; 06 clases de análisis; 07 ER definitivo y MER; 08 arquitectura de capas y general. 

## 20. Diferencias entre diagramas y código

Coincidencias generales: arquitectura por capas/monolito modular, autenticación, inscripción-pago, certificado y control de acceso tienen código. El código incluye además facultades, carreras, categorías, credenciales, encuestas, reportes y asistencias. No se realizó parseo/render completo de cada `.wsd`; por ello no es posible afirmar elementos o relaciones incorrectas individuales. Esa comparación detallada es **NO VERIFICADA**.

## 21. Documentación existente

`docs/` contiene visión, objetivos, alcance, actores, requisitos, datos, reglas, casos de uso, historias, riesgos, restricciones, arquitectura, módulos, backlog, glosario, MDD y pruebas. `docs/release_1_0/` contiene manuales/documentos técnico, de seguridad, datos, despliegue y narrativa. Hay dos colecciones Postman (recuperación y encuestas) y una portada DOCX. Los documentos aportan intención y contexto; código/configuración prevalece para confirmar implementación.

## 22. Pruebas

**PARCIALMENTE IMPLEMENTADO:** una prueba fuente Angular: `frontend/src/app/app.spec.ts`; scripts `ng test` y Vitest declarados. Hay documentación de sprints 1.3, 2, 3, 6, 7, 8 y recuperación, y colecciones Postman. No se hallaron `*Test.java`, resultados de CI, cobertura, pruebas e2e, integración backend o seguridad. No se ejecutaron pruebas durante auditoría.

## 23. Problemas actuales

| Clase | Hallazgo | Estado |
|---|---|---|
| Error | Ningún error bloqueante confirmado sin ejecución | NO VERIFICADO |
| Riesgo | secretos/defaults versionados; ddl-auto update/SQL visible; CORS no explícito | IMPLEMENTADO como configuración/riesgo |
| Deuda | prefijos `/api` y `/api/v1` mezclados; scripts SQL no localizados; escasa prueba fuente | IMPLEMENTADO como hallazgo |
| Inconsistencia | guarda Angular de asistencias excluye ORGANIZADOR, backend lo permite | IMPLEMENTADO como diferencia |
| Incompleto | app móvil, push, offline, pago electrónico, CI/CD visible | NO IMPLEMENTADO |

## 24. Matriz de trazabilidad

| Requisito | Funcionalidad | Código | API | BD | Interfaz | Prueba | Evidencia |
|---|---|---|---|---|---|---|---|
| RF-01/02 | auth/recuperación | `AutenticacionServicio` | auth | usuario/token | auth | docs de sprint | controlador/DTO |
| RF-05 | eventos | `EventoService` | eventos | eventos/categoría | público/admin | sprint 2 | controlador |
| RF-06 | inscripción | `InscripcionService` | inscripciones | inscripciones | público/privado | sprint 3 | controlador |
| RF-07 | pagos | `PagoService` | pagos | pagos/comprobantes | pagos/admin | docs 6/7 | controlador |
| RF-08/09 | credencial/acceso | servicios dominio | credenciales/control | credencial/QR/control | asistencias | docs 6/7 | controlador |
| RF-11 | certificados | `CertificadoService` | certificados | certificados | certificados | sprint 8 | controlador |
| RF-12 | encuestas | `EncuestaService` | encuestas | encuesta/respuestas | encuestas | Postman | controlador |
| RF-13 | reportes | `DashboardService` | dashboard/reportes | consultas | reportes | no localizada | controlador |

## 25. Estado actual del proyecto

### Implementado
Arquitectura/código Angular-Spring-JPA, módulos F01–F13, JWT/RBAC, Docker PostgreSQL/pgAdmin, documentación/diagramas fuente.

### Parcialmente implementado
Evidencia de pruebas automatizadas, trazabilidad prueba-requisito y scripts/base física SQL.

### No implementado
Proyecto móvil, push, offline, pago electrónico, migraciones SQL localizadas, CI/CD visible, requisitos formales de producción.

### No verificado
Ejecución integral, producción, usuarios/datos reales, métricas, UI responsive/accesible, respuestas reales de API y cobertura.

## 26. Información relevante para la tesis

### Capítulo 1
Propósito, problema y alcance: README, visión, objetivos, módulos y documentos 01, 04, 05 de `docs/`. Faltan datos de población, mediciones y justificación validada.

### Capítulo 2
Conceptos pertinentes: SPA, REST, arquitectura por capas, monolito modular, JWT, RBAC, JPA, PostgreSQL, QR, PDF, gestión de eventos.

### Capítulo 3
Documentos de sprint, backlog, historias, MDD y Git aportan evidencia de proceso. Metodología formal aplicada es **NO VERIFICADA**.

### Capítulo 4
Arquitectura, entidades, API, UI, seguridad, módulos y tecnología están evidenciados en código.

### Capítulo 5
Documentación de pruebas y funcionalidades en código son insumos; resultados cuantitativos, cobertura y conclusiones empíricas son **NO VERIFICADOS**.

## 27. Información relevante para la aplicación móvil

### Backend y APIs reutilizables
Auth JWT, perfil, eventos, inscripciones, pagos propios, credenciales/QR, certificados, encuestas y verificación pública son **IMPLEMENTADOS** y reutilizables por API.

### Datos reutilizables
Usuarios, eventos, inscripciones, pagos, comprobantes, credenciales, QR, control/asistencia, certificados, encuestas y catálogos: **IMPLEMENTADOS** como entidades JPA.

### Autenticación reutilizable
JWT Bearer: **IMPLEMENTADO**. Almacenamiento seguro del token en dispositivo: **PROPUESTO**.

### Funcionalidades móviles potenciales
Consultar eventos, inscribirse, consultar pagos, presentar QR, escanear QR con rol de control, certificados y encuestas: **PROPUESTAS** basadas en API existente.

### APIs faltantes/funciones a desarrollar
Refresh/revocación, push, filtros/paginación estables, sincronización offline y analítica móvil: **PROPUESTAS**, no carencias confirmadas de la web.

### Limitaciones actuales
No existe proyecto móvil; no se verificaron CORS, conectividad, seguridad de dispositivo, UX móvil ni operación offline.

## 28. CONTRADICCIONES E INCONSISTENCIAS

| Información A | Información B | Mayor evidencia | Qué verificar |
|---|---|---|---|
| README: Angular 21; requisito CLI 19+ | `package.json`: Angular/CLI 21, npm 11.6.3 | `package.json` | versión requerida/instalada y README |
| Frontend asistencias: admin/personal_control | Backend permite admin/organizador/personal_control | código backend define autorización efectiva | política para ORGANIZADOR y guarda Angular |
| Frontend usa `/api/v1` | controladores mezclan `/api` y `/api/v1`, algunos ambos | controladores/configuración | contrato de ruta canónica y consumidores |
| `database/scripts` como ubicación BD | carpeta sin SQL localizado; entidades JPA definen modelo | entidades JPA | DDL físico/migraciones e índices |
| Documentos/diagramas describen pruebas y diseño | prueba fuente localizada es una y diagramas no renderizados | código/artefactos fuente | ejecución, cobertura y comparación detallada |
| Pantalla de inscripción pública | POST inscripción exige rol autenticado | `InscripcionController` | si “pública” significa catálogo o inscripción anónima |
| PORTADA.docx disponible | no se extrajo/renderizó contenido, solo metadatos | metadatos OOXML | contenido, autoría y fecha documentales |

## 29. Información faltante

Uso y usuarios reales; producción/despliegue; BD física, DDL, datos, índices, backup; ejecución de pruebas, cobertura y CI; requisitos/aprobación institucional; metodología formal; métricas; requisitos UX/móvil; accesibilidad, rendimiento, disponibilidad, seguridad avanzada; OpenAPI exportado; comparación renderizada de diagramas y portada.

## 30. Evidencias

Fuentes primarias: `backend/pom.xml`, `backend/src/main/resources/application.yml`, `backend/src/main/java/bo/uajms/eventos/**`, `frontend/package.json`, `frontend/src/app/**`, `docker/docker-compose.yml`, `.env`, `README.md`. Fuentes secundarias: `docs/**/*.md`, `docs/design/*.wsd`, `postman/*.json`, `documentacion/PORTADA.docx`, Git. Consulte `23_EVIDENCIAS.md` y los documentos 01–24 para detalle rastreable.

## 31. Glosario

| Término | Definición |
|---|---|
| SPA | aplicación Angular de una página |
| API REST | controladores Spring que exponen HTTP/JSON |
| JWT | token Bearer firmado para autenticación |
| DTO | objeto de entrada/salida |
| JPA | persistencia de entidades Java |
| RBAC | roles/permisos mediante Rol, Permiso y tablas de unión |
| QR | código asociado a credencial/control |
| Credencial | identificador de participante vinculado a inscripción |
| Certificado | documento/código verificable vinculado a inscripción |

## 32. Conclusión del análisis

El repositorio contiene una implementación sustancial y coherente de una plataforma web de eventos, con arquitectura monolítica modular por capas y una superficie funcional amplia respaldada por código. La mayor incertidumbre no es la existencia de esos módulos, sino su comprobación operacional: faltan ejecución controlada, datos/DDL físicos, evidencia de pruebas suficiente, métricas y definición formal de producción/móvil. La documentación permite iniciar una tesis técnica y diseñar un cliente móvil consumidor de la API, siempre marcando las propuestas y validando primero las inconsistencias enumeradas.
