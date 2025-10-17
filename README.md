# VIP Guest Memory System

A comprehensive mobile application designed to help restaurant staff remember and delight repeat customers by tracking their preferences, visit history, and providing pre-arrival notifications.

## 🏗️ Architecture

The system consists of two main components:
- **Backend**: Spring Boot REST API with PostgreSQL database
- **Frontend**: React Native mobile app with Expo

## 📋 Prerequisites

### Backend Requirements
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+
- Docker (optional, for containerized deployment)

### Frontend Requirements
- Node.js 18+ and npm
- Expo CLI (`npm install -g @expo/cli`)
- iOS Simulator (macOS) or Android Studio (for emulators)
- Expo Go app on physical device (optional)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd vip-guest-memory-system
```

### 2. SSL Setup (Production Only)

For production deployment, create SSL directory:
```bash
mkdir -p ssl
# Place your SSL certificates in the ssl directory:
# ssl/cert.pem (certificate file)
# ssl/private.key (private key file)
```

For development, you can skip this step as nginx is optional.

### 3. Backend Setup

#### Database Setup
1. Install PostgreSQL and create a database:
```sql
CREATE DATABASE vip_guest_system;
CREATE USER vip_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE vip_guest_system TO vip_user;
```

2. Configure environment variables (create `src/main/resources/application-local.properties`):
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/vip_guest_system
spring.datasource.username=vip_user
spring.datasource.password=your_password

# JWT Configuration
app.jwt.secret=your-256-bit-secret-key-here
app.jwt.expiration=900000

# File Upload Configuration
app.upload.dir=./uploads
app.upload.max-file-size=5MB
```

#### Run Backend
```bash
# Install dependencies and run
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or run with Maven wrapper
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The backend will start on `http://localhost:8080`

### 4. Frontend Setup

```bash
cd mobile-app

# Install dependencies
npm install

# Start the development server
npm start

# Or run on specific platform
npm run android  # Android emulator
npm run ios      # iOS simulator
```

## 🔧 Configuration

### Backend Configuration

#### Environment Variables
Create environment-specific property files:

**application-dev.properties** (Development)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/vip_guest_system_dev
spring.jpa.hibernate.ddl-auto=update
logging.level.com.restaurant.vip=DEBUG
```

**application-prod.properties** (Production)
```properties
spring.datasource.url=${DATABASE_URL}
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.restaurant.vip=WARN
server.port=${PORT:8080}
```

#### Security Configuration
- JWT tokens expire after 15 minutes
- Refresh tokens expire after 7 days
- Account lockout after 5 failed attempts
- HTTPS required in production

### Frontend Configuration

#### Environment Setup
Create `mobile-app/.env` file:
```env
# API Configuration
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080/api
EXPO_PUBLIC_API_TIMEOUT=10000

# App Configuration
EXPO_PUBLIC_APP_NAME=VIP Guest System
EXPO_PUBLIC_VERSION=1.0.0
```

#### Build Configuration
Update `mobile-app/app.json` for your deployment:
```json
{
  "expo": {
    "name": "VIP Guest System",
    "slug": "vip-guest-system",
    "version": "1.0.0",
    "platforms": ["ios", "android"],
    "extra": {
      "apiBaseUrl": process.env.EXPO_PUBLIC_API_BASE_URL
    }
  }
}
```

## 🧪 Testing

### Backend Tests
```bash
# Run all tests
mvn test

# Run integration tests
mvn test -Dtest="**/*IntegrationTest"

# Run with coverage
mvn test jacoco:report
```

### Frontend Tests
```bash
cd mobile-app

# Run all tests
npm test

# Run with coverage
npm run test:coverage

# Run in watch mode
npm run test:watch
```

## 📚 API Documentation

### Authentication Endpoints

#### POST /api/auth/login
Authenticate staff member and receive JWT token.

**Request:**
```json
{
  "email": "staff@restaurant.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "user": {
    "id": 1,
    "email": "staff@restaurant.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "SERVER"
  }
}
```

#### POST /api/auth/refresh
Refresh expired JWT token.

**Request:**
```json
{
  "refreshToken": "refresh_token_here"
}
```

### Guest Management Endpoints

#### GET /api/guests
Retrieve paginated list of guests with optional search and filtering.

**Query Parameters:**
- `page` (int): Page number (default: 0)
- `size` (int): Page size (default: 20)
- `search` (string): Search by name or phone
- `dietaryRestrictions` (string): Filter by dietary restrictions
- `upcomingOccasions` (boolean): Filter guests with upcoming occasions

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "phone": "+1234567890",
      "email": "john.doe@email.com",
      "photoUrl": "https://storage.example.com/photos/guest1.jpg",
      "seatingPreference": "Window table",
      "dietaryRestrictions": ["Vegetarian"],
      "favoriteDrinks": ["Red wine"],
      "birthday": "1985-06-15",
      "lastVisit": "2024-01-15T19:30:00Z",
      "visitCount": 12
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20
}
```

#### POST /api/guests
Create a new guest profile.

