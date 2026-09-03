# 5. Convenciones de Base de Datos

Para garantizar la mantenibilidad y profesionalismo del esquema PostgreSQL, se aplicarán las siguientes convenciones:

## 5.1. Nomenclatura
*   **Tablas:** Minúsculas, plural, snake_case (ej. `event_categories`, `users`).
*   **Columnas:** Minúsculas, singular, snake_case (ej. `first_name`, `start_date`).
*   **Claves Primarias (PK):** Siempre se llamarán `id`.
*   **Claves Foráneas (FK):** Nombre de la tabla singular + `_id` (ej. `user_id`, `event_id`).

## 5.2. Tipos de Datos PostgreSQL
*   **Identificadores (PK/FK):** `UUID` (v4) para evitar la predictibilidad de IDs y facilitar migraciones.
*   **Textos largos:** `TEXT` (PostgreSQL maneja eficientemente el almacenamiento TOAST).
*   **Textos cortos:** `VARCHAR(n)` con límites lógicos.
*   **Fechas:** `TIMESTAMPTZ` (Timestamp con zona horaria) para evitar conflictos de hora.
*   **Precios/Montos:** `NUMERIC(12,2)` para precisión exacta en moneda boliviana.
*   **Booleanos:** `BOOLEAN` con valores por defecto.

## 5.3. Auditoría Estándar
Todas las tablas de negocio deben contener los siguientes campos obligatorios:
*   `created_at`: Fecha de creación.
*   `updated_at`: Fecha de última modificación.
*   `created_by`: UUID del usuario que creó el registro (nulo para registro público).
*   `deleted_at`: Fecha de borrado (para Soft Delete).

## 5.4. Restricciones e Índices
*   **FK:** Todas las claves foráneas deben tener un índice para optimizar JOINs.
*   **Índices:** Se usará el prefijo `idx_` seguido del nombre de la tabla y campo (ej. `idx_users_email`).
*   **Unique:** Prefijo `uk_` (ej. `uk_users_dni`).
