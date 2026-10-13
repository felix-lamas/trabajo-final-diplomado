# Manual de instalación local

[README](../../README.md) · [Producción](despliegue-produccion.md) · [Uso](uso-del-sistema.md)

Prepare un entorno de **desarrollo con datos ficticios**, nunca la base productiva. Las ejecuciones del Anexo B del 12/10/2026 respaldan arranque, compilación y pruebas en copias aisladas; no constituyen una instalación completa probada en todos los equipos. Esta revisión documental no volvió a compilar ni a ejecutar las suites.

## 1. Herramientas

No se midieron requisitos mínimos de CPU/RAM/disco. Reserve espacio para Maven/npm, Flutter/Gradle y, cuando corresponda, Android Emulator. Un teléfono puede sustituir al emulador; no se inventa un mínimo de hardware.

| Herramienta | Referencia | Verificar |
|---|---|---|
| Git | Obtener y revisar fuentes | `git --version` |
| JDK | Java 21 declarado en POM | `java -version` |
| Maven | Docker: 3.9.9; local comprobado: 3.10.0 | `mvn -version` |
| Node | Angular resuelto admite `^20.19.0`, `^22.12.0` o `>=24.0.0`; local: 24.16.0 | `node --version` |
| npm | Proyecto declara 11.6.3 | `npm --version` |
| Angular CLI | Local al proyecto, sin instalación global obligatoria | `npx --no-install ng version` desde frontend |
| PostgreSQL | Referencia 16; Compose `16-alpine` | `psql --version` |
| Flutter/Dart | Referencia probada 3.44.8/3.12.2; Dart `>=3.12.0 <4.0.0` | `flutter --version`; `flutter doctor` |
| Android SDK | Requerido para Android; compile/target dependen de Flutter; minSdk del proyecto 24 | `flutter doctor`; `flutter devices` |
| Docker/Compose | Opcionales; daemon requerido para contenedores | `docker version`; `docker compose version` |

En Windows, instale JDK 21, Maven, Git, Node/npm y PostgreSQL desde las distribuciones oficiales. Instale Flutter y Android SDK. Añada los directorios `bin` al PATH, configure `JAVA_HOME` con el JDK y abra una nueva terminal. `Get-Command java,mvn,node,npm,flutter` identifica qué ejecutables se resuelven. No cambie SDK sin comprobar dependencias y pruebas. Resuelva los componentes/licencias que indique `flutter doctor`; no se instalaron herramientas ni se aceptaron licencias en esta revisión.

## 2. Obtener el código

```powershell
git clone https://github.com/felix-lamas/trabajo-final-diplomado.git
cd trabajo-final-diplomado
git rev-parse HEAD
git status --short
Get-Item backend/pom.xml,frontend/package.json,frontend/angular.json,flutter/VidiaApp/pubspec.yaml
```

Registre commit y cambios locales para reproducir resultados. El HEAD revisado fue `f6891ef3ffb5a89a31e323d37a7ae9d2f734725c`, con modificaciones locales: no equivale a un checkout limpio de ese commit. GitLab es el otro remoto del mismo proyecto.

## 3. PostgreSQL local

### Instalación nativa

Con `psql` y una cuenta administradora **local**, en una instancia nueva:

```sql
CREATE ROLE usuario_local WITH LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE;
```

Defina una contraseña privada mediante el comando interactivo de psql:

```text
\password usuario_local
```

```sql
CREATE DATABASE uajms_eventos OWNER usuario_local;
```

`uajms_eventos` es el nombre predeterminado del backend; `usuario_local` es el ejemplo de `.env.example`. Si existen, identifique su uso antes de actuar; no borre ni recree datos. La cuenta propietaria aplica DDL en su base, sin privilegios globales. Compruebe conexión y autenticación por contraseña según su instalación:

```powershell
psql --host localhost --port 5432 --username usuario_local --dbname uajms_eventos --command "SELECT current_database(), current_user;"
```

### Alternativa Docker

El descriptor `docker/docker-compose.yml` contiene PostgreSQL y pgAdmin; no inicia backend/clientes. Su `POSTGRES_USER`, tomado de `DB_USER`, es una cuenta de inicialización con privilegios administrativos: no la confunda con un usuario limitado.

Prepare un archivo privado `.env.compose.local` con credenciales de inicialización y los campos PGADMIN que exige el descriptor. Mantenga `.env` separado para la cuenta de aplicación. Desde la raíz:

