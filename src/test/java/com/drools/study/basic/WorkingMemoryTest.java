package com.drools.study.basic;

import com.drools.study.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Drools Working Memory Operations
 * 
 * This test class demonstrates and validates working memory management:
 * - Fact insertion, modification, and retraction
 * - FactHandle management
 * - Working memory queries
 * - Memory lifecycle management
 * - Fact relationships and references
 * 
 * All tests use risk control domain scenarios.
 */
public class WorkingMemoryTest {
    
    private KieContainer kieContainer;
    private KieSession kieSession;

    @BeforeEach
    void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
        kieSession = kieContainer.newKieSession("default-session");
    }

    @Test
    @DisplayName("Test Basic Fact Insertion")
    void testBasicFactInsertion() {
        // Given: New customer
        Customer customer = createCustomer("CUST001");
        
        // When: Insert fact into working memory
        FactHandle customerHandle = kieSession.insert(customer);
        
        // Then: Verify fact was inserted
        assertNotNull(customerHandle, "FactHandle should not be null");
        assertEquals(1, kieSession.getFactCount(), "Working memory should contain 1 fact");
        
        // Verify fact can be retrieved
        Object retrievedFact = kieSession.getObject(customerHandle);
        assertNotNull(retrievedFact, "Retrieved fact should not be null");
        assertEquals(customer, retrievedFact, "Retrieved fact should be the same as inserted");
        
        System.out.println("Inserted customer: " + customer.getCustomerId());
        System.out.println("Working memory size: " + kieSession.getFactCount());
    }

    @Test
    @DisplayName("Test Multiple Fact Insertion")
    void testMultipleFactInsertion() {
        // Given: Multiple related facts
        Customer customer = createCustomer("CUST002");
        Account account = createAccount("ACC002", "CUST002", 5000.0);
        Transaction transaction = createTransaction("TXN002", "CUST002", 2500.0);
        RiskProfile riskProfile = createRiskProfile("CUST002");
        
        // When: Insert multiple facts
        FactHandle customerHandle = kieSession.insert(customer);
        FactHandle accountHandle = kieSession.insert(account);
        FactHandle transactionHandle = kieSession.insert(transaction);
        FactHandle riskHandle = kieSession.insert(riskProfile);
        
        // Then: Verify all facts were inserted
        assertEquals(4, kieSession.getFactCount(), "Working memory should contain 4 facts");
        
        // Verify all fact handles are valid
        assertNotNull(customerHandle, "Customer FactHandle should not be null");
        assertNotNull(accountHandle, "Account FactHandle should not be null");
        assertNotNull(transactionHandle, "Transaction FactHandle should not be null");
        assertNotNull(riskHandle, "RiskProfile FactHandle should not be null");
        
        // Verify all facts can be retrieved
        assertEquals(customer, kieSession.getObject(customerHandle));
        assertEquals(account, kieSession.getObject(accountHandle));
        assertEquals(transaction, kieSession.getObject(transactionHandle));
        assertEquals(riskProfile, kieSession.getObject(riskHandle));
        
        System.out.println("Total facts in working memory: " + kieSession.getFactCount());
    }

    @Test
    @DisplayName("Test Fact Modification")
    void testFactModification() {
        // Given: Customer in working memory
        Customer customer = createCustomer("CUST003");
        FactHandle customerHandle = kieSession.insert(customer);
        
        // Record initial state
        int initialCreditScore = customer.getCreditScore();
        
        // When: Modify fact
        customer.setCreditScore(750); // Improve credit score
        customer.setAnnualIncome(85000.0); // Increase income
        kieSession.update(customerHandle, customer);
        
        // Then: Verify modification
        Customer retrievedCustomer = (Customer) kieSession.getObject(customerHandle);
        assertEquals(750, retrievedCustomer.getCreditScore(), "Credit score should be updated");
        assertEquals(85000.0, retrievedCustomer.getAnnualIncome(), "Income should be updated");
        assertNotEquals(initialCreditScore, retrievedCustomer.getCreditScore(), "Credit score should be changed");
        
        // Working memory size should remain the same
        assertEquals(1, kieSession.getFactCount(), "Working memory size should not change after update");
        
        System.out.println("Updated credit score from " + initialCreditScore + " to " + retrievedCustomer.getCreditScore());
    }

    @Test
    @DisplayName("Test Fact Retraction")
    void testFactRetraction() {
        // Given: Multiple facts in working memory
        Customer customer = createCustomer("CUST004");
        Account account = createAccount("ACC004", "CUST004", 3000.0);
        Transaction transaction = createTransaction("TXN004", "CUST004", 1500.0);
        
        FactHandle customerHandle = kieSession.insert(customer);
        FactHandle accountHandle = kieSession.insert(account);
        FactHandle transactionHandle = kieSession.insert(transaction);
        
        assertEquals(3, kieSession.getFactCount(), "Should have 3 facts initially");
        
        // When: Retract one fact
        kieSession.delete(transactionHandle);
        
        // Then: Verify retraction
        assertEquals(2, kieSession.getFactCount(), "Should have 2 facts after retraction");
        
        // Verify retracted fact is no longer accessible
        Object retrievedTransaction = kieSession.getObject(transactionHandle);
        assertNull(retrievedTransaction, "Retracted fact should not be retrievable");
        
        // Verify other facts are still accessible
        assertNotNull(kieSession.getObject(customerHandle), "Customer should still be accessible");
        assertNotNull(kieSession.getObject(accountHandle), "Account should still be accessible");
        
        System.out.println("Facts after retraction: " + kieSession.getFactCount());
    }

    @Test
    @DisplayName("Test Working Memory Queries")
    void testWorkingMemoryQueries() {
        // Given: Various facts in working memory
        Customer customer1 = createCustomer("CUST005A");
        customer1.setCreditScore(700);
        Customer customer2 = createCustomer("CUST005B");
        customer2.setCreditScore(600);
        Customer customer3 = createCustomer("CUST005C");
        customer3.setCreditScore(800);
        
        Account account1 = createAccount("ACC005A", "CUST005A", 10000.0);
        Account account2 = createAccount("ACC005B", "CUST005B", 500.0);
        
        // Insert all facts
        kieSession.insert(customer1);
        kieSession.insert(customer2);
        kieSession.insert(customer3);
        kieSession.insert(account1);
        kieSession.insert(account2);
        
        // When: Query working memory
        Collection<Object> allFacts = kieSession.getObjects();
        
        // Then: Verify query results
        assertEquals(5, allFacts.size(), "Should retrieve all 5 facts");
        
        // Filter by type
        Collection<Customer> customers = kieSession.getObjects(object -> object instanceof Customer);
        Collection<Account> accounts = kieSession.getObjects(object -> object instanceof Account);
        
        assertEquals(3, customers.size(), "Should have 3 customers");
        assertEquals(2, accounts.size(), "Should have 2 accounts");
        
        // Filter by custom criteria
        Collection<Customer> highCreditCustomers = kieSession.getObjects(
            object -> object instanceof Customer && ((Customer) object).getCreditScore() >= 700
        );
        
        assertEquals(2, highCreditCustomers.size(), "Should have 2 high-credit customers");
        
        System.out.println("Total facts: " + allFacts.size());
        System.out.println("Customers: " + customers.size());
        System.out.println("Accounts: " + accounts.size());
        System.out.println("High credit customers: " + highCreditCustomers.size());
    }

    @Test
    @DisplayName("Test FactHandle Management")
    void testFactHandleManagement() {
        // Given: Facts with tracked handles
        Customer customer = createCustomer("CUST006");
        Account account = createAccount("ACC006", "CUST006", 7500.0);
        
        FactHandle customerHandle = kieSession.insert(customer);
        FactHandle accountHandle = kieSession.insert(account);
        
        // When: Verify handle properties
        assertTrue(customerHandle != null, "Customer handle should be valid");
        assertTrue(accountHandle != null, "Account handle should be valid");
        assertNotEquals(customerHandle, accountHandle, "Handles should be different");
        
        // Test handle identity
        FactHandle sameCustomerHandle = kieSession.getFactHandle(customer);
        assertEquals(customerHandle, sameCustomerHandle, "Same object should have same handle");
        
        // When: Delete using handle
        kieSession.delete(customerHandle);
        
        // Then: Verify handle is invalid after deletion
        assertNull(kieSession.getObject(customerHandle), "Handle should be invalid after deletion");
        assertEquals(1, kieSession.getFactCount(), "Should have 1 fact remaining");
        
        System.out.println("FactHandle management test completed");
    }

    @Test
    @DisplayName("Test Bulk Operations")
    void testBulkOperations() {
        // Given: Multiple customers for bulk operations
        List<Customer> customers = new ArrayList<>();
        List<FactHandle> handles = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Customer customer = createCustomer("BULK_CUST_" + i);
            customer.setCreditScore(600 + (i * 10)); // Varying credit scores
            customers.add(customer);
        }
        
        // When: Bulk insert
        for (Customer customer : customers) {
            FactHandle handle = kieSession.insert(customer);
            handles.add(handle);
        }
        
        assertEquals(10, kieSession.getFactCount(), "Should have 10 customers");
        
        // Bulk modification
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            customer.setAnnualIncome(50000.0 + (i * 5000)); // Set income
            kieSession.update(handles.get(i), customer);
        }
        
        // Verify modifications
        Collection<Customer> allCustomers = kieSession.getObjects(obj -> obj instanceof Customer);
        assertTrue(allCustomers.stream().allMatch(c -> c.getAnnualIncome() >= 50000), 
                  "All customers should have income set");
        
        // Bulk deletion of high-income customers
        List<FactHandle> toDelete = new ArrayList<>();
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getAnnualIncome() > 70000) {
                toDelete.add(handles.get(i));
            }
        }
        
        for (FactHandle handle : toDelete) {
            kieSession.delete(handle);
        }
        
        assertTrue(kieSession.getFactCount() < 10, "Some customers should be deleted");
        
        System.out.println("Bulk operations completed - remaining facts: " + kieSession.getFactCount());
    }

    @Test
    @DisplayName("Test Working Memory Lifecycle")
    void testWorkingMemoryLifecycle() {
        // Given: Initial empty working memory
        assertEquals(0, kieSession.getFactCount(), "Working memory should be empty initially");
        
        // Phase 1: Load data
        Customer customer = createCustomer("CUST007");
        Account account = createAccount("ACC007", "CUST007", 15000.0);
        Transaction txn1 = createTransaction("TXN007A", "CUST007", 5000.0);
        Transaction txn2 = createTransaction("TXN007B", "CUST007", 3000.0);
        
        kieSession.insert(customer);
        kieSession.insert(account);
        kieSession.insert(txn1);
        kieSession.insert(txn2);
        
        assertEquals(4, kieSession.getFactCount(), "Phase 1: Should have 4 facts");
        
        // Phase 2: Process transactions (fire rules)
        int rulesFired = kieSession.fireAllRules();
        System.out.println("Rules fired: " + rulesFired);
        
        // Phase 3: Add derived facts (alerts, assessments)
        RiskAlert alert = new RiskAlert();
        alert.setCustomerId("CUST007");
        alert.setAlertType("PROCESSING_COMPLETE");
        alert.setSeverity("INFO");
        alert.setCreatedAt(LocalDateTime.now());
        
        kieSession.insert(alert);
        assertEquals(5, kieSession.getFactCount(), "Phase 3: Should have 5 facts");
        
        // Phase 4: Cleanup old transactions
        kieSession.delete(kieSession.getFactHandle(txn1));
        kieSession.delete(kieSession.getFactHandle(txn2));
        
        assertEquals(3, kieSession.getFactCount(), "Phase 4: Should have 3 facts after cleanup");
        
        // Final verification
        Collection<Object> remainingFacts = kieSession.getObjects();
        assertTrue(remainingFacts.contains(customer), "Customer should remain");
        assertTrue(remainingFacts.contains(account), "Account should remain");
        assertTrue(remainingFacts.contains(alert), "Alert should remain");
        
        System.out.println("Working memory lifecycle test completed");
    }

    @Test
    @DisplayName("Test Complex Fact Relationships")
    void testComplexFactRelationships() {
        // Given: Complex related fact structure
        Customer customer = createCustomer("CUST008");
        
        // Multiple accounts for same customer
        Account checking = createAccount("CHECK008", "CUST008", 5000.0);
        checking.setAccountType("CHECKING");
        
        Account savings = createAccount("SAVE008", "CUST008", 15000.0);
        savings.setAccountType("SAVINGS");
        
        // Transactions across accounts
        Transaction txnCheck1 = createTransaction("TXN008A", "CUST008", 2000.0);
        txnCheck1.setFromAccount("CHECK008");
        
        Transaction txnSave1 = createTransaction("TXN008B", "CUST008", 8000.0);
        txnSave1.setFromAccount("SAVE008");
        
        // Risk profile and assessment
        RiskProfile riskProfile = createRiskProfile("CUST008");
        
        // When: Insert all related facts
        kieSession.insert(customer);
        kieSession.insert(checking);
        kieSession.insert(savings);
        kieSession.insert(txnCheck1);
        kieSession.insert(txnSave1);
        kieSession.insert(riskProfile);
        
        // Then: Verify relationships can be queried
        assertEquals(6, kieSession.getFactCount(), "Should have all related facts");
        
        // Query by customer ID
        Collection<Object> customerFacts = kieSession.getObjects(obj -> {
            if (obj instanceof Customer) return ((Customer) obj).getCustomerId().equals("CUST008");
            if (obj instanceof Account) return ((Account) obj).getCustomerId().equals("CUST008");
            if (obj instanceof Transaction) return ((Transaction) obj).getCustomerId().equals("CUST008");
            if (obj instanceof RiskProfile) return ((RiskProfile) obj).getCustomerId().equals("CUST008");
            return false;
        });
        
        assertEquals(6, customerFacts.size(), "All facts should be related to CUST008");
        
        // Query accounts for customer
        Collection<Account> customerAccounts = kieSession.getObjects(obj -> 
            obj instanceof Account && ((Account) obj).getCustomerId().equals("CUST008"));
        
        assertEquals(2, customerAccounts.size(), "Customer should have 2 accounts");
        
        System.out.println("Complex relationships verified - total related facts: " + customerFacts.size());
    }

    @Test
    @DisplayName("Test Working Memory Performance")
    void testWorkingMemoryPerformance() {
        // Given: Performance test parameters
        int factCount = 1000;
        long startTime, endTime;
        
        // Test insertion performance
        startTime = System.currentTimeMillis();
        List<FactHandle> handles = new ArrayList<>();
        
        for (int i = 0; i < factCount; i++) {
            Customer customer = createCustomer("PERF_CUST_" + i);
            FactHandle handle = kieSession.insert(customer);
            handles.add(handle);
        }
        
        endTime = System.currentTimeMillis();
        long insertTime = endTime - startTime;
        
        assertEquals(factCount, kieSession.getFactCount(), "Should have inserted all facts");
        
        // Test query performance
        startTime = System.currentTimeMillis();
        Collection<Object> allFacts = kieSession.getObjects();
        endTime = System.currentTimeMillis();
        long queryTime = endTime - startTime;
        
        assertEquals(factCount, allFacts.size(), "Query should return all facts");
        
        // Test deletion performance
        startTime = System.currentTimeMillis();
        for (FactHandle handle : handles) {
            kieSession.delete(handle);
        }
        endTime = System.currentTimeMillis();
        long deleteTime = endTime - startTime;
        
        assertEquals(0, kieSession.getFactCount(), "All facts should be deleted");
        
        // Report performance metrics
        System.out.println("=== Working Memory Performance ===");
        System.out.println("Facts processed: " + factCount);
        System.out.println("Insert time: " + insertTime + "ms");
        System.out.println("Query time: " + queryTime + "ms");
        System.out.println("Delete time: " + deleteTime + "ms");
        System.out.println("Insert rate: " + (factCount * 1000.0 / insertTime) + " facts/sec");
        
        // Performance assertions
        assertTrue(insertTime < 5000, "Insert should complete within 5 seconds");
        assertTrue(queryTime < 1000, "Query should complete within 1 second");
        assertTrue(deleteTime < 5000, "Delete should complete within 5 seconds");
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