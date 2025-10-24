# WOLTAXI.COM - Basic DNS Setup

## 🎯 Minimal DNS Configuration for Natro Panel

### A Records (IPv4 Addresses):

```dns
# Main Domain
@           → 76.76.19.19      (Main website)
www         → 76.76.19.19      (www redirect)

# API Endpoints (Future ready)
api         → 76.76.19.20      (API Gateway)
admin       → 76.76.19.21      (Admin Panel)
driver      → 76.76.19.22      (Driver Portal)
app         → 76.76.19.23      (User Web App)
```

### CNAME Records:
```dns
mail        → woltaxi.com      (Email redirect)
```

### MX Records (Email):
```dns
Priority    Host    Value
10          @       mx1.natro.com
20          @       mx2.natro.com
```

## 📱 Natro Panel Step-by-Step:

### 1. DNS Management Access:
- Login: https://panel.natro.com
- Select: WOLTAXI.COM
- Click: "DNS Yönetimi"

### 2. Add A Records:

#### Main Domain:
```
Type: A
Host: @
Value: 76.76.19.19
TTL: 3600
```

#### WWW Subdomain:
```
Type: A
Host: www
Value: 76.76.19.19
TTL: 3600
```

#### API Subdomain:
```
Type: A
Host: api
Value: 76.76.19.20
TTL: 3600
```

#### Admin Subdomain:
```
Type: A
Host: admin
Value: 76.76.19.21
TTL: 3600
```

#### Driver Subdomain:
```
Type: A
Host: driver
Value: 76.76.19.22
TTL: 3600
```

#### App Subdomain:
```
Type: A
Host: app
Value: 76.76.19.23
TTL: 3600
```

### 3. Add MX Records (Email):
```
Type: MX
Host: @
Value: mx1.natro.com
Priority: 10
TTL: 3600

Type: MX
Host: @
Value: mx2.natro.com
Priority: 20
TTL: 3600
```

## ✅ Quick Test Commands:

After 1-2 hours, test with:
```cmd
nslookup woltaxi.com
nslookup www.woltaxi.com
nslookup api.woltaxi.com
```

Expected results:
```
woltaxi.com     → 76.76.19.19
www.woltaxi.com → 76.76.19.19
api.woltaxi.com → 76.76.19.20
```

## 🚀 What This Enables:

- ✅ **woltaxi.com** → Main website
- ✅ **www.woltaxi.com** → Same as main
- ✅ **api.woltaxi.com** → API Gateway ready
- ✅ **admin.woltaxi.com** → Admin panel ready
- ✅ **driver.woltaxi.com** → Driver portal ready
- ✅ **app.woltaxi.com** → User app ready
- ✅ **Email** → Professional emails ready

## 📧 Email Setup (After DNS):

You can create:
- admin@woltaxi.com
- info@woltaxi.com
- support@woltaxi.com
- noreply@woltaxi.com

## 📊 Timeline:

- **Now:** Add DNS records in Natro
- **1-2 hours:** DNS active in Turkey
- **24 hours:** Global DNS propagation
- **Next:** Deploy applications to these IPs

---

## 🎯 Priority Order:

1. **First:** Add @ and www A records
2. **Second:** Add api A record
3. **Third:** Add admin, driver, app A records
4. **Fourth:** Add MX records for email

**Start with the main domain and expand! 🚀**