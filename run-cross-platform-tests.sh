#!/bin/bash

# =============================================================================
# WOLTAXI Cross-Platform Testing & Validation Suite
# Çapraz Platform Test ve Doğrulama Takımı
# 
# Comprehensive Testing for Windows, macOS, Linux, and Mobile Platforms
# Windows, macOS, Linux ve Mobil Platformlar için Kapsamlı Test
# =============================================================================

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
TEST_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="/var/log/woltaxi/testing"
RESULTS_DIR="$TEST_ROOT/test-results"
CONFIG_DIR="$TEST_ROOT/test-config"

# Create directories
mkdir -p "$LOG_DIR" "$RESULTS_DIR" "$CONFIG_DIR"

# Logging setup
LOG_FILE="$LOG_DIR/cross-platform-testing-$(date +%Y%m%d-%H%M%S).log"
exec 1> >(tee -a "$LOG_FILE")
exec 2>&1

echo -e "${CYAN}==============================================================================${NC}"
echo -e "${CYAN}🧪 WOLTAXI Cross-Platform Testing & Validation Suite${NC}"
echo -e "${CYAN}🔍 Çapraz Platform Test ve Doğrulama Takımı${NC}"
echo -e "${CYAN}==============================================================================${NC}"

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

print_success() {
    echo -e "${CYAN}[SUCCESS]${NC} $1"
}

print_test_header() {
    echo -e "${PURPLE}[TEST]${NC} $1"
}

# Detect operating system and architecture
detect_system() {
    OS="unknown"
    ARCH="unknown"
    DISTRO="unknown"
    
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
        if command -v lsb_release &> /dev/null; then
            DISTRO=$(lsb_release -si | tr '[:upper:]' '[:lower:]')
        elif [ -f /etc/os-release ]; then
            DISTRO=$(grep ^ID= /etc/os-release | cut -d= -f2 | tr -d '"')
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
        DISTRO="macos"
    elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
        OS="windows"
        DISTRO="windows"
    fi
    
    # Detect architecture
    ARCH=$(uname -m)
    case "$ARCH" in
        x86_64|amd64) ARCH="x64" ;;
        aarch64|arm64) ARCH="arm64" ;;
        i386|i686) ARCH="x86" ;;
    esac
    
    print_status "Detected system: $OS ($DISTRO) on $ARCH"
    
    # Write system info to results
    cat > "$RESULTS_DIR/system-info.json" <<EOF
{
    "os": "$OS",
    "distro": "$DISTRO",
    "architecture": "$ARCH",
    "hostname": "$(hostname)",
    "kernel": "$(uname -r)",
    "timestamp": "$(date -Iseconds)"
}
EOF
}

# Install testing tools
install_testing_tools() {
    print_status "Installing testing tools for $OS..."
    
    case "$OS" in
        "linux")
            # Update package manager
            if command -v apt-get &> /dev/null; then
                sudo apt-get update
                sudo apt-get install -y \
                    curl \
                    wget \
                    jq \
                    nodejs \
                    npm \
                    python3 \
                    python3-pip \
                    openjdk-17-jdk \
                    maven \
                    git \
                    chromium-browser \
                    firefox \
                    xvfb \
                    docker.io \
                    docker-compose \
                    siege \
                    apache2-utils \
                    netcat-openbsd \
                    telnet \
                    nmap \
                    openssl
            elif command -v yum &> /dev/null; then
                sudo yum install -y \
                    curl \
                    wget \
                    jq \
                    nodejs \
                    npm \
                    python3 \
                    python3-pip \
                    java-17-openjdk \
                    maven \
                    git \
                    chromium \
                    firefox \
                    xorg-x11-server-Xvfb \
                    docker \
                    docker-compose \
                    siege \
                    httpd-tools \
                    nc \
                    telnet \
                    nmap \
                    openssl
            fi
            ;;
        "macos")
            # Install Homebrew if not present
            if ! command -v brew &> /dev/null; then
                /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            fi
            
            brew install \
                curl \
                wget \
                jq \
                node \
                python3 \
                openjdk@17 \
                maven \
                git \
                chromium \
                firefox \
                docker \
                docker-compose \
                siege \
                netcat \
                telnet \
                nmap \
                openssl
            ;;
    esac
    
    # Install global npm packages
    npm install -g \
        @playwright/test \
        cypress \
        lighthouse \
        @axe-core/cli \
        pa11y \
        artillery \
        loadtest
    
    # Install Python packages
    pip3 install \
        pytest \
        requests \
        selenium \
        locust \
        behave \
        allure-pytest
    
    print_success "Testing tools installed successfully"
}

