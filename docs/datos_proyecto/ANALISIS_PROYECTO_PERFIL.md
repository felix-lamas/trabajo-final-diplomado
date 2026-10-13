# AUDITORÍA TÉCNICA Y FUNCIONAL DEL PROYECTO

**Fecha de corte:** 17 de septiembre de 2026  
**Repositorio:** `plataforma-eventos-uajms`  
**Finalidad:** insumo verificable para preparar posteriormente el Perfil de Proyecto y la monografía del diplomado.  
**Alcance de esta auditoría:** lectura estática del código fuente, configuración, documentación, colecciones Postman e historial Git. No se ejecutó el sistema, no se modificó código ni configuración y no se creó una aplicación Flutter.

## Criterio de estados

- **IMPLEMENTADO:** el comportamiento principal está desarrollado en código fuente y sus capas necesarias están presentes. No implica aceptación, prueba en ejecución ni uso productivo.
- **PARCIAL:** existe código, pero falta una parte esencial, hay una integración rota, se usan datos simulados o no se satisface toda la condición documentada.
- **CONFIGURADO PERO NO IMPLEMENTADO:** existe dependencia, carpeta, propiedad o infraestructura declarada, pero no una capacidad funcional completa.
- **DOCUMENTADO PERO NO IMPLEMENTADO:** la documentación describe la capacidad, pero no existe evidencia suficiente en código.
- **NO IMPLEMENTADO:** no existe evidencia de código o configuración funcional.
- **NO DETERMINADO EN EL REPOSITORIO:** la información requiere ejecución, validación institucional o datos externos.

La presencia de un controlador, entidad, componente o endpoint se tomó solo como evidencia estructural. Para clasificar una función se revisaron, cuando correspondía, controlador, servicio, repositorio, modelo, pantalla, cliente HTTP y reglas de seguridad.

---

# 1. IDENTIFICACIÓN DEL PROYECTO

| Elemento | Resultado de la auditoría |
|---|---|
| Nombre provisional aportado para esta fase | **Vidia**. No se encontró la palabra “Vidia” en el contenido rastreable del repositorio. |
| Nombre principal en el repositorio | **Plataforma Web para la Gestión Integral de Eventos Universitarios - UAJMS** (`README.md` y documentación). |
| Nombre del artefacto backend | **Plataforma Eventos UAJMS** (`backend/pom.xml`). |
| Nombre técnico de la aplicación | `eventos-uajms` por defecto en `application.yml`; frontend `Eventos UAJMS (Desarrollo)` en `environment.dev.ts`. |
| Nombre histórico alternativo | **Sistema Web para la Gestión Integral de Eventos Universitarios - UAJMS** en `documentacion/PORTADA.docx`. |
| Organización/ámbito | Universidad Autónoma Juan Misael Saracho; eventos académicos, científicos, culturales y de capacitación. |
| Descripción encontrada | Plataforma web para centralizar usuarios, catálogos académicos, eventos, inscripciones, pagos, control de acceso, asistencia, credenciales, certificados, encuestas y reportes. |
| Objetivo aparente | Digitalizar el ciclo de gestión de eventos y reducir la dispersión de información y los procesos manuales. Es una síntesis de README/documentación, no una medición de impacto. |
| Tipo de sistema actual | SPA web Angular que consume una API REST Spring Boot con persistencia PostgreSQL. |
| Estado actual | Implementación web amplia en código, con funciones completas, parciales y simuladas; sin validación integral, sin despliegue productivo comprobado y sin Flutter. |

El nombre “Vidia” debe considerarse una decisión externa/provisional. El repositorio aún utiliza nombres UAJMS/Eventos en código, artefactos y documentación. Cambiarlo no forma parte de esta auditoría.

**Evidencia principal:** `README.md`; `backend/pom.xml`; `backend/src/main/resources/application.yml`; `frontend/src/environments/environment.dev.ts`; `docs/01_Vision_del_Proyecto.md`; `documentacion/PORTADA.docx`.

# 2. ESTRUCTURA DEL REPOSITORIO

```text
plataforma-eventos-uajms/
├── .github/
├── .vscode/
├── backend/
├── database/
├── diagramas/
├── docker/
├── docs/
├── documentacion/
├── DOCUMENTACION_PROYECTO_TESIS_APP/
├── frontend/
├── postman/
├── prompts/
├── .env
├── .env.example
├── .gitignore
└── README.md
```

| Carpeta/archivo | Propósito real observado | Tecnologías/artefactos | Estado |
|---|---|---|---|
| `backend/` | API, seguridad, reglas de negocio y persistencia | Java 21, Spring Boot 3.3.0, Maven, JPA | **IMPLEMENTADO/PARCIAL** según módulo. |
| `frontend/` | SPA web, pantallas y clientes HTTP | Angular 21, TypeScript, Material, Tailwind | **IMPLEMENTADO/PARCIAL**. |
| `database/scripts/` | Punto de montaje para inicialización SQL | Carpeta vacía | **CONFIGURADO PERO NO IMPLEMENTADO**. |
| `docker/` | Servicios de desarrollo de BD | Compose, PostgreSQL 16, pgAdmin | **IMPLEMENTADO** para infraestructura de datos local. |
| `docs/` | Requisitos, arquitectura, pruebas, release y auditoría | Markdown | **IMPLEMENTADO** como documentación; contiene afirmaciones que no siempre coinciden con el código. |
| `documentacion/` | Documento académico histórico | `PORTADA.docx` | **DOCUMENTADO**. |
| `DOCUMENTACION_PROYECTO_TESIS_APP/` | Auditoría/documentación generada previamente | Markdown | **DOCUMENTADO**; fuente secundaria. |
| `postman/` | Colecciones de recuperación y encuestas | JSON Postman | **PARCIAL**; no cubre toda la API. |
| `diagramas/` | Directorio previsto para diagramas | Sin archivos encontrados | **CONFIGURADO PERO NO IMPLEMENTADO** en el estado actual. |
| `.github/` | Metadatos/herramientas de modernización Java | scripts bajo `modernize/` | No hay workflow CI/CD; **CONFIGURADO SIN PIPELINE**. |
| `prompts/` | Guías históricas para generación asistida | Archivos de apoyo | No constituye implementación. |
| `.env.example` | Plantilla de variables | Valores marcadores | **IMPLEMENTADO** como plantilla. |
| `.env` | Configuración local ignorada por Git | Contenido sensible potencial | Existe localmente; no se leyó ni se reproduce. |

No se encontraron `Dockerfile`, `pubspec.yaml`, workflows de CI/CD, migraciones SQL ni contrato OpenAPI exportado.

**Evidencia principal:** inventario físico del repositorio; `.gitignore`; `docker/docker-compose.yml`; `rg --files`.

# 3. BACKEND

## Tecnologías y construcción

| Elemento | Resultado | Evidencia |
|---|---|---|
| Lenguaje | Java | `backend/src/main/java` |
| Versión | Java 21 | propiedad `java.version` en `pom.xml` |
| Framework | Spring Boot | parent Maven |
| Versión | 3.3.0 | `backend/pom.xml` |
| Build | Maven | `pom.xml`; no se encontró Maven Wrapper |
| API | Spring MVC/REST | `spring-boot-starter-web`, controladores |
| ORM | Spring Data JPA/Hibernate | starter JPA, entidades/repositorios |
| BD | PostgreSQL | driver, `application.yml`, Compose |
| Seguridad | Spring Security, JWT, BCrypt | `core/seguridad` |
| Documentación API | springdoc OpenAPI 2.5.0 | dependencia y paths configurados |
| QR | ZXing 3.5.3 | `CodigoQrService` |
| PDF | iText 8.0.4 | PDF real de credencial; no de certificado |
| Archivos | Apache Tika 2.9.2 + Java NIO/ImageIO | comprobantes locales |

## Configuración

- `application.yml` importa opcionalmente `../.env` y activa por defecto el perfil nominal `dev`.
- Solo existe `application.yml`; no existen `application-dev.yml` o `application-prod.yml`.
- La conexión usa variables `DB_URL` o `DB_HOST/PORT/NAME`, `DB_USER` y `DB_PASS`.
- JPA usa por defecto `ddl-auto:update`; no existen migraciones versionadas.
- El puerto por defecto es 8080.
- Multipart está configurado a 10 MB, pero `ArchivoSeguroServicio` limita comprobantes a 5 MB.
- JWT, CORS, URL de recuperación, remitente, uploads y contraseña demo usan variables.
- `DatosInicialesSeed` es un `CommandLineRunner` sin `@Profile`; carga roles, permisos y datos demo al iniciar en cualquier perfil activo si la aplicación arranca con sus propiedades.

## Seguridad y calidad transversal

- Autenticación stateless con filtro JWT.
- Hash BCrypt.
- `@PreAuthorize` por roles.
- Bean Validation en DTO donde los controladores aplican `@Valid`.
- `ManejadorGlobalExcepciones` centraliza respuestas de error.
- CORS explícito y configurable.
- Soft delete mediante `@SQLDelete`/`@SQLRestriction` en entidades de negocio.
- Auditoría común de fechas/usuarios mediante `EntidadBase` y JPA Auditing.
- No hay pruebas Java en `src/test`.

## Estado del backend

El backend contiene 157 archivos Java, 15 controladores de API más el manejador global, 19 clases `@Service`, 21 repositorios JPA y 21 entidades. La estructura compila conceptualmente como una única aplicación; la ejecución no fue comprobada en esta auditoría. Funciones de certificados, reportes, autorización por objeto, notificaciones y auditoría de negocio están incompletas.

**Evidencia principal:** `backend/pom.xml`; `application.yml`; `EventosUajmsApplication.java`; paquetes `core`, `comun` y `modulos`.

