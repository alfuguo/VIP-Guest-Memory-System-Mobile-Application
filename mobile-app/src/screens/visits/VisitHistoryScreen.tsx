import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  FlatList,
  StyleSheet,
  RefreshControl,
  Alert
} from 'react-native';
import {
  Text,
  FAB,
  Snackbar,
  Searchbar,
  Chip,
  Menu,
  Divider
} from 'react-native-paper';
import { useNavigation, useRoute, RouteProp, useFocusEffect } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';

import { Guest } from '../../types/guest';
import { Visit } from '../../types/visit';
import { GuestStackParamList } from '../../types/navigation';
import { GuestService } from '../../services/guestService';
import { VisitService } from '../../services/visitService';
import { handleError } from '../../utils/errorHandler';

import LoadingState from '../../components/LoadingState';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import VisitTimelineCard from '../../components/VisitTimelineCard';
import VisitCardSkeleton from '../../components/skeletons/VisitCardSkeleton';

type VisitHistoryNavigationProp = StackNavigationProp<GuestStackParamList, 'VisitHistory'>;
type VisitHistoryRouteProp = RouteProp<GuestStackParamList, 'VisitHistory'>;

type SortOption = 'newest' | 'oldest';
type FilterOption = 'all' | 'thisYear' | 'lastYear' | 'withNotes';

