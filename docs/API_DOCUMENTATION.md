# API Documentation

## Base URL
- Development: `http://localhost:8080/api`
- Production: `https://your-domain.com/api`

## Authentication

All API endpoints (except login) require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Token Lifecycle
- **Access Token**: Expires after 15 minutes
- **Refresh Token**: Expires after 7 days
- **Auto-refresh**: Mobile app automatically refreshes tokens

## Error Responses

All endpoints return consistent error responses:

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable error message",
  "timestamp": "2024-01-20T10:30:00Z",
  "path": "/api/guests"
}
```

### Common Error Codes
- `VALIDATION_ERROR`: Request validation failed
- `UNAUTHORIZED`: Invalid or expired token
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `DUPLICATE_RESOURCE`: Resource already exists
- `INTERNAL_ERROR`: Server error

## Authentication Endpoints

### POST /auth/login
Authenticate staff member and receive tokens.

**Request Body:**
```json
{
  "email": "staff@restaurant.com",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_string",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "staff@restaurant.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "SERVER",
    "active": true
  }
}
```

**Error Response (401):**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid email or password"
}
```

### POST /auth/refresh
Refresh an expired access token.

**Request Body:**
```json
{
  "refreshToken": "refresh_token_string"
}
```

**Success Response (200):**
```json
{
  "token": "new_jwt_token",
  "expiresIn": 900
}
```

### POST /auth/logout
Invalidate current refresh token.

**Request Body:**
```json
{
  "refreshToken": "refresh_token_string"
}
```

**Success Response (200):**
```json
{
  "message": "Logged out successfully"
}
```

### GET /auth/profile
Get current user profile information.

**Success Response (200):**
```json
{
  "id": 1,
  "email": "staff@restaurant.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "SERVER",
  "active": true,
  "createdAt": "2024-01-01T10:00:00Z"
}
```

## Guest Management Endpoints

### GET /guests
Retrieve paginated list of guests with optional filtering.

**Query Parameters:**
- `page` (integer, default: 0): Page number
- `size` (integer, default: 20, max: 100): Page size
- `search` (string): Search by name or phone number
- `dietaryRestrictions` (string): Filter by dietary restrictions
- `seatingPreference` (string): Filter by seating preference
- `upcomingOccasions` (boolean): Show guests with occasions in next 30 days
- `sortBy` (string, default: "lastName"): Sort field
- `sortDirection` (string, default: "ASC"): Sort direction (ASC/DESC)

**Example Request:**
```
GET /guests?page=0&size=10&search=john&upcomingOccasions=true
```

**Success Response (200):**
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
      "dietaryRestrictions": ["Vegetarian", "No nuts"],
      "favoriteDrinks": ["Red wine", "Sparkling water"],
      "birthday": "1985-06-15",
      "anniversary": "2010-09-20",
      "notes": "Prefers quiet atmosphere",
      "lastVisit": "2024-01-15T19:30:00Z",
      "visitCount": 12,
      "createdAt": "2023-12-01T10:00:00Z",
      "updatedAt": "2024-01-10T15:30:00Z"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 150,
  "totalPages": 15,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

### POST /guests
Create a new guest profile.

**Required Role:** SERVER or MANAGER

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+1987654321",
  "email": "jane.smith@email.com",
  "seatingPreference": "Quiet corner",
  "dietaryRestrictions": ["Gluten-free", "Dairy-free"],
  "favoriteDrinks": ["White wine", "Sparkling water"],
  "birthday": "1990-03-20",
  "anniversary": "2015-08-10",
  "notes": "Prefers dim lighting, allergic to shellfish"
}
```

**Success Response (201):**
```json
{
  "id": 2,
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+1987654321",
  "email": "jane.smith@email.com",
  "photoUrl": null,
  "seatingPreference": "Quiet corner",
  "dietaryRestrictions": ["Gluten-free", "Dairy-free"],
  "favoriteDrinks": ["White wine", "Sparkling water"],
  "birthday": "1990-03-20",
  "anniversary": "2015-08-10",
  "notes": "Prefers dim lighting, allergic to shellfish",
  "visitCount": 0,
  "createdAt": "2024-01-20T14:30:00Z",
  "updatedAt": "2024-01-20T14:30:00Z"
}
```

**Error Response (400) - Validation:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Phone number is required",
  "fieldErrors": {
    "phone": "Phone number cannot be empty"
  }
}
```

