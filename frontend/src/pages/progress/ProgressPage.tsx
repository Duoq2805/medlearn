import { Link } from 'react-router-dom';
import { BarChart3, ChevronRight } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useUserStreak, useUserGoals, useUserProgress } from '../../hooks/useUserData';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function ProgressPage() {
  const { user } = useAuth();
  const { data: streakData } = useUserStreak();
  const { data: goalsData } = useUserGoals();
  const { data: progressData } = useUserProgress();

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><BarChart3 size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Learning Progress</h1>
              <p className="text-sm text-[var(--text-secondary)]">Track your improvement over time</p>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div className="card-neumorphic p-6 text-center">
              <p className="text-xs text-[var(--text-tertiary)] mb-1">Streak</p>
              <p className="font-display text-4xl font-bold text-[var(--text-primary)]">{streakData?.currentStreak || 0}</p>
              <p className="text-xs text-[var(--text-secondary)]">days</p>
            </div>
            <div className="card-neumorphic p-6 text-center">
              <p className="text-xs text-[var(--text-tertiary)] mb-1">Cases Solved</p>
              <p className="font-display text-4xl font-bold text-[var(--text-primary)]">{progressData?.totalCasesCompleted || 0}</p>
              <p className="text-xs text-[var(--text-secondary)]">completed</p>
            </div>
            <div className="card-neumorphic p-6 text-center">
              <p className="text-xs text-[var(--text-tertiary)] mb-1">Completion Rate</p>
              <p className="font-display text-4xl font-bold text-[var(--text-primary)]">{Math.round((progressData?.completionRate || 0) * 100)}%</p>
              <p className="text-xs text-[var(--text-secondary)]">overall</p>
            </div>
          </div>
          <div className="card-neumorphic p-6 text-center">
            <BarChart3 size={40} className="mx-auto mb-3 text-[var(--text-tertiary)]" />
            <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">Detailed analytics coming soon</p>
            <p className="text-sm text-[var(--text-secondary)] mb-4">Heatmaps, retention charts, and study time graphs are in development.</p>
            <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6 inline-flex items-center gap-2">
              Back to Dashboard <ChevronRight size={16} />
            </Link>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
