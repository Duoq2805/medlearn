import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Sparkles, BookOpen, Search, Plus, Brain, BarChart3,
  Download, Loader2, AlertCircle, Eye, Check, X, RotateCw
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';
import { flashcardApi } from '../../api/flashcard';
import type {
  FlashcardDeckResponse,
  FlashcardStatsResponse,
  FlashcardProgressResponse,
  FlashcardDifficulty,
} from '../../types/flashcard';

export default function FlashcardsPage() {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');

  // Modals state
  const [generateModalOpen, setGenerateModalOpen] = useState(false);
  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [activeDeckId, setActiveDeckId] = useState<number | null>(null);

  // Generate form state
  const [genTitle, setGenTitle] = useState('');
  const [genCount, setGenCount] = useState(10);
  const [genDifficulty, setGenDifficulty] = useState<FlashcardDifficulty>('MEDIUM');
  const [genDiseaseId, setGenDiseaseId] = useState('');
  const [genError, setGenError] = useState<string | null>(null);

  // Review session state
  const [reviewIndex, setReviewIndex] = useState(0);
  const [showAnswer, setShowAnswer] = useState(false);
  const [reviewing, setReviewing] = useState(false);

  // Queries
  const { data: decksData, isLoading: decksLoading } = useQuery({
    queryKey: ['flashcardDecks'],
    queryFn: () => flashcardApi.listDecks(false, 0, 50),
  });

  const { data: stats } = useQuery<FlashcardStatsResponse>({
    queryKey: ['flashcardStats'],
    queryFn: flashcardApi.getStats,
  });

  const { data: dueCards, isLoading: dueLoading } = useQuery<FlashcardProgressResponse[]>({
    queryKey: ['flashcardDue'],
    queryFn: () => flashcardApi.getDueCards(20),
  });

  const refreshAll = () => {
    queryClient.invalidateQueries({ queryKey: ['flashcardDecks'] });
    queryClient.invalidateQueries({ queryKey: ['flashcardStats'] });
    queryClient.invalidateQueries({ queryKey: ['flashcardDue'] });
  };

  // Mutations
  const generateMutation = useMutation({
    mutationFn: () =>
      flashcardApi.generate({
        title: genTitle.trim(),
        count: genCount,
        difficulty: genDifficulty,
        ...(genDiseaseId ? { diseaseId: parseInt(genDiseaseId, 10) } : {}),
      }),
    onSuccess: () => {
      setGenerateModalOpen(false);
      setGenTitle('');
      setGenDiseaseId('');
      setGenError(null);
      refreshAll();
    },
    onError: (err: any) => {
      setGenError(err.response?.data?.message || err.message || 'Generation failed');
    },
  });

  const reviewMutation = useMutation({
    mutationFn: ({ cardId, quality }: { cardId: number; quality: number }) =>
      flashcardApi.review(cardId, { quality }),
    onSuccess: () => {
      setShowAnswer(false);
      if (dueCards && reviewIndex < dueCards.length - 1) {
        setReviewIndex((prev) => prev + 1);
      } else {
        setReviewModalOpen(false);
        setReviewIndex(0);
        refreshAll();
      }
    },
  });

  const exportDeck = async (deckId: number, title: string) => {
    try {
      const exportRes = await flashcardApi.exportDeck(deckId, 'json');
      const blob = new Blob([JSON.stringify(exportRes, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${title.replace(/\s+/g, '_')}_flashcards.json`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Failed to export deck', err);
    }
  };

  const decks = decksData?.content || [];
  const filteredDecks = decks.filter((d) => d.title?.toLowerCase().includes(searchTerm.toLowerCase()));
  const currentCardProgress = dueCards && dueCards[reviewIndex];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well">
                <Sparkles size={22} />
              </div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">
                  Flashcards & Spaced Repetition
                </h1>
                <p className="text-sm text-[var(--text-secondary)]">
                  SM-2 algorithm spaced repetition learning
                </p>
              </div>
            </div>
            <button
              onClick={() => setGenerateModalOpen(true)}
              className="btn-neumorphic-primary py-2 px-4 text-sm flex items-center gap-2"
            >
              <Plus size={14} /> Generate Flashcards
            </button>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            {/* Left Panel */}
            <div className="lg:col-span-1 space-y-4">
              <div className="card-neumorphic p-4">
                <div className="relative">
                  <input
                    type="text"
                    placeholder="Search decks..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="input-neumorphic w-full pl-9 py-2 text-sm"
                  />
                  <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
                </div>
              </div>

              {/* Review Due CTA */}
              {dueCards && dueCards.length > 0 && (
                <div className="card-neumorphic p-4 bg-[var(--accent-primary)]/10 border border-[var(--accent-primary)]/20 space-y-3">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-bold text-[var(--text-primary)]">Ready for Review</span>
                    <span className="px-2 py-0.5 rounded-full bg-[var(--accent-primary)] text-white font-bold">
                      {dueCards.length} due
                    </span>
                  </div>
                  <button
                    onClick={() => {
                      setReviewIndex(0);
                      setShowAnswer(false);
                      setReviewModalOpen(true);
                    }}
                    className="btn-neumorphic-primary w-full py-2 text-xs flex items-center justify-center gap-2"
                  >
                    <Brain size={14} /> Start Review Session
                  </button>
                </div>
              )}

              {/* Stats */}
              <div className="card-neumorphic p-4 space-y-3">
                <h3 className="text-xs font-semibold uppercase tracking-wider text-[var(--text-tertiary)]">
                  Learning Stats
                </h3>
                <div className="space-y-2 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="flex items-center gap-2 text-[var(--text-secondary)]">
                      <Brain size={14} className="text-[var(--accent-primary)]" /> Cards Due Today
                    </span>
                    <span className="font-bold text-[var(--text-primary)]">{stats?.cardsDueToday ?? 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="flex items-center gap-2 text-[var(--text-secondary)]">
                      <BookOpen size={14} className="text-[var(--accent-primary)]" /> Total Decks
                    </span>
                    <span className="font-bold text-[var(--text-primary)]">{stats?.totalDecks ?? 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="flex items-center gap-2 text-[var(--text-secondary)]">
                      <BarChart3 size={14} className="text-[var(--accent-primary)]" /> Mastered Cards
                    </span>
                    <span className="font-bold text-[var(--text-primary)]">{stats?.masteredCards ?? 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="flex items-center gap-2 text-[var(--text-secondary)]">
                      <BarChart3 size={14} className="text-[var(--accent-primary)]" /> Total Reviews
                    </span>
                    <span className="font-bold text-[var(--text-primary)]">{stats?.totalReviews ?? 0}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Right: Decks */}
            <div className="lg:col-span-3">
              {decksLoading ? (
                <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">
                  Loading flashcard decks...
                </div>
              ) : filteredDecks.length === 0 ? (
                <div className="card-neumorphic p-12 text-center">
                  <Sparkles size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
                  <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No flashcard decks found</p>
                  <p className="text-sm text-[var(--text-secondary)] mb-6 max-w-md mx-auto">
                    Generate AI flashcards from disease topics using the button above.
                  </p>
                  <button
                    onClick={() => setGenerateModalOpen(true)}
                    className="btn-neumorphic-primary py-2 px-6 text-sm"
                  >
                    <Sparkles size={14} className="inline mr-1" /> Generate Flashcards
                  </button>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {filteredDecks.map((deck: FlashcardDeckResponse) => (
                    <div key={deck.id} className="card-neumorphic p-6 space-y-4">
                      <div className="flex items-start justify-between">
                        <div>
                          <span className="text-[10px] uppercase font-bold text-[var(--accent-primary)]">
                            {deck.sourceType || 'DECK'}
                          </span>
                          <h3 className="font-display text-lg font-bold text-[var(--text-primary)]">
                            {deck.title}
                          </h3>
                        </div>
                        <button
                          title="Export Deck (JSON)"
                          onClick={() => exportDeck(deck.id, deck.title)}
                          className="p-2 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 text-[var(--text-tertiary)] hover:text-[var(--text-primary)]"
                        >
                          <Download size={16} />
                        </button>
                      </div>

                      <p className="text-xs text-[var(--text-secondary)] line-clamp-2">
                        {deck.description || 'Generated flashcard deck'}
                      </p>

                      <div className="flex items-center justify-between text-xs border-t border-black/10 dark:border-white/10 pt-3">
                        <span className="text-[var(--text-tertiary)]">{deck.cardCount || 0} cards</span>
                        <button
                          onClick={() => {
                            setReviewIndex(0);
                            setShowAnswer(false);
                            setReviewModalOpen(true);
                          }}
                          className="btn-neumorphic-primary py-1.5 px-3 text-xs"
                        >
                          Review Deck
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

      {/* Generation Modal */}
      {generateModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setGenerateModalOpen(false)} />
          <div className="relative w-full max-w-md card-neumorphic p-6 space-y-4">
            <h3 className="font-display font-bold text-lg text-[var(--text-primary)]">
              Generate AI Flashcards
            </h3>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-[var(--text-tertiary)] uppercase tracking-wider font-semibold mb-1">
                  Deck Title *
                </label>
                <input
                  type="text"
                  placeholder="e.g. Asthma High Yield Facts"
                  value={genTitle}
                  onChange={(e) => setGenTitle(e.target.value)}
                  className="input-neumorphic w-full text-xs"
                  disabled={generateMutation.isPending}
                />
              </div>

              <div>
                <label className="block text-[var(--text-tertiary)] uppercase tracking-wider font-semibold mb-1">
                  Card Count: {genCount}
                </label>
                <input
                  type="range"
                  min={1}
                  max={50}
                  value={genCount}
                  onChange={(e) => setGenCount(parseInt(e.target.value, 10))}
                  className="w-full"
                  disabled={generateMutation.isPending}
                />
              </div>

              <div>
                <label className="block text-[var(--text-tertiary)] uppercase tracking-wider font-semibold mb-1">
                  Target Difficulty
                </label>
                <select
                  value={genDifficulty}
                  onChange={(e) => setGenDifficulty(e.target.value as FlashcardDifficulty)}
                  className="input-neumorphic w-full text-xs"
                  disabled={generateMutation.isPending}
                >
                  <option value="EASY">EASY</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HARD">HARD</option>
                </select>
              </div>

              <div>
                <label className="block text-[var(--text-tertiary)] uppercase tracking-wider font-semibold mb-1">
                  Disease ID (Optional)
                </label>
                <input
                  type="number"
                  placeholder="e.g. 1"
                  value={genDiseaseId}
                  onChange={(e) => setGenDiseaseId(e.target.value)}
                  className="input-neumorphic w-full text-xs"
                  disabled={generateMutation.isPending}
                />
              </div>
            </div>

            {genError && (
              <div className="flex items-center gap-2 p-3 rounded-lg bg-red-500/10 border border-red-500/20 text-xs text-red-600">
                <AlertCircle size={14} className="shrink-0" />
                <span>{genError}</span>
              </div>
            )}

            <div className="flex gap-2 pt-2">
              <button
                onClick={() => generateMutation.mutate()}
                disabled={!genTitle.trim() || generateMutation.isPending}
                className="btn-neumorphic-primary py-2 px-4 w-full text-xs flex items-center justify-center gap-2 disabled:opacity-50"
              >
                {generateMutation.isPending ? <Loader2 size={14} className="animate-spin" /> : <Sparkles size={14} />}
                {generateMutation.isPending ? 'Generating...' : 'Generate Flashcards'}
              </button>
              <button
                onClick={() => setGenerateModalOpen(false)}
                className="btn-neumorphic-secondary py-2 px-4 text-xs"
                disabled={generateMutation.isPending}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* SM-2 Spaced Repetition Review Modal */}
      {reviewModalOpen && dueCards && dueCards.length > 0 && currentCardProgress && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setReviewModalOpen(false)} />
          <div className="relative w-full max-w-lg card-neumorphic p-6 space-y-6">
            <div className="flex items-center justify-between text-xs">
              <span className="font-semibold text-[var(--accent-primary)] uppercase tracking-wider">
                Reviewing Card {reviewIndex + 1} of {dueCards.length}
              </span>
              <button onClick={() => setReviewModalOpen(false)} className="text-[var(--text-tertiary)] hover:text-[var(--text-primary)]">
                <X size={16} />
              </button>
            </div>

            {/* Card Content */}
            <div className="min-h-[180px] p-6 rounded-2xl bg-black/5 dark:bg-white/5 border border-black/10 dark:border-white/10 flex flex-col justify-between space-y-4">
              <div className="text-center space-y-2">
                <span className="text-[10px] font-bold uppercase text-[var(--text-tertiary)]">Question</span>
                <p className="font-semibold text-base text-[var(--text-primary)]">
                  {currentCardProgress.flashcard?.question}
                </p>
              </div>

              {showAnswer ? (
                <div className="text-center border-t border-black/10 dark:border-white/10 pt-4 space-y-2">
                  <span className="text-[10px] font-bold uppercase text-green-600">Answer</span>
                  <p className="text-sm text-[var(--text-primary)] font-medium">
                    {currentCardProgress.flashcard?.answer}
                  </p>
                  {currentCardProgress.flashcard?.explanation && (
                    <p className="text-xs text-[var(--text-secondary)] italic mt-2">
                      {currentCardProgress.flashcard.explanation}
                    </p>
                  )}
                </div>
              ) : (
                <button
                  onClick={() => setShowAnswer(true)}
                  className="btn-neumorphic-secondary py-2 px-4 w-full text-xs flex items-center justify-center gap-2"
                >
                  <Eye size={14} /> Show Answer
                </button>
              )}
            </div>

            {/* SM-2 Quality Rating Buttons (0 to 5) */}
            {showAnswer && (
              <div className="space-y-2">
                <label className="block text-[10px] font-semibold text-center uppercase tracking-wider text-[var(--text-tertiary)]">
                  Rate Recall Quality (SM-2 Spaced Repetition)
                </label>
                <div className="grid grid-cols-6 gap-1">
                  {[
                    { q: 0, label: 'Blackout', color: 'bg-red-500/20 text-red-600' },
                    { q: 1, label: 'Wrong', color: 'bg-red-400/20 text-red-500' },
                    { q: 2, label: 'Hard Wrong', color: 'bg-amber-500/20 text-amber-600' },
                    { q: 3, label: 'Hard', color: 'bg-yellow-500/20 text-yellow-600' },
                    { q: 4, label: 'Good', color: 'bg-blue-500/20 text-blue-600' },
                    { q: 5, label: 'Perfect', color: 'bg-green-500/20 text-green-600' },
                  ].map((item) => (
                    <button
                      key={item.q}
                      onClick={() =>
                        reviewMutation.mutate({
                          cardId: currentCardProgress.flashcard.id,
                          quality: item.q,
                        })
                      }
                      disabled={reviewMutation.isPending}
                      className={`p-2 rounded-xl border border-transparent text-center transition-all ${item.color} hover:scale-105 disabled:opacity-50`}
                    >
                      <div className="font-bold text-xs">{item.q}</div>
                      <div className="text-[9px] line-clamp-1">{item.label}</div>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
