DO $$
DECLARE
    v_conflictos TEXT;
    v_tabla_oid OID;
    v_columna_num SMALLINT;
    v_indice_unico_existe BOOLEAN;
BEGIN
    -- En una base vacía Hibernate crea la tabla al iniciar. Las bases con
    -- categorías existentes reciben esta evolución mediante Flyway.
    v_tabla_oid := to_regclass('public.categorias_evento');

    IF v_tabla_oid IS NOT NULL THEN
        ALTER TABLE public.categorias_evento
            ADD COLUMN IF NOT EXISTS nombre_normalizado VARCHAR(100);

        -- Completar solamente filas sin valor. Los valores existentes no se
        -- reescriben silenciosamente; abajo se valida la regla del dominio.
        UPDATE public.categorias_evento
        SET nombre_normalizado = lower(btrim(nombre))
        WHERE nombre_normalizado IS NULL;

        SELECT string_agg(format('id=%s, nombre=%L, nombre_normalizado=%L', id, nombre, nombre_normalizado), '; ')
        INTO v_conflictos
        FROM public.categorias_evento
        WHERE nombre_normalizado IS DISTINCT FROM lower(btrim(nombre));

        IF v_conflictos IS NOT NULL THEN
            RAISE EXCEPTION 'V2 abortada: nombre_normalizado no coincide con lower(trim(nombre)): %', v_conflictos;
        END IF;

        SELECT string_agg(format('normalizado=%L (ids: %s)', nombre_normalizado, ids), '; ')
        INTO v_conflictos
        FROM (
            SELECT nombre_normalizado, string_agg(id::TEXT, ', ' ORDER BY id) AS ids
            FROM public.categorias_evento
            GROUP BY nombre_normalizado
            HAVING count(*) > 1
        ) duplicados;

        IF v_conflictos IS NOT NULL THEN
            RAISE EXCEPTION 'V2 abortada: existen categorías duplicadas tras normalizar; revisar sin borrar ni renombrar datos: %', v_conflictos;
        END IF;

        ALTER TABLE public.categorias_evento
            ALTER COLUMN nombre_normalizado SET NOT NULL;

        SELECT attnum
        INTO v_columna_num
        FROM pg_attribute
        WHERE attrelid = v_tabla_oid
          AND attname = 'nombre_normalizado'
          AND NOT attisdropped;

        SELECT EXISTS (
            SELECT 1
            FROM pg_index i
            WHERE i.indrelid = v_tabla_oid
              AND i.indisunique
              AND i.indisvalid
              AND i.indpred IS NULL
              AND i.indexprs IS NULL
              AND i.indnkeyatts = 1
              AND i.indkey[0] = v_columna_num
        )
        INTO v_indice_unico_existe;

        IF NOT v_indice_unico_existe THEN
            ALTER TABLE public.categorias_evento
                ADD CONSTRAINT uq_categorias_evento_nombre_normalizado
                UNIQUE (nombre_normalizado);
        END IF;
    END IF;
END $$;
