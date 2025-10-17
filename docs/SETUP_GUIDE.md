# Development Setup Guide

This guide will help you set up the VIP Guest Memory System for local development.

## Prerequisites Checklist

Before starting, ensure you have the following installed:

### Backend Requirements
- [ ] Java 17 or higher (`java -version`)
- [ ] Maven 3.6+ (`mvn -version`)
- [ ] PostgreSQL 15+ (`psql --version`)
- [ ] Git (`git --version`)

### Frontend Requirements
- [ ] Node.js 18+ (`node --version`)
- [ ] npm 8+ (`npm --version`)
- [ ] Expo CLI (`expo --version`)

### Optional Tools
- [ ] Docker & Docker Compose (for containerized setup)
- [ ] IntelliJ IDEA or VS Code (recommended IDEs)
- [ ] Postman or similar API testing tool

## Step-by-Step Setup

### 1. Clone and Navigate to Project

```bash
git clone <repository-url>
cd vip-guest-memory-system
```

### 2. Database Setup

#### Option A: Local PostgreSQL Installation

1. **Install PostgreSQL** (if not already installed):
   - **macOS**: `brew install postgresql`
   - **Ubuntu**: `sudo apt-get install postgresql postgresql-contrib`
   - **Windows**: Download from [postgresql.org](https://www.postgresql.org/download/)

2. **Start PostgreSQL service**:
   - **macOS**: `brew services start postgresql`
   - **Ubuntu**: `sudo systemctl start postgresql`
   - **Windows**: Start via Services or pgAdmin

3. **Create database and user**:
```sql
-- Connect as postgres user
sudo -u postgres psql

-- Create database
CREATE DATABASE vip_guest_system;

-- Create user
CREATE USER vip_user WITH PASSWORD 'dev_password_123';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE vip_guest_system TO vip_user;

-- Exit
\q
```

4. **Test connection**:
```bash
psql -h localhost -U vip_user -d vip_guest_system
```

#### Option B: Docker PostgreSQL

```bash
# Create and start PostgreSQL container
docker run --name vip-postgres \
  -e POSTGRES_DB=vip_guest_system \
  -e POSTGRES_USER=vip_user \
  -e POSTGRES_PASSWORD=dev_password_123 \
  -p 5432:5432 \
  -d postgres:15

# Verify container is running
docker ps
```

### 3. Backend Configuration

1. **Create local configuration file**:
```bash
# Create the file
touch src/main/resources/application-local.properties
```

2. **Add configuration** (`src/main/resources/application-local.properties`):
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/vip_guest_system
spring.datasource.username=vip_user
spring.datasource.password=dev_password_123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
app.jwt.secret=mySecretKey123456789012345678901234567890123456789012345678901234567890
app.jwt.expiration=900000
app.jwt.refresh-expiration=604800000

# File Upload Configuration
app.upload.dir=./uploads
app.upload.max-file-size=5MB
app.upload.max-request-size=10MB

# Logging Configuration
logging.level.com.restaurant.vip=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# Server Configuration
server.port=8080
```

3. **Create uploads directory**:
```bash
mkdir uploads
```

### 4. Backend Startup

1. **Install dependencies and run**:
```bash
# Clean and install dependencies
mvn clean install

# Run with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

2. **Verify backend is running**:
   - Open browser to `http://localhost:8080/actuator/health`
   - Should see: `{"status":"UP"}`

3. **Check database tables were created**:
```sql
-- Connect to database
psql -h localhost -U vip_user -d vip_guest_system

-- List tables
\dt

-- Should see: staff, guests, visits tables
```

### 5. Frontend Setup

1. **Navigate to mobile app directory**:
```bash
cd mobile-app
```

2. **Install dependencies**:
```bash
npm install
```

3. **Create environment configuration**:
```bash
# Create .env file
touch .env
```

4. **Add environment variables** (`.env`):
```env
# API Configuration
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080/api
EXPO_PUBLIC_API_TIMEOUT=10000

# App Configuration
EXPO_PUBLIC_APP_NAME=VIP Guest System Dev
EXPO_PUBLIC_VERSION=1.0.0-dev
EXPO_PUBLIC_ENVIRONMENT=development
```

5. **Start the development server**:
```bash
npm start
```

6. **Choose your platform**:
   - Press `i` for iOS simulator
   - Press `a` for Android emulator
   - Scan QR code with Expo Go app on physical device

### 6. Initial Data Setup

1. **Create initial staff account** (run this SQL in your database):
```sql
-- Insert a manager account for testing
INSERT INTO staff (email, password_hash, first_name, last_name, role, active) 
VALUES (
  'manager@restaurant.com',
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',  -- password: "password"
  'Test',
  'Manager',
  'MANAGER',
  true
);

-- Insert a server account
INSERT INTO staff (email, password_hash, first_name, last_name, role, active) 
VALUES (
  'server@restaurant.com',
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',  -- password: "password"
  'Test',
  'Server',
  'SERVER',
  true
);
```

2. **Test login in mobile app**:
   - Email: `manager@restaurant.com`
   - Password: `password`

### 7. Development Tools Setup

#### IDE Configuration

**IntelliJ IDEA:**
1. Import project as Maven project
2. Set Project SDK to Java 17
3. Install Spring Boot plugin
4. Configure code style (Google Java Style)

**VS Code:**
1. Install extensions:
   - Spring Boot Extension Pack
   - Java Extension Pack
   - React Native Tools
   - Expo Tools

#### Database Tools

**pgAdmin (GUI for PostgreSQL):**
1. Download from [pgadmin.org](https://www.pgadmin.org/)
2. Add server connection:
   - Host: localhost
   - Port: 5432
   - Database: vip_guest_system
   - Username: vip_user

**DBeaver (Alternative):**
1. Download from [dbeaver.io](https://dbeaver.io/)
2. Create PostgreSQL connection with same details

### 8. Testing Setup

#### Backend Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=GuestServiceTest

# Run with coverage
mvn test jacoco:report
```

#### Frontend Tests
```bash
cd mobile-app

# Run all tests
npm test

# Run in watch mode
npm run test:watch

# Run with coverage
npm run test:coverage
```

## Verification Checklist

After setup, verify everything works:

### Backend Verification
- [ ] Backend starts without errors on port 8080
- [ ] Database connection successful
- [ ] Health check endpoint returns UP status
- [ ] Can create/login with test staff account
- [ ] API endpoints respond correctly (test with Postman)

### Frontend Verification
- [ ] Expo development server starts successfully
- [ ] App loads on simulator/device
- [ ] Can navigate between screens
- [ ] Login works with test credentials
- [ ] API calls to backend succeed

### Integration Verification
- [ ] Login flow works end-to-end
- [ ] Can create and view guest profiles
- [ ] Photo upload functionality works
- [ ] Search and filtering work
- [ ] Visit logging works

## Common Issues and Solutions

### Backend Issues

**Issue: Database connection failed**
```
Solution:
1. Verify PostgreSQL is running: `pg_ctl status`
2. Check connection details in application-local.properties
3. Test manual connection: `psql -h localhost -U vip_user -d vip_guest_system`
```

**Issue: Port 8080 already in use**
```
Solution:
1. Find process using port: `lsof -i :8080` (macOS/Linux) or `netstat -ano | findstr :8080` (Windows)
2. Kill process or change port in application-local.properties
```

**Issue: JWT secret too short**
```
Solution:
Generate a longer secret key (minimum 256 bits):
openssl rand -base64 64
```

### Frontend Issues

**Issue: Metro bundler fails to start**
```
Solution:
1. Clear cache: `expo start -c`
2. Delete node_modules: `rm -rf node_modules && npm install`
3. Reset Expo cache: `expo r -c`
```

**Issue: Network request failed**
```
Solution:
1. Verify backend is running on correct port
2. Check API_BASE_URL in .env file
3. For physical device, use computer's IP instead of localhost
```

**Issue: iOS simulator not opening**
```
Solution:
1. Install Xcode from App Store (macOS only)
2. Open Xcode and install additional components
3. Run: `sudo xcode-select --install`
```

### Database Issues

**Issue: Permission denied for database**
```sql
-- Grant all privileges to user
GRANT ALL PRIVILEGES ON DATABASE vip_guest_system TO vip_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO vip_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO vip_user;
```

**Issue: Tables not created**
```
Solution:
1. Check spring.jpa.hibernate.ddl-auto=update in properties
2. Verify database connection
3. Check application logs for errors
```

## Development Workflow

### Daily Development
1. Start PostgreSQL service
2. Start backend: `mvn spring-boot:run -Dspring-boot.run.profiles=local`
3. Start frontend: `cd mobile-app && npm start`
4. Make changes and test
5. Run tests before committing

### Before Committing
```bash
# Backend
mvn test
mvn checkstyle:check

# Frontend
cd mobile-app
npm test
npm run lint
```

### Database Migrations
When making schema changes:
1. Create SQL migration files in `src/main/resources/db/migration/`
2. Use Flyway naming convention: `V1__Initial_schema.sql`
3. Test migrations on clean database

## Next Steps

After successful setup:
1. Review the [API Documentation](API_DOCUMENTATION.md)
2. Explore the codebase structure
3. Run the test suites to understand functionality
4. Try creating a guest profile and logging visits
5. Experiment with the notification system

## Getting Help

If you encounter issues:
1. Check this guide's troubleshooting section
2. Review application logs for error details
3. Search existing issues in the project repository
4. Create a new issue with detailed error information