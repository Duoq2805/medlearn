import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ClipboardCheck, BookOpen, Clock, Search, ChevronRight,
  Plus, BarChart3, Brain, Target, AlertCircle, RefreshCw
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';
import Skeleton from '../../components/ui/Skeleton';

export default function QuizPage() {
  const { user } = useAuth();
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading] = useState(false);

  const mockQuizzes = [
    { id: '1', title: 'Pneumonia Quiz', questions: 10, accuracy: 80, lastTaken: '2h ago' },
    { id: '2', title: 'Hypertension Quiz', questions: 8, accuracy: 65, lastTaken: 'Yesterday' },
    { id: '3', title: 'Diabetes Type 2 Quiz', questions: 12, accuracy: 92, lastTaken: '3 days ago' },
  ];

  const filtered = mockQuizzes.filter(q => q.title.toLowerCase().includes(searchTerm.toLowerCase()));

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><ClipboardCheck size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Quiz</h1>
              <p className="text-sm text-[var(--text-secondary)]">Test your knowledge with adaptive quizzes</p>
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
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="input-neumorphic w-full pl-9 py-2 text-sm"
                  />
                  <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
                </div>
                <button disabled className="btn-neumorphic-primary py-2 px-3 w-full text-sm flex items-center justify-center gap-2 opacity-50 cursor-not-allowed">
                  <Plus size={14} /> Generate Quiz
                </button>
              </div>
              <div className="card-neumorphic p-4 space-y-2">
                <h3 className="text-xs font-semibold uppercase tracking-wider text-[var(--text-tertiary)]">Performance</h3>
                {[
                  { label: 'Average Score', value: '79%', icon: Brain },
                  { label: 'Quizzes Taken', value: '12', icon: BookOpen },
                  { label: 'Weak Topics', value: '3', icon: Target },
                  { label: 'Study Streak', value: '5 days', icon: Clock },
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
                      <Skeleton className="h-4 w-full mb-4" />
                      <Skeleton className="h-8 w-24" />
                    </div>
                  ))}
                </div>
              ) : filtered.length === 0 ? (
                <div className="card-neumorphic p-12 text-center">
                  <ClipboardCheck size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
                  <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No quizzes yet</p>
                  <p className="text-sm text-[var(--text-secondary)] mb-6">
                    Quizzes help reinforce your medical knowledge. Generate one from any disease to get started.
                  </p>
                  <button disabled className="btn-neumorphic-primary py-2 px-6 text-sm opacity-50 cursor-not-allowed">
                    Generate Your First Quiz
                  </button>
                  <p className="text-xs text-[var(--text-tertiary)] mt-4">Quiz generation endpoint in development. Coming soon.</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {filtered.map((quiz) => (
                    <div key={quiz.id} className="card-neumorphic p-6 hover:shadow-lg transition-all">
                      <h3 className="font-display text-lg font-bold text-[var(--text-primary)] mb-2">{quiz.title}</h3>
                      <div className="flex items-center gap-4 text-xs text-[var(--text-secondary)] mb-4">
                        <span>{quiz.questions} questions</span>
                        <span>Last: {quiz.lastTaken}</span>
                      </div>
                      <div className="flex items-center gap-2 mb-4">
                        <div className="flex-1 h-2 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_4px_var(--shadow-dark)] overflow-hidden">
                          <div className="h-full bg-gradient-to-r from-[var(--accent-primary)] to-emerald-300 rounded-full transition-all" style={{ width: `${quiz.accuracy}%` }} />
                        </div>
                        <span className={`text-xs font-semibold ${quiz.accuracy >= 80 ? 'text-[var(--accent-primary)]' : quiz.accuracy >= 60 ? 'text-amber-500' : 'text-red-500'}`}>{quiz.accuracy}%</span>
                      </div>
                      <div className="flex gap-2">
                        <button className="btn-neumorphic-primary py-2 px-4 flex-1 text-sm">Take Quiz</button>
                        <button className="btn-neumorphic-secondary py-2 px-4 text-sm">Review</button>
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
