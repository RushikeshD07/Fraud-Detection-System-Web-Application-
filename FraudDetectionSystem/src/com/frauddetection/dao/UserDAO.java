package com.frauddetection.dao;

import com.frauddetection.db.DBConnection;
import com.frauddetection.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO.java
 * -------------
 * Data Access Object for all database operations related to Users.
 * Handles: Add, View All, Find by ID, Update Balance, Delete.
 */
public class UserDAO {

    // ── INSERT: Add a new user ──────────────────────────────────────────────
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (name, email, account_balance) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setDouble(3, user.getAccountBalance());

            int rows = ps.executeUpdate();

            // Retrieve the auto-generated user_id
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
                System.out.println("[DAO] User added with ID: " + user.getUserId());
                return true;
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("[DAO ERROR] Email already exists: " + user.getEmail());
        } catch (SQLException e) {
            System.err.println("[DAO ERROR] addUser: " + e.getMessage());
        }
        return false;
    }

    // ── SELECT: Get all users ───────────────────────────────────────────────
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, name, email, account_balance FROM users ORDER BY user_id";

        try (Connection conn = DBConnection.getConnection();
             Statement st   = conn.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getDouble("account_balance")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] getAllUsers: " + e.getMessage());
        }
        return users;
    }

    // ── SELECT: Find user by ID ─────────────────────────────────────────────
    public User getUserById(int userId) {
        String sql = "SELECT user_id, name, email, account_balance FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getDouble("account_balance")
                );
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] getUserById: " + e.getMessage());
        }
        return null; // user not found
    }

    // ── UPDATE: Change account balance ──────────────────────────────────────
    public boolean updateBalance(int userId, double newBalance) {
        String sql = "UPDATE users SET account_balance = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newBalance);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] updateBalance: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE: Remove a user ───────────────────────────────────────────────
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("[DAO] User " + userId + " deleted successfully.");
                return true;
            } else {
                System.out.println("[DAO] No user found with ID: " + userId);
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] deleteUser: " + e.getMessage());
        }
        return false;
    }
}
