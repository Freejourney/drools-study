package com.drools.study.controller;

import com.drools.study.model.Transaction;
import com.drools.study.service.TransactionService;
import com.drools.study.service.TransactionService.TransactionProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API controller for transaction operations
 * Provides endpoints for transaction processing, monitoring, and analytics
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * Process a new transaction
     * POST /api/transactions/process
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processTransaction(@RequestBody TransactionRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Create transaction from request
            Transaction transaction = createTransactionFromRequest(request);
            
            // Process transaction
            TransactionProcessingResult result = transactionService.processTransaction(transaction);
            
            // Build response
            response.put("success", result.isApproved());
            response.put("transactionId", transaction.getTransactionId());
            response.put("status", result.getStatus());
            response.put("approved", result.isApproved());
            response.put("processedAt", result.getProcessedAt());
            response.put("riskAlerts", result.getRiskAlerts());
            
            if (!result.isApproved()) {
                response.put("rejectionReason", result.getRejectionReason());
            }
            
            if (result.getErrorMessage() != null) {
                response.put("errorMessage", result.getErrorMessage());
            }
            
            HttpStatus status = result.isApproved() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get transaction history for a customer
     * GET /api/transactions/history/{customerId}
     */
    @GetMapping("/history/{customerId}")
    public ResponseEntity<Map<String, Object>> getTransactionHistory(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Transaction> history = transactionService.getTransactionHistory(customerId, days);
            
            response.put("success", true);
            response.put("customerId", customerId);
            response.put("days", days);
            response.put("transactionCount", history.size());
            response.put("transactions", history);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get transaction analytics for a customer
     * GET /api/transactions/analytics/{customerId}
     */
    @GetMapping("/analytics/{customerId}")
    public ResponseEntity<Map<String, Object>> getTransactionAnalytics(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "24") int hours) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            double velocity = transactionService.calculateTransactionVelocity(customerId, hours);
            BigDecimal volume = transactionService.calculateTransactionVolume(customerId, hours);
            List<String> patterns = transactionService.detectSuspiciousPatterns(customerId);
            
            response.put("success", true);
            response.put("customerId", customerId);
            response.put("analysisHours", hours);
            response.put("transactionVelocity", velocity); // transactions per hour
            response.put("transactionVolume", volume);
            response.put("suspiciousPatterns", patterns);
            response.put("analysisTime", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Create a sample transaction for testing
     * POST /api/transactions/sample
     */
    @PostMapping("/sample")
    public ResponseEntity<Map<String, Object>> createSampleTransaction(@RequestParam String customerId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Transaction sample = createSampleTransactionForTesting(customerId);
            TransactionProcessingResult result = transactionService.processTransaction(sample);
            
            response.put("success", true);
            response.put("message", "Sample transaction created and processed");
            response.put("transaction", sample);
            response.put("result", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint
     * GET /api/transactions/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "TransactionController");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Create transaction from request
     */
    private Transaction createTransactionFromRequest(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setCustomerId(request.getCustomerId());
        transaction.setFromAccount(request.getAccountId());
        transaction.setAmount(request.getAmount().doubleValue());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setDescription(request.getDescription());
        transaction.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        transaction.setMerchantCategory(request.getMerchantCategory());
        transaction.setLocation(request.getLocation());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setChannel(request.getChannel() != null ? request.getChannel() : "API");
        transaction.setStatus("PENDING");
        
        return transaction;
    }

    /**
     * Create a sample transaction for testing
     */
    private Transaction createSampleTransactionForTesting(String customerId) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setCustomerId(customerId);
        transaction.setFromAccount("ACC001");
        transaction.setAmount(100.00);
        transaction.setTransactionType("DEBIT");
        transaction.setDescription("Sample transaction for testing");
        transaction.setCurrency("USD");
        transaction.setMerchantCategory("RETAIL");
        transaction.setLocation("New York, NY");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setChannel("API");
        transaction.setStatus("PENDING");
        
        return transaction;
    }

    /**
     * Transaction request DTO
     */
    public static class TransactionRequest {
        private String customerId;
        private String accountId;
        private BigDecimal amount;
        private String transactionType;
        private String description;
        private String currency;
        private String merchantName;
        private String merchantCategory;
        private String location;
        private String channel;

        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        public String getMerchantCategory() { return merchantCategory; }
        public void setMerchantCategory(String merchantCategory) { this.merchantCategory = merchantCategory; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
    }
} 