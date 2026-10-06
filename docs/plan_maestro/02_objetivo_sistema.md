# 02. Objetivo y canales del sistema

## Objetivo general

Desarrollar una plataforma web y móvil para la gestión de eventos universitarios en la Universidad Autónoma Juan Misael Saracho, que permita centralizar la información y gestionar los procesos de inscripción, pago, asistencia y certificación de los participantes.

## Actores

- `ADMINISTRADOR`: administración y revisión global solo donde API lo autoriza.
- `ORGANIZADOR`: administración de eventos propios y operaciones de su ámbito.
- `USUARIO`: participante, consulta y recursos propios.
- Tercero verificador público: consulta código certificado sin cuenta; no es rol.

## Canales

- Web Angular: rutas públicas, capacidades de usuario actualmente disponibles, administración y organización.
- Flutter 3.44.8: cliente participante. Catálogo, detalle, login e inscripción/mis inscripciones presentes; pagos/comprobantes, asistencia QR/GPS y certificados pendientes.
- API Spring Boot bajo `/api/v1`: fuente de estados, reglas, autorización y persistencia.

El objetivo final incluye móvil participante; E2 y el estado parcial actual de Flutter no reducen ese alcance.
