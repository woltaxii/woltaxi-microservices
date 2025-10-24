# WOLTAXI Atlas Basit Test

Write-Host "🌐 WOLTAXI MongoDB Atlas Baslangic" -ForegroundColor Magenta
Write-Host "=================================" -ForegroundColor Magenta

# Mevcut durum kontrolu
Write-Host "`n🔧 Gerekli Araclar:" -ForegroundColor Yellow

$tools = @("java", "mvn", "docker", "mongosh")
foreach ($tool in $tools) {
    try {
        $null = Get-Command $tool -ErrorAction Stop
        Write-Host "✅ $tool - Yüklü" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ $tool - Yüklü değil" -ForegroundColor Red
    }
}

# Atlas bilgileri
Write-Host "`n🌐 Atlas Bilgileri:" -ForegroundColor Yellow
Write-Host "Cluster: cluster0.q9ezcyu.mongodb.net" -ForegroundColor Cyan
Write-Host "Username: woltaxi_db_user" -ForegroundColor Cyan
Write-Host "Connection String Format:" -ForegroundColor Cyan
Write-Host "mongodb+srv://woltaxi_db_user:<PASSWORD>@cluster0.q9ezcyu.mongodb.net/<DATABASE>" -ForegroundColor Gray

# SRV Test
Write-Host "`n🔍 Atlas Cluster Test:" -ForegroundColor Yellow
try {
    $srvResult = nslookup -type=SRV _mongodb._tcp.cluster0.q9ezcyu.mongodb.net 2>$null
    if ($srvResult -like "*mongodb.net*") {
        Write-Host "✅ Atlas cluster erişilebilir (SRV records bulundu)" -ForegroundColor Green
    }
}
catch {
    Write-Host "⚠️  SRV test tamamlanamadi" -ForegroundColor Yellow
}

# Dosya kontrolleri
Write-Host "`n📁 WOLTAXI Dosyalari:" -ForegroundColor Yellow
$files = @(".env.atlas", "ATLAS-SETUP-GUIDE.md", "user-service\pom.xml")
foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "✅ $file - Mevcut" -ForegroundColor Green
    } else {
        Write-Host "❌ $file - Bulunamadi" -ForegroundColor Red
    }
}

Write-Host "`n🎯 Sonraki Adimlar:" -ForegroundColor Magenta
Write-Host "1. .env.atlas dosyasina gercek password yazin" -ForegroundColor Gray
Write-Host "2. MongoDB Compass indirin: https://www.mongodb.com/try/download/compass" -ForegroundColor Gray
Write-Host "3. Connection string test edin" -ForegroundColor Gray
Write-Host "4. Java ve Maven yukleyin" -ForegroundColor Gray
Write-Host "5. WOLTAXI servislerini calistirin" -ForegroundColor Gray

Write-Host "`n📖 Detayli rehber: ATLAS-SETUP-GUIDE.md dosyasini okuyun" -ForegroundColor Cyan
Write-Host "🎉 Atlas hazir! Sadece araclari yukleyin ve baslayabilirsiniz!" -ForegroundColor Green