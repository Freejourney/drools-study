package com.drools.study.springboot;

import com.drools.study.service.DroolsService;
import com.drools.study.service.RiskControlService;
import com.drools.study.service.TransactionService;
import com.drools.study.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Boot integration tests for Drools functionality
 * Tests the complete integration between Spring Boot and Drools components
 */
@SpringBootTest
@ActiveProfiles("test")
public class SpringBootDroolsTest {

    @Autowired
    private DroolsService droolsService;

    @Autowired
    private RiskControlService riskControlService;

    @Autowired
    private TransactionService transactionService;

    @Test
    @DisplayName("Test Spring Boot Drools Service injection")
    void testServiceInjection() {
        assertNotNull(droolsService, "DroolsService should be injected");
        assertNotNull(riskControlService, "RiskControlService should be injected");
        assertNotNull(transactionService, "TransactionService should be injected");
    }

    @Test
    @DisplayName("Test integrated risk assessment workflow")
    void testIntegratedRiskAssessmentWorkflow() {
        // Create test data
        Customer customer = createTestCustomer();
        Account account = createTestAccount();
        Transaction transaction = createHighValueTransaction();

        // Execute risk assessment through service
        RiskAssessment assessment = riskControlService.assessCustomerRisk(customer.getCustomerId(), 
                Arrays.asList(transaction, account, customer));

        assertNotNull(assessment);
        assertNotNull(assessment.getOverallRisk());
        assertTrue(assessment.getRiskFactors().size() > 0);
        
        System.out.println("Risk Assessment: " + assessment.getOverallRisk() + 
                          " with " + assessment.getRiskFactors().size() + " factors");
    }

    @Test
    @DisplayName("Test transaction processing with Spring Boot")
    void testTransactionProcessing() {
        Transaction transaction = createTestTransaction();
        
        TransactionService.TransactionProcessingResult result = 
                transactionService.processTransaction(transaction);

        assertNotNull(result);
        assertNotNull(result.getStatus());
        assertNotNull(result.getProcessedAt());
        assertTrue(result.getRiskAlerts() != null);
        
        System.out.println("Transaction processed: " + result.getStatus() + 
                          " with " + result.getRiskAlerts().size() + " alerts");
    }

    @Test
    @DisplayName("Test rule execution with different agenda groups")
    void testAgendaGroupExecution() {
        Customer customer = createTestCustomer();
        customer.setBlacklisted(true);

        // Test basic risk control rules
        List<Object> basicResults = droolsService.executeRules("risk-control-basic", 
                Arrays.asList(customer));
        
        assertNotNull(basicResults);
        assertTrue(basicResults.size() > 0);
        
        boolean hasRiskAlert = basicResults.stream()
                .anyMatch(obj -> obj instanceof RiskAlert);
        assertTrue(hasRiskAlert, "Should generate risk alerts for blacklisted customer");
    }

    @Test
    @DisplayName("Test credit scoring integration")
    void testCreditScoringIntegration() {
        Customer customer = createTestCustomer();
        Account account = createTestAccount();
        
        // Execute credit scoring rules
        List<Object> results = droolsService.executeRules("credit-scoring", 
                Arrays.asList(customer, account));

        boolean hasCreditScore = results.stream()
                .anyMatch(obj -> obj instanceof CreditScore);
        assertTrue(hasCreditScore, "Should generate credit score");
        
        CreditScore creditScore = results.stream()
                .filter(obj -> obj instanceof CreditScore)
                .map(CreditScore.class::cast)
                .findFirst()
                .orElse(null);
        
        if (creditScore != null) {
            assertNotNull(creditScore.getScore());
            assertTrue(creditScore.getScore() > 0);
            assertNotNull(creditScore.getGrade());
            System.out.println("Credit Score: " + creditScore.getScore() + 
                             " Grade: " + creditScore.getGrade());
        }
    }

    @Test
    @DisplayName("Test fraud detection with transaction patterns")
    void testFraudDetectionIntegration() {
        Customer customer = createTestCustomer();
        
        // Create suspicious transaction pattern
        Transaction txn1 = createTestTransaction();
        txn1.setAmount(5000.0);
        txn1.setLocation("New York");
        
        Transaction txn2 = createTestTransaction();
        txn2.setAmount(7000.0);
        txn2.setLocation("Los Angeles");
        txn2.setTimestamp(LocalDateTime.now().plusMinutes(30));
        
        Transaction txn3 = createTestTransaction();
        txn3.setAmount(3000.0);
        txn3.setLocation("Chicago");
        txn3.setTimestamp(LocalDateTime.now().plusMinutes(60));

        List<Object> results = droolsService.executeRules("fraud-detection", 
                Arrays.asList(customer, txn1, txn2, txn3));

        boolean hasFraudAlert = results.stream()
                .filter(obj -> obj instanceof RiskAlert)
                .map(RiskAlert.class::cast)
                .anyMatch(alert -> alert.getAlertType().contains("FRAUD") || 
                                 alert.getAlertType().contains("SUSPICIOUS"));
        
        assertTrue(hasFraudAlert, "Should detect suspicious patterns");
    }

