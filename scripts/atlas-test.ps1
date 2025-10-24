# WOLTAXI MongoDB Atlas Connection Test Script
# Test your Atlas connection without requiring mongosh

param(
    [string]$Password = ""
)

$ATLAS_CONNECTION_BASE = "mongodb+srv://woltaxi_db_user"
$ATLAS_CLUSTER = "cluster0.q9ezcyu.mongodb.net"
$ATLAS_DATABASE = "woltaxi_users"

function Test-AtlasConnectionWithCSharp {
    param([string]$ConnectionString)
    
    Write-Host "🔗 Testing MongoDB Atlas Connection with .NET Driver..." -ForegroundColor Yellow
    Write-Host "Connection: $ATLAS_CLUSTER" -ForegroundColor Cyan
    Write-Host "Database: $ATLAS_DATABASE" -ForegroundColor Cyan
    Write-Host "=====================================================" -ForegroundColor Green
    
    # Create a simple C# test program
    $testCode = @"
using System;
using MongoDB.Driver;
using MongoDB.Bson;

class Program 
{
    static void Main() 
    {
        try 
        {
            var connectionString = "$ConnectionString";
            var client = new MongoClient(connectionString);
            var database = client.GetDatabase("$ATLAS_DATABASE");
            
            Console.WriteLine("✅ Successfully connected to MongoDB Atlas!");
            Console.WriteLine("📊 Database: " + database.DatabaseNamespace.DatabaseName);
            
            // Test ping
            var pingCommand = new BsonDocument("ping", 1);
            var result = database.RunCommand<BsonDocument>(pingCommand);
            Console.WriteLine("🏓 Ping result: " + result["ok"]);
            
            // List collections
            var collections = database.ListCollectionNames().ToList();
            Console.WriteLine("📂 Collections count: " + collections.Count);
            
            foreach(var collection in collections) 
            {
                Console.WriteLine("   - " + collection);
            }
            
            // Test basic operations
            var testCollection = database.GetCollection<BsonDocument>("connection_test");
            var testDoc = new BsonDocument 
            {
                { "message", "WOLTAXI Atlas Connection Test" },
                { "timestamp", DateTime.UtcNow },
                { "version", "2.0.0" }
            };
            
            testCollection.InsertOne(testDoc);
            Console.WriteLine("✅ Write test successful!");
            
            var retrievedDoc = testCollection.Find(new BsonDocument()).FirstOrDefault();
            if (retrievedDoc != null) 
            {
                Console.WriteLine("✅ Read test successful!");
                Console.WriteLine("📝 Message: " + retrievedDoc["message"]);
            }
            
            // Cleanup
            testCollection.DeleteMany(new BsonDocument());
            
            Console.WriteLine("🎉 All tests passed!");
        }
        catch (Exception ex) 
        {
            Console.WriteLine("❌ Connection failed: " + ex.Message);
            if (ex.InnerException != null) 
            {
                Console.WriteLine("Inner exception: " + ex.InnerException.Message);
            }
        }
    }
}
"@

    # Save test code to temporary file
    $tempCsFile = [System.IO.Path]::GetTempFileName() + ".cs"
    $testCode | Out-File -FilePath $tempCsFile -Encoding UTF8
    
    Write-Host "💾 Created test file: $tempCsFile" -ForegroundColor Gray
    
    # Try to compile and run (requires .NET SDK and MongoDB.Driver NuGet package)
    try {
        Write-Host "⚠️  Note: This test requires .NET SDK and MongoDB.Driver package" -ForegroundColor Yellow
        Write-Host "For a complete test, please install MongoDB Shell or use MongoDB Compass" -ForegroundColor Yellow
    }
    finally {
        # Cleanup
        if (Test-Path $tempCsFile) {
            Remove-Item $tempCsFile -Force
        }
    }
}

