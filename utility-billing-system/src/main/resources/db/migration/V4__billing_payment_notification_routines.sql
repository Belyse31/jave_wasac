CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION utility_bill_month_year(p_year integer, p_month integer)
RETURNS text AS $$
BEGIN
    RETURN trim(to_char(make_date(p_year, p_month, 1), 'FMMonth/YYYY'));
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_bill_notification()
RETURNS trigger AS $$
DECLARE
    customer_record record;
    bill_month_year text;
    bill_message text;
BEGIN
    SELECT c.full_name, c.email, c.phone_number
    INTO customer_record
    FROM customers c
    WHERE c.id = NEW.customer_id;

    bill_month_year := utility_bill_month_year(NEW.billing_year, NEW.billing_month);

    bill_message := 'Dear ' || customer_record.full_name || ',' || chr(10) ||
                    'Your ' || bill_month_year || ' utility bill of ' ||
                    NEW.total_amount || ' FRW has been successfully processed.';

    INSERT INTO notifications (
        id,
        created_at,
        updated_at,
        recipient_email,
        recipient_phone,
        type,
        subject,
        message,
        sent,
        related_reference
    )
    VALUES (
        gen_random_uuid(),
        now(),
        now(),
        customer_record.email,
        customer_record.phone_number,
        'BILL_GENERATION',
        'Utility bill generated',
        bill_message,
        false,
        'BILL_GENERATED:' || NEW.bill_reference
    )
    ON CONFLICT (related_reference) DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_bill_after_payment()
RETURNS trigger AS $$
DECLARE
    bill_record record;
    total_paid numeric(18, 2);
    new_balance numeric(18, 2);
    bill_month_year text;
    payment_message text;
BEGIN
    SELECT b.id,
           b.bill_reference,
           b.total_amount,
           b.billing_year,
           b.billing_month,
           c.full_name,
           c.email,
           c.phone_number
    INTO bill_record
    FROM bills b
    JOIN customers c ON c.id = b.customer_id
    WHERE b.id = NEW.bill_id;

    SELECT coalesce(sum(p.amount_paid), 0)
    INTO total_paid
    FROM payments p
    WHERE p.bill_id = NEW.bill_id;

    new_balance := greatest(bill_record.total_amount - total_paid, 0);

    UPDATE bills
    SET outstanding_balance = new_balance,
        status = CASE
            WHEN new_balance <= 0 THEN 'PAID'
            WHEN total_paid > 0 THEN 'PARTIALLY_PAID'
            ELSE status
        END,
        updated_at = now()
    WHERE id = NEW.bill_id;

    IF new_balance <= 0 THEN
        bill_month_year := utility_bill_month_year(bill_record.billing_year, bill_record.billing_month);

        payment_message := 'Dear ' || bill_record.full_name || ',' || chr(10) ||
                           'Your ' || bill_month_year || ' utility bill of ' ||
                           bill_record.total_amount || ' FRW has been successfully processed.';

        INSERT INTO notifications (
            id,
            created_at,
            updated_at,
            recipient_email,
            recipient_phone,
            type,
            subject,
            message,
            sent,
            related_reference
        )
        VALUES (
            gen_random_uuid(),
            now(),
            now(),
            bill_record.email,
            bill_record.phone_number,
            'PAYMENT_CONFIRMATION',
            'Utility bill fully paid',
            payment_message,
            false,
            'BILL_FULLY_PAID:' || bill_record.bill_reference
        )
        ON CONFLICT (related_reference) DO NOTHING;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bill_notification ON bills;
CREATE TRIGGER trg_bill_notification
    AFTER INSERT ON bills
    FOR EACH ROW
    EXECUTE FUNCTION create_bill_notification();

DROP TRIGGER IF EXISTS trg_payment_bill_status_notification ON payments;
CREATE TRIGGER trg_payment_bill_status_notification
    AFTER INSERT ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_bill_after_payment();

