package com.drools.study.service;

import com.drools.study.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RiskControlService provides business-level risk management operations.
 * This service demonstrates how to integrate Drools with business logic
 * for comprehensive risk control in financial applications.
 * 
 * @author Drools Study Tutorial
 */
@Service
public class RiskControlService {
    
    private static final Logger logger = LoggerFactory.getLogger(RiskControlService.class);
    
    @Autowired
    private DroolsService droolsService;
    
    /**
     * Performs comprehensive customer onboarding risk assessment.
     * This method demonstrates a complete risk evaluation workflow.
     * 
     * @param customer the customer to onboard
     * @return complete risk assessment result
     */
    public RiskAssessment performCustomerOnboarding(Customer customer) {
        logger.info("Starting customer onboarding assessment for: {}", customer.getCustomerId());
        
        long startTime = System.currentTimeMillis();
        
        // Create assessment record
        RiskAssessment assessment = RiskAssessment.builder()
                .assessmentId(UUID.randomUUID().toString())
                .customerId(customer.getCustomerId())
                .assessmentType("ONBOARDING")
                .assessmentDate(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusMonths(12))
                .status("DRAFT")
                .firedRules(new ArrayList<>())
                .riskFactors(new ArrayList<>())
                .recommendations(new ArrayList<>())
                .dataSources(List.of("CUSTOMER_DATA", "CREDIT_BUREAU", "SANCTIONS_LIST"))
                .build();
        
        try {
            // Step 1: Basic customer validation
            validateCustomerData(customer, assessment);
            
            // Step 2: Execute Drools rules for risk evaluation
            List<RiskAlert> alerts = droolsService.evaluateCustomerRisk(customer);
            
            // Step 3: Calculate risk scores based on rule results
            calculateRiskScores(customer, assessment, alerts);
            
            // Step 4: Determine overall risk level and decision
            determineRiskDecision(assessment);
            
            // Step 5: Generate recommendations
            generateRecommendations(assessment, alerts);
            
            assessment.setStatus("COMPLETED");
            
        } catch (Exception e) {
            logger.error("Error in customer onboarding assessment: {}", e.getMessage(), e);
            assessment.setStatus("ERROR");
            assessment.setDecision("REFER");
            assessment.setDecisionReason("System error during assessment");
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        assessment.setProcessingTimeMs(processingTime);
        
        logger.info("Customer onboarding assessment completed for {} in {} ms", 
                   customer.getCustomerId(), processingTime);
        
        return assessment;
    }
    
    /**
     * Processes real-time transaction monitoring.
     * This method demonstrates real-time rule execution for fraud detection.
     * 
     * @param transaction the transaction to monitor
     * @param customer the customer who initiated the transaction
     * @return list of alerts generated
     */
    public List<RiskAlert> monitorTransaction(Transaction transaction, Customer customer) {
        logger.info("Monitoring transaction: {} for customer: {}", 
                   transaction.getTransactionId(), customer.getCustomerId());
        
        try {
            // Execute transaction monitoring rules
            List<RiskAlert> alerts = droolsService.evaluateTransactionRisk(transaction, customer);
            
            // Process alerts and determine actions
            for (RiskAlert alert : alerts) {
                processAlert(alert, transaction);
            }
            
            // Update transaction status based on alerts
            updateTransactionStatus(transaction, alerts);
            
            logger.info("Transaction monitoring completed. Generated {} alerts", alerts.size());
            return alerts;
            
        } catch (Exception e) {
            logger.error("Error monitoring transaction {}: {}", transaction.getTransactionId(), e.getMessage(), e);
            
            // Create system error alert
            RiskAlert errorAlert = createSystemErrorAlert(transaction, e.getMessage());
            return List.of(errorAlert);
        }
    }
    
    /**
     * Processes loan application with comprehensive risk assessment.
     * 
     * @param loanApplication the loan application
     * @param customer the applicant
     * @param creditScore the applicant's credit score
     * @return assessment result with decision
     */
    public RiskAssessment processLoanApplication(LoanApplication loanApplication, Customer customer, CreditScore creditScore) {
        logger.info("Processing loan application: {}", loanApplication.getApplicationId());
        
        try {
            // Execute loan approval rules using Drools
            RiskAssessment assessment = droolsService.processLoanApplication(loanApplication, customer, creditScore);
            
            // Apply business logic for final decision
            applyLoanDecisionLogic(assessment, loanApplication, customer, creditScore);
            
            // Update loan application status
            updateLoanApplicationStatus(loanApplication, assessment);
            
            return assessment;
            
        } catch (Exception e) {
            logger.error("Error processing loan application {}: {}", loanApplication.getApplicationId(), e.getMessage(), e);
            throw new RuntimeException("Loan application processing failed", e);
        }
    }
    
    /**
     * Validates customer data for completeness and accuracy.
     */
    private void validateCustomerData(Customer customer, RiskAssessment assessment) {
        logger.debug("Validating customer data for: {}", customer.getCustomerId());
        
        List<String> validationErrors = new ArrayList<>();
        
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            validationErrors.add("Missing customer name");
        }
        
        if (customer.getDateOfBirth() == null) {
            validationErrors.add("Missing date of birth");
        }
        
        if (customer.getAnnualIncome() == null || customer.getAnnualIncome() <= 0) {
            validationErrors.add("Invalid annual income");
        }
        
        if (customer.getCreditScore() == null || customer.getCreditScore() < 300 || customer.getCreditScore() > 850) {
            validationErrors.add("Invalid credit score");
        }
        
        if (!validationErrors.isEmpty()) {
            assessment.getRiskFactors().addAll(validationErrors);
            logger.warn("Customer data validation failed for {}: {}", customer.getCustomerId(), validationErrors);
        }
    }
    
