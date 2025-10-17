import React, { useState, useEffect, useCallback } from 'react';
import { 
  View, 
  FlatList, 
  StyleSheet, 
  RefreshControl,
  Alert 
} from 'react-native';
import { Text, FAB, Snackbar, Divider } from 'react-native-paper';
import { useNavigation, useRoute, RouteProp, useFocusEffect } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';

import { Guest } from '../../types/guest';
import { Visit } from '../../types/visit';
import { GuestStackParamList } from '../../types/navigation';
import { GuestService } from '../../services/guestService';
import { VisitService } from '../../services/visitService';
import { handleError } from '../../utils/errorHandler';

import GuestDetailHeader from '../../components/GuestDetailHeader';
import VisitCard from '../../components/VisitCard';
import LoadingState from '../../components/LoadingState';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import GuestDetailSkeleton from '../../components/skeletons/GuestDetailSkeleton';
import VisitCardSkeleton from '../../components/skeletons/VisitCardSkeleton';

type GuestDetailNavigationProp = StackNavigationProp<GuestStackParamList, 'GuestDetail'>;
type GuestDetailRouteProp = RouteProp<GuestStackParamList, 'GuestDetail'>;

export default function GuestDetailScreen() {
  const navigation = useNavigation<GuestDetailNavigationProp>();
  const route = useRoute<GuestDetailRouteProp>();
  const { guestId } = route.params;

  // State management
  const [guest, setGuest] = useState<Guest | null>(null);
  const [visits, setVisits] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Constants
  const VISITS_PER_PAGE = 10;

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

  // Handle edit guest
  const handleEditGuest = useCallback(() => {
    navigation.navigate('GuestProfile', { guestId });
  }, [navigation, guestId]);

  // Handle add visit
  const handleAddVisit = useCallback(() => {
    navigation.navigate('VisitLog', { guestId });
  }, [navigation, guestId]);

  // Handle edit visit
  const handleEditVisit = useCallback((visit: Visit) => {
    // For now, show an alert. This will be implemented in task 10
    Alert.alert(
      'Edit Visit',
      'Visit editing will be implemented in task 10',
      [{ text: 'OK' }]
    );
  }, []);

  // Render visit item
  const renderVisitItem = useCallback(({ item }: { item: Visit }) => (
    <VisitCard 
      visit={item} 
      onEdit={handleEditVisit}
      canEdit={true} // This could be based on user role
    />
  ), [handleEditVisit]);

  // Render footer for load more
  const renderFooter = useCallback(() => {
    if (!loadingMore) return null;
    return (
      <View style={styles.footerLoader}>
        <LoadingState message="Loading more visits..." size="small" />
      </View>
    );
  }, [loadingMore]);

  // Render list header (guest details)
  const renderHeader = useCallback(() => {
    if (!guest) return null;
    
    return (
      <>
        <GuestDetailHeader
          guest={guest}
          onEdit={handleEditGuest}
          onAddVisit={handleAddVisit}
        />
        <View style={styles.visitsHeader}>
          <Text style={styles.visitsTitle}>Visit History</Text>
          <Text style={styles.visitsCount}>
            {guest.visitCount} {guest.visitCount === 1 ? 'visit' : 'visits'} total
          </Text>
        </View>
        <Divider style={styles.divider} />
      </>
    );
  }, [guest, handleEditGuest, handleAddVisit]);

  // Show skeleton loaders on initial load
  if (loading && !guest) {
    return (
      <View style={styles.container}>
        <GuestDetailSkeleton />
        <View style={styles.visitsHeader}>
          <Text style={styles.visitsTitle}>Visit History</Text>
        </View>
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
          title="Failed to load guest"
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
        data={visits}
        renderItem={renderVisitItem}
        keyExtractor={(item) => item.id.toString()}
        ListHeaderComponent={renderHeader}
        ListEmptyComponent={
          !loading ? (
            <EmptyState
              icon="calendar-blank"
              title="No visits yet"
              message="This guest hasn't visited the restaurant yet. Log their first visit to start building their history."
              actionLabel="Log First Visit"
              onAction={handleAddVisit}
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
        contentContainerStyle={visits.length === 0 ? styles.emptyContainer : undefined}
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
  visitsHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#fff',
  },
  visitsTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  visitsCount: {
    fontSize: 14,
    color: '#666',
  },
  divider: {
    backgroundColor: '#e0e0e0',
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