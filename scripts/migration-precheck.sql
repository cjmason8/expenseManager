-- Run before migration.sql on prod (psql -d YOUR_DB -f scripts/migration-precheck.sql)
-- Read output; fix any FAIL rows before migrating.

\pset pager off

SELECT version() AS postgres_version;

SELECT extname, extversion
FROM pg_extension
WHERE extname IN ('pgcrypto', 'uuid-ossp');

-- documents column layout
SELECT column_name, data_type, udt_name, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'documents'
ORDER BY ordinal_position;

-- Child documentid column types (must be bigint before migration, unless already migrated)
SELECT table_name, column_name, data_type, udt_name
FROM information_schema.columns
WHERE table_schema = 'public'
  AND column_name = 'documentid'
  AND table_name IN ('transactions', 'donations', 'rentalpayments')
ORDER BY table_name;

SELECT 'documents row count' AS check_name, count(*)::text AS value FROM public.documents
UNION ALL
SELECT 'documents with NULL legacy id', count(*)::text FROM public.documents WHERE id IS NULL AND NOT EXISTS (
  SELECT 1 FROM information_schema.columns c
  WHERE c.table_schema = 'public' AND c.table_name = 'documents' AND c.column_name = 'old_id'
)
UNION ALL
SELECT 'documents old_id NULL (after rename)', count(*)::text FROM public.documents
WHERE EXISTS (
  SELECT 1 FROM information_schema.columns c
  WHERE c.table_schema = 'public' AND c.table_name = 'documents' AND c.column_name = 'old_id'
) AND old_id IS NULL
UNION ALL
SELECT 'documents id NULL with old_id present', count(*)::text FROM public.documents
WHERE EXISTS (
  SELECT 1 FROM information_schema.columns c
  WHERE c.table_schema = 'public' AND c.table_name = 'documents' AND c.column_name = 'old_id'
) AND id IS NULL
UNION ALL
SELECT 'documents id NULL uuid column', count(*)::text FROM public.documents
WHERE EXISTS (
  SELECT 1 FROM information_schema.columns c
  WHERE c.table_schema = 'public' AND c.table_name = 'documents' AND c.column_name = 'id' AND c.udt_name = 'uuid'
) AND id IS NULL;

-- Sample deterministic UUID for old_id=1 (compare with dev after migration)
SELECT public.legacy_document_uuid(1) AS sample_uuid_for_old_id_1
WHERE EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'legacy_document_uuid');
