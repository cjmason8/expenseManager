-- Consolidate duplicate metadata values and update existing JSON metadata.
-- Add new renames below as they are identified.
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/consolidate-metadata-values.sql

BEGIN;

-- ---------------------------------------------------------------------------
-- car: 2003 Verada -> Verada 2003
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{car}', '"Verada 2003"')
WHERE metadata->>'car' = '2003 Verada';

UPDATE donations
SET metadata = jsonb_set(metadata, '{car}', '"Verada 2003"')
WHERE metadata->>'car' = '2003 Verada';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{car}', '"Verada 2003"')
WHERE metadata->>'car' = '2003 Verada';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{car}', '"Verada 2003"')
WHERE metadata->>'car' = '2003 Verada';

DELETE FROM metadatavalues
WHERE value = '2003 Verada'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'car');

-- ---------------------------------------------------------------------------
-- car: 2016 Camry -> Camry 2016
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{car}', '"Camry 2016"')
WHERE metadata->>'car' = '2016 Camry';

UPDATE donations
SET metadata = jsonb_set(metadata, '{car}', '"Camry 2016"')
WHERE metadata->>'car' = '2016 Camry';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{car}', '"Camry 2016"')
WHERE metadata->>'car' = '2016 Camry';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{car}', '"Camry 2016"')
WHERE metadata->>'car' = '2016 Camry';

DELETE FROM metadatavalues
WHERE value = '2016 Camry'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'car');

-- ---------------------------------------------------------------------------
-- company: Ikea -> IKEA
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{company}', '"IKEA"')
WHERE metadata->>'company' = 'Ikea';

UPDATE donations
SET metadata = jsonb_set(metadata, '{company}', '"IKEA"')
WHERE metadata->>'company' = 'Ikea';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{company}', '"IKEA"')
WHERE metadata->>'company' = 'Ikea';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{company}', '"IKEA"')
WHERE metadata->>'company' = 'Ikea';

DELETE FROM metadatavalues
WHERE value = 'Ikea'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'company');

-- ---------------------------------------------------------------------------
-- company: JB Hi-fi -> JB HiFi
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{company}', '"JB HiFi"')
WHERE metadata->>'company' = 'JB Hi-fi';

UPDATE donations
SET metadata = jsonb_set(metadata, '{company}', '"JB HiFi"')
WHERE metadata->>'company' = 'JB Hi-fi';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{company}', '"JB HiFi"')
WHERE metadata->>'company' = 'JB Hi-fi';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{company}', '"JB HiFi"')
WHERE metadata->>'company' = 'JB Hi-fi';

DELETE FROM metadatavalues
WHERE value = 'JB Hi-fi'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'company');

-- ---------------------------------------------------------------------------
-- company: Katmandu -> Kathmandu
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{company}', '"Kathmandu"')
WHERE metadata->>'company' = 'Katmandu';

UPDATE donations
SET metadata = jsonb_set(metadata, '{company}', '"Kathmandu"')
WHERE metadata->>'company' = 'Katmandu';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{company}', '"Kathmandu"')
WHERE metadata->>'company' = 'Katmandu';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{company}', '"Kathmandu"')
WHERE metadata->>'company' = 'Katmandu';

DELETE FROM metadatavalues
WHERE value = 'Katmandu'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'company');

-- ---------------------------------------------------------------------------
-- company: Monash Doctor Surgery -> Monash Doctors Surgery
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{company}', '"Monash Doctors Surgery"')
WHERE metadata->>'company' = 'Monash Doctor Surgery';

UPDATE donations
SET metadata = jsonb_set(metadata, '{company}', '"Monash Doctors Surgery"')
WHERE metadata->>'company' = 'Monash Doctor Surgery';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{company}', '"Monash Doctors Surgery"')
WHERE metadata->>'company' = 'Monash Doctor Surgery';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{company}', '"Monash Doctors Surgery"')
WHERE metadata->>'company' = 'Monash Doctor Surgery';

DELETE FROM metadatavalues
WHERE value = 'Monash Doctor Surgery'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'company');

-- ---------------------------------------------------------------------------
-- company: Paterson Cheney -> Patterson Cheney
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{company}', '"Patterson Cheney"')
WHERE metadata->>'company' = 'Paterson Cheney';

UPDATE donations
SET metadata = jsonb_set(metadata, '{company}', '"Patterson Cheney"')
WHERE metadata->>'company' = 'Paterson Cheney';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{company}', '"Patterson Cheney"')
WHERE metadata->>'company' = 'Paterson Cheney';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{company}', '"Patterson Cheney"')
WHERE metadata->>'company' = 'Paterson Cheney';

DELETE FROM metadatavalues
WHERE value = 'Paterson Cheney'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'company');

