# =============================================================================
# WOLTAXI Cross-Platform Deployment Script for Windows PowerShell
# Windows PowerShell için Çapraz Platform Deployment Script'i
# 
# Supports: Windows 10/11, Windows Server, WSL2, Docker Desktop
# Desteklenen: Windows 10/11, Windows Server, WSL2, Docker Desktop
# =============================================================================

param(
    [string]$Action = "deploy",
    [switch]$SkipHealthCheck = $false,
    [switch]$Verbose = $false
)

# Set error action preference
$ErrorActionPreference = "Stop"

# Project information
$ProjectName = "WOLTAXI"
$Version = "1.0.0"
$BuildDate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# Colors for output (Windows PowerShell compatible)
function Write-ColoredOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    
    switch ($Color) {
        "Red" { Write-Host $Message -ForegroundColor Red }
        "Green" { Write-Host $Message -ForegroundColor Green }
        "Yellow" { Write-Host $Message -ForegroundColor Yellow }
        "Blue" { Write-Host $Message -ForegroundColor Blue }
        "Cyan" { Write-Host $Message -ForegroundColor Cyan }
        "Magenta" { Write-Host $Message -ForegroundColor Magenta }
        default { Write-Host $Message -ForegroundColor White }
    }
}

# Platform detection
function Get-PlatformInfo {
    $Platform = "Windows"
    $Architecture = if ([Environment]::Is64BitOperatingSystem) { "amd64" } else { "386" }
    $WindowsVersion = (Get-WmiObject -Class Win32_OperatingSystem).Caption
    $PowerShellVersion = $PSVersionTable.PSVersion.ToString()
    
    Write-ColoredOutput "Detected Platform: $Platform ($Architecture)" "Blue"
    Write-ColoredOutput "Windows Version: $WindowsVersion" "Cyan"
    Write-ColoredOutput "PowerShell Version: $PowerShellVersion" "Cyan"
    
    return @{
        Platform = $Platform
        Architecture = $Architecture
        WindowsVersion = $WindowsVersion
        PowerShellVersion = $PowerShellVersion
    }
}

# Check prerequisites
function Test-Prerequisites {
    Write-ColoredOutput "Checking prerequisites..." "Blue"
    
    $Prerequisites = @()
    
    # Check Docker
    try {
        $dockerVersion = docker --version
        Write-ColoredOutput "✓ Docker detected: $dockerVersion" "Green"
        $Prerequisites += "Docker"
    }
    catch {
        Write-ColoredOutput "✗ Docker is not installed or not in PATH" "Red"
        Write-ColoredOutput "Please install Docker Desktop for Windows: https://docs.docker.com/desktop/windows/install/" "Yellow"
        throw "Docker is required but not found"
    }
    
    # Check Docker Compose
    try {
        $composeVersion = docker-compose --version 2>$null
        if (-not $composeVersion) {
            $composeVersion = docker compose version
        }
        Write-ColoredOutput "✓ Docker Compose detected: $composeVersion" "Green"
        $Prerequisites += "Docker Compose"
    }
    catch {
        Write-ColoredOutput "✗ Docker Compose is not installed" "Red"
        throw "Docker Compose is required but not found"
    }
    
    # Check Git
    try {
        $gitVersion = git --version
        Write-ColoredOutput "✓ Git detected: $gitVersion" "Green"
        $Prerequisites += "Git"
    }
    catch {
        Write-ColoredOutput "✗ Git is not installed or not in PATH" "Red"
        Write-ColoredOutput "Please install Git for Windows: https://git-scm.com/download/win" "Yellow"
        Write-ColoredOutput "Git is recommended but not required for deployment" "Yellow"
    }
    
    # Check Java (optional)
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
        Write-ColoredOutput "✓ Java detected: $javaVersion" "Green"
        $Prerequisites += "Java"
    }
    catch {
        Write-ColoredOutput "⚠ Java not detected. Using Docker containers for Java applications." "Yellow"
    }
    
    # Check available disk space
    $disk = Get-WmiObject -Class Win32_LogicalDisk | Where-Object {$_.DeviceID -eq "C:"}
    $freeSpaceGB = [math]::Round($disk.FreeSpace / 1GB, 2)
    if ($freeSpaceGB -gt 10) {
        Write-ColoredOutput "✓ Available disk space: $freeSpaceGB GB" "Green"
    } else {
        Write-ColoredOutput "⚠ Low disk space: $freeSpaceGB GB (Recommended: >10GB)" "Yellow"
    }
    
    # Check available memory
    $memory = Get-WmiObject -Class Win32_ComputerSystem
    $totalMemoryGB = [math]::Round($memory.TotalPhysicalMemory / 1GB, 2)
    if ($totalMemoryGB -gt 8) {
        Write-ColoredOutput "✓ Available memory: $totalMemoryGB GB" "Green"
    } else {
        Write-ColoredOutput "⚠ Limited memory: $totalMemoryGB GB (Recommended: >8GB)" "Yellow"
    }
    
    Write-ColoredOutput "Prerequisites check completed!" "Green"
    return $Prerequisites
}

