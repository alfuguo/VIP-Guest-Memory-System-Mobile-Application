import React, { useState } from 'react';
import { View, StyleSheet, ScrollView, Alert } from 'react-native';
import { 
  Text, 
  TextInput, 
  Chip, 
  Button, 
  Card,
  Divider 
} from 'react-native-paper';

interface GuestPreferences {
  seatingPreference?: string;
  dietaryRestrictions: string[];
  favoriteDrinks: string[];
  birthday?: string;
  anniversary?: string;
  notes?: string;
}

interface GuestPreferencesFormProps {
  preferences: GuestPreferences;
  onPreferencesChange: (preferences: GuestPreferences) => void;
}

const COMMON_DIETARY_RESTRICTIONS = [
  'Vegetarian',
  'Vegan',
  'Gluten-free',
  'Dairy-free',
  'Nut allergy',
  'Shellfish allergy',
  'Kosher',
  'Halal',
  'Low sodium',
  'Diabetic',
];

const COMMON_DRINKS = [
  'Red wine',
  'White wine',
  'Champagne',
  'Beer',
  'Cocktails',
  'Whiskey',
  'Vodka',
  'Gin',
  'Coffee',
  'Tea',
  'Sparkling water',
  'Still water',
];

const SEATING_PREFERENCES = [
  'Window table',
  'Booth',
  'Bar seating',
  'Quiet area',
  'Near kitchen',
  'Away from kitchen',
  'Corner table',
  'Round table',
];

