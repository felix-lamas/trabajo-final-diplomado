# DOCUMENTACIÓN MAESTRA DEL PROYECTO

**Fecha de consolidación:** 16 de septiembre de 2026  
**Repositorio analizado:** `plataforma-eventos-uajms/`  
**Propósito:** línea base documental trazable para la monografía académica y para la futura definición de un cliente móvil Flutter.

## Criterio de clasificación y límites

- **IMPLEMENTADO:** existe evidencia directa en código fuente o configuración actual. No equivale a aceptación funcional ni a uso en producción.
- **DEFINIDO:** existe especificación documental, sin evidencia suficiente de implementación.
- **PARCIAL:** el código cubre solo parte de la condición documentada, usa datos simulados o deja una parte esencial sin implementar.
- **PENDIENTE:** está planificado o recomendado, pero no existe implementación suficiente.
- **OBSOLETO/CONTRADICTORIO:** fue superado por cambios posteriores o contradice fuentes de mayor evidencia.

La evidencia se priorizó en este orden: código y configuración actuales; historial Git; documentación técnica reciente; documentación histórica o generada por agentes; afirmaciones narrativas. Se revisaron **78 archivos documentales** —77 Markdown y 1 DOCX—, además de los 157 archivos Java del backend, 84 archivos TypeScript, rutas/pantallas Angular, manifiestos, configuración, dos colecciones Postman y el historial Git. No se ejecutó el sistema ni se modificó código. Por ello, “implementado” significa “presente en el código”, y no “probado en producción”.

---

# 1. Identificación del proyecto

| Elemento | Información consolidada | Estado/evidencia |
|---|---|---|
| Nombre más reciente y recurrente | **Plataforma Web para la Gestión Integral de Eventos Universitarios - UAJMS** | README y documentación de septiembre de 2026; es la variante documental más reciente. |
| Nombre del artefacto backend | **Plataforma Eventos UAJMS** | `backend/pom.xml`. |
| Variante anterior | **Sistema Web para la Gestión Integral de Eventos Universitarios - UAJMS** | `documentacion/PORTADA.docx`, 19/06/2026. |
| Institución | Universidad Autónoma Juan Misael Saracho (UAJMS) | Documentación de visión, README y portada. |
| Contexto | Gestión de eventos académicos, científicos, culturales y de capacitación; centralización de inscripción, pagos, acceso, asistencia y certificación | Documentado. |
| Tipo técnico | Plataforma web cliente-servidor: SPA Angular + API REST Spring Boot + PostgreSQL | **IMPLEMENTADO** en código/configuración. |
| Tipo académico | Trabajo destinado a una monografía de diplomado | Contexto del encargo actual; el repositorio lo presenta como entrega académica/tesis, sin identificar formalmente el diplomado. |
| Integrantes documentados | Iván Bismar Cruz Isnado; Félix Lamas Díaz; José Carlos Vasquez Yurquina; Diego Arenas | Extraídos de `PORTADA.docx`. |
| Carrera mencionada | Ingeniería de Alimentos | `PORTADA.docx`; no se explica su relación institucional con el sistema. |
| Estado actual | Implementación web sustancial en código; validación operativa, pruebas, despliegue productivo y cliente Flutter pendientes | Auditoría consolidada. |

No se elige arbitrariamente entre “Plataforma” y “Sistema”. La primera variante parece vigente por aparecer en las fuentes más recientes; la segunda se conserva como antecedente documental.

**Fuentes internas:** `plataforma-eventos-uajms/README.md`; `plataforma-eventos-uajms/backend/pom.xml`; `plataforma-eventos-uajms/docs/01_Vision_del_Proyecto.md`; `plataforma-eventos-uajms/docs/auditoria/auditoria-inicial.md`; `plataforma-eventos-uajms/documentacion/PORTADA.docx`; `plataforma-eventos-uajms/DOCUMENTACION_PROYECTO_TESIS_APP/01_RESUMEN_PROYECTO.md`.

# 2. Problema

## Situación problemática documentada

La UAJMS desarrolla actividades académicas, científicas y culturales cuya gestión se describe como dispersa y apoyada en procesos manuales o herramientas independientes. La documentación identifica baja visibilidad de eventos, registro descentralizado, dificultad para controlar cupos, inscripciones y pagos, control manual de asistencia, emisión lenta o manual de certificados y falta de datos consolidados para reportes y decisiones.

## Causas documentadas

- Ausencia de una plataforma centralizada que abarque el ciclo completo del evento.
- Uso de registros manuales o mecanismos separados para publicación, participantes, pagos, asistencia y certificados.
- Falta de integración y trazabilidad entre los procesos operativos.

## Consecuencias documentadas

- Duplicidad y dispersión de información.
- Errores y mayor carga administrativa para organizadores.
- Dificultad para dar seguimiento a inscripciones, pagos y asistencia.
- Retrasos e incertidumbre para participantes.
- Capacidad limitada de auditoría, generación de estadísticas y toma de decisiones institucionales.

## Necesidad y población involucrada

La necesidad documentada es centralizar y digitalizar la gestión integral de eventos en una sola solución. La población indicada comprende estudiantes, docentes, administrativos y participantes externos; coordinadores de facultad/carrera, organizadores, personal financiero y de control; autoridades universitarias y terceros verificadores de certificados.

La documentación no aporta mediciones de línea base, volumen real de eventos, población/muestra, tiempos de proceso, tasa de errores ni evidencia empírica institucional. Estas carencias impiden cuantificar el problema para la monografía.

**Fuentes internas:** `docs/01_Vision_del_Proyecto.md`; `docs/release_1_0/09_narrativa_defensa.md`; `documentacion/PORTADA.docx`; `DOCUMENTACION_PROYECTO_TESIS_APP/18_ANALISIS_TESIS.md`.

# 3. Objetivos

## Objetivo general documentado

> Desarrollar e implementar una plataforma web robusta y escalable para la gestión integral de eventos académicos, científicos, culturales y de capacitación de la Universidad Autónoma Juan Misael Saracho (UAJMS), que permita centralizar la información, automatizar los procesos de inscripción, control de asistencia y certificación, mejorando la eficiencia administrativa y la transparencia institucional.

Existe una variante en la portada: desarrollar una plataforma web que administre el ciclo completo mediante módulos de seguridad, usuarios, inscripción, pagos, asistencia y certificación digital. Ambas variantes son compatibles; la primera es la fuente específica de objetivos del repositorio.

## Objetivos específicos documentados

| ID | Objetivo | Estado frente al código |
|---|---|---|
| OE-01 | Implementar registro y autenticación segura de participantes internos y externos con roles diferenciados | **IMPLEMENTADO** en código; aceptación no verificada. |
| OE-02 | Crear un catálogo digital unificado para eventos académicos, científicos, culturales y de capacitación | **PARCIAL**: catálogo y categoría existen; falta el filtro por fecha documentado. |
| OE-03 | Automatizar inscripciones gratuitas y pagadas con validación administrativa de comprobantes | **IMPLEMENTADO** en su flujo principal; notificación del resultado es parcial. |
| OE-04 | Implementar QR para validar ingreso y registrar asistencia en tiempo real | **IMPLEMENTADO** en código; firma criptográfica y prueba operacional pendientes. |
| OE-05 | Generar certificados digitales automáticos y verificables mediante código único | **PARCIAL**: existe registro y verificación; no se genera el PDF real ni el proceso automático/masivo. |
| OE-06 | Proveer reportes y dashboards por facultad, carrera e ingresos | **PARCIAL**: hay endpoints/pantallas, pero el dashboard académico usa cifras simuladas y faltan filtros. |
| OE-07 | Garantizar disponibilidad de 99,5 % y experiencia fluida con tecnologías modernas | **DEFINIDO**: no hay medición de disponibilidad, rendimiento o experiencia. |

`PORTADA.docx` añade objetivos sobre análisis del proceso actual, diseño de arquitectura, administración de roles/permisos, diseño relacional y control de versiones. Son objetivos **documentados**, no inferidos; algunos no aparecen en `03_Objetivos_Especificos.md`. No se crean objetivos nuevos en esta consolidación. La aplicación Flutter no figura como objetivo aprobado en las fuentes originales.

**Fuentes internas:** `docs/02_Objetivos_Generales.md`; `docs/03_Objetivos_Especificos.md`; `documentacion/PORTADA.docx`; `docs/12_Matriz_de_Trazabilidad.md`.

# 4. Alcance

## Alcance implementado o parcial

| Área | Contenido | Estado |
|---|---|---|
| Identidad | Registro, login, perfil, cambio y recuperación de contraseña, JWT y roles | **IMPLEMENTADO**. |
| Catálogos académicos | Facultades, carreras y categorías de eventos | **IMPLEMENTADO**. |
| Eventos | Creación, edición, eliminación lógica, publicación, cancelación, finalización, modalidades, costo y cupos | **IMPLEMENTADO/PARCIAL** por divergencia de estados y filtros. |
| Inscripciones | Gratuitas/pagadas, consulta propia, cancelación, unicidad y cupos | **IMPLEMENTADO**. |
| Pagos | Registro, comprobante local, validación/rechazo manual | **IMPLEMENTADO**; notificación del resultado **PENDIENTE**. |
| Credenciales y QR | Generación de credencial, QR, imagen y PDF; validación/consumo | **IMPLEMENTADO**; QR firmado **PENDIENTE**. |
| Acceso y asistencia | Escaneo o búsqueda manual, autorizar/denegar, historial y asistencia | **IMPLEMENTADO**; ventana horaria documentada no se evidencia. |
| Certificados | Registro, código, estado y verificación pública | **PARCIAL**; archivo PDF, cálculo porcentual, emisión automática/masiva y liberación no están completos. |
| Encuestas | Respuesta única, calificación, comentario y estadísticas | **IMPLEMENTADO** como módulo adicional no trazado originalmente a RF/HU/CU. |
| Reportes | Dashboards, listados y rutas de exportación | **PARCIAL**; datos académicos simulados y archivos PDF/XLSX ficticios. |
| Plataforma web | Backend y SPA | **IMPLEMENTADO** a nivel de código. |
| Aplicación Flutter | Cliente móvil reutilizando la API | **PENDIENTE/DEFINIDO** solo como propuesta posterior. |

