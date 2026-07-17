-- Create entitymetadata table linking entities to metadatavalues.
--
-- type: EXPENSE | INCOME | DONATION | DOCUMENT | REF_DATA
-- entityid: string form of the source row id (bigint or uuid)
-- metadatavalueid: FK to metadatavalues
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/create-entitymetadata.sql

BEGIN;

CREATE SEQUENCE IF NOT EXISTS entitymetadata_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS entitymetadata (
	id bigint NOT NULL,
	type character varying(31) NOT NULL,
	entityid character varying(36) NOT NULL,
	metadatavalueid bigint NOT NULL,
	CONSTRAINT entitymetadata_pkey PRIMARY KEY (id),
	CONSTRAINT entitymetadata_type_entity_value_unique UNIQUE (type, entityid, metadatavalueid),
	CONSTRAINT entitymetadata_metadatavalueid_fkey FOREIGN KEY (metadatavalueid)
		REFERENCES metadatavalues (id)
);

CREATE INDEX IF NOT EXISTS entitymetadata_type_entityid_idx
	ON entitymetadata (type, entityid);

COMMIT;
