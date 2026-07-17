-- Insert distinct metadata keys found across all tables with a metadata column
-- (documents, donations, refdata, transactions).
--
-- Safe to re-run: skips names that already exist.
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/insert-metadatakeys.sql

BEGIN;

INSERT INTO metadatakeys (id, name)
VALUES
	(nextval('metadatakeys_seq'), 'car'),
	(nextval('metadatakeys_seq'), 'carModel'),
	(nextval('metadatakeys_seq'), 'company'),
	(nextval('metadatakeys_seq'), 'contact'),
	(nextval('metadatakeys_seq'), 'item'),
	(nextval('metadatakeys_seq'), 'person'),
	(nextval('metadatakeys_seq'), 'property'),
	(nextval('metadatakeys_seq'), 'state'),
	(nextval('metadatakeys_seq'), 'type'),
	(nextval('metadatakeys_seq'), 'year')
ON CONFLICT (name) DO NOTHING;

SELECT setval(
	'metadatakeys_seq',
	GREATEST(COALESCE((SELECT MAX(id) FROM metadatakeys), 1), 1)
);

COMMIT;
