import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { tokenStorage } from '../utils/tokenStorage';

// API configuration
const API_BASE_URL = __DEV__ 
  ? 'http://localhost:8080/api' // Development backend URL
  : 'https://your-production-api.com/api'; // Production backend URL

// Request queue for handling token refresh
interface QueuedRequest {
  resolve: (value: any) => void;
  reject: (error: any) => void;
  config: AxiosRequestConfig;
}

// Enhanced API client with Axios, JWT interceptors, and automatic token refresh
export class ApiClient {
  private axiosInstance: AxiosInstance;
  private isRefreshing = false;
  private failedQueue: QueuedRequest[] = [];

  constructor() {
    // Create Axios instance with base configuration
    this.axiosInstance = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor to add JWT token
    this.axiosInstance.interceptors.request.use(
      async (config) => {
        // Skip token injection for auth endpoints that don't require it
        const skipAuth = config.headers?.['skip-auth'] === 'true';
        
        if (!skipAuth) {
          const token = await tokenStorage.getToken();
          if (token) {
            config.headers.Authorization = `Bearer ${token}`;
          }
        }

        // Remove the skip-auth header before sending request
        if (config.headers?.['skip-auth']) {
          delete config.headers['skip-auth'];
        }

        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor to handle token refresh
    this.axiosInstance.interceptors.response.use(
      (response: AxiosResponse) => {
        return response;
      },
      async (error: AxiosError) => {
        const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

        // Handle 401 errors with automatic token refresh
        if (error.response?.status === 401 && !originalRequest._retry) {
          if (this.isRefreshing) {
            // If already refreshing, queue this request
            return new Promise((resolve, reject) => {
              this.failedQueue.push({ 
                resolve: (token: string) => {
                  if (originalRequest.headers) {
                    originalRequest.headers.Authorization = `Bearer ${token}`;
                  }
                  resolve(this.axiosInstance(originalRequest));
                },
                reject,
                config: originalRequest
              });
            });
          }

          originalRequest._retry = true;
          this.isRefreshing = true;

          try {
            const newToken = await this.refreshToken();
            this.isRefreshing = false;
            this.processQueue(null, newToken);

            // Retry the original request with new token
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${newToken}`;
            }
            return this.axiosInstance(originalRequest);
          } catch (refreshError) {
            this.isRefreshing = false;
            this.processQueue(refreshError);
            
            // Clear tokens and redirect to login
            await tokenStorage.clearAll();
            throw new Error('SESSION_EXPIRED');
          }
        }

        // Transform Axios errors to consistent format
        return Promise.reject(this.transformError(error));
      }
    );
  }

  private processQueue(error: any, token: string | null = null) {
    this.failedQueue.forEach(({ resolve, reject }) => {
      if (error) {
        reject(error);
      } else if (token) {
        resolve(token);
      }
    });
    
    this.failedQueue = [];
  }

  private async refreshToken(): Promise<string> {
    try {
      const refreshToken = await tokenStorage.getRefreshToken();
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      // Make refresh request without auth header
      const response = await this.axiosInstance.post<{ token: string; refreshToken: string }>(
        '/auth/refresh',
        { refreshToken },
        { headers: { 'skip-auth': 'true' } }
      );

      const { token: newToken, refreshToken: newRefreshToken } = response.data;
      
      // Store new tokens
      await Promise.all([
        tokenStorage.setToken(newToken),
        tokenStorage.setRefreshToken(newRefreshToken),
      ]);

      return newToken;
    } catch (error) {
      // Clear tokens on refresh failure
      await tokenStorage.clearAll();
      throw error;
    }
  }

  private transformError(error: AxiosError): Error {
    if (error.code === 'ECONNABORTED') {
      return new Error('REQUEST_TIMEOUT');
    }

    if (error.code === 'ERR_NETWORK' || !error.response) {
      return new Error('NETWORK_ERROR');
    }

    const status = error.response.status;
    const message = error.response.data || error.response.statusText;

    switch (status) {
      case 401:
        return new Error('UNAUTHORIZED');
      case 403:
        return new Error('FORBIDDEN');
      case 429:
        return new Error('TOO_MANY_REQUESTS');
      case 500:
      case 502:
      case 503:
      case 504:
        return new Error('SERVER_ERROR');
      default:
        return new Error(`HTTP_${status}: ${message}`);
    }
  }

  // Convenience methods with proper typing
  async get<T>(endpoint: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.get<T>(endpoint, config);
    return response.data;
  }

  async post<T>(endpoint: string, data?: any, requireAuth = true): Promise<T> {
    const config: AxiosRequestConfig = {};
    if (!requireAuth) {
      config.headers = { 'skip-auth': 'true' };
    }
    
    const response = await this.axiosInstance.post<T>(endpoint, data, config);
    return response.data;
  }

  async put<T>(endpoint: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.put<T>(endpoint, data, config);
    return response.data;
  }

  async delete<T>(endpoint: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.delete<T>(endpoint, config);
    return response.data;
  }

  // Upload method for file uploads with multipart/form-data
  async upload<T>(endpoint: string, formData: FormData, onUploadProgress?: (progress: number) => void): Promise<T> {
    const config: AxiosRequestConfig = {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      timeout: 30000, // Longer timeout for uploads
      onUploadProgress: onUploadProgress ? (progressEvent) => {
        if (progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onUploadProgress(progress);
        }
      } : undefined,
    };

    const response = await this.axiosInstance.post<T>(endpoint, formData, config);
    return response.data;
  }

  // Method to make requests with custom configuration
  async request<T>(config: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.request<T>(config);
    return response.data;
  }

  // Method to get the Axios instance for advanced usage
  getAxiosInstance(): AxiosInstance {
    return this.axiosInstance;
  }
}

// Create and export API client instance
export const apiClient = new ApiClient();

// Export error types for consistent error handling
export const ApiErrorTypes = {
  UNAUTHORIZED: 'UNAUTHORIZED',
  FORBIDDEN: 'FORBIDDEN',
  SERVER_ERROR: 'SERVER_ERROR',
  NETWORK_ERROR: 'NETWORK_ERROR',
  REQUEST_TIMEOUT: 'REQUEST_TIMEOUT',
  SESSION_EXPIRED: 'SESSION_EXPIRED',
  TOO_MANY_REQUESTS: 'TOO_MANY_REQUESTS',
} as const;

export type ApiErrorType = typeof ApiErrorTypes[keyof typeof ApiErrorTypes];