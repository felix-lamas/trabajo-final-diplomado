# Casos de uso reconciliados

Actores/roles técnicos: `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO`. “Participante” es nombre funcional de USUARIO. El verificador público es externo sin cuenta. Los casos marcados **Flutter pendiente** son alcance acordado, no funcionalidad ya entregada.

| Caso | Actor principal | Resumen / estado de canal |
|---|---|---|
| CU-01 Registro y acceso seguro | USUARIO | Registro, verificación de correo, login, recuperación/reset y sesión. Web implementada; en Flutter login existe, onboarding completo no localizado. |
| CU-02 Solicitar condición de organizador | USUARIO, ADMINISTRADOR | Usuario solicita; administrador revisa/aprueba/rechaza. Web/API. |
| CU-03 Gestionar evento y QR de pago | ORGANIZADOR | Crear BORRADOR propio, configurar evento, opcionalmente cargar/reemplazar/eliminar imagen QR de pago permitida y enviar revisión. Web/API. |
| CU-04 Revisar evento | ADMINISTRADOR | Consultar EN_REVISION, publicar o rechazar; backend confirma transición. Web/API. |
| CU-05 Consultar eventos | USUARIO/público | Catálogo, búsqueda/filtros actuales, detalle, información de pago/QR cuando existe. Web y Flutter (QR/pago móvil pendiente). |
| CU-06 Inscribirse | USUARIO | Validar publicación, duplicidad y cupo; gratuito confirma; pagado inicia pago. Web/API; Flutter existe para inscripción y su estado debe probarse. |
| CU-07 Pagar externamente y presentar comprobante | USUARIO, ORGANIZADOR, ADMINISTRADOR | Participante paga fuera de Vidia y carga comprobante privado; revisor de alcance lo aprueba/rechaza. Web/API; Flutter pendiente. No gateway. |
| CU-08 Registrar asistencia por sesión | ORGANIZADOR, USUARIO | Organizador configura sesión/emite QR; participante Flutter escanea y entrega GPS; backend valida y registra. Cliente móvil pendiente; administración/consulta web parcial. |
| CU-09 Generar/consultar certificado | ORGANIZADOR, ADMINISTRADOR, USUARIO | Generación autorizada y elegibilidad backend; titular/roles consultan y descargan PDF. Verificación pública separada. Web/API; Flutter pendiente. |
| CU-10 Consultar historial propio | USUARIO | Consulta recursos propios por inscripciones, pagos, asistencias y certificados mediante módulos/endpoints existentes. No se presupone endpoint agregado; Flutter parcial. |
| CU-11 Gestionar categorías | ADMINISTRADOR | Crear/editar/eliminar sujeto a restricciones backend. Web/API. |
| CU-12 Consultar dashboards/reportes | ADMINISTRADOR, ORGANIZADOR | Métricas reales en alcance y reportes/exportaciones disponibles. No incluir campos placeholder. Web/API. |
| CU-13 Verificar certificado | Tercero público | Consulta código en endpoint/ruta pública sin JWT y recibe información pública autorizada. Web/API. |

## Flujo de pago de evento pagado

1. ORGANIZADOR define precio e instrucciones; puede cargar una imagen QR proporcionada por él. QR es opcional y no es un gateway.
2. USUARIO consulta el evento y se inscribe. Backend crea pago con monto fuente de verdad.
3. USUARIO paga fuera de Vidia siguiendo las instrucciones y carga el comprobante por endpoint autenticado.
4. Backend valida rol, propiedad, estado, archivo y Storage antes de aceptar.
5. Pago queda pendiente de revisión. ADMINISTRADOR o ORGANIZADOR propietario resuelve conforme a permiso.
6. Flutter todavía no implementa el flujo móvil de pago/comprobante.

## Flujo de asistencia

1. ORGANIZADOR configura sesión y obtiene/rota QR temporal.
2. USUARIO abre Flutter (captura aún pendiente), escanea y acepta solicitud de ubicación.
3. Backend valida token temporal, sesión/ventana, inscripción, radio/precisión y duplicado.
4. El QR compartido no se consume globalmente; la unicidad se aplica por inscripción y sesión.
