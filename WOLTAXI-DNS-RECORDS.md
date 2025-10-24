# WOLTAXI.COM - Natro DNS Configuration

## 🎯 DNS Records to Add in Natro Panel

### A Records (IPv4 Addresses):

```dns
# Main Website
@                    → 185.199.108.153
www                  → 185.199.108.153

# API Services  
api                  → 140.82.112.3
admin                → 140.82.112.4
driver               → 140.82.112.5
app                  → 140.82.112.6

# Additional Services
support              → 185.199.109.153
docs                 → 185.199.110.153
status               → 185.199.111.153
```

### CNAME Records:

```dns
# Redirects
mail                 → ghs.google.com
ftp                  → woltaxi.com
```

### MX Records (Email):

```dns
Priority    Host    Points To
10          @       aspmx.l.google.com
20          @       alt1.aspmx.l.google.com
30          @       alt2.aspmx.l.google.com
```

### TXT Records (Security & Verification):

```dns
Host        Value
@           "v=spf1 include:_spf.google.com ~all"
_dmarc      "v=DMARC1; p=quarantine; rua=mailto:dmarc@woltaxi.com"
@           "google-site-verification=ABC123XYZ"
```

## 📱 Step-by-Step Natro Instructions:

### 1. Login to Natro Panel:
- Go to: https://panel.natro.com
- Login with your credentials
- Select: WOLTAXI.COM

### 2. Navigate to DNS Management:
- Click: "DNS Yönetimi" or "DNS Management"
- Look for "Kayıt Ekle" or "Add Record"

### 3. Add A Records:
```
Type: A
Host: @
Value: 185.199.108.153
TTL: 3600

Type: A  
Host: www
Value: 185.199.108.153
TTL: 3600

Type: A
Host: api
Value: 140.82.112.3
TTL: 3600

Type: A
Host: admin  
Value: 140.82.112.4
TTL: 3600

Type: A
Host: driver
Value: 140.82.112.5
TTL: 3600

Type: A
Host: app
Value: 140.82.112.6
TTL: 3600
```

### 4. Add MX Records:
```
Type: MX
Host: @
Value: aspmx.l.google.com
Priority: 10
TTL: 3600

Type: MX
Host: @
Value: alt1.aspmx.l.google.com
Priority: 20
TTL: 3600
```

### 5. Add TXT Records:
```
Type: TXT
Host: @
Value: "v=spf1 include:_spf.google.com ~all"
TTL: 3600

Type: TXT
Host: _dmarc
Value: "v=DMARC1; p=quarantine; rua=mailto:dmarc@woltaxi.com"
TTL: 3600
```

## 🔧 Natro Panel Screenshots Needed:

After adding records, please share:
- [ ] DNS Management page screenshot
- [ ] List of all added records
- [ ] Any error messages

## ✅ Verification Commands:

After 1-2 hours, test these:
```cmd
nslookup woltaxi.com
nslookup api.woltaxi.com
nslookup admin.woltaxi.com
nslookup driver.woltaxi.com
nslookup app.woltaxi.com
```

## 📧 Email Setup (After DNS):

```
admin@woltaxi.com     → Forward to: [your-email]
info@woltaxi.com      → Forward to: [your-email]
support@woltaxi.com   → Forward to: [your-email]
noreply@woltaxi.com   → Forward to: [your-email]
```

---

## 🚨 Important Notes:

1. **TTL:** Set to 3600 (1 hour) for faster updates
2. **Propagation:** Takes 1-24 hours globally
3. **Testing:** Use online DNS checkers
4. **Backup:** Keep list of all records

## 📞 Support:

If you need help with Natro interface:
- **Natro Support:** 0850 532 9955
- **Email:** destek@natro.com

**Ready to connect WOLTAXI to the world! 🌍**