import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card } from 'react-native-paper';
import SkeletonLoader from './SkeletonLoader';

export default function GuestDetailSkeleton() {
  return (
    <Card style={styles.card}>
      <Card.Content>
        {/* Header with photo and basic info */}
        <View style={styles.header}>
          <SkeletonLoader width={80} height={80} borderRadius={40} />
          <View style={styles.basicInfo}>
            <SkeletonLoader width="80%" height={24} style={styles.name} />
            <SkeletonLoader width="60%" height={16} style={styles.contact} />
            <SkeletonLoader width="70%" height={16} style={styles.contact} />
            <SkeletonLoader width="90%" height={14} style={styles.stats} />
          </View>
        </View>

        {/* Preferences section */}
        <View style={styles.preferencesSection}>
          <SkeletonLoader width={100} height={16} style={styles.sectionTitle} />
          <View style={styles.preferenceRow}>
            <SkeletonLoader width={16} height={16} />
            <SkeletonLoader width="70%" height={14} style={styles.preferenceText} />
          </View>
          <View style={styles.preferenceRow}>
            <SkeletonLoader width={16} height={16} />
            <SkeletonLoader width="60%" height={14} style={styles.preferenceText} />
          </View>
          <View style={styles.preferenceRow}>
            <SkeletonLoader width={16} height={16} />
            <SkeletonLoader width="80%" height={14} style={styles.preferenceText} />
          </View>
        </View>

        {/* Notes section */}
        <View style={styles.notesSection}>
          <SkeletonLoader width={60} height={16} style={styles.sectionTitle} />
          <View style={styles.notesContainer}>
            <SkeletonLoader width="100%" height={14} style={styles.notesLine} />
            <SkeletonLoader width="90%" height={14} style={styles.notesLine} />
            <SkeletonLoader width="70%" height={14} style={styles.notesLine} />
          </View>
        </View>

        {/* Action buttons */}
        <View style={styles.actionButtons}>
          <SkeletonLoader width="48%" height={40} borderRadius={20} />
          <SkeletonLoader width="48%" height={40} borderRadius={20} />
        </View>
      </Card.Content>
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    margin: 16,
    elevation: 2,
  },
  header: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  basicInfo: {
    flex: 1,
    marginLeft: 16,
    justifyContent: 'center',
  },
  name: {
    marginBottom: 8,
  },
  contact: {
    marginBottom: 4,
  },
  stats: {
    marginTop: 4,
  },
  preferencesSection: {
    marginBottom: 16,
  },
  sectionTitle: {
    marginBottom: 8,
  },
  preferenceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
  },
  preferenceText: {
    marginLeft: 8,
  },
  notesSection: {
    marginBottom: 16,
  },
  notesContainer: {
    backgroundColor: '#f8f9fa',
    padding: 12,
    borderRadius: 8,
    borderLeftWidth: 3,
    borderLeftColor: '#e0e0e0',
  },
  notesLine: {
    marginBottom: 4,
  },
  actionButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8,
  },
});