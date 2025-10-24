# WOLTAXI.COM - Natro Domain Setup Checklist

## 🎉 Congratulations! Domain Purchased Successfully!

### 📋 Required Information for Natro:

#### 1. Registrant Information (Domain Owner):
```
Full Name: ___________________________
Company: WOLTAXI (optional)
Address: ____________________________
City: _______________________________
Postal Code: ________________________
Country: Turkey
Phone: +90 __________________________
Email: ______________________________
```

#### 2. Administrative Contact:
```
Name: _______________________________
Email: ______________________________
Phone: +90 __________________________
```

#### 3. Technical Contact:
```
Name: _______________________________
Email: ______________________________
Phone: +90 __________________________
```

### ✅ Essential Settings to Enable:

- [ ] **Domain Privacy Protection** - Protects your personal info
- [ ] **Auto-Renewal** - Prevents accidental expiration
- [ ] **Domain Lock** - Prevents unauthorized transfers
- [ ] **Email Forwarding** - Setup professional emails

### 📧 Initial Email Setup:

```
admin@woltaxi.com     → Forward to: [your-email]
info@woltaxi.com      → Forward to: [your-email]
support@woltaxi.com   → Forward to: [your-email]
noreply@woltaxi.com   → Forward to: [your-email]
```

## 🔧 DNS Configuration (After Registration)

### Immediate DNS Records to Add:

```dns
# A Records (will be updated when you have servers)
@           → YOUR_SERVER_IP (placeholder for now)
www         → YOUR_SERVER_IP (placeholder for now)
api         → YOUR_API_SERVER_IP (placeholder for now)
admin       → YOUR_ADMIN_SERVER_IP (placeholder for now)
driver      → YOUR_DRIVER_SERVER_IP (placeholder for now)
app         → YOUR_APP_SERVER_IP (placeholder for now)

# MX Records (Email)
@           → mail.woltaxi.com (Priority: 10)

# TXT Records (Email Security)
@           → "v=spf1 include:natro.com ~all"
_dmarc      → "v=DMARC1; p=quarantine; rua=mailto:dmarc@woltaxi.com"
```

## 🚀 Next Steps After Domain Registration:

### Phase 1: Immediate Setup (Today)
- [ ] Complete domain registration with Natro
- [ ] Enable domain protection features
- [ ] Setup basic email forwarding
- [ ] Verify domain ownership

### Phase 2: Server Preparation (Tomorrow)
- [ ] Setup hosting/VPS
- [ ] Install SSL certificates
- [ ] Configure Nginx/Apache
- [ ] Setup MongoDB Atlas connection

### Phase 3: Application Deployment
- [ ] Deploy API Gateway to api.woltaxi.com
- [ ] Deploy Admin Panel to admin.woltaxi.com
- [ ] Deploy Driver Portal to driver.woltaxi.com
- [ ] Deploy User App to app.woltaxi.com

## 📞 Support Information:

### Natro Support:
- **Phone:** 0850 532 9955
- **Email:** destek@natro.com
- **Website:** https://www.natro.com

### Common Issues & Solutions:

#### DNS Propagation:
- Takes 24-48 hours globally
- Use https://whatsmydns.net to check

#### Email Setup:
- Configure in Natro control panel
- Forward to existing emails initially

#### SSL Certificate:
- Free Let's Encrypt available
- Wildcard cert for *.woltaxi.com

## 🎯 Professional Email Suggestions:

```
CEO/Founder:     ceo@woltaxi.com
Info:           info@woltaxi.com
Support:        support@woltaxi.com
Admin:          admin@woltaxi.com
Drivers:        drivers@woltaxi.com
Users:          users@woltaxi.com
Security:       security@woltaxi.com
Billing:        billing@woltaxi.com
API Alerts:     api@woltaxi.com
No-Reply:       noreply@woltaxi.com
```

## 🔧 Technical Configuration Updates Needed:

### Spring Boot Applications:
```yaml
# Update all application.yml files
server:
  domain: woltaxi.com
  
cors:
  allowed-origins:
    - https://woltaxi.com
    - https://app.woltaxi.com
    - https://admin.woltaxi.com
    - https://driver.woltaxi.com
```

### React Applications:
```javascript
// Update API endpoints
const API_BASE_URL = 'https://api.woltaxi.com';
const APP_DOMAIN = 'woltaxi.com';
```

## 📊 Monitoring Setup:

### Domain Monitoring:
- [ ] Setup uptime monitoring
- [ ] SSL certificate expiry alerts
- [ ] DNS change notifications
- [ ] Email delivery monitoring

---

## 🎉 Congratulations Again!

You now own **WOLTAXI.COM** - the foundation of your taxi empire! 🚕

**I'm ready to help you with the complete technical setup!**

Next: Share your Natro control panel access and I'll configure everything! 🚀