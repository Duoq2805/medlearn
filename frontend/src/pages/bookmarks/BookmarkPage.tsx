import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Bookmark, BookOpen, Target, Sparkles, ClipboardCheck,
  Search, ChevronRight, Trash2
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useBookmarks } from '../../hooks/useBookmarks';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function BookmarkPage() {
  const { user } = useAuth();
  const { bookmarks, removeBookmark } = useBookmarks();
  const [activeTab, setActiveTab] = useState<'diseases' | 'cases' | 'flashcards' | 'quizzes'>('diseases');
  const [searchTerm, setSearchTerm] = useState('');

  const currentItems = bookmarks.filter(b => b.type === activeTab);
  const filteredItems = currentItems.filter(item =>
    item.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const counts = {
    diseases: bookmarks.filter(b => b.type === 'diseases').length,
    cases: bookmarks.filter(b => b.type === 'cases').length,
    flashcards: bookmarks.filter(b => b.type === 'flashcards').length,
    quizzes: bookmarks.filter(b => b.type === 'quizzes').length,
  };

  const tabs = [
    { id: 'diseases' as const, label: 'Diseases', icon: BookOpen, count: counts.diseases },
    { id: 'cases' as const, label: 'Cases', icon: Target, count: counts.cases },
    { id: 'flashcards' as const, label: 'Flashcards', icon: Sparkles, count: counts.flashcards },
    { id: 'quizzes' as const, label: 'Quizzes', icon: ClipboardCheck, count: counts.quizzes },
  ];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><Bookmark size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Bookmarks</h1>
              <p className="text-sm text-[var(--text-secondary)]">Your saved learning materials</p>
            </div>
          </div>

          {/* Tabs */}
          <div className="mb-8 overflow-x-auto">
            <div className="card-neumorphic p-2 flex gap-1 min-w-min">
              {tabs.map((tab) => {
                const Icon = tab.icon;
                return (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`py-2 px-4 rounded-lg flex items-center gap-2 text-sm font-medium transition-all whitespace-nowrap ${
                      activeTab === tab.id
                        ? 'bg-[var(--accent-primary)] text-white'
                        : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                    }`}
                  >
                    <Icon size={14} /> {tab.label}
                    <span className={`ml-1 px-2 py-0.5 rounded-full text-xs font-semibold ${activeTab === tab.id ? 'bg-white/20' : 'bg-[var(--surface-hover)]'}`}>
                      {tab.count}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Search */}
          <div className="mb-6">
            <div className="relative">
              <input
                type="text"
                placeholder={`Search ${activeTab}...`}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input-neumorphic w-full !pl-11 py-3 text-sm"
              />
              <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)] pointer-events-none" />
            </div>
          </div>

          {/* Items */}
          {filteredItems.length === 0 ? (
            <div className="card-neumorphic p-12 text-center">
              <Bookmark size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
              <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No bookmarks yet</p>
              <p className="text-sm text-[var(--text-secondary)] mb-6">Start bookmarking {activeTab} to build your collection.</p>
              <Link to={activeTab === 'diseases' ? '/explorer' : activeTab === 'cases' ? '/cases' : '/dashboard'} className="btn-neumorphic-primary py-3 px-8 inline-flex items-center gap-2 text-sm">
                Browse {activeTab} <ChevronRight size={16} />
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredItems.map((item) => (
                <div key={item.id} className="card-neumorphic p-6 hover:shadow-lg transition-all flex flex-col">
                  <div className="flex items-start justify-between mb-3">
                    <h3 className="font-display text-lg font-bold text-[var(--text-primary)] flex-1">{item.title}</h3>
                    <button
                      onClick={() => removeBookmark(item.id, activeTab)}
                      className="p-1 rounded hover:bg-[var(--surface-hover)] text-[var(--text-tertiary)] hover:text-red-500 transition-colors"
                      title="Remove bookmark"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                  <p className="text-xs text-[var(--text-secondary)] mb-3">
                    {item.category || item.specialty || 'Saved Item'}
                  </p>
                  <p className="text-[10px] text-[var(--text-tertiary)] mb-4">Saved {item.savedAt}</p>
                  <div className="mt-auto pt-3 border-t border-[var(--shadow-dark)]">
                    <Link
                      to={item.link || (activeTab === 'diseases' ? `/disease/${item.id}` : activeTab === 'cases' ? `/case/${item.id}` : '#')}
                      className="btn-neumorphic-primary py-2 px-4 text-sm w-full text-center flex items-center justify-center gap-1"
                    >
                      Open <ChevronRight size={14} />
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </AnimatedSection>
      </div>
    </div>
  );
}
