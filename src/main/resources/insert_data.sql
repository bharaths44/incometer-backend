-- Sample data for Incometer application - 6 months of data (June 2025 - November 2025)

-- Define user ID variable for example data
-- Using a fixed UUID for the example user
DO $$
DECLARE example_user_id UUID := 'fb9ef875-ddf0-4dca-a81f-835953e03f96';
BEGIN

-- Insert user
INSERT INTO users (user_id, name, email, phone_number, password, created_at)
VALUES (example_user_id, 'bharath', 'bharathsatheesan@gmail.com', '+919605003511', 'achu1234', NOW());

-- Insert payment methods
INSERT INTO payment_methods (name, display_name, last_four_digits, issuer_name, type, icon, user_id, created_at)
VALUES ('Cash', 'Cash', null, null, 'CASH', 'banknote', example_user_id, NOW());
INSERT INTO payment_methods (name, display_name, last_four_digits, issuer_name, type, icon, user_id, created_at)
VALUES ('HDFC Credit Card', 'HDFC Credit Card', '1234', 'HDFC', 'CREDIT_CARD', 'credit-card', example_user_id, NOW());
INSERT INTO payment_methods (name, display_name, last_four_digits, issuer_name, type, icon, user_id, created_at)
VALUES ('SBI Debit Card', 'SBI Debit Card', '5678', 'SBI', 'DEBIT_CARD', 'credit-card', example_user_id, NOW());

-- Insert categories (assuming user_id=example_user_id)
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Food', 'utensils', 'EXPENSE', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Transportation', 'car', 'EXPENSE', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Shopping', 'shopping-bag', 'EXPENSE', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Entertainment', 'film', 'EXPENSE', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Utilities', 'bolt', 'EXPENSE', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Healthcare', 'heart', 'EXPENSE', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Education', 'book', 'EXPENSE', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Investments', 'chart-line', 'EXPENSE', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Salary', 'dollar-sign', 'INCOME', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Freelance', 'briefcase', 'INCOME', example_user_id, NOW());
INSERT INTO categories (name, icon, type, user_id, created_at)
VALUES ('Bonus', 'gift', 'INCOME', example_user_id, NOW());

-- ============================================================================
-- JUNE 2025 TRANSACTIONS
-- ============================================================================

