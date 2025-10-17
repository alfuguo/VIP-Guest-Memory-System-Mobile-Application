import AsyncStorage from '@react-native-async-storage/async-storage';

export interface RecentGuest {
  id: number;
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
  photoUrl?: string;
  lastVisit?: string;
  visitCount: number;
  viewedAt: string; // When this guest was last viewed
  seatingPreference?: string;
  dietaryRestrictions?: string[];
  favoriteDrinks?: string[];
}

const RECENT_GUESTS_KEY = 'recently_viewed_guests';
const MAX_RECENT_GUESTS = 50;
const RECENT_GUESTS_EXPIRY = 7 * 24 * 60 * 60 * 1000; // 7 days

export class RecentGuestsManager {
  private static instance: RecentGuestsManager;
  private recentGuests: RecentGuest[] = [];
  private listeners: Array<(guests: RecentGuest[]) => void> = [];

  private constructor() {
    this.loadRecentGuests();
  }

  static getInstance(): RecentGuestsManager {
    if (!RecentGuestsManager.instance) {
      RecentGuestsManager.instance = new RecentGuestsManager();
    }
    return RecentGuestsManager.instance;
  }

  /**
   * Load recently viewed guests from storage
   */
  private async loadRecentGuests(): Promise<void> {
    try {
      const stored = await AsyncStorage.getItem(RECENT_GUESTS_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        
        // Filter out expired entries
        const now = Date.now();
        this.recentGuests = parsed.filter((guest: RecentGuest) => {
          const viewedAt = new Date(guest.viewedAt).getTime();
          return now - viewedAt < RECENT_GUESTS_EXPIRY;
        });

        // Sort by most recently viewed
        this.recentGuests.sort((a, b) => 
          new Date(b.viewedAt).getTime() - new Date(a.viewedAt).getTime()
        );

        console.log(`Loaded ${this.recentGuests.length} recent guests`);
        this.notifyListeners();
      }
    } catch (error) {
      console.error('Failed to load recent guests:', error);
      this.recentGuests = [];
    }
  }

  /**
   * Save recently viewed guests to storage
   */
  private async saveRecentGuests(): Promise<void> {
    try {
      await AsyncStorage.setItem(RECENT_GUESTS_KEY, JSON.stringify(this.recentGuests));
    } catch (error) {
      console.error('Failed to save recent guests:', error);
    }
  }

  /**
   * Add or update a guest in the recent list
   */
  async addRecentGuest(guest: Partial<RecentGuest> & { id: number }): Promise<void> {
    const now = new Date().toISOString();
    
    // Remove existing entry if present
    this.recentGuests = this.recentGuests.filter(g => g.id !== guest.id);
    
    // Create recent guest entry
    const recentGuest: RecentGuest = {
      id: guest.id,
      firstName: guest.firstName || '',
      lastName: guest.lastName || '',
      phone: guest.phone || '',
      email: guest.email,
      photoUrl: guest.photoUrl,
      lastVisit: guest.lastVisit,
      visitCount: guest.visitCount || 0,
      viewedAt: now,
      seatingPreference: guest.seatingPreference,
      dietaryRestrictions: guest.dietaryRestrictions,
      favoriteDrinks: guest.favoriteDrinks,
    };

    // Add to beginning of array
    this.recentGuests.unshift(recentGuest);

    // Limit the number of recent guests
    if (this.recentGuests.length > MAX_RECENT_GUESTS) {
      this.recentGuests = this.recentGuests.slice(0, MAX_RECENT_GUESTS);
    }

    await this.saveRecentGuests();
    this.notifyListeners();
  }

  /**
   * Get all recent guests
   */
  getRecentGuests(): RecentGuest[] {
    return [...this.recentGuests];
  }

  /**
   * Get recent guests filtered by search term
   */
  searchRecentGuests(searchTerm: string): RecentGuest[] {
    if (!searchTerm.trim()) {
      return this.getRecentGuests();
    }

    const term = searchTerm.toLowerCase();
    return this.recentGuests.filter(guest => 
      guest.firstName.toLowerCase().includes(term) ||
      guest.lastName.toLowerCase().includes(term) ||
      guest.phone.includes(term) ||
      (guest.email && guest.email.toLowerCase().includes(term))
    );
  }