# Create necessary directories
function New-ProjectDirectories {
    Write-ColoredOutput "Creating necessary directories..." "Blue"
    
    $directories = @(
        "logs\eureka-server", "logs\api-gateway", "logs\user-service", "logs\driver-service",
        "logs\ride-service", "logs\payment-service", "logs\location-service", "logs\notification-service",
        "logs\subscription-service", "logs\marketing-service", "logs\analytics-service",
        "logs\global-performance-service", "logs\emergency-service", "logs\travel-integration-service",
        "logs\wolkurye-service", "logs\ai-ml-service",
        
        "uploads\user-profiles", "uploads\driver-documents", "uploads\ride-attachments",
        "uploads\emergency-files", "uploads\travel-documents", "uploads\wolkurye-packages",
        "uploads\ai-ml-models",
        
        "credentials", "database\init", "backups", "monitoring", "ssl",
        
        "ai-ml-service\models", "ai-ml-service\cache", "ai-ml-service\temp", "ai-ml-service\logs",
        "ai-ml-service\models\tensorflow", "ai-ml-service\models\pytorch", 
        "ai-ml-service\models\sklearn", "ai-ml-service\models\weka", "ai-ml-service\models\custom"
    )
    
    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-ColoredOutput "Created directory: $dir" "Cyan"
        }
    }
    
    Write-ColoredOutput "Directories created successfully!" "Green"
}

