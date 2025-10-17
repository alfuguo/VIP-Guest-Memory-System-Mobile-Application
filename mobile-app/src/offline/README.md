# Offline Support and Caching

This directory contains the implementation of offline support and caching functionality for the VIP Guest Memory System mobile app.

## Overview

The offline support system provides:

1. **Local Data Caching** - Automatic caching of API responses using React Query
2. **Offline Detection** - Network status monitoring and offline mode detection
3. **Request Queuing** - Automatic queuing of failed requests when offline
4. **Recently Viewed Guests** - Local storage of recently accessed guest profiles
5. **Optimistic Updates** - Immediate UI updates with rollback on failure

## Key Components

### React Query Configuration (`config/queryClient.ts`)
- Enhanced QueryClient with offline-aware retry logic
- Cache invalidation utilities
- Offline storage integration
- Configurable cache times for different data types

### Network Context (`contexts/NetworkContext.tsx`)
- Real-time network status monitoring
- Connection type detection (WiFi, cellular, etc.)
- Network state change notifications

### Offline Queue (`utils/offlineQueue.ts`)
- Automatic request queuing when offline
- Priority-based request processing
- Retry logic with exponential backoff
- Persistent storage of queued requests

### Recent Guests Manager (`utils/recentGuestsManager.ts`)
- Local storage of recently viewed guest profiles
- Search and filtering capabilities
- Automatic cleanup of expired entries
- Statistics and analytics

### React Query Hooks (`hooks/useGuestQueries.ts`, etc.)
- Offline-aware API hooks
- Automatic fallback to cached data
- Optimistic updates for mutations
- Cache invalidation strategies

## Usage Examples

### Using Offline-Aware Hooks

```typescript
import { useGuests, useCreateGuest } from '../hooks/useGuestQueries';
import { useOfflineStatus } from '../hooks/useOffline';

function GuestListScreen() {
  const { data: guests, isLoading } = useGuests();
  const { isOffline } = useOfflineStatus();
  const createGuestMutation = useCreateGuest();

  const handleCreateGuest = async (guestData) => {
    try {
      await createGuestMutation.mutateAsync(guestData);
      // Will automatically queue if offline
    } catch (error) {
      // Handle error
    }
  };

  return (
    <View>
      {isOffline && <OfflineBanner />}
      {/* Rest of component */}
    </View>
  );
}
```

### Managing Recent Guests

```typescript
import { useRecentGuests } from '../hooks/useRecentGuests';

function RecentGuestsScreen() {
  const { 
    recentGuests, 
    addRecentGuest, 
    searchRecentGuests 
  } = useRecentGuests();

  const handleGuestView = async (guest) => {
    await addRecentGuest(guest);
    // Navigate to guest detail
  };

  return (
    <View>
      {recentGuests.map(guest => (
        <GuestCard key={guest.id} guest={guest} />
      ))}
    </View>
  );
}
```

### Offline Indicators

```typescript
import { OfflineIndicator, OfflineBanner } from '../components/OfflineIndicator';

function MainScreen() {
  return (
    <View>
      <OfflineBanner />
      <OfflineIndicator showDetails />
      {/* Rest of screen */}
    </View>
  );
}
```

## Configuration

### Cache Times
- Guest data: 10 minutes
- Guest lists: 5 minutes
- Visit data: 15 minutes
- Notifications: 2 minutes
- Offline cache: 24 hours

### Queue Settings
- Max queue size: 100 requests
- Max retries: 3 attempts
- Retry delay: Exponential backoff (1s, 2s, 4s, ...)

### Recent Guests
- Max recent guests: 50
- Expiry time: 7 days
- Auto-cleanup on app start

## Requirements Addressed

This implementation addresses the following requirements:

- **7.3**: API caching and offline support
- **7.4**: Offline mode functionality and local storage
- **7.6**: Performance optimization and error handling

## Technical Details

### Network Detection
Uses `@react-native-community/netinfo` for reliable network status detection across iOS and Android.

### Data Persistence
- React Query cache for API responses
- AsyncStorage for offline queue and recent guests
- Automatic cleanup of expired data

### Error Handling
- Graceful degradation when offline
- Automatic retry with exponential backoff
- User-friendly error messages
- Rollback of optimistic updates on failure

### Performance Optimizations
- Debounced search queries
- Lazy loading of cached data
- Background queue processing
- Memory-efficient data structures