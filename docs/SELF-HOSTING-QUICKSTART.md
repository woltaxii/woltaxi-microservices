# WOLTAXI Self-Hosting (Windows) – Quickstart

Bu hızlı başlangıç, Windows PC’nizi WOLTAXI API’lerini barındıracak şekilde, admin yetkisi gerektirmeden (portable araçlarla) çalıştırmanızı hedefler. HTTPS + domain için Caddy adımı admin ister.

## 1) Portable JDK 21 + Maven

```powershell
cd C:\Users\Lon\Documents\woltaxi-microservices
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup-portable-java-maven.ps1
```

Bu komut .tools altında JDK ve Maven’ı indirip açar. JAVA_HOME/MAVEN_HOME yolları .tools\*\_HOME.txt dosyalarına yazılır.

## 2) Build (Eureka + API Gateway)

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-services-portable.ps1
```

Başarılıysa JAR’lar `eureka-server/target` ve `api-gateway/target` altına oluşur.

## 3) Çalıştır (Yerelde)

İki ayrı PowerShell penceresi açın:

```powershell
# Pencere-1: Eureka (8761)
$JAVA_HOME = Get-Content .\.tools\JAVA_HOME.txt
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-eureka.ps1 -JavaHome $JAVA_HOME
```

```powershell
# Pencere-2: API Gateway (8765)
$JAVA_HOME = Get-Content .\.tools\JAVA_HOME.txt
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-gateway.ps1 -JavaHome $JAVA_HOME
```

Doğrulama:
- Eureka: http://localhost:8761
- Gateway: http://localhost:8765

## 4) Domain + HTTPS (Opsiyonel, Admin gerekir)

Caddy ile otomatik TLS ve reverse proxy (80/443 → 8765):

```powershell
# Yönetici PowerShell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup-caddy.ps1
```

DNS:
- A: @ → (Dış IP’niz)
- A: api → (Dış IP’niz)
- CNAME: www → @

Router NAT:
- 80 ve 443’ü bu PC’ye forward edin.

Doğrulama:
- https://woltaxi.com (otomatik sertifika ve 8765’e proxy)

## 5) Sık Karşılaşılan Sorunlar
- JDK/Maven bulunamadı: Önce 1. adımı çalıştırın; .tools altındaki txt dosyalarından yolları çekin.
- Portlar kapalı: Güvenlik Duvarında 8761/8765 izinli mi? 80/443 dış erişime açık mı?
- Sertifika alınmıyor: DNS yönlendirmesi tamamlandı mı? Router’da 80/443 NAT var mı? ISP 80/443’ü engelliyor mu?

## 6) İleri Seviye
- Redis (rate-limit) ve diğer mikroservisleri de build/run ederek Eureka’ya kaydedip Gateway üzerinden dışarı açabilirsiniz.
- Ayrıntılı kurulum rehberi: `docs/SERVER-SETUP-WINDOWS.md`
