#!/bin/bash

# =============================================================================
# WOLTAXI Cross-Platform Deployment Script
# Çapraz Platform Deployment Betiği
# 
# Supports: Windows (WSL/Git Bash), macOS, Linux, ARM64 (Apple Silicon)
# Desteklenen: Windows (WSL/Git Bash), macOS, Linux, ARM64 (Apple Silicon)
# =============================================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project information
PROJECT_NAME="WOLTAXI"
VERSION="1.0.0"
BUILD_DATE=$(date +"%Y-%m-%d %H:%M:%S")

# Platform detection
detect_platform() {
    case "$(uname -s)" in
        Linux*)     PLATFORM=Linux;;
        Darwin*)    PLATFORM=Mac;;
        CYGWIN*)    PLATFORM=Windows;;
        MINGW*)     PLATFORM=Windows;;
        MSYS*)      PLATFORM=Windows;;
        *)          PLATFORM="UNKNOWN";;
    esac
    
    case "$(uname -m)" in
        x86_64)     ARCH=amd64;;
        arm64)      ARCH=arm64;;
        aarch64)    ARCH=arm64;;
        armv7l)     ARCH=arm;;
        *)          ARCH="UNKNOWN";;
    esac
    
    echo -e "${BLUE}Detected Platform: ${PLATFORM} (${ARCH})${NC}"
}

# Check prerequisites
check_prerequisites() {
    echo -e "${BLUE}Checking prerequisites...${NC}"
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Docker is not installed. Please install Docker first.${NC}"
        case $PLATFORM in
            "Mac")
                echo -e "${YELLOW}Install Docker Desktop for Mac: https://docs.docker.com/desktop/mac/install/${NC}"
                ;;
            "Linux")
                echo -e "${YELLOW}Install Docker: curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh${NC}"
                ;;
            "Windows")
                echo -e "${YELLOW}Install Docker Desktop for Windows: https://docs.docker.com/desktop/windows/install/${NC}"
                ;;
        esac
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        echo -e "${RED}Docker Compose is not installed. Please install Docker Compose first.${NC}"
        exit 1
    fi
    
    # Check Git
    if ! command -v git &> /dev/null; then
        echo -e "${RED}Git is not installed. Please install Git first.${NC}"
        case $PLATFORM in
            "Mac")
                echo -e "${YELLOW}Install Git via Homebrew: brew install git${NC}"
                ;;
            "Linux")
                echo -e "${YELLOW}Install Git: sudo apt-get install git (Ubuntu/Debian) or sudo yum install git (CentOS/RHEL)${NC}"
                ;;
            "Windows")
                echo -e "${YELLOW}Install Git for Windows: https://git-scm.com/download/win${NC}"
                ;;
        esac
        exit 1
    fi
    
    # Check Java (optional, for local development)
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        echo -e "${GREEN}Java detected: ${JAVA_VERSION}${NC}"
    else
        echo -e "${YELLOW}Java not detected. Using Docker containers for Java applications.${NC}"
    fi
    
    echo -e "${GREEN}Prerequisites check completed successfully!${NC}"
}

# Create necessary directories
create_directories() {
    echo -e "${BLUE}Creating necessary directories...${NC}"
    
    # Create directories for different platforms
    mkdir -p logs/{eureka-server,api-gateway,user-service,driver-service,ride-service,payment-service,location-service,notification-service,subscription-service,marketing-service,analytics-service,global-performance-service,emergency-service,travel-integration-service,wolkurye-service,ai-ml-service}
    mkdir -p uploads/{user-profiles,driver-documents,ride-attachments,emergency-files,travel-documents,wolkurye-packages,ai-ml-models}
    mkdir -p credentials
    mkdir -p database/init
    mkdir -p backups
    mkdir -p monitoring
    mkdir -p ssl
    
    # AI/ML specific directories
    mkdir -p ai-ml-service/{models,cache,temp,logs}
    mkdir -p ai-ml-service/models/{tensorflow,pytorch,sklearn,weka,custom}
    
    # Set permissions based on platform
    case $PLATFORM in
        "Linux"|"Mac")
            chmod -R 755 logs uploads credentials database backups monitoring ai-ml-service
            ;;
        "Windows")
            # Windows doesn't use chmod, but we can set basic permissions
            echo -e "${YELLOW}Setting Windows-compatible permissions...${NC}"
            ;;
    esac
    
    echo -e "${GREEN}Directories created successfully!${NC}"
}

