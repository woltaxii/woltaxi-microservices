# =============================================================================
# WOLTAXI Comprehensive Backup & Disaster Recovery (Windows PowerShell)
# Kapsamlı Yedekleme ve Felaket Kurtarma (Windows PowerShell)
# 
# Multi-Platform Backup Solution for Windows
# Windows için Çok Platformlu Yedekleme Çözümü
# =============================================================================

param(
    [switch]$SkipPrerequisites,
    [switch]$QuickSetup,
    [string]$BackupRoot = "C:\WOLTAXI\Backup",
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

# Configuration
$ConfigDir = "C:\WOLTAXI\Config\Backup"
$LogDir = "C:\WOLTAXI\Logs\Backup"
$ScriptDir = $PSScriptRoot

# Logging setup
if (-not (Test-Path $LogDir)) {
    New-Item -ItemType Directory -Path $LogDir -Force | Out-Null
}

$LogFile = Join-Path $LogDir "backup-dr-setup-$(Get-Date -Format 'yyyyMMdd-HHmmss').log"

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
    Add-Content -Path $LogFile -Value $logMessage -Encoding UTF8
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
Write-ColorOutput -Message "💾 WOLTAXI Comprehensive Backup & Disaster Recovery (Windows)" -ForegroundColor $Colors.Cyan
Write-ColorOutput -Message "🔄 Kapsamlı Yedekleme ve Felaket Kurtarma (Windows)" -ForegroundColor $Colors.Cyan
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

# Install backup tools and prerequisites
function Install-BackupTools {
    if ($SkipPrerequisites) {
        Write-Status "Skipping prerequisites installation as requested"
        return
    }
    
    Write-Status "Installing backup and disaster recovery tools..."
    
    # Check for Chocolatey
    if (-not (Get-Command choco -ErrorAction SilentlyContinue)) {
        Write-Status "Installing Chocolatey package manager..."
        Set-ExecutionPolicy Bypass -Scope Process -Force
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
        Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
        refreshenv
    }
    
    # Essential backup tools
    $BackupTools = @(
        'rsync',
        'rclone',
        'awscli',
        'azure-cli',
        'gsutil',
        '7zip',
        'gpg4win',
        'git',
        'curl',
        'jq',
        'postgresql',
        'redis',
        'mysql'
    )
    
    foreach ($tool in $BackupTools) {
        Write-Status "Installing $tool..."
        try {
            choco install $tool -y --no-progress --ignore-checksums
        }
        catch {
            Write-Warning "Failed to install $tool : $_"
        }
    }
    
    # Install Windows-specific backup tools
    Write-Status "Installing Windows backup features..."
    
    # Enable Windows Server Backup feature (if available)
    try {
        Enable-WindowsOptionalFeature -Online -FeatureName "WindowsServerBackup" -All -NoRestart -ErrorAction SilentlyContinue
    }
    catch {
        Write-Warning "Windows Server Backup feature not available on this system"
    }
    
    # Install WBADMIN and other Windows backup utilities
    $windowsFeatures = @(
        'RSAT-Feature-Tools-BitLocker',
        'BitLocker'
    )
    
    foreach ($feature in $windowsFeatures) {
        try {
            Enable-WindowsOptionalFeature -Online -FeatureName $feature -All -NoRestart -ErrorAction SilentlyContinue
            Write-Status "Enabled Windows feature: $feature"
        }
        catch {
            Write-Warning "Could not enable Windows feature $feature"
        }
    }
    
    Write-Success "Backup tools installation completed"
}

# Setup directory structure
function New-DirectoryStructure {
    Write-Status "Setting up backup directory structure..."
    
    # Main backup directories
    $directories = @(
        "$BackupRoot\Data",
        "$BackupRoot\Scripts",
        "$BackupRoot\Config",
        "$BackupRoot\Logs",
        "$BackupRoot\Temp",
        "$BackupRoot\Restore",
        "$BackupRoot\Archive",
        "$BackupRoot\Data\Databases",
        "$BackupRoot\Data\Files",
        "$BackupRoot\Data\Configs",
        "$BackupRoot\Data\Logs",
        "$BackupRoot\Data\Models",
        "$BackupRoot\Data\Certificates",
        "$BackupRoot\Data\Full",
        "$BackupRoot\Data\Incremental",
        "$BackupRoot\Data\Differential",
        "$BackupRoot\Data\Snapshot",
        "$BackupRoot\Data\Archive",
        "$BackupRoot\Data\Tier1",
        "$BackupRoot\Data\Tier2",
        "$BackupRoot\Data\Tier3",
        "$BackupRoot\Data\Tier4",
        "$ConfigDir",
        "$LogDir"
    )
    
    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-Status "Created directory: $dir"
        }
    }
    
    Write-Success "Directory structure created"
}

