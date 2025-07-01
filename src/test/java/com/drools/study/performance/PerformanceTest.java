package com.drools.study.performance;

import com.drools.study.service.DroolsService;
import com.drools.study.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for Drools rule execution
 * Measures execution time, memory usage, and scalability
 */
@SpringBootTest
@ActiveProfiles("test")
public class PerformanceTest {

    @Autowired
    private DroolsService droolsService;

    private List<Customer> testCustomers;
    private List<Transaction> testTransactions;
    private List<Account> testAccounts;

    @BeforeEach
    void setUp() {
        // Generate test data for performance testing
        testCustomers = generateTestCustomers(1000);
        testTransactions = generateTestTransactions(5000);
        testAccounts = generateTestAccounts(1000);
    }

    @Test
    @DisplayName("Test single rule execution performance")
    void testSingleRulePerformance() {
        Customer customer = testCustomers.get(0);
        customer.setBlacklisted(true);

        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 1000; i++) {
            List<Object> results = droolsService.executeRules("risk-control-basic", 
                    List.of(customer));
            assertNotNull(results);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        System.out.println("Single rule execution 1000 times took: " + totalTime + "ms");
        System.out.println("Average per execution: " + (totalTime / 1000.0) + "ms");
        
        // Assert reasonable performance (adjust threshold as needed)
        assertTrue(totalTime < 10000, "1000 executions should complete within 10 seconds");
    }

    @Test
    @DisplayName("Test bulk data processing performance")
    void testBulkDataProcessing() {
        List<Object> bulkData = new ArrayList<>();
        bulkData.addAll(testCustomers.subList(0, 100));
        bulkData.addAll(testTransactions.subList(0, 500));
        bulkData.addAll(testAccounts.subList(0, 100));

        long startTime = System.currentTimeMillis();
        
        List<Object> results = droolsService.executeRules("risk-control-basic", bulkData);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("Bulk processing (700 facts) took: " + executionTime + "ms");
        System.out.println("Results generated: " + results.size());
        System.out.println("Processing rate: " + (bulkData.size() / (executionTime / 1000.0)) + " facts/second");
        
        assertNotNull(results);
        assertTrue(executionTime < 30000, "Bulk processing should complete within 30 seconds");
    }

    @Test
    @DisplayName("Test memory usage with large datasets")
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        
        // Force garbage collection and measure initial memory
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Process large dataset
        List<Object> largeDataset = new ArrayList<>();
        largeDataset.addAll(testCustomers);
        largeDataset.addAll(testTransactions);
        largeDataset.addAll(testAccounts);
        
        List<Object> results = droolsService.executeRules("risk-control-basic", largeDataset);
        
        // Measure memory after processing
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        System.out.println("Initial memory: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("Final memory: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("Memory used: " + (memoryUsed / 1024 / 1024) + " MB");
        System.out.println("Facts processed: " + largeDataset.size());
        System.out.println("Results generated: " + results.size());
        
        assertNotNull(results);
        assertTrue(memoryUsed < 500 * 1024 * 1024, "Memory usage should be less than 500MB");
    }

