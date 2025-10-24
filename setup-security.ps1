# =============================================================================
# WOLTAXI Enterprise Security Implementation Script (Windows PowerShell)
# Kurumsal Güvenlik Uygulama Scripti (Windows PowerShell)
# 
# Cross-Platform Security Setup for Windows, macOS, Linux
# Windows, macOS, Linux için Çapraz Platform Güvenlik Kurulumu
# =============================================================================

param(
    [switch]$SkipPrerequisites,
    [switch]$QuickSetup,
    [string]$LogLevel = "INFO"
)

# Set execution policy for current session
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force

# Colors for output
$Colors = @{
    Red = [System.ConsoleColor]::Red
    Green = [System.ConsoleColor]::Green
    Yellow = [System.ConsoleColor]::Yellow
    Blue = [System.ConsoleColor]::Blue
    Cyan = [System.ConsoleColor]::Cyan
    Magenta = [System.ConsoleColor]::Magenta
    White = [System.ConsoleColor]::White
}

# Logging setup
$LogFile = "security-setup-$(Get-Date -Format 'yyyyMMdd-HHmmss').log"
$LogPath = Join-Path $PWD $LogFile

# Function to write colored output
function Write-ColorOutput {
    param(
        [string]$Message,
        [System.ConsoleColor]$ForegroundColor = [System.ConsoleColor]::White,
        [string]$Prefix = ""
    )
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] $Prefix$Message"
    
    Write-Host $logMessage -ForegroundColor $ForegroundColor
    Add-Content -Path $LogPath -Value $logMessage -Encoding UTF8
}

function Write-Status {
    param([string]$Message)
    Write-ColorOutput -Message $Message -ForegroundColor $Colors.Green -Prefix "[INFO] "
}

function Write-Warning {
    param([string]$Message)
    Write-ColorOutput -Message $Message -ForegroundColor $Colors.Yellow -Prefix "[WARNING] "
}

function Write-Error {
    param([string]$Message)
    Write-ColorOutput -Message $Message -ForegroundColor $Colors.Red -Prefix "[ERROR] "
}

function Write-Success {
    param([string]$Message)
    Write-ColorOutput -Message $Message -ForegroundColor $Colors.Cyan -Prefix "[SUCCESS] "
}

# Header
Write-ColorOutput -Message "==============================================================================" -ForegroundColor $Colors.Cyan
Write-ColorOutput -Message "🔒 WOLTAXI Enterprise Security Implementation (Windows)" -ForegroundColor $Colors.Cyan
Write-ColorOutput -Message "🛡️  Kurumsal Güvenlik Uygulama Sistemi (Windows)" -ForegroundColor $Colors.Cyan
Write-ColorOutput -Message "==============================================================================" -ForegroundColor $Colors.Cyan