# Generate environment file
generate_env_file() {
    echo -e "${BLUE}Generating environment configuration...${NC}"
    
    if [ ! -f ".env" ]; then
        cat > .env << EOF
# =============================================================================
# WOLTAXI Environment Configuration
# Cross-Platform Settings for Windows, macOS, Linux, ARM64
# =============================================================================

# Database Configuration
POSTGRES_DB=woltaxi
POSTGRES_USER=woltaxi_user
POSTGRES_PASSWORD=woltaxi_2024
DB_HOST=postgres
DB_PORT=5432

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=woltaxi_redis_2024

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# JWT Secrets (Generate your own in production)
JWT_SECRET_USER=woltaxi-user-secret-2024
JWT_SECRET_DRIVER=woltaxi-driver-secret-2024
JWT_SECRET_PAYMENT=woltaxi-payment-secret-2024
JWT_SECRET_GATEWAY=woltaxi-gateway-secret-2024
JWT_SECRET_AIML=woltaxi-aiml-secret-2024

# Third Party API Keys (Replace with your actual keys)
GOOGLE_MAPS_API_KEY=your-google-maps-api-key
GOOGLE_CLOUD_PROJECT_ID=woltaxi-ai-ml
GOOGLE_VISION_API_KEY=your-google-vision-api-key
GOOGLE_TRANSLATE_API_KEY=your-google-translate-api-key

# OpenAI Configuration
OPENAI_API_KEY=your-openai-api-key
OPENAI_MODEL=gpt-4

# Azure Cognitive Services
AZURE_COGNITIVE_SERVICES_KEY=your-azure-cognitive-key
AZURE_REGION=eastus
AZURE_COGNITIVE_SERVICES_ENDPOINT=your-azure-endpoint

# Hugging Face
HUGGINGFACE_API_KEY=your-huggingface-api-key

# Payment Gateways
STRIPE_PUBLISHABLE_KEY=pk_test_your-stripe-publishable-key
STRIPE_SECRET_KEY=sk_test_your-stripe-secret-key
PAYPAL_CLIENT_ID=your-paypal-client-id
PAYPAL_CLIENT_SECRET=your-paypal-client-secret
IYZICO_API_KEY=your-iyzico-api-key
IYZICO_SECRET_KEY=your-iyzico-secret-key

# Communication Services
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_FROM_NUMBER=+90XXXXXXXXX

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-email-password

# Firebase
FIREBASE_PROJECT_ID=woltaxi-app
FIREBASE_CREDENTIALS_PATH=firebase-adminsdk.json

# Social Media APIs
FACEBOOK_CLIENT_ID=your-facebook-client-id
FACEBOOK_CLIENT_SECRET=your-facebook-client-secret
INSTAGRAM_CLIENT_ID=your-instagram-client-id
INSTAGRAM_CLIENT_SECRET=your-instagram-client-secret

# Travel Integration APIs
AMADEUS_API_KEY=your-amadeus-api-key
AMADEUS_API_SECRET=your-amadeus-api-secret
BOOKING_COM_API_KEY=your-booking-com-api-key
EXPEDIA_API_KEY=your-expedia-api-key

# Security Configuration
AI_ML_ENCRYPTION_KEY=woltaxi-aiml-encryption-2024
JASYPT_ENCRYPTOR_PASSWORD=woltaxi-master-encryption-2024

# Performance Settings
JAVA_OPTS_SMALL=-Xmx512m -Xms256m
JAVA_OPTS_MEDIUM=-Xmx1024m -Xms512m
JAVA_OPTS_LARGE=-Xmx2048m -Xms1024m
JAVA_OPTS_XLARGE=-Xmx3072m -Xms1536m

# Monitoring
PROMETHEUS_ENABLED=true
GRAFANA_ENABLED=true
JAEGER_ENABLED=true

# Platform Specific Settings
PLATFORM=${PLATFORM}
ARCHITECTURE=${ARCH}
BUILD_DATE=${BUILD_DATE}
VERSION=${VERSION}
EOF
        echo -e "${GREEN}Environment file (.env) created successfully!${NC}"
    else
        echo -e "${YELLOW}Environment file (.env) already exists. Skipping creation.${NC}"
    fi
}

