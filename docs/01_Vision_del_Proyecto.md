# Visión del proyecto Vidia

## Contexto y problema

La monografía vigente describe que la información y los procedimientos de difusión, inscripción, asistencia y certificación de eventos universitarios UAJMS pueden distribuirse entre distintos canales. Esto dificulta que el participante encuentre información centralizada y que responsables y participantes sigan la participación a través de las etapas del evento. Esta descripción debe respaldarse en la entrega final con los resultados de la investigación exploratoria anunciada en la monografía; este documento no los sustituye ni afirma que una encuesta ya concluyó.

## Propuesta

Vidia es una plataforma web y móvil para centralizar información y gestionar eventos universitarios UAJMS, con procesos de inscripción, pagos externos mediante comprobante, asistencia por sesión y certificación cuando corresponda. La solución integra un backend común para Web y Flutter. La experiencia móvil del participante está parcialmente implementada: pagos/comprobantes, asistencia QR/GPS y certificados quedan pendientes en Flutter, aunque existan capacidades backend y web.

## Usuarios y canales

- Roles funcionales: `ADMINISTRADOR`, `ORGANIZADOR` y `USUARIO`.
- “Participante” describe funcionalmente al `USUARIO`; no es autoridad/rol separado.
- Administrador y Organizador operan desde Web Angular.
- Usuario dispone de Web donde las vistas existen y de la aplicación Flutter participante; la cobertura de Flutter aún se completa.
- Un tercero puede consultar públicamente un certificado por código sin cuenta ni rol.

## Límites de la propuesta

No se ofrece gateway/checkout ni pago electrónico con tarjeta. El pago se realiza externamente y Vidia procesa la presentación y revisión del comprobante. La imagen QR de pago opcional pertenece al evento; no es el comprobante del participante ni el QR temporal de asistencia.

La integración futura con SIA u otros sistemas académicos/financieros es una posibilidad, no una capacidad implementada ni parte de la evidencia actual.
