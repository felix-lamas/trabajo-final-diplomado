# 11. SEGURIDAD

- JWT para autenticación.
- Hashing seguro de contraseñas.
- Autorización por rol.
- Control de acceso a recursos propios.
- Validación de archivos.
- Límite de 5 MB cuando corresponda.
- Secretos mediante variables de entorno.
- Sin credenciales en repositorio.
- Sin datos personales reales en evidencias sin autorización.
- Evitar tokens o información sensible en logs.

## Pendientes derivados de la auditoría
- autorización a nivel de objeto;
- QR firmado/expirable;
- token en logs;
- almacenamiento de JWT en frontend;
- rate limiting/MFA.

## Seed de demostración
`DatosInicialesSeed` está restringido al perfil Spring `demo`. El arranque normal, incluido el perfil predeterminado `dev`, no registra ni ejecuta el seed.

El perfil `demo` debe activarse únicamente de forma explícita en entornos controlados mediante `SPRING_PROFILES_ACTIVE=demo`. La contraseña demo se suministra mediante la variable local `DEMO_PASSWORD`; no se incorpora ningún valor al repositorio. En producción el perfil `demo` debe permanecer inactivo.

## Autorización y propiedad de eventos

La autorización del módulo de eventos se aplica en el backend y no depende de los guards o de la visibilidad de controles en Angular.

- `ADMINISTRADOR` conserva alcance global sobre los eventos.
- `ORGANIZADOR` puede consultar y modificar sus eventos; en los listados también puede consultar los eventos publicados de otros organizadores.
- Los roles de participante actuales solo reciben eventos en estado `PUBLICADO`.
- Las consultas públicas por identificador devuelven exclusivamente eventos `PUBLICADO`.
- La publicación queda restringida a `ADMINISTRADOR` y no se autoriza por el solo hecho de ser propietario.

Para reducir la revelación de existencia, un evento fuera del alcance del usuario se trata como recurso no encontrado (`404`). Las prohibiciones de rol declaradas en los controladores se resuelven como acceso denegado (`403`) por Spring Security.

Las operaciones de organizador utilizan consultas acotadas por identificador de evento e identificador de organizador. Los UUID identifican recursos, pero no constituyen un mecanismo de autorización.

Queda pendiente incorporar el estado `EN_REVISIÓN` y completar el flujo `BORRADOR → EN_REVISIÓN → PUBLICADO`. Esta corrección no agrega estados ni migraciones y mantiene temporalmente la transición administrativa existente desde `BORRADOR`.

## Autorización y propiedad de inscripciones

La autorización del módulo de inscripciones se aplica en el backend con consultas acotadas al ámbito del usuario autenticado:

- `ADMINISTRADOR` puede consultar cualquier inscripción y listar inscritos de cualquier evento.
- `ORGANIZADOR` puede consultar inscripciones y listados únicamente cuando el evento le pertenece.
- `ESTUDIANTE`, `PARTICIPANTE_EXTERNO` y `PARTICIPANTE` solo pueden consultar y cancelar inscripciones propias.
- La creación de una inscripción conserva al usuario autenticado como propietario; el cliente no selecciona el usuario.

Las inscripciones y eventos existentes pero fuera del ámbito se responden como recurso no encontrado (`404`). Los roles no habilitados para una operación continúan siendo rechazados con `403` por Spring Security.

La cancelación administrativa de inscripciones ajenas queda pendiente de decisión funcional. En esta etapa `ADMINISTRADOR` conserva el comportamiento previo y solo puede cancelar una inscripción propia.

Esta corrección cubre específicamente el ámbito de Inscripciones y no implica que los demás módulos estén libres de IDOR.

## Autorización y propiedad de pagos

La autorización del módulo de pagos se resuelve en el backend mediante la cadena de propiedad del pago:

- `ADMINISTRADOR` conserva alcance global para consulta, validación y rechazo.
- `ORGANIZADOR` solo consulta, lista pendientes, valida y rechaza pagos asociados a inscripciones de eventos propios.
- `ESTUDIANTE` y `PARTICIPANTE_EXTERNO` solo consultan pagos propios y cargan comprobantes sobre pagos propios.

Los pagos e inscripciones existentes pero fuera del alcance se responden como recurso no encontrado (`404`). Los roles no admitidos por un endpoint se rechazan con `403`. Los UUID identifican el recurso, pero no otorgan acceso.

La carga de comprobantes comprueba autenticación, rol, ownership y estado antes de invocar el almacenamiento. Un pago `PENDIENTE` o `RECHAZADO` admite carga o reemplazo; un pago `VALIDADO` no admite reemplazo. La recarga de un comprobante rechazado conserva el estado `RECHAZADO`, porque no existe una transición funcional explícita que autorice cambiarlo automáticamente.

