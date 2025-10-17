export interface Guest {
  id: number;
  firstName: string;
  lastName?: string;
  phone: string;
  email?: string;
  photoUrl?: string;
  seatingPreference?: string;
  dietaryRestrictions: string[];
  favoriteDrinks: string[];
  birthday?: string; // ISO date string
  anniversary?: string; // ISO date string
  notes?: string;
  lastVisit?: string; // ISO datetime string
  visitCount: number;
  createdAt: string; // ISO datetime string
}

export interface GuestListResponse {
  guests: Guest[];
  totalCount: number;
  currentPage: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface GuestSearchParams {
  page?: number;
  limit?: number;
  search?: string;
  dietaryRestrictions?: string[];
  seatingPreference?: string;
  favoriteDrinks?: string[];
  upcomingOccasions?: boolean;
}

export interface CreateGuestRequest {
  firstName: string;
  lastName?: string;
  phone: string;
  email?: string;
  seatingPreference?: string;
  dietaryRestrictions?: string[];
  favoriteDrinks?: string[];
  birthday?: string;
  anniversary?: string;
  notes?: string;
}

export interface UpdateGuestRequest extends Partial<CreateGuestRequest> {
  id: number;
}