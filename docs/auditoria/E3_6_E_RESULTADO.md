# E3.6-E — Resultado

## 1. Resumen

**PROBADO LOCALMENTE** — Flutter consume los endpoints reales para pagos propios, subida multipart, descarga de comprobante, constancia de inscripción y QR de pago del evento. Se implementaron la pantalla de pago, validación local de archivos, presentación de los estados reales y acceso desde detalle/resultado de inscripción.

**PENDIENTE DE PRUEBA** — No se cierra la validación Android del upload con el archivo temporal claramente identificado. Durante la navegación del selector se envió inadvertidamente un archivo que ya estaba en el teléfono. El backend y la app muestran ahora `PENDIENTE_VALIDACION`; el archivo enviado no se cuenta como comprobante técnico de prueba y no se inspeccionó su contenido.

## 2. Auditoría inicial

Auditoría completada antes de cambiar Flutter. Se revisaron `PagoController`, `PagoService`, `ArchivoSeguroServicio`, DTOs/mappers/enum de pagos, controladores/servicio/DTO de inscripción, manejo global de errores, configuración multipart, el contrato OpenAPI publicado localmente en `/v3/api-docs`, `docs/api/openapi-contrato-v1.md`, servicios/modelos/pantallas Flutter, tests y dependencias.

Flutter tenía instrucciones/URL QR dentro de `Evento`, pero no tenía modelo, repositorio, pantalla de pagos, subida de archivos ni consultas de comprobante. `pubspec.yaml` no incluía selector de archivos. Se detectó que la inscripción pagada actual crea el pago automáticamente. `POST /pagos` solo cubre compatibilidad histórica cuando falta un pago; Flutter no lo invoca en el recorrido normal.

## 3. Contratos backend utilizados

- `GET /api/v1/pagos/mis-pagos` — rol `USUARIO`; lista solo pagos propios y contiene `id`, `inscripcionId`, `eventoTitulo`, `monto`, `estado`, `motivoRechazo`, fechas, intentos y metadatos opcionales del comprobante.
- `POST /api/v1/pagos/{id}/comprobante` — rol `USUARIO`; `multipart/form-data`, campo requerido `archivo`; respuesta `PagoResponse`. Admite pago `PENDIENTE_PAGO` o `RECHAZADO` cuando la inscripción propia está `PENDIENTE_PAGO`; la nueva presentación establece pago e inscripción en `PENDIENTE_VALIDACION` y reemplaza el archivo previo.
- `GET /api/v1/pagos/{id}/comprobante` — descarga los bytes del archivo propio con media type original: `image/jpeg`, `image/png` o `application/pdf`. No hay endpoint separado para consultar/descargarlo.
- `GET /api/v1/inscripciones/{id}/comprobante` — rol `USUARIO` propietario; devuelve JSON de constancia no tributaria, no factura ni certificado. No devuelve PDF.
- `GET /api/v1/eventos/{id}/qr-pago` — sirve PNG/JPEG para el evento visible. Flutter lo llama a Spring; no accede a Supabase.
- El alta normal de inscripción pagada (`POST /api/v1/inscripciones`) crea el pago y deriva el monto desde el evento. No se llama `POST /pagos` para inventar/duplicar un pago.
- Estados reales del enum `EstadoPago`: `PENDIENTE_PAGO`, `PENDIENTE_VALIDACION`, `APROBADO`, `RECHAZADO`. Al rechazar desde la gestión backend, el pago queda `RECHAZADO` y la inscripción vuelve a `PENDIENTE_PAGO`, habilitando el mismo endpoint de reenvío. Flutter no aprueba ni rechaza.
- El backend limita el archivo a 5 MiB, acepta extensiones JPG/JPEG/PNG/PDF, comprueba MIME declarado y contenido real y valida coherencia entre MIME/extensión. La configuración multipart permite 10 MiB para la solicitud; el servicio aplica el límite de 5 MiB.
- Errores observados/declarados para este flujo: `400` validación/regla/archivo, `401`, `403`, `404`, `409` (`PAYMENT_INVALID_STATE`) y `413` (`FILE_TOO_LARGE` al exceder el límite del parser multipart). La API de pagos no declara un `415` ni `422` propio; Flutter no añade semántica ficticia para ellos.

