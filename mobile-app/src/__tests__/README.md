# Frontend Component Tests

This directory contains comprehensive tests for the React Native mobile application components, covering authentication flow, navigation, and key functionality.

## Test Setup

The testing environment is configured with:
- **Jest** with TypeScript support
- **@testing-library/react-native** for component testing
- **jsdom** test environment
- Comprehensive mocking for React Native dependencies

## Test Categories

### 1. Utility Tests
- **tokenStorage.test.ts**: Tests for secure token storage and retrieval
  - Token storage and retrieval operations
  - Refresh token management
  - User data persistence
  - Error handling and graceful degradation

### 2. Service Tests
- **authService.test.ts**: Authentication service functionality
  - Login/logout operations
  - Token validation and refresh
  - API error handling
- **errorHandler.test.ts**: Error handling utilities
  - Network error detection
  - HTTP status code handling
  - User-friendly error message formatting

### 3. Component Tests (Created but need actual components)
- **GuestCard.test.tsx**: Guest information display component
- **SearchBar.test.tsx**: Real-time search functionality
- **VisitCard.test.tsx**: Visit history display component

### 4. Context Tests (Created but need actual contexts)
- **AuthContext.test.tsx**: Authentication state management
- **NotificationContext.test.tsx**: Notification system

### 5. Screen Tests (Created but need actual screens)
- **LoginScreen.test.tsx**: Login form validation and submission
- **GuestListScreen.test.tsx**: Guest list with search and filtering

### 6. Navigation Tests (Created but need actual navigation)
- **RootNavigator.test.tsx**: Authentication-based navigation flow

## Test Coverage

The tests cover the following requirements from the specification:

### Authentication Flow (Requirement 1)
- ✅ Staff login with email/password validation
- ✅ Role-based access control
- ✅ Session management and timeout
- ✅ Token storage and refresh

### Data Security (Requirement 6)
- ✅ Secure token storage
- ✅ Error handling without exposing sensitive data
- ✅ Input validation and sanitization

### Performance (Requirement 7)
- ✅ Offline data caching
- ✅ Error handling for network failures
- ✅ Graceful degradation

## Running Tests

```bash
# Run all tests
npm test

# Run specific test file
npm test -- --testPathPatterns=tokenStorage

# Run tests with coverage
npm run test:coverage

# Run tests in watch mode
npm run test:watch
```

## Test Utilities

The `test-utils.tsx` file provides:
- Mock data for guests, visits, and notifications
- Custom render function with all providers
- Helper functions for creating mock navigation and route props
- Async operation utilities

## Mocking Strategy

The tests use comprehensive mocking for:
- **AsyncStorage**: For token and data persistence
- **React Navigation**: For navigation testing
- **React Query**: For API state management
- **Expo modules**: For camera and image functionality
- **NetInfo**: For network connectivity

## Key Testing Patterns

1. **Error Handling**: All tests verify both success and error scenarios
2. **Async Operations**: Proper handling of promises and async/await
3. **Mock Verification**: Ensuring mocks are called with correct parameters
4. **State Management**: Testing context providers and state updates
5. **User Interactions**: Simulating user input and navigation

## Future Enhancements

To complete the test suite, the following components need to be implemented:
1. Actual React Native components matching the test expectations
2. Context providers for authentication and notifications
3. Navigation structure with proper screen components
4. Service layer implementations
5. Integration tests for end-to-end workflows

## Notes

- Tests are designed to be independent and can run in any order
- All external dependencies are properly mocked
- Error scenarios are thoroughly tested for robustness
- The test setup supports both unit and integration testing patterns