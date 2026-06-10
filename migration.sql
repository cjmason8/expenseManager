-- Migrate documents primary key from bigint (sequence) to UUID and update referencing foreign keys.
-- PostgreSQL 13+ recommended (gen_random_uuid() for DEFAULT only).
--
-- Existing rows: each documents.old_id maps to a single deterministic UUID (md5-based, no extension).
-- That guarantees id is never NULL after the UPDATE and child rows join reliably to the same UUID.
--
-- Flow:
--   1) documents: rename id -> old_id, add uuid column id, UPDATE from old_id (deterministic UUID),
--      NOT NULL + DEFAULT gen_random_uuid() + PK.
--   2) transactions, donations, rentalpayments: replace bigint documentid with uuid by joining
--      child.documentid::bigint = documents.old_id -> documents.id, then add FKs to documents(id).
--
-- FK column name: documentid (unquoted @JoinColumn "documentId" -> lowercase in PostgreSQL).
--
-- Tables: documents, transactions, donations, rentalpayments
--
-- Select database (psql meta-command; ignored by plain SQL clients — use: psql -d expensemanager -f migration.sql)

\connect expensemanager

BEGIN;

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

-- 3. Legacy id becomes old_id; new id is UUID (deterministic from old_id, then DEFAULT for new inserts)
ALTER TABLE public.documents RENAME COLUMN id TO old_id;

ALTER TABLE public.documents ADD COLUMN id uuid NULL;

-- One stable UUID per old_id (version-4-shaped, derived from md5 namespace + old_id).
UPDATE public.documents d
SET id = (
  substring(h, 1, 8) || '-' ||
  substring(h, 9, 4) || '-' ||
  '4' || substring(h, 13, 3) || '-' ||
  'a' || substring(h, 17, 3) || '-' ||
  substring(h, 21, 12)
)::uuid
FROM (
  SELECT
    old_id,
    md5('expensemanager.documents.pk:' || old_id::text) AS h
  FROM public.documents
) m
WHERE d.old_id = m.old_id;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM public.documents WHERE id IS NULL) THEN
    RAISE EXCEPTION 'migration failed: documents.id is still NULL for one or more rows';
  END IF;
END $$;

ALTER TABLE public.documents ALTER COLUMN id SET NOT NULL;
ALTER TABLE public.documents ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE public.documents ADD PRIMARY KEY (id);

-- 4. Re-point child FK columns from old bigint to new UUID

-- transactions (expenses / incomes)
ALTER TABLE public.transactions ADD COLUMN documentid_new uuid NULL;
UPDATE public.transactions t
SET documentid_new = d.id
FROM public.documents d
WHERE t.documentid IS NOT NULL
  AND t.documentid::bigint = d.old_id;
ALTER TABLE public.transactions DROP COLUMN documentid;
ALTER TABLE public.transactions RENAME COLUMN documentid_new TO documentid;
ALTER TABLE public.transactions
  ADD CONSTRAINT fk_transactions_document FOREIGN KEY (documentid) REFERENCES public.documents (id);

-- donations
ALTER TABLE public.donations ADD COLUMN documentid_new uuid NULL;
UPDATE public.donations dn
SET documentid_new = d.id
FROM public.documents d
WHERE dn.documentid IS NOT NULL
  AND dn.documentid::bigint = d.old_id;
ALTER TABLE public.donations DROP COLUMN documentid;
ALTER TABLE public.donations RENAME COLUMN documentid_new TO documentid;
ALTER TABLE public.donations
  ADD CONSTRAINT fk_donations_document FOREIGN KEY (documentid) REFERENCES public.documents (id);

-- rentalpayments
ALTER TABLE public.rentalpayments ADD COLUMN documentid_new uuid NULL;
UPDATE public.rentalpayments rp
SET documentid_new = d.id
FROM public.documents d
WHERE rp.documentid IS NOT NULL
  AND rp.documentid::bigint = d.old_id;
ALTER TABLE public.rentalpayments DROP COLUMN documentid;
ALTER TABLE public.rentalpayments RENAME COLUMN documentid_new TO documentid;
ALTER TABLE public.rentalpayments
  ADD CONSTRAINT fk_rentalpayments_document FOREIGN KEY (documentid) REFERENCES public.documents (id);

-- 5. Remove legacy key column from documents
ALTER TABLE public.documents DROP COLUMN old_id;

-- 6. Sequence no longer used for document ids
DROP SEQUENCE IF EXISTS public.documents_seq;

COMMIT;