# Platform-specific optimizations
apply_platform_optimizations() {
    echo -e "${BLUE}Applying platform-specific optimizations...${NC}"
    
    case $PLATFORM in
        "Mac")
            # macOS specific optimizations
            echo -e "${YELLOW}Applying macOS optimizations...${NC}"
            if [ "$ARCH" = "arm64" ]; then
                echo "# Apple Silicon (M1/M2) optimizations" >> .env
                echo "DOCKER_DEFAULT_PLATFORM=linux/arm64" >> .env
                echo "COMPOSE_DOCKER_CLI_BUILD=1" >> .env
                echo "DOCKER_BUILDKIT=1" >> .env
            fi
            ;;
        "Linux")
            # Linux specific optimizations
            echo -e "${YELLOW}Applying Linux optimizations...${NC}"
            echo "# Linux optimizations" >> .env
            echo "DOCKER_BUILDKIT=1" >> .env
            echo "COMPOSE_DOCKER_CLI_BUILD=1" >> .env
            ;;
        "Windows")
            # Windows specific optimizations
            echo -e "${YELLOW}Applying Windows optimizations...${NC}"
            echo "# Windows optimizations" >> .env
            echo "COMPOSE_PATH_SEPARATOR=;" >> .env
            echo "COMPOSE_CONVERT_WINDOWS_PATHS=1" >> .env
            ;;
    esac
    
    echo -e "${GREEN}Platform optimizations applied!${NC}"
}

# Build and deploy services
deploy_services() {
    echo -e "${BLUE}Building and deploying WOLTAXI services...${NC}"
    
    # Stop existing services
    echo -e "${YELLOW}Stopping existing services...${NC}"
    docker-compose down --remove-orphans 2>/dev/null || docker compose down --remove-orphans 2>/dev/null || true
    
    # Build services with platform support
    echo -e "${YELLOW}Building services for ${PLATFORM} (${ARCH})...${NC}"
    
    if command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
    else
        COMPOSE_CMD="docker compose"
    fi
    
    # Build with multi-platform support
    $COMPOSE_CMD build --parallel --build-arg BUILDPLATFORM=${PLATFORM,,}/amd64 --build-arg TARGETPLATFORM=${PLATFORM,,}/${ARCH}
    
    # Start infrastructure services first
    echo -e "${YELLOW}Starting infrastructure services...${NC}"
    $COMPOSE_CMD up -d postgres redis kafka zookeeper
    
    # Wait for infrastructure to be ready
    echo -e "${YELLOW}Waiting for infrastructure services to become healthy...${NC}"
    sleep 30
    
    # Start discovery service
    $COMPOSE_CMD up -d eureka-server
    sleep 20
    
    # Start core services
    echo -e "${YELLOW}Starting core services...${NC}"
    $COMPOSE_CMD up -d api-gateway user-service driver-service ride-service payment-service
    sleep 15
    
    # Start additional services
    echo -e "${YELLOW}Starting additional services...${NC}"
    $COMPOSE_CMD up -d location-service notification-service subscription-service
    sleep 10
    
    # Start business services
    echo -e "${YELLOW}Starting business services...${NC}"
    $COMPOSE_CMD up -d marketing-service analytics-service global-performance-service
    sleep 10
    
    # Start specialized services
    echo -e "${YELLOW}Starting specialized services...${NC}"
    $COMPOSE_CMD up -d emergency-service travel-integration-service wolkurye-service
    sleep 15
    
    # Start AI/ML service (requires more resources)
    echo -e "${YELLOW}Starting AI/ML service...${NC}"
    $COMPOSE_CMD up -d ai-ml-service
    sleep 30
    
    echo -e "${GREEN}All services started successfully!${NC}"
}

