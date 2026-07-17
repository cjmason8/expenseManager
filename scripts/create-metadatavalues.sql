-- Create metadatavalues lookup table for uniform metadata value selection in the UI.
-- Values are scoped to a metadatakey so the frontend can filter options by key.
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/create-metadatavalues.sql

BEGIN;

CREATE SEQUENCE IF NOT EXISTS metadatavalues_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS metadatavalues (
	id bigint NOT NULL,
	value character varying(255) NOT NULL,
	metadatakeyid bigint NOT NULL,
	CONSTRAINT metadatavalues_pkey PRIMARY KEY (id),
	CONSTRAINT metadatavalues_key_value_unique UNIQUE (metadatakeyid, value),
	CONSTRAINT metadatavalues_metadatakeyid_fkey FOREIGN KEY (metadatakeyid)
		REFERENCES metadatakeys (id)
);

COMMIT;