```powershell
docker compose --env-file .env.compose.local -f docker/docker-compose.yml config --services
docker compose --env-file .env.compose.local -f docker/docker-compose.yml up -d database
docker compose --env-file .env.compose.local -f docker/docker-compose.yml ps
```

Use psql dentro del contenedor o un cliente local para crear la cuenta limitada y su base antes de configurar el backend. Puerto publicado: 5432; contenedor: `uajms_eventos_db`; volumen: `db_data`. Los valores PGADMIN deben existir aunque inicie solo `database`, porque Compose interpola el descriptor completo. No use `down -v` para resolver errores: elimina datos. Los SQL de `database/scripts/contexts/` no sustituyen Flyway.

Se comprobó anteriormente el descriptor, pero no el arranque Docker, por daemon no disponible. Esta alternativa requiere validación en su equipo.

## 4. Variables y perfiles

```powershell
Copy-Item .env.example .env
notepad .env
```

No sobrescriba un `.env` existente sin conservar sus valores privadamente. Reemplace marcadores; no reutilice secretos de producción.

| Variable | Configuración local |
|---|---|
| `DB_NAME`, `DB_HOST`, `DB_PORT` | Base local; defaults `uajms_eventos`, `localhost`, `5432` |
| `DB_USER`, `DB_PASS` | Cuenta limitada y contraseña; sin defaults |
| `DB_URL` | Opcional: URL JDBC que sustituye host/puerto/base; no sirve directamente para psql |
| `JWT_SECRET` | Base64 propio de al menos 32 bytes aleatorios |
| `SPRING_PROFILES_ACTIVE` | `dev`; o `dev,demo` solo en base ficticia aislada |
| `DEMO_PASSWORD` | Contraseña elegida localmente; requerida con perfil `demo` |
| `JPA_DDL_AUTO` | `validate` para contrastar esquema Flyway; default del código `update` |
| `APP_STORAGE_PROVIDER` | `local`; adapte el ejemplo que selecciona Supabase |
| `MAIL_PROVIDER` | `brevo-api` para integración actual; el ejemplo selecciona SMTP |
| `BREVO_API_KEY`, `MAIL_FROM` | Clave de pruebas y remitente autorizado si probará correo |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |
| `SERVER_PORT` | 8080 para estos comandos |

Mantenga las URLs `FRONTEND_RESET_PASSWORD_URL`, `FRONTEND_VERIFY_EMAIL_URL` y `CERTIFICATE_VERIFICATION_BASE_URL` dirigidas al frontend local. Configure `FRONTEND_ADMIN_ACTIVATION_URL` si prueba invitaciones. [Variables completas](despliegue-produccion.md).

Para generar un secreto en memoria sin mostrarlo:

```powershell
$jwtBytesLocal = New-Object byte[] 32
$generadorLocal = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$generadorLocal.GetBytes($jwtBytesLocal)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytesLocal)
$generadorLocal.Dispose()
```

La variable dura solo en esa terminal y sus procesos. Para conservarla, guárdela privadamente en `.env` sin imprimir ni versionar el valor. Spring importa `../.env` como **properties** cuando arranca desde `backend/`; Angular, Flutter y PowerShell no lo cargan automáticamente. Variables antiguas del proceso pueden prevalecer: revíselas antes de arrancar.

Revise `git status --short` antes de preparar cambios. La ruta de ejemplo `backend/uploads/readme-check.pdf` no resultó ignorada por Git en esta revisión: no añada archivos cargados localmente ni comprobantes al repositorio. La revisión documental no cambió `.gitignore`.

Sin clave Brevo se puede iniciar, pero fallan los flujos que envían correos. No equivale a registro/verificación/recuperación completos. El perfil `dev` no crea cuentas/roles: el seed se activa con `demo`, inicializa seguridad y datos ficticios. No active `demo` en producción. El bootstrap institucional necesita un procedimiento autorizado propio.

Si desea probar usuarios y procesos desde una base local vacía, seleccione explícitamente `dev,demo` y defina su contraseña local antes del primer arranque. Sin inicialización de roles/cuentas, un backend que responde salud todavía no ofrece un recorrido funcional completo.

## 5. Backend y migraciones

Desde la raíz:

```powershell
cd backend
mvn clean verify
mvn spring-boot:run
```

Flyway aplica automáticamente las migraciones V1–V5 de `src/main/resources/db/migration` al iniciar. Compruebe logs y `flyway_schema_history` con su cliente local. No cree entidades ejecutando SQL contextual ni aplique `repair/reset` por defecto. El comentario histórico sobre Hibernate creando tablas no sustituye las migraciones actuales.

