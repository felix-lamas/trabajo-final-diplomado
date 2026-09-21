# 16. BITÁCORA DE CAMBIOS

| Fecha | Módulo | Tipo | Cambio | Pruebas | Estado |
|---|---|---|---|---|---|
| 20/09/2026 | Documentación | NUEVO | Creación del Plan Maestro de Implementación | Revisión documental | Completado |
| 21/09/2026 | Backend / Seguridad | ADAPTAR | Aislamiento de `DatosInicialesSeed` mediante el perfil explícito `demo`; actualización de documentación y DEC-011 | Prueba de perfil, compilación y arranque controlado | Completado |
| 21/09/2026 | Backend / Eventos / Seguridad | ADAPTAR | Corrección de autorización por propietario y estado en consultas y operaciones de eventos; publicación restringida a administración; registro de DEC-012 | Pruebas unitarias de acceso público, alcance por rol, ownership y ausencia de mutación en rechazos | Completado |
| 21/09/2026 | Backend / Inscripciones / Seguridad | ADAPTAR | Corrección de autorización por propietario y evento en consulta, listado y cancelación de inscripciones; reutilización de `UsuarioAutenticadoService` | Pruebas unitarias de aislamiento entre usuarios y organizadores, alcance administrativo y ausencia de efectos secundarios | Completado |
| 21/09/2026 | Backend / Pagos / Seguridad | ADAPTAR | Corrección de ownership e IDOR/BOLA en pagos, comprobantes, reportes y métricas financieras; autorización previa al almacenamiento y scope por usuario u organizador | Pruebas unitarias de acceso cruzado, estados de comprobante, ausencia de mutación/storage en rechazos, reportes y dashboards acotados | Completado |

## Tipos
CONSERVAR · ADAPTAR · COMPLETAR · REEMPLAZAR · ELIMINAR · NUEVO
