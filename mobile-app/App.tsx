import React from 'react';
import { StatusBar } from 'expo-status-bar';
import { QueryClientProvider } from '@tanstack/react-query';
import { PaperProvider } from 'react-native-paper';
import { AuthProvider } from './src/contexts/AuthContext';
import { NetworkProvider } from './src/contexts/NetworkContext';
import RootNavigator from './src/navigation/RootNavigator';
import { createQueryClient } from './src/config/queryClient';
import ErrorBoundary from './src/components/ErrorBoundary';

// Create enhanced query client with offline support
const queryClient = createQueryClient();

export default function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <PaperProvider>
          <NetworkProvider>
            <AuthProvider>
              <RootNavigator />
              <StatusBar style="auto" />
            </AuthProvider>
          </NetworkProvider>
        </PaperProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}
