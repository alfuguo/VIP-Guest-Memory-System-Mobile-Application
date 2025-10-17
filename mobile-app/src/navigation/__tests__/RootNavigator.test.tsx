import React from 'react';
import { render } from '../../__tests__/test-utils';
import RootNavigator from '../RootNavigator';
import { useAuth } from '../../contexts/AuthContext';

// Mock the useAuth hook
jest.mock('../../contexts/AuthContext');
const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;

// Mock the child navigators
jest.mock('../AuthNavigator', () => {
  return function MockAuthNavigator() {
    return <div testID="auth-navigator">Auth Navigator</div>;
  };
});

jest.mock('../MainNavigator', () => {
  return function MockMainNavigator() {
    return <div testID="main-navigator">Main Navigator</div>;
  };
});

describe('RootNavigator', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render AuthNavigator when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      error: null,
    });

    const { getByTestId, queryByTestId } = render(<RootNavigator />);

    expect(getByTestId('auth-navigator')).toBeTruthy();
    expect(queryByTestId('main-navigator')).toBeNull();
  });

  it('should render MainNavigator when user is authenticated', () => {
    const mockUser = {
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      role: 'SERVER' as const,
    };

    mockUseAuth.mockReturnValue({
      user: mockUser,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      error: null,
    });

    const { getByTestId, queryByTestId } = render(<RootNavigator />);

    expect(getByTestId('main-navigator')).toBeTruthy();
    expect(queryByTestId('auth-navigator')).toBeNull();
  });

  it('should show loading state while authentication is being checked', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: true,
      error: null,
    });

    const { getByTestId } = render(<RootNavigator />);

    expect(getByTestId('loading-screen')).toBeTruthy();
  });

  it('should handle authentication state changes', () => {
    const mockUser = {
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      role: 'SERVER' as const,
    };

    // Start with no user
    mockUseAuth.mockReturnValue({
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      error: null,
    });

    const { getByTestId, queryByTestId, rerender } = render(<RootNavigator />);

    expect(getByTestId('auth-navigator')).toBeTruthy();
    expect(queryByTestId('main-navigator')).toBeNull();

    // Update to authenticated user
    mockUseAuth.mockReturnValue({
      user: mockUser,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      error: null,
    });

    rerender(<RootNavigator />);

    expect(getByTestId('main-navigator')).toBeTruthy();
    expect(queryByTestId('auth-navigator')).toBeNull();
  });
});