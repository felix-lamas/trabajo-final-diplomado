# Validación de las figuras E.12 y E.13

Fecha de revisión: **12 de octubre de 2026**, entorno local Windows. Repositorio revisado: `C:/Trabajo_Final_Vidia/plataforma-eventos-uajms`. HEAD de referencia: `f6891ef3ffb5a89a31e323d37a7ae9d2f734725c`. Existen cambios locales previos; la representación corresponde al árbol de trabajo leído, no se afirma que coincida con el commit desplegado.

## 1. Fuentes y alcance

Se inspeccionaron controladores, servicios, repositorios, DTO, entidades, seguridad, migraciones y pruebas relacionadas; también las pantallas Flutter y la generación visual Angular. No se realizaron solicitudes a producción, migraciones, cambios en cuentas ni ejecución de suites de Vidia para esta tarea. La inspección confirma comportamiento implementado; no sustituye una prueba física QR/GPS ni una comprobación productiva.

Documentación anterior: `docs/design/03_Secuencia_Inscripcion_Pago.wsd` usa `BillingController`, `BillingService`, `/events/...` y `/inscriptions/pay`. Estos nombres y ese flujo no corresponden a la implementación actual y no se reutilizaron. También se localizaron documentos en `docs/auditoria/` y `docs/release_1_0/`; los archivos actuales de aplicación tienen prioridad para estos diagramas.

## 2. E.12: inscripción

Prefijo de rutas Java: `backend/src/main/java/bo/uajms/eventos/`.

| Evidencia real | Comportamiento representado |
|---|---|
| `modulos/inscripciones/controladores/InscripcionController.java`, `inscribir` | POST `/api/v1/inscripciones`, `@Valid`, `@PreAuthorize("hasRole('USUARIO')")`; devuelve 201 y `DetalleInscripcionResponse`. ADMINISTRADOR no habilita por sí solo esta operación. |
| `modulos/inscripciones/dtos/CrearInscripcionRequest.java` | `eventoId` UUID obligatorio. El usuario se obtiene del contexto autenticado, no del cuerpo de la petición. |
| `modulos/inscripciones/servicios/InscripcionService.java`, `inscribir` | `@Transactional`, exigir USUARIO, obtener usuario, buscar/bloquear evento, comprobar duplicado, estado, inscripción requerida, cupos y monto, en ese orden. |
| `modulos/eventos/repositorios/EventoRepository.java`, `findByIdForUpdate` | `@Lock(PESSIMISTIC_WRITE)`; lectura del evento bajo bloqueo dentro de la transacción. |
| `modulos/inscripciones/repositorios/InscripcionRepository.java` | `existsByUsuarioIdAndEventoId`, `save`. |
| `modulos/pagos/repositorios/PagoRepository.java`; `InscripcionService.inscribir` | Para PAGO se crea también un pago PENDIENTE_PAGO, con costo del evento e intentosComprobante=0. No se procesa una transferencia ni una pasarela. |
| `modulos/inscripciones/entidades/Inscripcion.java` | Tabla `inscripciones`; unicidad `(usuario_id, evento_id)` y código de participante único; filtro de borrado lógico. |
| `core/excepciones/ManejadorGlobalExcepciones.java` | Recurso inexistente: 404; negocio: 400; conflicto: 409. La figura agrupa este tratamiento en el límite REST. |
| `frontend/src/app/core/services/inscripcion.service.ts`, `inscribir` | Cliente Angular envía el request a `/inscripciones`. E.12 usa un cliente conceptual, sin convertir este servicio cliente en componente backend. |

### Resultados y reglas

- Evento inexistente: `RecursoNoEncontradoException`, HTTP 404 / `RESOURCE_NOT_FOUND`.
- Duplicado: `ConflictoException`, HTTP 409 / `INSCRIPTION_DUPLICATED`. Esta comprobación precede a las reglas del evento.
- Estado distinto de PUBLICADO: HTTP 400 / `EVENT_NOT_PUBLISHED`.
- Evento que no requiere inscripción: HTTP 400 / `BUSINESS_RULE_VIOLATION`.
- Cupo limitado y cupoDisponible nulo o <=0: HTTP 400 / `INSCRIPTION_CAPACITY_FULL`.
- Evento de pago con monto nulo o no positivo: HTTP 400 / `BUSINESS_RULE_VIOLATION`.
- GRATUITO: inscripción CONFIRMADA; PAGO: inscripción PENDIENTE_PAGO. Con cupo limitado se resta uno y se guarda el evento antes de guardar la inscripción. El pago inicial se guarda en la misma transacción cuando corresponde.

