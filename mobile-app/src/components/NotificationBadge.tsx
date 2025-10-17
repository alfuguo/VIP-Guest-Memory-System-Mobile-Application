import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface NotificationBadgeProps {
  count: number;
  maxCount?: number;
  size?: 'small' | 'medium' | 'large';
  color?: string;
}

export default function NotificationBadge({ 
  count, 
  maxCount = 99, 
  size = 'medium',
  color = '#FF5252' 
}: NotificationBadgeProps) {
  if (count <= 0) {
    return null;
  }

  const displayCount = count > maxCount ? `${maxCount}+` : count.toString();
  
  const sizeStyles = {
    small: {
      container: styles.smallContainer,
      text: styles.smallText,
    },
    medium: {
      container: styles.mediumContainer,
      text: styles.mediumText,
    },
    large: {
      container: styles.largeContainer,
      text: styles.largeText,
    },
  };

  const currentSize = sizeStyles[size];

  return (
    <View style={[currentSize.container, { backgroundColor: color }]}>
      <Text style={[currentSize.text, styles.text]}>{displayCount}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  text: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    textAlign: 'center',
  },
  smallContainer: {
    minWidth: 16,
    height: 16,
    borderRadius: 8,
    paddingHorizontal: 4,
    justifyContent: 'center',
    alignItems: 'center',
  },
  smallText: {
    fontSize: 10,
  },
  mediumContainer: {
    minWidth: 20,
    height: 20,
    borderRadius: 10,
    paddingHorizontal: 6,
    justifyContent: 'center',
    alignItems: 'center',
  },
  mediumText: {
    fontSize: 12,
  },
  largeContainer: {
    minWidth: 24,
    height: 24,
    borderRadius: 12,
    paddingHorizontal: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  largeText: {
    fontSize: 14,
  },
});