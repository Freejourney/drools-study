package com.drools.study.basic;

import com.drools.study.config.DroolsConfig;
import com.drools.study.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Drools fact insertion, modification, and retraction operations
 * Demonstrates working memory management and fact lifecycle
 */
@SpringBootTest
@ActiveProfiles("test")
public class FactInsertionTest {

    @Autowired
    private DroolsConfig droolsConfig;
    
    private KieSession kieSession;

    @BeforeEach
    void setUp() {
        kieSession = droolsConfig.kieSession();
    }

    @Test
    @DisplayName("Test basic fact insertion")
    void testBasicFactInsertion() {
        // Create and insert a customer
        Customer customer = createSampleCustomer();
        FactHandle customerHandle = kieSession.insert(customer);
        
        assertNotNull(customerHandle);
        assertEquals(1, kieSession.getFactCount());
        
        // Verify the fact is in working memory
        Collection<? extends Object> objects = kieSession.getObjects();
        assertEquals(1, objects.size());
        assertTrue(objects.contains(customer));
        
        kieSession.dispose();
    }

    @Test
    @DisplayName("Test fact modification")
    void testFactModification() {
        Customer customer = createSampleCustomer();
        FactHandle customerHandle = kieSession.insert(customer);
        
        // Modify the customer
        customer.setRiskRating("HIGH");
        kieSession.update(customerHandle, customer);
        
        // Verify modification
        assertEquals("HIGH", customer.getRiskRating());
        assertEquals(1, kieSession.getFactCount());
        
        kieSession.dispose();
    }

    @Test
    @DisplayName("Test fact retraction")
    void testFactRetraction() {
        Customer customer = createSampleCustomer();
        FactHandle customerHandle = kieSession.insert(customer);
        assertEquals(1, kieSession.getFactCount());
        
        // Retract the fact
        kieSession.delete(customerHandle);
        assertEquals(0, kieSession.getFactCount());
        
        Collection<? extends Object> objects = kieSession.getObjects();
        assertEquals(0, objects.size());
        
        kieSession.dispose();
    }

    @Test
    @DisplayName("Test multiple fact types insertion")
    void testMultipleFactTypes() {
        Customer customer = createSampleCustomer();
        Account account = createSampleAccount();
        Transaction transaction = createSampleTransaction();
        
        kieSession.insert(customer);
        kieSession.insert(account);
        kieSession.insert(transaction);
        
        assertEquals(3, kieSession.getFactCount());
        
        // Check specific fact types
        Collection<Customer> customers = kieSession.getObjects(Customer.class);
        Collection<Account> accounts = kieSession.getObjects(Account.class);
        Collection<Transaction> transactions = kieSession.getObjects(Transaction.class);
        
        assertEquals(1, customers.size());
        assertEquals(1, accounts.size());
        assertEquals(1, transactions.size());
        
        kieSession.dispose();
    }

    @Test
    @DisplayName("Test fact insertion with rule firing")
    void testFactInsertionWithRuleFiring() {
        Customer customer = createSampleCustomer();
        customer.setBlacklisted(true);
        
        kieSession.insert(customer);
        
        int rulesFired = kieSession.fireAllRules();
        assertTrue(rulesFired > 0, "At least one rule should have fired");
        
        // Check if risk alerts were generated
        Collection<RiskAlert> alerts = kieSession.getObjects(RiskAlert.class);
        assertFalse(alerts.isEmpty(), "Risk alerts should be generated for blacklisted customer");
        
        kieSession.dispose();
    }

    @Test
    @DisplayName("Test fact handle operations")
    void testFactHandleOperations() {
        Customer customer = createSampleCustomer();
        FactHandle handle = kieSession.insert(customer);
        
        // Test getObject
        Object retrievedCustomer = kieSession.getObject(handle);
        assertSame(customer, retrievedCustomer);
        
        // Test getFactHandle
        FactHandle retrievedHandle = kieSession.getFactHandle(customer);
        assertEquals(handle, retrievedHandle);
        
        kieSession.dispose();
    }