E.12 presupone entrada válida y autenticación/autorización satisfechas; no detalla sus 400 de DTO ni sus 401/403. Tampoco detalla cada error posible al hacer flush/commit: el camino válido exige escritura y commit satisfactorios. No hay envío de correo en `inscribir`.

### Concurrencia e integridad

El bloqueo de la fila del evento y la transacción respaldan la serialización de reservas que atraviesan este método. No se representa una lectura seguida de escritura desprotegida. JPA y V3 declaran `uk_inscripcion_usuario_evento`; no es un índice parcial de «inscripción activa». Una CANCELADA visible continúa siendo detectada por `existsByUsuarioIdAndEventoId`; el borrado lógico filtra la consulta, pero no elimina por sí mismo la restricción física de unicidad.

`backend/src/main/resources/db/migration/V3__reconciliar_esquema_actual.sql` crea las restricciones para esquemas nuevos, pero su reconciliación histórica puede omitir añadirlas cuando encuentra duplicados, emitiendo un NOTICE. No se comprobó el catálogo de restricciones de la base productiva. Las garantías se atribuyen al código y al esquema declarado, no a una instancia desconocida ni a escrituras externas al servicio.

## 3. E.13: QR/GPS

| Participante visual | Clases y archivos agrupados |
|---|---|
| Gestor autorizado | Angular `frontend/src/app/features/eventos-gestion/sesiones-evento/sesiones-evento.component.ts`, `generarQr`; utiliza el servicio cliente y `QRCode.toDataURL(qr.token, ...)`. |
| Participante / Flutter | `flutter/VidiaApp/lib/screens/attendance_sessions_screen.dart`, `_scan`/`_requiresGps`; `screens/attendance_scanner_screen.dart`, `_onCapture`/`_submit`; `services/asistencia_flow.dart`, `submit`; `repositories/asistencia_repository.dart`, `BackendAsistenciaRepository.register`; `services/api_service.dart`. |
| Escáner / ubicación | `MobileScanner` entrega `BarcodeCapture.rawValue`; `services/attendance_location_source.dart`, `GeolocatorAttendanceLocationSource.currentPosition`. Son mecanismos distintos agrupados, no un único servicio real. |
| API / seguridad | Java `core/seguridad/JwtAuthenticationFilter.java`, `JwtService`, `SecurityConfig`; `modulos/usuarios/servicios/SesionUsuarioServicio`; `modulos/asistencias/controladores/QrAsistenciaController.java` y `AsistenciaController.java`; manejador global de excepciones. |
| QrAsistenciaService | Java `modulos/asistencias/servicios/QrAsistenciaService.java`: `generar`, `resolverToken`, `validarVigencia`. |
| AsistenciaService | Java `modulos/asistencias/servicios/AsistenciaService.java`: `registrar`, `validarSesion`, `validarGps`, `validarRangos`, `haversine`. |
| Repositorios / PostgreSQL | `SesionEventoRepository`, `QrAsistenciaTemporalRepository`, `InscripcionRepository`, `AsistenciaRepository`; JPA/Hibernate y esquema PostgreSQL. No se representa SQL literal ni ejecución de un comando SQL por Flutter. |

### Emisión del QR

`QrAsistenciaController.generar`: POST `/api/v1/sesiones/{sesionId}/qr/generar`, ADMINISTRADOR u ORGANIZADOR, respuesta 201. El organizador solo opera sesiones de eventos propios; un recurso fuera de su alcance se trata como 404. El servicio bloquea la sesión, comprueba propiedad, sesión activa, requiereAsistencia, evento PUBLICADO y que ahora sea anterior al fin. No exige que la hora de inicio ya haya llegado para emitir el QR.

