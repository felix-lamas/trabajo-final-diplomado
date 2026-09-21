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
