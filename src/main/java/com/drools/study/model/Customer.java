package com.drools.study.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer entity representing a bank customer in the risk control domain.
 * This class contains all customer-related information used for risk assessment,
 * credit scoring, and fraud detection.
 * 
 * @author Drools Study Tutorial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    /**
     * Unique customer identifier
     */
    private String customerId;
    
    /**
     * Customer's full name
     */
    private String fullName;
    
    /**
     * Customer's date of birth - used for age calculation in risk assessment
     */
    private LocalDate dateOfBirth;
    
    /**
     * Customer's email address
     */
    private String email;
    
    /**
     * Customer's phone number
     */
    private String phoneNumber;
    
    /**
     * Customer's address
     */
    private String address;
    
    /**
     * Customer's annual income - critical for credit assessment
     */
    private Double annualIncome;
    
    /**
     * Customer's employment status (EMPLOYED, UNEMPLOYED, SELF_EMPLOYED, RETIRED)
     */
    private String employmentStatus;
    
    /**
     * Customer's credit score (300-850 range)
     */
    private Integer creditScore;
    
    /**
     * Account opening date
     */
    private LocalDateTime accountOpenDate;
    
    /**
     * Customer risk category (LOW, MEDIUM, HIGH, VERY_HIGH)
     */
    private String riskCategory;
    
    /**
     * Whether customer is blacklisted
     */
    private Boolean isBlacklisted;
    
    /**
     * Whether customer is VIP
     */
    private Boolean isVip;
    
    /**
     * Customer's country of residence
     */
    private String country;
    
    /**
     * Customer's occupation
     */
    private String occupation;
    
    /**
     * Number of years with the bank
     */
    private Integer yearsWithBank;
    
    /**
     * Total number of products owned by customer
     */
    private Integer numberOfProducts;
    
    /**
     * Customer's monthly income
     */
    private Double monthlyIncome;
    
    /**
     * Customer's debt-to-income ratio
     */
    private Double debtToIncomeRatio;
    
    /**
     * Previous fraud incidents count
     */
    private Integer fraudIncidents;
    
    /**
     * List of customer's accounts
     */
    private List<Account> accounts;
    
    /**
     * Customer's risk profile
     */
    private RiskProfile riskProfile;
    
    /**
     * Calculates customer's age based on date of birth
     * @return age in years
     */
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
    
    /**
     * Checks if customer is a senior citizen (age >= 65)
     * @return true if senior citizen
     */
    public boolean isSeniorCitizen() {
        return getAge() >= 65;
    }
    
    /**
     * Checks if customer is young (age < 25)
     * @return true if young customer
     */
    public boolean isYoungCustomer() {
        return getAge() < 25;
    }
    
    /**
     * Checks if customer has high income (>= 100,000)
     * @return true if high income
     */
    public boolean hasHighIncome() {
        return annualIncome != null && annualIncome >= 100000;
    }
    
    /**
     * Checks if customer has good credit score (>= 700)
     * @return true if good credit
     */
    public boolean hasGoodCredit() {
        return creditScore != null && creditScore >= 700;
    }
    
    /**
     * Checks if customer is new (less than 1 year with bank)
     * @return true if new customer
     */
    public boolean isNewCustomer() {
        return yearsWithBank != null && yearsWithBank < 1;
    }
    
    /**
     * Checks if customer is long-term (5+ years with bank)
     * @return true if long-term customer
     */
    public boolean isLongTermCustomer() {
        return yearsWithBank != null && yearsWithBank >= 5;
    }
} 