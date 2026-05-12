# 🛡️ Fraud Detection System
### Java (JDBC) + MySQL | College Mini Project

---

## 📁 Project Structure

```
FraudDetectionSystem/
├── sql/
│   └── schema.sql                          ← Run this FIRST in MySQL
│
└── src/
    └── com/
        └── frauddetection/
            ├── Main.java                   ← Entry point (menu-driven UI)
            ├── db/
            │   └── DBConnection.java       ← JDBC connection (Singleton)
            ├── model/
            │   ├── User.java               ← User model / POJO
            │   ├── Transaction.java        ← Transaction model / POJO
            │   └── FraudAlert.java         ← Fraud alert model / POJO
            └── dao/
                ├── UserDAO.java            ← CRUD for users table
                ├── TransactionDAO.java     ← CRUD + fraud detection logic
                └── FraudAlertDAO.java      ← CRUD for fraud_alerts table
```

---

## ⚙️ Prerequisites

| Requirement       | Version   | Download |
|-------------------|-----------|----------|
| Java (JDK)        | 17+       | https://adoptium.net |
| MySQL Server      | 8.0+      | https://dev.mysql.com/downloads/ |
| MySQL Connector/J | 8.0+      | https://dev.mysql.com/downloads/connector/j/ |

---

## 🚀 Step-by-Step Setup & Run Guide

### STEP 1 — Set up the database

1. Open MySQL Workbench (or the MySQL terminal).
2. Run the SQL script:

```sql
SOURCE /path/to/FraudDetectionSystem/sql/schema.sql;
```
Or open the file in MySQL Workbench and click ▶ Run.

This creates:
- `fraud_detection_db` database
- `users`, `transactions`, `fraud_alerts` tables
- 10 sample users, 15 transactions, and 5 pre-seeded fraud alerts

---

### STEP 2 — Configure database credentials

Open `src/com/frauddetection/db/DBConnection.java` and update:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/fraud_detection_db?useSSL=false&serverTimezone=UTC";
private static final String USER     = "root";       // ← your MySQL username
private static final String PASSWORD = "root";       // ← your MySQL password
```

---

### STEP 3 — Download MySQL Connector/J

1. Download `mysql-connector-j-8.x.x.jar` from:
   https://dev.mysql.com/downloads/connector/j/

2. Place the JAR file in a `lib/` folder inside the project:
   ```
   FraudDetectionSystem/
   └── lib/
       └── mysql-connector-j-8.x.x.jar
   ```

---

### STEP 4 — Compile the project

Open a terminal in the project root (`FraudDetectionSystem/`) and run:

**Windows:**
```bash
javac -cp "lib\mysql-connector-j-8.x.x.jar" -d out -sourcepath src src\com\frauddetection\Main.java
```

**Linux / macOS:**
```bash
javac -cp "lib/mysql-connector-j-8.x.x.jar" -d out -sourcepath src src/com/frauddetection/Main.java
```

---

### STEP 5 — Run the application

**Windows:**
```bash
java -cp "out;lib\mysql-connector-j-8.x.x.jar" com.frauddetection.Main
```

**Linux / macOS:**
```bash
java -cp "out:lib/mysql-connector-j-8.x.x.jar" com.frauddetection.Main
```

---

### ⚡ Alternative: Using an IDE (Recommended for beginners)

**IntelliJ IDEA / Eclipse:**
1. Open the project folder as a Java project.
2. Right-click `lib/mysql-connector-j.jar` → Add as Library.
3. Right-click `Main.java` → Run.

---

## 🧠 Fraud Detection Rules

The system automatically checks 3 rules on every transaction:

| Rule | Condition | Example |
|------|-----------|---------|
| **Rule 1** | Amount > ₹50,000 | Withdraw ₹75,000 → ALERT |
| **Rule 2** | Suspicious location | Location = "Unknown" / "Lagos" → ALERT |
| **Rule 3** | Rapid transactions | 3+ transactions in 5 minutes → ALERT |

Suspicious locations list: `Unknown`, `Dubai`, `Lagos`, `Karachi`, `Offshore`

---

## 🖥️ Sample Program Output

```
  ╔══════════════════════════════════════════════════╗
  ║        FRAUD DETECTION SYSTEM  v1.0             ║
  ║        Powered by Java + MySQL (JDBC)           ║
  ╚══════════════════════════════════════════════════╝

