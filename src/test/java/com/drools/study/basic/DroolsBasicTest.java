package com.drools.study.basic;

import com.drools.study.model.*;
import org.junit.jupiter.api.*;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DroolsBasicTest demonstrates fundamental Drools concepts and basic rule execution.
 * This test class covers:
 * 1. KieServices and KieContainer setup
 * 2. KieSession creation and management
 * 3. Fact insertion and rule execution
 * 4. Working memory operations
 * 5. Rule attributes and agenda groups
 * 6. Global variables usage
 * 
 * Each test method is extensively documented to explain Drools concepts
 * and provide learning examples for the tutorial.
 * 
 * @author Drools Study Tutorial
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DroolsBasicTest {
    
    private static final Logger logger = LoggerFactory.getLogger(DroolsBasicTest.class);
    
    // Static variables for test setup
    private static KieServices kieServices;
    private static KieContainer kieContainer;
    
    // Instance variables for each test
    private KieSession kieSession;
    private List<RiskAlert> alerts;
    
    /**
     * Set up KieServices and KieContainer once for all tests.
     * This method demonstrates how to programmatically create a Drools environment.
     */
    @BeforeAll
    static void setupClass() {
        logger.info("Setting up Drools environment for basic tests");
        
        // Step 1: Get KieServices instance - the main entry point for Drools
        kieServices = KieServices.Factory.get();
        assertNotNull(kieServices, "KieServices should not be null");
        
        // Step 2: Create KieFileSystem to hold rule files
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        // Step 3: Add rule files to the file system
        // ResourceFactory.newClassPathResource() loads files from classpath
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/risk-control-basic.drl"));
        
        // Step 4: Create KieBuilder to compile rules
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        
        // Step 5: Check for compilation errors
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            logger.error("Rule compilation errors:");
            results.getMessages().forEach(message -> logger.error("Error: {}", message.getText()));
            fail("Rule compilation failed with errors");
        }
        
        // Step 6: Create KieContainer from compiled rules
        kieContainer = kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
        assertNotNull(kieContainer, "KieContainer should not be null");
        
        logger.info("Drools environment setup completed successfully");
    }
    
    /**
     * Set up KieSession and global variables for each test.
     * This method demonstrates KieSession creation and configuration.
     */
    @BeforeEach
    void setUp() {
        logger.info("Setting up KieSession for test");
        
        // Create a new KieSession for each test to ensure isolation
        kieSession = kieContainer.newKieSession();
        assertNotNull(kieSession, "KieSession should not be null");
        
        // Initialize alerts collection for rule results
        alerts = new ArrayList<>();
        
        // Set up global variables that rules can access
        kieSession.setGlobal("logger", logger);
        kieSession.setGlobal("alerts", alerts);
        kieSession.setGlobal("riskThreshold", 70);
        kieSession.setGlobal("fraudThreshold", 80);
        kieSession.setGlobal("maxTransactionAmount", 50000.0);
        
        logger.info("KieSession setup completed");
    }
    
    /**
     * Clean up KieSession after each test to prevent memory leaks.
     */
    @AfterEach
    void tearDown() {
        if (kieSession != null) {
            kieSession.dispose();
            logger.info("KieSession disposed");
        }
    }
    
    /**
     * Test 1: Basic Rule Execution with Customer Facts
     * 
     * This test demonstrates:
     * - Creating domain objects (Customer)
     * - Inserting facts into working memory
     * - Firing rules and checking results
     * - Understanding rule matching and execution
     */
    @Test
    @Order(1)
    @DisplayName("Test basic rule execution with customer facts")
    void testBasicRuleExecution() {
        logger.info("Starting basic rule execution test");
        
        // Create a customer with poor credit score to trigger rules
        Customer customer = Customer.builder()
                .customerId("CUST001")
                .fullName("John Doe")
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .creditScore(550) // Poor credit score - should trigger rule
                .annualIncome(45000.0)
                .employmentStatus("EMPLOYED")
                .isBlacklisted(false)
                .isVip(false)
                .country("US")
                .build();
        
        // Insert customer fact into working memory
        // This makes the customer available for rule matching
        kieSession.insert(customer);
        
        // Fire all rules - Drools will evaluate all rule conditions
        // and execute the RHS (then part) of matching rules
        int rulesFired = kieSession.fireAllRules();
        
        // Verify that rules were fired
        assertTrue(rulesFired > 0, "At least one rule should have fired");
        logger.info("Rules fired: {}", rulesFired);
        
        // Verify that alerts were generated
        assertFalse(alerts.isEmpty(), "Alerts should have been generated");
        logger.info("Alerts generated: {}", alerts.size());
        
        // Find the specific alert we expect
        boolean foundCreditAlert = alerts.stream()
                .anyMatch(alert -> "CREDIT".equals(alert.getAlertType()) 
                                && "HIGH".equals(alert.getSeverity()));
        
        assertTrue(foundCreditAlert, "Should have generated a high credit risk alert");
        
        // Log all generated alerts for debugging
        alerts.forEach(alert -> 
            logger.info("Alert: {} - {} - {}", alert.getAlertType(), alert.getSeverity(), alert.getTitle())
        );
        
        logger.info("Basic rule execution test completed successfully");
    }
    
    /**
     * Test 2: Blacklisted Customer Detection
     * 
     * This test demonstrates:
     * - Boolean condition matching in rules
     * - Rule salience (priority) handling
     * - Critical alert generation
     * - Rule attributes usage (no-loop, salience)
     */
    @Test
    @Order(2)
    @DisplayName("Test blacklisted customer detection")
    void testBlacklistedCustomerDetection() {
        logger.info("Starting blacklisted customer detection test");
        
        // Create a blacklisted customer
        Customer blacklistedCustomer = Customer.builder()
                .customerId("CUST002")
                .fullName("Jane Smith")
                .dateOfBirth(LocalDate.of(1980, 3, 10))
                .creditScore(720) // Good credit but blacklisted
                .isBlacklisted(true) // This should trigger the blacklist rule
                .isVip(false)
                .build();
        
        // Insert the customer into working memory
        kieSession.insert(blacklistedCustomer);
        
        // Fire rules
        int rulesFired = kieSession.fireAllRules();
        
        // Verify blacklist rule was fired
        assertTrue(rulesFired > 0, "Blacklist rule should have fired");
        
        // Verify critical alert was generated
        boolean foundBlacklistAlert = alerts.stream()
                .anyMatch(alert -> "COMPLIANCE".equals(alert.getAlertType()) 
                                && "CRITICAL".equals(alert.getSeverity())
                                && alert.getTitle().contains("Blacklisted"));
        
        assertTrue(foundBlacklistAlert, "Should have generated a critical blacklist alert");
        
        // Verify regulatory reporting is required
        RiskAlert blacklistAlert = alerts.stream()
                .filter(alert -> "COMPLIANCE".equals(alert.getAlertType()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(blacklistAlert, "Blacklist alert should exist");
        assertTrue(blacklistAlert.getRequiresRegulatoryReporting(), 
                  "Blacklist alert should require regulatory reporting");
        
        logger.info("Blacklisted customer detection test completed successfully");
    }
    
    /**
     * Test 3: Transaction Monitoring and Modification
     * 
     * This test demonstrates:
     * - Multiple fact types in rules
     * - Fact modification using modify() statement
     * - Transaction pattern detection
     * - Cross-object rule conditions
     */
    @Test
    @Order(3)
    @DisplayName("Test transaction monitoring with fact modification")
    void testTransactionMonitoring() {
        logger.info("Starting transaction monitoring test");
        
        // Create customer
        Customer customer = Customer.builder()
                .customerId("CUST003")
                .fullName("Bob Johnson")
                .dateOfBirth(LocalDate.of(1975, 8, 22))
                .creditScore(680)
                .isBlacklisted(false)
                .isVip(false)
                .build();
        
        // Create high-value transaction
        Transaction transaction = Transaction.builder()
                .transactionId("TXN001")
                .customerId("CUST003")
                .amount(15000.0) // High value - should trigger rule
                .currency("USD")
                .transactionType("TRANSFER")
                .status("PENDING")
                .timestamp(LocalDateTime.now())
                .channel("ONLINE")
                .location("New York")
                .country("US")
                .isSuspicious(false) // Will be modified by rule
                .riskScore(0) // Will be set by rule
                .build();
        
        // Insert both facts
        kieSession.insert(customer);
        kieSession.insert(transaction);
        
        // Set agenda group focus for transaction monitoring
        // This demonstrates agenda group usage for rule organization
        kieSession.getAgenda().getAgendaGroup("transaction-monitoring").setFocus();
        
        // Fire rules
        int rulesFired = kieSession.fireAllRules();
        
        // Verify rules were fired
        assertTrue(rulesFired > 0, "Transaction monitoring rules should have fired");
        
        // Verify transaction was modified by the rule
        assertTrue(transaction.getIsSuspicious(), "Transaction should be marked as suspicious");
        assertEquals(60, transaction.getRiskScore(), "Transaction risk score should be set");
        
        // Verify alert was generated
        boolean foundTransactionAlert = alerts.stream()
                .anyMatch(alert -> "FRAUD".equals(alert.getAlertType()) 
                                && alert.getTransactionId().equals("TXN001"));
        
        assertTrue(foundTransactionAlert, "Should have generated a transaction alert");
        
        logger.info("Transaction monitoring test completed successfully");
    }
    
    /**
     * Test 4: Agenda Groups and Rule Execution Control
     * 
     * This test demonstrates:
     * - Agenda groups for rule organization
     * - Controlled rule execution phases
     * - Rule focus management
     * - Multi-phase rule processing
     */
    @Test
    @Order(4)
    @DisplayName("Test agenda groups and controlled rule execution")
    void testAgendaGroups() {
        logger.info("Starting agenda groups test");
        
        // Create test data
        Customer customer = Customer.builder()
                .customerId("CUST004")
                .fullName("Alice Wilson")
                .creditScore(580) // Poor credit
                .isBlacklisted(false)
                .isVip(false)
                .build();
        
        Account account = Account.builder()
                .accountNumber("ACC001")
                .customerId("CUST004")
                .balance(500.0) // Low balance
                .availableBalance(500.0)
                .accountType("CHECKING")
                .status("ACTIVE")
                .isDormant(false)
                .build();
        
        // Insert facts
        kieSession.insert(customer);
        kieSession.insert(account);
        
        // Phase 1: Execute validation rules only
        kieSession.getAgenda().getAgendaGroup("validation").setFocus();
        int validationRules = kieSession.fireAllRules();
        
        logger.info("Validation phase - rules fired: {}", validationRules);
        
        // Phase 2: Execute pattern analysis rules
        kieSession.getAgenda().getAgendaGroup("pattern-analysis").setFocus();
        int patternRules = kieSession.fireAllRules();
        
        logger.info("Pattern analysis phase - rules fired: {}", patternRules);
        
        // Verify both phases executed
        assertTrue(validationRules > 0, "Validation rules should have fired");
        
        // Check for expected alerts from different phases
        boolean hasValidationAlert = alerts.stream()
                .anyMatch(alert -> alert.getRuleName().contains("Credit Score") || 
                                 alert.getRuleName().contains("Balance"));
        
        assertTrue(hasValidationAlert, "Should have validation alerts");
        
        logger.info("Total alerts generated: {}", alerts.size());
        logger.info("Agenda groups test completed successfully");
    }
    
    /**
     * Test 5: Working Memory Operations
     * 
     * This test demonstrates:
     * - Fact handle management
     * - Fact retraction and modification
     * - Working memory queries
     * - Memory state management
     */
    @Test
    @Order(5)
    @DisplayName("Test working memory operations")
    void testWorkingMemoryOperations() {
        logger.info("Starting working memory operations test");
        
        // Create initial customer
        Customer customer = Customer.builder()
                .customerId("CUST005")
                .fullName("Charlie Brown")
                .creditScore(650)
                .isBlacklisted(false)
                .build();
        
        // Insert and get fact handle
        org.kie.api.runtime.rule.FactHandle customerHandle = kieSession.insert(customer);
        assertNotNull(customerHandle, "Fact handle should not be null");
        
        // Check working memory count
        assertEquals(1, kieSession.getFactCount(), "Should have 1 fact in working memory");
        
        // Modify customer data
        customer.setCreditScore(550); // Change to poor credit
        kieSession.update(customerHandle, customer);
        
        // Fire rules to see the effect of modification
        int rulesAfterUpdate = kieSession.fireAllRules();
        logger.info("Rules fired after update: {}", rulesAfterUpdate);
        
        // Verify alerts were generated due to poor credit
        boolean hasPoorCreditAlert = alerts.stream()
                .anyMatch(alert -> alert.getDescription().contains("poor credit"));
        
        assertTrue(hasPoorCreditAlert, "Should have poor credit alert after update");
        
        // Test fact retraction
        kieSession.delete(customerHandle);
        assertEquals(0, kieSession.getFactCount(), "Should have 0 facts after deletion");
        
        // Clear alerts and fire rules again - no new alerts should be generated
        alerts.clear();
        int rulesAfterDeletion = kieSession.fireAllRules();
        assertEquals(0, rulesAfterDeletion, "No rules should fire after fact deletion");
        assertTrue(alerts.isEmpty(), "No alerts should be generated after fact deletion");
        
        logger.info("Working memory operations test completed successfully");
    }
    
    /**
     * Test 6: Stateless Session Usage
     * 
     * This test demonstrates:
     * - Stateless vs stateful session differences
     * - Fire-and-forget rule execution
     * - Batch processing patterns
     * - Session lifecycle management
     */
    @Test
    @Order(6)
    @DisplayName("Test stateless session execution")
    void testStatelessSession() {
        logger.info("Starting stateless session test");
        
        // Create stateless session from the same container
        StatelessKieSession statelessSession = kieContainer.newStatelessKieSession();
        assertNotNull(statelessSession, "Stateless session should not be null");
        
        // Prepare facts for stateless execution
        Customer customer = Customer.builder()
                .customerId("CUST006")
                .fullName("Diana Prince")
                .creditScore(520) // Poor credit
                .isBlacklisted(false)
                .build();
        
        List<Object> facts = new ArrayList<>();
        facts.add(customer);
        facts.add(alerts); // Add alerts collection
        
        // Set up globals for stateless session
        statelessSession.setGlobal("logger", logger);
        statelessSession.setGlobal("alerts", alerts);
        
        // Execute all facts at once - stateless session doesn't maintain state
        statelessSession.execute(facts);
        
        // Verify rules were executed and alerts generated
        assertFalse(alerts.isEmpty(), "Stateless execution should generate alerts");
        
        boolean foundCreditAlert = alerts.stream()
                .anyMatch(alert -> alert.getDescription().contains("credit score"));
        
        assertTrue(foundCreditAlert, "Should have credit score alert from stateless execution");
        
        logger.info("Stateless session generated {} alerts", alerts.size());
        logger.info("Stateless session test completed successfully");
    }
    
    /**
     * Test 7: Rule Salience and Execution Order
     * 
     * This test demonstrates:
     * - Rule salience (priority) effects
     * - Execution order control
     * - Rule attributes understanding
     * - Agenda management
     */
    @Test
    @Order(7)
    @DisplayName("Test rule salience and execution order")
    void testRuleSalience() {
        logger.info("Starting rule salience test");
        
        // Create customer that will trigger multiple rules with different salience
        Customer customer = Customer.builder()
                .customerId("CUST007")
                .fullName("Edward Norton")
                .creditScore(550) // Poor credit (salience 100)
                .isBlacklisted(true) // Blacklisted (salience 200 - highest)
                .isVip(false)
                .build();
        
        Account account = Account.builder()
                .accountNumber("ACC007")
                .customerId("CUST007")
                .balance(800.0) // Low balance (salience 60)
                .availableBalance(800.0)
                .accountType("CHECKING")
                .status("ACTIVE")
                .isDormant(false)
                .build();
        
        // Insert facts
        kieSession.insert(customer);
        kieSession.insert(account);
        
        // Fire rules and capture execution order
        List<String> executionOrder = new ArrayList<>();
        
        // Add custom agenda listener to track execution order
        kieSession.addEventListener(new org.kie.api.event.rule.AgendaEventListener() {
            @Override
            public void afterMatchFired(org.kie.api.event.rule.AfterMatchFiredEvent event) {
                executionOrder.add(event.getMatch().getRule().getName());
                logger.info("Rule fired: {} (salience: {})", 
                          event.getMatch().getRule().getName(),
                          event.getMatch().getRule().getSalience());
            }
            
            // Other methods with empty implementations
            public void matchCreated(org.kie.api.event.rule.MatchCreatedEvent event) {}
            public void matchCancelled(org.kie.api.event.rule.MatchCancelledEvent event) {}
            public void beforeMatchFired(org.kie.api.event.rule.BeforeMatchFiredEvent event) {}
            public void agendaGroupPopped(org.kie.api.event.rule.AgendaGroupPoppedEvent event) {}
            public void agendaGroupPushed(org.kie.api.event.rule.AgendaGroupPushedEvent event) {}
            public void beforeRuleFlowGroupActivated(org.kie.api.event.rule.RuleFlowGroupActivatedEvent event) {}
            public void afterRuleFlowGroupActivated(org.kie.api.event.rule.RuleFlowGroupActivatedEvent event) {}
            public void beforeRuleFlowGroupDeactivated(org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent event) {}
            public void afterRuleFlowGroupDeactivated(org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent event) {}
        });
        
        int rulesFired = kieSession.fireAllRules();
        
        // Verify rules were fired
        assertTrue(rulesFired > 0, "Rules should have fired");
        
        // Log execution order for analysis
        logger.info("Rule execution order:");
        for (int i = 0; i < executionOrder.size(); i++) {
            logger.info("{}. {}", i + 1, executionOrder.get(i));
        }
        
        // Verify high salience rules executed first
        if (executionOrder.size() >= 2) {
            // Blacklist rule (salience 200) should execute before credit rule (salience 100)
            boolean blacklistFirst = executionOrder.stream()
                    .filter(rule -> rule.contains("Blacklisted") || rule.contains("Credit"))
                    .findFirst()
                    .map(rule -> rule.contains("Blacklisted"))
                    .orElse(false);
            
            assertTrue(blacklistFirst, "Blacklist rule should execute before credit rule due to higher salience");
        }
        
        logger.info("Rule salience test completed successfully");
    }
    
    /**
     * Utility method to create a sample customer for testing
     */
    private Customer createSampleCustomer(String customerId, int creditScore, boolean isBlacklisted) {
        return Customer.builder()
                .customerId(customerId)
                .fullName("Test Customer " + customerId)
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .creditScore(creditScore)
                .annualIncome(50000.0)
                .employmentStatus("EMPLOYED")
                .isBlacklisted(isBlacklisted)
                .isVip(false)
                .country("US")
                .build();
    }
} 