**Request:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+1987654321",
  "email": "jane.smith@email.com",
  "seatingPreference": "Quiet corner",
  "dietaryRestrictions": ["Gluten-free"],
  "favoriteDrinks": ["White wine", "Sparkling water"],
  "birthday": "1990-03-20",
  "anniversary": "2015-08-10",
  "notes": "Prefers dim lighting"
}
```

#### GET /api/guests/{id}
Retrieve detailed guest profile including visit history.

#### PUT /api/guests/{id}
Update existing guest profile.

#### POST /api/guests/{id}/photo
Upload guest photo (multipart/form-data).

### Visit Tracking Endpoints

#### GET /api/guests/{id}/visits
Retrieve guest's visit history with pagination.

#### POST /api/guests/{id}/visits
Log a new visit for the guest.

**Request:**
```json
{
  "visitDate": "2024-01-20",
  "visitTime": "19:30:00",
  "partySize": 2,
  "tableNumber": "A5",
  "serviceNotes": "Celebrated anniversary, provided complimentary dessert"
}
```

### Notification Endpoints

#### GET /api/notifications/upcoming
Get upcoming guest notifications (reservations, special occasions).

#### GET /api/notifications/special-occasions
Get guests with birthdays/anniversaries in the next 30 days.

## 🔐 Authentication Flow

1. **Login**: Staff enters email/password
2. **Token Generation**: Server validates credentials and returns JWT + refresh token
3. **Token Storage**: Mobile app stores tokens securely in AsyncStorage
4. **API Requests**: Include JWT in Authorization header: `Bearer <token>`
5. **Token Refresh**: Automatically refresh expired tokens using refresh token
6. **Logout**: Clear stored tokens and redirect to login

## 🏢 User Roles

### HOST
- View guest profiles and visit history
- Log new visits
- Search and filter guests
- View notifications

### SERVER
- All HOST permissions
- Edit visit notes for their own visits
- Add guest preferences and notes

### MANAGER
- All SERVER permissions
- Create/edit/delete guest profiles
- Edit any visit notes
- View audit logs
- Manage staff accounts

## 🛠️ Development

### Project Structure

```
vip-guest-memory-system/
├── src/                          # Backend source code
│   ├── main/java/com/restaurant/vip/
│   │   ├── config/              # Configuration classes
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # Data transfer objects
│   │   ├── entity/              # JPA entities
│   │   ├── repository/          # Data repositories
│   │   ├── service/             # Business logic
│   │   └── security/            # Security configuration
│   └── test/                    # Backend tests
├── mobile-app/                  # React Native frontend
│   ├── src/
│   │   ├── components/          # Reusable components
│   │   ├── contexts/            # React contexts
│   │   ├── navigation/          # Navigation configuration
│   │   ├── screens/             # Screen components
│   │   ├── services/            # API services
│   │   └── utils/               # Utility functions
│   └── __tests__/               # Frontend tests
└── docs/                        # Additional documentation
```

### Code Style

#### Backend (Java)
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add JavaDoc for public methods
- Prefer composition over inheritance

#### Frontend (TypeScript/React Native)
- Use TypeScript strict mode
- Follow React hooks best practices
- Use functional components with hooks
- Implement proper error boundaries

### Git Workflow

1. Create feature branch: `git checkout -b feature/guest-search`
2. Make changes and commit: `git commit -m "Add guest search functionality"`
3. Push branch: `git push origin feature/guest-search`
4. Create pull request for review
5. Merge after approval and testing

## 🚀 Deployment

### Backend Deployment

#### Using Docker (Recommended)
```bash
# Build JAR file
mvn clean package -DskipTests

# Build Docker image
docker build -t vip-guest-system-backend .

# Run with Docker Compose
docker-compose up -d
```

#### Manual Deployment
```bash
# Build production JAR
mvn clean package -Pprod

# Run with production profile
java -jar target/vip-guest-memory-system-1.0.0.jar --spring.profiles.active=prod
```

### Frontend Deployment

#### Build for Production
```bash
cd mobile-app

# Build for iOS
expo build:ios

# Build for Android
expo build:android

# Or use EAS Build (recommended)
eas build --platform all
```

## 🔍 Troubleshooting

### Common Backend Issues

**Database Connection Failed**
- Verify PostgreSQL is running
- Check connection string and credentials
- Ensure database exists and user has permissions

**JWT Token Issues**
- Verify JWT secret is properly configured
- Check token expiration settings
- Ensure system clock is synchronized

### Common Frontend Issues

**API Connection Failed**
- Verify backend is running and accessible
- Check API base URL configuration
- Ensure network connectivity

**Build Failures**
- Clear node_modules: `rm -rf node_modules && npm install`
- Clear Expo cache: `expo start -c`
- Update Expo CLI: `npm install -g @expo/cli@latest`

### Performance Issues

**Slow Database Queries**
- Check database indexes are created
- Analyze query execution plans
- Consider adding pagination to large result sets

**Mobile App Performance**
- Enable Flipper for debugging
- Use React DevTools for component analysis
- Implement proper image caching and optimization

## 📞 Support

For technical support or questions:
- Create an issue in the project repository
- Check existing documentation in `/docs` folder
- Review API documentation at `/api/swagger-ui` (when running)

## 📄 License

This project is proprietary software. All rights reserved.