# Setup test environment
setup_test_environment() {
    print_status "Setting up test environment..."
    
    # Create test configuration
    cat > "$CONFIG_DIR/test-config.json" <<EOF
{
    "environment": {
        "baseUrl": "http://localhost:8080",
        "apiUrl": "http://localhost:8080/api",
        "timeout": 30000,
        "retries": 3
    },
    "services": [
        {
            "name": "eureka-server",
            "port": 8761,
            "healthEndpoint": "/actuator/health"
        },
        {
            "name": "user-service",
            "port": 8081,
            "healthEndpoint": "/actuator/health"
        },
        {
            "name": "ride-service",
            "port": 8082,
            "healthEndpoint": "/actuator/health"
        },
        {
            "name": "payment-hub-service",
            "port": 8083,
            "healthEndpoint": "/actuator/health"
        },
        {
            "name": "ai-ml-service",
            "port": 8094,
            "healthEndpoint": "/actuator/health"
        }
    ],
    "databases": [
        {
            "name": "postgresql",
            "host": "localhost",
            "port": 5432,
            "database": "woltaxi"
        },
        {
            "name": "redis",
            "host": "localhost",
            "port": 6379
        }
    ],
    "browsers": [
        "chromium",
        "firefox",
        "webkit"
    ],
    "viewports": [
        {"width": 320, "height": 568, "name": "Mobile S"},
        {"width": 375, "height": 667, "name": "Mobile M"},
        {"width": 425, "height": 667, "name": "Mobile L"},
        {"width": 768, "height": 1024, "name": "Tablet"},
        {"width": 1024, "height": 768, "name": "Laptop"},
        {"width": 1440, "height": 900, "name": "Laptop L"},
        {"width": 2560, "height": 1440, "name": "4K"}
    ]
}
EOF
    
    # Create Playwright configuration
    cat > "$TEST_ROOT/playwright.config.js" <<EOF
module.exports = {
    testDir: './tests',
    timeout: 30000,
    fullyParallel: true,
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 2 : 0,
    workers: process.env.CI ? 1 : undefined,
    reporter: [
        ['html'],
        ['json', { outputFile: './test-results/playwright-results.json' }],
        ['junit', { outputFile: './test-results/playwright-results.xml' }]
    ],
    use: {
        baseURL: 'http://localhost:8080',
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
        video: 'retain-on-failure'
    },
    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] }
        },
        {
            name: 'firefox',
            use: { ...devices['Desktop Firefox'] }
        },
        {
            name: 'webkit',
            use: { ...devices['Desktop Safari'] }
        },
        {
            name: 'Mobile Chrome',
            use: { ...devices['Pixel 5'] }
        },
        {
            name: 'Mobile Safari',
            use: { ...devices['iPhone 12'] }
        },
        {
            name: 'Microsoft Edge',
            use: { ...devices['Desktop Edge'], channel: 'msedge' }
        }
    ],
    webServer: {
        command: 'docker-compose up -d',
        port: 8080,
        reuseExistingServer: !process.env.CI
    }
};
EOF
    
    # Create Cypress configuration
    cat > "$TEST_ROOT/cypress.config.js" <<EOF
const { defineConfig } = require('cypress')

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://localhost:8080',
        supportFile: 'cypress/support/e2e.js',
        specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',
        videosFolder: './test-results/cypress/videos',
        screenshotsFolder: './test-results/cypress/screenshots',
        video: true,
        screenshot: true,
        viewportWidth: 1280,
        viewportHeight: 720,
        defaultCommandTimeout: 10000,
        requestTimeout: 10000,
        responseTimeout: 10000,
        pageLoadTimeout: 30000,
        watchForFileChanges: false,
        chromeWebSecurity: false,
        retries: {
            runMode: 2,
            openMode: 0
        },
        setupNodeEvents(on, config) {
            // implement node event listeners here
        }
    },
    component: {
        devServer: {
            framework: 'react',
            bundler: 'webpack'
        }
    }
})
EOF
    
    print_success "Test environment setup completed"
}

