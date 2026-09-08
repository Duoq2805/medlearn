import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  ClipboardCheck, BookOpen, Clock, Search, Plus,
  Brain, Target, Loader2, AlertCircle, ChevronRight, RotateCw,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';
import Skeleton from '../../components/ui/Skeleton';
import { quizApi } from '../../api/quiz';
import type {
  QuizResponse, QuizGenerateRequest, QuizSubmitRequest,
  QuestionResult,
} from '../../types/quiz';

type Screen = 'list' | 'taking' | 'result';

export default function QuizPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [screen, setScreen] = useState<Screen>('list');
  const [activeQuiz, setActiveQuiz] = useState<QuizResponse | null>(null);
  const [answers, setAnswers] = useState<Record<number, 'A' | 'B' | 'C' | 'D'>>({});
  const [results, setResults] = useState<QuestionResult[]>([]);
  const [score, setScore] = useState({ correct: 0, total: 0, percentage: 0 });

  // Generate modal state
  const [genOpen, setGenOpen] = useState(false);
  const [genTitle, setGenTitle] = useState('');
  const [genDiseaseId, setGenDiseaseId] = useState('');
  const [genCount, setGenCount] = useState(10);
  const [genError, setGenError] = useState<string | null>(null);

  const { data: quizzesData, isLoading, error } = useQuery({
    queryKey: ['quizzes'],
    queryFn: () => quizApi.listByUser(0, 50),
  });

  const { data: attemptsData } = useQuery({
    queryKey: ['quizAttempts'],
    queryFn: () => quizApi.listAttempts(0, 50),
  });

  const generateMutation = useMutation({
    mutationFn: (req: QuizGenerateRequest) => quizApi.generate(req),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['quizzes'] });
      setGenOpen(false);
      setGenTitle('');
      setGenDiseaseId('');
      setGenError(null);
      // Auto-start the generated quiz
      setActiveQuiz(data.quiz);
      setAnswers({});
      setScreen('taking');
    },
    onError: (err: any) => {
      setGenError(err.response?.data?.message || err.message || 'Generation failed');
    },
  });

  const submitMutation = useMutation({
    mutationFn: ({ quizId, req }: { quizId: number; req: QuizSubmitRequest }) =>
      quizApi.submit(quizId, req),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['quizAttempts'] });
      setResults(data.results);
      setScore({ correct: data.correctCount, total: data.totalQuestions, percentage: data.percentage });
      setScreen('result');
    },
  });

  const quizzes = quizzesData?.content ?? [];
  const attempts = attemptsData?.content ?? [];
  const filtered = quizzes.filter(q => q.title.toLowerCase().includes(searchTerm.toLowerCase()));

  const avgScore = attempts.length
    ? Math.round(attempts.reduce((s, a) => s + a.percentage, 0) / attempts.length)
    : 0;

  function startQuiz(quiz: QuizResponse) {
    setActiveQuiz(quiz);
    setAnswers({});
    setScreen('taking');
  }

  function submitQuiz() {
    if (!activeQuiz) return;
    const answersArr: QuizSubmitRequest['answers'] = activeQuiz.questions.map(q => ({
      questionId: q.id,
      selectedAnswer: answers[q.id] ?? 'A',
    }));
    submitMutation.mutate({ quizId: activeQuiz.id, req: { answers: answersArr } });
  }

  // ── Taking screen ──────────────────────────────────────────────────────────
  if (screen === 'taking' && activeQuiz) {
    const questions = activeQuiz.questions;
    const answered = Object.keys(answers).length;
    return (
      <div className="min-h-screen pt-32 pb-24 px-6">
        <div className="max-w-3xl mx-auto">
          <AnimatedSection>
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)]">{activeQuiz.title}</h2>
                <p className="text-sm text-[var(--text-secondary)]">{answered}/{questions.length} answered</p>
              </div>
              <button onClick={() => setScreen('list')} className="btn-neumorphic-secondary py-2 px-4 text-sm">
                ← Back
              </button>
            </div>

            <div className="space-y-6 mb-8">
              {questions.map((q, idx) => (
                <div key={q.id} className="card-neumorphic p-6">
                  <p className="font-semibold text-[var(--text-primary)] mb-4">
                    {idx + 1}. {q.content}
                  </p>
                  <div className="grid grid-cols-1 gap-2">
                    {(['A', 'B', 'C', 'D'] as const).map(opt => {
                      const optText = q[`option${opt}` as 'optionA' | 'optionB' | 'optionC' | 'optionD'];
                      const selected = answers[q.id] === opt;
                      return (
                        <button
                          key={opt}
                          onClick={() => setAnswers(prev => ({ ...prev, [q.id]: opt }))}
                          className={`text-left px-4 py-3 rounded-xl text-sm transition-all border ${
                            selected
                              ? 'border-[var(--accent-primary)] bg-[var(--accent-primary)]/10 text-[var(--accent-primary)] font-semibold'
                              : 'border-transparent btn-neumorphic-secondary text-[var(--text-secondary)]'
                          }`}
                        >
                          <span className="font-bold mr-2">{opt}.</span>{optText}
                        </button>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>

            <button
              onClick={submitQuiz}
              disabled={submitMutation.isPending || answered < questions.length}
              className="btn-neumorphic-primary py-3 px-8 w-full flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {submitMutation.isPending ? <Loader2 size={16} className="animate-spin" /> : <ClipboardCheck size={16} />}
              {answered < questions.length ? `Answer all questions (${questions.length - answered} left)` : 'Submit Quiz'}
            </button>
          </AnimatedSection>
        </div>
      </div>
    );
  }

  // ── Result screen ──────────────────────────────────────────────────────────
  if (screen === 'result' && activeQuiz) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6">
        <div className="max-w-3xl mx-auto">
          <AnimatedSection>
            <div className="card-neumorphic p-8 text-center mb-6">
              <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-2">Quiz Complete!</h2>
              <p className="text-5xl font-bold text-[var(--accent-primary)] my-4">{Math.round(score.percentage)}%</p>
              <p className="text-[var(--text-secondary)]">{score.correct} / {score.total} correct</p>
              <div className="flex gap-3 mt-6 justify-center">
                <button onClick={() => { setScreen('list'); setActiveQuiz(null); }} className="btn-neumorphic-secondary py-2 px-6 text-sm">
                  Back to Quizzes
                </button>
                <button onClick={() => startQuiz(activeQuiz)} className="btn-neumorphic-primary py-2 px-6 text-sm flex items-center gap-2">
                  <RotateCw size={14} /> Retry
                </button>
              </div>
            </div>

            <div className="space-y-4">
              {results.map((r, idx) => {
                const q = activeQuiz.questions.find(q => q.id === r.questionId);
                return (
                  <div key={r.questionId} className={`card-neumorphic p-5 border-l-4 ${r.correct ? 'border-emerald-400' : 'border-red-400'}`}>
                    <p className="font-semibold text-[var(--text-primary)] mb-2">{idx + 1}. {q?.content}</p>
                    <p className="text-sm text-[var(--text-secondary)]">
                      Your answer: <span className={r.correct ? 'text-emerald-500 font-semibold' : 'text-red-500 font-semibold'}>{r.selectedAnswer}</span>
                      {!r.correct && <span className="ml-2 text-emerald-500 font-semibold">✓ {r.correctAnswer}</span>}
                    </p>
                    {r.explanation && <p className="text-xs text-[var(--text-tertiary)] mt-2 italic">{r.explanation}</p>}
                  </div>
                );
              })}
            </div>
          </AnimatedSection>
        </div>
      </div>
    );
  }

  // ── List screen ────────────────────────────────────────────────────────────
  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><ClipboardCheck size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Quiz</h1>
              <p className="text-sm text-[var(--text-secondary)]">Test your knowledge with AI-generated quizzes</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            {/* Left Panel */}
            <div className="lg:col-span-1 space-y-4">
              <div className="card-neumorphic p-4">
                <div className="relative mb-3">
                  <input
                    type="text"
                    placeholder="Search quizzes..."
                    value={searchTerm}
                    onChange={e => setSearchTerm(e.target.value)}
                    className="input-neumorphic w-full !pl-11 py-2 text-sm"
                  />
                  <Search size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)] pointer-events-none" />
                </div>
                <button
                  onClick={() => setGenOpen(true)}
                  className="btn-neumorphic-primary py-2 px-3 w-full text-sm flex items-center justify-center gap-2"
                >
                  <Plus size={14} /> Generate Quiz
                </button>
              </div>

              <div className="card-neumorphic p-4 space-y-2">
                <h3 className="text-xs font-semibold uppercase tracking-wider text-[var(--text-tertiary)]">Performance</h3>
                {[
                  { label: 'Average Score', value: attempts.length ? `${avgScore}%` : '—', icon: Brain },
                  { label: 'Quizzes Taken', value: String(attempts.length), icon: BookOpen },
                  { label: 'Total Quizzes', value: String(quizzes.length), icon: Target },
                  { label: 'Last Attempt', value: attempts[0] ? new Date(attempts[0].createdAt!).toLocaleDateString() : '—', icon: Clock },
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

            {/* Right: Quiz List */}
            <div className="lg:col-span-3">
              {isLoading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {[...Array(3)].map((_, i) => (
                    <div key={i} className="card-neumorphic p-6">
                      <Skeleton className="h-5 w-3/4 mb-4" />
                      <Skeleton className="h-4 w-1/2 mb-2" />
                      <Skeleton className="h-8 w-24" />
                    </div>
                  ))}
                </div>
              ) : error ? (
                <div className="card-neumorphic p-8 text-center">
                  <AlertCircle size={32} className="mx-auto mb-3 text-red-400" />
                  <p className="text-[var(--text-secondary)]">Failed to load quizzes</p>
                </div>
              ) : filtered.length === 0 ? (
                <div className="card-neumorphic p-12 text-center">
                  <ClipboardCheck size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
                  <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No quizzes yet</p>
                  <p className="text-sm text-[var(--text-secondary)] mb-6">
                    Generate a quiz from any disease to get started.
                  </p>
                  <button onClick={() => setGenOpen(true)} className="btn-neumorphic-primary py-2 px-6 text-sm">
                    Generate Your First Quiz
                  </button>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {filtered.map(quiz => {
                    const bestAttempt = attempts
                      .filter(a => a.quizId === quiz.id)
                      .sort((a, b) => b.percentage - a.percentage)[0];
                    return (
                      <div key={quiz.id} className="card-neumorphic p-6 hover:shadow-lg transition-all">
                        <h3 className="font-display text-lg font-bold text-[var(--text-primary)] mb-2">{quiz.title}</h3>
                        <div className="flex items-center gap-4 text-xs text-[var(--text-secondary)] mb-4">
                          <span>{quiz.questionCount} questions</span>
                          {bestAttempt && <span>Best: {Math.round(bestAttempt.percentage)}%</span>}
                        </div>
                        {bestAttempt && (
                          <div className="flex items-center gap-2 mb-4">
                            <div className="flex-1 h-2 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_4px_var(--shadow-dark)] overflow-hidden">
                              <div
                                className="h-full bg-gradient-to-r from-[var(--accent-primary)] to-emerald-300 rounded-full transition-all"
                                style={{ width: `${bestAttempt.percentage}%` }}
                              />
                            </div>
                            <span className={`text-xs font-semibold ${bestAttempt.percentage >= 80 ? 'text-[var(--accent-primary)]' : bestAttempt.percentage >= 60 ? 'text-amber-500' : 'text-red-500'}`}>
                              {Math.round(bestAttempt.percentage)}%
                            </span>
                          </div>
                        )}
                        <div className="flex gap-2">
                          <button
                            onClick={() => startQuiz(quiz)}
                            className="btn-neumorphic-primary py-2 px-4 flex-1 text-sm flex items-center justify-center gap-1"
                          >
                            {bestAttempt ? <><RotateCw size={13} /> Retry</> : <><ChevronRight size={13} /> Start</>}
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </AnimatedSection>

        {/* Generate Modal */}
        {genOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
            <div className="card-neumorphic p-8 w-full max-w-md mx-4">
              <h2 className="font-display text-xl font-bold text-[var(--text-primary)] mb-6">Generate Quiz</h2>

              <div className="space-y-4">
                <div>
                  <label className="text-sm text-[var(--text-secondary)] mb-1 block">Title *</label>
                  <input
                    type="text"
                    value={genTitle}
                    onChange={e => setGenTitle(e.target.value)}
                    placeholder="e.g. Pneumonia Quiz"
                    className="input-neumorphic w-full py-2"
                  />
                </div>
                <div>
                  <label className="text-sm text-[var(--text-secondary)] mb-1 block">Disease ID</label>
                  <input
                    type="number"
                    value={genDiseaseId}
                    onChange={e => setGenDiseaseId(e.target.value)}
                    placeholder="Leave blank to use document"
                    className="input-neumorphic w-full py-2"
                  />
                </div>
                <div>
                  <label className="text-sm text-[var(--text-secondary)] mb-1 block">Number of questions</label>
                  <input
                    type="number"
                    min={1} max={30}
                    value={genCount}
                    onChange={e => setGenCount(Number(e.target.value))}
                    className="input-neumorphic w-full py-2"
                  />
                </div>
                {genError && (
                  <div className="flex items-center gap-2 text-red-400 text-sm">
                    <AlertCircle size={14} /> {genError}
                  </div>
                )}
              </div>

              <div className="flex gap-3 mt-6">
                <button
                  onClick={() => { setGenOpen(false); setGenError(null); }}
                  className="btn-neumorphic-secondary py-2 px-4 flex-1 text-sm"
                >
                  Cancel
                </button>
                <button
                  onClick={() => generateMutation.mutate({
                    title: genTitle.trim(),
                    count: genCount,
                    ...(genDiseaseId ? { diseaseId: parseInt(genDiseaseId, 10) } : {}),
                  })}
                  disabled={generateMutation.isPending || !genTitle.trim()}
                  className="btn-neumorphic-primary py-2 px-4 flex-1 text-sm flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  {generateMutation.isPending ? <Loader2 size={14} className="animate-spin" /> : <Plus size={14} />}
                  Generate
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
