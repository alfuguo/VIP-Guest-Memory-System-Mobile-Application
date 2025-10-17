import { login, logout, validateToken, refreshAuthToken } from '../authService';
import { api } from '../api';
import { storeToken, storeRefreshToken, removeToken, removeRefreshToken } from '../../utils/tokenStorage';

// Mock dependencies
jest.mock('../api');
jest.mock('../../utils/tokenStorage');

const mockApi = api as jest.Mocked<typeof api>;
const mockStoreToken = storeToken as jest.MockedFunction<typeof storeToken>;
const mockStoreRefreshToken = storeRefreshToken as jest.MockedFunction<typeof storeRefreshToken>;
const mockRemoveToken = removeToken as jest.MockedFunction<typeof removeToken>;
const mockRemoveRefreshToken = removeRefreshToken as jest.MockedFunction<typeof removeRefreshToken>;

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('login', () => {
    it('should login successfully and store tokens', async () => {
      const mockResponse = {
        data: {
          user: {
            id: 1,
            email: 'test@example.com',
            firstName: 'Test',
            lastName: 'User',
            role: 'SERVER',
          },
          token: 'auth-token',
          refreshToken: 'refresh-token',
        },
      };

      mockApi.post.mockResolvedValue(mockResponse);
      mockStoreToken.mockResolvedValue();
      mockStoreRefreshToken.mockResolvedValue();

      const result = await login('test@example.com', 'password');

      expect(mockApi.post).toHaveBeenCalledWith('/auth/login', {
        email: 'test@example.com',
        password: 'password',
      });
      expect(mockStoreToken).toHaveBeenCalledWith('auth-token');
      expect(mockStoreRefreshToken).toHaveBeenCalledWith('refresh-token');
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle login failure', async () => {
      const error = new Error('Invalid credentials');
      mockApi.post.mockRejectedValue(error);

      await expect(login('test@example.com', 'wrong-password')).rejects.toThrow('Invalid credentials');
      expect(mockStoreToken).not.toHaveBeenCalled();
      expect(mockStoreRefreshToken).not.toHaveBeenCalled();
    });

    it('should handle token storage failure', async () => {
      const mockResponse = {
        data: {
          user: { id: 1, email: 'test@example.com', firstName: 'Test', lastName: 'User', role: 'SERVER' },
          token: 'auth-token',
          refreshToken: 'refresh-token',
        },
      };

      mockApi.post.mockResolvedValue(mockResponse);
      mockStoreToken.mockRejectedValue(new Error('Storage failed'));

      await expect(login('test@example.com', 'password')).rejects.toThrow('Storage failed');
    });
  });

  describe('logout', () => {
    it('should logout successfully and remove tokens', async () => {
      mockApi.post.mockResolvedValue({ data: { message: 'Logged out successfully' } });
      mockRemoveToken.mockResolvedValue();
      mockRemoveRefreshToken.mockResolvedValue();

      await logout();

      expect(mockApi.post).toHaveBeenCalledWith('/auth/logout');
      expect(mockRemoveToken).toHaveBeenCalled();
      expect(mockRemoveRefreshToken).toHaveBeenCalled();
    });

    it('should remove tokens even if API call fails', async () => {
      mockApi.post.mockRejectedValue(new Error('Network error'));
      mockRemoveToken.mockResolvedValue();
      mockRemoveRefreshToken.mockResolvedValue();

      await logout();

      expect(mockRemoveToken).toHaveBeenCalled();
      expect(mockRemoveRefreshToken).toHaveBeenCalled();
    });

    it('should handle token removal failure', async () => {
      mockApi.post.mockResolvedValue({ data: { message: 'Logged out successfully' } });
      mockRemoveToken.mockRejectedValue(new Error('Removal failed'));

      await expect(logout()).rejects.toThrow('Removal failed');
    });
  });

  describe('validateToken', () => {
    it('should validate token successfully', async () => {
      const mockUser = {
        id: 1,
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        role: 'SERVER',
      };

      mockApi.get.mockResolvedValue({ data: mockUser });

      const result = await validateToken('valid-token');

      expect(mockApi.get).toHaveBeenCalledWith('/auth/profile');
      expect(result).toEqual(mockUser);
    });

    it('should handle invalid token', async () => {
      const error = new Error('Invalid token');
      mockApi.get.mockRejectedValue(error);

      await expect(validateToken('invalid-token')).rejects.toThrow('Invalid token');
    });
  });

  describe('refreshAuthToken', () => {
    it('should refresh token successfully', async () => {
      const mockResponse = {
        data: {
          token: 'new-auth-token',
          refreshToken: 'new-refresh-token',
        },
      };

      mockApi.post.mockResolvedValue(mockResponse);
      mockStoreToken.mockResolvedValue();
      mockStoreRefreshToken.mockResolvedValue();

      const result = await refreshAuthToken('old-refresh-token');

      expect(mockApi.post).toHaveBeenCalledWith('/auth/refresh', {
        refreshToken: 'old-refresh-token',
      });
      expect(mockStoreToken).toHaveBeenCalledWith('new-auth-token');
      expect(mockStoreRefreshToken).toHaveBeenCalledWith('new-refresh-token');
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle refresh token failure', async () => {
      const error = new Error('Refresh token expired');
      mockApi.post.mockRejectedValue(error);

      await expect(refreshAuthToken('expired-refresh-token')).rejects.toThrow('Refresh token expired');
      expect(mockStoreToken).not.toHaveBeenCalled();
      expect(mockStoreRefreshToken).not.toHaveBeenCalled();
    });
  });
});