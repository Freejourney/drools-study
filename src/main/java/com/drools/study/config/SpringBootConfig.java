package com.drools.study.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;

/**
 * Spring Boot configuration for additional application settings
 * Configures async execution, CORS, task scheduling, and other features
 */
@Configuration
@EnableAsync
@EnableScheduling

public class SpringBootConfig implements WebMvcConfigurer {

    /**
     * Configure CORS for REST APIs
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    /**
     * Task executor for async processing
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("DroolsAsync-");
        executor.initialize();
        return executor;
    }

    /**
     * Task executor for risk processing
     */
    @Bean(name = "riskProcessingExecutor")
    public Executor riskProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("RiskProcessor-");
        executor.initialize();
        return executor;
    }

    /**
     * Development profile configuration
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentConfig {
        
        @Bean
        public String developmentMode() {
            return "Development mode enabled - detailed logging and debugging features active";
        }
    }

    /**
     * Production profile configuration
     */
    @Configuration
    @Profile("prod")
    static class ProductionConfig {
        
        @Bean
        public String productionMode() {
            return "Production mode enabled - optimized performance and minimal logging";
        }
    }

    /**
     * Test profile configuration
     */
    @Configuration
    @Profile("test")
    static class TestConfig {
        
        @Bean
        public String testMode() {
            return "Test mode enabled - mock data and test configurations active";
        }
    }
} 