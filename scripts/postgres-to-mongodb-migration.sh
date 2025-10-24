#!/bin/bash

# WOLTAXI PostgreSQL to MongoDB Migration Script
# This script migrates data from PostgreSQL to MongoDB
# Author: WOLTAXI Development Team  
# Version: 2.0.0

set -e

# Configuration
POSTGRES_HOST=${POSTGRES_HOST:-localhost}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
POSTGRES_USER=${POSTGRES_USER:-woltaxi_user}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-woltaxi_2024}
POSTGRES_DB=${POSTGRES_DB:-woltaxi}

MONGO_HOST=${MONGO_HOST:-localhost}
MONGO_PORT=${MONGO_PORT:-27017}
MONGO_USERNAME=${MONGO_USERNAME:-woltaxi}
MONGO_PASSWORD=${MONGO_PASSWORD:-woltaxi2024}

MIGRATION_DIR="/tmp/migration_$(date +%s)"
LOG_FILE="migration_$(date +%Y%m%d_%H%M%S).log"

# Function to log messages
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Function to export PostgreSQL table to JSON
export_postgres_table() {
    local table_name=$1
    local output_file="$MIGRATION_DIR/${table_name}.json"
    
    log_message "📤 Exporting PostgreSQL table: $table_name"
    
    # Export table data as JSON
    PGPASSWORD="$POSTGRES_PASSWORD" psql \
        -h "$POSTGRES_HOST" \
        -p "$POSTGRES_PORT" \
        -U "$POSTGRES_USER" \
        -d "$POSTGRES_DB" \
        -t \
        -c "SELECT array_to_json(array_agg(row_to_json(t))) FROM (SELECT * FROM $table_name) t;" \
        -o "$output_file"
    
    # Clean up the output file
    sed -i 's/^[ \t]*//;s/[ \t]*$//' "$output_file"
    
    if [ -s "$output_file" ]; then
        log_message "✅ Successfully exported $table_name ($(wc -l < "$output_file") lines)"
    else
        log_message "⚠️  Table $table_name appears to be empty"
    fi
}

# Function to import JSON to MongoDB
import_to_mongodb() {
    local collection_name=$1
    local json_file="$MIGRATION_DIR/${collection_name}.json"
    local db_name=$2
    
    if [ ! -f "$json_file" ] || [ ! -s "$json_file" ]; then
        log_message "⚠️  Skipping $collection_name - no data file found"
        return 0
    fi
    
    log_message "📥 Importing to MongoDB collection: $db_name.$collection_name"
    
    # Remove null array and clean JSON
    if grep -q "^\[null\]$" "$json_file"; then
        log_message "⚠️  No data to import for $collection_name"
        return 0
    fi
    
    # Import to MongoDB
    if mongoimport \
        --host "$MONGO_HOST:$MONGO_PORT" \
        --username "$MONGO_USERNAME" \
        --password "$MONGO_PASSWORD" \
        --authenticationDatabase admin \
        --db "$db_name" \
        --collection "$collection_name" \
        --file "$json_file" \
        --jsonArray \
        --drop; then
        
        log_message "✅ Successfully imported $collection_name to $db_name"
    else
        log_message "❌ Failed to import $collection_name to $db_name"
        return 1
    fi
}

# Function to create MongoDB indexes
create_mongodb_indexes() {
    local db_name=$1
    
    log_message "🔍 Creating MongoDB indexes for $db_name"
    
    case $db_name in
        "woltaxi_users")
            mongosh --host "$MONGO_HOST:$MONGO_PORT" \
                    --username "$MONGO_USERNAME" \
                    --password "$MONGO_PASSWORD" \
                    --authenticationDatabase admin \
                    --eval "
                        use $db_name;
                        db.users.createIndex({email: 1}, {unique: true});
                        db.users.createIndex({phone: 1}, {unique: true});
                        db.users.createIndex({status: 1});
                        db.users.createIndex({created_at: -1});
                        db.user_profiles.createIndex({user_id: 1}, {unique: true});
                        db.user_sessions.createIndex({user_id: 1});
                        db.user_sessions.createIndex({expires_at: 1}, {expireAfterSeconds: 0});
                    "
            ;;
        "woltaxi_drivers")
            mongosh --host "$MONGO_HOST:$MONGO_PORT" \
                    --username "$MONGO_USERNAME" \
                    --password "$MONGO_PASSWORD" \
                    --authenticationDatabase admin \
                    --eval "
                        use $db_name;
                        db.drivers.createIndex({email: 1}, {unique: true});
                        db.drivers.createIndex({phone: 1}, {unique: true});
                        db.drivers.createIndex({license_number: 1}, {unique: true});
                        db.drivers.createIndex({status: 1});
                        db.drivers.createIndex({location: '2dsphere'});
                        db.vehicles.createIndex({driver_id: 1});
                        db.vehicles.createIndex({plate_number: 1}, {unique: true});
                        db.driver_locations.createIndex({driver_id: 1});
                        db.driver_locations.createIndex({location: '2dsphere'});
                        db.driver_locations.createIndex({timestamp: -1});
                    "
            ;;
        "woltaxi_rides")
            mongosh --host "$MONGO_HOST:$MONGO_PORT" \
                    --username "$MONGO_USERNAME" \
                    --password "$MONGO_PASSWORD" \
                    --authenticationDatabase admin \
                    --eval "
                        use $db_name;
                        db.rides.createIndex({user_id: 1});
                        db.rides.createIndex({driver_id: 1});
                        db.rides.createIndex({status: 1});
                        db.rides.createIndex({created_at: -1});
                        db.rides.createIndex({pickup_location: '2dsphere'});
                        db.rides.createIndex({dropoff_location: '2dsphere'});
                        db.ride_tracking.createIndex({ride_id: 1});
                        db.ride_tracking.createIndex({timestamp: -1});
                    "
            ;;
    esac
    
    log_message "✅ Indexes created for $db_name"
}

