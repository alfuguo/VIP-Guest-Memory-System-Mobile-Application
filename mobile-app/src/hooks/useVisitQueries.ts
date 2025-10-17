import { useQuery, useMutation, useQueryClient, UseQueryOptions } from '@tanstack/react-query';
import { 
  QUERY_KEYS, 
  CACHE_CONFIG, 
  cacheUtils, 
  offlineStorage 
} from '../config/queryClient';
import { ApiErrorTypes } from '../services/api';

// Note: These types would need to be defined based on your visit service
// For now, I'll use generic types that should be replaced with actual types
interface Visit {
  id: number;
  guestId: number;
  visitDate: string;
  visitTime: string;
  partySize: number;
  tableNumber?: string;
  serviceNotes?: string;
  staffName: string;
  createdAt: string;
}

interface CreateVisitRequest {
  guestId: number;
  visitDate: string;
  visitTime: string;
  partySize: number;
  tableNumber?: string;
  serviceNotes?: string;
}

interface UpdateVisitRequest extends CreateVisitRequest {
  id: number;
}

// Mock visit service - this should be replaced with actual visit service
class VisitService {
  static async getGuestVisits(guestId: number): Promise<Visit[]> {
    // This would be implemented with actual API calls
    throw new Error('VisitService not implemented yet');
  }
  
  static async createVisit(visitData: CreateVisitRequest): Promise<Visit> {
    throw new Error('VisitService not implemented yet');
  }
  
  static async updateVisit(visitData: UpdateVisitRequest): Promise<Visit> {
    throw new Error('VisitService not implemented yet');
  }
  
  static async deleteVisit(visitId: number): Promise<void> {
    throw new Error('VisitService not implemented yet');
  }
}

// Hook for fetching guest visits
export const useGuestVisits = (
  guestId: number,
  options?: Partial<UseQueryOptions<Visit[], Error>>
) => {
  const queryKey = [QUERY_KEYS.VISITS, guestId];
  
  return useQuery({
    queryKey,
    queryFn: async () => {
      try {
        const result = await VisitService.getGuestVisits(guestId);
        
        // Cache the visits offline
        await offlineStorage.setItem(`visits_${guestId}`, result);
        
        return result;
      } catch (error) {
        // Try to get cached data if network fails
        if (error.message === ApiErrorTypes.NETWORK_ERROR) {
          const cachedData = await offlineStorage.getItem(`visits_${guestId}`);
          if (cachedData) {
            console.log(`Using cached visits for guest ${guestId} due to network error`);
            return cachedData;
          }
        }
        throw error;
      }
    },
    staleTime: CACHE_CONFIG.VISIT_STALE_TIME,
    enabled: !!guestId,
    ...options,
  });
};

// Mutation hook for creating a visit
export const useCreateVisit = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (visitData: CreateVisitRequest) => 
      VisitService.createVisit(visitData),
    
    onSuccess: (newVisit) => {
      // Invalidate visit queries for this guest
      cacheUtils.invalidateVisitQueries(queryClient, newVisit.guestId);
      
      // Also invalidate guest detail to update last visit info
      cacheUtils.invalidateGuestDetail(queryClient, newVisit.guestId);
      
      // Cache offline
      offlineStorage.setItem(`visit_${newVisit.id}`, newVisit);
    },
    
    onError: (error) => {
      console.error('Failed to create visit:', error);
    },
  });
};

// Mutation hook for updating a visit
export const useUpdateVisit = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (visitData: UpdateVisitRequest) => 
      VisitService.updateVisit(visitData),
    
    onSuccess: (updatedVisit) => {
      // Invalidate visit queries for this guest
      cacheUtils.invalidateVisitQueries(queryClient, updatedVisit.guestId);
      
      // Cache offline
      offlineStorage.setItem(`visit_${updatedVisit.id}`, updatedVisit);
    },
    
    onError: (error) => {
      console.error('Failed to update visit:', error);
    },
  });
};

// Mutation hook for deleting a visit
export const useDeleteVisit = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (visitId: number) => VisitService.deleteVisit(visitId),
    
    onMutate: async (visitId) => {
      // Optimistically remove the visit from cache
      // This would require knowing which guest the visit belongs to
      // For now, we'll just invalidate all visit queries
      return { visitId };
    },
    
    onSuccess: (_, visitId) => {
      // Invalidate all visit queries since we don't know the guest ID
      cacheUtils.invalidateVisitQueries(queryClient);
      
      // Remove from offline cache
      offlineStorage.removeItem(`visit_${visitId}`);
    },
    
    onError: (error) => {
      console.error('Failed to delete visit:', error);
    },
  });
};

// Hook to prefetch guest visits
export const usePrefetchGuestVisits = () => {
  const queryClient = useQueryClient();
  
  return (guestId: number) => {
    queryClient.prefetchQuery({
      queryKey: [QUERY_KEYS.VISITS, guestId],
      queryFn: () => VisitService.getGuestVisits(guestId),
      staleTime: CACHE_CONFIG.VISIT_STALE_TIME,
    });
  };
};