import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  Alert,
} from 'react-native';
import {
  Modal,
  Portal,
  Text,
  Button,
  Divider,
  Chip,
  Switch,
  TextInput,
  IconButton,
} from 'react-native-paper';
import { SearchFilters } from './SearchBar';

interface FilterModalProps {
  visible: boolean;
  onDismiss: () => void;
  onApplyFilters: (filters: SearchFilters) => void;
  initialFilters?: SearchFilters;
}

// Common dietary restrictions options
const DIETARY_RESTRICTIONS = [
  'Vegetarian',
  'Vegan',
  'Gluten-Free',
  'Dairy-Free',
  'Nut Allergy',
  'Shellfish Allergy',
  'Kosher',
  'Halal',
  'Low Sodium',
  'Diabetic',
];

// Common seating preferences
const SEATING_PREFERENCES = [
  'Window table',
  'Booth',
  'Bar seating',
  'Quiet area',
  'Near kitchen',
  'Outdoor patio',
  'Private dining',
  'High top',
];

// Common favorite drinks
const FAVORITE_DRINKS = [
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

export default function FilterModal({
  visible,
  onDismiss,
  onApplyFilters,
  initialFilters,
}: FilterModalProps) {
  const [selectedDietaryRestrictions, setSelectedDietaryRestrictions] = useState<string[]>([]);
  const [selectedSeatingPreference, setSelectedSeatingPreference] = useState<string>('');
  const [selectedFavoriteDrinks, setSelectedFavoriteDrinks] = useState<string[]>([]);
  const [upcomingOccasions, setUpcomingOccasions] = useState<boolean>(false);
  const [customDietaryRestriction, setCustomDietaryRestriction] = useState<string>('');
  const [customSeatingPreference, setCustomSeatingPreference] = useState<string>('');
  const [customFavoriteDrink, setCustomFavoriteDrink] = useState<string>('');

  // Initialize filters when modal opens
  useEffect(() => {
    if (visible && initialFilters) {
      setSelectedDietaryRestrictions(initialFilters.dietaryRestrictions || []);
      setSelectedSeatingPreference(initialFilters.seatingPreference || '');
      setSelectedFavoriteDrinks(initialFilters.favoriteDrinks || []);
      setUpcomingOccasions(initialFilters.upcomingOccasions || false);
    } else if (visible && !initialFilters) {
      // Reset to empty state when opening without initial filters
      setSelectedDietaryRestrictions([]);
      setSelectedSeatingPreference('');
      setSelectedFavoriteDrinks([]);
      setUpcomingOccasions(false);
    }
  }, [visible, initialFilters]);

  const handleDietaryRestrictionToggle = (restriction: string) => {
    setSelectedDietaryRestrictions(prev => 
      prev.includes(restriction)
        ? prev.filter(r => r !== restriction)
        : [...prev, restriction]
    );
  };

  const handleSeatingPreferenceSelect = (preference: string) => {
    setSelectedSeatingPreference(prev => 
      prev === preference ? '' : preference
    );
  };

  const handleFavoriteDrinkToggle = (drink: string) => {
    setSelectedFavoriteDrinks(prev => 
      prev.includes(drink)
        ? prev.filter(d => d !== drink)
        : [...prev, drink]
    );
  };

  const handleAddCustomDietaryRestriction = () => {
    const trimmed = customDietaryRestriction.trim();
    if (trimmed && !selectedDietaryRestrictions.includes(trimmed)) {
      setSelectedDietaryRestrictions(prev => [...prev, trimmed]);
      setCustomDietaryRestriction('');
    }
  };

  const handleAddCustomSeatingPreference = () => {
    const trimmed = customSeatingPreference.trim();
    if (trimmed) {
      setSelectedSeatingPreference(trimmed);
      setCustomSeatingPreference('');
    }
  };

  const handleAddCustomFavoriteDrink = () => {
    const trimmed = customFavoriteDrink.trim();
    if (trimmed && !selectedFavoriteDrinks.includes(trimmed)) {
      setSelectedFavoriteDrinks(prev => [...prev, trimmed]);
      setCustomFavoriteDrink('');
    }
  };

  const handleApplyFilters = () => {
    const filters: SearchFilters = {};
    
    if (selectedDietaryRestrictions.length > 0) {
      filters.dietaryRestrictions = selectedDietaryRestrictions;
    }
    
    if (selectedSeatingPreference) {
      filters.seatingPreference = selectedSeatingPreference;
    }
    
    if (selectedFavoriteDrinks.length > 0) {
      filters.favoriteDrinks = selectedFavoriteDrinks;
    }
    
    if (upcomingOccasions) {
      filters.upcomingOccasions = upcomingOccasions;
    }

    onApplyFilters(filters);
    onDismiss();
  };

  const handleClearAll = () => {
    setSelectedDietaryRestrictions([]);
    setSelectedSeatingPreference('');
    setSelectedFavoriteDrinks([]);
    setUpcomingOccasions(false);
    setCustomDietaryRestriction('');
    setCustomSeatingPreference('');
    setCustomFavoriteDrink('');
  };

  const hasActiveFilters = 
    selectedDietaryRestrictions.length > 0 ||
    selectedSeatingPreference !== '' ||
    selectedFavoriteDrinks.length > 0 ||
    upcomingOccasions;

  return (
    <Portal>
      <Modal
        visible={visible}
        onDismiss={onDismiss}
        contentContainerStyle={styles.modalContainer}
      >
        <View style={styles.header}>
          <Text variant="headlineSmall" style={styles.title}>
            Filter Guests
          </Text>
          <IconButton
            icon="close"
            size={24}
            onPress={onDismiss}
            style={styles.closeButton}
          />
        </View>

        <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
          {/* Upcoming Occasions Filter */}
          <View style={styles.section}>
            <Text variant="titleMedium" style={styles.sectionTitle}>
              Special Occasions
            </Text>
            <View style={styles.switchRow}>
              <Text variant="bodyMedium">Show guests with upcoming occasions (next 30 days)</Text>
              <Switch
                value={upcomingOccasions}
                onValueChange={setUpcomingOccasions}
              />
            </View>
          </View>

          <Divider style={styles.divider} />

          {/* Dietary Restrictions Filter */}
          <View style={styles.section}>
            <Text variant="titleMedium" style={styles.sectionTitle}>
              Dietary Restrictions
            </Text>
            <View style={styles.chipContainer}>
              {DIETARY_RESTRICTIONS.map((restriction) => (
                <Chip
                  key={restriction}
                  mode={selectedDietaryRestrictions.includes(restriction) ? 'flat' : 'outlined'}
                  selected={selectedDietaryRestrictions.includes(restriction)}
                  onPress={() => handleDietaryRestrictionToggle(restriction)}
                  style={styles.chip}
                  showSelectedOverlay
                >
                  {restriction}
                </Chip>
              ))}
            </View>
            
            {/* Custom dietary restriction input */}
            <View style={styles.customInputRow}>
              <TextInput
                mode="outlined"
                placeholder="Add custom restriction..."
                value={customDietaryRestriction}
                onChangeText={setCustomDietaryRestriction}
                style={styles.customInput}
                dense
              />
              <IconButton
                icon="plus"
                size={20}
                onPress={handleAddCustomDietaryRestriction}
                disabled={!customDietaryRestriction.trim()}
                style={styles.addButton}
              />
            </View>
          </View>

          <Divider style={styles.divider} />

          {/* Seating Preferences Filter */}
          <View style={styles.section}>
            <Text variant="titleMedium" style={styles.sectionTitle}>
              Seating Preference
            </Text>
            <View style={styles.chipContainer}>
              {SEATING_PREFERENCES.map((preference) => (
                <Chip
                  key={preference}
                  mode={selectedSeatingPreference === preference ? 'flat' : 'outlined'}
                  selected={selectedSeatingPreference === preference}
                  onPress={() => handleSeatingPreferenceSelect(preference)}
                  style={styles.chip}
                  showSelectedOverlay
                >
                  {preference}
                </Chip>
              ))}
            </View>
            
            {/* Custom seating preference input */}
            <View style={styles.customInputRow}>
              <TextInput
                mode="outlined"
                placeholder="Add custom preference..."
                value={customSeatingPreference}
                onChangeText={setCustomSeatingPreference}
                style={styles.customInput}
                dense
              />
              <IconButton
                icon="plus"
                size={20}
                onPress={handleAddCustomSeatingPreference}
                disabled={!customSeatingPreference.trim()}
                style={styles.addButton}
              />
            </View>
          </View>

          <Divider style={styles.divider} />

          {/* Favorite Drinks Filter */}
          <View style={styles.section}>
            <Text variant="titleMedium" style={styles.sectionTitle}>
              Favorite Drinks
            </Text>
            <View style={styles.chipContainer}>
              {FAVORITE_DRINKS.map((drink) => (
                <Chip
                  key={drink}
                  mode={selectedFavoriteDrinks.includes(drink) ? 'flat' : 'outlined'}
                  selected={selectedFavoriteDrinks.includes(drink)}
                  onPress={() => handleFavoriteDrinkToggle(drink)}
                  style={styles.chip}
                  showSelectedOverlay
                >
                  {drink}
                </Chip>
              ))}
            </View>
            
            {/* Custom favorite drink input */}
            <View style={styles.customInputRow}>
              <TextInput
                mode="outlined"
                placeholder="Add custom drink..."
                value={customFavoriteDrink}
                onChangeText={setCustomFavoriteDrink}
                style={styles.customInput}
                dense
              />
              <IconButton
                icon="plus"
                size={20}
                onPress={handleAddCustomFavoriteDrink}
                disabled={!customFavoriteDrink.trim()}
                style={styles.addButton}
              />
            </View>
          </View>
        </ScrollView>

        {/* Action buttons */}
        <View style={styles.actions}>
          <Button
            mode="outlined"
            onPress={handleClearAll}
            disabled={!hasActiveFilters}
            style={styles.clearButton}
          >
            Clear All
          </Button>
          <Button
            mode="contained"
            onPress={handleApplyFilters}
            style={styles.applyButton}
          >
            Apply Filters
          </Button>
        </View>
      </Modal>
    </Portal>
  );
}

const styles = StyleSheet.create({
  modalContainer: {
    backgroundColor: 'white',
    margin: 20,
    borderRadius: 12,
    maxHeight: '90%',
    elevation: 5,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 10,
  },
  title: {
    fontWeight: '600',
  },
  closeButton: {
    margin: 0,
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
  },
  section: {
    marginVertical: 16,
  },
  sectionTitle: {
    marginBottom: 12,
    fontWeight: '500',
  },
  switchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 8,
  },
  chipContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginBottom: 12,
  },
  chip: {
    marginRight: 8,
    marginBottom: 8,
  },
  customInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
  },
  customInput: {
    flex: 1,
    marginRight: 8,
  },
  addButton: {
    margin: 0,
  },
  divider: {
    marginVertical: 8,
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    padding: 20,
    borderTopWidth: 1,
    borderTopColor: '#e0e0e0',
  },
  clearButton: {
    flex: 1,
    marginRight: 8,
  },
  applyButton: {
    flex: 1,
    marginLeft: 8,
  },
});