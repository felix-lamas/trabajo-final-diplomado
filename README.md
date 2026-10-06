# Vidia — Gestión de eventos universitarios UAJMS

Vidia centraliza consulta y gestión de eventos universitarios mediante una API backend común, aplicación Web y aplicación Flutter para participantes. El sistema utiliza roles `ADMINISTRADOR`, `ORGANIZADOR` y `USUARIO`.

> **Estado del producto al corte documental E3.4:** backend y Web cubren gran parte de los flujos. Flutter ya incluye autenticación básica, catálogo/detalle e inscripción/mis inscripciones; pagos/comprobantes, captura de asistencia QR/GPS y certificados están pendientes. No se afirma que el despliegue productivo se haya verificado en esta fase. Para prioridades y detalle ver `docs/07_Requerimientos_Funcionales.md` y `docs/12_Matriz_de_Trazabilidad.md`.

## Arquitectura

- Backend: Java 21, Spring Boot 3.3, Spring Security/JWT, API REST `/api/v1`, arquitectura de monolito modular.
- Web: Angular 21.
- Móvil: Flutter 3.44.8 (cliente participante; brechas detalladas arriba).
- Persistencia: PostgreSQL con Spring Data JPA.
- Correo: SMTP mediante Brevo.
- Archivos privados: Supabase Storage S3-compatible, accedido únicamente por backend. Supabase no presta autenticación ni reemplaza PostgreSQL.
- No existe gateway de pago: los pagos son externos y el participante presenta comprobante.

## Requisitos previos

- Java/JDK 21 y Maven.
- Node.js compatible con Angular 21 y npm.
- PostgreSQL 16.
- Flutter SDK/Dart compatibles con `flutter/VidiaApp/pubspec.yaml` para el cliente móvil.
- Docker Compose es opcional para servicios locales si se usa `docker/docker-compose.yml`.

## Configuración local

1. Copiar `.env.example` a `.env` y completar valores locales. El `.env` es secreto y no se versiona.
2. Iniciar PostgreSQL local o el servicio definido en Docker Compose.
3. Iniciar backend desde `backend/` con `mvn spring-boot:run`.
4. Iniciar Web desde `frontend/` con `npm install` y `npm start` (consultar scripts reales de `frontend/package.json`).
5. Para Flutter consultar `flutter/VidiaApp/README.md`; configurar la API base de desarrollo mediante `--dart-define=API_BASE_URL=...`.

Swagger local se publica bajo `/api/v1/swagger-ui.html` según configuración. URLs públicas, CORS productivo, base productiva, correo/Storage en hosting y distribución instalable Flutter requieren verificación del entorno; esta guía no certifica su disponibilidad.

## Documentación

- `docs/07_Requerimientos_Funcionales.md`: RF y CA reconciliados, pendientes de incorporarse formalmente a la monografía en E3.5.
- `docs/08_Requerimientos_No_Funcionales.md`: métricas normativas conservadas y evidencia pendiente.
- `docs/12_Matriz_de_Trazabilidad.md`: estado backend/Web/Flutter sin confundirlo con producción.
- `docs/auditoria/E3_3_RECONCILIACION_DOCUMENTO_SISTEMA.md`: decisiones y brechas detectadas.
- `docs/auditoria/E3_4_ALINEACION_DOCUMENTAL.md`: cambios realizados en esta alineación.
- `docs/release_1_0/`, `docs/design/` y documentos Postman señalados como históricos no son contratos vigentes sin reconciliación adicional.