# Check if running as Administrator
function Test-Administrator {
    $currentUser = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($currentUser)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

if (-not (Test-Administrator)) {
    Write-Error "This script must be run as Administrator. Please restart PowerShell as Administrator."
    exit 1
}

Write-Status "Running with Administrator privileges"

# Detect Windows version and architecture
function Get-SystemInfo {
    $os = Get-CimInstance -ClassName Win32_OperatingSystem
    $cpu = Get-CimInstance -ClassName Win32_Processor
    
    $systemInfo = @{
        OSName = $os.Caption
        OSVersion = $os.Version
        Architecture = $cpu.Architecture
        Is64Bit = [Environment]::Is64BitOperatingSystem
    }
    
    Write-Status "System Information:"
    Write-Status "  OS: $($systemInfo.OSName)"
    Write-Status "  Version: $($systemInfo.OSVersion)"
    Write-Status "  Architecture: $(if($systemInfo.Is64Bit) { 'x64' } else { 'x86' })"
    
    return $systemInfo
}

$SystemInfo = Get-SystemInfo

# Check and install prerequisites
function Install-Prerequisites {
    if ($SkipPrerequisites) {
        Write-Status "Skipping prerequisites check as requested"
        return
    }
    
    Write-Status "Checking and installing prerequisites..."
    
    # Check for Chocolatey
    if (-not (Get-Command choco -ErrorAction SilentlyContinue)) {
        Write-Status "Installing Chocolatey package manager..."
        Set-ExecutionPolicy Bypass -Scope Process -Force
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
        Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
        refreshenv
    }
    
    # Essential tools to install
    $RequiredTools = @(
        'docker-desktop',
        'docker-compose',
        'openssl.light',
        'curl',
        'jq',
        'git',
        'nmap',
        'wget'
    )
    
    foreach ($tool in $RequiredTools) {
        Write-Status "Checking for $tool..."
        
        try {
            $installed = choco list --local-only $tool
            if ($installed -match $tool) {
                Write-Status "$tool is already installed"
            } else {
                Write-Status "Installing $tool..."
                choco install $tool -y --no-progress
            }
        }
        catch {
            Write-Warning "Failed to install $tool : $_"
        }
    }
    
    # Enable Windows features
    Write-Status "Enabling required Windows features..."
    $features = @(
        'Microsoft-Windows-Subsystem-Linux',
        'VirtualMachinePlatform',
        'Containers'
    )
    
    foreach ($feature in $features) {
        try {
            $featureState = Get-WindowsOptionalFeature -Online -FeatureName $feature -ErrorAction SilentlyContinue
            if ($featureState -and $featureState.State -eq 'Disabled') {
                Write-Status "Enabling Windows feature: $feature"
                Enable-WindowsOptionalFeature -Online -FeatureName $feature -All -NoRestart
            }
            else {
                Write-Status "Windows feature $feature is already enabled or not available"
            }
        }
        catch {
            Write-Warning "Could not enable Windows feature $feature : $_"
        }
    }
    
    Write-Success "Prerequisites installation completed"
}

# Generate SSL certificates
function New-SSLCertificates {
    Write-Status "Generating SSL certificates..."
    
    # Create directories
    $sslDirs = @('ssl\certs', 'ssl\private', 'ssl\ca')
    foreach ($dir in $sslDirs) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }
    
    # Generate CA private key
    Write-Status "Generating CA private key..."
    & openssl genrsa -out ssl\ca\ca-key.pem 4096
    
    # Generate CA certificate
    Write-Status "Generating CA certificate..."
    & openssl req -new -x509 -days 3650 -key ssl\ca\ca-key.pem -sha256 -out ssl\ca\ca-cert.pem -subj "/C=TR/ST=Istanbul/L=Istanbul/O=WOLTAXI/OU=Security/CN=WOLTAXI-CA"
    
    # Generate server private key
    Write-Status "Generating server private key..."
    & openssl genrsa -out ssl\private\server-key.pem 4096
    
    # Generate server certificate signing request
    Write-Status "Generating server CSR..."
    & openssl req -subj "/C=TR/ST=Istanbul/L=Istanbul/O=WOLTAXI/OU=Security/CN=woltaxi.com" -sha256 -new -key ssl\private\server-key.pem -out ssl\server.csr
    
    # Create extensions file
    $serverExtensions = @"
subjectAltName = DNS:woltaxi.com,DNS:*.woltaxi.com,DNS:localhost,IP:127.0.0.1
extendedKeyUsage = serverAuth
"@
    $serverExtensions | Out-File -FilePath ssl\server-extfile.cnf -Encoding ascii
    
    # Generate server certificate signed by CA
    Write-Status "Generating server certificate..."
    & openssl x509 -req -days 365 -in ssl\server.csr -CA ssl\ca\ca-cert.pem -CAkey ssl\ca\ca-key.pem -out ssl\certs\server-cert.pem -extensions v3_req -extfile ssl\server-extfile.cnf -CAcreateserial
    
    # Clean up
    Remove-Item ssl\server.csr, ssl\server-extfile.cnf -ErrorAction SilentlyContinue
    
    Write-Success "SSL certificates generated successfully"
}

# Setup HashiCorp Vault
function Set-VaultConfiguration {
    Write-Status "Setting up HashiCorp Vault..."
    
    # Create vault directories
    $vaultDirs = @('vault\config', 'vault\data', 'vault\logs')
    foreach ($dir in $vaultDirs) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }
    
    # Vault configuration
    $vaultConfig = @"
storage "file" {
  path = "/vault/data"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  tls_cert_file = "/vault/ssl/certs/server-cert.pem"
  tls_key_file = "/vault/ssl/private/server-key.pem"
}

ui = true
api_addr = "https://127.0.0.1:8200"
cluster_addr = "https://127.0.0.1:8201"
disable_mlock = true
"@
    $vaultConfig | Out-File -FilePath vault\config\vault.hcl -Encoding ascii
    
    # Vault Docker Compose
    $vaultCompose = @"
version: '3.8'

