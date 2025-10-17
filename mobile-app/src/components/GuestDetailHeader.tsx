import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card, Text, Chip, Button } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Guest } from '../types/guest';
import OptimizedImage from './OptimizedImage';

interface GuestDetailHeaderProps {
  guest: Guest;
  onEdit: () => void;
  onAddVisit: () => void;
}

export default function GuestDetailHeader({ guest, onEdit, onAddVisit }: GuestDetailHeaderProps) {
  const getInitials = (firstName: string, lastName?: string) => {
    const first = firstName.charAt(0).toUpperCase();
    const last = lastName ? lastName.charAt(0).toUpperCase() : '';
    return first + last;
  };

  const formatLastVisit = (lastVisit?: string) => {
    if (!lastVisit) return 'Never visited';
    
    const date = new Date(lastVisit);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.ceil(diffDays / 7)} weeks ago`;
    if (diffDays < 365) return `${Math.ceil(diffDays / 30)} months ago`;
    return `${Math.ceil(diffDays / 365)} years ago`;
  };

  const getUpcomingOccasion = () => {
    const today = new Date();
    const currentYear = today.getFullYear();
    
    if (guest.birthday) {
      const birthday = new Date(guest.birthday);
      const thisYearBirthday = new Date(currentYear, birthday.getMonth(), birthday.getDate());
      const daysDiff = Math.ceil((thisYearBirthday.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
      
      if (daysDiff >= 0 && daysDiff <= 30) {
        return { type: 'Birthday', days: daysDiff };
      }
    }
    
    if (guest.anniversary) {
      const anniversary = new Date(guest.anniversary);
      const thisYearAnniversary = new Date(currentYear, anniversary.getMonth(), anniversary.getDate());
      const daysDiff = Math.ceil((thisYearAnniversary.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
      
      if (daysDiff >= 0 && daysDiff <= 30) {
        return { type: 'Anniversary', days: daysDiff };
      }
    }
    
    return null;
  };

  const upcomingOccasion = getUpcomingOccasion();

  return (
    <Card style={styles.card}>
      <Card.Content>
        {/* Header with photo and basic info */}
        <View style={styles.header}>
          <View style={styles.avatarContainer}>
            <OptimizedImage
              uri={guest.photoUrl}
              size={80}
              fallbackInitials={getInitials(guest.firstName, guest.lastName)}
              priority="high"
            />
          </View>
          
          <View style={styles.basicInfo}>
            <Text style={styles.name}>
              {guest.firstName} {guest.lastName || ''}
            </Text>
            <View style={styles.contactInfo}>
              <MaterialCommunityIcons name="phone" size={16} color="#666" />
              <Text style={styles.phone}>{guest.phone}</Text>
            </View>
            {guest.email && (
              <View style={styles.contactInfo}>
                <MaterialCommunityIcons name="email" size={16} color="#666" />
                <Text style={styles.email}>{guest.email}</Text>
              </View>
            )}
            <Text style={styles.visitStats}>
              {guest.visitCount} visits • Last: {formatLastVisit(guest.lastVisit)}
            </Text>
          </View>
        </View>

        {/* Upcoming occasion alert */}
        {upcomingOccasion && (
          <View style={styles.occasionContainer}>
            <Chip 
              icon={upcomingOccasion.type === 'Birthday' ? 'cake' : 'heart'}
              style={[
                styles.occasionChip,
                upcomingOccasion.days === 0 ? styles.todayChip : styles.upcomingChip
              ]}
              textStyle={styles.occasionText}
            >
              {upcomingOccasion.type} {upcomingOccasion.days === 0 ? 'Today!' : `in ${upcomingOccasion.days} days`}
            </Chip>
          </View>
        )}

        {/* Preferences */}
        {(guest.seatingPreference || guest.dietaryRestrictions.length > 0 || guest.favoriteDrinks.length > 0) && (
          <View style={styles.preferencesSection}>
            <Text style={styles.sectionTitle}>Preferences</Text>
            
            {guest.seatingPreference && (
              <View style={styles.preferenceRow}>
                <MaterialCommunityIcons name="table-furniture" size={16} color="#666" />
                <Text style={styles.preferenceText}>{guest.seatingPreference}</Text>
              </View>
            )}
            
            {guest.dietaryRestrictions.length > 0 && (
              <View style={styles.preferenceRow}>
                <MaterialCommunityIcons name="food-apple" size={16} color="#666" />
                <Text style={styles.preferenceText}>
                  {guest.dietaryRestrictions.join(', ')}
                </Text>
              </View>
            )}
            
            {guest.favoriteDrinks.length > 0 && (
              <View style={styles.preferenceRow}>
                <MaterialCommunityIcons name="glass-wine" size={16} color="#666" />
                <Text style={styles.preferenceText}>
                  {guest.favoriteDrinks.join(', ')}
                </Text>
              </View>
            )}
          </View>
        )}

        {/* Notes */}
        {guest.notes && (
          <View style={styles.notesSection}>
            <Text style={styles.sectionTitle}>Notes</Text>
            <Text style={styles.notesText}>{guest.notes}</Text>
          </View>
        )}

        {/* Action buttons */}
        <View style={styles.actionButtons}>
          <Button 
            mode="contained" 
            onPress={onAddVisit}
            style={styles.primaryButton}
            icon="plus"
          >
            Log Visit
          </Button>
          <Button 
            mode="outlined" 
            onPress={onEdit}
            style={styles.secondaryButton}
            icon="pencil"
          >
            Edit
          </Button>
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
  avatarContainer: {
    marginRight: 16,
  },
  avatar: {
    backgroundColor: '#6200ee',
  },
  basicInfo: {
    flex: 1,
    justifyContent: 'center',
  },
  name: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1a1a1a',
    marginBottom: 8,
  },
  contactInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  phone: {
    fontSize: 16,
    color: '#666',
    marginLeft: 8,
  },
  email: {
    fontSize: 14,
    color: '#666',
    marginLeft: 8,
  },
  visitStats: {
    fontSize: 14,
    color: '#888',
    marginTop: 4,
  },
  occasionContainer: {
    marginBottom: 16,
    alignItems: 'flex-start',
  },
  occasionChip: {
    alignSelf: 'flex-start',
  },
  todayChip: {
    backgroundColor: '#ff6b6b',
  },
  upcomingChip: {
    backgroundColor: '#4ecdc4',
  },
  occasionText: {
    color: 'white',
    fontSize: 14,
    fontWeight: 'bold',
  },
  preferencesSection: {
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  preferenceRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 6,
  },
  preferenceText: {
    fontSize: 14,
    color: '#666',
    marginLeft: 8,
    flex: 1,
  },
  notesSection: {
    marginBottom: 16,
  },
  notesText: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
    backgroundColor: '#f8f9fa',
    padding: 12,
    borderRadius: 8,
    borderLeftWidth: 3,
    borderLeftColor: '#6200ee',
  },
  actionButtons: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 8,
  },
  primaryButton: {
    flex: 1,
  },
  secondaryButton: {
    flex: 1,
  },
});