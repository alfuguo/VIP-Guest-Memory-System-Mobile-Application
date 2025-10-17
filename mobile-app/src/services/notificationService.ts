import { api } from './api';
import { Notification, NotificationSummary, NotificationType } from '../types/notification';

export const notificationService = {
  /**
   * Get all notifications summary
   */
  async getAllNotifications(): Promise<NotificationSummary> {
    const response = await api.get<NotificationSummary>('/notifications');
    return response.data;
  },

  /**
   * Get pre-arrival notifications
   */
  async getPreArrivalNotifications(): Promise<Notification[]> {
    const response = await api.get<Notification[]>('/notifications/pre-arrival');
    return response.data;
  },

  /**
   * Get special occasion notifications
   */
  async getSpecialOccasionNotifications(): Promise<Notification[]> {
    const response = await api.get<Notification[]>('/notifications/special-occasions');
    return response.data;
  },

  /**
   * Get returning guest notifications
   */
  async getReturningGuestNotifications(): Promise<Notification[]> {
    const response = await api.get<Notification[]>('/notifications/returning-guests');
    return response.data;
  },

  /**
   * Get notifications by type
   */
  async getNotificationsByType(type: NotificationType): Promise<Notification[]> {
    const response = await api.get<Notification[]>(`/notifications/type/${type.toLowerCase()}`);
    return response.data;
  },

  /**
   * Acknowledge notification
   */
  async acknowledgeNotification(guestId: number): Promise<void> {
    await api.post(`/notifications/${guestId}/acknowledge`);
  }
};