-- Income - June
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (5000.00, 'Monthly Salary', '2025-06-01', 'INCOME', example_user_id, 9, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (1200.00, 'Freelance Website Project', '2025-06-15', 'INCOME', example_user_id, 10, 2, NOW());

-- Expenses - June
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (45.50, 'Grocery shopping', '2025-06-02', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (25.00, 'Lunch with colleagues', '2025-06-05', 'EXPENSE', example_user_id, 1, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (15.00, 'Bus pass', '2025-06-03', 'EXPENSE', example_user_id, 2, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (60.00, 'Uber rides', '2025-06-10', 'EXPENSE', example_user_id, 2, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (120.00, 'New shoes', '2025-06-08', 'EXPENSE', example_user_id, 3, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (30.00, 'Movie tickets', '2025-06-12', 'EXPENSE', example_user_id, 4, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (85.00, 'Electricity bill', '2025-06-05', 'EXPENSE', example_user_id, 5, 3, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (500.00, 'Monthly investment - Mutual funds', '2025-06-01', 'EXPENSE', example_user_id, 8, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (35.00, 'Dinner at restaurant', '2025-06-18', 'EXPENSE', example_user_id, 1, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (150.00, 'Online course subscription', '2025-06-20', 'EXPENSE', example_user_id, 7, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (50.00, 'Medical checkup', '2025-06-22', 'EXPENSE', example_user_id, 6, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (40.00, 'Coffee and snacks', '2025-06-25', 'EXPENSE', example_user_id, 1, 1, NOW());

-- ============================================================================
-- JULY 2025 TRANSACTIONS
-- ============================================================================

-- Income - July
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (5000.00, 'Monthly Salary', '2025-07-01', 'INCOME', example_user_id, 9, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (800.00, 'Freelance Logo Design', '2025-07-12', 'INCOME', example_user_id, 10, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (1000.00, 'Performance Bonus', '2025-07-15', 'INCOME', example_user_id, 11, 2, NOW());

-- Expenses - July
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (55.00, 'Grocery shopping', '2025-07-03', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (28.00, 'Breakfast at cafe', '2025-07-06', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (75.00, 'Gas for car', '2025-07-02', 'EXPENSE', example_user_id, 2, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (200.00, 'Clothing shopping', '2025-07-10', 'EXPENSE', example_user_id, 3, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (45.00, 'Netflix subscription', '2025-07-01', 'EXPENSE', example_user_id, 4, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (90.00, 'Internet bill', '2025-07-05', 'EXPENSE', example_user_id, 5, 3, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (500.00, 'Monthly investment - Mutual funds', '2025-07-01', 'EXPENSE', example_user_id, 8, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (65.00, 'Dinner with family', '2025-07-14', 'EXPENSE', example_user_id, 1, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (180.00, 'Books purchase', '2025-07-18', 'EXPENSE', example_user_id, 7, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (120.00, 'Pharmacy - medicines', '2025-07-20', 'EXPENSE', example_user_id, 6, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (35.00, 'Fast food', '2025-07-22', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (50.00, 'Concert tickets', '2025-07-25', 'EXPENSE', example_user_id, 4, 2, NOW());

-- ============================================================================
-- AUGUST 2025 TRANSACTIONS
-- ============================================================================

-- Income - August
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (5000.00, 'Monthly Salary', '2025-08-01', 'INCOME', example_user_id, 9, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (1500.00, 'Freelance Mobile App Development', '2025-08-20', 'INCOME', example_user_id, 10, 2, NOW());

-- Expenses - August
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (60.00, 'Grocery shopping', '2025-08-02', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (42.00, 'Lunch buffet', '2025-08-05', 'EXPENSE', example_user_id, 1, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (80.00, 'Train tickets', '2025-08-03', 'EXPENSE', example_user_id, 2, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (250.00, 'Electronics - headphones', '2025-08-08', 'EXPENSE', example_user_id, 3, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (40.00, 'Gaming subscription', '2025-08-10', 'EXPENSE', example_user_id, 4, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (95.00, 'Water and sewage bill', '2025-08-05', 'EXPENSE', example_user_id, 5, 3, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (500.00, 'Monthly investment - Mutual funds', '2025-08-01', 'EXPENSE', example_user_id, 8, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (55.00, 'Pizza night', '2025-08-12', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (200.00, 'Professional certification exam', '2025-08-15', 'EXPENSE', example_user_id, 7, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (85.00, 'Dental checkup', '2025-08-18', 'EXPENSE', example_user_id, 6, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (70.00, 'Gas for car', '2025-08-22', 'EXPENSE', example_user_id, 2, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (48.00, 'Grocery refill', '2025-08-25', 'EXPENSE', example_user_id, 1, 1, NOW());

-- ============================================================================
-- SEPTEMBER 2025 TRANSACTIONS
-- ============================================================================

-- Income - September
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (5000.00, 'Monthly Salary', '2025-09-01', 'INCOME', example_user_id, 9, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (950.00, 'Freelance Consulting', '2025-09-18', 'INCOME', example_user_id, 10, 2, NOW());

-- Expenses - September
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (52.00, 'Grocery shopping', '2025-09-02', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (38.00, 'Sushi restaurant', '2025-09-07', 'EXPENSE', example_user_id, 1, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (25.00, 'Parking fees', '2025-09-04', 'EXPENSE', example_user_id, 2, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (150.00, 'Home decor items', '2025-09-10', 'EXPENSE', example_user_id, 3, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (55.00, 'Theater play tickets', '2025-09-14', 'EXPENSE', example_user_id, 4, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (88.00, 'Electricity bill', '2025-09-05', 'EXPENSE', example_user_id, 5, 3, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (500.00, 'Monthly investment - Mutual funds', '2025-09-01', 'EXPENSE', example_user_id, 8, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (75.00, 'Birthday dinner', '2025-09-16', 'EXPENSE', example_user_id, 1, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (120.00, 'Workshop registration', '2025-09-20', 'EXPENSE', example_user_id, 7, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (65.00, 'Vitamins and supplements', '2025-09-22', 'EXPENSE', example_user_id, 6, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (45.00, 'Coffee shop', '2025-09-25', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (90.00, 'Internet bill', '2025-09-05', 'EXPENSE', example_user_id, 5, 3, NOW());

-- ============================================================================
-- OCTOBER 2025 TRANSACTIONS
-- ============================================================================

-- Income - October
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (5000.00, 'Monthly Salary', '2025-10-01', 'INCOME', example_user_id, 9, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (1100.00, 'Freelance Content Writing', '2025-10-15', 'INCOME', example_user_id, 10, 2, NOW());

-- Expenses - October
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (58.00, 'Grocery shopping', '2025-10-03', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (32.00, 'Lunch at food court', '2025-10-06', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (65.00, 'Taxi rides', '2025-10-05', 'EXPENSE', example_user_id, 2, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (180.00, 'New jacket', '2025-10-10', 'EXPENSE', example_user_id, 3, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (35.00, 'Movie night', '2025-10-12', 'EXPENSE', example_user_id, 4, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (92.00, 'Gas bill', '2025-10-05', 'EXPENSE', example_user_id, 5, 3, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (500.00, 'Monthly investment - Mutual funds', '2025-10-01', 'EXPENSE', example_user_id, 8, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (50.00, 'Restaurant dinner', '2025-10-18', 'EXPENSE', example_user_id, 1, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (95.00, 'Online course', '2025-10-20', 'EXPENSE', example_user_id, 7, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (110.00, 'Doctor consultation', '2025-10-22', 'EXPENSE', example_user_id, 6, 2, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (40.00, 'Snacks and drinks', '2025-10-25', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (75.00, 'Gas for car', '2025-10-28', 'EXPENSE', example_user_id, 2, 2, NOW());

-- ============================================================================
-- NOVEMBER 2025 TRANSACTIONS
-- ============================================================================

-- Income - November
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (5000.00, 'Monthly Salary', '2025-11-01', 'INCOME', example_user_id, 9, 2, NOW());

-- Expenses - November (partial month)
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (62.00, 'Grocery shopping', '2025-11-02', 'EXPENSE', example_user_id, 1, 1, NOW());
INSERT INTO transactions (amount, description, transaction_date, transaction_type, user_id, category_id,
                          payment_method_id, created_at)
VALUES (500.00, 'Monthly investment - Mutual funds', '2025-11-01', 'EXPENSE', example_user_id, 8, 2, NOW());

-- ============================================================================
-- BUDGETS
-- ============================================================================

-- Monthly budgets for June 2025
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (400.00, '2025-06-01', '2025-06-30', 'MONTHLY', false, example_user_id, 1, 'LIMIT', NOW());
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (200.00, '2025-06-01', '2025-06-30', 'MONTHLY', false, example_user_id, 2, 'LIMIT', NOW());

-- Monthly budgets for July 2025
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (450.00, '2025-07-01', '2025-07-31', 'MONTHLY', false, example_user_id, 1, 'LIMIT', NOW());
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (250.00, '2025-07-01', '2025-07-31', 'MONTHLY', false, example_user_id, 2, 'LIMIT', NOW());

-- Monthly budgets for August 2025
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (500.00, '2025-08-01', '2025-08-31', 'MONTHLY', false, example_user_id, 1, 'LIMIT', NOW());
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (200.00, '2025-08-01', '2025-08-31', 'MONTHLY', false, example_user_id, 2, 'LIMIT', NOW());

-- Monthly budgets for September 2025
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (400.00, '2025-09-01', '2025-09-30', 'MONTHLY', false, example_user_id, 1, 'LIMIT', NOW());
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (180.00, '2025-09-01', '2025-09-30', 'MONTHLY', false, example_user_id, 2, 'LIMIT', NOW());

-- Monthly budgets for October 2025
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (450.00, '2025-10-01', '2025-10-31', 'MONTHLY', false, example_user_id, 1, 'LIMIT', NOW());
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (220.00, '2025-10-01', '2025-10-31', 'MONTHLY', false, example_user_id, 2, 'LIMIT', NOW());

-- Monthly budgets for November 2025 (current/active)
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (500.00, '2025-11-01', '2025-11-30', 'MONTHLY', true, example_user_id, 1, 'LIMIT', NOW());
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (250.00, '2025-11-01', '2025-11-30', 'MONTHLY', true, example_user_id, 2, 'LIMIT', NOW());
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (300.00, '2025-11-01', '2025-11-30', 'MONTHLY', true, example_user_id, 3, 'LIMIT', NOW());

-- Yearly budget for Food
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (6000.00, '2025-01-01', '2025-12-31', 'YEARLY', true, example_user_id, 1, 'LIMIT', NOW());

-- Yearly target for Investments
INSERT INTO budgets (amount, start_date, end_date, frequency, is_active, user_id, category_id, type, created_at)
VALUES (6000.00, '2025-01-01', '2025-12-31', 'YEARLY', true, example_user_id, 8, 'TARGET', NOW());

END $$;

-- ============================================================================
-- USER STATS VIEW
-- ============================================================================

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
       MAX(t.created_at)                                                                   AS last_transaction_date

FROM users u
         LEFT JOIN transactions t ON u.user_id = t.user_id
GROUP BY u.user_id, u.name, u.email, u.created_at;

END ;
