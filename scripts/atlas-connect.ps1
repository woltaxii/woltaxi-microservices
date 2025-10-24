# WOLTAXI MongoDB Atlas Connection and Data Viewer Script
# This script connects to MongoDB Atlas and displays your WOLTAXI data
# Author: WOLTAXI Development Team
# Version: 2.0.0

param(
    [string]$ConnectionString = "",
    [string]$Database = "woltaxi_users",
    [string]$Collection = "",
    [switch]$ListDatabases,
    [switch]$ListCollections,
    [switch]$ShowStats,
    [int]$Limit = 10
)

# Function to show usage
function Show-Usage {
    Write-Host @"
WOLTAXI MongoDB Atlas Data Viewer

Usage:
    .\atlas-connect.ps1 -ConnectionString "mongodb+srv://..." [OPTIONS]

Options:
    -ConnectionString    MongoDB Atlas connection string
    -Database           Database name (default: woltaxi_users)
    -Collection         Show documents from specific collection
    -ListDatabases      List all databases
    -ListCollections    List all collections in database
    -ShowStats          Show database statistics
    -Limit              Limit number of documents to show (default: 10)

Examples:
    # List all databases
    .\atlas-connect.ps1 -ConnectionString "mongodb+srv://..." -ListDatabases
    
    # List collections in woltaxi_users database
    .\atlas-connect.ps1 -ConnectionString "mongodb+srv://..." -ListCollections
    
    # Show users collection data
    .\atlas-connect.ps1 -ConnectionString "mongodb+srv://..." -Collection "users"
    
    # Show database statistics
    .\atlas-connect.ps1 -ConnectionString "mongodb+srv://..." -ShowStats

Note: Make sure you have mongosh (MongoDB Shell) installed.
"@
}

# Function to test if mongosh is available
function Test-MongoShell {
    try {
        $null = Get-Command mongosh -ErrorAction Stop
        return $true
    }
    catch {
        Write-Host "❌ mongosh is not installed or not in PATH" -ForegroundColor Red
        Write-Host "Please install MongoDB Shell from: https://docs.mongodb.com/mongodb-shell/install/" -ForegroundColor Yellow
        return $false
    }
}

# Function to validate connection string
function Test-ConnectionString {
    param([string]$ConnString)
    
    if ([string]::IsNullOrWhiteSpace($ConnString)) {
        Write-Host "❌ Connection string is required" -ForegroundColor Red
        return $false
    }
    
    if (-not $ConnString.StartsWith("mongodb+srv://")) {
        Write-Host "⚠️  Warning: Connection string should start with 'mongodb+srv://' for Atlas" -ForegroundColor Yellow
    }
    
    return $true
}

