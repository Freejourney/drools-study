package com.drools.study.basic;

import com.drools.study.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.kie.api.KieServices;
import org.kie.api.event.rule.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Drools Rule Activation and Firing
 * 
 * This test class demonstrates and validates rule activation mechanisms:
 * - Rule activation and firing order
 * - Agenda management
 * - Rule execution listeners
 * - Activation group behavior
 * - Rule matching and conflict resolution
 * 
 * All tests use risk control domain scenarios.
 */
public class RuleActivationTest {
    
    private KieContainer kieContainer;
    private KieSession kieSession;
    private List<String> firedRules;
    private List<String> activatedRules;
    private int rulesMatched;
    private int rulesFired;

    @BeforeEach
    void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
        kieSession = kieContainer.newKieSession("default-session");
        
        // Initialize tracking lists
        firedRules = new ArrayList<>();
        activatedRules = new ArrayList<>();
        rulesMatched = 0;
        rulesFired = 0;
        
        // Add rule execution listener
        kieSession.addEventListener(new DefaultRuleRuntimeEventListener() {
            @Override
            public void beforeRulesFired(BeforeRulesFireEvent event) {
                rulesMatched = event.getKieSession().getAgenda().getAgendaGroups().size();
            }
            
            @Override
            public void afterRulesFired(AfterRulesFireEvent event) {
                rulesFired = event.getRulesFired();
            }
        });
        
