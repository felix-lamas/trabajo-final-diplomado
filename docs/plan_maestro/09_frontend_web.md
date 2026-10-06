# 09. Aplicación Web Angular

## Capacidades presentes en el árbol de código

- Público/participante: catálogo, detalle, registro/login, verificación/recuperación, inscripción, mis inscripciones/pagos, carga de comprobante, perfil y certificados.
- Organizador: dashboard, eventos propios, formulario, envío a revisión, QR pago opcional, sesiones, inscritos, pagos de eventos propios, asistencia/consultas y certificados según endpoints.
- Administrador: solicitudes de organizador, categorías, revisión de eventos, pagos, asistencia/certificados y reportes según endpoints.
- Verificación pública de certificado con ruta sin guard.

## Criterios de interfaz

States loading/empty/error, responsive, navegación por teclado, labels/foco y estilos de Design System. La visibilidad de UI no sustituye autorización backend. No documentar un botón como capacidad si no existe endpoint/acción.

El participante puede usar vistas Web existentes; la arquitectura también prevé Flutter. La captura GPS de participante no pertenece a Web. Métricas de encuestas/satisfacción o QR utilizados devueltas como placeholder no deben mostrarse ni describirse como funcionales.

## Evidencia

E3.2 ejecutó `npm test -- --watch=false`: 271/271 PASS, y `npm run build`: PASS con warnings de presupuesto. No equivale a visual/E2E de todos los recorridos ni prueba productiva; E3.4 no ejecutó pruebas.
