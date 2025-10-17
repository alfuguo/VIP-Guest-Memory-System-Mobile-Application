import React, { createContext, useContext, useReducer, useEffect } from 'react';
import { AuthState, AuthContextType, LoginRequest, User } from '../types/auth';
import { tokenStorage } from '../utils/tokenStorage';

// Auth reducer actions
type AuthAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'LOGIN_SUCCESS'; payload: { user: User; token: string; refreshToken: string } }
  | { type: 'LOGOUT' }
  | { type: 'REFRESH_TOKEN_SUCCESS'; payload: { token: string; refreshToken: string } }
  | { type: 'RESTORE_AUTH'; payload: { user: User; token: string; refreshToken: string } };

// Initial state
const initialState: AuthState = {
  user: null,
  token: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true,
};

// Auth reducer
function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    case 'LOGIN_SUCCESS':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken,
        isAuthenticated: true,
        isLoading: false,
      };
    case 'LOGOUT':
      return {
        ...initialState,
        isLoading: false,
      };
    case 'REFRESH_TOKEN_SUCCESS':
      return {
        ...state,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken,
      };
    case 'RESTORE_AUTH':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken,
        isAuthenticated: true,
        isLoading: false,
      };
    default:
      return state;
  }
}

// Create context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Auth provider component
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Restore authentication state on app start
  useEffect(() => {
    restoreAuthState();
  }, []);

  const restoreAuthState = async () => {
    try {
      const { token, refreshToken, user } = await tokenStorage.getAllAuthData();
      
      if (token && refreshToken && user) {
        // Validate token with backend before restoring
        const { authService } = await import('../services/authService');
        const isValid = await authService.validateToken();
        
        if (isValid) {
          dispatch({
            type: 'RESTORE_AUTH',
            payload: { user, token, refreshToken },
          });
        } else {
          // Token is invalid, try to refresh
          try {
            await refreshAuthToken();
          } catch (refreshError) {
            // If refresh also fails, clear everything
            await tokenStorage.clearAll();
            dispatch({ type: 'SET_LOADING', payload: false });
          }
        }
      } else {
        dispatch({ type: 'SET_LOADING', payload: false });
      }
    } catch (error) {
      console.error('Error restoring auth state:', error);
      // Clear potentially corrupted data
      await tokenStorage.clearAll();
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const login = async (credentials: LoginRequest) => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });
      
      // Import authService dynamically to avoid circular dependency
      const { authService } = await import('../services/authService');
      const response = await authService.login(credentials);

      // Store tokens and user data
      await Promise.all([
        tokenStorage.setToken(response.token),
        tokenStorage.setRefreshToken(response.refreshToken),
        tokenStorage.setUser(response.user),
      ]);

      dispatch({
        type: 'LOGIN_SUCCESS',
        payload: response,
      });
    } catch (error) {
      dispatch({ type: 'SET_LOADING', payload: false });
      throw error;
    }
  };

  const logout = async () => {
    try {
      // Import authService dynamically to avoid circular dependency
      const { authService } = await import('../services/authService');
      await authService.logout();
      await tokenStorage.clearAll();
      dispatch({ type: 'LOGOUT' });
    } catch (error) {
      console.error('Error during logout:', error);
      // Clear local data even if API call fails
      await tokenStorage.clearAll();
      dispatch({ type: 'LOGOUT' });
    }
  };

  const refreshAuthToken = async () => {
    try {
      const refreshToken = await tokenStorage.getRefreshToken();
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      // Import authService dynamically to avoid circular dependency
      const { authService } = await import('../services/authService');
      const response = await authService.refreshToken(refreshToken);

      await Promise.all([
        tokenStorage.setToken(response.token),
        tokenStorage.setRefreshToken(response.refreshToken),
      ]);

      dispatch({
        type: 'REFRESH_TOKEN_SUCCESS',
        payload: response,
      });
    } catch (error) {
      console.error('Error refreshing token:', error);
      // If refresh fails, logout user
      await logout();
      throw error;
    }
  };

  const contextValue: AuthContextType = {
    ...state,
    login,
    logout,
    refreshAuthToken,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

// Custom hook to use auth context
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}