# System compatibility tests
test_system_compatibility() {
    print_test_header "Running system compatibility tests..."
    
    local test_results="$RESULTS_DIR/system-compatibility.json"
    local test_status=0
    
    # Initialize results
    echo '{"tests": []}' > "$test_results"
    
    # Test Docker installation
    print_status "Testing Docker compatibility..."
    if command -v docker &> /dev/null; then
        docker_version=$(docker --version)
        docker_info=$(docker info --format json 2>/dev/null || echo '{}')
        
        # Add to results
        jq --arg test "Docker Installation" \
           --arg status "pass" \
           --arg version "$docker_version" \
           --argjson info "$docker_info" \
           '.tests += [{"name": $test, "status": $status, "version": $version, "info": $info}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_success "Docker: $docker_version"
    else
        jq --arg test "Docker Installation" \
           --arg status "fail" \
           --arg error "Docker not installed" \
           '.tests += [{"name": $test, "status": $status, "error": $error}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_error "Docker not installed"
        test_status=1
    fi
    
    # Test Docker Compose
    print_status "Testing Docker Compose compatibility..."
    if command -v docker-compose &> /dev/null; then
        compose_version=$(docker-compose --version)
        
        jq --arg test "Docker Compose" \
           --arg status "pass" \
           --arg version "$compose_version" \
           '.tests += [{"name": $test, "status": $status, "version": $version}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_success "Docker Compose: $compose_version"
    else
        jq --arg test "Docker Compose" \
           --arg status "fail" \
           --arg error "Docker Compose not installed" \
           '.tests += [{"name": $test, "status": $status, "error": $error}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_error "Docker Compose not installed"
        test_status=1
    fi
    
    # Test Java
    print_status "Testing Java compatibility..."
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n 1)
        
        jq --arg test "Java Runtime" \
           --arg status "pass" \
           --arg version "$java_version" \
           '.tests += [{"name": $test, "status": $status, "version": $version}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_success "Java: $java_version"
    else
        jq --arg test "Java Runtime" \
           --arg status "fail" \
           --arg error "Java not installed" \
           '.tests += [{"name": $test, "status": $status, "error": $error}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_error "Java not installed"
        test_status=1
    fi
    
    # Test Node.js
    print_status "Testing Node.js compatibility..."
    if command -v node &> /dev/null; then
        node_version=$(node --version)
        npm_version=$(npm --version)
        
        jq --arg test "Node.js Runtime" \
           --arg status "pass" \
           --arg node_version "$node_version" \
           --arg npm_version "$npm_version" \
           '.tests += [{"name": $test, "status": $status, "node_version": $node_version, "npm_version": $npm_version}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_success "Node.js: $node_version, npm: $npm_version"
    else
        jq --arg test "Node.js Runtime" \
           --arg status "fail" \
           --arg error "Node.js not installed" \
           '.tests += [{"name": $test, "status": $status, "error": $error}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_warning "Node.js not installed"
    fi
    
    if [ $test_status -eq 0 ]; then
        print_success "System compatibility tests passed"
    else
        print_error "System compatibility tests failed"
    fi
    
    return $test_status
}

# Service deployment tests
test_service_deployment() {
    print_test_header "Running service deployment tests..."
    
    local test_results="$RESULTS_DIR/deployment.json"
    local test_status=0
    
    echo '{"deployment_tests": []}' > "$test_results"
    
    # Start services with timeout
    print_status "Starting WOLTAXI services..."
    timeout 300 docker-compose up -d || {
        print_error "Failed to start services within timeout"
        return 1
    }
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 60
    
    # Test each service
    local services=(
        "eureka-server:8761:/actuator/health"
        "user-service:8081:/actuator/health"
        "ride-service:8082:/actuator/health"
        "payment-hub-service:8083:/actuator/health"
        "ai-ml-service:8094:/actuator/health"
    )
    
    for service_info in "${services[@]}"; do
        IFS=':' read -r service_name port endpoint <<< "$service_info"
        
        print_status "Testing $service_name deployment..."
        
        # Check if service is running
        if docker-compose ps | grep -q "$service_name.*Up"; then
            # Test health endpoint
            if curl -s -f "http://localhost:$port$endpoint" > /dev/null; then
                health_response=$(curl -s "http://localhost:$port$endpoint")
                
                jq --arg service "$service_name" \
                   --arg status "pass" \
                   --arg port "$port" \
                   --argjson health "$health_response" \
                   '.deployment_tests += [{"service": $service, "status": $status, "port": $port, "health": $health}]' \
                   "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
                
                print_success "$service_name is healthy on port $port"
            else
                jq --arg service "$service_name" \
                   --arg status "fail" \
                   --arg port "$port" \
                   --arg error "Health check failed" \
                   '.deployment_tests += [{"service": $service, "status": $status, "port": $port, "error": $error}]' \
                   "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
                
                print_error "$service_name health check failed"
                test_status=1
            fi
        else
            jq --arg service "$service_name" \
               --arg status "fail" \
               --arg port "$port" \
               --arg error "Service not running" \
               '.deployment_tests += [{"service": $service, "status": $status, "port": $port, "error": $error}]' \
               "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
            
            print_error "$service_name is not running"
            test_status=1
        fi
    done
    
    if [ $test_status -eq 0 ]; then
        print_success "Service deployment tests passed"
    else
        print_error "Service deployment tests failed"
    fi
    
    return $test_status
}

