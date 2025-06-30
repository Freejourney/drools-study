package com.drools.study.config;

import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Drools configuration class that sets up the Drools rule engine.
 * This configuration demonstrates various ways to configure Drools in a Spring application,
 * including KieContainer, KieSession, and rule compilation.
 * 
 * @author Drools Study Tutorial
 */
@Configuration
public class DroolsConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DroolsConfig.class);
    
    /**
     * Creates and configures KieServices instance.
     * KieServices is the main entry point for accessing Drools services.
     * 
     * @return KieServices instance
     */
    @Bean
    public KieServices kieServices() {
        logger.info("Creating KieServices instance");
        return KieServices.Factory.get();
    }
    
    /**
     * Creates KieContainer which holds all the compiled knowledge.
     * This method demonstrates programmatic rule loading and compilation.
     * 
     * @return KieContainer with compiled rules
     * @throws IOException if rule files cannot be read
     */
    @Bean
    public KieContainer kieContainer() throws IOException {
        logger.info("Creating KieContainer with rule compilation");
        
        KieServices kieServices = kieServices();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        // Add rule files to the file system
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/risk-control-basic.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/transaction-monitoring.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/credit-scoring.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/fraud-detection.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/loan-approval.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/complex-conditions.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/advanced-features.drl"));
        
        // Add kmodule.xml configuration
        kieFileSystem.writeKModuleXML(getKModuleXML());
        
        // Build the rules
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        
        // Check for compilation errors
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            logger.error("Rule compilation errors found:");
            results.getMessages().forEach(message -> logger.error("Error: {}", message.getText()));
            throw new IllegalStateException("Rule compilation failed with errors");
        }
        
        // Log warnings if any
        if (results.hasMessages(Message.Level.WARNING)) {
            logger.warn("Rule compilation warnings found:");
            results.getMessages(Message.Level.WARNING)
                   .forEach(message -> logger.warn("Warning: {}", message.getText()));
        }
        
        logger.info("Rules compiled successfully");
        return kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
    }
    
    /**
     * Creates a stateful KieSession for rule execution.
     * Stateful sessions maintain state between rule executions.
     * 
     * @return KieSession for rule execution
     * @throws IOException if KieContainer creation fails
     */
    @Bean
    public KieSession kieSession() throws IOException {
        logger.info("Creating stateful KieSession");
        KieSession kieSession = kieContainer().newKieSession("risk-control-session");
        
        // Configure session-level settings
        configureKieSession(kieSession);
        
        return kieSession;
    }
    
    /**
     * Creates a stateless KieSession for one-time rule execution.
     * Stateless sessions are used for fire-and-forget rule execution.
     * 
     * @return StatelessKieSession for one-time execution
     * @throws IOException if KieContainer creation fails
     */
    @Bean
    public org.kie.api.runtime.StatelessKieSession statelessKieSession() throws IOException {
        logger.info("Creating stateless KieSession");
        return kieContainer().newStatelessKieSession("risk-control-stateless-session");
    }
    
    /**
     * Configures the KieSession with globals and event listeners.
     * This method demonstrates how to set up session-level configurations.
     * 
     * @param kieSession the session to configure
     */
    private void configureKieSession(KieSession kieSession) {
        logger.info("Configuring KieSession with globals and listeners");
        
        // Set global variables that can be used in rules
        kieSession.setGlobal("logger", logger);
        kieSession.setGlobal("riskThreshold", 70);
        kieSession.setGlobal("fraudThreshold", 80);
        kieSession.setGlobal("maxTransactionAmount", 50000.0);
        
        // Add event listeners for debugging and monitoring
        kieSession.addEventListener(new org.kie.api.event.rule.DebugAgendaEventListener());
        kieSession.addEventListener(new org.kie.api.event.rule.DebugRuleRuntimeEventListener());
        
        // Add custom event listener for rule execution tracking
        kieSession.addEventListener(new RuleExecutionListener());
    }
    
    /**
     * Provides the kmodule.xml configuration as a string.
     * This defines different KieSessions and their configurations.
     * 
     * @return kmodule.xml content as string
     */
    private String getKModuleXML() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <kmodule xmlns="http://www.drools.org/xsd/kmodule">
                    
                    <!-- Stateful session for interactive rule execution -->
                    <kbase name="risk-control-rules" packages="rules" default="true">
                        <ksession name="risk-control-session" type="stateful" default="true">
                            <consoleLogger/>
                        </ksession>
                        
                        <ksession name="risk-control-stateless-session" type="stateless" default="false">
                            <consoleLogger/>
                        </ksession>
                    </kbase>
                    
                    <!-- Separate knowledge base for fraud detection rules -->
                    <kbase name="fraud-detection-rules" packages="rules.fraud">
                        <ksession name="fraud-detection-session" type="stateful">
                            <consoleLogger/>
                        </ksession>
                    </kbase>
                    
                    <!-- Knowledge base for credit scoring -->
                    <kbase name="credit-scoring-rules" packages="rules.credit">
                        <ksession name="credit-scoring-session" type="stateful">
                            <consoleLogger/>
                        </ksession>
                    </kbase>
                    
                    <!-- Knowledge base for transaction monitoring -->
                    <kbase name="transaction-monitoring-rules" packages="rules.transaction">
                        <ksession name="transaction-monitoring-session" type="stateful">
                            <consoleLogger/>
                        </ksession>
                    </kbase>
                    
                </kmodule>
                """;
    }
    
    /**
     * Custom rule execution listener for monitoring rule firing.
     * This demonstrates how to track rule execution for debugging and analytics.
     */
    public static class RuleExecutionListener implements org.kie.api.event.rule.AgendaEventListener {
        
        private static final Logger log = LoggerFactory.getLogger(RuleExecutionListener.class);
        
        @Override
        public void matchCreated(org.kie.api.event.rule.MatchCreatedEvent event) {
            log.debug("Rule match created: {}", event.getMatch().getRule().getName());
        }
        
        @Override
        public void matchCancelled(org.kie.api.event.rule.MatchCancelledEvent event) {
            log.debug("Rule match cancelled: {}", event.getMatch().getRule().getName());
        }
        
        @Override
        public void beforeMatchFired(org.kie.api.event.rule.BeforeMatchFiredEvent event) {
            log.info("Before rule fired: {}", event.getMatch().getRule().getName());
        }
        
        @Override
        public void afterMatchFired(org.kie.api.event.rule.AfterMatchFiredEvent event) {
            log.info("After rule fired: {} - Result: {}", 
                    event.getMatch().getRule().getName(),
                    event.getMatch().getRule().getMetaData());
        }
        
        @Override
        public void agendaGroupPopped(org.kie.api.event.rule.AgendaGroupPoppedEvent event) {
            log.debug("Agenda group popped: {}", event.getAgendaGroup().getName());
        }
        
        @Override
        public void agendaGroupPushed(org.kie.api.event.rule.AgendaGroupPushedEvent event) {
            log.debug("Agenda group pushed: {}", event.getAgendaGroup().getName());
        }
        
        @Override
        public void beforeRuleFlowGroupActivated(org.kie.api.event.rule.RuleFlowGroupActivatedEvent event) {
            log.debug("Before rule flow group activated: {}", event.getRuleFlowGroup().getName());
        }
        
        @Override
        public void afterRuleFlowGroupActivated(org.kie.api.event.rule.RuleFlowGroupActivatedEvent event) {
            log.debug("After rule flow group activated: {}", event.getRuleFlowGroup().getName());
        }
        
        @Override
        public void beforeRuleFlowGroupDeactivated(org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent event) {
            log.debug("Before rule flow group deactivated: {}", event.getRuleFlowGroup().getName());
        }
        
        @Override
        public void afterRuleFlowGroupDeactivated(org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent event) {
            log.debug("After rule flow group deactivated: {}", event.getRuleFlowGroup().getName());
        }
    }
} 