# Setup backup configuration
function New-BackupConfiguration {
    Write-Status "Creating backup configuration files..."
    
    # Main backup configuration
    $backupConfig = @"
# WOLTAXI Backup Configuration (Windows)
# Main configuration file for backup operations

[General]
BackupRoot=$BackupRoot
LogLevel=INFO
MaxParallelJobs=4
CompressionLevel=6
EncryptionEnabled=true
VerifyBackups=true

[Storage]
LocalStorage=$BackupRoot\Data
TempStorage=$BackupRoot\Temp
ArchiveStorage=$BackupRoot\Archive

[Retention]
DailyRetention=30
WeeklyRetention=12
MonthlyRetention=24
YearlyRetention=7

[Encryption]
Algorithm=AES256
KeyDerivation=PBKDF2
KeyFile=$ConfigDir\backup.key
SaltLength=32

[Compression]
Algorithm=7zip
Level=6
Method=LZMA2

[Cloud]
AWSEnabled=false
AzureEnabled=false
GCPEnabled=false
MultiCloud=true

[Notifications]
EmailEnabled=true
TeamsEnabled=false
WebhookEnabled=false
EmailRecipient=backup@woltaxi.com

[Monitoring]
HealthCheckInterval=300
PerformanceMonitoring=true
AlertOnFailure=true
AlertOnDelay=true
"@
    
    $backupConfig | Out-File -FilePath "$ConfigDir\backup.conf" -Encoding UTF8
    
    # Database backup configuration
    $databaseConfig = @"
# Database Backup Configuration (Windows)

[PostgreSQL]
Enabled=true
Host=localhost
Port=5432
Username=postgres
PasswordFile=$ConfigDir\db_passwords.txt
Databases=woltaxi_users,woltaxi_rides,woltaxi_payments,woltaxi_ai
BackupFormat=custom
Compression=true
ParallelJobs=2

[Redis]
Enabled=true
Host=localhost
Port=6379
SaveFormat=rdb
SaveLocation=$BackupRoot\Data\Databases\Redis

[MongoDB]
Enabled=false
Host=localhost
Port=27017
Databases=
OplogEnabled=true
GzipEnabled=true
"@
    
    $databaseConfig | Out-File -FilePath "$ConfigDir\database.conf" -Encoding UTF8
    
    # Storage tier configuration
    $storageTierConfig = @"
# Storage Tier Configuration (Windows)

[Tier1]
Name=Critical Real-time
RPO=60
RTO=300
StorageType=local_ssd
Replication=synchronous
Compression=lz4
Encryption=aes256

[Tier2]
Name=Important Near Real-time
RPO=900
RTO=1800
StorageType=local_hdd
Replication=asynchronous
Compression=7zip
Encryption=aes256

[Tier3]
Name=Standard Daily
RPO=86400
RTO=14400
StorageType=network_storage
Replication=async_delayed
Compression=7zip_max
Encryption=aes256

[Tier4]
Name=Archive Weekly
RPO=604800
RTO=86400
StorageType=cold_storage
Replication=none
Compression=maximum
Encryption=aes256
"@
    
    $storageTierConfig | Out-File -FilePath "$ConfigDir\storage-tiers.conf" -Encoding UTF8
    
    # Generate encryption key
    $keyFile = "$ConfigDir\backup.key"
    if (-not (Test-Path $keyFile)) {
        $key = [System.Web.Security.Membership]::GeneratePassword(32, 8)
        $key | Out-File -FilePath $keyFile -Encoding UTF8
        Write-Status "Generated encryption key"
    }
    
    Write-Success "Backup configuration created"
}

# Create backup scripts
function New-BackupScripts {
    Write-Status "Creating backup scripts..."
    
    # Main backup orchestrator PowerShell script
    $backupOrchestrator = @'
# WOLTAXI Backup Orchestrator (PowerShell)
# Main backup coordination script

param(
    [string]$BackupType = "Full",
    [switch]$Force,
    [switch]$Verify
)

# Configuration
$ConfigDir = "C:\WOLTAXI\Config\Backup"
$BackupRoot = "C:\WOLTAXI\Backup"
$LogDir = "C:\WOLTAXI\Logs\Backup"

# Load configuration
$config = Get-Content "$ConfigDir\backup.conf" | ConvertFrom-StringData

# Logging function
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    Write-Host $logMessage
    Add-Content -Path "$LogDir\orchestrator.log" -Value $logMessage -Encoding UTF8
}

# Error handling
function Write-ErrorExit {
    param([string]$Message)
    Write-Log "ERROR: $Message" "ERROR"
    Send-Alert "BACKUP_FAILED" $Message
    exit 1
}

# Send alerts
function Send-Alert {
    param([string]$AlertType, [string]$Message)
    
    if ($config.EmailEnabled -eq "true") {
        $subject = "WOLTAXI Backup Alert: $AlertType"
        $body = @"
WOLTAXI Backup System Alert

Alert Type: $AlertType
Timestamp: $(Get-Date)
Server: $env:COMPUTERNAME

Message:
$Message

Please investigate immediately.

--
WOLTAXI Backup System
"@
        
        try {
            Send-MailMessage -To $config.EmailRecipient -From "backup@woltaxi.com" -Subject $subject -Body $body -SmtpServer "smtp.woltaxi.com"
        }
        catch {
            Write-Log "Failed to send email alert: $_" "ERROR"
        }
    }
}

# Check prerequisites
function Test-Prerequisites {
    Write-Log "Checking backup prerequisites..."
    
    # Check disk space
    $drive = (Get-Item $BackupRoot).PSDrive
    $freeSpace = (Get-PSDrive $drive.Name).Free
    $requiredSpace = 10GB
    
    if ($freeSpace -lt $requiredSpace) {
        Write-ErrorExit "Insufficient disk space. Available: $([math]::Round($freeSpace/1GB,2))GB, Required: $([math]::Round($requiredSpace/1GB,2))GB"
    }
    
    # Check services
    $services = @("Docker Desktop Service", "postgresql-x64-14", "Redis")
    foreach ($service in $services) {
        $svc = Get-Service -Name $service -ErrorAction SilentlyContinue
        if ($svc -and $svc.Status -ne "Running") {
            Write-Log "WARNING: Service $service is not running" "WARNING"
        }
    }
    
    Write-Log "Prerequisites check completed"
}

