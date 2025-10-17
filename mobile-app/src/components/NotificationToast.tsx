import React, { useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Animated,
  TouchableOpacity,
  Dimensions,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { NotificationType, NotificationPriority } from '../types/notification';

interface NotificationToastProps {
  visible: boolean;
  message: string;
  type: NotificationType;
  priority: NotificationPriority;
  onPress?: () => void;
  onDismiss: () => void;
  duration?: number;
}

const { width } = Dimensions.get('window');

export default function NotificationToast({
  visible,
  message,
  type,
  priority,
  onPress,
  onDismiss,
  duration = 4000,
}: NotificationToastProps) {
  const translateY = useRef(new Animated.Value(-100)).current;
  const opacity = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (visible) {
      // Show animation
      Animated.parallel([
        Animated.timing(translateY, {
          toValue: 0,
          duration: 300,
          useNativeDriver: true,
        }),
        Animated.timing(opacity, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
        }),
      ]).start();

      // Auto dismiss after duration
      const timer = setTimeout(() => {
        hideToast();
      }, duration);

      return () => clearTimeout(timer);
    } else {
      hideToast();
    }
  }, [visible, duration]);

  const hideToast = () => {
    Animated.parallel([
      Animated.timing(translateY, {
        toValue: -100,
        duration: 300,
        useNativeDriver: true,
      }),
      Animated.timing(opacity, {
        toValue: 0,
        duration: 300,
        useNativeDriver: true,
      }),
    ]).start(() => {
      onDismiss();
    });
  };

  const getToastStyle = () => {
    switch (priority) {
      case NotificationPriority.URGENT:
        return { backgroundColor: '#FF5252', borderColor: '#D32F2F' };
      case NotificationPriority.HIGH:
        return { backgroundColor: '#FF9800', borderColor: '#F57C00' };
      case NotificationPriority.MEDIUM:
        return { backgroundColor: '#2196F3', borderColor: '#1976D2' };
      case NotificationPriority.LOW:
        return { backgroundColor: '#4CAF50', borderColor: '#388E3C' };
      default:
        return { backgroundColor: '#757575', borderColor: '#616161' };
    }
  };

  const getIcon = () => {
    switch (type) {
      case NotificationType.BIRTHDAY:
        return 'gift';
      case NotificationType.ANNIVERSARY:
        return 'heart';
      case NotificationType.PRE_ARRIVAL:
        return 'time';
      case NotificationType.RETURNING_GUEST:
        return 'return-up-back';
      default:
        return 'notifications';
    }
  };

  if (!visible) {
    return null;
  }

  const toastStyle = getToastStyle();
  const iconName = getIcon();

  return (
    <Animated.View
      style={[
        styles.container,
        toastStyle,
        {
          transform: [{ translateY }],
          opacity,
        },
      ]}
    >
      <TouchableOpacity
        style={styles.content}
        onPress={onPress}
        activeOpacity={onPress ? 0.8 : 1}
      >
        <Ionicons name={iconName} size={24} color="#FFFFFF" style={styles.icon} />
        <Text style={styles.message} numberOfLines={2}>
          {message}
        </Text>
        <TouchableOpacity style={styles.closeButton} onPress={hideToast}>
          <Ionicons name="close" size={20} color="#FFFFFF" />
        </TouchableOpacity>
      </TouchableOpacity>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    top: 50,
    left: 16,
    right: 16,
    zIndex: 1000,
    borderRadius: 12,
    borderWidth: 1,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
  },
  icon: {
    marginRight: 12,
  },
  message: {
    flex: 1,
    fontSize: 14,
    fontWeight: '500',
    color: '#FFFFFF',
    lineHeight: 18,
  },
  closeButton: {
    padding: 4,
    marginLeft: 8,
  },
});