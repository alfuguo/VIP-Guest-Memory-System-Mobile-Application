import React, { useState, useCallback, useEffect } from 'react';
import { 
  View, 
  FlatList, 
  StyleSheet, 
  RefreshControl, 
  Alert 
} from 'react-native';
import { FAB, Snackbar } from 'react-native-paper';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';

import { Guest, GuestSearchParams } from '../../types/guest';
import { GuestStackParamList } from '../../types/navigation';
import { GuestService } from '../../services/guestService';
import { handleError } from '../../utils/errorHandler';

import LazyGuestCard from '../../components/LazyGuestCard';
import SearchBar, { SearchFilters } from '../../components/SearchBar';
import FilterModal from '../../components/FilterModal';
import LoadingState from '../../components/LoadingState';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import GuestCardSkeleton from '../../components/skeletons/GuestCardSkeleton';
import { useViewportList } from '../../hooks/useViewportList';

type GuestListNavigationProp = StackNavigationProp<GuestStackParamList, 'GuestList'>;

export default function GuestListScreen() {
  const navigation = useNavigation<GuestListNavigationProp>();
  
  // State management
  const [guests, setGuests] = useState<Guest[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilters, setActiveFilters] = useState<SearchFilters>({});
  const [showFilterModal, setShowFilterModal] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Constants
  const ITEMS_PER_PAGE = 20;
  const ITEM_HEIGHT = 120; // Approximate height of each guest card

  // Viewport list for lazy loading
  const { onScroll: onViewportScroll, isItemVisible } = useViewportList({
    itemHeight: ITEM_HEIGHT,
    data: guests,
    overscan: 3,
  });

  // Load guests with search, filters, and pagination
  const loadGuests = useCallback(async (
    page: number = 1, 
    search: string = '', 
    filters: SearchFilters = {},
    append: boolean = false
  ) => {
    try {
      if (page === 1) {
        setLoading(true);
      } else {
        setLoadingMore(true);
      }

      const params: GuestSearchParams = {
        page,
        limit: ITEMS_PER_PAGE,
        search: search.trim() || undefined,
        dietaryRestrictions: filters.dietaryRestrictions,
        seatingPreference: filters.seatingPreference,
        favoriteDrinks: filters.favoriteDrinks,
        upcomingOccasions: filters.upcomingOccasions,
      };

      const response = await GuestService.getGuests(params);
      
      if (append && page > 1) {
        setGuests(prev => [...prev, ...response.guests]);
      } else {
        setGuests(response.guests);
      }
      
      setCurrentPage(response.currentPage);
      setTotalPages(response.totalPages);
      setHasNextPage(response.hasNext);
      setError(null);
      
    } catch (err) {
      const errorMessage = handleError(err);
      setError(errorMessage);
      
      if (page === 1) {
        setGuests([]);
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
      setLoadingMore(false);
    }
  }, []);

  // Initial load
  useEffect(() => {
    loadGuests(1, searchQuery, activeFilters);
  }, [loadGuests, searchQuery, activeFilters]);

  // Handle search with filters
  const handleSearch = useCallback((query: string, filters?: SearchFilters) => {
    setSearchQuery(query);
    if (filters !== undefined) {
      setActiveFilters(filters);
    }
    setCurrentPage(1);
    // loadGuests will be called by useEffect when searchQuery or activeFilters change
  }, []);

  // Handle refresh
  const handleRefresh = useCallback(() => {
    setRefreshing(true);
    setCurrentPage(1);
    loadGuests(1, searchQuery, activeFilters);
  }, [loadGuests, searchQuery, activeFilters]);

  // Handle load more
  const handleLoadMore = useCallback(() => {
    if (!loadingMore && hasNextPage && currentPage < totalPages) {
      loadGuests(currentPage + 1, searchQuery, activeFilters, true);
    }
  }, [loadGuests, loadingMore, hasNextPage, currentPage, totalPages, searchQuery, activeFilters]);

  // Handle guest selection
  const handleGuestPress = useCallback((guest: Guest) => {
    navigation.navigate('GuestDetail', { guestId: guest.id });
  }, [navigation]);

  // Handle create new guest
  const handleCreateGuest = useCallback(() => {
    navigation.navigate('GuestProfile', {});
  }, [navigation]);

  // Handle filter modal
  const handleFilterPress = useCallback(() => {
    setShowFilterModal(true);
  }, []);

  const handleApplyFilters = useCallback((filters: SearchFilters) => {
    setActiveFilters(filters);
    setCurrentPage(1);
    // loadGuests will be called by useEffect when activeFilters change
  }, []);

  const handleClearFilters = useCallback(() => {
    setActiveFilters({});
    setCurrentPage(1);
    // loadGuests will be called by useEffect when activeFilters change
  }, []);

  // Render guest item with lazy loading
  const renderGuestItem = useCallback(({ item, index }: { item: Guest; index: number }) => (
    <LazyGuestCard 
      guest={item} 
      onPress={handleGuestPress}
      index={index}
      isVisible={isItemVisible(index)}
    />
  ), [handleGuestPress, isItemVisible]);

  // Render footer for load more
  const renderFooter = useCallback(() => {
    if (!loadingMore) return null;
    return (
      <View style={styles.footerLoader}>
        <LoadingState message="Loading more guests..." size="small" />
      </View>
    );
  }, [loadingMore]);

  // Render skeleton loaders
  const renderSkeletonLoaders = useCallback(() => {
    return (
      <View>
        {Array.from({ length: 5 }).map((_, index) => (
          <GuestCardSkeleton key={`skeleton-${index}`} />
        ))}
      </View>
    );
  }, []);

  // Show skeleton loaders on initial load
  if (loading && guests.length === 0) {
    return (
      <View style={styles.container}>
        <SearchBar 
          onSearch={handleSearch}
          onFilterPress={handleFilterPress}
          showFilter={true}
          activeFilters={activeFilters}
          onClearFilters={handleClearFilters}
        />
        {renderSkeletonLoaders()}
      </View>
    );
  }

  // Show error state
  if (error && guests.length === 0) {
    return (
      <View style={styles.container}>
        <SearchBar 
          onSearch={handleSearch}
          onFilterPress={handleFilterPress}
          showFilter={true}
          activeFilters={activeFilters}
          onClearFilters={handleClearFilters}
        />
        <ErrorState
          title="Failed to load guests"
          message={error}
          onRetry={handleRefresh}
          actionLabel="Go to Settings"
          onAction={() => {
            // Navigate to settings or show help
            Alert.alert('Help', 'Please check your internet connection and try again.');
          }}
        />
      </View>
    );
  }

  // Show empty state when no guests found
  if (!loading && guests.length === 0 && !error) {
    const isSearching = searchQuery.trim().length > 0;
    return (
      <View style={styles.container}>
        <SearchBar 
          onSearch={handleSearch}
          onFilterPress={handleFilterPress}
          showFilter={true}
          activeFilters={activeFilters}
          onClearFilters={handleClearFilters}
        />
        <EmptyState
          icon={isSearching ? 'magnify' : 'account-group-outline'}
          title={isSearching ? 'No guests found' : 'No guests yet'}
          message={
            isSearching 
              ? `No guests match "${searchQuery}". Try a different search term.`
              : 'Start building your guest database by adding your first guest.'
          }
          actionLabel={isSearching ? undefined : 'Add First Guest'}
          onAction={isSearching ? undefined : handleCreateGuest}
        />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <SearchBar 
        onSearch={handleSearch}
        onFilterPress={handleFilterPress}
        showFilter={true}
        activeFilters={activeFilters}
        onClearFilters={handleClearFilters}
      />
      
      <FlatList
        data={guests}
        renderItem={renderGuestItem}
        keyExtractor={(item) => item.id.toString()}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={handleRefresh}
            colors={['#6200ee']}
            tintColor="#6200ee"
          />
        }
        onEndReached={handleLoadMore}
        onEndReachedThreshold={0.1}
        ListFooterComponent={renderFooter}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={guests.length === 0 ? styles.emptyContainer : undefined}
        onScroll={onViewportScroll}
        scrollEventThrottle={16}
        removeClippedSubviews={true}
        maxToRenderPerBatch={10}
        windowSize={10}
        initialNumToRender={10}
        getItemLayout={(data, index) => ({
          length: ITEM_HEIGHT,
          offset: ITEM_HEIGHT * index,
          index,
        })}
      />

      <FAB
        icon="plus"
        style={styles.fab}
        onPress={handleCreateGuest}
        label="Add Guest"
      />

      <FilterModal
        visible={showFilterModal}
        onDismiss={() => setShowFilterModal(false)}
        onApplyFilters={handleApplyFilters}
        initialFilters={activeFilters}
      />

      <Snackbar
        visible={!!error}
        onDismiss={() => setError(null)}
        duration={4000}
        action={{
          label: 'Retry',
          onPress: () => {
            setError(null);
            handleRefresh();
          },
        }}
      >
        {error}
      </Snackbar>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  emptyContainer: {
    flex: 1,
  },
  footerLoader: {
    paddingVertical: 20,
  },
  fab: {
    position: 'absolute',
    margin: 16,
    right: 0,
    bottom: 0,
    backgroundColor: '#6200ee',
  },
});