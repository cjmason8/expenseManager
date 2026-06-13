-- Migrate documents primary key from bigint (sequence) to UUID and update referencing foreign keys.
--
-- RUN WITH psql (whole file, one transaction):
--   psql -d expensemanager -v ON_ERROR_STOP=1 -f migration.sql
--
-- Do NOT run step-by-step in GUI clients that skip DO blocks or stop on first error without rollback.
-- Run scripts/migration-precheck.sql on prod first.
--
-- If prod stopped after ADD COLUMN id with NULL uuids, run:
--   scripts/migration-repair-documents-id.sql
-- then continue from step 4 below (or re-run this file only if transaction was fully rolled back).
--
-- PostgreSQL 13+ recommended. On PG 12 and earlier, pgcrypto is enabled automatically below.

-- \connect is psql-only; use: psql -d expensemanager -f migration.sql
\connect expensemanager

BEGIN;

-- gen_random_uuid(): built-in PG 13+; otherwise provided by pgcrypto
DO $$
BEGIN
  PERFORM gen_random_uuid();
EXCEPTION
  WHEN undefined_function THEN
    CREATE EXTENSION IF NOT EXISTS pgcrypto;
END $$;

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

-- Abort if migration already finished
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'documents'
      AND column_name = 'id' AND udt_name = 'uuid'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'documents' AND column_name = 'old_id'
  ) THEN
    RAISE EXCEPTION 'Migration already applied (documents.id is uuid, old_id removed).';
  END IF;
END $$;

-- Require legacy bigint id column before we rename it
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'documents'
      AND column_name = 'id'
      AND udt_name IN ('int8', 'int4', 'int2')
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'documents' AND column_name = 'old_id'
  ) THEN
    RAISE EXCEPTION 'Unexpected documents.id type. Expected bigint (or old_id from a partial run). Run migration-precheck.sql.';
  END IF;
END $$;

-- 1. Drop all foreign keys that reference documents
DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT c.conname AS conname, n.nspname AS schema_name, t.relname AS table_name
    FROM pg_constraint c
    JOIN pg_class t ON c.conrelid = t.oid
    JOIN pg_namespace n ON t.relnamespace = n.oid
    JOIN pg_class d ON c.confrelid = d.oid
    JOIN pg_namespace nd ON d.relnamespace = nd.oid
    WHERE c.contype = 'f'
      AND d.relname = 'documents'
      AND nd.nspname = 'public'
  LOOP
    EXECUTE format('ALTER TABLE %I.%I DROP CONSTRAINT %I', r.schema_name, r.table_name, r.conname);
  END LOOP;
END $$;

-- 2. Drop primary key on documents (name may vary)
DO $$
DECLARE
  pkname text;
BEGIN
  SELECT c.conname INTO pkname
  FROM pg_constraint c
  JOIN pg_class t ON c.conrelid = t.oid
  JOIN pg_namespace n ON t.relnamespace = n.oid
  WHERE t.relname = 'documents'
    AND n.nspname = 'public'
    AND c.contype = 'p'
  LIMIT 1;
  IF pkname IS NOT NULL THEN
    EXECUTE format('ALTER TABLE public.documents DROP CONSTRAINT %I', pkname);
  END IF;
END $$;

-- 3. Legacy id -> old_id; add uuid id (skip rename/add if resuming a partial migration)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'documents' AND column_name = 'old_id'
  ) THEN
    EXECUTE 'ALTER TABLE public.documents RENAME COLUMN id TO old_id';
  ELSE
    RAISE NOTICE 'Step 3: old_id already present, skipping rename';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'documents'
      AND column_name = 'id' AND udt_name = 'uuid'
  ) THEN
    EXECUTE 'ALTER TABLE public.documents ADD COLUMN id uuid NULL';
  ELSE
    RAISE NOTICE 'Step 3: uuid id column already present';
  END IF;
END $$;

DO $$
DECLARE
  null_old_id bigint;
BEGIN
  SELECT count(*) INTO null_old_id FROM public.documents WHERE old_id IS NULL;
  IF null_old_id > 0 THEN
    RAISE EXCEPTION 'Migration failed: % document row(s) have NULL old_id before UUID mapping', null_old_id;
  END IF;
END $$;

UPDATE public.documents
SET id = public.legacy_document_uuid(old_id)
WHERE id IS NULL AND old_id IS NOT NULL;

DO $$
DECLARE
  null_ids bigint;
  total bigint;
BEGIN
  SELECT count(*) INTO null_ids FROM public.documents WHERE id IS NULL;
  SELECT count(*) INTO total FROM public.documents;
  IF null_ids > 0 THEN
    RAISE EXCEPTION 'Migration failed: % of % document row(s) still have NULL id after UUID update', null_ids, total;
  END IF;
  RAISE NOTICE 'Step 3 OK: populated uuid id for % document row(s)', total;
