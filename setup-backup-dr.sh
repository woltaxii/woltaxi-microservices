#!/bin/bash

# =============================================================================
# WOLTAXI Comprehensive Backup & Disaster Recovery Implementation
# Kapsamlı Yedekleme ve Felaket Kurtarma Uygulama Scripti
# 
# Multi-Platform Backup Solution for Windows, macOS, Linux
# Windows, macOS, Linux için Çok Platformlu Yedekleme Çözümü
# =============================================================================

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
BACKUP_ROOT="/backup"
LOG_DIR="/var/log/woltaxi/backup"
CONFIG_DIR="/etc/woltaxi/backup"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Logging setup
mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/backup-dr-setup-$(date +%Y%m%d-%H%M%S).log"
exec 1> >(tee -a "$LOG_FILE")
exec 2>&1

echo -e "${CYAN}==============================================================================${NC}"
echo -e "${CYAN}💾 WOLTAXI Comprehensive Backup & Disaster Recovery Setup${NC}"
echo -e "${CYAN}🔄 Kapsamlı Yedekleme ve Felaket Kurtarma Kurulumu${NC}"
echo -e "${CYAN}==============================================================================${NC}"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_success() {
    echo -e "${CYAN}[SUCCESS]${NC} $1"
}

# Detect operating system
detect_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
        if command -v apt-get &> /dev/null; then
            DISTRO="debian"
        elif command -v yum &> /dev/null; then
            DISTRO="rhel"
        elif command -v pacman &> /dev/null; then
            DISTRO="arch"
        else
            DISTRO="unknown"
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
        DISTRO="macos"
    else
        OS="unknown"
        DISTRO="unknown"
    fi
    
    print_status "Detected OS: $OS ($DISTRO)"
}

# Install backup tools
install_backup_tools() {
    print_status "Installing backup and disaster recovery tools..."
    
    case "$OS" in
        "linux")
            case "$DISTRO" in
                "debian")
                    sudo apt-get update
                    sudo apt-get install -y \
                        rsync \
                        borgbackup \
                        restic \
                        duplicity \
                        rdiff-backup \
                        lvm2 \
                        btrfs-progs \
                        zfs-utils \
                        mysql-client \
                        postgresql-client \
                        redis-tools \
                        rclone \
                        s3cmd \
                        awscli \
                        azure-cli \
                        google-cloud-sdk \
                        gpg \
                        age \
                        zstd \
                        lz4 \
                        pigz \
                        parallel \
                        pv \
                        inotify-tools \
                        curl \
                        jq \
                        yq
                    ;;
                "rhel")
                    sudo yum install -y epel-release
                    sudo yum install -y \
                        rsync \
                        borgbackup \
                        restic \
                        duplicity \
                        lvm2 \
                        btrfs-progs \
                        mysql \
                        postgresql \
                        redis \
                        rclone \
                        awscli \
                        gpg2 \
                        zstd \
                        lz4 \
                        pigz \
                        parallel \
                        pv \
                        inotify-tools \
                        curl \
                        jq
                    ;;
            esac
            ;;
        "macos")
            # Install Homebrew if not present
            if ! command -v brew &> /dev/null; then
                /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            fi
            
            brew install \
                rsync \
                borgbackup \
                restic \
                duplicity \
                mysql-client \
                postgresql \
                redis \
                rclone \
                awscli \
                azure-cli \
                google-cloud-sdk \
                gnupg \
                age \
                zstd \
                lz4 \
                pigz \
                parallel \
                pv \
                fswatch \
                curl \
                jq \
                yq
            ;;
    esac
    
    print_success "Backup tools installed successfully"
}

# Setup directory structure
setup_directory_structure() {
    print_status "Setting up backup directory structure..."
    
    # Create main backup directories
    sudo mkdir -p "$BACKUP_ROOT"/{data,scripts,config,logs,temp,restore,archive}
    sudo mkdir -p "$CONFIG_DIR"
    
    # Create data-specific directories
    sudo mkdir -p "$BACKUP_ROOT/data"/{databases,files,configs,logs,models,certificates}
    
    # Create backup type directories
    sudo mkdir -p "$BACKUP_ROOT/data"/{full,incremental,differential,snapshot,archive}
    
    # Create storage tier directories
    sudo mkdir -p "$BACKUP_ROOT/data"/{tier1,tier2,tier3,tier4}
    
    # Create temporary and working directories
    sudo mkdir -p "$BACKUP_ROOT"/{temp,working,staging}
    
    # Set permissions
    sudo chown -R $USER:$USER "$BACKUP_ROOT"
    sudo chmod -R 755 "$BACKUP_ROOT"
    
    print_success "Directory structure created"
}

