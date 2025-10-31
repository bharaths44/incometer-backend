-- Sample data for Incometer application

-- Insert user
INSERT INTO users (name, email, phone_number, password, created_at)
VALUES ('bharath', 'bharathsatheesan@gmail.com', '+919605003511', 'achu1234', NOW());

-- Insert payment methods
INSERT INTO payment_methods (name, display_name, last_four_digits, issuer_name, type, icon, user_id, created_at)
VALUES ('Cash', 'Cash', null, null, 'CASH', 'banknote', 1, NOW());
INSERT INTO payment_methods (name, display_name, last_four_digits, issuer_name, type, icon, user_id, created_at)
VALUES ('Card', 'HDFC Credit Card', '1234', 'HDFC', 'CREDIT_CARD', 'credit-card', 1, NOW());

-- Insert categories (assuming user_id=1)
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Food', 'utensils', 'EXPENSE', 1, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Transportation', 'car', 'EXPENSE', 1, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Salary', 'dollar-sign', 'INCOME', 1, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Freelance', 'briefcase', 'INCOME', 1, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Investments', 'chart-line', 'EXPENSE', 1, NOW());

-- Insert transactions (expenses)
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (50.00, 'Lunch at restaurant', '2025-10-25', 'EXPENSE', 1, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (20.00, 'Bus fare', '2025-10-24', 'EXPENSE', 1, 2, 2, NOW());

-- Insert transactions (incomes)
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (5000.00, 'Monthly Salary', '2025-10-01', 'INCOME', 1, 3, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (1000.00, 'Freelance Project', '2025-10-15', 'INCOME', 1, 4, 2, NOW());

-- Additional budget examples
-- Yearly budget for Food
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (6000.00, '2025-01-01', '2025-12-31', 'YEARLY', true, 1, 1, 'LIMIT', NOW());

-- One-time budget for Transportation (expired)
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (150.00, '2025-09-01', '2025-09-30', 'ONE_TIME', false, 1, 2, 'LIMIT', NOW());

-- Weekly budget for Food (current week)
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (100.00, '2025-10-21', '2025-10-27', 'WEEKLY', true, 1, 1, 'LIMIT', NOW());

-- Yearly target for Investments
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (12000.00, '2025-01-01', '2025-12-31', 'YEARLY', true, 1, 5, 'TARGET', NOW());
