# 9. Reglas de Negocio

Estas reglas rigen la lógica de operación de la plataforma:

## 9.1. Reglas de Inscripción
*   **RN-01 Unicidad:** Un usuario no puede inscribirse dos veces al mismo evento.
*   **RN-02 Pre-requisito de Pago:** En eventos con costo, el QR de acceso y la credencial no se habilitarán hasta que el pago sea validado manualmente por el Validador Financiero.
*   **RN-03 Límite de Cupos:** Una vez alcanzado el cupo máximo, el botón de inscripción se deshabilitará y el evento se mostrará como "Agotado".

## 9.2. Reglas de Asistencia
*   **RN-04 Registro Único de Entrada:** No se puede marcar entrada dos veces para el mismo bloque horario del evento.
*   **RN-05 Tolerancia de Ingreso:** El sistema permitirá el registro de asistencia desde 30 minutos antes del inicio del evento hasta el final del mismo.

## 9.3. Reglas de Certificación
*   **RN-06 Asistencia Mínima:** Para ser acreedor a un certificado, el participante debe tener registrado al menos el 80% de las asistencias (o lo que defina el organizador al crear el evento).
*   **RN-07 Cierre de Evento:** Los certificados solo podrán ser descargados una vez que el Coordinador marque el evento como "Finalizado" y "Certificados Liberados".
*   **RN-08 Inmutabilidad del Certificado:** Una vez generado, los datos del certificado (Nombre, Fecha, Carga Horaria) no pueden ser modificados. Si hay un error, el administrador debe anular y regenerar.

## 9.4. Reglas de Usuario
*   **RN-09 Correo Institucional:** Los usuarios que se registren con el dominio `@uajms.edu.bo` serán categorizados automáticamente como "Participantes Internos".