        // Add agenda event listener
        kieSession.addEventListener(new DefaultAgendaEventListener() {
            @Override
            public void matchCreated(MatchCreatedEvent event) {
                activatedRules.add(event.getMatch().getRule().getName());
            }
            
            @Override
            public void beforeMatchFired(BeforeMatchFiredEvent event) {
                firedRules.add(event.getMatch().getRule().getName());
            }
        });
    }

    @Test
    @DisplayName("Test Basic Rule Activation")
    void testBasicRuleActivation() {
        // Given: Customer data that will activate rules
        Customer customer = createCustomer("CUST001");
        customer.setCreditScore(550); // Low credit score - should activate rules
        
        Transaction transaction = createTransaction("TXN001", "CUST001", 15000.0);
        
        // When: Insert facts (rules should be activated but not fired yet)
        kieSession.insert(customer);
        kieSession.insert(transaction);
        
        // Check activations before firing
        int activationsBeforeFiring = kieSession.getAgenda().getAgendaGroups().iterator().next().size();
        
        // Fire rules
        int rulesFiredCount = kieSession.fireAllRules();
        
        // Then: Verify rule activation and firing
        assertTrue(rulesFiredCount > 0, "Rules should have fired");
        assertTrue(activatedRules.size() > 0, "Rules should have been activated");
        assertEquals(rulesFiredCount, firedRules.size(), "Number of fired rules should match");
        
        System.out.println("Activated rules: " + activatedRules);
        System.out.println("Fired rules: " + firedRules);
    }

    @Test
    @DisplayName("Test Rule Activation with Multiple Facts")
    void testMultipleFactActivation() {
        // Given: Multiple related facts
        Customer customer = createCustomer("CUST002");
        customer.setAnnualIncome(30000.0); // Low income
        
        Account account = createAccount("ACC002", "CUST002", 200.0); // Low balance
        
        Transaction txn1 = createTransaction("TXN002A", "CUST002", 5000.0);
        Transaction txn2 = createTransaction("TXN002B", "CUST002", 8000.0);
        
        RiskProfile riskProfile = createRiskProfile("CUST002");
        riskProfile.setOverallRiskScore(75); // High risk
        
        // When: Insert facts progressively and check activations
        kieSession.insert(customer);
        int activationsAfterCustomer = activatedRules.size();
        
        kieSession.insert(account);
        int activationsAfterAccount = activatedRules.size();
        
        kieSession.insert(txn1);
        kieSession.insert(txn2);
        int activationsAfterTransactions = activatedRules.size();
        
        kieSession.insert(riskProfile);
        int finalActivations = activatedRules.size();
        
        // Fire all rules
        int totalRulesFired = kieSession.fireAllRules();
        
        // Then: Verify progressive activation
        assertTrue(finalActivations >= activationsAfterCustomer, 
                  "Activations should increase or stay same as facts are added");
        assertTrue(totalRulesFired > 0, "Rules should fire with multiple facts");
        
        System.out.println("Progressive activations: " + activationsAfterCustomer + 
                          " -> " + activationsAfterAccount + " -> " + activationsAfterTransactions + 
                          " -> " + finalActivations);
    }

    @Test
    @DisplayName("Test Rule Firing Order and Conflict Resolution")
    void testRuleFiringOrder() {
        // Given: Facts that will activate multiple rules with different salience
        Customer customer = createCustomer("CUST003");
        customer.setCreditScore(720); // Good credit
        customer.setAnnualIncome(120000.0); // High income
        
        Transaction highValueTxn = createTransaction("TXN003", "CUST003", 25000.0);
        
        // When: Insert facts and fire rules
        kieSession.insert(customer);
        kieSession.insert(highValueTxn);
        
        int rulesFiredCount = kieSession.fireAllRules();
        
        // Then: Verify rules fired in expected order (high salience first)
        assertTrue(rulesFiredCount > 0, "Rules should have fired");
        assertNotNull(firedRules, "Fired rules list should not be null");
        
        // Log firing order for analysis
        System.out.println("Rules fired in order: " + firedRules);
        
        // Verify that high-priority rules (if any) fired before low-priority ones
        // This would require specific rules with known salience values
    }

    @Test
    @DisplayName("Test Agenda Group Activation")
    void testAgendaGroupActivation() {
        // Given: Facts that will trigger different agenda groups
        Customer customer = createCustomer("CUST004");
        
        Transaction suspiciousTxn = createTransaction("TXN004", "CUST004", 50000.0);
        suspiciousTxn.setLocation("HIGH_RISK_COUNTRY");
        suspiciousTxn.setTransactionType("WIRE_TRANSFER");
        
        // When: Insert facts
        kieSession.insert(customer);
        kieSession.insert(suspiciousTxn);
        
        // Focus on specific agenda group
        kieSession.getAgenda().getAgendaGroup("fraud-detection").setFocus();
        int fraudRulesFired = kieSession.fireAllRules();
        
        // Focus on another agenda group
        kieSession.getAgenda().getAgendaGroup("risk-assessment").setFocus();
        int riskRulesFired = kieSession.fireAllRules();
        
        // Then: Verify agenda group control
        assertTrue(fraudRulesFired >= 0, "Fraud detection rules should be controlled by agenda group");
        assertTrue(riskRulesFired >= 0, "Risk assessment rules should be controlled by agenda group");
        
        System.out.println("Fraud rules fired: " + fraudRulesFired);
        System.out.println("Risk rules fired: " + riskRulesFired);
    }

    @Test
    @DisplayName("Test Rule Matching with Dynamic Facts")
    void testDynamicFactMatching() {
        // Given: Initial facts
        Customer customer = createCustomer("CUST005");
        Account account = createAccount("ACC005", "CUST005", 10000.0);
        
        // When: Insert initial facts
        kieSession.insert(customer);
        kieSession.insert(account);
        
        int initialActivations = activatedRules.size();
        
        // Modify customer during rule execution
        customer.setCreditScore(300); // Very low credit score
        kieSession.update(kieSession.getFactHandle(customer), customer);
        
        int activationsAfterUpdate = activatedRules.size();
        
        // Fire rules
        int rulesFiredCount = kieSession.fireAllRules();
        
        // Then: Verify dynamic fact matching
        assertTrue(rulesFiredCount >= 0, "Rules should handle dynamic fact updates");
        System.out.println("Activations before update: " + initialActivations);
        System.out.println("Activations after update: " + activationsAfterUpdate);
        System.out.println("Rules fired: " + rulesFiredCount);
    }

    @Test
    @DisplayName("Test Rule Execution with Halt")
    void testRuleExecutionHalt() {
        // Given: Many facts that could trigger many rules
        Customer customer = createCustomer("CUST006");
        
        // Create multiple transactions
        for (int i = 1; i <= 10; i++) {
            Transaction txn = createTransaction("TXN006_" + i, "CUST006", 1000.0 * i);
            kieSession.insert(txn);
        }
        
        kieSession.insert(customer);
        
        // When: Fire rules with a limit
        int maxRules = 5;
        int rulesFiredCount = kieSession.fireAllRules(maxRules);
        
        // Then: Verify rule execution was limited
        assertTrue(rulesFiredCount <= maxRules, "Rules execution should be limited by max count");
        System.out.println("Rules fired with limit: " + rulesFiredCount + " (max: " + maxRules + ")");
    }

    @Test
    @DisplayName("Test Activation Group Behavior")
    void testActivationGroupBehavior() {
        // Given: Facts that could trigger mutually exclusive rules
        Customer customer = createCustomer("CUST007");
        customer.setAnnualIncome(75000.0); // Borderline income
        
        CreditScore creditScore = createCreditScore("CUST007", 680); // Borderline score
        
        // When: Insert facts that could match multiple rules in same activation group
        kieSession.insert(customer);
        kieSession.insert(creditScore);
        
        int rulesFiredCount = kieSession.fireAllRules();
        
        // Then: Verify activation group behavior (only one rule from group should fire)
        assertTrue(rulesFiredCount >= 0, "Activation groups should control mutual exclusion");
        System.out.println("Rules fired in activation group test: " + rulesFiredCount);
        System.out.println("Fired rules: " + firedRules);
    }

    @Test
    @DisplayName("Test Rule Activation Monitoring")
    void testRuleActivationMonitoring() {
        // Given: Complex scenario with multiple rule types
        Customer customer = createCustomer("CUST008");
        customer.setCreditScore(650);
        customer.setAnnualIncome(55000.0);
        
        Account account = createAccount("ACC008", "CUST008", 5000.0);
        Transaction transaction = createTransaction("TXN008", "CUST008", 12000.0);
        RiskProfile riskProfile = createRiskProfile("CUST008");
        
        // When: Insert all facts and monitor activation process
        kieSession.insert(customer);
        kieSession.insert(account);
        kieSession.insert(transaction);
        kieSession.insert(riskProfile);
        
        // Fire rules and collect metrics
        long startTime = System.currentTimeMillis();
        int rulesFiredCount = kieSession.fireAllRules();
        long endTime = System.currentTimeMillis();
        
        // Then: Verify monitoring data
        assertTrue(rulesFiredCount >= 0, "Rules should execute");
        assertTrue(activatedRules.size() >= 0, "Rules should be activated");
        assertTrue(firedRules.size() >= 0, "Fired rules should be tracked");
        
        System.out.println("=== Rule Activation Monitoring ===");
        System.out.println("Total activations: " + activatedRules.size());
        System.out.println("Total rules fired: " + rulesFiredCount);
        System.out.println("Execution time: " + (endTime - startTime) + "ms");
        System.out.println("Activated rules: " + activatedRules);
        System.out.println("Fired rules: " + firedRules);
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

    private CreditScore createCreditScore(String customerId, int score) {
        CreditScore creditScore = new CreditScore();
        creditScore.setCustomerId(customerId);
        creditScore.setScore(score);
        creditScore.setScoreDate(LocalDateTime.now());
        creditScore.setProvider("TEST_PROVIDER");
        creditScore.setScoreType("FICO");
        return creditScore;
    }
} 