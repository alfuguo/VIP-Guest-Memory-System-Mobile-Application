import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useNetworkStatus } from '../contexts/NetworkContext';
import { offlineQueue, QueuedRequest } from '../utils/offlineQueue';
import { offlineStorage } from '../config/queryClient';

export interface OfflineState {
  isOffline: boolean;
  queuedRequests: QueuedRequest[];
  queueStats: {
    total: number;
    high: number;
    medium: number;
    low: number;
  };
  isProcessingQueue: boolean;
}

export const useOffline = () => {
  const { isOffline } = useNetworkStatus();
  const queryClient = useQueryClient();
  const [queuedRequests, setQueuedRequests] = useState<QueuedRequest[]>([]);
  const [isProcessingQueue, setIsProcessingQueue] = useState(false);

  // Subscribe to queue changes
  useEffect(() => {
    const unsubscribe = offlineQueue.subscribe((queue) => {
      setQueuedRequests(queue);
    });

    // Load initial queue state
    setQueuedRequests(offlineQueue.getQueue());

    return unsubscribe;
  }, []);

  // Process queue when coming back online
  useEffect(() => {
    if (!isOffline && queuedRequests.length > 0) {
      processOfflineQueue();
    }
  }, [isOffline, queuedRequests.length]);

  const processOfflineQueue = async () => {
    if (isProcessingQueue) return;

    setIsProcessingQueue(true);
    try {
      await offlineQueue.processQueue();
      
      // Invalidate all queries to refresh data after processing queue
      queryClient.invalidateQueries();
    } catch (error) {
      console.error('Failed to process offline queue:', error);
    } finally {
      setIsProcessingQueue(false);
    }
  };

  const clearOfflineQueue = async () => {
    await offlineQueue.clearQueue();
  };

  const getQueueStats = () => {
    return offlineQueue.getQueueStats();
  };

  // Helper function to queue a request when offline
  const queueRequest = async (
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    endpoint: string,
    data?: any,
    options?: {
      priority?: 'HIGH' | 'MEDIUM' | 'LOW';
      description?: string;
      maxRetries?: number;
    }
  ) => {
    return offlineQueue.addRequest({
      method,
      endpoint,
      data,
      priority: options?.priority || 'MEDIUM',
      description: options?.description || `${method} ${endpoint}`,
      maxRetries: options?.maxRetries || 3,
    });
  };

  const offlineState: OfflineState = {
    isOffline,
    queuedRequests,
    queueStats: getQueueStats(),
    isProcessingQueue,
  };

  return {
    ...offlineState,
    processOfflineQueue,
    clearOfflineQueue,
    queueRequest,
  };
};

// Hook for components that need to show offline status
export const useOfflineStatus = () => {
  const { isOffline, queueStats } = useOffline();
  
  return {
    isOffline,
    hasQueuedRequests: queueStats.total > 0,
    queueCount: queueStats.total,
    highPriorityCount: queueStats.high,
  };
};

// Hook for managing recently viewed guests offline storage
export const useRecentlyViewedGuests = () => {
  const [recentGuests, setRecentGuests] = useState<any[]>([]);
  const MAX_RECENT_GUESTS = 20;

  useEffect(() => {
    loadRecentGuests();
  }, []);

  const loadRecentGuests = async () => {
    try {
      const recent = await offlineStorage.getItem('recently_viewed_guests');
      if (recent && Array.isArray(recent)) {
        setRecentGuests(recent);
      }
    } catch (error) {
      console.error('Failed to load recently viewed guests:', error);
    }
  };

  const addRecentGuest = async (guest: any) => {
    try {
      const updatedRecent = [
        guest,
        ...recentGuests.filter(g => g.id !== guest.id)
      ].slice(0, MAX_RECENT_GUESTS);
      
      setRecentGuests(updatedRecent);
      await offlineStorage.setItem('recently_viewed_guests', updatedRecent);
    } catch (error) {
      console.error('Failed to add recent guest:', error);
    }
  };

  const clearRecentGuests = async () => {
    try {
      setRecentGuests([]);
      await offlineStorage.removeItem('recently_viewed_guests');
    } catch (error) {
      console.error('Failed to clear recent guests:', error);
    }
  };

  return {
    recentGuests,
    addRecentGuest,
    clearRecentGuests,
  };
};

// Hook for offline-aware mutations
export const useOfflineMutation = () => {
  const { isOffline } = useNetworkStatus();
  const { queueRequest } = useOffline();

  const mutateWithOfflineSupport = async (
    mutationFn: () => Promise<any>,
    offlineOptions: {
      method: 'POST' | 'PUT' | 'DELETE';
      endpoint: string;
      data?: any;
      priority?: 'HIGH' | 'MEDIUM' | 'LOW';
      description: string;
    }
  ) => {
    if (isOffline) {
      // Queue the request for later processing
      const requestId = await queueRequest(
        offlineOptions.method,
        offlineOptions.endpoint,
        offlineOptions.data,
        {
          priority: offlineOptions.priority,
          description: offlineOptions.description,
        }
      );
      
      console.log(`Queued ${offlineOptions.description} for offline processing`);
      return { queued: true, requestId };
    } else {
      // Execute immediately if online
      return await mutationFn();
    }
  };

  return {
    mutateWithOfflineSupport,
    isOffline,
  };
};