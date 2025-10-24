#!/bin/bash

# WOLTAXI MongoDB Backup Script
# This script creates automated backups of all MongoDB databases
# Author: WOLTAXI Development Team
# Version: 2.0.0

set -e

# Configuration
MONGO_HOST=${MONGO_HOST:-mongodb-primary}
MONGO_PORT=${MONGO_PORT:-27017}
MONGO_USERNAME=${MONGO_USERNAME:-woltaxi}
MONGO_PASSWORD=${MONGO_PASSWORD:-woltaxi2024}
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=${BACKUP_RETENTION_DAYS:-30}

echo "🚀 Starting WOLTAXI MongoDB Backup - $(date)"

# Create backup directory if it doesn't exist
mkdir -p $BACKUP_DIR

# Database list to backup
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

# Function to backup a single database
backup_database() {
    local db_name=$1
    local backup_file="$BACKUP_DIR/${db_name}_backup_$DATE"
    
    echo "📦 Backing up database: $db_name"
    
    if mongodump \
        --host "$MONGO_HOST:$MONGO_PORT" \
        --username "$MONGO_USERNAME" \
        --password "$MONGO_PASSWORD" \
        --authenticationDatabase admin \
        --db "$db_name" \
        --out "$backup_file" \
        --gzip; then
        
        echo "✅ Successfully backed up $db_name to $backup_file"
        
        # Create tar archive for better compression
        tar -czf "${backup_file}.tar.gz" -C "$backup_file" .
        rm -rf "$backup_file"
        
        echo "🗜️ Compressed backup: ${backup_file}.tar.gz"
    else
        echo "❌ Failed to backup database: $db_name"
        return 1
    fi
}

# Function to create full cluster backup
create_full_backup() {
    local full_backup_file="$BACKUP_DIR/woltaxi_full_backup_$DATE"
    
    echo "🔄 Creating full cluster backup..."
    
    if mongodump \
        --host "$MONGO_HOST:$MONGO_PORT" \
        --username "$MONGO_USERNAME" \
        --password "$MONGO_PASSWORD" \
        --authenticationDatabase admin \
        --out "$full_backup_file" \
        --gzip; then
        
        # Create tar archive
        tar -czf "${full_backup_file}.tar.gz" -C "$full_backup_file" .
        rm -rf "$full_backup_file"
        
        echo "✅ Full backup completed: ${full_backup_file}.tar.gz"
    else
        echo "❌ Full backup failed"
        return 1
    fi
}

# Function to clean old backups
cleanup_old_backups() {
    echo "🧹 Cleaning up backups older than $RETENTION_DAYS days..."
    
    find "$BACKUP_DIR" -name "*.tar.gz" -type f -mtime +$RETENTION_DAYS -delete
    
    echo "✅ Cleanup completed"
}

# Function to check backup integrity
verify_backup() {
    local backup_file=$1
    
    echo "🔍 Verifying backup integrity: $backup_file"
    
    if tar -tzf "$backup_file" > /dev/null 2>&1; then
        echo "✅ Backup integrity verified: $backup_file"
        return 0
    else
        echo "❌ Backup integrity check failed: $backup_file"
        return 1
    fi
}

# Function to get backup statistics
get_backup_stats() {
    echo "📊 Backup Statistics:"
    echo "-------------------"
    echo "Total backups: $(find $BACKUP_DIR -name "*.tar.gz" | wc -l)"
    echo "Total size: $(du -sh $BACKUP_DIR | cut -f1)"
    echo "Oldest backup: $(find $BACKUP_DIR -name "*.tar.gz" -printf '%T+ %p\n' | sort | head -1 | cut -d' ' -f2- | xargs basename)"
    echo "Newest backup: $(find $BACKUP_DIR -name "*.tar.gz" -printf '%T+ %p\n' | sort | tail -1 | cut -d' ' -f2- | xargs basename)"
}

# Main execution
main() {
    echo "🎯 WOLTAXI MongoDB Backup Process Started"
    echo "=========================================="
    
    # Check MongoDB connection
    if ! mongosh --host "$MONGO_HOST:$MONGO_PORT" \
                 --username "$MONGO_USERNAME" \
                 --password "$MONGO_PASSWORD" \
                 --authenticationDatabase admin \
                 --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
        echo "❌ Cannot connect to MongoDB at $MONGO_HOST:$MONGO_PORT"
        exit 1
    fi
    
    echo "✅ MongoDB connection verified"
    
    # Backup individual databases
    for db in "${DATABASES[@]}"; do
        backup_database "$db"
    done
    
    # Create full backup (weekly on Sundays)
    if [ "$(date +%u)" -eq 7 ]; then
        create_full_backup
    fi
    
    # Verify the latest backup
    latest_backup=$(find $BACKUP_DIR -name "*backup_$DATE*.tar.gz" | head -1)
    if [ -n "$latest_backup" ]; then
        verify_backup "$latest_backup"
    fi
    
    # Cleanup old backups
    cleanup_old_backups
    
    # Show statistics
    get_backup_stats
    
    echo "🎉 WOLTAXI MongoDB Backup Process Completed Successfully!"
    echo "========================================================"
}

# Execute main function
main "$@"