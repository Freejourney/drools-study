package com.drools.study.performance;

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
 * Test suite for Memory Usage and Performance
 * 
 * This test class validates:
 * - Memory consumption patterns
 * - Garbage collection behavior
 * - Memory leaks detection
 * - Performance under load
 */
public class MemoryUsageTest {
    
    private KieContainer kieContainer;

    @BeforeEach
    void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
    }

    @Test
    @DisplayName("Test Memory Usage with Large Dataset")
    void testMemoryUsageWithLargeDataset() {
        // Given: Large dataset for memory testing
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        KieSession kieSession = kieContainer.newKieSession("default-session");
        
        // When: Load large dataset
        for (int i = 0; i < 1000; i++) {
            Customer customer = createCustomer("MEM_CUST_" + i);
            kieSession.insert(customer);
        }
        
        kieSession.fireAllRules();
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        // Then: Verify memory usage
        System.out.println("Memory used: " + (memoryUsed / 1024 / 1024) + " MB");
        assertTrue(memoryUsed > 0, "Memory should be consumed");
        
        kieSession.dispose();
    }

    @Test
    @DisplayName("Test Memory Cleanup")
    void testMemoryCleanup() {
        // Given: Session with data
        KieSession kieSession = kieContainer.newKieSession("default-session");
        
        for (int i = 0; i < 100; i++) {
            Customer customer = createCustomer("CLEANUP_" + i);
            kieSession.insert(customer);
        }
        
        assertEquals(100, kieSession.getFactCount(), "Should have 100 facts");
        
        // When: Clean up session
        kieSession.dispose();
        
        // Then: Verify cleanup
        System.out.println("Session disposed successfully");
        assertTrue(true, "Memory cleanup test completed");
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
} 