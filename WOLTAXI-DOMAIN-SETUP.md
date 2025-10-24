# WOLTAXI Domain Ecosystem Setup Guide

## 🌐 Domain Architecture

### Primary Domains
```
woltaxi.com                 → Main Website & Landing Page
├── api.woltaxi.com        → API Gateway (Port 8080)
├── admin.woltaxi.com      → Admin Dashboard (Port 3001)
├── driver.woltaxi.com     → Driver Portal (Port 3002)
├── app.woltaxi.com        → User Web Application (Port 3000)
├── support.woltaxi.com    → Customer Support Portal
├── docs.woltaxi.com       → API Documentation
└── status.woltaxi.com     → System Status Page
```

## 📋 Domain Registration Checklist

### ✅ Domains to Purchase:
- [ ] woltaxi.com (primary) ← **ANA DOMAIN** 
- [ ] woltaxi.net (protection)
- [ ] woltaxi.org (protection)
- [ ] woltaxi.co (alternative)

### 🔧 DNS Configuration Required:

#### A Records:
```dns
@ (root)                   → YOUR_SERVER_IP
www                        → YOUR_SERVER_IP
api                        → YOUR_API_SERVER_IP
admin                      → YOUR_ADMIN_SERVER_IP
driver                     → YOUR_DRIVER_SERVER_IP
app                        → YOUR_APP_SERVER_IP
support                    → YOUR_SUPPORT_SERVER_IP
docs                       → YOUR_DOCS_SERVER_IP
status                     → YOUR_STATUS_SERVER_IP
```

#### CNAME Records:
```dns
*.woltaxi.com             → woltaxi.com (wildcard)
```

#### MX Records (Email):
```dns
@                         → mail.woltaxi.com (Priority: 10)
```

#### TXT Records (Verification):
```dns
@                         → "v=spf1 include:_spf.google.com ~all"
_dmarc                    → "v=DMARC1; p=quarantine; rua=mailto:dmarc@woltaxi.com"
```

## 🔐 SSL Certificate Configuration

### Let's Encrypt Wildcard Certificate:
```bash
# Install certbot
sudo apt install certbot

# Generate wildcard certificate
sudo certbot certonly --manual --preferred-challenges=dns \
  -d woltaxi.com -d *.woltaxi.com

# Automatic renewal
sudo crontab -e
# Add: 0 3 * * * /usr/bin/certbot renew --quiet
```

## 🌐 Nginx Reverse Proxy Configuration

### Main Configuration (`/etc/nginx/sites-available/woltaxi.com`):
```nginx
# Main Website
server {
    listen 80;
    listen 443 ssl http2;
    server_name woltaxi.com www.woltaxi.com;

    ssl_certificate /etc/letsencrypt/live/woltaxi.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/woltaxi.com/privkey.pem;

    # Redirect HTTP to HTTPS
    if ($scheme != "https") {
        return 301 https://$host$request_uri;
    }

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# API Gateway
server {
    listen 80;
    listen 443 ssl http2;
    server_name api.woltaxi.com;

    ssl_certificate /etc/letsencrypt/live/woltaxi.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/woltaxi.com/privkey.pem;

    if ($scheme != "https") {
        return 301 https://$host$request_uri;
    }

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req zone=api burst=20 nodelay;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# Admin Dashboard
server {
    listen 80;
    listen 443 ssl http2;
    server_name admin.woltaxi.com;

    ssl_certificate /etc/letsencrypt/live/woltaxi.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/woltaxi.com/privkey.pem;

    if ($scheme != "https") {
        return 301 https://$host$request_uri;
    }

    # Admin access restriction
    allow YOUR_OFFICE_IP;
    allow YOUR_HOME_IP;
    deny all;

    location / {
        proxy_pass http://localhost:3001;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Driver Portal
server {
    listen 80;
    listen 443 ssl http2;
    server_name driver.woltaxi.com;

    ssl_certificate /etc/letsencrypt/live/woltaxi.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/woltaxi.com/privkey.pem;

    if ($scheme != "https") {
        return 301 https://$host$request_uri;
    }

    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# User Web App
server {
    listen 80;
    listen 443 ssl http2;
    server_name app.woltaxi.com;

    ssl_certificate /etc/letsencrypt/live/woltaxi.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/woltaxi.com/privkey.pem;

    if ($scheme != "https") {
        return 301 https://$host$request_uri;
    }

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 📧 Email Configuration

### Professional Email Addresses:
```
admin@woltaxi.com          → Admin team
support@woltaxi.com        → Customer support
noreply@woltaxi.com        → System notifications
api-alerts@woltaxi.com     → API monitoring
drivers@woltaxi.com        → Driver communications
users@woltaxi.com          → User communications
security@woltaxi.com       → Security alerts
billing@woltaxi.com        → Payment & billing
```

## 🔧 Spring Boot Configuration Updates

### API Gateway (`application.yml`):
```yaml
server:
  port: 8080
  
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Host=api.woltaxi.com
            - Path=/api/v1/users/**
        - id: driver-service
          uri: http://localhost:8083
          predicates:
            - Host=api.woltaxi.com
            - Path=/api/v1/drivers/**
```

### CORS Configuration:
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "https://woltaxi.com",
            "https://app.woltaxi.com", 
            "https://admin.woltaxi.com",
            "https://driver.woltaxi.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

## 📊 Monitoring & Analytics

### Status Page Configuration:
```javascript
// status.woltaxi.com configuration
const services = [
    { name: 'API Gateway', url: 'https://api.woltaxi.com/health' },
    { name: 'User Service', url: 'https://api.woltaxi.com/api/v1/users/health' },
    { name: 'Driver Service', url: 'https://api.woltaxi.com/api/v1/drivers/health' },
    { name: 'MongoDB Atlas', url: 'mongodb-status-check' },
    { name: 'Admin Panel', url: 'https://admin.woltaxi.com/health' }
];
```

## 🚀 Deployment Steps

1. **Purchase Domains**
2. **Configure DNS Records**
3. **Install SSL Certificates**
4. **Setup Nginx Reverse Proxy**
5. **Configure Email Accounts**
6. **Update Application Configurations**
7. **Deploy Applications**
8. **Test All Endpoints**
9. **Setup Monitoring**

## 📞 Post-Setup Support

After domain setup, I'll help you with:
- ✅ DNS propagation verification
- ✅ SSL certificate validation
- ✅ Application deployment
- ✅ Email configuration testing
- ✅ Performance optimization
- ✅ Security hardening
- ✅ Monitoring setup

---

**Ready to build the complete WOLTAXI ecosystem! 🚀**