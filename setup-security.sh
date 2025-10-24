#!/bin/bash

# =============================================================================
# WOLTAXI Enterprise Security Implementation Script
# Kurumsal Güvenlik Uygulama Scripti
# 
# Cross-Platform Security Setup for Windows, macOS, Linux
# Windows, macOS, Linux için Çapraz Platform Güvenlik Kurulumu
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

# Logging setup
LOG_FILE="security-setup-$(date +%Y%m%d-%H%M%S).log"
exec 1> >(tee -a "$LOG_FILE")
exec 2>&1

echo -e "${CYAN}==============================================================================${NC}"
echo -e "${CYAN}🔒 WOLTAXI Enterprise Security Implementation${NC}"
echo -e "${CYAN}🛡️  Kurumsal Güvenlik Uygulama Sistemi${NC}"
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
    elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
        OS="windows"
        DISTRO="windows"
    else
        OS="unknown"
        DISTRO="unknown"
    fi
    
    print_status "Detected OS: $OS ($DISTRO)"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    local missing_tools=()
    
    # Essential tools
    command -v docker >/dev/null 2>&1 || missing_tools+=("docker")
    command -v docker-compose >/dev/null 2>&1 || missing_tools+=("docker-compose")
    command -v openssl >/dev/null 2>&1 || missing_tools+=("openssl")
    command -v curl >/dev/null 2>&1 || missing_tools+=("curl")
    command -v jq >/dev/null 2>&1 || missing_tools+=("jq")
    
    if [ ${#missing_tools[@]} -ne 0 ]; then
        print_error "Missing required tools: ${missing_tools[*]}"
        print_status "Installing missing tools..."
        install_prerequisites "${missing_tools[@]}"
    else
        print_success "All prerequisites are installed"
    fi
}

# Install prerequisites based on OS
install_prerequisites() {
    local tools=("$@")
    
    case "$OS" in
        "linux")
            case "$DISTRO" in
                "debian")
                    sudo apt-get update
                    for tool in "${tools[@]}"; do
                        case "$tool" in
                            "docker")
                                curl -fsSL https://get.docker.com -o get-docker.sh
                                sudo sh get-docker.sh
                                sudo usermod -aG docker $USER
                                ;;
                            "docker-compose")
                                sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
                                sudo chmod +x /usr/local/bin/docker-compose
                                ;;
                            *)
                                sudo apt-get install -y "$tool"
                                ;;
                        esac
                    done
                    ;;
                "rhel")
                    for tool in "${tools[@]}"; do
                        case "$tool" in
                            "docker")
                                sudo yum install -y docker
                                sudo systemctl start docker
                                sudo systemctl enable docker
                                sudo usermod -aG docker $USER
                                ;;
                            *)
                                sudo yum install -y "$tool"
                                ;;
                        esac
                    done
                    ;;
            esac
            ;;
        "macos")
            # Install Homebrew if not present
            if ! command -v brew &> /dev/null; then
                /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            fi
            
            for tool in "${tools[@]}"; do
                case "$tool" in
                    "docker")
                        brew install --cask docker
                        ;;
                    "docker-compose")
                        brew install docker-compose
                        ;;
                    *)
                        brew install "$tool"
                        ;;
                esac
            done
            ;;
    esac
}

# Generate SSL certificates
generate_ssl_certificates() {
    print_status "Generating SSL certificates..."
    
    mkdir -p ssl/certs ssl/private ssl/ca
    
    # Generate CA private key
    openssl genrsa -out ssl/ca/ca-key.pem 4096
    
    # Generate CA certificate
    openssl req -new -x509 -days 3650 -key ssl/ca/ca-key.pem -sha256 -out ssl/ca/ca-cert.pem -subj "/C=TR/ST=Istanbul/L=Istanbul/O=WOLTAXI/OU=Security/CN=WOLTAXI-CA"
    
    # Generate server private key
    openssl genrsa -out ssl/private/server-key.pem 4096
    
    # Generate server certificate signing request
    openssl req -subj "/C=TR/ST=Istanbul/L=Istanbul/O=WOLTAXI/OU=Security/CN=woltaxi.com" -sha256 -new -key ssl/private/server-key.pem -out ssl/server.csr
    
    # Create extensions file
    cat > ssl/server-extfile.cnf <<EOF
subjectAltName = DNS:woltaxi.com,DNS:*.woltaxi.com,DNS:localhost,IP:127.0.0.1
extendedKeyUsage = serverAuth
EOF
    
    # Generate server certificate signed by CA
    openssl x509 -req -days 365 -in ssl/server.csr -CA ssl/ca/ca-cert.pem -CAkey ssl/ca/ca-key.pem -out ssl/certs/server-cert.pem -extensions v3_req -extfile ssl/server-extfile.cnf -CAcreateserial
    
    # Set permissions
    chmod 400 ssl/private/server-key.pem ssl/ca/ca-key.pem
    chmod 444 ssl/certs/server-cert.pem ssl/ca/ca-cert.pem
    
    # Clean up
    rm ssl/server.csr ssl/server-extfile.cnf
    
    print_success "SSL certificates generated successfully"
}