# Function to verify migration
verify_migration() {
    local postgres_table=$1
    local mongo_db=$2
    local mongo_collection=$3
    
    log_message "🔍 Verifying migration: $postgres_table -> $mongo_db.$mongo_collection"
    
    # Count records in PostgreSQL
    postgres_count=$(PGPASSWORD="$POSTGRES_PASSWORD" psql \
        -h "$POSTGRES_HOST" \
        -p "$POSTGRES_PORT" \
        -U "$POSTGRES_USER" \
        -d "$POSTGRES_DB" \
        -t -c "SELECT COUNT(*) FROM $postgres_table;" | tr -d ' ')
    
    # Count documents in MongoDB
    mongo_count=$(mongosh --host "$MONGO_HOST:$MONGO_PORT" \
                          --username "$MONGO_USERNAME" \
                          --password "$MONGO_PASSWORD" \
                          --authenticationDatabase admin \
                          --quiet \
                          --eval "db.getSiblingDB('$mongo_db').$mongo_collection.countDocuments()")
    
    if [ "$postgres_count" -eq "$mongo_count" ]; then
        log_message "✅ Migration verified: $postgres_count records migrated successfully"
    else
        log_message "⚠️  Migration count mismatch: PostgreSQL=$postgres_count, MongoDB=$mongo_count"
    fi
}

# Function to migrate users data
migrate_users() {
    log_message "👥 Migrating Users Service Data"
    
    local tables=("users" "user_profiles" "user_sessions" "user_preferences" "user_notifications")
    
    for table in "${tables[@]}"; do
        export_postgres_table "$table"
        import_to_mongodb "$table" "woltaxi_users"
        verify_migration "$table" "woltaxi_users" "$table"
    done
    
    create_mongodb_indexes "woltaxi_users"
}

# Function to migrate drivers data
migrate_drivers() {
    log_message "🚗 Migrating Drivers Service Data"
    
    local tables=("drivers" "vehicles" "driver_documents" "driver_ratings" "driver_locations" "driver_earnings")
    
    for table in "${tables[@]}"; do
        export_postgres_table "$table"
        import_to_mongodb "$table" "woltaxi_drivers"
        verify_migration "$table" "woltaxi_drivers" "$table"
    done
    
    create_mongodb_indexes "woltaxi_drivers"
}

# Function to migrate rides data
migrate_rides() {
    log_message "🛣️  Migrating Rides Service Data"
    
    local tables=("rides" "ride_requests" "ride_tracking" "ride_ratings" "ride_cancellations")
    
    for table in "${tables[@]}"; do
        export_postgres_table "$table"
        import_to_mongodb "$table" "woltaxi_rides"
        verify_migration "$table" "woltaxi_rides" "$table"
    done
    
    create_mongodb_indexes "woltaxi_rides"
}

# Function to migrate payments data
migrate_payments() {
    log_message "💳 Migrating Payments Service Data"
    
    local tables=("payments" "payment_methods" "payment_transactions" "refunds" "invoices")
    
    for table in "${tables[@]}"; do
        export_postgres_table "$table"
        import_to_mongodb "$table" "woltaxi_payments"
        verify_migration "$table" "woltaxi_payments" "$table"
    done
    
    create_mongodb_indexes "woltaxi_payments"
}

# Function to check prerequisites
check_prerequisites() {
    log_message "🔍 Checking prerequisites..."
    
    # Check PostgreSQL connection
    if ! PGPASSWORD="$POSTGRES_PASSWORD" psql \
        -h "$POSTGRES_HOST" \
        -p "$POSTGRES_PORT" \
        -U "$POSTGRES_USER" \
        -d "$POSTGRES_DB" \
        -c "SELECT 1;" > /dev/null 2>&1; then
        log_message "❌ Cannot connect to PostgreSQL"
        exit 1
    fi
    
    # Check MongoDB connection
    if ! mongosh --host "$MONGO_HOST:$MONGO_PORT" \
                 --username "$MONGO_USERNAME" \
                 --password "$MONGO_PASSWORD" \
                 --authenticationDatabase admin \
                 --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
        log_message "❌ Cannot connect to MongoDB"
        exit 1
    fi
    
    log_message "✅ Prerequisites check passed"
}

# Main function
main() {
    log_message "🚀 Starting WOLTAXI PostgreSQL to MongoDB Migration"
    log_message "=================================================="
    
    # Create migration directory
    mkdir -p "$MIGRATION_DIR"
    
    # Check prerequisites
    check_prerequisites
    
    # Perform migrations
    migrate_users
    migrate_drivers
    migrate_rides
    migrate_payments
    
    # Generate migration report
    log_message "📊 Migration Summary"
    log_message "==================="
    
    for db in "woltaxi_users" "woltaxi_drivers" "woltaxi_rides" "woltaxi_payments"; do
        collection_count=$(mongosh --host "$MONGO_HOST:$MONGO_PORT" \
                                  --username "$MONGO_USERNAME" \
                                  --password "$MONGO_PASSWORD" \
                                  --authenticationDatabase admin \
                                  --quiet \
                                  --eval "db.getSiblingDB('$db').runCommand('listCollections').cursor.firstBatch.length")
        log_message "$db: $collection_count collections"
    done
    
    # Cleanup
    rm -rf "$MIGRATION_DIR"
    
    log_message "🎉 WOLTAXI Migration Completed Successfully!"
    log_message "=========================================="
    log_message "📝 Log file: $LOG_FILE"
}

# Execute main function
main "$@"