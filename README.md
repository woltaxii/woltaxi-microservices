# WOLTAXI Enterprise Microservices

## 🚀 Hızlı Başlangıç

### 1. Önkoşullar
- Docker & Docker Compose
- Java 17+
- Maven 3.8+

### 2. Servisleri Başlat
```bash
# Tüm servisleri ayağa kaldır
docker-compose up -d

# Logları takip et
docker-compose logs -f
```

### 3. Servis Durumlarını Kontrol Et
- **Eureka Dashboard:** http://localhost:8761
- **API Gateway:** http://localhost:8765
- **Health Checks:** http://localhost:8765/actuator/health

## 📋 Servis Listesi

| Servis | Port | Açıklama |
|--------|------|----------|
| Eureka Server | 8761 | Service Discovery |
| API Gateway | 8765 | Routing & Load Balancing |
| User Service | 8081 | Kullanıcı Yönetimi |
| Ride Service | 8082 | Yolculuk Yönetimi |
| Driver Service | 8083 | Sürücü Yönetimi |
| Payment Service | 8084 | Ödeme İşlemleri |

## 🛠️ Geliştirme

### Yeni Servis Ekleme
1. `microservices/` altında yeni klasör oluştur
2. Spring Boot projesi kur
3. `docker-compose.yml`'e ekle
4. Eureka'ya kaydet

### Database Migration
```bash
# PostgreSQL'e bağlan
docker exec -it woltaxi_postgres psql -U woltaxi_user -d woltaxi
```

### Redis Cache
```bash
# Redis CLI
docker exec -it woltaxi_redis redis-cli
```

## 🔧 Yapılandırma

### Environment Variables
- `DB_USERNAME`: PostgreSQL kullanıcı adı
- `DB_PASSWORD`: PostgreSQL şifresi  
- `REDIS_PASSWORD`: Redis şifresi
- `JWT_SECRET`: JWT secret key

### Profiles
- `local`: Yerel geliştirme
- `docker`: Container ortamı
- `production`: Canlı ortam

## 📊 Monitoring

### Health Checks
```bash
curl http://localhost:8765/actuator/health
```

### Metrics
```bash
curl http://localhost:8765/actuator/metrics
```

## 🚀 Deployment

### Production Deployment
```bash
# Production profili ile çalıştır
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes
```bash
# Kubernetes deployment
kubectl apply -f k8s/
```
