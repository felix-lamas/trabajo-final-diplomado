# E3.6-D — Resultado

## 1. Resumen

Se implementaron catálogo, búsqueda/filtros soportados por backend, detalle, inscripción gratis/pagada, mis inscripciones, estados y cancelación. Análisis, tests, build y arranque Android se verificaron. La aceptación funcional integral queda PENDIENTE DE PRUEBA por falta de credencial de participante demo para operar en el teléfono.

## 2. Auditoría inicial

Se revisaron la estructura Flutter completa, servicios API, configuración, almacenamiento/token, repositorios, modelos, pantallas y tests existentes; también los controladores, DTO, enums y servicios Spring Boot de eventos, inscripciones y categorías, además del contrato OpenAPI disponible en `docs/api/openapi-contrato-v1.md`.

El árbol ya tenía cambios previos de E3.6-B/E3.6-C y documentación anterior. Se preservarán; el inventario inicial completo se conserva en el `git status` de inicio de esta fase y se comparará con los archivos tocados por E3.6-D al finalizar.

## 3. Contratos backend utilizados

Contratos identificados en auditoría y utilizados:

- `GET /api/v1/eventos/publicados` — catálogo público, sin autenticación, `List<EventoResponse>`.
- `GET /api/v1/eventos/publicados/buscar?texto=&categoriaId=&tipo=&modalidad=` — búsqueda pública; filtros opcionales combinables. `tipo`: `GRATUITO|PAGO`; `modalidad`: `PRESENCIAL|VIRTUAL`.
- `GET /api/v1/eventos/categoria/{id}` — eventos publicados de categoría.
- `GET /api/v1/eventos/{id}` — `EventoDetalleResponse`; anónimo solo ve evento publicado; no visible/inexistente da 404.
- `GET /api/v1/categorias-evento/activas` — categorías activas, requiere rol USUARIO autenticado.
- `POST /api/v1/inscripciones` — body `{eventoId}`, rol USUARIO; crea estado decidido por backend; 400 restricciones de registro/publicación, 404 evento ausente, 409 duplicado.
- `GET /api/v1/inscripciones/mis-inscripciones` — lista propia, rol USUARIO; campos `id`, `eventoId`, `eventoTitulo`, `fechaInscripcion`, `estado`.
- `GET /api/v1/inscripciones/{id}` — detalle según ownership.
- `PATCH /api/v1/inscripciones/{id}/cancelar` — cancelación propia para USUARIO; devuelve 200 sin cuerpo, con reglas de negocio backend.
- Estados reales: `PENDIENTE_PAGO`, `PENDIENTE_VALIDACION`, `CONFIRMADA`, `CANCELADA`.
- `EventoResponse` incluye `id`, título, descripción, objetivos, categoría ID/nombre, modalidad, tipo de inscripción, fechas/horas, costo, cupo máximo/disponible, estado, imagen, requiere inscripción, cupo limitado, certificado/horas, público objetivo y organizador.
- `EventoDetalleResponse` añade ubicación, dirección, latitud/longitud, radio, enlace virtual, contactos, URL/instrucciones de pago y motivos internos de rechazo/cancelación. Flutter muestra solo los datos pertinentes al participante; motivos administrativos no se renderizan.
- La búsqueda no soporta paginación ni filtros de fecha, audiencia, disponibilidad o rango de precio. No se creó endpoint ni campo alguno.

## 4. Matriz backend vs Flutter

| Funcionalidad | Backend real | Flutter actual | Diferencia | Acción |
|---|---|---|---|---|
| Catálogo | `GET /eventos/publicados`, lista pública | Consume endpoint | No presenta búsqueda/filtros y algunos estados solo están implícitos | Completar UI y estados |
| Búsqueda | `/eventos/publicados/buscar`, `texto` | No consumido | Búsqueda ausente | Conectar parámetros reales |
| Categorías | `/categorias-evento/activas`, autenticado; `categoriaId` server-side | Nombre en Evento solamente | Selector/catálogo ausente | Cargar categorías activas para usuario autenticado |
| Filtros | `categoriaId`, `tipo`, `modalidad` combinables | Ninguno | Filtros ausentes | Implementar solo los tres soportados |
| Detalle | `GET /eventos/{id}`, `EventoDetalleResponse` con datos ampliados | Consume; modelo derivado del DTO de lista | Omite audiencia, coordenadas, contacto, pago y campos específicos | Ampliar modelo y mostrar datos de participante disponibles |
| Inscripción gratuita | `POST /inscripciones`, estado respuesta del servicio | Consumida solo si gratuito | POST bloqueado para pagados; resultado no preserva todos los campos del detalle | Permitir ambas clases y presentar estado devuelto |
| Inscripción pagada | Mismo `POST`; backend crea estado | Bloqueada con mensaje etapa futura | No crea inscripción | Enviar solicitud sin simular pago |
| Mis inscripciones | `GET /inscripciones/mis-inscripciones` | Consume | Vista solo muestra título/fecha/estado; sin precio/modalidad; chips admiten estado inexistente | Enriquecer desde detalle cuando sea viable y usar enum/estados reales |
| Cancelación | `PATCH /inscripciones/{id}/cancelar`, USUARIO | No implementada | Operación disponible omitida | Añadir confirmación y refresco |

## 5. Archivos modificados

Cambios Flutter de E3.6-D: `lib/main.dart`; modelos `evento.dart`, `categoria_evento.dart`; repositorios de eventos, inscripciones y categorías (interfaces y backend); pantallas `events_screen.dart`, `event_detail_screen.dart`, `my_registrations_screen.dart`, `registration_confirmation_screen.dart`; `services/api_service.dart`; tests `api_error_test.dart`, `categoria_repository_test.dart`, `evento_repository_test.dart`, `inscripcion_repository_test.dart`; este informe.