## Funcionalidades excluidas en la primera fase

- Aplicación móvil nativa, según `05_Fuera_de_Alcance.md`.
- Pasarela de pagos en línea; el proyecto usa comprobante y validación manual.
- Hardware de acceso industrial; se prevé cámara en web/dispositivo convencional.
- LMS o gestión de contenido académico.
- Firma digital legal de una autoridad certificadora externa.
- Compras, viáticos y presupuestos logísticos.

## Usuarios, plataformas e integraciones

Usuarios: administrativos, organizadores/coordinadores, personal de control, estudiantes, participantes externos y verificadores públicos. Plataformas implementadas: navegador web y API REST. Integraciones implementadas o configuradas: PostgreSQL, correo de recuperación si hay `JavaMailSender`, almacenamiento local de comprobantes, OpenAPI/Swagger. No existe pasarela bancaria, FCM/push, servicio de almacenamiento de certificados, SIA, CI/CD ni integración productiva comprobada.

## Limitaciones conocidas

Ausencia de migraciones SQL; dependencia de `ddl-auto:update`; pruebas automatizadas mínimas; rutas API no uniformes; datos simulados en analítica; exportaciones ficticias; certificado sin PDF real; almacenamiento local de comprobantes; falta de métricas; sin evidencia de producción; sin cliente Flutter.

**Fuentes internas:** `docs/04_Alcance.md`; `docs/05_Fuera_de_Alcance.md`; `docs/16_Modulos_del_Sistema.md`; `docs/17_Backlog_Priorizado.md`; `DOCUMENTACION_PROYECTO_TESIS_APP/05_FUNCIONALIDADES.md`; código bajo `backend/.../modulos` y `frontend/src/app/features`.

# 5. Actores y roles

| Rol | Descripción | Funciones documentadas | Evidencia |
|---|---|---|---|
| Super Administrador (TI) | Administración técnica global | Parámetros, logs, usuarios, roles y control total | **DEFINIDO**; código usa `ADMINISTRADOR`, sin rol separado de superadministrador. |
| Administrador de Eventos | Administración institucional del catálogo | Aprobar/gestionar eventos y reportes generales | **PARCIAL**; funciones absorbidas por `ADMINISTRADOR` y `ORGANIZADOR`. |
| Coordinador de Facultad/Carrera | Responsable de eventos de una unidad | Crear eventos, validar inscripciones y supervisar asistencia | **PARCIAL**; corresponde aproximadamente a `ORGANIZADOR`, sin restricción por unidad comprobada. |
| Validador Financiero | Revisa comprobantes | Aprobar o rechazar pagos | **DEFINIDO** como actor; no existe rol técnico `VALIDADOR_FINANCIERO`; lo hacen `ADMINISTRADOR`/`ORGANIZADOR`. |
| Registrador de Asistencia | Personal operativo | Escanear QR y registrar ingreso/asistencia | **IMPLEMENTADO** como `PERSONAL_CONTROL`. |
| Participante Interno | Estudiante/docente/administrativo UAJMS | Consultar, inscribirse, pagar, usar credencial y certificado | **PARCIAL**: código registra internos como `ESTUDIANTE`; docente/administrativo no tienen rol propio. |
| Participante Externo | Público no perteneciente a UAJMS | Registro e interacción en eventos habilitados | **IMPLEMENTADO** como `PARTICIPANTE_EXTERNO`. |
| Verificador de Certificado | Tercero sin autenticación | Verificar un certificado por código | **IMPLEMENTADO** mediante endpoint público. |

Roles técnicos adicionales comprobados: `ADMINISTRADOR`, `ORGANIZADOR`, `ESTUDIANTE`, `PARTICIPANTE_EXTERNO`, `PARTICIPANTE` —rol compatible para certificados— y `PERSONAL_CONTROL`. El seeder asigna también `PARTICIPANTE` a estudiantes y externos. Las entidades `Rol`, `Permiso`, `UsuarioRol` y `RolPermiso` implementan el modelo RBAC, aunque la autorización de endpoints usa principalmente nombres de rol en `@PreAuthorize`.

**Fuentes internas:** `docs/06_Actores_del_Sistema.md`; `docs/DOCUMENTO_MAESTRO_DESARROLLO.md`; `backend/.../core/configuracion/DatosInicialesSeed.java`; controladores; `DOCUMENTACION_PROYECTO_TESIS_APP/06_USUARIOS_ROLES.md`.

# 6. Requisitos funcionales

La prioridad formal no está documentada por requisito. Se conserva como “No documentada”, salvo el requisito opcional de lista de espera.

| ID | Requisito | Descripción | Prioridad | Estado | Evidencia |
|---|---|---|---|---|---|
| RF-01 | Registro de usuarios | Registro externo con datos personales y rol según tipo | No documentada | **IMPLEMENTADO** | `AutenticacionControlador`, `AutenticacionServicio`, pantalla `registro`. |
| RF-02 | Autenticación | Inicio de sesión por correo/contraseña y JWT | No documentada | **IMPLEMENTADO** | `JwtService`, filtro JWT, login Angular. |
| RF-03 | Gestión de perfil | Consulta y actualización de perfil | No documentada | **IMPLEMENTADO** | `/api/v1/usuarios/perfil`, `perfil-usuario`. |
| RF-04 | Recuperación de contraseña | Solicitud por correo y token de un solo uso | No documentada | **IMPLEMENTADO** | endpoints auth, `TokenRecuperacion`, `CorreoServicio`. |
| RF-05 | Creación de eventos | Alta de eventos con categoría, modalidad, fechas, costo y cupos | No documentada | **IMPLEMENTADO** | `EventoController`, `EventoService`, formulario admin. |
| RF-06 | Gestión de cupos | Controlar cupo máximo y disponible | No documentada | **IMPLEMENTADO** | `Evento`, `InscripcionService`. |
| RF-07 | Catálogo público | Eventos publicados con filtros por categoría y fecha | No documentada | **PARCIAL** | publicados y categoría existen; filtro por fecha no localizado. |
| RF-08 | Estados de evento | Borrador, pendiente, publicado y finalizado | No documentada | **PARCIAL** | enum real: `BORRADOR`, `PUBLICADO`, `INSCRIPCIONES_CERRADAS`, `EN_CURSO`, `FINALIZADO`, `CANCELADO`; no `PENDIENTE`/`ARCHIVADO`. |
| RF-09 | Inscripción gratuita | Confirmación inmediata con cupo disponible | No documentada | **IMPLEMENTADO** | `InscripcionService`. |
| RF-10 | Inscripción pagada | Estado pendiente y carga de comprobante JPG/PNG/PDF | No documentada | **IMPLEMENTADO** | `PagoController`, `PagoService`, `ArchivoSeguroServicio`. |
| RF-11 | Validación de pagos | Aprobar/rechazar y notificar al usuario | No documentada | **PARCIAL** | aprobación/rechazo y cambio de inscripción existen; notificación no. |
| RF-12 | Lista de espera | Registrar interesados cuando no hay cupo | Opcional | **PENDIENTE** | Sin entidad, endpoint ni UI. |
| RF-13 | Generación de QR | QR único y firmado/cifrado al habilitar inscripción | No documentada | **PARCIAL** | QR único contiene UUID de credencial; no está firmado ni cifrado. |
| RF-14 | Credencial digital | Vista o PDF con datos y QR | No documentada | **IMPLEMENTADO** | `CredencialService.descargarPdf`, endpoints y pantallas. |
| RF-15 | Escaneo QR | Validar, autorizar y registrar asistencia | No documentada | **IMPLEMENTADO** | control de acceso, asistencia y scanner Angular. |
| RF-16 | Registro manual | Buscar por CI o código de participante | No documentada | **IMPLEMENTADO** | `/control-acceso/documento/{documento}` y `/codigo/{codigoParticipante}`. |
| RF-17 | Certificado automático PDF | Generar PDF al cierre y de forma masiva | No documentada | **PARCIAL** | crea registro y URL fija; no crea archivo PDF ni proceso masivo/automático. |
| RF-18 | Criterio de certificación | Exigir porcentaje configurable de asistencia | No documentada | **PARCIAL** | evento almacena porcentaje, pero servicio solo exige una asistencia. |
| RF-19 | Verificación pública | Validar por código único y mostrar vigencia | No documentada | **IMPLEMENTADO** | `/verificacion-certificados/{codigo}` y UI pública. |
| RF-20 | Participación por facultad | Estadísticas de inscritos por unidad | No documentada | **PARCIAL** | endpoint/pantalla existen; `DashboardAcademico` usa cifras fijas simuladas. |
| RF-21 | Reporte económico | Ingresos por evento y rango de fechas | No documentada | **PARCIAL** | total validado/listado existe; sin filtros por evento/fecha. |
| RF-22 | Reporte de asistencia | Participantes y hora de registro | No documentada | **PARCIAL** | asistencia guarda hora y se lista por evento; no hay reporte/exportación específico completo. |

Funciones implementadas sin requisito funcional formal propio: CRUD de facultades/carreras/categorías, cancelación de inscripción, historial de acceso, encuestas de satisfacción y cambio de contraseña.

**Fuentes internas:** `docs/07_Requerimientos_Funcionales.md`; `docs/12_Matriz_de_Trazabilidad.md`; controladores, servicios, entidades y componentes citados; `DOCUMENTACION_PROYECTO_TESIS_APP/20_REQUISITOS_EXISTENTES.md`.

# 7. Requisitos no funcionales

