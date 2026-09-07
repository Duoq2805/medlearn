import { useState } from 'react';
import { useNotification } from '../../context/NotificationContext';
import { Bell } from 'lucide-react';

export const NotificationBell = () => {
  const { notifications, unreadCount, clearNotifications } = useNotification();
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="p-2.5 rounded-full border border-[var(--border)] bg-[--surface-white] dark:bg-[var(--surface-secondary)] text-[var(--text-primary)] hover:bg-gray-100 dark:hover:bg-[var(--surface-hover)] transition-colors relative"
        aria-label="Notifications"
      >
        <Bell size={18} />
        <span className="absolute -top-0.5 -right-0.5 w-4 h-4 bg-red-500 rounded-full text-[10px] flex items-center justify-center text-white font-bold">
          {unreadCount}
        </span>
      </button>
      {isOpen && (
        <div className="absolute right-0 mt-2 w-72 rounded-xl border border-[var(--border)] bg-[var(--surface-primary)] backdrop-blur-xl shadow-lg z-50 overflow-hidden depth-layer-1">
          <div className="p-3 border-b border-[var(--shadow-dark)]">
            <p className="text-sm font-semibold text-[var(--text-primary)]">Notifications</p>
          </div>
          <div className="p-2 space-y-1 max-h-64 overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="text-center py-4 text-[var(--text-tertiary)]">No notifications</div>
            ) : (
              <>
                {notifications.map((n, i) => (
                  <div key={i} className="flex items-start gap-2 p-2 rounded-lg hover:bg-[var(--surface-hover)] transition-colors cursor-pointer">
                    <span className="w-2 h-2 mt-1.5 rounded-full bg-[var(--accent-primary)] shrink-0" />
                    <div>
                      <p className="text-xs text-[var(--text-primary)]">{n.title}</p>
                      <p className="text-[10px] text-[var(--text-tertiary)]">{n.message}</p>
                    </div>
                  </div>
                ))}
                <div className="pt-2">
                  <button onClick={clearNotifications} className="w-full text-xs text-[var(--text-secondary)] hover:text-[var(--text-primary)]">
                    Clear All
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
