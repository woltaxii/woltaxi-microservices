# WOLTAXI Atlas Setup PowerShell Script
# This script helps setup the development environment for WOLTAXI with MongoDB Atlas

param(
    [switch]$InstallTools,
    [switch]$TestConnection,
    [switch]$SetupEnvironment,
    [SecureString]$AtlasPassword = (ConvertTo-SecureString "" -AsPlainText -Force)
)

function Write-Title {
    param([string]$Title)
    Write-Host "`n🎯 $Title" -ForegroundColor Magenta
    Write-Host "=" * ($Title.Length + 3) -ForegroundColor Magenta
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Test-CommandExists {
    param([string]$Command)
    try {
        Get-Command $Command -ErrorAction Stop | Out-Null
        return $true
    }
    catch {
        return $false
    }
}

function Install-ChocoIfNeeded {
    if (!(Test-CommandExists "choco")) {
        Write-Info "Installing Chocolatey package manager..."
        Set-ExecutionPolicy Bypass -Scope Process -Force
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
        Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
        Write-Success "Chocolatey installed successfully!"
        return $true
    }
    Write-Success "Chocolatey is already installed"
    return $true
}

function Install-DevelopmentTools {
    Write-Title "Installing WOLTAXI Development Tools"
    
    # Check if running as administrator
    $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")
    if (!$isAdmin) {
        Write-Warning "Please run PowerShell as Administrator to install tools"
        Write-Info "Right-click PowerShell and select 'Run as Administrator'"
        return $false
    }
    
    # Install Chocolatey first
    if (!(Install-ChocoIfNeeded)) {
        return $false
    }
    
    # Install Java JDK 21
    if (!(Test-CommandExists "java")) {
        Write-Info "Installing OpenJDK 21..."
        choco install openjdk21 -y
        Write-Success "OpenJDK 21 installed!"
    } else {
        Write-Success "Java is already installed"
    }
    
    # Install Maven
    if (!(Test-CommandExists "mvn")) {
        Write-Info "Installing Apache Maven..."
        choco install maven -y
        Write-Success "Maven installed!"
    } else {
        Write-Success "Maven is already installed"
    }
    
    # Install Docker Desktop
    if (!(Test-CommandExists "docker")) {
        Write-Info "Installing Docker Desktop..."
        choco install docker-desktop -y
        Write-Warning "Docker Desktop requires restart after installation"
        Write-Success "Docker Desktop installed!"
    } else {
        Write-Success "Docker is already installed"
    }
    
    # Install MongoDB Compass
    Write-Info "Installing MongoDB Compass..."
    choco install mongodb-compass -y
    Write-Success "MongoDB Compass installed!"
    
    # Install MongoDB Shell
    Write-Info "Installing MongoDB Shell (mongosh)..."
    choco install mongodb-shell -y
    Write-Success "MongoDB Shell installed!"
    
    Write-Success "All development tools installed successfully!"
    Write-Warning "Please restart PowerShell to refresh PATH variables"
    return $true
}

function Test-AtlasConnection {
    param([SecureString]$Password)
    
    Write-Title "Testing MongoDB Atlas Connection"
    
    if ([string]::IsNullOrEmpty($Password)) {
        Write-Error "Atlas password is required for connection test"
        Write-Info "Usage: .\atlas-setup.ps1 -TestConnection -AtlasPassword 'your-password'"
        return $false
    }
    
    # Test SRV Records (This should work)
    Write-Info "Testing DNS SRV records..."
    try {
        $srvResult = Resolve-DnsName -Name "_mongodb._tcp.cluster0.q9ezcyu.mongodb.net" -Type SRV -ErrorAction Stop
        if ($srvResult) {
            Write-Success "SRV records found - Atlas cluster is accessible"
            $srvResult | ForEach-Object {
                Write-Host "   📡 $($_.NameTarget):$($_.Port)" -ForegroundColor Gray
            }
        }
    }
    catch {
        Write-Error "SRV record lookup failed: $($_.Exception.Message)"
        return $false
    }
    
    # Test with mongosh if available
    if (Test-CommandExists "mongosh") {
        Write-Info "Testing connection with MongoDB Shell..."
        $connectionString = "mongodb+srv://woltaxi_db_user:$Password@cluster0.q9ezcyu.mongodb.net/woltaxi_users?retryWrites=true&w=majority"
        
        try {
            $testResult = mongosh $connectionString --eval "db.adminCommand('ping')" --quiet 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-Success "MongoDB Atlas connection successful!"
                Write-Success "Ready to run WOLTAXI services with Atlas"
                return $true
            } else {
                Write-Error "MongoDB connection failed: $testResult"
                return $false
            }
        }
        catch {
            Write-Error "MongoDB connection test error: $($_.Exception.Message)"
            return $false
        }
    } else {
        Write-Warning "MongoDB Shell not found - install it first with -InstallTools"
        Write-Info "You can still test connection manually with MongoDB Compass"
        return $false
    }
}

