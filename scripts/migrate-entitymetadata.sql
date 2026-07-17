-- Migrate existing JSON metadata blobs into entitymetadata rows.
-- Uses INSERT...SELECT so it works on both Dev and Prod (data differs).
--
-- Prerequisite: metadatakeys, metadatavalues, and entitymetadata tables exist
-- and lookup values have been seeded/consolidated.
--
-- Only pairs that match a metadatavalues row are migrated.
-- String values and array elements are both included.
-- Safe to re-run: skips existing (type, entityid, metadatavalueid) triples.
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/migrate-entitymetadata.sql

BEGIN;

INSERT INTO entitymetadata (id, type, entityid, metadatavalueid)
SELECT nextval('entitymetadata_seq'), src.type, src.entity_id, mv.id
FROM (
	-- EXPENSE (string values)
	SELECT 'EXPENSE'::text AS type, t.id::text AS entity_id, e.key AS key, trim(both '"' from e.value::text) AS value
	FROM transactions t
	CROSS JOIN LATERAL jsonb_each(t.metadata) e
	WHERE t.transactiontype = 'EXPENSE'
		AND t.metadata IS NOT NULL
		AND t.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'string'

	UNION ALL

	-- EXPENSE (array elements)
	SELECT 'EXPENSE', t.id::text, e.key, elem
	FROM transactions t
	CROSS JOIN LATERAL jsonb_each(t.metadata) e
	CROSS JOIN LATERAL jsonb_array_elements_text(e.value) elem
	WHERE t.transactiontype = 'EXPENSE'
		AND t.metadata IS NOT NULL
		AND t.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'array'

	UNION ALL

	-- INCOME (string values)
	SELECT 'INCOME', t.id::text, e.key, trim(both '"' from e.value::text)
	FROM transactions t
	CROSS JOIN LATERAL jsonb_each(t.metadata) e
	WHERE t.transactiontype = 'INCOME'
		AND t.metadata IS NOT NULL
		AND t.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'string'

	UNION ALL

	-- INCOME (array elements)
	SELECT 'INCOME', t.id::text, e.key, elem
	FROM transactions t
	CROSS JOIN LATERAL jsonb_each(t.metadata) e
	CROSS JOIN LATERAL jsonb_array_elements_text(e.value) elem
	WHERE t.transactiontype = 'INCOME'
		AND t.metadata IS NOT NULL
		AND t.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'array'

	UNION ALL

	-- DONATION (string values)
	SELECT 'DONATION', d.id::text, e.key, trim(both '"' from e.value::text)
	FROM donations d
	CROSS JOIN LATERAL jsonb_each(d.metadata) e
	WHERE d.metadata IS NOT NULL
		AND d.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'string'

	UNION ALL

	-- DONATION (array elements)
	SELECT 'DONATION', d.id::text, e.key, elem
	FROM donations d
	CROSS JOIN LATERAL jsonb_each(d.metadata) e
	CROSS JOIN LATERAL jsonb_array_elements_text(e.value) elem
	WHERE d.metadata IS NOT NULL
		AND d.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'array'

	UNION ALL

	-- DOCUMENT (string values)
	SELECT 'DOCUMENT', d.id::text, e.key, trim(both '"' from e.value::text)
	FROM documents d
	CROSS JOIN LATERAL jsonb_each(d.metadata) e
	WHERE d.metadata IS NOT NULL
		AND d.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'string'

	UNION ALL

	-- DOCUMENT (array elements)
	SELECT 'DOCUMENT', d.id::text, e.key, elem
	FROM documents d
	CROSS JOIN LATERAL jsonb_each(d.metadata) e
	CROSS JOIN LATERAL jsonb_array_elements_text(e.value) elem
	WHERE d.metadata IS NOT NULL
		AND d.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'array'

	UNION ALL

	-- REF_DATA (string values)
	SELECT 'REF_DATA', r.id::text, e.key, trim(both '"' from e.value::text)
	FROM refdata r
	CROSS JOIN LATERAL jsonb_each(r.metadata) e
	WHERE r.metadata IS NOT NULL
		AND r.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'string'

	UNION ALL

	-- REF_DATA (array elements)
	SELECT 'REF_DATA', r.id::text, e.key, elem
	FROM refdata r
	CROSS JOIN LATERAL jsonb_each(r.metadata) e
	CROSS JOIN LATERAL jsonb_array_elements_text(e.value) elem
	WHERE r.metadata IS NOT NULL
		AND r.metadata <> 'null'::jsonb
		AND jsonb_typeof(e.value) = 'array'
) src
JOIN metadatakeys mk ON mk.name = src.key
JOIN metadatavalues mv ON mv.metadatakeyid = mk.id AND mv.value = src.value
ON CONFLICT (type, entityid, metadatavalueid) DO NOTHING;

SELECT setval(
	'entitymetadata_seq',
	GREATEST(COALESCE((SELECT MAX(id) FROM entitymetadata), 1), 1)
);

COMMIT;