Se revocan los QR activos anteriores de la sesión. El token se genera con 32 bytes aleatorios y Base64URL; se persiste su hash SHA-256. Su expiración es el mínimo entre ahora+2 minutos y el fin de la sesión. El token se devuelve al emitir; la consulta GET del QR activo devuelve metadatos sin reexponerlo. La imagen la prepara Angular.

La figura muestra el camino autorizado de emisión. Sus rechazos 400/401/403/404 no se despliegan como operandos adicionales, para conservar legibilidad; esta simplificación no afirma que emisión siempre tenga éxito.

### Lectura, ubicación y autenticación

Flutter toma el primer código de formato QR con contenido no vacío. `_processing` y `_submitted` evitan envíos repetidos de la pantalla; no convierten el QR en token global de un solo uso. Se detiene el escáner durante el envío. Fallos de cámara, permisos, consentimiento o adquisición GPS se muestran localmente y no producen una respuesta HTTP de registro que no se haya solicitado.

`SesionEvento.requiereGps` es verdadero si hay latitud o longitud; `_requiresGps` también lo exige para modalidad distinta de VIRTUAL. Si aplica, Geolocator comprueba servicios, consulta/solicita permisos y obtiene ubicación con `LocationAccuracy.best`, límite de 20 s. El JSON incluye token QR y, cuando aplica, `latitud`, `longitud` y `precision`. El `Authorization: Bearer` corresponde al JWT de autenticación, no al token QR.

`JwtAuthenticationFilter` valida el JWT y `SesionUsuarioServicio.esSesionActiva`. Ausencia de credenciales conduce a 401 `AUTH_REQUIRED`; JWT inválido a `AUTH_INVALID_TOKEN`; sesión ausente/expirada/revocada a `AUTH_INVALID_SESSION`. La operación de asistencia exige rol USUARIO, con 403 para rol insuficiente. El DTO `RegistrarAsistenciaRequest` valida token y rangos de los campos GPS presentes; los campos de ubicación no son universalmente obligatorios en el DTO.

### Validación del servidor y registro

El orden real de `AsistenciaService.registrar` es:

1. Exigir USUARIO y obtener su identidad.
2. Resolver SHA-256 del token mediante `findByTokenHashForUpdate` y obtener su sesión.
3. Buscar inscripción del usuario para el evento de esa sesión; exigir CONFIRMADA y correspondencia usuario/evento.
4. Exigir evento PUBLICADO, sesión activa y que requiera asistencia; ahora dentro del intervalo inicio/fin.
5. Validar QR activo, no revocado, no expirado y no emitido en el futuro.
6. Validar GPS cuando corresponda.
7. Comprobar asistencia duplicada por inscripción/sesión.
8. Guardar mediante `saveAndFlush` una asistencia VALIDADA y devolver 201 / `AsistenciaResponse`.

Los fragmentos opcionales de la figura evitan presentar las validaciones posteriores como ejecutadas tras un rechazo previo. Los mensajes de validación local del servicio resumen varias comprobaciones; no constituyen llamadas nuevas del cliente. Las excepciones son devoluciones conceptuales hacia el manejo REST, no valores normales retornados por esos métodos Java.

En VIRTUAL sin coordenadas de sesión se omite GPS. En otros casos se exigen coordenadas y precisión válidas, precisión <=30 m, configuración geográfica de sesión válida y `Haversine + precision <= radioMetros`. Se redondea la distancia a escala 2. Esta regla usa datos declarados por el dispositivo; no prueba presencia física de forma absoluta ni detecta por sí sola una ubicación falsificada.

Los rechazos de negocio devuelven 400 con el código de la causa: `QR_INVALID`, `QR_REVOKED`, `QR_EXPIRED`, `INSCRIPTION_REQUIRED`, `INSCRIPTION_NOT_CONFIRMED`, `EVENT_NOT_PUBLISHED`, `ATTENDANCE_SESSION_INACTIVE`, `ATTENDANCE_SESSION_NOT_REQUIRED`, `ATTENDANCE_OUTSIDE_WINDOW`, `ATTENDANCE_GPS_INVALID`, `ATTENDANCE_GPS_ACCURACY_INVALID`, `ATTENDANCE_OUTSIDE_RADIUS`; configuración GPS incompleta usa `BUSINESS_RULE_VIOLATION`. Datos DTO inválidos devuelven 400 / `VALIDATION_ERROR`. Se agrupan causas dentro de un operando, sin afirmar que todas ocurran en una misma ejecución.

