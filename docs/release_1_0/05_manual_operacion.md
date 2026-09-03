# Manual de Operacion

## 1. Flujo de registro
1. El usuario completa sus datos.
2. El sistema valida correo, CI y contrasena.
3. Se crea la cuenta y se asigna el rol correspondiente.
4. El usuario accede al sistema con JWT.

## 2. Flujo de eventos
1. El administrador u organizador crea el evento.
2. El evento pasa por estado borrador.
3. Se publica cuando la informacion esta lista.
4. El evento puede cerrarse o finalizarse segun el ciclo operativo.

## 3. Flujo de inscripciones
1. El participante selecciona un evento.
2. El sistema valida cupos y reglas del evento.
3. Se registra la inscripcion.
4. En eventos de pago, la inscripcion queda pendiente hasta validacion.

## 4. Flujo de pagos
1. El participante registra su pago.
2. Sube el comprobante.
3. El validador revisa el documento.
4. El pago se aprueba o rechaza.

## 5. Flujo QR
1. El sistema genera el codigo QR para acceso.
2. El participante presenta el QR en control.
3. Se valida la autenticidad del codigo.
4. Se registra ingreso o egreso segun el punto de control.

## 6. Flujo de control de acceso
1. Personal de control revisa la credencial o QR.
2. Se confirma que el participante este habilitado.
3. Se registra la asistencia.
4. Se almacena la observacion si aplica.

## 7. Flujo de certificados
1. El evento finaliza.
2. Se verifican condiciones de asistencia minima.
3. El sistema genera el certificado.
4. El participante descarga el PDF.