export default function VisitHistoryScreen() {
  const navigation = useNavigation<VisitHistoryNavigationProp>();
  const route = useRoute<VisitHistoryRouteProp>();
  const { guestId } = route.params;

  // State management
  const [guest, setGuest] = useState<Guest | null>(null);
  const [visits, setVisits] = useState<Visit[]>([]);
  const [filteredVisits, setFilteredVisits] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Search and filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<SortOption>('newest');
  const [filterBy, setFilterBy] = useState<FilterOption>('all');
  const [showSortMenu, setShowSortMenu] = useState(false);
  const [showFilterMenu, setShowFilterMenu] = useState(false);

  // Constants
  const VISITS_PER_PAGE = 20;

  // Load guest data and visits
  const loadData = useCallback(async (page: number = 1, append: boolean = false) => {
    try {
      if (page === 1) {
        setLoading(true);
      } else {
        setLoadingMore(true);
      }

      // Load guest data and visits in parallel
      const [guestData, visitsResponse] = await Promise.all([
        page === 1 ? GuestService.getGuest(guestId) : Promise.resolve(guest),
        VisitService.getGuestVisits(guestId, page, VISITS_PER_PAGE)
      ]);

      if (guestData) {
        setGuest(guestData);
      }

      if (append && page > 1) {
        setVisits(prev => [...prev, ...visitsResponse.visits]);
      } else {
        setVisits(visitsResponse.visits);
      }

      setCurrentPage(visitsResponse.currentPage);
      setTotalPages(visitsResponse.totalPages);
      setHasNextPage(visitsResponse.hasNext);
      setError(null);

    } catch (err) {
      const errorMessage = handleError(err);
      setError(errorMessage);

      if (page === 1) {
        setVisits([]);
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
      setLoadingMore(false);
    }
  }, [guestId, guest]);

  // Filter and sort visits
  useEffect(() => {
    let filtered = [...visits];

    // Apply search filter
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(visit =>
        visit.serviceNotes?.toLowerCase().includes(query) ||
        visit.staffName.toLowerCase().includes(query) ||
        visit.tableNumber?.toLowerCase().includes(query)
      );
    }

    // Apply date filter
    const currentYear = new Date().getFullYear();
    const lastYear = currentYear - 1;

    switch (filterBy) {
      case 'thisYear':
        filtered = filtered.filter(visit =>
          new Date(visit.visitDate).getFullYear() === currentYear
        );
        break;
      case 'lastYear':
        filtered = filtered.filter(visit =>
          new Date(visit.visitDate).getFullYear() === lastYear
        );
        break;
      case 'withNotes':
        filtered = filtered.filter(visit =>
          visit.serviceNotes && visit.serviceNotes.trim().length > 0
        );
        break;
    }

    // Apply sorting
    filtered.sort((a, b) => {
      const dateA = new Date(`${a.visitDate}T${a.visitTime}`);
      const dateB = new Date(`${b.visitDate}T${b.visitTime}`);

      return sortBy === 'newest'
        ? dateB.getTime() - dateA.getTime()
        : dateA.getTime() - dateB.getTime();
    });

    setFilteredVisits(filtered);
  }, [visits, searchQuery, sortBy, filterBy]);

  // Initial load and refresh when screen comes into focus
  useFocusEffect(
    useCallback(() => {
      loadData(1);
    }, [loadData])
  );

  // Handle refresh
  const handleRefresh = useCallback(() => {
    setRefreshing(true);
    setCurrentPage(1);
    loadData(1);
  }, [loadData]);

  // Handle load more visits
  const handleLoadMore = useCallback(() => {
    if (!loadingMore && hasNextPage && currentPage < totalPages) {
      loadData(currentPage + 1, true);
    }
  }, [loadData, loadingMore, hasNextPage, currentPage, totalPages]);

  // Handle add visit
  const handleAddVisit = useCallback(() => {
    navigation.navigate('VisitLog', { guestId });
  }, [navigation, guestId]);

  // Handle edit visit
  const handleEditVisit = useCallback((visit: Visit) => {
    Alert.alert(
      'Edit Visit',
      'Visit editing functionality will be implemented in a future update.',
      [{ text: 'OK' }]
    );
  }, []);

  // Handle delete visit
  const handleDeleteVisit = useCallback((visit: Visit) => {
    Alert.alert(
      'Delete Visit',
      'Are you sure you want to delete this visit? This action cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await VisitService.deleteVisit(visit.id);
              setVisits(prev => prev.filter(v => v.id !== visit.id));
            } catch (err) {
              const errorMessage = handleError(err);
              setError(errorMessage);
            }
          }
        }
      ]
    );
  }, []);

  // Get filter label
  const getFilterLabel = (filter: FilterOption): string => {
    switch (filter) {
      case 'thisYear': return 'This Year';
      case 'lastYear': return 'Last Year';
      case 'withNotes': return 'With Notes';
      default: return 'All Visits';
    }
  };

  // Get sort label
  const getSortLabel = (sort: SortOption): string => {
    return sort === 'newest' ? 'Newest First' : 'Oldest First';
  };

  // Render visit item
  const renderVisitItem = useCallback(({ item, index }: { item: Visit; index: number }) => (
    <VisitTimelineCard
      visit={item}
      onEdit={handleEditVisit}
      onDelete={handleDeleteVisit}
      canEdit={true}
      canDelete={true}
      isFirst={index === 0}
      isLast={index === filteredVisits.length - 1}
    />
  ), [handleEditVisit, handleDeleteVisit, filteredVisits.length]);

  // Render footer for load more
  const renderFooter = useCallback(() => {
    if (!loadingMore) return null;
    return (
      <View style={styles.footerLoader}>
        <LoadingState message="Loading more visits..." size="small" />
      </View>
    );
  }, [loadingMore]);

  // Render header with search and filters
  const renderHeader = useCallback(() => (
    <View style={styles.headerContainer}>
      {guest && (
        <View style={styles.guestInfo}>
          <Text style={styles.guestName}>
            {guest.firstName} {guest.lastName}
          </Text>
          <Text style={styles.visitStats}>
            {filteredVisits.length} of {visits.length} visits
          </Text>
        </View>
      )}

      <Searchbar
        placeholder="Search visits, notes, staff, or table..."
        onChangeText={setSearchQuery}
        value={searchQuery}
        style={styles.searchBar}
        inputStyle={styles.searchInput}
      />

      <View style={styles.filtersContainer}>
        <Menu
          visible={showSortMenu}
          onDismiss={() => setShowSortMenu(false)}
          anchor={
            <Chip
              icon="sort"
              onPress={() => setShowSortMenu(true)}
              style={styles.filterChip}
            >
              {getSortLabel(sortBy)}
            </Chip>
          }
        >
          <Menu.Item
            onPress={() => {
              setSortBy('newest');
              setShowSortMenu(false);
            }}
            title="Newest First"
            leadingIcon={sortBy === 'newest' ? 'check' : undefined}
          />
          <Menu.Item
            onPress={() => {
              setSortBy('oldest');
              setShowSortMenu(false);
            }}
            title="Oldest First"
            leadingIcon={sortBy === 'oldest' ? 'check' : undefined}
          />
        </Menu>

        <Menu
          visible={showFilterMenu}
          onDismiss={() => setShowFilterMenu(false)}
          anchor={
            <Chip
              icon="filter"
              onPress={() => setShowFilterMenu(true)}
              style={styles.filterChip}
            >
              {getFilterLabel(filterBy)}
            </Chip>
          }
        >
          <Menu.Item
            onPress={() => {
              setFilterBy('all');
              setShowFilterMenu(false);
            }}
            title="All Visits"
            leadingIcon={filterBy === 'all' ? 'check' : undefined}
          />
          <Divider />
          <Menu.Item
            onPress={() => {
              setFilterBy('thisYear');
              setShowFilterMenu(false);
            }}
            title="This Year"
            leadingIcon={filterBy === 'thisYear' ? 'check' : undefined}
          />
          <Menu.Item
            onPress={() => {
              setFilterBy('lastYear');
              setShowFilterMenu(false);
            }}
            title="Last Year"
            leadingIcon={filterBy === 'lastYear' ? 'check' : undefined}
          />
          <Divider />
          <Menu.Item
            onPress={() => {
              setFilterBy('withNotes');
              setShowFilterMenu(false);
            }}
            title="With Notes"
            leadingIcon={filterBy === 'withNotes' ? 'check' : undefined}
          />
        </Menu>
      </View>
    </View>
  ), [guest, filteredVisits.length, visits.length, searchQuery, sortBy, filterBy, showSortMenu, showFilterMenu]);

  // Show skeleton loaders on initial load
  if (loading && !guest) {
    return (
      <View style={styles.container}>
        <View style={styles.header}>
          <Text style={styles.title}>Visit History</Text>
        </View>
        <VisitCardSkeleton />
        <VisitCardSkeleton />
        <VisitCardSkeleton />
        <VisitCardSkeleton />
      </View>
    );
  }

  // Show error state if guest couldn't be loaded
  if (!loading && !guest && error) {
    return (
      <View style={styles.container}>
        <ErrorState
          title="Failed to load visit history"
          message={error}
          onRetry={() => loadData(1)}
          actionLabel="Go Back"
          onAction={() => navigation.goBack()}
        />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={filteredVisits}
        renderItem={renderVisitItem}
        keyExtractor={(item) => item.id.toString()}
        ListHeaderComponent={renderHeader}
        ListEmptyComponent={
          !loading ? (
            <EmptyState
              icon="calendar-blank"
              title={searchQuery || filterBy !== 'all' ? 'No matching visits' : 'No visits yet'}
              message={
                searchQuery || filterBy !== 'all'
                  ? 'Try adjusting your search or filter criteria.'
                  : 'This guest hasn\'t visited the restaurant yet. Log their first visit to start building their history.'
              }
              actionLabel={searchQuery || filterBy !== 'all' ? 'Clear Filters' : 'Log First Visit'}
              onAction={() => {
                if (searchQuery || filterBy !== 'all') {
                  setSearchQuery('');
                  setFilterBy('all');
                } else {
                  handleAddVisit();
                }
              }}
            />
          ) : null
        }
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
        contentContainerStyle={filteredVisits.length === 0 ? styles.emptyContainer : styles.listContainer}
      />

      <FAB
        icon="plus"
        style={styles.fab}
        onPress={handleAddVisit}
        label="Log Visit"
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
  listContainer: {
    paddingBottom: 80,
  },
  headerContainer: {
    backgroundColor: '#fff',
    paddingBottom: 16,
    elevation: 1,
  },
  header: {
    padding: 16,
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
  },
  guestInfo: {
    padding: 16,
    paddingBottom: 8,
  },
  guestName: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
  },
  visitStats: {
    fontSize: 14,
    color: '#666',
  },
  searchBar: {
    marginHorizontal: 16,
    marginBottom: 12,
    elevation: 0,
    backgroundColor: '#f8f9fa',
  },
  searchInput: {
    fontSize: 14,
  },
  filtersContainer: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    gap: 8,
  },
  filterChip: {
    backgroundColor: '#e3f2fd',
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