# Generate environment file
function New-EnvironmentFile {
    Write-ColoredOutput "Generating environment configuration..." "Blue"
    
    if (-not (Test-Path ".env")) {
        $envContent = @"
# =============================================================================
# WOLTAXI Environment Configuration for Windows
# Windows için WOLTAXI Ortam Yapılandırması
# =============================================================================

# Database Configuration
POSTGRES_DB=woltaxi
POSTGRES_USER=woltaxi_user
POSTGRES_PASSWORD=woltaxi_2024
DB_HOST=postgres
DB_PORT=5432

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=woltaxi_redis_2024

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# JWT Secrets (Generate your own in production)
JWT_SECRET_USER=woltaxi-user-secret-2024
JWT_SECRET_DRIVER=woltaxi-driver-secret-2024
JWT_SECRET_PAYMENT=woltaxi-payment-secret-2024
JWT_SECRET_GATEWAY=woltaxi-gateway-secret-2024
JWT_SECRET_AIML=woltaxi-aiml-secret-2024

# Third Party API Keys (Replace with your actual keys)
GOOGLE_MAPS_API_KEY=your-google-maps-api-key
GOOGLE_CLOUD_PROJECT_ID=woltaxi-ai-ml
GOOGLE_VISION_API_KEY=your-google-vision-api-key
GOOGLE_TRANSLATE_API_KEY=your-google-translate-api-key

# OpenAI Configuration
OPENAI_API_KEY=your-openai-api-key
OPENAI_MODEL=gpt-4

# Azure Cognitive Services
AZURE_COGNITIVE_SERVICES_KEY=your-azure-cognitive-key
AZURE_REGION=eastus
AZURE_COGNITIVE_SERVICES_ENDPOINT=your-azure-endpoint

# Hugging Face
HUGGINGFACE_API_KEY=your-huggingface-api-key

# Payment Gateways
STRIPE_PUBLISHABLE_KEY=pk_test_your-stripe-publishable-key
STRIPE_SECRET_KEY=sk_test_your-stripe-secret-key
PAYPAL_CLIENT_ID=your-paypal-client-id
PAYPAL_CLIENT_SECRET=your-paypal-client-secret
IYZICO_API_KEY=your-iyzico-api-key
IYZICO_SECRET_KEY=your-iyzico-secret-key

# Communication Services
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_FROM_NUMBER=+90XXXXXXXXX

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-email-password

# Firebase
FIREBASE_PROJECT_ID=woltaxi-app
FIREBASE_CREDENTIALS_PATH=firebase-adminsdk.json

# Security Configuration
AI_ML_ENCRYPTION_KEY=woltaxi-aiml-encryption-2024
JASYPT_ENCRYPTOR_PASSWORD=woltaxi-master-encryption-2024

# Windows Specific Settings
COMPOSE_PATH_SEPARATOR=;
COMPOSE_CONVERT_WINDOWS_PATHS=1
DOCKER_BUILDKIT=1
COMPOSE_DOCKER_CLI_BUILD=1

# Performance Settings
JAVA_OPTS_SMALL=-Xmx512m -Xms256m
JAVA_OPTS_MEDIUM=-Xmx1024m -Xms512m
JAVA_OPTS_LARGE=-Xmx2048m -Xms1024m
JAVA_OPTS_XLARGE=-Xmx3072m -Xms1536m

# Platform Information
PLATFORM=Windows
ARCHITECTURE=amd64
BUILD_DATE=$BuildDate
VERSION=$Version
"@
        
        $envContent | Out-File -FilePath ".env" -Encoding UTF8
        Write-ColoredOutput "Environment file (.env) created successfully!" "Green"
    } else {
        Write-ColoredOutput "Environment file (.env) already exists. Skipping creation." "Yellow"
    }
}

# Deploy services
function Start-WoltaxiServices {
    Write-ColoredOutput "Building and deploying WOLTAXI services..." "Blue"
    
    # Stop existing services
    Write-ColoredOutput "Stopping existing services..." "Yellow"
    try {
        docker-compose down --remove-orphans 2>$null
    } catch {
        try {
            docker compose down --remove-orphans 2>$null
        } catch {
            Write-ColoredOutput "No existing services to stop" "Cyan"
        }
    }
    
    # Determine compose command
    $ComposeCmd = "docker-compose"
    try {
        docker-compose version | Out-Null
    } catch {
        $ComposeCmd = "docker compose"
    }
    
    Write-ColoredOutput "Using compose command: $ComposeCmd" "Cyan"
    
    # Build services
    Write-ColoredOutput "Building services for Windows..." "Yellow"
    & $ComposeCmd build --parallel
    
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to build services"
    }
    
    # Start infrastructure services
    Write-ColoredOutput "Starting infrastructure services..." "Yellow"
    & $ComposeCmd up -d postgres redis kafka zookeeper
    
    # Wait for infrastructure
    Write-ColoredOutput "Waiting for infrastructure services..." "Yellow"
    Start-Sleep -Seconds 30
    
    # Start discovery service
    & $ComposeCmd up -d eureka-server
    Start-Sleep -Seconds 20
    
    # Start core services
    Write-ColoredOutput "Starting core services..." "Yellow"
    & $ComposeCmd up -d api-gateway user-service driver-service ride-service payment-service
    Start-Sleep -Seconds 15
    
    # Start additional services
    Write-ColoredOutput "Starting additional services..." "Yellow"
    & $ComposeCmd up -d location-service notification-service subscription-service
    Start-Sleep -Seconds 10
    
    # Start business services
    Write-ColoredOutput "Starting business services..." "Yellow"
    & $ComposeCmd up -d marketing-service analytics-service global-performance-service
    Start-Sleep -Seconds 10
    
    # Start specialized services
    Write-ColoredOutput "Starting specialized services..." "Yellow"
    & $ComposeCmd up -d emergency-service travel-integration-service wolkurye-service
    Start-Sleep -Seconds 15
    
    # Start AI/ML service
    Write-ColoredOutput "Starting AI/ML service..." "Yellow"
    & $ComposeCmd up -d ai-ml-service
    Start-Sleep -Seconds 30
    
    Write-ColoredOutput "All services started successfully!" "Green"
}

