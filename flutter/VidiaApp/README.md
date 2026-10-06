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
