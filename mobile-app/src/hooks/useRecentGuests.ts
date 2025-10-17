import { useState, useEffect } from 'react';
import { recentGuestsManager, RecentGuest } from '../utils/recentGuestsManager';

export const useRecentGuests = () => {
  const [recentGuests, setRecentGuests] = useState<RecentGuest[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Subscribe to changes
    const unsubscribe = recentGuestsManager.subscribe((guests) => {
      setRecentGuests(guests);
      setIsLoading(false);
    });

    // Get initial data
    setRecentGuests(recentGuestsManager.getRecentGuests());
    setIsLoading(false);

    return unsubscribe;
  }, []);

  const addRecentGuest = async (guest: Partial<RecentGuest> & { id: number }) => {
    await recentGuestsManager.addRecentGuest(guest);
  };

  const removeRecentGuest = async (guestId: number) => {
    await recentGuestsManager.removeRecentGuest(guestId);
  };

  const clearRecentGuests = async () => {
    await recentGuestsManager.clearRecentGuests();
  };

  const searchRecentGuests = (searchTerm: string) => {
    return recentGuestsManager.searchRecentGuests(searchTerm);
  };

  const getRecentGuestsByPreference = (preference: string) => {
    return recentGuestsManager.getRecentGuestsByPreference(preference);
  };

  const getStats = () => {
    return recentGuestsManager.getRecentGuestsStats();
  };

  return {
    recentGuests,
    isLoading,
    addRecentGuest,
    removeRecentGuest,
    clearRecentGuests,
    searchRecentGuests,
    getRecentGuestsByPreference,
    getStats,
  };
};

// Hook for searching recent guests with debouncing
export const useRecentGuestSearch = (searchTerm: string, debounceMs: number = 300) => {
  const [debouncedTerm, setDebouncedTerm] = useState(searchTerm);
  const [searchResults, setSearchResults] = useState<RecentGuest[]>([]);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedTerm(searchTerm);
    }, debounceMs);

    return () => clearTimeout(timer);
  }, [searchTerm, debounceMs]);

  useEffect(() => {
    if (debouncedTerm) {
      const results = recentGuestsManager.searchRecentGuests(debouncedTerm);
      setSearchResults(results);
    } else {
      setSearchResults([]);
    }
  }, [debouncedTerm]);

  return {
    searchResults,
    isSearching: searchTerm !== debouncedTerm,
  };
};

// Hook for recent guests statistics
export const useRecentGuestsStats = () => {
  const [stats, setStats] = useState(recentGuestsManager.getRecentGuestsStats());

  useEffect(() => {
    const unsubscribe = recentGuestsManager.subscribe(() => {
      setStats(recentGuestsManager.getRecentGuestsStats());
    });

    return unsubscribe;
  }, []);

  return stats;
};