# Health check
function Test-ServicesHealth {
    if ($SkipHealthCheck) {
        Write-ColoredOutput "Skipping health check as requested" "Yellow"
        return $true
    }
    
    Write-ColoredOutput "Performing health check..." "Blue"
    
    $services = @(
        @{Name="eureka-server"; Port=8761},
        @{Name="api-gateway"; Port=8765},
        @{Name="user-service"; Port=8081},
        @{Name="driver-service"; Port=8082},
        @{Name="ride-service"; Port=8083},
        @{Name="payment-service"; Port=8084},
        @{Name="location-service"; Port=8085},
        @{Name="notification-service"; Port=8086},
        @{Name="subscription-service"; Port=8087},
        @{Name="marketing-service"; Port=8088},
        @{Name="analytics-service"; Port=8089},
        @{Name="global-performance-service"; Port=8090},
        @{Name="emergency-service"; Port=8091},
        @{Name="travel-integration-service"; Port=8092},
        @{Name="wolkurye-service"; Port=8093},
        @{Name="ai-ml-service"; Port=8094}
    )
    
    $healthyCount = 0
    $totalCount = $services.Count
    
    foreach ($service in $services) {
        $url = "http://localhost:$($service.Port)/actuator/health"
        try {
            $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-ColoredOutput "✓ $($service.Name) is healthy" "Green"
                $healthyCount++
            } else {
                Write-ColoredOutput "✗ $($service.Name) returned status $($response.StatusCode)" "Red"
            }
        }
        catch {
            Write-ColoredOutput "✗ $($service.Name) is not responding" "Red"
        }
    }
    
    Write-ColoredOutput "`nHealth Check Summary:" "Blue"
    Write-ColoredOutput "Healthy services: $healthyCount/$totalCount" "Cyan"
    
    if ($healthyCount -eq $totalCount) {
        Write-ColoredOutput "🎉 All services are healthy!" "Green"
        return $true
    } else {
        Write-ColoredOutput "⚠️ Some services are not healthy. Check logs for details." "Yellow"
        return $false
    }
}

# Show service URLs
function Show-ServiceUrls {
    Write-ColoredOutput "`n=== WOLTAXI Service URLs ===" "Blue"
    Write-ColoredOutput "🌐 API Gateway: http://localhost:8765" "Green"
    Write-ColoredOutput "🔍 Eureka Dashboard: http://localhost:8761" "Green"
    Write-ColoredOutput "👥 User Service: http://localhost:8081" "Green"
    Write-ColoredOutput "🚗 Driver Service: http://localhost:8082" "Green"
    Write-ColoredOutput "🛣️ Ride Service: http://localhost:8083" "Green"
    Write-ColoredOutput "💳 Payment Service: http://localhost:8084" "Green"
    Write-ColoredOutput "📍 Location Service: http://localhost:8085" "Green"
    Write-ColoredOutput "📢 Notification Service: http://localhost:8086" "Green"
    Write-ColoredOutput "📊 Subscription Service: http://localhost:8087" "Green"
    Write-ColoredOutput "📈 Marketing Service: http://localhost:8088" "Green"
    Write-ColoredOutput "📉 Analytics Service: http://localhost:8089" "Green"
    Write-ColoredOutput "🏆 Performance Service: http://localhost:8090" "Green"
    Write-ColoredOutput "🚨 Emergency Service: http://localhost:8091" "Green"
    Write-ColoredOutput "✈️ Travel Service: http://localhost:8092" "Green"
    Write-ColoredOutput "🏍️ WolKurye Service: http://localhost:8093" "Green"
    Write-ColoredOutput "🤖 AI/ML Service: http://localhost:8094" "Green"
    Write-ColoredOutput "`n=== Database & Infrastructure ===" "Blue"
    Write-ColoredOutput "🐘 PostgreSQL: localhost:5432" "Green"
    Write-ColoredOutput "🔴 Redis: localhost:6379" "Green"
    Write-ColoredOutput "📊 Kafka: localhost:9092" "Green"
}

