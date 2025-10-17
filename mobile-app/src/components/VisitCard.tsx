import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card, Text, Chip, IconButton } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Visit } from '../types/visit';

interface VisitCardProps {
  visit: Visit;
  onEdit?: (visit: Visit) => void;
  canEdit?: boolean;
}

export default function VisitCard({ visit, onEdit, canEdit = false }: VisitCardProps) {
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatTime = (timeString: string) => {
    const [hours, minutes] = timeString.split(':');
    const hour = parseInt(hours, 10);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${ampm}`;
  };

  const getTimeAgo = (dateString: string, timeString: string) => {
    const visitDateTime = new Date(`${dateString}T${timeString}`);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - visitDateTime.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.ceil(diffDays / 7)} weeks ago`;
    if (diffDays < 365) return `${Math.ceil(diffDays / 30)} months ago`;
    return `${Math.ceil(diffDays / 365)} years ago`;
  };

  return (
    <Card style={styles.card}>
      <Card.Content>
        <View style={styles.header}>
          <View style={styles.dateTimeContainer}>
            <Text style={styles.date}>{formatDate(visit.visitDate)}</Text>
            <Text style={styles.time}>{formatTime(visit.visitTime)}</Text>
            <Text style={styles.timeAgo}>{getTimeAgo(visit.visitDate, visit.visitTime)}</Text>
          </View>
          {canEdit && onEdit && (
            <IconButton
              icon="pencil"
              size={20}
              onPress={() => onEdit(visit)}
              style={styles.editButton}
            />
          )}
        </View>

        <View style={styles.detailsContainer}>
          <View style={styles.detailRow}>
            <MaterialCommunityIcons name="account-group" size={16} color="#666" />
            <Text style={styles.detailText}>
              Party of {visit.partySize}
            </Text>
          </View>

          {visit.tableNumber && (
            <View style={styles.detailRow}>
              <MaterialCommunityIcons name="table-furniture" size={16} color="#666" />
              <Text style={styles.detailText}>Table {visit.tableNumber}</Text>
            </View>
          )}

          <View style={styles.detailRow}>
            <MaterialCommunityIcons name="account" size={16} color="#666" />
            <Text style={styles.detailText}>Served by {visit.staffName}</Text>
          </View>
        </View>

        {visit.serviceNotes && (
          <View style={styles.notesContainer}>
            <Text style={styles.notesLabel}>Service Notes:</Text>
            <Text style={styles.notesText}>{visit.serviceNotes}</Text>
          </View>
        )}
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
  dateTimeContainer: {
    flex: 1,
  },
  date: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 2,
  },
  time: {
    fontSize: 14,
    color: '#666',
    marginBottom: 2,
  },
  timeAgo: {
    fontSize: 12,
    color: '#999',
  },
  editButton: {
    margin: 0,
    marginTop: -8,
  },
  detailsContainer: {
    marginBottom: 12,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  detailText: {
    fontSize: 14,
    color: '#666',
    marginLeft: 8,
  },
  notesContainer: {
    backgroundColor: '#f8f9fa',
    padding: 12,
    borderRadius: 8,
    borderLeftWidth: 3,
    borderLeftColor: '#6200ee',
  },
  notesLabel: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#666',
    marginBottom: 4,
  },
  notesText: {
    fontSize: 14,
    color: '#333',
    lineHeight: 20,
  },
});