| ID | Requisito | Categoría | Estado | Evidencia |
|---|---|---|---|---|
| RNF-01 | Catálogo en menos de 2 s con 100 usuarios concurrentes | Rendimiento | **PENDIENTE** | No hay pruebas de carga ni métricas. |
| RNF-02 | Certificados masivos asíncronos | Rendimiento/disponibilidad | **PENDIENTE** | Servicio unitario, síncrono y sin PDF real. |
| RNF-03 | Contraseñas con BCrypt | Seguridad/protección de datos | **IMPLEMENTADO** | `BCryptPasswordEncoder`; contraseñas codificadas al registrar/restablecer. |
| RNF-04 | Expiración JWT configurable | Seguridad | **IMPLEMENTADO** | `JwtPropiedades`, `JWT_EXPIRATION`. |
| RNF-05 | QR firmado para impedir falsificación | Seguridad/confiabilidad | **PENDIENTE** | QR codifica un UUID sin firma. |
| RNF-06 | Interfaz funcional en móviles/tablets | Usabilidad/compatibilidad | **DEFINIDO** | Hay Tailwind/Material y HTML, pero no pruebas responsivas. |
| RNF-07 | WCAG 2.1 básica | Usabilidad/accesibilidad | **PENDIENTE** | No hay auditoría ni pruebas de accesibilidad. |
| RNF-08 | Arquitectura modular | Mantenibilidad/escalabilidad | **IMPLEMENTADO** | Paquetes de dominio y módulos Angular. |
| RNF-09 | Logging de errores críticos | Mantenibilidad/auditoría | **PARCIAL** | SLF4J/Logback disponible y algunos logs; no hay política/configuración ni trazabilidad central. |
| RNF-10 | 50.000 certificados/año sin degradación | Capacidad/escalabilidad | **PENDIENTE** | Sin prueba de capacidad ni almacenamiento real de certificados. |

Restricciones tecnológicas implementadas: Java 21, Spring Boot 3.3, JPA/Hibernate, PostgreSQL, Angular, REST/JSON y monolito modular. La meta de disponibilidad del 99,5 % aparece como objetivo, pero no tiene SLA, monitorización o evidencia.

**Fuentes internas:** `docs/08_Requerimientos_No_Funcionales.md`; `docs/14_Restricciones_Tecnicas.md`; `backend/pom.xml`; `frontend/package.json`; `application.yml`; código de seguridad y QR.

# 8. Arquitectura

## Arquitectura implementada

La arquitectura real es un **monolito modular cliente-servidor por capas**, desplegable como un único backend, no microservicios:

```text
Navegador / SPA Angular 21
        │ HTTP/JSON + Bearer JWT
        ▼
API REST Spring Boot 3.3
Controladores → Servicios → Repositorios JPA
        │
        ▼
PostgreSQL 16
```

- **Frontend web:** SPA Angular con rutas públicas, privadas, administrativas y operativas; servicios `HttpClient`, guardas y un interceptor JWT.
- **Backend:** Java 21/Spring Boot organizado en `core`, `comun` y módulos de dominio.
- **Persistencia:** JPA/Hibernate sobre PostgreSQL; UUID y campos comunes de auditoría/borrado lógico mediante `EntidadBase`.
- **Autenticación:** JWT stateless, BCrypt y autorización por roles.
- **Archivos:** comprobantes en almacenamiento local configurable; credencial PDF generada en memoria.
- **QR:** ZXing genera PNG; el contenido actual es el UUID de credencial.
- **Pagos:** registro de comprobante y validación manual; no hay pasarela.
- **Certificados:** registro y código verificable en BD; la URL de PDF apunta a un dominio fijo, sin generación/almacenamiento real comprobado.
- **Correo:** recuperación de contraseña mediante `JavaMailSender`; si no está disponible, el enlace completo se escribe en logs.
- **OpenAPI:** springdoc configurado; no existe contrato exportado.

Módulos backend implementados: usuarios/seguridad, facultades, carreras, categorías, eventos, inscripciones, pagos, credenciales, código QR, control de acceso, asistencias, certificados, encuestas y reportes. Son catorce dominios funcionales más componentes transversales.

## Arquitectura definida

La documentación define normalización 3FN, auditoría central JSONB, índices, soft delete, certificados en MinIO/S3, notificaciones y arquitectura modular. Solo UUID/campos de auditoría/soft delete y parte del modelo relacional se evidencian. No existen entidad/tabla central `auditoria`, entidad `notificacion`, MinIO/S3 ni migraciones.

## Arquitectura planificada

El análisis móvil propone: `Flutter → misma API REST → mismo backend monolítico → PostgreSQL`. También propone almacenamiento seguro del token, lectura/presentación de QR, push, refresh/revocación, paginación y modo offline. Ningún artefacto Flutter o servicio móvil existe actualmente.

**Fuentes internas:** `docs/15_Arquitectura_General.md`; `docs/release_1_0/01_arquitectura.md`; `docs/DOCUMENTO_MAESTRO_DESARROLLO.md`; paquetes backend/frontend; `DOCUMENTACION_PROYECTO_TESIS_APP/03_ARQUITECTURA.md`; `19_ANALISIS_APP_MOVIL.md`.

# 9. Tecnologías

| Tecnología | Versión | Uso | Estado |
|---|---:|---|---|
| Java | 21 | Backend | **IMPLEMENTADO** |
| Spring Boot | 3.3.0 | API y contenedor de aplicación | **IMPLEMENTADO** |
| Spring Security | Gestionada por Boot 3.3.0 | JWT, BCrypt, RBAC | **IMPLEMENTADO** |
| Spring Data JPA / Hibernate | Gestionada por Boot | ORM/persistencia | **IMPLEMENTADO** |
| Spring Validation | Gestionada por Boot | Validación de DTO | **IMPLEMENTADO** |
| Spring Mail | Gestionada por Boot | Recuperación de contraseña | **PARCIAL**; depende de SMTP no documentado. |
| JJWT | 0.12.5 | Firma/verificación JWT | **IMPLEMENTADO** |
| springdoc OpenAPI | 2.5.0 | Swagger/API docs | **IMPLEMENTADO** en configuración; ejecución no verificada. |
| ZXing | 3.5.3 | Imagen QR | **IMPLEMENTADO** |
| iText 7 | 8.0.4 | PDF de credencial | **IMPLEMENTADO** |
| Apache Tika | 2.9.2 | Detección de archivos | **PARCIAL** en validación efectiva. |
| PostgreSQL | 16-alpine | Base de datos Docker | **IMPLEMENTADO** en configuración. |
| Docker Compose | Sin versión fijada | PostgreSQL y pgAdmin | **IMPLEMENTADO** en desarrollo. |
| pgAdmin 4 | Tag no fijado | Administración de BD | **IMPLEMENTADO** en Compose. |
| Angular | ^21.0.0 | SPA web | **IMPLEMENTADO** |
| Angular Material/CDK | ^21.0.0 | UI | **IMPLEMENTADO** |
| Tailwind CSS | ^4.1.12 | Estilos | **IMPLEMENTADO** |
| TypeScript | ~5.9.3 | Frontend | **IMPLEMENTADO** |
| RxJS | ~7.8.0 | Reactividad | **IMPLEMENTADO** |
| Vitest | ^4.0.8 | Prueba frontend | **PARCIAL**; solo una prueba mínima y obsoleta. |
| npm | 11.6.3 declarada | Gestión frontend | **DEFINIDO** |
| Node.js | 20+ documentado | Ejecución frontend | **DEFINIDO**; versión instalada no verificada. |
| Maven | 3.9+ documentado | Build backend | **DEFINIDO**; versión instalada no verificada. |
| Flutter/Dart | Sin versión | Aplicación móvil | **PENDIENTE**; no hay `pubspec.yaml`. |

Quarkus aparece una vez en `PORTADA.docx`, pero contradice `pom.xml` y todo el backend Spring Boot; se clasifica **OBSOLETO/CONTRADICTORIO**.

**Fuentes internas:** `backend/pom.xml`; `frontend/package.json`; `docker/docker-compose.yml`; `README.md`; `docs/release_1_0/03_manual_instalacion.md`; `documentacion/PORTADA.docx`.

# 10. Base de datos

## Motor y estrategia real

PostgreSQL 16 está configurado en Docker. Hibernate crea/actualiza el esquema con `ddl-auto:update`; `database/scripts/` está vacío. Por tanto, las entidades JPA son la fuente actual del modelo y no hay DDL/migraciones versionadas para verificar índices físicos, triggers o evolución del esquema.

Todas las entidades que heredan `EntidadBase` reciben `id` UUID, `fecha_creacion`, `fecha_actualizacion`, `creado_por`, `actualizado_por` y `fecha_eliminacion`.

| Tabla/entidad | Campos importantes | Claves y relaciones principales |
|---|---|---|
| `usuario` | correo, contraseña, nombres, apellidos, CI, celular, fotografía, tipo | Correo y CI únicos; N:1 carrera. |
| `rol` | nombre, descripción | Nombre único. |
| `permiso` | nombre, descripción | Nombre único. |
| `usuario_rol` | usuario, rol | N:1 a ambos; tabla de asignación. |
| `rol_permiso` | rol, permiso | N:1 a ambos; tabla de asignación. |
| `token_recuperacion` | token, expiración, utilizado | Token único; N:1 usuario. |
| `facultades` | nombre, descripción, estado | Nombre único; 1:N carreras. |
| `carreras` | nombre, descripción, estado | N:1 facultad. |
| `categorias_evento` | nombre, descripción, estado | Nombre único; referenciada por evento. |
| `eventos` | título, descripción, objetivos, modalidad, tipo, costo, fechas/horas, ubicación/enlace, cupos, carga, asistencia mínima, estado, imagen | N:1 categoría y organizador. |
| `inscripciones` | usuario, evento, código, fecha, estado, observación | `UNIQUE(usuario_id, evento_id)`; código único. |
| `pagos` | inscripción, monto, fecha, estado, observación | 1:1 inscripción, única. |
| `comprobantes_pago` | pago, URL, nombre, MIME | 1:1 pago. |
| `credenciales` | usuario, evento, inscripción, código, fecha, estado | 1:1 inscripción; código único. |
| `codigos_qr` | credencial, contenido, fecha, estado, activo | 1:1 credencial. |
| `control_acceso` | credencial, usuario de control, fecha/hora, estado, observación | N:1 credencial y usuario. |
| `asistencias` | inscripción, usuario de control, fecha/hora, observación | N:1 inscripción y usuario. |
| `certificados` | usuario, evento, inscripción, código, emisión, URL, estado, URL PDF | 1:1 inscripción; código único. |
| `encuestas` | evento, usuario, inscripción | `UNIQUE(evento_id, usuario_id)`; inscripción única; 1:N respuestas. |
| `preguntas_encuesta` | texto, tipo, obligatoria, orden, activa | Catálogo de preguntas. |
| `respuestas_encuesta` | encuesta, pregunta, calificación, comentario | `UNIQUE(encuesta_id, pregunta_id)`. |

