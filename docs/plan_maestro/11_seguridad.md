# 11. Seguridad y protección de datos

## Identidad

- Contraseñas codificadas con BCrypt/PasswordEncoder; JWT tiene expiración configurable.
- Roles exactos: `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO`; registro asigna menor privilegio USUARIO.
- Verificación de correo y recuperación/reset usan tokens con expiración y uso limitado; el token de recuperación no se devuelve en respuesta de recuperación no enumerativa.
- `SesionUsuarioServicio` registra sesión, revoca sesiones activas según política de inicio y logout; revocación de sesión está implementada. Esto contradice textos antiguos que afirmaban que no existía.

## Autorización y ownership

Spring Security y servicios backend validan rol y ámbito de recurso. Administrador tiene alcance global únicamente donde controller/service lo concede. Organizador queda limitado por ownership de evento; usuario, a sus recursos. Angular guards y Flutter UI son UX, no barreras de seguridad.

- 401: falta de autenticación/token inválido/expirado.
- 403: rol insuficiente.
- 404 puede ocultar un recurso existente fuera del ownership para no filtrar su existencia.
- Las pruebas/locales no acreditan exhaustividad ni producción hasta ejecutar inventario endpoint×rol.

## Archivos y proveedores

- Comprobantes y QR pago son objetos privados en Supabase Storage S3-compatible. Solo backend conoce keys/credenciales y valida permiso antes de acceder. No se generan URL públicas para comprobantes; Spring sirve el archivo bajo endpoint Vidia autenticado/autorizado.
- QR de pago es imagen que facilita pago externo; QR asistencia es token temporal por sesión; QR de verificación certificado conduce a endpoint público independiente.
- Validación archivo backend verifica contenido/tipo/tamaño según operación. No enviar token, key o path interno en logs/evidencia.
- Supabase Storage no autentica usuarios ni sustituye PostgreSQL/Spring Security.

## Comunicación y secretos

HTTPS debe usarse en despliegue. Valores de JWT, PostgreSQL, SMTP/Brevo y Storage se proporcionan por variables de entorno locales/hosting; nunca documentar sus valores. No incluir contraseñas de demo, tokens, datos personales reales ni claves de proveedores en capturas, README o informes.

## Limitaciones/evidencia pendiente

La revocación concurrente del token de reset se registró como deuda técnica en auditoría previa; requiere tratamiento aparte. La configuración productiva de TLS, CORS, JWT, DB, Brevo y Storage no se verificó durante E3.4. RNF-02 exige pruebas completas 401/403 por endpoint/rol.