## 4. Matriz backend vs Flutter

| Funcionalidad | Backend real | Flutter actual | Diferencia | Acción |
|---|---|---|---|---|
| Consultar pago | `GET /pagos/mis-pagos`, pagos del usuario autenticado | Antes no implementado | Sin modelo ni consulta | Modelo `Pago`, repositorio y cruce por `inscripcionId` propio |
| Subir comprobante | `POST /pagos/{id}/comprobante`, multipart `archivo` | Antes no implementado | Sin selector/API multipart | `MultipartRequest` autenticado con campo y MIME exactos |
| Reemplazar comprobante | Mismo POST para pago `RECHAZADO` e inscripción `PENDIENTE_PAGO` | Antes no implementado | Sin reenvío | Mismo endpoint; acción aparece solo si los estados permiten reenvío |
| Consultar comprobante | `GET /pagos/{id}/comprobante`, bytes con MIME original | Antes no implementado | Sin descarga | Descarga propia; imagen previsualizable y PDF guardable vía selector del sistema |
| Descargar comprobante | Mismo GET binario; ownership backend | Antes no implementado | Sin manejo binario | API conserva bytes/MIME; Android muestra imagen o permite guardar PDF/imagen |
| Estado pago | `EstadoPago` y response propio | Antes solo estado de inscripción | No existía pago asociado en Flutter | UI muestra estado/motivo/fechas reales del pago |
| Constancia de inscripción | `GET /inscripciones/{id}/comprobante`, JSON propio | Antes no implementado | Sin consulta | Diálogo separado; omite CI/RU y aclara que no es factura/certificado |
| Instrucciones y QR | Datos de evento y `GET /eventos/{id}/qr-pago` | Instrucciones ya se mostraban; QR no | Sin render del QR | Instrucciones del DTO y bytes del endpoint Spring cuando el detalle informa QR |

## 5. Archivos modificados

- Flutter: `pubspec.yaml`, `pubspec.lock`, `lib/main.dart`, `lib/services/api_service.dart`, nuevos `lib/models/pago.dart`, `lib/models/archivo_comprobante.dart`, `lib/repositories/pago_repository.dart`, `lib/screens/payment_management_screen.dart`, además de `event_detail_screen.dart`, `registration_confirmation_screen.dart` y `my_registrations_screen.dart` para acceso y refresco de estados.
- Tests: nuevos `test/pago_repository_test.dart`, `test/archivo_comprobante_test.dart`, `test/payment_management_screen_test.dart`; ampliado `test/api_error_test.dart`.
- Documentación: este archivo.
- El único paquete Flutter añadido es `file_picker` 10.3.7 para selector nativo y guardado Android. `http_parser` se declara directamente para el MIME multipart; ya estaba resuelto transitivamente por `http`. Se eligió file_picker 10.3.7 para conservar compatibilidad con `win32` 5 requerido por el almacenamiento seguro presente.
- No se modificaron backend, OpenAPI, monografía, Render, Supabase ni E3.6-D/E3.6-D.1. Se preservó todo lo anterior.

## 6. Estados de pago

**OK** — Se presentan de forma distinta `PENDIENTE_PAGO`, `PENDIENTE_VALIDACION`, `APROBADO` y `RECHAZADO`; cualquier nombre desconocido se muestra tal como llegó, sin transformarlo en estado de negocio. La UI toma el pago desde `mis-pagos` y refresca también la inscripción antes de habilitar reenvío.

**OK** — `PENDIENTE_VALIDACION` bloquea otra carga; `APROBADO` no presenta acción de modificación; `RECHAZADO` puede volver a presentar si la inscripción propia está en `PENDIENTE_PAGO`.

## 7. Monto

**OK** — La pantalla muestra `PagoResponse.monto`; no permite edición ni calcula el importe a partir de valores locales. Para la inscripción demo probada, el backend respondió Bs. 60.00.

## 8. Instrucciones de pago

