package com.drools.study.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RiskProfile entity representing customer's risk assessment profile.
 * Contains comprehensive risk indicators and scores used by Drools rules
 * for risk-based decision making.
 * 
 * @author Drools Study Tutorial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskProfile {
    
    /**
     * Unique risk profile identifier
     */
    private String profileId;
    
    /**
     * Customer ID this profile belongs to
     */
    private String customerId;
    
    /**
     * Overall risk score (0-100, higher is riskier)
     */
    private Integer overallRiskScore;
    
    /**
     * Credit risk score (0-100)
     */
    private Integer creditRiskScore;
    
    /**
     * Fraud risk score (0-100)
     */
    private Integer fraudRiskScore;
    
    /**
     * Operational risk score (0-100)
     */
    private Integer operationalRiskScore;
    
    /**
     * Risk category (LOW, MEDIUM, HIGH, VERY_HIGH)
     */
    private String riskCategory;
    
    /**
     * Customer segment (RETAIL, PREMIUM, PRIVATE, CORPORATE)
     */
    private String customerSegment;
    
    /**
     * Geographic risk level (LOW, MEDIUM, HIGH)
     */
    private String geographicRisk;
    
    /**
     * Industry risk level for business customers
     */
    private String industryRisk;
    
    /**
     * PEP status (Politically Exposed Person)
     */
    private Boolean isPep;
    
    /**
     * Sanctions list status
     */
    private Boolean isOnSanctionsList;
    
    /**
     * Enhanced due diligence required
     */
    private Boolean requiresEdd;
    
    /**
     * Know Your Customer (KYC) status
     */
    private String kycStatus;
    
    /**
     * Anti-Money Laundering (AML) risk level
     */
    private String amlRiskLevel;
    
    /**
     * Last review date
     */
    private LocalDateTime lastReviewDate;
    
    /**
     * Next review due date
     */
    private LocalDateTime nextReviewDate;
    
    /**
     * Risk assessment notes
     */
    private String notes;
    
    /**
     * Monthly transaction limit
     */
    private Double monthlyTransactionLimit;
    
    /**
     * Daily transaction limit
     */
    private Double dailyTransactionLimit;
    
    /**
     * Single transaction limit
     */
    private Double singleTransactionLimit;
    
    /**
     * Whether automatic approvals are allowed
     */
    private Boolean allowAutoApproval;
    
    /**
     * Number of risk incidents in last 12 months
     */
    private Integer riskIncidentsCount;
    
    /**
     * Customer's money laundering risk score
     */
    private Integer mlRiskScore;
    
    /**
     * Terrorist financing risk score
     */
    private Integer tfRiskScore;
    
    /**
     * Checks if customer is high risk overall
     * @return true if high risk
     */
    public boolean isHighRisk() {
        return overallRiskScore != null && overallRiskScore >= 70;
    }
    
    /**
     * Checks if customer is low risk
     * @return true if low risk
     */
    public boolean isLowRisk() {
        return overallRiskScore != null && overallRiskScore <= 30;
    }
    
    /**
     * Checks if customer is medium risk
     * @return true if medium risk
     */
    public boolean isMediumRisk() {
        return overallRiskScore != null && overallRiskScore > 30 && overallRiskScore < 70;
    }
    
    /**
     * Checks if fraud risk is high
     * @return true if high fraud risk
     */
    public boolean isHighFraudRisk() {
        return fraudRiskScore != null && fraudRiskScore >= 70;
    }
    
    /**
     * Checks if credit risk is high
     * @return true if high credit risk
     */
    public boolean isHighCreditRisk() {
        return creditRiskScore != null && creditRiskScore >= 70;
    }
    
    /**
     * Checks if enhanced monitoring is required
     * @return true if enhanced monitoring needed
     */
    public boolean requiresEnhancedMonitoring() {
        return isHighRisk() || Boolean.TRUE.equals(isPep) || Boolean.TRUE.equals(isOnSanctionsList);
    }
    
    /**
     * Checks if profile needs review (overdue)
     * @return true if review is overdue
     */
    public boolean isReviewOverdue() {
        return nextReviewDate != null && nextReviewDate.isBefore(LocalDateTime.now());
    }
    
    /**
     * Checks if customer is premium segment
     * @return true if premium customer
     */
    public boolean isPremiumCustomer() {
        return "PREMIUM".equals(customerSegment) || "PRIVATE".equals(customerSegment);
    }
} 