Relaciones centrales: Facultad 1:N Carrera; Categoría 1:N Evento; Usuario/Evento 1:N Inscripción; Inscripción 1:1 Pago, Credencial, Certificado y Encuesta; Pago 1:1 Comprobante; Credencial 1:1 QR; Inscripción 1:N Asistencia; Encuesta 1:N Respuesta.

Diferencias documentales: las convenciones antiguas prescriben tablas plurales y luego singulares; el código usa ambas. El catálogo histórico incluye `MetodoPago`, `Notificacion` y `Auditoria`, pero no hay entidades actuales. El diccionario antiguo ubica `datos_qr` en inscripción, mientras el código usa `codigos_qr`. Los índices y `CHECK` propuestos no pueden confirmarse sin migraciones/DDL.

**Fuentes internas:** `docs/07_*`; `docs/release_1_0/07_documento_base_datos.md`; entidades bajo `backend/.../entidades`; `comun/EntidadBase.java`; `application.yml`; `docker/docker-compose.yml`.

# 11. API

La tabla se deriva de los 15 controladores REST. La notación `/api[/v1]` indica que el controlador expone ambos alias. “Autenticado” significa que no hay `@PreAuthorize` local, pero `SecurityConfig` exige autenticación salvo rutas permitidas expresamente.

| Método | Endpoint | Función | Módulo | Autenticación | Estado |
|---|---|---|---|---|---|
| POST | `/api[/v1]/auth/registro` | Registrar usuario | Auth | Pública | **IMPLEMENTADO** |
| POST | `/api[/v1]/auth/login` | Iniciar sesión | Auth | Pública | **IMPLEMENTADO** |
| POST | `/api[/v1]/auth/recuperar-contrasena` | Solicitar recuperación | Auth | Pública | **IMPLEMENTADO** |
| POST | `/api[/v1]/auth/restablecer-contrasena` | Restablecer con token | Auth | Pública | **IMPLEMENTADO** |
| POST | `/api[/v1]/auth/resetear-contrasena` | Alias de restablecimiento | Auth | Pública | **IMPLEMENTADO** |
| GET | `/api/v1/usuarios` | Listar usuarios | Usuarios | ADMINISTRADOR | **IMPLEMENTADO** |
| GET | `/api/v1/usuarios/{id}` | Consultar usuario | Usuarios | ADMINISTRADOR | **IMPLEMENTADO** |
| GET | `/api/v1/usuarios/perfil` | Perfil propio | Usuarios | Autenticado | **IMPLEMENTADO** |
| PUT | `/api/v1/usuarios/perfil` | Actualizar perfil | Usuarios | Autenticado | **IMPLEMENTADO** |
| POST | `/api/v1/usuarios/cambiar-contrasena` | Cambiar contraseña | Usuarios | Autenticado | **IMPLEMENTADO** |
| GET | `/api/v1/facultades` | Listar | Facultades | ADMINISTRADOR/ORGANIZADOR/ESTUDIANTE | **IMPLEMENTADO** |
| GET | `/api/v1/facultades/{id}` | Consultar | Facultades | mismos roles | **IMPLEMENTADO** |
| GET | `/api/v1/facultades/{id}/carreras` | Carreras de facultad | Facultades | mismos roles | **IMPLEMENTADO** |
| POST | `/api/v1/facultades` | Crear | Facultades | ADMINISTRADOR | **IMPLEMENTADO** |
| PUT | `/api/v1/facultades/{id}` | Actualizar | Facultades | ADMINISTRADOR | **IMPLEMENTADO** |
| DELETE | `/api/v1/facultades/{id}` | Eliminar | Facultades | ADMINISTRADOR | **IMPLEMENTADO** |
| GET | `/api/v1/carreras` | Listar | Carreras | ADMINISTRADOR/ORGANIZADOR/ESTUDIANTE | **IMPLEMENTADO** |
| GET | `/api/v1/carreras/{id}` | Consultar | Carreras | mismos roles | **IMPLEMENTADO** |
| POST | `/api/v1/carreras` | Crear | Carreras | ADMINISTRADOR | **IMPLEMENTADO** |
| PUT | `/api/v1/carreras/{id}` | Actualizar | Carreras | ADMINISTRADOR | **IMPLEMENTADO** |
| DELETE | `/api/v1/carreras/{id}` | Eliminar | Carreras | ADMINISTRADOR | **IMPLEMENTADO** |
| GET | `/api/v1/categorias-evento` | Listar | Categorías | Roles administrativos y participantes | **IMPLEMENTADO** |
| GET | `/api/v1/categorias-evento/activas` | Listar activas | Categorías | Roles administrativos y participantes | **IMPLEMENTADO** |
| GET | `/api/v1/categorias-evento/{id}` | Consultar | Categorías | Roles administrativos y participantes | **IMPLEMENTADO** |
| POST | `/api/v1/categorias-evento` | Crear | Categorías | ADMINISTRADOR | **IMPLEMENTADO** |
| PUT | `/api/v1/categorias-evento/{id}` | Actualizar | Categorías | ADMINISTRADOR | **IMPLEMENTADO** |
| DELETE | `/api/v1/categorias-evento/{id}` | Eliminar | Categorías | ADMINISTRADOR | **IMPLEMENTADO** |
| GET | `/api/v1/eventos/publicados` | Catálogo publicado | Eventos | Pública | **IMPLEMENTADO** |
| GET | `/api/v1/eventos/{id}` | Detalle | Eventos | Pública | **IMPLEMENTADO** |
| GET | `/api/v1/eventos/categoria/{id}` | Filtrar por categoría | Eventos | Pública | **IMPLEMENTADO** |
| GET | `/api/v1/eventos` | Listar todos | Eventos | Roles administrativos y participantes | **IMPLEMENTADO** |
| POST | `/api/v1/eventos` | Crear | Eventos | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| PUT | `/api/v1/eventos/{id}` | Actualizar | Eventos | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| DELETE | `/api/v1/eventos/{id}` | Eliminar | Eventos | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| PATCH | `/api/v1/eventos/{id}/publicar` | Publicar | Eventos | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| PATCH | `/api/v1/eventos/{id}/cancelar` | Cancelar | Eventos | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| PATCH | `/api/v1/eventos/{id}/finalizar` | Finalizar | Eventos | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| POST | `/api/v1/inscripciones` | Inscribirse | Inscripciones | ADMINISTRADOR/ESTUDIANTE/EXTERNO | **IMPLEMENTADO** |
| GET | `/api/v1/inscripciones/mis-inscripciones` | Listar propias | Inscripciones | mismos roles | **IMPLEMENTADO** |
| GET | `/api/v1/inscripciones/{id}` | Consultar por ID | Inscripciones | Roles administrativos y participantes | **PARCIAL**; propiedad del recurso no se valida en servicio. |
| PATCH | `/api/v1/inscripciones/{id}/cancelar` | Cancelar propia | Inscripciones | ADMINISTRADOR/ESTUDIANTE/EXTERNO | **IMPLEMENTADO** con validación de propietario. |
| GET | `/api/v1/inscripciones/evento/{eventoId}` | Inscritos por evento | Inscripciones | ADMINISTRADOR/ORGANIZADOR | **PARCIAL**; TODO de alcance del organizador. |
| POST | `/api/v1/pagos` | Registrar pago | Pagos | ADMINISTRADOR/ESTUDIANTE/EXTERNO | **IMPLEMENTADO** |
| POST | `/api/v1/pagos/{id}/comprobante` | Subir comprobante | Pagos | mismos roles | **IMPLEMENTADO** |
| GET | `/api/v1/pagos/mis-pagos` | Pagos propios | Pagos | mismos roles | **IMPLEMENTADO** |
| GET | `/api/v1/pagos/pendientes` | Pendientes | Pagos | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| GET | `/api/v1/pagos` | Todos | Pagos | ADMINISTRADOR | **IMPLEMENTADO** |
| GET | `/api/v1/pagos/{id}` | Consultar | Pagos | Roles administrativos y participantes | **PARCIAL**; propiedad no verificada en método. |
| PATCH | `/api/v1/pagos/{id}/validar` | Validar | Pagos | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| PATCH | `/api/v1/pagos/{id}/rechazar` | Rechazar | Pagos | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| POST | `/api/v1/credenciales/generar/{inscripcionId}` | Generar credencial/QR | Credenciales | ADMINISTRADOR/ESTUDIANTE/EXTERNO | **PARCIAL**; no verifica propietario de la inscripción. |
| GET | `/api/v1/credenciales/{id}` | Consultar | Credenciales | Roles administrativos y participantes | **PARCIAL**; propiedad no verificada. |
| GET | `/api/v1/usuarios/mis-credenciales` | Credenciales propias | Credenciales | ADMINISTRADOR/ESTUDIANTE/EXTERNO | **IMPLEMENTADO** |
| GET | `/api/v1/credenciales/{id}/qr` | Imagen QR | Credenciales | Roles administrativos y participantes | **PARCIAL**; QR sin firma y propiedad no verificada. |
| GET | `/api/v1/credenciales/{id}/descargar` | PDF credencial | Credenciales | ADMINISTRADOR/ESTUDIANTE/EXTERNO | **IMPLEMENTADO/PARCIAL** por propiedad. |
| GET | `/api/v1/codigos-qr/validar/{contenido}` | Validar existencia | QR | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| POST | `/api[/v1]/control-acceso/validar-qr` | Validar QR | Acceso | ADMINISTRADOR/ORGANIZADOR/CONTROL | **IMPLEMENTADO** |
| POST | `/api[/v1]/control-acceso/autorizar` | Autorizar y marcar asistencia | Acceso | mismos roles | **IMPLEMENTADO** |
| POST | `/api[/v1]/control-acceso/denegar` | Denegar | Acceso | mismos roles | **IMPLEMENTADO** |
| GET | `/api[/v1]/control-acceso/historial/{eventoId}` | Historial | Acceso | mismos roles | **IMPLEMENTADO** |
| GET | `/api[/v1]/control-acceso/codigo/{codigoParticipante}` | Buscar por código | Acceso | mismos roles | **IMPLEMENTADO** |
| GET | `/api[/v1]/control-acceso/documento/{documento}?eventoId=` | Buscar por CI/evento | Acceso | mismos roles | **IMPLEMENTADO** |
| GET | `/api[/v1]/asistencias/evento/{id}` | Asistencia por evento | Asistencia | ADMINISTRADOR/ORGANIZADOR/CONTROL | **IMPLEMENTADO** |
| POST | `/api[/v1]/certificados/generar/{inscripcionId}` | Generar registro | Certificados | ADMINISTRADOR/ORGANIZADOR | **PARCIAL**; no genera PDF ni porcentaje real. |
| GET | `/api[/v1]/certificados/{id}` | Consultar | Certificados | ADMINISTRADOR/ORGANIZADOR/PARTICIPANTE | **PARCIAL**; propiedad no verificada. |
| GET | `/api[/v1]/certificados/mis-certificados` | Listar propios | Certificados | PARTICIPANTE | **IMPLEMENTADO** |
| GET | `/api[/v1]/certificados/{id}/descargar` | Marcar como descargado | Certificados | ADMINISTRADOR/ORGANIZADOR/PARTICIPANTE | **PARCIAL**; devuelve DTO/URL, no archivo PDF. |
| GET | `/api[/v1]/verificacion-certificados/{codigo}` | Verificar públicamente | Certificados | Pública | **IMPLEMENTADO** |
| POST | `/api[/v1]/encuestas/responder` | Responder | Encuestas | Autenticado | **IMPLEMENTADO** |
| GET | `/api[/v1]/encuestas/evento/{id}` | Respuestas por evento | Encuestas | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| GET | `/api[/v1]/encuestas/estadisticas/{id}` | Estadísticas | Encuestas | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| GET | `/api/dashboard` | Dashboard general | Reportes | ADMINISTRADOR/ORGANIZADOR | **PARCIAL** |
| GET | `/api/dashboard/ejecutivo` | KPIs | Reportes | ADMINISTRADOR/ORGANIZADOR | **PARCIAL**; parte simplificada. |
| GET | `/api/dashboard/academico` | Distribución académica | Reportes | ADMINISTRADOR/ORGANIZADOR | **PARCIAL**; cifras simuladas. |
| GET | `/api/dashboard/operativo` | Indicadores operativos | Reportes | ADMINISTRADOR/ORGANIZADOR/CONTROL | **IMPLEMENTADO** en código. |
| GET | `/api/reportes/eventos` | Listado de eventos | Reportes | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| GET | `/api/reportes/participantes` | Listado de participantes | Reportes | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| GET | `/api/reportes/pagos` | Listado de pagos | Reportes | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| GET | `/api/reportes/certificados` | Listado de certificados | Reportes | ADMINISTRADOR/ORGANIZADOR | **IMPLEMENTADO** |
| GET | `/api/reportes/exportar/pdf?tipo=` | Exportar PDF | Reportes | ADMINISTRADOR/ORGANIZADOR | **PARCIAL**; devuelve texto simulado con MIME PDF. |
| GET | `/api/reportes/exportar/excel?tipo=` | Exportar XLSX | Reportes | ADMINISTRADOR/ORGANIZADOR | **PARCIAL**; devuelve texto simulado con MIME XLSX. |