**Error Response (409) - Duplicate:**
```json
{
  "error": "DUPLICATE_RESOURCE",
  "message": "Guest with this phone number already exists",
  "existingGuestId": 1
}
```

### GET /guests/{id}
Retrieve detailed guest profile by ID.

**Path Parameters:**
- `id` (integer): Guest ID

**Success Response (200):**
```json
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
  "anniversary": "2010-09-20",
  "notes": "Prefers quiet atmosphere",
  "visitCount": 12,
  "lastVisit": "2024-01-15T19:30:00Z",
  "createdAt": "2023-12-01T10:00:00Z",
  "updatedAt": "2024-01-10T15:30:00Z",
  "recentVisits": [
    {
      "id": 25,
      "visitDate": "2024-01-15",
      "visitTime": "19:30:00",
      "partySize": 2,
      "tableNumber": "A5",
      "serviceNotes": "Celebrated anniversary",
      "staffName": "Jane Smith"
    }
  ]
}
```

### PUT /guests/{id}
Update existing guest profile.

**Required Role:** SERVER or MANAGER

**Path Parameters:**
- `id` (integer): Guest ID

**Request Body:** (Same as POST /guests)

**Success Response (200):** (Same as GET /guests/{id})

### DELETE /guests/{id}
Soft delete a guest profile (sets active=false).

**Required Role:** MANAGER

**Path Parameters:**
- `id` (integer): Guest ID

**Success Response (204):** No content

### POST /guests/{id}/photo
Upload a photo for the guest.

**Required Role:** SERVER or MANAGER

**Path Parameters:**
- `id` (integer): Guest ID

**Request:** Multipart form data with file field named "photo"

**Supported formats:** JPEG, PNG, WebP
**Max file size:** 5MB

**Success Response (200):**
```json
{
  "photoUrl": "https://storage.example.com/photos/guest1_updated.jpg"
}
```

## Visit Tracking Endpoints

### GET /guests/{guestId}/visits
Retrieve visit history for a specific guest.

**Path Parameters:**
- `guestId` (integer): Guest ID

**Query Parameters:**
- `page` (integer, default: 0): Page number
- `size` (integer, default: 20): Page size
- `sortDirection` (string, default: "DESC"): Sort by visit date

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 25,
      "guestId": 1,
      "visitDate": "2024-01-15",
      "visitTime": "19:30:00",
      "partySize": 2,
      "tableNumber": "A5",
      "serviceNotes": "Celebrated anniversary, provided complimentary dessert",
      "staffId": 2,
      "staffName": "Jane Smith",
      "createdAt": "2024-01-15T19:30:00Z",
      "updatedAt": "2024-01-15T20:00:00Z"
    }
  ],
  "totalElements": 12,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

### POST /guests/{guestId}/visits
Log a new visit for the guest.

**Path Parameters:**
- `guestId` (integer): Guest ID

**Request Body:**
```json
{
  "visitDate": "2024-01-20",
  "visitTime": "19:30:00",
  "partySize": 2,
  "tableNumber": "B3",
  "serviceNotes": "Requested wine pairing, enjoyed the salmon special"
}
```

**Success Response (201):**
```json
{
  "id": 26,
  "guestId": 1,
  "visitDate": "2024-01-20",
  "visitTime": "19:30:00",
  "partySize": 2,
  "tableNumber": "B3",
  "serviceNotes": "Requested wine pairing, enjoyed the salmon special",
  "staffId": 1,
  "staffName": "John Doe",
  "createdAt": "2024-01-20T19:30:00Z",
  "updatedAt": "2024-01-20T19:30:00Z"
}
```

### PUT /visits/{id}
Update an existing visit record.

**Required Role:** Original staff member or MANAGER

**Path Parameters:**
- `id` (integer): Visit ID

**Request Body:** (Same as POST visit)

**Success Response (200):** (Same as visit response)

### DELETE /visits/{id}
Delete a visit record.

**Required Role:** MANAGER

**Path Parameters:**
- `id` (integer): Visit ID

**Success Response (204):** No content