-- ---------------------------------------------------------------------------
-- company: Power Direct -> PowerDirect
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{company}', '"PowerDirect"')
WHERE metadata->>'company' = 'Power Direct';

UPDATE donations
SET metadata = jsonb_set(metadata, '{company}', '"PowerDirect"')
WHERE metadata->>'company' = 'Power Direct';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{company}', '"PowerDirect"')
WHERE metadata->>'company' = 'Power Direct';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{company}', '"PowerDirect"')
WHERE metadata->>'company' = 'Power Direct';

DELETE FROM metadatavalues
WHERE value = 'Power Direct'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'company');

-- ---------------------------------------------------------------------------
-- company: Simon Candy, Simon Candy Guitar -> Simon Candy School of Guitar
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{company}', '"Simon Candy School of Guitar"')
WHERE metadata->>'company' IN ('Simon Candy', 'Simon Candy Guitar');

UPDATE donations
SET metadata = jsonb_set(metadata, '{company}', '"Simon Candy School of Guitar"')
WHERE metadata->>'company' IN ('Simon Candy', 'Simon Candy Guitar');

UPDATE refdata
SET metadata = jsonb_set(metadata, '{company}', '"Simon Candy School of Guitar"')
WHERE metadata->>'company' IN ('Simon Candy', 'Simon Candy Guitar');

UPDATE transactions
SET metadata = jsonb_set(metadata, '{company}', '"Simon Candy School of Guitar"')
WHERE metadata->>'company' IN ('Simon Candy', 'Simon Candy Guitar');

DELETE FROM metadatavalues
WHERE value IN ('Simon Candy', 'Simon Candy Guitar')
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'company');

-- ---------------------------------------------------------------------------
-- company: Spec Savers, SpecSavers -> Specsavers
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{company}', '"Specsavers"')
WHERE metadata->>'company' IN ('Spec Savers', 'SpecSavers');

UPDATE donations
SET metadata = jsonb_set(metadata, '{company}', '"Specsavers"')
WHERE metadata->>'company' IN ('Spec Savers', 'SpecSavers');

UPDATE refdata
SET metadata = jsonb_set(metadata, '{company}', '"Specsavers"')
WHERE metadata->>'company' IN ('Spec Savers', 'SpecSavers');

UPDATE transactions
SET metadata = jsonb_set(metadata, '{company}', '"Specsavers"')
WHERE metadata->>'company' IN ('Spec Savers', 'SpecSavers');

DELETE FROM metadatavalues
WHERE value IN ('Spec Savers', 'SpecSavers')
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'company');

-- ---------------------------------------------------------------------------
-- type: Body Corp -> Body Corporate
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{type}', '"Body Corporate"')
WHERE metadata->>'type' = 'Body Corp';

UPDATE donations
SET metadata = jsonb_set(metadata, '{type}', '"Body Corporate"')
WHERE metadata->>'type' = 'Body Corp';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{type}', '"Body Corporate"')
WHERE metadata->>'type' = 'Body Corp';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{type}', '"Body Corporate"')
WHERE metadata->>'type' = 'Body Corp';

DELETE FROM metadatavalues
WHERE value = 'Body Corp'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'type');

-- ---------------------------------------------------------------------------
-- type: Owners Corp -> Owners Corporation
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{type}', '"Owners Corporation"')
WHERE metadata->>'type' = 'Owners Corp';

UPDATE donations
SET metadata = jsonb_set(metadata, '{type}', '"Owners Corporation"')
WHERE metadata->>'type' = 'Owners Corp';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{type}', '"Owners Corporation"')
WHERE metadata->>'type' = 'Owners Corp';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{type}', '"Owners Corporation"')
WHERE metadata->>'type' = 'Owners Corp';

DELETE FROM metadatavalues
WHERE value = 'Owners Corp'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'type');

-- ---------------------------------------------------------------------------
-- car: Ford 2001 -> Fairmont 2001
-- ---------------------------------------------------------------------------
UPDATE documents
SET metadata = jsonb_set(metadata, '{car}', '"Fairmont 2001"')
WHERE metadata->>'car' = 'Ford 2001';

UPDATE donations
SET metadata = jsonb_set(metadata, '{car}', '"Fairmont 2001"')
WHERE metadata->>'car' = 'Ford 2001';

UPDATE refdata
SET metadata = jsonb_set(metadata, '{car}', '"Fairmont 2001"')
WHERE metadata->>'car' = 'Ford 2001';

UPDATE transactions
SET metadata = jsonb_set(metadata, '{car}', '"Fairmont 2001"')
WHERE metadata->>'car' = 'Ford 2001';

DELETE FROM metadatavalues
WHERE value = 'Ford 2001'
	AND metadatakeyid = (SELECT id FROM metadatakeys WHERE name = 'car');

COMMIT;
