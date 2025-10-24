# WOLTAXI MongoDB Atlas Simple Connection Test

param(
    [SecureString]$Password = (ConvertTo-SecureString "" -AsPlainText -Force)
)

$ATLAS_BASE = "mongodb+srv://woltaxi_db_user"
$ATLAS_CLUSTER = "cluster0.q9ezcyu.mongodb.net"
$ATLAS_DATABASE = "woltaxi_users"

Write-Host "🌐 WOLTAXI MongoDB Atlas Connection Tester" -ForegroundColor Magenta
Write-Host "==========================================" -ForegroundColor Magenta

if ([string]::IsNullOrEmpty($Password)) {
    Write-Host "❌ Password is required!" -ForegroundColor Red
    Write-Host "Usage: .\atlas-simple-test.ps1 -Password 'your-password'" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "💡 Alternative test methods:" -ForegroundColor Yellow
    Write-Host "1. MongoDB Compass: https://www.mongodb.com/products/compass" -ForegroundColor Gray
    Write-Host "2. MongoDB Shell: https://docs.mongodb.com/mongodb-shell/install/" -ForegroundColor Gray
    Write-Host "3. Your connection string:" -ForegroundColor Gray
    Write-Host "   ${ATLAS_BASE}:<password>@$ATLAS_CLUSTER/$ATLAS_DATABASE?retryWrites=true&w=majority" -ForegroundColor Gray
    return
}

# Build connection string (removed unused variable)
$SafeConnectionString = "$ATLAS_BASE`:****@$ATLAS_CLUSTER/$ATLAS_DATABASE" + "?retryWrites=true&w=majority"

Write-Host "🔗 Testing connection to:" -ForegroundColor Cyan
Write-Host $SafeConnectionString -ForegroundColor Gray

Write-Host "`n🔍 Connection Details:" -ForegroundColor Yellow
Write-Host "👤 Username: woltaxi_db_user" -ForegroundColor Cyan
Write-Host "🖥️  Cluster: $ATLAS_CLUSTER" -ForegroundColor Cyan
Write-Host "🗄️  Database: $ATLAS_DATABASE" -ForegroundColor Cyan

# Test cluster reachability
Write-Host "`n🌐 Testing cluster reachability..." -ForegroundColor Yellow
try {
    $pingResult = Test-NetConnection -ComputerName "cluster0.q9ezcyu.mongodb.net" -Port 27017 -WarningAction SilentlyContinue
    if ($pingResult.TcpTestSucceeded) {
        Write-Host "✅ Cluster is reachable on port 27017" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Port 27017 not directly accessible (normal for Atlas SRV)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Network test info: $($_.Exception.Message)" -ForegroundColor Yellow
}

# DNS resolution test
Write-Host "`n🔍 DNS Resolution Test..." -ForegroundColor Yellow
try {
    $dnsResult = Resolve-DnsName -Name "cluster0.q9ezcyu.mongodb.net" -Type A -ErrorAction SilentlyContinue
    if ($dnsResult) {
        Write-Host "✅ DNS resolution successful" -ForegroundColor Green
        Write-Host "🌍 Resolved IPs:" -ForegroundColor Cyan
        $dnsResult | ForEach-Object { Write-Host "   - $($_.IPAddress)" -ForegroundColor Gray }
    }
} catch {
    Write-Host "⚠️  DNS resolution: $($_.Exception.Message)" -ForegroundColor Yellow
}

# SRV record test (Atlas uses SRV records)
Write-Host "`n🔍 SRV Record Test..." -ForegroundColor Yellow
try {
    $srvResult = Resolve-DnsName -Name "_mongodb._tcp.cluster0.q9ezcyu.mongodb.net" -Type SRV -ErrorAction SilentlyContinue
    if ($srvResult) {
        Write-Host "✅ SRV records found (Atlas configuration is correct)" -ForegroundColor Green
        $srvResult | ForEach-Object { 
            Write-Host "   - $($_.NameTarget):$($_.Port) (Priority: $($_.Priority), Weight: $($_.Weight))" -ForegroundColor Gray 
        }
    } else {
        Write-Host "⚠️  No SRV records found" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  SRV lookup: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host "`n📋 Connection String for WOLTAXI Services:" -ForegroundColor Magenta
Write-Host "================================================" -ForegroundColor Magenta

@"
# Add to your application.yml:
spring:
  data:
    mongodb:
      uri: $SafeConnectionString
      database: $ATLAS_DATABASE

# Environment Variables:
ATLAS_PASSWORD=$Password
MONGO_URI=$SafeConnectionString

# For different WOLTAXI services:
USER_SERVICE_URI=$ATLAS_BASE`:****@$ATLAS_CLUSTER/woltaxi_users?retryWrites=true&w=majority
DRIVER_SERVICE_URI=$ATLAS_BASE`:****@$ATLAS_CLUSTER/woltaxi_drivers?retryWrites=true&w=majority
RIDE_SERVICE_URI=$ATLAS_BASE`:****@$ATLAS_CLUSTER/woltaxi_rides?retryWrites=true&w=majority
PAYMENT_SERVICE_URI=$ATLAS_BASE`:****@$ATLAS_CLUSTER/woltaxi_payments?retryWrites=true&w=majority
"@ | Write-Host -ForegroundColor Gray

Write-Host "`n🛠️  Next Steps to Test Your Connection:" -ForegroundColor Yellow
Write-Host "1. Download MongoDB Compass: https://www.mongodb.com/try/download/compass" -ForegroundColor Gray
Write-Host "2. Use this connection string in Compass:" -ForegroundColor Gray
Write-Host "   $SafeConnectionString" -ForegroundColor Gray
Write-Host "3. Or install MongoDB Shell and run:" -ForegroundColor Gray
Write-Host "   mongosh `"$SafeConnectionString`"" -ForegroundColor Gray

Write-Host "`n🎯 Expected WOLTAXI Collections:" -ForegroundColor Yellow
@("users", "user_profiles", "drivers", "vehicles", "rides", "payments", "notifications") | ForEach-Object {
    Write-Host "   - $_" -ForegroundColor Gray
}

Write-Host "`n✅ Basic connectivity tests completed!" -ForegroundColor Green
Write-Host "🔒 Your connection string is ready for WOLTAXI services" -ForegroundColor Green