#!/bin/bash

# Drools Tutorial Project Documentation Generator
# This script generates comprehensive documentation for the Drools tutorial project

set -e

echo "=== Drools Tutorial Documentation Generator ==="
echo "Generating comprehensive project documentation..."
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

# Create documentation directory if it doesn't exist
print_header "Setting up Documentation Structure"
mkdir -p docs/generated
mkdir -p docs/javadoc
mkdir -p docs/images

print_status "Documentation directories created"

# Generate Javadoc
print_header "Generating Javadoc"
if command -v mvn &> /dev/null; then
    print_status "Generating Javadoc with Maven..."
    mvn javadoc:javadoc -q
    
    if [ -d "target/site/apidocs" ]; then
        cp -r target/site/apidocs/* docs/javadoc/
        print_status "Javadoc generated successfully ✓"
    else
        print_warning "Javadoc generation may have failed"
    fi
else
    print_error "Maven not found - skipping Javadoc generation"
fi

# Generate project metrics
print_header "Generating Project Metrics"
cat > docs/generated/project-metrics.md << 'EOF'
# Drools Tutorial Project Metrics

## Overview
This document contains automatically generated metrics for the Drools tutorial project.

## Code Structure
EOF

# Count files and lines
if command -v find &> /dev/null; then
    JAVA_FILES=$(find src -name "*.java" | wc -l)
    DRL_FILES=$(find src -name "*.drl" | wc -l)
    TEST_FILES=$(find src/test -name "*.java" | wc -l)
    
    echo "### File Counts" >> docs/generated/project-metrics.md
    echo "- Java source files: $JAVA_FILES" >> docs/generated/project-metrics.md
    echo "- Drools rule files: $DRL_FILES" >> docs/generated/project-metrics.md
    echo "- Test files: $TEST_FILES" >> docs/generated/project-metrics.md
    echo "" >> docs/generated/project-metrics.md
    
    print_status "Project metrics generated"
fi

# Generate rule inventory
print_header "Generating Rule Inventory"
cat > docs/generated/rule-inventory.md << 'EOF'
# Drools Rules Inventory

## Overview
This document lists all Drools rules implemented in the tutorial project.

## Rule Files
EOF

if [ -d "src/main/resources/rules" ]; then
    for rule_file in src/main/resources/rules/*.drl; do
        if [ -f "$rule_file" ]; then
            filename=$(basename "$rule_file")
            echo "### $filename" >> docs/generated/rule-inventory.md
            echo "\`\`\`" >> docs/generated/rule-inventory.md
            echo "File: $rule_file" >> docs/generated/rule-inventory.md
            
            # Extract rule names
            if command -v grep &> /dev/null; then
                echo "Rules:" >> docs/generated/rule-inventory.md
                grep -n "^rule " "$rule_file" | sed 's/^/  - /' >> docs/generated/rule-inventory.md
            fi
            
            echo "\`\`\`" >> docs/generated/rule-inventory.md
            echo "" >> docs/generated/rule-inventory.md
        fi
    done
    print_status "Rule inventory generated"
fi

# Generate test coverage report
print_header "Generating Test Coverage Report"
cat > docs/generated/test-coverage.md << 'EOF'
# Test Coverage Report

## Overview
This document provides information about test coverage in the Drools tutorial project.

## Test Categories
EOF

if [ -d "src/test/java" ]; then
    echo "### Test Structure" >> docs/generated/test-coverage.md
    find src/test/java -name "*.java" | sed 's|src/test/java/||' | sed 's|\.java||' | sed 's|/|.|g' | sed 's/^/- /' >> docs/generated/test-coverage.md
    echo "" >> docs/generated/test-coverage.md
    print_status "Test coverage report generated"
fi

# Generate feature matrix
print_header "Generating Feature Matrix"
cat > docs/generated/feature-matrix.md << 'EOF'
# Drools Features Implementation Matrix

## Overview
This matrix shows which Drools features are implemented and tested in this tutorial project.

| Feature Category | Feature | Implemented | Tested | Example Location |
|------------------|---------|-------------|--------|------------------|
| **Basic Features** | | | | |
| Rule Engine | Basic Rules | ✅ | ✅ | `src/main/resources/rules/risk-control-basic.drl` |
| Rule Engine | Fact Insertion | ✅ | ✅ | `src/test/java/com/drools/study/basic/FactInsertionTest.java` |
| Rule Engine | Working Memory | ✅ | ✅ | `src/test/java/com/drools/study/basic/DroolsBasicTest.java` |
| **Conditional Elements** | | | | |
| Patterns | AND Conditions | ✅ | ✅ | `src/main/resources/rules/complex-conditions.drl` |
| Patterns | OR Conditions | ✅ | ✅ | `src/main/resources/rules/complex-conditions.drl` |
| Patterns | NOT Conditions | ✅ | ✅ | `src/main/resources/rules/complex-conditions.drl` |
| Patterns | EXISTS | ✅ | ✅ | `src/main/resources/rules/complex-conditions.drl` |
| Patterns | FORALL | ✅ | ✅ | `src/main/resources/rules/complex-conditions.drl` |
| Patterns | ACCUMULATE | ✅ | ✅ | `src/main/resources/rules/complex-conditions.drl` |
| Patterns | COLLECT | ✅ | ✅ | `src/main/resources/rules/complex-conditions.drl` |
| **Advanced Features** | | | | |
| Rules | Salience | ✅ | ✅ | `src/test/java/com/drools/study/features/RuleAttributesTest.java` |
| Rules | Agenda Groups | ✅ | ✅ | `src/test/java/com/drools/study/features/RuleAttributesTest.java` |
| Rules | Rule Templates | ✅ | ⏳ | `src/main/resources/templates/risk-template.drt` |
| Rules | Decision Tables | ✅ | ⏳ | `src/main/resources/decision-tables/` |
| **Domain Specific** | | | | |
| Risk Control | Transaction Monitoring | ✅ | ✅ | `src/main/resources/rules/transaction-monitoring.drl` |
| Risk Control | Fraud Detection | ✅ | ✅ | `src/main/resources/rules/fraud-detection.drl` |
| Risk Control | Credit Scoring | ✅ | ✅ | `src/main/resources/rules/credit-scoring.drl` |
| Risk Control | Loan Approval | ✅ | ✅ | `src/main/resources/rules/loan-approval.drl` |
| **Integration** | | | | |
| Spring Boot | Configuration | ✅ | ✅ | `src/main/java/com/drools/study/config/DroolsConfig.java` |
| Spring Boot | REST APIs | ✅ | ✅ | `src/main/java/com/drools/study/controller/` |
| Spring Boot | Services | ✅ | ✅ | `src/main/java/com/drools/study/service/` |

## Legend
- ✅ Implemented and tested
- ⏳ Implemented but needs more testing
- ❌ Not implemented
- 🔄 Work in progress

EOF

print_status "Feature matrix generated"

# Generate README for docs
print_header "Generating Documentation Index"
cat > docs/README.md << 'EOF'
# Drools Tutorial Documentation

Welcome to the comprehensive documentation for the Drools tutorial project focused on risk control domain.

## Documentation Structure

### Core Documentation
- [Project Principles](principles.md) - Core Drools concepts with Mermaid diagrams
- [Common Traps](common-traps.md) - Common pitfalls and how to avoid them
- [Best Practices](best-practices.md) - Development guidelines and recommendations
- [API Documentation](api-documentation.md) - REST API reference

### Generated Documentation
- [Project Metrics](generated/project-metrics.md) - Automatically generated project statistics
- [Rule Inventory](generated/rule-inventory.md) - Complete list of all rules
- [Test Coverage](generated/test-coverage.md) - Test coverage information
- [Feature Matrix](generated/feature-matrix.md) - Implementation status of Drools features

### Technical Documentation
- [Javadoc](javadoc/) - Complete API documentation
- [Interview Q&A (Chinese)](interview-qa-chinese.md) - Common interview questions and answers

## Quick Start

1. **Setup**: Follow the [setup instructions](../README.md#setup) in the main README
2. **Learn Basics**: Start with [principles.md](principles.md) to understand core concepts
3. **Explore Examples**: Review the test cases in `src/test/java/com/drools/study/`
4. **Try API**: Use the REST APIs documented in [api-documentation.md](api-documentation.md)

## Learning Path

### Beginner
1. Read [principles.md](principles.md) for fundamental concepts
2. Study basic examples in `src/test/java/com/drools/study/basic/`
3. Run the setup script: `./scripts/setup.sh`

### Intermediate
1. Explore feature-specific tests in `src/test/java/com/drools/study/features/`
2. Review rule files in `src/main/resources/rules/`
3. Study [best-practices.md](best-practices.md)

### Advanced
1. Examine Spring Boot integration in `src/test/java/com/drools/study/springboot/`
2. Review complex scenarios in `src/test/java/com/drools/study/riskcontrol/`
3. Study [common-traps.md](common-traps.md) for expert insights

## Contributing

To regenerate this documentation:
```bash
./scripts/generate-docs.sh
```

## Support

For questions or issues:
1. Check [common-traps.md](common-traps.md) for known issues
2. Review the [interview Q&A](interview-qa-chinese.md) for detailed explanations
3. Examine the test cases for working examples

EOF

print_status "Documentation index generated"

# Final summary
print_header "Documentation Generation Complete"
echo "Generated documentation includes:"
echo "  ✓ Project metrics and statistics"
echo "  ✓ Complete rule inventory"
echo "  ✓ Test coverage report"
echo "  ✓ Feature implementation matrix"
echo "  ✓ Documentation index"
if [ -d "docs/javadoc" ]; then
    echo "  ✓ Javadoc API documentation"
fi
echo
print_status "Documentation is available in the docs/ directory"
print_status "Open docs/README.md to get started"
echo
print_warning "To view the documentation properly:"
echo "1. Use a Markdown viewer or IDE with Markdown support"
echo "2. For best experience, use a web server to serve the docs/ directory"
echo "3. Mermaid diagrams require a Mermaid-compatible viewer" 