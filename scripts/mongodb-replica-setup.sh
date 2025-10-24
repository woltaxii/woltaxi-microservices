#!/bin/bash

# WOLTAXI MongoDB Replica Set Setup Script
# This script initializes MongoDB replica set for high availability
# Author: WOLTAXI Development Team
# Version: 2.0.0

set -e

MONGO_HOST=${MONGO_HOST:-mongodb-primary}
MONGO_PORT=${MONGO_PORT:-27017}
MONGO_USERNAME=${MONGO_USERNAME:-woltaxi}
MONGO_PASSWORD=${MONGO_PASSWORD:-woltaxi2024}
REPLICA_SET_NAME=${REPLICA_SET_NAME:-woltaxi-rs}

echo "🚀 Initializing WOLTAXI MongoDB Replica Set"
echo "==========================================="

# Wait for MongoDB to be ready
echo "⏳ Waiting for MongoDB to be ready..."
sleep 30

# Initialize replica set
echo "🔧 Initializing replica set: $REPLICA_SET_NAME"

mongosh --host "$MONGO_HOST:$MONGO_PORT" --eval "
rs.initiate({
  _id: '$REPLICA_SET_NAME',
  members: [
    {
      _id: 0,
      host: 'mongodb-primary:27017',
      priority: 2
    },
    {
      _id: 1,
      host: 'mongodb-secondary1:27017',
      priority: 1
    },
    {
      _id: 2,
      host: 'mongodb-secondary2:27017',
      priority: 1
    }
  ]
})
"

echo "⏳ Waiting for replica set to be established..."
sleep 60

# Create admin user
echo "👤 Creating admin user..."
mongosh --host "$MONGO_HOST:$MONGO_PORT" --eval "
use admin;
db.createUser({
  user: '$MONGO_USERNAME',
  pwd: '$MONGO_PASSWORD',
  roles: [
    { role: 'root', db: 'admin' },
    { role: 'readWriteAnyDatabase', db: 'admin' },
    { role: 'dbAdminAnyDatabase', db: 'admin' },
    { role: 'userAdminAnyDatabase', db: 'admin' },
    { role: 'clusterAdmin', db: 'admin' }
  ]
})
"

# Create application databases and users
echo "🗄️  Creating application databases..."

DATABASES=(
    "woltaxi_users"
    "woltaxi_drivers"
    "woltaxi_rides"
    "woltaxi_payments"
    "woltaxi_analytics"
    "woltaxi_notifications"
    "woltaxi_emergency"
    "woltaxi_wolkurye"
    "woltaxi_aiml"
    "woltaxi_smartvehicle"
    "woltaxi_travel"
    "woltaxi_marketing"
    "woltaxi_subscription"
    "woltaxi_performance"
)

for db in "${DATABASES[@]}"; do
    echo "📂 Setting up database: $db"
    
    mongosh --host "$MONGO_HOST:$MONGO_PORT" \
            --username "$MONGO_USERNAME" \
            --password "$MONGO_PASSWORD" \
            --authenticationDatabase admin \
            --eval "
                use $db;
                
                // Create database-specific user
                db.createUser({
                    user: '${db}_user',
                    pwd: 'woltaxi_${db}_2024',
                    roles: [
                        { role: 'readWrite', db: '$db' },
                        { role: 'dbAdmin', db: '$db' }
                    ]
                });
                
                // Create initial collection to ensure database exists
                db.init.insertOne({
                    created: new Date(),
                    message: 'Database initialized for WOLTAXI',
                    version: '2.0.0'
                });
            "
done

echo "📊 Checking replica set status..."
mongosh --host "$MONGO_HOST:$MONGO_PORT" \
        --username "$MONGO_USERNAME" \
        --password "$MONGO_PASSWORD" \
        --authenticationDatabase admin \
        --eval "rs.status()"

echo "✅ MongoDB Replica Set Setup Completed Successfully!"
echo "🔐 Admin User: $MONGO_USERNAME"
echo "📊 Replica Set: $REPLICA_SET_NAME"
echo "🗄️  Databases: ${#DATABASES[@]} application databases created"