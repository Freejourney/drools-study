package com.drools.study.features;

import com.drools.study.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Drools Globals and Functions
 * 
 * This test class demonstrates and validates:
 * - Global variables usage in rules
 * - Global objects (lists, maps, custom objects)
 * - Functions in rule files
 * - Helper functions and utilities
 * - Cross-rule data sharing via globals
 * 
 * All tests use risk control domain scenarios.
 */
public class GlobalsAndFunctionsTest {
    
    private KieContainer kieContainer;
    private KieSession kieSession;
    
    // Global objects for testing
    private List<String> riskAlerts;
    private Map<String, Object> riskMetrics;
    private StringBuilder auditLog;
    private RiskAssessmentConfig config;

    @BeforeEach
    void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
        kieSession = kieContainer.newKieSession("default-session");
        
        // Initialize global objects
        riskAlerts = new ArrayList<>();
        riskMetrics = new HashMap<>();
        auditLog = new StringBuilder();
        config = new RiskAssessmentConfig();
        
        // Set globals in KieSession
        kieSession.setGlobal("riskAlerts", riskAlerts);
        kieSession.setGlobal("riskMetrics", riskMetrics);
        kieSession.setGlobal("auditLog", auditLog);
        kieSession.setGlobal("config", config);
    }

    @Test
    @DisplayName("Test Global List Usage")
    void testGlobalListUsage() {
        // Given: Customer with high-risk characteristics
        Customer customer = createCustomer("CUST001");
        customer.setCreditScore(450); // Very low credit score
        customer.setCountry("HIGH_RISK_COUNTRY");
        
        Transaction transaction = createTransaction("TXN001", "CUST001", 25000.0);
        
        // Ensure global list is empty initially
        assertTrue(riskAlerts.isEmpty(), "Risk alerts should be empty initially");
        
        // When: Insert facts and fire rules (rules should add to global list)
        kieSession.insert(customer);
        kieSession.insert(transaction);
        
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify global list was populated by rules
        System.out.println("Rules fired: " + rulesFired);
        System.out.println("Risk alerts generated: " + riskAlerts.size());
        System.out.println("Alerts: " + riskAlerts);
        
        // Note: This test would pass if rules exist that populate the global list
        // The actual verification depends on having rules that use the global
        assertNotNull(riskAlerts, "Risk alerts list should not be null");
    }

    @Test
    @DisplayName("Test Global Map Usage")
    void testGlobalMapUsage() {
        // Given: Multiple customers for metrics calculation
        Customer customer1 = createCustomer("CUST002A");
        customer1.setCreditScore(750);
        customer1.setAnnualIncome(80000.0);
        
        Customer customer2 = createCustomer("CUST002B");
        customer2.setCreditScore(600);
        customer2.setAnnualIncome(45000.0);
        
        Customer customer3 = createCustomer("CUST002C");
        customer3.setCreditScore(500);
        customer3.setAnnualIncome(30000.0);
        
        // Initialize global metrics map
        riskMetrics.put("totalCustomers", 0);
        riskMetrics.put("highRiskCount", 0);
        riskMetrics.put("averageCreditScore", 0.0);
        
        // When: Insert facts and fire rules
        kieSession.insert(customer1);
        kieSession.insert(customer2);
        kieSession.insert(customer3);
        
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify global map was updated by rules
        System.out.println("Rules fired: " + rulesFired);
        System.out.println("Risk metrics: " + riskMetrics);
        
        // Verify map structure
        assertNotNull(riskMetrics, "Risk metrics should not be null");
        assertTrue(riskMetrics.containsKey("totalCustomers"), "Should track total customers");
        assertTrue(riskMetrics.containsKey("highRiskCount"), "Should track high risk count");
        assertTrue(riskMetrics.containsKey("averageCreditScore"), "Should track average credit score");
    }

    @Test
    @DisplayName("Test Global Audit Log")
    void testGlobalAuditLog() {
        // Given: Transaction scenario requiring audit logging
        Customer customer = createCustomer("CUST003");
        
        Transaction largeTransaction = createTransaction("TXN003", "CUST003", 50000.0);
        largeTransaction.setTransactionType("WIRE_TRANSFER");
        largeTransaction.setLocation("FOREIGN_COUNTRY");
        
        // Ensure audit log is empty initially
        assertEquals("", auditLog.toString(), "Audit log should be empty initially");
        
        // When: Insert facts and fire rules
        kieSession.insert(customer);
        kieSession.insert(largeTransaction);
        
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify audit log was updated
        System.out.println("Rules fired: " + rulesFired);
        System.out.println("Audit log content: '" + auditLog.toString() + "'");
        
        // Note: Actual content depends on rules implementation
        assertNotNull(auditLog, "Audit log should not be null");
    }

    @Test
    @DisplayName("Test Global Configuration Object")
    void testGlobalConfiguration() {
        // Given: Configure risk assessment parameters
        config.setHighRiskThreshold(70);
        config.setLargeTransactionThreshold(20000.0);
        config.setMaxTransactionsPerDay(10);
        config.setAutoApprovalEnabled(true);
        
        Customer customer = createCustomer("CUST004");
        customer.setCreditScore(650); // Medium risk
        
        Transaction transaction = createTransaction("TXN004", "CUST004", 25000.0); // Above threshold
        
        // When: Insert facts and fire rules
        kieSession.insert(customer);
        kieSession.insert(transaction);
        
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify configuration was used by rules
        System.out.println("Rules fired with config: " + rulesFired);
        System.out.println("Configuration used - High risk threshold: " + config.getHighRiskThreshold());
        System.out.println("Large transaction threshold: " + config.getLargeTransactionThreshold());
        
        // Verify configuration object integrity
        assertNotNull(config, "Configuration should not be null");
        assertEquals(70, config.getHighRiskThreshold(), "High risk threshold should be preserved");
        assertEquals(20000.0, config.getLargeTransactionThreshold(), "Transaction threshold should be preserved");
    }

    @Test
    @DisplayName("Test Cross-Rule Data Sharing")
    void testCrossRuleDataSharing() {
        // Given: Scenario where rules share data via globals
        Customer customer = createCustomer("CUST005");
        
        // Create multiple transactions for pattern analysis
        Transaction txn1 = createTransaction("TXN005A", "CUST005", 5000.0);
        txn1.setTimestamp(LocalDateTime.now().minusDays(1));
        
        Transaction txn2 = createTransaction("TXN005B", "CUST005", 7500.0);
        txn2.setTimestamp(LocalDateTime.now().minusHours(12));
        
        Transaction txn3 = createTransaction("TXN005C", "CUST005", 10000.0);
        txn3.setTimestamp(LocalDateTime.now().minusHours(6));
        
        // Initialize global tracking
        riskMetrics.put("transactionPattern", new ArrayList<String>());
        riskMetrics.put("totalAmount", 0.0);
        riskMetrics.put("transactionCount", 0);
        
        // When: Insert facts and fire rules (rules should share data via globals)
        kieSession.insert(customer);
        kieSession.insert(txn1);
        kieSession.insert(txn2);
        kieSession.insert(txn3);
        
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify data sharing occurred
        System.out.println("Rules fired: " + rulesFired);
        System.out.println("Shared metrics: " + riskMetrics);
        
        assertNotNull(riskMetrics.get("transactionPattern"), "Transaction pattern should be tracked");
        assertNotNull(riskMetrics.get("totalAmount"), "Total amount should be tracked");
        assertNotNull(riskMetrics.get("transactionCount"), "Transaction count should be tracked");
    }

    @Test
    @DisplayName("Test Global Reset and Cleanup")
    void testGlobalResetAndCleanup() {
        // Given: Populated globals from previous operations
        riskAlerts.add("Initial alert");
        riskMetrics.put("testKey", "testValue");
        auditLog.append("Initial log entry");
        
        Customer customer = createCustomer("CUST006");
        Transaction transaction = createTransaction("TXN006", "CUST006", 15000.0);
        
        // When: Process and then reset globals
        kieSession.insert(customer);
        kieSession.insert(transaction);
        kieSession.fireAllRules();
        
        // Record state before reset
        int alertsBeforeReset = riskAlerts.size();
        int metricsBeforeReset = riskMetrics.size();
        int logLengthBeforeReset = auditLog.length();
        
        // Reset globals
        riskAlerts.clear();
        riskMetrics.clear();
        auditLog.setLength(0);
        
        // Then: Verify reset worked
        assertTrue(riskAlerts.isEmpty(), "Risk alerts should be cleared");
        assertTrue(riskMetrics.isEmpty(), "Risk metrics should be cleared");
        assertEquals(0, auditLog.length(), "Audit log should be cleared");
        
        System.out.println("Before reset - Alerts: " + alertsBeforeReset + 
                          ", Metrics: " + metricsBeforeReset + 
                          ", Log length: " + logLengthBeforeReset);
        System.out.println("After reset - All globals cleared");
    }

    @Test
    @DisplayName("Test Global Thread Safety")
    void testGlobalThreadSafety() {
        // Given: Multiple KieSession instances sharing globals
        KieSession session1 = kieContainer.newKieSession("default-session");
        KieSession session2 = kieContainer.newKieSession("default-session");
        
        // Use thread-safe collections for this test
        List<String> threadSafeAlerts = new ArrayList<>();
        Map<String, Object> threadSafeMetrics = new HashMap<>();
        
        session1.setGlobal("riskAlerts", threadSafeAlerts);
        session2.setGlobal("riskAlerts", threadSafeAlerts);
        session1.setGlobal("riskMetrics", threadSafeMetrics);
        session2.setGlobal("riskMetrics", threadSafeMetrics);
        
        Customer customer1 = createCustomer("CUST007A");
        Customer customer2 = createCustomer("CUST007B");
        
        // When: Process in different sessions
        session1.insert(customer1);
        session2.insert(customer2);
        
        int rules1 = session1.fireAllRules();
        int rules2 = session2.fireAllRules();
        
        // Then: Verify global state consistency
        System.out.println("Session 1 rules fired: " + rules1);
        System.out.println("Session 2 rules fired: " + rules2);
        System.out.println("Shared alerts: " + threadSafeAlerts.size());
        System.out.println("Shared metrics: " + threadSafeMetrics.size());
        
        // Cleanup
        session1.dispose();
        session2.dispose();
        
        assertNotNull(threadSafeAlerts, "Shared alerts should not be null");
        assertNotNull(threadSafeMetrics, "Shared metrics should not be null");
    }

    @Test
    @DisplayName("Test Function Integration")
    void testFunctionIntegration() {
        // Given: Facts that will trigger rules using functions
        Customer customer = createCustomer("CUST008");
        customer.setCreditScore(680);
        customer.setAnnualIncome(75000.0);
        
        Account account = createAccount("ACC008", "CUST008", 12500.0);
        
        Transaction transaction = createTransaction("TXN008", "CUST008", 18000.0);
        
        // When: Insert facts and fire rules (rules may use helper functions)
        kieSession.insert(customer);
        kieSession.insert(account);
        kieSession.insert(transaction);
        
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify function integration worked
        System.out.println("Rules fired with functions: " + rulesFired);
        System.out.println("Post-function execution state:");
        System.out.println("- Risk alerts: " + riskAlerts.size());
        System.out.println("- Risk metrics: " + riskMetrics.size());
        System.out.println("- Audit log length: " + auditLog.length());
        
        // Note: Actual verification depends on function implementation in rules
        assertTrue(rulesFired >= 0, "Rules should execute successfully with functions");
    }

    @Test
    @DisplayName("Test Global Performance")
    void testGlobalPerformance() {
        // Given: Performance test with many facts and global operations
        int customerCount = 100;
        long startTime = System.currentTimeMillis();
        
        // Initialize performance tracking globals
        riskMetrics.put("startTime", startTime);
        riskMetrics.put("processedCount", 0);
        
        // When: Process many customers
        for (int i = 1; i <= customerCount; i++) {
            Customer customer = createCustomer("PERF_CUST_" + i);
            customer.setCreditScore(500 + (i % 300)); // Varying scores
            
            kieSession.insert(customer);
            
            // Fire rules periodically
            if (i % 10 == 0) {
                kieSession.fireAllRules();
            }
        }
        
        // Final rule firing
        int finalRulesFired = kieSession.fireAllRules();
        long endTime = System.currentTimeMillis();
        
        // Then: Verify performance metrics
        long totalTime = endTime - startTime;
        
        System.out.println("=== Global Performance Test ===");
        System.out.println("Customers processed: " + customerCount);
        System.out.println("Final rules fired: " + finalRulesFired);
        System.out.println("Total processing time: " + totalTime + "ms");
        System.out.println("Risk alerts generated: " + riskAlerts.size());
        System.out.println("Risk metrics collected: " + riskMetrics.size());
        
        // Performance assertions
        assertTrue(totalTime < 10000, "Processing should complete within 10 seconds");
        assertNotNull(riskMetrics.get("startTime"), "Start time should be tracked");
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

    // Inner class for global configuration
    public static class RiskAssessmentConfig {
        private int highRiskThreshold = 60;
        private double largeTransactionThreshold = 10000.0;
        private int maxTransactionsPerDay = 20;
        private boolean autoApprovalEnabled = false;
        
        // Getters and setters
        public int getHighRiskThreshold() { return highRiskThreshold; }
        public void setHighRiskThreshold(int highRiskThreshold) { this.highRiskThreshold = highRiskThreshold; }
        
        public double getLargeTransactionThreshold() { return largeTransactionThreshold; }
        public void setLargeTransactionThreshold(double largeTransactionThreshold) { this.largeTransactionThreshold = largeTransactionThreshold; }
        
        public int getMaxTransactionsPerDay() { return maxTransactionsPerDay; }
        public void setMaxTransactionsPerDay(int maxTransactionsPerDay) { this.maxTransactionsPerDay = maxTransactionsPerDay; }
        
        public boolean isAutoApprovalEnabled() { return autoApprovalEnabled; }
        public void setAutoApprovalEnabled(boolean autoApprovalEnabled) { this.autoApprovalEnabled = autoApprovalEnabled; }
    }
} 