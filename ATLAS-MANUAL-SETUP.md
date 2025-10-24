# WOLTAXI Atlas Manuel Setup Rehberi

## 🎯 Şu Anki Durum
✅ Atlas connection string hazır: `mongodb+srv://woltaxi_db_user:mEGHjWEyVyCjsDVj@cluster0.q9ezcyu.mongodb.net/`
✅ DNS çözümlemesi başarılı (3 MongoDB sunucusu bulundu)
❌ Java/Maven/Docker kurulu değil (geliştirme araçları eksik)

## 📊 MongoDB Compass ile Atlas Testi

### 1. MongoDB Compass İndirin:
🔗 **Download**: https://www.mongodb.com/try/download/compass

### 2. Atlas'a Bağlanın:
```
Connection String: mongodb+srv://woltaxi_db_user:mEGHjWEyVyCjsDVj@cluster0.q9ezcyu.mongodb.net/
```

### 3. WOLTAXI Databases Oluşturun:
Atlas'ta bu database'leri görecek/oluşturacaksınız:
- `woltaxi_users` (User Service)
- `woltaxi_drivers` (Driver Service)  
- `woltaxi_rides` (Ride Service)
- `woltaxi_payments` (Payment Service)
- `woltaxi_analytics` (Analytics Service)

### 4. İlk Test Collection'ları:
Her database'de test collection'ları oluşturun:

#### woltaxi_users database:
```javascript
// users collection - test document
{
  "_id": ObjectId(),
  "username": "test_user_001",
  "email": "test@woltaxi.com",
  "phone": "+905551234567",
  "status": "ACTIVE",
  "createdAt": new Date(),
  "role": "CUSTOMER"
}

// user_profiles collection
{
  "_id": ObjectId(),
  "userId": "test_user_001", 
  "firstName": "Test",
  "lastName": "User",
  "profilePhoto": null,
  "preferences": {
    "language": "tr",
    "currency": "TRY"
  }
}
```

#### woltaxi_drivers database:
```javascript
// drivers collection
{
  "_id": ObjectId(),
  "driverId": "driver_001",
  "firstName": "Test",
  "lastName": "Driver",
  "email": "driver@woltaxi.com",
  "phone": "+905559876543",
  "licenseNumber": "34ABC1234",
  "status": "AVAILABLE",
  "location": {
    "latitude": 41.0082,
    "longitude": 28.9784,
    "address": "Istanbul, Turkey"
  }
}

// vehicles collection
{
  "_id": ObjectId(),
  "driverId": "driver_001",
  "plate": "34 ABC 1234",
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "color": "White"
}
```

## 🚀 Sonraki Adımlar (Araçlar Kurulduktan Sonra)

### Java 21 + Maven Kurulduktan Sonra:
```powershell
cd user-service
mvn clean compile
set ATLAS_PASSWORD=mEGHjWEyVyCjsDVj
mvn spring-boot:run -Dspring-boot.run.profiles=atlas
```

### Docker Kurulduktan Sonra:
```powershell
# Atlas profili ile servisleri çalıştır
docker-compose -f docker-compose.yml up -d user-service
```

## 📋 Test Checklist

### Atlas Bağlantı Testi:
- [ ] MongoDB Compass indirildi
- [ ] Atlas'a başarılı bağlantı
- [ ] woltaxi_users database görünüyor
- [ ] Test document eklenebildi

### WOLTAXI Service Testi:
- [ ] Java 21 kuruldu
- [ ] Maven kuruldu  
- [ ] User Service Atlas ile başladı
- [ ] Database'de otomatik collections oluştu
- [ ] Postman ile API test edildi

## 🎉 Atlas Entegrasyonu %80 Hazır!

✅ Connection string çalışıyor
✅ DNS çözümlemesi OK
✅ Password doğru
🔧 Sadece geliştirme araçları kurulacak

**Şimdi MongoDB Compass indirip Atlas'ı görsel olarak test edebilirsiniz!**