Duplicado detectado previamente: 409 / `ATTENDANCE_DUPLICATED`. Hay además unicidad física `(inscripcion_id, sesion_evento_id)`, declarada en `Asistencia.java` y V3. `saveAndFlush` captura `DataIntegrityViolationException` y la convierte también a ese conflicto. **Limitación:** la captura no discrimina qué restricción falló, por lo que puede etiquetar como duplicado otra violación de integridad. El diagrama refleja esa traducción, sin legitimarla como diagnóstico universal. La asistencia utiliza borrado lógico; la unicidad física no es parcial.

El QR permanece activo después de registrar asistencia: varios inscritos confirmados pueden usar el mismo QR de sesión mientras sea válido, pero cada inscripción/sesión admite una asistencia. No existe consumo global de token en este flujo ni un QR personal.

## 4. Pruebas existentes contrastadas

- `backend/src/test/java/bo/uajms/eventos/modulos/inscripciones/servicios/InscripcionEventoEligibilityTest.java`: estados del evento, inscripción requerida y cupo no limitado.
- `.../inscripciones/servicios/InscripcionPagoFlowTest.java`: inscripción gratuita/pago y creación del pago.
- `.../inscripciones/InscripcionPersistenceContractTest.java`: unicidad y anotaciones de bloqueo.
- `.../asistencias/servicios/QrAsistenciaServiceTest.java`: emisión, propiedad, expiración/revocación.
- `.../asistencias/servicios/AsistenciaRegistroTest.java`: inscripción, sesión, QR, precisión, distancia, duplicado y registro.
- `.../asistencias/AsistenciaPersistenceContractTest.java`: unicidad/índices y bloqueos de sesión/QR.
- `.../asistencias/controladores/AsistenciaApiSecurityContractTest.java`: contratos de seguridad de la API.
- `flutter/VidiaApp/test/attendance_scanner_screen_test.dart` y `asistencia_repository_test.dart`: pantalla de escaneo y envío del registro.

Estos archivos existen y se leyeron. No se presentan como suites recién ejecutadas ni como demostración de lectura física de cámara/GPS. Los reportes anteriores del Anexo B pertenecen a otra ejecución y no se mezclan con esta comprobación de diagramas.

## 5. Validación de Enterprise Architect

Se detectó EA Trial **17.2.1721.15**, API build **1721**, y un modelo abierto. Se inspeccionó ese modelo sin escribir en él; no se encontraron diagramas Sequence existentes. Todas las operaciones de ensayo se dirigieron a modelos nuevos en `%TEMP%/vidia_ea_xj4tqjxk/`.

La ayuda oficial confirma los tipos y colecciones empleados. Se comprobó directamente la persistencia de `IsReturn` e `IsAsync` mediante `Connector.Properties`, la creación de `InteractionFragment` con Subtype 0 (alt)/1 (opt) y sus Partitions, y la exportación auténtica mediante `Project.PutDiagramImageToFile`. Los operandos no exponen Update y se guardan actualizando su elemento.

La exportación inicial mostró que EA recoloca las flechas por orden y separación del modelo: las coordenadas explícitas y los huecos en SequenceNo no bastan. Se corrigió el cálculo de fragmentos para seguir las filas reales de mensajes. La configuración de separación del modelo debe revisarse al importar a otro modelo; el script no escribe ajustes globales ni metadatos internos mediante SQL.

El script conserva cualquier figura con el mismo nombre y no crea nuevos participantes/conectores para una figura omitida. Si falta modelo/paquete, o hay varios destinos, se detiene con diagnóstico. Ante fallo durante creación, los objetos parciales se conservan: no hay eliminación automática ni promesa de rollback de operaciones de EA. Un ensayo intermedio falló al preparar operandos con delimitadores; se normalizaron los puntos y coma en sus nombres/notas y se repitió la creación en otro modelo nuevo, sin borrar el ensayo fallido.

