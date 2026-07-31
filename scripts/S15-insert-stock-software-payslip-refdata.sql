-- RefData row for Stock Software payslip email processing.
-- Run once in each environment where EmailTrawler should handle payslips.
INSERT INTO refdata (id, description, type, emailkey, emailprocessor, deleted)
SELECT nextval('refdata_seq'), 'Stock Software Payslip', 'INCOME_TYPE', 'Payslip', 'STOCK_SOFTWARE_PAYSLIP', false
WHERE NOT EXISTS (
	SELECT 1 FROM refdata WHERE emailprocessor = 'STOCK_SOFTWARE_PAYSLIP' AND deleted = false
);
