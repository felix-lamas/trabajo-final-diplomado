# Manual de despliegue y operación

[README](../../README.md) · [Local](instalacion-local.md) · [Uso](uso-del-sistema.md)

Esta guía distingue configuración versionada, servicios públicos y operaciones que debe ejecutar un responsable autorizado. **No se desplegó ni se modificó producción durante esta revisión.** No se accedió al panel privado de Render ni a variables productivas.

## 1. Servicios y responsabilidades

| Servicio | Dirección / evidencia |
|---|---|
| Web Angular | https://trabajo-final-diplomado-web.onrender.com/ |
| Backend | https://trabajo-final-diplomado.onrender.com/ |
| Swagger | https://trabajo-final-diplomado.onrender.com/swagger-ui/index.html |
| OpenAPI | https://trabajo-final-diplomado.onrender.com/v3/api-docs |
| Salud | https://trabajo-final-diplomado.onrender.com/api/v1/salud |
| Android v1.0.0 | https://github.com/felix-lamas/trabajo-final-diplomado/releases/tag/v1.0.0 |

Web/backend se presentan en Render; PostgreSQL es un servicio independiente de persistencia. Supabase Storage y Brevo son integraciones externas del backend. No hay microservicios, conexiones directas cliente/base o secretos de almacenamiento/correo en los clientes.

Se obtuvo respuesta HTTPS con validación de certificado del cliente HTTP en las URL consultadas el 13/10/2026. Eso no prueba TLS interno de PostgreSQL, cabeceras completas, disponibilidad continua ni todos los flujos. Versión PostgreSQL, SHA desplegado y proveedor efectivo de cada integración siguen sin verificarse en el panel.

## 2. Web: configuración versionada

`render.yaml` declara **solo un servicio web estático**:

| Campo | Valor |
|---|---|
| Nombre | `trabajo-final-diplomado-web` |
| Root directory | `frontend` |
| Runtime | `static` |
| Build | `npm ci && npm run build` |
| Publish directory | `dist/frontend/browser` |
| Rewrite | `/* → /index.html` |

Un responsable puede vincular el repositorio y aplicar/revisar ese descriptor en Render. Debe comprobar rama, commit, runtime Node compatible y resultado del build antes de publicar. `environment.ts` contiene la URL API HTTPS pública; `environment.dev.ts` es local. No hay sustitución automática de esa URL por variables de entorno del servicio estático. Si se cambia la dirección del backend, se debe coordinar la configuración cliente y una nueva compilación autorizada.

`angular.json` habilita `security.autoCsp` en producción. Compruebe el artefacto y las cabeceras reales del hosting; no se certifica CSP solo por leer ese campo. No publique claves en archivos JavaScript.

## 3. Backend: Docker y servicio externo

`backend/Dockerfile` utiliza Maven **3.9.9 / Temurin 21** para descargar dependencias y ejecutar `mvn -B clean package -DskipTests`; copia el JAR a una imagen **Temurin 21 JRE** y ejecuta `/app/app.jar`. `EXPOSE 8080` es metadato: el puerto efectivo depende de Spring y las variables.

Para construcción autorizada, desde la raíz:

```powershell
docker build -f backend/Dockerfile -t vidia-backend:revision backend
```

El contexto es `backend`, porque el Dockerfile copia `pom.xml` y `src` relativos a ese contexto. Se comprobó el archivo, no este build Docker: el daemon no estuvo disponible en la validación anterior. El build Docker omite tests y no reemplaza `mvn clean verify` en un entorno aislado.

El servicio backend **no está declarado en render.yaml**. Para reproducirlo, un responsable debe configurar el servicio Docker, root directory/contexto `backend`, Dockerfile, rama/commit, variables y health check `/api/v1/salud`, y comprobar estos valores contra su panel. No se afirma que exista automatización de todos esos pasos.