# 4. ARQUITECTURA DEL BACKEND

## Clasificación

La evidencia corresponde a un **monolito modular por capas**:

- Existe un solo artefacto Maven/Spring Boot y un solo proceso de despliegue.
- Los dominios están separados en paquetes bajo `modulos/`.
- Todos comparten configuración, seguridad, transacciones y base de datos.
- No existen servicios independientes, gateways, brokers ni despliegues separados que sustenten microservicios.

## Capas reales

| Capa/elemento | Responsabilidad observada | Estado |
|---|---|---|
| `controladores` | Rutas HTTP, DTO de entrada/salida, códigos de respuesta y restricciones por rol | Presente en módulos funcionales. |
| `servicios` | Reglas, transacciones, acceso al usuario autenticado y coordinación de repositorios | Presente; varias reglas están simplificadas. |
| `repositorios` | Consultas Spring Data JPA | Presente. |
| `entidades` | Modelo persistente y relaciones ORM | Presente. |
| `dtos` | Contratos de entrada/salida | Presente. |
| `mappers` | Conversión entidad/DTO | Presente en varios módulos; algunos servicios mapean manualmente. |
| `core/seguridad` | JWT, filtro, `UserDetailsService` y `SecurityConfig` | Presente. |
| `core/configuracion` | OpenAPI, propiedades, CORS indirecto, seed y configuración transversal | Presente. |
| `core/excepciones` | Excepciones de negocio/recurso y manejador global | Presente. |
| `comun/EntidadBase` | UUID, auditoría común y soft delete | Presente. |
| `core/auditoria` | Carpeta para auditoría funcional | Vacía: **CONFIGURADO PERO NO IMPLEMENTADO**. |
| `modulos/notificaciones` | Estructura de controlador/servicio/repositorio/etc. | Solo directorios vacíos: **CONFIGURADO PERO NO IMPLEMENTADO**. |

## Módulos con código

Usuarios; facultades; carreras; categorías; eventos; inscripciones; pagos; credenciales; código QR; control de acceso; asistencias; certificados; encuestas y reportes. Seguridad es transversal dentro de `core`.

## Flujo técnico

```text
Angular/otro cliente
   → Controlador REST
   → Servicio transaccional/reglas
   → Repositorio Spring Data
   → Entidad JPA
   → PostgreSQL
   → DTO/HTTP
```

**Evidencia principal:** `backend/src/main/java/bo/uajms/eventos/`; dependencias del `pom.xml`; ausencia de otros artefactos de servicio.

# 5. BASE DE DATOS

## Tecnología y conexión

PostgreSQL 16 se declara en Compose y el backend usa el driver PostgreSQL. El esquema depende actualmente de Hibernate `ddl-auto:update`. `database/scripts/` está vacío: no hay Flyway, Liquibase, DDL ni migraciones para reproducir o auditar el esquema físico.

`DatosInicialesSeed` carga roles, permisos, facultades, carreras, categorías, usuarios demo, eventos, inscripciones y pagos. Es código de seed, no una migración. No está limitado por perfil.

## Entidades/tablas existentes

Todas las entidades que heredan `EntidadBase` usan UUID generado como PK y campos `fecha_creacion`, `fecha_actualizacion`, `creado_por`, `actualizado_por` y `fecha_eliminacion`.

| Entidad / tabla | Propósito | Campos principales | Relaciones/restricciones |
|---|---|---|---|
| `Usuario` / `usuario` | Cuenta/persona | correo, hash, nombres, apellidos, CI, celular, foto, tipo | Correo y CI únicos; N:1 Carrera. |
| `Rol` / `rol` | Rol de seguridad | nombre, descripción | Nombre único. |
| `Permiso` / `permiso` | Permiso catalogado | nombre, descripción | Nombre único. |
| `UsuarioRol` / `usuario_rol` | Asignación de roles | usuario, rol | N:1 a Usuario y Rol. |
| `RolPermiso` / `rol_permiso` | Asignación de permisos | rol, permiso | N:1 a Rol y Permiso. |
| `TokenRecuperacion` / `token_recuperacion` | Recuperación de contraseña | token, expiración, utilizado | Token único; N:1 Usuario. |
| `Facultad` / `facultades` | Unidad académica | nombre, descripción, estado | Nombre único; 1:N Carrera. |
| `Carrera` / `carreras` | Carrera académica | nombre, descripción, estado | N:1 Facultad. |
| `CategoriaEvento` / `categorias_evento` | Clasificación | nombre, descripción, estado | Nombre único. |
| `Evento` / `eventos` | Evento universitario | título, descripción, objetivos, modalidad, tipo, costo, fechas/horas, ubicación/enlace, cupos, carga, porcentaje, estado, imagen | N:1 Categoría; N:1 Usuario organizador. |
| `Inscripcion` / `inscripciones` | Usuario inscrito en evento | código, fecha, estado, observación | N:1 Usuario/Evento; única por usuario+evento; código único. |
| `Pago` / `pagos` | Pago declarado | monto, fecha, estado, observación | 1:1 Inscripción única; 1:1 Comprobante. |
| `ComprobantePago` / `comprobantes_pago` | Archivo de respaldo | URL local, nombre, MIME | 1:1 Pago. |
| `Credencial` / `credenciales` | Identificación de acceso | código, fecha, estado | N:1 Usuario/Evento; 1:1 Inscripción; código único. |
| `CodigoQr` / `codigos_qr` | Payload/estado QR | contenido, fecha, estado, activo | 1:1 Credencial única. |
| `ControlAcceso` / `control_acceso` | Intento/resultado de ingreso | fecha/hora, estado, observación | N:1 Credencial y Usuario de control. |
| `Asistencia` / `asistencias` | Marca de asistencia | fecha/hora, observación | N:1 Inscripción y Usuario de control. |
| `Certificado` / `certificados` | Registro verificable | código, emisión, URL verificación, estado, URL PDF | N:1 Usuario/Evento; 1:1 Inscripción; código único. |
| `Encuesta` / `encuestas` | Encuesta respondida | referencias y respuestas | Única por evento+usuario; inscripción única; 1:N Respuesta. |
| `PreguntaEncuesta` / `preguntas_encuesta` | Pregunta | texto, tipo, obligatoriedad, orden, activa | Catálogo. |
| `RespuestaEncuesta` / `respuestas_encuesta` | Respuesta | calificación, comentario | N:1 Encuesta/Pregunta; única por ambas. |

## Observaciones y límites

- Los estados reales están en enums Java; no hay constraints SQL versionados para confirmarlos físicamente.
- La documentación histórica menciona tablas `auditoria`, `notificacion` y `metodo_pago`; no existen entidades correspondientes.
- No se verificaron índices reales, tamaño, datos productivos, backups, triggers, vistas ni rendimiento.
- `ddl-auto:update` hace que la evolución del esquema no sea trazable de forma determinista.

**Evidencia principal:** entidades y repositorios; `EntidadBase.java`; `DatosInicialesSeed.java`; `application.yml`; `docker-compose.yml`; `database/scripts/`.

# 6. SEGURIDAD Y AUTENTICACIÓN

## Controles implementados

| Control | Evidencia/estado |
|---|---|
| Registro | `/api[/v1]/auth/registro`; asigna `ESTUDIANTE` a tipo interno y `PARTICIPANTE_EXTERNO` a externo. |
| Login | AuthenticationManager + `UserDetailsService`; entrega JWT. |
| JWT | JJWT 0.12.5, firma HMAC con clave Base64, subject correo y expiración configurable. |
| Sesión | `STATELESS`; sin sesión de servidor. |
| Contraseñas | BCrypt al registrar, seed y restablecer. |
| Recuperación | Token aleatorio de 32 bytes, 30 minutos y un solo uso. |
| Roles | `@PreAuthorize` en controladores; guardas Angular adicionales. |
| Permisos | Entidades/seed de permisos presentes, pero los endpoints autorizan por rol, no por permisos. |
| Validación | DTOs con Bean Validation y `@Valid` en rutas aplicables. |
| Errores | Manejador global para validación, negocio, autenticación y recurso inexistente. |
| CORS | Orígenes configurables; GET/POST/PUT/PATCH/DELETE/OPTIONS; headers limitados; sin cookies. |
| Archivos | Tamaño 5 MB, extensiones permitidas, MIME declarado, nombres aleatorios y resize de imágenes. |
| Secretos | Variables externas; `.env.example` usa marcadores; `.env` está ignorado y no rastreado. |

## Endpoints públicos efectivos según `SecurityConfig`

- `/api/auth/**` y `/api/v1/auth/**`.
- `/api/verificacion-certificados/**` y `/api/v1/verificacion-certificados/**`.
- `/api/v1/eventos/publicados`.
- `/api/v1/eventos/*`, que cubre el detalle con un segmento; no demuestra que `/categoria/{id}` sea público.
- Rutas estándar de Swagger `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`.

## Riesgos evidentes