    /**
     * Calculates risk scores based on customer data and rule results.
     */
    private void calculateRiskScores(Customer customer, RiskAssessment assessment, List<RiskAlert> alerts) {
        logger.debug("Calculating risk scores for customer: {}", customer.getCustomerId());
        
        // Base credit risk calculation
        int creditRisk = calculateCreditRisk(customer);
        assessment.setCreditRiskScore(creditRisk);
        
        // Base fraud risk calculation  
        int fraudRisk = calculateFraudRisk(customer, alerts);
        assessment.setFraudRiskScore(fraudRisk);
        
        // AML risk calculation
        int amlRisk = calculateAmlRisk(customer);
        assessment.setAmlRiskScore(amlRisk);
        
        // Overall risk is the maximum of all components
        int overallRisk = Math.max(Math.max(creditRisk, fraudRisk), amlRisk);
        assessment.setOverallRiskScore(overallRisk);
        
        // Determine risk level based on score
        if (overallRisk >= 80) {
            assessment.setOverallRiskLevel("VERY_HIGH");
        } else if (overallRisk >= 60) {
            assessment.setOverallRiskLevel("HIGH");
        } else if (overallRisk >= 40) {
            assessment.setOverallRiskLevel("MEDIUM");
        } else {
            assessment.setOverallRiskLevel("LOW");
        }
    }
    
    /**
     * Calculates credit risk score based on customer data.
     */
    private int calculateCreditRisk(Customer customer) {
        int risk = 0;
        
        // Credit score impact (inverted - lower score = higher risk)
        if (customer.getCreditScore() != null) {
            risk += Math.max(0, 100 - (customer.getCreditScore() - 300) * 100 / 550);
        }
        
        // Income impact
        if (customer.getAnnualIncome() != null) {
            if (customer.getAnnualIncome() < 30000) risk += 20;
            else if (customer.getAnnualIncome() < 50000) risk += 10;
        }
        
        // Employment status impact
        if ("UNEMPLOYED".equals(customer.getEmploymentStatus())) {
            risk += 30;
        } else if ("SELF_EMPLOYED".equals(customer.getEmploymentStatus())) {
            risk += 15;
        }
        
        // Age impact (very young or very old customers)
        int age = customer.getAge();
        if (age < 21 || age > 75) {
            risk += 15;
        }
        
        return Math.min(100, risk);
    }
    
