ALTER TABLE IF EXISTS app_users
    ADD COLUMN IF NOT EXISTS password_change_required boolean DEFAULT false;

UPDATE app_users
SET password_change_required = false
WHERE password_change_required IS NULL;

ALTER TABLE IF EXISTS app_users
    ALTER COLUMN password_change_required SET NOT NULL;

ALTER TABLE IF EXISTS app_users
    ADD COLUMN IF NOT EXISTS password_expires_at timestamp with time zone;

ALTER TABLE IF EXISTS customers
    ADD COLUMN IF NOT EXISTS date_of_birth date;

ALTER TABLE IF EXISTS notifications
    ADD COLUMN IF NOT EXISTS related_reference varchar(255);

CREATE UNIQUE INDEX IF NOT EXISTS uk_notifications_related_reference
    ON notifications (related_reference)
    WHERE related_reference IS NOT NULL;

ALTER TABLE IF EXISTS bills
    ADD COLUMN IF NOT EXISTS late_penalty_applied_at timestamp with time zone;
