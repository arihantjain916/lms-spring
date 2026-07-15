-- Hibernate ddl-auto does not reliably change existing VARCHAR column types.
-- Flyway runs this migration once and records it in flyway_schema_history.

ALTER TABLE IF EXISTS asset_meta
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE IF EXISTS blog
    ALTER COLUMN description TYPE TEXT,
    ALTER COLUMN content TYPE TEXT;

ALTER TABLE IF EXISTS blog_comment
    ALTER COLUMN comment TYPE TEXT;

ALTER TABLE IF EXISTS category
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE IF EXISTS contact_us
    ALTER COLUMN message TYPE TEXT;

ALTER TABLE IF EXISTS course_question
    ALTER COLUMN content TYPE TEXT;

ALTER TABLE IF EXISTS courses
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE IF EXISTS lesson
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE IF EXISTS pricing_plans
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE IF EXISTS program
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE IF EXISTS program_application
    ALTER COLUMN message TYPE TEXT;

ALTER TABLE IF EXISTS question_attempt
    ALTER COLUMN answer TYPE TEXT;

ALTER TABLE IF EXISTS question_options
    ALTER COLUMN option TYPE TEXT;

ALTER TABLE IF EXISTS question_reply
    ALTER COLUMN content TYPE TEXT;

ALTER TABLE IF EXISTS questions
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE IF EXISTS ratings
    ALTER COLUMN comment TYPE TEXT;

ALTER TABLE IF EXISTS tutorial
    ALTER COLUMN description TYPE TEXT,
    ALTER COLUMN content TYPE TEXT;

ALTER TABLE IF EXISTS webinar
    ALTER COLUMN description TYPE TEXT;

ALTER TABLE IF EXISTS webinar_host_application
    ALTER COLUMN message TYPE TEXT;