    /**
     * Calculates fraud risk score.
     */
    private int calculateFraudRisk(Customer customer, List<RiskAlert> alerts) {
        int risk = 0;
        
        // Alert-based risk
        for (RiskAlert alert : alerts) {
            if (alert.isFraudAlert()) {
                risk += 25;
            }
        }
        
        // Blacklist check
        if (Boolean.TRUE.equals(customer.getIsBlacklisted())) {
            risk += 50;
        }
        
        // New customer risk
        if (customer.isNewCustomer()) {
            risk += 10;
        }
        
        // Previous fraud incidents
        if (customer.getFraudIncidents() != null && customer.getFraudIncidents() > 0) {
            risk += customer.getFraudIncidents() * 15;
        }
        
        return Math.min(100, risk);
    }
    
    /**
     * Calculates AML risk score.
     */
    private int calculateAmlRisk(Customer customer) {
        int risk = 0;
        
        // PEP status
        if (customer.getRiskProfile() != null && Boolean.TRUE.equals(customer.getRiskProfile().getIsPep())) {
            risk += 40;
        }
        
        // Sanctions list
        if (customer.getRiskProfile() != null && Boolean.TRUE.equals(customer.getRiskProfile().getIsOnSanctionsList())) {
            risk += 60;
        }
        
        // High-risk countries
        if (isHighRiskCountry(customer.getCountry())) {
            risk += 20;
        }
        
        // High-risk occupation
        if (isHighRiskOccupation(customer.getOccupation())) {
            risk += 15;
        }
        
        return Math.min(100, risk);
    }
    
    /**
     * Determines final risk decision based on assessment.
     */
    private void determineRiskDecision(RiskAssessment assessment) {
        String decision;
        String reason;
        
        if (assessment.getOverallRiskScore() >= 80) {
            decision = "REJECT";
            reason = "High overall risk score: " + assessment.getOverallRiskScore();
        } else if (assessment.getOverallRiskScore() >= 60) {
            decision = "REFER";
            reason = "Medium-high risk requires manual review";
        } else if (assessment.hasMultipleRiskFactors()) {
            decision = "REFER";
            reason = "Multiple risk factors identified";
        } else {
            decision = "APPROVE";
            reason = "Risk within acceptable limits";
        }
        
        assessment.setDecision(decision);
        assessment.setDecisionReason(reason);
        
        // Set monitoring requirements
        if ("APPROVE".equals(decision) && assessment.getOverallRiskScore() >= 40) {
            assessment.setRequiresMonitoring(true);
            assessment.setMonitoringFrequency("MONTHLY");
        }
        
        // Set EDD requirements
        if (assessment.getAmlRiskScore() >= 60) {
            assessment.setRequiresEdd(true);
        }
    }
    
