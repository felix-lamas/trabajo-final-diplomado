# Vidia — aplicación móvil de participante

Aplicación Flutter de Vidia para el participante (`USUARIO`). La app forma parte del alcance final; E2 fue una entrega académica intermedia y no limita este alcance.

## Estado de implementación al corte E3.4

**Implementado en el árbol actual:** login/sesión, catálogo y detalle de eventos publicados, inscripción y consulta de mis inscripciones.

**Pendiente de implementación:** registro/verificación/recuperación móvil (si se decide permitir alta autónoma desde móvil), flujo de pago externo y comprobantes, escaneo QR de asistencia con captura de ubicación, consulta/descarga de certificados e historial móvil de pagos/asistencias/certificados. El detalle de evento todavía informa que pagos estarán disponibles posteriormente. No se afirma que estos flujos estén listos porque existan en backend o Web.

No hay funciones móviles de Administrador u Organizador. El backend conserva autorización, ownership, reglas y estados; Flutter no conecta directamente a Supabase.

## API y configuración de ambientes

Flutter consume exclusivamente la API REST de Spring Boot. La única fuente de la URL base es `API_BASE_URL`, suministrada mediante `--dart-define`; debe incluir `/api/v1`. Si no se especifica, la configuración elige `localhost` para web/escritorio y `10.0.2.2` para Android Emulator. En un teléfono físico se debe proporcionar la dirección IP LAN del equipo que ejecuta el backend.

### Desarrollo local

Backend local en el equipo:

```text
http://localhost:8080
```

Android Emulator accede al mismo backend por:

```text
http://10.0.2.2:8080/api/v1
```

Ejecutar desde PowerShell:

```powershell
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080/api/v1
```

Para web/escritorio local:

```powershell
flutter run -d windows --dart-define=API_BASE_URL=http://localhost:8080/api/v1
```

El manifiesto Android de `debug` permite HTTP para el backend local. Esa opción no forma parte del manifiesto `main` ni del build `release`.

### Producción

Backend Render:

```text
https://trabajo-final-diplomado.onrender.com
```

API:

```text
https://trabajo-final-diplomado.onrender.com/api/v1
```

Compilar el APK release con HTTPS:

```powershell
flutter build apk --release --dart-define=API_BASE_URL=https://trabajo-final-diplomado.onrender.com/api/v1
```

La aplicación rechaza una `API_BASE_URL` que no termine en `/api/v1` y una URL que use HTTP en compilación release. La disponibilidad de Render debe verificarse por separado; esta configuración no constituye evidencia de conectividad.

El token de sesión se almacena mediante almacenamiento seguro del dispositivo. No incluir credenciales de PostgreSQL, JWT, SMTP/Brevo o Supabase en Dart, `--dart-define`, assets ni README.

## Validación y distribución

```powershell
flutter analyze
flutter test
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080/api/v1
```

Al corte E3.4 no se registró un resultado concluyente de `flutter test`. En E3.6-B, `flutter analyze` y los 6 tests terminaron correctamente y se generó un APK debug con la URL del emulador. El AVD disponible quedó sin autorización ADB, por lo que no se verificó la conexión desde Android; Render tampoco fue accesible desde el entorno de auditoría. No se declara APK release validado ni publicado.


## Autenticación móvil y App Links - fase 1

Registro conduce a **Revisa tu correo**, con el correo destino y reenvío de
verificación. Login ofrece **¿Olvidaste tu contraseña?** y un acceso separado
a reenvío. `EMAIL_NOT_VERIFIED` abre las instrucciones de verificación.
Recuperación pide solo el correo y muestra la respuesta no enumerativa.
No hay entrada manual de tokens.

`AuthLinkCoordinator` recibe el enlace inicial y eventos posteriores mediante
`app_links 7.2.2`; conserva Navigator y espera su disponibilidad, la restauración
de sesión y el estado resumed. Los duplicados se identifican con SHA-256
(`crypto`) solo en memoria durante el proceso. No guarda URLs ni tokens en disco.
Los tokens se usan temporalmente en las pantallas y nunca como JWT o Authorization.
Los errores de enlaces muestran mensajes locales sin exponer detalles de la URI.

Solo se aceptan HTTPS, el dominio `trabajo-final-diplomado-web.onrender.com` y:

- `/auth/verificar-correo?token=...`: verificación automática y éxito/error.
- `/auth/restablecer-contrasena?token=...`: nueva contraseña y confirmación;
  el envío se realiza al confirmar el formulario. Al completar, limpia la sesión local.

También se pueden navegar esas rutas mediante el Navigator interno. RouteSettings
conserva únicamente el path, sin token. Los enlaces con token ausente, vacío o
repetido, autoridad incorrecta, fragmentos o rutas desconocidas se rechazan.
Un error de red permite reintentar desde la pantalla; un token expirado, inválido
o utilizado conduce a solicitar otro enlace. La deduplicación no persiste tras
cerrar el proceso; el backend sigue validando la vigencia y uso del token.

Esta fase usa Flutter >=3.44 / Dart >=3.12. Se fijó app_links 7.2.2 porque su
implementación Android no imprime los intents/URLs; versiones anteriores como
6.4.1 exponen la URI en logs nativos. No se modificó la firma Android.

### Límite de esta fase

El Manifest prepara VIEW/DEFAULT/BROWSABLE y autoVerify para los dos paths.
**App Links aún no está verificado**: falta publicar un JSON válido en
`https://trabajo-final-diplomado-web.onrender.com/.well-known/assetlinks.json`
con el paquete `bo.edu.uajms.vidia` y la huella SHA-256 de la firma de distribución.
La respuesta debe ser JSON real, no el fallback HTML de Angular.
Este archivo público y la configuración de hosting no se implementan aquí.
La app instalada debe permitir abrir enlaces compatibles; el correo puede usar
un navegador interno y el usuario puede cambiar las preferencias de apertura.
Sin asociación verificada o sin app, el enlace sigue el flujo web.

El Gradle existente usa la firma debug en release si no existe `key.properties`.
Antes de distribución hay que confirmar la firma efectiva y asociar su huella;
no se deben asumir verificadas las URLs por haber compilado un APK.

### Validación

`flutter test` incluye formularios independientes, contratos HTTP sin Authorization
para enlaces, rechazo de URIs, deduplicación, errores de red y tokens inválidos,
expirados/utilizados, además de pruebas de Navigator con inicio frío, app abierta
y background simulados. No envían correos reales. La validación Android real
requiere posteriormente instalar el APK firmado, publicar la asociación y
comprobar `adb shell pm get-app-links bo.edu.uajms.vidia` y ambos correos con
una cuenta de prueba. Una prueba que fuerce el paquete con ADB no demuestra
que Android haya verificado el dominio.
