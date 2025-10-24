# 🚀 RAILWAY.APP DEPLOYMENT STEP-BY-STEP

## 1️⃣ EUREKA SERVER DEPLOY (İLK ÖNCE!)

### Railway.app Setup:
```
1. ✅ GitHub ile login yaptın
2. ✅ "Deploy from GitHub repo" seç
3. ✅ "woltaxi-microservices" repo'yu seç
4. 🎯 Root Directory: "eureka-server" YAZ
```

### Environment Variables (Eureka için):
```
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
```

### Deploy Settings:
```
✅ Framework: Spring Boot (otomatik detect)
✅ Build Command: mvn clean package
✅ Start Command: java -jar target/*.jar
✅ Port: 8080
```

## 2️⃣ API GATEWAY DEPLOY (İKİNCİ!)

### Railway.app Setup:
```
1. "New Project" → "Deploy from GitHub repo"
2. "woltaxi-microservices" repo'yu seç
3. 🎯 Root Directory: "api-gateway" YAZ
```

### Environment Variables (API Gateway için):
```
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://eureka-server-production.railway.app/eureka/
```

## 3️⃣ USER SERVICE DEPLOY (ÜÇÜNCÜ!)

### Railway.app Setup:
```
1. "New Project" → "Deploy from GitHub repo"  
2. "woltaxi-microservices" repo'yu seç
3. 🎯 Root Directory: "user-service" YAZ
```

### Environment Variables (User Service için):
```
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
MONGODB_URI=mongodb+srv://woltaxi_db_user:mEGHjWEyVyCjsDVj@cluster0.q9ezcyu.mongodb.net/woltaxi?retryWrites=true&w=majority
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://eureka-server-production.railway.app/eureka/
```

## 4️⃣ DRIVER SERVICE DEPLOY (DÖRDÜNCÜ!)

### Railway.app Setup:
```
1. "New Project" → "Deploy from GitHub repo"
2. "woltaxi-microservices" repo'yu seç  
3. 🎯 Root Directory: "driver-service" YAZ
```

### Environment Variables (Driver Service için):
```
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
MONGODB_URI=mongodb+srv://woltaxi_db_user:mEGHjWEyVyCjsDVj@cluster0.q9ezcyu.mongodb.net/woltaxi?retryWrites=true&w=majority
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://eureka-server-production.railway.app/eureka/
```

## 🎯 EXPECTED RAILWAY URLs:

### Railway Automatic URLs:
```
Eureka:      https://eureka-server-production.railway.app
API Gateway: https://api-gateway-production.railway.app
User:        https://user-service-production.railway.app
Driver:      https://driver-service-production.railway.app
```

## 🌐 DNS UPDATE (Son Adım):

### Natro Panel'de CNAME Records Ekle:
```dns
# Bu kayıtları ekle:
api.woltaxi.com     → CNAME: api-gateway-production.railway.app
eureka.woltaxi.com  → CNAME: eureka-server-production.railway.app
users.woltaxi.com   → CNAME: user-service-production.railway.app
drivers.woltaxi.com → CNAME: driver-service-production.railway.app
```

## ⚡ QUICK CHECKLIST:

### Her Service Deploy Edilirken Kontrol Et:
```
✅ Root Directory doğru mu? (eureka-server, api-gateway, vs.)
✅ Environment Variables eklendi mi?
✅ Build başarılı mı?
✅ Deploy statusu "Success" mi?
✅ URL erişilebilir mi?
```

## 🚨 DEPLOY SIRASI (ÖNEMLİ!):
```
1. 🥇 Eureka Server (FIRST!)
2. 🥈 API Gateway (SECOND!)  
3. 🥉 User Service (THIRD!)
4. 🏅 Driver Service (FOURTH!)
```

---

## 🎯 ŞİMDİ BAŞLA!

**Eureka Server ile başla, Railway'de "eureka-server" klasörünü seç!** 🚀