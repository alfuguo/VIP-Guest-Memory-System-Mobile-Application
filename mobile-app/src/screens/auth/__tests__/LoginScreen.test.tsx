import React from 'react';
import { render, fireEvent, waitFor } from '../../../__tests__/test-utils';
import LoginScreen from '../LoginScreen';
import { useAuth } from '../../../contexts/AuthContext';

// Mock the useAuth hook
jest.mock('../../../contexts/AuthContext');
const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;

// Mock navigation
const mockNavigation = {
  navigate: jest.fn(),
  goBack: jest.fn(),
  dispatch: jest.fn(),
  setOptions: jest.fn(),
  isFocused: jest.fn(() => true),
  addListener: jest.fn(),
  removeListener: jest.fn(),
};

describe('LoginScreen', () => {
  const mockLogin = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: null,
      login: mockLogin,
      logout: jest.fn(),
      isLoading: false,
      error: null,
    });
  });

  it('should render login form correctly', () => {
    const { getByTestId, getByText } = render(
      <LoginScreen navigation={mockNavigation} route={{ key: 'login', name: 'Login', params: {} }} />
    );

    expect(getByText('Welcome Back')).toBeTruthy();
    expect(getByTestId('email-input')).toBeTruthy();
    expect(getByTestId('password-input')).toBeTruthy();
    expect(getByTestId('login-button')).toBeTruthy();
  });

  it('should handle email input', () => {
    const { getByTestId } = render(
      <LoginScreen navigation={mockNavigation} route={{ key: 'login', name: 'Login', params: {} }} />
    );

    const emailInput = getByTestId('email-input');
    fireEvent.changeText(emailInput, 'test@example.com');

    expect(emailInput.props.value).toBe('test@example.com');
  });

  it('should handle password input', () => {
    const { getByTestId } = render(
      <LoginScreen navigation={mockNavigation} route={{ key: 'login', name: 'Login', params: {} }} />
    );

    const passwordInput = getByTestId('password-input');
    fireEvent.changeText(passwordInput, 'password123');

    expect(passwordInput.props.value).toBe('password123');
  });

  it('should validate required fields', async () => {
    const { getByTestId, getByText } = render(
      <LoginScreen navigation={mockNavigation} route={{ key: 'login', name: 'Login', params: {} }} />
    );

    const loginButton = getByTestId('login-button');
    fireEvent.press(loginButton);

    await waitFor(() => {
      expect(getByText('Email is required')).toBeTruthy();
      expect(getByText('Password is required')).toBeTruthy();
    });

    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('should validate email format', async () => {
    const { getByTestId, getByText } = render(
      <LoginScreen navigation={mockNavigation} route={{ key: 'login', name: 'Login', params: {} }} />
    );

    const emailInput = getByTestId('email-input');
    const passwordInput = getByTestId('password-input');
    const loginButton = getByTestId('login-button');

    fireEvent.changeText(emailInput, 'invalid-email');
    fireEvent.changeText(passwordInput, 'password123');
    fireEvent.press(loginButton);

    await waitFor(() => {
      expect(getByText('Please enter a valid email address')).toBeTruthy();
    });

    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('should submit form with valid data', async () => {
    mockLogin.mockResolvedValue(undefined);

    const { getByTestId } = render(
      <LoginScreen navigation={mockNavigation} route={{ key: 'login', name: 'Login', params: {} }} />
    );

    const emailInput = getByTestId('email-input');
    const passwordInput = getByTestId('password-input');
    const loginButton = getByTestId('login-button');

    fireEvent.changeText(emailInput, 'test@example.com');
    fireEvent.changeText(passwordInput, 'password123');
    fireEvent.press(loginButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123');
    });
  });

  it('should show loading state during login', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      login: mockLogin,
      logout: jest.fn(),
      isLoading: true,
      error: null,
    });

    const { getByTestId } = render(
      <LoginScreen navigation={mockNavigation} route={{ key: 'login', name: 'Login', params: {} }} />
    );

    const loginButton = getByTestId('login-button');
    expect(loginButton.props.accessibilityState.disabled).toBe(true);
  });

  it('should display error message on login failure', () => {
    const errorMessage = 'Invalid credentials';
    mockUseAuth.mockReturnValue({
      user: null,
      login: mockLogin,
      logout: jest.fn(),
      isLoading: false,
      error: errorMessage,
    });

    const { getByText } = render(
      <LoginScreen navigation={mockNavigation} route={{ key: 'login', name: 'Login', params: {} }} />
    );

    expect(getByText(errorMessage)).toBeTruthy();
  });

  it('should toggle password visibility', () => {
    const { getByTestId } = render(
      <LoginScreen navigation={mockNavigation} route={{ key: 'login', name: 'Login', params: {} }} />
    );

    const passwordInput = getByTestId('password-input');
    const toggleButton = getByTestId('password-toggle');

    // Initially password should be hidden
    expect(passwordInput.props.secureTextEntry).toBe(true);

    // Toggle to show password
    fireEvent.press(toggleButton);
    expect(passwordInput.props.secureTextEntry).toBe(false);

    // Toggle to hide password again
    fireEvent.press(toggleButton);
    expect(passwordInput.props.secureTextEntry).toBe(true);
  });
});