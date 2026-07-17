-- A pricing plan was tied to exactly one course (pricing_plans.course_id), so the same
-- price point had to be recreated for every course. Plans are now reusable: a plan attaches
-- to many courses through course_pricing_plans, and a course still offers one plan per type.
--
-- courses.pricing_plan_id is dropped rather than migrated. Nothing ever read it, but 154 of
-- 157 courses pointed at a single 500 plan; importing those links would silently turn 154
-- free courses into paid ones. Attach them through /pricing when you actually want that.

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_name = 'pricing_plans') THEN

            CREATE TABLE IF NOT EXISTS course_pricing_plans
            (
                pricing_plan_id VARCHAR(255) NOT NULL REFERENCES pricing_plans (id),
                course_id       BIGINT       NOT NULL REFERENCES courses (id),
                PRIMARY KEY (pricing_plan_id, course_id)
            );

            IF EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'pricing_plans'
                         AND column_name = 'course_id') THEN

                INSERT INTO course_pricing_plans (pricing_plan_id, course_id)
                SELECT id, course_id
                FROM pricing_plans
                WHERE course_id IS NOT NULL
                ON CONFLICT DO NOTHING;

                ALTER TABLE pricing_plans
                    DROP COLUMN course_id;
            END IF;
        END IF;

        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_name = 'courses'
                     AND column_name = 'pricing_plan_id') THEN
            ALTER TABLE courses
                DROP COLUMN pricing_plan_id;
        END IF;
    END
$$;
