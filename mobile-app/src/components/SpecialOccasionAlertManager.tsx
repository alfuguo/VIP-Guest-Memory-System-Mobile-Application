import React, { useState, useEffect } from 'react';
import { useNavigation } from '@react-navigation/native';
import { useNotifications } from '../contexts/NotificationContext';
import SpecialOccasionAlert from './SpecialOccasionAlert';
import { Notification } from '../types/notification';

export default function SpecialOccasionAlertManager() {
  const navigation = useNavigation();
  const { getSpecialOccasionAlerts, markAsRead } = useNotifications();
  const [currentAlert, setCurrentAlert] = useState<Notification | null>(null);
  const [alertQueue, setAlertQueue] = useState<Notification[]>([]);
  const [shownAlerts, setShownAlerts] = useState<Set<number>>(new Set());

  useEffect(() => {
    const specialOccasions = getSpecialOccasionAlerts();
    const newAlerts = specialOccasions.filter(alert => !shownAlerts.has(alert.id));
    
    if (newAlerts.length > 0 && !currentAlert) {
      setAlertQueue(newAlerts);
      showNextAlert(newAlerts);
    }
  }, [getSpecialOccasionAlerts, currentAlert, shownAlerts]);

  const showNextAlert = (alerts: Notification[]) => {
    if (alerts.length > 0) {
      const nextAlert = alerts[0];
      setCurrentAlert(nextAlert);
      setShownAlerts(prev => new Set([...prev, nextAlert.id]));
    }
  };

  const handleCloseAlert = () => {
    if (currentAlert) {
      markAsRead(currentAlert.id);
    }
    
    const remainingAlerts = alertQueue.slice(1);
    setAlertQueue(remainingAlerts);
    setCurrentAlert(null);
    
    // Show next alert after a short delay
    if (remainingAlerts.length > 0) {
      setTimeout(() => {
        showNextAlert(remainingAlerts);
      }, 500);
    }
  };

  const handleViewGuest = () => {
    if (currentAlert) {
      markAsRead(currentAlert.id);
      
      // Navigate to guest detail
      (navigation as any).navigate('Guests', {
        screen: 'GuestDetail',
        params: { guestId: currentAlert.id },
      });
    }
    
    handleCloseAlert();
  };

  if (!currentAlert) {
    return null;
  }

  return (
    <SpecialOccasionAlert
      notification={currentAlert}
      visible={!!currentAlert}
      onClose={handleCloseAlert}
      onViewGuest={handleViewGuest}
    />
  );
}