services:
  vault:
    image: vault:1.15.2
    container_name: woltaxi-vault
    restart: unless-stopped
    ports:
      - "8200:8200"
    volumes:
      - ./config:/vault/config:ro
      - ./data:/vault/data
      - ./logs:/vault/logs
      - ../ssl:/vault/ssl:ro
    cap_add:
      - IPC_LOCK
    environment:
      VAULT_CONFIG_DIR: /vault/config
      VAULT_API_ADDR: https://0.0.0.0:8200
    command: vault server -config=/vault/config/vault.hcl
    healthcheck:
      test: ["CMD", "vault", "status"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - woltaxi-security

networks:
  woltaxi-security:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
"@
    $vaultCompose | Out-File -FilePath vault\docker-compose.vault.yml -Encoding ascii
    
    Write-Success "Vault configuration created"
}

# Setup Windows Firewall
function Set-WindowsFirewall {
    Write-Status "Configuring Windows Firewall..."
    
    try {
        # Enable Windows Firewall for all profiles
        Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled True
        
        # Allow WOLTAXI services through firewall
        $firewallRules = @(
            @{ Name = "WOLTAXI-HTTP"; Port = 80; Protocol = "TCP"; Description = "WOLTAXI HTTP Traffic" },
            @{ Name = "WOLTAXI-HTTPS"; Port = 443; Protocol = "TCP"; Description = "WOLTAXI HTTPS Traffic" },
            @{ Name = "WOLTAXI-Eureka"; Port = 8761; Protocol = "TCP"; Description = "WOLTAXI Eureka Server" },
            @{ Name = "WOLTAXI-UserService"; Port = 8081; Protocol = "TCP"; Description = "WOLTAXI User Service" },
            @{ Name = "WOLTAXI-RideService"; Port = 8082; Protocol = "TCP"; Description = "WOLTAXI Ride Service" },
            @{ Name = "WOLTAXI-PaymentHub"; Port = 8083; Protocol = "TCP"; Description = "WOLTAXI Payment Hub" },
            @{ Name = "WOLTAXI-AIML"; Port = 8094; Protocol = "TCP"; Description = "WOLTAXI AI/ML Service" },
            @{ Name = "WOLTAXI-Vault"; Port = 8200; Protocol = "TCP"; Description = "WOLTAXI Vault" },
            @{ Name = "WOLTAXI-Grafana"; Port = 3000; Protocol = "TCP"; Description = "WOLTAXI Grafana" },
            @{ Name = "WOLTAXI-Prometheus"; Port = 9090; Protocol = "TCP"; Description = "WOLTAXI Prometheus" },
            @{ Name = "WOLTAXI-Kibana"; Port = 5601; Protocol = "TCP"; Description = "WOLTAXI Kibana" }
        )
        
        foreach ($rule in $firewallRules) {
            # Remove existing rule if present
            Remove-NetFirewallRule -DisplayName $rule.Name -ErrorAction SilentlyContinue
            
            # Create new rule
            New-NetFirewallRule -DisplayName $rule.Name -Direction Inbound -Protocol $rule.Protocol -LocalPort $rule.Port -Action Allow -Description $rule.Description | Out-Null
            Write-Status "Created firewall rule: $($rule.Name) (Port $($rule.Port))"
        }
        
        # Block suspicious traffic
        $blockRules = @(
            @{ Name = "Block-TOR"; RemoteAddress = "198.96.155.3/32"; Description = "Block known TOR exit nodes" },
            @{ Name = "Block-Malware-C2"; RemoteAddress = "185.220.100.0/22"; Description = "Block known malware C2 servers" }
        )
        
        foreach ($rule in $blockRules) {
            Remove-NetFirewallRule -DisplayName $rule.Name -ErrorAction SilentlyContinue
            New-NetFirewallRule -DisplayName $rule.Name -Direction Outbound -RemoteAddress $rule.RemoteAddress -Action Block -Description $rule.Description | Out-Null
            Write-Status "Created blocking rule: $($rule.Name)"
        }
        
        Write-Success "Windows Firewall configured successfully"
    }
    catch {
        Write-Error "Failed to configure Windows Firewall: $_"
    }
}