function Initialize-Environment {
    param([SecureString]$Password)
    
    Write-Title "Setting up WOLTAXI Atlas Environment"
    
    if ([string]::IsNullOrEmpty($Password)) {
        Write-Error "Atlas password is required for environment setup"
        return $false
    }
    
    # Update .env.atlas file
    $envAtlasPath = ".\.env.atlas"
    if (Test-Path $envAtlasPath) {
        Write-Info "Updating .env.atlas file with your password..."
        
        $content = Get-Content $envAtlasPath -Raw
        $updatedContent = $content -replace "your-real-atlas-password-here", $Password
        Set-Content -Path $envAtlasPath -Value $updatedContent
        
        Write-Success "Environment variables updated in .env.atlas"
    } else {
        Write-Error ".env.atlas file not found"
        return $false
    }
    
    # Create application-atlas.yml for each service if it doesn't exist
    $services = @("user-service", "driver-service", "ride-service", "payment-hub-service")
    
    foreach ($service in $services) {
        $atlasConfigPath = ".\$service\src\main\resources\application-atlas.yml"
        $servicePath = ".\$service"
        
        if (Test-Path $servicePath) {
            if (!(Test-Path $atlasConfigPath)) {
                Write-Info "Creating Atlas configuration for $service..."
                
                $dbName = switch ($service) {
                    "user-service" { "woltaxi_users" }
                    "driver-service" { "woltaxi_drivers" }
                    "ride-service" { "woltaxi_rides" }
                    "payment-hub-service" { "woltaxi_payments" }
                    default { "woltaxi_$service" }
                }
                
                $atlasConfig = @"
spring:
  profiles:
    active: atlas
  data:
    mongodb:
      uri: mongodb+srv://woltaxi_db_user:${Password}@cluster0.q9ezcyu.mongodb.net/${dbName}?retryWrites=true&w=majority
      database: $dbName
      auto-index-creation: true

logging:
  level:
    org.springframework.data.mongodb: DEBUG
    org.mongodb.driver: INFO
    com.woltaxi: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,mongodb
"@
                
                # Create directory if it doesn't exist
                $configDir = Split-Path $atlasConfigPath -Parent
                if (!(Test-Path $configDir)) {
                    New-Item -ItemType Directory -Path $configDir -Force | Out-Null
                }
                
                Set-Content -Path $atlasConfigPath -Value $atlasConfig
                Write-Success "Atlas configuration created for $service"
            }
        }
    }
    
    Write-Success "WOLTAXI Atlas environment setup completed!"
    return $true
}

function Show-Status {
    Write-Title "WOLTAXI Development Environment Status"
    
    # Check installed tools
    $tools = @{
        "Java" = "java"
        "Maven" = "mvn"
        "Docker" = "docker"
        "MongoDB Shell" = "mongosh"
        "Git" = "git"
    }
    
    Write-Host "🔧 Installed Tools:" -ForegroundColor Yellow
    foreach ($tool in $tools.GetEnumerator()) {
        $status = if (Test-CommandExists $tool.Value) { "✅ Installed" } else { "❌ Not Installed" }
        $color = if (Test-CommandExists $tool.Value) { "Green" } else { "Red" }
        Write-Host "   $($tool.Key): $status" -ForegroundColor $color
    }
    
    # Check Atlas configuration
    Write-Host "`n📁 Atlas Configuration:" -ForegroundColor Yellow
    $envAtlasExists = Test-Path ".\.env.atlas"
    Write-Host "   .env.atlas: $(if ($envAtlasExists) { '✅ Found' } else { '❌ Missing' })" -ForegroundColor $(if ($envAtlasExists) { 'Green' } else { 'Red' })
    
    # Check WOLTAXI services
    Write-Host "`n🚀 WOLTAXI Services:" -ForegroundColor Yellow
    $services = @("eureka-server", "api-gateway", "user-service", "driver-service", "ride-service")
    foreach ($service in $services) {
        $serviceExists = Test-Path ".\$service"
        Write-Host "   ${service}: $(if ($serviceExists) { '✅ Found' } else { '❌ Missing' })" -ForegroundColor $(if ($serviceExists) { 'Green' } else { 'Red' })
    }
}

function Show-Usage {
    Write-Host @"
🌐 WOLTAXI MongoDB Atlas Setup Script

Usage:
    .\atlas-setup.ps1 [OPTIONS]

Options:
    -InstallTools              Install development tools (Java, Maven, Docker, MongoDB tools)
    -TestConnection           Test MongoDB Atlas connection
    -SetupEnvironment         Setup environment files with Atlas configuration
    -AtlasPassword <password> Your MongoDB Atlas password

Examples:
    # Install all development tools (requires Administrator)
    .\atlas-setup.ps1 -InstallTools
    
    # Test Atlas connection
    .\atlas-setup.ps1 -TestConnection -AtlasPassword "your-password"
    
    # Setup environment files
    .\atlas-setup.ps1 -SetupEnvironment -AtlasPassword "your-password"
    
    # Complete setup
    .\atlas-setup.ps1 -InstallTools -SetupEnvironment -TestConnection -AtlasPassword "your-password"

Atlas Connection String:
    mongodb+srv://woltaxi_db_user:<password>@cluster0.q9ezcyu.mongodb.net/<database>

Next Steps after setup:
    1. Restart PowerShell to refresh PATH
    2. Test with MongoDB Compass
    3. Run WOLTAXI services:
       cd user-service
       mvn spring-boot:run -Dspring-boot.run.profiles=atlas
"@
}

# Main execution
function Main {
    Write-Host "🌐 WOLTAXI MongoDB Atlas Setup" -ForegroundColor Magenta
    Write-Host "=============================" -ForegroundColor Magenta
    
    if (!$InstallTools -and !$TestConnection -and !$SetupEnvironment) {
        Show-Status
        Write-Host ""
        Show-Usage
        return
    }
    
    if ($InstallTools) {
        Install-DevelopmentTools
    }
    
    if ($SetupEnvironment) {
        Setup-Environment -Password $AtlasPassword
    }
    
    if ($TestConnection) {
        Test-AtlasConnection -Password $AtlasPassword
    }
    
    Write-Host ""
    Show-Status
    
    Write-Host "`n🎉 WOLTAXI Atlas setup operations completed!" -ForegroundColor Green
    Write-Host "📖 See ATLAS-SETUP-GUIDE.md for detailed instructions" -ForegroundColor Cyan
}

# Execute main function
Main