export interface Notification {
  id: number;
  firstName: string;
  lastName: string;
  phone: string;
  photoUrl?: string;
  notificationType: NotificationType;
  message: string;
  priority: NotificationPriority;
  seatingPreference?: string;
  dietaryRestrictions?: string[];
  favoriteDrinks?: string[];
  lastVisitDate?: string;
  lastVisitNotes?: string;
  visitCount?: number;
  specialOccasionDate?: string;
  daysSinceLastVisit?: number;
  createdAt: string;
}

export interface NotificationSummary {
  notifications: Notification[];
}

export enum NotificationType {
  PRE_ARRIVAL = 'PRE_ARRIVAL',
  BIRTHDAY = 'BIRTHDAY',
  ANNIVERSARY = 'ANNIVERSARY',
  RETURNING_GUEST = 'RETURNING_GUEST',
  DIETARY_RESTRICTION = 'DIETARY_RESTRICTION',
  SPECIAL_OCCASION = 'SPECIAL_OCCASION'
}

export enum NotificationPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

export interface NotificationFilter {
  type?: NotificationType;
  priority?: NotificationPriority;
}