**PROBADO LOCALMENTE** — Se mostró el texto de instrucciones que devolvió el detalle publicado del evento. No se añadieron cuentas, beneficiarios ni datos bancarios en Flutter.

## 9. QR de pago

**PROBADO LOCALMENTE** — El evento demo informó QR y Spring sirvió la imagen desde `GET /eventos/{id}/qr-pago`; Flutter la mostró desde bytes PNG/JPEG. El recurso visible del entorno lleva la marca “FICTICIO / SOLO PRUEBAS”; no se escaneó ni se realizó un pago. Si el evento no informa QR, no se genera uno.

## 10. Subida de comprobante

**OK** — Implementado multipart con método `POST`, autorización Bearer almacenada en `TokenStore`, campo `archivo`, filename y MIME derivados del tipo permitido, respuesta JSON `PagoResponse` y bloqueo de doble envío en la pantalla. La selección exige acción posterior “Enviar comprobante” y confirmación.

**PENDIENTE DE PRUEBA** — Upload Android con un archivo temporal identificado como prueba. La pantalla cambió a `PENDIENTE_VALIDACION` durante una navegación accidental del picker que seleccionó un archivo ya existente en el dispositivo. No se verificó el contenido ni se considera evidencia de un upload de prueba limpio. No se volvió a interactuar con pagos.

## 11. Validación de archivo

**OK** — El selector acepta únicamente JPG/JPEG, PNG y PDF; comprueba extensión, archivo vacío y tamaño máximo de 5 MiB antes de enviar. La lectura Android usa stream y corta al superar 5 MiB, sin cargar de antemano archivos grandes completos. El MIME de request deriva de la extensión; la validación de contenido real sigue correspondiendo al backend.

**OK** — Cancelar el selector no produce envío. Error de lectura/archivo ausente se comunica al usuario. El backend sigue validando tamaño, Tika/MIME real y coherencia.

## 12. PENDIENTE_VALIDACION

**PROBADO LOCALMENTE** — Tras el cambio de estado observado en el teléfono, Flutter mostró `Pendiente de validación` y “Comprobante enviado. Pendiente de validación.” No mostró aprobación. La respuesta se vio en la pantalla recargada desde backend; la identidad del archivo no quedó validada como archivo técnico autorizado para pruebas.

## 13. APROBADO

**OK** — Widget test confirma la presentación “Pago aprobado” y que no aparece acción para seleccionar/reemplazar el comprobante. No se ejecutó una aprobación real desde un rol organizador/administrador.

**PENDIENTE DE PRUEBA** — Respuesta real `APROBADO` y estado de inscripción actualizado; requiere una revisión autorizada en backend, fuera del cliente participante.

## 14. RECHAZADO

**OK** — Widget test confirma estado rechazado, motivo backend y presentación de acción de reenvío solo cuando la inscripción está `PENDIENTE_PAGO`.

**PENDIENTE DE PRUEBA** — Rechazo real y motivo real devueltos por backend; Flutter no ejecuta aprobación/rechazo.

## 15. Reenvío

**OK** — Tests verifican que el reenvío usa el mismo multipart real y que la respuesta `PENDIENTE_VALIDACION` se refleja. En runtime la opción se condiciona al pago `RECHAZADO` y a inscripción actual `PENDIENTE_PAGO`.

**PENDIENTE DE PRUEBA** — Reenvío Android después de rechazo real. No se fabricó una decisión de revisión.

## 16. Comprobante de inscripción

**OK** — Se implementó consulta del endpoint JSON real en UI separada del comprobante de pago. No se intenta abrir como PDF y se excluyen CI/RU de la presentación. El contrato lo define como constancia no tributaria, no factura ni certificado.

**PENDIENTE DE PRUEBA** — Consulta real de la constancia desde Android.

## 17. Seguridad

**OK** — Pago se selecciona del endpoint `mis-pagos` y se vincula a una inscripción presente en `mis-inscripciones`; el id usado para descargar/subir no se escribe manualmente. El backend sigue comprobando ownership.

