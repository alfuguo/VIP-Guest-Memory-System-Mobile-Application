import AsyncStorage from '@react-native-async-storage/async-storage';
import { tokenStorage } from '../tokenStorage';

// Mock AsyncStorage
jest.mock('@react-native-async-storage/async-storage', () => ({
  setItem: jest.fn(),
  getItem: jest.fn(),
  removeItem: jest.fn(),
  multiRemove: jest.fn(),
  multiGet: jest.fn(),
}));

const mockAsyncStorage = AsyncStorage as jest.Mocked<typeof AsyncStorage>;

describe('tokenStorage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('setToken', () => {
    it('should store token in AsyncStorage', async () => {
      const token = 'test-token';
      mockAsyncStorage.setItem.mockResolvedValue();

      await tokenStorage.setToken(token);

      expect(mockAsyncStorage.setItem).toHaveBeenCalledWith('auth_token', token);
    });

    it('should handle storage errors gracefully', async () => {
      const token = 'test-token';
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      mockAsyncStorage.setItem.mockRejectedValue(new Error('Storage error'));

      await tokenStorage.setToken(token);

      expect(consoleSpy).toHaveBeenCalledWith('Error setting token:', expect.any(Error));
      consoleSpy.mockRestore();
    });
  });

  describe('getToken', () => {
    it('should retrieve token from AsyncStorage', async () => {
      const token = 'test-token';
      mockAsyncStorage.getItem.mockResolvedValue(token);

      const result = await tokenStorage.getToken();

      expect(mockAsyncStorage.getItem).toHaveBeenCalledWith('auth_token');
      expect(result).toBe(token);
    });

    it('should return null when no token exists', async () => {
      mockAsyncStorage.getItem.mockResolvedValue(null);

      const result = await tokenStorage.getToken();

      expect(result).toBeNull();
    });

    it('should handle retrieval errors gracefully', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      mockAsyncStorage.getItem.mockRejectedValue(new Error('Retrieval error'));

      const result = await tokenStorage.getToken();

      expect(result).toBeNull();
      expect(consoleSpy).toHaveBeenCalledWith('Error getting token:', expect.any(Error));
      consoleSpy.mockRestore();
    });
  });

  describe('refresh token operations', () => {
    it('should store refresh token', async () => {
      const refreshToken = 'test-refresh-token';
      mockAsyncStorage.setItem.mockResolvedValue();

      await tokenStorage.setRefreshToken(refreshToken);

      expect(mockAsyncStorage.setItem).toHaveBeenCalledWith('refresh_token', refreshToken);
    });

    it('should retrieve refresh token', async () => {
      const refreshToken = 'test-refresh-token';
      mockAsyncStorage.getItem.mockResolvedValue(refreshToken);

      const result = await tokenStorage.getRefreshToken();

      expect(mockAsyncStorage.getItem).toHaveBeenCalledWith('refresh_token');
      expect(result).toBe(refreshToken);
    });
  });

  describe('user data operations', () => {
    it('should store user data', async () => {
      const user = { id: 1, name: 'Test User' };
      mockAsyncStorage.setItem.mockResolvedValue();

      await tokenStorage.setUser(user);

      expect(mockAsyncStorage.setItem).toHaveBeenCalledWith('user_data', JSON.stringify(user));
    });

    it('should retrieve user data', async () => {
      const user = { id: 1, name: 'Test User' };
      mockAsyncStorage.getItem.mockResolvedValue(JSON.stringify(user));

      const result = await tokenStorage.getUser();

      expect(mockAsyncStorage.getItem).toHaveBeenCalledWith('user_data');
      expect(result).toEqual(user);
    });

    it('should return null when no user data exists', async () => {
      mockAsyncStorage.getItem.mockResolvedValue(null);

      const result = await tokenStorage.getUser();

      expect(result).toBeNull();
    });

    it('should handle invalid JSON gracefully', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      mockAsyncStorage.getItem.mockResolvedValue('invalid-json');

      const result = await tokenStorage.getUser();

      expect(result).toBeNull();
      expect(consoleSpy).toHaveBeenCalledWith('Error getting user data:', expect.any(Error));
      consoleSpy.mockRestore();
    });
  });

  describe('clearAll', () => {
    it('should clear all auth data', async () => {
      mockAsyncStorage.multiRemove.mockResolvedValue();

      await tokenStorage.clearAll();

      expect(mockAsyncStorage.multiRemove).toHaveBeenCalledWith([
        'auth_token',
        'refresh_token',
        'user_data',
      ]);
    });

    it('should handle clear errors gracefully', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      mockAsyncStorage.multiRemove.mockRejectedValue(new Error('Clear error'));

      await tokenStorage.clearAll();

      expect(consoleSpy).toHaveBeenCalledWith('Error clearing auth data:', expect.any(Error));
      consoleSpy.mockRestore();
    });
  });

  describe('getAllAuthData', () => {
    it('should retrieve all auth data', async () => {
      const token = 'test-token';
      const refreshToken = 'test-refresh-token';
      const user = { id: 1, name: 'Test User' };

      mockAsyncStorage.multiGet.mockResolvedValue([
        ['auth_token', token],
        ['refresh_token', refreshToken],
        ['user_data', JSON.stringify(user)],
      ]);

      const result = await tokenStorage.getAllAuthData();

      expect(mockAsyncStorage.multiGet).toHaveBeenCalledWith([
        'auth_token',
        'refresh_token',
        'user_data',
      ]);
      expect(result).toEqual({
        token,
        refreshToken,
        user,
      });
    });

    it('should handle missing data', async () => {
      mockAsyncStorage.multiGet.mockResolvedValue([
        ['auth_token', null],
        ['refresh_token', null],
        ['user_data', null],
      ]);

      const result = await tokenStorage.getAllAuthData();

      expect(result).toEqual({
        token: null,
        refreshToken: null,
        user: null,
      });
    });

    it('should handle errors gracefully', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      mockAsyncStorage.multiGet.mockRejectedValue(new Error('MultiGet error'));

      const result = await tokenStorage.getAllAuthData();

      expect(result).toEqual({
        token: null,
        refreshToken: null,
        user: null,
      });
      expect(consoleSpy).toHaveBeenCalledWith('Error getting all auth data:', expect.any(Error));
      consoleSpy.mockRestore();
    });
  });
});