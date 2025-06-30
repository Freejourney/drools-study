package com.drools.study.service;

import com.drools.study.model.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DroolsService provides high-level methods for rule execution and fact management.
 * This service demonstrates various patterns for using Drools in a business application,
 * including stateful and stateless execution, fact management, and result collection.
 * 
 * @author Drools Study Tutorial
 */
@Service
public class DroolsService {
    
    private static final Logger logger = LoggerFactory.getLogger(DroolsService.class);
    
    @Autowired
    private KieContainer kieContainer;
    
    @Autowired
    private KieSession kieSession;
    
    @Autowired
    private StatelessKieSession statelessKieSession;
    
    /**
     * Executes rules using stateful session for a single customer.
     * This method demonstrates basic rule execution with fact insertion.
     * 
     * @param customer the customer to evaluate
     * @return list of risk alerts generated
     */
    public List<RiskAlert> evaluateCustomerRisk(Customer customer) {
        logger.info("Evaluating customer risk for customer: {}", customer.getCustomerId());
        
        List<RiskAlert> alerts = new ArrayList<>();
        
        try {
            // Insert customer fact into working memory
            FactHandle customerHandle = kieSession.insert(customer);
            
            // Insert customer's risk profile if available
            if (customer.getRiskProfile() != null) {
                kieSession.insert(customer.getRiskProfile());
            }
            
            // Insert customer's accounts if available
            if (customer.getAccounts() != null) {
                for (Account account : customer.getAccounts()) {
                    kieSession.insert(account);
                }
            }
            
            // Set global for collecting alerts
            kieSession.setGlobal("alerts", alerts);
            
            // Fire all applicable rules
            int rulesFired = kieSession.fireAllRules();
            logger.info("Fired {} rules for customer {}", rulesFired, customer.getCustomerId());
            
            // Clean up facts from working memory
            kieSession.delete(customerHandle);
            
        } catch (Exception e) {
            logger.error("Error evaluating customer risk for {}: {}", customer.getCustomerId(), e.getMessage(), e);
            throw new RuntimeException("Rule execution failed", e);
        }
        
        return alerts;
    }
    
