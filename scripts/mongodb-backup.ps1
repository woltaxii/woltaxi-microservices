# WOLTAXI MongoDB Backup Script for Windows PowerShell
# This script creates automated backups of all MongoDB databases on Windows
# Author: WOLTAXI Development Team
# Version: 2.0.0

param(
    [string]$Action = "backup",
    [string]$Database = "",
    [string]$BackupFile = "",
    [switch]$List,
    [switch]$Latest,
    [switch]$Full
)

# Configuration
$MONGO_HOST = if ($env:MONGO_HOST) { $env:MONGO_HOST } else { "localhost" }
$MONGO_PORT = if ($env:MONGO_PORT) { $env:MONGO_PORT } else { "27017" }
$MONGO_USERNAME = if ($env:MONGO_USERNAME) { $env:MONGO_USERNAME } else { "woltaxi" }
$MONGO_PASSWORD = if ($env:MONGO_PASSWORD) { $env:MONGO_PASSWORD } else { "woltaxi2024" }
$BACKUP_DIR = ".\database\mongodb\backups"
$DATE = Get-Date -Format "yyyyMMdd_HHmmss"
$RETENTION_DAYS = if ($env:BACKUP_RETENTION_DAYS) { $env:BACKUP_RETENTION_DAYS } else { 30 }

# Ensure backup directory exists
if (!(Test-Path -Path $BACKUP_DIR)) {
    New-Item -ItemType Directory -Path $BACKUP_DIR -Force | Out-Null
}

# Function to write log messages
function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor Green
}

function Write-Error-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor Red
}

function Write-Warning-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor Yellow
}

# Function to test MongoDB connection
function Test-MongoConnection {
    try {
        $testCmd = "mongosh --host `"$MONGO_HOST`:$MONGO_PORT`" --username `"$MONGO_USERNAME`" --password `"$MONGO_PASSWORD`" --authenticationDatabase admin --eval `"db.adminCommand('ping')`" --quiet"
        $result = Invoke-Expression $testCmd 2>$null
        return $true
    }
    catch {
        return $false
    }
}

# Function to backup a single database
function Backup-Database {
    param(
        [string]$DatabaseName,
        [string]$BackupPath
    )
    
    Write-Log "📦 Backing up database: $DatabaseName"
    
    try {
        $mongodumpCmd = "mongodump --host `"$MONGO_HOST`:$MONGO_PORT`" --username `"$MONGO_USERNAME`" --password `"$MONGO_PASSWORD`" --authenticationDatabase admin --db `"$DatabaseName`" --out `"$BackupPath`" --gzip"
        
        $result = Invoke-Expression $mongodumpCmd 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Log "✅ Successfully backed up $DatabaseName"
            
            # Create compressed archive
            $archivePath = "$BackupPath.zip"
            Compress-Archive -Path "$BackupPath\*" -DestinationPath $archivePath -Force
            Remove-Item -Path $BackupPath -Recurse -Force
            
            Write-Log "🗜️ Compressed backup: $archivePath"
            return $true
        }
        else {
            Write-Error-Log "❌ Failed to backup database: $DatabaseName"
            return $false
        }
    }
    catch {
        Write-Error-Log "❌ Exception during backup of $DatabaseName`: $($_.Exception.Message)"
        return $false
    }
}

# Function to create full backup
function Backup-FullCluster {
    param([string]$BackupPath)
    
    Write-Log "🔄 Creating full cluster backup..."
    
    try {
        $mongodumpCmd = "mongodump --host `"$MONGO_HOST`:$MONGO_PORT`" --username `"$MONGO_USERNAME`" --password `"$MONGO_PASSWORD`" --authenticationDatabase admin --out `"$BackupPath`" --gzip"
        
        $result = Invoke-Expression $mongodumpCmd 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            # Create compressed archive
            $archivePath = "$BackupPath.zip"
            Compress-Archive -Path "$BackupPath\*" -DestinationPath $archivePath -Force
            Remove-Item -Path $BackupPath -Recurse -Force
            
            Write-Log "✅ Full backup completed: $archivePath"
            return $true
        }
        else {
            Write-Error-Log "❌ Full backup failed"
            return $false
        }
    }
    catch {
        Write-Error-Log "❌ Exception during full backup: $($_.Exception.Message)"
        return $false
    }
}

# Function to list available backups
function Get-BackupList {
    Write-Log "📋 Available Backups:"
    Write-Log "===================="
    
    if (!(Test-Path -Path $BACKUP_DIR)) {
        Write-Warning-Log "❌ Backup directory not found: $BACKUP_DIR"
        return
    }
    
    Write-Log "Individual Database Backups:"
    $individualBackups = Get-ChildItem -Path $BACKUP_DIR -Filter "*_backup_*.zip" | Where-Object { $_.Name -notlike "*full_backup*" } | Sort-Object LastWriteTime -Descending
    
    foreach ($backup in $individualBackups) {
        $size = [math]::Round($backup.Length / 1MB, 2)
        Write-Log "  📦 $($backup.Name) ($size MB) - $($backup.LastWriteTime)"
    }
    
    Write-Log ""
    Write-Log "Full Cluster Backups:"
    $fullBackups = Get-ChildItem -Path $BACKUP_DIR -Filter "*full_backup*.zip" | Sort-Object LastWriteTime -Descending
    
    foreach ($backup in $fullBackups) {
        $size = [math]::Round($backup.Length / 1MB, 2)
        Write-Log "  🔄 $($backup.Name) ($size MB) - $($backup.LastWriteTime)"
    }
}

