# 6. Índices y Optimización

Para asegurar el rendimiento de la plataforma bajo carga, se definen los siguientes índices estratégicos:

## 6.1. Índices de Búsqueda Frecuente (B-Tree)
*   `idx_users_email`: Búsqueda de usuario por correo institucional (Autenticación).
*   `idx_users_dni`: Búsqueda de participante por documento (Contingencia en control de acceso).
*   `idx_events_status_dates`: Índice compuesto sobre `(status, start_date)` para el catálogo público de eventos activos.

## 6.2. Índices de Integridad (FKs)
*   `idx_inscriptions_event_id`: Crucial para listar participantes de un evento.
*   `idx_payments_inscription_id`: Acceso rápido al estado financiero de una inscripción.
*   `idx_attendance_inscription_id`: Cálculo rápido de porcentaje de asistencia.

## 6.3. Índices de Auditoría y Reportes
*   `idx_audit_logs_timestamp`: Ordenamiento cronológico de logs.
*   `idx_events_faculty_id`: Agrupación de eventos por unidad académica para reportes estadísticos.

# 7. Restricciones de Integridad (Constraints)

*   **CHK_event_dates:** `CHECK (end_date > start_date)`. Asegura coherencia temporal.
*   **CHK_event_cost:** `CHECK (cost >= 0)`. Evita costos negativos.
*   **CHK_event_capacity:** `CHECK (capacity > 0)`. Un evento debe permitir al menos un participante.
*   **UK_inscription_user_event:** `UNIQUE (user_id, event_id)`. Regla de negocio: Un usuario solo puede inscribirse una vez al mismo evento.
*   **UK_certificate_verification:** `UNIQUE (verification_code)`. Garantiza la unicidad del UUID de validación pública.
