#!/bin/bash

# Health Check Script for VIP Guest Memory System
# This script performs comprehensive health checks on all system components

set -e

# Configuration
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
HEALTH_ENDPOINT="$API_BASE_URL/actuator/health"
TIMEOUT=10
LOG_FILE="./health-check.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$LOG_FILE"
}

# Check if Docker containers are running
check_containers() {
    log "Checking Docker containers..."
    
    local containers=("vip-postgres" "vip-redis" "vip-backend" "vip-nginx")
    local all_healthy=true
    
    for container in "${containers[@]}"; do
        if docker ps --format "table {{.Names}}" | grep -q "$container"; then
            local status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "no-healthcheck")
            
            if [ "$status" = "healthy" ] || [ "$status" = "no-healthcheck" ]; then
                success "Container $container is running"
            else
                error "Container $container is unhealthy (status: $status)"
                all_healthy=false
            fi
        else
            error "Container $container is not running"
            all_healthy=false
        fi
    done
    
    if [ "$all_healthy" = true ]; then
        success "All containers are healthy"
    else
        error "Some containers are not healthy"
        return 1
    fi
}

# Check database connectivity
check_database() {
    log "Checking database connectivity..."
    
    # Load environment variables if .env file exists
    if [ -f ".env" ]; then
        source .env
    fi
    
    local db_user="${DB_USER:-vip_user}"
    local db_name="${DB_NAME:-vip_guest_system}"
    
    if docker-compose exec -T postgres pg_isready -U "$db_user" -d "$db_name" >/dev/null 2>&1; then
        success "Database is accessible"
        
        # Check database tables
        local table_count=$(docker-compose exec -T postgres psql -U "$db_user" -d "$db_name" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | tr -d ' ')
        
        if [ "$table_count" -gt 0 ]; then
            success "Database has $table_count tables"
        else
            warning "Database appears to be empty"
        fi
    else
        error "Database is not accessible"
        return 1
    fi
}

# Check Redis connectivity
check_redis() {
    log "Checking Redis connectivity..."
    
    if docker-compose exec -T redis redis-cli ping >/dev/null 2>&1; then
        success "Redis is accessible"
        
        # Check Redis memory usage
        local memory_usage=$(docker-compose exec -T redis redis-cli info memory | grep "used_memory_human" | cut -d: -f2 | tr -d '\r')
        success "Redis memory usage: $memory_usage"
    else
        error "Redis is not accessible"
        return 1
    fi
}

# Check application health endpoint
check_application() {
    log "Checking application health endpoint..."
    
    local response=$(curl -s -w "%{http_code}" -o /tmp/health_response.json --connect-timeout $TIMEOUT "$HEALTH_ENDPOINT" 2>/dev/null || echo "000")
    
    if [ "$response" = "200" ]; then
        success "Application health endpoint is responding"
        
        # Parse health response
        if command -v jq >/dev/null 2>&1; then
            local status=$(jq -r '.status' /tmp/health_response.json 2>/dev/null || echo "unknown")
            if [ "$status" = "UP" ]; then
                success "Application status: UP"
            else
                warning "Application status: $status"
            fi
            
            # Check individual components
            local db_status=$(jq -r '.components.db.status' /tmp/health_response.json 2>/dev/null || echo "unknown")
            local redis_status=$(jq -r '.components.redis.status' /tmp/health_response.json 2>/dev/null || echo "unknown")
            
            log "Database component status: $db_status"
            log "Redis component status: $redis_status"
        fi
    else
        error "Application health endpoint returned status: $response"
        return 1
    fi
    
    # Clean up temp file
    rm -f /tmp/health_response.json
}

# Check API endpoints
check_api_endpoints() {
    log "Checking critical API endpoints..."
    
    local endpoints=(
        "/api/auth/login"
        "/api/guests"
        "/api/notifications/upcoming"
    )
    
    for endpoint in "${endpoints[@]}"; do
        local url="$API_BASE_URL$endpoint"
        local response=$(curl -s -w "%{http_code}" -o /dev/null --connect-timeout $TIMEOUT "$url" 2>/dev/null || echo "000")
        
        # We expect 401 for protected endpoints without auth, which is normal
        if [ "$response" = "200" ] || [ "$response" = "401" ] || [ "$response" = "405" ]; then
            success "Endpoint $endpoint is accessible (status: $response)"
        else
            error "Endpoint $endpoint returned status: $response"
        fi
    done
}

# Check disk space
check_disk_space() {
    log "Checking disk space..."
    
    local disk_usage=$(df -h / | awk 'NR==2 {print $5}' | sed 's/%//')
    
    if [ "$disk_usage" -lt 80 ]; then
        success "Disk usage is $disk_usage% (healthy)"
    elif [ "$disk_usage" -lt 90 ]; then
        warning "Disk usage is $disk_usage% (warning)"
    else
        error "Disk usage is $disk_usage% (critical)"
    fi
}