## Notification Endpoints

### GET /notifications/upcoming
Get notifications for guests with upcoming reservations or occasions.

**Query Parameters:**
- `days` (integer, default: 7): Look ahead days for occasions

**Success Response (200):**
```json
{
  "upcomingOccasions": [
    {
      "guestId": 1,
      "guestName": "John Doe",
      "photoUrl": "https://storage.example.com/photos/guest1.jpg",
      "occasionType": "BIRTHDAY",
      "occasionDate": "2024-01-25",
      "daysUntil": 5,
      "preferences": {
        "seatingPreference": "Window table",
        "dietaryRestrictions": ["Vegetarian"],
        "favoriteDrinks": ["Red wine"]
      }
    }
  ],
  "returningGuests": [
    {
      "guestId": 3,
      "guestName": "Alice Johnson",
      "photoUrl": "https://storage.example.com/photos/guest3.jpg",
      "lastVisit": "2023-06-15T18:00:00Z",
      "daysSinceLastVisit": 218,
      "visitCount": 8
    }
  ]
}
```

### GET /notifications/special-occasions
Get guests with birthdays or anniversaries in the specified date range.

**Query Parameters:**
- `startDate` (date, default: today): Start date (YYYY-MM-DD)
- `endDate` (date, default: +30 days): End date (YYYY-MM-DD)

**Success Response (200):**
```json
{
  "occasions": [
    {
      "guestId": 1,
      "guestName": "John Doe",
      "photoUrl": "https://storage.example.com/photos/guest1.jpg",
      "occasionType": "BIRTHDAY",
      "occasionDate": "2024-01-25",
      "age": 39,
      "preferences": {
        "seatingPreference": "Window table",
        "dietaryRestrictions": ["Vegetarian"],
        "favoriteDrinks": ["Red wine"]
      }
    },
    {
      "guestId": 2,
      "guestName": "Jane Smith",
      "photoUrl": "https://storage.example.com/photos/guest2.jpg",
      "occasionType": "ANNIVERSARY",
      "occasionDate": "2024-02-14",
      "yearsMarried": 9,
      "preferences": {
        "seatingPreference": "Romantic corner",
        "favoriteDrinks": ["Champagne"]
      }
    }
  ]
}
```

### POST /notifications/{id}/acknowledge
Mark a notification as acknowledged.

**Path Parameters:**
- `id` (integer): Notification ID

**Success Response (200):**
```json
{
  "message": "Notification acknowledged"
}
```

## Staff Management Endpoints (Manager Only)

### GET /staff
Get list of all staff members.

**Required Role:** MANAGER

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "email": "john.doe@restaurant.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "SERVER",
      "active": true,
      "createdAt": "2024-01-01T10:00:00Z"
    }
  ]
}
```

### POST /staff
Create a new staff member.

**Required Role:** MANAGER

**Request Body:**
```json
{
  "email": "new.staff@restaurant.com",
  "firstName": "New",
  "lastName": "Staff",
  "role": "HOST",
  "password": "temporaryPassword123"
}
```

### PUT /staff/{id}/deactivate
Deactivate a staff member account.

**Required Role:** MANAGER

**Success Response (200):**
```json
{
  "message": "Staff member deactivated"
}
```

## Rate Limiting

API endpoints are rate limited to prevent abuse:

- **Authentication endpoints**: 5 requests per minute per IP
- **Guest management**: 100 requests per minute per user
- **File uploads**: 10 requests per minute per user
- **Other endpoints**: 200 requests per minute per user

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642694400
```

## Pagination

All list endpoints support pagination with consistent parameters:

- `page`: Page number (0-based)
- `size`: Items per page (max 100)
- `sort`: Sort field name
- `direction`: Sort direction (ASC/DESC)

Response includes pagination metadata:
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

## WebSocket Events (Future Enhancement)

Real-time notifications will be available via WebSocket connection:

```javascript
// Connect to WebSocket
const ws = new WebSocket('ws://localhost:8080/ws');

// Listen for events
ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  switch(data.type) {
    case 'GUEST_ARRIVED':
      // Handle guest arrival notification
      break;
    case 'SPECIAL_OCCASION':
      // Handle special occasion alert
      break;
  }
};
```