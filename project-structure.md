# Drools Tutorial Project Structure

## Overview
This is a comprehensive Drools tutorial project focused on risk control domain, demonstrating all features of Drools with detailed test cases and Spring Boot integration.

## Project Structure

```
drools-study/
├── pom.xml                                    # Maven configuration with JDK17 + Drools dependencies
├── project-structure.md                       # This file - project structure documentation
├── README.md                                  # Project overview and setup instructions
├── 
├── src/main/java/
│   ├── com/drools/study/
│   │   ├── config/                           # Configuration classes
│   │   │   ├── DroolsConfig.java            # Drools configuration
│   │   │   └── SpringBootConfig.java        # Spring Boot configuration
│   │   │
│   │   ├── model/                           # Domain models for risk control
│   │   │   ├── Customer.java               # Customer entity
│   │   │   ├── Transaction.java            # Transaction entity
│   │   │   ├── RiskProfile.java            # Risk profile entity
│   │   │   ├── Account.java                # Account entity
│   │   │   ├── CreditScore.java            # Credit score entity
│   │   │   ├── RiskAlert.java              # Risk alert entity
│   │   │   ├── LoanApplication.java        # Loan application entity
│   │   │   └── RiskAssessment.java         # Risk assessment result
│   │   │
│   │   ├── service/                         # Business services
│   │   │   ├── RiskControlService.java     # Risk control business service
│   │   │   ├── DroolsService.java          # Drools rule engine service
│   │   │   └── TransactionService.java     # Transaction processing service
│   │   │
│   │   ├── controller/                      # REST controllers (Spring Boot)
│   │   │   ├── RiskControlController.java  # Risk control REST API
│   │   │   └── TransactionController.java  # Transaction REST API
│   │   │
│   │   └── Application.java                # Spring Boot main application
│   │
├── src/main/resources/
│   │   ├── rules/                           # Drools rule files (.drl)
│   │   │   ├── risk-control-basic.drl      # Basic risk control rules
│   │   │   ├── transaction-monitoring.drl  # Transaction monitoring rules
│   │   │   ├── credit-scoring.drl          # Credit scoring rules
│   │   │   ├── fraud-detection.drl         # Fraud detection rules
│   │   │   ├── loan-approval.drl           # Loan approval rules
│   │   │   ├── complex-conditions.drl      # Complex condition examples
│   │   │   ├── decision-tables.drl         # Decision table examples
│   │   │   └── advanced-features.drl       # Advanced Drools features
│   │   │
│   │   ├── decision-tables/                # Excel decision tables
│   │   │   ├── credit-scoring-table.xlsx   # Credit scoring decision table
│   │   │   └── risk-matrix.xlsx           # Risk assessment matrix
│   │   │
│   │   ├── templates/                       # Rule templates
│   │   │   ├── risk-template.drt           # Risk assessment template
│   │   │   └── threshold-template.drt      # Threshold check template
│   │   │
│   │   ├── knowledge-sessions/              # KieSession configurations
│   │   │   └── kmodule.xml                 # Kie module configuration
│   │   │
│   │   ├── application.yml                 # Spring Boot configuration
│   │   └── logback.xml                     # Logging configuration
│   │
├── src/test/java/
│   ├── com/drools/study/
│   │   ├── basic/                          # Basic Drools functionality tests
│   │   │   ├── DroolsBasicTest.java       # Basic rule execution tests
│   │   │   ├── FactInsertionTest.java     # Fact insertion and retraction tests
│   │   │   ├── RuleActivationTest.java    # Rule activation and firing tests
│   │   │   └── WorkingMemoryTest.java     # Working memory operations tests
│   │   │
│   │   ├── features/                       # Drools feature-specific tests
│   │   │   ├── ConditionalElementsTest.java    # LHS conditional elements tests
│   │   │   ├── RuleAttributesTest.java         # Rule attributes (salience, no-loop, etc.)
│   │   │   ├── GlobalsAndFunctionsTest.java    # Globals and functions tests
│   │   │   ├── QueriesTest.java               # Drools queries tests
│   │   │   ├── EventProcessingTest.java       # Complex event processing tests
│   │   │   ├── DecisionTablesTest.java        # Decision tables tests
│   │   │   ├── RuleTemplatesTest.java         # Rule templates tests
│   │   │   ├── DynamicRulesTest.java          # Dynamic rule loading tests
│   │   │   ├── RuleFlowTest.java              # Rule flow and agenda groups tests
│   │   │   └── AdvancedFeaturesTest.java      # Advanced features tests
│   │   │
│   │   ├── riskcontrol/                    # Risk control domain tests
│   │   │   ├── RiskControlBasicTest.java      # Basic risk control scenarios
│   │   │   ├── TransactionMonitoringTest.java  # Transaction monitoring tests
│   │   │   ├── FraudDetectionTest.java        # Fraud detection tests
│   │   │   ├── CreditScoringTest.java         # Credit scoring tests
│   │   │   ├── LoanApprovalTest.java          # Loan approval process tests
│   │   │   ├── RiskAssessmentTest.java        # Risk assessment tests
│   │   │   └── ComplexScenariosTest.java      # Complex business scenarios
│   │   │
│   │   ├── springboot/                     # Spring Boot integration tests
│   │   │   ├── SpringBootDroolsTest.java      # Spring Boot + Drools integration
│   │   │   ├── RestApiTest.java               # REST API tests
│   │   │   ├── ServiceLayerTest.java          # Service layer tests
│   │   │   └── ConfigurationTest.java         # Configuration tests
│   │   │
│   │   └── performance/                    # Performance and optimization tests
│   │       ├── PerformanceTest.java           # Performance benchmarks
│   │       ├── MemoryUsageTest.java           # Memory usage tests
│   │       └── ConcurrencyTest.java           # Concurrent execution tests
│   │
├── docs/                                   # Documentation
│   ├── principles.md                       # Drools principles with mermaid charts
│   ├── common-traps.md                     # Common traps and key points
│   ├── interview-qa-chinese.md             # Interview Q&A in Chinese
│   ├── api-documentation.md                # API documentation
│   └── best-practices.md                   # Best practices guide
│
└── scripts/                               # Utility scripts
    ├── run-tests.sh                       # Test execution script
    ├── generate-docs.sh                   # Documentation generation
    └── setup.sh                          # Project setup script
```