END $$;

ALTER TABLE public.documents ALTER COLUMN id SET NOT NULL;
ALTER TABLE public.documents ALTER COLUMN id SET DEFAULT gen_random_uuid();

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint c
    JOIN pg_class t ON c.conrelid = t.oid
    WHERE t.relname = 'documents' AND c.contype = 'p'
  ) THEN
    EXECUTE 'ALTER TABLE public.documents ADD PRIMARY KEY (id)';
  ELSE
    RAISE NOTICE 'Step 3: primary key on documents.id already exists';
  END IF;
END $$;

-- 4. Re-point child FK columns from old bigint to new UUID (skip if documentid is already uuid)

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'transactions'
      AND column_name = 'documentid' AND udt_name = 'uuid'
  ) THEN
    RAISE NOTICE 'Step 4: transactions.documentid already uuid, skipping';
  ELSE
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = 'transactions' AND column_name = 'documentid_new'
    ) THEN
      EXECUTE 'ALTER TABLE public.transactions ADD COLUMN documentid_new uuid NULL';
    END IF;
    UPDATE public.transactions t
    SET documentid_new = d.id
    FROM public.documents d
    WHERE t.documentid IS NOT NULL
      AND t.documentid_new IS NULL
      AND t.documentid::bigint = d.old_id;
    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = 'transactions' AND column_name = 'documentid'
        AND udt_name IN ('int8', 'int4', 'int2')
    ) THEN
      EXECUTE 'ALTER TABLE public.transactions DROP COLUMN documentid';
      EXECUTE 'ALTER TABLE public.transactions RENAME COLUMN documentid_new TO documentid';
    END IF;
  END IF;
END $$;

ALTER TABLE public.transactions DROP CONSTRAINT IF EXISTS fk_transactions_document;
ALTER TABLE public.transactions
  ADD CONSTRAINT fk_transactions_document FOREIGN KEY (documentid) REFERENCES public.documents (id);

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'donations'
      AND column_name = 'documentid' AND udt_name = 'uuid'
  ) THEN
    RAISE NOTICE 'Step 4: donations.documentid already uuid, skipping';
  ELSE
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = 'donations' AND column_name = 'documentid_new'
    ) THEN
      EXECUTE 'ALTER TABLE public.donations ADD COLUMN documentid_new uuid NULL';
    END IF;
    UPDATE public.donations dn
    SET documentid_new = d.id
    FROM public.documents d
    WHERE dn.documentid IS NOT NULL
      AND dn.documentid_new IS NULL
      AND dn.documentid::bigint = d.old_id;
    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = 'donations' AND column_name = 'documentid'
        AND udt_name IN ('int8', 'int4', 'int2')
    ) THEN
      EXECUTE 'ALTER TABLE public.donations DROP COLUMN documentid';
      EXECUTE 'ALTER TABLE public.donations RENAME COLUMN documentid_new TO documentid';
    END IF;
  END IF;
END $$;

ALTER TABLE public.donations DROP CONSTRAINT IF EXISTS fk_donations_document;
ALTER TABLE public.donations
  ADD CONSTRAINT fk_donations_document FOREIGN KEY (documentid) REFERENCES public.documents (id);

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'rentalpayments'
      AND column_name = 'documentid' AND udt_name = 'uuid'
  ) THEN
    RAISE NOTICE 'Step 4: rentalpayments.documentid already uuid, skipping';
  ELSE
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = 'rentalpayments' AND column_name = 'documentid_new'
    ) THEN
      EXECUTE 'ALTER TABLE public.rentalpayments ADD COLUMN documentid_new uuid NULL';
    END IF;
    UPDATE public.rentalpayments rp
    SET documentid_new = d.id
    FROM public.documents d
    WHERE rp.documentid IS NOT NULL
      AND rp.documentid_new IS NULL
      AND rp.documentid::bigint = d.old_id;
    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = 'rentalpayments' AND column_name = 'documentid'
        AND udt_name IN ('int8', 'int4', 'int2')
    ) THEN
      EXECUTE 'ALTER TABLE public.rentalpayments DROP COLUMN documentid';
      EXECUTE 'ALTER TABLE public.rentalpayments RENAME COLUMN documentid_new TO documentid';
    END IF;
  END IF;
END $$;

ALTER TABLE public.rentalpayments DROP CONSTRAINT IF EXISTS fk_rentalpayments_document;
ALTER TABLE public.rentalpayments
  ADD CONSTRAINT fk_rentalpayments_document FOREIGN KEY (documentid) REFERENCES public.documents (id);

-- 5. Remove legacy key column from documents
ALTER TABLE public.documents DROP COLUMN IF EXISTS old_id;

-- 6. Sequence no longer used for document ids
DROP SEQUENCE IF EXISTS public.documents_seq;

COMMIT;
