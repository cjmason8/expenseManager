-- Fix duplicate/typo metadata keys:
--   Company, copmpany  -> company
--   proerty            -> property
--
-- Also removes the bad keys from metadatakeys.
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/fix-metadata-key-typos.sql

BEGIN;

-- documents
UPDATE documents
SET metadata = (metadata - 'Company') || jsonb_build_object('company', metadata->'Company')
WHERE metadata ? 'Company';

UPDATE documents
SET metadata = (metadata - 'copmpany') || jsonb_build_object('company', metadata->'copmpany')
WHERE metadata ? 'copmpany';

UPDATE documents
SET metadata = (metadata - 'proerty') || jsonb_build_object('property', metadata->'proerty')
WHERE metadata ? 'proerty';

-- donations
UPDATE donations
SET metadata = (metadata - 'Company') || jsonb_build_object('company', metadata->'Company')
WHERE metadata ? 'Company';

UPDATE donations
SET metadata = (metadata - 'copmpany') || jsonb_build_object('company', metadata->'copmpany')
WHERE metadata ? 'copmpany';

UPDATE donations
SET metadata = (metadata - 'proerty') || jsonb_build_object('property', metadata->'proerty')
WHERE metadata ? 'proerty';

-- refdata
UPDATE refdata
SET metadata = (metadata - 'Company') || jsonb_build_object('company', metadata->'Company')
WHERE metadata ? 'Company';

UPDATE refdata
SET metadata = (metadata - 'copmpany') || jsonb_build_object('company', metadata->'copmpany')
WHERE metadata ? 'copmpany';

UPDATE refdata
SET metadata = (metadata - 'proerty') || jsonb_build_object('property', metadata->'proerty')
WHERE metadata ? 'proerty';

-- transactions
UPDATE transactions
SET metadata = (metadata - 'Company') || jsonb_build_object('company', metadata->'Company')
WHERE metadata ? 'Company';

UPDATE transactions
SET metadata = (metadata - 'copmpany') || jsonb_build_object('company', metadata->'copmpany')
WHERE metadata ? 'copmpany';

UPDATE transactions
SET metadata = (metadata - 'proerty') || jsonb_build_object('property', metadata->'proerty')
WHERE metadata ? 'proerty';

-- remove bad keys from lookup table
DELETE FROM metadatakeys
WHERE name IN ('Company', 'copmpany', 'proerty');

COMMIT;
