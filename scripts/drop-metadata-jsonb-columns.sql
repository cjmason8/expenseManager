-- Drop legacy JSON metadata columns after entitymetadata migration is complete
-- and the application has been deployed to read/write entitymetadata instead.
--
-- Run order:
--   1. scripts/migrate-entitymetadata.sql
--   2. Deploy application that uses entitymetadata
--   3. This script
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/drop-metadata-jsonb-columns.sql

BEGIN;

ALTER TABLE documents DROP COLUMN IF EXISTS metadata;
ALTER TABLE donations DROP COLUMN IF EXISTS metadata;
ALTER TABLE refdata DROP COLUMN IF EXISTS metadata;
ALTER TABLE transactions DROP COLUMN IF EXISTS metadata;

COMMIT;
