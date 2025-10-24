# WOLTAXI MongoDB Atlas Manuel Kurulum Rehberi
# Bu dosya Atlas'a bağlanmak için gereken tüm adımları içerir

# =============================================================================
# 🎯 ATLAS KURULUM ADIMLARI
# =============================================================================

## 1. GEREKLI ARAÇLARI YÜKLEYIN

### A) MongoDB Compass (Tavsiye Edilen)
1. https://www.mongodb.com/try/download/compass adresine gidin
2. Windows için MongoDB Compass'ı indirin
3. Kurulum dosyasını çalıştırın
4. Connection string: mongodb+srv://woltaxi_db_user:<PASSWORD>@cluster0.q9ezcyu.mongodb.net/woltaxi_users

### B) MongoDB Shell (mongosh) - Alternatif
1. https://docs.mongodb.com/mongodb-shell/install/ adresine gidin
2. Windows için mongosh indirin
3. PATH'e ekleyin
4. Test komutu: mongosh "mongodb+srv://woltaxi_db_user:<PASSWORD>@cluster0.q9ezcyu.mongodb.net/woltaxi_users"

### C) Java Development Kit (JDK) 21
1. https://adoptium.net/ adresine gidin
2. JDK 21 (LTS) indirin
3. JAVA_HOME environment variable'ını ayarlayın

### D) Apache Maven
1. https://maven.apache.org/download.cgi adresine gidin
2. Maven 3.9.x indirin
3. PATH'e ekleyin

### E) Docker Desktop (Opsiyonel ama Tavsiye Edilen)
1. https://www.docker.com/products/docker-desktop adresine gidin
2. Windows için Docker Desktop indirin
3. WSL 2 backend'i etkinleştirin

# =============================================================================
# 🔐 ATLAS BAĞLANTI AYARLARI
# =============================================================================

## Atlas Connection String Format:
mongodb+srv://woltaxi_db_user:<PASSWORD>@cluster0.q9ezcyu.mongodb.net/<DATABASE>?retryWrites=true&w=majority

## WOLTAXI Veritabanları:
- woltaxi_users      (Kullanıcı bilgileri)
- woltaxi_drivers    (Şoför bilgileri)
- woltaxi_rides      (Yolculuk kayıtları)
- woltaxi_payments   (Ödeme bilgileri)
- woltaxi_analytics  (Analitik veriler)
- woltaxi_emergency  (Acil durum kayıtları)

# =============================================================================
# 🧪 ATLAS BAĞLANTI TESTİ
# =============================================================================

## PowerShell ile DNS Testi (Bu çalışıyor ✅):
nslookup -type=SRV _mongodb._tcp.cluster0.q9ezcyu.mongodb.net

## MongoDB Compass ile Test:
1. MongoDB Compass'ı açın
2. "New Connection" tıklayın
3. Connection string'i yapıştırın
4. <PASSWORD> yerine gerçek password'ünüzü yazın
5. "Connect" tıklayın

## mongosh ile Test (MongoDB Shell yüklendikten sonra):
mongosh "mongodb+srv://woltaxi_db_user:<PASSWORD>@cluster0.q9ezcyu.mongodb.net/woltaxi_users"

# =============================================================================
# 🚀 WOLTAXI SERVİSLERİNİ ÇALIŞTIRMA
# =============================================================================

## 1. Environment Variables Ayarlayın:
# .env.atlas dosyasına gerçek password'ünüzü yazın:
ATLAS_PASSWORD=your-real-password-here

## 2. User Service'i Atlas ile Çalıştırın:
cd user-service
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.profiles=atlas

## 3. Docker ile Çalıştırma (Docker yüklüyse):
docker-compose up -d
# veya sadece belirli servisleri:
docker-compose up -d user-service

# =============================================================================
# 📊 ATLAS'TA GÖREBILECEĞINIZ WOLTAXI VERİLERİ
# =============================================================================