    @Test
    @DisplayName("Test concurrent execution performance")
    void testConcurrentPerformance() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // Submit 50 concurrent tasks
        for (int i = 0; i < 50; i++) {
            final int taskId = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                Customer customer = testCustomers.get(taskId % testCustomers.size());
                Transaction transaction = testTransactions.get(taskId % testTransactions.size());
                
                long taskStart = System.currentTimeMillis();
                List<Object> results = droolsService.executeRules("risk-control-basic", 
                        List.of(customer, transaction));
                long taskEnd = System.currentTimeMillis();
                
                assertNotNull(results);
                return taskEnd - taskStart;
            }, executor);
            
            futures.add(future);
        }
        
        // Wait for all tasks to complete and collect execution times
        List<Long> executionTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            executionTimes.add(future.get());
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        double avgExecutionTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxExecutionTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        long minExecutionTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0L);
        
        System.out.println("Concurrent execution results:");
        System.out.println("Total time for 50 concurrent tasks: " + totalTime + "ms");
        System.out.println("Average task execution time: " + avgExecutionTime + "ms");
        System.out.println("Min task execution time: " + minExecutionTime + "ms");
        System.out.println("Max task execution time: " + maxExecutionTime + "ms");
        
        executor.shutdown();
        
        assertTrue(totalTime < 60000, "50 concurrent tasks should complete within 60 seconds");
        assertTrue(avgExecutionTime < 1000, "Average task time should be less than 1 second");
    }

    @Test
    @DisplayName("Test rule complexity performance impact")
    void testRuleComplexityPerformance() {
        List<Object> testData = List.of(
                testCustomers.get(0),
                testTransactions.get(0),
                testAccounts.get(0)
        );
        
        // Test simple rules
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            droolsService.executeRules("risk-control-basic", testData);
        }
        long simpleRulesTime = System.currentTimeMillis() - startTime;
        
        // Test complex rules
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            droolsService.executeRules("complex-conditions", testData);
        }
        long complexRulesTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Simple rules (100 executions): " + simpleRulesTime + "ms");
        System.out.println("Complex rules (100 executions): " + complexRulesTime + "ms");
        System.out.println("Complexity overhead: " + (complexRulesTime - simpleRulesTime) + "ms");
        
        assertTrue(simpleRulesTime > 0, "Simple rules should take some time");
        assertTrue(complexRulesTime > 0, "Complex rules should take some time");
    }

    @Test
    @DisplayName("Test scalability with increasing data size")
    void testScalability() {
        int[] dataSizes = {10, 50, 100, 500, 1000};
        
        System.out.println("Scalability test results:");
        System.out.println("Data Size | Execution Time (ms) | Facts/Second");
        System.out.println("----------|--------------------|--------------");
        
        for (int size : dataSizes) {
            List<Object> data = new ArrayList<>();
            data.addAll(testCustomers.subList(0, Math.min(size, testCustomers.size())));
            data.addAll(testTransactions.subList(0, Math.min(size * 2, testTransactions.size())));
            
            long startTime = System.currentTimeMillis();
            List<Object> results = droolsService.executeRules("risk-control-basic", data);
            long executionTime = System.currentTimeMillis() - startTime;
            
            double factsPerSecond = data.size() / (executionTime / 1000.0);
            
            System.out.printf("%9d | %18d | %12.2f%n", 
                    data.size(), executionTime, factsPerSecond);
            
            assertNotNull(results);
            assertTrue(executionTime < 60000, "Execution should complete within 60 seconds");
        }
    }

    // Helper methods to generate test data
    private List<Customer> generateTestCustomers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Customer customer = new Customer();
                    customer.setCustomerId("PERF_CUST_" + String.format("%06d", i));
                    customer.setFirstName("Customer" + i);
                    customer.setLastName("Test");
                    customer.setEmail("customer" + i + "@test.com");
                    customer.setDateOfBirth(LocalDateTime.now().minusYears(25 + (i % 40)));
                    customer.setCustomerType(i % 10 == 0 ? "BUSINESS" : "INDIVIDUAL");
                    customer.setKycStatus("VERIFIED");
                    customer.setRiskRating(i % 3 == 0 ? "HIGH" : i % 3 == 1 ? "MEDIUM" : "LOW");
                    customer.setBlacklisted(i % 100 == 0); // 1% blacklisted
                    customer.setVip(i % 50 == 0); // 2% VIP
                    customer.setRegistrationDate(LocalDateTime.now().minusDays(i % 1000));
                    return customer;
                })
                .toList();
    }

    private List<Transaction> generateTestTransactions(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Transaction transaction = new Transaction();
                    transaction.setTransactionId("PERF_TXN_" + String.format("%08d", i));
                    transaction.setCustomerId("PERF_CUST_" + String.format("%06d", i % 1000));
                    transaction.setFromAccount("PERF_ACC_" + String.format("%06d", i % 1000));
                    transaction.setAmount((i % 10 + 1) * 1000.0); // $1000 to $10000
                    transaction.setTransactionType(i % 4 == 0 ? "WITHDRAWAL" : 
                                                 i % 4 == 1 ? "DEPOSIT" :
                                                 i % 4 == 2 ? "TRANSFER" : "PAYMENT");
                    transaction.setStatus("COMPLETED");
                    transaction.setTimestamp(LocalDateTime.now().minusMinutes(i % 10080)); // Last week
                    transaction.setCurrency("USD");
                    transaction.setChannel(i % 3 == 0 ? "ATM" : i % 3 == 1 ? "ONLINE" : "BRANCH");
                    transaction.setLocation("City" + (i % 10));
                    return transaction;
                })
                .toList();
    }

    private List<Account> generateTestAccounts(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Account account = new Account();
                    account.setAccountId("PERF_ACC_" + String.format("%06d", i));
                    account.setCustomerId("PERF_CUST_" + String.format("%06d", i));
                    account.setAccountType(i % 2 == 0 ? "CHECKING" : "SAVINGS");
                    account.setBalance(new BigDecimal((i % 100 + 1) * 1000)); // $1000 to $100000
                    account.setCurrency("USD");
                    account.setStatus("ACTIVE");
                    account.setOpenDate(LocalDateTime.now().minusDays(i % 365));
                    account.setDailyLimit(new BigDecimal("5000"));
                    return account;
                })
                .toList();
    }
} 