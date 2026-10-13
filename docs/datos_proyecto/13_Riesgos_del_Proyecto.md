# 13. Riesgos del Proyecto

Se identifican los siguientes riesgos y sus respectivas estrategias de mitigación:

## 13.1. Riesgos Técnicos
*   **R-T1 Intermitencia de Conexión en el Evento:** Si el lugar del evento tiene mala señal, el escaneo de QR podría fallar.
    *   *Mitigación:* Implementar un modo de "registro manual" rápido y optimizar la carga de la aplicación web para conexiones lentas.
*   **R-T2 Sobrecarga en la Apertura de Inscripciones:** Eventos muy populares pueden causar picos de tráfico.
    *   *Mitigación:* Optimización de consultas SQL y uso de caché en el catálogo de eventos.
*   **R-T3 Falsificación de QRs:** Intentos de ingreso con QRs generados externamente.
    *   *Mitigación:* El QR contendrá un token firmado digitalmente por el backend que solo la aplicación puede validar.

## 13.2. Riesgos de Negocio / Operativos
*   **R-O1 Resistencia al Cambio:** Personal universitario acostumbrado al registro en papel.
    *   *Mitigación:* Capacitación previa y designación de "evangelizadores tecnológicos" en cada facultad.
*   **R-O2 Demora en Validación de Pagos:** Usuarios que cargan comprobantes falsos o ilegibles.
    *   *Mitigación:* Implementar un flujo claro de "Rechazo con motivo" para que el usuario pueda corregir su carga rápidamente.

## 13.3. Riesgos de Seguridad
*   **R-S1 Exposición de Datos Personales:** Acceso no autorizado a la base de datos de inscritos.
    *   *Mitigación:* Uso estricto de Spring Security, JWT y cifrado de datos sensibles.