# Setup HashiCorp Vault for secrets management
setup_vault() {
    print_status "Setting up HashiCorp Vault..."
    
    # Create vault directory
    mkdir -p vault/config vault/data vault/logs
    
    # Create vault configuration
    cat > vault/config/vault.hcl <<EOF
storage "file" {
  path = "/vault/data"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  tls_cert_file = "/vault/ssl/certs/server-cert.pem"
  tls_key_file = "/vault/ssl/private/server-key.pem"
}

ui = true
api_addr = "https://127.0.0.1:8200"
cluster_addr = "https://127.0.0.1:8201"
disable_mlock = true
EOF
    
    # Create vault Docker service
    cat > vault/docker-compose.vault.yml <<EOF
version: '3.8'

services:
  vault:
    image: vault:1.15.2
    container_name: woltaxi-vault
    restart: unless-stopped
    ports:
      - "8200:8200"
    volumes:
      - ./config:/vault/config:ro
      - ./data:/vault/data
      - ./logs:/vault/logs
      - ../ssl:/vault/ssl:ro
    cap_add:
      - IPC_LOCK
    environment:
      VAULT_CONFIG_DIR: /vault/config
      VAULT_API_ADDR: https://0.0.0.0:8200
    command: vault server -config=/vault/config/vault.hcl
    healthcheck:
      test: ["CMD", "vault", "status"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - woltaxi-security

networks:
  woltaxi-security:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
EOF
    
    print_success "Vault configuration created"
}

# Setup monitoring and logging
setup_monitoring() {
    print_status "Setting up monitoring and logging..."
    
    mkdir -p monitoring/prometheus monitoring/grafana monitoring/alertmanager monitoring/elasticsearch monitoring/kibana monitoring/logstash
    
    # Prometheus configuration
    cat > monitoring/prometheus/prometheus.yml <<EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "security_rules.yml"
  - "performance_rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  - job_name: 'woltaxi-services'
    static_configs:
      - targets: 
        - 'eureka-server:8761'
        - 'user-service:8081'
        - 'ride-service:8082'
        - 'payment-hub-service:8083'
        - 'ai-ml-service:8094'
    metrics_path: /actuator/prometheus
    scrape_interval: 5s
    
  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']
      
  - job_name: 'cadvisor'
    static_configs:
      - targets: ['cadvisor:8080']
EOF
    
    # Security monitoring rules
    cat > monitoring/prometheus/security_rules.yml <<EOF
groups:
  - name: woltaxi.security
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Service {{ \$labels.job }} has error rate of {{ \$value }}"
      
      - alert: UnauthorizedAccess
        expr: rate(http_requests_total{status="401"}[5m]) > 10
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High unauthorized access attempts"
          description: "Service {{ \$labels.job }} has {{ \$value }} unauthorized attempts per second"
      
      - alert: DDoSAttack
        expr: rate(http_requests_total[1m]) > 1000
        for: 30s
        labels:
          severity: critical
        annotations:
          summary: "Potential DDoS attack detected"
          description: "Service {{ \$labels.job }} receiving {{ \$value }} requests per second"
EOF
    
    # ELK Stack configuration
    cat > monitoring/docker-compose.monitoring.yml <<EOF
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:v2.45.0
    container_name: woltaxi-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus:/etc/prometheus
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--web.enable-lifecycle'
      - '--web.enable-admin-api'
    networks:
      - woltaxi-monitoring

  grafana:
    image: grafana/grafana:10.2.0
    container_name: woltaxi-grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=woltaxi-secure-2024
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_SERVER_CERT_FILE=/etc/ssl/certs/server-cert.pem
      - GF_SERVER_CERT_KEY=/etc/ssl/private/server-key.pem
      - GF_SERVER_PROTOCOL=https
    volumes:
      - grafana-data:/var/lib/grafana
      - ../ssl:/etc/ssl:ro
    networks:
      - woltaxi-monitoring

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: woltaxi-elasticsearch
    restart: unless-stopped
    environment:
      - node.name=woltaxi-es01
      - cluster.name=woltaxi-cluster
      - discovery.type=single-node
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
      - xpack.security.enabled=true
      - xpack.security.http.ssl.enabled=true
      - xpack.security.http.ssl.key=/usr/share/elasticsearch/config/ssl/server-key.pem
      - xpack.security.http.ssl.certificate=/usr/share/elasticsearch/config/ssl/server-cert.pem
      - xpack.security.transport.ssl.enabled=true
      - xpack.security.transport.ssl.key=/usr/share/elasticsearch/config/ssl/server-key.pem
      - xpack.security.transport.ssl.certificate=/usr/share/elasticsearch/config/ssl/server-cert.pem
      - ELASTIC_PASSWORD=woltaxi-elastic-2024
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
      - ../ssl:/usr/share/elasticsearch/config/ssl:ro
    ports:
      - "9200:9200"
    networks:
      - woltaxi-monitoring

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: woltaxi-kibana
    restart: unless-stopped
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=https://elasticsearch:9200
      - ELASTICSEARCH_USERNAME=elastic
      - ELASTICSEARCH_PASSWORD=woltaxi-elastic-2024
      - ELASTICSEARCH_SSL_CERTIFICATEAUTHORITIES=/usr/share/kibana/config/ssl/ca-cert.pem
      - SERVER_SSL_ENABLED=true
      - SERVER_SSL_CERTIFICATE=/usr/share/kibana/config/ssl/server-cert.pem
      - SERVER_SSL_KEY=/usr/share/kibana/config/ssl/server-key.pem
    volumes:
      - ../ssl:/usr/share/kibana/config/ssl:ro
    depends_on:
      - elasticsearch
    networks:
      - woltaxi-monitoring

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    container_name: woltaxi-logstash
    restart: unless-stopped
    volumes:
      - ./logstash/config:/usr/share/logstash/config:ro
      - ./logstash/pipeline:/usr/share/logstash/pipeline:ro
      - ../ssl:/usr/share/logstash/config/ssl:ro
    ports:
      - "5044:5044"
      - "9600:9600"
    environment:
      - LS_JAVA_OPTS=-Xmx1g -Xms1g
    depends_on:
      - elasticsearch
    networks:
      - woltaxi-monitoring

volumes:
  prometheus-data:
  grafana-data:
  elasticsearch-data:

networks:
  woltaxi-monitoring:
    driver: bridge
EOF
    
    print_success "Monitoring and logging setup completed"
}

# Setup firewall rules
setup_firewall() {
    print_status "Setting up firewall rules..."
    
    case "$OS" in
        "linux")
            # UFW (Uncomplicated Firewall) for Ubuntu/Debian
            if command -v ufw &> /dev/null; then
                sudo ufw --force reset
                sudo ufw default deny incoming
                sudo ufw default allow outgoing
                
                # Allow SSH
                sudo ufw allow 22/tcp
                
                # Allow HTTP/HTTPS
                sudo ufw allow 80/tcp
                sudo ufw allow 443/tcp
                
                # Allow WOLTAXI services
                sudo ufw allow 8761/tcp comment "Eureka Server"
                sudo ufw allow 8081/tcp comment "User Service"
                sudo ufw allow 8082/tcp comment "Ride Service"
                sudo ufw allow 8083/tcp comment "Payment Hub"
                sudo ufw allow 8094/tcp comment "AI/ML Service"
                
                # Allow monitoring
                sudo ufw allow 3000/tcp comment "Grafana"
                sudo ufw allow 9090/tcp comment "Prometheus"
                sudo ufw allow 5601/tcp comment "Kibana"
                
                # Allow Vault
                sudo ufw allow 8200/tcp comment "Vault"
                
                # Rate limiting
                sudo ufw limit ssh
                
                sudo ufw --force enable
                print_success "UFW firewall configured"
            fi
            ;;
        "macos")
            # macOS Application Firewall
            sudo /usr/libexec/ApplicationFirewall/socketfilterfw --setglobalstate on
            sudo /usr/libexec/ApplicationFirewall/socketfilterfw --setloggingmode on
            sudo /usr/libexec/ApplicationFirewall/socketfilterfw --setstealthmode on
            print_success "macOS firewall configured"
            ;;
    esac
}

