import type { NotificationEventResponse } from '../types/notification';

export interface NotificationStreamHandlers {
  onConnected?: (data: { connected: boolean }) => void;
  onNotification?: (notification: NotificationEventResponse) => void;
  onError?: (error: Error) => void;
  onClose?: () => void;
}

export function subscribeNotifications(handlers: NotificationStreamHandlers): () => void {
  const controller = new AbortController();
  const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:6060/api';

  const runStream = async () => {
    try {
      const response = await fetch(`${baseUrl}/notifications/stream`, {
        method: 'GET',
        headers: {
          Accept: 'text/event-stream',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        signal: controller.signal,
      });

      if (!response.ok) {
        throw new Error(`SSE stream failed with status ${response.status}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('ReadableStream not supported by response body');
      }

      const decoder = new TextDecoder();
      let buffer = '';
      let currentEvent = 'message';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split(/\r?\n/);
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim();
          } else if (line.startsWith('data:')) {
            const dataStr = line.slice(5).trim();
            try {
              const data = JSON.parse(dataStr);
              if (currentEvent === 'notification-connected' && handlers.onConnected) {
                handlers.onConnected(data);
              } else if (currentEvent === 'notification' && handlers.onNotification) {
                handlers.onNotification(data as NotificationEventResponse);
              }
            } catch {
              // Ignore non-JSON control signals
            }
          } else if (line.trim() === '') {
            currentEvent = 'message';
          }
        }
      }

      handlers.onClose?.();
    } catch (err: unknown) {
      if ((err as Error).name !== 'AbortError') {
        handlers.onError?.(err instanceof Error ? err : new Error(String(err)));
      }
    }
  };

  runStream();

  return () => {
    controller.abort();
  };
}
