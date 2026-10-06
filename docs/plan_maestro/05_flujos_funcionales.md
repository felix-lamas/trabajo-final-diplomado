# 05. Flujos funcionales vigentes

## Acceso y organizador

Registro → rol inicial `USUARIO` → verificación de correo → login. Usuario puede solicitar condición de organizador; ADMINISTRADOR resuelve, y la aprobación concede `ORGANIZADOR`. Recuperación/reset y logout siguen servicios de correo/sesión.

## Evento

`ORGANIZADOR aprobado → BORRADOR → EN_REVISION → ADMINISTRADOR → PUBLICADO | RECHAZADO`.
Evento rechazado puede corregirse y reenviarse según reglas. Evento publicado puede finalizarse o cancelarse si backend lo permite. ORGANIZADOR solo opera sus eventos.

## QR pago del evento

Evento de pago → organizador configura monto e instrucciones → opcionalmente sube imagen QR PNG/JPG/JPEG (≤5 MB) en estados permitidos → el backend guarda objeto privado y proporciona endpoint Vidia para lectura. No se genera QR automáticamente.

## Inscripción/pago

- Gratuito: inscripción elegible → `CONFIRMADA`.
- Pagado: inscripción + pago backend `PENDIENTE_PAGO` → pago externo mediante instrucciones/QR opcional → participante carga comprobante privado → `PENDIENTE_VALIDACION` → organizador propietario/administrador revisa → `APROBADO` o `RECHAZADO` según operación y posible reenvío del comprobante.
- No existe gateway ni checkout.
- Flujo móvil Flutter de pago/comprobante pendiente.

## Asistencia

Organizador configura `SesionEvento` y emite/rota token QR temporal. USUARIO escanea desde Flutter previsto, acepta ubicación y envía token/GPS; backend valida inscripción confirmada, sesión y ventana, token vigente, precisión/radio y no duplicidad. QR es compartido por la sesión, no de un solo uso global. Captura Flutter pendiente; UI web sirve a gestión/consulta del organizador.

## Certificación e historial

Evento finalizado + inscripción confirmada + pago aprobado si aplica + elegibilidad asistencia → operación autorizada genera certificado; descarga produce PDF y código verifica por ruta pública. Curricular requiere 80 %; cliente no calcula elegibilidad. Flutter certificado e historial completo pendientes. Historial usa recursos por módulo, no endpoint agregado inventado.