1. **Autorización por objeto parcial:** obtener inscripción, pago, credencial o certificado por ID no comprueba siempre que el recurso pertenezca al participante autenticado. `listarInscritosEvento` contiene un TODO para validar que el organizador sea propietario.
2. **QR no firmado:** el contenido es el UUID de credencial. Hay consulta y estado de un solo uso, pero no firma, cifrado ni expiración del payload.
3. **Token en logs:** si no existe `JavaMailSender`, `CorreoServicio` registra el enlace completo de recuperación.
4. **MIME real:** Tika detecta el contenido después de la validación inicial, pero no se observa un segundo rechazo contra la lista permitida.
5. **Ruta de upload:** se normaliza, pero no se comprueba explícitamente que el destino permanezca dentro de `baseDir`.
6. **Seed global:** `DatosInicialesSeed` no está restringido a `dev`; puede crear datos demo en otro perfil.
7. **Swagger:** los paths personalizados `/api/v1/api-docs` y `/api/v1/swagger-ui.html` no coinciden con todos los matchers públicos estándar; comportamiento efectivo **NO DETERMINADO EN EL REPOSITORIO** sin ejecución.
8. **JWT en navegador:** se almacena en `localStorage`; no hay refresh, revocación, lista de bloqueo ni detección de expiración en la guarda.
9. **CSRF deshabilitado:** coherente con Bearer sin cookies, mientras el sistema mantenga ese modelo.
10. **Datos públicos:** verificación de certificado devuelve CI; no hay política de privacidad/minimización documentada.
11. No hay rate limiting, MFA, cabeceras explícitas, escaneo de dependencias ni pruebas de penetración.

Se detectó un archivo `.env` local, pero no se leyó ni se expone su contenido. Git actual no lo rastrea.

**Evidencia principal:** `SecurityConfig.java`; `JwtService.java`; `JwtAuthenticationFilter.java`; `AutenticacionServicio.java`; `CorreoServicio.java`; `ArchivoSeguroServicio.java`; `.env.example`; `.gitignore`; `git ls-files`.

# 7. ROLES Y ACTORES

## Roles implementados

| Rol | Descripción sustentada en código | Funciones permitidas | Restricciones/observaciones |
|---|---|---|---|
| `ADMINISTRADOR` | Acceso global técnico/operativo | Usuarios, catálogos, eventos, pagos, credenciales, acceso, certificados y reportes | Único rol con rutas `/admin` en web. |
| `ORGANIZADOR` | Gestiona eventos y resultados | CRUD/estado de eventos, inscritos, pagos pendientes, certificados, encuestas, reportes y acceso según backend | La web no le permite entrar a `/admin` ni a asistencias; solo reportes está alineado. No se verifica alcance por facultad o propiedad. |
| `ESTUDIANTE` | Participante interno | Consultar catálogos, inscribirse, pagos propios y credenciales | No puede usar dashboard ejecutivo, aunque el dashboard privado lo solicita. |
| `PARTICIPANTE_EXTERNO` | Participante externo | Inscripciones, pagos propios y credenciales | Igual problema del dashboard privado. |
| `PARTICIPANTE` | Rol compatible para certificados/vistas | Certificados propios y descarga | Seeder lo añade a estudiantes/externos demo; el registro normal solo asigna ESTUDIANTE o EXTERNO, por lo que no se demuestra que nuevos usuarios reciban también PARTICIPANTE. |
| `PERSONAL_CONTROL` | Control presencial | Validar/autorizar/denegar acceso, asistencia y dashboard operativo | Web de asistencias disponible; dashboard privado llama endpoint no autorizado. |

## Permisos persistidos

El seed crea nueve permisos (`usuarios:leer`, `usuarios:escribir`, `eventos:leer`, `eventos:escribir`, `catalogos:gestionar`, `pagos:validar`, `asistencias:registrar`, `certificados:emitir`, `reportes:leer`). Son **IMPLEMENTADOS como datos/modelo**, pero **NO UTILIZADOS directamente** en expresiones de autorización de endpoints.

## Roles solamente documentados

`Super Administrador`, `Administrador de Eventos`, `Coordinador de Facultad/Carrera`, `Validador Financiero`, `Registrador de Asistencia`, participante interno/externo y verificador público aparecen en `docs/06_Actores_del_Sistema.md`. El rol `VALIDADOR_FINANCIERO` no existe en el seed ni en `@PreAuthorize`; sus tareas las ejecutan `ADMINISTRADOR`/`ORGANIZADOR`. Superadministrador, administrador de eventos y coordinador tampoco son roles técnicos separados.

**Evidencia principal:** `DatosInicialesSeed.java`; todos los `@PreAuthorize`; `frontend/src/app/app.routes.ts`; `docs/06_Actores_del_Sistema.md`.

# 8. FUNCIONALIDADES DEL SISTEMA

## Usuarios y autenticación

| Funcionalidad | Estado | Evidencia |
|---|---|---|
| Registro interno/externo | **IMPLEMENTADO** | Auth controller/service; formulario Angular. |
| Login JWT | **IMPLEMENTADO** | `/auth/login`, `JwtService`, login. |
| Logout cliente | **IMPLEMENTADO** | Elimina token/usuario de `localStorage`. |
| Recuperar/restablecer contraseña | **IMPLEMENTADO/PARCIAL** | Token y pantallas existen; SMTP no está configurado en repositorio y fallback expone token en log. |
| Consultar perfil backend | **IMPLEMENTADO** | `GET /usuarios/perfil`. |
| Actualizar perfil backend | **IMPLEMENTADO** | `PUT /usuarios/perfil`. |
| Perfil web | **PARCIAL** | Solo muestra datos guardados en `localStorage`; no consume GET/PUT perfil. |
| Cambiar contraseña autenticado | Backend **IMPLEMENTADO**, web **NO IMPLEMENTADO** | Endpoint sin pantalla/cliente dedicado. |
| Listar/consultar usuarios admin | Backend **IMPLEMENTADO**, web **NO IMPLEMENTADO** | Endpoints sin módulo/pantalla de usuarios. |

## Catálogos académicos

CRUD de facultades, carreras y categorías está **IMPLEMENTADO** en backend, servicios HTTP y pantallas administrativas. Lectura de facultades/carreras también se usa en registro. No se demostró restricción por facultad para organizadores.

## Eventos

| Funcionalidad | Estado | Evidencia |
|---|---|---|
| Listar publicados y detalle | **IMPLEMENTADO** | API y pantallas públicas. |
| Búsqueda/categoría/paginación web | **IMPLEMENTADO EN CLIENTE** | Filtro local por texto/categoría y paginación de array. |
| Filtro por fecha | **DOCUMENTADO PERO NO IMPLEMENTADO** | No existe endpoint ni control en catálogo. |
| CRUD administrativo | **IMPLEMENTADO** | Controller/service y pantallas admin. |
| Publicar/cancelar/finalizar | **IMPLEMENTADO** | PATCH y acciones de pantalla. |
| Estados documentados completos | **PARCIAL** | Enum real difiere de documentos; no hay transición a `EN_CURSO`/`INSCRIPCIONES_CERRADAS`. |
| Propiedad del evento por organizador | **NO IMPLEMENTADO** | Cualquier ORGANIZADOR autorizado puede operar por ID; no se observa control de propiedad. |

## Inscripciones

- Inscripción gratuita confirmada, inscripción pagada pendiente, unicidad y decremento de cupo: **IMPLEMENTADO**.
- Consulta de inscripciones propias y detalle: **IMPLEMENTADO**, con riesgo de consulta por ID ajeno.
- Cancelación propia y restitución de cupo: **IMPLEMENTADO**.
- Listado por evento: backend **IMPLEMENTADO/PARCIAL** por TODO de autorización; cliente HTTP existe, sin pantalla que lo invoque.
- Lista de espera: **DOCUMENTADA PERO NO IMPLEMENTADA**.
- Control de concurrencia/overselling: **NO DETERMINADO EN EL REPOSITORIO**; no hay prueba concurrente ni locking explícito.

## Pagos y comprobantes

- Registro de pago para inscripción propia y evento pagado: **IMPLEMENTADO**.
- Carga local de comprobante JPG/JPEG/PNG/PDF: **IMPLEMENTADO/PARCIAL** por validación MIME y almacenamiento local.
- Pagos propios: **IMPLEMENTADO**.
- Bandeja de pendientes y aprobar/rechazar: **IMPLEMENTADO** para admin/organizador backend; pantalla solo bajo `/admin`.
- Notificación de aprobación/rechazo: **DOCUMENTADA PERO NO IMPLEMENTADA**.
- Pasarela en línea: **FUERA DE ALCANCE/NO IMPLEMENTADA**.

## Credenciales y QR

- Generar credencial solo con inscripción confirmada: **IMPLEMENTADO**.
- Generar QR y PNG: **IMPLEMENTADO**.
- PDF real de credencial con datos/QR: **IMPLEMENTADO** con iText.
- Listar credenciales propias y detalle: **IMPLEMENTADO**.
- Firma/cifrado/expiración de QR: **DOCUMENTADO PERO NO IMPLEMENTADO**.
- Autorización de propiedad en consulta/generación por ID: **PARCIAL**.

## Control de acceso y asistencia

- Validar QR, buscar por código o CI+evento, autorizar/denegar, registrar reintento, consumir QR, guardar asistencia e historial: **IMPLEMENTADO**.
- Pantallas de scanner, historial y lista: **IMPLEMENTADO**.
- Ventana horaria de 30 minutos y control de contexto temporal: **DOCUMENTADO PERO NO IMPLEMENTADO**.
- Salida/reingreso por bloques: **DOCUMENTADO/PARCIAL**; el QR queda utilizado después de una autorización.

## Certificados

- Crear registro con código y verificar públicamente: **IMPLEMENTADO** en backend.
- Exigir evento finalizado y al menos una asistencia: **IMPLEMENTADO**, pero no cumple el porcentaje configurable.
- Calcular 80 %/porcentaje del evento: **DOCUMENTADO PERO NO IMPLEMENTADO**.
- PDF de certificado: **NO IMPLEMENTADO**; se guarda una URL fija sin crear archivo.
- Emisión automática/masiva/asíncrona: **NO IMPLEMENTADA**.
- Descarga: **PARCIAL**; cambia estado y devuelve DTO/URL, no bytes.
- Generación desde web: **NO IMPLEMENTADA**; el servicio Angular declara método, pero ningún componente lo invoca.
- Verificación pública web: **PARCIAL/BLOQUEADA**; la pantalla está bajo `authGuard` y la URL generada por backend usa una ruta Angular inexistente.

