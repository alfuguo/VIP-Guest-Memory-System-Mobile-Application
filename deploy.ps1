# VIP Guest Memory System Deployment Script (PowerShell)
# This script handles the deployment of the application to production on Windows

param(
    [Parameter(Position=0)]
    [string]$Command = "help",
    
    [Parameter(Position=1)]
    [string]$Option = "",
    
    [switch]$SkipBackup,
    [switch]$Volumes
)

# Configuration
$ComposeFile = "docker-compose.yml"
$ProdComposeFile = "docker-compose.prod.yml"
$EnvFile = ".env"
$BackupDir = "./backups"
$LogFile = "./deploy.log"

# Function to write log messages
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    
    switch ($Level) {
        "ERROR" { Write-Host $logMessage -ForegroundColor Red }
        "SUCCESS" { Write-Host $logMessage -ForegroundColor Green }
        "WARNING" { Write-Host $logMessage -ForegroundColor Yellow }
        default { Write-Host $logMessage -ForegroundColor Blue }
    }
    
    Add-Content -Path $LogFile -Value $logMessage
}

function Write-Error-Log {
    param([string]$Message)
    Write-Log $Message "ERROR"
    exit 1
}

function Write-Success-Log {
    param([string]$Message)
    Write-Log $Message "SUCCESS"
}

function Write-Warning-Log {
    param([string]$Message)
    Write-Log $Message "WARNING"
}

# Check if required files exist
function Test-Requirements {
    Write-Log "Checking deployment requirements..."
    
    if (-not (Test-Path $EnvFile)) {
        Write-Error-Log "Environment file $EnvFile not found. Please copy .env.example to .env and configure it."
    }
    
    if (-not (Test-Path $ComposeFile)) {
        Write-Error-Log "Docker compose file $ComposeFile not found."
    }
    
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Error-Log "Docker is not installed or not in PATH."
    }
    
    if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
        Write-Error-Log "Docker Compose is not installed or not in PATH."
    }
    
    Write-Success-Log "All requirements met."
}

# Validate environment variables
function Test-Environment {
    Write-Log "Validating environment variables..."
    
    if (-not (Test-Path $EnvFile)) {
        Write-Error-Log "Environment file $EnvFile not found."
    }
    
    # Read environment file
    $envVars = @{}
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $envVars[$matches[1].Trim()] = $matches[2].Trim()
        }
    }
    
    $requiredVars = @("DB_PASSWORD", "JWT_SECRET")
    
    foreach ($var in $requiredVars) {
        if (-not $envVars.ContainsKey($var) -or [string]::IsNullOrEmpty($envVars[$var])) {
            Write-Error-Log "Required environment variable $var is not set in $EnvFile"
        }
    }
    
    # Check JWT secret length
    if ($envVars["JWT_SECRET"].Length -lt 32) {
        Write-Error-Log "JWT_SECRET must be at least 32 characters long for security."
    }
    
    Write-Success-Log "Environment variables validated."
}

# Create backup of database
function Backup-Database {
    if ($SkipBackup) {
        Write-Warning-Log "Skipping database backup as requested."
        return
    }
    
    Write-Log "Creating database backup..."
    
    if (-not (Test-Path $BackupDir)) {
        New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
        Write-Log "Created backup directory: $BackupDir"
    }
    
    # Check if database container is running
    $postgresStatus = docker-compose ps postgres
    if ($postgresStatus -match "Up") {
        $backupFile = "$BackupDir/backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"
        
        # Read DB credentials from env file
        $envVars = @{}
        Get-Content $EnvFile | ForEach-Object {
            if ($_ -match '^([^#][^=]+)=(.*)$') {
                $envVars[$matches[1]] = $matches[2]
            }
        }
        
        $dbUser = $envVars["DB_USER"]
        $dbName = $envVars["DB_NAME"]
        
        docker-compose exec -T postgres pg_dump -U $dbUser $dbName | Out-File -FilePath $backupFile -Encoding UTF8
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success-Log "Database backup created: $backupFile"
        } else {
            Write-Error-Log "Failed to create database backup."
        }
    } else {
        Write-Warning-Log "Database container not running, skipping backup."
    }
}

# Build and deploy application
function Start-Deployment {
    Write-Log "Starting deployment..."
    
    # Pull latest images
    Write-Log "Pulling latest base images..."
    docker-compose pull
    
    # Build application
    Write-Log "Building application..."
    docker-compose build --no-cache backend
    
    # Stop existing containers
    Write-Log "Stopping existing containers..."
    docker-compose down
    
    # Start services
    Write-Log "Starting services..."
    if (Test-Path $ProdComposeFile) {
        docker-compose -f $ComposeFile -f $ProdComposeFile up -d
    } else {
        docker-compose up -d
    }
    
    # Wait for services to be healthy
    Write-Log "Waiting for services to be healthy..."
    Start-Sleep -Seconds 30
    
    # Check service health
    Test-ServiceHealth
    
    Write-Success-Log "Deployment completed successfully!"
}

