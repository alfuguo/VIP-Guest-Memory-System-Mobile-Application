import React, { useState, useEffect } from 'react';
import { View, StyleSheet } from 'react-native';
import { Searchbar, IconButton, Chip } from 'react-native-paper';

export interface SearchFilters {
  dietaryRestrictions?: string[];
  seatingPreference?: string;
  favoriteDrinks?: string[];
  upcomingOccasions?: boolean;
}

interface SearchBarProps {
  placeholder?: string;
  onSearch: (query: string, filters?: SearchFilters) => void;
  onFilterPress?: () => void;
  showFilter?: boolean;
  debounceMs?: number;
  activeFilters?: SearchFilters;
  onClearFilters?: () => void;
}

export default function SearchBar({ 
  placeholder = 'Search by name, phone, or preferences...', 
  onSearch, 
  onFilterPress,
  showFilter = false,
  debounceMs = 300,
  activeFilters,
  onClearFilters
}: SearchBarProps) {
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      onSearch(searchQuery, activeFilters);
    }, debounceMs);

    return () => clearTimeout(timeoutId);
  }, [searchQuery, onSearch, debounceMs, activeFilters]);

  // Count active filters
  const getActiveFilterCount = (): number => {
    if (!activeFilters) return 0;
    
    let count = 0;
    if (activeFilters.dietaryRestrictions?.length) count += activeFilters.dietaryRestrictions.length;
    if (activeFilters.seatingPreference) count += 1;
    if (activeFilters.favoriteDrinks?.length) count += activeFilters.favoriteDrinks.length;
    if (activeFilters.upcomingOccasions) count += 1;
    
    return count;
  };

  const activeFilterCount = getActiveFilterCount();

  // Generate filter chips for display
  const getFilterChips = (): string[] => {
    if (!activeFilters) return [];
    
    const chips: string[] = [];
    
    if (activeFilters.dietaryRestrictions?.length) {
      chips.push(...activeFilters.dietaryRestrictions);
    }
    
    if (activeFilters.seatingPreference) {
      chips.push(activeFilters.seatingPreference);
    }
    
    if (activeFilters.favoriteDrinks?.length) {
      chips.push(...activeFilters.favoriteDrinks);
    }
    
    if (activeFilters.upcomingOccasions) {
      chips.push('Upcoming Occasions');
    }
    
    return chips;
  };

  const filterChips = getFilterChips();

  return (
    <View style={styles.container}>
      <View style={styles.searchRow}>
        <Searchbar
          placeholder={placeholder}
          onChangeText={setSearchQuery}
          value={searchQuery}
          style={[styles.searchbar, showFilter && styles.searchbarWithFilter]}
          inputStyle={styles.searchInput}
          iconColor="#666"
          placeholderTextColor="#999"
        />
        {showFilter && (
          <IconButton
            icon={activeFilterCount > 0 ? "filter" : "filter-variant"}
            size={24}
            onPress={onFilterPress}
            style={[
              styles.filterButton,
              activeFilterCount > 0 && styles.filterButtonActive
            ]}
            iconColor={activeFilterCount > 0 ? "#6200ee" : "#666"}
          />
        )}
      </View>
      
      {/* Active filter chips */}
      {filterChips.length > 0 && (
        <View style={styles.filterChipsContainer}>
          {filterChips.slice(0, 3).map((chip, index) => (
            <Chip
              key={`${chip}-${index}`}
              mode="outlined"
              compact
              style={styles.filterChip}
              textStyle={styles.filterChipText}
            >
              {chip}
            </Chip>
          ))}
          {filterChips.length > 3 && (
            <Chip
              mode="outlined"
              compact
              style={styles.filterChip}
              textStyle={styles.filterChipText}
            >
              +{filterChips.length - 3} more
            </Chip>
          )}
          {onClearFilters && (
            <Chip
              mode="outlined"
              compact
              icon="close"
              onPress={onClearFilters}
              style={[styles.filterChip, styles.clearFiltersChip]}
              textStyle={styles.filterChipText}
            >
              Clear
            </Chip>
          )}
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#fff',
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 4,
  },
  searchRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  searchbar: {
    flex: 1,
    elevation: 1,
    backgroundColor: '#f5f5f5',
  },
  searchbarWithFilter: {
    marginRight: 8,
  },
  searchInput: {
    fontSize: 16,
  },
  filterButton: {
    margin: 0,
    backgroundColor: '#f5f5f5',
  },
  filterButtonActive: {
    backgroundColor: '#e8e4f3',
  },
  filterChipsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: 8,
    marginBottom: 4,
  },
  filterChip: {
    marginRight: 8,
    marginBottom: 4,
    height: 28,
  },
  filterChipText: {
    fontSize: 12,
  },
  clearFiltersChip: {
    backgroundColor: '#ffebee',
    borderColor: '#f44336',
  },
});