## Encuestas

Respuesta única por evento/usuario, requisito de evento finalizado y asistencia, escala 1-5, comentario, listado y estadísticas: **IMPLEMENTADO** en backend y web. No tiene requisito/historia formal en la línea base original.

## Dashboards y reportes

- Dashboard ejecutivo: **PARCIAL**; usa repositorios reales, pero total de participantes equivale simplificadamente a total de usuarios.
- Dashboard operativo: **IMPLEMENTADO** a nivel de consulta.
- Dashboard académico: **PARCIAL/SIMULADO**; devuelve cifras fijas.
- Reportes JSON de eventos, participantes, pagos y certificados: **IMPLEMENTADO** como listados básicos, sin filtros documentados.
- Exportar PDF/XLSX: **PARCIAL/SIMULADO**; devuelve cadenas de texto con MIME y extensión de archivo.
- Dashboard privado de participantes: **BLOQUEADO/PARCIAL**; incluye una llamada al dashboard ejecutivo, permitido solo a admin/organizador, por lo que `forkJoin` falla para estudiante/externo/control.

## Notificaciones y auditoría

- Correo de recuperación: **IMPLEMENTADO CON DEPENDENCIA EXTERNA**.
- Notificaciones funcionales de eventos/pagos/certificados: **NO IMPLEMENTADAS**; estructura de módulo vacía.
- Auditoría de fechas/usuarios y soft delete: **IMPLEMENTADO** a nivel de entidad base.
- Bitácora central de acciones/cambios JSONB: **DOCUMENTADA PERO NO IMPLEMENTADA**; `core/auditoria` está vacío.

**Evidencia principal:** controladores, servicios, entidades, repositorios y componentes de cada módulo; `docs/07_Requerimientos_Funcionales.md`; `docs/09_Reglas_De_Negocio.md`.

# 9. API / ENDPOINTS

Se localizaron 81 operaciones en 15 controladores de API. `/api[/v1]` significa que existen ambos alias. “Autenticado” significa que no hay anotación de rol local pero el filtro global exige JWT. Los estados reflejan el comportamiento, no solo la existencia del método.

| Método | Endpoint | Auth | Rol/permiso | Función | Estado |
|---|---|---|---|---|---|
| POST | `/api[/v1]/auth/registro` | No | Público | Registrar usuario | **IMPLEMENTADO** |
| POST | `/api[/v1]/auth/login` | No | Público | Autenticar y emitir JWT | **IMPLEMENTADO** |
| POST | `/api[/v1]/auth/recuperar-contrasena` | No | Público | Solicitar recuperación | **IMPLEMENTADO/PARCIAL** |
| POST | `/api[/v1]/auth/restablecer-contrasena` | No | Público | Restablecer con token | **IMPLEMENTADO** |
| POST | `/api[/v1]/auth/resetear-contrasena` | No | Público | Alias de restablecimiento | **IMPLEMENTADO**, sin consumidor web directo |
| GET | `/api/v1/usuarios` | Sí | ADMINISTRADOR | Listar usuarios | **IMPLEMENTADO**, sin pantalla web |
| GET | `/api/v1/usuarios/{id}` | Sí | ADMINISTRADOR | Consultar usuario | **IMPLEMENTADO**, sin pantalla web |
| GET | `/api/v1/usuarios/perfil` | Sí | Autenticado | Obtener perfil | **IMPLEMENTADO**, no consumido por web |
| PUT | `/api/v1/usuarios/perfil` | Sí | Autenticado | Actualizar perfil | **IMPLEMENTADO**, no consumido por web |
| POST | `/api/v1/usuarios/cambiar-contrasena` | Sí | Autenticado | Cambiar contraseña | **IMPLEMENTADO**, no consumido por web |
| GET | `/api/v1/facultades` | Sí | ADMINISTRADOR/ORGANIZADOR/ESTUDIANTE | Listar | **IMPLEMENTADO** |
| GET | `/api/v1/facultades/{id}` | Sí | mismos | Consultar | **IMPLEMENTADO** |
| GET | `/api/v1/facultades/{id}/carreras` | Sí | mismos | Listar carreras | **IMPLEMENTADO**, cliente declarado |
| POST | `/api/v1/facultades` | Sí | ADMINISTRADOR | Crear | **IMPLEMENTADO** |
| PUT | `/api/v1/facultades/{id}` | Sí | ADMINISTRADOR | Actualizar | **IMPLEMENTADO** |
| DELETE | `/api/v1/facultades/{id}` | Sí | ADMINISTRADOR | Eliminar lógico | **IMPLEMENTADO** |
| GET | `/api/v1/carreras` | Sí | ADMINISTRADOR/ORGANIZADOR/ESTUDIANTE | Listar | **IMPLEMENTADO** |
| GET | `/api/v1/carreras/{id}` | Sí | mismos | Consultar | **IMPLEMENTADO** |
| POST | `/api/v1/carreras` | Sí | ADMINISTRADOR | Crear | **IMPLEMENTADO** |
| PUT | `/api/v1/carreras/{id}` | Sí | ADMINISTRADOR | Actualizar | **IMPLEMENTADO** |
| DELETE | `/api/v1/carreras/{id}` | Sí | ADMINISTRADOR | Eliminar lógico | **IMPLEMENTADO** |
| GET | `/api/v1/categorias-evento` | Sí | Admin/organizador/participantes | Listar | **IMPLEMENTADO** |
| GET | `/api/v1/categorias-evento/activas` | Sí | mismos | Listar activas | **IMPLEMENTADO** |
| GET | `/api/v1/categorias-evento/{id}` | Sí | mismos | Consultar | **IMPLEMENTADO** |
| POST | `/api/v1/categorias-evento` | Sí | ADMINISTRADOR | Crear | **IMPLEMENTADO** |
| PUT | `/api/v1/categorias-evento/{id}` | Sí | ADMINISTRADOR | Actualizar | **IMPLEMENTADO** |
| DELETE | `/api/v1/categorias-evento/{id}` | Sí | ADMINISTRADOR | Eliminar lógico | **IMPLEMENTADO** |
| GET | `/api/v1/eventos/publicados` | No | Público | Catálogo | **IMPLEMENTADO** |
| GET | `/api/v1/eventos/{id}` | No | Público por matcher | Detalle | **IMPLEMENTADO** |
| GET | `/api/v1/eventos/categoria/{id}` | Sí | Autenticado por regla global | Filtrar categoría | **IMPLEMENTADO**, no consumido por web |
| GET | `/api/v1/eventos` | Sí | Admin/organizador/participantes | Listar todos | **IMPLEMENTADO** |
| POST | `/api/v1/eventos` | Sí | ADMINISTRADOR/ORGANIZADOR | Crear | **IMPLEMENTADO/PARCIAL** por propiedad |
| PUT | `/api/v1/eventos/{id}` | Sí | ADMINISTRADOR/ORGANIZADOR | Editar borrador | **IMPLEMENTADO/PARCIAL** por propiedad |
| DELETE | `/api/v1/eventos/{id}` | Sí | ADMINISTRADOR/ORGANIZADOR | Eliminar borrador | **IMPLEMENTADO/PARCIAL** por propiedad |
| PATCH | `/api/v1/eventos/{id}/publicar` | Sí | ADMINISTRADOR/ORGANIZADOR | Publicar | **IMPLEMENTADO** |
| PATCH | `/api/v1/eventos/{id}/cancelar` | Sí | ADMINISTRADOR/ORGANIZADOR | Cancelar | **IMPLEMENTADO** |
| PATCH | `/api/v1/eventos/{id}/finalizar` | Sí | ADMINISTRADOR/ORGANIZADOR | Finalizar | **IMPLEMENTADO** |
| POST | `/api/v1/inscripciones` | Sí | ADMINISTRADOR/ESTUDIANTE/EXTERNO | Inscribirse | **IMPLEMENTADO** |
| GET | `/api/v1/inscripciones/mis-inscripciones` | Sí | mismos | Listar propias | **IMPLEMENTADO** |
| GET | `/api/v1/inscripciones/{id}` | Sí | Admin/organizador/participantes | Consultar | **PARCIAL** por propiedad |
| PATCH | `/api/v1/inscripciones/{id}/cancelar` | Sí | ADMINISTRADOR/ESTUDIANTE/EXTERNO | Cancelar propia | **IMPLEMENTADO** |
| GET | `/api/v1/inscripciones/evento/{eventoId}` | Sí | ADMINISTRADOR/ORGANIZADOR | Listar inscritos | **PARCIAL**; TODO de alcance y sin pantalla |
| POST | `/api/v1/pagos` | Sí | ADMINISTRADOR/ESTUDIANTE/EXTERNO | Registrar pago | **IMPLEMENTADO** |
| POST | `/api/v1/pagos/{id}/comprobante` | Sí | mismos | Subir comprobante | **IMPLEMENTADO/PARCIAL** |
| GET | `/api/v1/pagos/mis-pagos` | Sí | mismos | Listar propios | **IMPLEMENTADO** |
| GET | `/api/v1/pagos/pendientes` | Sí | ADMINISTRADOR/ORGANIZADOR | Listar pendientes | **IMPLEMENTADO** |
| GET | `/api/v1/pagos` | Sí | ADMINISTRADOR | Listar todos | **IMPLEMENTADO**, método web no invocado |
| GET | `/api/v1/pagos/{id}` | Sí | Admin/organizador/participantes | Consultar | **PARCIAL** por propiedad; método web no invocado |
| PATCH | `/api/v1/pagos/{id}/validar` | Sí | ADMINISTRADOR/ORGANIZADOR | Aprobar | **IMPLEMENTADO** |
| PATCH | `/api/v1/pagos/{id}/rechazar` | Sí | ADMINISTRADOR/ORGANIZADOR | Rechazar | **IMPLEMENTADO** |
| POST | `/api/v1/credenciales/generar/{inscripcionId}` | Sí | ADMINISTRADOR/ESTUDIANTE/EXTERNO | Generar credencial/QR | **PARCIAL** por propiedad |
| GET | `/api/v1/credenciales/{id}` | Sí | Admin/organizador/participantes | Consultar | **PARCIAL** por propiedad |
| GET | `/api/v1/usuarios/mis-credenciales` | Sí | ADMINISTRADOR/ESTUDIANTE/EXTERNO | Listar propias | **IMPLEMENTADO** |
| GET | `/api/v1/credenciales/{id}/qr` | Sí | Admin/organizador/participantes | Obtener PNG | **PARCIAL** por propiedad/firma |
| GET | `/api/v1/credenciales/{id}/descargar` | Sí | ADMINISTRADOR/ESTUDIANTE/EXTERNO | Descargar PDF | **IMPLEMENTADO/PARCIAL** por propiedad |
| GET | `/api/v1/codigos-qr/validar/{contenido}` | Sí | ADMINISTRADOR/ORGANIZADOR | Consultar QR | **IMPLEMENTADO**, cliente declarado no invocado |
| POST | `/api[/v1]/control-acceso/validar-qr` | Sí | ADMINISTRADOR/ORGANIZADOR/CONTROL | Validar QR | **IMPLEMENTADO** |
| POST | `/api[/v1]/control-acceso/autorizar` | Sí | mismos | Autorizar y asistir | **IMPLEMENTADO** |
| POST | `/api[/v1]/control-acceso/denegar` | Sí | mismos | Denegar | **IMPLEMENTADO** |
| GET | `/api[/v1]/control-acceso/historial/{eventoId}` | Sí | mismos | Historial | **IMPLEMENTADO** |
| GET | `/api[/v1]/control-acceso/codigo/{codigo}` | Sí | mismos | Buscar código | **IMPLEMENTADO** |
| GET | `/api[/v1]/control-acceso/documento/{documento}?eventoId=` | Sí | mismos | Buscar CI/evento | **IMPLEMENTADO** |
| GET | `/api[/v1]/asistencias/evento/{id}` | Sí | ADMINISTRADOR/ORGANIZADOR/CONTROL | Listar asistencia | **IMPLEMENTADO** |
| POST | `/api[/v1]/certificados/generar/{inscripcionId}` | Sí | ADMINISTRADOR/ORGANIZADOR | Crear registro | **PARCIAL**, sin PDF/porcentaje/web |
| GET | `/api[/v1]/certificados/{id}` | Sí | ADMINISTRADOR/ORGANIZADOR/PARTICIPANTE | Consultar | **PARCIAL** por propiedad; sin uso web |
| GET | `/api[/v1]/certificados/mis-certificados` | Sí | PARTICIPANTE | Listar propios | **IMPLEMENTADO**, sujeto a asignación del rol |
| GET | `/api[/v1]/certificados/{id}/descargar` | Sí | ADMINISTRADOR/ORGANIZADOR/PARTICIPANTE | Marcar descargado | **PARCIAL**, no devuelve PDF |
| GET | `/api[/v1]/verificacion-certificados/{codigo}` | No | Público | Verificar | Backend **IMPLEMENTADO**, integración web **PARCIAL** |
| POST | `/api[/v1]/encuestas/responder` | Sí | Autenticado | Responder | **IMPLEMENTADO** |
| GET | `/api[/v1]/encuestas/evento/{id}` | Sí | ADMINISTRADOR/ORGANIZADOR | Listar respuestas | **IMPLEMENTADO** |
| GET | `/api[/v1]/encuestas/estadisticas/{id}` | Sí | ADMINISTRADOR/ORGANIZADOR | Estadísticas | **IMPLEMENTADO** |
| GET | `/api/dashboard` | Sí | ADMINISTRADOR/ORGANIZADOR | Dashboard general | **PARCIAL**, no consumido |
| GET | `/api/dashboard/ejecutivo` | Sí | ADMINISTRADOR/ORGANIZADOR | KPIs | **PARCIAL** |
| GET | `/api/dashboard/academico` | Sí | ADMINISTRADOR/ORGANIZADOR | Distribuciones | **PARCIAL/SIMULADO** |
| GET | `/api/dashboard/operativo` | Sí | ADMINISTRADOR/ORGANIZADOR/CONTROL | Operación | **IMPLEMENTADO** |
| GET | `/api/reportes/eventos` | Sí | ADMINISTRADOR/ORGANIZADOR | Listado | **IMPLEMENTADO** |
| GET | `/api/reportes/participantes` | Sí | ADMINISTRADOR/ORGANIZADOR | Listado | **IMPLEMENTADO** |
| GET | `/api/reportes/pagos` | Sí | ADMINISTRADOR/ORGANIZADOR | Listado | **IMPLEMENTADO** |
| GET | `/api/reportes/certificados` | Sí | ADMINISTRADOR/ORGANIZADOR | Listado | **IMPLEMENTADO** |
| GET | `/api/reportes/exportar/pdf?tipo=` | Sí | ADMINISTRADOR/ORGANIZADOR | Exportar | **PARCIAL/SIMULADO** |
| GET | `/api/reportes/exportar/excel?tipo=` | Sí | ADMINISTRADOR/ORGANIZADOR | Exportar | **PARCIAL/SIMULADO** |

