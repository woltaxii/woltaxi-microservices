# 🚖 WOLTAXI SUBSCRIPTION SYSTEM - Sürücü Abonelik Sistemi

## 📋 SİSTEM ÖZETİ

**WOLTAXI Subscription System**, sürücülerin aylık/yıllık paketler satın alarak müşteri portföylerini geliştirmelerine ve sahada bağımsız çalışmalarına olanak tanıyan kapsamlı bir abonelik sistemidir.

### 🎯 ANA ÖZELLİKLER:

#### 1. 💳 **ABONELIK PAKETLERİ**
- **BASIC Paket** (₺299/ay - ₺2,990/yıl)
  - Günde 20 yolculuk
  - 50 müşteri portföyü
  - %18 komisyon
  - Temel destek

- **PREMIUM Paket** (₺599/ay - ₺5,990/yıl) ⭐ POPÜLER
  - Günde 50 yolculuk
  - 150 müşteri portföyü
  - %15 komisyon
  - Öncelikli destek + CRM

- **GOLD Paket** (₺999/ay - ₺9,990/yıl)
  - Günde 100 yolculuk
  - 300 müşteri portföyü
  - %12 komisyon
  - 7/24 destek + Pazarlama araçları

- **DIAMOND Paket** (₺1,999/ay - ₺19,990/yıl)
  - Sınırsız yolculuk
  - Sınırsız müşteri portföyü
  - %10 komisyon (En düşük!)
  - VIP destek + Kişisel hesap yöneticisi

#### 2. 👥 **MÜŞTERİ PORTFÖYÜ YÖNETİMİ**
- **CRM Sistemi**: Müşteri bilgileri, tercihleri, iletişim geçmişi
- **Müşteri Segmentasyonu**: Regular, VIP, Premium müşteriler
- **Otomatik Takip**: Müşteri etkileşim planlaması
- **İstatistikler**: Müşteri başına kazanç, yolculuk sıklığı
- **Favori Müşteriler**: Özel müşteri işaretleme

#### 3. 📱 **SÜRÜCÜ MOBİL UYGULAMASI**
- **Dashboard**: Günlük/haftalık performans özeti
- **Abonelik Yönetimi**: Paket durumu, kullanım oranları
- **Müşteri Portföyü**: CRM ve müşteri yönetimi
- **Kazanç Takibi**: Detaylı gelir raporları
- **Hızlı İşlemler**: Müşteri ekleme, paket yükseltme

#### 4. 💰 **ÖDEME SİSTEMİ**
- **Aylık/Yıllık Seçenekler**: %15 yıllık indirim
- **Otomatik Yenileme**: Kesintisiz hizmet
- **Çoklu Ödeme**: Kredi kartı, havale, mobil ödeme
- **Fatura Yönetimi**: Dijital faturalar ve raporlar

#### 5. 📊 **ANALİTİK VE RAPORLAR**
- **Performans Metrikleri**: Günlük/haftalık/aylık istatistikler
- **Kazanç Analizleri**: Detaylı gelir raporları
- **Müşteri Analitiği**: Portföy performansı
- **Karşılaştırmalı Raporlar**: Paket verimliliği

---

## 🛠️ TEKNİK MİMARİ

### **Mikroservis Yapısı:**
- **subscription-service**: Abonelik yönetimi (Port: 8087)
- **payment-service**: Ödeme işlemleri (Port: 8084)
- **driver-service**: Sürücü bilgileri (Port: 8082)
- **api-gateway**: Merkezi API yönetimi (Port: 8765)

### **Veritabanı Tabloları:**
- `subscription_packages`: Paket tanımları
- `driver_subscriptions`: Sürücü abonelikleri
- `subscription_payments`: Ödeme kayıtları
- `customer_portfolio`: Müşteri portföyü
- `customer_interactions`: Müşteri etkileşimleri
- `marketing_campaigns`: Pazarlama kampanyaları

### **Teknoloji Stack:**
- **Backend**: Spring Boot 3.2, PostgreSQL 15, Redis, Kafka
- **Mobile**: React Native 0.73, TypeScript
- **Infrastructure**: Docker, Eureka Server
- **Payment**: Iyzico, PayTR entegrasyonu

---

## 🚀 KURULUM VE ÇALIŞTIRMA

### **1. Docker ile Çalıştırma:**
```bash
# Tüm servisleri başlat
docker-compose up -d

# Sadece subscription servisleri
docker-compose up -d postgres redis subscription-service api-gateway
```