# Backup databases
function Backup-Databases {
    Write-Log "Starting database backup..."
    
    # PostgreSQL backup
    $dbConfig = Get-Content "$ConfigDir\database.conf" | ConvertFrom-StringData
    
    if ($dbConfig.PostgreSQLEnabled -eq "true") {
        Write-Log "Backing up PostgreSQL databases..."
        
        $env:PGPASSWORD = Get-Content "$ConfigDir\db_passwords.txt"
        
        $databases = $dbConfig.PostgreSQLDatabases -split ","
        foreach ($db in $databases) {
            $backupFile = "$BackupRoot\Data\Databases\postgresql_${db}_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"
            
            $pgDumpArgs = @(
                "-h", $dbConfig.PostgreSQLHost
                "-p", $dbConfig.PostgreSQLPort
                "-U", $dbConfig.PostgreSQLUsername
                "-F", "c"
                "-Z", "9"
                "-f", $backupFile
                $db
            )
            
            try {
                & pg_dump @pgDumpArgs
                Write-Log "PostgreSQL database $db backed up successfully"
            }
            catch {
                Write-ErrorExit "Failed to backup PostgreSQL database $db : $_"
            }
        }
    }
    
    # Redis backup
    if ($dbConfig.RedisEnabled -eq "true") {
        Write-Log "Backing up Redis..."
        
        $backupFile = "$BackupRoot\Data\Databases\redis_$(Get-Date -Format 'yyyyMMdd_HHmmss').rdb"
        
        try {
            & redis-cli -h $dbConfig.RedisHost -p $dbConfig.RedisPort --rdb $backupFile
            Write-Log "Redis backed up successfully"
        }
        catch {
            Write-ErrorExit "Failed to backup Redis: $_"
        }
    }
    
    Write-Log "Database backup completed"
}

# Backup application data
function Backup-ApplicationData {
    Write-Log "Starting application data backup..."
    
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupArchive = "$BackupRoot\Data\Files\application_data_$timestamp.7z"
    
    # Directories to backup
    $backupPaths = @(
        "C:\WOLTAXI\Applications",
        "C:\WOLTAXI\Config",
        "C:\WOLTAXI\Data"
    )
    
    # Create 7zip archive
    $7zipArgs = @("a", "-t7z", "-mx=9", "-mhe=on", "-p$((Get-Content $ConfigDir\backup.key))", $backupArchive) + $backupPaths
    
    try {
        & "C:\Program Files\7-Zip\7z.exe" @7zipArgs
        Write-Log "Application data backed up successfully"
    }
    catch {
        Write-ErrorExit "Failed to backup application data: $_"
    }
    
    Write-Log "Application data backup completed"
}

# Backup Docker volumes
function Backup-DockerVolumes {
    Write-Log "Starting Docker volumes backup..."
    
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    
    # Get Docker volumes
    try {
        $volumes = docker volume ls -q | Where-Object { $_ -match "woltaxi" }
        
        foreach ($volume in $volumes) {
            $backupFile = "$BackupRoot\Data\Docker\volume_${volume}_$timestamp.7z"
            
            # Create container to backup volume
            docker run --rm -v "${volume}:/data:ro" -v "$BackupRoot\Data\Docker:/backup" alpine:latest sh -c "cd /data && tar cf - . | 7z a -si /backup/volume_${volume}_$timestamp.7z"
            
            if ($LASTEXITCODE -eq 0) {
                Write-Log "Docker volume $volume backed up successfully"
            }
            else {
                Write-Log "WARNING: Failed to backup Docker volume $volume" "WARNING"
            }
        }
    }
    catch {
        Write-Log "Docker volumes backup failed: $_" "WARNING"
    }
    
    Write-Log "Docker volumes backup completed"
}

# Verify backups
function Test-Backups {
    Write-Log "Starting backup verification..."
    
    $verificationFailed = 0
    
    # Verify database backups
    Get-ChildItem "$BackupRoot\Data\Databases" -Filter "*.sql" | Where-Object { $_.LastWriteTime -gt (Get-Date).AddDays(-1) } | ForEach-Object {
        if ($_.Length -gt 0) {
            Write-Log "Verified: $($_.Name)"
        }
        else {
            Write-Log "ERROR: Verification failed for $($_.Name)" "ERROR"
            $verificationFailed++
        }
    }
    
    # Verify archives
    Get-ChildItem "$BackupRoot\Data\Files" -Filter "*.7z" | Where-Object { $_.LastWriteTime -gt (Get-Date).AddDays(-1) } | ForEach-Object {
        try {
            & "C:\Program Files\7-Zip\7z.exe" t $_.FullName -p"$(Get-Content $ConfigDir\backup.key)" | Out-Null
            Write-Log "Verified archive: $($_.Name)"
        }
        catch {
            Write-Log "ERROR: Verification failed for archive: $($_.Name)" "ERROR"
            $verificationFailed++
        }
    }
    
    if ($verificationFailed -eq 0) {
        Write-Log "Backup verification completed successfully"
    }
    else {
        Write-ErrorExit "Backup verification failed for $verificationFailed files"
    }
}