## woltaxi_users Database:
```
db.users.find({}).limit(5)           // Kullanıcı listesi
db.user_profiles.find({}).limit(5)   // Profil bilgileri
db.user_sessions.find({}).limit(5)   // Aktif oturumlar
```

## woltaxi_drivers Database:
```
db.drivers.find({}).limit(5)         // Şoför listesi
db.vehicles.find({}).limit(5)        // Araç bilgileri
db.driver_locations.find({}).limit(5) // Lokasyon verileri
```

## woltaxi_rides Database:
```
db.rides.find({}).limit(5)           // Yolculuk kayıtları
db.ride_requests.find({}).limit(5)   // Yolculuk talepleri
db.ride_tracking.find({}).limit(5)   // Gerçek zamanlı takip
```

# =============================================================================
# 🔧 SORUN GİDERME
# =============================================================================

## Yaygın Sorunlar ve Çözümleri:

### 1. "Authentication Failed" Hatası:
- Username: woltaxi_db_user (doğru)
- Password'ü kontrol edin
- Atlas'ta IP whitelist kontrolü yapın

### 2. "Network Error" Hatası:
- İnternet bağlantınızı kontrol edin
- Firewall ayarlarını kontrol edin
- Atlas cluster'ın aktif olduğunu doğrulayın

### 3. "Database Not Found" Hatası:
- Database otomatik oluşturulacak
- İlk document insert'inde oluşur

### 4. "Connection Timeout":
- Atlas'ta Network Access ayarlarını kontrol edin
- IP adresinizi whitelist'e ekleyin
- 0.0.0.0/0 (tüm IP'ler) geçici test için

# =============================================================================
# 📈 ATLAS MONİTORİNG
# =============================================================================

## Atlas Dashboard'ta İzleyebilecekleriniz:
1. Real-time Metrics
   - CPU kullanımı
   - Memory kullanımı
   - Network I/O
   - Disk kullanımı

2. Database Metrics
   - Collection sayısı
   - Document sayısı
   - Index kullanımı
   - Query performance

3. Connection Metrics
   - Aktif bağlantı sayısı
   - Connection pool durumu
   - Slow query'ler

4. Backup Status
   - Otomatik backup durumu
   - Point-in-time recovery
   - Backup boyutu

# =============================================================================
# 🎉 BAŞARI ÇIKILARI
# =============================================================================

## Atlas bağlantısı başarılı olduğunda göreceğiniz çıktılar:

### MongoDB Compass:
- Sol panelde veritabanları listelenir
- Collections görünür
- Document'ler görüntülenebilir

### Spring Boot Console:
```
✅ Connected to MongoDB Atlas!
📊 Database: woltaxi_users
✅ Write test successful!
✅ Read test successful!
🎉 MongoDB Atlas Connection Test Completed Successfully!
```

### mongosh Console:
```
Current Mongosh Log ID: xxxxx
Connecting to: mongodb+srv://cluster0.q9ezcyu.mongodb.net/woltaxi_users...
Using MongoDB: 7.0.x
Atlas atlas-xxxxx-shard-0 [primary] woltaxi_users>
```

# =============================================================================
# 🔄 SONRAKI ADIMLAR
# =============================================================================

1. ✅ Atlas cluster'ı test edildi (SRV records bulundu)
2. 🔧 Gerekli araçları yükleyin (Compass/mongosh/Java/Maven)
3. 🔐 .env.atlas dosyasına gerçek password yazın
4. 🧪 MongoDB Compass ile bağlantıyı test edin
5. 🚀 WOLTAXI servislerini çalıştırın
6. 📊 Atlas Dashboard'tan performansı izleyin

# =============================================================================
# 📞 YARDIM GEREKİRSE
# =============================================================================

Bu rehberi takip ettikten sonra:
- Hangi adımda sorun yaşadığınızı belirtin
- Aldığınız hata mesajlarını paylaşın
- Atlas Dashboard'taki durumu kontrol edin
- WOLTAXI servisleri loglarını inceleyin

Başarılar! 🚀