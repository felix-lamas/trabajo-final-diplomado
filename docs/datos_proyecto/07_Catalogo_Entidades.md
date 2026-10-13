# 1. Catálogo Completo de Entidades

A continuación se describen las entidades identificadas para el sistema, agrupadas por su dominio lógico.

## Módulo: Organización Académica
*   **Facultad:** Representa las unidades académicas mayores de la UAJMS. Es el nivel más alto de organización de eventos.
*   **Carrera:** Representa las divisiones académicas dentro de una facultad.

## Módulo: Seguridad y Usuarios
*   **Usuario:** Entidad central que representa a cualquier persona autenticada (Estudiantes, Docentes, Admin).
*   **Rol:** Define un conjunto de responsabilidades dentro del sistema (ej. ORGANIZADOR, VALIDADOR).
*   **Permiso:** Acciones atómicas permitidas (ej. "event:create", "payment:validate").

## Módulo: Gestión de Eventos
*   **CategoriaEvento:** Clasificación de los eventos (Taller, Seminario, etc.).
*   **Evento:** La entidad core que contiene la información logística, de costos y cupos.

## Módulo: Inscripciones y Pagos
*   **Inscripcion:** Relación entre un Usuario y un Evento. Gestiona el ciclo de vida del participante en el evento.
*   **MetodoPago:** Catálogo de formas de pago aceptadas (Transferencia, Depósito).
*   **Pago:** Registro de la transacción financiera, vincula la inscripción con el comprobante y su validación.

## Módulo: Control de Asistencia y Certificados
*   **Asistencia:** Registro de cada marca de ingreso/salida de un participante mediante escaneo.
*   **Certificado:** Documento digital generado tras cumplir requisitos de asistencia.

## Módulo: Soporte y Auditoría
*   **Notificacion:** Registro de mensajes enviados a los usuarios.
*   **Auditoria:** Registro de cambios (logs) en las tablas críticas para trazabilidad administrativa.
