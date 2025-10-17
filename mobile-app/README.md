# VIP Guest Memory System - Mobile App

This is the React Native mobile application for the VIP Guest Memory System, built with Expo and TypeScript.

## Project Structure

```
src/
├── components/          # Reusable UI components
├── contexts/           # React contexts (Auth, etc.)
├── navigation/         # Navigation configuration
├── screens/           # Screen components
│   ├── auth/          # Authentication screens
│   ├── guests/        # Guest management screens
│   ├── visits/        # Visit tracking screens
│   ├── search/        # Search functionality screens
│   ├── notifications/ # Notification screens
│   └── profile/       # User profile screens
├── services/          # API services and utilities
├── types/             # TypeScript type definitions
└── utils/             # Utility functions
```

## Technology Stack

- **React Native** with Expo SDK 54
- **TypeScript** for type safety
- **React Navigation 6** for navigation
- **React Query** for API state management and caching
- **AsyncStorage** for local data persistence
- **React Native Paper** for UI components
- **Expo Camera** for photo capture

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn
- Expo CLI (`npm install -g @expo/cli`)

### Installation

1. Navigate to the mobile-app directory:
   ```bash
   cd mobile-app
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

4. Use the Expo Go app on your phone to scan the QR code, or run on simulator:
   ```bash
   npm run ios     # iOS simulator (macOS only)
   npm run android # Android emulator
   npm run web     # Web browser
   ```

## Development Status

This project is currently in development. The following tasks have been completed:

- ✅ Project structure and navigation setup
- ✅ Authentication context and token management
- 🚧 Authentication screens (Task 8)
- 🚧 Guest management screens (Task 9)
- 🚧 Visit tracking functionality (Task 10)
- 🚧 Search and filtering (Task 11)
- 🚧 Notifications (Task 12)
- 🚧 Offline support (Task 13)

## Configuration

### API Configuration

The API base URL is configured in `src/services/api.ts`:
- Development: `http://localhost:8080/api`
- Production: Update with your production API URL

### Environment Variables

Create a `.env` file in the mobile-app directory for environment-specific configuration:

```
API_BASE_URL=http://localhost:8080/api
```

## Authentication Flow

The app uses JWT-based authentication with the following flow:

1. User enters credentials on login screen
2. App sends login request to backend API
3. Backend returns JWT token and refresh token
4. Tokens are stored securely in AsyncStorage
5. API requests include JWT token in Authorization header
6. Tokens are automatically refreshed when expired

## Navigation Structure

```
RootNavigator
├── AuthNavigator (when not authenticated)
│   └── LoginScreen
└── MainNavigator (when authenticated)
    ├── GuestNavigator (Tab 1)
    │   ├── GuestListScreen
    │   ├── GuestProfileScreen
    │   ├── VisitHistoryScreen
    │   └── VisitLogScreen
    ├── SearchScreen (Tab 2)
    ├── NotificationsScreen (Tab 3)
    └── ProfileScreen (Tab 4)
```

## State Management

- **Authentication State**: Managed by AuthContext with useReducer
- **API State**: Managed by React Query for caching and synchronization
- **Local Storage**: AsyncStorage for token persistence and offline data

## Next Steps

1. Implement authentication screens and API integration (Task 8)
2. Build guest management functionality (Task 9)
3. Add visit tracking features (Task 10)
4. Implement search and filtering (Task 11)
5. Add notification system (Task 12)
6. Implement offline support (Task 13)