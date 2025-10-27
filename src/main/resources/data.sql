-- Sample data for Incometer application

-- Insert user
INSERT INTO users (name, email, phone_number, password, created_at)
VALUES ('bharath', 'bharathsatheesan@gmail.com', '+919605003511', 'achu1234', NOW());

-- Insert categories (assuming user_id=1)
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Food', 'utensils', 'EXPENSE', 1, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Transportation', 'car', 'EXPENSE', 1, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Salary', 'dollar-sign', 'INCOME', 1, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Freelance', 'briefcase', 'INCOME', 1, NOW());

-- Insert expenses (assuming category_id=1 for Food, 2 for Transportation)
INSERT INTO expenses (amount, description, payment_method, expense_date, user_id, category_id, created_at)
VALUES (50.00, 'Lunch at restaurant', 'Cash', '2025-10-25', 1, 1, NOW());
INSERT INTO expenses (amount, description, payment_method, expense_date, user_id, category_id, created_at)
VALUES (20.00, 'Bus fare', 'Card', '2025-10-24', 1, 2, NOW());

-- Insert incomes (assuming category_id=3 for Salary, 4 for Freelance)
INSERT INTO incomes (amount, source, received_date, user_id, created_at)
VALUES (5000.00, 'Monthly Salary', '2025-10-01', 1, NOW());
INSERT INTO incomes (amount, source, received_date, user_id, created_at)
VALUES (1000.00, 'Freelance Project', '2025-10-15', 1, NOW());

-- Additional budget examples
-- Yearly budget for Food
INSERT INTO budgets (amount_limit, start_date, end_date, frequency, is_active, user_id, category_id, created_at)
VALUES (6000.00, '2025-01-01', '2025-12-31', 'YEARLY', true, 1, 1, NOW());

-- One-time budget for Transportation (expired)
INSERT INTO budgets (amount_limit, start_date, end_date, frequency, is_active, user_id, category_id, created_at)
VALUES (150.00, '2025-09-01', '2025-09-30', 'ONE_TIME', false, 1, 2, NOW());

-- Weekly budget for Food (current week)
INSERT INTO budgets (amount_limit, start_date, end_date, frequency, is_active, user_id, category_id, created_at)
VALUES (100.00, '2025-10-21', '2025-10-27', 'WEEKLY', true, 1, 1, NOW());