No hay paginación, contrato OpenAPI estático, versionado uniforme ni endpoints móviles exclusivos. El frontend apunta a `/api/v1`, pero reportes solo existe bajo `/api`.

**Fuentes internas:** todos los controladores bajo `backend/src/main/java/bo/uajms/eventos/modulos/**/controladores`; `SecurityConfig.java`; `frontend/src/environments/environment.dev.ts`; `DOCUMENTACION_PROYECTO_TESIS_APP/09_API.md`.

# 12. Seguridad

## Medidas implementadas

- JWT firmado con clave Base64 y expiración configurable; sesión stateless.
- Filtro Bearer antes del filtro de autenticación estándar.
- BCrypt para contraseñas.
- Roles y `@PreAuthorize`; guardas de navegación Angular.
- Recuperación con token aleatorio de 32 bytes, expiración de 30 minutos y un solo uso.
- Validación de DTO con Bean Validation y manejo global de excepciones.
- CORS con orígenes configurables, métodos/cabeceras limitados, sin cookies y sin comodín.
- Secretos externalizados a `.env`; `.env` está ignorado y no está rastreado en Git actual.
- Comprobantes limitados a 5 MB; extensiones JPG/JPEG/PNG/PDF; nombre aleatorio; redimensionado de imágenes.
- QR de un solo uso a nivel de estado; reintentos registrados.
- Unicidad de inscripción, credencial, certificado y encuesta mediante restricciones JPA.

## Medidas parciales o riesgos actuales

- El QR contiene el UUID de credencial sin firma/cifrado; la protección depende de consultar el backend y del estado.
- La validación inicial de MIME del upload confía en `MultipartFile.getContentType()`; Tika detecta después, pero el código no vuelve a rechazar si el MIME real es no permitido.
- La ruta local normalizada no demuestra explícitamente confinamiento con `startsWith(baseDir)`.
- Varios endpoints por ID admiten roles de participante sin comprobar propiedad del recurso en el servicio; requiere validación de autorización por objeto.
- Si no hay servicio SMTP, el enlace completo de recuperación —incluido el token— se escribe en logs.
- `ddl-auto:update` es adecuado para desarrollo, pero riesgoso para producción sin migraciones.
- CSRF está deshabilitado; es coherente con Bearer sin cookies, condicionado a mantener ese modelo.
- Swagger/OpenAPI se pretende público, pero las rutas permitidas en `SecurityConfig` no reflejan completamente los paths personalizados `/api/v1/api-docs` y `/api/v1/swagger-ui.html`; ejecución no verificada.
- La verificación pública devuelve CI del titular; no hay política documentada de minimización/consentimiento.
- No hay MFA, rate limiting, refresh/revocación JWT, cabeceras explícitas, escaneo de dependencias, política de retención ni pruebas de penetración.

## Recomendaciones documentadas

Normalizar perfiles dev/prod, usar migraciones, fortalecer pruebas, limitar Swagger en producción, centralizar logs, usar TLS/reverse proxy, backups, almacenar uploads fuera del web root, firmar QR, definir privacidad y añadir controles avanzados. Son **RECOMENDADAS**, no implementadas salvo externalización de secretos y CORS.

**Fuentes internas:** `SecurityConfig.java`; `JwtService.java`; `AutenticacionServicio.java`; `CorreoServicio.java`; `ArchivoSeguroServicio.java`; `docs/release_1_0/06_documento_seguridad.md`; `docs/auditoria/auditoria-inicial.md`; commits `58234b5` y `cefba41`.

# 13. Auditoría

| ID | Hallazgo | Riesgo | Estado actual | Acción recomendada |
|---|---|---|---|---|
| AUD-01 | `.env` y secretos se reportaron como versionados | Alto | **RESUELTO**: `.env` ignorado/no rastreado; configuración usa variables | Mantener rotación y gestor de secretos en producción. |
| AUD-02 | `backend/target` estaba versionado | Bajo/medio | **RESUELTO**: no aparece en archivos rastreados | Mantener reglas `.gitignore`. |
| AUD-03 | No hay SQL/migraciones en `database/scripts` | Alto | **PENDIENTE** | Adoptar Flyway/Liquibase y versionar esquema/índices. |
| AUD-04 | Pruebas fuente escasas; ninguna prueba Java | Alto | **PENDIENTE** | Añadir unitarias, integración, API, e2e, carga y seguridad. |
| AUD-05 | Sin CI/CD, Dockerfile de aplicación ni despliegue productivo verificable | Medio | **PENDIENTE** | Definir pipeline, perfiles y artefactos de despliegue. |
| AUD-06 | Historial inicial concentrado en pocos commits | Medio | **HISTÓRICO/PARCIAL**: ahora hay cinco commits, aún compacto | Mantener trazabilidad por historia/sprint/PR. |
| AUD-07 | Prefijos `/api` y `/api/v1` mezclados | Medio | **PENDIENTE** | Definir contrato canónico versionado y migración. |
| AUD-08 | Guarda Angular de asistencias excluye ORGANIZADOR, backend lo permite | Medio | **PENDIENTE** | Alinear política de frontend/backend. |
| AUD-09 | Dashboard académico usa cifras fijas | Alto para resultados | **PENDIENTE** | Sustituir por consultas agregadas reales y probarlas. |
| AUD-10 | Exportaciones PDF/XLSX son cadenas simuladas | Alto | **PENDIENTE** | Generar archivos válidos y añadir pruebas de formato. |
| AUD-11 | Certificado guarda URL fija sin generar PDF | Alto | **PENDIENTE** | Implementar generación, almacenamiento y descarga real. |
| AUD-12 | QR no firmado ni cifrado | Medio | **PENDIENTE** | Firmar payload, validar contexto/expiración y rotar claves. |
| AUD-13 | Autorización por propiedad no comprobada en varios endpoints por ID | Alto potencial | **PARCIAL/REQUIERE PRUEBA** | Aplicar controles por usuario/organizador y pruebas IDOR. |
| AUD-14 | Token de recuperación puede aparecer en logs sin SMTP | Alto | **PENDIENTE** | No registrar tokens; usar entrega segura o modo demo explícito. |
| AUD-15 | Validación MIME real incompleta y confinamiento de path no explícito | Medio | **PARCIAL** | Rechazar por MIME detectado y verificar destino bajo base permitida. |
| AUD-16 | Prueba Angular espera un título del scaffolding inexistente | Bajo | **OBSOLETA** | Actualizar pruebas para comportamiento actual. |
| AUD-17 | CORS no explícito según auditoría anterior | Medio | **RESUELTO** por commit `cefba41` | Añadir prueba de orígenes permitidos/denegados. |

