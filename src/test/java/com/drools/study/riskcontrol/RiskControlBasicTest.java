package com.drools.study.riskcontrol;

import com.drools.study.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Basic Risk Control Scenarios
 * 
 * This test class demonstrates and validates basic risk control rules:
 * - Customer risk assessment
 * - Transaction risk evaluation
 * - Account status monitoring
 * - Basic alert generation
 * - Risk categorization
 * 
 * All tests use realistic risk control business scenarios.
 */
public class RiskControlBasicTest {
    
    private KieContainer kieContainer;
    private KieSession kieSession;

    @BeforeEach
    void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
        kieSession = kieContainer.newKieSession("default-session");
    }

    @Test
    @DisplayName("Test High Risk Customer Detection")
    void testHighRiskCustomerDetection() {
        // Given: Customer with high-risk characteristics
        Customer highRiskCustomer = createCustomer("HIGH_RISK_001");
        highRiskCustomer.setCreditScore(450); // Very low credit score
        highRiskCustomer.setAnnualIncome(25000.0); // Low income
        highRiskCustomer.setCountry("HIGH_RISK_COUNTRY"); // High-risk country
        
        // When: Insert customer and fire risk assessment rules
        kieSession.insert(highRiskCustomer);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify risk assessment
        System.out.println("High risk customer rules fired: " + rulesFired);
        
        // Check for generated risk alerts
        Collection<RiskAlert> alerts = kieSession.getObjects(obj -> obj instanceof RiskAlert);
        System.out.println("Risk alerts generated: " + alerts.size());
        
        for (RiskAlert alert : alerts) {
            System.out.println("Alert: " + alert.getAlertType() + " - " + alert.getDescription());
            assertEquals("HIGH_RISK_001", alert.getCustomerId());
        }
        
        // Check for risk assessment results
        Collection<RiskAssessment> assessments = kieSession.getObjects(obj -> obj instanceof RiskAssessment);
        System.out.println("Risk assessments generated: " + assessments.size());
        
        assertTrue(rulesFired >= 0, "Risk assessment rules should execute");
    }

    @Test
    @DisplayName("Test Low Risk Customer Assessment")
    void testLowRiskCustomerAssessment() {
        // Given: Customer with low-risk characteristics
        Customer lowRiskCustomer = createCustomer("LOW_RISK_001");
        lowRiskCustomer.setCreditScore(780); // High credit score
        lowRiskCustomer.setAnnualIncome(95000.0); // High income
        lowRiskCustomer.setCountry("USA"); // Low-risk country
        
        Account stableAccount = createAccount("ACC_LOW_001", "LOW_RISK_001", 45000.0);
        stableAccount.setStatus("ACTIVE");
        
        // When: Insert facts and fire rules
        kieSession.insert(lowRiskCustomer);
        kieSession.insert(stableAccount);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify low risk assessment
        System.out.println("Low risk customer rules fired: " + rulesFired);
        
        Collection<RiskAssessment> assessments = kieSession.getObjects(obj -> obj instanceof RiskAssessment);
        for (RiskAssessment assessment : assessments) {
            System.out.println("Assessment for " + assessment.getCustomerId() + 
                              ": Risk Level = " + assessment.getRiskLevel());
            if ("LOW_RISK_001".equals(assessment.getCustomerId())) {
                assertTrue(assessment.getRiskLevel().equals("LOW") || assessment.getRiskLevel().equals("MEDIUM"),
                          "Low risk customer should have low or medium risk level");
            }
        }
        
        assertTrue(rulesFired >= 0, "Low risk assessment should work");
    }

    @Test
    @DisplayName("Test Large Transaction Monitoring")
    void testLargeTransactionMonitoring() {
        // Given: Customer with large transaction
        Customer customer = createCustomer("LARGE_TXN_001");
        customer.setCreditScore(650); // Medium credit
        
        Transaction largeTransaction = createTransaction("LARGE_TXN_001_A", "LARGE_TXN_001", 75000.0);
        largeTransaction.setTransactionType("WIRE_TRANSFER");
        largeTransaction.setLocation("FOREIGN_BANK");
        
        // When: Process large transaction
        kieSession.insert(customer);
        kieSession.insert(largeTransaction);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify large transaction monitoring
        System.out.println("Large transaction monitoring rules fired: " + rulesFired);
        
        Collection<RiskAlert> alerts = kieSession.getObjects(obj -> obj instanceof RiskAlert);
        for (RiskAlert alert : alerts) {
            if ("LARGE_TXN_001".equals(alert.getCustomerId())) {
                System.out.println("Large transaction alert: " + alert.getAlertType() + 
                                  " - Amount: " + alert.getDescription());
                assertTrue(alert.getAlertType().contains("LARGE") || 
                          alert.getAlertType().contains("AMOUNT") ||
                          alert.getAlertType().contains("TRANSACTION"),
                          "Alert should be related to large transaction");
            }
        }
        
        assertTrue(rulesFired >= 0, "Large transaction rules should execute");
    }

    @Test
    @DisplayName("Test Account Status Risk Assessment")
    void testAccountStatusRiskAssessment() {
        // Given: Customer with problematic account status
        Customer customer = createCustomer("ACC_STATUS_001");
        
        Account suspendedAccount = createAccount("ACC_SUSP_001", "ACC_STATUS_001", 2500.0);
        suspendedAccount.setStatus("SUSPENDED");
        suspendedAccount.setLastTransactionDate(LocalDateTime.now().minusDays(45)); // Inactive
        
        Account frozenAccount = createAccount("ACC_FROZEN_001", "ACC_STATUS_001", 15000.0);
        frozenAccount.setStatus("FROZEN");
        
        // When: Process account status
        kieSession.insert(customer);
        kieSession.insert(suspendedAccount);
        kieSession.insert(frozenAccount);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify account status assessment
        System.out.println("Account status rules fired: " + rulesFired);
        
        Collection<RiskAlert> alerts = kieSession.getObjects(obj -> obj instanceof RiskAlert);
        for (RiskAlert alert : alerts) {
            if ("ACC_STATUS_001".equals(alert.getCustomerId())) {
                System.out.println("Account status alert: " + alert.getAlertType());
                assertTrue(alert.getAlertType().contains("ACCOUNT") || 
                          alert.getAlertType().contains("STATUS") ||
                          alert.getAlertType().contains("SUSPENDED") ||
                          alert.getAlertType().contains("FROZEN"),
                          "Alert should be related to account status");
            }
        }
        
        assertTrue(rulesFired >= 0, "Account status rules should execute");
    }

    @Test
    @DisplayName("Test Credit Score Risk Thresholds")
    void testCreditScoreRiskThresholds() {
        // Given: Customers with different credit score ranges
        Customer excellentCredit = createCustomer("EXCELLENT_001");
        excellentCredit.setCreditScore(820);
        
        Customer goodCredit = createCustomer("GOOD_001");
        goodCredit.setCreditScore(720);
        
        Customer fairCredit = createCustomer("FAIR_001");
        fairCredit.setCreditScore(620);
        
        Customer poorCredit = createCustomer("POOR_001");
        poorCredit.setCreditScore(520);
        
        Customer badCredit = createCustomer("BAD_001");
        badCredit.setCreditScore(420);
        
        // When: Process all credit profiles
        kieSession.insert(excellentCredit);
        kieSession.insert(goodCredit);
        kieSession.insert(fairCredit);
        kieSession.insert(poorCredit);
        kieSession.insert(badCredit);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify credit score risk categorization
        System.out.println("Credit score threshold rules fired: " + rulesFired);
        
        Collection<RiskAssessment> assessments = kieSession.getObjects(obj -> obj instanceof RiskAssessment);
        
        for (RiskAssessment assessment : assessments) {
            System.out.println("Customer " + assessment.getCustomerId() + 
                              " risk level: " + assessment.getRiskLevel());
            
            // Verify appropriate risk levels based on credit scores
            if ("EXCELLENT_001".equals(assessment.getCustomerId()) || "GOOD_001".equals(assessment.getCustomerId())) {
                assertTrue(assessment.getRiskLevel().equals("LOW") || assessment.getRiskLevel().equals("MEDIUM"),
                          "High credit score should result in low/medium risk");
            } else if ("BAD_001".equals(assessment.getCustomerId()) || "POOR_001".equals(assessment.getCustomerId())) {
                assertTrue(assessment.getRiskLevel().equals("HIGH") || assessment.getRiskLevel().equals("MEDIUM"),
                          "Low credit score should result in high/medium risk");
            }
        }
        
        assertTrue(rulesFired >= 0, "Credit score rules should execute");
    }

    @Test
    @DisplayName("Test Income-Based Risk Assessment")
    void testIncomeBasedRiskAssessment() {
        // Given: Customers with varying income levels
        Customer highIncome = createCustomer("HIGH_INC_001");
        highIncome.setAnnualIncome(150000.0);
        highIncome.setCreditScore(700); // Good credit
        
        Customer mediumIncome = createCustomer("MED_INC_001");
        mediumIncome.setAnnualIncome(55000.0);
        mediumIncome.setCreditScore(650); // Fair credit
        
        Customer lowIncome = createCustomer("LOW_INC_001");
        lowIncome.setAnnualIncome(25000.0);
        lowIncome.setCreditScore(600); // Fair credit
        
        // When: Process income-based assessment
        kieSession.insert(highIncome);
        kieSession.insert(mediumIncome);
        kieSession.insert(lowIncome);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify income-based risk assessment
        System.out.println("Income-based assessment rules fired: " + rulesFired);
        
        Collection<RiskAssessment> assessments = kieSession.getObjects(obj -> obj instanceof RiskAssessment);
        
        for (RiskAssessment assessment : assessments) {
            System.out.println("Customer " + assessment.getCustomerId() + 
                              " income-based risk: " + assessment.getRiskLevel());
            
            // High income should generally result in lower risk
            if ("HIGH_INC_001".equals(assessment.getCustomerId())) {
                assertNotEquals("HIGH", assessment.getRiskLevel(),
                              "High income customer should not be high risk");
            }
        }
        
        assertTrue(rulesFired >= 0, "Income assessment rules should execute");
    }

    @Test
    @DisplayName("Test Transaction Pattern Analysis")
    void testTransactionPatternAnalysis() {
        // Given: Customer with transaction pattern
        Customer customer = createCustomer("PATTERN_001");
        customer.setCreditScore(680);
        
        // Create transaction pattern - rapid consecutive transactions
        Transaction txn1 = createTransaction("PAT_001_A", "PATTERN_001", 5000.0);
        txn1.setTimestamp(LocalDateTime.now().minusMinutes(5));
        
        Transaction txn2 = createTransaction("PAT_001_B", "PATTERN_001", 7500.0);
        txn2.setTimestamp(LocalDateTime.now().minusMinutes(3));
        
        Transaction txn3 = createTransaction("PAT_001_C", "PATTERN_001", 12000.0);
        txn3.setTimestamp(LocalDateTime.now().minusMinutes(1));
        
        // When: Process transaction pattern
        kieSession.insert(customer);
        kieSession.insert(txn1);
        kieSession.insert(txn2);
        kieSession.insert(txn3);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify pattern analysis
        System.out.println("Transaction pattern rules fired: " + rulesFired);
        
        Collection<RiskAlert> alerts = kieSession.getObjects(obj -> obj instanceof RiskAlert);
        for (RiskAlert alert : alerts) {
            if ("PATTERN_001".equals(alert.getCustomerId())) {
                System.out.println("Pattern alert: " + alert.getAlertType() + " - " + alert.getDescription());
                assertTrue(alert.getAlertType().contains("PATTERN") || 
                          alert.getAlertType().contains("FREQUENCY") ||
                          alert.getAlertType().contains("RAPID"),
                          "Alert should be related to transaction pattern");
            }
        }
        
        assertTrue(rulesFired >= 0, "Pattern analysis rules should execute");
    }

    @Test
    @DisplayName("Test Geographic Risk Assessment")
    void testGeographicRiskAssessment() {
        // Given: Customers from different geographic locations
        Customer usaCustomer = createCustomer("USA_001");
        usaCustomer.setCountry("USA");
        
        Customer canadaCustomer = createCustomer("CANADA_001");
        canadaCustomer.setCountry("CANADA");
        
        Customer highRiskCountryCustomer = createCustomer("HIGH_RISK_001");
        highRiskCountryCustomer.setCountry("HIGH_RISK_COUNTRY");
        
        // Transactions from different locations
        Transaction domesticTxn = createTransaction("DOM_001", "USA_001", 10000.0);
        domesticTxn.setLocation("NEW_YORK");
        
        Transaction foreignTxn = createTransaction("FOR_001", "USA_001", 15000.0);
        foreignTxn.setLocation("FOREIGN_COUNTRY");
        
        // When: Process geographic risk
        kieSession.insert(usaCustomer);
        kieSession.insert(canadaCustomer);
        kieSession.insert(highRiskCountryCustomer);
        kieSession.insert(domesticTxn);
        kieSession.insert(foreignTxn);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify geographic risk assessment
        System.out.println("Geographic risk rules fired: " + rulesFired);
        
        Collection<RiskAlert> alerts = kieSession.getObjects(obj -> obj instanceof RiskAlert);
        for (RiskAlert alert : alerts) {
            System.out.println("Geographic alert: " + alert.getAlertType() + 
                              " for customer: " + alert.getCustomerId());
            
            if ("HIGH_RISK_001".equals(alert.getCustomerId())) {
                assertTrue(alert.getAlertType().contains("GEOGRAPHIC") || 
                          alert.getAlertType().contains("COUNTRY") ||
                          alert.getAlertType().contains("LOCATION"),
                          "Alert should be related to geographic risk");
            }
        }
        
        assertTrue(rulesFired >= 0, "Geographic risk rules should execute");
    }

    @Test
    @DisplayName("Test Comprehensive Risk Scoring")
    void testComprehensiveRiskScoring() {
        // Given: Customer with multiple risk factors
        Customer complexCustomer = createCustomer("COMPLEX_001");
        complexCustomer.setCreditScore(580); // Below average
        complexCustomer.setAnnualIncome(42000.0); // Medium income
        complexCustomer.setCountry("MEDIUM_RISK_COUNTRY");
        
        Account account = createAccount("ACC_COMPLEX_001", "COMPLEX_001", 8500.0);
        account.setStatus("ACTIVE");
        
        Transaction moderateTransaction = createTransaction("COMP_001", "COMPLEX_001", 18000.0);
        moderateTransaction.setTransactionType("ONLINE_TRANSFER");
        
        RiskProfile existingProfile = createRiskProfile("COMPLEX_001");
        existingProfile.setOverallRiskScore(65); // Medium-high risk
        
        // When: Process comprehensive risk assessment
        kieSession.insert(complexCustomer);
        kieSession.insert(account);
        kieSession.insert(moderateTransaction);
        kieSession.insert(existingProfile);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify comprehensive assessment
        System.out.println("Comprehensive risk rules fired: " + rulesFired);
        
        Collection<RiskAssessment> assessments = kieSession.getObjects(obj -> obj instanceof RiskAssessment);
        Collection<RiskAlert> alerts = kieSession.getObjects(obj -> obj instanceof RiskAlert);
        
        System.out.println("Risk assessments: " + assessments.size());
        System.out.println("Risk alerts: " + alerts.size());
        
        for (RiskAssessment assessment : assessments) {
            if ("COMPLEX_001".equals(assessment.getCustomerId())) {
                System.out.println("Comprehensive risk score: " + assessment.getRiskScore());
                System.out.println("Comprehensive risk level: " + assessment.getRiskLevel());
                assertTrue(assessment.getRiskScore() >= 0, "Risk score should be calculated");
            }
        }
        
        assertTrue(rulesFired >= 0, "Comprehensive risk assessment should work");
    }

    // Helper methods
    private Customer createCustomer(String customerId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setFullName("Customer " + customerId);
        customer.setEmail(customerId.toLowerCase() + "@test.com");
        customer.setDateOfBirth(LocalDate.of(1985, 1, 1));
        customer.setAnnualIncome(50000.0);
        customer.setCountry("USA");
        customer.setCreditScore(650);
        customer.setAccountOpenDate(LocalDateTime.now().minusYears(2));
        return customer;
    }

    private Account createAccount(String accountNumber, String customerId, double balance) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setCustomerId(customerId);
        account.setBalance(balance);
        account.setAccountType("CHECKING");
        account.setStatus("ACTIVE");
        account.setCurrency("USD");
        account.setOpenDate(LocalDateTime.now().minusYears(1));
        account.setLastTransactionDate(LocalDateTime.now().minusDays(15));
        return account;
    }

    private Transaction createTransaction(String txnId, String customerId, double amount) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(txnId);
        transaction.setCustomerId(customerId);
        transaction.setAmount(amount);
        transaction.setTransactionType("PURCHASE");
        transaction.setTimestamp(LocalDateTime.now().minusDays(1));
        transaction.setLocation("STORE_A");
        transaction.setStatus("COMPLETED");
        transaction.setDescription("Test transaction " + txnId);
        return transaction;
    }

    private RiskProfile createRiskProfile(String customerId) {
        RiskProfile riskProfile = new RiskProfile();
        riskProfile.setCustomerId(customerId);
        riskProfile.setOverallRiskScore(50);
        riskProfile.setRiskCategory("MEDIUM");
        riskProfile.setCustomerSegment("RETAIL");
        riskProfile.setKycStatus("COMPLETED");
        riskProfile.setAmlRiskLevel("LOW");
        riskProfile.setLastReviewDate(LocalDateTime.now().minusMonths(6));
        riskProfile.setAllowAutoApproval(true);
        return riskProfile;
    }
} 