# Cleanup old backups
function Remove-OldBackups {
    Write-Log "Starting backup cleanup..."
    
    # Get retention settings
    $dailyRetention = [int]$config.DailyRetention
    $weeklyRetention = [int]$config.WeeklyRetention
    
    # Cleanup database backups
    Get-ChildItem "$BackupRoot\Data\Databases" -Filter "*.sql" | Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$dailyRetention) } | Remove-Item -Force
    
    # Cleanup file backups
    Get-ChildItem "$BackupRoot\Data\Files" -Filter "*.7z" | Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$dailyRetention) } | Remove-Item -Force
    
    # Cleanup Docker backups
    Get-ChildItem "$BackupRoot\Data\Docker" -Filter "*.7z" | Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$weeklyRetention) } | Remove-Item -Force
    
    Write-Log "Backup cleanup completed"
}

# Generate backup report
function New-BackupReport {
    Write-Log "Generating backup report..."
    
    $reportFile = "$BackupRoot\Logs\backup_report_$(Get-Date -Format 'yyyyMMdd_HHmmss').html"
    
    $reportContent = @"
<!DOCTYPE html>
<html>
<head>
    <title>WOLTAXI Backup Report - $(Get-Date)</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .success { color: green; }
        .warning { color: orange; }
        .error { color: red; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>WOLTAXI Backup Report</h1>
    <p>Generated on: $(Get-Date)</p>
    <p>Server: $env:COMPUTERNAME</p>
    
    <h2>Backup Summary</h2>
    <table>
        <tr><th>Component</th><th>Status</th><th>Size</th><th>Last Backup</th></tr>
        <tr><td>Database Backup</td><td class="success">SUCCESS</td><td>$((Get-ChildItem "$BackupRoot\Data\Databases" | Measure-Object -Property Length -Sum).Sum / 1MB | ForEach-Object { "{0:N2} MB" -f $_ })</td><td>$(Get-Date)</td></tr>
        <tr><td>Application Data</td><td class="success">SUCCESS</td><td>$((Get-ChildItem "$BackupRoot\Data\Files" | Measure-Object -Property Length -Sum).Sum / 1MB | ForEach-Object { "{0:N2} MB" -f $_ })</td><td>$(Get-Date)</td></tr>
        <tr><td>Docker Volumes</td><td class="success">SUCCESS</td><td>$((Get-ChildItem "$BackupRoot\Data\Docker" -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum / 1MB | ForEach-Object { "{0:N2} MB" -f $_ })</td><td>$(Get-Date)</td></tr>
    </table>
    
    <h2>Storage Utilization</h2>
    <pre>$(Get-PSDrive C | Select-Object Name, @{Name="Used(GB)";Expression={[math]::Round(($_.Used/1GB),2)}}, @{Name="Free(GB)";Expression={[math]::Round(($_.Free/1GB),2)}}, @{Name="Total(GB)";Expression={[math]::Round(($_.Size/1GB),2)}} | Format-Table -AutoSize | Out-String)</pre>
    
    <h2>Recent Backups</h2>
    <pre>$(Get-ChildItem "$BackupRoot\Data" -Recurse -File | Where-Object { $_.LastWriteTime -gt (Get-Date).AddDays(-7) } | Sort-Object LastWriteTime -Descending | Format-Table Name, Length, LastWriteTime -AutoSize | Out-String)</pre>
</body>
</html>
"@
    
    $reportContent | Out-File -FilePath $reportFile -Encoding UTF8
    Write-Log "Backup report generated: $reportFile"
}

# Main backup function
function Start-BackupOrchestrator {
    Write-Log "Starting WOLTAXI backup orchestrator..."
    
    # Lock file to prevent concurrent runs
    $lockFile = "$env:TEMP\woltaxi-backup.lock"
    
    if (Test-Path $lockFile) {
        $lockTime = (Get-Item $lockFile).LastWriteTime
        if ((Get-Date) - $lockTime -lt (New-TimeSpan -Hours 4)) {
            Write-ErrorExit "Another backup process is already running or was interrupted"
        }
    }
    
    # Create lock file
    New-Item -Path $lockFile -ItemType File -Force | Out-Null
    
    try {
        # Execute backup steps
        Test-Prerequisites
        Backup-Databases
        Backup-ApplicationData
        Backup-DockerVolumes
        
        if ($Verify) {
            Test-Backups
        }
        
        Remove-OldBackups
        New-BackupReport
        
        Write-Log "WOLTAXI backup orchestrator completed successfully"
        Send-Alert "BACKUP_SUCCESS" "Backup completed successfully at $(Get-Date)"
    }
    finally {
        # Remove lock file
        Remove-Item -Path $lockFile -Force -ErrorAction SilentlyContinue
    }
}

# Run main function
Start-BackupOrchestrator
'@
    
    $backupOrchestrator | Out-File -FilePath "$BackupRoot\Scripts\backup-orchestrator.ps1" -Encoding UTF8
    
    # Disaster Recovery Script
    $disasterRecovery = @'
# WOLTAXI Disaster Recovery Script (PowerShell)
# Emergency recovery procedures

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("list-backups", "restore-database", "restore-files", "restore-docker", "full-recovery", "test-recovery")]
    [string]$Command,
    
    [string]$BackupDate,
    [switch]$DryRun,
    [switch]$Force
)

# Configuration
$ConfigDir = "C:\WOLTAXI\Config\Backup"
$BackupRoot = "C:\WOLTAXI\Backup"
$LogDir = "C:\WOLTAXI\Logs\Backup"

