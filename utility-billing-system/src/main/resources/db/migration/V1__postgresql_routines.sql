CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION create_bill_notification()
RETURNS trigger AS $$
BEGIN
    INSERT INTO notifications (id, created_at, updated_at, recipient_email, recipient_phone, type, subject, message, sent)
    VALUES (gen_random_uuid(), now(), now(), NULL, NULL, 'BILL_GENERATION', 'Bill generated',
            'Dear customer, your utility bill ' || NEW.bill_reference || ' of ' || NEW.total_amount || ' FRW has been successfully processed.',
            false);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mark_paid_bill()
RETURNS trigger AS $$
BEGIN
    IF NEW.outstanding_balance <= 0 THEN
        NEW.status := 'PAID';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE generate_monthly_bills(p_year integer, p_month integer, p_due_date date)
LANGUAGE plpgsql
AS $$
DECLARE
    reading_record record;
    reading_cursor cursor FOR
        SELECT mr.*
        FROM meter_readings mr
        WHERE mr.billing_year = p_year
          AND mr.billing_month = p_month
          AND NOT EXISTS (
              SELECT 1 FROM bills b
              WHERE b.meter_id = mr.meter_id
                AND b.billing_year = p_year
                AND b.billing_month = p_month
          );
BEGIN
    OPEN reading_cursor;
    LOOP
        FETCH reading_cursor INTO reading_record;
        EXIT WHEN NOT FOUND;
        INSERT INTO notifications (id, created_at, updated_at, type, subject, message, sent)
        VALUES (gen_random_uuid(), now(), now(), 'BILL_GENERATION', 'Monthly billing queued',
                'Monthly billing record processed for reading ' || reading_record.id, false);
    END LOOP;
    CLOSE reading_cursor;
END;
$$;

DO $$
BEGIN
    IF to_regclass('public.bills') IS NOT NULL AND to_regclass('public.notifications') IS NOT NULL THEN
        DROP TRIGGER IF EXISTS trg_bill_notification ON bills;
        CREATE TRIGGER trg_bill_notification
            AFTER INSERT ON bills
            FOR EACH ROW
            EXECUTE FUNCTION create_bill_notification();

        DROP TRIGGER IF EXISTS trg_bill_paid_status ON bills;
        CREATE TRIGGER trg_bill_paid_status
            BEFORE INSERT OR UPDATE OF outstanding_balance ON bills
            FOR EACH ROW
            EXECUTE FUNCTION mark_paid_bill();
    END IF;
END;
$$;
