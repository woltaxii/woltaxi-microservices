# WOLTAXI Heroku Deployment - 23 Gün Free Plan

## 🎯 Heroku Free Tier Strategy (550 hours = 23 days)

### ⏰ Free Tier Details:
```
Monthly Hours: 550 hours
Daily Average: ~18 hours active
RAM per App: 512MB
Storage: Ephemeral (files reset on restart)
Database: PostgreSQL add-on (free tier)
SSL: Automatic & free
Custom Domain: Free
```

### 🚦 Sleep Management:
```
Sleep After: 30 minutes of inactivity
Wake Up: First request (30-60 seconds)
Keep Alive: Use uptime monitoring services
Pro Tip: Combine multiple services to stay awake longer
```

## 📋 WOLTAXI Heroku Deployment Plan

### Phase 1: Heroku Setup (15 minutes)

#### 1. Heroku Account Creation:
```
1. Visit: https://heroku.com
2. Sign up (free account)
3. Verify email
4. No credit card needed for basic features
```

#### 2. Heroku CLI Installation:
```powershell
# Download from: https://devcenter.heroku.com/articles/heroku-cli
# Or use chocolatey:
choco install heroku-cli

# Verify installation:
heroku --version
heroku login
```

#### 3. Git Repository Preparation:
```powershell
# Each microservice needs its own Heroku app
cd c:\Users\Lon\Documents\woltaxi-microservices
git status
```

### Phase 2: Deploy API Gateway (20 minutes)

#### 1. Create Heroku App:
```powershell
cd api-gateway
heroku create woltaxi-api-gateway
```

#### 2. Configure Environment Variables:
```powershell
heroku config:set SPRING_PROFILES_ACTIVE=production
heroku config:set MONGODB_URI=mongodb+srv://woltaxi_db_user:mEGHjWEyVyCjsDVj@cluster0.q9ezcyu.mongodb.net/woltaxi?retryWrites=true&w=majority
heroku config:set EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://woltaxi-eureka.herokuapp.com/eureka/
```

#### 3. Create Procfile:
```
web: java -Dserver.port=$PORT -Xmx400m -jar target/*.jar
```

#### 4. Deploy:
```powershell
git add .
git commit -m "Heroku deployment configuration"
git push heroku main
```

### Phase 3: Deploy All Microservices

#### Eureka Server:
```powershell
cd ../eureka-server
heroku create woltaxi-eureka
heroku config:set SPRING_PROFILES_ACTIVE=production
echo "web: java -Dserver.port=$PORT -Xmx400m -jar target/*.jar" > Procfile
git add .
git commit -m "Heroku config"
git push heroku main
```

#### User Service:
```powershell
cd ../user-service
heroku create woltaxi-user-service
heroku config:set SPRING_PROFILES_ACTIVE=production
heroku config:set MONGODB_URI=mongodb+srv://woltaxi_db_user:mEGHjWEyVyCjsDVj@cluster0.q9ezcyu.mongodb.net/woltaxi?retryWrites=true&w=majority
heroku config:set EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://woltaxi-eureka.herokuapp.com/eureka/
echo "web: java -Dserver.port=$PORT -Xmx400m -jar target/*.jar" > Procfile
git add .
git commit -m "Heroku config"
git push heroku main
```

#### Driver Service:
```powershell
cd ../driver-service
heroku create woltaxi-driver-service
heroku config:set SPRING_PROFILES_ACTIVE=production
heroku config:set MONGODB_URI=mongodb+srv://woltaxi_db_user:mEGHjWEyVyCjsDVj@cluster0.q9ezcyu.mongodb.net/woltaxi?retryWrites=true&w=majority
heroku config:set EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://woltaxi-eureka.herokuapp.com/eureka/
echo "web: java -Dserver.port=$PORT -Xmx400m -jar target/*.jar" > Procfile
git add .
git commit -m "Heroku config"
git push heroku main
```

## 🔧 Heroku Optimization for Free Tier

### Memory Optimization (application.yml):
```yaml
server:
  tomcat:
    max-threads: 8
    min-spare-threads: 2
    
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 5
        
  datasource:
    hikari:
      maximum-pool-size: 3
      minimum-idle: 1
      
  data:
    mongodb:
      auto-index-creation: false
```

### JVM Settings (Procfile):
```
web: java -Dserver.port=$PORT -Xmx400m -Xms200m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar target/*.jar
```

### Keep-Alive Strategy:
```javascript
// Create a simple keep-alive service
setInterval(() => {
  fetch('https://woltaxi-api-gateway.herokuapp.com/health')
  fetch('https://woltaxi-user-service.herokuapp.com/health')
  fetch('https://woltaxi-driver-service.herokuapp.com/health')
}, 25 * 60 * 1000) // Every 25 minutes
```

## 📊 Expected Heroku URLs

### Backend Services:
```
Eureka Server:   https://woltaxi-eureka.herokuapp.com
API Gateway:     https://woltaxi-api-gateway.herokuapp.com
User Service:    https://woltaxi-user-service.herokuapp.com
Driver Service:  https://woltaxi-driver-service.herokuapp.com
Ride Service:    https://woltaxi-ride-service.herokuapp.com
Payment Service: https://woltaxi-payment-service.herokuapp.com
```

### Custom Domain Mapping:
```dns
# Update Natro DNS:
api.woltaxi.com     → CNAME: woltaxi-api-gateway.herokuapp.com
eureka.woltaxi.com  → CNAME: woltaxi-eureka.herokuapp.com
users.woltaxi.com   → CNAME: woltaxi-user-service.herokuapp.com
drivers.woltaxi.com → CNAME: woltaxi-driver-service.herokuapp.com
```

## ⚡ Quick Start Commands

### 1. Install Heroku CLI:
```powershell
# Download and install from: https://devcenter.heroku.com/articles/heroku-cli
heroku --version
heroku login
```

### 2. Deploy API Gateway (First!):
```powershell
cd c:\Users\Lon\Documents\woltaxi-microservices\api-gateway
heroku create woltaxi-api-gateway
echo "web: java -Dserver.port=$PORT -Xmx400m -jar target/*.jar" > Procfile
git add .
git commit -m "Heroku deployment"
git push heroku main
heroku open
```

### 3. Check Logs:
```powershell
heroku logs --tail -a woltaxi-api-gateway
```

## 🎯 Success Indicators

### Deployment Success:
```
✅ heroku ps:scale web=1
✅ heroku logs show "Started Application"
✅ https://app-name.herokuapp.com responds
✅ Health endpoints return 200
```

### MongoDB Connection:
```
✅ Atlas connection successful
✅ No authentication errors
✅ Collections accessible
```

## 📈 Free Tier Management

### Hour Tracking:
```
Total Hours: 550/month
Per App: ~137 hours each (4 apps)
Strategy: Rotate active apps
Monitor: heroku ps -a app-name
```

### Cost Optimization:
```
Keep 2-3 core services always on
Sleep non-critical services
Use shared MongoDB Atlas
Monitor usage in dashboard
```

---

## 🚀 Let's Start Now!

**Ready to deploy WOLTAXI to Heroku for 23 days free?**

First, let's install Heroku CLI and deploy API Gateway!