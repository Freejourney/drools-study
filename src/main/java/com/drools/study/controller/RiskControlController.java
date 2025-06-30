package com.drools.study.controller;

import com.drools.study.model.*;
import com.drools.study.service.RiskControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Request wrapper classes for API endpoints
 */
@Data
class TransactionRequest {
    private Transaction transaction;
    private Customer customer;
}

@Data 
class LoanApplicationRequest {
    private LoanApplication application;
    private Customer customer;
    private CreditScore creditScore;
}

/**
 * REST Controller for Risk Control Operations
 * 
 * This controller demonstrates how to integrate Drools with Spring Boot REST APIs.
 * It provides endpoints for various risk control operations including:
 * - Customer onboarding with risk assessment
 * - Transaction monitoring and fraud detection
 * - Loan application processing
 * - Credit score evaluation
 * 
 * @author Drools Study Project
 */
@RestController
@RequestMapping("/api/risk-control")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RiskControlController {

    private final RiskControlService riskControlService;

    /**
     * Customer onboarding with comprehensive risk assessment
     * 
     * @param customer Customer to onboard
     * @return Risk assessment results
     */
    @PostMapping("/customers/onboard")
    public ResponseEntity<RiskAssessment> onboardCustomer(@RequestBody Customer customer) {
        log.info("Onboarding customer: {}", customer.getFullName());
        
        try {
            RiskAssessment assessment = riskControlService.performCustomerOnboarding(customer);
            return ResponseEntity.ok(assessment);
        } catch (Exception e) {
            log.error("Error onboarding customer: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Process transaction and check for risks
     * 
     * @param request Transaction request containing transaction and customer data
     * @return List of risk alerts generated
     */
    @PostMapping("/transactions/process")
    public ResponseEntity<List<RiskAlert>> processTransaction(@RequestBody TransactionRequest request) {
        log.info("Processing transaction: {} for amount: {}", 
                request.getTransaction().getTransactionId(), request.getTransaction().getAmount());
        
        try {
            List<RiskAlert> alerts = riskControlService.monitorTransaction(request.getTransaction(), request.getCustomer());
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error processing transaction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Process loan application
     * 
     * @param request Loan application request containing application, customer, and credit score
     * @return Risk assessment with decision
     */
    @PostMapping("/loans/apply")
    public ResponseEntity<RiskAssessment> processLoanApplication(@RequestBody LoanApplicationRequest request) {
        log.info("Processing loan application: {} for amount: {}", 
                request.getApplication().getApplicationId(), request.getApplication().getRequestedAmount());
        
        try {
            RiskAssessment assessment = riskControlService.processLoanApplication(
                request.getApplication(), request.getCustomer(), request.getCreditScore());
            return ResponseEntity.ok(assessment);
        } catch (Exception e) {
            log.error("Error processing loan application: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get customer risk profile
     * 
     * @param customerId Customer ID
     * @return Customer's risk assessment
     */
    @GetMapping("/customers/{customerId}/risk-profile")
    public ResponseEntity<RiskAssessment> getCustomerRiskProfile(@PathVariable String customerId) {
        log.info("Getting risk profile for customer: {}", customerId);
        
        try {
            // Create a sample customer for demonstration
            Customer customer = Customer.builder()
                    .customerId(customerId)
                    .fullName("Customer " + customerId)
                    .creditScore(750)
                    .build();
            
            RiskAssessment assessment = riskControlService.performCustomerOnboarding(customer);
            return ResponseEntity.ok(assessment);
        } catch (Exception e) {
            log.error("Error getting customer risk profile: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Calculate credit score for a customer
     * 
     * @param customer Customer data
     * @return Credit score calculation
     */
    @PostMapping("/credit-score/calculate")
    public ResponseEntity<CreditScore> calculateCreditScore(@RequestBody Customer customer) {
        log.info("Calculating credit score for customer: {}", customer.getFullName());
        
        try {
            // Create a sample credit score calculation
            CreditScore creditScore = CreditScore.builder()
                    .customerId(customer.getCustomerId())
                    .primaryScore(customer.getCreditScore())
                    .paymentHistoryScore(85)
                    .creditUtilizationScore(30)
                    .creditHistoryLengthScore(10)
                    .creditMixScore(5)
                    .newCreditScore(2)
                    .build();
            
            return ResponseEntity.ok(creditScore);
        } catch (Exception e) {
            log.error("Error calculating credit score: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all active risk alerts for a customer
     * 
     * @param customerId Customer ID
     * @return List of active alerts
     */
    @GetMapping("/customers/{customerId}/alerts")
    public ResponseEntity<List<RiskAlert>> getCustomerAlerts(@PathVariable String customerId) {
        log.info("Getting alerts for customer: {}", customerId);
        
        try {
            // For demo purposes, return sample alerts
            // In real implementation, this would fetch from database
            List<RiskAlert> alerts = List.of(
                RiskAlert.builder()
                    .alertId("ALERT001")
                    .customerId(customerId)
                    .alertType("HIGH_RISK_TRANSACTION")
                    .severity("HIGH")
                    .description("High value transaction detected")
                    .status("ACTIVE")
                    .build()
            );
            
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error getting customer alerts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     * 
     * @return System status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Risk Control API",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    /**
     * Get Drools rule execution statistics
     * 
     * @return Rule execution stats
     */
    @GetMapping("/stats/rules")
    public ResponseEntity<Map<String, Object>> getRuleStats() {
        try {
            // This would typically fetch real statistics
            Map<String, Object> stats = Map.of(
                "totalRulesExecuted", 1247,
                "averageExecutionTime", "12ms",
                "mostTriggeredRule", "HighRiskCustomerDetection",
                "rulesInMemory", 15,
                "lastExecutionTime", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting rule stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 