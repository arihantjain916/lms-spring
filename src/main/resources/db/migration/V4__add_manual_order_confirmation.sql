-- Lets an admin confirm an offline payment (UPI/bank transfer/cash) by hand, since
-- there is no payment gateway wired up yet to call the webhook.
-- Types match what Hibernate derives from the entity so ddl-auto=update finds nothing to change.

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(255);

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS confirmed_by_id VARCHAR(255);

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMP(6);

DO
$$
    BEGIN
        ALTER TABLE payments
            ADD CONSTRAINT fk_payments_confirmed_by FOREIGN KEY (confirmed_by_id) REFERENCES users (id);
    EXCEPTION
        WHEN duplicate_object THEN NULL;
    END
$$;
