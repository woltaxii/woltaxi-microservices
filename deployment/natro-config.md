# 🌐 WOLTAXI NATRO HOSTING KONFIGÜRASYONU

## 🚀 **DEPLOYMENT ADIMI ADIMI**

### **1. NATRO CPANEL ERİŞİMİ**
```
URL: https://cpanel.natro.com
Kullanıcı: [natro-username]
Şifre: [natro-password]
```

### **2. DOMAIN AYARLARI**
- **Ana Domain:** woltaxi.com
- **Alt Domainler:**
  - api.woltaxi.com → /api/
  - admin.woltaxi.com → /admin/
  - driver.woltaxi.com → /driver/

### **3. SSL SERTİFİKASI**
```
Natro cPanel → SSL/TLS → Let's Encrypt
Domain: woltaxi.com
Auto-Renewal: ✅ Enabled
```

### **4. VERİTABANI KURULUMU**
```sql
-- MySQL Database Creation
CREATE DATABASE woltaxi_production;
CREATE USER 'woltaxi_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON woltaxi_production.* TO 'woltaxi_user'@'localhost';
FLUSH PRIVILEGES;
```

### **5. DOSYA YAPILANDIRMASI**
```
/public_html/
├── woltaxi-api-gateway.jar
├── static/
│   ├── css/
│   ├── js/
│   └── images/
├── templates/
└── .htaccess
```

### **6. .HTACCESS YAPILANDIRMASI**
```apache
# Force HTTPS
RewriteEngine On
RewriteCond %{HTTPS} off
RewriteRule ^(.*)$ https://%{HTTP_HOST}%{REQUEST_URI} [L,R=301]

# Java Application Proxy
RewriteRule ^api/(.*)$ http://localhost:8080/api/$1 [P,L]

# Static Files Cache
<FilesMatch "\.(css|js|png|jpg|jpeg|gif|ico|svg)$">
    ExpiresActive On
    ExpiresDefault "access plus 1 month"
</FilesMatch>
```

## 📊 **PRODUCTION CHECKLIST**

### **DEPLOY ÖNCESİ**
- [ ] Domain DNS ayarları
- [ ] SSL sertifikası
- [ ] Database oluşturma
- [ ] FTP erişim bilgileri
- [ ] Email ayarları

### **DEPLOY SONRASI**
- [ ] Health check: https://woltaxi.com/actuator/health
- [ ] API test: https://api.woltaxi.com/test
- [ ] SSL test: https://www.ssllabs.com/ssltest/
- [ ] Performance test: https://pagespeed.web.dev/

## 🔧 **NATRO SPESİFİK AYARLAR**

### **PHP AYARLARI**
```ini
memory_limit = 512M
max_execution_time = 300
upload_max_filesize = 100M
post_max_size = 100M
```

### **JAVA RUNTIME**
```bash
# Java version check
java -version

# Run WOLTAXI
nohup java -jar woltaxi-api-gateway.jar --spring.profiles.active=production &
```

### **MONİTÖRİNG**
```bash
# Process monitoring
ps aux | grep woltaxi

# Log monitoring  
tail -f logs/woltaxi.log

# Resource usage
top -p $(pgrep -f woltaxi)
```

## 📈 **PERFORMANS OPTİMİZASYONU**

### **CACHING**
- Browser caching: 7 gün
- CDN caching: 30 gün
- Database query caching
- Redis caching

### **COMPRESSION**
- Gzip compression: ✅
- Image optimization: ✅
- CSS/JS minification: ✅

## 🔐 **GÜVENLİK AYARLARI**

### **FIREWALL**
```
# Natro firewall rules
Allow: 80, 443, 8080
Block: All other ports
```

### **BACKUP**
```
Frequency: Daily at 3:00 AM
Retention: 30 days
Location: Natro + Google Drive
```

## 🚨 **ACİL DURUM PROSEDÜRLERI**

### **SITE DOWN**
1. Check Natro server status
2. Check application logs
3. Restart Java application
4. Contact Natro support

### **DATABASE ISSUES**
1. Check MySQL status
2. Restore from backup
3. Check disk space
4. Optimize database

## 📞 **DESTEK BİLGİLERİ**

### **NATRO DESTEK**
- Tel: +90 (212) 283 77 99
- Email: destek@natro.com
- Ticket System: https://destek.natro.com

### **WOLTAXI TEKNİK**
- Email: teknik@woltaxi.com
- Emergency: +90-555-WOLTAXI

---

**✅ NATRO'DA WOLTAXI HAZIR!**
🌐 **Live URL:** https://woltaxi.com
