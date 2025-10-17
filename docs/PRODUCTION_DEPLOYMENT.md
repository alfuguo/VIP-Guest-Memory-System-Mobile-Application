# Production Deployment Guide

This guide covers the complete process of deploying the VIP Guest Memory System to a production environment.

## Pre-Deployment Checklist

### Infrastructure Requirements
- [ ] Server with minimum 4GB RAM, 2 CPU cores, 50GB storage
- [ ] Docker and Docker Compose installed
- [ ] SSL certificate obtained (Let's Encrypt recommended)
- [ ] Domain name configured and DNS pointing to server
- [ ] Firewall configured (ports 80, 443, 22 open)
- [ ] Backup storage solution configured

### Security Requirements
- [ ] Strong passwords generated for all accounts
- [ ] JWT secret key generated (minimum 64 characters)
- [ ] Database credentials secured
- [ ] SSL certificates installed
- [ ] Firewall rules configured
- [ ] SSH key-based authentication enabled
- [ ] Regular security updates scheduled

### Environment Configuration
- [ ] `.env` file created from `.env.example`
- [ ] All required environment variables set
- [ ] Database connection tested
- [ ] File upload directory permissions set
- [ ] Log directory created with proper permissions

## Step-by-Step Deployment

### 1. Server Preparation

#### Update System
```bash
# Ubuntu/Debian
sudo apt update && sudo apt upgrade -y

# CentOS/RHEL
sudo yum update -y
```

#### Install Docker
```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Add user to docker group
sudo usermod -aG docker $USER
```

#### Configure Firewall
```bash
# UFW (Ubuntu)
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# Firewalld (CentOS/RHEL)
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 2. Application Deployment

#### Clone Repository
```bash
git clone <repository-url>
cd vip-guest-memory-system
```

#### Configure Environment
```bash
# Copy environment template
cp .env.example .env

# Edit environment file
nano .env
```

**Required Environment Variables:**
```env
# Database Configuration
DB_NAME=vip_guest_system_prod
DB_USER=vip_user_prod
DB_PASSWORD=<generate-strong-password>

# JWT Configuration
JWT_SECRET=<generate-64-character-secret>
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# File Upload
MAX_FILE_SIZE=5MB
UPLOAD_DIR=/app/uploads
```

#### Generate Secure Secrets
```bash
# Generate JWT secret (64 characters)
openssl rand -base64 64

# Generate database password
openssl rand -base64 32
```

### 3. SSL Certificate Setup

#### Using Let's Encrypt (Recommended)
```bash
# Install Certbot
sudo apt install certbot

# Obtain certificate
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# Copy certificates to project directory
sudo mkdir -p ./ssl
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem ./ssl/cert.pem
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem ./ssl/private.key
sudo chown -R $USER:$USER ./ssl
```

#### Update Nginx Configuration
Edit `nginx.prod.conf` and replace `your-domain.com` with your actual domain.

### 4. Database Initialization

#### Start Database Only
```bash
docker-compose up -d postgres
```

#### Wait for Database to Start
```bash
sleep 30
```

#### Verify Database Connection
```bash
docker-compose exec postgres psql -U vip_user_prod -d vip_guest_system_prod -c "SELECT version();"
```

### 5. Application Deployment

#### Deploy Using Script
```bash
# Make deployment script executable
chmod +x deploy.sh

# Deploy application
./deploy.sh deploy
```

#### Manual Deployment (Alternative)
```bash
# Build and start all services
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build

# Check service status
docker-compose ps

# View logs
docker-compose logs -f
```

### 6. Post-Deployment Verification

#### Health Checks
```bash
# Check application health
curl -f https://yourdomain.com/health

# Check database connectivity
docker-compose exec postgres pg_isready -U vip_user_prod -d vip_guest_system_prod

# Check Redis connectivity
docker-compose exec redis redis-cli ping
```

#### Create Initial Admin User
```bash
# Connect to database
docker-compose exec postgres psql -U vip_user_prod -d vip_guest_system_prod

# Create admin user (replace with secure password hash)
INSERT INTO staff (email, password_hash, first_name, last_name, role, active) 
VALUES (
    'admin@yourdomain.com',
    '$2a$12$your_bcrypt_hashed_password_here',
    'System',
    'Administrator',
    'MANAGER',
    true
);
```

#### Test API Endpoints
```bash
# Test login endpoint
curl -X POST https://yourdomain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@yourdomain.com","password":"your_password"}'

# Test protected endpoint (use token from login response)
curl -H "Authorization: Bearer <your-jwt-token>" \
  https://yourdomain.com/api/guests
```

## Monitoring and Maintenance

### Log Management

#### View Application Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Last 100 lines
docker-compose logs --tail=100 backend
```

#### Log Rotation Setup
```bash
# Create logrotate configuration
sudo nano /etc/logrotate.d/vip-guest-system

# Add configuration
/var/lib/docker/containers/*/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 0644 root root
}
```

### Backup Strategy

#### Automated Database Backups
```bash
# Create backup script
cat > backup-db.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/backups/vip-guest-system"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

docker-compose exec -T postgres pg_dump -U vip_user_prod vip_guest_system_prod > $BACKUP_DIR/backup_$DATE.sql

# Keep only last 30 days of backups
find $BACKUP_DIR -name "backup_*.sql" -mtime +30 -delete
EOF

chmod +x backup-db.sh
```

#### Schedule Backups with Cron
```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * /path/to/vip-guest-memory-system/backup-db.sh
```

### SSL Certificate Renewal

#### Automatic Renewal with Certbot
```bash
# Test renewal
sudo certbot renew --dry-run

# Add to crontab for automatic renewal
0 12 * * * /usr/bin/certbot renew --quiet --deploy-hook "cd /path/to/vip-guest-memory-system && docker-compose restart nginx"
```

### Performance Monitoring

#### System Monitoring
```bash
# Monitor resource usage
docker stats

# Monitor disk usage
df -h

# Monitor memory usage
free -h
```

#### Application Monitoring
```bash
# Check application metrics
curl https://yourdomain.com/actuator/metrics

# Check health endpoint
curl https://yourdomain.com/actuator/health
```

## Scaling and High Availability

### Load Balancing Setup

#### Multiple Backend Instances
```yaml
# In docker-compose.prod.yml
services:
  backend-1:
    <<: *backend-service
    container_name: vip-backend-1
  
  backend-2:
    <<: *backend-service
    container_name: vip-backend-2
```

#### Nginx Load Balancer Configuration
```nginx
upstream backend {
    server backend-1:8080;
    server backend-2:8080;
    keepalive 32;
}
```

### Database High Availability

#### PostgreSQL Replication
```yaml
# Master-slave setup
postgres-master:
  image: postgres:15-alpine
  environment:
    POSTGRES_REPLICATION_MODE: master
    POSTGRES_REPLICATION_USER: replicator
    POSTGRES_REPLICATION_PASSWORD: replication_password

postgres-slave:
  image: postgres:15-alpine
  environment:
    POSTGRES_REPLICATION_MODE: slave
    POSTGRES_MASTER_HOST: postgres-master
```

## Troubleshooting

### Common Issues

#### Application Won't Start
```bash
# Check logs
docker-compose logs backend

# Check environment variables
docker-compose config

# Verify database connection
docker-compose exec backend nc -z postgres 5432
```

#### Database Connection Issues
```bash
# Check database status
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Test connection manually
docker-compose exec postgres psql -U vip_user_prod -d vip_guest_system_prod
```

#### SSL Certificate Issues
```bash
# Check certificate validity
openssl x509 -in ./ssl/cert.pem -text -noout

# Test SSL connection
openssl s_client -connect yourdomain.com:443
```

#### Performance Issues
```bash
# Check resource usage
docker stats

# Check database performance
docker-compose exec postgres psql -U vip_user_prod -d vip_guest_system_prod -c "SELECT * FROM pg_stat_activity;"

# Check slow queries
docker-compose exec postgres psql -U vip_user_prod -d vip_guest_system_prod -c "SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"
```

### Recovery Procedures

#### Database Recovery
```bash
# Stop application
docker-compose stop backend

# Restore from backup
docker-compose exec -T postgres psql -U vip_user_prod -d vip_guest_system_prod < /path/to/backup.sql

# Start application
docker-compose start backend
```

#### Complete System Recovery
```bash
# Stop all services
docker-compose down

# Remove volumes (if corrupted)
docker-compose down -v

# Restore from backup
# ... restore database and files ...

# Restart services
docker-compose up -d
```

## Security Hardening

### Additional Security Measures

#### Fail2Ban Setup
```bash
# Install Fail2Ban
sudo apt install fail2ban

# Configure for Nginx
sudo nano /etc/fail2ban/jail.local

[nginx-http-auth]
enabled = true
filter = nginx-http-auth
logpath = /var/log/nginx/error.log
maxretry = 3
bantime = 3600
```

#### Regular Security Updates
```bash
# Create update script
cat > update-system.sh << 'EOF'
#!/bin/bash
sudo apt update && sudo apt upgrade -y
docker-compose pull
docker-compose up -d --force-recreate
EOF

# Schedule weekly updates
0 3 * * 0 /path/to/update-system.sh
```

#### Network Security
```bash
# Restrict Docker daemon access
sudo nano /etc/docker/daemon.json
{
  "hosts": ["unix:///var/run/docker.sock"],
  "tls": true,
  "tlscert": "/path/to/cert.pem",
  "tlskey": "/path/to/key.pem"
}
```

## Maintenance Schedule

### Daily Tasks
- [ ] Check application health endpoints
- [ ] Review error logs
- [ ] Monitor resource usage
- [ ] Verify backup completion

### Weekly Tasks
- [ ] Review security logs
- [ ] Update system packages
- [ ] Check SSL certificate expiration
- [ ] Performance analysis

### Monthly Tasks
- [ ] Full system backup
- [ ] Security audit
- [ ] Capacity planning review
- [ ] Update documentation

### Quarterly Tasks
- [ ] Disaster recovery testing
- [ ] Security penetration testing
- [ ] Performance optimization
- [ ] Infrastructure review