import { useState, useEffect, useCallback } from 'react';
import { useAuth } from './useAuth';

export interface BookmarkItem {
  id: string;
  type: 'diseases' | 'cases' | 'flashcards' | 'quizzes';
  title: string;
  category?: string;
  specialty?: string;
  savedAt: string;
  progress?: number;
  status?: string;
  link?: string;
}

export function useBookmarks() {
  const { user } = useAuth();
  const storageKey = `medlearn_bookmarks_${user?.id || 'guest'}`;

  const [bookmarks, setBookmarks] = useState<BookmarkItem[]>(() => {
    try {
      const saved = localStorage.getItem(storageKey);
      return saved ? JSON.parse(saved) : [];
    } catch (e) {
      console.error('Failed to load bookmarks', e);
      return [];
    }
  });

  useEffect(() => {
    try {
      const saved = localStorage.getItem(storageKey);
      setBookmarks(saved ? JSON.parse(saved) : []);
    } catch (e) {
      console.error('Failed to reload bookmarks on user change', e);
    }
  }, [storageKey]);

  const isBookmarked = useCallback((id: string, type: 'diseases' | 'cases' | 'flashcards' | 'quizzes') => {
    return bookmarks.some(b => b.id === id && b.type === type);
  }, [bookmarks]);

  const toggleBookmark = useCallback((item: Omit<BookmarkItem, 'savedAt'>) => {
    setBookmarks(prev => {
      const exists = prev.some(b => b.id === item.id && b.type === item.type);
      let updated: BookmarkItem[];
      if (exists) {
        updated = prev.filter(b => !(b.id === item.id && b.type === item.type));
      } else {
        const newItem: BookmarkItem = {
          ...item,
          savedAt: new Date().toLocaleDateString(),
        };
        updated = [newItem, ...prev];
      }
      try {
        localStorage.setItem(storageKey, JSON.stringify(updated));
      } catch (e) {
        console.error('Failed to save bookmarks', e);
      }
      return updated;
    });
  }, [storageKey]);

  const removeBookmark = useCallback((id: string, type: 'diseases' | 'cases' | 'flashcards' | 'quizzes') => {
    setBookmarks(prev => {
      const updated = prev.filter(b => !(b.id === id && b.type === type));
      try {
        localStorage.setItem(storageKey, JSON.stringify(updated));
      } catch (e) {
        console.error('Failed to remove bookmark', e);
      }
      return updated;
    });
  }, [storageKey]);

  return {
    bookmarks,
    isBookmarked,
    toggleBookmark,
    removeBookmark,
  };
}
