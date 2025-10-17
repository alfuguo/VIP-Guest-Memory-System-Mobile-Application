import { authService } from '../authService';
import { apiClient } from '../api';
import { tokenStorage } from '../../utils/tokenStorage';

// Mock dependencies
jest.mock('../api');
jest.mock('../../utils/tokenStorage');

const mockApiClient = apiClient as jest.Mocked<typeof apiClient>;
const mockTokenStorage = tokenStorage as jest.Mocked<typeof tokenStorage>;

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('login', () => {
    it('should login successfully', async () => {
      const mockCredentials = {
        email: 'test@example.com',
        password: 'password123',
      };

      const mockResponse = {
        user: {
          id: 1,
          email: 'test@example.com',
          firstName: 'Test',
          lastName: 'User',
          role: 'SERVER',
        },
        token: 'auth-token',
        refreshToken: 'refresh-token',
      };

      mockApiClient.post.mockResolvedValue(mockResponse);

      const result = await authService.login(mockCredentials);

      expect(mockApiClient.post).toHaveBeenCalledWith('/auth/login', mockCredentials, false);
      expect(result).toEqual(mockResponse);
    });

    it('should handle login failure with 401 error', async () => {
      const mockCredentials = {
        email: 'test@example.com',
        password: 'wrong-password',
      };

      const error = new Error('HTTP_401: Unauthorized');
      mockApiClient.post.mockRejectedValue(error);

      await expect(authService.login(mockCredentials)).rejects.toThrow('Invalid email or password');
    });

    it('should handle network error', async () => {
      const mockCredentials = {
        email: 'test@example.com',
        password: 'password123',
      };

      const error = new Error('NETWORK_ERROR');
      mockApiClient.post.mockRejectedValue(error);

      await expect(authService.login(mockCredentials)).rejects.toThrow('Network connection failed. Please check your internet connection');
    });
  });

  describe('logout', () => {
    it('should logout successfully', async () => {
      mockApiClient.post.mockResolvedValue(undefined);

      await authService.logout();

      expect(mockApiClient.post).toHaveBeenCalledWith('/auth/logout');
    });

    it('should handle logout API failure gracefully', async () => {
      const error = new Error('Network error');
      mockApiClient.post.mockRejectedValue(error);

      // Should not throw error even if API call fails
      await expect(authService.logout()).resolves.toBeUndefined();
    });
  });

  describe('validateToken', () => {
    it('should validate token successfully', async () => {
      mockApiClient.get.mockResolvedValue({ valid: true });

      const result = await authService.validateToken();

      expect(mockApiClient.get).toHaveBeenCalledWith('/auth/validate');
      expect(result).toBe(true);
    });

    it('should handle invalid token', async () => {
      const error = new Error('Invalid token');
      mockApiClient.get.mockRejectedValue(error);

      const result = await authService.validateToken();

      expect(result).toBe(false);
    });
  });

  describe('refreshToken', () => {
    it('should refresh token successfully', async () => {
      const mockRefreshToken = 'old-refresh-token';
      const mockResponse = {
        token: 'new-auth-token',
        refreshToken: 'new-refresh-token',
      };

      mockApiClient.post.mockResolvedValue(mockResponse);

      const result = await authService.refreshToken(mockRefreshToken);

      expect(mockApiClient.post).toHaveBeenCalledWith('/auth/refresh', { refreshToken: mockRefreshToken }, false);
      expect(result).toEqual(mockResponse);
    });

    it('should handle refresh token failure', async () => {
      const mockRefreshToken = 'expired-refresh-token';
      const error = new Error('Refresh token expired');
      mockApiClient.post.mockRejectedValue(error);

      await expect(authService.refreshToken(mockRefreshToken)).rejects.toThrow('Session expired. Please log in again');
    });
  });

  describe('getCurrentUser', () => {
    it('should get current user successfully', async () => {
      const mockUser = {
        id: 1,
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        role: 'SERVER',
      };

      mockApiClient.get.mockResolvedValue(mockUser);

      const result = await authService.getCurrentUser();

      expect(mockApiClient.get).toHaveBeenCalledWith('/auth/profile');
      expect(result).toEqual(mockUser);
    });

    it('should handle get user failure', async () => {
      const error = new Error('Unauthorized');
      mockApiClient.get.mockRejectedValue(error);

      await expect(authService.getCurrentUser()).rejects.toThrow('Failed to fetch user profile');
    });
  });
});