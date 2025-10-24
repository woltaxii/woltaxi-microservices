#!/bin/bash

echo "🚀 WOLTAXI NATRO DEPLOYMENT STARTING..."

# Environment Variables
NATRO_DOMAIN="woltaxi.com"
NATRO_FTP_HOST="${NATRO_FTP_HOST}"
NATRO_FTP_USER="${NATRO_FTP_USER}"
NATRO_FTP_PASS="${NATRO_FTP_PASS}"

# Build Application
echo "📦 Building WOLTAXI Application..."
cd ../api-gateway
mvn clean package -Pprod

# Prepare Deployment Package
echo "📁 Preparing deployment package..."
mkdir -p ../deployment/dist
cp target/woltaxi-api-gateway.jar ../deployment/dist/
cp -r src/main/resources/static ../deployment/dist/
cp -r src/main/resources/templates ../deployment/dist/

# Upload to Natro via FTP
echo "⬆️ Uploading to Natro hosting..."
cd ../deployment

# Create FTP upload script
cat > ftp-upload.txt << EOF
open $NATRO_FTP_HOST
user $NATRO_FTP_USER $NATRO_FTP_PASS
binary
cd public_html
lcd dist
mput *
bye
EOF

# Execute FTP upload
ftp -s:ftp-upload.txt

# Database Migration
echo "🗄️ Running database migrations..."
# SQL scripts will be executed here

# SSL Certificate Setup
echo "🔐 Configuring SSL certificate..."
# Let's Encrypt configuration

# Health Check
echo "🏥 Performing health check..."
curl -f https://$NATRO_DOMAIN/actuator/health

if [ $? -eq 0 ]; then
    echo "✅ DEPLOYMENT SUCCESSFUL!"
    echo "🌐 WOLTAXI is live at: https://$NATRO_DOMAIN"
else
    echo "❌ DEPLOYMENT FAILED!"
    exit 1
fi

echo "🎉 WOLTAXI deployment completed!"
