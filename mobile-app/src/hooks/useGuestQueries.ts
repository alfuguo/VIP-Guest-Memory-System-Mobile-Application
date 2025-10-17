import { useQuery, useMutation, useQueryClient, UseQueryOptions } from '@tanstack/react-query';
import { GuestService } from '../services/guestService';
import { 
  Guest, 
  GuestListResponse, 
  GuestSearchParams, 
  CreateGuestRequest, 
  UpdateGuestRequest 
} from '../types/guest';
import { 
  QUERY_KEYS, 
  CACHE_CONFIG, 
  cacheUtils, 
  offlineStorage 
} from '../config/queryClient';
import { ApiErrorTypes } from '../services/api';

// Hook for fetching guest list with search and pagination
export const useGuests = (
  params: GuestSearchParams = {},
  options?: Partial<UseQueryOptions<GuestListResponse, Error>>
) => {
  const queryKey = [QUERY_KEYS.GUESTS, params];
  
  return useQuery({
    queryKey,
    queryFn: async () => {
      try {
        const result = await GuestService.getGuests(params);
        
        // Cache the result offline for later use
        await offlineStorage.setItem(
          `guests_${JSON.stringify(params)}`, 
          result
        );
        
        return result;
      } catch (error: any) {
        // Try to get cached data if network fails
        if (error.message === ApiErrorTypes.NETWORK_ERROR) {
          const cachedData = await offlineStorage.getItem(
            `guests_${JSON.stringify(params)}`
          );
          if (cachedData) {
            console.log('Using cached guest data due to network error');
            return cachedData;
          }
        }
        throw error;
      }
    },
    staleTime: CACHE_CONFIG.GUEST_LIST_STALE_TIME,
    ...options,
  });
};

// Hook for fetching a single guest
export const useGuest = (
  guestId: number,
  options?: Partial<UseQueryOptions<Guest, Error>>
) => {
  const queryKey = [QUERY_KEYS.GUEST_DETAIL, guestId];
  
  return useQuery({
    queryKey,
    queryFn: async () => {
      try {
        const result = await GuestService.getGuest(guestId);
        
        // Cache the guest data offline
        await offlineStorage.setItem(`guest_${guestId}`, result);
        
        return result;
      } catch (error: any) {
        // Try to get cached data if network fails
        if (error.message === ApiErrorTypes.NETWORK_ERROR) {
          const cachedData = await offlineStorage.getItem(`guest_${guestId}`);
          if (cachedData) {
            console.log(`Using cached data for guest ${guestId} due to network error`);
            return cachedData;
          }
        }
        throw error;
      }
    },
    staleTime: CACHE_CONFIG.GUEST_STALE_TIME,
    enabled: !!guestId,
    ...options,
  });
};

// Hook for guest search (with debouncing handled by the component)
export const useGuestSearch = (
  query: string,
  limit: number = 10,
  options?: Partial<UseQueryOptions<Guest[], Error>>
) => {
  const queryKey = [QUERY_KEYS.GUEST_SEARCH, query, limit];
  
  return useQuery({
    queryKey,
    queryFn: async () => {
      if (!query.trim()) return [];
      
      try {
        const result = await GuestService.searchGuests(query, limit);
        
        // Cache search results
        await offlineStorage.setItem(
          `search_${query}_${limit}`, 
          result
        );
        
        return result;
      } catch (error: any) {
        // Try to get cached search results if network fails
        if (error.message === ApiErrorTypes.NETWORK_ERROR) {
          const cachedData = await offlineStorage.getItem(
            `search_${query}_${limit}`
          );
          if (cachedData) {
            console.log('Using cached search results due to network error');
            return cachedData;
          }
        }
        throw error;
      }
    },
    staleTime: CACHE_CONFIG.GUEST_LIST_STALE_TIME,
    enabled: query.trim().length > 0,
    ...options,
  });
};

// Mutation hook for creating a guest with offline support
export const useCreateGuest = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async (guestData: CreateGuestRequest) => {
      try {
        return await GuestService.createGuest(guestData);
      } catch (error: any) {
        // If network error, queue the request
        if (error.message === ApiErrorTypes.NETWORK_ERROR) {
          const { offlineQueue } = await import('../utils/offlineQueue');
          await offlineQueue.queueGuestCreation(guestData);
          
          // Return a temporary guest object for optimistic updates
          const tempGuest = {
            id: Date.now(), // Temporary ID
            ...guestData,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            visitCount: 0,
            lastVisit: null,
            _isTemporary: true, // Flag to indicate this is a temporary object
          };
          
          return tempGuest;
        }
        throw error;
      }
    },
    
    onSuccess: (newGuest) => {
      // Invalidate and refetch guest lists
      cacheUtils.invalidateGuestQueries(queryClient);
      
      // Add the new guest to cache (even if temporary)
      queryClient.setQueryData(
        [QUERY_KEYS.GUEST_DETAIL, newGuest.id], 
        newGuest
      );
      
      // Cache offline
      offlineStorage.setItem(`guest_${newGuest.id}`, newGuest);
    },
    
    onError: (error) => {
      console.error('Failed to create guest:', error);
    },
  });
};