# Setup intrusion detection
setup_intrusion_detection() {
    print_status "Setting up intrusion detection..."
    
    mkdir -p security/suricata/rules security/suricata/logs
    
    # Suricata configuration
    cat > security/suricata/suricata.yaml <<EOF
vars:
  address-groups:
    HOME_NET: "[192.168.0.0/16,10.0.0.0/8,172.16.0.0/12]"
    EXTERNAL_NET: "!$HOME_NET"
    HTTP_SERVERS: "$HOME_NET"
    SMTP_SERVERS: "$HOME_NET"
    SQL_SERVERS: "$HOME_NET"
    DNS_SERVERS: "$HOME_NET"
    TELNET_SERVERS: "$HOME_NET"
    AIM_SERVERS: "$EXTERNAL_NET"
    DC_SERVERS: "$HOME_NET"
    DNP3_SERVER: "$HOME_NET"
    DNP3_CLIENT: "$HOME_NET"
    MODBUS_CLIENT: "$HOME_NET"
    MODBUS_SERVER: "$HOME_NET"
    ENIP_CLIENT: "$HOME_NET"
    ENIP_SERVER: "$HOME_NET"

  port-groups:
    HTTP_PORTS: "80"
    SHELLCODE_PORTS: "!80"
    ORACLE_PORTS: "1521"
    SSH_PORTS: "22"
    DNP3_PORTS: "20000"
    MODBUS_PORTS: "502"
    FILE_DATA_PORTS: "[$HTTP_PORTS,110,143]"
    FTP_PORTS: "21"
    GENEVE_PORTS: "6081"
    VXLAN_PORTS: "4789"
    TEREDO_PORTS: "3544"

default-log-dir: /var/log/suricata/

stats:
  enabled: yes
  interval: 8

outputs:
  - fast:
      enabled: yes
      filename: fast.log
      append: yes
  - eve-log:
      enabled: yes
      filetype: regular
      filename: eve.json
      types:
        - alert:
            payload: yes
            packet: yes
            http-body: yes
            http-body-printable: yes
            metadata: no
        - http:
            extended: yes
        - dns:
            query: yes
            answer: yes
        - tls:
            extended: yes
        - files:
            force-magic: no
        - smtp:
        - ssh
        - stats:
            totals: yes
            threads: no
            deltas: no
        - flow

logging:
  default-log-level: notice
  default-log-format: "[%i] %t - (%f:%l) <%d> (%n) -- "

af-packet:
  - interface: eth0
    cluster-id: 99
    cluster-type: cluster_flow
    defrag: yes
  - interface: default

pcap:
  - interface: eth0

pcap-file:
  checksum-checks: auto

app-layer:
  protocols:
    tls:
      enabled: yes
      detection-ports:
        dp: "443"
    http:
      enabled: yes
      libhtp:
        default-config:
          personality: IDS
          request-body-limit: 100kb
          response-body-limit: 100kb
          request-body-minimal-inspect-size: 32kb
          request-body-inspect-window: 4kb
          response-body-minimal-inspect-size: 40kb
          response-body-inspect-window: 16kb
          response-body-decompress-layer-limit: 2
          http-body-inline: auto
          swf-decompression:
            enabled: yes
            type: both
            compress-depth: 0
            decompress-depth: 0
          double-decode-path: no
          double-decode-query: no

asn1-max-frames: 256

run-as:
  user: suricata
  group: suricata

sensor-name: woltaxi-ids

include: /etc/suricata/classification.config
include: /etc/suricata/reference.config
include: /etc/suricata/threshold.config
EOF
    
    # Custom WOLTAXI rules
    cat > security/suricata/rules/woltaxi-local.rules <<EOF
# WOLTAXI Custom Security Rules

# Detect SQL injection attempts
alert http any any -> any any (msg:"WOLTAXI SQL Injection Attempt"; flow:established,to_server; content:"GET"; http_method; pcre:"/(\%27)|(\')|(\-\-)|(\%23)|(#)/i"; classtype:web-application-attack; sid:1000001; rev:1;)
alert http any any -> any any (msg:"WOLTAXI SQL Injection Attempt POST"; flow:established,to_server; content:"POST"; http_method; pcre:"/(\%27)|(\')|(\-\-)|(\%23)|(#)/i"; classtype:web-application-attack; sid:1000002; rev:1;)

# Detect XSS attempts
alert http any any -> any any (msg:"WOLTAXI XSS Attempt"; flow:established,to_server; content:"<script"; nocase; classtype:web-application-attack; sid:1000003; rev:1;)
alert http any any -> any any (msg:"WOLTAXI XSS JavaScript"; flow:established,to_server; content:"javascript:"; nocase; classtype:web-application-attack; sid:1000004; rev:1;)

# Detect brute force attacks
alert tcp any any -> any [22,80,443,8081,8082,8083,8094] (msg:"WOLTAXI Potential Brute Force Attack"; flags:S; threshold:type both, track by_src, count 50, seconds 60; classtype:attempted-dos; sid:1000005; rev:1;)

# Detect API abuse
alert http any any -> any any (msg:"WOLTAXI API Rate Limit Exceeded"; flow:established,to_server; content:"/api/"; http_uri; threshold:type both, track by_src, count 100, seconds 60; classtype:attempted-dos; sid:1000006; rev:1;)

# Detect unauthorized access to admin endpoints
alert http any any -> any any (msg:"WOLTAXI Unauthorized Admin Access"; flow:established,to_server; content:"/admin/"; http_uri; content:!"Authorization:"; http_header; classtype:web-application-attack; sid:1000007; rev:1;)

# Detect suspicious user agents
alert http any any -> any any (msg:"WOLTAXI Suspicious User Agent"; flow:established,to_server; content:"User-Agent: "; http_header; pcre:"/User-Agent:\s*(sqlmap|nikto|nmap|masscan|zap)/i"; classtype:web-application-attack; sid:1000008; rev:1;)

# Detect potential data exfiltration
alert http any any -> any any (msg:"WOLTAXI Large Data Transfer"; flow:established,from_server; content:"Content-Length: "; http_header; pcre:"/Content-Length:\s*([1-9][0-9]{7,})/"; classtype:policy-violation; sid:1000009; rev:1;)

# Detect AI/ML model tampering
alert http any any -> any any (msg:"WOLTAXI AI Model Upload Attempt"; flow:established,to_server; content:"POST"; http_method; content:"/api/ai-ml/models"; http_uri; content:!"Authorization:"; http_header; classtype:web-application-attack; sid:1000010; rev:1;)

# Detect cryptocurrency mining
alert tcp any any -> any any (msg:"WOLTAXI Cryptocurrency Mining Detected"; content:"|01 00|"; depth:2; content:"stratum"; distance:0; classtype:policy-violation; sid:1000011; rev:1;)

# Detect suspicious file uploads
alert http any any -> any any (msg:"WOLTAXI Suspicious File Upload"; flow:established,to_server; content:"POST"; http_method; content:"multipart/form-data"; http_header; content:".php"; http_client_body; classtype:web-application-attack; sid:1000012; rev:1;)
alert http any any -> any any (msg:"WOLTAXI Executable Upload"; flow:established,to_server; content:"POST"; http_method; content:"multipart/form-data"; http_header; content:".exe"; http_client_body; classtype:web-application-attack; sid:1000013; rev:1;)
EOF
    
    # Docker service for Suricata
    cat > security/docker-compose.suricata.yml <<EOF
version: '3.8'

services:
  suricata:
    image: jasonish/suricata:7.0
    container_name: woltaxi-suricata
    restart: unless-stopped
    network_mode: host
    cap_add:
      - NET_ADMIN
      - SYS_NICE
    volumes:
      - ./suricata:/etc/suricata:ro
      - ./suricata/logs:/var/log/suricata
      - /var/log/suricata:/var/log/suricata
    command: -i eth0 -v
    environment:
      - SURICATA_OPTIONS=-i eth0 -v
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "3"
EOF
    
    print_success "Intrusion detection system configured"
}

