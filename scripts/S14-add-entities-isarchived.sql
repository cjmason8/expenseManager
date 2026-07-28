-- Add archive flag to entities table (used for Notes).
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/S14-add-entities-isarchived.sql

BEGIN;

ALTER TABLE entities ADD COLUMN IF NOT EXISTS isarchived boolean NOT NULL DEFAULT false;

COMMIT;
