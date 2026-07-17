-- Create metadatakeys lookup table for uniform metadata key selection in the UI.
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/create-metadatakeys.sql
--
-- Or via Docker:
--   docker run --rm -e PGPASSWORD=... -v "$PWD":/work -w /work postgres:15 \
--     psql -h host.docker.internal -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/create-metadatakeys.sql

BEGIN;

CREATE SEQUENCE IF NOT EXISTS metadatakeys_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS metadatakeys (
	id bigint NOT NULL,
	name character varying(255) NOT NULL,
	CONSTRAINT metadatakeys_pkey PRIMARY KEY (id),
	CONSTRAINT metadatakeys_name_unique UNIQUE (name)
);

-- Seed with distinct keys already present in metadata JSON blobs
INSERT INTO metadatakeys (id, name)
SELECT nextval('metadatakeys_seq'), key
FROM (
	SELECT DISTINCT jsonb_object_keys(metadata) AS key
	FROM (
		SELECT metadata FROM documents WHERE metadata IS NOT NULL AND metadata <> 'null'::jsonb
		UNION ALL
		SELECT metadata FROM donations WHERE metadata IS NOT NULL AND metadata <> 'null'::jsonb
		UNION ALL
		SELECT metadata FROM refdata WHERE metadata IS NOT NULL AND metadata <> 'null'::jsonb
		UNION ALL
		SELECT metadata FROM transactions WHERE metadata IS NOT NULL AND metadata <> 'null'::jsonb
	) all_metadata
) distinct_keys
WHERE NOT EXISTS (
	SELECT 1 FROM metadatakeys existing WHERE existing.name = distinct_keys.key
)
ORDER BY key;

SELECT setval(
	'metadatakeys_seq',
	GREATEST(COALESCE((SELECT MAX(id) FROM metadatakeys), 1), 1)
);

COMMIT;
