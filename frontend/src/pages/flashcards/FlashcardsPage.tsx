import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Sparkles, BookOpen, Clock, Search, ChevronRight,
  Plus, Filter, Bookmark, BarChart3, Brain, Target,
  AlertCircle, RefreshCw
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';
import Skeleton from '../../components/ui/Skeleton';

export default function FlashcardsPage() {
  const { user } = useAuth();
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading] = useState(false);

  const mockDecks = [
    { id: '1', title: 'Pneumonia', count: 24, progress: 70, lastReviewed: '2h ago', bookmarked: true },
    { id: '2', title: 'Hypertension', count: 15, progress: 40, lastReviewed: 'Yesterday', bookmarked: false },
    { id: '3', title: 'Diabetes Type 2', count: 30, progress: 90, lastReviewed: '3 days ago', bookmarked: true },
  ];

  const filteredDecks = mockDecks.filter(d => d.title.toLowerCase().includes(searchTerm.toLowerCase()));

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><Sparkles size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Flashcards</h1>
              <p className="text-sm text-[var(--text-secondary)]">Review and generate flashcards for any disease</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            {/* Left Panel */}
            <div className="lg:col-span-1 space-y-4">
              <div className="card-neumorphic p-4">
                <div className="relative mb-3">
                  <input
                    type="text"
                    placeholder="Search decks..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="input-neumorphic w-full pl-9 py-2 text-sm"
                  />
                  <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
                </div>
                <button disabled className="btn-neumorphic-primary py-2 px-3 w-full text-sm flex items-center justify-center gap-2">
                  <Plus size={14} /> New Deck
                </button>
              </div>
              <div className="card-neumorphic p-4 space-y-2">
                <h3 className="text-xs font-semibold uppercase tracking-wider text-[var(--text-tertiary)]">Stats</h3>
                {[
                  { label: 'Cards Due', value: '47', icon: Brain },
                  { label: 'Total Cards', value: '234', icon: BookOpen },
                  { label: 'Retention', value: '82%', icon: BarChart3 },
                ].map((s, i) => (
                  <div key={i} className="flex items-center justify-between text-sm">
                    <span className="flex items-center gap-2 text-[var(--text-secondary)]">
                      <s.icon size={14} className="text-[var(--accent-primary)]" /> {s.label}
                    </span>
                    <span className="font-semibold text-[var(--text-primary)]">{s.value}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Right: Decks */}
            <div className="lg:col-span-3">
              {isLoading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {[...Array(3)].map((_, i) => (
                    <div key={i} className="card-neumorphic p-6">
                      <Skeleton className="h-5 w-3/4 mb-4" />
                      <Skeleton className="h-4 w-1/2 mb-2" />
                      <Skeleton className="h-4 w-full mb-4" />
                      <Skeleton className="h-8 w-24" />
                    </div>
                  ))}
                </div>
              ) : filteredDecks.length === 0 ? (
                <div className="card-neumorphic p-12 text-center">
                  <Sparkles size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
                  <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No flashcards yet</p>
                  <p className="text-sm text-[var(--text-secondary)] mb-6">
                    Flashcards help you retain medical knowledge through spaced repetition.
                    Create a deck from any disease to get started.
                  </p>
                  <div className="flex items-center justify-center gap-3">
                    <button disabled className="btn-neumorphic-primary py-2 px-6 text-sm opacity-50 cursor-not-allowed">
                      <Sparkles size={14} className="inline mr-1" /> Generate from Disease
                    </button>
                  </div>
                  <p className="text-xs text-[var(--text-tertiary)] mt-4">
                    Flashcards generation endpoint is in development. Coming soon.
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {filteredDecks.map((deck) => (
                    <div key={deck.id} className="card-neumorphic p-6 hover:shadow-lg transition-all">
                      <div className="flex items-start justify-between mb-3">
                        <h3 className="font-display text-lg font-bold text-[var(--text-primary)]">{deck.title}</h3>
                        <Bookmark size={16} className={deck.bookmarked ? 'text-[var(--accent-primary)]' : 'text-[var(--text-tertiary)]'} fill={deck.bookmarked ? 'var(--accent-primary)' : 'none'} />
                      </div>
                      <div className="flex items-center gap-4 text-xs text-[var(--text-secondary)] mb-4">
                        <span>{deck.count} cards</span>
                        <span>Last: {deck.lastReviewed}</span>
                      </div>
                      <div className="flex items-center gap-2 mb-4">
                        <div className="flex-1 h-2 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_4px_var(--shadow-dark)] overflow-hidden">
                          <div className="h-full bg-[var(--accent-primary)] rounded-full transition-all" style={{ width: `${deck.progress}%` }} />
                        </div>
                        <span className="text-xs font-semibold">{deck.progress}%</span>
                      </div>
                      <div className="flex gap-2">
                        <button className="btn-neumorphic-primary py-2 px-4 flex-1 text-sm">Review</button>
                        <button disabled className="btn-neumorphic-secondary py-2 px-4 text-sm opacity-50 cursor-not-allowed">
                          <RefreshCw size={14} />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
