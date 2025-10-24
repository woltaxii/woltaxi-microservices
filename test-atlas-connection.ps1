# WOLTAXI Atlas Bağlantı Test Scripti
Write-Host "🧪 WOLTAXI Atlas Bağlantı Testi" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

# .env.atlas dosyasını oku
if (Test-Path ".env.atlas") {
    Write-Host "✅ .env.atlas dosyası bulundu" -ForegroundColor Green
    
    # Gerçek connection string'i göster
    Write-Host "`n🔗 Atlas Connection String:" -ForegroundColor Yellow
    Write-Host "mongodb+srv://woltaxi_db_user:20Cemilmanap22@cluster0.q9ezcyu.mongodb.net/woltaxi_users" -ForegroundColor Gray
    
    # MongoDB Compass için connection string
    Write-Host "`n📊 MongoDB Compass için:" -ForegroundColor Yellow
    Write-Host "mongodb+srv://woltaxi_db_user:20Cemilmanap22@cluster0.q9ezcyu.mongodb.net/" -ForegroundColor Gray
    
    # DNS test
    Write-Host "`n🔍 DNS Test:" -ForegroundColor Yellow
    try {
        $dnsTest = nslookup -type=SRV _mongodb._tcp.cluster0.q9ezcyu.mongodb.net 2>$null
        if ($dnsTest -match "mongodb") {
            Write-Host "✅ Atlas cluster DNS çözümlendi" -ForegroundColor Green
        }
    }
    catch {
        Write-Host "⚠️  DNS test tamamlanamadı" -ForegroundColor Yellow
    }
    
    Write-Host "`n🎯 Sonraki Adımlar:" -ForegroundColor Magenta
    Write-Host "1. MongoDB Compass indirin: https://www.mongodb.com/try/download/compass" -ForegroundColor Gray
    Write-Host "2. Yukarıdaki connection string'i kopyalayın" -ForegroundColor Gray
    Write-Host "3. Compass'ta 'Connect' ile bağlanın" -ForegroundColor Gray
    Write-Host "4. woltaxi_users database'ini göreceksiniz" -ForegroundColor Gray
    
} else {
    Write-Host "❌ .env.atlas dosyası bulunamadı" -ForegroundColor Red
}

Write-Host "`n🎉 Atlas hazır! Compass ile test edin!" -ForegroundColor Green