Puerto: `SERVER_PORT`, después `PORT`, después 8080. Detenga con Ctrl+C. Alternativa JAR:

```powershell
mvn clean package
java -jar target/eventos-uajms-0.0.1-SNAPSHOT.jar
```

Para demostración local active `dev,demo` y defina su propia `DEMO_PASSWORD`. El seed utiliza `admin@demo.local`, `organizador1@demo.local`, `organizador2@demo.local` y `usuario@demo.local`. `organizador2` tiene solicitud pendiente, no rol organizador aprobado. No son credenciales publicadas para producción.

## 6. Angular

Desde la raíz, en otra terminal:

```powershell
cd frontend
npm ci
npx --no-install ng version
npm start
```

Abra `http://localhost:4200`. `angular.json` selecciona `environment.dev.ts` para desarrollo: API `http://localhost:8080/api/v1`. El build de producción usa `environment.ts`, dirigido a Render. npm no transforma `.env` en configuración Angular. Coordine cambios locales de puerto con API y CORS.

```powershell
npm test -- --watch=false
npm run build
```

Publicación: `dist/frontend/browser`. Revise advertencias de budgets sin ampliar límites únicamente para ocultarlas.

## 7. Flutter/Android

Desde la raíz, en otra terminal:

```powershell
cd flutter/VidiaApp
flutter doctor
flutter pub get
flutter devices
flutter test --reporter expanded
flutter analyze
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080/api/v1
```

El comando de ejecución presupone Android Emulator. En teléfono físico use la IP LAN real del PC, con `/api/v1`, y compruebe firewall/conectividad. `localhost` identifica al teléfono; `10.0.2.2` identifica al host del emulador. `ApiConfig` exige ruta `/api/v1`; release exige HTTPS. No elimine esta validación ni publique IP personales en capturas.

APK de desarrollo, sin firma de publicación:

```powershell
flutter build apk --debug --dart-define=API_BASE_URL=http://10.0.2.2:8080/api/v1
```

Salida esperada tras éxito: `build/app/outputs/flutter-apk/app-debug.apk`. No es una release publicada. La firma release se trata en producción; esta revisión no instaló APK ni cambió permisos de teléfonos.

## 8. Verificación y pruebas

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/salud
```

Abra Swagger local en `http://localhost:8080/swagger-ui/index.html`; revise el servidor seleccionado antes de enviar solicitudes. Compruebe web/Android con datos ficticios propios. Salud solo acredita respuesta del servicio; un 401 del perfil anónimo no demuestra todos los controles de autorización.

Las suites PostgreSQL opt-in exigen exactamente `VIDIA_TEST_PG_URL=jdbc:postgresql://127.0.0.1:55439/postgres`, instancia desechable y fixture `vidia_test` definido en esas pruebas. No las dirija a la base normal o productiva. Sin ese entorno se omiten. `RUN_SUPABASE_STORAGE_SMOKE=true` escribe/elimina un objeto: manténgala deshabilitada salvo bucket de pruebas autorizado.

Los resultados históricos del 12/10 se enlazan en el README. No se repitieron suites en esta revisión. Flutter analyze tuvo salida 1, no un análisis limpio.

## 9. Diagnóstico

| Problema | Comprobación y solución segura |
|---|---|
| Java/Maven no encontrados | PATH/JAVA_HOME y nueva terminal; identificar ejecutable |
| Node incompatible | Rango declarado y CLI local del proyecto |
| Conexión SQL fallida | Instancia local, host/puerto, base, rol y contraseña |
| Puerto 8080 ocupado | Identificar proceso propio; detenerlo o coordinar puerto y clientes |
| JWT inválido | Base64 de al menos 32 bytes; no publicar su valor |
| Supabase falla al arrancar | Local: seleccionar `local`; integración: completar credenciales propias |
| Correo no disponible | Proveedor, clave y remitente; no declarar verificación enviada |
| Flutter no conecta | Ruta, emulador/LAN, firewall y modo debug/release |
| Seed ausente | Perfil `demo` y contraseña local, solo en base ficticia |
| Flyway falla | Conservar datos, leer versión/checksum/error; no reparar automáticamente |

El [Anexo B Word](../documentoFinal/Anexo_B_Manual_de_Instalacion_Despliegue_Mantenimiento_Vidia.docx) conserva las evidencias extensas y los límites de reproducción.