    /**
     * Generates recommendations based on assessment results.
     */
    private void generateRecommendations(RiskAssessment assessment, List<RiskAlert> alerts) {
        List<String> recommendations = assessment.getRecommendations();
        
        if (assessment.getOverallRiskScore() >= 60) {
            recommendations.add("Enhanced monitoring recommended");
        }
        
        if (assessment.getAmlRiskScore() >= 40) {
            recommendations.add("Perform enhanced due diligence checks");
        }
        
        if (assessment.getCreditRiskScore() >= 70) {
            recommendations.add("Consider lower credit limits");
        }
        
        for (RiskAlert alert : alerts) {
            if (alert.isHighSeverity()) {
                recommendations.add("Investigate alert: " + alert.getTitle());
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Standard monitoring procedures apply");
        }
    }
    
    private void processAlert(RiskAlert alert, Transaction transaction) {
        // Set alert details
        alert.setCreatedDate(LocalDateTime.now());
        alert.setDueDate(LocalDateTime.now().plusHours(24));
        alert.setSourceSystem("DROOLS_RISK_ENGINE");
        
        // Assign severity-based due dates
        if (alert.isCritical()) {
            alert.setDueDate(LocalDateTime.now().plusHours(2));
        } else if (alert.isHighSeverity()) {
            alert.setDueDate(LocalDateTime.now().plusHours(8));
        }
    }
    
    private void updateTransactionStatus(Transaction transaction, List<RiskAlert> alerts) {
        boolean hasCriticalAlert = alerts.stream().anyMatch(RiskAlert::isCritical);
        boolean hasHighSeverityAlert = alerts.stream().anyMatch(RiskAlert::isHighSeverity);
        
        if (hasCriticalAlert) {
            transaction.setStatus("BLOCKED");
            transaction.setBlockReason("Critical risk alert triggered");
        } else if (hasHighSeverityAlert) {
            transaction.setStatus("PENDING");
            transaction.setBlockReason("High severity alert requires review");
        }
    }
    
    private RiskAlert createSystemErrorAlert(Transaction transaction, String errorMessage) {
        return RiskAlert.builder()
                .alertId(UUID.randomUUID().toString())
                .customerId(transaction.getCustomerId())
                .transactionId(transaction.getTransactionId())
                .alertType("OPERATIONAL")
                .severity("HIGH")
                .status("OPEN")
                .title("System Error in Risk Assessment")
                .description("Error occurred during risk assessment: " + errorMessage)
                .createdDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusHours(4))
                .build();
    }
    
    private void applyLoanDecisionLogic(RiskAssessment assessment, LoanApplication loanApplication, Customer customer, CreditScore creditScore) {
        // Business rules for loan decisions
        if (assessment.isHighRisk()) {
            loanApplication.setStatus("REJECTED");
            loanApplication.setRejectionReason("High risk assessment score");
        } else if (assessment.getOverallRiskScore() >= 40 || (customer.getCreditScore() != null && customer.getCreditScore() < 600)) {
            loanApplication.setStatus("UNDER_REVIEW");
            loanApplication.setRequiresAdditionalDocs(true);
        } else {
            loanApplication.setStatus("APPROVED");
            calculateLoanTerms(loanApplication, customer, creditScore);
        }
    }
    
    private void calculateLoanTerms(LoanApplication loanApplication, Customer customer, CreditScore creditScore) {
        // Calculate approved amount (may be less than requested)
        double approvedAmount = loanApplication.getRequestedAmount();
        
        if (customer.hasGoodCredit()) {
            // Full approval for good credit
            loanApplication.setApprovedAmount(approvedAmount);
            loanApplication.setApprovedRate(loanApplication.getRequestedRate());
        } else {
            // Reduced amount or higher rate for fair credit
            approvedAmount *= 0.8; // 80% of requested amount
            loanApplication.setApprovedAmount(approvedAmount);
            loanApplication.setApprovedRate(loanApplication.getRequestedRate() + 1.5); // Add 1.5% to rate
        }
    }
    
    private void updateLoanApplicationStatus(LoanApplication loanApplication, RiskAssessment assessment) {
        loanApplication.setDecisionDate(LocalDateTime.now());
        loanApplication.setRiskScore(assessment.getOverallRiskScore());
        
        if (Boolean.TRUE.equals(assessment.getRequiresManualReview())) {
            loanApplication.setRequiresAdditionalDocs(true);
        }
    }
    
    private boolean isHighRiskCountry(String country) {
        // Simplified high-risk country check
        return country != null && (country.equals("XX") || country.equals("YY"));
    }
    
    private boolean isHighRiskOccupation(String occupation) {
        // Simplified high-risk occupation check
        return occupation != null && (occupation.contains("CASH") || occupation.contains("GAMBLING"));
    }
} 