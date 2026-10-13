# Vidia — aplicación móvil de participante

Aplicación Flutter de Vidia para el participante (`USUARIO`). La app forma parte del alcance final; E2 fue una entrega académica intermedia y no limita este alcance.

## Implementación actual y guías

**Implementado en el árbol revisado el 13/10/2026:** registro/verificación/recuperación, login/sesión, catálogo y detalle, inscripción y actividad/historial propios, gestión de pago externo y comprobantes, pantalla de escaneo QR con ubicación y consulta/descarga de certificados. La solicitud de organizador está disponible desde el perfil; las funciones de gestión siguen siendo web.

**Pendiente de validación completa:** correo, flujos que modifican registros y escaneo físico QR/GPS. El manual del 12/10 documenta una incidencia al consultar sesiones, sin comprobación posterior de resolución. Implementación no equivale a funcionamiento productivo validado.

Consulte las guías actuales: [instalación local](../../docs/manuales/instalacion-local.md), [despliegue y operación](../../docs/manuales/despliegue-produccion.md) y [uso del sistema](../../docs/manuales/uso-del-sistema.md). Los comandos de este README son referencias específicas del cliente; los resultados de suites conservados tienen su propia fecha.

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

Los resultados de E3.4/E3.6-B corresponden a cortes históricos. La evidencia del Anexo B del 12/10/2026 registra 139 pruebas aprobadas, `flutter analyze` con salida 1 y 17 observaciones informativas, y un APK debug generado sin instalación. La release Android v1.0.0 ya está publicada en GitHub; no se verificaron nuevamente su firma ni instalación al revisar esta documentación. Las suites no se repitieron el 13/10. Consulte las guías actuales y sus fuentes de resultados.


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

El pubspec declara Dart `>=3.12.0 <4.0.0` y app_links 7.2.2; no fija una versión
exacta de Flutter. El entorno comprobado utiliza Flutter 3.44.8 y Dart 3.12.2.
No se modificó la firma Android durante la actualización documental.

### Límite de esta fase

El Manifest prepara VIEW/DEFAULT/BROWSABLE y autoVerify para los dos paths.
El 13/10/2026 se consultó
`https://trabajo-final-diplomado-web.onrender.com/.well-known/assetlinks.json`:
respondió 200, application/json y un arreglo con el paquete `bo.edu.uajms.vidia`.
Esto confirma publicación del descriptor, no la asociación verificada por Android:
queda comprobar la huella contra la firma de la APK instalada y el estado del dispositivo.
La app instalada debe permitir abrir enlaces compatibles; el correo puede usar
un navegador interno y el usuario puede cambiar las preferencias de apertura.
Sin asociación verificada o sin app, el enlace sigue el flujo web.

El Gradle actual exige `android/key.properties` y una configuración release completa;
no utiliza una firma debug de respaldo si faltan esos datos. Antes de distribuir
hay que comprobar la firma efectiva y su correspondencia con assetlinks; no se
asumen verificadas las URLs por compilar un APK.

### Validación

`flutter test` incluye formularios independientes, contratos HTTP sin Authorization
para enlaces, rechazo de URIs, deduplicación, errores de red y tokens inválidos,
expirados/utilizados, además de pruebas de Navigator con inicio frío, app abierta
y background simulados. No envían correos reales. La validación Android real
requiere comprobar el APK firmado, la correspondencia del descriptor publicado y
comprobar `adb shell pm get-app-links bo.edu.uajms.vidia` y ambos correos con
una cuenta de prueba. Una prueba que fuerce el paquete con ADB no demuestra
que Android haya verificado el dominio.
