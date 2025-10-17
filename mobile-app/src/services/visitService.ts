import { apiClient } from './api';
import { 
  Visit, 
  VisitListResponse, 
  CreateVisitRequest, 
  UpdateVisitRequest 
} from '../types/visit';

export class VisitService {
  /**
   * Get visits for a specific guest
   */
  static async getGuestVisits(
    guestId: number, 
    page: number = 1, 
    limit: number = 20
  ): Promise<VisitListResponse> {
    const searchParams = new URLSearchParams({
      page: page.toString(),
      limit: limit.toString(),
    });

    return apiClient.get<VisitListResponse>(
      `/guests/${guestId}/visits?${searchParams.toString()}`
    );
  }

  /**
   * Get a single visit by ID
   */
  static async getVisit(visitId: number): Promise<Visit> {
    return apiClient.get<Visit>(`/visits/${visitId}`);
  }

  /**
   * Create a new visit
   */
  static async createVisit(visitData: CreateVisitRequest): Promise<Visit> {
    return apiClient.post<Visit>(`/guests/${visitData.guestId}/visits`, visitData);
  }

  /**
   * Update an existing visit
   */
  static async updateVisit(visitData: UpdateVisitRequest): Promise<Visit> {
    const { id, ...updateData } = visitData;
    return apiClient.put<Visit>(`/visits/${id}`, updateData);
  }

  /**
   * Delete a visit
   */
  static async deleteVisit(visitId: number): Promise<void> {
    return apiClient.delete<void>(`/visits/${visitId}`);
  }
}