    @Test
    @DisplayName("Test batch fact insertion")
    void testBatchFactInsertion() {
        Customer customer1 = createSampleCustomer();
        customer1.setCustomerId("CUST001");
        
        Customer customer2 = createSampleCustomer();
        customer2.setCustomerId("CUST002");
        customer2.setVip(true);
        
        Customer customer3 = createSampleCustomer();
        customer3.setCustomerId("CUST003");
        customer3.setBlacklisted(true);
        
        kieSession.insert(customer1);
        kieSession.insert(customer2);
        kieSession.insert(customer3);
        
        assertEquals(3, kieSession.getFactCount());
        
        int rulesFired = kieSession.fireAllRules();
        assertTrue(rulesFired > 0);
        
        // Check that alerts were generated for blacklisted customer
        Collection<RiskAlert> alerts = kieSession.getObjects(RiskAlert.class);
        boolean hasBlacklistAlert = alerts.stream()
                .anyMatch(alert -> alert.getAlertType().contains("BLACKLISTED"));
        assertTrue(hasBlacklistAlert);
        
        kieSession.dispose();
    }

    @Test
    @DisplayName("Test fact modification with rule re-evaluation")
    void testFactModificationWithRules() {
        Customer customer = createSampleCustomer();
        customer.setBlacklisted(false);
        
        FactHandle handle = kieSession.insert(customer);
        int initialRulesFired = kieSession.fireAllRules();
        
        // Modify customer to trigger rules
        customer.setBlacklisted(true);
        kieSession.update(handle, customer);
        
        int additionalRulesFired = kieSession.fireAllRules();
        assertTrue(additionalRulesFired > 0, "Rules should fire after modification");
        
        Collection<RiskAlert> alerts = kieSession.getObjects(RiskAlert.class);
        assertFalse(alerts.isEmpty(), "Alerts should be generated after blacklisting");
        
        kieSession.dispose();
    }

    @Test
    @DisplayName("Test working memory query")
    void testWorkingMemoryQuery() {
        // Insert multiple customers with different risk ratings
        Customer lowRisk = createSampleCustomer();
        lowRisk.setCustomerId("LOW001");
        lowRisk.setRiskRating("LOW");
        
        Customer mediumRisk = createSampleCustomer();
        mediumRisk.setCustomerId("MED001");
        mediumRisk.setRiskRating("MEDIUM");
        
        Customer highRisk = createSampleCustomer();
        highRisk.setCustomerId("HIGH001");
        highRisk.setRiskRating("HIGH");
        
        kieSession.insert(lowRisk);
        kieSession.insert(mediumRisk);
        kieSession.insert(highRisk);
        
        // Query for high risk customers
        Collection<Customer> allCustomers = kieSession.getObjects(Customer.class);
        assertEquals(3, allCustomers.size());
        
        long highRiskCount = allCustomers.stream()
                .filter(c -> "HIGH".equals(c.getRiskRating()))
                .count();
        assertEquals(1, highRiskCount);
        
        kieSession.dispose();
    }

    // Helper methods to create sample objects
    private Customer createSampleCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId("CUST001");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setDateOfBirth(LocalDateTime.now().minusYears(30));
        customer.setCustomerType("INDIVIDUAL");
        customer.setKycStatus("VERIFIED");
        customer.setRiskRating("MEDIUM");
        customer.setBlacklisted(false);
        customer.setVip(false);
        customer.setRegistrationDate(LocalDateTime.now().minusYears(2));
        return customer;
    }

    private Account createSampleAccount() {
        Account account = new Account();
        account.setAccountId("ACC001");
        account.setCustomerId("CUST001");
        account.setAccountType("CHECKING");
        account.setBalance(new BigDecimal("10000.00"));
        account.setCurrency("USD");
        account.setStatus("ACTIVE");
        account.setOpenDate(LocalDateTime.now().minusYears(1));
        return account;
    }

    private Transaction createSampleTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN001");
        transaction.setCustomerId("CUST001");
        transaction.setFromAccount("ACC001");
        transaction.setAmount(1000.0);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setStatus("COMPLETED");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setCurrency("USD");
        transaction.setChannel("ATM");
        return transaction;
    }
} 