# Function to execute MongoDB command
function Invoke-MongoCommand {
    param(
        [string]$ConnString,
        [string]$Command,
        [string]$DatabaseName = ""
    )
    
    try {
        if ($DatabaseName) {
            $fullCommand = "use $DatabaseName; $Command"
        } else {
            $fullCommand = $Command
        }
        
        $result = mongosh $ConnString --quiet --eval $fullCommand 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            return $result
        } else {
            Write-Host "❌ MongoDB command failed: $result" -ForegroundColor Red
            return $null
        }
    }
    catch {
        Write-Host "❌ Error executing MongoDB command: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Function to list databases
function Get-DatabaseList {
    param([string]$ConnString)
    
    Write-Host "📊 WOLTAXI Databases in Atlas:" -ForegroundColor Green
    Write-Host "=============================" -ForegroundColor Green
    
    $command = "db.adminCommand('listDatabases')"
    $result = Invoke-MongoCommand -ConnString $ConnString -Command $command
    
    if ($result) {
        Write-Host $result
    }
}

# Function to list collections
function Get-CollectionList {
    param(
        [string]$ConnString,
        [string]$DatabaseName
    )
    
    Write-Host "📂 Collections in ${DatabaseName}:" -ForegroundColor Green
    Write-Host "================================" -ForegroundColor Green
    
    $command = "db.runCommand('listCollections')"
    $result = Invoke-MongoCommand -ConnString $ConnString -Command $command -DatabaseName $DatabaseName
    
    if ($result) {
        Write-Host $result
    }
    
    # Also show simple collection names
    Write-Host "`n📋 Collection Names:" -ForegroundColor Cyan
    $simpleCommand = "db.getCollectionNames()"
    $collections = Invoke-MongoCommand -ConnString $ConnString -Command $simpleCommand -DatabaseName $DatabaseName
    
    if ($collections) {
        Write-Host $collections
    }
}

# Function to show collection data
function Get-CollectionData {
    param(
        [string]$ConnString,
        [string]$DatabaseName,
        [string]$CollectionName,
        [int]$LimitCount
    )
    
    Write-Host "📄 Documents in $DatabaseName.$CollectionName (limit: $LimitCount):" -ForegroundColor Green
    Write-Host "================================================================" -ForegroundColor Green
    
    # Count total documents
    $countCommand = "db.$CollectionName.countDocuments({})"
    $totalCount = Invoke-MongoCommand -ConnString $ConnString -Command $countCommand -DatabaseName $DatabaseName
    
    if ($totalCount) {
        Write-Host "📊 Total Documents: $totalCount" -ForegroundColor Cyan
        Write-Host "================================" -ForegroundColor Cyan
    }
    
    # Get sample documents
    $findCommand = "db.$CollectionName.find({}).limit($LimitCount).pretty()"
    $result = Invoke-MongoCommand -ConnString $ConnString -Command $findCommand -DatabaseName $DatabaseName
    
    if ($result) {
        Write-Host $result
    }
    
    # Show collection stats
    Write-Host "`n📈 Collection Statistics:" -ForegroundColor Cyan
    $statsCommand = "db.$CollectionName.stats()"
    $stats = Invoke-MongoCommand -ConnString $ConnString -Command $statsCommand -DatabaseName $DatabaseName
    
    if ($stats) {
        Write-Host $stats
    }
}

# Function to show database statistics
function Get-DatabaseStats {
    param(
        [string]$ConnString,
        [string]$DatabaseName
    )
    
    Write-Host "📈 Database Statistics for ${DatabaseName}:" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    $statsCommand = "db.stats()"
    $result = Invoke-MongoCommand -ConnString $ConnString -Command $statsCommand -DatabaseName $DatabaseName
    
    if ($result) {
        Write-Host $result
    }
}

# Function to test connection
function Test-AtlasConnection {
    param([string]$ConnString)
    
    Write-Host "🔗 Testing MongoDB Atlas Connection..." -ForegroundColor Yellow
    
    $testCommand = "db.adminCommand('ping')"
    $result = Invoke-MongoCommand -ConnString $ConnString -Command $testCommand
    
    if ($result -and $result -match "ok.*1") {
        Write-Host "✅ Successfully connected to MongoDB Atlas!" -ForegroundColor Green
        
        # Get server info
        $serverCommand = "db.adminCommand('buildInfo')"
        $serverInfo = Invoke-MongoCommand -ConnString $ConnString -Command $serverCommand
        
        if ($serverInfo) {
            Write-Host "`n🖥️  Server Information:" -ForegroundColor Cyan
            Write-Host $serverInfo
        }
        
        return $true
    } else {
        Write-Host "❌ Failed to connect to MongoDB Atlas" -ForegroundColor Red
        Write-Host "Check your connection string and network access." -ForegroundColor Yellow
        return $false
    }
}

# Main execution
function Main {
    Write-Host "🌐 WOLTAXI MongoDB Atlas Data Viewer" -ForegroundColor Magenta
    Write-Host "====================================" -ForegroundColor Magenta
    
    # Show help if no parameters
    if ([string]::IsNullOrWhiteSpace($ConnectionString)) {
        Show-Usage
        return
    }
    
    # Check prerequisites
    if (-not (Test-MongoShell)) {
        return
    }
    
    if (-not (Test-ConnectionString -ConnString $ConnectionString)) {
        return
    }
    
    # Test connection first
    if (-not (Test-AtlasConnection -ConnString $ConnectionString)) {
        return
    }
    
    # Execute requested operation
    if ($ListDatabases) {
        Get-DatabaseList -ConnString $ConnectionString
    }
    elseif ($ListCollections) {
        Get-CollectionList -ConnString $ConnectionString -DatabaseName $Database
    }
    elseif ($ShowStats) {
        Get-DatabaseStats -ConnString $ConnectionString -DatabaseName $Database
    }
    elseif ($Collection) {
        Get-CollectionData -ConnString $ConnectionString -DatabaseName $Database -CollectionName $Collection -LimitCount $Limit
    }
    else {
        # Default: show database overview
        Write-Host "`n🎯 WOLTAXI Database Overview:" -ForegroundColor Magenta
        Get-DatabaseStats -ConnString $ConnectionString -DatabaseName $Database
        Write-Host "`n"
        Get-CollectionList -ConnString $ConnectionString -DatabaseName $Database
    }
    
    Write-Host "`n🎉 Operation completed successfully!" -ForegroundColor Green
}

# Execute main function
Main