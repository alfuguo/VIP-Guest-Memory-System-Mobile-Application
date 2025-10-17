import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Text, Button, Card } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';

interface ErrorStateProps {
  title?: string;
  message?: string;
  icon?: string;
  actionLabel?: string;
  onAction?: () => void;
  showRetry?: boolean;
  onRetry?: () => void;
  style?: any;
}

export default function ErrorState({
  title = 'Something went wrong',
  message = 'An error occurred while loading this content.',
  icon = 'alert-circle-outline',
  actionLabel,
  onAction,
  showRetry = true,
  onRetry,
  style,
}: ErrorStateProps) {
  return (
    <View style={[styles.container, style]}>
      <Card style={styles.errorCard}>
        <Card.Content style={styles.content}>
          <MaterialCommunityIcons 
            name={icon as any} 
            size={48} 
            color="#d32f2f" 
            style={styles.icon}
          />
          <Text style={styles.title}>{title}</Text>
          <Text style={styles.message}>{message}</Text>
          
          <View style={styles.actions}>
            {showRetry && onRetry && (
              <Button 
                mode="contained" 
                onPress={onRetry}
                style={styles.button}
              >
                Try Again
              </Button>
            )}
            {actionLabel && onAction && (
              <Button 
                mode="outlined" 
                onPress={onAction}
                style={styles.button}
              >
                {actionLabel}
              </Button>
            )}
          </View>
        </Card.Content>
      </Card>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#f5f5f5',
  },
  errorCard: {
    width: '100%',
    maxWidth: 400,
    elevation: 2,
  },
  content: {
    alignItems: 'center',
    paddingVertical: 24,
  },
  icon: {
    marginBottom: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#d32f2f',
    marginBottom: 12,
    textAlign: 'center',
  },
  message: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    lineHeight: 20,
    marginBottom: 24,
  },
  actions: {
    flexDirection: 'row',
    gap: 12,
    flexWrap: 'wrap',
    justifyContent: 'center',
  },
  button: {
    minWidth: 100,
  },
});