# WOLTAXI - Windows Sunucu Kurulumu (Kişisel Bilgisayarı Sunucuya Çevirme)

Bu rehber, Windows PC'nizi (bu makineyi) WOLTAXI API'lerini barındıran bir sunucuya dönüştürmek, domain (woltaxi.com) yönlendirmesi ve SSL ile 443 üzerinden yayına almak için adım adım yönergeler içerir.

## 1) Gerekli Bileşenler
- Java 21 JDK (Temurin önerilir)
- Maven (paketleme için)
- Caddy (Windows reverse proxy + Auto HTTPS)
- (İsteğe bağlı) Redis, MongoDB gibi bağımlılıklar
- Yönlendirici (Router) port iletimi: 80 ve 443 (WAN -> Bu PC)

Not: Bu repo script'leri sisteminize kurulum YAPMAZ; kurulum ve servisleştimeyi planlı, şeffaf şekilde yapmanız için script'ler hazırlanmıştır.

## 2) Build ve Servis Başlatma (Eureka + API Gateway)

1. PowerShell'i Yönetici olarak açın.
2. Java ve Maven yüklü olduğundan emin olun (winget veya manuel kurulum):
   - Java: https://adoptium.net
   - Maven: https://maven.apache.org/download.cgi
3. Servisleri build edip başlangıçta otomatik çalışacak görevleri oluşturun:

```powershell
# Yönetici PowerShell
cd C:\Users\Lon\Documents\woltaxi-microservices
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-and-register-services.ps1
```

Bu script:
- eureka-server ve api-gateway modüllerini `mvn package -DskipTests` ile derler
- Windows Güvenlik Duvarında 80/443/8761/8765 için inbound izinleri açar
- Başlangıçta çalışacak 2 görev oluşturur:
  - WOLTAXI-Eureka → `scripts/run-eureka.ps1`
  - WOLTAXI-API-Gateway → `scripts/run-gateway.ps1` (Redis zorunluluğunu geçici devre dışı bırakır)

## 3) Caddy Reverse Proxy ve SSL (HTTPS)

Caddy, domain için Let's Encrypt sertifikasını otomatik alır ve yeniler (80/443 açık ve domain A kaydı bu makineye işaret ediyorsa).

```powershell
# Yönetici PowerShell
cd C:\Users\Lon\Documents\woltaxi-microservices
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup-caddy.ps1
```

Bu script:
- Caddy'yi winget (yoksa choco) ile kurmaya çalışır
- `caddy\Caddyfile` dosyasını `C:\caddy\Caddyfile` konumuna kopyalar
- 80 ve 443 portlarını açar
- Başlangıçta Caddy'yi çalıştıran görev oluşturur: `WOLTAXI-Caddy`

Caddyfile, `woltaxi.com`, `www.woltaxi.com`, `api.woltaxi.com` için gelen trafiği `localhost:8765` (API Gateway) adresine yönlendirir.

## 4) DNS Ayarları (Natro / Registrar)

- A kaydı: `@` → (ev internetinizin harici IP adresi)
- A kaydı: `api` → (aynı harici IP)
- `www` için CNAME `@` veya A kaydı aynı IP
- TTL: 300s (5dk) veya 1 saat

IP adresinizi öğrenmek için: https://ifconfig.me veya Google: "what is my ip"

## 5) Router (Modem) Port Yönlendirme

- WAN → Bu PC'ye TCP 80 ve 443 portları forward edin.
- Bu PC'nin yerel IP adresini sabitleyin (DHCP Reservation). Örn: 192.168.1.50

## 6) Doğrulama

- `http://woltaxi.com` → Caddy 80 portunu dinlemeli, sertifika alıp `https://woltaxi.com`'a yönlenmeli
- `https://woltaxi.com/api/health` (varsa) veya Gateway health endpoint'leri çalışmalı
- Windows Görev Zamanlayıcı'da şu görevler `Ready/Running` durumda olmalı:
  - WOLTAXI-Eureka
  - WOLTAXI-API-Gateway
  - WOLTAXI-Caddy

## 7) Uygulama Servisleri (Opsiyonel Genişletme)

Gateway arkasında `lb://...` hedefleri Eureka'ya kayıtlı servislere gider. Aşağıdaki servisleri de build edip benzer şekilde başlatabilirsiniz:
- user-service, driver-service, ride-service, payment-hub-service, vb.

Ayrıca Redis (Rate Limiter) ve MongoDB (atlas veya lokal) konfigürasyonlarını tamamlayın.

## 8) Sorun Giderme
- Sertifika alınamıyorsa: 80/443 gerçekten dış dünyaya açık mı? (Router NAT ve ISP kısıtları)
- Domain DNS A kayıtları doğru IP'ye mi bakıyor? (Propagation 5dk–1saat)
- Firewall kuralları mevcut mu? `Get-NetFirewallRule | ? DisplayName -like 'WOLTAXI*'`
- Gateway 8765 dinliyor mu? `netstat -ano | findstr 8765`
- Eureka 8761 dinliyor mu? `http://localhost:8761`

## 9) Güvenlik Notları
- Admin parolası ve JWT secret'ları `.env` dosyalarında yönetin; repo'ya koymayın
- Uzak erişimler için brute-force koruması ve rate-limit (Redis) etkinleştirin
- Logları döndürme (log rotation) ve yedek almayı planlayın

---
Bu kurulum, kişisel bilgisayarı temel bir sunucuya dönüştürmek için minimum araçlarla (Caddy + Scheduled Tasks) tasarlanmıştır. Üretim ortamında bir bulut VM ve systemd (Linux) ile daha dayanıklı bir mimari önerilir.