  /**
   * Get recent guests with specific preferences
   */
  getRecentGuestsByPreference(preference: string): RecentGuest[] {
    return this.recentGuests.filter(guest =>
      guest.seatingPreference?.toLowerCase().includes(preference.toLowerCase()) ||
      guest.dietaryRestrictions?.some(restriction => 
        restriction.toLowerCase().includes(preference.toLowerCase())
      ) ||
      guest.favoriteDrinks?.some(drink => 
        drink.toLowerCase().includes(preference.toLowerCase())
      )
    );
  }

  /**
   * Remove a guest from recent list
   */
  async removeRecentGuest(guestId: number): Promise<void> {
    this.recentGuests = this.recentGuests.filter(g => g.id !== guestId);
    await this.saveRecentGuests();
    this.notifyListeners();
  }

  /**
   * Clear all recent guests
   */
  async clearRecentGuests(): Promise<void> {
    this.recentGuests = [];
    await this.saveRecentGuests();
    this.notifyListeners();
  }

  /**
   * Get statistics about recent guests
   */
  getRecentGuestsStats(): {
    total: number;
    viewedToday: number;
    viewedThisWeek: number;
    mostViewedPreferences: string[];
  } {
    const now = Date.now();
    const oneDayAgo = now - (24 * 60 * 60 * 1000);
    const oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000);

    const viewedToday = this.recentGuests.filter(guest => 
      new Date(guest.viewedAt).getTime() > oneDayAgo
    ).length;

    const viewedThisWeek = this.recentGuests.filter(guest => 
      new Date(guest.viewedAt).getTime() > oneWeekAgo
    ).length;

    // Get most common preferences
    const preferences: { [key: string]: number } = {};
    this.recentGuests.forEach(guest => {
      if (guest.seatingPreference) {
        preferences[guest.seatingPreference] = (preferences[guest.seatingPreference] || 0) + 1;
      }
      guest.dietaryRestrictions?.forEach(restriction => {
        preferences[restriction] = (preferences[restriction] || 0) + 1;
      });
      guest.favoriteDrinks?.forEach(drink => {
        preferences[drink] = (preferences[drink] || 0) + 1;
      });
    });

    const mostViewedPreferences = Object.entries(preferences)
      .sort(([, a], [, b]) => b - a)
      .slice(0, 5)
      .map(([preference]) => preference);

    return {
      total: this.recentGuests.length,
      viewedToday,
      viewedThisWeek,
      mostViewedPreferences,
    };
  }

  /**
   * Subscribe to changes in recent guests
   */
  subscribe(listener: (guests: RecentGuest[]) => void): () => void {
    this.listeners.push(listener);
    return () => {
      const index = this.listeners.indexOf(listener);
      if (index !== -1) {
        this.listeners.splice(index, 1);
      }
    };
  }

  /**
   * Notify all listeners of changes
   */
  private notifyListeners(): void {
    this.listeners.forEach(listener => listener([...this.recentGuests]));
  }

  /**
   * Update guest information in recent list (when guest data changes)
   */
  async updateRecentGuest(guestId: number, updates: Partial<RecentGuest>): Promise<void> {
    const index = this.recentGuests.findIndex(g => g.id === guestId);
    if (index !== -1) {
      this.recentGuests[index] = {
        ...this.recentGuests[index],
        ...updates,
        viewedAt: new Date().toISOString(), // Update viewed time
      };

      // Move to front of list
      const updatedGuest = this.recentGuests.splice(index, 1)[0];
      this.recentGuests.unshift(updatedGuest);

      await this.saveRecentGuests();
      this.notifyListeners();
    }
  }

  /**
   * Cleanup expired entries
   */
  async cleanup(): Promise<void> {
    const now = Date.now();
    const initialCount = this.recentGuests.length;
    
    this.recentGuests = this.recentGuests.filter(guest => {
      const viewedAt = new Date(guest.viewedAt).getTime();
      return now - viewedAt < RECENT_GUESTS_EXPIRY;
    });

    if (this.recentGuests.length !== initialCount) {
      await this.saveRecentGuests();
      this.notifyListeners();
      console.log(`Cleaned up ${initialCount - this.recentGuests.length} expired recent guests`);
    }
  }
}

// Export singleton instance
export const recentGuestsManager = RecentGuestsManager.getInstance();