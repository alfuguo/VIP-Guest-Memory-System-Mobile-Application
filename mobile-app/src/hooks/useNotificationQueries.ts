import { useQuery, useMutation, useQueryClient, UseQueryOptions } from '@tanstack/react-query';
import { 
  QUERY_KEYS, 
  CACHE_CONFIG, 
  cacheUtils, 
  offlineStorage 
} from '../config/queryClient';
import { ApiErrorTypes } from '../services/api';

// Note: These types should match your actual notification types
interface Notification {
  id: number;
  type: 'PRE_ARRIVAL' | 'SPECIAL_OCCASION' | 'RETURNING_GUEST';
  guestId: number;
  guestName: string;
  message: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  acknowledged: boolean;
  createdAt: string;
  scheduledFor?: string;
}

interface NotificationSummary {
  totalCount: number;
  unacknowledgedCount: number;
  highPriorityCount: number;
  notifications: Notification[];
}

interface SpecialOccasion {
  id: number;
  guestId: number;
  guestName: string;
  occasionType: 'BIRTHDAY' | 'ANNIVERSARY';
  occasionDate: string;
  daysUntil: number;
  notes?: string;
}

// Mock notification service - this should be replaced with actual notification service
class NotificationService {
  static async getNotifications(): Promise<NotificationSummary> {
    // This would be implemented with actual API calls
    throw new Error('NotificationService not implemented yet');
  }
  
  static async getSpecialOccasions(): Promise<SpecialOccasion[]> {
    throw new Error('NotificationService not implemented yet');
  }
  
  static async acknowledgeNotification(notificationId: number): Promise<void> {
    throw new Error('NotificationService not implemented yet');
  }
  
  static async acknowledgeAllNotifications(): Promise<void> {
    throw new Error('NotificationService not implemented yet');
  }
}

// Hook for fetching notifications
export const useNotifications = (
  options?: Partial<UseQueryOptions<NotificationSummary, Error>>
) => {
  const queryKey = [QUERY_KEYS.NOTIFICATIONS];
  
  return useQuery({
    queryKey,
    queryFn: async () => {
      try {
        const result = await NotificationService.getNotifications();
        
        // Cache notifications offline (shorter cache time due to time-sensitive nature)
        await offlineStorage.setItem('notifications', result);
        
        return result;
      } catch (error) {
        // Try to get cached data if network fails
        if (error.message === ApiErrorTypes.NETWORK_ERROR) {
          const cachedData = await offlineStorage.getItem('notifications');
          if (cachedData) {
            console.log('Using cached notifications due to network error');
            return cachedData;
          }
        }
        throw error;
      }
    },
    staleTime: CACHE_CONFIG.NOTIFICATION_STALE_TIME,
    refetchInterval: 5 * 60 * 1000, // Refetch every 5 minutes for fresh notifications
    ...options,
  });
};

// Hook for fetching special occasions
export const useSpecialOccasions = (
  options?: Partial<UseQueryOptions<SpecialOccasion[], Error>>
) => {
  const queryKey = [QUERY_KEYS.SPECIAL_OCCASIONS];
  
  return useQuery({
    queryKey,
    queryFn: async () => {
      try {
        const result = await NotificationService.getSpecialOccasions();
        
        // Cache special occasions offline
        await offlineStorage.setItem('special_occasions', result);
        
        return result;
      } catch (error) {
        // Try to get cached data if network fails
        if (error.message === ApiErrorTypes.NETWORK_ERROR) {
          const cachedData = await offlineStorage.getItem('special_occasions');
          if (cachedData) {
            console.log('Using cached special occasions due to network error');
            return cachedData;
          }
        }
        throw error;
      }
    },
    staleTime: CACHE_CONFIG.NOTIFICATION_STALE_TIME,
    ...options,
  });
};

// Mutation hook for acknowledging a notification
export const useAcknowledgeNotification = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (notificationId: number) => 
      NotificationService.acknowledgeNotification(notificationId),
    
    onMutate: async (notificationId) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [QUERY_KEYS.NOTIFICATIONS] });
      
      // Snapshot the previous value
      const previousNotifications = queryClient.getQueryData<NotificationSummary>(
        [QUERY_KEYS.NOTIFICATIONS]
      );
      
      // Optimistically update the notification
      queryClient.setQueryData<NotificationSummary>(
        [QUERY_KEYS.NOTIFICATIONS],
        (old) => {
          if (!old) return old;
          
          return {
            ...old,
            unacknowledgedCount: Math.max(0, old.unacknowledgedCount - 1),
            notifications: old.notifications.map(notification =>
              notification.id === notificationId
                ? { ...notification, acknowledged: true }
                : notification
            ),
          };
        }
      );
      
      return { previousNotifications };
    },
    
    onError: (error, notificationId, context) => {
      // Rollback on error
      if (context?.previousNotifications) {
        queryClient.setQueryData(
          [QUERY_KEYS.NOTIFICATIONS],
          context.previousNotifications
        );
      }
      console.error('Failed to acknowledge notification:', error);
    },
    
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.NOTIFICATIONS] });
    },
  });
};

// Mutation hook for acknowledging all notifications
export const useAcknowledgeAllNotifications = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: () => NotificationService.acknowledgeAllNotifications(),
    
    onMutate: async () => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [QUERY_KEYS.NOTIFICATIONS] });
      
      // Snapshot the previous value
      const previousNotifications = queryClient.getQueryData<NotificationSummary>(
        [QUERY_KEYS.NOTIFICATIONS]
      );
      
      // Optimistically update all notifications
      queryClient.setQueryData<NotificationSummary>(
        [QUERY_KEYS.NOTIFICATIONS],
        (old) => {
          if (!old) return old;
          
          return {
            ...old,
            unacknowledgedCount: 0,
            notifications: old.notifications.map(notification => ({
              ...notification,
              acknowledged: true,
            })),
          };
        }
      );
      
      return { previousNotifications };
    },
    
    onError: (error, _, context) => {
      // Rollback on error
      if (context?.previousNotifications) {
        queryClient.setQueryData(
          [QUERY_KEYS.NOTIFICATIONS],
          context.previousNotifications
        );
      }
      console.error('Failed to acknowledge all notifications:', error);
    },
    
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.NOTIFICATIONS] });
    },
  });
};

// Hook to get notification count for badges
export const useNotificationCount = () => {
  const { data: notifications } = useNotifications({
    select: (data) => ({
      total: data.totalCount,
      unacknowledged: data.unacknowledgedCount,
      highPriority: data.highPriorityCount,
    }),
  });
  
  return notifications || { total: 0, unacknowledged: 0, highPriority: 0 };
};

// Hook to prefetch notifications
export const usePrefetchNotifications = () => {
  const queryClient = useQueryClient();
  
  return () => {
    queryClient.prefetchQuery({
      queryKey: [QUERY_KEYS.NOTIFICATIONS],
      queryFn: () => NotificationService.getNotifications(),
      staleTime: CACHE_CONFIG.NOTIFICATION_STALE_TIME,
    });
  };
};