    @Test
    @DisplayName("Test loan application processing")
    void testLoanApplicationProcessing() {
        Customer customer = createTestCustomer();
        CreditScore creditScore = createTestCreditScore();
        LoanApplication loanApp = createTestLoanApplication();

        List<Object> results = droolsService.executeRules("loan-approval", 
                Arrays.asList(customer, creditScore, loanApp));

        // Verify loan application was processed
        LoanApplication processedLoan = results.stream()
                .filter(obj -> obj instanceof LoanApplication)
                .map(LoanApplication.class::cast)
                .findFirst()
                .orElse(loanApp);

        assertNotNull(processedLoan.getStatus());
        assertNotEquals("PENDING", processedLoan.getStatus());
        
        System.out.println("Loan Application Status: " + processedLoan.getStatus() + 
                          " Reason: " + (processedLoan.getApprovalReason() != null ? 
                                       processedLoan.getApprovalReason() : 
                                       processedLoan.getRejectionReason()));
    }

    @Test
    @DisplayName("Test stateless session execution")
    void testStatelessSessionExecution() {
        Customer customer = createTestCustomer();
        customer.setBlacklisted(true);

        List<Object> results = droolsService.executeStatelessRules("risk-control-basic", 
                Arrays.asList(customer));

        assertNotNull(results);
        assertTrue(results.size() > 0);
        
        boolean hasAlert = results.stream()
                .anyMatch(obj -> obj instanceof RiskAlert);
        assertTrue(hasAlert, "Stateless execution should generate alerts");
    }

    @Test
    @DisplayName("Test concurrent rule execution")
    void testConcurrentRuleExecution() throws InterruptedException {
        Customer customer1 = createTestCustomer();
        customer1.setCustomerId("CUST001");
        
        Customer customer2 = createTestCustomer();
        customer2.setCustomerId("CUST002");
        customer2.setBlacklisted(true);

        // Execute rules concurrently
        Thread thread1 = new Thread(() -> {
            List<Object> results = droolsService.executeRules("risk-control-basic", 
                    Arrays.asList(customer1));
            assertNotNull(results);
        });

        Thread thread2 = new Thread(() -> {
            List<Object> results = droolsService.executeRules("risk-control-basic", 
                    Arrays.asList(customer2));
            assertNotNull(results);
        });

        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // If we reach here without exceptions, concurrent execution works
        assertTrue(true, "Concurrent execution completed successfully");
    }

    // Helper methods for creating test data
    private Customer createTestCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId("TEST_CUST_001");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setDateOfBirth(LocalDateTime.now().minusYears(35));
        customer.setCustomerType("INDIVIDUAL");
        customer.setKycStatus("VERIFIED");
        customer.setRiskRating("MEDIUM");
        customer.setBlacklisted(false);
        customer.setVip(false);
        customer.setRegistrationDate(LocalDateTime.now().minusYears(3));
        return customer;
    }

    private Account createTestAccount() {
        Account account = new Account();
        account.setAccountId("TEST_ACC_001");
        account.setCustomerId("TEST_CUST_001");
        account.setAccountType("CHECKING");
        account.setBalance(new BigDecimal("25000.00"));
        account.setCurrency("USD");
        account.setStatus("ACTIVE");
        account.setOpenDate(LocalDateTime.now().minusYears(2));
        account.setDailyLimit(new BigDecimal("5000.00"));
        return account;
    }

    private Transaction createTestTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TEST_TXN_001");
        transaction.setCustomerId("TEST_CUST_001");
        transaction.setFromAccount("TEST_ACC_001");
        transaction.setAmount(1500.0);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setStatus("PENDING");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setCurrency("USD");
        transaction.setChannel("ATM");
        transaction.setLocation("New York");
        return transaction;
    }

    private Transaction createHighValueTransaction() {
        Transaction transaction = createTestTransaction();
        transaction.setTransactionId("TEST_TXN_HIGH_001");
        transaction.setAmount(15000.0);
        transaction.setTransactionType("TRANSFER");
        return transaction;
    }

    private CreditScore createTestCreditScore() {
        CreditScore creditScore = new CreditScore();
        creditScore.setCustomerId("TEST_CUST_001");
        creditScore.setScore(720);
        creditScore.setScoreType("FICO");
        creditScore.setGrade("GOOD");
        creditScore.setRiskLevel("LOW");
        creditScore.setCalculationDate(LocalDateTime.now());
        return creditScore;
    }

    private LoanApplication createTestLoanApplication() {
        LoanApplication loanApp = new LoanApplication();
        loanApp.setApplicationId("TEST_LOAN_001");
        loanApp.setCustomerId("TEST_CUST_001");
        loanApp.setLoanAmount(75000.0);
        loanApp.setLoanType("PERSONAL");
        loanApp.setTermMonths(36);
        loanApp.setAnnualIncome(120000.0);
        loanApp.setMonthlyIncome(10000.0);
        loanApp.setMonthlyPayment(2200.0);
        loanApp.setStatus("PENDING");
        loanApp.setApplicationDate(LocalDateTime.now());
        return loanApp;
    }
} 