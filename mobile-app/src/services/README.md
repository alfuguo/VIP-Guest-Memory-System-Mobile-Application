# API Service Layer

This directory contains the API service layer implementation for the VIP Guest Memory System mobile app.

## Overview

The API service is built using Axios with JWT token interceptors and automatic token refresh functionality. It provides a robust, secure, and user-friendly way to communicate with the backend API.

## Key Features

### 🔐 JWT Authentication
- Automatic JWT token injection in request headers
- Token refresh on 401 responses
- Secure token storage using AsyncStorage
- Request queuing during token refresh

### 🔄 Automatic Token Refresh
- Seamless token refresh without user intervention
- Failed request retry after successful token refresh
- Automatic logout on refresh failure
- Request queuing to prevent duplicate refresh attempts

### 🚨 Error Handling
- Consistent error transformation and typing
- Network error detection and handling
- Timeout handling with configurable timeouts
- User-friendly error messages

### 📤 File Upload Support
- Multipart/form-data support for file uploads
- Upload progress tracking
- Extended timeout for large files
- Automatic token refresh for upload requests

## Files

### `api.ts`
Main API client implementation with Axios interceptors.

**Key Components:**
- `ApiClient` class with Axios instance
- Request/response interceptors for JWT handling
- Automatic token refresh logic
- Error transformation and handling
- Convenience methods (get, post, put, delete, upload)

### `authService.ts`
Authentication-specific API calls.

**Methods:**
- `login(credentials)` - User authentication
- `refreshToken(refreshToken)` - Token refresh
- `logout()` - User logout
- `validateToken()` - Token validation
- `getCurrentUser()` - Get user profile

### `apiTest.ts`
Integration test utilities for manual testing.

**Test Functions:**
- `testApiService()` - Complete integration test
- `testTokenInterceptor()` - JWT interceptor test
- `testTokenRefresh()` - Token refresh test
- `testErrorHandling()` - Error handling test

## Usage Examples

### Basic API Calls

```typescript
import { apiClient } from './services/api';

// GET request with authentication
const guests = await apiClient.get('/guests');

// POST request with data
const newGuest = await apiClient.post('/guests', guestData);

// PUT request
const updatedGuest = await apiClient.put(`/guests/${id}`, updateData);

// DELETE request
await apiClient.delete(`/guests/${id}`);
```

### File Upload

```typescript
import { apiClient } from './services/api';

const formData = new FormData();
formData.append('photo', {
  uri: photoUri,
  type: 'image/jpeg',
  name: 'guest-photo.jpg',
} as any);

const result = await apiClient.upload('/guests/123/photo', formData, (progress) => {
  console.log(`Upload progress: ${progress}%`);
});
```

### Authentication

```typescript
import { authService } from './services/authService';

// Login
const response = await authService.login({
  email: 'user@example.com',
  password: 'password'
});

// The API client will automatically handle token storage and injection
```

### Error Handling

```typescript
import { apiClient, ApiErrorTypes } from './services/api';

try {
  const data = await apiClient.get('/some-endpoint');
} catch (error) {
  switch (error.message) {
    case ApiErrorTypes.NETWORK_ERROR:
      // Handle network issues
      break;
    case ApiErrorTypes.UNAUTHORIZED:
      // Handle authentication issues
      break;
    case ApiErrorTypes.SESSION_EXPIRED:
      // Handle session expiration
      break;
    default:
      // Handle other errors
      break;
  }
}
```

## Configuration

### Environment Variables
The API base URL is configured based on the environment:

```typescript
const API_BASE_URL = __DEV__ 
  ? 'http://localhost:8080/api' // Development
  : 'https://your-production-api.com/api'; // Production
```

### Timeouts
- Default request timeout: 10 seconds
- Upload timeout: 30 seconds
- Configurable per request

## Error Types

The service provides consistent error types:

- `UNAUTHORIZED` - 401 authentication errors
- `FORBIDDEN` - 403 permission errors
- `SERVER_ERROR` - 5xx server errors
- `NETWORK_ERROR` - Network connectivity issues
- `REQUEST_TIMEOUT` - Request timeout errors
- `SESSION_EXPIRED` - Token refresh failures
- `TOO_MANY_REQUESTS` - Rate limiting errors

## Security Features

### Token Management
- JWT tokens stored securely in AsyncStorage
- Automatic token injection via interceptors
- Token refresh without user intervention
- Secure token cleanup on logout

### Request Security
- HTTPS enforcement in production
- Request/response interceptors for security headers
- Automatic retry with fresh tokens
- Request queuing during authentication

## Testing

### Manual Testing
Use the test utilities in `apiTest.ts`:

```typescript
import { testApiService } from './services/apiTest';

// Run complete integration test
const result = await testApiService();
console.log(result);
```

### Unit Testing
Basic unit tests are provided in `__tests__/api.test.ts`.

## Requirements Satisfied

This implementation satisfies the following requirements:

- **Requirement 1.6**: Automatic logout after 30 minutes of inactivity (handled by backend)
- **Requirement 6.5**: Secure HTTPS connections and JWT token management
- **Requirement 7.3**: Fast API responses and offline caching support (with React Query)

## Integration with React Query

The API client is designed to work seamlessly with React Query for caching and offline support:

```typescript
import { useQuery } from '@tanstack/react-query';
import { apiClient } from './services/api';

const useGuests = () => {
  return useQuery({
    queryKey: ['guests'],
    queryFn: () => apiClient.get('/guests'),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
```