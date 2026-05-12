package com.frauddetection.model;

import java.sql.Timestamp;

/**
 * FraudAlert.java
 * ----------------
 * Model class representing a fraud alert raised against a transaction.
 */
public class FraudAlert {

    private int       alertId;
    private int       txnId;
    private String    fraudReason;
    private String    status;       // "OPEN", "REVIEWED", "RESOLVED"
    private Timestamp createdAt;

    // ── Constructors ────────────────────────────────────────────────────────
    public FraudAlert() {}

    /** Used when inserting a new alert */
    public FraudAlert(int txnId, String fraudReason) {
        this.txnId       = txnId;
        this.fraudReason = fraudReason;
        this.status      = "OPEN";
    }

    /** Full constructor for reading from DB */
    public FraudAlert(int alertId, int txnId, String fraudReason,
                      String status, Timestamp createdAt) {
        this.alertId     = alertId;
        this.txnId       = txnId;
        this.fraudReason = fraudReason;
        this.status      = status;
        this.createdAt   = createdAt;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────
    public int       getAlertId()               { return alertId; }
    public void      setAlertId(int id)         { this.alertId = id; }

    public int       getTxnId()                 { return txnId; }
    public void      setTxnId(int id)           { this.txnId = id; }

    public String    getFraudReason()           { return fraudReason; }
    public void      setFraudReason(String r)   { this.fraudReason = r; }

    public String    getStatus()                { return status; }
    public void      setStatus(String s)        { this.status = s; }

    public Timestamp getCreatedAt()             { return createdAt; }
    public void      setCreatedAt(Timestamp ts) { this.createdAt = ts; }

    // ── Display helper ──────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "| %-8d | %-6d | %-50s | %-10s | %-20s |",
            alertId, txnId,
            fraudReason.length() > 50 ? fraudReason.substring(0, 47) + "..." : fraudReason,
            status,
            createdAt != null ? createdAt.toString().substring(0, 19) : "N/A"
        );
    }
}