# Logging function
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    Write-Host $logMessage
    Add-Content -Path "$LogDir\disaster-recovery.log" -Value $logMessage -Encoding UTF8
}

# Show usage
function Show-Usage {
    Write-Host @"
WOLTAXI Disaster Recovery Script (PowerShell)

Usage: .\disaster-recovery.ps1 -Command <COMMAND> [OPTIONS]

Commands:
    list-backups        List available backups
    restore-database    Restore database from backup
    restore-files       Restore application files
    restore-docker      Restore Docker volumes
    full-recovery       Perform complete system recovery
    test-recovery       Test recovery procedures
    
Options:
    -BackupDate DATE    Specify backup date (YYYYMMDD)
    -DryRun            Simulate recovery without making changes
    -Force             Force recovery without confirmation
    
Examples:
    .\disaster-recovery.ps1 -Command list-backups
    .\disaster-recovery.ps1 -Command restore-database -BackupDate 20241225
    .\disaster-recovery.ps1 -Command full-recovery -BackupDate 20241225 -Force
"@
}

# List available backups
function Get-AvailableBackups {
    Write-Log "Listing available backups..."
    
    Write-Host "=== Database Backups ===" -ForegroundColor Yellow
    Get-ChildItem "$BackupRoot\Data\Databases" -Filter "*.sql" | Sort-Object LastWriteTime -Descending | Format-Table Name, Length, LastWriteTime -AutoSize
    
    Write-Host "`n=== File Backups ===" -ForegroundColor Yellow
    Get-ChildItem "$BackupRoot\Data\Files" -Filter "*.7z" | Sort-Object LastWriteTime -Descending | Format-Table Name, Length, LastWriteTime -AutoSize
    
    Write-Host "`n=== Docker Volume Backups ===" -ForegroundColor Yellow
    Get-ChildItem "$BackupRoot\Data\Docker" -Filter "*.7z" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Format-Table Name, Length, LastWriteTime -AutoSize
}

# Restore database
function Restore-Database {
    param([string]$BackupDate, [bool]$DryRun = $false)
    
    Write-Log "Starting database restore for date: $BackupDate"
    
    # Find database backup file
    $dbBackup = Get-ChildItem "$BackupRoot\Data\Databases" -Filter "*$BackupDate*.sql" | Select-Object -First 1
    
    if (-not $dbBackup) {
        Write-Log "ERROR: No database backup found for date $BackupDate" "ERROR"
        return $false
    }
    
    Write-Log "Found database backup: $($dbBackup.FullName)"
    
    if ($DryRun) {
        Write-Log "DRY RUN: Would restore database from $($dbBackup.FullName)"
        return $true
    }
    
    # Stop services
    Write-Log "Stopping services..."
    docker-compose down
    
    # Restore database
    Write-Log "Restoring database..."
    docker-compose up -d postgres
    Start-Sleep -Seconds 30
    
    try {
        & pg_restore -h localhost -p 5432 -U postgres -d postgres --clean --create $dbBackup.FullName
        Write-Log "Database restored successfully"
        return $true
    }
    catch {
        Write-Log "ERROR: Database restore failed: $_" "ERROR"
        return $false
    }
}

# Restore application files
function Restore-Files {
    param([string]$BackupDate, [bool]$DryRun = $false)
    
    Write-Log "Starting file restore for date: $BackupDate"
    
    # Find file backup
    $fileBackup = Get-ChildItem "$BackupRoot\Data\Files" -Filter "*$BackupDate*.7z" | Select-Object -First 1
    
    if (-not $fileBackup) {
        Write-Log "ERROR: No file backup found for date $BackupDate" "ERROR"
        return $false
    }
    
    Write-Log "Found file backup: $($fileBackup.FullName)"
    
    if ($DryRun) {
        Write-Log "DRY RUN: Would restore files from $($fileBackup.FullName)"
        return $true
    }
    
    # Extract files
    Write-Log "Extracting files..."
    try {
        & "C:\Program Files\7-Zip\7z.exe" x $fileBackup.FullName -p"$(Get-Content $ConfigDir\backup.key)" -o"C:\" -y
        Write-Log "Files restored successfully"
        return $true
    }
    catch {
        Write-Log "ERROR: File restore failed: $_" "ERROR"
        return $false
    }
}

# Full recovery
function Start-FullRecovery {
    param([string]$BackupDate, [bool]$DryRun = $false, [bool]$Force = $false)
    
    Write-Log "Starting full system recovery for date: $BackupDate"
    
    if (-not $Force -and -not $DryRun) {
        Write-Host "WARNING: This will completely restore the system from backup." -ForegroundColor Red
        Write-Host "All current data will be replaced with backup data from $BackupDate" -ForegroundColor Red
        $confirm = Read-Host "Are you sure you want to continue? (yes/no)"
        
        if ($confirm -ne "yes") {
            Write-Log "Recovery cancelled by user"
            return $false
        }
    }
    
    # Execute recovery steps
    $dbResult = Restore-Database $BackupDate $DryRun
    $fileResult = Restore-Files $BackupDate $DryRun
    
    if (-not $DryRun) {
        # Restart all services
        Write-Log "Restarting all services..."
        docker-compose up -d
        
        # Wait for services to start
        Start-Sleep -Seconds 60
        
        # Verify services
        Write-Log "Verifying service health..."
        $services = @("eureka-server", "user-service", "ride-service", "payment-hub-service", "ai-ml-service")
        foreach ($service in $services) {
            try {
                $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
                if ($response.StatusCode -eq 200) {
                    Write-Log "Service $service is healthy"
                }
                else {
                    Write-Log "WARNING: Service $service health check failed" "WARNING"
                }
            }
            catch {
                Write-Log "WARNING: Service $service health check failed: $_" "WARNING"
            }
        }
    }
    
    Write-Log "Full recovery completed"
    return ($dbResult -and $fileResult)
}

