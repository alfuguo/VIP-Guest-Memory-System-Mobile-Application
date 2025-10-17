#!/bin/bash

# VIP Guest Memory System Deployment Script
# This script handles the deployment of the application to production

set -e  # Exit on any error

# Configuration
COMPOSE_FILE="docker-compose.yml"
PROD_COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env"
BACKUP_DIR="./backups"
LOG_FILE="./deploy.log"

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
    exit 1
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$LOG_FILE"
}

# Check if required files exist
check_requirements() {
    log "Checking deployment requirements..."
    
    if [ ! -f "$ENV_FILE" ]; then
        error "Environment file $ENV_FILE not found. Please copy .env.example to .env and configure it."
    fi
    
    if [ ! -f "$COMPOSE_FILE" ]; then
        error "Docker compose file $COMPOSE_FILE not found."
    fi
    
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed or not in PATH."
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose is not installed or not in PATH."
    fi
    
    success "All requirements met."
}

# Validate environment variables
validate_env() {
    log "Validating environment variables..."
    
    if [ -f "$ENV_FILE" ]; then
        source "$ENV_FILE"
    else
        error "Environment file $ENV_FILE not found"
    fi
    
    required_vars=("DB_PASSWORD" "JWT_SECRET")
    
    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            error "Required environment variable $var is not set in $ENV_FILE"
        fi
    done
    
    # Check JWT secret length (should be at least 32 characters)
    if [ ${#JWT_SECRET} -lt 32 ]; then
        error "JWT_SECRET must be at least 32 characters long for security."
    fi
    
    success "Environment variables validated."
}

# Create backup of database
backup_database() {
    if [ "$1" = "--skip-backup" ]; then
        warning "Skipping database backup as requested."
        return
    fi
    
    log "Creating database backup..."
    
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
        log "Created backup directory: $BACKUP_DIR"
    fi
    
    # Check if database container is running
    if docker-compose ps postgres | grep -q "Up"; then
        BACKUP_FILE="$BACKUP_DIR/backup_$(date +%Y%m%d_%H%M%S).sql"
        
        docker-compose exec -T postgres pg_dump -U "$DB_USER" "$DB_NAME" > "$BACKUP_FILE"
        
        if [ $? -eq 0 ]; then
            success "Database backup created: $BACKUP_FILE"
        else
            error "Failed to create database backup."
        fi
    else
        warning "Database container not running, skipping backup."
    fi
}

# Build and deploy application
deploy() {
    log "Starting deployment..."
    
    # Pull latest images
    log "Pulling latest base images..."
    docker-compose pull
    
    # Build application
    log "Building application..."
    docker-compose build --no-cache backend
    
    # Stop existing containers
    log "Stopping existing containers..."
    docker-compose down
    
    # Start services
    log "Starting services..."
    if [ -f "$PROD_COMPOSE_FILE" ]; then
        docker-compose -f "$COMPOSE_FILE" -f "$PROD_COMPOSE_FILE" up -d
    else
        docker-compose up -d
    fi
    
    # Wait for services to be healthy
    log "Waiting for services to be healthy..."
    sleep 30
    
    # Check service health
    check_health
    
    success "Deployment completed successfully!"
}

# Check service health
check_health() {
    log "Checking service health..."
    
    # Check database
    if docker-compose exec -T postgres pg_isready -U "$DB_USER" -d "$DB_NAME" > /dev/null 2>&1; then
        success "Database is healthy"
    else
        error "Database health check failed"
    fi
    
    # Check backend
    max_attempts=30
    attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
            success "Backend is healthy"
            break
        else
            log "Backend health check attempt $attempt/$max_attempts failed, retrying in 10 seconds..."
            sleep 10
            ((attempt++))
        fi
    done
    
    if [ $attempt -gt $max_attempts ]; then
        error "Backend health check failed after $max_attempts attempts"
    fi
}

# Rollback to previous version
rollback() {
    log "Rolling back to previous version..."
    
    # Find latest backup
    LATEST_BACKUP=$(ls -t "$BACKUP_DIR"/backup_*.sql 2>/dev/null | head -n1)
    
    if [ -z "$LATEST_BACKUP" ]; then
        error "No backup found for rollback."
    fi
    
    log "Restoring database from backup: $LATEST_BACKUP"
    
    # Stop services
    docker-compose down
    
    # Start only database
    docker-compose up -d postgres
    sleep 10
    
    # Restore database
    docker-compose exec -T postgres psql -U "$DB_USER" -d "$DB_NAME" < "$LATEST_BACKUP"
    
    if [ $? -eq 0 ]; then
        success "Database restored from backup."
    else
        error "Failed to restore database from backup."
    fi
    
    # Restart all services
    docker-compose up -d
    
    success "Rollback completed."
}

# Show logs
show_logs() {
    docker-compose logs -f --tail=100 "$1"
}

# Clean up old images and containers
cleanup() {
    log "Cleaning up old Docker images and containers..."
    
    # Remove unused containers
    docker container prune -f
    
    # Remove unused images
    docker image prune -f
    
    # Remove unused volumes (be careful with this)
    if [ "$1" = "--volumes" ]; then
        warning "Removing unused volumes..."
        docker volume prune -f
    fi
    
    success "Cleanup completed."
}

# Show usage information
usage() {
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  deploy              Deploy the application"
    echo "  rollback            Rollback to previous version"
    echo "  backup              Create database backup"
    echo "  health              Check service health"
    echo "  logs [service]      Show logs for service (default: all)"
    echo "  cleanup [--volumes] Clean up Docker resources"
    echo "  stop                Stop all services"
    echo "  start               Start all services"
    echo "  restart             Restart all services"
    echo ""
    echo "Options:"
    echo "  --skip-backup       Skip database backup during deployment"
    echo "  --help              Show this help message"
}

# Main script logic
main() {
    case "$1" in
        "deploy")
            check_requirements
            validate_env
            backup_database "$2"
            deploy
            ;;
        "rollback")
            rollback
            ;;
        "backup")
            backup_database
            ;;
        "health")
            check_health
            ;;
        "logs")
            show_logs "$2"
            ;;
        "cleanup")
            cleanup "$2"
            ;;
        "stop")
            docker-compose down
            ;;
        "start")
            docker-compose up -d
            ;;
        "restart")
            docker-compose restart
            ;;
        "--help"|"help"|"")
            usage
            ;;
        *)
            error "Unknown command: $1"
            usage
            ;;
    esac
}

# Run main function with all arguments
main "$@"