Se conservan los hallazgos resueltos para no borrar la historia de auditoría.

**Fuentes internas:** `docs/auditoria/auditoria-inicial.md`; `DOCUMENTACION_PROYECTO_TESIS_APP/07_SEGURIDAD.md`; `16_PROBLEMAS_DEUDA_TECNICA.md`; código citado; `.gitignore`; `git ls-files`; historial Git actual.

# 14. Scrum y Sprints

La metodología declarada para la monografía es Scrum. El repositorio contiene historias, backlog y una planificación de ocho sprints, pero no evidencia ceremonias, responsables, velocidad, fechas/duración, burndown, retrospectivas ni aceptación formal. La documentación de “pruebas” describe escenarios esperados, no resultados ejecutados. El MDD histórico tenía cuatro sprints MVP; `17_Backlog_Priorizado.md` declara que fue sustituido por ocho.

## Product Backlog

| ID | Historia de usuario | Prioridad | Estado |
|---|---|---|---|
| HU-01 | Ver eventos próximos | No documentada | **IMPLEMENTADO** |
| HU-02 | Definir cupos | No documentada | **IMPLEMENTADO** |
| HU-03 | Categorizar eventos | No documentada | **IMPLEMENTADO** |
| HU-04 | Inscribirse a curso pagado y subir comprobante | No documentada | **IMPLEMENTADO** |
| HU-05 | Ver pagos pendientes y aprobarlos | No documentada | **IMPLEMENTADO** |
| HU-06 | Recibir correo cuando el pago se apruebe | No documentada | **PENDIENTE** |
| HU-07 | Tener credencial QR en el celular | No documentada | **IMPLEMENTADO** como vista/PDF web |
| HU-08 | Escanear QR desde la web | No documentada | **IMPLEMENTADO** |
| HU-09 | Descargar certificado PDF tras cumplir asistencia | No documentada | **PARCIAL**; no existe PDF real |
| HU-10 | Verificar autenticidad de certificado | No documentada | **IMPLEMENTADO** |

## Sprints

### Sprint 1 — Preparación técnica, seguridad y usuarios

- **Objetivo:** base técnica, registro, login, perfil y recuperación.
- **Duración:** no documentada.
- **Historias/requisitos:** RF-01 a RF-04; CU-01; RNF-03, RNF-04 y RNF-08.
- **Tareas/entregable:** estructura modular, PostgreSQL, JWT, BCrypt, perfiles y recuperación.
- **Resultado:** código implementado; seguridad y pruebas no están cerradas.
- **Problemas/correcciones:** prueba backend ausente; token en log de fallback; autorización por objeto por revisar.
- **Estado:** **IMPLEMENTADO EN CÓDIGO / PARCIALMENTE VALIDADO**.

### Sprint 2 — Catálogos académicos y eventos

- **Objetivo:** administrar y publicar eventos con categorías/cupos.
- **Duración:** no documentada.
- **Historias/requisitos:** HU-01 a HU-03; CU-02, CU-07; RF-05 a RF-08.
- **Tareas/entregable:** facultades, carreras, categorías, CRUD de eventos, publicación y catálogo.
- **Resultado:** existe código y escenarios de prueba documentados.
- **Problemas/correcciones:** filtro por fecha ausente; estados divergentes; no hay resultado de ejecución.
- **Estado:** **PARCIAL**.

### Sprint 3 — Inscripciones y cupos

- **Objetivo:** inscripción gratuita/pagada, unicidad y cupos.
- **Duración:** no documentada.
- **Historias/requisitos:** HU-04; CU-03/CU-04; RF-09, RF-10 y RF-12.
- **Tareas/entregable:** alta, consulta, cancelación y actualización de cupos.
- **Resultado:** flujo principal implementado; escenarios Postman documentados.
- **Problemas/correcciones:** lista de espera ausente; posible concurrencia de cupos no probada.
- **Estado:** **IMPLEMENTADO EN CÓDIGO**, excepto lista de espera.

### Sprint 4 — Pagos, credenciales y QR

- **Objetivo:** completar pago y habilitar credencial/QR.
- **Duración:** no documentada.
- **Historias/requisitos:** HU-05 a HU-07; CU-05/CU-08/CU-12/CU-13; RF-10, RF-11, RF-13, RF-14.
- **Tareas/entregable:** comprobante, validar/rechazar, PDF de credencial y QR.
- **Resultado:** pago y credencial PDF implementados.
- **Problemas/correcciones:** notificación de pago y firma QR pendientes; no existe documento de prueba propio del sprint.
- **Estado:** **PARCIAL**.

### Sprint 5 — Control de acceso y asistencia

- **Objetivo:** validar ingreso por QR y contingencia manual.
- **Duración:** no documentada.
- **Historias/requisitos:** HU-08; CU-09; RF-15/RF-16.
- **Tareas/entregable:** validar, autorizar/denegar, consumir QR, asistencia e historial.
- **Resultado:** implementado y cubierto por especificación `sprint_6_pruebas.md` —numeración histórica distinta—.
- **Problemas/correcciones:** tolerancia horaria no implementada; no hay resultados ejecutados.
- **Estado:** **IMPLEMENTADO EN CÓDIGO / PARCIALMENTE VALIDADO**.

### Sprint 6 — Certificación y validación pública

- **Objetivo:** emitir certificado por asistencia y verificarlo.
- **Duración:** no documentada.
- **Historias/requisitos:** HU-09/HU-10; CU-06/CU-10/CU-14; RF-17 a RF-19.
- **Tareas/entregable:** cierre, criterio de asistencia, código, archivo y portal público.
- **Resultado:** registro/código/verificación implementados.
- **Problemas/correcciones:** no hay PDF, proceso masivo, porcentaje ni liberación; el archivo de pruebas se llama `sprint_7_pruebas.md`.
- **Estado:** **PARCIAL**.

### Sprint 7 — Encuestas, estadísticas y reportes

- **Objetivo:** retroalimentación y analítica institucional.
- **Duración:** no documentada.
- **Historias/requisitos:** CU-11; RF-20 a RF-22; encuestas sin HU/RF formal.
- **Tareas/entregable:** encuestas, dashboards, reportes y exportaciones.
- **Resultado:** encuestas y varios listados reales; dashboard académico y exportaciones simulados.
- **Problemas/correcciones:** filtros económicos/asistencia pendientes; datos ficticios deben excluirse de resultados académicos.
- **Estado:** **PARCIAL**.

### Sprint 8 — Calidad y liberación

- **Objetivo:** comprobar funcionalidad, rendimiento, accesibilidad, logging y capacidad.
- **Duración:** no documentada.
- **Historias/requisitos:** RNF-01, RNF-02, RNF-06, RNF-07, RNF-09 y RNF-10.
- **Tareas/entregable:** pruebas integrales, carga, responsive/WCAG y evidencia de liberación.
- **Resultado:** documentos de escenarios y una prueba Angular mínima; sin suite backend ni evidencia de ejecución.
- **Problemas/correcciones:** la prueba Angular actual es obsoleta; no hay CI/cobertura.
- **Estado:** **PENDIENTE DE VERIFICACIÓN**.

**Fuentes internas:** `docs/17_Backlog_Priorizado.md`; `docs/11_Historias_de_Usuario.md`; `docs/sprint_*_pruebas.md`; `docs/pruebas_recuperacion_contrasena.md`; `docs/DOCUMENTO_MAESTRO_DESARROLLO.md`; `frontend/src/app/app.spec.ts`.

# 15. Historias de usuario

| ID | Historia consolidada | RF relacionados | Estado |
|---|---|---|---|
| HU-01 | Como estudiante, quiero ver una lista de eventos próximos para saber en cuáles puedo participar. | RF-07 | **IMPLEMENTADO** |
| HU-02 | Como coordinador, quiero definir cupos para no exceder la capacidad. | RF-05, RF-06 | **IMPLEMENTADO** |
| HU-03 | Como administrador, quiero categorizar eventos para facilitar su búsqueda. | RF-05, RF-07 | **IMPLEMENTADO** |
| HU-04 | Como participante externo, quiero inscribirme a un curso pagado subiendo mi comprobante para asegurar mi cupo. | RF-10 | **IMPLEMENTADO** |
| HU-05 | Como validador financiero, quiero ver pagos pendientes para aprobarlos rápidamente. | RF-11 | **IMPLEMENTADO** por otros roles técnicos |
| HU-06 | Como usuario, quiero recibir un correo cuando mi pago sea aprobado. | RF-11 | **PENDIENTE** |
| HU-07 | Como participante, quiero tener mi credencial QR en el celular para no imprimir. | RF-13, RF-14 | **IMPLEMENTADO/PARCIAL** por QR sin firma |
| HU-08 | Como registrador, quiero escanear QR desde la web para automatizar el registro. | RF-15 | **IMPLEMENTADO** |
| HU-09 | Como asistente, quiero descargar mi certificado PDF tras cumplir la asistencia para añadirlo a mi CV. | RF-17, RF-18 | **PARCIAL** |
| HU-10 | Como empleador, quiero verificar que un certificado sea auténtico y emitido por la UAJMS. | RF-19 | **IMPLEMENTADO** |