# Check service health
function Test-ServiceHealth {
    Write-Log "Checking service health..."
    
    # Read DB credentials from env file
    $envVars = @{}
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $envVars[$matches[1]] = $matches[2]
        }
    }
    
    $dbUser = $envVars["DB_USER"]
    $dbName = $envVars["DB_NAME"]
    
    # Check database
    $dbHealthCheck = docker-compose exec -T postgres pg_isready -U $dbUser -d $dbName 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Success-Log "Database is healthy"
    } else {
        Write-Error-Log "Database health check failed"
    }
    
    # Check backend
    $maxAttempts = 30
    $attempt = 1
    
    while ($attempt -le $maxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Success-Log "Backend is healthy"
                break
            }
        } catch {
            Write-Log "Backend health check attempt $attempt/$maxAttempts failed, retrying in 10 seconds..."
            Start-Sleep -Seconds 10
            $attempt++
        }
    }
    
    if ($attempt -gt $maxAttempts) {
        Write-Error-Log "Backend health check failed after $maxAttempts attempts"
    }
}

# Rollback to previous version
function Start-Rollback {
    Write-Log "Rolling back to previous version..."
    
    # Find latest backup
    $latestBackup = Get-ChildItem -Path "$BackupDir/backup_*.sql" -ErrorAction SilentlyContinue | 
                   Sort-Object LastWriteTime -Descending | 
                   Select-Object -First 1
    
    if (-not $latestBackup) {
        Write-Error-Log "No backup found for rollback."
    }
    
    Write-Log "Restoring database from backup: $($latestBackup.Name)"
    
    # Stop services
    docker-compose down
    
    # Start only database
    docker-compose up -d postgres
    Start-Sleep -Seconds 10
    
    # Read DB credentials from env file
    $envVars = @{}
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $envVars[$matches[1]] = $matches[2]
        }
    }
    
    $dbUser = $envVars["DB_USER"]
    $dbName = $envVars["DB_NAME"]
    
    # Restore database
    Get-Content $latestBackup.FullName | docker-compose exec -T postgres psql -U $dbUser -d $dbName
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success-Log "Database restored from backup."
    } else {
        Write-Error-Log "Failed to restore database from backup."
    }
    
    # Restart all services
    docker-compose up -d
    
    Write-Success-Log "Rollback completed."
}

# Show logs
function Show-Logs {
    param([string]$Service = "")
    
    if ($Service) {
        docker-compose logs -f --tail=100 $Service
    } else {
        docker-compose logs -f --tail=100
    }
}

# Clean up old images and containers
function Start-Cleanup {
    Write-Log "Cleaning up old Docker images and containers..."
    
    # Remove unused containers
    docker container prune -f
    
    # Remove unused images
    docker image prune -f
    
    # Remove unused volumes (be careful with this)
    if ($Volumes) {
        Write-Warning-Log "Removing unused volumes..."
        docker volume prune -f
    }
    
    Write-Success-Log "Cleanup completed."
}

# Show usage information
function Show-Usage {
    Write-Host "Usage: .\deploy.ps1 [COMMAND] [OPTIONS]" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor Yellow
    Write-Host "  deploy              Deploy the application"
    Write-Host "  rollback            Rollback to previous version"
    Write-Host "  backup              Create database backup"
    Write-Host "  health              Check service health"
    Write-Host "  logs [service]      Show logs for service (default: all)"
    Write-Host "  cleanup             Clean up Docker resources"
    Write-Host "  stop                Stop all services"
    Write-Host "  start               Start all services"
    Write-Host "  restart             Restart all services"
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  -SkipBackup         Skip database backup during deployment"
    Write-Host "  -Volumes            Remove unused volumes during cleanup"
    Write-Host "  -Help               Show this help message"
}

# Main script logic
switch ($Command.ToLower()) {
    "deploy" {
        Test-Requirements
        Test-Environment
        Backup-Database
        Start-Deployment
    }
    "rollback" {
        Start-Rollback
    }
    "backup" {
        Backup-Database
    }
    "health" {
        Test-ServiceHealth
    }
    "logs" {
        Show-Logs $Option
    }
    "cleanup" {
        Start-Cleanup
    }
    "stop" {
        docker-compose down
    }
    "start" {
        docker-compose up -d
    }
    "restart" {
        docker-compose restart
    }
    "help" {
        Show-Usage
    }
    default {
        Write-Error-Log "Unknown command: $Command"
        Show-Usage
    }
}