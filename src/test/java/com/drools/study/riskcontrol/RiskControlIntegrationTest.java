package com.drools.study.riskcontrol;

import com.drools.study.model.*;
import com.drools.study.service.RiskControlService;
import com.drools.study.service.DroolsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Risk Control Integration Test
 * 
 * This test class demonstrates the complete risk control workflow
 * integrating all components:
 * - Customer onboarding with risk assessment
 * - Transaction monitoring and fraud detection
 * - Loan application processing
 * - Credit score evaluation
 * - Real-time alert generation
 * 
 * @author Drools Study Tutorial
 */
@SpringBootTest
@ActiveProfiles("test")
public class RiskControlIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(RiskControlIntegrationTest.class);

    @Autowired
    private RiskControlService riskControlService;

    @Autowired
    private DroolsService droolsService;

    private Customer testCustomer;
    private Account testAccount;
    private CreditScore testCreditScore;

    @BeforeEach
    void setUp() {
        logger.info("Setting up integration test data");
        
        // Create comprehensive test customer
        testCustomer = Customer.builder()
                .customerId("INT_TEST_001")
                .fullName("Integration Test Customer")
                .dateOfBirth(LocalDate.of(1985, 6, 15))
                .email("integration.test@example.com")
                .phoneNumber("+1234567890")
                .address("123 Test Street, Test City, TC 12345")
                .annualIncome(75000.0)
                .employmentStatus("EMPLOYED")
                .creditScore(720)
                .accountOpenDate(LocalDateTime.now().minusYears(2))
                .riskCategory("MEDIUM")
                .isBlacklisted(false)
                .isVip(false)
                .country("USA")
                .occupation("SOFTWARE_ENGINEER")
                .yearsWithBank(2)
                .numberOfProducts(3)
                .monthlyIncome(6250.0)
                .debtToIncomeRatio(0.25)
                .fraudIncidents(0)
                .build();

        // Create test account
        testAccount = Account.builder()
                .accountId("ACC_INT_001")
                .customerId("INT_TEST_001")
                .accountType("CHECKING")
                .balance(50000.0)
                .currency("USD")
                .status("ACTIVE")
                .openDate(LocalDateTime.now().minusYears(2))
                .lastTransactionDate(LocalDateTime.now().minusDays(1))
                .averageTransactionAmount(500.0)
                .monthlyTransactionCount(25)
                .overdraftCount(0)
                .build();

        // Create test credit score
        testCreditScore = CreditScore.builder()
                .scoreId("CS_INT_001")
                .customerId("INT_TEST_001")
                .primaryScore(720)
                .secondaryScore(715)
                .creditBureau("EXPERIAN")
                .scoreDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusMonths(12))
                .scoreCategory("GOOD")
                .paymentHistoryScore(85)
                .creditUtilizationScore(70)
                .creditHistoryLengthScore(75)
                .newCreditScore(90)
                .creditMixScore(80)
                .numberOfAccounts(8)
                .openAccounts(6)
                .closedAccounts(2)
                .delinquentAccounts(0)
                .totalCreditLimit(100000.0)
                .totalCreditUtilized(25000.0)
                .utilizationRatio(25.0)
                .recentInquiries(1)
                .oldestAccountAge(120)
                .averageAccountAge(45)
                .latePayments24Months(0)
                .bankruptcies(0)
                .foreclosures(0)
                .scoreChange(15)
                .reasonCodes("GOOD_PAYMENT_HISTORY,LOW_UTILIZATION")
                .build();
    }

    /**
     * Test 1: Complete Customer Onboarding Workflow
     * Tests the full customer onboarding process with risk assessment
     */
    @Test
    @DisplayName("Complete Customer Onboarding Workflow")
    void testCompleteCustomerOnboarding() {
        logger.info("=== Testing Complete Customer Onboarding Workflow ===");
        
        // Perform customer onboarding
        RiskAssessment assessment = riskControlService.performCustomerOnboarding(testCustomer);
        
        // Verify assessment results
        assertNotNull(assessment, "Risk assessment should not be null");
        assertNotNull(assessment.getAssessmentId(), "Assessment ID should be generated");
        assertEquals("INT_TEST_001", assessment.getCustomerId(), "Customer ID should match");
        assertEquals("ONBOARDING", assessment.getAssessmentType(), "Assessment type should be ONBOARDING");
        assertEquals("COMPLETED", assessment.getStatus(), "Assessment should be completed");
        
        // Verify risk scores are calculated
        assertNotNull(assessment.getCreditRiskScore(), "Credit risk score should be calculated");
        assertNotNull(assessment.getFraudRiskScore(), "Fraud risk score should be calculated");
        assertNotNull(assessment.getAmlRiskScore(), "AML risk score should be calculated");
        
        // Log assessment details
        logger.info("Assessment ID: {}", assessment.getAssessmentId());
        logger.info("Overall Risk Level: {}", assessment.getOverallRiskLevel());
        logger.info("Credit Risk Score: {}", assessment.getCreditRiskScore());
        logger.info("Fraud Risk Score: {}", assessment.getFraudRiskScore());
        logger.info("AML Risk Score: {}", assessment.getAmlRiskScore());
        logger.info("Decision: {}", assessment.getDecision());
        logger.info("Processing Time: {} ms", assessment.getProcessingTimeMs());
        
        // Verify decision is reasonable for good customer
        assertTrue(List.of("APPROVE", "MONITOR").contains(assessment.getDecision()), 
                  "Good customer should be approved or monitored");
        
        // Verify recommendations are provided
        assertNotNull(assessment.getRecommendations(), "Recommendations should be provided");
        logger.info("Recommendations: {}", assessment.getRecommendations());
    }

    /**
     * Test 2: Transaction Monitoring and Fraud Detection
     * Tests real-time transaction monitoring with various scenarios
     */
    @Test
    @DisplayName("Transaction Monitoring and Fraud Detection")
    void testTransactionMonitoringAndFraudDetection() {
        logger.info("=== Testing Transaction Monitoring and Fraud Detection ===");
        
        // Test scenario 1: Normal transaction
        Transaction normalTransaction = Transaction.builder()
                .transactionId("TXN_NORMAL_001")
                .customerId("INT_TEST_001")
                .amount(500.0)
                .currency("USD")
                .transactionTime(LocalDateTime.now())
                .transactionType("PURCHASE")
                .merchantName("Local Store")
                .merchantCategory("RETAIL")
                .location("Test City")
                .status("PENDING")
                .fraudScore(0)
                .build();
        
        List<RiskAlert> normalAlerts = riskControlService.monitorTransaction(normalTransaction, testCustomer);
        logger.info("Normal transaction alerts: {}", normalAlerts.size());
        
        // Test scenario 2: High-value transaction
        Transaction highValueTransaction = Transaction.builder()
                .transactionId("TXN_HIGH_001")
                .customerId("INT_TEST_001")
                .amount(25000.0)  // Very high amount
                .currency("USD")
                .transactionTime(LocalDateTime.now())
                .transactionType("TRANSFER")
                .merchantName("Wire Transfer")
                .merchantCategory("FINANCIAL")
                .location("Test City")
                .status("PENDING")
                .fraudScore(0)
                .build();
        
        List<RiskAlert> highValueAlerts = riskControlService.monitorTransaction(highValueTransaction, testCustomer);
        logger.info("High-value transaction alerts: {}", highValueAlerts.size());
        
        // Test scenario 3: Suspicious time transaction
        Transaction nightTransaction = Transaction.builder()
                .transactionId("TXN_NIGHT_001")
                .customerId("INT_TEST_001")
                .amount(5000.0)
                .currency("USD")
                .transactionTime(LocalDateTime.now().withHour(2).withMinute(30))  // 2:30 AM
                .transactionType("WITHDRAWAL")
                .merchantName("ATM")
                .merchantCategory("ATM")
                .location("Unknown Location")
                .status("PENDING")
                .fraudScore(0)
                .build();
        
        List<RiskAlert> nightAlerts = riskControlService.monitorTransaction(nightTransaction, testCustomer);
        logger.info("Night transaction alerts: {}", nightAlerts.size());
        
        // High-value transactions should generate more alerts
        assertTrue(highValueAlerts.size() >= normalAlerts.size(), 
                  "High-value transactions should generate at least as many alerts as normal transactions");
        
        // Log alert details
        for (RiskAlert alert : highValueAlerts) {
            logger.info("Alert: {} - {} - {}", alert.getAlertType(), alert.getSeverity(), alert.getDescription());
        }
    }

    /**
     * Test 3: Loan Application Processing
     * Tests the complete loan application workflow
     */
    @Test
    @DisplayName("Loan Application Processing")
    void testLoanApplicationProcessing() {
        logger.info("=== Testing Loan Application Processing ===");
        
        // Test scenario 1: Reasonable loan request
        LoanApplication reasonableLoan = LoanApplication.builder()
                .applicationId("LOAN_REASONABLE_001")
                .customerId("INT_TEST_001")
                .loanType("PERSONAL")
                .requestedAmount(25000.0)
                .loanPurpose("HOME_IMPROVEMENT")
                .requestedTerm(36)
                .applicationDate(LocalDateTime.now())
                .status("SUBMITTED")
                .collateralValue(0.0)
                .cosignerRequired(false)
                .build();
        
        RiskAssessment reasonableAssessment = riskControlService.processLoanApplication(
                reasonableLoan, testCustomer, testCreditScore);
        
        assertNotNull(reasonableAssessment, "Loan assessment should not be null");
        assertEquals("INT_TEST_001", reasonableAssessment.getCustomerId(), "Customer ID should match");
        
        logger.info("Reasonable loan decision: {}", reasonableAssessment.getDecision());
        logger.info("Reasonable loan risk level: {}", reasonableAssessment.getOverallRiskLevel());
        
        // Test scenario 2: High-risk loan request
        LoanApplication highRiskLoan = LoanApplication.builder()
                .applicationId("LOAN_HIGH_RISK_001")
                .customerId("INT_TEST_001")
                .loanType("PERSONAL")
                .requestedAmount(150000.0)  // Very high amount (2x annual income)
                .loanPurpose("DEBT_CONSOLIDATION")
                .requestedTerm(84)  // Long term
                .applicationDate(LocalDateTime.now())
                .status("SUBMITTED")
                .collateralValue(0.0)
                .cosignerRequired(false)
                .build();
        
        RiskAssessment highRiskAssessment = riskControlService.processLoanApplication(
                highRiskLoan, testCustomer, testCreditScore);
        
        assertNotNull(highRiskAssessment, "High-risk loan assessment should not be null");
        
        logger.info("High-risk loan decision: {}", highRiskAssessment.getDecision());
        logger.info("High-risk loan risk level: {}", highRiskAssessment.getOverallRiskLevel());
        
        // High-risk loan should have higher risk scores
        assertTrue(highRiskAssessment.getOverallRiskScore() >= reasonableAssessment.getOverallRiskScore(),
                  "High-risk loan should have higher or equal risk score");
    }

    /**
     * Test 4: Multi-Product Risk Assessment
     * Tests risk assessment across multiple financial products
     */
    @Test
    @DisplayName("Multi-Product Risk Assessment")
    void testMultiProductRiskAssessment() {
        logger.info("=== Testing Multi-Product Risk Assessment ===");
        
        // Create multiple transactions to simulate transaction history
        for (int i = 1; i <= 10; i++) {
            Transaction transaction = Transaction.builder()
                    .transactionId("TXN_HISTORY_" + String.format("%03d", i))
                    .customerId("INT_TEST_001")
                    .amount(100.0 + (i * 50))
                    .currency("USD")
                    .transactionTime(LocalDateTime.now().minusDays(i))
                    .transactionType("PURCHASE")
                    .merchantCategory(i % 2 == 0 ? "RETAIL" : "RESTAURANT")
                    .status("COMPLETED")
                    .fraudScore(0)
                    .build();
            
            List<RiskAlert> alerts = riskControlService.monitorTransaction(transaction, testCustomer);
            logger.info("Historical transaction {} generated {} alerts", i, alerts.size());
        }
        
        // Test credit card application
        LoanApplication creditCardApp = LoanApplication.builder()
                .applicationId("CC_APP_001")
                .customerId("INT_TEST_001")
                .loanType("CREDIT_CARD")
                .requestedAmount(15000.0)
                .loanPurpose("GENERAL_PURPOSE")
                .applicationDate(LocalDateTime.now())
                .status("SUBMITTED")
                .build();
        
        RiskAssessment creditCardAssessment = riskControlService.processLoanApplication(
                creditCardApp, testCustomer, testCreditScore);
        
        assertNotNull(creditCardAssessment, "Credit card assessment should not be null");
        logger.info("Credit card application decision: {}", creditCardAssessment.getDecision());
        
        // Verify that transaction history influenced the decision
        assertNotNull(creditCardAssessment.getDataSources(), "Data sources should be recorded");
        assertTrue(creditCardAssessment.getProcessingTimeMs() > 0, "Processing time should be recorded");
    }

    /**
     * Test 5: Real-time Alert Processing
     * Tests the alert generation and processing system
     */
    @Test
    @DisplayName("Real-time Alert Processing")
    void testRealTimeAlertProcessing() {
        logger.info("=== Testing Real-time Alert Processing ===");
        
        // Create a series of rapid transactions to trigger velocity alerts
        LocalDateTime baseTime = LocalDateTime.now();
        int totalAlerts = 0;
        
        for (int i = 1; i <= 8; i++) {
            Transaction rapidTransaction = Transaction.builder()
                    .transactionId("TXN_RAPID_" + String.format("%03d", i))
                    .customerId("INT_TEST_001")
                    .amount(1000.0 + (i * 200))
                    .currency("USD")
                    .transactionTime(baseTime.plusMinutes(i))
                    .transactionType("WITHDRAWAL")
                    .merchantCategory("ATM")
                    .location("Location " + i)
                    .status("PENDING")
                    .fraudScore(0)
                    .build();
            
            List<RiskAlert> alerts = riskControlService.monitorTransaction(rapidTransaction, testCustomer);
            totalAlerts += alerts.size();
            
            logger.info("Rapid transaction {} generated {} alerts (total: {})", i, alerts.size(), totalAlerts);
            
            // Log alert types
            for (RiskAlert alert : alerts) {
                logger.info("  -> Alert Type: {}, Severity: {}", alert.getAlertType(), alert.getSeverity());
            }
        }
        
        assertTrue(totalAlerts > 0, "Rapid transactions should generate alerts");
        logger.info("Total alerts generated from rapid transactions: {}", totalAlerts);
    }

    /**
     * Test 6: Performance and Scalability
     * Tests system performance under load
     */
    @Test
    @DisplayName("Performance and Scalability Test")
    void testPerformanceAndScalability() {
        logger.info("=== Testing Performance and Scalability ===");
        
        long startTime = System.currentTimeMillis();
        int customerCount = 100;
        int transactionCount = 500;
        
        // Process multiple customers
        for (int i = 1; i <= customerCount; i++) {
            Customer batchCustomer = Customer.builder()
                    .customerId("BATCH_CUST_" + String.format("%04d", i))
                    .fullName("Batch Customer " + i)
                    .creditScore(500 + (i % 350))
                    .annualIncome(30000.0 + (i * 500))
                    .isBlacklisted(i % 50 == 0)  // 2% blacklisted
                    .isVip(i % 20 == 0)          // 5% VIP
                    .build();
            
            RiskAssessment assessment = riskControlService.performCustomerOnboarding(batchCustomer);
            assertNotNull(assessment, "Assessment should not be null for customer " + i);
        }
        
        long customerProcessingTime = System.currentTimeMillis() - startTime;
        
        // Process multiple transactions
        long transactionStartTime = System.currentTimeMillis();
        
        for (int i = 1; i <= transactionCount; i++) {
            Transaction batchTransaction = Transaction.builder()
                    .transactionId("BATCH_TXN_" + String.format("%05d", i))
                    .customerId("INT_TEST_001")
                    .amount(50.0 + (i % 1000))
                    .transactionTime(LocalDateTime.now())
                    .merchantCategory(i % 2 == 0 ? "RETAIL" : "ONLINE")
                    .status("PENDING")
                    .build();
            
            List<RiskAlert> alerts = riskControlService.monitorTransaction(batchTransaction, testCustomer);
            // Don't assert here to avoid slowing down the performance test
        }
        
        long transactionProcessingTime = System.currentTimeMillis() - transactionStartTime;
        long totalTime = System.currentTimeMillis() - startTime;
        
        // Log performance metrics
        logger.info("Performance Results:");
        logger.info("  Customers processed: {}", customerCount);
        logger.info("  Customer processing time: {} ms", customerProcessingTime);
        logger.info("  Average time per customer: {} ms", customerProcessingTime / customerCount);
        logger.info("  Transactions processed: {}", transactionCount);
        logger.info("  Transaction processing time: {} ms", transactionProcessingTime);
        logger.info("  Average time per transaction: {} ms", transactionProcessingTime / transactionCount);
        logger.info("  Total processing time: {} ms", totalTime);
        
        // Performance assertions (adjust thresholds based on requirements)
        assertTrue(customerProcessingTime / customerCount < 1000, 
                  "Average customer processing should be under 1 second");
        assertTrue(transactionProcessingTime / transactionCount < 100, 
                  "Average transaction processing should be under 100ms");
    }
} 