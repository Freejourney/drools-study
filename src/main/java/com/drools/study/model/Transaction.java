package com.drools.study.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Transaction entity representing financial transactions for monitoring and fraud detection.
 * Used in rules for transaction pattern analysis, velocity checks, and anomaly detection.
 * 
 * @author Drools Study Tutorial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    /**
     * Unique transaction identifier
     */
    private String transactionId;
    
    /**
     * Customer ID who initiated the transaction
     */
    private String customerId;
    
    /**
     * Source account number
     */
    private String fromAccount;
    
    /**
     * Destination account number (null for cash withdrawals)
     */
    private String toAccount;
    
    /**
     * Transaction amount
     */
    private Double amount;
    
    /**
     * Transaction currency (USD, EUR, GBP, etc.)
     */
    private String currency;
    
    /**
     * Transaction type (TRANSFER, WITHDRAWAL, DEPOSIT, PAYMENT, etc.)
     */
    private String transactionType;
    
    /**
     * Transaction status (PENDING, COMPLETED, FAILED, BLOCKED)
     */
    private String status;
    
    /**
     * Transaction timestamp
     */
    private LocalDateTime timestamp;
    
    /**
     * Transaction description/memo
     */
    private String description;
    
    /**
     * Channel used for transaction (ONLINE, ATM, BRANCH, MOBILE, etc.)
     */
    private String channel;
    
    /**
     * Location where transaction occurred
     */
    private String location;
    
    /**
     * Country where transaction occurred
     */
    private String country;
    
    /**
     * IP address for online transactions
     */
    private String ipAddress;
    
    /**
     * Device fingerprint for mobile/online transactions
     */
    private String deviceFingerprint;
    
    /**
     * Merchant category code for card transactions
     */
    private String merchantCategory;
    
    /**
     * Whether transaction is flagged as suspicious
     */
    private Boolean isSuspicious;
    
    /**
     * Risk score assigned to this transaction (0-100)
     */
    private Integer riskScore;
    
    /**
     * Reason for blocking/flagging (if any)
     */
    private String blockReason;
    
    /**
     * Whether transaction is international
     */
    private Boolean isInternational;
    
    /**
     * Whether transaction is high value (>= 10,000)
     */
    private Boolean isHighValue;
    
    /**
     * Time zone of transaction
     */
    private String timeZone;
    
    /**
     * Checks if transaction is high value (>= 10,000)
     * @return true if high value transaction
     */
    public boolean isHighValueTransaction() {
        return amount != null && amount >= 10000;
    }
    
    /**
     * Checks if transaction is large amount (>= 5,000)
     * @return true if large amount
     */
    public boolean isLargeAmount() {
        return amount != null && amount >= 5000;
    }
    
    /**
     * Checks if transaction is cash withdrawal
     * @return true if cash withdrawal
     */
    public boolean isCashWithdrawal() {
        return "WITHDRAWAL".equals(transactionType);
    }
    
    /**
     * Checks if transaction is wire transfer
     * @return true if wire transfer
     */
    public boolean isWireTransfer() {
        return "WIRE_TRANSFER".equals(transactionType);
    }
    
    /**
     * Checks if transaction is ATM transaction
     * @return true if ATM transaction
     */
    public boolean isAtmTransaction() {
        return "ATM".equals(channel);
    }
    
    /**
     * Checks if transaction is online
     * @return true if online transaction
     */
    public boolean isOnlineTransaction() {
        return "ONLINE".equals(channel) || "MOBILE".equals(channel);
    }
    
    /**
     * Checks if transaction occurred during business hours (9 AM - 5 PM)
     * @return true if during business hours
     */
    public boolean isDuringBusinessHours() {
        if (timestamp == null) return false;
        int hour = timestamp.getHour();
        return hour >= 9 && hour <= 17;
    }
    
    /**
     * Checks if transaction occurred during night time (10 PM - 6 AM)
     * @return true if night transaction
     */
    public boolean isNightTransaction() {
        if (timestamp == null) return false;
        int hour = timestamp.getHour();
        return hour >= 22 || hour <= 6;
    }
    
    /**
     * Checks if transaction is weekend transaction
     * @return true if weekend transaction
     */
    public boolean isWeekendTransaction() {
        if (timestamp == null) return false;
        int dayOfWeek = timestamp.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
    }
} 