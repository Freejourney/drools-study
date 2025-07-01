#!/bin/bash

# Drools Tutorial Project Setup Script
# This script sets up the development environment for the Drools tutorial project

set -e  # Exit on any error

echo "=== Drools Tutorial Project Setup ==="
echo "Setting up a comprehensive Drools tutorial project for risk control domain..."
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Check if Java 17 is installed
print_header "Checking Java Installation"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        print_status "Java $JAVA_VERSION is installed ✓"
    else
        print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
        echo "Please install JDK 17 or higher:"
        echo "  - Ubuntu/Debian: sudo apt install openjdk-17-jdk"
        echo "  - macOS: brew install openjdk@17"
        echo "  - Windows: Download from https://adoptium.net/"
        exit 1
    fi
else
    print_error "Java is not installed or not in PATH"
    exit 1
fi

# Check if Maven is installed
print_header "Checking Maven Installation"
if command -v mvn &> /dev/null; then
    MAVEN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
    print_status "Maven $MAVEN_VERSION is installed ✓"
else
    print_warning "Maven is not installed. Attempting to install..."
    
    # Try to install Maven based on OS
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        sudo apt update && sudo apt install maven -y
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        brew install maven
    else
        print_error "Please install Maven manually: https://maven.apache.org/install.html"
        exit 1
    fi
    
    if command -v mvn &> /dev/null; then
        print_status "Maven installed successfully ✓"
    else
        print_error "Failed to install Maven"
        exit 1
    fi
fi

# Check if Git is available
print_header "Checking Git Installation"
if command -v git &> /dev/null; then
    print_status "Git is available ✓"
else
    print_warning "Git is not installed. Some features may not work."
fi

# Project setup
print_header "Setting up Project"

# Make scripts executable
chmod +x scripts/*.sh
print_status "Made all scripts executable"

# Clean and compile the project
print_status "Cleaning and compiling the project..."
mvn clean compile

# Download dependencies
print_status "Downloading Maven dependencies..."
mvn dependency:resolve

# Run tests to verify setup
print_header "Running Basic Tests"
print_status "Running basic Drools tests to verify setup..."
mvn test -Dtest="DroolsBasicTest" -q

if [ $? -eq 0 ]; then
    print_status "Basic tests passed ✓"
else
    print_error "Some tests failed. Please check the output above."
fi

# Create IDE workspace files if needed
print_header "IDE Setup"
if [ ! -d ".vscode" ]; then
    mkdir -p .vscode
    cat > .vscode/settings.json << 'EOF'
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.configuration.maven.userSettings": null,
    "java.import.gradle.enabled": false,
    "java.import.maven.enabled": true,
    "files.exclude": {
        "**/target": true,
        "**/.classpath": true,
        "**/.project": true,
        "**/.settings": true,
        "**/.factorypath": true
    }
}
EOF
    print_status "Created VS Code configuration"
fi

# Display project information
print_header "Project Information"
echo "Project Name: Drools Tutorial - Risk Control Domain"
echo "Java Version: $(java -version 2>&1 | head -n 1)"
echo "Maven Version: $(mvn -version | head -n 1)"
echo "Project Structure:"
echo "  ├── src/main/java/          # Main source code"
echo "  ├── src/main/resources/     # Drools rules and configuration"
echo "  ├── src/test/java/          # Test cases"
echo "  ├── docs/                   # Documentation"
echo "  └── scripts/                # Utility scripts"
echo

print_header "Available Scripts"
echo "  ./scripts/run-tests.sh      # Run all tests with detailed output"
echo "  ./scripts/generate-docs.sh  # Generate project documentation"
echo "  mvn spring-boot:run         # Start the Spring Boot application"
echo "  mvn test                    # Run all tests"
echo "  mvn clean install           # Clean build and install"
echo

print_header "Quick Start Commands"
echo "1. Run all tests:"
echo "   ./scripts/run-tests.sh"
echo
echo "2. Start the application:"
echo "   mvn spring-boot:run"
echo
echo "3. Access REST APIs:"
echo "   http://localhost:8080/api/risk-control"
echo "   http://localhost:8080/api/transactions"
echo
echo "4. View API documentation:"
echo "   cat docs/api-documentation.md"
echo

print_header "Learning Path"
echo "1. Review basic concepts: docs/principles.md"
echo "2. Study common examples: src/test/java/com/drools/study/basic/"
echo "3. Explore advanced features: src/test/java/com/drools/study/features/"
echo "4. Try risk control scenarios: src/test/java/com/drools/study/riskcontrol/"
echo "5. Test Spring Boot integration: src/test/java/com/drools/study/springboot/"
echo

print_status "Setup completed successfully! 🎉"
print_status "You can now start exploring the Drools tutorial project."
echo
print_warning "Next steps:"
echo "1. Review the README.md file for detailed instructions"
echo "2. Explore the test cases to understand Drools features"
echo "3. Check the documentation in the docs/ directory"
echo "4. Run './scripts/run-tests.sh' to see all features in action" 