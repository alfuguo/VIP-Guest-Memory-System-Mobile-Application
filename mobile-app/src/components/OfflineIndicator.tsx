import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Text, Card, Button, Badge, IconButton } from 'react-native-paper';
import { useOfflineStatus, useOffline } from '../hooks/useOffline';

interface OfflineIndicatorProps {
  showDetails?: boolean;
  compact?: boolean;
}

export const OfflineIndicator: React.FC<OfflineIndicatorProps> = ({ 
  showDetails = false, 
  compact = false 
}) => {
  const { isOffline, hasQueuedRequests, queueCount, highPriorityCount } = useOfflineStatus();
  const { processOfflineQueue, clearOfflineQueue, isProcessingQueue } = useOffline();

  if (!isOffline && !hasQueuedRequests) {
    return null;
  }

  if (compact) {
    return (
      <View style={styles.compactContainer}>
        {isOffline && (
          <Badge style={styles.offlineBadge}>
            Offline
          </Badge>
        )}
        {hasQueuedRequests && (
          <Badge style={styles.queueBadge}>
            {queueCount} queued
          </Badge>
        )}
      </View>
    );
  }

  return (
    <Card style={[styles.container, isOffline ? styles.offlineContainer : styles.queueContainer]}>
      <Card.Content>
        <View style={styles.header}>
          <View style={styles.statusInfo}>
            {isOffline ? (
              <>
                <Text variant="titleMedium" style={styles.offlineText}>
                  You're offline
                </Text>
                <Text variant="bodySmall" style={styles.subtitle}>
                  Changes will be saved when you reconnect
                </Text>
              </>
            ) : (
              <>
                <Text variant="titleMedium" style={styles.queueText}>
                  Syncing changes
                </Text>
                <Text variant="bodySmall" style={styles.subtitle}>
                  {queueCount} pending requests
                  {highPriorityCount > 0 && ` (${highPriorityCount} high priority)`}
                </Text>
              </>
            )}
          </View>
          
          {hasQueuedRequests && (
            <Badge style={styles.countBadge}>
              {queueCount}
            </Badge>
          )}
        </View>

        {showDetails && hasQueuedRequests && (
          <View style={styles.actions}>
            <Button
              mode="outlined"
              onPress={processOfflineQueue}
              loading={isProcessingQueue}
              disabled={isOffline || isProcessingQueue}
              style={styles.actionButton}
            >
              {isProcessingQueue ? 'Syncing...' : 'Sync Now'}
            </Button>
            
            <Button
              mode="text"
              onPress={clearOfflineQueue}
              disabled={isProcessingQueue}
              style={styles.actionButton}
            >
              Clear Queue
            </Button>
          </View>
        )}
      </Card.Content>
    </Card>
  );
};

// Simple banner version for top of screen
export const OfflineBanner: React.FC = () => {
  const { isOffline } = useOfflineStatus();

  if (!isOffline) {
    return null;
  }

  return (
    <View style={styles.banner}>
      <Text variant="bodySmall" style={styles.bannerText}>
        You're offline. Changes will be saved when you reconnect.
      </Text>
    </View>
  );
};

// Queue status for navigation or header
export const QueueStatus: React.FC<{ onPress?: () => void }> = ({ onPress }) => {
  const { hasQueuedRequests, queueCount, isOffline } = useOfflineStatus();

  if (!hasQueuedRequests && !isOffline) {
    return null;
  }

  return (
    <IconButton
      icon={isOffline ? "cloud-off" : "cloud-sync"}
      size={20}
      onPress={onPress}
      style={styles.queueIcon}
    >
      {hasQueuedRequests && (
        <Badge style={styles.iconBadge} size={16}>
          {queueCount}
        </Badge>
      )}
    </IconButton>
  );
};

const styles = StyleSheet.create({
  container: {
    margin: 16,
    elevation: 2,
  },
  offlineContainer: {
    backgroundColor: '#fff3cd',
    borderLeftWidth: 4,
    borderLeftColor: '#ffc107',
  },
  queueContainer: {
    backgroundColor: '#d1ecf1',
    borderLeftWidth: 4,
    borderLeftColor: '#17a2b8',
  },
  compactContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  statusInfo: {
    flex: 1,
  },
  offlineText: {
    color: '#856404',
    fontWeight: 'bold',
  },
  queueText: {
    color: '#0c5460',
    fontWeight: 'bold',
  },
  subtitle: {
    color: '#6c757d',
    marginTop: 4,
  },
  countBadge: {
    backgroundColor: '#17a2b8',
  },
  offlineBadge: {
    backgroundColor: '#ffc107',
    color: '#856404',
  },
  queueBadge: {
    backgroundColor: '#17a2b8',
  },
  actions: {
    flexDirection: 'row',
    marginTop: 16,
    gap: 8,
  },
  actionButton: {
    flex: 1,
  },
  banner: {
    backgroundColor: '#ffc107',
    paddingVertical: 8,
    paddingHorizontal: 16,
    alignItems: 'center',
  },
  bannerText: {
    color: '#856404',
    fontWeight: '500',
  },
  queueIcon: {
    position: 'relative',
  },
  iconBadge: {
    position: 'absolute',
    top: -2,
    right: -2,
    backgroundColor: '#dc3545',
  },
});