function Test-AtlasConnectionWithHTTP {
    param([string]$ClusterUrl)
    
    Write-Host "🌐 Testing Atlas cluster reachability..." -ForegroundColor Yellow
    
    try {
        # Test if we can reach the cluster (basic connectivity)
        $testUrl = "https://$ClusterUrl"
        $response = Invoke-WebRequest -Uri $testUrl -TimeoutSec 10 -ErrorAction Stop
        Write-Host "✅ Cluster is reachable" -ForegroundColor Green
    }
    catch {
        if ($_.Exception.Message -like "*SSL*" -or $_.Exception.Message -like "*TLS*") {
            Write-Host "✅ Cluster is reachable (SSL/TLS connection available)" -ForegroundColor Green
        }
        else {
            Write-Host "⚠️  Cluster connectivity test: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
}

function Show-ConnectionStringInfo {
    param([string]$ConnectionString)
    
    Write-Host "🔍 Connection String Analysis:" -ForegroundColor Magenta
    Write-Host "==============================" -ForegroundColor Magenta
    
    if ($ConnectionString -match "mongodb\+srv://([^:]+):([^@]+)@([^/]+)/(.*)") {
        $username = $Matches[1]
        $password = $Matches[2] -replace '.', '*'
        $cluster = $Matches[3]
        $params = $Matches[4]
        
        Write-Host "👤 Username: $username" -ForegroundColor Cyan
        Write-Host "🔐 Password: $password" -ForegroundColor Cyan
        Write-Host "🖥️  Cluster: $cluster" -ForegroundColor Cyan
        Write-Host "⚙️  Parameters: $params" -ForegroundColor Cyan
    }
    
    Write-Host "`n📋 Connection String Format Validation:" -ForegroundColor Magenta
    
    $checks = @(
        @{ Name = "Starts with mongodb+srv://"; Pass = $ConnectionString.StartsWith("mongodb+srv://") },
        @{ Name = "Contains username"; Pass = $ConnectionString -match "://[^:]+:" },
        @{ Name = "Contains password"; Pass = $ConnectionString -match "://[^:]+:[^@]+@" },
        @{ Name = "Contains cluster address"; Pass = $ConnectionString -match "@[^/]+" },
        @{ Name = "Has retryWrites parameter"; Pass = $ConnectionString -like "*retryWrites=true*" },
        @{ Name = "Has w=majority parameter"; Pass = $ConnectionString -like "*w=majority*" }
    )
    
    foreach ($check in $checks) {
        $status = if ($check.Pass) { "✅" } else { "❌" }
        $color = if ($check.Pass) { "Green" } else { "Red" }
        Write-Host "$status $($check.Name)" -ForegroundColor $color
    }
}

function Main {
    Write-Host "🌐 WOLTAXI MongoDB Atlas Connection Tester" -ForegroundColor Magenta
    Write-Host "==========================================" -ForegroundColor Magenta
    
    if ([string]::IsNullOrEmpty($Password)) {
        Write-Host "❌ Password is required!" -ForegroundColor Red
        Write-Host "Usage: .\atlas-test.ps1 -Password 'your-db-password'" -ForegroundColor Yellow
        return
    }
    
    # Build full connection string
    $fullConnectionString = "$ATLAS_CONNECTION_BASE`:$Password@$ATLAS_CLUSTER/$ATLAS_DATABASE" + "?retryWrites=true&w=majority"
    
    # Show connection info (without revealing password)
    $safeConnectionString = $fullConnectionString -replace ":[^@]+@", ":****@"
    Write-Host "🔗 Testing connection to: $safeConnectionString" -ForegroundColor Cyan
    
    # Analyze connection string
    Show-ConnectionStringInfo -ConnectionString $fullConnectionString
    
    # Test cluster reachability
    Test-AtlasConnectionWithHTTP -ClusterUrl $ATLAS_CLUSTER
    
    # Additional info
    Write-Host "`n💡 Next Steps:" -ForegroundColor Yellow
    Write-Host "1. Install MongoDB Shell: https://docs.mongodb.com/mongodb-shell/install/" -ForegroundColor Gray
    Write-Host "2. Install MongoDB Compass: https://www.mongodb.com/products/compass" -ForegroundColor Gray
    Write-Host "3. Test with mongosh: mongosh `"$safeConnectionString`"" -ForegroundColor Gray
    
    Write-Host "`n📊 Spring Boot Configuration:" -ForegroundColor Yellow
    Write-Host "Add this to your application.yml:" -ForegroundColor Gray
    Write-Host @"
spring:
  data:
    mongodb:
      uri: $safeConnectionString
      database: $ATLAS_DATABASE
"@ -ForegroundColor Gray

    Write-Host "`n🔧 Environment Variable:" -ForegroundColor Yellow
    Write-Host "ATLAS_PASSWORD=$Password" -ForegroundColor Gray
    
    Write-Host "`n🎯 WOLTAXI Collections to expect:" -ForegroundColor Yellow
    $expectedCollections = @("users", "user_profiles", "drivers", "rides", "payments")
    foreach ($collection in $expectedCollections) {
        Write-Host "   - $collection" -ForegroundColor Gray
    }
}

# Execute main function
Main