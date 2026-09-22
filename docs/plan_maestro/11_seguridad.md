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

Queda pendiente separar el QR permanente de identificación del futuro QR temporal de asistencia. También permanecen pendientes de una tarea específica todos los endpoints de Control de acceso, incluidos validación operativa, autorización, denegación, historial y búsquedas por código o documento. El modelo mantiene referencias redundantes de usuario y evento entre credencial e inscripción; esta tarea no las sincroniza ni modifica.
