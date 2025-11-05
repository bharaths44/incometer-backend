-- Create database view for user statistics
-- This view automatically updates as transactions are added/modified/deleted

CREATE OR REPLACE VIEW user_stats_view AS
SELECT u.user_id,
       u.name                                                                              AS user_name,
       u.email                                                                             AS user_email,
       u.created_at                                                                        AS account_created_at,

       -- Transaction counts
       COALESCE(COUNT(t.transaction_id), 0)                                                AS total_transactions,
       COALESCE(COUNT(CASE WHEN t.transaction_type = 'EXPENSE' THEN 1 END), 0)             AS total_expenses,
       COALESCE(COUNT(CASE WHEN t.transaction_type = 'INCOME' THEN 1 END), 0)              AS total_income,

       -- Transaction amounts
       COALESCE(SUM(CASE WHEN t.transaction_type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS total_expense_amount,
       COALESCE(SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE 0 END), 0)  AS total_income_amount,
       COALESCE(
               SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE -t.amount END),
               0
       )                                                                                   AS net_balance,

       -- Days logged (unique transaction dates)
       COALESCE(COUNT(DISTINCT t.transaction_date), 0)                                     AS total_days_logged,

       -- Transaction date statistics
       MIN(t.created_at)                                                                   AS first_transaction_date,
       MAX(t.created_at)                                                                   AS last_transaction_date,

       -- Days since account created
       CAST(EXTRACT(DAY FROM (CURRENT_TIMESTAMP - u.created_at)) AS BIGINT)                AS days_since_account_created

FROM users u
         LEFT JOIN transactions t ON u.user_id = t.user_id
GROUP BY u.user_id, u.name, u.email, u.created_at;

