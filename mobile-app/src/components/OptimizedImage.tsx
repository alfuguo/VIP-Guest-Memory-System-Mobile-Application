import React, { useState } from 'react';
import { View, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { ActivityIndicator, Avatar } from 'react-native-paper';

interface OptimizedImageProps {
  uri?: string;
  size: number;
  fallbackInitials?: string;
  style?: any;
  borderRadius?: number;
  placeholder?: string;
  priority?: 'low' | 'normal' | 'high';
}

export default function OptimizedImage({
  uri,
  size,
  fallbackInitials,
  style,
  borderRadius,
  placeholder = 'blurhash:L6PZfSi_.AyE_3t7t7R**0o#DgR4',
  priority = 'normal'
}: OptimizedImageProps) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const imageStyle = [
    {
      width: size,
      height: size,
      borderRadius: borderRadius ?? size / 2,
    },
    style,
  ];

  // If no URI provided or error occurred, show fallback
  if (!uri || error) {
    if (fallbackInitials) {
      return (
        <Avatar.Text 
          size={size} 
          label={fallbackInitials}
          style={[{ backgroundColor: '#6200ee' }, style]}
        />
      );
    }
    return (
      <View style={[imageStyle, styles.fallbackContainer]}>
        <Avatar.Icon 
          size={size * 0.6} 
          icon="account" 
          style={styles.fallbackIcon}
        />
      </View>
    );
  }

  return (
    <View style={imageStyle}>
      <Image
        source={{ uri }}
        style={imageStyle}
        placeholder={placeholder}
        contentFit="cover"
        transition={200}
        priority={priority}
        cachePolicy="memory-disk"
        onLoadStart={() => setLoading(true)}
        onLoad={() => setLoading(false)}
        onError={() => {
          setError(true);
          setLoading(false);
        }}
      />
      {loading && (
        <View style={[imageStyle, styles.loadingOverlay]}>
          <ActivityIndicator size="small" color="#6200ee" />
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  fallbackContainer: {
    backgroundColor: '#f0f0f0',
    justifyContent: 'center',
    alignItems: 'center',
  },
  fallbackIcon: {
    backgroundColor: 'transparent',
  },
  loadingOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    backgroundColor: 'rgba(255, 255, 255, 0.8)',
    justifyContent: 'center',
    alignItems: 'center',
  },
});