**Fuentes internas:** `docs/11_Historias_de_Usuario.md`; `docs/12_Matriz_de_Trazabilidad.md`; código de módulos relacionados.

# 16. Casos de uso

| ID | Caso de uso | Actor principal | Descripción | Requisitos relacionados |
|---|---|---|---|---|
| CU-01 | Registrarse | Participante | Crear cuenta e iniciar acceso | RF-01, RF-02 |
| CU-02 | Ver catálogo | Participante | Consultar eventos publicados | RF-07 |
| CU-03 | Inscribirse a evento gratuito | Participante | Confirmar inscripción con cupo | RF-06, RF-09 |
| CU-04 | Inscribirse a evento pagado | Participante | Inscribirse y adjuntar comprobante | RF-10 |
| CU-05 | Descargar credencial QR | Participante | Obtener PDF/QR | RF-13, RF-14 |
| CU-06 | Descargar certificado | Participante | Obtener certificado elegible | RF-17, RF-18 |
| CU-07 | Crear evento | Coordinador/Organizador | Registrar datos, costo, fechas y cupos | RF-05, RF-06 |
| CU-08 | Validar pago | Validador/Administrador/Organizador | Aprobar o rechazar comprobante | RF-11 |
| CU-09 | Registrar asistencia por QR | Personal de control | Validar, autorizar y marcar | RF-15 |
| CU-10 | Finalizar evento y liberar certificados | Organizador | Cerrar evento y habilitar certificación | RF-17, RF-18 |
| CU-11 | Generar reportes | Administrador/Organizador | Consultar indicadores y exportar | RF-20 a RF-22 |
| CU-12 | Generar QR único | Sistema | Crear QR asociado a credencial | RF-13 |
| CU-13 | Notificar estado de inscripción/pago | Sistema | Comunicar aprobación/rechazo | RF-11 |
| CU-14 | Verificar certificado | Verificador público | Consultar validez por código | RF-19 |

Estados: CU-01/02/03/04/05/07/08/09/14 **IMPLEMENTADOS** en su flujo principal; CU-06/10/11/12 **PARCIALES**; CU-13 **PENDIENTE**. No se añaden casos para encuestas porque no existen en el documento formal, aunque el módulo está implementado.

**Fuentes internas:** `docs/10_Casos_de_Uso.md`; `docs/07_Requerimientos_Funcionales.md`; controladores/servicios relacionados.

# 17. Estado actual del proyecto

## Implementado

- Monolito modular Spring Boot, API REST, SPA Angular y modelo JPA/PostgreSQL.
- Registro, login JWT, perfil, cambio y recuperación de contraseña.
- Roles técnicos y catálogos de facultades, carreras y categorías.
- CRUD/ciclo principal de eventos y control de cupos.
- Inscripciones gratuitas y pagadas, cancelación y unicidad.
- Pagos con comprobante y validación/rechazo manual.
- Credencial digital con QR y PDF.
- Control de acceso, consumo de QR, asistencia e historial.
- Verificación pública de certificados.
- Encuestas de satisfacción y estadísticas.
- Listados/reportes básicos y dashboard operativo.
- Externalización de secretos y CORS configurable.

## Parcialmente implementado

- Catálogo sin filtro de fecha.
- Estados de evento distintos de la especificación.
- Notificación solo para recuperación, no para resultado de pago.
- QR único pero no firmado/cifrado.
- Certificado como registro/URL, sin PDF real, porcentaje o masividad.
- Reportes sin filtros completos; analítica académica simulada.
- Exportaciones con MIME correcto pero contenido ficticio.
- Logging, autorización por objeto y validación de archivo incompletos.
- Evidencia de pruebas y Scrum.

## Pendiente

- Lista de espera.
- Certificado PDF real, almacenamiento, liberación y emisión masiva/asíncrona.
- Notificaciones de pago/eventos/certificados.
- QR firmado y ventana horaria de ingreso.
- Migraciones SQL, CI/CD, despliegue productivo, backups ejecutados y monitorización.
- Pruebas backend, integración, e2e, seguridad, carga, WCAG y capacidad.
- Aplicación Flutter, push, offline y almacenamiento seguro móvil.

## Problemas conocidos

Rutas API heterogéneas; frontend `/api/v1` frente a reportes `/api`; guardas no alineadas; dashboard/exportaciones simulados; prueba Angular obsoleta; URLs de certificado/medios hardcoded de demostración; directorio SQL vacío; Swagger custom y reglas de seguridad potencialmente desalineadas.

## Riesgos

Uso indebido de endpoints por ID si no se valida propiedad; filtración de token de recuperación por logs; falsificación/reproducción de identificadores QR; resultados académicos inválidos si se usan datos simulados; pérdida/inconsistencia de esquema sin migraciones; ausencia de evidencia de rendimiento, disponibilidad y recuperación.

## Próximos pasos documentados y derivados directamente de hallazgos

1. Sustituir datos/archivos simulados y completar certificados.
2. Corregir controles de autorización por recurso y manejo seguro de tokens/uploads.
3. Versionar el esquema y normalizar la API.
4. Crear pruebas trazables y ejecutar aceptación/carga/accesibilidad.
5. Levantar requisitos móviles antes de iniciar Flutter.

**Fuentes internas:** `docs/12_Matriz_de_Trazabilidad.md`; `docs/17_Backlog_Priorizado.md`; `docs/auditoria/auditoria-inicial.md`; `DOCUMENTACION_PROYECTO_TESIS_APP/INFORME_EJECUTIVO.md`; evidencia de código actual.

# 18. Web y aplicación móvil

## Plataforma Web

Pantallas localizadas: login, registro, recuperar/restablecer contraseña; landing, catálogo, detalle e inscripción; dashboard/perfil; mis inscripciones y detalle; pagos propios/registro; credenciales propias/detalle; administración de facultades, carreras, categorías y eventos; validación de pagos; scanner, historial y lista de asistencia; certificados propios y validación pública; encuestas/respuestas; dashboards y reportes.

La navegación usa lazy loading, `authGuard`, `roleGuard` e interceptor JWT. La ejecución visual, responsive y accesibilidad no fueron verificadas. El frontend permite `ADMINISTRADOR`/`PERSONAL_CONTROL` en asistencias, pero el backend también admite `ORGANIZADOR`. Reportes en backend están bajo `/api`, mientras el entorno frontend declara base `/api/v1`, lo cual requiere verificación del servicio concreto/proxy.

## Aplicación Flutter

| Aspecto | Estado documentado |
|---|---|
| Proyecto Flutter/Dart | **PENDIENTE**; no hay carpeta móvil ni `pubspec.yaml`. |
| Arquitectura | **DEFINIDA COMO PROPUESTA**: cliente Flutter reutiliza la API/backend/BD existentes. |
| Funciones previstas | Catálogo/detalle, login/perfil, inscripciones, pagos propios, credencial/QR, certificados, encuestas; scanner para personal de control. |
| Pantallas | **NO DEFINIDAS FORMALMENTE**; no existen artefactos de UI/navegación móvil. |
| Navegación | **PENDIENTE**. |
| Consumo de API | Backend reutilizable **IMPLEMENTADO**; contrato móvil, paginación y estabilidad no aprobados. |
| Autenticación | JWT reutilizable **IMPLEMENTADO**; almacenamiento seguro, refresh y revocación **PENDIENTES**. |
| Push | **PENDIENTE**; no hay FCM ni endpoints de dispositivo/notificación. |
| Offline | **PENDIENTE**; no hay sincronización ni resolución de conflictos. |
| QR móvil | Presentación/lectura **PROPUESTA**; seguridad criptográfica del QR pendiente también en web. |

Existe una contradicción de alcance: `05_Fuera_de_Alcance.md` excluye la app móvil en la primera fase, mientras el contexto actual desea incorporarla a la solución/monografía. Se requiere una decisión formal de fase y requisitos; no debe reescribirse la historia afirmando que Flutter ya formaba parte del release web.

**Fuentes internas:** `frontend/src/app/app.routes.ts`; componentes bajo `features`; `DOCUMENTACION_PROYECTO_TESIS_APP/10_FRONTEND.md`; `19_ANALISIS_APP_MOVIL.md`; `docs/05_Fuera_de_Alcance.md`.

# 19. Matriz de trazabilidad

| Objetivo | Requisito | Historia de usuario | Caso de uso | Sprint | Implementación |
|---|---|---|---|---|---|
| OE-01 Identidad segura | RF-01 a RF-04; RNF-03/RNF-04 | Sin HU formal específica | CU-01 | 1 | Auth, usuarios, JWT, BCrypt, perfil y recuperación; **IMPLEMENTADO**. |
| OE-02 Catálogo unificado | RF-05 a RF-08 | HU-01 a HU-03 | CU-02, CU-07 | 2 | Catálogos/eventos **IMPLEMENTADOS**; fecha/estados **PARCIALES**. |
| OE-03 Inscripciones/pagos | RF-09 a RF-12 | HU-04 a HU-06 | CU-03, CU-04, CU-08, CU-13 | 3-4 | Flujo y validación **IMPLEMENTADOS**; notificación/lista de espera pendientes. |
| OE-04 Control QR | RF-13 a RF-16; RNF-05 | HU-07, HU-08 | CU-05, CU-09, CU-12 | 4-5 | Credencial/escaneo/asistencia **IMPLEMENTADOS**; firma QR **PENDIENTE**. |
| OE-05 Certificación | RF-17 a RF-19; RNF-02 | HU-09, HU-10 | CU-06, CU-10, CU-14 | 6 | Registro/verificación **IMPLEMENTADOS**; PDF/porcentaje/masividad **PARCIALES**. |
| OE-06 Inteligencia | RF-20 a RF-22 | Sin HU formal | CU-11 | 7 | Listados/operativo **IMPLEMENTADOS**; académico/exportaciones/filtros **PARCIALES**. |
| OE-07 Calidad/disponibilidad | RNF-01, RNF-06 a RNF-10 | No aplica | No aplica | 8 | Modularidad **IMPLEMENTADA**; métricas, WCAG, capacidad y pruebas **PENDIENTES**. |

