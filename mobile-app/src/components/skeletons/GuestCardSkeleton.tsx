import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card } from 'react-native-paper';
import SkeletonLoader from './SkeletonLoader';

export default function GuestCardSkeleton() {
  return (
    <Card style={styles.card}>
      <Card.Content>
        <View style={styles.header}>
          <SkeletonLoader width={50} height={50} borderRadius={25} />
          <View style={styles.guestInfo}>
            <SkeletonLoader width="70%" height={18} style={styles.name} />
            <SkeletonLoader width="50%" height={14} style={styles.phone} />
            <SkeletonLoader width="80%" height={12} style={styles.lastVisit} />
          </View>
        </View>
        
        <View style={styles.preferencesContainer}>
          <SkeletonLoader width={80} height={28} borderRadius={14} />
          <SkeletonLoader width={60} height={28} borderRadius={14} />
          <SkeletonLoader width={90} height={28} borderRadius={14} />
        </View>
      </Card.Content>
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    marginHorizontal: 16,
    marginVertical: 8,
    elevation: 2,
    minHeight: 120,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  guestInfo: {
    flex: 1,
    marginLeft: 12,
  },
  name: {
    marginBottom: 4,
  },
  phone: {
    marginBottom: 4,
  },
  lastVisit: {
    marginBottom: 0,
  },
  preferencesContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
  },
});