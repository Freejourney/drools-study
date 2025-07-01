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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Concurrency and Thread Safety
 * 
 * This test class validates:
 * - Multi-threaded rule execution
 * - Thread safety of KieSession
 * - Concurrent data processing
 * - Performance under concurrent load
 */
public class ConcurrencyTest {
    
    private KieContainer kieContainer;

    @BeforeEach
    void setUp() {
        KieServices kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
    }

    @Test
    @DisplayName("Test Concurrent Rule Execution")
    void testConcurrentRuleExecution() throws InterruptedException {
        // Given: Multiple threads for concurrent execution
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger completedTasks = new AtomicInteger(0);
        
        // When: Execute rules concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    KieSession kieSession = kieContainer.newKieSession("default-session");
                    
                    Customer customer = createCustomer("CONC_" + threadId);
                    kieSession.insert(customer);
                    
                    int rulesFired = kieSession.fireAllRules();
                    System.out.println("Thread " + threadId + " fired " + rulesFired + " rules");
                    
                    kieSession.dispose();
                    completedTasks.incrementAndGet();
                    
                } catch (Exception e) {
                    System.err.println("Thread " + threadId + " failed: " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
        
        // Then: Verify concurrent execution
        assertTrue(finished, "All threads should complete within timeout");
        assertEquals(threadCount, completedTasks.get(), "All tasks should complete successfully");
        System.out.println("Concurrent execution completed successfully");
    }

    @Test
    @DisplayName("Test Thread Safety")
    void testThreadSafety() throws InterruptedException {
        // Given: Shared session scenario (note: KieSession is not thread-safe by design)
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // When: Each thread uses its own session (recommended approach)
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    // Each thread gets its own session - this is thread-safe
                    KieSession kieSession = kieContainer.newKieSession("default-session");
                    
                    for (int j = 0; j < 10; j++) {
                        Customer customer = createCustomer("SAFE_" + threadId + "_" + j);
                        kieSession.insert(customer);
                    }
                    
                    int rulesFired = kieSession.fireAllRules();
                    System.out.println("Thread " + threadId + " safely processed " + rulesFired + " rules");
                    
                    kieSession.dispose();
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    System.err.println("Thread safety test failed in thread " + threadId + ": " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
        
        // Then: Verify thread safety
        assertTrue(finished, "All threads should complete within timeout");
        assertEquals(threadCount, successCount.get(), "All threads should complete successfully");
        System.out.println("Thread safety test completed successfully");
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