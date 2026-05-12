-- ============================================================
--  FRAUD DETECTION SYSTEM - DATABASE SCHEMA & SAMPLE DATA
--  Run this script first before launching the Java app
-- ============================================================

-- 1. Create (or switch to) the database
CREATE DATABASE IF NOT EXISTS fraud_detection_db;
USE fraud_detection_db;

-- ============================================================
--  TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)   NOT NULL,
    email         VARCHAR(150)   UNIQUE NOT NULL,
    account_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--  TABLE: transactions
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    txn_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT            NOT NULL,
    amount        DECIMAL(15,2)  NOT NULL,
    txn_type      ENUM('DEPOSIT','WITHDRAW') NOT NULL,
    location      VARCHAR(100)   NOT NULL DEFAULT 'Local',
    txn_timestamp TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- ============================================================
--  TABLE: fraud_alerts
-- ============================================================
CREATE TABLE IF NOT EXISTS fraud_alerts (
    alert_id      INT AUTO_INCREMENT PRIMARY KEY,
    txn_id        INT            NOT NULL,
    fraud_reason  VARCHAR(255)   NOT NULL,
    status        ENUM('OPEN','REVIEWED','RESOLVED') NOT NULL DEFAULT 'OPEN',
    created_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alert_txn FOREIGN KEY (txn_id)
        REFERENCES transactions(txn_id) ON DELETE CASCADE
);

-- ============================================================
--  SAMPLE DATA – Users (10 records)
-- ============================================================
INSERT INTO users (name, email, account_balance) VALUES
('Alice Johnson',   'alice@example.com',   85000.00),
('Bob Smith',       'bob@example.com',     42000.50),
('Carol Williams',  'carol@example.com',   15000.00),
('David Brown',     'david@example.com',  120000.75),
('Eva Martinez',    'eva@example.com',      5000.00),
('Frank Lee',       'frank@example.com',   63000.00),
('Grace Kim',       'grace@example.com',   29000.00),
('Henry Wilson',    'henry@example.com',   71000.00),
('Irene Clark',     'irene@example.com',   33000.00),
('James Taylor',    'james@example.com',   98000.00);

-- ============================================================
--  SAMPLE DATA – Transactions (15 records, some fraudulent)
-- ============================================================
INSERT INTO transactions (user_id, amount, txn_type, location, txn_timestamp) VALUES
(1, 1000.00,  'DEPOSIT',  'Mumbai',    NOW() - INTERVAL 5 DAY),
(1, 55000.00, 'WITHDRAW', 'Dubai',     NOW() - INTERVAL 4 DAY),   -- HIGH AMOUNT + SUSPICIOUS LOCATION
(2, 2000.00,  'DEPOSIT',  'Delhi',     NOW() - INTERVAL 3 DAY),
(2, 2500.00,  'WITHDRAW', 'Delhi',     NOW() - INTERVAL 3 DAY),
(3, 75000.00, 'WITHDRAW', 'Unknown',   NOW() - INTERVAL 2 DAY),   -- HIGH AMOUNT + SUSPICIOUS LOCATION
(4, 500.00,   'DEPOSIT',  'Pune',      NOW() - INTERVAL 2 DAY),
(4, 51000.00, 'WITHDRAW', 'Lagos',     NOW() - INTERVAL 1 DAY),   -- HIGH AMOUNT + SUSPICIOUS LOCATION
(5, 100.00,   'DEPOSIT',  'Nagpur',    NOW() - INTERVAL 1 DAY),
(5, 100.00,   'WITHDRAW', 'Nagpur',    NOW() - INTERVAL 1 DAY),   -- RAPID REPEAT
(5, 100.00,   'WITHDRAW', 'Nagpur',    NOW() - INTERVAL 1 DAY),   -- RAPID REPEAT
(6, 3000.00,  'DEPOSIT',  'Bangalore', NOW() - INTERVAL 1 DAY),
(7, 200.00,   'WITHDRAW', 'Chennai',   NOW()),
(8, 62000.00, 'WITHDRAW', 'Unknown',   NOW()),                    -- HIGH AMOUNT + SUSPICIOUS LOCATION
(9, 15000.00, 'DEPOSIT',  'Hyderabad', NOW()),
(10,88000.00, 'WITHDRAW', 'Karachi',   NOW());                    -- HIGH AMOUNT + SUSPICIOUS LOCATION

-- ============================================================
--  SAMPLE DATA – Fraud Alerts (pre-seeded for demo)
-- ============================================================
INSERT INTO fraud_alerts (txn_id, fraud_reason, status) VALUES
(2,  'Transaction amount exceeds Rs.50,000; Suspicious foreign location: Dubai',  'OPEN'),
(7,  'Transaction amount exceeds Rs.50,000; Suspicious foreign location: Lagos',  'OPEN'),
(13, 'Transaction amount exceeds Rs.50,000; Suspicious location: Unknown',        'OPEN'),
(5,  'Transaction amount exceeds Rs.50,000; Suspicious location: Unknown',        'REVIEWED'),
(15, 'Transaction amount exceeds Rs.50,000; Suspicious foreign location: Karachi','OPEN');

-- Verify data
SELECT 'Users loaded:'        AS Info, COUNT(*) AS Count FROM users;
SELECT 'Transactions loaded:' AS Info, COUNT(*) AS Count FROM transactions;
SELECT 'Fraud alerts loaded:' AS Info, COUNT(*) AS Count FROM fraud_alerts;