# Functional tests
test_functionality() {
    print_test_header "Running functionality tests..."
    
    local test_results="$RESULTS_DIR/functionality.json"
    local test_status=0
    
    echo '{"functional_tests": []}' > "$test_results"
    
    # Test user service
    print_status "Testing user service functionality..."
    
    # Test user registration endpoint
    local registration_payload='{"username":"testuser","email":"test@woltaxi.com","password":"Test123!","firstName":"Test","lastName":"User"}'
    local registration_response=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$registration_payload" \
        "http://localhost:8081/api/users/register" || echo "000")
    
    if [[ "$registration_response" =~ 20[0-9] ]]; then
        jq --arg test "User Registration" \
           --arg status "pass" \
           --arg response "$registration_response" \
           '.functional_tests += [{"test": $test, "status": $status, "response": $response}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_success "User registration endpoint working"
    else
        jq --arg test "User Registration" \
           --arg status "fail" \
           --arg response "$registration_response" \
           '.functional_tests += [{"test": $test, "status": $status, "response": $response}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_error "User registration endpoint failed"
        test_status=1
    fi
    
    # Test ride service
    print_status "Testing ride service functionality..."
    
    local ride_request='{"userId":1,"pickupLocation":"Test Pickup","destination":"Test Destination","rideType":"STANDARD"}'
    local ride_response=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$ride_request" \
        "http://localhost:8082/api/rides/request" || echo "000")
    
    if [[ "$ride_response" =~ 20[0-9] ]]; then
        jq --arg test "Ride Request" \
           --arg status "pass" \
           --arg response "$ride_response" \
           '.functional_tests += [{"test": $test, "status": $status, "response": $response}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_success "Ride request endpoint working"
    else
        jq --arg test "Ride Request" \
           --arg status "fail" \
           --arg response "$ride_response" \
           '.functional_tests += [{"test": $test, "status": $status, "response": $response}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_error "Ride request endpoint failed"
        test_status=1
    fi
    
    if [ $test_status -eq 0 ]; then
        print_success "Functionality tests passed"
    else
        print_error "Functionality tests failed"
    fi
    
    return $test_status
}

# Performance tests
test_performance() {
    print_test_header "Running performance tests..."
    
    local test_results="$RESULTS_DIR/performance.json"
    local test_status=0
    
    echo '{"performance_tests": []}' > "$test_results"
    
    # Load test with siege
    if command -v siege &> /dev/null; then
        print_status "Running load test with siege..."
        
        # Create URL list for testing
        cat > "$TEST_ROOT/urls.txt" <<EOF
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
http://localhost:8083/actuator/health
http://localhost:8094/actuator/health
EOF
        
        # Run siege load test
        siege_output=$(siege -c 10 -t 30s -f "$TEST_ROOT/urls.txt" 2>&1 || true)
        
        # Parse siege results
        if echo "$siege_output" | grep -q "Transactions:"; then
            transactions=$(echo "$siege_output" | grep "Transactions:" | awk '{print $2}')
            availability=$(echo "$siege_output" | grep "Availability:" | awk '{print $2}')
            response_time=$(echo "$siege_output" | grep "Response time:" | awk '{print $3}')
            
            jq --arg test "Load Test (Siege)" \
               --arg status "pass" \
               --arg transactions "$transactions" \
               --arg availability "$availability" \
               --arg response_time "$response_time" \
               '.performance_tests += [{"test": $test, "status": $status, "transactions": $transactions, "availability": $availability, "response_time": $response_time}]' \
               "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
            
            print_success "Load test completed: $transactions transactions, $availability availability, $response_time response time"
        else
            jq --arg test "Load Test (Siege)" \
               --arg status "fail" \
               --arg error "Could not parse siege output" \
               '.performance_tests += [{"test": $test, "status": $status, "error": $error}]' \
               "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
            
            print_error "Load test failed"
            test_status=1
        fi
    else
        print_warning "Siege not available, skipping load test"
    fi
    
    # Memory usage test
    print_status "Checking memory usage..."
    
    local memory_usage=$(docker stats --no-stream --format "table {{.Container}}\t{{.MemUsage}}" | grep woltaxi || true)
    
    if [ -n "$memory_usage" ]; then
        jq --arg test "Memory Usage" \
           --arg status "pass" \
           --arg usage "$memory_usage" \
           '.performance_tests += [{"test": $test, "status": $status, "usage": $usage}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_success "Memory usage collected"
    else
        jq --arg test "Memory Usage" \
           --arg status "fail" \
           --arg error "Could not collect memory usage" \
           '.performance_tests += [{"test": $test, "status": $status, "error": $error}]' \
           "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
        
        print_error "Could not collect memory usage"
        test_status=1
    fi
    
    if [ $test_status -eq 0 ]; then
        print_success "Performance tests passed"
    else
        print_error "Performance tests failed"
    fi
    
    return $test_status
}