Encuestas de satisfacción quedan fuera de la cadena formal objetivo→RF→HU→CU, aunque tienen implementación. Deben incorporarse mediante una decisión de alcance futura, no asignarse retroactivamente sin aprobación.

**Fuentes internas:** `docs/02_Objetivos_Generales.md`; `03_Objetivos_Especificos.md`; `07_Requerimientos_Funcionales.md`; `08_Requerimientos_No_Funcionales.md`; `10_Casos_de_Uso.md`; `11_Historias_de_Usuario.md`; `12_Matriz_de_Trazabilidad.md`; `17_Backlog_Priorizado.md`.

# CONTRADICCIONES Y DECISIONES PENDIENTES

| ID | Contradicción o decisión | Evidencia A | Evidencia B / estado real | Decisión pendiente |
|---|---|---|---|---|
| CD-01 | Nombre “Sistema Web” vs “Plataforma Web” | `PORTADA.docx` | README/docs recientes usan Plataforma | Aprobar denominación académica final. |
| CD-02 | Spring Boot vs Quarkus | Portada, capítulo III, dice Quarkus | `pom.xml` y código usan Spring Boot 3.3 | Corregir la monografía fuente. |
| CD-03 | Flutter excluido vs nueva intención móvil | `05_Fuera_de_Alcance.md` | Encargo actual contempla Flutter | Definir si es nueva fase, objetivo y alcance aprobado. |
| CD-04 | Cuatro sprints vs ocho sprints | MDD histórico | Backlog reciente declara ocho y sustituye al anterior | Conservar historia y usar ocho como planificación vigente. |
| CD-05 | Nombres de sprint/pruebas no alineados | archivos `sprint_6`, `7`, `8` | backlog asigna esos dominios a sprints 5, 6 y 7 | Mapear explícitamente sin renombrar historia. |
| CD-06 | Rol Validador Financiero documentado, ausente en código | actores/MDD | pagos autorizados a ADMINISTRADOR/ORGANIZADOR | Crear rol o actualizar modelo organizacional. |
| CD-07 | Superadmin/admin/coordinador documentados | actores | código usa ADMINISTRADOR/ORGANIZADOR | Definir equivalencias y alcance por facultad. |
| CD-08 | Estados de evento documentados difieren | Borrador→Pendiente→Publicado→Finalizado/Archivado | enum real añade cerradas/en curso/cancelado y no pendiente/archivado | Aprobar máquina de estados canónica. |
| CD-09 | Convenciones BD plural vs singular | `07_Convenciones_BD.md` | `08_Estandarizacion_Idioma.md` y tablas reales son mixtas | Adoptar convención y migraciones. |
| CD-10 | Entidades `Auditoria`, `Notificacion`, `MetodoPago` documentadas | catálogo/MDD | no existen entidades actuales | Implementar o retirar del modelo vigente. |
| CD-11 | Datos QR en inscripción | diccionario antiguo | código usa tabla `codigos_qr` ligada a credencial | Actualizar diccionario. |
| CD-12 | Certificado PDF afirmado como implementado | release/portada/conclusiones | servicio solo crea URL fija y devuelve DTO | No presentar PDF de certificado como resultado. |
| CD-13 | Reportes exportables afirmados | alcance/release | endpoints devuelven cadenas mock | Clasificar como parcial hasta archivos válidos. |
| CD-14 | Reporte por facultad afirmado | requisitos/release | dashboard académico tiene cifras hardcoded | Sustituir simulación antes de usar resultados. |
| CD-15 | API versionada | frontend y mayor parte backend `/api/v1` | reportes `/api`; otros controladores duplican `/api` | Definir versión canónica. |
| CD-16 | Permiso de ORGANIZADOR para asistencias | backend lo admite | guarda Angular lo excluye | Alinear UX/política. |
| CD-17 | Inscripción denominada pública | componente `inscripcion-publica` | endpoint exige JWT/rol | Aclarar que es pantalla pública con autenticación requerida, o cambiar requisito. |
| CD-18 | Auditoría antigua dice secretos/target rastreados y CORS ausente | auditoría inicial/agentes | Git actual muestra correcciones y CORS implementado | Mantener como hallazgo resuelto. |
| CD-19 | Diagramas “encontrados” por auditoría IA | documentos generados citan `docs/design/*.wsd` | directorios `docs/design`/`diagramas` no contienen archivos en estado actual | Recuperar artefactos o corregir inventario. |
| CD-20 | README pide Angular CLI 19+ | README | `package.json` usa CLI/Angular 21 | Actualizar requisito. |
| CD-21 | Swagger path personalizado | `application.yml` y README usan `/api/v1/...` | `SecurityConfig` permite paths estándar | Verificar en ejecución y alinear reglas. |
| CD-22 | Prueba frontend | espera `Hello, frontend` | `app.html` solo contiene `router-outlet` | Actualizar/eliminar prueba obsoleta. |
| CD-23 | “3FN, índices y auditoría JSONB implementados” | validación/MDD/portada | sin DDL/migraciones ni entidad auditoría | Tratar como diseño, no resultado probado. |
| CD-24 | Conclusiones de portada afirman solución terminada | `PORTADA.docx` | varios RF/RNF siguen parciales/pendientes | Reescribir conclusiones tras pruebas reales. |

**Fuentes internas:** todas las fuentes citadas en la tabla; `git log`; inventario actual con `rg --files`; código fuente.

# 21. Fuentes internas y cobertura documental

Se analizaron 78 documentos relevantes:

- 47 archivos bajo `docs/`: visión, objetivos, alcance, actores, datos, RF/RNF, reglas, casos, historias, trazabilidad, riesgos, arquitectura, módulos, backlog, glosario, auditoría, pruebas y release 1.0.
- 28 archivos bajo `DOCUMENTACION_PROYECTO_TESIS_APP/`, incluida la documentación maestra generada previamente y su control de información.
- `README.md` raíz y `frontend/README.md`.
- `documentacion/PORTADA.docx`, cuyo texto fue extraído directamente del OOXML sin modificar el archivo.

Fuentes de implementación contrastadas: `backend/pom.xml`; `backend/src/main/resources/application.yml`; 157 archivos Java; `frontend/package.json`; `frontend/angular.json`; 84 archivos TypeScript y 42 plantillas HTML; `docker/docker-compose.yml`; `.env.example`; `.gitignore`; dos colecciones Postman; historial Git. Los archivos dentro de `node_modules`, `dist`, `target` y licencias de dependencias no se consideraron documentación del proyecto.

## Información faltante para elaborar correctamente la monografía

- Validación institucional del problema, línea base, población, muestra e instrumentos.
- Nombre académico definitivo, programa/diplomado, tutor, unidad, lugar y gestión.
- Objetivos aprobados y decisión formal sobre Flutter.
- Product Owner, Scrum Master, equipo, duración/fechas de sprint, ceremonias, criterios de aceptación y retrospectivas.
- Resultados ejecutados de pruebas, cobertura, evidencias de aceptación y correcciones.
- Métricas reales de rendimiento, disponibilidad, usabilidad, accesibilidad y satisfacción.
- Esquema versionado, índices reales, diccionario actualizado, backup/restauración y datos de volumen.
- Ambiente de producción, arquitectura de despliegue, dominio/TLS, monitorización y soporte.
- Política de privacidad, consentimiento, retención, clasificación de datos y responsables.
- Contrato OpenAPI validado y política canónica de rutas/roles.
- Requisitos móviles: actores, plataformas, pantallas, UX, offline, push, seguridad y criterios de aceptación.
- Evidencias visuales válidas de la web y diagramas actuales.
- Resultados reales que permitan redactar conclusiones sin confundir diseño o código con impacto institucional.

# Resumen cuantitativo final

| Métrica | Resultado |
|---|---:|
| Archivos documentales analizados | 78 |
| Requisitos funcionales consolidados | 22 |
| RF implementados en su condición principal | 12 |
| RF parciales | 9 |
| RF pendientes | 1 |
| Requisitos no funcionales | 10 |
| RNF implementados | 3 |
| RNF parcial | 1 |
| RNF definidos/pendientes sin verificación | 6 |
| Actores documentados | 8 |
| Roles técnicos comprobados | 6 |
| Historias de usuario | 10 |
| Casos de uso | 14 |
| Sprints de la planificación vigente | 8 |
| Módulos funcionales backend identificados | 14 |
| Pantallas/componentes Angular funcionales identificados | 36 |
| Operaciones REST inventariadas | 81 |
| Entidades/tablas JPA identificadas | 21 |
| Funcionalidades principales implementadas —agrupación de estado, no RF— | 15 |
| Pendientes principales —agrupación de estado— | 12 |
| Contradicciones/decisiones registradas | 24 |

Las 15 funcionalidades implementadas agrupadas son: autenticación; perfiles/recuperación; roles; catálogos académicos; categorías; eventos; inscripciones; pagos/comprobantes; credencial PDF; QR; control de acceso; asistencia; verificación de certificados; encuestas; y listados/dashboard operativo. Los 12 pendientes agrupados son: lista de espera; notificación de pagos; QR firmado; regla horaria; certificado PDF; porcentaje de asistencia; emisión masiva/asíncrona; analítica académica real; exportaciones reales; filtros completos; migraciones/pruebas/despliegue como frente técnico conjunto; y cliente Flutter.

---

**Conclusión documental:** el repositorio respalda una plataforma web amplia a nivel de código, pero no una solución completamente aceptada o productiva. La fuente más confiable para la monografía debe presentar como resultados implementados solo las capacidades verificadas en código, separar los componentes parciales —especialmente certificación y reportes— y tratar Flutter como una nueva fase hasta que exista una decisión formal de alcance.
