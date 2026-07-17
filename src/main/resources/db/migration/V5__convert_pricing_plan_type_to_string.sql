-- plan_type had no @Enumerated, so Hibernate persisted it as an ordinal smallint.
-- It is now @Enumerated(EnumType.STRING); ddl-auto=update will not convert the column
-- type itself, so existing rows are remapped here from the old declaration order.
-- Skipped on a fresh database, where the table does not exist yet and Hibernate
-- creates plan_type as varchar directly.

DO
$$
    DECLARE
        v_constraint text;
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_name = 'pricing_plans'
                     AND column_name = 'plan_type'
                     AND data_type IN ('smallint', 'integer', 'bigint')) THEN

            -- Hibernate guards an ordinal enum column with a check constraint
            -- (plan_type >= 0 AND plan_type <= 3). ALTER COLUMN TYPE re-validates it against
            -- the new varchar and fails on the missing varchar >= integer operator, so drop it
            -- first. ddl-auto=update recreates the varchar equivalent on the next boot.
            FOR v_constraint IN
                SELECT con.conname
                FROM pg_constraint con
                         JOIN pg_class rel ON rel.oid = con.conrelid
                         JOIN pg_attribute att
                              ON att.attrelid = con.conrelid AND att.attnum = ANY (con.conkey)
                WHERE rel.relname = 'pricing_plans'
                  AND con.contype = 'c'
                  AND att.attname = 'plan_type'
                LOOP
                    EXECUTE format('ALTER TABLE pricing_plans DROP CONSTRAINT %I', v_constraint);
                END LOOP;

            ALTER TABLE pricing_plans
                ALTER COLUMN plan_type TYPE VARCHAR(255)
                    USING CASE plan_type
                              WHEN 0 THEN 'MONTHLY'
                              WHEN 1 THEN 'QUARTERLY'
                              WHEN 2 THEN 'YEARLY'
                              WHEN 3 THEN 'LIFETIME'
                        END;
        END IF;
    END
$$;