# Clean up resources
function Remove-WoltaxiServices {
    Write-ColoredOutput "Cleaning up Docker resources..." "Yellow"
    
    try {
        docker-compose down --volumes --remove-orphans 2>$null
    } catch {
        docker compose down --volumes --remove-orphans
    }
    
    docker system prune -af
    Write-ColoredOutput "Cleanup completed!" "Green"
}

# Main function
function Start-Deployment {
    Write-ColoredOutput "=============================================="
    Write-ColoredOutput "🚀 WOLTAXI Windows Deployment"
    Write-ColoredOutput "=============================================="
    
    $platformInfo = Get-PlatformInfo
    $prerequisites = Test-Prerequisites
    New-ProjectDirectories
    New-EnvironmentFile
    Start-WoltaxiServices
    
    Write-ColoredOutput "`nWaiting for services to fully initialize..." "Yellow"
    Start-Sleep -Seconds 45
    
    $healthStatus = Test-ServicesHealth
    Show-ServiceUrls
    
    if ($healthStatus) {
        Write-ColoredOutput "`n🎉 WOLTAXI deployment completed successfully!" "Green"
        Write-ColoredOutput "Platform: $($platformInfo.Platform) ($($platformInfo.Architecture))" "Green"
        Write-ColoredOutput "Build Date: $BuildDate" "Green"
        Write-ColoredOutput "Version: $Version" "Green"
        
        Write-ColoredOutput "`nNext Steps:" "Blue"
        Write-ColoredOutput "1. Access the API Gateway at: http://localhost:8765" "Cyan"
        Write-ColoredOutput "2. Check Eureka Dashboard at: http://localhost:8761" "Cyan"
        Write-ColoredOutput "3. View logs: docker-compose logs -f [service-name]" "Cyan"
        Write-ColoredOutput "4. Stop services: docker-compose down" "Cyan"
    } else {
        Write-ColoredOutput "`n⚠️ Deployment completed with some issues." "Yellow"
        Write-ColoredOutput "Check service logs: docker-compose logs [service-name]" "Cyan"
    }
}

# Script execution
switch ($Action.ToLower()) {
    "deploy" { Start-Deployment }
    "health" { Test-ServicesHealth }
    "urls" { Show-ServiceUrls }
    "clean" { Remove-WoltaxiServices }
    default { 
        Write-ColoredOutput "Usage: .\deploy.ps1 [-Action <deploy|health|urls|clean>] [-SkipHealthCheck] [-Verbose]" "Yellow"
        Write-ColoredOutput "Examples:" "Cyan"
        Write-ColoredOutput "  .\deploy.ps1                    # Full deployment" "Cyan"
        Write-ColoredOutput "  .\deploy.ps1 -Action health     # Health check only" "Cyan"
        Write-ColoredOutput "  .\deploy.ps1 -Action urls       # Show service URLs" "Cyan"
        Write-ColoredOutput "  .\deploy.ps1 -Action clean      # Clean up resources" "Cyan"
    }
}