    /**
     * Evaluates transaction risk using stateless session.
     * This method demonstrates stateless rule execution for individual transactions.
     * 
     * @param transaction the transaction to evaluate
     * @param customer the customer who initiated the transaction
     * @return list of risk alerts generated
     */
    public List<RiskAlert> evaluateTransactionRisk(Transaction transaction, Customer customer) {
        logger.info("Evaluating transaction risk for transaction: {}", transaction.getTransactionId());
        
        List<RiskAlert> alerts = new ArrayList<>();
        List<Object> facts = new ArrayList<>();
        
        // Prepare facts for stateless execution
        facts.add(transaction);
        facts.add(customer);
        
        if (customer.getRiskProfile() != null) {
            facts.add(customer.getRiskProfile());
        }
        
        // Add account information if available
        if (customer.getAccounts() != null) {
            facts.addAll(customer.getAccounts());
        }
        
        // Add alert collection
        facts.add(alerts);
        
        try {
            // Execute rules in stateless mode
            statelessKieSession.execute(facts);
            logger.info("Generated {} alerts for transaction {}", alerts.size(), transaction.getTransactionId());
            
        } catch (Exception e) {
            logger.error("Error evaluating transaction risk for {}: {}", transaction.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Rule execution failed", e);
        }
        
        return alerts;
    }
    
    /**
     * Processes loan application using comprehensive rule evaluation.
     * This method demonstrates complex fact scenarios with multiple related objects.
     * 
     * @param loanApplication the loan application to process
     * @param customer the applicant
     * @param creditScore the applicant's credit score
     * @return risk assessment result
     */
    public RiskAssessment processLoanApplication(LoanApplication loanApplication, Customer customer, CreditScore creditScore) {
        logger.info("Processing loan application: {}", loanApplication.getApplicationId());
        
        RiskAssessment assessment = RiskAssessment.builder()
                .assessmentId("ASSESS_" + loanApplication.getApplicationId())
                .customerId(customer.getCustomerId())
                .assessmentType("LOAN_APPLICATION")
                .build();
        
        KieSession session = kieContainer.newKieSession("risk-control-session");
        
        try {
            // Insert all relevant facts
            session.insert(loanApplication);
            session.insert(customer);
            session.insert(creditScore);
            session.insert(assessment);
            
            if (customer.getRiskProfile() != null) {
                session.insert(customer.getRiskProfile());
            }
            
            // Set up globals for rule execution
            List<String> firedRules = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();
            
            session.setGlobal("firedRules", firedRules);
            session.setGlobal("recommendations", recommendations);
            session.setGlobal("logger", logger);
            
            // Fire rules and collect results
            int rulesFired = session.fireAllRules();
            logger.info("Fired {} rules for loan application {}", rulesFired, loanApplication.getApplicationId());
            
            // Update assessment with results
            assessment.setFiredRules(firedRules);
            assessment.setRecommendations(recommendations);
            
        } catch (Exception e) {
            logger.error("Error processing loan application {}: {}", loanApplication.getApplicationId(), e.getMessage(), e);
            throw new RuntimeException("Loan application processing failed", e);
        } finally {
            session.dispose();
        }
        
        return assessment;
    }
    
    /**
     * Performs batch processing of transactions for fraud detection.
     * This method demonstrates bulk rule execution with agenda groups.
     * 
     * @param transactions list of transactions to analyze
     * @param customer the customer owning the transactions
     * @return comprehensive risk assessment
     */
    public RiskAssessment batchProcessTransactions(List<Transaction> transactions, Customer customer) {
        logger.info("Batch processing {} transactions for customer {}", transactions.size(), customer.getCustomerId());
        
        RiskAssessment assessment = RiskAssessment.builder()
                .assessmentId("BATCH_" + customer.getCustomerId())
                .customerId(customer.getCustomerId())
                .assessmentType("TRANSACTION")
                .build();
        
        KieSession session = kieContainer.newKieSession("risk-control-session");
        
        try {
            // Insert customer and assessment
            session.insert(customer);
            session.insert(assessment);
            
            // Insert all transactions
            for (Transaction transaction : transactions) {
                session.insert(transaction);
            }
            
            // Set up agenda groups for controlled execution
            session.getAgenda().getAgendaGroup("fraud-detection").setFocus();
            session.getAgenda().getAgendaGroup("transaction-monitoring").setFocus();
            
            // Execute rules in phases
            int totalRulesFired = 0;
            
            // Phase 1: Basic validation rules
            session.getAgenda().getAgendaGroup("validation").setFocus();
            totalRulesFired += session.fireAllRules();
            
            // Phase 2: Fraud detection rules
            session.getAgenda().getAgendaGroup("fraud-detection").setFocus();
            totalRulesFired += session.fireAllRules();
            
            // Phase 3: Pattern analysis rules
            session.getAgenda().getAgendaGroup("pattern-analysis").setFocus();
            totalRulesFired += session.fireAllRules();
            
            logger.info("Total rules fired in batch processing: {}", totalRulesFired);
            
        } catch (Exception e) {
            logger.error("Error in batch processing for customer {}: {}", customer.getCustomerId(), e.getMessage(), e);
            throw new RuntimeException("Batch processing failed", e);
        } finally {
            session.dispose();
        }
        
        return assessment;
    }
    
    /**
     * Retrieves all facts of a specific type from the working memory.
     * This method demonstrates how to query working memory.
     * 
     * @param factClass the class of facts to retrieve
     * @param <T> the type of facts
     * @return collection of facts
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getFactsOfType(Class<T> factClass) {
        logger.debug("Retrieving facts of type: {}", factClass.getSimpleName());
        
        Collection<T> facts = new ArrayList<>();
        
        try {
            Collection<FactHandle> factHandles = kieSession.getFactHandles(
                obj -> factClass.isAssignableFrom(obj.getClass())
            );
            
            for (FactHandle handle : factHandles) {
                facts.add((T) kieSession.getObject(handle));
            }
            
            logger.debug("Found {} facts of type {}", facts.size(), factClass.getSimpleName());
            
        } catch (Exception e) {
            logger.error("Error retrieving facts of type {}: {}", factClass.getSimpleName(), e.getMessage(), e);
        }
        
        return facts;
    }
    
    /**
     * Clears all facts from the working memory.
     * This method demonstrates working memory management.
     */
    public void clearWorkingMemory() {
        logger.info("Clearing working memory");
        
        try {
            Collection<FactHandle> factHandles = kieSession.getFactHandles();
            
            for (FactHandle handle : factHandles) {
                kieSession.delete(handle);
            }
            
            logger.info("Cleared {} facts from working memory", factHandles.size());
            
        } catch (Exception e) {
            logger.error("Error clearing working memory: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Gets the count of facts currently in working memory.
     * 
     * @return number of facts in working memory
     */
    public long getWorkingMemoryFactCount() {
        try {
            return kieSession.getFactCount();
        } catch (Exception e) {
            logger.error("Error getting fact count: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Creates a new KieSession for isolated rule execution.
     * This method demonstrates session management patterns.
     * 
     * @param sessionName the name of the session to create
     * @return new KieSession instance
     */
    public KieSession createNewSession(String sessionName) {
        logger.info("Creating new KieSession: {}", sessionName);
        
        try {
            KieSession session = kieContainer.newKieSession(sessionName);
            
            // Configure the new session
            session.setGlobal("logger", logger);
            
            return session;
            
        } catch (Exception e) {
            logger.error("Error creating new session {}: {}", sessionName, e.getMessage(), e);
            throw new RuntimeException("Failed to create new session", e);
        }
    }
} 