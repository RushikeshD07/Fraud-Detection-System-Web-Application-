package com.frauddetection.model;

/**
 * User.java
 * ----------
 * Model class representing a bank user / account holder.
 */
public class User {

    private int    userId;
    private String name;
    private String email;
    private double accountBalance;

    // ── Constructors ────────────────────────────────────────────────────────
    public User() {}

    public User(String name, String email, double accountBalance) {
        this.name           = name;
        this.email          = email;
        this.accountBalance = accountBalance;
    }

    public User(int userId, String name, String email, double accountBalance) {
        this.userId         = userId;
        this.name           = name;
        this.email          = email;
        this.accountBalance = accountBalance;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────
    public int    getUserId()         { return userId; }
    public void   setUserId(int id)   { this.userId = id; }

    public String getName()           { return name; }
    public void   setName(String n)   { this.name = n; }

    public String getEmail()          { return email; }
    public void   setEmail(String e)  { this.email = e; }

    public double getAccountBalance()         { return accountBalance; }
    public void   setAccountBalance(double b) { this.accountBalance = b; }

    // ── Display helper ──────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "| %-4d | %-20s | %-25s | Rs. %-10.2f |",
            userId, name, email, accountBalance
        );
    }
}
