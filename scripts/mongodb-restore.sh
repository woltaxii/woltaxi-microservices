#!/bin/bash

# WOLTAXI MongoDB Restore Script  
# This script restores MongoDB databases from backup files
# Author: WOLTAXI Development Team
# Version: 2.0.0

set -e

# Configuration
MONGO_HOST=${MONGO_HOST:-mongodb-primary}
MONGO_PORT=${MONGO_PORT:-27017}
MONGO_USERNAME=${MONGO_USERNAME:-woltaxi}
MONGO_PASSWORD=${MONGO_PASSWORD:-woltaxi2024}
BACKUP_DIR="/backups"

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -f, --file BACKUP_FILE    Restore from specific backup file"
    echo "  -d, --database DB_NAME    Restore specific database"
    echo "  --list                    List available backups"
    echo "  --latest                  Restore from latest backup"
    echo "  --full                    Restore full cluster backup"
    echo "  -h, --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --latest --database woltaxi_users"
    echo "  $0 --file woltaxi_users_backup_20241024_143022.tar.gz"
    echo "  $0 --list"
    echo "  $0 --full --latest"
}

# Function to list available backups
list_backups() {
    echo "📋 Available Backups:"
    echo "===================="
    
    if [ ! -d "$BACKUP_DIR" ]; then
        echo "❌ Backup directory not found: $BACKUP_DIR"
        return 1
    fi
    
    echo "Individual Database Backups:"
    find "$BACKUP_DIR" -name "*_backup_*.tar.gz" -not -name "*full_backup*" -printf '%T+ %p\n' | sort -r | while read -r line; do
        date_time=$(echo "$line" | cut -d' ' -f1)
        file_path=$(echo "$line" | cut -d' ' -f2-)
        file_name=$(basename "$file_path")
        file_size=$(du -h "$file_path" | cut -f1)
        echo "  📦 $file_name ($file_size) - $date_time"
    done
    
    echo ""
    echo "Full Cluster Backups:"
    find "$BACKUP_DIR" -name "*full_backup*.tar.gz" -printf '%T+ %p\n' | sort -r | while read -r line; do
        date_time=$(echo "$line" | cut -d' ' -f1)
        file_path=$(echo "$line" | cut -d' ' -f2-)
        file_name=$(basename "$file_path")
        file_size=$(du -h "$file_path" | cut -f1)
        echo "  🔄 $file_name ($file_size) - $date_time"
    done
}

# Function to get latest backup for a database
get_latest_backup() {
    local db_name=$1
    
    if [ -n "$db_name" ]; then
        find "$BACKUP_DIR" -name "${db_name}_backup_*.tar.gz" -printf '%T+ %p\n' | sort -r | head -1 | cut -d' ' -f2-
    else
        find "$BACKUP_DIR" -name "*full_backup*.tar.gz" -printf '%T+ %p\n' | sort -r | head -1 | cut -d' ' -f2-
    fi
}

# Function to restore a database
restore_database() {
    local backup_file=$1
    local target_db=$2
    local restore_dir="/tmp/restore_$(date +%s)"
    
    if [ ! -f "$backup_file" ]; then
        echo "❌ Backup file not found: $backup_file"
        return 1
    fi
    
    echo "🔄 Restoring from backup: $(basename $backup_file)"
    
    # Create temporary restore directory
    mkdir -p "$restore_dir"
    
    # Extract backup
    echo "📦 Extracting backup archive..."
    if ! tar -xzf "$backup_file" -C "$restore_dir"; then
        echo "❌ Failed to extract backup file"
        rm -rf "$restore_dir"
        return 1
    fi
    
    # Determine source database name from backup structure
    local source_db=$(find "$restore_dir" -maxdepth 1 -type d -not -path "$restore_dir" | head -1 | xargs basename)
    
    if [ -z "$source_db" ]; then
        echo "❌ Could not determine source database from backup"
        rm -rf "$restore_dir"
        return 1
    fi
    
    # Use target database name if provided, otherwise use source name
    local db_name=${target_db:-$source_db}
    
    echo "📥 Restoring database: $source_db -> $db_name"
    
    # Perform restore
    if mongorestore \
        --host "$MONGO_HOST:$MONGO_PORT" \
        --username "$MONGO_USERNAME" \
        --password "$MONGO_PASSWORD" \
        --authenticationDatabase admin \
        --db "$db_name" \
        --drop \
        --gzip \
        "$restore_dir/$source_db"; then
        
        echo "✅ Successfully restored database: $db_name"
    else
        echo "❌ Failed to restore database: $db_name"
        rm -rf "$restore_dir"
        return 1
    fi
    
    # Cleanup
    rm -rf "$restore_dir"
}

