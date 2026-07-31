-- Optional folder path for notifications that should open a documents folder (e.g. payslips).
--
-- RUN WITH psql:
--   PGPASSWORD=... psql -h localhost -p 5430 -U postgres -d expensemanager -v ON_ERROR_STOP=1 -f scripts/S17-add-notification-document-folder-path.sql

BEGIN;

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS documentfolderpath varchar(512);

COMMIT;