# Setup Windows Defender
function Set-WindowsDefender {
    Write-Status "Configuring Windows Defender..."
    
    try {
        # Enable real-time protection
        Set-MpPreference -DisableRealtimeMonitoring $false
        
        # Enable cloud protection
        Set-MpPreference -MAPSReporting Advanced
        Set-MpPreference -SubmitSamplesConsent SendAllSamples
        
        # Enable network protection
        Set-MpPreference -EnableNetworkProtection Enabled
        
        # Enable controlled folder access
        Set-MpPreference -EnableControlledFolderAccess Enabled
        
        # Add exclusions for Docker and development tools
        $exclusions = @(
            "$env:ProgramFiles\Docker\Docker\*",
            "$env:USERPROFILE\.docker\*",
            "$PWD\*"
        )
        
        foreach ($exclusion in $exclusions) {
            Add-MpPreference -ExclusionPath $exclusion -ErrorAction SilentlyContinue
            Write-Status "Added Windows Defender exclusion: $exclusion"
        }
        
        # Update definitions
        Update-MpSignature
        
        Write-Success "Windows Defender configured successfully"
    }
    catch {
        Write-Error "Failed to configure Windows Defender: $_"
    }
}

# Setup monitoring and logging
function Set-MonitoringLogging {
    Write-Status "Setting up monitoring and logging..."
    
    # Create monitoring directories
    $monitoringDirs = @('monitoring\prometheus', 'monitoring\grafana', 'monitoring\elasticsearch', 'monitoring\kibana', 'monitoring\logstash')
    foreach ($dir in $monitoringDirs) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }
    
    # Prometheus configuration
    $prometheusConfig = @"
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "security_rules.yml"
  - "performance_rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  - job_name: 'woltaxi-services'
    static_configs:
      - targets: 
        - 'eureka-server:8761'
        - 'user-service:8081'
        - 'ride-service:8082'
        - 'payment-hub-service:8083'
        - 'ai-ml-service:8094'
    metrics_path: /actuator/prometheus
    scrape_interval: 5s
    
  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']
      
  - job_name: 'cadvisor'
    static_configs:
      - targets: ['cadvisor:8080']
"@
    $prometheusConfig | Out-File -FilePath monitoring\prometheus\prometheus.yml -Encoding ascii
    
    # Docker Compose for monitoring
    $monitoringCompose = @"
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:v2.45.0
    container_name: woltaxi-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus:/etc/prometheus
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--web.enable-lifecycle'
      - '--web.enable-admin-api'
    networks:
      - woltaxi-monitoring

  grafana:
    image: grafana/grafana:10.2.0
    container_name: woltaxi-grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=woltaxi-secure-2024
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_SERVER_CERT_FILE=/etc/ssl/certs/server-cert.pem
      - GF_SERVER_CERT_KEY=/etc/ssl/private/server-key.pem
      - GF_SERVER_PROTOCOL=https
    volumes:
      - grafana-data:/var/lib/grafana
      - ../ssl:/etc/ssl:ro
    networks:
      - woltaxi-monitoring

volumes:
  prometheus-data:
  grafana-data:

networks:
  woltaxi-monitoring:
    driver: bridge
"@
    $monitoringCompose | Out-File -FilePath monitoring\docker-compose.monitoring.yml -Encoding ascii
    
    Write-Success "Monitoring and logging configuration created"
}

# Setup backup system
function Set-BackupSystem {
    Write-Status "Setting up backup system..."
    
    # Create backup directories
    $backupDirs = @('backup\scripts', 'backup\config', 'backup\data', 'backup\logs')
    foreach ($dir in $backupDirs) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }
    
    # Backup PowerShell script
    $backupScript = @"
# WOLTAXI Backup Script (PowerShell)
# Automated backup for all services and data

param(
    [switch]`$Force,
    [string]`$BackupType = "Full"
)

`$BackupDir = "backup\data"
`$Date = Get-Date -Format "yyyyMMdd_HHmmss"
`$BackupName = "woltaxi_backup_`$Date"
`$LogFile = "backup\logs\backup_`$Date.log"

function Write-Log {
    param([string]`$Message)
    `$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    `$logMessage = "[`$timestamp] - `$Message"
    Write-Host `$logMessage
    Add-Content -Path `$LogFile -Value `$logMessage -Encoding UTF8
}

Write-Log "Starting WOLTAXI backup process (Type: `$BackupType)"

# Create backup directory
`$BackupPath = Join-Path `$BackupDir `$BackupName
New-Item -ItemType Directory -Path `$BackupPath -Force | Out-Null

