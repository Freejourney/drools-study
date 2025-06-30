package com.drools.study.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CreditScore entity representing credit scoring information.
 * Used in credit assessment rules and loan approval decisions.
 * 
 * @author Drools Study Tutorial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditScore {
    
    /**
     * Unique credit score record identifier
     */
    private String scoreId;
    
    /**
     * Customer ID this score belongs to
     */
    private String customerId;
    
    /**
     * Primary credit score (300-850 FICO scale)
     */
    private Integer primaryScore;
    
    /**
     * Secondary credit score (alternative scoring model)
     */
    private Integer secondaryScore;
    
    /**
     * Credit bureau that provided the score
     */
    private String creditBureau;
    
    /**
     * Score calculation date
     */
    private LocalDateTime scoreDate;
    
    /**
     * Score expiry date
     */
    private LocalDateTime expiryDate;
    
    /**
     * Credit score category (EXCELLENT, GOOD, FAIR, POOR, VERY_POOR)
     */
    private String scoreCategory;
    
    /**
     * Payment history score component (0-100)
     */
    private Integer paymentHistoryScore;
    
    /**
     * Credit utilization score component (0-100)
     */
    private Integer creditUtilizationScore;
    
    /**
     * Length of credit history score component (0-100)
     */
    private Integer creditHistoryLengthScore;
    
    /**
     * New credit score component (0-100)
     */
    private Integer newCreditScore;
    
    /**
     * Credit mix score component (0-100)
     */
    private Integer creditMixScore;
    
    /**
     * Number of credit accounts
     */
    private Integer numberOfAccounts;
    
    /**
     * Number of open accounts
     */
    private Integer openAccounts;
    
    /**
     * Number of closed accounts
     */
    private Integer closedAccounts;
    
    /**
     * Number of delinquent accounts
     */
    private Integer delinquentAccounts;
    
    /**
     * Total credit limit across all accounts
     */
    private Double totalCreditLimit;
    
    /**
     * Total credit utilized
     */
    private Double totalCreditUtilized;
    
    /**
     * Credit utilization ratio (0-100%)
     */
    private Double utilizationRatio;
    
    /**
     * Number of recent credit inquiries (last 12 months)
     */
    private Integer recentInquiries;
    
    /**
     * Oldest account age in months
     */
    private Integer oldestAccountAge;
    
    /**
     * Average account age in months
     */
    private Integer averageAccountAge;
    
    /**
     * Number of late payments in last 24 months
     */
    private Integer latePayments24Months;
    
    /**
     * Number of bankruptcies
     */
    private Integer bankruptcies;
    
    /**
     * Number of foreclosures
     */
    private Integer foreclosures;
    
    /**
     * Score change from previous assessment
     */
    private Integer scoreChange;
    
    /**
     * Reason codes for score factors
     */
    private String reasonCodes;
    
    /**
     * Checks if credit score is excellent (>= 800)
     * @return true if excellent credit
     */
    public boolean isExcellentCredit() {
        return primaryScore != null && primaryScore >= 800;
    }
    
    /**
     * Checks if credit score is good (>= 700)
     * @return true if good credit
     */
    public boolean isGoodCredit() {
        return primaryScore != null && primaryScore >= 700;
    }
    
    /**
     * Checks if credit score is fair (600-699)
     * @return true if fair credit
     */
    public boolean isFairCredit() {
        return primaryScore != null && primaryScore >= 600 && primaryScore < 700;
    }
    
    /**
     * Checks if credit score is poor (< 600)
     * @return true if poor credit
     */
    public boolean isPoorCredit() {
        return primaryScore != null && primaryScore < 600;
    }
    
    /**
     * Checks if credit utilization is high (>= 80%)
     * @return true if high utilization
     */
    public boolean isHighUtilization() {
        return utilizationRatio != null && utilizationRatio >= 80;
    }
    
    /**
     * Checks if credit utilization is low (<= 30%)
     * @return true if low utilization
     */
    public boolean isLowUtilization() {
        return utilizationRatio != null && utilizationRatio <= 30;
    }
    
    /**
     * Checks if there are recent credit inquiries (>= 3 in last 12 months)
     * @return true if recent inquiries
     */
    public boolean hasRecentInquiries() {
        return recentInquiries != null && recentInquiries >= 3;
    }
    
    /**
     * Checks if credit history is thin (< 12 months)
     * @return true if thin credit history
     */
    public boolean isThinCreditHistory() {
        return oldestAccountAge != null && oldestAccountAge < 12;
    }
    
    /**
     * Checks if credit history is established (>= 24 months)
     * @return true if established credit history
     */
    public boolean isEstablishedCreditHistory() {
        return oldestAccountAge != null && oldestAccountAge >= 24;
    }
    
    /**
     * Checks if there are recent late payments
     * @return true if recent late payments
     */
    public boolean hasRecentLatePayments() {
        return latePayments24Months != null && latePayments24Months > 0;
    }
    
    /**
     * Checks if customer has bankruptcy history
     * @return true if bankruptcy history exists
     */
    public boolean hasBankruptcyHistory() {
        return bankruptcies != null && bankruptcies > 0;
    }
    
    /**
     * Checks if score is improving (positive change)
     * @return true if score is improving
     */
    public boolean isScoreImproving() {
        return scoreChange != null && scoreChange > 0;
    }
    
    /**
     * Checks if score is declining (negative change)
     * @return true if score is declining
     */
    public boolean isScoreDeclining() {
        return scoreChange != null && scoreChange < 0;
    }
    
    /**
     * Checks if score is expired
     * @return true if score is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
} 