## Contraste con Swagger y Postman

- Springdoc está configurado, pero no hay archivo OpenAPI versionado ni ejecución para confirmar el contrato.
- Solo hay colecciones Postman para recuperación de contraseña y encuestas.
- Los documentos `sprint_*_pruebas.md` contienen casos esperados, no colecciones ejecutables ni resultados.
- Documentos antiguos usan `/api`, mientras gran parte de la web usa `/api/v1`; algunos controladores aceptan ambos y reportes solo `/api`.

**Evidencia principal:** los 15 controladores; `SecurityConfig`; `OpenAPIConfig`; `postman/`; servicios Angular.

# 10. FRONTEND WEB

## Tecnología y estructura

- Angular 21, TypeScript 5.9, RxJS 7.8, Angular Material/CDK 21, Tailwind CSS 4.1 y Material Icons.
- 84 archivos TypeScript y 42 plantillas HTML bajo `src/app`.
- Estructura `core` —servicios, modelos, guards, interceptor, layout—, `features` y `shared`.
- Rutas diferidas con `loadChildren`/`loadComponent`.
- Formularios reactivos y validadores en autenticación, catálogos, eventos, pagos, scanner, reportes y encuestas.
- JWT en `localStorage`; interceptor añade Bearer a URLs de la API salvo auth.
- No hay `environment.prod.ts`; el build de producción usa el import fijo `environment.dev.ts`.
- Responsive real y accesibilidad: **NO DETERMINADO EN EL REPOSITORIO**. Hay clases/librerías, pero no pruebas.

## Pantallas y rutas reales

| Ruta | Pantalla/propósito | Rol aparente | Estado |
|---|---|---|---|
| `/` | Redirección a login | Público | **IMPLEMENTADO**; hace inalcanzable el landing también declarado en `''`. |
| `/auth/login` | Login | Público | **IMPLEMENTADO** |
| `/auth/registro` | Registro | Público | **IMPLEMENTADO** |
| `/auth/recuperar-contrasena` | Solicitud de recuperación | Público | **IMPLEMENTADO** |
| `/auth/restablecer-contrasena` | Nueva contraseña | Público | **IMPLEMENTADO** |
| `/auth/resetear-contrasena` | Alias de la misma pantalla | Público | **IMPLEMENTADO** |
| `/eventos` | Catálogo publicado, filtros locales y paginación | Público | **IMPLEMENTADO** |
| `/eventos/:id` | Detalle | Público | **IMPLEMENTADO** |
| `/eventos/:id/inscripcion` | Confirmar inscripción | Ruta pública, API exige auth | **PARCIAL**; puede recibir 401 sin sesión. |
| `''` del módulo público | Landing de eventos | Público | Componente existe, pero **BLOQUEADO POR ORDEN DE RUTAS**. |
| `/admin` | Dashboard administrativo | ADMINISTRADOR | **PARCIAL** por datos académicos simulados. |
| `/admin/facultades` | Lista/CRUD | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/facultades/nuevo` | Alta | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/facultades/editar/:id` | Edición | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/carreras` | Lista/CRUD | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/carreras/nuevo` | Alta | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/carreras/editar/:id` | Edición | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/categorias` | Lista/CRUD | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/categorias/nuevo` | Alta | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/categorias/editar/:id` | Edición | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/eventos` | Lista/filtros | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/eventos/nuevo` | Alta | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/eventos/editar/:id` | Edición | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/eventos/:id` | Detalle/estado | ADMINISTRADOR | **IMPLEMENTADO** |
| `/admin/pagos/validar` | Validar/rechazar pagos | ADMINISTRADOR | **IMPLEMENTADO**; ORGANIZADOR backend no tiene acceso web equivalente. |
| `/asistencias/escaneo` | QR y búsqueda manual | ADMINISTRADOR/CONTROL | **IMPLEMENTADO** |
| `/asistencias/historial` | Historial por evento | ADMINISTRADOR/CONTROL | **IMPLEMENTADO** |
| `/asistencias/lista` | Asistencia por evento | ADMINISTRADOR/CONTROL | **IMPLEMENTADO** |
| `/privado/dashboard` | Resumen personal | Cualquier autenticado | **PARCIAL/BLOQUEADO** para participantes por endpoint ejecutivo restringido. |
| `/privado/perfil` | Ver sesión/roles | Cualquier autenticado | **PARCIAL**; solo `localStorage`, sin editar. |
| `/privado/inscripciones` | Listar/cancelar/generar credencial | Cualquier autenticado; API restringe roles | **IMPLEMENTADO/PARCIAL** |
| `/privado/inscripciones/:id` | Detalle | Autenticado | **PARCIAL** por autorización por ID. |
| `/privado/pagos` | Pagos propios | Autenticado | **IMPLEMENTADO** |
| `/privado/pagos/registrar` | Registrar y subir comprobante | Participante | **IMPLEMENTADO** |
| `/privado/credenciales` | Credenciales propias | Autenticado | **IMPLEMENTADO** |
| `/privado/credenciales/:id` | Detalle, QR y PDF | Autenticado | **IMPLEMENTADO/PARCIAL** por ID. |
| `/certificados/mis-certificados` | Listado/“descarga” | Autenticado + rol backend PARTICIPANTE | **PARCIAL**; abre URL no generada. |
| `/certificados/verificacion` | Verificar código | Autenticado por módulo padre | **PARCIAL**; no es pública. |
| `/certificados/verificacion/:codigo` | Verificación por URL | Autenticado por módulo padre | **PARCIAL**; ruta no coincide con URL del backend. |
| `/encuestas/responder/:eventoId` | Responder | Autenticado | **IMPLEMENTADO** |
| `/encuestas/resultados/:eventoId` | Resultados | Autenticado en cliente; backend admin/organizador | **PARCIAL**; guarda de rol ausente, backend deniega a participante. |
| `/reportes/dashboard` | Paneles | ADMINISTRADOR/ORGANIZADOR | **PARCIAL** |
| `/reportes/generador` | Listados/exportaciones | ADMINISTRADOR/ORGANIZADOR | **PARCIAL/SIMULADO** |