# Setup backup system
setup_backup_system() {
    print_status "Setting up backup system..."
    
    mkdir -p backup/scripts backup/config backup/data
    
    # Backup script
    cat > backup/scripts/backup.sh <<EOF
#!/bin/bash

# WOLTAXI Backup Script
# Automated backup for all services and data

set -euo pipefail

BACKUP_DIR="/backup/data"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="woltaxi_backup_\${DATE}"
LOG_FILE="/backup/logs/backup_\${DATE}.log"

# Create backup directory
mkdir -p "\${BACKUP_DIR}/\${BACKUP_NAME}"
mkdir -p "/backup/logs"

# Logging function
log() {
    echo "\$(date '+%Y-%m-%d %H:%M:%S') - \$1" | tee -a "\${LOG_FILE}"
}

log "Starting WOLTAXI backup process"

# Backup databases
log "Backing up databases..."
docker exec woltaxi-postgres pg_dumpall -U postgres > "\${BACKUP_DIR}/\${BACKUP_NAME}/postgres_backup.sql"
docker exec woltaxi-redis redis-cli --rdb "\${BACKUP_DIR}/\${BACKUP_NAME}/redis_backup.rdb"

# Backup configuration files
log "Backing up configuration files..."
cp -r /etc/woltaxi "\${BACKUP_DIR}/\${BACKUP_NAME}/config"
cp docker-compose*.yml "\${BACKUP_DIR}/\${BACKUP_NAME}/"
cp -r ssl "\${BACKUP_DIR}/\${BACKUP_NAME}/"

# Backup logs
log "Backing up logs..."
cp -r /var/log/woltaxi "\${BACKUP_DIR}/\${BACKUP_NAME}/logs"

# Backup AI/ML models
log "Backing up AI/ML models..."
docker exec woltaxi-ai-ml-service tar -czf - /app/models > "\${BACKUP_DIR}/\${BACKUP_NAME}/ai_models.tar.gz"

# Backup Vault data
log "Backing up Vault data..."
cp -r vault/data "\${BACKUP_DIR}/\${BACKUP_NAME}/vault_data"

# Create compressed archive
log "Creating compressed archive..."
cd "\${BACKUP_DIR}"
tar -czf "\${BACKUP_NAME}.tar.gz" "\${BACKUP_NAME}"
rm -rf "\${BACKUP_NAME}"

# Upload to cloud storage (if configured)
if [ ! -z "\${CLOUD_BACKUP_ENABLED:-}" ]; then
    log "Uploading to cloud storage..."
    # Add your cloud storage upload command here
    # aws s3 cp "\${BACKUP_NAME}.tar.gz" s3://woltaxi-backups/
fi

# Cleanup old backups (keep last 30 days)
log "Cleaning up old backups..."
find "\${BACKUP_DIR}" -name "woltaxi_backup_*.tar.gz" -mtime +30 -delete

log "Backup process completed successfully"
EOF
    
    chmod +x backup/scripts/backup.sh
    
    # Backup configuration
    cat > backup/config/backup.conf <<EOF
# WOLTAXI Backup Configuration

# Backup schedule (cron format)
BACKUP_SCHEDULE="0 2 * * *"  # Daily at 2 AM

# Retention policy
BACKUP_RETENTION_DAYS=30
ARCHIVE_RETENTION_DAYS=365

# Cloud backup settings
CLOUD_BACKUP_ENABLED=false
CLOUD_PROVIDER=""  # aws, gcp, azure
CLOUD_BUCKET=""
CLOUD_REGION=""

# Encryption settings
BACKUP_ENCRYPTION=true
ENCRYPTION_KEY_FILE="/etc/woltaxi/backup-key"

# Notification settings
NOTIFICATION_EMAIL=""
NOTIFICATION_SLACK_WEBHOOK=""

# Backup verification
VERIFY_BACKUPS=true
TEST_RESTORE=false
EOF
    
    # Restore script
    cat > backup/scripts/restore.sh <<EOF
#!/bin/bash

# WOLTAXI Restore Script
# Restore from backup

set -euo pipefail

if [ \$# -eq 0 ]; then
    echo "Usage: \$0 <backup_file>"
    echo "Available backups:"
    ls -la /backup/data/woltaxi_backup_*.tar.gz
    exit 1
fi

BACKUP_FILE="\$1"
RESTORE_DIR="/tmp/woltaxi_restore_\$(date +%s)"
LOG_FILE="/backup/logs/restore_\$(date +%Y%m%d_%H%M%S).log"

log() {
    echo "\$(date '+%Y-%m-%d %H:%M:%S') - \$1" | tee -a "\${LOG_FILE}"
}

log "Starting WOLTAXI restore process from \${BACKUP_FILE}"

# Extract backup
mkdir -p "\${RESTORE_DIR}"
tar -xzf "\${BACKUP_FILE}" -C "\${RESTORE_DIR}" --strip-components=1

# Stop services
log "Stopping services..."
docker-compose down

# Restore databases
log "Restoring databases..."
docker-compose up -d postgres redis
sleep 30

docker exec -i woltaxi-postgres psql -U postgres < "\${RESTORE_DIR}/postgres_backup.sql"
docker cp "\${RESTORE_DIR}/redis_backup.rdb" woltaxi-redis:/data/dump.rdb
docker restart woltaxi-redis

# Restore configuration
log "Restoring configuration..."
cp -r "\${RESTORE_DIR}/config/"* /etc/woltaxi/
cp "\${RESTORE_DIR}/"docker-compose*.yml .
cp -r "\${RESTORE_DIR}/ssl" .

# Restore AI/ML models
log "Restoring AI/ML models..."
docker run --rm -v "\${RESTORE_DIR}/ai_models.tar.gz":/backup.tar.gz -v woltaxi_ai_models:/app/models alpine sh -c "cd /app && tar -xzf /backup.tar.gz"

# Restore Vault data
log "Restoring Vault data..."
cp -r "\${RESTORE_DIR}/vault_data/"* vault/data/

# Start services
log "Starting all services..."
docker-compose up -d

# Cleanup
rm -rf "\${RESTORE_DIR}"

log "Restore process completed successfully"
EOF
    
    chmod +x backup/scripts/restore.sh
    
    print_success "Backup system configured"
}

# Setup comprehensive security scanning
setup_security_scanning() {
    print_status "Setting up security scanning..."
    
    mkdir -p security/scanning/trivy security/scanning/clair security/scanning/anchore
    
    # Trivy configuration for container scanning
    cat > security/scanning/trivy/trivy.yaml <<EOF
# Trivy configuration for WOLTAXI container security scanning

cache:
  dir: /tmp/trivy

db:
  no-update: false
  skip-java-db-update: false

vulnerability:
  type: os,library
  scanners: vuln,secret,config

secret:
  config: /etc/trivy/secret.yaml

format: json
output: /var/log/trivy/scan-results.json

severity: HIGH,CRITICAL
ignore-unfixed: false
exit-code: 1

timeout: 5m0s
EOF
    
    # Security scanning script
    cat > security/scanning/scan.sh <<EOF
#!/bin/bash

# WOLTAXI Security Scanning Script
# Comprehensive security scanning for all components

set -euo pipefail

SCAN_DIR="/security/scanning"
RESULTS_DIR="\${SCAN_DIR}/results"
DATE=\$(date +%Y%m%d_%H%M%S)

mkdir -p "\${RESULTS_DIR}"

log() {
    echo "\$(date '+%Y-%m-%d %H:%M:%S') - \$1" | tee -a "\${RESULTS_DIR}/scan_\${DATE}.log"
}

log "Starting comprehensive security scan"

# Container vulnerability scanning with Trivy
log "Scanning containers for vulnerabilities..."
for image in \$(docker images --format "{{.Repository}}:{{.Tag}}" | grep woltaxi); do
    log "Scanning image: \${image}"
    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \\
        -v trivy-cache:/root/.cache/ \\
        aquasec/trivy:latest image --format json --output "/tmp/\${image//[:\/]/_}_\${DATE}.json" "\${image}"
done

# Network security scanning with Nmap
log "Performing network security scan..."
nmap -sS -sV -O -A --script vuln localhost > "\${RESULTS_DIR}/network_scan_\${DATE}.txt"

# Web application security scanning with OWASP ZAP
log "Performing web application security scan..."
docker run -v \$(pwd):/zap/wrk/:rw -t owasp/zap2docker-stable zap-baseline.py \\
    -t http://localhost:8080 -J "\${RESULTS_DIR}/zap_report_\${DATE}.json"

# SSL/TLS security assessment
log "Assessing SSL/TLS security..."
docker run --rm -ti drwetter/testssl.sh --jsonfile "\${RESULTS_DIR}/ssl_scan_\${DATE}.json" localhost:443

# Database security assessment
log "Performing database security assessment..."
docker exec woltaxi-postgres pg_audit --output "\${RESULTS_DIR}/db_audit_\${DATE}.txt"

# File system integrity check
log "Checking file system integrity..."
find /etc/woltaxi -type f -exec sha256sum {} \\; > "\${RESULTS_DIR}/integrity_\${DATE}.txt"

# Generate security report
log "Generating security report..."
cat > "\${RESULTS_DIR}/security_report_\${DATE}.html" <<HTML
<!DOCTYPE html>
<html>
<head>
    <title>WOLTAXI Security Scan Report - \${DATE}</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .critical { color: red; font-weight: bold; }
        .high { color: orange; font-weight: bold; }
        .medium { color: yellow; }
        .low { color: green; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>WOLTAXI Security Scan Report</h1>
    <p>Generated on: \$(date)</p>
    
    <h2>Executive Summary</h2>
    <p>This report contains the results of comprehensive security scanning performed on the WOLTAXI ecosystem.</p>
    
    <h2>Container Security</h2>
    <p>Container vulnerability scanning results are available in the JSON files.</p>
    
    <h2>Network Security</h2>
    <p>Network security scan results are available in network_scan_\${DATE}.txt</p>
    
    <h2>Web Application Security</h2>
    <p>OWASP ZAP baseline scan results are available in zap_report_\${DATE}.json</p>
    
    <h2>SSL/TLS Security</h2>
    <p>SSL/TLS assessment results are available in ssl_scan_\${DATE}.json</p>
    
    <h2>Recommendations</h2>
    <ul>
        <li>Review and remediate all CRITICAL and HIGH severity vulnerabilities</li>
        <li>Implement additional security controls as needed</li>
        <li>Schedule regular security assessments</li>
        <li>Update security documentation and procedures</li>
    </ul>
</body>
</html>
HTML

log "Security scan completed. Results available in \${RESULTS_DIR}"
EOF
    
    chmod +x security/scanning/scan.sh
    
    print_success "Security scanning configured"
}

# Main security setup function
main() {
    print_status "Starting WOLTAXI Enterprise Security Setup..."
    
    # Detect OS and check prerequisites
    detect_os
    check_prerequisites
    
    # Setup security components
    generate_ssl_certificates
    setup_vault
    setup_monitoring
    setup_firewall
    setup_intrusion_detection
    setup_backup_system
    setup_security_scanning
    
    # Create main security Docker Compose file
    cat > docker-compose.security.yml <<EOF
version: '3.8'

services:
  vault:
    extends:
      file: vault/docker-compose.vault.yml
      service: vault
    
  prometheus:
    extends:
      file: monitoring/docker-compose.monitoring.yml
      service: prometheus
      
  grafana:
    extends:
      file: monitoring/docker-compose.monitoring.yml
      service: grafana
      
  elasticsearch:
    extends:
      file: monitoring/docker-compose.monitoring.yml
      service: elasticsearch
      
  kibana:
    extends:
      file: monitoring/docker-compose.monitoring.yml
      service: kibana
      
  logstash:
    extends:
      file: monitoring/docker-compose.monitoring.yml
      service: logstash
      
  suricata:
    extends:
      file: security/docker-compose.suricata.yml
      service: suricata

networks:
  woltaxi-security:
    external: true
  woltaxi-monitoring:
    external: true

volumes:
  vault-data:
  prometheus-data:
  grafana-data:
  elasticsearch-data:
EOF
    
    # Create startup script
    cat > start-security.sh <<EOF
#!/bin/bash

echo "🔒 Starting WOLTAXI Enterprise Security Stack..."

# Create networks
docker network create woltaxi-security 2>/dev/null || true
docker network create woltaxi-monitoring 2>/dev/null || true

# Start security services
docker-compose -f docker-compose.security.yml up -d

echo "✅ WOLTAXI Enterprise Security Stack started successfully!"
echo ""
echo "🌐 Access URLs:"
echo "   Vault UI: https://localhost:8200"
echo "   Grafana: https://localhost:3000 (admin/woltaxi-secure-2024)"
echo "   Kibana: https://localhost:5601"
echo "   Prometheus: http://localhost:9090"
echo ""
echo "📊 Default Credentials:"
echo "   Grafana: admin / woltaxi-secure-2024"
echo "   Elasticsearch: elastic / woltaxi-elastic-2024"
echo ""
echo "🔧 Security Tools:"
echo "   Run security scan: ./security/scanning/scan.sh"
echo "   Create backup: ./backup/scripts/backup.sh"
echo "   Restore backup: ./backup/scripts/restore.sh <backup_file>"
EOF
    
    chmod +x start-security.sh
    
    # Setup cron jobs for automated tasks
    if command -v crontab &> /dev/null; then
        print_status "Setting up automated security tasks..."
        
        # Create cron jobs
        cat > security-crontab <<EOF
# WOLTAXI Security Automation
# Daily backup at 2 AM
0 2 * * * /bin/bash $(pwd)/backup/scripts/backup.sh >> /var/log/woltaxi/backup.log 2>&1

# Weekly security scan on Sundays at 3 AM
0 3 * * 0 /bin/bash $(pwd)/security/scanning/scan.sh >> /var/log/woltaxi/security-scan.log 2>&1

# Daily log rotation at 1 AM
0 1 * * * /usr/sbin/logrotate /etc/logrotate.d/woltaxi

# SSL certificate renewal check (monthly)
0 0 1 * * /bin/bash $(pwd)/ssl/renew-certificates.sh >> /var/log/woltaxi/ssl-renewal.log 2>&1
EOF
        
        crontab security-crontab
        rm security-crontab
        print_success "Automated security tasks configured"
    fi
    
    print_success "🎉 WOLTAXI Enterprise Security Setup Completed Successfully!"
    echo ""
    echo -e "${CYAN}==============================================================================${NC}"
    echo -e "${GREEN}✅ Security Components Installed:${NC}"
    echo -e "${BLUE}   🔐 SSL/TLS Certificates with CA${NC}"
    echo -e "${BLUE}   🗄️  HashiCorp Vault for Secrets Management${NC}"
    echo -e "${BLUE}   📊 ELK Stack for Logging and Monitoring${NC}"
    echo -e "${BLUE}   📈 Prometheus + Grafana for Metrics${NC}"
    echo -e "${BLUE}   🛡️  Suricata IDS for Intrusion Detection${NC}"
    echo -e "${BLUE}   🔥 Firewall Configuration${NC}"
    echo -e "${BLUE}   💾 Automated Backup System${NC}"
    echo -e "${BLUE}   🔍 Comprehensive Security Scanning${NC}"
    echo -e "${BLUE}   ⚡ Automated Security Tasks${NC}"
    echo ""
    echo -e "${YELLOW}📋 Next Steps:${NC}"
    echo -e "${BLUE}   1. Run: ./start-security.sh${NC}"
    echo -e "${BLUE}   2. Initialize Vault: vault operator init${NC}"
    echo -e "${BLUE}   3. Configure monitoring dashboards${NC}"
    echo -e "${BLUE}   4. Test backup and restore procedures${NC}"
    echo -e "${BLUE}   5. Run initial security scan${NC}"
    echo -e "${CYAN}==============================================================================${NC}"
}

# Run main function
main "$@"