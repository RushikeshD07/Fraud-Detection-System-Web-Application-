package com.frauddetection.model;

import java.sql.Timestamp;

/**
 * Transaction.java
 * -----------------
 * Model class representing a financial transaction.
 */
public class Transaction {

    private int       txnId;
    private int       userId;
    private double    amount;
    private String    txnType;     // "DEPOSIT" or "WITHDRAW"
    private String    location;
    private Timestamp txnTimestamp;

    // ── Constructors ────────────────────────────────────────────────────────
    public Transaction() {}

    /** Used when creating a new transaction (no ID yet) */
    public Transaction(int userId, double amount, String txnType, String location) {
        this.userId   = userId;
        this.amount   = amount;
        this.txnType  = txnType;
        this.location = location;
    }

    /** Used when reading from DB (ID already assigned) */
    public Transaction(int txnId, int userId, double amount,
                       String txnType, String location, Timestamp txnTimestamp) {
        this.txnId        = txnId;
        this.userId       = userId;
        this.amount       = amount;
        this.txnType      = txnType;
        this.location     = location;
        this.txnTimestamp = txnTimestamp;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────
    public int       getTxnId()              { return txnId; }
    public void      setTxnId(int id)        { this.txnId = id; }

    public int       getUserId()             { return userId; }
    public void      setUserId(int id)       { this.userId = id; }

    public double    getAmount()             { return amount; }
    public void      setAmount(double a)     { this.amount = a; }

    public String    getTxnType()            { return txnType; }
    public void      setTxnType(String t)    { this.txnType = t; }

    public String    getLocation()           { return location; }
    public void      setLocation(String l)   { this.location = l; }

    public Timestamp getTxnTimestamp()                 { return txnTimestamp; }
    public void      setTxnTimestamp(Timestamp ts)     { this.txnTimestamp = ts; }

    // ── Display helper ──────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "| %-6d | %-6d | %-10s | Rs. %-8.2f | %-15s | %-20s |",
            txnId, userId, txnType, amount, location,
            txnTimestamp != null ? txnTimestamp.toString().substring(0, 19) : "N/A"
        );
    }
}