## Pruebas frontend

Solo existe `app.spec.ts`. Comprueba creación del componente y busca `Hello, frontend`, pero `app.html` solo contiene `<router-outlet>`, por lo que la prueba es obsoleta y probablemente falla. No se ejecutó.

**Evidencia principal:** `package.json`; `angular.json`; `app.routes.ts`; módulos/rutas/componentes de `features`; `core/services`, `guards`, `interceptors`; `app.spec.ts`.

# 11. INTEGRACIÓN FRONTEND-BACKEND

## Configuración y autenticación

- `environment.dev.ts`: `http://localhost:8080/api/v1`.
- Auth, encuestas y dashboard eliminan `/v1` para consumir controladores bajo `/api`.
- El interceptor considera base `/api`, excluye rutas auth y añade `Authorization: Bearer` al resto si hay token.
- La guarda solo comprueba existencia del token; no valida expiración.
- El backend permite CORS desde `http://localhost:4200` por defecto, sin cookies.
- No existe configuración frontend productiva ni reemplazo de environment.
- Los componentes manejan errores principalmente con `subscribe.error` y `MatSnackBar`; no hay interceptor global de errores.

## Integraciones completas

Auth; facultades; carreras; categorías; catálogo/detalle/eventos admin; inscripción propia; pagos/comprobantes; credenciales/QR/PDF; control de acceso/asistencia; encuestas; listados y dashboards para roles admitidos.

## Integraciones parciales o rotas

1. Dashboard privado llama `/api/dashboard/ejecutivo`, restringido a ADMINISTRADOR/ORGANIZADOR; participantes fallan y `forkJoin` cancela el conjunto.
2. Backend genera `http://localhost:4200/publico/verificacion/{codigo}`; Angular no declara esa ruta. La ruta existente es `/certificados/verificacion/:codigo` y está detrás de `authGuard`.
3. Certificado “descargar” retorna DTO y URL fija; web abre esa URL, no descarga un archivo producido por la API.
4. Reportes PDF/XLSX abren endpoints que retornan contenido simulado.
5. ORGANIZADOR tiene permisos backend para eventos, pagos y asistencias, pero frontend `/admin` es solo ADMINISTRADOR y `/asistencias` lo excluye.
6. Resultados de encuestas no tienen `roleGuard`; un participante puede navegar, pero backend responde 403.
7. La pantalla de inscripción es pública, pero el endpoint exige rol autenticado.

## Endpoints sin uso web localizado

Sin cliente/pantalla: usuarios listar/ID/perfil/actualizar/cambiar contraseña; evento por categoría; dashboard general. Con método de servicio declarado pero sin invocación desde componentes: listar carreras por facultad, inscritos por evento, pagos todos/por ID, validar código QR, generar/obtener certificado. El alias `/auth/resetear-contrasena` no se llama: el método Angular homónimo reusa `/restablecer-contrasena`.

**Evidencia principal:** `environment.dev.ts`; todos los servicios Angular; `jwt.interceptor.ts`; guards; controladores y `SecurityConfig`.

# 12. FLUJO FUNCIONAL ACTUAL

| Flujo sustentado | Secuencia | Estado |
|---|---|---|
| Registro y login | Registro → BCrypt/rol → JWT → sesión local → rutas privadas | **COMPLETO EN CÓDIGO**; prueba operativa no ejecutada. |
| Recuperación | Solicitud → token 30 min → correo o log → restablecer → token usado | **PARCIAL** por dependencia SMTP/fallback inseguro. |
| Catálogo | Eventos publicados → búsqueda/categoría local → detalle | **COMPLETO EN CÓDIGO**; sin filtro fecha. |
| Inscripción gratuita | Detalle → JWT → validar publicado/cupo/unicidad → CONFIRMADA → cupo-1 | **COMPLETO EN CÓDIGO**. |
| Inscripción pagada | Inscripción PENDIENTE_PAGO → registrar pago → comprobante → PENDIENTE_VALIDACION | **COMPLETO EN CÓDIGO**. |
| Validar pago | Bandeja admin → aprobar/rechazar → pago e inscripción cambian estado | **COMPLETO EN CÓDIGO**, sin notificación. |
| Credencial | Inscripción CONFIRMADA → generar credencial → QR → vista/PDF | **COMPLETO EN CÓDIGO**, con riesgo de propiedad y QR sin firma. |
| Acceso | QR/código/CI → ficha → autorizar/denegar → control → asistencia → QR usado | **COMPLETO EN CÓDIGO**, sin regla horaria. |
| Encuesta | Evento finalizado + inscrito + asistencia → responder una vez → estadísticas | **COMPLETO EN CÓDIGO**. |
| Certificación | Evento finalizado + alguna asistencia → registro/código → verificación | **PARCIAL**: no porcentaje, PDF, emisión masiva ni integración pública correcta. |
| Reportes | Consultar repositorios → DTO/listado → panel | **PARCIAL**: académico y archivos simulados. |
| Perfil | Login guarda usuario → pantalla lee `localStorage` | **PARCIAL**: no consulta/actualiza servidor. |
| Dashboard participante | Datos propios + dashboard ejecutivo | **BLOQUEADO** para roles de participante por autorización del endpoint. |

**Evidencia principal:** servicios de backend y frontend asociados a cada flujo.

# 13. FUNCIONALIDADES PENDIENTES

## Backend

- Certificado PDF real, almacenamiento, descarga, liberación, porcentaje de asistencia y emisión masiva/asíncrona.
- Firma/expiración contextual del QR y regla horaria de acceso.
- Notificaciones de pago, evento y certificado; módulo actual vacío.
- Auditoría funcional central; carpeta actual vacía.
- Lista de espera si se mantiene como requisito.
- Filtros reales de reportes y agregaciones académicas desde BD.
- Exportaciones PDF/XLSX válidas.
- Autorización por propiedad/organizador y alcance por facultad.
- Migraciones, índices versionados y perfiles de configuración.
- Evitar seed demo fuera de desarrollo.
- Refresh/revocación JWT, rate limit y endurecimiento documentado.

## Frontend web

- Perfil editable y cambio de contraseña.
- Administración de usuarios/roles si se confirma el alcance.
- Experiencia de ORGANIZADOR coherente con permisos backend.
- Verificación de certificados verdaderamente pública y ruta alineada.
- Dashboard privado compatible con participantes.
- Generación de certificados desde una pantalla autorizada.
- Corregir landing inaccesible, guardas de encuestas/asistencias y mensajes 401/403.
- Environment de producción y manejo global de errores.
- Pruebas unitarias/e2e, responsive y accesibilidad.

## Móvil

Todo el cliente Flutter: proyecto, navegación, pantallas, almacenamiento seguro, integración HTTP, manejo de token, QR, push, offline, pruebas y distribución.

## Pruebas, despliegue y documentación

- Pruebas backend, integración, seguridad, carga, concurrencia, e2e y aceptación.
- Pipeline CI/CD, Dockerfiles de aplicación, reverse proxy, TLS, backups y monitorización.
- Contrato OpenAPI exportado.
- Actualizar documentos que presentan simulaciones o diseño como implementación terminada.
- Recuperar/crear diagramas actuales; `diagramas/` está vacío.

Este inventario no convierte cada punto en requisito aprobado.

**Evidencia principal:** TODOs, código simulado, directorios vacíos, ausencia de artefactos y documentación de backlog/auditoría.

# 14. APLICACIÓN MÓVIL FLUTTER

**Aplicación móvil Flutter: no implementada en el estado analizado.**

No se encontró `pubspec.yaml`, código Dart, carpeta móvil, configuración Android/iOS, navegación, pantallas ni pruebas Flutter.

## API potencialmente reutilizable

