-- Create entities table for recipe/notes entries with optional document attachment.
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/create-entities.sql

BEGIN;

CREATE SEQUENCE IF NOT EXISTS entities_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS entities (
	id bigint NOT NULL,
	name character varying(255) NOT NULL,
	description character varying(2000),
	type character varying(50) NOT NULL,
	documentid uuid,
	CONSTRAINT entities_pkey PRIMARY KEY (id),
	CONSTRAINT entities_documentid_fkey FOREIGN KEY (documentid) REFERENCES documents (id)
);

CREATE INDEX IF NOT EXISTS entities_type_idx ON entities (type);

SELECT setval(
	'entities_seq',
	GREATEST(COALESCE((SELECT MAX(id) FROM entities), 1), 1)
);

COMMIT;
