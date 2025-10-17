import React, { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react-native';
import { NavigationContainer } from '@react-navigation/native';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { PaperProvider } from 'react-native-paper';
import { AuthProvider } from '../contexts/AuthContext';
import { NetworkProvider } from '../contexts/NetworkContext';
import { NotificationProvider } from '../contexts/NotificationContext';

// Mock user data for testing
export const mockUser = {
  id: 1,
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
  role: 'SERVER' as const,
};

export const mockGuest = {
  id: 1,
  firstName: 'John',
  lastName: 'Doe',
  phone: '+1234567890',
  email: 'john.doe@email.com',
  photoUrl: 'https://example.com/photo.jpg',
  seatingPreference: 'Window table',
  dietaryRestrictions: ['Vegetarian'],
  favoriteDrinks: ['Red wine'],
  birthday: '1985-06-15',
  anniversary: '2010-09-20',
  notes: 'Prefers quiet atmosphere',
  lastVisit: '2024-01-15T19:30:00Z',
  visitCount: 12,
  createdAt: '2023-12-01T10:00:00Z',
};

export const mockVisit = {
  id: 1,
  guestId: 1,
  staffId: 2,
  visitDate: '2024-01-15',
  visitTime: '19:30:00',
  partySize: 2,
  tableNumber: 'A5',
  serviceNotes: 'Celebrated anniversary',
  staffName: 'Jane Smith',
  createdAt: '2024-01-15T19:30:00Z',
  updatedAt: '2024-01-15T19:30:00Z',
};

export const mockNotification = {
  id: 1,
  type: 'PRE_ARRIVAL' as const,
  guestId: 1,
  guest: mockGuest,
  message: 'John Doe has a reservation in 30 minutes',
  acknowledged: false,
  createdAt: '2024-01-15T18:00:00Z',
};

// Create a custom render function that includes providers
const AllTheProviders = ({ children }: { children: React.ReactNode }) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <PaperProvider>
        <NavigationContainer>
          <AuthProvider>
            <NetworkProvider>
              <NotificationProvider>
                {children}
              </NotificationProvider>
            </NetworkProvider>
          </AuthProvider>
        </NavigationContainer>
      </PaperProvider>
    </QueryClientProvider>
  );
};

const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => render(ui, { wrapper: AllTheProviders, ...options });

export * from '@testing-library/react-native';
export { customRender as render };

// Helper function to create mock navigation prop
export const createMockNavigation = () => ({
  navigate: jest.fn(),
  goBack: jest.fn(),
  dispatch: jest.fn(),
  setOptions: jest.fn(),
  isFocused: jest.fn(() => true),
  addListener: jest.fn(),
  removeListener: jest.fn(),
});

// Helper function to create mock route prop
export const createMockRoute = (params = {}) => ({
  key: 'test-route',
  name: 'TestScreen',
  params,
});

// Helper to wait for async operations
export const waitForAsync = () => new Promise(resolve => setTimeout(resolve, 0));