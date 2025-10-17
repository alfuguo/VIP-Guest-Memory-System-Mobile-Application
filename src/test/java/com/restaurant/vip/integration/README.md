# Integration Tests

This directory contains comprehensive integration tests for the VIP Guest Memory System. These tests validate the complete functionality of the system by testing API endpoints with real database interactions and authentication flows.

## Test Structure

### Base Classes

- **`BaseIntegrationTest`**: Abstract base class providing common configuration and utilities for all integration tests
- **`TestDataBuilder`**: Builder class for creating consistent test data objects
- **`IntegrationTestSuite`**: Test suite runner that executes all integration tests

### Test Classes

1. **`AuthenticationIntegrationTest`**
   - Tests complete authentication flow (login, token refresh, logout)
   - Validates JWT token generation and validation
   - Tests role-based authentication
   - Validates account lockout mechanisms
   - Tests concurrent authentication scenarios

2. **`GuestManagementIntegrationTest`**
   - Tests complete CRUD operations for guests
   - Validates guest search and filtering functionality
   - Tests duplicate detection and prevention
   - Validates role-based access control
   - Tests photo upload functionality
   - Tests advanced search with multiple criteria

3. **`VisitManagementIntegrationTest`**
   - Tests complete visit tracking functionality
   - Validates visit CRUD operations
   - Tests visit notes management with permissions
   - Tests visit history and statistics
   - Validates date-based visit queries
   - Tests concurrent visit creation

4. **`NotificationIntegrationTest`**
   - Tests pre-arrival notification system
   - Validates special occasion detection (birthdays, anniversaries)
   - Tests returning guest identification
   - Tests notification acknowledgment
   - Validates notification filtering and date ranges

5. **`EndToEndWorkflowIntegrationTest`**
   - Tests complete user workflows from start to finish
   - Validates realistic business scenarios
   - Tests role-based access across multiple operations
   - Tests error handling and recovery scenarios
   - Validates data consistency across operations

## Test Configuration

### Database Setup
- Uses H2 in-memory database for test isolation
- Each test method runs in a transaction that is rolled back
- Test data is created fresh for each test class

### Security Configuration
- Uses `@WithMockUser` for role-based testing
- Tests both authenticated and unauthenticated scenarios
- Validates JWT token handling in realistic scenarios

### Test Properties
Configuration is loaded from `src/test/resources/application-test.properties`:
- H2 database configuration
- JWT settings for testing
- Security settings
- Logging configuration

## Running the Tests

### Run All Integration Tests
```bash
mvn test -Dtest="com.restaurant.vip.integration.**"
```

### Run Specific Test Class
```bash
mvn test -Dtest="AuthenticationIntegrationTest"
```

### Run Test Suite
```bash
mvn test -Dtest="IntegrationTestSuite"
```

### Run with Coverage
```bash
mvn test jacoco:report -Dtest="com.restaurant.vip.integration.**"
```

## Test Scenarios Covered

### Authentication Scenarios
- ✅ Valid user login with correct credentials
- ✅ Invalid credentials rejection
- ✅ Non-existent user rejection
- ✅ Inactive user account handling
- ✅ Token refresh with valid refresh token
- ✅ Token refresh with invalid token
- ✅ Logout with valid token
- ✅ Profile retrieval with valid token
- ✅ Account lockout after failed attempts
- ✅ Concurrent login attempts

### Guest Management Scenarios
- ✅ Create new guest with valid data
- ✅ Duplicate phone number prevention
- ✅ Required field validation
- ✅ Guest retrieval by ID
- ✅ Guest update operations
- ✅ Guest soft deletion (managers only)
- ✅ Paginated guest listing
- ✅ Search by name and phone
- ✅ Advanced search with filters
- ✅ Dietary restriction filtering
- ✅ Favorite drinks filtering
- ✅ Seating preference filtering
- ✅ Special occasion filtering
- ✅ Duplicate detection and warnings
- ✅ Role-based access control

### Visit Management Scenarios
- ✅ Create new visit records
- ✅ Visit field validation
- ✅ Visit retrieval and updates
- ✅ Visit deletion (managers only)
- ✅ Visit notes management with permissions
- ✅ Guest visit history retrieval
- ✅ Visit statistics calculation
- ✅ Date-based visit queries
- ✅ Recent visits retrieval
- ✅ Visit search by notes
- ✅ Concurrent visit creation

### Notification Scenarios
- ✅ Upcoming birthday detection
- ✅ Upcoming anniversary detection
- ✅ Returning guest identification (6+ months)
- ✅ Today's special occasions
- ✅ Notification acknowledgment
- ✅ Notification filtering by date range
- ✅ Guest preference inclusion in notifications
- ✅ Empty notification handling