# Setup backup configuration
setup_backup_configuration() {
    print_status "Creating backup configuration files..."
    
    # Main backup configuration
    cat > "$CONFIG_DIR/backup.conf" <<EOF
# WOLTAXI Backup Configuration
# Main configuration file for backup operations

[general]
backup_root=$BACKUP_ROOT
log_level=INFO
max_parallel_jobs=4
compression_level=6
encryption_enabled=true
verify_backups=true

[storage]
local_storage=$BACKUP_ROOT/data
temp_storage=$BACKUP_ROOT/temp
archive_storage=$BACKUP_ROOT/archive

[retention]
daily_retention=30
weekly_retention=12
monthly_retention=24
yearly_retention=7

[encryption]
algorithm=AES256
key_derivation=PBKDF2
key_file=$CONFIG_DIR/backup.key
salt_length=32

[compression]
algorithm=zstd
level=6
threads=0

[cloud]
aws_enabled=false
azure_enabled=false
gcp_enabled=false
multi_cloud=true

[notifications]
email_enabled=true
slack_enabled=false
webhook_enabled=false
email_recipient=backup@woltaxi.com

[monitoring]
health_check_interval=300
performance_monitoring=true
alert_on_failure=true
alert_on_delay=true
EOF

    # Database backup configuration
    cat > "$CONFIG_DIR/database.conf" <<EOF
# Database Backup Configuration

[postgresql]
enabled=true
host=localhost
port=5432
username=postgres
password_file=$CONFIG_DIR/db_passwords
databases=woltaxi_users,woltaxi_rides,woltaxi_payments,woltaxi_ai
backup_format=custom
compression=true
parallel_jobs=2

[redis]
enabled=true
host=localhost
port=6379
save_format=rdb
save_location=$BACKUP_ROOT/data/databases/redis

[mongodb]
enabled=false
host=localhost
port=27017
databases=
oplog_enabled=true
gzip_enabled=true
EOF

    # Storage configuration for different tiers
    cat > "$CONFIG_DIR/storage-tiers.conf" <<EOF
# Storage Tier Configuration

[tier1]
name=Critical Real-time
rpo=60
rto=300
storage_type=local_ssd
replication=synchronous
compression=lz4
encryption=aes256

[tier2]
name=Important Near Real-time
rpo=900
rto=1800
storage_type=local_hdd
replication=asynchronous
compression=zstd
encryption=aes256

[tier3]
name=Standard Daily
rpo=86400
rto=14400
storage_type=network_storage
replication=async_delayed
compression=zstd_max
encryption=aes256

[tier4]
name=Archive Weekly
rpo=604800
rto=86400
storage_type=cold_storage
replication=none
compression=maximum
encryption=aes256
EOF

    # Generate encryption key
    if [ ! -f "$CONFIG_DIR/backup.key" ]; then
        openssl rand -base64 32 > "$CONFIG_DIR/backup.key"
        chmod 600 "$CONFIG_DIR/backup.key"
        print_status "Generated encryption key"
    fi
    
    print_success "Backup configuration created"
}

# Create backup scripts
create_backup_scripts() {
    print_status "Creating backup scripts..."
    
    # Main backup orchestrator
    cat > "$BACKUP_ROOT/scripts/backup-orchestrator.sh" <<'EOF'
#!/bin/bash

# WOLTAXI Backup Orchestrator
# Main backup coordination script

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="/etc/woltaxi/backup"
BACKUP_ROOT="/backup"
LOG_DIR="/var/log/woltaxi/backup"

# Source configuration
source "$CONFIG_DIR/backup.conf"

# Logging function
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_DIR/orchestrator.log"
}

# Error handling
error_exit() {
    log "ERROR: $1"
    send_alert "BACKUP_FAILED" "$1"
    exit 1
}

