package com.drools.study.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LoanApplication entity representing loan applications for approval processing.
 * Used in loan approval rules, risk assessment, and decision automation.
 * 
 * @author Drools Study Tutorial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {
    
    /**
     * Unique loan application identifier
     */
    private String applicationId;
    
    /**
     * Customer ID who applied for the loan
     */
    private String customerId;
    
    /**
     * Loan type (PERSONAL, MORTGAGE, AUTO, BUSINESS, CREDIT_LINE)
     */
    private String loanType;
    
    /**
     * Requested loan amount
     */
    private Double requestedAmount;
    
    /**
     * Approved loan amount (if approved)
     */
    private Double approvedAmount;
    
    /**
     * Loan term in months
     */
    private Integer termMonths;
    
    /**
     * Requested interest rate
     */
    private Double requestedRate;
    
    /**
     * Approved interest rate
     */
    private Double approvedRate;
    
    /**
     * Application status (SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CANCELLED)
     */
    private String status;
    
    /**
     * Application submission date
     */
    private LocalDateTime submissionDate;
    
    /**
     * Application review date
     */
    private LocalDateTime reviewDate;
    
    /**
     * Application decision date
     */
    private LocalDateTime decisionDate;
    
    /**
     * Loan purpose
     */
    private String purpose;
    
    /**
     * Applicant's stated income
     */
    private Double statedIncome;
    
    /**
     * Verified income
     */
    private Double verifiedIncome;
    
    /**
     * Applicant's employment status
     */
    private String employmentStatus;
    
    /**
     * Years of employment
     */
    private Integer employmentYears;
    
    /**
     * Monthly debt payments
     */
    private Double monthlyDebtPayments;
    
    /**
     * Debt-to-income ratio
     */
    private Double debtToIncomeRatio;
    
    /**
     * Loan-to-value ratio (for secured loans)
     */
    private Double loanToValueRatio;
    
    /**
     * Collateral value (for secured loans)
     */
    private Double collateralValue;
    
    /**
     * Collateral type
     */
    private String collateralType;
    
    /**
     * Credit score at application time
     */
    private Integer creditScore;
    
    /**
     * Down payment amount
     */
    private Double downPayment;
    
    /**
     * Application channel (ONLINE, BRANCH, PHONE, MOBILE)
     */
    private String applicationChannel;
    
    /**
     * Loan officer assigned
     */
    private String loanOfficer;
    
    /**
     * Underwriter assigned
     */
    private String underwriter;
    
    /**
     * Risk rating assigned
     */
    private String riskRating;
    
    /**
     * Application risk score (0-100)
     */
    private Integer riskScore;
    
    /**
     * Rejection reason (if rejected)
     */
    private String rejectionReason;
    
    /**
     * Special conditions or notes
     */
    private String conditions;
    
    /**
     * Whether co-signer is required
     */
    private Boolean requiresCosigner;
    
    /**
     * Whether additional documentation is required
     */
    private Boolean requiresAdditionalDocs;
    
    /**
     * Automated decision indicator
     */
    private Boolean isAutomatedDecision;
    
    /**
     * Processing priority (LOW, NORMAL, HIGH, URGENT)
     */
    private String priority;
    
    /**
     * Checks if loan amount is high value (>= 100,000)
     * @return true if high value loan
     */
    public boolean isHighValueLoan() {
        return requestedAmount != null && requestedAmount >= 100000;
    }
    
    /**
     * Checks if loan amount is small (< 10,000)
     * @return true if small loan
     */
    public boolean isSmallLoan() {
        return requestedAmount != null && requestedAmount < 10000;
    }
    
    /**
     * Checks if debt-to-income ratio is high (>= 40%)
     * @return true if high DTI
     */
    public boolean isHighDebtToIncome() {
        return debtToIncomeRatio != null && debtToIncomeRatio >= 0.4;
    }
    
    /**
     * Checks if loan-to-value ratio is high (>= 80%)
     * @return true if high LTV
     */
    public boolean isHighLoanToValue() {
        return loanToValueRatio != null && loanToValueRatio >= 0.8;
    }
    
    /**
     * Checks if applicant has good credit (>= 700)
     * @return true if good credit
     */
    public boolean hasGoodCredit() {
        return creditScore != null && creditScore >= 700;
    }
    
    /**
     * Checks if applicant has poor credit (< 600)
     * @return true if poor credit
     */
    public boolean hasPoorCredit() {
        return creditScore != null && creditScore < 600;
    }
    
    /**
     * Checks if application is under review
     * @return true if under review
     */
    public boolean isUnderReview() {
        return "UNDER_REVIEW".equals(status);
    }
    
    /**
     * Checks if application is approved
     * @return true if approved
     */
    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
    
    /**
     * Checks if application is rejected
     * @return true if rejected
     */
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }
    
    /**
     * Checks if employment is stable (>= 2 years)
     * @return true if stable employment
     */
    public boolean hasStableEmployment() {
        return employmentYears != null && employmentYears >= 2;
    }
    
    /**
     * Checks if loan is secured (has collateral)
     * @return true if secured loan
     */
    public boolean isSecuredLoan() {
        return collateralValue != null && collateralValue > 0;
    }
    
    /**
     * Checks if application is for mortgage
     * @return true if mortgage application
     */
    public boolean isMortgageApplication() {
        return "MORTGAGE".equals(loanType);
    }
    
    /**
     * Checks if application processing is overdue (> 30 days)
     * @return true if processing is overdue
     */
    public boolean isProcessingOverdue() {
        if (submissionDate == null || !isUnderReview()) return false;
        return submissionDate.isBefore(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * Gets application age in days
     * @return age in days
     */
    public long getApplicationAgeInDays() {
        if (submissionDate == null) return 0;
        return java.time.Duration.between(submissionDate, LocalDateTime.now()).toDays();
    }
    
    /**
     * Checks if income is verified
     * @return true if income is verified
     */
    public boolean isIncomeVerified() {
        return verifiedIncome != null && verifiedIncome > 0;
    }
    
    /**
     * Gets income variance percentage
     * @return variance between stated and verified income
     */
    public double getIncomeVariance() {
        if (statedIncome == null || verifiedIncome == null || statedIncome == 0) return 0;
        return Math.abs((statedIncome - verifiedIncome) / statedIncome) * 100;
    }
} 