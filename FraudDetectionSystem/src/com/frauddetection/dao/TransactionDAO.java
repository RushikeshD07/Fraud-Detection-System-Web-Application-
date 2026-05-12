package com.frauddetection.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.frauddetection.db.DBConnection;
import com.frauddetection.model.Transaction;

/**
 * TransactionDAO.java
 * --------------------
 * Handles all database operations for the Transactions table.
 * Also contains the fraud-detection logic (called after each insert).
 */
public class TransactionDAO {

    // ── Suspicious locations list (used in fraud check) ────────────────────
    private static final String[] SUSPICIOUS_LOCATIONS = {
        "Unknown", "Dubai", "Lagos", "Karachi", "Offshore"
    };

    // ── Fraud rule thresholds ───────────────────────────────────────────────
    private static final double HIGH_AMOUNT_THRESHOLD    = 50000.00;  // ₹50,000
    private static final int    RAPID_TXN_LIMIT          = 3;         // 3+ txns in 5 minutes
    private static final int    RAPID_TXN_WINDOW_MINUTES = 5;

    // ── INSERT: Add a new transaction and run fraud check ───────────────────
    public int addTransaction(Transaction txn) {
        String sql = "INSERT INTO transactions (user_id, amount, txn_type, location) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, txn.getUserId());
            ps.setDouble(2, txn.getAmount());
            ps.setString(3, txn.getTxnType());
            ps.setString(4, txn.getLocation());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    txn.setTxnId(newId);
                    System.out.println("[DAO] Transaction #" + newId + " recorded.");

                    // Run fraud detection immediately after insertion
                    runFraudDetection(txn);

                    return newId;
                }
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] addTransaction: " + e.getMessage());
        }
        return -1; // indicates failure
    }

    // ── SELECT: Get all transactions ────────────────────────────────────────
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT txn_id, user_id, amount, txn_type, location, txn_timestamp " +
                     "FROM transactions ORDER BY txn_timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement st   = conn.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Transaction(
                    rs.getInt("txn_id"),
                    rs.getInt("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("txn_type"),
                    rs.getString("location"),
                    rs.getTimestamp("txn_timestamp")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] getAllTransactions: " + e.getMessage());
        }
        return list;
    }

    // ── SELECT: Get transactions by a specific user ─────────────────────────
    public List<Transaction> getTransactionsByUser(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT txn_id, user_id, amount, txn_type, location, txn_timestamp " +
                     "FROM transactions WHERE user_id = ? ORDER BY txn_timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Transaction(
                    rs.getInt("txn_id"),
                    rs.getInt("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("txn_type"),
                    rs.getString("location"),
                    rs.getTimestamp("txn_timestamp")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] getTransactionsByUser: " + e.getMessage());
        }
        return list;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FRAUD DETECTION ENGINE
    //  Checks 3 rules and stores alerts in fraud_alerts table if triggered.
    // ════════════════════════════════════════════════════════════════════════
    private void runFraudDetection(Transaction txn) {
        FraudAlertDAO alertDAO = new FraudAlertDAO();
        StringBuilder reasons  = new StringBuilder();

        // -- RULE 1: High transaction amount --------------------------------
        if (txn.getAmount() > HIGH_AMOUNT_THRESHOLD) {
            reasons.append("Transaction amount exceeds Rs. ")
                   .append((int) HIGH_AMOUNT_THRESHOLD)
                   .append("; ");
        }

        // -- RULE 2: Suspicious/foreign location -------------------------------
        for (String suspLoc : SUSPICIOUS_LOCATIONS) {
            if (suspLoc.equalsIgnoreCase(txn.getLocation())) {
                reasons.append("Suspicious location: ").append(txn.getLocation()).append("; ");
                break;
            }
        }

        // ── RULE 3: Multiple transactions within a short time window ───────
        if (isRapidTransaction(txn.getUserId(), txn.getTxnId())) {
            reasons.append("Multiple transactions within ")
                   .append(RAPID_TXN_WINDOW_MINUTES)
                   .append(" minutes; ");
        }

        // -- Raise alert if any rule was violated --------------------------------
        if (reasons.length() > 0) {
            String reason = reasons.toString().trim();
            if (reason.endsWith(";")) {
                reason = reason.substring(0, reason.length() - 1);
            }
            alertDAO.addAlert(txn.getTxnId(), reason);
            System.out.println("\u001B[31m\n  [ALERT] FRAUD ALERT RAISED: " + reason + "\u001B[0m");
        } else {
            System.out.println("  [OK] No fraud indicators found for this transaction.");
        }
    }

    /**
     * Checks whether the user made RAPID_TXN_LIMIT or more transactions
     * (excluding the current one) within the last RAPID_TXN_WINDOW_MINUTES minutes.
     */
    private boolean isRapidTransaction(int userId, int currentTxnId) {
        String sql = "SELECT COUNT(*) AS cnt FROM transactions " +
                     "WHERE user_id = ? " +
                     "  AND txn_id != ? " +
                     "  AND txn_timestamp >= NOW() - INTERVAL ? MINUTE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, currentTxnId);
            ps.setInt(3, RAPID_TXN_WINDOW_MINUTES);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt") >= RAPID_TXN_LIMIT;
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] isRapidTransaction: " + e.getMessage());
        }
        return false;
    }
}
