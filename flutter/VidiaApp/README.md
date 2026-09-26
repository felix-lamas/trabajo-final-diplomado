# Vidia

Aplicación Flutter para participantes de la plataforma de eventos UAJMS.

> Donde los eventos cobran vida

## Alcance E2

- Inicio y cierre de sesión con JWT.
- Catálogo de eventos publicados.
- Detalle de evento.
- Inscripción directa a eventos gratuitos.
- Consulta de mis inscripciones.

Pagos, comprobantes, QR, GPS, asistencia, certificados y funciones
administrativas no forman parte de esta entrega.

## Backend

Vidia consume exclusivamente la API REST de Spring Boot. La URL se configura
una sola vez mediante `API_BASE_URL`; debe incluir `/api/v1` y no terminar en
`/`.

El valor predeterminado está preparado para un emulador Android:

```text
http://10.0.2.2:8080/api/v1
```

Para Windows, web o un dispositivo con otra dirección local:

```powershell
flutter run --dart-define=API_BASE_URL=http://localhost:8080/api/v1
```

Para Render:

```powershell
flutter run --dart-define=API_BASE_URL=https://TU-BACKEND.onrender.com/api/v1
```

No se almacenan contraseñas, secretos JWT ni credenciales de base de datos en
el proyecto. El token de sesión se guarda mediante almacenamiento seguro del
dispositivo.

## Ejecución y validación

```powershell
flutter pub get
flutter run
flutter analyze
flutter test
flutter build apk --release --dart-define=API_BASE_URL=https://TU-BACKEND.onrender.com/api/v1
```
