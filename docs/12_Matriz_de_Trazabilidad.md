# 12. Matriz de Trazabilidad

## Propósito y alcance de la revisión

Esta matriz relaciona los requisitos ya documentados con actores, casos de uso, historias de usuario y evidencia localizada en el código existente. No incorpora requisitos nuevos ni constituye una prueba de aceptación.

**Estados:** **Implementado** (evidencia directa), **Parcial** (la evidencia no cubre toda la condición documentada) y **No verificado** (no se localizó evidencia suficiente).

## Requisitos funcionales

| ID | Actor | Requisito o funcionalidad | Historia de usuario / caso de uso relacionado | Estado | Evidencia o módulo donde está implementado |
|---|---|---|---|---|---|
| RF-01.01 | Participante externo | Registro de usuarios | CU-01; HU-01 (implícita) | Implementado | `AutenticacionControlador`, `AutenticacionServicio` y `features/auth/registro`. |
| RF-01.02 | Participante | Autenticación con correo, contraseña y JWT | CU-01 | Implementado | `AutenticacionControlador`, `AutenticacionServicio`, `core/seguridad` y `features/auth/login`. |
| RF-01.03 | Participante | Gestión de perfil | Sin caso de uso o historia específica documentados | Implementado | `UsuarioControlador` (`/perfil`), `UsuarioServicio` y `features/privado/perfil-usuario`. |
| RF-01.04 | Participante | Recuperación de contraseña por correo | Sin caso de uso o historia específica documentados | Implementado | `AutenticacionControlador`, `CorreoServicio`, `TokenRecuperacionRepository` y `features/auth/recuperar-contrasena`. |
| RF-02.01 | Coordinador de Facultad/Carrera | Creación de eventos | CU-07; HU-02 | Implementado | `EventoController`, `EventoService` y `features/admin/eventos/evento-form`. |
| RF-02.02 | Coordinador de Facultad/Carrera | Gestión de cupos | HU-02 | Implementado | `InscripcionService` valida y actualiza `cupoDisponible`; entidad `Evento`. |
| RF-02.03 | Participante | Catálogo público con filtros por categoría y fecha | CU-02; HU-01 | Parcial | `EventoController` ofrece `/publicados` y `/categoria/{id}`; existe `catalogo-eventos-publico`, pero no se localizó filtro por fecha. |
| RF-02.04 | Coordinador de Facultad/Carrera | Gestión de estados de eventos | Sin historia específica documentada | Parcial | `EstadoEvento` y `EventoController` implementan borrador, publicación y finalización; no existe el estado documentado `PENDIENTE`. |
| RF-03.01 | Participante | Inscripción gratuita | CU-03 | Implementado | `InscripcionService` confirma cuando `TipoInscripcion` es `GRATUITO`; `InscripcionController` e `inscripcion-publica`. |
| RF-03.02 | Participante | Inscripción de pago y carga de comprobante | CU-04; HU-04 | Implementado | `PagoController`, `PagoService`, `ArchivoSeguroServicio` y `registrar-pago`. |
| RF-03.03 | Validador Financiero | Validación de pagos y notificación al usuario | CU-08; HU-05, HU-06 | Parcial | `PagoService` valida o rechaza pagos y actualiza la inscripción; no se localizó envío de notificación en ese flujo. |
| RF-03.04 | Participante | Lista de espera | Sin caso de uso o historia documentados | No verificado | No se localizó entidad, endpoint ni interfaz de lista de espera. |
| RF-04.01 | Participante | Generación de QR único encriptado | CU-12; HU-07 | Parcial | `CredencialService`, `CodigoQrService` y `codigo_qr` generan y validan QR; no se localizó firma o cifrado del contenido. |
| RF-04.02 | Participante | Credencial digital en vista o PDF | CU-05; HU-07 | Parcial | `CredencialController` ofrece credencial, QR y descarga; existe `features/privado/credenciales`, pero el formato PDF no se confirma en `CredencialService`. |
| RF-04.03 | Registrador de Asistencia | Escaneo y registro mediante QR | CU-09; HU-08 | Implementado | `ControlAccesoController`, `ControlAccesoService`, `AsistenciaService` y `features/asistencias/control-scanner`. |
| RF-04.04 | Registrador de Asistencia | Registro manual por CI o código de inscripción | Sin historia específica documentada | Implementado | `ControlAccesoController` expone búsquedas por `codigo` y `documento`; `ControlAccesoService` implementa ambas. |
| RF-05.01 | Coordinador de Facultad/Carrera | Generación automática de certificados PDF al cierre | CU-10; HU-09 | Parcial | `CertificadoService` crea un registro si el evento está finalizado; no se localizó generación de PDF ni proceso automático masivo. |
| RF-05.02 | Coordinador de Facultad/Carrera | Certificación según porcentaje de asistencia | Sin caso de uso o historia específica documentados | Parcial | `CertificadoService` exige al menos una asistencia; no calcula el porcentaje configurable documentado. |
| RF-05.03 | Verificador de Certificado | Verificación pública por código único | CU-14; HU-10 | Implementado | `CertificadoController` (`/verificacion-certificados/{codigo}`), `CertificadoService` y `validacion-publica`. |
| RF-06.01 | Administrador de Eventos | Reporte de participación por facultad | CU-11 | Implementado | `DashboardController` (`/dashboard/academico`, `/reportes/participantes`) y módulo `reportes`. |
| RF-06.02 | Administrador de Eventos | Reporte económico por evento y rango de fechas | CU-11 | Parcial | `DashboardController` expone `/reportes/pagos`; la evidencia revisada no confirma filtros por evento y rango de fechas. |
| RF-06.03 | Administrador de Eventos | Reporte de asistencia con hora de registro | CU-11 | Parcial | `Asistencia` registra `fechaHoraRegistro` y existe `/api/v1/asistencias/evento/{id}`; no se localizó el reporte específico descrito. |