# Check memory usage
check_memory() {
    log "Checking memory usage..."
    
    local memory_info=$(free | grep Mem)
    local total=$(echo $memory_info | awk '{print $2}')
    local used=$(echo $memory_info | awk '{print $3}')
    local usage_percent=$((used * 100 / total))
    
    if [ "$usage_percent" -lt 80 ]; then
        success "Memory usage is $usage_percent% (healthy)"
    elif [ "$usage_percent" -lt 90 ]; then
        warning "Memory usage is $usage_percent% (warning)"
    else
        error "Memory usage is $usage_percent% (critical)"
    fi
}

# Check SSL certificate (if HTTPS is configured)
check_ssl_certificate() {
    if [ -n "$SSL_DOMAIN" ]; then
        log "Checking SSL certificate for $SSL_DOMAIN..."
        
        local expiry_date=$(echo | openssl s_client -servername "$SSL_DOMAIN" -connect "$SSL_DOMAIN:443" 2>/dev/null | openssl x509 -noout -dates | grep notAfter | cut -d= -f2)
        local expiry_epoch=$(date -d "$expiry_date" +%s 2>/dev/null || echo "0")
        local current_epoch=$(date +%s)
        local days_until_expiry=$(( (expiry_epoch - current_epoch) / 86400 ))
        
        if [ "$days_until_expiry" -gt 30 ]; then
            success "SSL certificate expires in $days_until_expiry days"
        elif [ "$days_until_expiry" -gt 7 ]; then
            warning "SSL certificate expires in $days_until_expiry days"
        else
            error "SSL certificate expires in $days_until_expiry days (critical)"
        fi
    fi
}

# Check log files for errors
check_logs() {
    log "Checking recent logs for errors..."
    
    local error_count=$(docker-compose logs --since="1h" 2>/dev/null | grep -i "error\|exception\|failed" | wc -l)
    
    if [ "$error_count" -eq 0 ]; then
        success "No errors found in recent logs"
    elif [ "$error_count" -lt 5 ]; then
        warning "Found $error_count errors in recent logs"
    else
        error "Found $error_count errors in recent logs (investigate)"
    fi
}

# Generate health report
generate_report() {
    log "Generating health report..."
    
    local report_file="health-report-$(date +%Y%m%d_%H%M%S).json"
    
    cat > "$report_file" << EOF
{
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "system": {
    "hostname": "$(hostname)",
    "uptime": "$(uptime -p)",
    "load_average": "$(uptime | awk -F'load average:' '{print $2}')"
  },
  "containers": {
    "postgres": "$(docker inspect --format='{{.State.Status}}' vip-postgres 2>/dev/null || echo 'not-found')",
    "redis": "$(docker inspect --format='{{.State.Status}}' vip-redis 2>/dev/null || echo 'not-found')",
    "backend": "$(docker inspect --format='{{.State.Status}}' vip-backend 2>/dev/null || echo 'not-found')",
    "nginx": "$(docker inspect --format='{{.State.Status}}' vip-nginx 2>/dev/null || echo 'not-found')"
  },
  "resources": {
    "disk_usage": "$(df -h / | awk 'NR==2 {print $5}')",
    "memory_usage": "$(free | grep Mem | awk '{printf "%.1f%%", $3/$2 * 100.0}')",
    "cpu_load": "$(uptime | awk -F'load average:' '{print $2}' | awk '{print $1}' | sed 's/,//')"
  }
}
EOF
    
    success "Health report generated: $report_file"
}

# Main health check function
main() {
    log "Starting comprehensive health check..."
    
    local exit_code=0
    
    # Run all health checks
    check_containers || exit_code=1
    check_database || exit_code=1
    check_redis || exit_code=1
    check_application || exit_code=1
    check_api_endpoints || exit_code=1
    check_disk_space || exit_code=1
    check_memory || exit_code=1
    check_ssl_certificate || exit_code=1
    check_logs || exit_code=1
    
    # Generate report
    generate_report
    
    if [ $exit_code -eq 0 ]; then
        success "All health checks passed!"
    else
        error "Some health checks failed. Please review the issues above."
    fi
    
    log "Health check completed."
    exit $exit_code
}

# Handle command line arguments
case "${1:-}" in
    --containers)
        check_containers
        ;;
    --database)
        check_database
        ;;
    --redis)
        check_redis
        ;;
    --application)
        check_application
        ;;
    --api)
        check_api_endpoints
        ;;
    --resources)
        check_disk_space
        check_memory
        ;;
    --ssl)
        check_ssl_certificate
        ;;
    --logs)
        check_logs
        ;;
    --report)
        generate_report
        ;;
    --help)
        echo "Usage: $0 [option]"
        echo "Options:"
        echo "  --containers    Check Docker containers"
        echo "  --database      Check database connectivity"
        echo "  --redis         Check Redis connectivity"
        echo "  --application   Check application health"
        echo "  --api           Check API endpoints"
        echo "  --resources     Check system resources"
        echo "  --ssl           Check SSL certificate"
        echo "  --logs          Check recent logs"
        echo "  --report        Generate health report"
        echo "  --help          Show this help"
        echo ""
        echo "Run without arguments to perform all checks."
        ;;
    *)
        main
        ;;
esac