`lib/config/app_config.dart` ya estaba modificado por E3.6-C. El formateador de la pasada inicial lo tocó por formato; no cambié lógica/configuración de ambientes en D. Los demás cambios que aparecían en el `git status` inicial siguen preservados. No se cambiaron backend ni monografía.

## 6. Catálogo

**OK** — consume `GET /eventos/publicados`; presenta carga, vacío, error con reintento, refresco y lista. Las tarjetas usan datos recibidos de imagen, título/descripción, categoría, fecha/hora, modalidad, tipo/precio y disponibilidad cuando se informa. Sin mocks ni datos hardcodeados.

## 7. Búsqueda

**OK** — consume `/eventos/publicados/buscar` con `texto` y debounce; no reconstruye listas localmente. El contrato no declara paginación.

## 8. Filtros

**OK** — filtros combinables reales `categoriaId`, `tipo` (`GRATUITO|PAGO`) y `modalidad` (`PRESENCIAL|VIRTUAL`); limpiar restablece texto y selecciones. Diferencia catálogo vacío de cero coincidencias. No hay filtros backend para fecha, audiencia, disponibilidad ni rango de precio.

## 9. Categorías

**OK** — `GET /categorias-evento/activas`, requiere token de usuario. Si falla, no inventa categorías; el catálogo continúa disponible.

## 10. Detalle del evento

**OK** — consume `EventoDetalleResponse`: descripción, objetivos, fechas/horas, modalidad, ubicación/dirección/enlace virtual, audiencia, capacidad devuelta, precio/registro, estado, imagen, certificado/horas, contacto e instrucciones de pago si existen. Coordenadas no se muestran porque no hacen falta para información. QR de pago no se implementa.

**OK** — evento no visible/no encontrado responde a estado 404 del backend con mensaje y reintento; no se representa como detalle vacío.

## 11. Inscripción gratuita

**OK** — solicita `{eventoId}`; la confirmación presenta estado que devolvió backend. Botón bloqueado mientras procesa; cupo/duplicidad siguen siendo decisión backend.

## 12. Inscripción pagada

**OK** — el mismo POST crea la inscripción; `PENDIENTE_PAGO` se presenta como “Inscripción pendiente de pago”. No simula pago, comprobante ni aprobación.

## 13. Mis inscripciones

**OK** — lista propia con evento, fecha de inscripción y estado; chips visuales distintos para los cuatro estados reales. El response no incluye precio/modalidad/fecha del evento, por eso no se inventan. Se puede abrir el detalle mientras el evento siga visible.

## 14. Cancelación

**OK** — endpoint real `PATCH /inscripciones/{id}/cancelar`; confirma antes, recarga lista al responder y presenta error seguro si backend rechaza por sus reglas. Sin acción para estado `CANCELADA`.

## 15. Estados de UI

**OK** — catálogo, detalle e inscripciones manejan carga, éxito, vacío y error/reintento donde procede. No se exponen stack traces. Doble toque no duplica POST durante el envío.

## 16. Tests

**OK** — `flutter analyze`: `No issues found`. `flutter test`: 29 aprobados, 0 fallidos, 0 omitidos. Incluye catálogo exitoso/vacío/error, categorías, detalle/404, búsqueda/params, inscripción gratis/paga, mis inscripciones, cancelación y códigos 409 de duplicado/capacidad, además de suite previa. `git diff --check`: sin errores; Git avisa conversión de finales LF/CRLF en archivos preexistentes.

## 17. Validación Android

**PROBADO LOCALMENTE** — APK debug compilado con `API_BASE_URL` del backend local por LAN (el SM A125M y el equipo estaban en la misma subred), instalado y abierto en `bo.edu.uajms.vidia`. La actividad permaneció activa y la consulta de logcat no encontró errores fatales Flutter/Android. Desde el teléfono, un GET al catálogo público del backend respondió HTTP 200. No se cambió configuración persistente ni se hizo deployment.

**PROBADO LOCALMENTE** — `http://localhost:8080/api/v1/eventos/publicados` respondió HTTP 200.

**PENDIENTE DE PRUEBA** — recorrido autenticado en dispositivo de catálogo, detalle, inscripción gratis/paga, mis inscripciones, duplicado y evento no publicado. No se suministró credencial de participante demo para operar el flujo; el arranque Android no se considera prueba funcional.

## 18. Pendientes

- **PENDIENTE DE PRUEBA** — flujo autenticado manual en dispositivo con participante demo; falta credencial verificable en esta sesión.
- **NO VERIFICADO** — escala con grandes volúmenes. El contrato no pagina el catálogo y la búsqueda es server-side.
- **NO VERIFICADO** — Flutter conectado a Render. No se probó producción ni se hizo deployment.

## 19. Riesgos

- Lista y catálogo no declaran paginación; un crecimiento relevante puede requerir una evolución backend, fuera de D.
- Mis inscripciones no devuelve modalidad, precio ni fecha del evento; no se inventan ni se realizan requests masivos para completarlos.
- Disponibilidad es informativa según el último response; backend valida capacidad y duplicidad al crear.
- Categorías requiere autenticación `USUARIO`; ante fallo no se sustituye la fuente por datos ficticios.

## 20. Estado final

**PENDIENTE DE PRUEBA** — cambios cliente alineados al contrato real, análisis sin issues, suite aprobada, build e inicio físico verificados. Falta recorrido autenticado móvil. Sin commit/push; sin cambios backend, monografía, producción, pagos, QR, GPS, asistencia o certificados.
