import React, { createContext, useContext, useState, useCallback, useRef, useEffect } from 'react';
import type { NotificationEventResponse } from '../types/notification';
import { subscribeNotifications } from '../api/notification';

interface NotificationContextProps {
  notifications: NotificationEventResponse[];
  unreadCount: number;
  clearNotifications: () => void;
}

const NotificationContext = createContext<NotificationContextProps | undefined>(undefined);

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};

interface NotificationProviderProps {
  children: React.ReactNode;
}

export const NotificationProvider = ({ children }: NotificationProviderProps) => {
  const [notifications, setNotifications] = useState<NotificationEventResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const subscriptionRef = useRef<(() => void) | null>(null);

  const clearNotifications = useCallback(() => {
    setNotifications([]);
    setUnreadCount(0);
  }, []);

  const addNotification = useCallback((notification: NotificationEventResponse) => {
    setNotifications(prev => [notification, ...prev]);
    setUnreadCount(prev => prev + 1);
  }, []);

  useEffect(() => {
    // Subscribe to the notification stream
    const unsubscribe = subscribeNotifications({
      onConnected: (data) => {
        console.log('Notification SSE connected:', data);
      },
      onNotification: (notification) => {
        addNotification(notification);
      },
      onError: (error) => {
        console.error('Notification SSE error:', error);
      },
      onClose: () => {
        console.log('Notification SSE connection closed');
      },
    });

    subscriptionRef.current = unsubscribe;

    // Cleanup on unmount
    return () => {
      if (subscriptionRef.current) {
        subscriptionRef.current();
      }
    };
  }, [addNotification]);

  const value: NotificationContextProps = {
    notifications,
    unreadCount,
    clearNotifications,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};
