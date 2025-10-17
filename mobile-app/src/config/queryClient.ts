import { QueryClient } from '@tanstack/react-query';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { ApiErrorTypes } from '../services/api';

// Cache keys for different data types
export const QUERY_KEYS = {
  GUESTS: 'guests',
  GUEST_DETAIL: 'guest-detail',
  GUEST_SEARCH: 'guest-search',
  VISITS: 'visits',
  NOTIFICATIONS: 'notifications',
  SPECIAL_OCCASIONS: 'special-occasions',
} as const;

// Cache configuration
const CACHE_CONFIG = {
  // Guest data cache for 10 minutes (frequently accessed)
  GUEST_STALE_TIME: 10 * 60 * 1000,
  // Guest list cache for 5 minutes (may change frequently)
  GUEST_LIST_STALE_TIME: 5 * 60 * 1000,
  // Visit data cache for 15 minutes (less frequently changed)
  VISIT_STALE_TIME: 15 * 60 * 1000,
  // Notification cache for 2 minutes (time-sensitive)
  NOTIFICATION_STALE_TIME: 2 * 60 * 1000,
  // Cache time for offline storage (24 hours)
  OFFLINE_CACHE_TIME: 24 * 60 * 60 * 1000,
};

// Offline cache storage utilities
export const offlineStorage = {
  async setItem(key: string, value: any): Promise<void> {
    try {
      const serializedValue = JSON.stringify({
        data: value,
        timestamp: Date.now(),
      });
      await AsyncStorage.setItem(`cache_${key}`, serializedValue);
    } catch (error) {
      console.warn('Failed to cache data offline:', error);
    }
  },

  async getItem(key: string): Promise<any | null> {
    try {
      const serializedValue = await AsyncStorage.getItem(`cache_${key}`);
      if (!serializedValue) return null;

      const { data, timestamp } = JSON.parse(serializedValue);
      
      // Check if cache is still valid (24 hours)
      if (Date.now() - timestamp > CACHE_CONFIG.OFFLINE_CACHE_TIME) {
        await this.removeItem(key);
        return null;
      }

      return data;
    } catch (error) {
      console.warn('Failed to retrieve cached data:', error);
      return null;
    }
  },

  async removeItem(key: string): Promise<void> {
    try {
      await AsyncStorage.removeItem(`cache_${key}`);
    } catch (error) {
      console.warn('Failed to remove cached data:', error);
    }
  },

  async clear(): Promise<void> {
    try {
      const keys = await AsyncStorage.getAllKeys();
      const cacheKeys = keys.filter(key => key.startsWith('cache_'));
      await AsyncStorage.multiRemove(cacheKeys);
    } catch (error) {
      console.warn('Failed to clear cache:', error);
    }
  },
};

// Create enhanced query client with offline support
export const createQueryClient = () => {
  return new QueryClient({
    defaultOptions: {
      queries: {
        // Retry configuration
        retry: (failureCount, error) => {
          // Don't retry on authentication errors
          if (error.message === ApiErrorTypes.UNAUTHORIZED || 
              error.message === ApiErrorTypes.FORBIDDEN) {
            return false;
          }
          
          // Retry network errors up to 3 times
          if (error.message === ApiErrorTypes.NETWORK_ERROR) {
            return failureCount < 3;
          }
          
          // Retry server errors up to 2 times
          if (error.message === ApiErrorTypes.SERVER_ERROR) {
            return failureCount < 2;
          }
          
          return failureCount < 1;
        },
        
        // Retry delay with exponential backoff
        retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
        
        // Default stale time
        staleTime: CACHE_CONFIG.GUEST_STALE_TIME,
        
        // Cache time (how long data stays in cache when not used)
        gcTime: 30 * 60 * 1000, // 30 minutes
        
        // Refetch on window focus (useful for web, less relevant for mobile)
        refetchOnWindowFocus: false,
        
        // Refetch on reconnect
        refetchOnReconnect: true,
        
        // Background refetch interval (disabled by default)
        refetchInterval: false,
      },
      mutations: {
        // Retry mutations on network errors
        retry: (failureCount, error) => {
          if (error.message === ApiErrorTypes.NETWORK_ERROR) {
            return failureCount < 2;
          }
          return false;
        },
        
        // Retry delay for mutations
        retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
      },
    },
  });
};

// Cache invalidation utilities
export const cacheUtils = {
  // Invalidate all guest-related queries
  invalidateGuestQueries: (queryClient: QueryClient) => {
    queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.GUESTS] });
    queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.GUEST_DETAIL] });
    queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.GUEST_SEARCH] });
  },

  // Invalidate specific guest detail
  invalidateGuestDetail: (queryClient: QueryClient, guestId: number) => {
    queryClient.invalidateQueries({ 
      queryKey: [QUERY_KEYS.GUEST_DETAIL, guestId] 
    });
  },

  // Invalidate visit-related queries
  invalidateVisitQueries: (queryClient: QueryClient, guestId?: number) => {
    if (guestId) {
      queryClient.invalidateQueries({ 
        queryKey: [QUERY_KEYS.VISITS, guestId] 
      });
    } else {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.VISITS] });
    }
  },

  // Invalidate notification queries
  invalidateNotificationQueries: (queryClient: QueryClient) => {
    queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.NOTIFICATIONS] });
    queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.SPECIAL_OCCASIONS] });
  },

  // Remove specific guest from cache
  removeGuestFromCache: (queryClient: QueryClient, guestId: number) => {
    queryClient.removeQueries({ 
      queryKey: [QUERY_KEYS.GUEST_DETAIL, guestId] 
    });
  },

  // Update guest in cache
  updateGuestInCache: (queryClient: QueryClient, guestId: number, updatedGuest: any) => {
    queryClient.setQueryData([QUERY_KEYS.GUEST_DETAIL, guestId], updatedGuest);
    
    // Also update in guest lists
    queryClient.setQueriesData(
      { queryKey: [QUERY_KEYS.GUESTS] },
      (oldData: any) => {
        if (!oldData) return oldData;
        
        return {
          ...oldData,
          guests: oldData.guests.map((guest: any) => 
            guest.id === guestId ? { ...guest, ...updatedGuest } : guest
          ),
        };
      }
    );
  },
};

export { CACHE_CONFIG };