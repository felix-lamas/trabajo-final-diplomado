# Reglas de negocio y estados vigentes

Este documento refleja reglas de backend inspeccionadas; el backend valida de forma definitiva. Los nombres técnicos se mantienen exactamente como contrato. “Participante” representa al rol `USUARIO`.

## Identidad y permisos

- Registro público asigna `USUARIO`; no hay autoasignación de ORGANIZADOR ni ADMINISTRADOR.
- Solo ADMINISTRADOR resuelve solicitud de organizador.
- ADMINISTRADOR opera globalmente donde lo autorizan los endpoints; ORGANIZADOR solo sobre recursos/eventos propios; USUARIO opera sobre recursos propios.
- Recurso fuera de ownership puede responder 404 para no revelar existencia; rol insuficiente responde 403; falta/invalidación de autenticación responde 401.

## Estados

- Evento: `BORRADOR`, `EN_REVISION`, `PUBLICADO`, `FINALIZADO`, `RECHAZADO`, `CANCELADO`.
- Inscripción: `PENDIENTE_PAGO`, `PENDIENTE_VALIDACION`, `CONFIRMADA`, `CANCELADA`.
- Pago: `PENDIENTE_PAGO`, `PENDIENTE_VALIDACION`, `APROBADO`, `RECHAZADO`.
- Los estados adicionales de certificado se documentan solo conforme a enum/DTO actuales, no por analogía.

## Evento

- Evento se asocia al organizador autenticado; ORGANIZADOR crea BORRADOR y gestiona solo sus propios eventos en estados permitidos.
- Envío a revisión transiciona a `EN_REVISION`; publicación/rechazo corresponden a ADMINISTRADOR. Rechazo, cancelación y finalización siguen endpoints/estados backend.
- Solo eventos `PUBLICADO` son visibles en consulta pública y admiten nueva inscripción/asistencia según la operación.
- Modalidad vigente: Presencial o Virtual.
- QR de pago opcional y distinto del pago/comprobante: archivo imagen PNG/JPG/JPEG de hasta 5 MB, clave privada generada por backend, bucket privado; se administra para evento pagado en estados `BORRADOR` o `RECHAZADO`. No se exige QR para crear evento pagado. El backend sirve la imagen; no se expone key ni URL directa de Supabase.
- `imagenPortada` permanece URL HTTP(S); no forma parte de la carga QR ni almacenamiento nuevo.

## Inscripción y pago

- Un usuario no puede generar una inscripción duplicada al mismo evento conforme a unicidad/estado y disponibilidad.
- Para evento gratuito, inscripción elegible queda `CONFIRMADA` sin crear `Pago`.
- Para evento de pago, el backend usa costo de evento, crea pago único en `PENDIENTE_PAGO`; el usuario transfiere/deposita externamente y presenta comprobante.
- Comprobante del participante es privado, validado antes de almacenar, y nunca se sirve desde URL pública de Storage.
- Carga válida cambia pago e inscripción a `PENDIENTE_VALIDACION`. ADMINISTRADOR puede revisar globalmente; ORGANIZADOR solo pagos de eventos propios.
- Aprobación produce pago `APROBADO` e inscripción `CONFIRMADA`; rechazo produce `RECHAZADO` y permite reenvío solo conforme a regla/estado backend.
- No existe checkout ni pasarela.

## QR de asistencia y asistencia

- QR de asistencia es un token temporal asociado a `SesionEvento`; el token plano no se persiste.
- Token tiene vigencia limitada (actualmente dos minutos) y puede ser compartido por participantes de la sesión; el uso se limita por asistencia única de inscripción+sesión. No afirmar consumo global de un solo uso.
- Backend valida usuario `USUARIO`, inscripción `CONFIRMADA`, sesión/evento/ventana activa, vigencia del token, ubicación y precisión cuando corresponde, radio configurado y no duplicidad.
- Límite de precisión reportado por backend: 30 m. Para sesión presencial se compara `distancia + precisión` con radio; las coordenadas exactas del participante no se persisten en la entidad de asistencia observada.
- Flutter debe capturar cámara/ubicación; esa integración cliente sigue pendiente. Angular del organizador no sustituye captura del participante.

## Certificados

- Generación requiere evento `FINALIZADO`, inscripción `CONFIRMADA` y pago `APROBADO` si es pagado, además de configuración de certificado y criterio de asistencia.
- Curricular exige al menos 80 % de sesiones con asistencia requerida; horas vienen del evento. No curricular usa la regla de asistencia requerida del backend.
- Generación se inicia mediante operación autorizada; PDF se prepara bajo descarga. No hay emisión masiva automática al cambiar evento a FINALIZADO.
- Verificación pública recibe código backend y expone DTO público acotado; no requiere login.