**OK** — JWT solo viaja en `Authorization` leído desde `TokenStore`; no se escribe en query/body ni se imprime. Flutter no lleva credenciales Supabase y no se conecta a Storage directamente.

## 18. Tests

**VERIFICADO** — `flutter analyze`: `No issues found`.

**VERIFICADO** — `flutter test`: 45/45 aprobados, 0 fallidos, 0 omitidos. La última ejecución completa incluyó el test de reenvío corregido para inspeccionar los bytes multipart como request binario.

**VERIFICADO** — `flutter analyze`: `No issues found` en la ejecución final.

Los casos nuevos cubren pagos propios, subida multipart y respuesta pendiente, reenvío, conflicto de estado, descarga binaria, constancia JSON, QR de Spring, MIME/extensiones/tamaño, errores HTTP y UI para los cuatro estados.

## 19. Validación Android

**PROBADO LOCALMENTE** — Backend local `http://localhost:8080/api/v1` respondió HTTP 200 al catálogo. Dispositivo físico SM A125M (`R58R240RCWH`) disponible. APK debug compilado con `API_BASE_URL=http://192.168.100.131:8080/api/v1` e instalado sobre Vidia conservando la sesión demo.

**PROBADO LOCALMENTE** — En “Jornada Cultural en Preparacion” el detalle mostró Bs. 60.00, instrucciones, capacidad devuelta por backend y QR servido por Spring. En gestión de pago se verificó monto y estado `PENDIENTE_PAGO`; después de la selección accidental el backend/app mostraron `PENDIENTE_VALIDACION` y el aviso de espera. No se aprobó ni rechazó el pago, ni se hizo una transferencia.

**PENDIENTE DE PRUEBA** — Flujo limpio de seleccionar el PDF “PRUEBA TÉCNICA — NO ACREDITA PAGO REAL”, subirlo y comprobar que la respuesta del backend fue la de ese archivo. El PDF temporal no fue el archivo que el sistema registró.

## 20. Pendientes

- **PENDIENTE DE PRUEBA** — Upload Android controlado con archivo demo identificado y sin datos personales.
- **PENDIENTE DE PRUEBA** — Descarga/guardado del comprobante propio en Android; se implementa con el endpoint binario y selector de guardado, pero no se ejecutó en dispositivo.
- **PENDIENTE DE PRUEBA** — Consultar constancia de inscripción real desde Android.
- **PENDIENTE DE PRUEBA** — Validar integración real con respuestas `APROBADO`, `RECHAZADO` y reenvío después de rechazo; no se controló el rol de revisión.
- **NO VERIFICADO** — Flutter conectado a Render/producción.
- Los pendientes de HTTP 409 por duplicidad y capacidad agotada de E3.6-D.1 no se alteraron ni se falsificaron en esta fase.

## 21. Riesgos

- **PENDIENTE DE PRUEBA** — La inscripción demo local asociada a “Jornada Cultural en Preparacion” ahora aparece `PENDIENTE_VALIDACION` y el backend registra un comprobante seleccionado durante el uso accidental del picker. Su contenido no se abrió ni se distribuyó en el informe. El contrato no ofrece borrado/reemplazo participante mientras el estado está pendiente; la pantalla se dejó sin más operaciones.
- **NO VERIFICADO** — Gradle avisa que `file_picker` aplica Kotlin Gradle Plugin, una combinación que Flutter indica que podría dejar de compilar en futuras versiones. El APK actual sí compiló.
- **NO APLICA** — No se creó una pasarela, pago automático ni validación automática.
- El QR de pago es informativo y el entorno de prueba muestra un recurso marcado ficticio; no se debe interpretar como confirmación de transacción.

## 22. Estado final

**PENDIENTE DE PRUEBA** — Implementación cliente alineada con contratos existentes; `flutter analyze` sin issues; `flutter test` 45/45; APK debug compila; pantalla Android obtuvo monto, instrucciones y QR de backend. La prueba de upload controlada, descarga, constancia y estados de revisión reales queda pendiente por la incidencia del selector. Sin cambios backend/OpenAPI/monografía/producción, sin commit ni push.