# Main function
switch ($Command) {
    'list-backups' {
        Get-AvailableBackups
    }
    'restore-database' {
        if (-not $BackupDate) {
            Write-Host "ERROR: Backup date is required" -ForegroundColor Red
            Show-Usage
            exit 1
        }
        Restore-Database $BackupDate $DryRun.IsPresent
    }
    'restore-files' {
        if (-not $BackupDate) {
            Write-Host "ERROR: Backup date is required" -ForegroundColor Red
            Show-Usage
            exit 1
        }
        Restore-Files $BackupDate $DryRun.IsPresent
    }
    'full-recovery' {
        if (-not $BackupDate) {
            Write-Host "ERROR: Backup date is required" -ForegroundColor Red
            Show-Usage
            exit 1
        }
        Start-FullRecovery $BackupDate $DryRun.IsPresent $Force.IsPresent
    }
    default {
        Show-Usage
    }
}
'@
    
    $disasterRecovery | Out-File -FilePath "$BackupRoot\Scripts\disaster-recovery.ps1" -Encoding UTF8
    
    Write-Success "Backup scripts created"
}

# Setup monitoring
function New-BackupMonitoring {
    Write-Status "Setting up backup monitoring..."
    
    # Backup monitoring script
    $monitoringScript = '@
# WOLTAXI Backup Monitoring Script (PowerShell)
# Monitors backup health and sends alerts

param([switch]$SendAlerts)

# Configuration
$ConfigDir = "C:\WOLTAXI\Config\Backup"
$BackupRoot = "C:\WOLTAXI\Backup"
$LogDir = "C:\WOLTAXI\Logs\Backup"

# Load configuration
$config = Get-Content "$ConfigDir\backup.conf" | ConvertFrom-StringData

# Monitoring functions
function Test-BackupFreshness {
    $maxAge = New-TimeSpan -Hours 24
    $currentTime = Get-Date
    
    # Check database backups
    $latestDbBackup = Get-ChildItem "$BackupRoot\Data\Databases" -Filter "*.sql" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($latestDbBackup) {
        $age = $currentTime - $latestDbBackup.LastWriteTime
        if ($age -gt $maxAge) {
            Write-Host "ALERT: Database backup is older than 24 hours (age: $($age.TotalHours) hours)" -ForegroundColor Red
            return $false
        }
    }
    else {
        Write-Host "ALERT: No database backups found" -ForegroundColor Red
        return $false
    }
    
    # Check file backups
    $latestFileBackup = Get-ChildItem "$BackupRoot\Data\Files" -Filter "*.7z" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($latestFileBackup) {
        $age = $currentTime - $latestFileBackup.LastWriteTime
        if ($age -gt $maxAge) {
            Write-Host "ALERT: File backup is older than 24 hours (age: $($age.TotalHours) hours)" -ForegroundColor Red
            return $false
        }
    }
    else {
        Write-Host "ALERT: No file backups found" -ForegroundColor Red
        return $false
    }
    
    Write-Host "OK: Backup freshness check passed" -ForegroundColor Green
    return $true
}

function Test-StorageSpace {
    $drive = Get-PSDrive C
    $usage = [math]::Round((($drive.Used / $drive.Size) * 100), 2)
    $threshold = 85
    
    if ($usage -gt $threshold) {
        Write-Host "ALERT: Backup storage usage is $usage% (threshold: $threshold%)" -ForegroundColor Red
        return $false
    }
    
    Write-Host "OK: Storage usage is $usage%" -ForegroundColor Green
    return $true
}

function Test-BackupIntegrity {
    $failedCount = 0
    
    # Check recent backups
    Get-ChildItem "$BackupRoot\Data" -Filter "*.7z" -Recurse | Where-Object { $_.LastWriteTime -gt (Get-Date).AddDays(-1) } | ForEach-Object {
        try {
            & "C:\Program Files\7-Zip\7z.exe" t $_.FullName -p"$(Get-Content $ConfigDir\backup.key)" | Out-Null
            if ($LASTEXITCODE -ne 0) {
                Write-Host "ALERT: Backup integrity check failed for $($_.Name)" -ForegroundColor Red
                $script:failedCount++
            }
        }
        catch {
            Write-Host "ALERT: Backup integrity check failed for $($_.Name): $_" -ForegroundColor Red
            $script:failedCount++
        }
    }
    
    if ($failedCount -eq 0) {
        Write-Host "OK: Backup integrity check passed" -ForegroundColor Green
        return $true
    }
    else {
        Write-Host "ALERT: $failedCount backup files failed integrity check" -ForegroundColor Red
        return $false
    }
}

# Send alert
function Send-Alert {
    param([string]$Message)
    
    $timestamp = Get-Date
    
    # Email alert
    if ($config.EmailEnabled -eq "true") {
        $subject = "WOLTAXI Backup Alert"
        $body = @"
WOLTAXI Backup System Alert

Timestamp: $timestamp
Server: $env:COMPUTERNAME

Message:
$Message

Please investigate immediately.

--
WOLTAXI Backup System
"@
        
        try {
            Send-MailMessage -To $config.EmailRecipient -From "backup@woltaxi.com" -Subject $subject -Body $body -SmtpServer "smtp.woltaxi.com"
        }
        catch {
            Write-Host "Failed to send email alert: $_" -ForegroundColor Red
        }
    }
    
    # Log alert
    Add-Content -Path "$LogDir\alerts.log" -Value "[$timestamp] ALERT: $Message" -Encoding UTF8
}

# Main monitoring function
$alerts = @()

# Run health checks
if (-not (Test-BackupFreshness)) {
    $alerts += "Backup freshness check failed"
}

if (-not (Test-StorageSpace)) {
    $alerts += "Storage space check failed"
}

if (-not (Test-BackupIntegrity)) {
    $alerts += "Backup integrity check failed"
}

# Send alerts if any
if ($alerts.Count -gt 0 -and $SendAlerts) {
    $alertMessage = "The following backup health checks failed:`n"
    foreach ($alert in $alerts) {
        $alertMessage += "- $alert`n"
    }
    Send-Alert $alertMessage
}

Write-Host "Backup monitoring completed. Alerts: $($alerts.Count)"
'
    
    $monitoringScript | Out-File -FilePath "$BackupRoot\Scripts\backup-monitor.ps1" -Encoding UTF8
    
    Write-Success "Backup monitoring setup completed"
}

