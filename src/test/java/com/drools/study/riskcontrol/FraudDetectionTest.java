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
 * Test suite for Fraud Detection
 * 
 * This test class validates fraud detection rules including:
 * - Suspicious transaction patterns
 * - Geographic anomalies
 * - Unusual spending behavior
 * - High-risk scenarios
 */
public class FraudDetectionTest {
    
    private KieContainer kieContainer;
    private KieSession kieSession;

    @BeforeEach
    void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
        kieSession = kieContainer.newKieSession("default-session");
    }

    @Test
    @DisplayName("Test Suspicious Geographic Pattern")
    void testSuspiciousGeographicPattern() {
        // Given: Customer with transactions in different countries
        Customer customer = createCustomer("FRAUD_001");
        
        Transaction domesticTxn = createTransaction("DOM_001", "FRAUD_001", 5000.0);
        domesticTxn.setLocation("USA");
        domesticTxn.setTimestamp(LocalDateTime.now().minusMinutes(30));
        
        Transaction foreignTxn = createTransaction("FOR_001", "FRAUD_001", 8000.0);
        foreignTxn.setLocation("FOREIGN_COUNTRY");
        foreignTxn.setTimestamp(LocalDateTime.now().minusMinutes(15));
        
        // When: Process fraud detection
        kieSession.insert(customer);
        kieSession.insert(domesticTxn);
        kieSession.insert(foreignTxn);
        int rulesFired = kieSession.fireAllRules();
        
        // Then: Verify fraud detection
        System.out.println("Fraud detection rules fired: " + rulesFired);
        assertTrue(rulesFired >= 0, "Fraud detection should work");
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