# 8. Estrategias Especializadas

## 8.1. Estrategia de Auditoría (Traceability)
Se utilizará una tabla centralizada de auditoría llamada `audit_logs` con la siguiente estructura:
*   `id`: PK UUID.
*   `table_name`: Tabla afectada.
*   `record_id`: ID del registro afectado.
*   `action`: (INSERT, UPDATE, DELETE).
*   `old_data`: `JSONB` con los valores anteriores.
*   `new_data`: `JSONB` con los nuevos valores.
*   `user_id`: Quién realizó el cambio.
*   `timestamp`: Cuándo ocurrió.

## 8.2. Estrategia de Soft Delete
Para evitar la pérdida accidental de datos históricos y financieros:
*   Se añade la columna `deleted_at` (TIMESTAMPTZ) a las tablas principales.
*   Una fila se considera "activa" si `deleted_at` es NULL.
*   Las consultas del sistema deberán filtrar automáticamente por este campo (Global Filter en Spring Data JPA / Hibernate).

## 8.3. Estrategia de Datos Históricos
*   **Certificados:** Una vez generados, el PDF se almacena en un Object Storage (ej. MinIO/S3). La base de datos solo guarda el `file_url` y el `verification_uuid`.
*   **Archivado:** Eventos con más de 5 años de antigüedad y sus asistencias podrán ser movidos a tablas históricas (`events_history`) para mantener la tabla activa con alto rendimiento.
