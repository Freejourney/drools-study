package com.drools.study.features;

import com.drools.study.model.*;
import org.drools.core.ClassObjectFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Advanced Drools Features Test Class
 * 
 * This test class demonstrates advanced Drools features including:
 * - Agenda Groups and Focus
 * - Rule Salience and Priority
 * - Accumulate Functions
 * - Complex Event Processing (CEP)
 * - Global Variables
 * - Function Definitions
 * - Query Operations
 * - Rule Flow Groups
 * - Conditional Elements (and, or, not, exists, forall)
 * 
 * @author Drools Study Tutorial
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DroolsAdvancedFeaturesTest {

    private static final Logger logger = LoggerFactory.getLogger(DroolsAdvancedFeaturesTest.class);
    
    private KieContainer kieContainer;
    private KieServices kieServices;

    @BeforeEach
    void setUp() {
        kieServices = KieServices.Factory.get();
        KieRepository kieRepository = kieServices.getRepository();
        kieContainer = kieServices.getKieClasspathContainer();
        
        // Verify that our knowledge base is properly loaded
        assertNotNull(kieContainer, "KieContainer should not be null");
        logger.info("Drools container initialized successfully");
    }

    /**
     * Test 1: Agenda Groups and Focus Control
     * Demonstrates how to control rule execution order using agenda groups
     */
    @Test
    @DisplayName("Test Agenda Groups and Focus Control")
    void testAgendaGroupsAndFocus() {
        logger.info("=== Testing Agenda Groups and Focus Control ===");
        
        KieSession kieSession = kieContainer.newKieSession();
        
        // Set global variables
        kieSession.setGlobal("logger", logger);
        
        // Create test data
        Customer customer = Customer.builder()
                .customerId("CUST001")
                .fullName("John Doe")
                .creditScore(650)
                .isBlacklisted(false)
                .isVip(false)
                .build();
        
        Transaction transaction = Transaction.builder()
                .transactionId("TXN001")
                .customerId("CUST001")
                .amount(15000.0)
                .timestamp(LocalDateTime.now())
                .status("PENDING")
                .build();
        
        // Insert facts
        kieSession.insert(customer);
        kieSession.insert(transaction);
        
        // Test agenda group focus
        logger.info("Setting focus to 'risk-assessment' agenda group");
        kieSession.getAgenda().getAgendaGroup("risk-assessment").setFocus();
        
        // Fire rules - only risk-assessment rules should fire
        int rulesFired = kieSession.fireAllRules();
        logger.info("Rules fired in risk-assessment group: {}", rulesFired);
        
        // Check for risk alerts generated
        Collection<RiskAlert> alerts = (Collection<RiskAlert>) kieSession.getObjects(new ClassObjectFilter(RiskAlert.class));
        logger.info("Risk alerts generated: {}", alerts.size());
        
        // Set focus to fraud-detection group
        logger.info("Setting focus to 'fraud-detection' agenda group");
        kieSession.getAgenda().getAgendaGroup("fraud-detection").setFocus();
        
        // Fire additional rules
        int additionalRules = kieSession.fireAllRules();
        logger.info("Additional rules fired in fraud-detection group: {}", additionalRules);
        
        assertTrue(rulesFired > 0, "At least one rule should have fired");
        
        kieSession.dispose();
    }

    /**
     * Test 2: Rule Salience and Priority
     * Demonstrates how rule salience affects execution order
     */
    @Test
    @DisplayName("Test Rule Salience and Priority")
    void testRuleSalienceAndPriority() {
        logger.info("=== Testing Rule Salience and Priority ===");
        
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("logger", logger);
        
        // Create a customer that will trigger multiple rules
        Customer customer = Customer.builder()
                .customerId("CUST002")
                .fullName("Jane Smith")
                .creditScore(550)  // Poor credit - will trigger high salience rule
                .isBlacklisted(true)  // Blacklisted - will trigger highest salience rule
                .isVip(false)
                .build();
        
        kieSession.insert(customer);
        
        // Track rule execution order
        List<String> executionOrder = new java.util.ArrayList<>();
        
        // Add an agenda event listener to track rule firing order
        kieSession.addEventListener(new org.kie.api.event.rule.DefaultAgendaEventListener() {
            @Override
            public void afterMatchFired(org.kie.api.event.rule.AfterMatchFiredEvent event) {
                executionOrder.add(event.getMatch().getRule().getName());
                logger.info("Rule fired: {} (salience: {})", 
                           event.getMatch().getRule().getName(),
                           event.getMatch().getRule().getSalience());
            }
        });
        
        int rulesFired = kieSession.fireAllRules();
        logger.info("Total rules fired: {}", rulesFired);
        logger.info("Execution order: {}", executionOrder);
        
        // Higher salience rules should fire first
        // Blacklisted customer rule should fire before poor credit rule
        assertTrue(rulesFired > 0, "Rules should have fired");
        
        kieSession.dispose();
    }

    /**
     * Test 3: Accumulate Functions
     * Demonstrates complex aggregation using accumulate
     */
    @Test
    @DisplayName("Test Accumulate Functions for Risk Calculation")
    void testAccumulateFunctions() {
        logger.info("=== Testing Accumulate Functions ===");
        
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("logger", logger);
        
        // Create customer
        Customer customer = Customer.builder()
                .customerId("CUST003")
                .fullName("Bob Johnson")
                .creditScore(700)
                .build();
        
        // Create multiple transactions for accumulation
        Transaction txn1 = Transaction.builder()
                .transactionId("TXN001")
                .customerId("CUST003")
                .amount(5000.0)
                .transactionTime(LocalDateTime.now().minusMinutes(10))
                .merchantCategory("RETAIL")
                .build();
        
        Transaction txn2 = Transaction.builder()
                .transactionId("TXN002")
                .customerId("CUST003")
                .amount(7500.0)
                .transactionTime(LocalDateTime.now().minusMinutes(5))
                .merchantCategory("ATM")
                .build();
        
        Transaction txn3 = Transaction.builder()
                .transactionId("TXN003")
                .customerId("CUST003")
                .amount(12000.0)
                .transactionTime(LocalDateTime.now())
                .merchantCategory("CASINO")
                .build();
        
        // Insert facts
        kieSession.insert(customer);
        kieSession.insert(txn1);
        kieSession.insert(txn2);
        kieSession.insert(txn3);
        
        // Set focus to fraud-detection to trigger accumulate rules
        kieSession.getAgenda().getAgendaGroup("fraud-detection").setFocus();
        
        int rulesFired = kieSession.fireAllRules();
        logger.info("Rules fired: {}", rulesFired);
        
        // Check for alerts generated by accumulate functions
        Collection<RiskAlert> alerts = (Collection<RiskAlert>) kieSession.getObjects(new ClassObjectFilter(RiskAlert.class));
        logger.info("Alerts generated: {}", alerts.size());
        
        for (RiskAlert alert : alerts) {
            logger.info("Alert: {} - {}", alert.getAlertType(), alert.getDescription());
        }
        
        assertTrue(alerts.size() > 0, "Accumulate rules should generate alerts");
        
        kieSession.dispose();
    }

    /**
     * Test 4: Complex Event Processing (CEP) - Time-based Rules
     * Demonstrates temporal pattern matching
     */
    @Test
    @DisplayName("Test Complex Event Processing - Temporal Patterns")
    void testComplexEventProcessing() {
        logger.info("=== Testing Complex Event Processing ===");
        
        // Create a session with stream mode for CEP
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("logger", logger);
        
        Customer customer = Customer.builder()
                .customerId("CUST004")
                .fullName("Alice Williams")
                .creditScore(680)
                .build();
        
        kieSession.insert(customer);
        
        // Insert time-based events
        LocalDateTime baseTime = LocalDateTime.now();
        
        // Create a series of transactions in rapid succession
        for (int i = 1; i <= 6; i++) {
            Transaction transaction = Transaction.builder()
                    .transactionId("TXN00" + i)
                    .customerId("CUST004")
                    .amount(1000.0 + (i * 500))
                    .transactionTime(baseTime.plusMinutes(i))
                    .location("Location" + i)
                    .build();
            
            kieSession.insert(transaction);
            logger.info("Inserted transaction {} at time {}", transaction.getTransactionId(), transaction.getTransactionTime());
        }
        
        // Set focus to fraud-detection for velocity rules
        kieSession.getAgenda().getAgendaGroup("fraud-detection").setFocus();
        
        int rulesFired = kieSession.fireAllRules();
        logger.info("CEP rules fired: {}", rulesFired);
        
        // Check for velocity-based alerts
        Collection<RiskAlert> alerts = (Collection<RiskAlert>) kieSession.getObjects(new ClassObjectFilter(RiskAlert.class));
        logger.info("CEP alerts generated: {}", alerts.size());
        
        boolean velocityAlertFound = alerts.stream()
                .anyMatch(alert -> alert.getAlertType().contains("VELOCITY"));
        
        assertTrue(velocityAlertFound, "Velocity fraud alert should be generated");
        
        kieSession.dispose();
    }

    /**
     * Test 5: Global Variables and Functions
     * Demonstrates the use of global variables and custom functions
     */
    @Test
    @DisplayName("Test Global Variables and Functions")
    void testGlobalVariablesAndFunctions() {
        logger.info("=== Testing Global Variables and Functions ===");
        
        KieSession kieSession = kieContainer.newKieSession();
        
        // Set global variables
        kieSession.setGlobal("logger", logger);
        kieSession.setGlobal("maxDailyTransactions", 10);
        kieSession.setGlobal("maxSingleTransactionAmount", 50000.0);
        kieSession.setGlobal("suspiciousVelocityThreshold", 5);
        
        Customer customer = Customer.builder()
                .customerId("CUST005")
                .fullName("Charlie Brown")
                .creditScore(720)
                .build();
        
        Transaction largeTransaction = Transaction.builder()
                .transactionId("TXN_LARGE")
                .customerId("CUST005")
                .amount(75000.0)  // Exceeds global threshold
                .transactionTime(LocalDateTime.now())
                .build();
        
        kieSession.insert(customer);
        kieSession.insert(largeTransaction);
        
        int rulesFired = kieSession.fireAllRules();
        logger.info("Rules fired with global variables: {}", rulesFired);
        
        Collection<RiskAlert> alerts = (Collection<RiskAlert>) kieSession.getObjects(new ClassObjectFilter(RiskAlert.class));
        logger.info("Alerts using global variables: {}", alerts.size());
        
        kieSession.dispose();
    }

    /**
     * Test 6: Conditional Elements (exists, not, forall)
     * Demonstrates complex logical conditions
     */
    @Test
    @DisplayName("Test Conditional Elements - exists, not, forall")
    void testConditionalElements() {
        logger.info("=== Testing Conditional Elements ===");
        
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("logger", logger);
        
        // Test scenario 1: Customer exists but no account
        Customer customerWithoutAccount = Customer.builder()
                .customerId("CUST006")
                .fullName("David Davis")
                .creditScore(650)
                .build();
        
        kieSession.insert(customerWithoutAccount);
        
        // Test scenario 2: Customer with account
        Customer customerWithAccount = Customer.builder()
                .customerId("CUST007")
                .fullName("Eva Evans")
                .creditScore(720)
                .build();
        
        Account account = Account.builder()
                .accountNumber("ACC001")
                .customerId("CUST007")
                .accountType("CHECKING")
                .balance(25000.0)
                .status("ACTIVE")
                .build();
        
        kieSession.insert(customerWithAccount);
        kieSession.insert(account);
        
        int rulesFired = kieSession.fireAllRules();
        logger.info("Rules fired with conditional elements: {}", rulesFired);
        
        assertTrue(rulesFired > 0, "Conditional element rules should fire");
        
        kieSession.dispose();
    }

    /**
     * Test 7: Stateless Session for Batch Processing
     * Demonstrates stateless processing for high-performance scenarios
     */
    @Test
    @DisplayName("Test Stateless Session for Batch Processing")
    void testStatelessSession() {
        logger.info("=== Testing Stateless Session ===");
        
        StatelessKieSession statelessSession = kieContainer.newStatelessKieSession();
        statelessSession.setGlobal("logger", logger);
        
        // Create batch of facts
        List<Object> facts = new java.util.ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Customer customer = Customer.builder()
                    .customerId("BATCH_CUST_" + i)
                    .fullName("Batch Customer " + i)
                    .creditScore(600 + (i * 20))
                    .isBlacklisted(i % 2 == 0)  // Every other customer is blacklisted
                    .build();
            
            facts.add(customer);
        }
        
        // Execute batch processing
        logger.info("Processing batch of {} customers", facts.size());
        statelessSession.execute(facts);
        
        // Note: In stateless sessions, we can't query the working memory
        // Results would typically be collected through output channels or globals
        logger.info("Batch processing completed");
    }

    /**
     * Test 8: Rule Modification and Update
     * Demonstrates modifying facts and triggering rule re-evaluation
     */
    @Test
    @DisplayName("Test Rule Modification and Update")
    void testRuleModificationAndUpdate() {
        logger.info("=== Testing Rule Modification and Update ===");
        
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("logger", logger);
        
        Customer customer = Customer.builder()
                .customerId("CUST008")
                .fullName("Frank Foster")
                .creditScore(550)  // Initially poor credit
                .isVip(false)
                .build();
        
        kieSession.insert(customer);
        
        // Fire rules with initial data
        int initialRules = kieSession.fireAllRules();
        logger.info("Initial rules fired: {}", initialRules);
        
        Collection<RiskAlert> initialAlerts = (Collection<RiskAlert>) kieSession.getObjects(new ClassObjectFilter(RiskAlert.class));
        logger.info("Initial alerts: {}", initialAlerts.size());
        
        // Modify customer data (improve credit score)
        customer.setCreditScore(750);  // Now good credit
        customer.setIsVip(true);       // Make VIP
        
        kieSession.update(kieSession.getFactHandle(customer), customer);
        
        // Fire rules again after modification
        int updatedRules = kieSession.fireAllRules();
        logger.info("Rules fired after update: {}", updatedRules);
        
        Collection<RiskAlert> finalAlerts = (Collection<RiskAlert>) kieSession.getObjects(new ClassObjectFilter(RiskAlert.class));
        logger.info("Final alerts: {}", finalAlerts.size());
        
        // The total number of rules may change based on the updated customer data
        assertTrue(initialRules > 0 || updatedRules > 0, "Rules should fire either initially or after update");
        
        kieSession.dispose();
    }
} 