try {
    # Backup databases
    Write-Log "Backing up databases..."
    docker exec woltaxi-postgres pg_dumpall -U postgres > "`$BackupPath\postgres_backup.sql"
    docker exec woltaxi-redis redis-cli --rdb "`$BackupPath\redis_backup.rdb"
    
    # Backup configuration files
    Write-Log "Backing up configuration files..."
    Copy-Item -Path "docker-compose*.yml" -Destination `$BackupPath -Force
    Copy-Item -Path "ssl" -Destination `$BackupPath -Recurse -Force
    
    # Backup AI/ML models
    Write-Log "Backing up AI/ML models..."
    docker exec woltaxi-ai-ml-service tar -czf - /app/models > "`$BackupPath\ai_models.tar.gz"
    
    # Create compressed archive
    Write-Log "Creating compressed archive..."
    Compress-Archive -Path `$BackupPath -DestinationPath "`$BackupDir\`$BackupName.zip" -Force
    Remove-Item -Path `$BackupPath -Recurse -Force
    
    Write-Log "Backup process completed successfully"
}
catch {
    Write-Log "Backup failed: `$_"
    exit 1
}
"@
    $backupScript | Out-File -FilePath backup\scripts\backup.ps1 -Encoding ascii
    
    # Backup configuration
    $backupConfig = @"
# WOLTAXI Backup Configuration (Windows)

# Backup schedule (Task Scheduler format)
BACKUP_SCHEDULE="Daily at 2:00 AM"

# Retention policy
BACKUP_RETENTION_DAYS=30
CLOUD_BACKUP_ENABLED=false

# Encryption settings
BACKUP_ENCRYPTION=true
ENCRYPTION_ALGORITHM="AES256"

# Notification settings
NOTIFICATION_EMAIL=""
NOTIFICATION_WEBHOOK=""
"@
    $backupConfig | Out-File -FilePath backup\config\backup.conf -Encoding ascii
    
    # Create scheduled task for backup
    try {
        $action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-ExecutionPolicy Bypass -File `"$PWD\backup\scripts\backup.ps1`""
        $trigger = New-ScheduledTaskTrigger -Daily -At "2:00AM"
        $settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable
        $principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest
        
        Register-ScheduledTask -TaskName "WOLTAXI-Daily-Backup" -Action $action -Trigger $trigger -Settings $settings -Principal $principal -Description "WOLTAXI Daily Backup Task" -Force | Out-Null
        
        Write-Success "Scheduled backup task created"
    }
    catch {
        Write-Warning "Failed to create scheduled backup task: $_"
    }
    
    Write-Success "Backup system configured"
}

# Setup security scanning
function Set-SecurityScanning {
    Write-Status "Setting up security scanning..."
    
    # Create security scanning directories
    $scanningDirs = @('security\scanning', 'security\scanning\results')
    foreach ($dir in $scanningDirs) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }
    
    # Security scanning PowerShell script
    $scanningScript = @"
# WOLTAXI Security Scanning Script (PowerShell)
# Comprehensive security scanning for all components

param(
    [string]`$ScanType = "Full",
    [switch]`$SaveResults
)

`$ScanDir = "security\scanning"
`$ResultsDir = "`$ScanDir\results"
`$Date = Get-Date -Format "yyyyMMdd_HHmmss"

function Write-ScanLog {
    param([string]`$Message)
    `$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    `$logMessage = "[`$timestamp] - `$Message"
    Write-Host `$logMessage -ForegroundColor Cyan
    if (`$SaveResults) {
        Add-Content -Path "`$ResultsDir\scan_`$Date.log" -Value `$logMessage -Encoding UTF8
    }
}

Write-ScanLog "Starting comprehensive security scan (Type: `$ScanType)"

try {
    # Container vulnerability scanning with Docker scan
    Write-ScanLog "Scanning containers for vulnerabilities..."
    `$images = docker images --format "{{.Repository}}:{{.Tag}}" | Where-Object { `$_ -match "woltaxi" }
    foreach (`$image in `$images) {
        Write-ScanLog "Scanning image: `$image"
        docker scan `$image > "`$ResultsDir\`$(`$image -replace '[:\\/]', '_')_`$Date.txt" 2>&1
    }
    
    # Network security scanning with Nmap
    if (Get-Command nmap -ErrorAction SilentlyContinue) {
        Write-ScanLog "Performing network security scan..."
        nmap -sS -sV -O -A --script vuln localhost > "`$ResultsDir\network_scan_`$Date.txt"
    }
    
    # Windows security assessment
    Write-ScanLog "Performing Windows security assessment..."
    Get-ComputerInfo > "`$ResultsDir\system_info_`$Date.txt"
    Get-NetFirewallRule > "`$ResultsDir\firewall_rules_`$Date.txt"
    Get-MpPreference > "`$ResultsDir\defender_config_`$Date.txt"
    
    # SSL/TLS security assessment
    Write-ScanLog "Assessing SSL/TLS security..."
    if (Get-Command testssl -ErrorAction SilentlyContinue) {
        testssl --jsonfile "`$ResultsDir\ssl_scan_`$Date.json" localhost:443
    }
    
    Write-ScanLog "Security scan completed successfully"
}
catch {
    Write-ScanLog "Security scan failed: `$_"
    exit 1
}
"@
    $scanningScript | Out-File -FilePath security\scanning\scan.ps1 -Encoding ascii
    
    Write-Success "Security scanning configured"
}