## Requisitos no funcionales

| ID | Actor | Requisito o funcionalidad | Historia de usuario / caso de uso relacionado | Estado | Evidencia o módulo donde está implementado |
|---|---|---|---|---|---|
| RNF-01.01 | No aplica | Catálogo en menos de 2 segundos con 100 usuarios concurrentes | No aplica | No verificado | No se localizaron pruebas de carga ni métricas del umbral. |
| RNF-01.02 | No aplica | Generación asíncrona de certificados masivos | No aplica | No verificado | `CertificadoService` genera un certificado por inscripción; no se localizó procesamiento asíncrono o masivo. |
| RNF-02.01 | Participante | Contraseñas cifradas con BCrypt | CU-01 | Implementado | `SecurityConfig` y servicios del módulo `usuarios`. |
| RNF-02.02 | Participante | Expiración configurable de JWT | CU-01 | Implementado | `JwtPropiedades`, `JwtService` y `application.yml`. |
| RNF-02.03 | Participante | QR firmado contra generación fraudulenta | CU-12 | No verificado | `CodigoQrService` genera la imagen QR; no se localizó firma del contenido. |
| RNF-03.01 | Participante / Registrador de Asistencia | Diseño responsivo | CU-02, CU-09 | No verificado | Hay componentes Angular, pero no evidencia verificable de pruebas responsivas. |
| RNF-03.02 | Participante | Accesibilidad WCAG 2.1 básica | No aplica | No verificado | No se localizaron evidencias de evaluación WCAG. |
| RNF-04.01 | Equipo de desarrollo | Arquitectura modular | No aplica | Implementado | Paquetes por módulo en `backend/.../modulos` y módulos Angular en `frontend/src/app/features`. |
| RNF-04.02 | Equipo de desarrollo | Registro de errores con SLF4J/Logback | No aplica | No verificado | No se localizó configuración o uso explícito de SLF4J/Logback. |
| RNF-05.01 | Administración del sistema | Capacidad de 50.000 certificados/año | No aplica | No verificado | No se localizaron pruebas de capacidad ni métricas de base de datos. |

## Funcionalidades implementadas sin correspondencia explícita

Las siguientes capacidades tienen evidencia de código, pero no un RF, CU o HU específico en los documentos `07`, `10` y `11`:

* Administración de facultades, carreras y categorías: módulos `facultades`, `carreras` y `categorias`; interfaces en `features/admin`.
* Cancelación de inscripciones: `InscripcionController` y `InscripcionService`.
* Cambio de contraseña del perfil: `UsuarioControlador` y `UsuarioServicio`.
* Encuestas de satisfacción y estadísticas: módulo `encuestas`, `EncuestaController` y `features/encuestas`.
* Exportación de reportes en PDF y Excel: endpoints de `DashboardController`.

## Inconsistencias comprobadas

* El flujo documentado incluye `PENDIENTE`; `EstadoEvento` no lo define y sí define `INSCRIPCIONES_CERRADAS`, `EN_CURSO` y `CANCELADO`.
* RF-03.03 y HU-06 mencionan notificación del pago aprobado, pero `PagoService` no invoca el servicio de correo en los métodos revisados.
* RF-04.01 y RNF-02.03 describen cifrado o firma del QR; `CodigoQrService` no muestra esa protección.
* RF-05.01 describe generación automática de PDF; `CertificadoService` crea el registro, pero no genera el PDF ni programa un proceso automático.
* RF-05.02 y RN-06 requieren un porcentaje de asistencia; `CertificadoService` comprueba únicamente que exista al menos una asistencia.

## Relación con reglas de negocio

* **RN-01** y **RN-03** están respaldadas por `InscripcionService` mediante validación de unicidad y cupos.
* **RN-02** está respaldada parcialmente: `PagoService` confirma la inscripción tras validar el pago, pero no genera la credencial ni el QR.
* **RN-04** está respaldada por `ControlAccesoService`, que impide autorizar un QR cuyo estado ya no es `GENERADO`.
* **RN-05**, **RN-07**, **RN-08** y **RN-09** no cuentan con evidencia suficiente. **RN-06** se aplica parcialmente, como se indica para RF-05.02.
