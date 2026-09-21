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