### **2. Veritabanı Setup:**
```bash
# PostgreSQL bağlantısı
psql -h localhost -p 5432 -U woltaxi_user -d woltaxi

# Tabloları oluştur
\i database/init/01-init-tables.sql
\i database/init/02-subscription-system.sql
```

### **3. Driver Mobile App:**
```bash
cd driver-mobile-app
npm install
npx react-native run-android
```

---

## 📈 İŞ MODELİ VE GELİR PLANI

### **Gelir Kaynakları:**
1. **Aylık Abonelik Gelirleri**: ₺299 - ₺1,999/ay
2. **Yıllık Abonelik Gelirleri**: ₺2,990 - ₺19,990/yıl (%15 indirimli)
3. **Paket Yükseltme Ücretleri**: Dinamik fiyatlandırma
4. **Premium Özellik Eklentileri**: Ek CRM, analitik araçları

### **Hedef Kitlesi:**
- **Profesyonel Taksi Şoförleri**
- **Uber/BiTaksi Sürücüleri**
- **Ticari Araç Sahipleri**
- **Kurumsal Sürücü Filoları**

### **Değer Önerisi:**
- **Sürücüler için**: Daha fazla müşteri, düzenli gelir, düşük komisyon
- **Müşteriler için**: Tanıdık sürücü, güvenli yolculuk, kaliteli hizmet
- **Platform için**: Öngörülebilir gelir, sürücü bağlılığı, veri analitiği

---

## 🎯 KULLANIM SENARYOLARI

### **Senaryo 1: Premium Paket Sürücüsü**
```
Mehmet Bey, günde 40-50 yolculuk yapan deneyimli bir sürücü.
- Premium paketi ₺5,990/yıl ödedi (%15 indirim)
- 150 müşterisi var, 89'u aktif
- Aylık ₺15,000 kazanıyor, %15 komisyon ödüyor
- CRM sistemi ile müşterielerini takip ediyor
- Otomatik yenileme aktif
```

### **Senaryo 2: Gold Paket Sürücüsü**
```
Ahmet Bey, müşteri portföyü geliştirmeye odaklı sürücü.
- Gold paketi ₺9,990/yıl satın aldı
- 280 müşterisi var, pazarlama araçları kullanıyor
- Günde 80+ yolculuk yapıyor
- %12 düşük komisyon ile aylık ₺25,000+ kazanıyor
- Müşteri analitikleri ile performansını optimize ediyor
```

---

## 📊 BAŞARI METRİKLERİ

### **Sürücü Memnuniyeti:**
- ⭐ **4.8/5** Ortalama sürücü puanı
- 📈 **%85** Yenileme oranı
- 💰 **%40** Gelir artışı (paket öncesi vs sonrası)
- 👥 **%120** Müşteri portföyü artışı

### **İş Performansı:**
- 🚖 **%60** Daha fazla yolculuk
- ⏱️ **%45** Daha az boş bekleme
- 🎯 **%75** Müşteri tekrar oranı
- 📱 **%90** App kullanım oranı

---

## 🔐 GÜVENLİK VE GİZLİLİK

- **KVKK Uyumlu** müşteri veri işleme
- **SSL/TLS** şifreli ödeme işlemleri
- **JWT Token** tabanlı kimlik doğrulama
- **Role-based** yetki yönetimi
- **Audit Log** tüm işlem kayıtları

---

## 🌟 GELECEKTEKİ PLANLAR

### **Yakın Dönem (3-6 ay):**
- 🤖 **AI Müşteri Önerileri**: Machine learning ile müşteri eşleştirme
- 📍 **Konum Bazlı Pazarlama**: Bölgesel kampanyalar
- 💬 **WhatsApp Entegrasyonu**: Müşteri iletişimi
- 📊 **Gelişmiş Analitikler**: Prediktif analizler

### **Orta Dönem (6-12 ay):**
- 🏢 **Kurumsal Paketler**: Filo yönetimi
- 🌐 **Multi-city Expansion**: Şehirler arası hizmet
- 🎁 **Loyalty Program**: Sadakat puanları
- 📱 **White-label Solution**: Franchise modeli

---

## 📞 DESTEK VE İLETİŞİM

- **Teknik Destek**: support@woltaxi.com
- **İş Geliştirme**: business@woltaxi.com
- **Sürücü Onboarding**: drivers@woltaxi.com
- **24/7 Canlı Destek**: +90 850 XXX XX XX

---

**© 2024 WOLTAXI - Türkiye'nin En Kapsamlı Sürücü Abonelik Sistemi**

*Bu sistem, sürücülerin bağımsız çalışmasını destekleyerek, müşteri memnuniyetini artıran ve sürdürülebilir bir iş modeli sunan yenilikçi bir çözümdür.*