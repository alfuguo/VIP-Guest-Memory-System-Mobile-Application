import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { notificationService } from '../services/notificationService';
import { Notification, NotificationType, NotificationPriority } from '../types/notification';
import { handleError } from '../utils/errorHandler';

interface NotificationContextType {
  notifications: Notification[];
  notificationCounts: {
    total: number;
    urgent: number;
    high: number;
    unread: number;
  };
  loading: boolean;
  refreshNotifications: () => Promise<void>;
  getSpecialOccasionAlerts: () => Notification[];
  markAsRead: (notificationId: number) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

interface NotificationProviderProps {
  children: React.ReactNode;
}

export function NotificationProvider({ children }: NotificationProviderProps) {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(false);
  const [readNotifications, setReadNotifications] = useState<Set<number>>(new Set());

  const refreshNotifications = useCallback(async () => {
    setLoading(true);
    try {
      const response = await notificationService.getAllNotifications();
      setNotifications(response.notifications);
    } catch (error) {
      handleError(error, 'Failed to refresh notifications');
    } finally {
      setLoading(false);
    }
  }, []);

  const getSpecialOccasionAlerts = useCallback(() => {
    const today = new Date().toISOString().split('T')[0];
    
    return notifications.filter(notification => {
      // Show urgent special occasions (birthdays/anniversaries today)
      if (notification.priority === NotificationPriority.URGENT &&
          (notification.notificationType === NotificationType.BIRTHDAY ||
           notification.notificationType === NotificationType.ANNIVERSARY)) {
        return notification.specialOccasionDate === today;
      }
      return false;
    });
  }, [notifications]);

  const markAsRead = useCallback((notificationId: number) => {
    setReadNotifications(prev => new Set([...prev, notificationId]));
  }, []);

  const notificationCounts = React.useMemo(() => {
    const unreadNotifications = notifications.filter(n => !readNotifications.has(n.id));
    
    return {
      total: notifications.length,
      urgent: notifications.filter(n => n.priority === NotificationPriority.URGENT).length,
      high: notifications.filter(n => n.priority === NotificationPriority.HIGH).length,
      unread: unreadNotifications.length,
    };
  }, [notifications, readNotifications]);

  // Auto-refresh notifications every 5 minutes
  useEffect(() => {
    refreshNotifications();
    
    const interval = setInterval(refreshNotifications, 5 * 60 * 1000);
    return () => clearInterval(interval);
  }, [refreshNotifications]);

  const value: NotificationContextType = {
    notifications,
    notificationCounts,
    loading,
    refreshNotifications,
    getSpecialOccasionAlerts,
    markAsRead,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  const context = useContext(NotificationContext);
  if (context === undefined) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
}