Use `SPRING_PROFILES_ACTIVE=prod`, sin `demo`, una cuenta PostgreSQL de privilegio mínimo y `JPA_DDL_AUTO=validate` después de validar migraciones en staging. Si Render proporciona `PORT`, no lo contradiga con un `SERVER_PORT` heredado: este último tiene precedencia. El Dockerfile no contiene secretos ni recibe la `.env` de la raíz; configure variables privadamente en el servicio.

## 4. Variables reales

Los nombres provienen de `application.yml`, `application-prod.yml`, `.env.example` y clases de integración. Los valores sensibles nunca se incluyen aquí. «Necesaria» depende del perfil y funcionalidad.

| Grupo | Variables | Condición / configuración |
|---|---|---|
| Servidor | `APP_NAME`, `SPRING_PROFILES_ACTIVE`, `SERVER_PORT`, `PORT` | Defaults nombre/`dev`/8080; establecer `prod` explícitamente |
| PostgreSQL | `DB_URL` o `DB_HOST`, `DB_PORT`, `DB_NAME`; `DB_USER`, `DB_PASS` | Usuario/password necesarios. DB_URL es JDBC y sustituye la URL compuesta |
| JPA | `JPA_DDL_AUTO`, `JPA_SHOW_SQL`, `JPA_FORMAT_SQL` | Default update; operación controlada: validate tras ensayar migraciones; no habilitar logs sensibles |
| JWT | `JWT_SECRET`, `JWT_EXPIRATION` | Secreto Base64 propio obligatorio; expiración default 86400000 ms, no verificada en producción |
| Límites de acceso | `AUTH_RATE_IDENTIFIER_LIMIT`, `AUTH_RATE_OPERATION_LIMIT`, `AUTH_RATE_WINDOW_SECONDS`, `AUTH_RATE_MAX_IDENTIFIERS` | Defaults 10/120/60/10000; memoria por instancia, no limitación distribuida |
| Invitaciones | `ADMIN_INVITATION_EXPIRATION_HOURS`, `ADMIN_INVITATION_RESEND_MINUTES` | Defaults 24/5; URLs de activación coordinadas con la web |
| CORS | `CORS_ALLOWED_ORIGINS` | Lista por comas; origen web público exacto, sin rutas; no usar comodines para resolver errores |
| Enlaces de correo | `FRONTEND_RESET_PASSWORD_URL`, `FRONTEND_VERIFY_EMAIL_URL`, `FRONTEND_ADMIN_ACTIVATION_URL` | Definir destinos públicos correctos; los defaults locales no sirven a usuarios de producción |
| Certificados | `CERTIFICATE_VERIFICATION_BASE_URL` | URL pública `/verificar-certificado`; comprobar PDF y enlaces existentes |
| Correo HTTP | `MAIL_PROVIDER`, `MAIL_FROM`, `MAIL_FROM_NAME`, `BREVO_API_KEY` | `brevo-api`, remitente autorizado y clave privada; proveedor acepta JSON por HTTPS |
| SMTP alternativo | `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | Solo cuando MAIL_PROVIDER=smtp; verificar conectividad del hosting antes de elegirlo |
| SMTP opciones | `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH`, `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE`, `SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTIONTIMEOUT`, `SPRING_MAIL_PROPERTIES_MAIL_SMTP_TIMEOUT`, `SPRING_MAIL_PROPERTIES_MAIL_SMTP_WRITETIMEOUT` | Defaults true/true/5000/5000/5000; no se validó transporte SMTP productivo |
| Almacenamiento | `APP_STORAGE_PROVIDER`, `UPLOADS_BASE_DIR` | `prod` declara Supabase; entorno/argumentos pueden prevalecer; no usar disco efímero para ocultar falta de credenciales |
| Supabase S3 | `SUPABASE_STORAGE_ENDPOINT`, `SUPABASE_STORAGE_REGION`, `SUPABASE_STORAGE_BUCKET`, `SUPABASE_STORAGE_ACCESS_KEY`, `SUPABASE_STORAGE_SECRET_KEY` | Endpoint HTTPS, bucket privado y credenciales S3 del backend; no son credenciales de autenticación de usuarios |
| Timeouts S3 | `SUPABASE_STORAGE_CONNECTION_TIMEOUT_MS`, `SUPABASE_STORAGE_API_TIMEOUT_MS` | Defaults 5000/20000 |
| Multipart | `MAX_FILE_SIZE`, `MAX_REQUEST_SIZE` | Defaults 10MB; el validador de comprobantes limita a 5MB, por separado |
| Solo demostración | `DEMO_PASSWORD` | Necesaria para seed con perfil demo; nunca habilitarlo en producción |
| Solo desarrollo | `PGADMIN_EMAIL`, `PGADMIN_PASSWORD` | Descriptor Compose; no son variables necesarias del backend Render |

Brevo implementa POST HTTPS `/v3/smtp/email`, encabezado `api-key` y JSON. Que la ruta contenga «smtp» no significa usar transporte SMTP. El backend consume aceptación/error y no procesa el cuerpo de respuesta. La clave ausente provoca fallo al enviar; no se certifica entrega al destinatario.

Supabase utiliza AWS SDK S3 con credenciales estáticas privadas, path style y operaciones PUT/GET/HEAD/DELETE. El backend controla autorización y entrega bytes; no se conectan los clientes directamente al bucket. Los certificados se regeneran al descargar; no atribuya su persistencia a S3 sin evidencia.

## 5. PostgreSQL y migraciones

Configure una URL JDBC proporcionada por el servicio PostgreSQL y credenciales por entorno. La etiqueta local `postgres:16-alpine` no acredita versión de la instancia alojada. Compruebe TLS conforme al proveedor y al driver, y verifique la conexión efectiva; no suponga cifrado por usar Render.

Flyway se ejecuta al arrancar. V1–V5 están en `backend/src/main/resources/db/migration`. Antes de actualizar:

1. Respaldar y conocer esquema/historial de migraciones.
2. Ensayar la actualización en una copia aislada y ejecutar pruebas.
3. No editar migraciones ya aplicadas; agregar versiones posteriores para cambios futuros.
4. Revisar efectos de `baseline-on-migrate=true` y baseline 0 antes de incorporar una base existente.
5. Revisar warnings/NOTICE de reconciliación V3: algunas restricciones históricas pueden omitirse si existen duplicados.

No ejecutar `repair`, reset ni scripts contextuales automáticamente. El rollback de aplicación no garantiza reversión del esquema.

## 6. Android y distribución

El código declara app `1.0.0+1`, paquete `bo.edu.uajms.vidia`, minSdk 24. La release pública v1.0.0 y el enlace APK respondieron el 13/10; una consulta HEAD no verifica hash, firma ni instalación del archivo publicado.

Construcción autorizada desde `flutter/VidiaApp`:

```powershell
flutter pub get
flutter test
flutter build apk --release --dart-define=API_BASE_URL=https://trabajo-final-diplomado.onrender.com/api/v1
```

Salida esperada después de éxito: `build/app/outputs/flutter-apk/app-release.apk`. Se requiere `android/key.properties` y el keystore original. El Gradle valida su existencia incluso cuando las credenciales vienen de `VIDIA_RELEASE_STORE_FILE`, `VIDIA_RELEASE_STORE_PASSWORD`, `VIDIA_RELEASE_KEY_ALIAS` y `VIDIA_RELEASE_KEY_PASSWORD`. No sustituya la firma ni genere otra para reproducir una publicación existente. Mantenga estos archivos fuera de Git.

La compilación release no se reprodujo: las credenciales efectivas anteriores estaban incompletas. Para publicar una futura versión, el responsable debe comprobar firma, versión, instalación y URL API antes de adjuntar el artefacto a GitHub Releases. Esta guía no realizó una publicación.

App Links: `frontend/public/.well-known/assetlinks.json` existe y la URL pública respondió HTTP 200 con tipo `application/json` y el paquete esperado el 13/10. Esto no demuestra asociación Android: compare la huella pública con la firma de la APK instalada y compruebe el estado del dominio en el dispositivo autorizado. No publique ni use enlaces de correo con tokens para ilustrar este procedimiento.

## 7. Actualización y comprobación posterior

Procedimiento operativo propuesto, no ejecutado en producción:

1. Identificar commit, cambios, dependencias y migraciones; acordar ventana/responsable.
2. Ensayar en staging aislado, conservar resultados y respaldos recuperables.
3. Revisar secretos, URLs, CORS y proveedores; no habilitar demo.
4. Ejecutar el despliegue autorizado con los artefactos revisados.
5. Comprobar web, salud, Swagger, OpenAPI y rechazo del perfil sin autenticación.
6. Ejecutar flujos controlados con cuentas ficticias autorizadas; registrar versión, fecha, resultado y limitaciones.
7. Revisar logs sin tokens/datos personales; conservar plan de recuperación y criterios de rollback.

Consultas públicas de lectura:

```powershell
Invoke-RestMethod https://trabajo-final-diplomado.onrender.com/api/v1/salud
Invoke-WebRequest https://trabajo-final-diplomado.onrender.com/swagger-ui/index.html
```

Perfil anónimo puede provocar una excepción HTTP de PowerShell con 401: es el rechazo esperado, no una prueba exhaustiva. Swagger permite seleccionar servidor; asegúrese de no escribir en producción durante pruebas locales.

## 8. Copias, recuperación y archivos

No se verificaron respaldos automáticos de Render ni restauración institucional. El 12/10 se probó pg_dump/pg_restore en un clúster local temporal, conservando esquema y una categoría ficticia; no demuestra recuperación de todos los datos ni de archivos externos.

Procedimiento previsto con herramientas PostgreSQL compatibles y autenticación privada, usando valores del operador:

```text
pg_dump --host <HOST> --port <PUERTO> --username <USUARIO> --dbname <BASE> --format=custom --file <RESPALDO_PRIVADO>
pg_restore --host <HOST_AISLADO> --port <PUERTO> --username <USUARIO> --dbname <BASE_AISLADA_VACIA> --no-owner --no-acl --exit-on-error <RESPALDO_PRIVADO>
```

No use directamente una URL `jdbc:` con estas herramientas ni incluya contraseña en la línea de comandos. La cuenta de restauración debe ser apropiada para el destino; ensaye fuera de producción. Compare historial Flyway, conteos, restricciones, datos representativos y flujos. Respalde objetos de Supabase por separado con un procedimiento autorizado y pruebe su recuperación junto a sus referencias SQL. No se afirma que exista un job de respaldo configurado.

## 9. Seguridad y diagnóstico

- Revisar dependencias y sus licencias; ejecutar pruebas antes de liberar cambios. No actualizar versiones sin ensayo.
- Proteger y rotar secretos con responsables, registrar impacto en sesiones/servicios y evitar logs sensibles. La rotación no se ejecutó aquí.
- Revisar disponibilidad y logs con retención/alertas definidas; no se verificó monitoreo automático.
- El limitador de intentos está en memoria por instancia: reinicios y múltiples réplicas requieren evaluación adicional.
- JWT no reemplaza autorización/propiedad. Angular usa almacenamiento del navegador y requiere prevención de XSS; Flutter usa almacenamiento seguro. No migrar autenticación a cookies sin diseñar CSRF/CORS y ambos clientes.
- Una respuesta de salud no prueba PostgreSQL, Supabase o entrega de correo. Si falla una integración, revisar proveedor efectivo, permisos y configuración; no degradar producción a disco local ni exponer secretos para diagnosticar.
- Ante error Flyway conservar logs y datos, revisar checksums y versión; no reparar a ciegas. Ante indisponibilidad pública puntual, distinguir arranque, red y error de aplicación mediante registros del servicio.

Pendientes antes de una reproducción completa: panel Render, variables efectivas, TLS/versión PostgreSQL, firma y APK publicado, pruebas de integraciones externas, recuperación completa, monitoreo y procedimientos institucionales.
