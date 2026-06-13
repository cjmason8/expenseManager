-- Repair: populate documents.id (uuid) from old_id when migration stopped mid-way.
-- Safe when: public.documents has BOTH old_id (bigint) and id (uuid), and id is still NULL.
--
-- Usage: psql -d YOUR_DB -f scripts/migration-repair-documents-id.sql
--
-- After repair, re-run migration-precheck.sql — id NULL count must be 0.
-- Then continue with the remainder of migration.sql from step 4 (child FK columns) if needed.

BEGIN;

CREATE OR REPLACE FUNCTION public.legacy_document_uuid(p_old_id bigint)
RETURNS uuid
LANGUAGE sql
IMMUTABLE
AS $$
  SELECT (
    substring(h, 1, 8) || '-' ||
    substring(h, 9, 4) || '-' ||
    '4' || substring(h, 13, 3) || '-' ||
    'a' || substring(h, 17, 3) || '-' ||
    substring(h, 21, 12)
  )::uuid
  FROM (SELECT md5('expensemanager.documents.pk:' || p_old_id::text) AS h) x;
$$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'documents' AND column_name = 'old_id'
  ) THEN
    RAISE EXCEPTION 'Repair not applicable: documents.old_id does not exist (migration may be complete or never started)';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'documents' AND column_name = 'id' AND udt_name = 'uuid'
  ) THEN
    RAISE EXCEPTION 'Repair not applicable: documents.id is not a uuid column — run full migration.sql first';
  END IF;
END $$;

DO $$
DECLARE
  null_old_id bigint;
BEGIN
  SELECT count(*) INTO null_old_id FROM public.documents WHERE old_id IS NULL;
  IF null_old_id > 0 THEN
    RAISE EXCEPTION 'Cannot repair: % document row(s) have NULL old_id. Restore from backup or assign ids manually.', null_old_id;
  END IF;
END $$;

UPDATE public.documents
SET id = public.legacy_document_uuid(old_id)
WHERE id IS NULL AND old_id IS NOT NULL;

DO $$
DECLARE
  remaining bigint;
BEGIN
  SELECT count(*) INTO remaining FROM public.documents WHERE id IS NULL;
  IF remaining > 0 THEN
    RAISE EXCEPTION 'Repair failed: % document row(s) still have NULL id', remaining;
  END IF;
  RAISE NOTICE 'Repair OK: all documents.id values populated from old_id';
END $$;

COMMIT;
