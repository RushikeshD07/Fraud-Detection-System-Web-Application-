package com.frauddetection.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.frauddetection.db.DBConnection;
import com.frauddetection.model.FraudAlert;

/**
 * FraudAlertDAO.java
 * -------------------
 * Handles all database operations for the Fraud_Alerts table.
 */
public class FraudAlertDAO {

    // ── INSERT: Store a new fraud alert ─────────────────────────────────────
    public boolean addAlert(int txnId, String reason) {
        String sql = "INSERT INTO fraud_alerts (txn_id, fraud_reason, status) VALUES (?, ?, 'OPEN')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, txnId);
            ps.setString(2, reason);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] addAlert: " + e.getMessage());
        }
        return false;
    }

    // ── SELECT: Get all fraud alerts ────────────────────────────────────────
    public List<FraudAlert> getAllAlerts() {
        List<FraudAlert> list = new ArrayList<>();
        String sql = "SELECT alert_id, txn_id, fraud_reason, status, created_at " +
                     "FROM fraud_alerts ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement st   = conn.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new FraudAlert(
                    rs.getInt("alert_id"),
                    rs.getInt("txn_id"),
                    rs.getString("fraud_reason"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] getAllAlerts: " + e.getMessage());
        }
        return list;
    }

    // ── SELECT: Get only OPEN alerts ────────────────────────────────────────
    public List<FraudAlert> getOpenAlerts() {
        List<FraudAlert> list = new ArrayList<>();
        String sql = "SELECT alert_id, txn_id, fraud_reason, status, created_at " +
                     "FROM fraud_alerts WHERE status = 'OPEN' ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement st   = conn.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new FraudAlert(
                    rs.getInt("alert_id"),
                    rs.getInt("txn_id"),
                    rs.getString("fraud_reason"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] getOpenAlerts: " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE: Change the status of an alert ───────────────────────────────
    public boolean updateAlertStatus(int alertId, String newStatus) {
        // Valid statuses: OPEN, REVIEWED, RESOLVED
        String sql = "UPDATE fraud_alerts SET status = ? WHERE alert_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, alertId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[DAO] Alert #" + alertId + " status -> " + newStatus);
                return true;
            } else {
                System.out.println("[DAO] Alert #" + alertId + " not found.");
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] updateAlertStatus: " + e.getMessage());
        }
        return false;
    }
}
