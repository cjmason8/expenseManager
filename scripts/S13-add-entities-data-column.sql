-- Add type-specific JSON data column to entities table.
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/S13-add-entities-data-column.sql

BEGIN;

ALTER TABLE entities ADD COLUMN IF NOT EXISTS data jsonb;

COMMIT;