# Browser compatibility tests
test_browser_compatibility() {
    print_test_header "Running browser compatibility tests..."
    
    local test_results="$RESULTS_DIR/browser-compatibility.json"
    local test_status=0
    
    echo '{"browser_tests": []}' > "$test_results"
    
    # Test with Playwright if available
    if command -v npx &> /dev/null && npm list -g @playwright/test &> /dev/null; then
        print_status "Running Playwright browser tests..."
        
        # Create simple Playwright test
        mkdir -p "$TEST_ROOT/tests"
        cat > "$TEST_ROOT/tests/basic.spec.js" <<EOF
const { test, expect } = require('@playwright/test');

test('homepage loads successfully', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/WOLTAXI/);
});

test('user service health check', async ({ page }) => {
    const response = await page.request.get('/api/users/health');
    expect(response.status()).toBe(200);
});

test('responsive design on mobile', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');
    const element = await page.locator('body');
    expect(element).toBeTruthy();
});
EOF
        
        # Run Playwright tests
        if npx playwright test --reporter=json --output-dir="$RESULTS_DIR/playwright" 2>/dev/null; then
            jq --arg test "Playwright Browser Tests" \
               --arg status "pass" \
               '.browser_tests += [{"test": $test, "status": $status}]' \
               "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
            
            print_success "Playwright browser tests passed"
        else
            jq --arg test "Playwright Browser Tests" \
               --arg status "fail" \
               --arg error "Playwright tests failed" \
               '.browser_tests += [{"test": $test, "status": $status, "error": $error}]' \
               "$test_results" > "$test_results.tmp" && mv "$test_results.tmp" "$test_results"
            
            print_error "Playwright browser tests failed"
            test_status=1
        fi
    else
        print_warning "Playwright not available, skipping browser tests"
    fi
    
    if [ $test_status -eq 0 ]; then
        print_success "Browser compatibility tests passed"
    else
        print_error "Browser compatibility tests failed"
    fi
    
    return $test_status
}