export default function GuestPreferencesForm({ 
  preferences, 
  onPreferencesChange 
}: GuestPreferencesFormProps) {
  const [customDietaryRestriction, setCustomDietaryRestriction] = useState('');
  const [customDrink, setCustomDrink] = useState('');
  const [birthdayInput, setBirthdayInput] = useState(preferences.birthday || '');
  const [anniversaryInput, setAnniversaryInput] = useState(preferences.anniversary || '');

  const updatePreferences = (updates: Partial<GuestPreferences>) => {
    onPreferencesChange({ ...preferences, ...updates });
  };

  const toggleDietaryRestriction = (restriction: string) => {
    const current = preferences.dietaryRestrictions;
    const updated = current.includes(restriction)
      ? current.filter(r => r !== restriction)
      : [...current, restriction];
    updatePreferences({ dietaryRestrictions: updated });
  };

  const toggleFavoriteDrink = (drink: string) => {
    const current = preferences.favoriteDrinks;
    const updated = current.includes(drink)
      ? current.filter(d => d !== drink)
      : [...current, drink];
    updatePreferences({ favoriteDrinks: updated });
  };

  const addCustomDietaryRestriction = () => {
    if (customDietaryRestriction.trim() && !preferences.dietaryRestrictions.includes(customDietaryRestriction.trim())) {
      updatePreferences({ 
        dietaryRestrictions: [...preferences.dietaryRestrictions, customDietaryRestriction.trim()] 
      });
      setCustomDietaryRestriction('');
    }
  };

  const addCustomDrink = () => {
    if (customDrink.trim() && !preferences.favoriteDrinks.includes(customDrink.trim())) {
      updatePreferences({ 
        favoriteDrinks: [...preferences.favoriteDrinks, customDrink.trim()] 
      });
      setCustomDrink('');
    }
  };

  const validateAndSetDate = (dateString: string, type: 'birthday' | 'anniversary') => {
    if (!dateString.trim()) {
      updatePreferences({ [type]: undefined });
      return;
    }

    // Simple date validation for YYYY-MM-DD format
    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
    if (dateRegex.test(dateString)) {
      const date = new Date(dateString);
      if (!isNaN(date.getTime())) {
        updatePreferences({ [type]: dateString });
        return;
      }
    }

    Alert.alert('Invalid Date', 'Please enter date in YYYY-MM-DD format (e.g., 1990-12-25)');
  };

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Seating Preferences */}
      <Card style={styles.section}>
        <Card.Content>
          <Text style={styles.sectionTitle}>Seating Preference</Text>
          <View style={styles.chipContainer}>
            {SEATING_PREFERENCES.map((preference) => (
              <Chip
                key={preference}
                selected={preferences.seatingPreference === preference}
                onPress={() => updatePreferences({ 
                  seatingPreference: preferences.seatingPreference === preference ? undefined : preference 
                })}
                style={styles.chip}
              >
                {preference}
              </Chip>
            ))}
          </View>
          <TextInput
            label="Custom seating preference"
            value={preferences.seatingPreference && !SEATING_PREFERENCES.includes(preferences.seatingPreference) 
              ? preferences.seatingPreference : ''}
            onChangeText={(text) => updatePreferences({ seatingPreference: text })}
            style={styles.customInput}
            mode="outlined"
            dense
          />
        </Card.Content>
      </Card>

      {/* Dietary Restrictions */}
      <Card style={styles.section}>
        <Card.Content>
          <Text style={styles.sectionTitle}>Dietary Restrictions</Text>
          <View style={styles.chipContainer}>
            {COMMON_DIETARY_RESTRICTIONS.map((restriction) => (
              <Chip
                key={restriction}
                selected={preferences.dietaryRestrictions.includes(restriction)}
                onPress={() => toggleDietaryRestriction(restriction)}
                style={styles.chip}
              >
                {restriction}
              </Chip>
            ))}
            {preferences.dietaryRestrictions
              .filter(r => !COMMON_DIETARY_RESTRICTIONS.includes(r))
              .map((restriction) => (
                <Chip
                  key={restriction}
                  selected={true}
                  onPress={() => toggleDietaryRestriction(restriction)}
                  style={styles.chip}
                  onClose={() => toggleDietaryRestriction(restriction)}
                >
                  {restriction}
                </Chip>
              ))}
          </View>
          <View style={styles.addCustomContainer}>
            <TextInput
              label="Add custom restriction"
              value={customDietaryRestriction}
              onChangeText={setCustomDietaryRestriction}
              style={styles.customInputWithButton}
              mode="outlined"
              dense
              onSubmitEditing={addCustomDietaryRestriction}
            />
            <Button 
              mode="outlined" 
              onPress={addCustomDietaryRestriction}
              disabled={!customDietaryRestriction.trim()}
              style={styles.addButton}
            >
              Add
            </Button>
          </View>
        </Card.Content>
      </Card>

      {/* Favorite Drinks */}
      <Card style={styles.section}>
        <Card.Content>
          <Text style={styles.sectionTitle}>Favorite Drinks</Text>
          <View style={styles.chipContainer}>
            {COMMON_DRINKS.map((drink) => (
              <Chip
                key={drink}
                selected={preferences.favoriteDrinks.includes(drink)}
                onPress={() => toggleFavoriteDrink(drink)}
                style={styles.chip}
              >
                {drink}
              </Chip>
            ))}
            {preferences.favoriteDrinks
              .filter(d => !COMMON_DRINKS.includes(d))
              .map((drink) => (
                <Chip
                  key={drink}
                  selected={true}
                  onPress={() => toggleFavoriteDrink(drink)}
                  style={styles.chip}
                  onClose={() => toggleFavoriteDrink(drink)}
                >
                  {drink}
                </Chip>
              ))}
          </View>
          <View style={styles.addCustomContainer}>
            <TextInput
              label="Add custom drink"
              value={customDrink}
              onChangeText={setCustomDrink}
              style={styles.customInputWithButton}
              mode="outlined"
              dense
              onSubmitEditing={addCustomDrink}
            />
            <Button 
              mode="outlined" 
              onPress={addCustomDrink}
              disabled={!customDrink.trim()}
              style={styles.addButton}
            >
              Add
            </Button>
          </View>
        </Card.Content>
      </Card>

      {/* Special Occasions */}
      <Card style={styles.section}>
        <Card.Content>
          <Text style={styles.sectionTitle}>Special Occasions</Text>
          
          <TextInput
            label="Birthday (YYYY-MM-DD)"
            value={birthdayInput}
            onChangeText={setBirthdayInput}
            onBlur={() => validateAndSetDate(birthdayInput, 'birthday')}
            placeholder="1990-12-25"
            mode="outlined"
            style={styles.dateInput}
            left={<TextInput.Icon icon="cake" />}
            right={birthdayInput ? <TextInput.Icon icon="close" onPress={() => {
              setBirthdayInput('');
              updatePreferences({ birthday: undefined });
            }} /> : undefined}
          />

          <TextInput
            label="Anniversary (YYYY-MM-DD)"
            value={anniversaryInput}
            onChangeText={setAnniversaryInput}
            onBlur={() => validateAndSetDate(anniversaryInput, 'anniversary')}
            placeholder="2010-06-15"
            mode="outlined"
            style={styles.dateInput}
            left={<TextInput.Icon icon="heart" />}
            right={anniversaryInput ? <TextInput.Icon icon="close" onPress={() => {
              setAnniversaryInput('');
              updatePreferences({ anniversary: undefined });
            }} /> : undefined}
          />
        </Card.Content>
      </Card>

      {/* Notes */}
      <Card style={styles.section}>
        <Card.Content>
          <Text style={styles.sectionTitle}>Additional Notes</Text>
          <TextInput
            label="Special requests, preferences, or notes"
            value={preferences.notes || ''}
            onChangeText={(text) => updatePreferences({ notes: text })}
            multiline
            numberOfLines={4}
            mode="outlined"
            style={styles.notesInput}
          />
        </Card.Content>
      </Card>


    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  section: {
    marginBottom: 16,
    elevation: 1,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
    color: '#333',
  },
  chipContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 12,
  },
  chip: {
    marginBottom: 4,
  },
  customInput: {
    marginTop: 8,
  },
  addCustomContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginTop: 8,
  },
  customInputWithButton: {
    flex: 1,
  },
  addButton: {
    marginTop: 8,
  },
  dateInput: {
    marginBottom: 12,
  },
  notesInput: {
    marginTop: 8,
  },
});