package com.frauddetection;

import java.util.List;
import java.util.Scanner;

import com.frauddetection.dao.FraudAlertDAO;
import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.dao.UserDAO;
import com.frauddetection.db.DBConnection;
import com.frauddetection.model.FraudAlert;
import com.frauddetection.model.Transaction;
import com.frauddetection.model.User;

/**
 * Main.java
 * ----------
 * Entry point for the Fraud Detection System.
 * Provides a menu-driven console interface for all operations.
 *
 * HOW TO RUN:
 *   1. Run schema.sql in MySQL first.
 *   2. Set your DB credentials in DBConnection.java.
 *   3. Compile all .java files and add mysql-connector-java.jar to the classpath.
 *   4. Run this class.
 */
public class Main {

    // Shared DAO instances
    private static final UserDAO        userDAO    = new UserDAO();
    private static final TransactionDAO txnDAO     = new TransactionDAO();
    private static final FraudAlertDAO  alertDAO   = new FraudAlertDAO();

    // ── Program entry point ─────────────────────────────────────────────────
    public static void main(String[] args) {
        printBanner();

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMainMenu();
                int choice = readInt("Enter your choice: ", scanner);

                switch (choice) {
                    case 1  -> addUser(scanner);
                    case 2  -> performTransaction(scanner);
                    case 3  -> viewAllUsers();
                    case 4  -> viewAllTransactions();
                    case 5  -> viewTransactionsByUser(scanner);
                    case 6  -> viewFraudAlerts(scanner);
                    case 7  -> updateAlertStatus(scanner);
                    case 8  -> deleteUser(scanner);
                    case 9  -> { System.out.println("\n  Goodbye! Stay safe.\n");
                                  DBConnection.closeConnection();
                                  running = false; }
                    default -> System.out.println("  X  Invalid choice. Please try again.\n");
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENU OPTION 1 – Add a new user
    // ════════════════════════════════════════════════════════════════════════
    private static void addUser(Scanner scanner) {
        printSectionHeader("ADD NEW USER");

        System.out.print("  Full Name    : ");
        String name = scanner.nextLine().trim();

        System.out.print("  Email        : ");
        String email = scanner.nextLine().trim();

        double balance = readDouble("  Initial Balance (Rs.): ", scanner);

        User user = new User(name, email, balance);
        if (userDAO.addUser(user)) {
            System.out.println("\n  [OK]  User '" + name + "' added successfully! (ID: " + user.getUserId() + ")\n");
        } else {
            System.out.println("\n  X  Failed to add user. Email might already exist.\n");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENU OPTION 2 – Perform a transaction (deposit / withdraw)
    // ════════════════════════════════════════════════════════════════════════
    private static void performTransaction(Scanner scanner) {
        printSectionHeader("PERFORM TRANSACTION");

        int userId = readInt("  User ID: ", scanner);

        // Verify the user exists
        User user = userDAO.getUserById(userId);
        if (user == null) {
            System.out.println("\n  X  User not found.\n");
            return;
        }
        System.out.println("  Account holder : " + user.getName());
        System.out.printf ("  Current balance: Rs.%.2f%n%n", user.getAccountBalance());

        System.out.println("  Transaction Type:");
        System.out.println("    1. DEPOSIT");
        System.out.println("    2. WITHDRAW");
        int typeChoice = readInt("  Choose (1/2): ", scanner);

        String txnType;
        switch (typeChoice) {
            case 1 -> txnType = "DEPOSIT";
            case 2 -> txnType = "WITHDRAW";
            default -> { System.out.println("  X  Invalid choice.\n"); return; }
        }

        double amount = readDouble("  Amount (Rs.): ", scanner);
        if (amount <= 0) { System.out.println("  X  Amount must be positive.\n"); return; }

        // Withdrawal: check sufficient funds
        if ("WITHDRAW".equals(txnType) && amount > user.getAccountBalance()) {
            System.out.println("  X  Insufficient balance.\n");
            return;
        }

        System.out.print("  Location       : ");
        String location = scanner.nextLine().trim();
        if (location.isEmpty()) location = "Local";

        // Build and save the transaction
        Transaction txn = new Transaction(userId, amount, txnType, location);
        System.out.println("\n  Processing transaction...");
        int txnId = txnDAO.addTransaction(txn);

        if (txnId > 0) {
            // Update account balance
            double newBalance = "DEPOSIT".equals(txnType)
                ? user.getAccountBalance() + amount
                : user.getAccountBalance() - amount;

            userDAO.updateBalance(userId, newBalance);
            System.out.printf("\u001B[32m\n  [OK]  Transaction successful! New balance: Rs.%.2f%n%n\u001B[0m", newBalance);
        } else {
            System.out.println("  X  Transaction failed.\n");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENU OPTION 3 – View all users
    // ════════════════════════════════════════════════════════════════════════
    private static void viewAllUsers() {
        printSectionHeader("ALL USERS");

        List<User> users = userDAO.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("  No users found.\n");
            return;
        }

        String line = "+------+----------------------+---------------------------+-----------------+";
        System.out.println(line);
        System.out.println("| ID   | Name                 | Email                     | Balance         |");
        System.out.println(line);
        users.forEach(u -> System.out.println(u.toString()));
        System.out.println(line);
        System.out.println("  Total users: " + users.size() + "\n");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENU OPTION 4 – View all transactions
    // ════════════════════════════════════════════════════════════════════════
    private static void viewAllTransactions() {
        printSectionHeader("ALL TRANSACTIONS");

        List<Transaction> txns = txnDAO.getAllTransactions();
        if (txns.isEmpty()) {
            System.out.println("  No transactions found.\n");
            return;
        }

        String line = "+--------+--------+------------+----------------+-----------------+----------------------+";
        System.out.println(line);
        System.out.println("| TxnID  | UserID | Type       | Amount         | Location        | Timestamp            |");
        System.out.println(line);
        txns.forEach(t -> System.out.println(t.toString()));
        System.out.println(line);
        System.out.println("  Total transactions: " + txns.size() + "\n");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENU OPTION 5 – View transactions for a specific user
    // ════════════════════════════════════════════════════════════════════════
    private static void viewTransactionsByUser(Scanner scanner) {
        printSectionHeader("TRANSACTIONS BY USER");

        int userId = readInt("  Enter User ID: ", scanner);
        User user = userDAO.getUserById(userId);

        if (user == null) {
            System.out.println("  X  User not found.\n");
            return;
        }

        System.out.println("  Transactions for: " + user.getName() + "\n");
        List<Transaction> txns = txnDAO.getTransactionsByUser(userId);

        if (txns.isEmpty()) {
            System.out.println("  No transactions yet.\n");
            return;
        }

        String line = "+--------+--------+------------+----------------+-----------------+----------------------+";
        System.out.println(line);
        System.out.println("| TxnID  | UserID | Type       | Amount         | Location        | Timestamp            |");
        System.out.println(line);
        txns.forEach(t -> System.out.println(t.toString()));
        System.out.println(line + "\n");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENU OPTION 6 – View fraud alerts
    // ════════════════════════════════════════════════════════════════════════
    private static void viewFraudAlerts(Scanner scanner) {
        printSectionHeader("FRAUD ALERTS");

        System.out.println("  1. All alerts");
        System.out.println("  2. Open alerts only");
        int choice = readInt("  Choose (1/2): ", scanner);

        List<FraudAlert> alerts = (choice == 2) ? alertDAO.getOpenAlerts() : alertDAO.getAllAlerts();

        if (alerts.isEmpty()) {
            System.out.println("  No fraud alerts found.\n");
            return;
        }

        String line = "+----------+--------+----------------------------------------------------+------------+----------------------+";
        System.out.println(line);
        System.out.println("| AlertID  | TxnID  | Fraud Reason                                       | Status     | Raised At            |");
        System.out.println(line);
        alerts.forEach(a -> System.out.println(a.toString()));
        System.out.println(line);
        System.out.println("  Total alerts: " + alerts.size() + "\n");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENU OPTION 7 – Update alert status
    // ════════════════════════════════════════════════════════════════════════
    private static void updateAlertStatus(Scanner scanner) {
        printSectionHeader("UPDATE ALERT STATUS");

        int alertId = readInt("  Alert ID to update: ", scanner);

        System.out.println("  New Status:");
        System.out.println("    1. OPEN");
        System.out.println("    2. REVIEWED");
        System.out.println("    3. RESOLVED");
        int choice = readInt("  Choose (1/2/3): ", scanner);

        String status = switch (choice) {
            case 1 -> "OPEN";
            case 2 -> "REVIEWED";
            case 3 -> "RESOLVED";
            default -> null;
        };

        if (status == null) {
            System.out.println("  X  Invalid choice.\n");
            return;
        }

        if (alertDAO.updateAlertStatus(alertId, status)) {
            System.out.println("  [OK]  Alert #" + alertId + " updated to '" + status + "'.\n");
        } else {
            System.out.println("  X  Update failed.\n");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENU OPTION 8 – Delete a user
    // ════════════════════════════════════════════════════════════════════════
    private static void deleteUser(Scanner scanner) {
        printSectionHeader("DELETE USER");

        int userId = readInt("  User ID to delete: ", scanner);
        User user = userDAO.getUserById(userId);

        if (user == null) {
            System.out.println("  X  User not found.\n");
            return;
        }

        System.out.println("  Deleting user: " + user.getName() + " | " + user.getEmail());
        System.out.print("  Confirm deletion? (yes/no): ");
        String confirm = scanner.nextLine().trim();

        if ("yes".equalsIgnoreCase(confirm)) {
            if (userDAO.deleteUser(userId)) {
                System.out.println("  [OK]  User deleted (related transactions also removed).\n");
            } else {
                System.out.println("  X  Deletion failed.\n");
            }
        } else {
            System.out.println("  Deletion cancelled.\n");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private static void printBanner() {
        System.out.println();
        System.out.println("  +----------------------------------------------+");
        System.out.println("  |       FRAUD DETECTION SYSTEM  v1.0           |");
        System.out.println("  |      Powered by Java + MySQL (JDBC)          |");
        System.out.println("  +----------------------------------------------+");
        System.out.println();
    }

    private static void printMainMenu() {
        System.out.println("  +---------------------------------+");
        System.out.println("  |           MAIN MENU             |");
        System.out.println("  +---------------------------------+");
        System.out.println("  | 1. Add New User                 |");
        System.out.println("  | 2. Perform Transaction          |");
        System.out.println("  | 3. View All Users               |");
        System.out.println("  | 4. View All Transactions        |");
        System.out.println("  | 5. View User Transactions       |");
        System.out.println("  | 6. View Fraud Alerts            |");
        System.out.println("  | 7. Update Alert Status          |");
        System.out.println("  | 8. Delete User                  |");
        System.out.println("  | 9. Exit                         |");
        System.out.println("  +---------------------------------+");
    }

    private static void printSectionHeader(String title) {
        System.out.println();
        System.out.println("  ═══ " + title + " ═══");
        System.out.println();
    }

    /** Reads a valid integer from the user; keeps prompting on invalid input. */
    private static int readInt(String prompt, Scanner scanner) {
        while (true) {
            try {
                System.out.print(prompt);
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  X  Please enter a valid number.");
            }
        }
    }

    /** Reads a valid double from the user; keeps prompting on invalid input. */
    private static double readDouble(String prompt, Scanner scanner) {
        while (true) {
            try {
                System.out.print(prompt);
                String line = scanner.nextLine().trim();
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("  X  Please enter a valid amount.");
            }
        }
    }
}
