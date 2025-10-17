import React from 'react';
import { render, fireEvent, waitFor } from '../../__tests__/test-utils';
import { AuthProvider, useAuth } from '../AuthContext';
import { Text, TouchableOpacity } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as authService from '../../services/authService';

// Mock the auth service
jest.mock('../../services/authService');
const mockAuthService = authService as jest.Mocked<typeof authService>;

// Test component to interact with AuthContext
const TestComponent = () => {
  const { user, login, logout, isLoading } = useAuth();

  return (
    <>
      <Text testID="user-info">
        {user ? `${user.firstName} ${user.lastName}` : 'Not logged in'}
      </Text>
      <Text testID="loading-state">{isLoading ? 'Loading' : 'Not loading'}</Text>
      <TouchableOpacity
        testID="login-button"
        onPress={() => login('test@example.com', 'password')}
      >
        <Text>Login</Text>
      </TouchableOpacity>
      <TouchableOpacity testID="logout-button" onPress={logout}>
        <Text>Logout</Text>
      </TouchableOpacity>
    </>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    AsyncStorage.clear();
  });

  it('should provide initial state with no user', () => {
    const { getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(getByTestId('user-info')).toHaveTextContent('Not logged in');
    expect(getByTestId('loading-state')).toHaveTextContent('Not loading');
  });

  it('should handle successful login', async () => {
    const mockUser = {
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      role: 'SERVER' as const,
    };

    const mockResponse = {
      user: mockUser,
      token: 'mock-token',
      refreshToken: 'mock-refresh-token',
    };

    mockAuthService.login.mockResolvedValue(mockResponse);

    const { getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    fireEvent.press(getByTestId('login-button'));

    // Should show loading state
    expect(getByTestId('loading-state')).toHaveTextContent('Loading');

    await waitFor(() => {
      expect(getByTestId('user-info')).toHaveTextContent('Test User');
      expect(getByTestId('loading-state')).toHaveTextContent('Not loading');
    });

    // Verify token was stored
    expect(AsyncStorage.setItem).toHaveBeenCalledWith('authToken', 'mock-token');
    expect(AsyncStorage.setItem).toHaveBeenCalledWith('refreshToken', 'mock-refresh-token');
  });

  it('should handle login failure', async () => {
    mockAuthService.login.mockRejectedValue(new Error('Invalid credentials'));

    const { getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    fireEvent.press(getByTestId('login-button'));

    await waitFor(() => {
      expect(getByTestId('user-info')).toHaveTextContent('Not logged in');
      expect(getByTestId('loading-state')).toHaveTextContent('Not loading');
    });

    // Verify no token was stored
    expect(AsyncStorage.setItem).not.toHaveBeenCalledWith('authToken', expect.any(String));
  });

  it('should handle logout', async () => {
    const mockUser = {
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      role: 'SERVER' as const,
    };

    const mockResponse = {
      user: mockUser,
      token: 'mock-token',
      refreshToken: 'mock-refresh-token',
    };

    mockAuthService.login.mockResolvedValue(mockResponse);

    const { getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    // Login first
    fireEvent.press(getByTestId('login-button'));
    await waitFor(() => {
      expect(getByTestId('user-info')).toHaveTextContent('Test User');
    });

    // Then logout
    fireEvent.press(getByTestId('logout-button'));

    await waitFor(() => {
      expect(getByTestId('user-info')).toHaveTextContent('Not logged in');
    });

    // Verify tokens were removed
    expect(AsyncStorage.removeItem).toHaveBeenCalledWith('authToken');
    expect(AsyncStorage.removeItem).toHaveBeenCalledWith('refreshToken');
  });

  it('should restore user session on app start', async () => {
    const mockUser = {
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      role: 'SERVER' as const,
    };

    // Mock stored token
    (AsyncStorage.getItem as jest.Mock).mockImplementation((key) => {
      if (key === 'authToken') return Promise.resolve('stored-token');
      if (key === 'refreshToken') return Promise.resolve('stored-refresh-token');
      return Promise.resolve(null);
    });

    mockAuthService.validateToken.mockResolvedValue(mockUser);

    const { getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(getByTestId('user-info')).toHaveTextContent('Test User');
    });

    expect(mockAuthService.validateToken).toHaveBeenCalledWith('stored-token');
  });
});