# Health check for all services
health_check() {
    echo -e "${BLUE}Performing health check...${NC}"
    
    services=(
        "eureka-server:8761"
        "api-gateway:8765"
        "user-service:8081"
        "driver-service:8082"
        "ride-service:8083"
        "payment-service:8084"
        "location-service:8085"
        "notification-service:8086"
        "subscription-service:8087"
        "marketing-service:8088"
        "analytics-service:8089"
        "global-performance-service:8090"
        "emergency-service:8091"
        "travel-integration-service:8092"
        "wolkurye-service:8093"
        "ai-ml-service:8094"
    )
    
    echo "Checking service health..."
    healthy_count=0
    total_count=${#services[@]}
    
    for service in "${services[@]}"; do
        service_name=$(echo $service | cut -d':' -f1)
        port=$(echo $service | cut -d':' -f2)
        
        if curl -f -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service_name is healthy${NC}"
            ((healthy_count++))
        else
            echo -e "${RED}✗ $service_name is not healthy${NC}"
        fi
    done
    
    echo -e "\n${BLUE}Health Check Summary:${NC}"
    echo -e "Healthy services: ${GREEN}$healthy_count${NC}/${total_count}"
    
    if [ $healthy_count -eq $total_count ]; then
        echo -e "${GREEN}🎉 All services are healthy!${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  Some services are not healthy. Check logs for details.${NC}"
        return 1
    fi
}

# Display service URLs
show_service_urls() {
    echo -e "\n${BLUE}=== WOLTAXI Service URLs ===${NC}"
    echo -e "${GREEN}🌐 API Gateway:${NC} http://localhost:8765"
    echo -e "${GREEN}🔍 Eureka Dashboard:${NC} http://localhost:8761"
    echo -e "${GREEN}👥 User Service:${NC} http://localhost:8081"
    echo -e "${GREEN}🚗 Driver Service:${NC} http://localhost:8082"
    echo -e "${GREEN}🛣️  Ride Service:${NC} http://localhost:8083"
    echo -e "${GREEN}💳 Payment Service:${NC} http://localhost:8084"
    echo -e "${GREEN}📍 Location Service:${NC} http://localhost:8085"
    echo -e "${GREEN}📢 Notification Service:${NC} http://localhost:8086"
    echo -e "${GREEN}📊 Subscription Service:${NC} http://localhost:8087"
    echo -e "${GREEN}📈 Marketing Service:${NC} http://localhost:8088"
    echo -e "${GREEN}📉 Analytics Service:${NC} http://localhost:8089"
    echo -e "${GREEN}🏆 Performance Service:${NC} http://localhost:8090"
    echo -e "${GREEN}🚨 Emergency Service:${NC} http://localhost:8091"
    echo -e "${GREEN}✈️  Travel Service:${NC} http://localhost:8092"
    echo -e "${GREEN}🏍️  WolKurye Service:${NC} http://localhost:8093"
    echo -e "${GREEN}🤖 AI/ML Service:${NC} http://localhost:8094"
    echo -e "\n${BLUE}=== Database & Infrastructure ===${NC}"
    echo -e "${GREEN}🐘 PostgreSQL:${NC} localhost:5432"
    echo -e "${GREEN}🔴 Redis:${NC} localhost:6379"
    echo -e "${GREEN}📊 Kafka:${NC} localhost:9092"
}

# Main deployment function
main() {
    echo -e "${BLUE}"
    echo "=============================================="
    echo "🚀 WOLTAXI Cross-Platform Deployment"
    echo "=============================================="
    echo -e "${NC}"
    
    detect_platform
    check_prerequisites
    create_directories
    generate_env_file
    apply_platform_optimizations
    deploy_services
    
    echo -e "\n${YELLOW}Waiting for services to fully initialize...${NC}"
    sleep 45
    
    if health_check; then
        show_service_urls
        echo -e "\n${GREEN}🎉 WOLTAXI deployment completed successfully!${NC}"
        echo -e "${GREEN}Platform: ${PLATFORM} (${ARCH})${NC}"
        echo -e "${GREEN}Build Date: ${BUILD_DATE}${NC}"
        echo -e "${GREEN}Version: ${VERSION}${NC}"
        
        echo -e "\n${BLUE}Next Steps:${NC}"
        echo -e "1. Access the API Gateway at: ${GREEN}http://localhost:8765${NC}"
        echo -e "2. Check Eureka Dashboard at: ${GREEN}http://localhost:8761${NC}"
        echo -e "3. View logs: ${YELLOW}docker-compose logs -f [service-name]${NC}"
        echo -e "4. Stop services: ${YELLOW}docker-compose down${NC}"
        
    else
        echo -e "\n${YELLOW}⚠️  Deployment completed with some issues.${NC}"
        echo -e "Check service logs: ${YELLOW}docker-compose logs [service-name]${NC}"
    fi
}

# Handle script arguments
case "${1:-}" in
    "health")
        health_check
        ;;
    "urls")
        show_service_urls
        ;;
    "clean")
        echo -e "${YELLOW}Cleaning up Docker resources...${NC}"
        docker-compose down --volumes --remove-orphans 2>/dev/null || docker compose down --volumes --remove-orphans
        docker system prune -af
        echo -e "${GREEN}Cleanup completed!${NC}"
        ;;
    *)
        main
        ;;
esac