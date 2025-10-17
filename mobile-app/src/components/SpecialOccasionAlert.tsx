import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
  Modal,
  Animated,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Notification, NotificationType } from '../types/notification';

interface SpecialOccasionAlertProps {
  notification: Notification;
  visible: boolean;
  onClose: () => void;
  onViewGuest: () => void;
}

export default function SpecialOccasionAlert({
  notification,
  visible,
  onClose,
  onViewGuest,
}: SpecialOccasionAlertProps) {
  const scaleValue = React.useRef(new Animated.Value(0)).current;

  React.useEffect(() => {
    if (visible) {
      Animated.spring(scaleValue, {
        toValue: 1,
        useNativeDriver: true,
        tension: 100,
        friction: 8,
      }).start();
    } else {
      scaleValue.setValue(0);
    }
  }, [visible, scaleValue]);

  const getOccasionDetails = () => {
    switch (notification.notificationType) {
      case NotificationType.BIRTHDAY:
        return {
          icon: 'gift',
          color: '#E91E63',
          emoji: '🎂',
          title: 'Birthday Alert!',
          subtitle: 'Make this day special',
        };
      case NotificationType.ANNIVERSARY:
        return {
          icon: 'heart',
          color: '#E91E63',
          emoji: '💕',
          title: 'Anniversary Alert!',
          subtitle: 'Celebrate their special day',
        };
      default:
        return {
          icon: 'star',
          color: '#FF9800',
          emoji: '⭐',
          title: 'Special Occasion!',
          subtitle: 'Something to celebrate',
        };
    }
  };

  const occasionDetails = getOccasionDetails();

  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <View style={styles.overlay}>
        <Animated.View
          style={[
            styles.alertContainer,
            {
              transform: [{ scale: scaleValue }],
              borderColor: occasionDetails.color,
            },
          ]}
        >
          {/* Header */}
          <View style={[styles.header, { backgroundColor: occasionDetails.color }]}>
            <View style={styles.headerContent}>
              <Text style={styles.emoji}>{occasionDetails.emoji}</Text>
              <View style={styles.headerText}>
                <Text style={styles.title}>{occasionDetails.title}</Text>
                <Text style={styles.subtitle}>{occasionDetails.subtitle}</Text>
              </View>
            </View>
            <TouchableOpacity style={styles.closeButton} onPress={onClose}>
              <Ionicons name="close" size={24} color="#FFFFFF" />
            </TouchableOpacity>
          </View>

          {/* Guest Info */}
          <View style={styles.guestInfo}>
            <View style={styles.guestHeader}>
              {notification.photoUrl ? (
                <Image 
                  source={{ uri: notification.photoUrl }} 
                  style={styles.guestPhoto}
                />
              ) : (
                <View style={[styles.guestPhoto, styles.placeholderPhoto]}>
                  <Ionicons name="person" size={32} color="#757575" />
                </View>
              )}
              
              <View style={styles.guestDetails}>
                <Text style={styles.guestName}>
                  {notification.firstName} {notification.lastName}
                </Text>
                <Text style={styles.guestPhone}>{notification.phone}</Text>
                {notification.visitCount && (
                  <Text style={styles.visitCount}>
                    {notification.visitCount} previous visits
                  </Text>
                )}
              </View>
            </View>

            <Text style={styles.message}>{notification.message}</Text>
          </View>

          {/* Guest Preferences */}
          {(notification.seatingPreference || 
            notification.dietaryRestrictions?.length || 
            notification.favoriteDrinks?.length) && (
            <View style={styles.preferencesSection}>
              <Text style={styles.preferencesTitle}>Quick Preferences</Text>
              
              {notification.seatingPreference && (
                <View style={styles.preferenceItem}>
                  <Ionicons name="location" size={16} color="#757575" />
                  <Text style={styles.preferenceText}>
                    {notification.seatingPreference}
                  </Text>
                </View>
              )}
              
              {notification.dietaryRestrictions && notification.dietaryRestrictions.length > 0 && (
                <View style={styles.preferenceItem}>
                  <Ionicons name="restaurant" size={16} color="#FF9800" />
                  <Text style={styles.preferenceText}>
                    {notification.dietaryRestrictions.join(', ')}
                  </Text>
                </View>
              )}
              
              {notification.favoriteDrinks && notification.favoriteDrinks.length > 0 && (
                <View style={styles.preferenceItem}>
                  <Ionicons name="wine" size={16} color="#2196F3" />
                  <Text style={styles.preferenceText}>
                    {notification.favoriteDrinks.slice(0, 2).join(', ')}
                    {notification.favoriteDrinks.length > 2 && '...'}
                  </Text>
                </View>
              )}
            </View>
          )}

          {/* Actions */}
          <View style={styles.actions}>
            <TouchableOpacity style={styles.secondaryButton} onPress={onClose}>
              <Text style={styles.secondaryButtonText}>Dismiss</Text>
            </TouchableOpacity>
            
            <TouchableOpacity 
              style={[styles.primaryButton, { backgroundColor: occasionDetails.color }]} 
              onPress={onViewGuest}
            >
              <Ionicons name="person" size={16} color="#FFFFFF" />
              <Text style={styles.primaryButtonText}>View Guest</Text>
            </TouchableOpacity>
          </View>
        </Animated.View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  alertContainer: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    width: '100%',
    maxWidth: 400,
    borderWidth: 2,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 10,
    },
    shadowOpacity: 0.25,
    shadowRadius: 20,
    elevation: 10,
  },
  header: {
    borderTopLeftRadius: 14,
    borderTopRightRadius: 14,
    padding: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerContent: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  emoji: {
    fontSize: 32,
    marginRight: 12,
  },
  headerText: {
    flex: 1,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginBottom: 2,
  },
  subtitle: {
    fontSize: 14,
    color: '#FFFFFF',
    opacity: 0.9,
  },
  closeButton: {
    padding: 4,
  },
  guestInfo: {
    padding: 16,
  },
  guestHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  guestPhoto: {
    width: 60,
    height: 60,
    borderRadius: 30,
    marginRight: 16,
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
    fontSize: 20,
    fontWeight: 'bold',
    color: '#212121',
    marginBottom: 4,
  },
  guestPhone: {
    fontSize: 14,
    color: '#757575',
    marginBottom: 2,
  },
  visitCount: {
    fontSize: 12,
    color: '#9E9E9E',
    fontStyle: 'italic',
  },
  message: {
    fontSize: 16,
    color: '#424242',
    lineHeight: 22,
    textAlign: 'center',
    fontWeight: '500',
  },
  preferencesSection: {
    paddingHorizontal: 16,
    paddingBottom: 16,
    borderTopWidth: 1,
    borderTopColor: '#F0F0F0',
    marginTop: 8,
    paddingTop: 16,
  },
  preferencesTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#424242',
    marginBottom: 12,
  },
  preferenceItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  preferenceText: {
    fontSize: 14,
    color: '#616161',
    marginLeft: 8,
    flex: 1,
  },
  actions: {
    flexDirection: 'row',
    padding: 16,
    gap: 12,
  },
  secondaryButton: {
    flex: 1,
    backgroundColor: '#F5F5F5',
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  secondaryButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#757575',
  },
  primaryButton: {
    flex: 1,
    borderRadius: 8,
    paddingVertical: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  primaryButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
    marginLeft: 6,
  },
});