Los reportes de pagos y las métricas financieras de los dashboards se calculan globalmente para `ADMINISTRADOR` y se acotan a eventos propios para `ORGANIZADOR`. `PERSONAL_CONTROL` conserva temporalmente su comportamiento previo en el dashboard operativo hasta que se defina formalmente su alcance financiero.

Las pruebas negativas cubren acceso cruzado entre usuarios y organizadores, ausencia de persistencia ante rechazos y la garantía de que el almacenamiento no se invoca antes de autorizar. Queda como mejora futura servir comprobantes mediante un endpoint autenticado y resolver de forma transaccional el ciclo de vida de archivos reemplazados. La exportación PDF continúa siendo simulada; cuando sea implementada deberá aplicar el mismo scope de pagos.

## Autorización y propiedad de credenciales y QR

La autorización de credenciales y de su QR permanente se aplica en el backend antes de devolver datos o generar archivos:

- `ADMINISTRADOR` conserva alcance global para las operaciones que ya tiene habilitadas.
- `ESTUDIANTE` y `PARTICIPANTE_EXTERNO` solo pueden generar, consultar, descargar y obtener el QR de credenciales propias, de acuerdo con los roles admitidos por cada endpoint.
- `ORGANIZADOR` solo puede consultar credenciales, obtener imágenes QR y usar la validación simple cuando el recurso pertenece a uno de sus eventos. El endpoint de descarga conserva sus roles previos y no habilita al organizador.

Una credencial, inscripción o QR fuera del ámbito se responde como recurso no encontrado (`404`); los roles no admitidos continúan recibiendo `403`. La autorización se completa antes de generar PNG, PDF o cualquier byte de respuesta. El contenido no UUID de la validación simple también se responde de forma controlada como `404`.

Esta corrección conserva el contenido UUID y el carácter permanente del QR de credencial. No incorpora firma, expiración, sesión, GPS, nonce ni cambios de modelo. El conteo de QR utilizados del dashboard operativo se acota a eventos propios para `ORGANIZADOR`; `ADMINISTRADOR` y `PERSONAL_CONTROL` conservan el comportamiento global anterior.

Queda pendiente separar el QR permanente de identificación del futuro QR temporal de asistencia. El modelo mantiene referencias redundantes de usuario y evento entre credencial e inscripción; esta tarea no las sincroniza ni modifica.

## Autorización de Control de acceso y Asistencia — Fase 1.4-A

El backend aplica ownership por evento antes de exponer datos o realizar escrituras en los endpoints de validación QR operativa, autorización, denegación, historial, búsqueda por código, búsqueda por documento y listado de asistencias:

- `ADMINISTRADOR` conserva alcance global.
- `ORGANIZADOR` solo opera sobre credenciales, inscripciones, controles y asistencias vinculados mediante `Credencial → Inscripción → Evento → Organizador` a sus propios eventos.
- Los recursos existentes fuera del alcance se responden como no encontrados (`404`).
- `PERSONAL_CONTROL` conserva temporalmente el alcance previo porque todavía no existe una asignación formal a eventos; esta definición sigue pendiente.

La búsqueda por código mantiene su contrato actual y se acota con el organizador autenticado. La búsqueda por documento valida primero el scope del `eventoId` y solo después consulta la inscripción. La autorización de ingreso comprueba ownership antes de guardar `ControlAcceso`, crear `Asistencia` o cambiar el QR a `UTILIZADO`.

Con el modelo actual se aplican las reglas inequívocas siguientes: inscripción `CONFIRMADA`; pago `VALIDADO` para eventos de tipo `PAGO`; ausencia de pago permitida para eventos gratuitos; credencial con estado `ACTIVA`; QR activo y en estado `GENERADO`; y rechazo de eventos `BORRADOR`, `CANCELADO` o `FINALIZADO`. No se introducen estados nuevos.

El dashboard operativo cuenta asistencias globalmente para `ADMINISTRADOR` y únicamente sobre eventos propios para `ORGANIZADOR`. `PERSONAL_CONTROL` conserva el conteo global hasta que se resuelva su alcance.

Permanecen pendientes el control atómico de autorizaciones concurrentes, la asignación de `PERSONAL_CONTROL`, `SESION_EVENTO`, QR temporal, firma, expiración, nonce, GPS y radio. Esta fase no modifica entidades, migraciones, roles ni dependencias.

## Alineación del backend con el modelo oficial

La reestructuración del backend posterior a las fases de ownership conserva `UsuarioAutenticadoService`, JWT, BCrypt y las consultas acotadas de Eventos, Inscripciones, Pagos y Asistencia. Los tres roles funcionales vigentes son `ADMINISTRADOR`, `ORGANIZADOR` y `USUARIO`; los tipos interno/externo permanecen como atributo de usuario y no como autoridades.