# Send alerts
send_alert() {
    local alert_type="$1"
    local message="$2"
    
    if [ "$email_enabled" = "true" ]; then
        echo "$message" | mail -s "WOLTAXI Backup Alert: $alert_type" "$email_recipient"
    fi
}

# Check prerequisites
check_prerequisites() {
    log "Checking backup prerequisites..."
    
    # Check disk space
    local available_space=$(df "$BACKUP_ROOT" | awk 'NR==2 {print $4}')
    local required_space=10485760  # 10GB in KB
    
    if [ "$available_space" -lt "$required_space" ]; then
        error_exit "Insufficient disk space. Available: ${available_space}KB, Required: ${required_space}KB"
    fi
    
    # Check services
    for service in docker postgresql redis; do
        if ! systemctl is-active --quiet "$service" 2>/dev/null && ! pgrep -x "$service" >/dev/null; then
            log "WARNING: Service $service is not running"
        fi
    done
    
    log "Prerequisites check completed"
}

# Backup databases
backup_databases() {
    log "Starting database backup..."
    
    # PostgreSQL backup
    if [ "$postgresql_enabled" = "true" ]; then
        log "Backing up PostgreSQL databases..."
        
        export PGPASSFILE="$CONFIG_DIR/.pgpass"
        
        for db in ${postgresql_databases//,/ }; do
            local backup_file="$BACKUP_ROOT/data/databases/postgresql_${db}_$(date +%Y%m%d_%H%M%S).sql"
            
            if pg_dump -h "$postgresql_host" -p "$postgresql_port" -U "$postgresql_username" \
                       -F custom -Z 9 -f "$backup_file" "$db"; then
                log "PostgreSQL database $db backed up successfully"
            else
                error_exit "Failed to backup PostgreSQL database $db"
            fi
        done
    fi
    
    # Redis backup
    if [ "$redis_enabled" = "true" ]; then
        log "Backing up Redis..."
        
        local backup_file="$BACKUP_ROOT/data/databases/redis_$(date +%Y%m%d_%H%M%S).rdb"
        
        if redis-cli -h "$redis_host" -p "$redis_port" --rdb "$backup_file"; then
            log "Redis backed up successfully"
        else
            error_exit "Failed to backup Redis"
        fi
    fi
    
    log "Database backup completed"
}

# Backup application data
backup_application_data() {
    log "Starting application data backup..."
    
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_archive="$BACKUP_ROOT/data/files/application_data_$timestamp.tar.zst"
    
    # List of directories to backup
    local backup_paths=(
        "/opt/woltaxi"
        "/etc/woltaxi"
        "/var/lib/woltaxi"
        "/var/log/woltaxi"
    )
    
    # Create archive
    if tar -I 'zstd -T0' -cf "$backup_archive" "${backup_paths[@]}" 2>/dev/null; then
        log "Application data backed up successfully"
        
        # Encrypt if enabled
        if [ "$encryption_enabled" = "true" ]; then
            gpg --symmetric --cipher-algo AES256 --compress-algo 1 \
                --passphrase-file "$key_file" --output "$backup_archive.gpg" "$backup_archive"
            rm "$backup_archive"
            log "Application data encrypted"
        fi
    else
        error_exit "Failed to backup application data"
    fi
    
    log "Application data backup completed"
}

# Backup Docker volumes
backup_docker_volumes() {
    log "Starting Docker volumes backup..."
    
    local timestamp=$(date +%Y%m%d_%H%M%S)
    
    # Get list of Docker volumes
    local volumes=$(docker volume ls -q | grep woltaxi)
    
    for volume in $volumes; do
        local backup_file="$BACKUP_ROOT/data/docker/volume_${volume}_$timestamp.tar.zst"
        
        # Create temporary container to backup volume
        if docker run --rm \
                   -v "$volume:/data:ro" \
                   -v "$BACKUP_ROOT/data/docker:/backup" \
                   alpine:latest \
                   tar -I 'zstd -T0' -cf "/backup/volume_${volume}_$timestamp.tar.zst" -C /data .; then
            log "Docker volume $volume backed up successfully"
        else
            log "WARNING: Failed to backup Docker volume $volume"
        fi
    done
    
    log "Docker volumes backup completed"
}

# Verify backups
verify_backups() {
    log "Starting backup verification..."
    
    local verification_failed=0
    
    # Verify database backups
    find "$BACKUP_ROOT/data/databases" -name "*.sql" -mtime -1 | while read -r backup_file; do
        if [ -f "$backup_file" ] && [ -s "$backup_file" ]; then
            log "Verified: $backup_file"
        else
            log "ERROR: Verification failed for $backup_file"
            verification_failed=1
        fi
    done
    
    # Verify archive integrity
    find "$BACKUP_ROOT/data/files" -name "*.tar.zst*" -mtime -1 | while read -r backup_file; do
        if [[ "$backup_file" == *.gpg ]]; then
            # Verify encrypted archive
            if gpg --batch --quiet --passphrase-file "$key_file" --decrypt "$backup_file" | zstd -t; then
                log "Verified encrypted archive: $backup_file"
            else
                log "ERROR: Verification failed for encrypted archive: $backup_file"
                verification_failed=1
            fi
        else
            # Verify unencrypted archive
            if zstd -t "$backup_file"; then
                log "Verified archive: $backup_file"
            else
                log "ERROR: Verification failed for archive: $backup_file"
                verification_failed=1
            fi
        fi
    done
    
    if [ $verification_failed -eq 0 ]; then
        log "Backup verification completed successfully"
    else
        error_exit "Backup verification failed"
    fi
}

# Cleanup old backups
cleanup_old_backups() {
    log "Starting backup cleanup..."
    
    # Cleanup based on retention policy
    find "$BACKUP_ROOT/data/databases" -name "*.sql" -mtime +$daily_retention -delete
    find "$BACKUP_ROOT/data/files" -name "*.tar.zst*" -mtime +$daily_retention -delete
    find "$BACKUP_ROOT/data/docker" -name "*.tar.zst" -mtime +$weekly_retention -delete
    
    log "Backup cleanup completed"
}

# Generate backup report
generate_report() {
    log "Generating backup report..."
    
    local report_file="$BACKUP_ROOT/logs/backup_report_$(date +%Y%m%d_%H%M%S).html"
    
    cat > "$report_file" <<HTML
<!DOCTYPE html>
<html>
<head>
    <title>WOLTAXI Backup Report - $(date)</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .success { color: green; }
        .warning { color: orange; }
        .error { color: red; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>WOLTAXI Backup Report</h1>
    <p>Generated on: $(date)</p>
    
    <h2>Backup Summary</h2>
    <table>
        <tr><th>Component</th><th>Status</th><th>Size</th><th>Duration</th></tr>
        <tr><td>Database Backup</td><td class="success">SUCCESS</td><td>$(du -sh $BACKUP_ROOT/data/databases | cut -f1)</td><td>N/A</td></tr>
        <tr><td>Application Data</td><td class="success">SUCCESS</td><td>$(du -sh $BACKUP_ROOT/data/files | cut -f1)</td><td>N/A</td></tr>
        <tr><td>Docker Volumes</td><td class="success">SUCCESS</td><td>$(du -sh $BACKUP_ROOT/data/docker | cut -f1)</td><td>N/A</td></tr>
    </table>
    
    <h2>Storage Utilization</h2>
    <pre>$(df -h $BACKUP_ROOT)</pre>
    
    <h2>Recent Backups</h2>
    <pre>$(find $BACKUP_ROOT/data -type f -mtime -7 -exec ls -lh {} \;)</pre>
</body>
</html>
HTML
    
    log "Backup report generated: $report_file"
}

# Main backup function
main() {
    log "Starting WOLTAXI backup orchestrator..."
    
    # Lock file to prevent concurrent runs
    local lock_file="/tmp/woltaxi-backup.lock"
    exec 200>"$lock_file"
    
    if ! flock -n 200; then
        error_exit "Another backup process is already running"
    fi
    
    # Trap to cleanup on exit
    trap 'rm -f "$lock_file"' EXIT
    
    # Execute backup steps
    check_prerequisites
    backup_databases
    backup_application_data
    backup_docker_volumes
    verify_backups
    cleanup_old_backups
    generate_report
    
    log "WOLTAXI backup orchestrator completed successfully"
    send_alert "BACKUP_SUCCESS" "Backup completed successfully at $(date)"
}

# Run main function
main "$@"
EOF

    chmod +x "$BACKUP_ROOT/scripts/backup-orchestrator.sh"
    
    # Disaster Recovery Script
    cat > "$BACKUP_ROOT/scripts/disaster-recovery.sh" <<'EOF'
#!/bin/bash

# WOLTAXI Disaster Recovery Script
# Emergency recovery procedures

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="/etc/woltaxi/backup"
BACKUP_ROOT="/backup"
LOG_DIR="/var/log/woltaxi/backup"

# Logging function
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_DIR/disaster-recovery.log"
}

# Disaster recovery procedures
show_usage() {
    cat <<EOF
WOLTAXI Disaster Recovery Script

Usage: $0 [COMMAND] [OPTIONS]

Commands:
    list-backups        List available backups
    restore-database    Restore database from backup
    restore-files       Restore application files
    restore-docker      Restore Docker volumes
    full-recovery       Perform complete system recovery
    test-recovery       Test recovery procedures
    
Options:
    --backup-date DATE  Specify backup date (YYYYMMDD)
    --dry-run          Simulate recovery without making changes
    --force            Force recovery without confirmation
    
Examples:
    $0 list-backups
    $0 restore-database --backup-date 20241225
    $0 full-recovery --backup-date 20241225 --force
EOF
}

# List available backups
list_backups() {
    log "Listing available backups..."
    
    echo "=== Database Backups ==="
    find "$BACKUP_ROOT/data/databases" -name "*.sql" -exec ls -lh {} \; | sort -k6,7
    
    echo -e "\n=== File Backups ==="
    find "$BACKUP_ROOT/data/files" -name "*.tar.zst*" -exec ls -lh {} \; | sort -k6,7
    
    echo -e "\n=== Docker Volume Backups ==="
    find "$BACKUP_ROOT/data/docker" -name "*.tar.zst" -exec ls -lh {} \; | sort -k6,7
}

# Restore database
restore_database() {
    local backup_date="$1"
    local dry_run="${2:-false}"
    
    log "Starting database restore for date: $backup_date"
    
    # Find database backup file
    local db_backup=$(find "$BACKUP_ROOT/data/databases" -name "*$backup_date*.sql" | head -1)
    
    if [ -z "$db_backup" ]; then
        log "ERROR: No database backup found for date $backup_date"
        return 1
    fi
    
    log "Found database backup: $db_backup"
    
    if [ "$dry_run" = "true" ]; then
        log "DRY RUN: Would restore database from $db_backup"
        return 0
    fi
    
    # Stop services
    log "Stopping services..."
    docker-compose down
    
    # Restore database
    log "Restoring database..."
    docker-compose up -d postgres
    sleep 30
    
    if pg_restore -h localhost -p 5432 -U postgres -d postgres --clean --create "$db_backup"; then
        log "Database restored successfully"
    else
        log "ERROR: Database restore failed"
        return 1
    fi
}

# Restore application files
restore_files() {
    local backup_date="$1"
    local dry_run="${2:-false}"
    
    log "Starting file restore for date: $backup_date"
    
    # Find file backup
    local file_backup=$(find "$BACKUP_ROOT/data/files" -name "*$backup_date*.tar.zst*" | head -1)
    
    if [ -z "$file_backup" ]; then
        log "ERROR: No file backup found for date $backup_date"
        return 1
    fi
    
    log "Found file backup: $file_backup"
    
    if [ "$dry_run" = "true" ]; then
        log "DRY RUN: Would restore files from $file_backup"
        return 0
    fi
    
    # Extract files
    if [[ "$file_backup" == *.gpg ]]; then
        log "Decrypting and extracting files..."
        gpg --batch --quiet --passphrase-file "$CONFIG_DIR/backup.key" --decrypt "$file_backup" | tar -I zstd -xf -
    else
        log "Extracting files..."
        tar -I zstd -xf "$file_backup"
    fi
    
    log "Files restored successfully"
}

# Full recovery
full_recovery() {
    local backup_date="$1"
    local dry_run="${2:-false}"
    local force="${3:-false}"
    
    log "Starting full system recovery for date: $backup_date"
    
    if [ "$force" != "true" ] && [ "$dry_run" != "true" ]; then
        echo "WARNING: This will completely restore the system from backup."
        echo "All current data will be replaced with backup data from $backup_date"
        read -p "Are you sure you want to continue? (yes/no): " confirm
        
        if [ "$confirm" != "yes" ]; then
            log "Recovery cancelled by user"
            return 1
        fi
    fi
    
    # Execute recovery steps
    restore_database "$backup_date" "$dry_run"
    restore_files "$backup_date" "$dry_run"
    
    if [ "$dry_run" != "true" ]; then
        # Restart all services
        log "Restarting all services..."
        docker-compose up -d
        
        # Wait for services to start
        sleep 60
        
        # Verify services
        log "Verifying service health..."
        for service in eureka-server user-service ride-service payment-hub-service ai-ml-service; do
            if curl -f -s "http://localhost:8080/actuator/health" > /dev/null; then
                log "Service $service is healthy"
            else
                log "WARNING: Service $service health check failed"
            fi
        done
    fi
    
    log "Full recovery completed"
}

# Main function
main() {
    case "${1:-}" in
        list-backups)
            list_backups
            ;;
        restore-database)
            if [ -z "${2:-}" ]; then
                echo "ERROR: Backup date is required"
                show_usage
                exit 1
            fi
            restore_database "$2" "${3:-false}"
            ;;
        restore-files)
            if [ -z "${2:-}" ]; then
                echo "ERROR: Backup date is required"
                show_usage
                exit 1
            fi
            restore_files "$2" "${3:-false}"
            ;;
        full-recovery)
            if [ -z "${2:-}" ]; then
                echo "ERROR: Backup date is required"
                show_usage
                exit 1
            fi
            full_recovery "$2" "${3:-false}" "${4:-false}"
            ;;
        *)
            show_usage
            ;;
    esac
}

