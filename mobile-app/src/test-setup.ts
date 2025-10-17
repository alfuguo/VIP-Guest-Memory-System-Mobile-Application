// Mock AsyncStorage
jest.mock('@react-native-async-storage/async-storage', () =>
  require('@react-native-async-storage/async-storage/jest/async-storage-mock')
);

// Mock React Navigation
jest.mock('@react-navigation/native', () => {
  return {
    useNavigation: () => ({
      navigate: jest.fn(),
      goBack: jest.fn(),
      dispatch: jest.fn(),
    }),
    useRoute: () => ({
      params: {},
    }),
    useFocusEffect: jest.fn(),
    NavigationContainer: ({ children }: { children: React.ReactNode }) => children,
  };
});

// Mock Expo modules
jest.mock('expo-camera', () => ({
  Camera: {
    Constants: {
      Type: {
        back: 'back',
        front: 'front',
      },
    },
  },
  CameraType: {
    back: 'back',
    front: 'front',
  },
}));

// Mock NetInfo
jest.mock('@react-native-community/netinfo', () => ({
  addEventListener: jest.fn(),
  fetch: jest.fn(() => Promise.resolve({ isConnected: true })),
}));

// Mock React Query
jest.mock('@tanstack/react-query', () => ({
  useQuery: jest.fn(),
  useMutation: jest.fn(),
  useQueryClient: jest.fn(),
  QueryClient: jest.fn().mockImplementation(() => ({
    setQueryData: jest.fn(),
    getQueryData: jest.fn(),
    invalidateQueries: jest.fn(),
  })),
  QueryClientProvider: ({ children }: { children: React.ReactNode }) => children,
}));

// Global test utilities
global.fetch = jest.fn();