Otro ensayo devolvió `Cannot update connector as either the Start or the End object is NULL`. No se aisló la causa interna de EA. Se reforzó el script para conservar identificadores numéricos, comprobar que se asignaron y recuperar el emisor mediante `GetElementByID` antes de crear cada mensaje, en lugar de conservar referencias COM a elementos mientras se insertan fragmentos. La versión final se volvió a ejecutar en `entrega_validada.qea`, con creación, reejecución y exportación satisfactorias. Los modelos de ensayos fallidos se conservaron en la carpeta temporal.

### Resultados comprobados

| Comprobación | Resultado y alcance |
|---|---|
| Sintaxis del cuerpo JavaScript | `node --check` terminó con código 0. Se excluyó exclusivamente la directiva `!INC`, que pertenece al alojamiento de EA. No se usan API de Node en el entregable. |
| Referencias de participantes y operadores | Validación temporal: todas las referencias existen; no hay IDs de participantes repetidos; operadores alt/opt y ramas no vacías. |
| Colisión y selección | Comprobación temporal de colisión sin mutaciones, selección ausente y paquetes Anexo E ambiguos: resultados satisfactorios. Los objetos simulados de estas tres comprobaciones solo verifican los caminos de control; no sustituyen la prueba COM. |
| Creación real mediante EA COM | E.12: 5 líneas de vida, 24 mensajes y 4 fragmentos. E.13: 7 líneas de vida, 41 mensajes y 10 fragmentos. En total: 12 participantes, 65 conectores Sequence y 14 fragmentos combinados. |
| Retornos y sincronía | E.12: 15 retornos. E.13: 22 retornos y un callback asíncrono de escáner. Propiedades persistidas en el modelo real temporal. |
| Segunda ejecución real | Ambas figuras se omitieron por nombre existente. Los conteos permanecieron iguales; no se reemplazaron ni añadieron objetos propios de esas figuras. |
| Exportación | Las dos figuras se exportaron realmente desde EA a metafiles temporales, convertidos únicamente para inspección visual. No son imágenes inventadas ni se entregan como exportación definitiva. |
| Revisión visual | Se revisaron ambas figuras, participantes, flechas, retornos y operandos; se corrigieron la alineación vertical, los cruces entre límites de fragmentos y el margen del último foco de control. Sigue pendiente verificar tamaño de impresión y ajustes del modelo definitivo. |

La generación se ejecutó con **Windows Script Host/JScript y la API COM real** de EA 17.2 en un archivo `.qea` independiente. El cuerpo de funciones del entregable se utilizó sin sustituir la lógica de creación; el alojamiento de prueba proporciona `Repository` y `Session` y llama directamente a `ejecutarVidia`. La directiva y el botón Run del motor JavaScript integrado siguen pendientes de comprobación. Los archivos de ensayo y sus registros están en `%TEMP%/vidia_ea_xj4tqjxk/`; no son dependencias del script entregado.

Archivo de ensayo final: `entrega_validada.qea`; registro: `trace_entrega_validada.txt`. SHA-256 del JavaScript comprobado: `771aed9c4709ebe58dee9472dffc0a1ba67b2ae9e466ebee3366c7b6d732ed39`. Las exportaciones finales de ensayo se inspeccionaron visualmente; mantienen la marca Trial. Solo los tres archivos solicitados se incorporan al repositorio.

## 6. Pendientes y límites

- Confirmar el resultado en el motor JavaScript integrado y el modelo de destino del usuario; la comprobación COM utiliza un alojamiento externo de prueba.
- Revisar separación, guardas, activaciones y lectura al tamaño final de impresión. La marca de agua Trial impide considerar las exportaciones de ensayo como figuras académicas finales.
- Exportar las dos figuras definitivas desde una instalación apropiada. No se entregan imágenes simuladas.
- No se verificó el estado de restricciones, versión de código ni rendimiento concurrente de producción.
- No se realizó escaneo físico, comprobación de permisos en un teléfono ni registro GPS real.
- No se muestran todos los errores técnicos de persistencia, red, emisión QR o autenticación en E.12; las precondiciones y agrupaciones se documentaron expresamente.

Se crearon únicamente el script, este informe y el README en `scripts/enterprise-architect/`. No se modificaron aplicación, migraciones, monografía, configuración productiva ni el modelo UML original.
