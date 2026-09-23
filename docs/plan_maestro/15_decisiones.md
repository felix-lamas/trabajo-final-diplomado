# 15. DECISIONES

| ID | Fecha | Decisión | Estado |
|---|---|---|---|
| DEC-001 | 20/09/2026 | Mantener monolito modular profesional | Vigente |
| DEC-002 | 20/09/2026 | Java 21 + Spring Boot 3.3.0 | Vigente |
| DEC-003 | 20/09/2026 | Angular 21 para web | Vigente |
| DEC-004 | 20/09/2026 | Flutter para Usuario | Vigente |
| DEC-005 | 20/09/2026 | Exactamente tres roles | Vigente |
| DEC-006 | 20/09/2026 | Web para Administrador y Organizador | Vigente |
| DEC-007 | 20/09/2026 | Móvil para Usuario | Vigente |
| DEC-008 | 20/09/2026 | Asistencia QR + GPS | Vigente |
| DEC-009 | 20/09/2026 | QR temporal y de un solo uso | Vigente |
| DEC-010 | 20/09/2026 | Sin pasarela de pago | Vigente |
| DEC-011 | 21/09/2026 | Los datos demo/semilla no deberán ejecutarse automáticamente en todos los perfiles. Se aislarán mediante configuración/perfil y se habilitarán únicamente de forma explícita en entornos controlados. | Vigente |
| DEC-012 | 21/09/2026 | La autorización de eventos se aplicará en el backend por rol, propietario y estado. El administrador tendrá alcance global; el organizador solo gestionará eventos propios; participantes y público solo consultarán eventos publicados. Los recursos fuera de alcance responderán `404` y la publicación quedará reservada al administrador mientras se implementa el flujo `EN_REVISIÓN`. | Vigente |
| DEC-014 | 22/09/2026 | El modelo funcional del backend queda limitado a `Usuario`, `Evento`, `CategoriaEvento`, `Inscripcion`, `Pago`, `SesionEvento`, `Asistencia` y `Certificado`, con los roles funcionales `ADMINISTRADOR`, `ORGANIZADOR` y `USUARIO`. Las entidades RBAC, recuperación y auditoría base se conservan como infraestructura técnica y no como entidades conceptuales. | Vigente |
| DEC-015 | 22/09/2026 | La solicitud para convertirse en organizador se representará dentro de `Usuario`, sin crear una entidad conceptual adicional. Solo `USUARIO` puede solicitar; solo `ADMINISTRADOR` puede resolver; la aprobación sustituye el rol funcional por `ORGANIZADOR` y la solicitud conserva estado, fechas, motivo y administrador resolutor. | Vigente |