# Function to cleanup old backups
function Remove-OldBackups {
    Write-Log "🧹 Cleaning up backups older than $RETENTION_DAYS days..."
    
    $cutoffDate = (Get-Date).AddDays(-$RETENTION_DAYS)
    $oldBackups = Get-ChildItem -Path $BACKUP_DIR -Filter "*.zip" | Where-Object { $_.LastWriteTime -lt $cutoffDate }
    
    foreach ($backup in $oldBackups) {
        Remove-Item -Path $backup.FullName -Force
        Write-Log "🗑️ Removed old backup: $($backup.Name)"
    }
    
    Write-Log "✅ Cleanup completed"
}

# Function to get backup statistics
function Get-BackupStats {
    Write-Log "📊 Backup Statistics:"
    Write-Log "-------------------"
    
    $backups = Get-ChildItem -Path $BACKUP_DIR -Filter "*.zip"
    $totalCount = $backups.Count
    $totalSize = [math]::Round(($backups | Measure-Object -Sum Length).Sum / 1GB, 2)
    
    $oldestBackup = $backups | Sort-Object LastWriteTime | Select-Object -First 1
    $newestBackup = $backups | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    
    Write-Log "Total backups: $totalCount"
    Write-Log "Total size: $totalSize GB"
    
    if ($oldestBackup) {
        Write-Log "Oldest backup: $($oldestBackup.Name)"
    }
    
    if ($newestBackup) {
        Write-Log "Newest backup: $($newestBackup.Name)"
    }
}

# Main execution
function Main {
    Write-Log "🚀 WOLTAXI MongoDB Backup Process Started"
    Write-Log "=========================================="
    
    # Handle list option
    if ($List) {
        Get-BackupList
        return
    }
    
    # Test MongoDB connection
    Write-Log "🔍 Testing MongoDB connection..."
    if (!(Test-MongoConnection)) {
        Write-Error-Log "❌ Cannot connect to MongoDB at $MONGO_HOST`:$MONGO_PORT"
        exit 1
    }
    Write-Log "✅ MongoDB connection verified"
    
    # Database list to backup
    $databases = @(
        "woltaxi_users",
        "woltaxi_drivers",
        "woltaxi_rides",
        "woltaxi_payments",
        "woltaxi_analytics",
        "woltaxi_notifications",
        "woltaxi_emergency",
        "woltaxi_wolkurye",
        "woltaxi_aiml",
        "woltaxi_smartvehicle",
        "woltaxi_travel",
        "woltaxi_marketing",
        "woltaxi_subscription",
        "woltaxi_performance"
    )
    
    switch ($Action.ToLower()) {
        "backup" {
            if ($Full) {
                # Full cluster backup
                $backupPath = Join-Path $BACKUP_DIR "woltaxi_full_backup_$DATE"
                Backup-FullCluster $backupPath
            }
            elseif ($Database) {
                # Single database backup
                $backupPath = Join-Path $BACKUP_DIR "${Database}_backup_$DATE"
                Backup-Database $Database $backupPath
            }
            else {
                # Backup all databases
                foreach ($db in $databases) {
                    $backupPath = Join-Path $BACKUP_DIR "${db}_backup_$DATE"
                    Backup-Database $db $backupPath
                }
                
                # Create full backup on Sundays
                if ((Get-Date).DayOfWeek -eq "Sunday") {
                    $fullBackupPath = Join-Path $BACKUP_DIR "woltaxi_full_backup_$DATE"
                    Backup-FullCluster $fullBackupPath
                }
            }
            
            # Cleanup old backups
            Remove-OldBackups
            
            # Show statistics
            Get-BackupStats
        }
        
        "list" {
            Get-BackupList
        }
        
        "stats" {
            Get-BackupStats
        }
        
        default {
            Write-Error-Log "❌ Unknown action: $Action"
            Write-Log "Available actions: backup, list, stats"
            exit 1
        }
    }
    
    Write-Log "🎉 WOLTAXI MongoDB Backup Process Completed Successfully!"
    Write-Log "========================================================"
}

# Show usage information
function Show-Usage {
    Write-Host @"
WOLTAXI MongoDB Backup Script for Windows

Usage:
    .\mongodb-backup.ps1 [OPTIONS]

Options:
    -Action <backup|list|stats>  Action to perform (default: backup)
    -Database <name>             Backup specific database only
    -Full                        Create full cluster backup
    -List                        List available backups
    
Examples:
    .\mongodb-backup.ps1                           # Backup all databases
    .\mongodb-backup.ps1 -Database woltaxi_users   # Backup specific database
    .\mongodb-backup.ps1 -Full                     # Full cluster backup
    .\mongodb-backup.ps1 -List                     # List backups
    .\mongodb-backup.ps1 -Action stats             # Show statistics

Environment Variables:
    MONGO_HOST                   MongoDB host (default: localhost)
    MONGO_PORT                   MongoDB port (default: 27017)
    MONGO_USERNAME               MongoDB username (default: woltaxi)
    MONGO_PASSWORD               MongoDB password (default: woltaxi2024)
    BACKUP_RETENTION_DAYS        Backup retention days (default: 30)
"@
}

# Check if help is requested
if ($args -contains "-h" -or $args -contains "--help" -or $args -contains "/?") {
    Show-Usage
    exit 0
}

# Execute main function
Main