# 13. Despliegue y configuración

## Arquitectura de despliegue prevista

Frontend Web Angular y API Spring Boot se despliegan como servicios web; PostgreSQL es persistencia administrada; el backend usa Brevo SMTP y Supabase Storage privado. Flutter se distribuye como artefacto móvil Android una vez compilado/verificado. Los usuarios móviles apuntan al backend HTTPS, no a Supabase.

## Variables de entorno

Los nombres requeridos se mantienen en `.env.example`/configuración: conexión PostgreSQL, `JWT_SECRET`/expiración, `CORS_ALLOWED_ORIGINS`, URLs frontend de correo, variables `SPRING_MAIL_*`/`MAIL_FROM*`, `SUPABASE_STORAGE_*`, provider y ubicación del backend/API móvil. Valores y credenciales reales no se incluyen aquí ni en Git.

## Estado de verificación

- Render/hosting y URLs mencionadas en documentos históricos: **configuración/propuesta, no verificada en esta fase**.
- Conexión productiva a PostgreSQL y migraciones: **no verificada**; solo migración V1 localizada, Hibernate `ddl-auto:update` sigue como default base.
- HTTPS/CORS, perfil Spring productivo/JWT: **no verificados en ejecución**.
- Correo Brevo: integrado y prueba real local reportada en fase anterior; envío desde deployment actual **no verificado**.
- Supabase: integración backend presente; smoke test E3.2 omitido y servicio productivo **no verificado**.
- Health check, uptime RNF-05 y APK/AAB distribuido: **pendientes de evidencia**.

Antes de llamar al sistema “desplegado/producción listo”, registrar commit, URLs activas, smoke test, versión, DB/migración, correo/storage, CORS/TLS y período de disponibilidad, sin exponer secretos.
