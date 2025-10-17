export interface Visit {
  id: number;
  guestId: number;
  staffId: number;
  visitDate: string; // ISO date string (YYYY-MM-DD)
  visitTime: string; // Time string (HH:MM:SS)
  partySize: number;
  tableNumber?: string;
  serviceNotes?: string;
  staffName: string;
  createdAt: string; // ISO datetime string
  updatedAt: string; // ISO datetime string
}

export interface VisitListResponse {
  visits: Visit[];
  totalCount: number;
  currentPage: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface CreateVisitRequest {
  guestId: number;
  visitDate: string;
  visitTime: string;
  partySize: number;
  tableNumber?: string;
  serviceNotes?: string;
}

export interface UpdateVisitRequest extends Partial<CreateVisitRequest> {
  id: number;
}