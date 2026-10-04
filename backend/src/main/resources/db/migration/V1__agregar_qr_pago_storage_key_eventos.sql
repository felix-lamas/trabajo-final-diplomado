DO $$
BEGIN
    -- Bases existentes reciben la columna mediante Flyway. En una base vacía,
    -- Hibernate creará la tabla eventos incluyendo el nuevo mapeo JPA.
    IF to_regclass('eventos') IS NOT NULL THEN
        ALTER TABLE eventos ADD COLUMN IF NOT EXISTS qr_pago_storage_key VARCHAR(500);
    END IF;
END $$;