// Mutation hook for updating a guest with offline support
export const useUpdateGuest = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async (guestData: UpdateGuestRequest) => {
      try {
        return await GuestService.updateGuest(guestData);
      } catch (error: any) {
        // If network error, queue the request and return optimistic update
        if (error.message === ApiErrorTypes.NETWORK_ERROR) {
          const { offlineQueue } = await import('../utils/offlineQueue');
          await offlineQueue.queueGuestUpdate(guestData.id, guestData);
          
          // Return the updated data for optimistic updates
          return {
            ...guestData,
            updatedAt: new Date().toISOString(),
            _isTemporary: true,
          };
        }
        throw error;
      }
    },
    
    onMutate: async (guestData) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ 
        queryKey: [QUERY_KEYS.GUEST_DETAIL, guestData.id] 
      });
      
      // Snapshot the previous value
      const previousGuest = queryClient.getQueryData([QUERY_KEYS.GUEST_DETAIL, guestData.id]);
      
      // Optimistically update the guest
      queryClient.setQueryData([QUERY_KEYS.GUEST_DETAIL, guestData.id], (old: any) => ({
        ...old,
        ...guestData,
        updatedAt: new Date().toISOString(),
      }));
      
      return { previousGuest };
    },
    
    onSuccess: (updatedGuest) => {
      // Update the guest in cache
      cacheUtils.updateGuestInCache(queryClient, updatedGuest.id, updatedGuest);
      
      // Cache offline
      offlineStorage.setItem(`guest_${updatedGuest.id}`, updatedGuest);
    },
    
    onError: (error, guestData, context) => {
      // Rollback optimistic update on error (except for network errors)
      if (error.message !== ApiErrorTypes.NETWORK_ERROR && context?.previousGuest) {
        queryClient.setQueryData(
          [QUERY_KEYS.GUEST_DETAIL, guestData.id],
          context.previousGuest
        );
      }
      console.error('Failed to update guest:', error);
    },
  });
};

// Mutation hook for deleting a guest
export const useDeleteGuest = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (guestId: number) => GuestService.deleteGuest(guestId),
    
    onSuccess: (_, guestId) => {
      // Remove guest from cache
      cacheUtils.removeGuestFromCache(queryClient, guestId);
      
      // Invalidate guest lists
      cacheUtils.invalidateGuestQueries(queryClient);
      
      // Remove from offline cache
      offlineStorage.removeItem(`guest_${guestId}`);
    },
    
    onError: (error) => {
      console.error('Failed to delete guest:', error);
    },
  });
};

// Mutation hook for uploading guest photo
export const useUploadGuestPhoto = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ 
      guestId, 
      imageUri, 
      onProgress 
    }: { 
      guestId: number; 
      imageUri: string; 
      onProgress?: (progress: number) => void;
    }) => GuestService.uploadGuestPhoto(guestId, imageUri, onProgress),
    
    onSuccess: (result, { guestId }) => {
      // Update the guest's photo URL in cache
      queryClient.setQueryData(
        [QUERY_KEYS.GUEST_DETAIL, guestId],
        (oldGuest: Guest | undefined) => {
          if (!oldGuest) return oldGuest;
          const updatedGuest = { ...oldGuest, photoUrl: result.photoUrl };
          
          // Cache offline
          offlineStorage.setItem(`guest_${guestId}`, updatedGuest);
          
          return updatedGuest;
        }
      );
      
      // Also update in guest lists
      cacheUtils.invalidateGuestQueries(queryClient);
    },
    
    onError: (error) => {
      console.error('Failed to upload guest photo:', error);
    },
  });
};

// Prefetch utilities for better UX
export const usePrefetchGuest = () => {
  const queryClient = useQueryClient();
  
  return (guestId: number) => {
    queryClient.prefetchQuery({
      queryKey: [QUERY_KEYS.GUEST_DETAIL, guestId],
      queryFn: () => GuestService.getGuest(guestId),
      staleTime: CACHE_CONFIG.GUEST_STALE_TIME,
    });
  };
};

// Hook to get cached guest data without triggering a network request
export const useCachedGuest = (guestId: number) => {
  const queryClient = useQueryClient();
  
  return queryClient.getQueryData<Guest>([QUERY_KEYS.GUEST_DETAIL, guestId]);
};