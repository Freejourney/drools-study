#!/bin/bash

# Drools Study - Test Execution Script
# This script provides convenient commands to run different types of tests

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Maven is installed
check_maven() {
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Test execution options:"
    echo "  all          Run all tests"
    echo "  basic        Run basic Drools functionality tests"
    echo "  features     Run Drools feature-specific tests"
    echo "  riskcontrol  Run risk control domain tests"
    echo "  springboot   Run Spring Boot integration tests"
    echo "  performance  Run performance tests"
    echo "  unit         Run unit tests only"
    echo "  integration  Run integration tests only"
    echo "  fast         Run fast tests (exclude performance tests)"
    echo "  coverage     Run tests with coverage report"
    echo "  clean        Clean and run all tests"
    echo "  verify       Run full verification (clean, compile, test)"
    echo ""
    echo "Examples:"
    echo "  $0 all           # Run all tests"
    echo "  $0 basic         # Run basic Drools tests"
    echo "  $0 coverage      # Run tests with coverage"
    echo "  $0 performance   # Run performance tests only"
}

# Function to run all tests
run_all_tests() {
    print_info "Running all tests..."
    mvn clean test
    print_success "All tests completed"
}

# Function to run basic tests
run_basic_tests() {
    print_info "Running basic Drools functionality tests..."
    mvn test -Dtest="**/basic/*Test"
    print_success "Basic tests completed"
}

# Function to run feature tests
run_feature_tests() {
    print_info "Running Drools feature-specific tests..."
    mvn test -Dtest="**/features/*Test"
    print_success "Feature tests completed"
}

# Function to run risk control tests
run_riskcontrol_tests() {
    print_info "Running risk control domain tests..."
    mvn test -Dtest="**/riskcontrol/*Test"
    print_success "Risk control tests completed"
}

# Function to run Spring Boot tests
run_springboot_tests() {
    print_info "Running Spring Boot integration tests..."
    mvn test -Dtest="**/springboot/*Test"
    print_success "Spring Boot tests completed"
}

# Function to run performance tests
run_performance_tests() {
    print_info "Running performance tests..."
    print_warning "Performance tests may take several minutes..."
    mvn test -Dtest="**/performance/*Test"
    print_success "Performance tests completed"
}

# Function to run unit tests only
run_unit_tests() {
    print_info "Running unit tests only..."
    mvn test -Dtest="**/*Test" -DexcludedGroups="integration,performance"
    print_success "Unit tests completed"
}

# Function to run integration tests only
run_integration_tests() {
    print_info "Running integration tests only..."
    mvn test -Dgroups="integration"
    print_success "Integration tests completed"
}

# Function to run fast tests (excluding performance)
run_fast_tests() {
    print_info "Running fast tests (excluding performance tests)..."
    mvn test -Dtest="**/*Test" -DexcludedGroups="performance"
    print_success "Fast tests completed"
}

# Function to run tests with coverage
run_coverage_tests() {
    print_info "Running tests with coverage report..."
    mvn clean test jacoco:report
    print_success "Tests with coverage completed"
    print_info "Coverage report available at: target/site/jacoco/index.html"
}

# Function to clean and run tests
run_clean_tests() {
    print_info "Cleaning project and running all tests..."
    mvn clean compile test
    print_success "Clean tests completed"
}

# Function to run full verification
run_verification() {
    print_info "Running full verification (clean, compile, test, verify)..."
    mvn clean compile test verify
    print_success "Full verification completed"
}

# Function to generate test report
generate_test_report() {
    print_info "Generating test reports..."
    mvn surefire-report:report
    print_success "Test report generated at: target/site/surefire-report.html"
}

# Main execution
main() {
    # Check if Maven is available
    check_maven
    
    # Check if we're in the right directory
    if [ ! -f "pom.xml" ]; then
        print_error "pom.xml not found. Please run this script from the project root directory."
        exit 1
    fi
    
    # Parse command line arguments
    case "${1:-}" in
        "all")
            run_all_tests
            ;;
        "basic")
            run_basic_tests
            ;;
        "features")
            run_feature_tests
            ;;
        "riskcontrol")
            run_riskcontrol_tests
            ;;
        "springboot")
            run_springboot_tests
            ;;
        "performance")
            run_performance_tests
            ;;
        "unit")
            run_unit_tests
            ;;
        "integration")
            run_integration_tests
            ;;
        "fast")
            run_fast_tests
            ;;
        "coverage")
            run_coverage_tests
            ;;
        "clean")
            run_clean_tests
            ;;
        "verify")
            run_verification
            ;;
        "report")
            generate_test_report
            ;;
        "help"|"-h"|"--help")
            show_usage
            ;;
        "")
            print_warning "No option specified. Running all tests..."
            run_all_tests
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
}

# Set up exit trap for cleanup
cleanup() {
    print_info "Cleaning up..."
}
trap cleanup EXIT

# Run main function
main "$@" 