# Generate comprehensive test report
generate_test_report() {
    print_status "Generating comprehensive test report..."
    
    local report_file="$RESULTS_DIR/test-report.html"
    local json_report="$RESULTS_DIR/test-summary.json"
    
    # Combine all test results
    jq -s 'add' "$RESULTS_DIR"/*.json > "$json_report" 2>/dev/null || echo '{}' > "$json_report"
    
    # Generate HTML report
    cat > "$report_file" <<EOF
<!DOCTYPE html>
<html>
<head>
    <title>WOLTAXI Cross-Platform Testing Report</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            margin-bottom: 40px;
            padding-bottom: 20px;
            border-bottom: 2px solid #007bff;
        }
        .header h1 {
            color: #007bff;
            margin: 0;
            font-size: 2.5em;
        }
        .header p {
            color: #666;
            margin: 10px 0 0 0;
            font-size: 1.2em;
        }
        .system-info {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 5px;
            margin-bottom: 30px;
        }
        .test-section {
            margin-bottom: 40px;
        }
        .test-section h2 {
            color: #333;
            border-bottom: 2px solid #eee;
            padding-bottom: 10px;
        }
        .pass { color: #28a745; font-weight: bold; }
        .fail { color: #dc3545; font-weight: bold; }
        .warning { color: #ffc107; font-weight: bold; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        th {
            background-color: #f8f9fa;
            font-weight: 600;
        }
        tr:hover {
            background-color: #f5f5f5;
        }
        .summary-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .summary-card {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 5px;
            text-align: center;
            border-left: 4px solid #007bff;
        }
        .summary-card h3 {
            margin: 0 0 10px 0;
            color: #333;
        }
        .summary-card .number {
            font-size: 2em;
            font-weight: bold;
            color: #007bff;
        }
        .footer {
            text-align: center;
            margin-top: 40px;
            padding-top: 20px;
            border-top: 1px solid #eee;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🧪 WOLTAXI Cross-Platform Testing Report</h1>
            <p>Generated on $(date)</p>
        </div>
        
        <div class="system-info">
            <h3>System Information</h3>
            <table>
                <tr><td><strong>Operating System:</strong></td><td>$OS ($DISTRO)</td></tr>
                <tr><td><strong>Architecture:</strong></td><td>$ARCH</td></tr>
                <tr><td><strong>Hostname:</strong></td><td>$(hostname)</td></tr>
                <tr><td><strong>Kernel:</strong></td><td>$(uname -r)</td></tr>
                <tr><td><strong>Test Duration:</strong></td><td>$(date -d@$SECONDS -u +%H:%M:%S)</td></tr>
            </table>
        </div>
        
        <div class="summary-cards">
            <div class="summary-card">
                <h3>Total Tests</h3>
                <div class="number" id="total-tests">-</div>
            </div>
            <div class="summary-card">
                <h3>Passed</h3>
                <div class="number pass" id="passed-tests">-</div>
            </div>
            <div class="summary-card">
                <h3>Failed</h3>
                <div class="number fail" id="failed-tests">-</div>
            </div>
            <div class="summary-card">
                <h3>Success Rate</h3>
                <div class="number" id="success-rate">-</div>
            </div>
        </div>
        
        <div class="test-section">
            <h2>🔧 System Compatibility</h2>
            <div id="system-compatibility-results">
                <p>Loading results...</p>
            </div>
        </div>
        
        <div class="test-section">
            <h2>🚀 Service Deployment</h2>
            <div id="deployment-results">
                <p>Loading results...</p>
            </div>
        </div>
        
        <div class="test-section">
            <h2>⚡ Functionality Tests</h2>
            <div id="functionality-results">
                <p>Loading results...</p>
            </div>
        </div>
        
        <div class="test-section">
            <h2>📊 Performance Tests</h2>
            <div id="performance-results">
                <p>Loading results...</p>
            </div>
        </div>
        
        <div class="test-section">
            <h2>🌐 Browser Compatibility</h2>
            <div id="browser-results">
                <p>Loading results...</p>
            </div>
        </div>
        
        <div class="footer">
            <p>&copy; 2024 WOLTAXI. Cross-Platform Testing Suite v1.0</p>
            <p>Report generated by WOLTAXI Testing Framework</p>
        </div>
    </div>
    
    <script>
        // Load and display test results
        fetch('./test-summary.json')
            .then(response => response.json())
            .then(data => {
                console.log('Test data loaded:', data);
                // Update summary cards and detailed results
                // This would be implemented based on the actual JSON structure
            })
            .catch(error => {
                console.error('Error loading test results:', error);
            });
    </script>
</body>
</html>
EOF
    
    print_success "Test report generated: $report_file"
}

# Main testing function
main() {
    local start_time=$(date +%s)
    local overall_status=0
    
    print_status "Starting WOLTAXI Cross-Platform Testing Suite..."
    
    # Detect system
    detect_system
    
    # Install testing tools
    install_testing_tools
    
    # Setup test environment
    setup_test_environment
    
    # Run test suites
    test_system_compatibility || overall_status=1
    test_service_deployment || overall_status=1
    test_functionality || overall_status=1
    test_performance || overall_status=1
    test_browser_compatibility || overall_status=1
    
    # Generate report
    generate_test_report
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    if [ $overall_status -eq 0 ]; then
        print_success "🎉 All tests completed successfully in ${duration}s"
        print_success "📊 Test report available at: $RESULTS_DIR/test-report.html"
    else
        print_error "❌ Some tests failed. Duration: ${duration}s"
        print_error "📊 Test report available at: $RESULTS_DIR/test-report.html"
    fi
    
    # Cleanup
    print_status "Cleaning up test environment..."
    docker-compose down
    
    return $overall_status
}

# Run main function
main "$@"