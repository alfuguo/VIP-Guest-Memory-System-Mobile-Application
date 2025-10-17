import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, Animated, Dimensions } from 'react-native';
import { Text, Card } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';

interface ToastProps {
  visible: boolean;
  message: string;
  type?: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
  onHide: () => void;
  position?: 'top' | 'bottom';
}

const { width: screenWidth } = Dimensions.get('window');

export default function Toast({
  visible,
  message,
  type = 'info',
  duration = 3000,
  onHide,
  position = 'top',
}: ToastProps) {
  const translateY = useRef(new Animated.Value(position === 'top' ? -100 : 100)).current;
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

      // Auto hide after duration
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
        toValue: position === 'top' ? -100 : 100,
        duration: 300,
        useNativeDriver: true,
      }),
      Animated.timing(opacity, {
        toValue: 0,
        duration: 300,
        useNativeDriver: true,
      }),
    ]).start(() => {
      onHide();
    });
  };

  const getToastConfig = () => {
    switch (type) {
      case 'success':
        return {
          backgroundColor: '#4caf50',
          icon: 'check-circle',
          iconColor: '#fff',
          textColor: '#fff',
        };
      case 'error':
        return {
          backgroundColor: '#f44336',
          icon: 'alert-circle',
          iconColor: '#fff',
          textColor: '#fff',
        };
      case 'warning':
        return {
          backgroundColor: '#ff9800',
          icon: 'alert',
          iconColor: '#fff',
          textColor: '#fff',
        };
      default:
        return {
          backgroundColor: '#2196f3',
          icon: 'information',
          iconColor: '#fff',
          textColor: '#fff',
        };
    }
  };

  const config = getToastConfig();

  if (!visible) return null;

  return (
    <View style={[styles.container, position === 'top' ? styles.top : styles.bottom]}>
      <Animated.View
        style={[
          styles.toast,
          {
            backgroundColor: config.backgroundColor,
            transform: [{ translateY }],
            opacity,
          },
        ]}
      >
        <MaterialCommunityIcons
          name={config.icon as any}
          size={20}
          color={config.iconColor}
          style={styles.icon}
        />
        <Text style={[styles.message, { color: config.textColor }]} numberOfLines={3}>
          {message}
        </Text>
      </Animated.View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    left: 0,
    right: 0,
    zIndex: 9999,
    paddingHorizontal: 16,
  },
  top: {
    top: 50,
  },
  bottom: {
    bottom: 50,
  },
  toast: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 8,
    elevation: 6,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    maxWidth: screenWidth - 32,
  },
  icon: {
    marginRight: 12,
  },
  message: {
    flex: 1,
    fontSize: 14,
    fontWeight: '500',
    lineHeight: 20,
  },
});