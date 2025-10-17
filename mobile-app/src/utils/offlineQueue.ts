import AsyncStorage from '@react-native-async-storage/async-storage';
import { apiClient } from '../services/api';

export interface QueuedRequest {
  id: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';
  endpoint: string;
  data?: any;
  timestamp: number;
  retryCount: number;
  maxRetries: number;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  description: string; // Human-readable description for debugging
}

export interface QueuedMutation extends QueuedRequest {
  optimisticUpdate?: {
    queryKey: string[];
    updater: (oldData: any) => any;
  };
  onSuccess?: (result: any) => void;
  onError?: (error: any) => void;
}

const QUEUE_STORAGE_KEY = 'offline_request_queue';
const MAX_QUEUE_SIZE = 100;
const MAX_RETRY_ATTEMPTS = 3;
const RETRY_DELAY_BASE = 1000; // 1 second base delay

export class OfflineQueue {
  private static instance: OfflineQueue;
  private queue: QueuedRequest[] = [];
  private isProcessing = false;
  private listeners: Array<(queue: QueuedRequest[]) => void> = [];

  private constructor() {
    this.loadQueue();
  }

  static getInstance(): OfflineQueue {
    if (!OfflineQueue.instance) {
      OfflineQueue.instance = new OfflineQueue();
    }
    return OfflineQueue.instance;
  }

  // Load queue from storage
  private async loadQueue(): Promise<void> {
    try {
      const storedQueue = await AsyncStorage.getItem(QUEUE_STORAGE_KEY);
      if (storedQueue) {
        this.queue = JSON.parse(storedQueue);
        console.log(`Loaded ${this.queue.length} requests from offline queue`);
      }
    } catch (error) {
      console.error('Failed to load offline queue:', error);
      this.queue = [];
    }
  }

  // Save queue to storage
  private async saveQueue(): Promise<void> {
    try {
      await AsyncStorage.setItem(QUEUE_STORAGE_KEY, JSON.stringify(this.queue));
    } catch (error) {
      console.error('Failed to save offline queue:', error);
    }
  }

