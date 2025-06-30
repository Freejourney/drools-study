package com.drools.study.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RiskAssessment entity representing comprehensive risk assessment results.
 * Contains aggregated risk information from multiple sources and rule evaluations.
 * 
 * @author Drools Study Tutorial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    
    /**
     * Unique assessment identifier
     */
    private String assessmentId;
    
    /**
     * Customer ID being assessed
     */
    private String customerId;
    
    /**
     * Assessment type (ONBOARDING, PERIODIC, TRANSACTION, LOAN_APPLICATION)
     */
    private String assessmentType;
    
    /**
     * Overall risk score (0-100)
     */
    private Integer overallRiskScore;
    
    /**
     * Overall risk level (LOW, MEDIUM, HIGH, VERY_HIGH)
     */
    private String overallRiskLevel;
    
    /**
     * Credit risk score (0-100)
     */
    private Integer creditRiskScore;
    
    /**
     * Fraud risk score (0-100)
     */
    private Integer fraudRiskScore;
    
    /**
     * AML risk score (0-100)
     */
    private Integer amlRiskScore;
    
    /**
     * Operational risk score (0-100)
     */
    private Integer operationalRiskScore;
    
    /**
     * Reputation risk score (0-100)
     */
    private Integer reputationRiskScore;
    
    /**
     * Assessment creation timestamp
     */
    private LocalDateTime assessmentDate;
    
    /**
     * Assessment validity period (end date)
     */
    private LocalDateTime validUntil;
    
    /**
     * Assessment status (DRAFT, COMPLETED, APPROVED, REJECTED)
     */
    private String status;
    
    /**
     * List of rules that were fired during assessment
     */
    private List<String> firedRules;
    
    /**
     * List of risk factors identified
     */
    private List<String> riskFactors;
    
    /**
     * List of recommendations
     */
    private List<String> recommendations;
    
    /**
     * Risk mitigation measures required
     */
    private List<String> mitigationMeasures;
    
    /**
     * Assessment notes
     */
    private String notes;
    
    /**
     * Assessor (user or system)
     */
    private String assessor;
    
    /**
     * Reviewer (if manual review required)
     */
    private String reviewer;
    
    /**
     * Decision (APPROVE, REJECT, REFER, MONITOR)
     */
    private String decision;
    
    /**
     * Decision reason
     */
    private String decisionReason;
    
    /**
     * Confidence level in assessment (0-100)
     */
    private Integer confidenceLevel;
    
    /**
     * Enhanced due diligence required
     */
    private Boolean requiresEdd;
    
    /**
     * Continuous monitoring required
     */
    private Boolean requiresMonitoring;
    
    /**
     * Manual review required
     */
    private Boolean requiresManualReview;
    
    /**
     * Regulatory reporting required
     */
    private Boolean requiresRegulatoryReporting;
    
    /**
     * Transaction limits recommended
     */
    private Double recommendedTransactionLimit;
    
    /**
     * Monitoring frequency (DAILY, WEEKLY, MONTHLY)
     */
    private String monitoringFrequency;
    
    /**
     * Next review due date
     */
    private LocalDateTime nextReviewDate;
    
    /**
     * Data sources used in assessment
     */
    private List<String> dataSources;
    
    /**
     * Model versions used
     */
    private String modelVersions;
    
    /**
     * Processing time in milliseconds
     */
    private Long processingTimeMs;
    
    /**
     * Checks if overall risk is high
     * @return true if high risk
     */
    public boolean isHighRisk() {
        return overallRiskScore != null && overallRiskScore >= 70;
    }
    
    /**
     * Checks if overall risk is low
     * @return true if low risk
     */
    public boolean isLowRisk() {
        return overallRiskScore != null && overallRiskScore <= 30;
    }
    
    /**
     * Checks if any risk component is high
     * @return true if any component is high risk
     */
    public boolean hasHighRiskComponents() {
        return (creditRiskScore != null && creditRiskScore >= 70) ||
               (fraudRiskScore != null && fraudRiskScore >= 70) ||
               (amlRiskScore != null && amlRiskScore >= 70) ||
               (operationalRiskScore != null && operationalRiskScore >= 70);
    }
    
    /**
     * Checks if assessment is approved
     * @return true if approved
     */
    public boolean isApproved() {
        return "APPROVE".equals(decision);
    }
    
    /**
     * Checks if assessment is rejected
     * @return true if rejected
     */
    public boolean isRejected() {
        return "REJECT".equals(decision);
    }
    
    /**
     * Checks if assessment requires referral
     * @return true if requires referral
     */
    public boolean requiresReferral() {
        return "REFER".equals(decision);
    }
    
    /**
     * Checks if assessment is expired
     * @return true if expired
     */
    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(LocalDateTime.now());
    }
    
    /**
     * Checks if assessment has high confidence
     * @return true if high confidence (>= 80%)
     */
    public boolean isHighConfidence() {
        return confidenceLevel != null && confidenceLevel >= 80;
    }
    
    /**
     * Checks if assessment has low confidence
     * @return true if low confidence (<= 50%)
     */
    public boolean isLowConfidence() {
        return confidenceLevel != null && confidenceLevel <= 50;
    }
    
    /**
     * Checks if review is overdue
     * @return true if review is overdue
     */
    public boolean isReviewOverdue() {
        return nextReviewDate != null && nextReviewDate.isBefore(LocalDateTime.now());
    }
    
    /**
     * Gets the highest risk score among all components
     * @return highest risk score
     */
    public int getHighestRiskScore() {
        int highest = overallRiskScore != null ? overallRiskScore : 0;
        
        if (creditRiskScore != null && creditRiskScore > highest) {
            highest = creditRiskScore;
        }
        if (fraudRiskScore != null && fraudRiskScore > highest) {
            highest = fraudRiskScore;
        }
        if (amlRiskScore != null && amlRiskScore > highest) {
            highest = amlRiskScore;
        }
        if (operationalRiskScore != null && operationalRiskScore > highest) {
            highest = operationalRiskScore;
        }
        
        return highest;
    }
    
    /**
     * Checks if assessment processing was fast (< 1 second)
     * @return true if fast processing
     */
    public boolean isFastProcessing() {
        return processingTimeMs != null && processingTimeMs < 1000;
    }
    
    /**
     * Checks if assessment processing was slow (> 10 seconds)
     * @return true if slow processing
     */
    public boolean isSlowProcessing() {
        return processingTimeMs != null && processingTimeMs > 10000;
    }
    
    /**
     * Gets number of risk factors identified
     * @return count of risk factors
     */
    public int getRiskFactorCount() {
        return riskFactors != null ? riskFactors.size() : 0;
    }
    
    /**
     * Checks if multiple risk factors are present
     * @return true if multiple risk factors (>= 3)
     */
    public boolean hasMultipleRiskFactors() {
        return getRiskFactorCount() >= 3;
    }
} 