-- Widen metadatavalues.value beyond 255 characters.
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/S18-widen-metadatavalues-value.sql

BEGIN;

ALTER TABLE metadatavalues ALTER COLUMN value TYPE varchar(1000);

COMMIT;
