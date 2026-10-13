# 16. Módulos del Sistema

El sistema se divide en los siguientes módulos lógicos:

## 1. Módulo de Seguridad (security-module)
*   Responsabilidad: Autenticación, Autorización, Gestión de Roles y Permisos.
*   Tecnologías: Spring Security, JWT.

## 2. Módulo de Usuarios (user-module)
*   Responsabilidad: CRUD de usuarios, gestión de perfiles de participantes internos y externos.

## 3. Módulo de Eventos (event-module)
*   Responsabilidad: Gestión de categorías, creación de eventos, control de cupos y publicación.

## 4. Módulo de Inscripciones y Pagos (billing-module)
*   Responsabilidad: Flujo de inscripción, carga de comprobantes, validación administrativa y estados de pago.

## 5. Módulo de Asistencia (attendance-module)
*   Responsabilidad: Generación de QRs únicos, motor de escaneo y registro de entrada/salida.

## 6. Módulo de Certificación (certificate-module)
*   Responsabilidad: Plantillas de certificados, generación de PDFs y portal de verificación UUID.

## 7. Módulo de Reportes (analytics-module)
*   Responsabilidad: Consolidación de datos para dashboards y exportación de reportes.