- Registro/login/recuperación.
- Perfil —requiere revisar autorización y diseño móvil—.
- Catálogo/detalle de eventos.
- Inscripciones propias y cancelación.
- Registro/consulta de pagos y comprobantes.
- Credenciales, QR y PDF.
- Certificados propios y verificación pública, una vez corregidos.
- Encuestas.
- Control de acceso/asistencia para personal autorizado.

Antes de Flutter deben definirse requisitos móviles, roles, endpoints canónicos, manejo seguro del token, refresh/revocación, paginación, errores, política offline, push y seguridad del QR. Reutilizar la API es arquitectónicamente posible; su idoneidad productiva **NO ESTÁ DEMOSTRADA**.

**Evidencia principal:** ausencia de artefactos Flutter; `DOCUMENTACION_PROYECTO_TESIS_APP/19_ANALISIS_APP_MOVIL.md`; controladores existentes.

# 15. DESPLIEGUE

| Elemento | Estado actual |
|---|---|
| Docker Compose | **IMPLEMENTADO** para PostgreSQL y pgAdmin. |
| PostgreSQL | Imagen `postgres:16-alpine`, puerto 5432, volumen persistente. |
| pgAdmin | Imagen sin tag fijado, puerto 5050. |
| Scripts de inicialización | Volumen configurado, carpeta vacía: **NO IMPLEMENTADOS**. |
| Backend en contenedor | **NO IMPLEMENTADO**; no hay Dockerfile ni servicio Compose. |
| Frontend en contenedor | **NO IMPLEMENTADO**. |
| Ejecución manual | Documentada: Maven en 8080 y Angular en 4200. |
| Perfil desarrollo | Nombre `dev` por defecto, pero sin archivo específico. |
| Perfil producción | **NO IMPLEMENTADO**. |
| Frontend production environment | **NO IMPLEMENTADO**; import fijo de `environment.dev.ts`. |
| Secretos | Externalizados/ignorados en Git; gestión productiva **NO DETERMINADA**. |
| CI/CD | **NO IMPLEMENTADO**; no hay workflows. |
| TLS/reverse proxy | Solo recomendado en documentación. |
| Backup/restore | Solo documentado, no automatizado. |
| Logs/monitorización | **NO IMPLEMENTADOS** como infraestructura. |

El despliegue preparado es exclusivamente de servicios de base de datos para desarrollo. La plataforma completa no está empaquetada ni configurada para producción.

**Evidencia principal:** `docker/docker-compose.yml`; `application.yml`; `angular.json`; `.env.example`; ausencia de Dockerfiles/workflows.

# 16. PRUEBAS

| Tipo | Ubicación | Alcance | Estado |
|---|---|---|---|
| Unitarias backend | No encontradas | Ninguno | **NO IMPLEMENTADAS** |
| Integración backend | No encontradas | Ninguno | **NO IMPLEMENTADAS** |
| Frontend | `frontend/src/app/app.spec.ts` | Crear App y título scaffold | **PARCIAL/OBSOLETA**; no coincide con plantilla. |
| API Postman recuperación | `postman/recuperacion_contrasena...json` | Recuperación | Colección existente; ejecución **NO DETERMINADA**. |
| API Postman encuestas | `postman/encuestas_satisfaccion...json` | Encuestas | Colección existente; ejecución **NO DETERMINADA**. |
| Casos documentados | `docs/sprint_*_pruebas.md` | Catálogos, eventos, inscripciones, acceso, certificados y reportes | Especificaciones esperadas, no resultados. |
| Recuperación documentada | `docs/pruebas_recuperacion_contrasena.md` | Casos positivos/negativos | Documentada; ejecución no acreditada. |
| E2E | No encontradas | Ninguno | **NO IMPLEMENTADAS** |
| Carga/rendimiento | No encontradas | Ninguno | **NO IMPLEMENTADAS** |
| Seguridad/accesibilidad | No encontradas | Ninguno | **NO IMPLEMENTADAS** |
| CI/cobertura | No encontradas | Ninguno | **NO IMPLEMENTADAS** |

No se ejecutaron pruebas durante esta auditoría para evitar generar/modificar artefactos y porque el objetivo es documentar el estado estático.

**Evidencia principal:** inventario `*Test.java`, `*.spec.ts`, `postman/`, `docs/*pruebas*.md`, scripts de `package.json`.

# 17. DOCUMENTACIÓN EXISTENTE

| Grupo | Contenido disponible | Observación |
|---|---|---|
| README | Estructura, requisitos e inicio rápido | Útil; requisito CLI tiene inconsistencia frente a Angular 21. |
| Visión/objetivos/alcance | Problema, objetivos, alcance y fuera de alcance | Fuente para perfil, requiere validación institucional. |
| Requisitos/reglas | RF, RNF, reglas de negocio, casos e historias | Varios puntos no implementados o parciales. |
| Datos | catálogo, diccionario, convenciones, índices, estrategias | Parte es diseño deseado, no esquema comprobado. |
| Arquitectura/módulos | Monolito modular, capas y stack | Coincide en términos generales con código. |
| Backlog/sprints | Plan reciente de ocho sprints y plan histórico de cuatro | Debe conservarse la evolución. |
| Pruebas | Escenarios por sprint, recuperación y encuestas | No equivalen a resultados ejecutados. |
| Release 1.0 | Arquitectura, técnico, instalación, usuario, operación, seguridad, BD, despliegue y defensa | Algunas afirmaciones exceden la implementación. |
| Auditoría inicial | Hallazgos de septiembre 2026 | Secretos/CORS/target tienen correcciones posteriores; otros siguen abiertos. |
| Documentación IA | 28 archivos de inventario/análisis | Fuente secundaria; contiene referencias a diagramas ausentes y hallazgos ya superados. |
| Documento académico | `documentacion/PORTADA.docx` | Incluye contenido de capítulos; contiene contradicción Spring Boot/Quarkus y conclusiones prematuras. |
| Swagger | Configurado en código | Contrato/ejecución no verificados. |
| Postman | Dos colecciones | Cobertura parcial. |

Hay abundante documentación, pero debe depurarse contra el estado actual antes de reutilizarse en el Perfil o la monografía.

**Evidencia principal:** 77 archivos Markdown, un DOCX y dos colecciones Postman del repositorio.

# 18. ESTADO DEL PROYECTO

| Área | Estado | Evidencia |
|---|---|---|
| Backend | **PARCIALMENTE IMPLEMENTADO** | Amplia API modular; certificados/reportes/notificaciones/autorización incompletos. |
| Frontend web | **PARCIALMENTE IMPLEMENTADO** | 36 componentes funcionales; rutas e integraciones rotas señaladas. |
| Base de datos | **CONFIGURADA E IMPLEMENTADA POR JPA, PARCIAL EN GESTIÓN** | 21 entidades; sin migraciones/DDL/versionado. |
| Seguridad | **IMPLEMENTADA/PARCIAL** | JWT, BCrypt, RBAC y CORS; riesgos por objeto, QR, logs y seed. |
| API | **IMPLEMENTADA/PARCIAL** | 81 operaciones; versionado heterogéneo y algunas respuestas simuladas. |
| Flutter | **NO IMPLEMENTADO** | Sin artefactos Dart/Flutter. |
| Despliegue | **PARCIAL** | Solo BD/pgAdmin en Compose; aplicación no contenerizada ni productiva. |
| Pruebas | **NO IMPLEMENTADAS SUFICIENTEMENTE** | Una prueba web obsoleta; cero Java; dos colecciones parciales. |
| Documentación | **AMPLIA PERO INCONSISTENTE** | Docs, release, auditorías y documento académico. |
| Scrum | **DOCUMENTADO, EJECUCIÓN NO DETERMINADA** | Backlog/sprints sin ceremonias, duración ni resultados de aceptación. |

# 19. MATRIZ IMPLEMENTADO / PENDIENTE

