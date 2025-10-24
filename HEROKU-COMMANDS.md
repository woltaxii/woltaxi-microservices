# WOLTAXI Heroku Deployment Commands

## 🚀 Quick Commands for Heroku Deployment

### After Heroku CLI Installation (restart terminal first):

```powershell
# 1. Login to Heroku
heroku login

# 2. Create and Deploy Eureka Server (FIRST!)
cd eureka-server
heroku create woltaxi-eureka
heroku config:set SPRING_PROFILES_ACTIVE=production
git add .
git commit -m "Heroku deployment"
git push heroku main

# 3. Create and Deploy API Gateway
cd ../api-gateway  
heroku create woltaxi-api-gateway
heroku config:set SPRING_PROFILES_ACTIVE=production
heroku config:set EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://woltaxi-eureka.herokuapp.com/eureka/
git add .
git commit -m "Heroku deployment"
git push heroku main

# 4. Create and Deploy User Service
cd ../user-service
heroku create woltaxi-user-service
heroku config:set SPRING_PROFILES_ACTIVE=production
heroku config:set MONGODB_URI=mongodb+srv://woltaxi_db_user:mEGHjWEyVyCjsDVj@cluster0.q9ezcyu.mongodb.net/woltaxi?retryWrites=true&w=majority
heroku config:set EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://woltaxi-eureka.herokuapp.com/eureka/
git add .
git commit -m "Heroku deployment"
git push heroku main

# 5. Create and Deploy Driver Service
cd ../driver-service
heroku create woltaxi-driver-service
heroku config:set SPRING_PROFILES_ACTIVE=production
heroku config:set MONGODB_URI=mongodb+srv://woltaxi_db_user:mEGHjWEyVyCjsDVj@cluster0.q9ezcyu.mongodb.net/woltaxi?retryWrites=true&w=majority
heroku config:set EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://woltaxi-eureka.herokuapp.com/eureka/
git add .
git commit -m "Heroku deployment"
git push heroku main
```

### Check Deployment Status:
```powershell
# Check all apps
heroku apps

# Check specific app logs
heroku logs --tail -a woltaxi-eureka
heroku logs --tail -a woltaxi-api-gateway

# Open in browser
heroku open -a woltaxi-api-gateway
```

### Expected URLs:
```
Eureka:      https://woltaxi-eureka.herokuapp.com
API Gateway: https://woltaxi-api-gateway.herokuapp.com  
User:        https://woltaxi-user-service.herokuapp.com
Driver:      https://woltaxi-driver-service.herokuapp.com
```

### DNS Update (After Deployment):
```dns
# Natro DNS Panel:
api.woltaxi.com     → CNAME: woltaxi-api-gateway.herokuapp.com
eureka.woltaxi.com  → CNAME: woltaxi-eureka.herokuapp.com
users.woltaxi.com   → CNAME: woltaxi-user-service.herokuapp.com
drivers.woltaxi.com → CNAME: woltaxi-driver-service.herokuapp.com
```

---

## 🎯 Ready to Deploy!

1. **Restart PowerShell terminal**
2. **Run `heroku login`** 
3. **Follow the commands above**
4. **23 days of free testing!** 🎉