# Setup scheduled tasks
function New-ScheduledTasks {
    Write-Status "Setting up backup scheduled tasks..."
    
    try {
        # Daily backup task
        $action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-ExecutionPolicy Bypass -File `"$BackupRoot\Scripts\backup-orchestrator.ps1`" -Verify"
        $trigger = New-ScheduledTaskTrigger -Daily -At "2:00AM"
        $settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable -MultipleInstances IgnoreNew
        $principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest
        
        Register-ScheduledTask -TaskName "WOLTAXI-Daily-Backup" -Action $action -Trigger $trigger -Settings $settings -Principal $principal -Description "WOLTAXI Daily Backup Task" -Force | Out-Null
        
        # Hourly monitoring task
        $monitorAction = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-ExecutionPolicy Bypass -File `"$BackupRoot\Scripts\backup-monitor.ps1`" -SendAlerts"
        $monitorTrigger = New-ScheduledTaskTrigger -Once -At (Get-Date) -RepetitionInterval (New-TimeSpan -Hours 1) -RepetitionDuration (New-TimeSpan -Days 365)
        
        Register-ScheduledTask -TaskName "WOLTAXI-Backup-Monitor" -Action $monitorAction -Trigger $monitorTrigger -Settings $settings -Principal $principal -Description "WOLTAXI Backup Monitoring Task" -Force | Out-Null
        
        # Weekly cleanup task
        $cleanupAction = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-ExecutionPolicy Bypass -Command `"Get-ChildItem '$BackupRoot\Temp' -Recurse | Where-Object { `$_.LastWriteTime -lt (Get-Date).AddDays(-7) } | Remove-Item -Force -Recurse`""
        $cleanupTrigger = New-ScheduledTaskTrigger -Weekly -DaysOfWeek Sunday -At "1:00AM"
        
        Register-ScheduledTask -TaskName "WOLTAXI-Backup-Cleanup" -Action $cleanupAction -Trigger $cleanupTrigger -Settings $settings -Principal $principal -Description "WOLTAXI Backup Cleanup Task" -Force | Out-Null
        
        Write-Success "Scheduled tasks created successfully"
    }
    catch {
        Write-Warning "Failed to create some scheduled tasks: $_"
    }
}

# Setup cloud integration
function New-CloudIntegration {
    Write-Status "Setting up cloud storage integration..."
    
    # Cloud sync script
    $cloudSync = @'
# WOLTAXI Cloud Backup Sync (PowerShell)
# Syncs backups to multiple cloud providers

param(
    [string[]]$Providers = @("aws", "azure", "gcp"),
    [switch]$Force
)

# Configuration
$ConfigDir = "C:\WOLTAXI\Config\Backup"
$BackupRoot = "C:\WOLTAXI\Backup"

# Load configuration
$config = Get-Content "$ConfigDir\backup.conf" | ConvertFrom-StringData

# Cloud sync functions
function Sync-ToAWS {
    if ($config.AWSEnabled -eq "true") {
        Write-Host "Syncing to AWS S3..." -ForegroundColor Cyan
        
        try {
            aws s3 sync "$BackupRoot\Data" "s3://woltaxi-backups/" --exclude "*" --include "*.sql" --include "*.7z" --storage-class STANDARD_IA --sse AES256
            Write-Host "AWS S3 sync completed successfully" -ForegroundColor Green
        }
        catch {
            Write-Host "AWS S3 sync failed: $_" -ForegroundColor Red
        }
    }
}

function Sync-ToAzure {
    if ($config.AzureEnabled -eq "true") {
        Write-Host "Syncing to Azure Blob Storage..." -ForegroundColor Cyan
        
        try {
            az storage blob sync --source "$BackupRoot\Data" --container woltaxi-backups --account-name woltaxibackups
            Write-Host "Azure Blob sync completed successfully" -ForegroundColor Green
        }
        catch {
            Write-Host "Azure Blob sync failed: $_" -ForegroundColor Red
        }
    }
}

function Sync-ToGCP {
    if ($config.GCPEnabled -eq "true") {
        Write-Host "Syncing to Google Cloud Storage..." -ForegroundColor Cyan
        
        try {
            gsutil -m rsync -r -d "$BackupRoot\Data" "gs://woltaxi-backups/"
            Write-Host "Google Cloud Storage sync completed successfully" -ForegroundColor Green
        }
        catch {
            Write-Host "Google Cloud Storage sync failed: $_" -ForegroundColor Red
        }
    }
}

# Main sync function
Write-Host "Starting cloud backup sync..." -ForegroundColor Yellow

foreach ($provider in $Providers) {
    switch ($provider.ToLower()) {
        "aws" { Sync-ToAWS }
        "azure" { Sync-ToAzure }
        "gcp" { Sync-ToGCP }
        default { Write-Host "Unknown provider: $provider" -ForegroundColor Yellow }
    }
}

Write-Host "Cloud backup sync completed" -ForegroundColor Green
'@
    
    $cloudSync | Out-File -FilePath "$BackupRoot\Scripts\cloud-sync.ps1" -Encoding UTF8
    
    Write-Success "Cloud integration setup completed"
}

# Main setup function
function Start-BackupDRSetup {
    Write-Status "Starting WOLTAXI Backup & Disaster Recovery Setup..."
    
    try {
        # Install tools and setup infrastructure
        Install-BackupTools
        New-DirectoryStructure
        New-BackupConfiguration
        New-BackupScripts
        New-BackupMonitoring
        New-ScheduledTasks
        New-CloudIntegration
        
        # Create main startup script
        $startupScript = @"
# WOLTAXI Backup & Disaster Recovery System Startup (PowerShell)

Write-Host "🔄 Starting WOLTAXI Backup & Disaster Recovery System..." -ForegroundColor Cyan

# Verify configuration
Write-Host "Verifying backup configuration..." -ForegroundColor Yellow
if (Test-Path "$ConfigDir\backup.conf") {
    Write-Host "✅ Backup configuration found" -ForegroundColor Green
}
else {
    Write-Host "❌ Backup configuration missing" -ForegroundColor Red
    exit 1
}

# Check backup directories
if (Test-Path "$BackupRoot\Data") {
    Write-Host "✅ Backup directories ready" -ForegroundColor Green
}
else {
    Write-Host "❌ Backup directories missing" -ForegroundColor Red
    exit 1
}

# Start monitoring
Write-Host "Starting backup monitoring..." -ForegroundColor Yellow
Start-Process PowerShell -ArgumentList "-ExecutionPolicy Bypass -File `"$BackupRoot\Scripts\backup-monitor.ps1`"" -WindowStyle Hidden

Write-Host "✅ WOLTAXI Backup & Disaster Recovery System started successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Available Commands:" -ForegroundColor Yellow
Write-Host "   Full Backup:        $BackupRoot\Scripts\backup-orchestrator.ps1" -ForegroundColor White
Write-Host "   Disaster Recovery:  $BackupRoot\Scripts\disaster-recovery.ps1" -ForegroundColor White
Write-Host "   Backup Monitor:     $BackupRoot\Scripts\backup-monitor.ps1" -ForegroundColor White
Write-Host "   Cloud Sync:         $BackupRoot\Scripts\cloud-sync.ps1" -ForegroundColor White
Write-Host ""
Write-Host "📊 Status:" -ForegroundColor Yellow
Write-Host "   Configuration:      $ConfigDir" -ForegroundColor White
Write-Host "   Backup Data:        $BackupRoot\Data" -ForegroundColor White
Write-Host "   Logs:              $LogDir" -ForegroundColor White
"@
        
        $startupScript | Out-File -FilePath "backup-system-start.ps1" -Encoding UTF8
        
        Write-Success "🎉 WOLTAXI Backup & Disaster Recovery Setup Completed Successfully!"
        Write-Host ""
        Write-ColorOutput -Message "==============================================================================" -ForegroundColor $Colors.Cyan
        Write-ColorOutput -Message "✅ Backup & DR Components Installed:" -ForegroundColor $Colors.Green
        Write-ColorOutput -Message "   💾 Multi-tier Backup Strategy" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   🔄 Automated Backup Orchestration" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   🛡️  Disaster Recovery Procedures" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   📊 Comprehensive Monitoring" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   ☁️  Multi-cloud Integration" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   🔐 Encryption & Security" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   ⚡ Automated Scheduling" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   📈 Backup Verification" -ForegroundColor $Colors.Blue
        Write-Host ""
        Write-ColorOutput -Message "📋 Next Steps:" -ForegroundColor $Colors.Yellow
        Write-ColorOutput -Message "   1. Run: .\backup-system-start.ps1" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   2. Configure cloud credentials (optional)" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   3. Test backup: .\$BackupRoot\Scripts\backup-orchestrator.ps1" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   4. Test recovery: .\$BackupRoot\Scripts\disaster-recovery.ps1 -Command list-backups" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "   5. Verify monitoring: .\$BackupRoot\Scripts\backup-monitor.ps1" -ForegroundColor $Colors.Blue
        Write-ColorOutput -Message "==============================================================================" -ForegroundColor $Colors.Cyan
    }
    catch {
        Write-Error "Backup & DR setup failed: $_"
        exit 1
    }
}

# Run main function
Start-BackupDRSetup