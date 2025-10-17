import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { NotificationType, NotificationPriority } from '../types/notification';

interface NotificationFilterProps {
  selectedType?: NotificationType;
  selectedPriority?: NotificationPriority;
  onTypeChange: (type?: NotificationType) => void;
  onPriorityChange: (priority?: NotificationPriority) => void;
}

export default function NotificationFilter({
  selectedType,
  selectedPriority,
  onTypeChange,
  onPriorityChange,
}: NotificationFilterProps) {
  const notificationTypes = [
    { type: NotificationType.PRE_ARRIVAL, label: 'Pre-Arrival', icon: 'time-outline' },
    { type: NotificationType.BIRTHDAY, label: 'Birthdays', icon: 'gift-outline' },
    { type: NotificationType.ANNIVERSARY, label: 'Anniversaries', icon: 'heart-outline' },
    { type: NotificationType.RETURNING_GUEST, label: 'Returning', icon: 'return-up-back-outline' },
  ];

  const priorities = [
    { priority: NotificationPriority.URGENT, label: 'Urgent', color: '#FF5252' },
    { priority: NotificationPriority.HIGH, label: 'High', color: '#FF9800' },
    { priority: NotificationPriority.MEDIUM, label: 'Medium', color: '#2196F3' },
    { priority: NotificationPriority.LOW, label: 'Low', color: '#4CAF50' },
  ];

  return (
    <View style={styles.container}>
      {/* Type Filters */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Type</Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          <TouchableOpacity
            style={[
              styles.filterChip,
              !selectedType && styles.filterChipActive,
            ]}
            onPress={() => onTypeChange(undefined)}
          >
            <Text
              style={[
                styles.filterChipText,
                !selectedType && styles.filterChipTextActive,
              ]}
            >
              All
            </Text>
          </TouchableOpacity>
          
          {notificationTypes.map(({ type, label, icon }) => (
            <TouchableOpacity
              key={type}
              style={[
                styles.filterChip,
                selectedType === type && styles.filterChipActive,
              ]}
              onPress={() => onTypeChange(type)}
            >
              <Ionicons
                name={icon}
                size={16}
                color={selectedType === type ? '#FFFFFF' : '#757575'}
                style={styles.chipIcon}
              />
              <Text
                style={[
                  styles.filterChipText,
                  selectedType === type && styles.filterChipTextActive,
                ]}
              >
                {label}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>

      {/* Priority Filters */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Priority</Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          <TouchableOpacity
            style={[
              styles.filterChip,
              !selectedPriority && styles.filterChipActive,
            ]}
            onPress={() => onPriorityChange(undefined)}
          >
            <Text
              style={[
                styles.filterChipText,
                !selectedPriority && styles.filterChipTextActive,
              ]}
            >
              All
            </Text>
          </TouchableOpacity>
          
          {priorities.map(({ priority, label, color }) => (
            <TouchableOpacity
              key={priority}
              style={[
                styles.filterChip,
                selectedPriority === priority && [
                  styles.filterChipActive,
                  { backgroundColor: color },
                ],
              ]}
              onPress={() => onPriorityChange(priority)}
            >
              <View
                style={[
                  styles.priorityDot,
                  { backgroundColor: selectedPriority === priority ? '#FFFFFF' : color },
                ]}
              />
              <Text
                style={[
                  styles.filterChipText,
                  selectedPriority === priority && styles.filterChipTextActive,
                ]}
              >
                {label}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
  },
  section: {
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#424242',
    marginBottom: 8,
    marginHorizontal: 16,
  },
  filterChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F5F5F5',
    borderRadius: 20,
    paddingHorizontal: 12,
    paddingVertical: 6,
    marginHorizontal: 4,
    marginLeft: 16,
  },
  filterChipActive: {
    backgroundColor: '#2196F3',
  },
  filterChipText: {
    fontSize: 12,
    color: '#757575',
    fontWeight: '500',
  },
  filterChipTextActive: {
    color: '#FFFFFF',
  },
  chipIcon: {
    marginRight: 4,
  },
  priorityDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 6,
  },
});