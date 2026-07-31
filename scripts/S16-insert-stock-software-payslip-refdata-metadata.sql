-- Metadata for Stock Software Payslip refdata (type + company).
-- Copies to documents via StockSoftwarePayslipProcessor (year is added per payslip).
--
-- Metadata is stored in entitymetadata (used by the application).
-- If refdata.metadata JSONB column still exists (pre-S11), that is updated too.
--
-- Prerequisite: scripts/S15-insert-stock-software-payslip-refdata.sql
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/S16-insert-stock-software-payslip-refdata-metadata.sql

BEGIN;

INSERT INTO metadatavalues (id, metadatakeyid, value)
SELECT nextval('metadatavalues_seq'), key_id, value
FROM (VALUES
	((SELECT id FROM metadatakeys WHERE name = 'type'), 'Payslip'),
	((SELECT id FROM metadatakeys WHERE name = 'company'), 'Stock Software')
) AS seed(key_id, value)
WHERE key_id IS NOT NULL
ON CONFLICT (metadatakeyid, value) DO NOTHING;

DELETE FROM entitymetadata em
USING refdata r, metadatavalues mv, metadatakeys mk
WHERE r.emailprocessor = 'STOCK_SOFTWARE_PAYSLIP'
	AND r.deleted = false
	AND em.type = 'REF_DATA'
	AND em.entityid = r.id::text
	AND em.metadatavalueid = mv.id
	AND mv.metadatakeyid = mk.id
	AND mk.name IN ('type', 'company');

INSERT INTO entitymetadata (id, type, entityid, metadatavalueid)
SELECT nextval('entitymetadata_seq'), 'REF_DATA', r.id::text, mv.id
FROM refdata r
JOIN metadatakeys mk ON mk.name IN ('type', 'company')
JOIN metadatavalues mv ON mv.metadatakeyid = mk.id
	AND (
		(mk.name = 'type' AND mv.value = 'Payslip')
		OR (mk.name = 'company' AND mv.value = 'Stock Software')
	)
WHERE r.emailprocessor = 'STOCK_SOFTWARE_PAYSLIP'
	AND r.deleted = false
ON CONFLICT (type, entityid, metadatavalueid) DO NOTHING;

DO $$
BEGIN
	IF EXISTS (
		SELECT 1
		FROM information_schema.columns
		WHERE table_schema = 'public'
			AND table_name = 'refdata'
			AND column_name = 'metadata'
	) THEN
		UPDATE refdata
		SET metadata = jsonb_build_object(
			'type', 'Payslip',
			'company', 'Stock Software'
		)
		WHERE emailprocessor = 'STOCK_SOFTWARE_PAYSLIP'
			AND deleted = false;
	END IF;
END $$;

SELECT setval(
	'metadatavalues_seq',
	GREATEST(COALESCE((SELECT MAX(id) FROM metadatavalues), 1), 1)
);

SELECT setval(
	'entitymetadata_seq',
	GREATEST(COALESCE((SELECT MAX(id) FROM entitymetadata), 1), 1)
);

COMMIT;

-- Verify
SELECT r.id, r.description
FROM refdata r
WHERE r.emailprocessor = 'STOCK_SOFTWARE_PAYSLIP'
	AND r.deleted = false;

SELECT mk.name, mv.value
FROM refdata r
JOIN entitymetadata em ON em.type = 'REF_DATA' AND em.entityid = r.id::text
JOIN metadatavalues mv ON mv.id = em.metadatavalueid
JOIN metadatakeys mk ON mk.id = mv.metadatakeyid
WHERE r.emailprocessor = 'STOCK_SOFTWARE_PAYSLIP'
	AND r.deleted = false
ORDER BY mk.name;