| Funcionalidad | Estado | Backend | Web | Móvil | Evidencia |
|---|---|---|---|---|---|
| Registro/login JWT | **IMPLEMENTADO** | Sí | Sí | No | Auth controller/service/component |
| Recuperación | **PARCIAL** | Sí | Sí | No | Token/correo/fallback log |
| Perfil | **PARCIAL** | Consultar/editar | Solo local, lectura | No | UsuarioController/perfil component |
| Cambio de contraseña | **PARCIAL** | Sí | No | No | Endpoint sin UI |
| Usuarios/roles admin | **PARCIAL** | Lista/modelo | No | No | UsuarioController; sin pantalla |
| Facultades/carreras | **IMPLEMENTADO** | Sí | Sí | No | CRUD y formularios |
| Categorías | **IMPLEMENTADO** | Sí | Sí | No | CRUD y formularios |
| Catálogo/detalle | **IMPLEMENTADO** | Sí | Sí | No | Eventos publicados |
| Filtro fecha | **DOCUMENTADO** | No | No | No | RF documental |
| CRUD/estado evento | **IMPLEMENTADO/PARCIAL** | Sí | Admin | No | Sin control de propiedad/estados completos |
| Inscripción gratuita | **IMPLEMENTADO** | Sí | Sí | No | InscripcionService |
| Inscripción pagada | **IMPLEMENTADO** | Sí | Sí | No | Inscripción+Pago |
| Lista de espera | **NO IMPLEMENTADO** | No | No | No | Sin artefactos |
| Pago/comprobante | **IMPLEMENTADO/PARCIAL** | Sí | Sí | No | Archivo local/validación parcial |
| Validar/rechazar pago | **IMPLEMENTADO/PARCIAL** | Sí | Solo admin | No | Organizador sin UI |
| Notificación de pago | **DOCUMENTADO** | No | No | No | Módulo vacío |
| Credencial/QR | **IMPLEMENTADO/PARCIAL** | Sí | Sí | No | QR sin firma |
| PDF credencial | **IMPLEMENTADO** | Sí | Sí | No | iText/Blob |
| Control acceso | **IMPLEMENTADO/PARCIAL** | Sí | Sí | No | Sin regla horaria |
| Asistencia/historial | **IMPLEMENTADO** | Sí | Sí | No | Controllers/components |
| Certificado registro | **PARCIAL** | Sí | Lista | No | Código/URL sin archivo |
| PDF certificado | **NO IMPLEMENTADO** | No | No | No | URL fija solamente |
| Verificación certificado | **PARCIAL** | Pública | Ruta protegida/desalineada | No | API vs routing |
| Encuestas | **IMPLEMENTADO/PARCIAL** | Sí | Sí, guardas parciales | No | EncuestaService/components |
| Dashboard ejecutivo | **PARCIAL** | Sí | Admin; rompe privado | No | Conteo simplificado/roles |
| Dashboard académico | **PARCIAL/SIMULADO** | Cifras fijas | Sí | No | DashboardService |
| Dashboard operativo | **IMPLEMENTADO** | Sí | Roles parciales | No | DashboardService |
| Reportes JSON | **IMPLEMENTADO/PARCIAL** | Sí, básicos | Sí | No | Sin filtros completos |
| Exportar PDF/XLSX | **PARCIAL/SIMULADO** | Texto mock | Abre URL | No | DashboardController |
| Notificaciones | **NO IMPLEMENTADO** | Carpeta vacía | No | No | Estructura sin archivos |
| Auditoría funcional | **DOCUMENTADO** | Solo campos base | No | No | `core/auditoria` vacío |
| Migraciones | **NO IMPLEMENTADO** | No | N/A | N/A | `database/scripts` vacío |
| Docker BD | **IMPLEMENTADO** | Infraestructura | N/A | N/A | Compose |
| Docker aplicación | **NO IMPLEMENTADO** | No | No | No | Sin Dockerfiles |
| Pruebas automatizadas | **NO IMPLEMENTADO SUFICIENTEMENTE** | Cero | Una obsoleta | No | Inventario de tests |
| Flutter | **NO IMPLEMENTADO** | API reutilizable | N/A | No | Sin `pubspec.yaml` |

# 20. INFORMACIÓN ÚTIL PARA EL PERFIL DE PROYECTO

Esta sección identifica insumos; no redacta todavía afirmaciones académicas definitivas.

| Tema futuro | Evidencia utilizable | Límite |
|---|---|---|
| Antecedentes técnicos | Stack, arquitectura y módulos actuales | No demuestra antecedentes institucionales ni comparación con sistemas previos. |
| Problema | Visión y narrativa describen dispersión/manualidad | Requiere entrevistas, datos y validación UAJMS. |
| Objetivos | `02_Objetivos_Generales.md`, `03_Objetivos_Especificos.md` | Deben revisarse frente a Vidia/Flutter y alcance aprobado. |
| RF/RNF | Documentos 07/08 y comportamiento real | Separar implementado, parcial y deseado. |
| Actores | Roles técnicos y actores documentados | Resolver equivalencias y Validador Financiero. |
| Casos de uso | `10_Casos_de_Uso.md` y flujos de código | Encuestas no tienen trazabilidad formal. |
| Arquitectura | Monolito modular Angular→REST→JPA→PostgreSQL | Flutter solo puede presentarse como fase propuesta. |
| Modelo de datos | 21 entidades, relaciones y constraints JPA | Sin esquema físico/migraciones validadas. |
| API | 81 operaciones y roles | Falta contrato OpenAPI ejecutado y versión canónica. |
| Stack | Versiones en Maven/npm/Compose | No confirma versiones productivas instaladas. |
| Seguridad | JWT, BCrypt, RBAC, CORS, archivos | Deben incluirse riesgos y no prometer seguridad probada. |
| Pruebas | Casos documentados/Postman | No hay resultados ni cobertura. |
| Despliegue | Compose de PostgreSQL/pgAdmin | No existe despliegue completo. |
| Alcance | Web y API existentes; exclusiones documentadas | Nueva app móvil requiere decisión formal. |
| Fuera de alcance | Pasarela, hardware, LMS, firma legal, móvil en fase inicial | Debe actualizarse solo con aprobación del nuevo proyecto. |

Las métricas, beneficios y conclusiones institucionales no deben redactarse como resultados hasta disponer de evidencia externa o pruebas ejecutadas.

**Evidencia principal:** documentación de visión/objetivos/alcance/requisitos; código y configuraciones analizados.

# 21. DATOS QUE NO PUEDEN OBTENERSE DEL CÓDIGO

La siguiente información es **NO DETERMINADA EN EL REPOSITORIO** y debe proporcionarla el usuario, tutor o institución:

- Nombre completo, documento, correo y datos académicos oficiales del postulante.
- Nombre del diplomado, cohorte, universidad/unidad responsable y formato exigido.
- Grupo de tutoría, tutor/docente, decisión T1 y actas/aprobaciones.
- Título académico definitivo y aprobación del nombre “Vidia”.
- Integrantes/autores definitivos; la portada histórica contiene cuatro nombres, pero no confirma la autoría actual.
- Lugar, gestión, fechas de inicio/fin y cronograma aprobado.
- Product Owner, Scrum Master, equipo, disponibilidad y responsabilidades.
- Duración real de sprints, ceremonias realizadas, velocidad, retrospectivas e incidencias.
- Problema institucional validado mediante entrevistas/observación.
- Procesos actuales reales, responsables, formularios y normativa UAJMS.
- Cantidad real de eventos, participantes, organizadores y usuarios esperados.
- Tiempos actuales de inscripción, pago, acceso y certificación.
- Tasas de error, duplicidad, abandono, fraude o demora del proceso actual.
- Costos actuales, presupuesto, recursos, infraestructura y restricciones institucionales.
- Población, muestra, técnicas e instrumentos de investigación.
- Resultados de encuestas/entrevistas reales y consentimiento.
- Criterios de éxito, indicadores y valores de línea base/meta.
- Prioridad y aprobación formal de cada requisito.
- Decisión sobre alcance web, móvil y relación entre fase previa y proyecto final.
- Plataformas móviles objetivo, versiones mínimas, dispositivos y conectividad.
- Necesidad real de offline, push, geolocalización, cámara y almacenamiento local.
- Política institucional de seguridad, privacidad, retención y tratamiento de CI/fotografías.
- Responsable de datos, base legal, términos de uso y consentimiento.
- Integraciones reales con SIA, correo institucional, finanzas, bancos o firma digital.
- Servidores, dominios, TLS, red, capacidad, SLA, RPO/RTO y soporte.
- Volumen real de BD, estrategia de backup/restauración y retención.
- Credenciales productivas y secretos; no deben incorporarse a la monografía.
- Estado real de ejecución, aceptación de usuarios, producción y adopción.
- Resultados de pruebas, cobertura, rendimiento, accesibilidad y seguridad.
- Evidencias visuales aprobadas, diagramas vigentes y manuales validados.
- Conclusiones y recomendaciones académicas basadas en resultados reales.

# 22. RECOMENDACIONES PARA LA SIGUIENTE FASE

Estas acciones no se ejecutan en esta auditoría:

1. **Definir alcance:** aprobar nombre, problema, beneficiarios, frontera web/móvil, exclusiones y si Flutter corresponde a una nueva fase.
2. **Levantar requisitos:** validar RF/RNF con actores; resolver roles, estados, privacidad, certificado, reportes, offline/push y criterios de aceptación.
3. **Construir Product Backlog:** convertir únicamente requisitos aprobados en historias priorizadas, con dependencias y definición de terminado.
4. **Organizar Scrum:** designar roles, duración de sprint, calendario, ceremonias, artefactos y mecanismo de evidencia.
5. **Planificar correcciones web/backend:** priorizar autorización por objeto, integración de rutas/roles, certificado real, reportes reales, notificaciones y migraciones.
6. **Preparar contrato API:** normalizar `/api/v1`, publicar OpenAPI, definir paginación/errores/versionado y probarlo antes de Flutter.
7. **Diseñar Flutter:** definir arquitectura cliente, pantallas, navegación, almacenamiento seguro, estado, cámara/QR, conectividad y estrategia offline; después consumir la API existente corregida.
8. **Crear estrategia de pruebas:** unitarias, integración, contrato, e2e web/móvil, seguridad, concurrencia, carga, accesibilidad y aceptación; registrar resultados.
9. **Actualizar documentación:** corregir contradicciones, diccionario de datos, diagramas, manuales y trazabilidad sin presentar simulaciones como resultados.
10. **Preparar despliegue:** migraciones, perfiles dev/test/prod, Dockerfiles, CI/CD, secretos, TLS, reverse proxy, backups, logs y monitorización.
11. **Recopilar datos académicos:** entrevistas, línea base, población/muestra, indicadores y aprobaciones necesarias para el Perfil.
12. **Redactar el Perfil:** hacerlo solo después de resolver alcance, datos externos y requisitos, conservando la separación entre sistema actual y trabajo propuesto.

---

## CONCLUSIÓN DE LA AUDITORÍA

El repositorio contiene una base web considerable y reutilizable: backend Spring Boot, SPA Angular, modelo JPA/PostgreSQL, autenticación JWT y módulos operativos. No constituye todavía una solución integral validada: certificados y reportes tienen implementaciones simuladas o incompletas, varias rutas/roles no están alineados, las pruebas son insuficientes y el despliegue completo no está preparado. Flutter no existe y debe tratarse como trabajo futuro. Cualquier Perfil o monografía debe distinguir explícitamente el producto web actual, las correcciones necesarias y la nueva fase móvil propuesta.
