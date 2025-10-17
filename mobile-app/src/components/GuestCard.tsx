import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image } from 'react-native';
import { Card, Avatar, Chip } from 'react-native-paper';
import { Guest } from '../types/guest';

interface GuestCardProps {
  guest: Guest;
  onPress: (guest: Guest) => void;
}

export default function GuestCard({ guest, onPress }: GuestCardProps) {
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
    <Card style={styles.card} onPress={() => onPress(guest)}>
      <Card.Content>
        <View style={styles.header}>
          <View style={styles.avatarContainer}>
            {guest.photoUrl ? (
              <Avatar.Image size={50} source={{ uri: guest.photoUrl }} />
            ) : (
              <Avatar.Text 
                size={50} 
                label={getInitials(guest.firstName, guest.lastName)} 
                style={styles.avatar}
              />
            )}
          </View>
          
          <View style={styles.guestInfo}>
            <Text style={styles.name}>
              {guest.firstName} {guest.lastName || ''}
            </Text>
            <Text style={styles.phone}>{guest.phone}</Text>
            <Text style={styles.lastVisit}>
              Last visit: {formatLastVisit(guest.lastVisit)} • {guest.visitCount} visits
            </Text>
          </View>
        </View>

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

        {(guest.dietaryRestrictions.length > 0 || guest.seatingPreference) && (
          <View style={styles.preferencesContainer}>
            {guest.seatingPreference && (
              <Chip style={styles.preferenceChip} textStyle={styles.preferenceText}>
                {guest.seatingPreference}
              </Chip>
            )}
            {guest.dietaryRestrictions.slice(0, 2).map((restriction, index) => (
              <Chip 
                key={index} 
                style={styles.preferenceChip} 
                textStyle={styles.preferenceText}
              >
                {restriction}
              </Chip>
            ))}
            {guest.dietaryRestrictions.length > 2 && (
              <Chip style={styles.preferenceChip} textStyle={styles.preferenceText}>
                +{guest.dietaryRestrictions.length - 2} more
              </Chip>
            )}
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
    elevation: 2,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  avatarContainer: {
    marginRight: 12,
  },
  avatar: {
    backgroundColor: '#6200ee',
  },
  guestInfo: {
    flex: 1,
  },
  name: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1a1a1a',
    marginBottom: 2,
  },
  phone: {
    fontSize: 14,
    color: '#666',
    marginBottom: 2,
  },
  lastVisit: {
    fontSize: 12,
    color: '#888',
  },
  occasionContainer: {
    marginBottom: 8,
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
    fontSize: 12,
    fontWeight: 'bold',
  },
  preferencesContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
  },
  preferenceChip: {
    backgroundColor: '#f0f0f0',
    height: 28,
  },
  preferenceText: {
    fontSize: 11,
    color: '#666',
  },
});