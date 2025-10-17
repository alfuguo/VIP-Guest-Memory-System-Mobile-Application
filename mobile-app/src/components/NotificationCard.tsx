import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Notification, NotificationType, NotificationPriority } from '../types/notification';

interface NotificationCardProps {
  notification: Notification;
  onPress: (notification: Notification) => void;
  onAcknowledge?: (notification: Notification) => void;
}

export default function NotificationCard({ 
  notification, 
  onPress, 
  onAcknowledge 
}: NotificationCardProps) {
  const getNotificationIcon = (type: NotificationType) => {
    switch (type) {
      case NotificationType.PRE_ARRIVAL:
        return 'time-outline';
      case NotificationType.BIRTHDAY:
        return 'gift-outline';
      case NotificationType.ANNIVERSARY:
        return 'heart-outline';
      case NotificationType.RETURNING_GUEST:
        return 'return-up-back-outline';
      case NotificationType.DIETARY_RESTRICTION:
        return 'restaurant-outline';
      default:
        return 'notifications-outline';
    }
  };

  const getPriorityColor = (priority: NotificationPriority) => {
    switch (priority) {
      case NotificationPriority.URGENT:
        return '#FF5252';
      case NotificationPriority.HIGH:
        return '#FF9800';
      case NotificationPriority.MEDIUM:
        return '#2196F3';
      case NotificationPriority.LOW:
        return '#4CAF50';
      default:
        return '#757575';
    }
  };

  const formatNotificationType = (type: NotificationType) => {
    switch (type) {
      case NotificationType.PRE_ARRIVAL:
        return 'Pre-Arrival';
      case NotificationType.BIRTHDAY:
        return 'Birthday';
      case NotificationType.ANNIVERSARY:
        return 'Anniversary';
      case NotificationType.RETURNING_GUEST:
        return 'Returning Guest';
      case NotificationType.DIETARY_RESTRICTION:
        return 'Dietary Alert';
      default:
        return 'Notification';
    }
  };

  const priorityColor = getPriorityColor(notification.priority);
  const iconName = getNotificationIcon(notification.notificationType);

  return (
    <TouchableOpacity
      style={[styles.container, { borderLeftColor: priorityColor }]}
      onPress={() => onPress(notification)}
      activeOpacity={0.7}
    >
      <View style={styles.header}>
        <View style={styles.iconContainer}>
          <Ionicons 
            name={iconName} 
            size={24} 
            color={priorityColor} 
          />
        </View>
        
        <View style={styles.guestInfo}>
          {notification.photoUrl ? (
            <Image 
              source={{ uri: notification.photoUrl }} 
              style={styles.guestPhoto}
            />
          ) : (
            <View style={[styles.guestPhoto, styles.placeholderPhoto]}>
              <Ionicons name="person" size={20} color="#757575" />
            </View>
          )}
          
          <View style={styles.guestDetails}>
            <Text style={styles.guestName}>
              {notification.firstName} {notification.lastName}
            </Text>
            <Text style={styles.notificationType}>
              {formatNotificationType(notification.notificationType)}
            </Text>
          </View>
        </View>

        {onAcknowledge && (
          <TouchableOpacity
            style={styles.acknowledgeButton}
            onPress={() => onAcknowledge(notification)}
            hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
          >
            <Ionicons name="checkmark-circle-outline" size={24} color="#4CAF50" />
          </TouchableOpacity>
        )}
      </View>

      <Text style={styles.message}>{notification.message}</Text>

      {/* Guest preferences summary */}
      <View style={styles.preferencesContainer}>
        {notification.seatingPreference && (
          <View style={styles.preferenceItem}>
            <Ionicons name="location-outline" size={16} color="#757575" />
            <Text style={styles.preferenceText}>{notification.seatingPreference}</Text>
          </View>
        )}
        
        {notification.dietaryRestrictions && notification.dietaryRestrictions.length > 0 && (
          <View style={styles.preferenceItem}>
            <Ionicons name="restaurant-outline" size={16} color="#FF9800" />
            <Text style={styles.preferenceText}>
              {notification.dietaryRestrictions.join(', ')}
            </Text>
          </View>
        )}
        
        {notification.favoriteDrinks && notification.favoriteDrinks.length > 0 && (
          <View style={styles.preferenceItem}>
            <Ionicons name="wine-outline" size={16} color="#2196F3" />
            <Text style={styles.preferenceText}>
              {notification.favoriteDrinks.slice(0, 2).join(', ')}
              {notification.favoriteDrinks.length > 2 && '...'}
            </Text>
          </View>
        )}
      </View>

      {/* Additional info for specific notification types */}
      {notification.lastVisitDate && (
        <Text style={styles.lastVisit}>
          Last visit: {new Date(notification.lastVisitDate).toLocaleDateString()}
        </Text>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginVertical: 6,
    marginHorizontal: 16,
    borderLeftWidth: 4,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
    elevation: 5,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  iconContainer: {
    marginRight: 12,
  },
  guestInfo: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
  guestPhoto: {
    width: 40,
    height: 40,
    borderRadius: 20,
    marginRight: 12,
  },
  placeholderPhoto: {
    backgroundColor: '#F5F5F5',
    justifyContent: 'center',
    alignItems: 'center',
  },
  guestDetails: {
    flex: 1,
  },
  guestName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#212121',
    marginBottom: 2,
  },
  notificationType: {
    fontSize: 12,
    color: '#757575',
    textTransform: 'uppercase',
    fontWeight: '500',
  },
  acknowledgeButton: {
    padding: 4,
  },
  message: {
    fontSize: 14,
    color: '#424242',
    lineHeight: 20,
    marginBottom: 12,
  },
  preferencesContainer: {
    marginBottom: 8,
  },
  preferenceItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  preferenceText: {
    fontSize: 12,
    color: '#616161',
    marginLeft: 6,
    flex: 1,
  },
  lastVisit: {
    fontSize: 12,
    color: '#9E9E9E',
    fontStyle: 'italic',
  },
});