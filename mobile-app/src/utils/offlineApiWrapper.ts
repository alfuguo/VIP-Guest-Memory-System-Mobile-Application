import { apiClient, ApiErrorTypes } from '../services/api';
import { offlineQueue } from './offlineQueue';

export interface OfflineApiOptions {
  priority?: 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
  maxRetries?: number;
  enableOptimisticUpdate?: boolean;
  optimisticData?: any;
}

/**
 * Wrapper for API calls that provides offline support
 * Automatically queues requests when offline and provides optimistic updates
 */
export class OfflineApiWrapper {
  /**
   * Make a GET request with offline caching support
   */
  static async get<T>(
    endpoint: string,
    options?: Partial<OfflineApiOptions>
  ): Promise<T> {
    try {
      return await apiClient.get<T>(endpoint);
    } catch (error) {
      if (error.message === ApiErrorTypes.NETWORK_ERROR) {
        // For GET requests, we don't queue them, just throw the error
        // The React Query hooks will handle fallback to cached data
        throw new Error('OFFLINE_GET_REQUEST');
      }
      throw error;
    }
  }

  /**
   * Make a POST request with offline queuing support
   */
  static async post<T>(
    endpoint: string,
    data: any,
    options: OfflineApiOptions
  ): Promise<T> {
    try {
      return await apiClient.post<T>(endpoint, data);
    } catch (error) {
      if (error.message === ApiErrorTypes.NETWORK_ERROR) {
        // Queue the POST request for later
        await offlineQueue.addRequest({
          method: 'POST',
          endpoint,
          data,
          priority: options.priority || 'MEDIUM',
          description: options.description,
          maxRetries: options.maxRetries || 3,
        });

        // Return optimistic data if provided
        if (options.enableOptimisticUpdate && options.optimisticData) {
          return options.optimisticData;
        }

        throw new Error('OFFLINE_POST_QUEUED');
      }
      throw error;
    }
  }

  /**
   * Make a PUT request with offline queuing support
   */
  static async put<T>(
    endpoint: string,
    data: any,
    options: OfflineApiOptions
  ): Promise<T> {
    try {
      return await apiClient.put<T>(endpoint, data);
    } catch (error) {
      if (error.message === ApiErrorTypes.NETWORK_ERROR) {
        // Queue the PUT request for later
        await offlineQueue.addRequest({
          method: 'PUT',
          endpoint,
          data,
          priority: options.priority || 'MEDIUM',
          description: options.description,
          maxRetries: options.maxRetries || 3,
        });

        // Return optimistic data if provided
        if (options.enableOptimisticUpdate && options.optimisticData) {
          return options.optimisticData;
        }

        throw new Error('OFFLINE_PUT_QUEUED');
      }
      throw error;
    }
  }

  /**
   * Make a DELETE request with offline queuing support
   */
  static async delete<T>(
    endpoint: string,
    options: OfflineApiOptions
  ): Promise<T> {
    try {
      return await apiClient.delete<T>(endpoint);
    } catch (error) {
      if (error.message === ApiErrorTypes.NETWORK_ERROR) {
        // Queue the DELETE request for later
        await offlineQueue.addRequest({
          method: 'DELETE',
          endpoint,
          data: undefined,
          priority: options.priority || 'HIGH', // Deletes are usually high priority
          description: options.description,
          maxRetries: options.maxRetries || 3,
        });

        throw new Error('OFFLINE_DELETE_QUEUED');
      }
      throw error;
    }
  }

  /**
   * Upload file with offline queuing support
   */
  static async upload<T>(
    endpoint: string,
    formData: FormData,
    options: OfflineApiOptions & { onUploadProgress?: (progress: number) => void }
  ): Promise<T> {
    try {
      return await apiClient.upload<T>(endpoint, formData, options.onUploadProgress);
    } catch (error) {
      if (error.message === ApiErrorTypes.NETWORK_ERROR) {
        // Queue the upload for later (note: FormData might not serialize well)
        await offlineQueue.addRequest({
          method: 'POST',
          endpoint,
          data: formData,
          priority: options.priority || 'LOW', // Uploads are usually lower priority
          description: options.description,
          maxRetries: options.maxRetries || 2, // Fewer retries for uploads
        });

        throw new Error('OFFLINE_UPLOAD_QUEUED');
      }
      throw error;
    }
  }
}

/**
 * Enhanced guest service with offline support
 */
export class OfflineGuestService {
  static async createGuest(guestData: any) {
    return OfflineApiWrapper.post('/guests', guestData, {
      description: `Create guest: ${guestData.firstName} ${guestData.lastName}`,
      priority: 'HIGH',
      enableOptimisticUpdate: true,
      optimisticData: {
        id: Date.now(), // Temporary ID
        ...guestData,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        visitCount: 0,
        lastVisit: null,
        _isTemporary: true,
      },
    });
  }

  static async updateGuest(guestData: any) {
    const { id, ...updateData } = guestData;
    return OfflineApiWrapper.put(`/guests/${id}`, updateData, {
      description: `Update guest: ${updateData.firstName} ${updateData.lastName}`,
      priority: 'MEDIUM',
      enableOptimisticUpdate: true,
      optimisticData: {
        ...guestData,
        updatedAt: new Date().toISOString(),
        _isTemporary: true,
      },
    });
  }

  static async deleteGuest(guestId: number) {
    return OfflineApiWrapper.delete(`/guests/${guestId}`, {
      description: `Delete guest ID: ${guestId}`,
      priority: 'HIGH',
    });
  }

  static async uploadGuestPhoto(guestId: number, formData: FormData) {
    return OfflineApiWrapper.upload(`/guests/${guestId}/photo`, formData, {
      description: `Upload photo for guest ID: ${guestId}`,
      priority: 'LOW',
    });
  }
}

/**
 * Enhanced visit service with offline support
 */
export class OfflineVisitService {
  static async createVisit(visitData: any) {
    return OfflineApiWrapper.post(`/guests/${visitData.guestId}/visits`, visitData, {
      description: `Log visit for guest ID: ${visitData.guestId}`,
      priority: 'HIGH',
      enableOptimisticUpdate: true,
      optimisticData: {
        id: Date.now(), // Temporary ID
        ...visitData,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        _isTemporary: true,
      },
    });
  }

  static async updateVisit(visitData: any) {
    const { id, ...updateData } = visitData;
    return OfflineApiWrapper.put(`/visits/${id}`, updateData, {
      description: `Update visit ID: ${id}`,
      priority: 'MEDIUM',
      enableOptimisticUpdate: true,
      optimisticData: {
        ...visitData,
        updatedAt: new Date().toISOString(),
        _isTemporary: true,
      },
    });
  }

  static async deleteVisit(visitId: number) {
    return OfflineApiWrapper.delete(`/visits/${visitId}`, {
      description: `Delete visit ID: ${visitId}`,
      priority: 'MEDIUM',
    });
  }
}