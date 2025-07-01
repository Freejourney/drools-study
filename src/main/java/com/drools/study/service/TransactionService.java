package com.drools.study.service;

import com.drools.study.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Simplified Transaction processing and validation service
 * Handles basic transaction processing using existing domain model methods
 */
@Service
public class TransactionService {

    @Autowired
    private DroolsService droolsService;

    @Autowired
    private RiskControlService riskControlService;

    // In-memory storage for demo purposes
    private final Map<String, List<Transaction>> transactionHistory = new ConcurrentHashMap<>();
    private final Map<String, Double> customerVelocity = new ConcurrentHashMap<>();

    /**
     * Process a single transaction with risk validation
     */
    public TransactionProcessingResult processTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        // Set processing timestamp
        transaction.setTimestamp(LocalDateTime.now());
        
        // Store transaction in history
        String customerId = transaction.getCustomerId();
        transactionHistory.computeIfAbsent(customerId, k -> new ArrayList<>()).add(transaction);

        // Create processing result
        TransactionProcessingResult result = new TransactionProcessingResult();
        result.setTransactionId(transaction.getTransactionId());
        result.setStatus("COMPLETED");
        result.setApproved(true);
        result.setProcessedAt(LocalDateTime.now());
        result.setRiskAlerts(new ArrayList<>());

        try {
            // Execute risk assessment using simple rule execution
            List<Object> facts = Arrays.asList(transaction);
            List<Object> results = droolsService.executeRules("risk-control-basic", facts);
            
            // Extract risk alerts from results
            List<RiskAlert> alerts = results.stream()
                    .filter(obj -> obj instanceof RiskAlert)
                    .map(RiskAlert.class::cast)
                    .collect(Collectors.toList());
            
            result.setRiskAlerts(alerts);
            
            // Check if transaction should be rejected based on alerts
            boolean hasHighRiskAlert = alerts.stream()
                    .anyMatch(alert -> "HIGH".equals(alert.getRiskLevel()) || 
                                     "CRITICAL".equals(alert.getRiskLevel()));
            
            if (hasHighRiskAlert) {
                result.setApproved(false);
                result.setStatus("REJECTED");
                result.setRejectionReason("High risk transaction detected");
                transaction.setStatus("REJECTED");
            } else {
                transaction.setStatus("COMPLETED");
            }

        } catch (Exception e) {
            result.setApproved(false);
            result.setStatus("ERROR");
            result.setErrorMessage("Error processing transaction: " + e.getMessage());
            transaction.setStatus("FAILED");
        }

        return result;
    }

    /**
     * Get transaction history for a customer
     */
    public List<Transaction> getTransactionHistory(String customerId, int days) {
        List<Transaction> allTransactions = transactionHistory.getOrDefault(customerId, new ArrayList<>());
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        
        return allTransactions.stream()
                .filter(txn -> txn.getTimestamp().isAfter(cutoffDate))
                .collect(Collectors.toList());
    }

    /**
     * Calculate transaction velocity for a customer
     */
    public double calculateTransactionVelocity(String customerId, int hours) {
        List<Transaction> transactions = getTransactionHistory(customerId, hours / 24 + 1);
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        
        long recentTransactions = transactions.stream()
                .filter(txn -> txn.getTimestamp().isAfter(cutoffTime))
                .count();
        
        return (double) recentTransactions / hours;
    }

    /**
     * Calculate transaction volume for a customer
     */
    public BigDecimal calculateTransactionVolume(String customerId, int hours) {
        List<Transaction> transactions = getTransactionHistory(customerId, hours / 24 + 1);
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        
        double totalAmount = transactions.stream()
                .filter(txn -> txn.getTimestamp().isAfter(cutoffTime))
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        return BigDecimal.valueOf(totalAmount);
    }

    /**
     * Detect suspicious patterns in customer transactions
     */
    public List<String> detectSuspiciousPatterns(String customerId) {
        List<String> patterns = new ArrayList<>();
        List<Transaction> recentTransactions = getTransactionHistory(customerId, 7);
        
        if (recentTransactions.size() >= 10) {
            patterns.add("HIGH_FREQUENCY");
        }
        
        // Check for round number pattern
        long roundNumbers = recentTransactions.stream()
                .filter(txn -> txn.getAmount() % 1000 == 0)
                .count();
        
        if (roundNumbers >= 3 && roundNumbers >= recentTransactions.size() * 0.5) {
            patterns.add("ROUND_NUMBER_PATTERN");
        }
        
        // Check for escalating amounts
        if (isEscalatingPattern(recentTransactions)) {
            patterns.add("ESCALATING_AMOUNTS");
        }
        
        return patterns;
    }

    /**
     * Check if transactions show an escalating pattern
     */
    private boolean isEscalatingPattern(List<Transaction> transactions) {
        if (transactions.size() < 3) return false;
        
        // Sort by timestamp
        List<Transaction> sorted = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .collect(Collectors.toList());
        
        // Check if amounts are generally increasing
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).getAmount() <= sorted.get(i-1).getAmount()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Transaction processing result DTO
     */
    public static class TransactionProcessingResult {
        private String transactionId;
        private String status;
        private boolean approved;
        private LocalDateTime processedAt;
        private List<RiskAlert> riskAlerts;
        private String rejectionReason;
        private String errorMessage;

        // Getters and setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public boolean isApproved() { return approved; }
        public void setApproved(boolean approved) { this.approved = approved; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
        
        public List<RiskAlert> getRiskAlerts() { return riskAlerts; }
        public void setRiskAlerts(List<RiskAlert> riskAlerts) { this.riskAlerts = riskAlerts; }
        
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
} 