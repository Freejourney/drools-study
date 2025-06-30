package com.drools.study.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Account entity representing customer bank accounts used in risk control rules.
 * Contains account-specific information for transaction monitoring,
 * balance checks, and account-based risk assessments.
 * 
 * @author Drools Study Tutorial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    /**
     * Unique account number
     */
    private String accountNumber;
    
    /**
     * Customer ID who owns this account
     */
    private String customerId;
    
    /**
     * Account type (CHECKING, SAVINGS, CREDIT, LOAN, INVESTMENT)
     */
    private String accountType;
    
    /**
     * Current account balance
     */
    private Double balance;
    
    /**
     * Available balance (after holds)
     */
    private Double availableBalance;
    
    /**
     * Account currency
     */
    private String currency;
    
    /**
     * Account status (ACTIVE, INACTIVE, FROZEN, CLOSED)
     */
    private String status;
    
    /**
     * Account opening date
     */
    private LocalDateTime openDate;
    
    /**
     * Account closing date (if closed)
     */
    private LocalDateTime closeDate;
    
    /**
     * Credit limit for credit accounts
     */
    private Double creditLimit;
    
    /**
     * Overdraft limit
     */
    private Double overdraftLimit;
    
    /**
     * Interest rate
     */
    private Double interestRate;
    
    /**
     * Account branch code
     */
    private String branchCode;
    
    /**
     * Whether account has overdraft protection
     */
    private Boolean hasOverdraftProtection;
    
    /**
     * Whether account is joint account
     */
    private Boolean isJointAccount;
    
    /**
     * Account risk rating (LOW, MEDIUM, HIGH)
     */
    private String riskRating;
    
    /**
     * Monthly average balance
     */
    private Double monthlyAverageBalance;
    
    /**
     * Number of transactions this month
     */
    private Integer monthlyTransactionCount;
    
    /**
     * Total monthly transaction volume
     */
    private Double monthlyTransactionVolume;
    
    /**
     * Number of days account has been dormant
     */
    private Integer dormantDays;
    
    /**
     * Whether account is flagged for monitoring
     */
    private Boolean isFlaggedForMonitoring;
    
    /**
     * Last transaction date
     */
    private LocalDateTime lastTransactionDate;
    
    /**
     * Minimum balance maintained
     */
    private Double minimumBalance;
    
    /**
     * Maximum balance reached
     */
    private Double maximumBalance;
    
    /**
     * Checks if account has sufficient balance for given amount
     * @param amount amount to check
     * @return true if sufficient balance
     */
    public boolean hasSufficientBalance(Double amount) {
        if (amount == null || availableBalance == null) return false;
        return availableBalance >= amount;
    }
    
    /**
     * Checks if account is overdrawn
     * @return true if overdrawn
     */
    public boolean isOverdrawn() {
        return balance != null && balance < 0;
    }
    
    /**
     * Checks if account has low balance (< 1000)
     * @return true if low balance
     */
    public boolean hasLowBalance() {
        return balance != null && balance < 1000;
    }
    
    /**
     * Checks if account has high balance (>= 100,000)
     * @return true if high balance
     */
    public boolean hasHighBalance() {
        return balance != null && balance >= 100000;
    }
    
    /**
     * Checks if account is near credit limit (>90% utilized)
     * @return true if near credit limit
     */
    public boolean isNearCreditLimit() {
        if (creditLimit == null || balance == null) return false;
        double utilization = Math.abs(balance) / creditLimit;
        return utilization >= 0.9;
    }
    
    /**
     * Checks if account is dormant (no transactions for 90+ days)
     * @return true if dormant
     */
    public boolean isDormant() {
        return dormantDays != null && dormantDays >= 90;
    }
    
    /**
     * Checks if account is new (opened within last 30 days)
     * @return true if new account
     */
    public boolean isNewAccount() {
        if (openDate == null) return false;
        return openDate.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * Checks if account has high transaction velocity
     * @return true if high velocity
     */
    public boolean hasHighTransactionVelocity() {
        return monthlyTransactionCount != null && monthlyTransactionCount >= 50;
    }
    
    /**
     * Checks if account has unusual transaction volume
     * @return true if unusual volume
     */
    public boolean hasUnusualTransactionVolume() {
        if (monthlyTransactionVolume == null || monthlyAverageBalance == null) return false;
        return monthlyTransactionVolume > (monthlyAverageBalance * 5);
    }
    
    /**
     * Gets credit utilization ratio
     * @return credit utilization as percentage (0-100)
     */
    public double getCreditUtilization() {
        if (creditLimit == null || balance == null) return 0;
        return (Math.abs(balance) / creditLimit) * 100;
    }
} 