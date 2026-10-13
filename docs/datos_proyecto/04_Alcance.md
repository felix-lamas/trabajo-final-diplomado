# Alcance funcional de Vidia

## Identidad y roles

Registro, verificación de correo, login, recuperación/restablecimiento de contraseña, perfil y control de acceso backend por `ADMINISTRADOR`, `ORGANIZADOR` y `USUARIO`. La condición de Organizador se obtiene mediante solicitud y resolución administrativa.

## Gestión y consulta de eventos

El organizador autorizado crea eventos propios y los envía a revisión. El administrador revisa, publica o rechaza según estados backend. El catálogo público permite buscar eventos publicados y consultar su detalle. Las modalidades funcionales actuales son Presencial y Virtual; no se declara modalidad Híbrida.

Para eventos pagados se puede configurar monto e instrucciones. La imagen QR de pago es opcional, la proporciona el organizador y se guarda en almacenamiento privado accedido mediante el backend. No es un QR generado por Vidia.

## Inscripciones, pagos y comprobantes

La inscripción gratuita puede confirmarse según reglas backend. En eventos pagados se crea el pago conforme al monto real del evento; el participante realiza el pago fuera de Vidia, presenta comprobante y el organizador propietario o administrador lo revisa según permisos. Se admite reemplazo/reenvío únicamente de acuerdo con el estado vigente. No existe gateway ni checkout.

## Sesiones y asistencia

El organizador configura sesiones y puede emitir/rotar un QR temporal asociado a la sesión. La operación de asistencia del participante requiere cliente móvil Flutter con escaneo y ubicación; el backend valida inscripción, sesión/ventana, token, GPS/precisión/radio y duplicidad. El cliente móvil de captura todavía está pendiente; la web del organizador permite consulta/gestión, no simula GPS del participante.

## Certificados, historial y reportes

El backend controla elegibilidad de certificados, código verificable, descarga PDF y consulta pública. El tipo curricular usa el umbral de asistencia implementado de 80 %; la elegibilidad no se calcula en el cliente. Flutter aún no implementa consulta/descarga de certificados. Hay consultas de historial por módulos/recursos propios; no se declara un endpoint agregado. Dashboard y exportaciones cubren solo las métricas/datos realmente calculados.

## Canales

- Web Angular: funciones públicas, de usuario existentes, administración y organización.
- Flutter: aplicación de participante. Catálogo, detalle, login, inscripción y mis inscripciones están presentes; RF-07 pagos/comprobantes, RF-08 asistencia QR/GPS y RF-09 certificados quedan pendientes de implementación móvil.
- Backend Spring Boot: contratos, autorización, validaciones y estados canónicos.
