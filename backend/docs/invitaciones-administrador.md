# Invitación y activación de administradores

La administración es exclusivamente Web. El registro público continúa creando USUARIO;
la aprobación de organizadores continúa agregando ORGANIZADOR sin quitar USUARIO.
La activación administrativa crea un usuario nuevo con **solo ADMINISTRADOR**, nunca
convierte una cuenta existente. No se modifica el seed ni admin@demo.local.

## Flujo

1. Un administrador autenticado abre Administración → Administradores → Invitar administrador.
2. Proporciona nombres, apellidos, correo, CI y celular con las validaciones existentes.
3. Se valida unicidad, se guarda una invitación PENDIENTE y se solicita su envío mediante
   CorreoServicio → ProveedorCorreo (Brevo API/SMTP según la configuración actual).
4. El destinatario abre `/auth/activar-administrador?token=...`. Angular elimina la query
   del historial de navegación y mantiene el token únicamente en memoria. Recargar
   requiere abrir nuevamente el enlace del correo.
5. La consulta pública devuelve únicamente el estado. El destinatario confirma sus datos,
   incluyendo el CI invitado, y establece/confirmar su contraseña segura. No puede cambiar
   el correo invitado ni proporcionar roles.
6. El backend consume la invitación bajo bloqueo, revalida correo/CI, crea usuario activo
   con correo verificado y asigna exclusivamente ADMINISTRADOR. El estado pasa a ACEPTADA.
7. El destinatario utiliza el login y JWT existentes. Puede invitar a otro administrador.

## Contrato

Base: `/api/v1`. Todos los endpoints `/administradores` requieren ADMINISTRADOR.

| Método | Ruta | Body | Éxito |
|---|---|---|---|
| GET | /administradores | — | 200: cuentas, activo y fechaCreacion |
| GET | /administradores/invitaciones | — | 200: invitaciones y estados efectivos |
| POST | /administradores/invitaciones | nombres, apellidos, correo, ci, celular | 201 |
| POST | /administradores/invitaciones/{id}/reenviar | — | 200 |
| PATCH | /administradores/invitaciones/{id}/revocar | — | 200 |
| PATCH | /administradores/{id}/desactivar | — | 204 |
| POST | /auth/invitaciones-administrador/consultar | token | 200: solo estado |
| POST | /auth/invitaciones-administrador/aceptar | token, nombres, apellidos, ci, celular, contrasena, confirmacionContrasena | 204 |

La consulta es POST para no incluir secretos en rutas de API, historial ni logs de acceso
de la API. No consume el token. Estados: PENDIENTE, ACEPTADA, EXPIRADA, REVOCADA.
La expiración se calcula también al consultar/listar; se materializa antes de nuevas
invitaciones/reenvíos para liberar índices de pendientes. Tokens inválidos producen 400;
aceptar un enlace expirado/revocado/utilizado también produce 400 con error genérico.
Validaciones funcionales: 400; recurso inexistente o usuario no administrativo: 404;
sin autenticación: 401; rol incorrecto: 403; conflicto concurrente de unicidad: 409;
proveedor de correo no disponible: 503.

Los administradores tienen alcance administrativo global sobre invitaciones. Cambiar un
UUID no autoriza desactivar participantes ni acceder a entidades de otro tipo. No existe
endpoint de eliminación de usuarios, conversión de roles o reactivación en esta fase.

## Configuración

| Variable | Default / comportamiento |
|---|---|
| ADMIN_INVITATION_EXPIRATION_HOURS | 24 horas, debe ser positivo |
| ADMIN_INVITATION_RESEND_MINUTES | 5 minutos, debe ser positivo |
| FRONTEND_ADMIN_ACTIVATION_URL | Opcional; si está vacía, usa el origen de FRONTEND_VERIFY_EMAIL_URL y `/auth/activar-administrador` |

No cambia las URLs de verificación ni recuperación existentes. El dominio Web debe estar
configurado correctamente y permitir navegación directa a la nueva ruta mediante su
fallback SPA actual. No se modifica Render ni App Links.

## Seguridad y concurrencia

- 32 bytes de SecureRandom, Base64 URL-safe, SHA-256 persistido; token crudo solo en correo.
- Token/contraseñas son write-only y excluidos de toString en DTOs; no hay logs de secretos.
- Consulta pública sin datos personales; token no se guarda en localStorage/sessionStorage.
- Respuestas públicas no-store; política no-referrer en Angular y consulta pública.
- Reenvío rota el hash y extiende la expiración; el enlace anterior deja de ser válido.
- Revocación solo de PENDIENTE. Una invitación aceptada no se reabre.
- Espera de reenvío persistente de 5 minutos y límite de 10 invitaciones recientes por invitante.
- Bloqueo de la fila del rol ADMINISTRADOR serializa creación, aceptación y desactivación;
  bloqueo adicional de invitación impide aceptación/revocación concurrentes incompatibles.
- Índices únicos parciales para correo normalizado y CI mientras estén PENDIENTES.
- Usuario/correo/CI se revalidan al aceptar. Las restricciones de usuario existentes se mantienen.
- Desactivar revoca sesiones e impide login/acceso con JWT anterior. No modifica el JWT.
- No puede desactivarse el último administrador activo; admin@demo.local está protegido.
- V4 es aditiva: columna activo con default true y tabla nueva. V1/V2/V3 permanecen intactas.

El envío de correo no participa en la transacción de PostgreSQL: ante fallo del proveedor
la invitación se revierte. Un fallo de commit posterior a un envío aceptado podría entregar
un enlace inválido; requeriría reintentar la invitación. No se agrega un outbox en esta fase.
No habilitar logs de bodies HTTP ni registrar queries del enlace Web en servicios externos.

## Validación local reproducible

Suite ordinaria: `mvn test`, `mvn package`, `npm test -- --watch=false`, `npm run build`.

La integración usa PostgreSQL desechable, nunca una base existente:

1. Inicializar un cluster temporal de PostgreSQL 16 fuera de archivos versionados, usuario
   `vidia_test`, escuchando **solo 127.0.0.1:55439**, sin reutilizar el servicio habitual.
2. En PowerShell, definir `VIDIA_TEST_PG_URL=jdbc:postgresql://127.0.0.1:55439/postgres`.
3. Ejecutar `mvn test` o `mvn package` desde backend. El test exige exactamente esa URL;
   si no se define, sus casos se omiten. Crea bases con nombres aleatorios en ese cluster.
4. Detener el cluster temporal al finalizar. Sus datos son exclusivamente fixtures ficticios.

La integración verifica HTTP real, BCrypt/JWT/login, exclusividad del rol, permisos,
desactivación/sesiones, carreras de aceptación y último administrador, seed original,
rollback por fallo de correo, rotación/revocación, UUIDs ajenos, OpenAPI y Swagger UI.
Prueba migraciones desde limpio, V1, V2, V3 y una base histórica con usuario dual.
El proveedor de correo está simulado: no envía correos ni crea usuarios en producción.

## Validación manual pendiente antes de dar por probado producción

- Desplegar mediante el proceso habitual y verificar SHA/migración V4.
- Con buzones ficticios controlados, confirmar entrega real mediante Brevo y abrir el enlace.
- Completar activación Web, login, panel y una segunda invitación administrativa.
- Confirmar errores por correo/CI duplicados y permisos de USUARIO/ORGANIZADOR.
- Comprobar redacción de queries de activación en logs externos y entrega por HTTPS.
- No regularizar ni convertir cuentas históricas automáticamente. Cualquier cuenta con
  ADMINISTRADOR mezclado con otros roles requeriría una auditoría y decisión separadas.
