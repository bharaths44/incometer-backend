-- summary table
CREATE TABLE user_stats (
                            user_id bigint PRIMARY KEY,
                            total_transactions bigint NOT NULL DEFAULT 0,
                            total_expenses bigint NOT NULL DEFAULT 0,
                            total_income bigint NOT NULL DEFAULT 0,
                            total_expense_amount numeric(20,2) NOT NULL DEFAULT 0,
                            total_income_amount numeric(20,2) NOT NULL DEFAULT 0,
                            net_balance numeric(20,2) NOT NULL DEFAULT 0,
                            total_days_logged bigint NOT NULL DEFAULT 0,
                            first_transaction_date timestamp with time zone,
                            last_transaction_date timestamp with time zone
);

-- initialize from existing data
INSERT INTO user_stats (user_id, total_transactions, total_expenses, total_income, total_expense_amount, total_income_amount, net_balance, total_days_logged, first_transaction_date, last_transaction_date)
SELECT
    u.user_id,
    COUNT(t.transaction_id),
    COUNT(*) FILTER (WHERE t.transaction_type = 'EXPENSE'),
    COUNT(*) FILTER (WHERE t.transaction_type = 'INCOME'),
    COALESCE(SUM(t.amount) FILTER (WHERE t.transaction_type = 'EXPENSE'),0),
    COALESCE(SUM(t.amount) FILTER (WHERE t.transaction_type = 'INCOME'),0),
    COALESCE(SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE -t.amount END),0),
    COUNT(DISTINCT t.transaction_date::date),
    MIN(t.created_at),
    MAX(t.created_at)
FROM users u
         LEFT JOIN transactions t ON u.user_id = t.user_id
GROUP BY u.user_id;

-- trigger function (simplified)
CREATE OR REPLACE FUNCTION trg_update_user_stats() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    inc_tx integer;
    inc_exp integer;
    inc_inc integer;
    amt numeric;
    old_amt numeric;
BEGIN
    IF TG_OP = 'INSERT' THEN
        inc_tx := 1;
        inc_exp := CASE WHEN NEW.transaction_type = 'EXPENSE' THEN 1 ELSE 0 END;
        inc_inc := CASE WHEN NEW.transaction_type = 'INCOME' THEN 1 ELSE 0 END;
        amt := COALESCE(NEW.amount,0);
        UPDATE user_stats
        SET
            total_transactions = total_transactions + inc_tx,
            total_expenses = total_expenses + inc_exp,
            total_income = total_income + inc_inc,
            total_expense_amount = total_expense_amount + CASE WHEN NEW.transaction_type='EXPENSE' THEN amt ELSE 0 END,
            total_income_amount = total_income_amount + CASE WHEN NEW.transaction_type='INCOME' THEN amt ELSE 0 END,
            net_balance = net_balance + (CASE WHEN NEW.transaction_type='INCOME' THEN amt ELSE -amt END),
            last_transaction_date = GREATEST(COALESCE(last_transaction_date, NEW.created_at), NEW.created_at),
            first_transaction_date = LEAST(COALESCE(first_transaction_date, NEW.created_at), NEW.created_at)
        WHERE user_id = NEW.user_id;

        -- if row does not exist create it
        IF NOT FOUND THEN
            INSERT INTO user_stats (user_id, total_transactions, total_expenses, total_income, total_expense_amount, total_income_amount, net_balance, first_transaction_date, last_transaction_date)
            VALUES (NEW.user_id, 1, inc_exp, inc_inc,
                    CASE WHEN NEW.transaction_type='EXPENSE' THEN amt ELSE 0 END,
                    CASE WHEN NEW.transaction_type='INCOME' THEN amt ELSE 0 END,
                    CASE WHEN NEW.transaction_type='INCOME' THEN amt ELSE -amt END,
                    NEW.created_at, NEW.created_at);
        END IF;

        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        old_amt := COALESCE(OLD.amount,0);
        UPDATE user_stats
        SET
            total_transactions = total_transactions - 1,
            total_expenses = total_expenses - CASE WHEN OLD.transaction_type='EXPENSE' THEN 1 ELSE 0 END,
            total_income = total_income - CASE WHEN OLD.transaction_type='INCOME' THEN 1 ELSE 0 END,
            total_expense_amount = total_expense_amount - CASE WHEN OLD.transaction_type='EXPENSE' THEN old_amt ELSE 0 END,
            total_income_amount = total_income_amount - CASE WHEN OLD.transaction_type='INCOME' THEN old_amt ELSE 0 END,
            net_balance = net_balance - (CASE WHEN OLD.transaction_type='INCOME' THEN old_amt ELSE -old_amt END)
        WHERE user_id = OLD.user_id;
        -- NOTE: recomputing first/last transaction date on delete is tricky — you may need to recalc from transactions table for that user occasionally.
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' THEN
        -- handle type/amount changes: subtract OLD, add NEW (similar logic)
        PERFORM 1; -- omitted for brevity — implement symmetric of DELETE + INSERT
        RETURN NEW;
    END IF;
END;
$$;

-- attach trigger
CREATE TRIGGER transactions_after_write
    AFTER INSERT OR UPDATE OR DELETE ON transactions
    FOR EACH ROW EXECUTE FUNCTION trg_update_user_stats();