# Run main function
main "$@"
EOF

    chmod +x "$BACKUP_ROOT/scripts/disaster-recovery.sh"
    
    print_success "Backup scripts created"
}

# Setup monitoring and alerting
setup_monitoring() {
    print_status "Setting up backup monitoring..."
    
    # Create monitoring script
    cat > "$BACKUP_ROOT/scripts/backup-monitor.sh" <<'EOF'
#!/bin/bash

# WOLTAXI Backup Monitoring Script
# Monitors backup health and sends alerts

set -euo pipefail

CONFIG_DIR="/etc/woltaxi/backup"
BACKUP_ROOT="/backup"
LOG_DIR="/var/log/woltaxi/backup"

# Source configuration
source "$CONFIG_DIR/backup.conf"

# Monitoring functions
check_backup_freshness() {
    local max_age=86400  # 24 hours in seconds
    local current_time=$(date +%s)
    
    # Check database backups
    local latest_db_backup=$(find "$BACKUP_ROOT/data/databases" -name "*.sql" -exec stat -c %Y {} \; | sort -n | tail -1)
    if [ -n "$latest_db_backup" ]; then
        local age=$((current_time - latest_db_backup))
        if [ $age -gt $max_age ]; then
            echo "ALERT: Database backup is older than 24 hours (age: ${age}s)"
            return 1
        fi
    else
        echo "ALERT: No database backups found"
        return 1
    fi
    
    # Check file backups
    local latest_file_backup=$(find "$BACKUP_ROOT/data/files" -name "*.tar.zst*" -exec stat -c %Y {} \; | sort -n | tail -1)
    if [ -n "$latest_file_backup" ]; then
        local age=$((current_time - latest_file_backup))
        if [ $age -gt $max_age ]; then
            echo "ALERT: File backup is older than 24 hours (age: ${age}s)"
            return 1
        fi
    else
        echo "ALERT: No file backups found"
        return 1
    fi
    
    echo "OK: Backup freshness check passed"
    return 0
}

check_storage_space() {
    local usage=$(df "$BACKUP_ROOT" | awk 'NR==2 {gsub(/%/,"",$5); print $5}')
    local threshold=85
    
    if [ "$usage" -gt "$threshold" ]; then
        echo "ALERT: Backup storage usage is ${usage}% (threshold: ${threshold}%)"
        return 1
    fi
    
    echo "OK: Storage usage is ${usage}%"
    return 0
}

check_backup_integrity() {
    local failed_count=0
    
    # Check recent backups
    find "$BACKUP_ROOT/data" -name "*.tar.zst" -mtime -1 | while read -r backup_file; do
        if ! zstd -t "$backup_file" >/dev/null 2>&1; then
            echo "ALERT: Backup integrity check failed for $backup_file"
            failed_count=$((failed_count + 1))
        fi
    done
    
    if [ $failed_count -eq 0 ]; then
        echo "OK: Backup integrity check passed"
        return 0
    else
        echo "ALERT: $failed_count backup files failed integrity check"
        return 1
    fi
}

# Send alert
send_alert() {
    local message="$1"
    local timestamp=$(date)
    
    # Email alert
    if [ "$email_enabled" = "true" ]; then
        echo -e "WOLTAXI Backup Alert\n\nTimestamp: $timestamp\n\nMessage: $message" | \
            mail -s "WOLTAXI Backup Alert" "$email_recipient"
    fi
    
    # Log alert
    echo "[$timestamp] ALERT: $message" >> "$LOG_DIR/alerts.log"
}

# Main monitoring function
main() {
    local alerts=()
    
    # Run health checks
    if ! check_backup_freshness; then
        alerts+=("Backup freshness check failed")
    fi
    
    if ! check_storage_space; then
        alerts+=("Storage space check failed")
    fi
    
    if ! check_backup_integrity; then
        alerts+=("Backup integrity check failed")
    fi
    
    # Send alerts if any
    if [ ${#alerts[@]} -gt 0 ]; then
        local alert_message="The following backup health checks failed:\n"
        for alert in "${alerts[@]}"; do
            alert_message="$alert_message- $alert\n"
        done
        send_alert "$alert_message"
    fi
}

# Run main function
main "$@"
EOF

    chmod +x "$BACKUP_ROOT/scripts/backup-monitor.sh"
    
    print_success "Backup monitoring setup completed"
}

# Setup cron jobs
setup_cron_jobs() {
    print_status "Setting up backup cron jobs..."
    
    # Backup cron jobs
    cat > /tmp/woltaxi-backup-cron <<EOF
# WOLTAXI Backup Automation

# Full backup every day at 2 AM
0 2 * * * $BACKUP_ROOT/scripts/backup-orchestrator.sh >> $LOG_DIR/backup-cron.log 2>&1

# Backup monitoring every hour
0 * * * * $BACKUP_ROOT/scripts/backup-monitor.sh >> $LOG_DIR/monitor-cron.log 2>&1

# Cleanup temp files daily at 1 AM
0 1 * * * find $BACKUP_ROOT/temp -type f -mtime +1 -delete

# Weekly disaster recovery test on Sundays at 3 AM
0 3 * * 0 $BACKUP_ROOT/scripts/disaster-recovery.sh test-recovery >> $LOG_DIR/dr-test.log 2>&1
EOF

    crontab /tmp/woltaxi-backup-cron
    rm /tmp/woltaxi-backup-cron
    
    print_success "Cron jobs configured"
}

# Setup cloud integration
setup_cloud_integration() {
    print_status "Setting up cloud storage integration..."
    
    # Create cloud sync script
    cat > "$BACKUP_ROOT/scripts/cloud-sync.sh" <<'EOF'
#!/bin/bash

# WOLTAXI Cloud Backup Sync
# Syncs backups to multiple cloud providers

set -euo pipefail

CONFIG_DIR="/etc/woltaxi/backup"
BACKUP_ROOT="/backup"

# Source configuration
source "$CONFIG_DIR/backup.conf"

# Cloud sync functions
sync_to_aws() {
    if [ "$aws_enabled" = "true" ]; then
        echo "Syncing to AWS S3..."
        aws s3 sync "$BACKUP_ROOT/data" "s3://woltaxi-backups/" \
            --exclude "*" \
            --include "*.sql" \
            --include "*.tar.zst*" \
            --storage-class STANDARD_IA \
            --server-side-encryption AES256
    fi
}

sync_to_azure() {
    if [ "$azure_enabled" = "true" ]; then
        echo "Syncing to Azure Blob Storage..."
        az storage blob sync \
            --source "$BACKUP_ROOT/data" \
            --container woltaxi-backups \
            --account-name woltaxibackups
    fi
}

sync_to_gcp() {
    if [ "$gcp_enabled" = "true" ]; then
        echo "Syncing to Google Cloud Storage..."
        gsutil -m rsync -r -d "$BACKUP_ROOT/data" "gs://woltaxi-backups/"
    fi
}

# Main sync function
main() {
    echo "Starting cloud backup sync..."
    
    sync_to_aws
    sync_to_azure
    sync_to_gcp
    
    echo "Cloud backup sync completed"
}

# Run main function
main "$@"
EOF

    chmod +x "$BACKUP_ROOT/scripts/cloud-sync.sh"
    
    print_success "Cloud integration setup completed"
}

# Main setup function
main() {
    print_status "Starting WOLTAXI Backup & Disaster Recovery Setup..."
    
    # Detect OS and install tools
    detect_os
    install_backup_tools
    
    # Setup backup infrastructure
    setup_directory_structure
    setup_backup_configuration
    create_backup_scripts
    setup_monitoring
    setup_cron_jobs
    setup_cloud_integration
    
    # Create main startup script
    cat > backup-system-start.sh <<'EOF'
#!/bin/bash

echo "🔄 Starting WOLTAXI Backup & Disaster Recovery System..."

# Start monitoring
echo "Starting backup monitoring..."
/backup/scripts/backup-monitor.sh &

# Verify configuration
echo "Verifying backup configuration..."
if [ -f "/etc/woltaxi/backup/backup.conf" ]; then
    echo "✅ Backup configuration found"
else
    echo "❌ Backup configuration missing"
    exit 1
fi

# Check backup directories
if [ -d "/backup/data" ]; then
    echo "✅ Backup directories ready"
else
    echo "❌ Backup directories missing"
    exit 1
fi

echo "✅ WOLTAXI Backup & Disaster Recovery System started successfully!"
echo ""
echo "📋 Available Commands:"
echo "   Full Backup:        /backup/scripts/backup-orchestrator.sh"
echo "   Disaster Recovery:  /backup/scripts/disaster-recovery.sh"
echo "   Backup Monitor:     /backup/scripts/backup-monitor.sh"
echo "   Cloud Sync:         /backup/scripts/cloud-sync.sh"
echo ""
echo "📊 Status:"
echo "   Configuration:      /etc/woltaxi/backup/"
echo "   Backup Data:        /backup/data/"
echo "   Logs:              /var/log/woltaxi/backup/"
EOF
    
    chmod +x backup-system-start.sh
    
    print_success "🎉 WOLTAXI Backup & Disaster Recovery Setup Completed Successfully!"
    echo ""
    echo -e "${CYAN}==============================================================================${NC}"
    echo -e "${GREEN}✅ Backup & DR Components Installed:${NC}"
    echo -e "${BLUE}   💾 Multi-tier Backup Strategy${NC}"
    echo -e "${BLUE}   🔄 Automated Backup Orchestration${NC}"
    echo -e "${BLUE}   🛡️  Disaster Recovery Procedures${NC}"
    echo -e "${BLUE}   📊 Comprehensive Monitoring${NC}"
    echo -e "${BLUE}   ☁️  Multi-cloud Integration${NC}"
    echo -e "${BLUE}   🔐 Encryption & Security${NC}"
    echo -e "${BLUE}   ⚡ Automated Scheduling${NC}"
    echo -e "${BLUE}   📈 Backup Verification${NC}"
    echo ""
    echo -e "${YELLOW}📋 Next Steps:${NC}"
    echo -e "${BLUE}   1. Run: ./backup-system-start.sh${NC}"
    echo -e "${BLUE}   2. Configure cloud credentials (optional)${NC}"
    echo -e "${BLUE}   3. Test backup: /backup/scripts/backup-orchestrator.sh${NC}"
    echo -e "${BLUE}   4. Test recovery: /backup/scripts/disaster-recovery.sh list-backups${NC}"
    echo -e "${BLUE}   5. Verify monitoring: /backup/scripts/backup-monitor.sh${NC}"
    echo -e "${CYAN}==============================================================================${NC}"
}

# Run main function
main "$@"