## Key Components

### 1. Domain Models (Risk Control)
- **Customer**: Customer information and risk profile
- **Transaction**: Financial transaction details
- **RiskProfile**: Customer risk assessment profile  
- **Account**: Account information and status
- **CreditScore**: Credit scoring data
- **RiskAlert**: Risk alert notifications
- **LoanApplication**: Loan application data
- **RiskAssessment**: Risk assessment results

### 2. Test Categories

#### Basic Drools Tests
- Rule engine initialization and configuration
- Fact insertion, modification, and retraction
- Rule activation and execution
- Working memory operations
- KieSession lifecycle management

#### Feature-Specific Tests
- **Conditional Elements**: and, or, not, exists, forall, accumulate, collect
- **Rule Attributes**: salience, no-loop, lock-on-active, agenda-group, etc.
- **Functions and Globals**: Global variables and utility functions
- **Queries**: Drools query functionality
- **Event Processing**: Complex event processing (CEP)
- **Decision Tables**: Excel-based decision tables
- **Rule Templates**: Parameterized rule templates
- **Dynamic Rules**: Runtime rule loading and modification

#### Risk Control Domain Tests
- Transaction monitoring and anomaly detection
- Fraud detection algorithms
- Credit scoring and risk assessment
- Loan approval workflows
- Risk profiling and segmentation
- Alert generation and notification

#### Spring Boot Integration
- Configuration and dependency injection
- REST API integration
- Service layer integration
- Transaction management
- Error handling and logging

### 3. Rule Files Structure
- **Basic Rules**: Simple condition-action rules
- **Complex Rules**: Multi-condition rules with accumulations
- **Decision Tables**: Tabular rule representations
- **Rule Templates**: Reusable rule patterns
- **Advanced Features**: CEP, rule flows, agenda groups

### 4. Documentation
- **Principles**: Core concepts with visual diagrams
- **Common Traps**: Pitfalls and solutions
- **Interview Q&A**: Common interview questions (Chinese)
- **Best Practices**: Development guidelines
- **API Documentation**: REST API reference

## Technology Stack
- **Java**: JDK 17
- **Build Tool**: Maven 3.x
- **Rules Engine**: Drools 8.x
- **Framework**: Spring Boot 3.x
- **Testing**: JUnit 5, Mockito
- **Documentation**: Markdown with Mermaid diagrams

## Setup Instructions
1. Ensure JDK 17 is installed
2. Run `mvn clean install` to build the project
3. Run `mvn test` to execute all tests
4. Run `mvn spring-boot:run` to start the Spring Boot application
5. Access REST APIs at `http://localhost:8080`

## Test Execution
- **All Tests**: `mvn test`
- **Specific Category**: `mvn test -Dtest="*BasicTest"`
- **Performance Tests**: `mvn test -Dtest="*PerformanceTest"`
- **Integration Tests**: `mvn test -Dtest="*SpringBootTest"` 