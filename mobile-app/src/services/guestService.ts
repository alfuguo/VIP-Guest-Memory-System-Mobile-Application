import { apiClient } from './api';
import { 
  Guest, 
  GuestListResponse, 
  GuestSearchParams, 
  CreateGuestRequest, 
  UpdateGuestRequest 
} from '../types/guest';

export class GuestService {
  private static readonly BASE_ENDPOINT = '/guests';

  /**
   * Fetch guests with search and pagination using POST search endpoint
   */
  static async getGuests(params: GuestSearchParams = {}): Promise<GuestListResponse> {
    // Convert frontend params to backend search request format
    const searchRequest = {
      searchTerm: params.search || undefined,
      seatingPreference: params.seatingPreference || undefined,
      dietaryRestrictions: params.dietaryRestrictions || undefined,
      favoriteDrinks: params.favoriteDrinks || undefined,
      upcomingOccasions: params.upcomingOccasions || undefined,
      page: (params.page || 1) - 1, // Backend uses 0-based pagination
      size: params.limit || 20,
      sortBy: 'firstName',
      sortDirection: 'ASC'
    };

    // Remove undefined values to keep request clean
    const cleanRequest = Object.fromEntries(
      Object.entries(searchRequest).filter(([_, value]) => value !== undefined)
    );

    const response = await apiClient.post<any>(`${this.BASE_ENDPOINT}/search`, cleanRequest);
    
    // Convert backend response format to frontend format
    return {
      guests: response.content || [],
      totalCount: response.totalElements || 0,
      currentPage: (response.number || 0) + 1, // Convert back to 1-based pagination
      totalPages: response.totalPages || 1,
      hasNext: !response.last,
      hasPrevious: !response.first
    };
  }

  /**
   * Get a single guest by ID
   */
  static async getGuest(id: number): Promise<Guest> {
    return apiClient.get<Guest>(`${this.BASE_ENDPOINT}/${id}`);
  }

  /**
   * Create a new guest
   */
  static async createGuest(guestData: CreateGuestRequest): Promise<Guest> {
    return apiClient.post<Guest>(this.BASE_ENDPOINT, guestData);
  }

  /**
   * Update an existing guest
   */
  static async updateGuest(guestData: UpdateGuestRequest): Promise<Guest> {
    const { id, ...updateData } = guestData;
    return apiClient.put<Guest>(`${this.BASE_ENDPOINT}/${id}`, updateData);
  }

  /**
   * Delete a guest (soft delete)
   */
  static async deleteGuest(id: number): Promise<void> {
    return apiClient.delete<void>(`${this.BASE_ENDPOINT}/${id}`);
  }

  /**
   * Upload guest photo
   */
  static async uploadGuestPhoto(
    guestId: number, 
    imageUri: string, 
    onProgress?: (progress: number) => void
  ): Promise<{ photoUrl: string }> {
    const formData = new FormData();
    
    // Create file object for React Native
    const filename = `guest_${guestId}_${Date.now()}.jpg`;
    formData.append('photo', {
      uri: imageUri,
      type: 'image/jpeg',
      name: filename,
    } as any);

    return apiClient.upload<{ photoUrl: string }>(
      `${this.BASE_ENDPOINT}/${guestId}/photo`,
      formData,
      onProgress
    );
  }

  /**
   * Search guests by name or phone (for quick lookup)
   */
  static async searchGuests(query: string, limit: number = 10): Promise<Guest[]> {
    const response = await this.getGuests({ 
      search: query, 
      limit,
      page: 1 
    });
    return response.guests;
  }
}