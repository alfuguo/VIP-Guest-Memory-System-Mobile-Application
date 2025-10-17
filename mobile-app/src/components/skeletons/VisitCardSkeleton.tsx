import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card } from 'react-native-paper';
import SkeletonLoader from './SkeletonLoader';

export default function VisitCardSkeleton() {
  return (
    <Card style={styles.card}>
      <Card.Content>
        <View style={styles.header}>
          <View style={styles.dateInfo}>
            <SkeletonLoader width={80} height={16} style={styles.date} />
            <SkeletonLoader width={60} height={14} style={styles.time} />
          </View>
          <View style={styles.serverInfo}>
            <SkeletonLoader width={100} height={14} />
          </View>
        </View>
        
        <View style={styles.details}>
          <SkeletonLoader width="100%" height={14} style={styles.detailLine} />
          <SkeletonLoader width="80%" height={14} style={styles.detailLine} />
        </View>
        
        <View style={styles.notes}>
          <SkeletonLoader width="100%" height={12} style={styles.noteLine} />
          <SkeletonLoader width="90%" height={12} style={styles.noteLine} />
          <SkeletonLoader width="60%" height={12} />
        </View>
      </Card.Content>
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    marginHorizontal: 16,
    marginVertical: 8,
    elevation: 1,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  dateInfo: {
    flex: 1,
  },
  date: {
    marginBottom: 4,
  },
  time: {
    marginBottom: 0,
  },
  serverInfo: {
    alignItems: 'flex-end',
  },
  details: {
    marginBottom: 12,
  },
  detailLine: {
    marginBottom: 4,
  },
  notes: {
    backgroundColor: '#f8f9fa',
    padding: 8,
    borderRadius: 4,
  },
  noteLine: {
    marginBottom: 4,
  },
});