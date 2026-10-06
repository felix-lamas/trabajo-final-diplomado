# 10. Aplicación Flutter de participante

Flutter 3.44.8 es canal del participante (`USUARIO`); no es app de Administrador u Organizador. E2 fue un incremento intermedio, no el alcance final.

| Función | Estado estático al corte E3.4 |
|---|---|
| Login/gestión de sesión segura | Presente |
| Catálogo/detalle de eventos publicados | Presente |
| Inscripción/mis inscripciones | Presente; falta completar cobertura de estados/error E2E |
| Registro/verificación/reset desde app | No localizado; decidir si cuenta se crea en Web o añadir onboarding móvil sin cambiar API |
| QR/instrucciones pago evento | No demostrado como flujo completo; debe verificarse/integrarse al completar pago |
| Pago externo/comprobante/mis pagos | Pendiente; detalle señala disponibilidad posterior |
| Escáner de QR temporal + ubicación del dispositivo | Pendiente; no se encontraron dependencias/código de cámara ni GPS |
| Mis asistencias | Pendiente |
| Mis certificados/descarga PDF | Pendiente |
| Historial de pagos/asistencias/certificados | Pendiente; usar endpoints separados existentes, no inventar endpoint agregado |

El backend valida token de asistencia, sesión, inscripción, ventana, GPS/precisión/radio y duplicidad. Flutter captura/solicita datos del dispositivo; no decide elegibilidad o acceso. No se afirma test Flutter aprobado ni APK/AAB distribuido a E3.4.
