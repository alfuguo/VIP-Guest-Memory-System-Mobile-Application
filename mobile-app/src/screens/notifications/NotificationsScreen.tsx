import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  RefreshControl,
  Alert,
  TouchableOpacity,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { useNotifications } from '../../contexts/NotificationContext';
import { notificationService } from '../../services/notificationService';
import { Notification, NotificationType, NotificationPriority } from '../../types/notification';
import NotificationCard from '../../components/NotificationCard';
import NotificationFilter from '../../components/NotificationFilter';
import LoadingState from '../../components/LoadingState';
import EmptyState from '../../components/EmptyState';
import { handleError } from '../../utils/errorHandler';

export default function NotificationsScreen() {
  const navigation = useNavigation();
  const { notifications, loading, refreshNotifications, markAsRead } = useNotifications();
  const [filteredNotifications, setFilteredNotifications] = useState<Notification[]>([]);
  const [refreshing, setRefreshing] = useState(false);
  const [selectedType, setSelectedType] = useState<NotificationType | undefined>();
  const [selectedPriority, setSelectedPriority] = useState<NotificationPriority | undefined>();
  const [showFilters, setShowFilters] = useState(false);

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    try {
      await refreshNotifications();
    } finally {
      setRefreshing(false);
    }
  }, [refreshNotifications]);

  const handleNotificationPress = useCallback((notification: Notification) => {
    // Navigate to guest detail screen
    (navigation as any).navigate('Guests', {
      screen: 'GuestDetail',
      params: { guestId: notification.id },
    });
  }, [navigation]);

  const handleAcknowledgeNotification = useCallback(async (notification: Notification) => {
    try {
      await notificationService.acknowledgeNotification(notification.id);
      markAsRead(notification.id);
      
      Alert.alert(
        'Notification Acknowledged',
        `${notification.firstName} ${notification.lastName}'s notification has been acknowledged.`,
        [{ text: 'OK' }]
      );
    } catch (error) {
      handleError(error, 'Failed to acknowledge notification');
    }
  }, [markAsRead]);

  const applyFilters = useCallback(() => {
    let filtered = [...notifications];

    if (selectedType) {
      filtered = filtered.filter(n => n.notificationType === selectedType);
    }

    if (selectedPriority) {
      filtered = filtered.filter(n => n.priority === selectedPriority);
    }

    setFilteredNotifications(filtered);
  }, [notifications, selectedType, selectedPriority]);

  const handleTypeChange = useCallback((type?: NotificationType) => {
    setSelectedType(type);
  }, []);

  const handlePriorityChange = useCallback((priority?: NotificationPriority) => {
    setSelectedPriority(priority);
  }, []);

  const getNotificationCounts = useCallback(() => {
    const counts = {
      total: notifications.length,
      urgent: notifications.filter(n => n.priority === NotificationPriority.URGENT).length,
      high: notifications.filter(n => n.priority === NotificationPriority.HIGH).length,
      preArrival: notifications.filter(n => n.notificationType === NotificationType.PRE_ARRIVAL).length,
      specialOccasions: notifications.filter(n => 
        n.notificationType === NotificationType.BIRTHDAY || 
        n.notificationType === NotificationType.ANNIVERSARY
      ).length,
    };
    return counts;
  }, [notifications]);

  // Remove the loadNotifications useEffect since we're using the context

  useEffect(() => {
    applyFilters();
  }, [applyFilters]);

  const renderNotificationItem = ({ item }: { item: Notification }) => (
    <NotificationCard
      notification={item}
      onPress={handleNotificationPress}
      onAcknowledge={handleAcknowledgeNotification}
    />
  );

  const renderHeader = () => {
    const counts = getNotificationCounts();
    
    return (
      <View style={styles.header}>
        <View style={styles.titleContainer}>
          <Text style={styles.title}>Notifications</Text>
          <TouchableOpacity
            style={styles.filterButton}
            onPress={() => setShowFilters(!showFilters)}
          >
            <Ionicons 
              name={showFilters ? 'filter' : 'filter-outline'} 
              size={24} 
              color="#2196F3" 
            />
          </TouchableOpacity>
        </View>

        {/* Summary Cards */}
        <View style={styles.summaryContainer}>
          <View style={[styles.summaryCard, styles.urgentCard]}>
            <Text style={styles.summaryNumber}>{counts.urgent}</Text>
            <Text style={styles.summaryLabel}>Urgent</Text>
          </View>
          
          <View style={[styles.summaryCard, styles.highCard]}>
            <Text style={styles.summaryNumber}>{counts.high}</Text>
            <Text style={styles.summaryLabel}>High Priority</Text>
          </View>
          
          <View style={[styles.summaryCard, styles.preArrivalCard]}>
            <Text style={styles.summaryNumber}>{counts.preArrival}</Text>
            <Text style={styles.summaryLabel}>Pre-Arrival</Text>
          </View>
          
          <View style={[styles.summaryCard, styles.occasionCard]}>
            <Text style={styles.summaryNumber}>{counts.specialOccasions}</Text>
            <Text style={styles.summaryLabel}>Occasions</Text>
          </View>
        </View>

        {showFilters && (
          <NotificationFilter
            selectedType={selectedType}
            selectedPriority={selectedPriority}
            onTypeChange={handleTypeChange}
            onPriorityChange={handlePriorityChange}
          />
        )}

        <View style={styles.resultsHeader}>
          <Text style={styles.resultsText}>
            {filteredNotifications.length} notification{filteredNotifications.length !== 1 ? 's' : ''}
            {(selectedType || selectedPriority) && ' (filtered)'}
          </Text>
        </View>
      </View>
    );
  };

  if (loading) {
    return <LoadingState message="Loading notifications..." />;
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={filteredNotifications}
        renderItem={renderNotificationItem}
        keyExtractor={(item) => `${item.id}-${item.notificationType}`}
        ListHeaderComponent={renderHeader}
        ListEmptyComponent={
          <EmptyState
            icon="bell-outline"
            title="No notifications"
            message={
              selectedType || selectedPriority
                ? "No notifications match your current filters."
                : "All caught up! No notifications at this time."
            }
          />
        }
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        contentContainerStyle={filteredNotifications.length === 0 ? styles.emptyContainer : undefined}
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  emptyContainer: {
    flex: 1,
  },
  header: {
    backgroundColor: '#FFFFFF',
    paddingTop: 16,
    marginBottom: 8,
  },
  titleContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    marginBottom: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#212121',
  },
  filterButton: {
    padding: 8,
  },
  summaryContainer: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    marginBottom: 16,
  },
  summaryCard: {
    flex: 1,
    backgroundColor: '#F8F9FA',
    borderRadius: 12,
    padding: 12,
    marginHorizontal: 4,
    alignItems: 'center',
    borderLeftWidth: 4,
  },
  urgentCard: {
    borderLeftColor: '#FF5252',
  },
  highCard: {
    borderLeftColor: '#FF9800',
  },
  preArrivalCard: {
    borderLeftColor: '#2196F3',
  },
  occasionCard: {
    borderLeftColor: '#E91E63',
  },
  summaryNumber: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#212121',
    marginBottom: 4,
  },
  summaryLabel: {
    fontSize: 10,
    color: '#757575',
    textAlign: 'center',
    fontWeight: '500',
  },
  resultsHeader: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderTopWidth: 1,
    borderTopColor: '#E0E0E0',
  },
  resultsText: {
    fontSize: 14,
    color: '#757575',
    fontWeight: '500',
  },
});