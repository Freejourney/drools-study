package com.drools.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot application class for the Drools Study tutorial.
 * This application demonstrates comprehensive Drools integration with Spring Boot
 * for risk control and financial applications.
 * 
 * @author Drools Study Tutorial
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.drools.study")
public class Application {
    
    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
} 