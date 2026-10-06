# 06. Arquitectura actual y objetivo

## Patrón y componentes

Monolito modular en capas, sin microservicios en esta versión:

- Spring Boot 3.3 / Java 21: API REST, reglas de negocio, autenticación/autorización, validación e integración externa.
- Angular 21: aplicación Web pública, de usuario, administrador y organizador.
- Flutter 3.44.8: aplicación móvil de participante; flujos RF-07, RF-08, RF-09/10 aún incompletos.
- PostgreSQL: persistencia relacional.
- Spring Security/JWT: identidad, roles, ownership.
- Brevo SMTP: correo transaccional.
- Supabase Storage S3-compatible: objetos privados; el backend es cliente/intermediario.

## Comunicaciones

- Web/Flutter → backend: REST sobre HTTP local y HTTPS esperado en producción; JSON para recursos, multipart para comprobantes/QR imagen, binario/Blob para descargas.
- Backend → PostgreSQL: JPA/JDBC.
- Backend → Storage: S3-compatible HTTPS; credenciales solo backend.
- Backend → Brevo: SMTP STARTTLS por variables de entorno.

Supabase no reemplaza PostgreSQL ni Spring Security y no brinda autenticación Vidia. QR de pago, comprobante, QR asistencia y QR/código de certificado son flujos distintos. `Evento.imagenPortada` sigue URL externa HTTP(S), no carga objeto.

El deployment y la conexión productiva de cada servicio deben marcarse no verificados hasta ejecución con evidencia.