# Main security setup function
function Start-SecuritySetup {
    Write-Status "Starting WOLTAXI Enterprise Security Setup..."
    
    try {
        # Install prerequisites
        if (-not $QuickSetup) {
            Install-Prerequisites
        }
        
        # Setup security components
        New-SSLCertificates
        Set-VaultConfiguration
        Set-WindowsFirewall
        Set-WindowsDefender
        Set-MonitoringLogging
        Set-BackupSystem
        Set-SecurityScanning
        
        # Create main security Docker Compose file
        $securityCompose = @"
version: '3.8'

services:
  vault:
    extends:
      file: vault/docker-compose.vault.yml
      service: vault
    
  prometheus:
    extends:
      file: monitoring/docker-compose.monitoring.yml
      service: prometheus
      
  grafana:
    extends:
      file: monitoring/docker-compose.monitoring.yml
      service: grafana

networks:
  woltaxi-security:
    external: true
  woltaxi-monitoring:
    external: true

volumes:
  vault-data:
  prometheus-data:
  grafana-data:
"@
        $securityCompose | Out-File -FilePath docker-compose.security.yml -Encoding ascii
        
        # Create startup script
        $startupScript = @"
# WOLTAXI Enterprise Security Stack Startup (PowerShell)

Write-Host "🔒 Starting WOLTAXI Enterprise Security Stack..." -ForegroundColor Cyan

# Create networks
docker network create woltaxi-security 2>`$null
docker network create woltaxi-monitoring 2>`$null

# Start security services
docker-compose -f docker-compose.security.yml up -d

Write-Host "✅ WOLTAXI Enterprise Security Stack started successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Access URLs:" -ForegroundColor Yellow
Write-Host "   Vault UI: https://localhost:8200" -ForegroundColor White
Write-Host "   Grafana: https://localhost:3000 (admin/woltaxi-secure-2024)" -ForegroundColor White
Write-Host "   Prometheus: http://localhost:9090" -ForegroundColor White
Write-Host ""
Write-Host "📊 Default Credentials:" -ForegroundColor Yellow
Write-Host "   Grafana: admin / woltaxi-secure-2024" -ForegroundColor White
Write-Host ""
Write-Host "🔧 Security Tools:" -ForegroundColor Yellow
Write-Host "   Run security scan: .\security\scanning\scan.ps1" -ForegroundColor White
Write-Host "   Create backup: .\backup\scripts\backup.ps1" -ForegroundColor White
"@
        $startupScript | Out-File -FilePath start-security.ps1 -Encoding ascii
        
        Write-Success "🎉 WOLTAXI Enterprise Security Setup Completed Successfully!"
        Write-Host ""
        Write-ColorOutput -Message "==============================================================================" -ForegroundColor $Colors.Cyan
        Write-ColorOutput -Message "✅ Security Components Installed:" -ForegroundColor $Colors.Green
        Write-ColorOutput -Message "   🔐 SSL/TLS Certificates with CA" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   🗄️  HashiCorp Vault for Secrets Management" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   📊 Prometheus + Grafana for Monitoring" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   🔥 Windows Firewall Configuration" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   🛡️  Windows Defender Enhancement" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   💾 Automated Backup System" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   🔍 Comprehensive Security Scanning" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   ⚡ Scheduled Security Tasks" -ForegroundColor $Colors.Blue
        Write-Host ""
        Write-ColorOutput -Message "📋 Next Steps:" -ForegroundColor $Colors.Yellow
        Write-ColorOutput -Message "   1. Run: .\start-security.ps1" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   2. Initialize Vault: vault operator init" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   3. Configure monitoring dashboards" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   4. Test backup and restore procedures" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   5. Run initial security scan" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "==============================================================================" -ForegroundColor $Colors.Cyan
    }
    catch {
        Write-Error "Security setup failed: $_"
        exit 1
    }
}

# Run main function
Start-SecuritySetup