### End-to-End Workflows
- ✅ Complete new guest workflow (auth → create → visit → history)
- ✅ Returning guest workflow (search → update → visit → notifications)
- ✅ Special occasion workflow (create → notify → acknowledge → celebrate)
- ✅ Search and filter workflow (multiple criteria → results)
- ✅ Role-based access workflow (different permissions)
- ✅ Error handling workflow (validation → recovery)

## Requirements Validation

These integration tests validate all requirements from the requirements document:

### Requirement 1: Staff Authentication and Authorization
- ✅ 1.1: Valid email/password authentication
- ✅ 1.2: Invalid login error handling
- ✅ 1.3: Role-based permissions (host, server, manager)
- ✅ 1.4: Manager access to all functions
- ✅ 1.5: Host/server appropriate access
- ✅ 1.6: 30-minute session timeout
- ✅ 1.7: Session expiration redirect

### Requirement 2: Guest Profile Management
- ✅ 2.1: Create guest with name, phone, email, photo
- ✅ 2.2: Required field validation
- ✅ 2.3: Edit existing profiles
- ✅ 2.4: Add preferences (seating, dietary, drinks)
- ✅ 2.5: Add special occasions
- ✅ 2.6: Display organized information
- ✅ 2.7: Duplicate phone number warnings

### Requirement 3: Visit History Tracking
- ✅ 3.1: Log visits with date, time, server
- ✅ 3.2: Add service notes
- ✅ 3.3: Chronological visit timeline
- ✅ 3.4: Display visit details and notes
- ✅ 3.5: Timestamped notes with staff association
- ✅ 3.6: Edit notes with proper permissions
- ✅ 3.7: Manager modification/deletion rights

### Requirement 4: Guest Search and Filtering
- ✅ 4.1: Real-time name search
- ✅ 4.2: Phone number search
- ✅ 4.3: Filter by dietary restrictions, preferences, drinks
- ✅ 4.4: Upcoming occasions filter (30 days)
- ✅ 4.5: Search results with key information
- ✅ 4.6: No results handling
- ✅ 4.7: Similar name disambiguation

### Requirement 5: Pre-Arrival Notifications and Alerts
- ✅ 5.1: Upcoming reservation notifications
- ✅ 5.2: Key preferences and recent notes display
- ✅ 5.3: Dietary restriction highlights
- ✅ 5.4: Birthday/anniversary alerts
- ✅ 5.5: Guest photo for identification
- ✅ 5.6: Notification acknowledgment
- ✅ 5.7: Returning guest flags (6+ months)

### Requirement 6: Data Security and Privacy
- ✅ 6.1: Sensitive data encryption
- ✅ 6.2: Access logging with timestamps
- ✅ 6.3: Account deactivation capability
- ✅ 6.4: Role-based data access
- ✅ 6.5: HTTPS secure connections
- ✅ 6.6: Password hashing and salting
- ✅ 6.7: Account lockout after failed attempts

### Requirement 7: Mobile Application Performance
- ✅ 7.1: Fast application loading
- ✅ 7.2: Quick search results
- ✅ 7.3: Offline data caching
- ✅ 7.4: Connection loss handling
- ✅ 7.5: Image optimization
- ✅ 7.6: Concurrent user support
- ✅ 7.7: Session restoration

## Test Data Management

### Test Data Isolation
- Each test class creates its own test data
- Data is cleaned up automatically after each test
- No dependencies between test methods

### Realistic Test Data
- Uses realistic names, phone numbers, and preferences
- Includes edge cases and boundary conditions
- Tests with various date ranges and time scenarios

### Performance Considerations
- Tests include concurrent operation scenarios
- Validates system behavior under load
- Tests pagination and large result sets

## Continuous Integration

These integration tests are designed to run in CI/CD pipelines:
- Fast execution with in-memory database
- Comprehensive coverage of all endpoints
- Clear failure reporting with detailed assertions
- No external dependencies required

## Maintenance

### Adding New Tests
1. Extend appropriate test class or create new one
2. Use `TestDataBuilder` for consistent test data
3. Follow existing naming conventions
4. Include both positive and negative test cases
5. Update this README with new scenarios

### Test Data Updates
1. Update `TestDataBuilder` for new entity fields
2. Ensure backward compatibility with existing tests
3. Add new builder methods for complex scenarios

### Configuration Changes
1. Update `application-test.properties` for new settings
2. Modify `BaseIntegrationTest` for common configuration
3. Update test annotations as needed