[DB] Connected to MySQL successfully.

  ┌─────────────────────────────────┐
  │           MAIN  MENU            │
  ├─────────────────────────────────┤
  │  1. Add New User                │
  │  2. Perform Transaction         │
  │  3. View All Users              │
  │  4. View All Transactions       │
  │  5. View User Transactions      │
  │  6. View Fraud Alerts           │
  │  7. Update Alert Status         │
  │  8. Delete User                 │
  │  9. Exit                        │
  └─────────────────────────────────┘
Enter your choice: 2

  ═══ PERFORM TRANSACTION ═══

  User ID: 1
  Account holder : Alice Johnson
  Current balance: ₹85000.00

  Transaction Type:
    1. DEPOSIT
    2. WITHDRAW
  Choose (1/2): 2
  Amount (₹): 60000
  Location       : Dubai

  Processing transaction...
[DAO] Transaction #16 recorded.

  ⚠  FRAUD ALERT RAISED: Transaction amount exceeds ₹50000; Suspicious location: Dubai

  ✓  Transaction successful! New balance: ₹25000.00
```

**View Fraud Alerts (Option 6):**
```
  ═══ FRAUD ALERTS ═══

+----------+--------+----------------------------------------------------+------------+----------------------+
| AlertID  | TxnID  | Fraud Reason                                       | Status     | Raised At            |
+----------+--------+----------------------------------------------------+------------+----------------------+
| 6        | 16     | Transaction amount exceeds ₹50000; Suspicious l... | OPEN       | 2025-01-15 14:32:01  |
| 5        | 15     | Transaction amount exceeds ₹50000; Suspicious f... | OPEN       | 2025-01-14 10:00:00  |
| 4        | 13     | Transaction amount exceeds ₹50000; Suspicious l... | OPEN       | 2025-01-14 09:58:00  |
+----------+--------+----------------------------------------------------+------------+----------------------+
  Total alerts: 6
```

---

## 🗄️ Database Schema (ER Summary)

```
users
  user_id (PK)  name  email  account_balance  created_at

transactions
  txn_id (PK)  user_id (FK → users)  amount  txn_type  location  txn_timestamp

fraud_alerts
  alert_id (PK)  txn_id (FK → transactions)  fraud_reason  status  created_at
```

---

## 📋 Features Checklist

- [x] Add new user
- [x] Deposit / Withdraw with balance update
- [x] Fraud detection (3 rules)
- [x] Auto-raise fraud alert on violation
- [x] View all users with balances
- [x] View all / user-specific transactions
- [x] View all / open fraud alerts
- [x] Update alert status (Open → Reviewed → Resolved)
- [x] Delete user (cascades to transactions & alerts)
- [x] Proper exception handling throughout
- [x] Clean, commented, beginner-friendly code

---

## 🛠️ Troubleshooting

| Problem | Solution |
|---------|----------|
| `ClassNotFoundException` for driver | Ensure MySQL Connector JAR is in classpath |
| `Access denied for user 'root'` | Check USER and PASSWORD in DBConnection.java |
| `Unknown database 'fraud_detection_db'` | Run schema.sql first in MySQL |
| `Communications link failure` | Make sure MySQL server is running |

---

*Mini Project — Fraud Detection System | Java (JDBC) + MySQL*

# To Run the command :
java -cp "out;lib\mysql-connector-j-9.6.0.jar" com.frauddetection.Main