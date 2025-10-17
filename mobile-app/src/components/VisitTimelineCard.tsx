import React, { useState } from 'react';
import { View, StyleSheet, Animated } from 'react-native';
import { Card, Text, IconButton, Chip, Divider } from 'react-native-paper';
import { Visit } from '../types/visit';

interface VisitTimelineCardProps {
  visit: Visit;
  onEdit?: (visit: Visit) => void;
  onDelete?: (visit: Visit) => void;
  canEdit?: boolean;
  canDelete?: boolean;
  isFirst?: boolean;
  isLast?: boolean;
}

export default function VisitTimelineCard({ 
  visit, 
  onEdit, 
  onDelete,
  canEdit = false,
  canDelete = false,
  isFirst = false,
  isLast = false
}: VisitTimelineCardProps) {
  const [expanded, setExpanded] = useState(false);
  const [animation] = useState(new Animated.Value(0));

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

  const toggleExpanded = () => {
    const toValue = expanded ? 0 : 1;
    setExpanded(!expanded);
    
    Animated.timing(animation, {
      toValue,
      duration: 300,
      useNativeDriver: false,
    }).start();
  };

  const expandedHeight = animation.interpolate({
    inputRange: [0, 1],
    outputRange: [0, 120], // Adjust based on content
  });

  const rotateIcon = animation.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '180deg'],
  });

  return (
    <View style={styles.timelineContainer}>
      {/* Timeline Line */}
      <View style={styles.timelineLineContainer}>
        {!isFirst && <View style={styles.timelineLineTop} />}
        <View style={[styles.timelineDot, expanded && styles.timelineDotExpanded]} />
        {!isLast && <View style={styles.timelineLineBottom} />}
      </View>

      {/* Visit Card */}
      <Card style={[styles.card, expanded && styles.cardExpanded]}>
        <Card.Content>
          {/* Header with date, time, and actions */}
          <View style={styles.header}>
            <View style={styles.dateTimeContainer}>
              <Text style={styles.date}>{formatDate(visit.visitDate)}</Text>
              <Text style={styles.time}>{formatTime(visit.visitTime)}</Text>
              <Text style={styles.timeAgo}>{getTimeAgo(visit.visitDate, visit.visitTime)}</Text>
            </View>
            
            <View style={styles.actionsContainer}>
              {(canEdit || canDelete) && (
                <>
                  {canEdit && onEdit && (
                    <IconButton
                      icon="pencil"
                      size={18}
                      onPress={() => onEdit(visit)}
                      style={styles.actionButton}
                    />
                  )}
                  {canDelete && onDelete && (
                    <IconButton
                      icon="delete"
                      size={18}
                      onPress={() => onDelete(visit)}
                      style={styles.actionButton}
                    />
                  )}
                </>
              )}
              
              <IconButton
                icon="chevron-down"
                size={20}
                onPress={toggleExpanded}
                style={[
                  styles.expandButton,
                  { transform: [{ rotate: rotateIcon }] }
                ]}
              />
            </View>
          </View>

          {/* Basic visit info */}
          <View style={styles.basicInfo}>
            <View style={styles.infoRow}>
              <Text style={styles.infoText}>Party of {visit.partySize}</Text>
            </View>

            {visit.tableNumber && (
              <View style={styles.infoRow}>
                <Text style={styles.infoText}>Table {visit.tableNumber}</Text>
              </View>
            )}

            <View style={styles.infoRow}>
              <Text style={styles.infoText}>Served by {visit.staffName}</Text>
            </View>
          </View>

          {/* Service notes preview */}
          {visit.serviceNotes && !expanded && (
            <View style={styles.notesPreview}>
              <Text style={styles.notesPreviewText} numberOfLines={2}>
                {visit.serviceNotes}
              </Text>
            </View>
          )}

          {/* Expandable content */}
          <Animated.View style={[styles.expandedContent, { height: expandedHeight }]}>
            <View style={styles.expandedInner}>
              <Divider style={styles.divider} />
              
              {/* Full service notes */}
              {visit.serviceNotes && (
                <View style={styles.fullNotes}>
                  <Text style={styles.notesLabel}>Service Notes:</Text>
                  <Text style={styles.notesText}>{visit.serviceNotes}</Text>
                </View>
              )}

              {/* Visit metadata */}
              <View style={styles.metadata}>
                <Chip
                  icon="clock"
                  style={styles.metadataChip}
                  textStyle={styles.metadataText}
                >
                  Logged {new Date(visit.createdAt).toLocaleDateString()}
                </Chip>
                
                {visit.updatedAt !== visit.createdAt && (
                  <Chip
                    icon="pencil"
                    style={styles.metadataChip}
                    textStyle={styles.metadataText}
                  >
                    Updated {new Date(visit.updatedAt).toLocaleDateString()}
                  </Chip>
                )}
              </View>
            </View>
          </Animated.View>
        </Card.Content>
      </Card>
    </View>
  );
}

const styles = StyleSheet.create({
  timelineContainer: {
    flexDirection: 'row',
    marginVertical: 4,
    paddingHorizontal: 16,
  },
  timelineLineContainer: {
    width: 24,
    alignItems: 'center',
    marginRight: 12,
  },
  timelineLineTop: {
    width: 2,
    height: 20,
    backgroundColor: '#e0e0e0',
  },
  timelineDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: '#6200ee',
    borderWidth: 2,
    borderColor: '#fff',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 2,
  },
  timelineDotExpanded: {
    backgroundColor: '#3700b3',
    width: 16,
    height: 16,
    borderRadius: 8,
  },
  timelineLineBottom: {
    width: 2,
    height: 20,
    backgroundColor: '#e0e0e0',
    flex: 1,
  },
  card: {
    flex: 1,
    elevation: 2,
    marginBottom: 8,
  },
  cardExpanded: {
    elevation: 4,
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
  actionsContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  actionButton: {
    margin: 0,
    marginHorizontal: 2,
  },
  expandButton: {
    margin: 0,
    marginLeft: 4,
  },
  basicInfo: {
    marginBottom: 8,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  infoText: {
    fontSize: 14,
    color: '#666',
    marginLeft: 8,
  },
  notesPreview: {
    backgroundColor: '#f8f9fa',
    padding: 8,
    borderRadius: 6,
    borderLeftWidth: 3,
    borderLeftColor: '#6200ee',
    marginTop: 4,
  },
  notesPreviewText: {
    fontSize: 13,
    color: '#555',
    lineHeight: 18,
  },
  expandedContent: {
    overflow: 'hidden',
  },
  expandedInner: {
    paddingTop: 8,
  },
  divider: {
    backgroundColor: '#e0e0e0',
    marginBottom: 12,
  },
  fullNotes: {
    backgroundColor: '#f8f9fa',
    padding: 12,
    borderRadius: 8,
    borderLeftWidth: 3,
    borderLeftColor: '#6200ee',
    marginBottom: 12,
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
  metadata: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  metadataChip: {
    backgroundColor: '#e8f5e8',
    height: 28,
  },
  metadataText: {
    fontSize: 11,
    color: '#2e7d32',
  },
});