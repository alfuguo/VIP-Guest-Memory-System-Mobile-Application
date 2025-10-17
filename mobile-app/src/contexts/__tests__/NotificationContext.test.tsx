import React from 'react';
import { render, fireEvent, waitFor } from '../../__tests__/test-utils';
import { NotificationProvider, useNotifications } from '../NotificationContext';
import { Text, TouchableOpacity } from 'react-native';
import { mockNotification } from '../../__tests__/test-utils';

// Test component to interact with NotificationContext
const TestComponent = () => {
  const { 
    notifications, 
    unreadCount, 
    markAsRead, 
    clearAll,
    addNotification 
  } = useNotifications();

  return (
    <>
      <Text testID="notification-count">{notifications.length}</Text>
      <Text testID="unread-count">{unreadCount}</Text>
      <TouchableOpacity
        testID="mark-read-button"
        onPress={() => markAsRead(1)}
      >
        <Text>Mark as Read</Text>
      </TouchableOpacity>
      <TouchableOpacity
        testID="clear-all-button"
        onPress={clearAll}
      >
        <Text>Clear All</Text>
      </TouchableOpacity>
      <TouchableOpacity
        testID="add-notification-button"
        onPress={() => addNotification(mockNotification)}
      >
        <Text>Add Notification</Text>
      </TouchableOpacity>
      {notifications.map((notification) => (
        <Text key={notification.id} testID={`notification-${notification.id}`}>
          {notification.message}
        </Text>
      ))}
    </>
  );
};

describe('NotificationContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should provide initial empty state', () => {
    const { getByTestId } = render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    expect(getByTestId('notification-count')).toHaveTextContent('0');
    expect(getByTestId('unread-count')).toHaveTextContent('0');
  });

  it('should add notifications', () => {
    const { getByTestId } = render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    fireEvent.press(getByTestId('add-notification-button'));

    expect(getByTestId('notification-count')).toHaveTextContent('1');
    expect(getByTestId('unread-count')).toHaveTextContent('1');
    expect(getByTestId(`notification-${mockNotification.id}`)).toHaveTextContent(
      mockNotification.message
    );
  });

  it('should mark notifications as read', async () => {
    const { getByTestId } = render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    // Add notification first
    fireEvent.press(getByTestId('add-notification-button'));
    expect(getByTestId('unread-count')).toHaveTextContent('1');

    // Mark as read
    fireEvent.press(getByTestId('mark-read-button'));

    await waitFor(() => {
      expect(getByTestId('unread-count')).toHaveTextContent('0');
    });

    // Notification should still exist but be marked as read
    expect(getByTestId('notification-count')).toHaveTextContent('1');
  });

  it('should clear all notifications', async () => {
    const { getByTestId } = render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    // Add notification first
    fireEvent.press(getByTestId('add-notification-button'));
    expect(getByTestId('notification-count')).toHaveTextContent('1');

    // Clear all
    fireEvent.press(getByTestId('clear-all-button'));

    await waitFor(() => {
      expect(getByTestId('notification-count')).toHaveTextContent('0');
      expect(getByTestId('unread-count')).toHaveTextContent('0');
    });
  });

  it('should handle multiple notifications', () => {
    const { getByTestId } = render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    // Add multiple notifications
    fireEvent.press(getByTestId('add-notification-button'));
    fireEvent.press(getByTestId('add-notification-button'));
    fireEvent.press(getByTestId('add-notification-button'));

    expect(getByTestId('notification-count')).toHaveTextContent('3');
    expect(getByTestId('unread-count')).toHaveTextContent('3');
  });

  it('should calculate unread count correctly', async () => {
    const { getByTestId } = render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    // Add 3 notifications
    fireEvent.press(getByTestId('add-notification-button'));
    fireEvent.press(getByTestId('add-notification-button'));
    fireEvent.press(getByTestId('add-notification-button'));

    expect(getByTestId('unread-count')).toHaveTextContent('3');

    // Mark one as read
    fireEvent.press(getByTestId('mark-read-button'));

    await waitFor(() => {
      expect(getByTestId('unread-count')).toHaveTextContent('2');
    });

    expect(getByTestId('notification-count')).toHaveTextContent('3');
  });

  it('should handle notification types correctly', () => {
    const specialOccasionNotification = {
      ...mockNotification,
      id: 2,
      type: 'SPECIAL_OCCASION' as const,
      message: 'Birthday celebration today!',
    };

    const TestComponentWithTypes = () => {
      const { addNotification, notifications } = useNotifications();

      return (
        <>
          <TouchableOpacity
            testID="add-special-notification"
            onPress={() => addNotification(specialOccasionNotification)}
          >
            <Text>Add Special</Text>
          </TouchableOpacity>
          {notifications.map((notification) => (
            <Text key={notification.id} testID={`notification-type-${notification.id}`}>
              {notification.type}
            </Text>
          ))}
        </>
      );
    };

    const { getByTestId } = render(
      <NotificationProvider>
        <TestComponentWithTypes />
      </NotificationProvider>
    );

    fireEvent.press(getByTestId('add-special-notification'));

    expect(getByTestId('notification-type-2')).toHaveTextContent('SPECIAL_OCCASION');
  });
});