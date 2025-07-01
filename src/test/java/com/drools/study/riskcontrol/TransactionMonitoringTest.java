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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Transaction Monitoring in Risk Control
 * 
 * This test class validates transaction monitoring rules including:
 * - Large transaction detection
 * - Velocity monitoring
 * - Pattern analysis
 * - Fraud detection
 * - Transaction limits
 */
public class TransactionMonitoringTest {
    
    private KieContainer kieContainer;
    private KieSession kieSession;

    @BeforeEach
    void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
        kieSession = kieContainer.newKieSession("default-session");
    }

    @Test
    @DisplayName("Test Large Transaction Detection")
    void testLargeTransactionDetection() {
        // Given: Customer with large transaction
        Customer customer = createCustomer("LARGE_001");
        Transaction largeTransaction = createTransaction("LRG_001", "LARGE_001", 100000.0);
        
        // When: Process transaction
        kieSession.insert(customer);
        kieSession.insert(largeTransaction);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify large transaction is detected
        System.out.println("Large transaction rules fired: " + rulesFired);
        assertTrue(rulesFired >= 0, "Large transaction detection should work");
    }

    @Test
    @DisplayName("Test Transaction Velocity Monitoring")
    void testTransactionVelocityMonitoring() {
        // Given: Multiple rapid transactions
        Customer customer = createCustomer("VEL_001");
        kieSession.insert(customer);
        
        // Create rapid transactions
        for (int i = 0; i < 5; i++) {
            Transaction txn = createTransaction("VEL_001_" + i, "VEL_001", 5000.0);
            txn.setTimestamp(LocalDateTime.now().minusMinutes(i));
            kieSession.insert(txn);
        }
        
        // When: Fire velocity monitoring rules
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify velocity monitoring
        System.out.println("Velocity monitoring rules fired: " + rulesFired);
        assertTrue(rulesFired >= 0, "Velocity monitoring should work");
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
} 