Los módulos de credencial/QR permanente y Control de acceso fueron retirados porque no pertenecen al modelo final. Por tanto, las secciones históricas anteriores describen correcciones realizadas antes de su retirada, pero sus endpoints ya no forman parte de la API vigente. El futuro QR temporal de asistencia no está implementado en esta fase.

`SesionEvento` y la relación única `Inscripcion + SesionEvento` en Asistencia preparan el control de duplicados por sesión. La implementación completa de QR temporal, GPS, validación de ventana horaria y persistencia física queda pendiente de migraciones y fases posteriores. No se modificó la configuración JWT ni se introdujeron secretos.

## Usuarios, roles y promoción a organizador

El registro público crea siempre un usuario con rol `USUARIO`; el contrato no acepta roles y no permite alta directa como `ORGANIZADOR` o `ADMINISTRADOR`. Se validan confirmación de contraseña, correo, CI y RU único cuando el tipo es UAJMS. La contraseña continúa almacenándose mediante BCrypt. La verificación institucional externa del RU no está implementada porque no existe una integración institucional disponible; solo se aplica validación estructural y unicidad local.

La autenticación reconoce exclusivamente `ADMINISTRADOR`, `ORGANIZADOR` y `USUARIO`, incluso si una base anterior conserva registros técnicos con otros nombres. El perfil y el cambio de contraseña resuelven siempre al usuario autenticado mediante `UsuarioAutenticadoService` y no aceptan un identificador de tercero ni cambios de rol.

La solicitud de organizador se almacena dentro de `Usuario` con estado, fechas, motivo de rechazo y administrador resolutor. Solo `USUARIO` puede crearla y solo `ADMINISTRADOR` puede listar, aprobar o rechazar. La aprobación reemplaza la asignación funcional por `ORGANIZADOR`; el rechazo conserva `USUARIO`. No se creó una entidad ni endpoint que permita autoasignarse privilegios.

## Ciclo seguro de gestión de eventos

El backend obtiene siempre al organizador desde el usuario autenticado; los DTO de creación y actualización no aceptan organizador, estado ni campos de auditoría. `ORGANIZADOR` crea en `BORRADOR`, edita únicamente recursos propios en `BORRADOR` o `RECHAZADO` y puede enviarlos a `EN_REVISION`. Los recursos ajenos se ocultan mediante consultas acotadas y respuesta `404`.

Solo `ADMINISTRADOR` publica o rechaza eventos en revisión y finaliza eventos publicados cuya fecha y hora de fin ya transcurrieron. El rechazo y la cancelación exigen motivo. Ninguna transición permite reactivar eventos `CANCELADO` o `FINALIZADO`, y los endpoints públicos y filtros consultan exclusivamente eventos `PUBLICADO`.

Las nuevas inscripciones se rechazan para cualquier estado distinto de `PUBLICADO`. Los eventos sin inscripción y los cupos ilimitados se tratan explícitamente, sin crear límites artificiales. La zona funcional usada para comprobar la finalización es `America/La_Paz`; la persistencia mantiene `LocalDate` y `LocalTime` sin conversiones implícitas de zona.

## Sesiones, QR temporal y asistencia

`SesionEvento` aplica ownership mediante `SesionEvento -> Evento -> organizador`. `ADMINISTRADOR` tiene alcance global; `ORGANIZADOR` gestiona únicamente sesiones de eventos propios y `USUARIO` solo consulta sesiones de eventos donde posee una inscripción confirmada. Las sesiones iniciadas o históricas son inmutables desde la API y no pueden desactivarse retroactivamente.

El QR de asistencia es infraestructura técnica separada del modelo conceptual. Se genera con 32 bytes de `SecureRandom`, se entrega una sola vez y solo se persiste su hash SHA-256. Dura dos minutos, pertenece a una sesión y puede ser compartido por sus participantes. La rotación bloquea la sesión y revoca emisiones activas anteriores; el QR no se consume globalmente.

El registro obtiene al usuario desde JWT y deriva la inscripción a partir del usuario autenticado y del evento de la sesión. Exige inscripción `CONFIRMADA`, sesión activa y requerida, ventana inclusiva y QR vigente. Para sesiones presenciales, el backend calcula Haversine y acepta únicamente `distancia + precisión <= radio`, con precisión máxima de 30 metros. Las coordenadas del participante no se persisten.

La entidad declara unicidad `inscripcion_id + sesion_evento_id`, y el servicio usa `saveAndFlush` para convertir carreras concurrentes en un rechazo de duplicado. La migración Flyway pendiente deberá crear esta restricción considerando el soft delete y una restricción/índice parcial que garantice una sola emisión QR activa por sesión. No se migraron asistencias históricas ni se inventaron horarios o coordenadas.
