import { apiClient, ApiErrorTypes } from './api';
import { LoginRequest, LoginResponse } from '../types/auth';

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      return await apiClient.post<LoginResponse>('/auth/login', credentials, false);
    } catch (error) {
      // Transform API errors to user-friendly messages
      if (error instanceof Error) {
        switch (error.message) {
          case 'HTTP_401: Unauthorized':
            throw new Error('Invalid email or password');
          case 'HTTP_403: Forbidden':
            throw new Error('Account is locked or disabled');
          case 'HTTP_429: Too Many Requests':
            throw new Error('Too many login attempts. Please try again later');
          case ApiErrorTypes.NETWORK_ERROR:
            throw new Error('Network connection failed. Please check your internet connection');
          case ApiErrorTypes.REQUEST_TIMEOUT:
            throw new Error('Request timed out. Please try again');
          case ApiErrorTypes.SERVER_ERROR:
            throw new Error('Server error. Please try again later');
          default:
            throw new Error('Login failed. Please try again');
        }
      }
      throw error;
    }
  },

  async refreshToken(refreshToken: string): Promise<{ token: string; refreshToken: string }> {
    try {
      return await apiClient.post<{ token: string; refreshToken: string }>(
        '/auth/refresh',
        { refreshToken },
        false
      );
    } catch (error) {
      // If refresh fails, the token is invalid
      throw new Error('Session expired. Please log in again');
    }
  },

  async logout(): Promise<void> {
    try {
      await apiClient.post<void>('/auth/logout');
    } catch (error) {
      // Even if logout API fails, we should clear local data
      console.warn('Logout API call failed, but continuing with local cleanup:', error);
    }
  },

  async validateToken(): Promise<boolean> {
    try {
      await apiClient.get<{ valid: boolean }>('/auth/validate');
      return true;
    } catch (error) {
      return false;
    }
  },

  async getCurrentUser(): Promise<any> {
    try {
      return await apiClient.get<any>('/auth/profile');
    } catch (error) {
      throw new Error('Failed to fetch user profile');
    }
  },
};