# Function to restore full cluster
restore_full_cluster() {
    local backup_file=$1
    local restore_dir="/tmp/restore_full_$(date +%s)"
    
    if [ ! -f "$backup_file" ]; then
        echo "❌ Backup file not found: $backup_file"
        return 1
    fi
    
    echo "🔄 Restoring full cluster from: $(basename $backup_file)"
    echo "⚠️  WARNING: This will drop all existing databases!"
    
    read -p "Are you sure you want to continue? (yes/no): " -r
    if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        echo "❌ Restore cancelled by user"
        return 1
    fi
    
    # Create temporary restore directory
    mkdir -p "$restore_dir"
    
    # Extract backup
    echo "📦 Extracting full backup archive..."
    if ! tar -xzf "$backup_file" -C "$restore_dir"; then
        echo "❌ Failed to extract backup file"
        rm -rf "$restore_dir"
        return 1
    fi
    
    # Perform full restore
    if mongorestore \
        --host "$MONGO_HOST:$MONGO_PORT" \
        --username "$MONGO_USERNAME" \
        --password "$MONGO_PASSWORD" \
        --authenticationDatabase admin \
        --drop \
        --gzip \
        "$restore_dir"; then
        
        echo "✅ Successfully restored full cluster"
    else
        echo "❌ Failed to restore full cluster"
        rm -rf "$restore_dir"
        return 1
    fi
    
    # Cleanup
    rm -rf "$restore_dir"
}

# Function to verify MongoDB connection
verify_connection() {
    echo "🔍 Verifying MongoDB connection..."
    
    if mongosh --host "$MONGO_HOST:$MONGO_PORT" \
               --username "$MONGO_USERNAME" \
               --password "$MONGO_PASSWORD" \
               --authenticationDatabase admin \
               --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
        echo "✅ MongoDB connection verified"
        return 0
    else
        echo "❌ Cannot connect to MongoDB at $MONGO_HOST:$MONGO_PORT"
        return 1
    fi
}

# Main function
main() {
    local backup_file=""
    local database=""
    local list_backups=false
    local use_latest=false
    local full_restore=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -f|--file)
                backup_file="$2"
                shift 2
                ;;
            -d|--database)
                database="$2"
                shift 2
                ;;
            --list)
                list_backups=true
                shift
                ;;
            --latest)
                use_latest=true
                shift
                ;;
            --full)
                full_restore=true
                shift
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                echo "❌ Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Execute based on options
    if [ "$list_backups" = true ]; then
        list_backups
        exit 0
    fi
    
    # Verify MongoDB connection
    if ! verify_connection; then
        exit 1
    fi
    
    # Handle latest backup option
    if [ "$use_latest" = true ]; then
        if [ "$full_restore" = true ]; then
            backup_file=$(get_latest_backup)
        else
            backup_file=$(get_latest_backup "$database")
        fi
        
        if [ -z "$backup_file" ]; then
            echo "❌ No backup found for the specified criteria"
            exit 1
        fi
    fi
    
    # Validate backup file
    if [ -z "$backup_file" ]; then
        echo "❌ No backup file specified"
        show_usage
        exit 1
    fi
    
    # Make backup file path absolute if relative
    if [[ "$backup_file" != /* ]]; then
        backup_file="$BACKUP_DIR/$backup_file"
    fi
    
    echo "🚀 Starting WOLTAXI MongoDB Restore Process"
    echo "==========================================="
    
    # Perform restore
    if [ "$full_restore" = true ]; then
        restore_full_cluster "$backup_file"
    else
        restore_database "$backup_file" "$database"
    fi
    
    echo "🎉 WOLTAXI MongoDB Restore Process Completed!"
    echo "============================================="
}

# Execute main function
main "$@"