  // Add request to queue
  async addRequest(request: Omit<QueuedRequest, 'id' | 'timestamp' | 'retryCount'>): Promise<string> {
    const queuedRequest: QueuedRequest = {
      ...request,
      id: `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      timestamp: Date.now(),
      retryCount: 0,
      maxRetries: request.maxRetries || MAX_RETRY_ATTEMPTS,
    };

    // Remove oldest requests if queue is full
    if (this.queue.length >= MAX_QUEUE_SIZE) {
      this.queue.sort((a, b) => a.timestamp - b.timestamp);
      this.queue.splice(0, this.queue.length - MAX_QUEUE_SIZE + 1);
    }

    this.queue.push(queuedRequest);
    await this.saveQueue();
    this.notifyListeners();

    console.log(`Added request to offline queue: ${request.description}`);
    return queuedRequest.id;
  }

  // Remove request from queue
  async removeRequest(requestId: string): Promise<void> {
    const index = this.queue.findIndex(req => req.id === requestId);
    if (index !== -1) {
      const removed = this.queue.splice(index, 1)[0];
      await this.saveQueue();
      this.notifyListeners();
      console.log(`Removed request from offline queue: ${removed.description}`);
    }
  }

  // Get all queued requests
  getQueue(): QueuedRequest[] {
    return [...this.queue];
  }

  // Get queue count by priority
  getQueueStats(): { total: number; high: number; medium: number; low: number } {
    return {
      total: this.queue.length,
      high: this.queue.filter(req => req.priority === 'HIGH').length,
      medium: this.queue.filter(req => req.priority === 'MEDIUM').length,
      low: this.queue.filter(req => req.priority === 'LOW').length,
    };
  }

  // Process queue when network is available
  async processQueue(): Promise<void> {
    if (this.isProcessing || this.queue.length === 0) {
      return;
    }

    this.isProcessing = true;
    console.log(`Processing offline queue with ${this.queue.length} requests`);

    // Sort by priority and timestamp
    const sortedQueue = [...this.queue].sort((a, b) => {
      const priorityOrder = { HIGH: 3, MEDIUM: 2, LOW: 1 };
      const priorityDiff = priorityOrder[b.priority] - priorityOrder[a.priority];
      if (priorityDiff !== 0) return priorityDiff;
      return a.timestamp - b.timestamp;
    });

    const results = {
      successful: 0,
      failed: 0,
      retried: 0,
    };

    for (const request of sortedQueue) {
      try {
        await this.processRequest(request);
        await this.removeRequest(request.id);
        results.successful++;
      } catch (error) {
        console.error(`Failed to process queued request: ${request.description}`, error);
        
        if (request.retryCount < request.maxRetries) {
          // Increment retry count and delay next attempt
          const updatedRequest = { ...request, retryCount: request.retryCount + 1 };
          const index = this.queue.findIndex(req => req.id === request.id);
          if (index !== -1) {
            this.queue[index] = updatedRequest;
            await this.saveQueue();
            results.retried++;
          }
        } else {
          // Max retries reached, remove from queue
          await this.removeRequest(request.id);
          results.failed++;
        }
      }

      // Small delay between requests to avoid overwhelming the server
      await new Promise(resolve => setTimeout(resolve, 100));
    }

    this.isProcessing = false;
    console.log('Offline queue processing completed:', results);
  }

  // Process individual request
  private async processRequest(request: QueuedRequest): Promise<any> {
    const delay = RETRY_DELAY_BASE * Math.pow(2, request.retryCount);
    if (request.retryCount > 0) {
      await new Promise(resolve => setTimeout(resolve, delay));
    }

    switch (request.method) {
      case 'GET':
        return await apiClient.get(request.endpoint);
      case 'POST':
        return await apiClient.post(request.endpoint, request.data);
      case 'PUT':
        return await apiClient.put(request.endpoint, request.data);
      case 'DELETE':
        return await apiClient.delete(request.endpoint);
      default:
        throw new Error(`Unsupported method: ${request.method}`);
    }
  }

  // Clear all queued requests
  async clearQueue(): Promise<void> {
    this.queue = [];
    await this.saveQueue();
    this.notifyListeners();
    console.log('Offline queue cleared');
  }

  // Subscribe to queue changes
  subscribe(listener: (queue: QueuedRequest[]) => void): () => void {
    this.listeners.push(listener);
    return () => {
      const index = this.listeners.indexOf(listener);
      if (index !== -1) {
        this.listeners.splice(index, 1);
      }
    };
  }

  // Notify listeners of queue changes
  private notifyListeners(): void {
    this.listeners.forEach(listener => listener([...this.queue]));
  }

  // Helper methods for common operations
  async queueGuestCreation(guestData: any): Promise<string> {
    return this.addRequest({
      method: 'POST',
      endpoint: '/guests',
      data: guestData,
      priority: 'HIGH',
      description: `Create guest: ${guestData.firstName} ${guestData.lastName}`,
      maxRetries: MAX_RETRY_ATTEMPTS,
    });
  }

  async queueGuestUpdate(guestId: number, guestData: any): Promise<string> {
    return this.addRequest({
      method: 'PUT',
      endpoint: `/guests/${guestId}`,
      data: guestData,
      priority: 'MEDIUM',
      description: `Update guest: ${guestData.firstName} ${guestData.lastName}`,
      maxRetries: MAX_RETRY_ATTEMPTS,
    });
  }

  async queueVisitCreation(visitData: any): Promise<string> {
    return this.addRequest({
      method: 'POST',
      endpoint: `/guests/${visitData.guestId}/visits`,
      data: visitData,
      priority: 'HIGH',
      description: `Log visit for guest ID: ${visitData.guestId}`,
      maxRetries: MAX_RETRY_ATTEMPTS,
    });
  }

  async queuePhotoUpload(guestId: number, formData: FormData): Promise<string> {
    return this.addRequest({
      method: 'POST',
      endpoint: `/guests/${guestId}/photo`,
      data: formData,
      priority: 'LOW',
      description: `Upload photo for guest ID: ${guestId}`,
      maxRetries: 2, // Fewer retries for uploads
    });
  }
}

// Export singleton instance
export const offlineQueue = OfflineQueue.getInstance();