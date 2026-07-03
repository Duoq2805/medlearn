import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useUserStreak, useUserGoals, useUserProgress, useRecommendedCases } from '../../hooks/useUserData';
import Skeleton from '../../components/ui/Skeleton';
import {
  ArrowRight, ChevronRight, BookOpen, Brain, Sparkles,
  Flame, Clock, TrendingUp, Award, Target, Zap,
  GraduationCap, CheckCircle2, Plus, AlertCircle,
  Bookmark, BarChart3
} from 'lucide-react';
import { AnimatedSection, StaggerContainer, StaggerItem, FadeIn } from '../../components/motion/MotionWrappers';

/* ─── Skeleton ─── */
function DashSkeleton() {
  return (
    <div className="max-w-7xl mx-auto px-6 pt-32 pb-24 space-y-6 animate-pulse">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {[...Array(6)].map((_, i) => (
          <div key={i} className="card-neumorphic p-8"><Skeleton className="h-5 w-3/4 mb-4" /><Skeleton className="h-4 w-full" /></div>
        ))}
      </div>
    </div>
  );
}

/* ─── Stat Pill ─── */
function StatPill({ icon: Icon, label, value }: { icon: any; label: string; value: string }) {
  return (
    <div className="stat-pill">
      <Icon size={14} className="text-[var(--accent-primary)]" />
      <span>{label}</span>
      <span className="font-semibold text-[var(--text-primary)] ml-1">{value}</span>
    </div>
  );
}

/* ─── Dashboard ─── */
export default function UserDashboardPage() {
  const { user } = useAuth();
  const { data: streakData } = useUserStreak();
  const { data: goalsData } = useUserGoals();
  const { data: progressData } = useUserProgress();
  const { data: recommendedCases } = useRecommendedCases();

  const isLoading = !streakData || !goalsData || !progressData;

  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';
  const name = user?.fullName?.split(' ')[0] || user?.username || 'there';

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center pt-32">
        <div className="card-neumorphic p-12 text-center max-w-md mx-auto">
          <GraduationCap size={40} className="mx-auto mb-4 text-[var(--accent-primary)]" />
          <p className="text-lg font-semibold mb-2 text-[var(--text-primary)]">Welcome to Medvora</p>
          <p className="text-sm text-[var(--text-secondary)] mb-6">Sign in to access your learning dashboard</p>
          <Link to="/login" className="btn-neumorphic-primary py-3 px-8 inline-flex items-center gap-2">
            Sign In <ChevronRight size={18} />
          </Link>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return <div className="min-h-screen"><DashSkeleton /></div>;
  }

  return (
    <div className="min-h-screen pt-28 pb-24 relative z-10">
      <div className="max-w-7xl mx-auto px-6">

        {/* GREETING BAR */}
        <AnimatedSection>
          <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
            <div>
              <p className="text-sm font-medium text-[var(--text-tertiary)]">{greeting}</p>
              <h1 className="font-display text-3xl md:text-4xl font-bold text-[var(--text-primary)]">{name}</h1>
            </div>
            <div className="flex flex-wrap items-center gap-2">
              <StatPill icon={Flame} label="Streak" value={`${streakData.currentStreak} days`} />
              <StatPill icon={TrendingUp} label="Level" value="Intermediate" />
              <StatPill icon={Clock} label="Today" value="45 min" />
              <StatPill icon={Award} label="XP" value="2,450" />
            </div>
          </div>
        </AnimatedSection>

        {/* 2-COLUMN WORKSPACE */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

          {/* LEFT COLUMN (2/3) */}
          <div className="lg:col-span-2 space-y-6">

            {/* Hero Continue Learning */}
            <AnimatedSection>
              <div className="card-neumorphic p-8 relative overflow-hidden group">
                <div className="absolute top-0 right-0 w-56 h-56 rounded-full bg-gradient-to-bl from-[var(--accent-primary)]/6 to-transparent -translate-y-1/2 translate-x-1/2" />
                <div className="relative z-10">
                  <div className="flex items-center justify-between mb-1">
                    <div className="flex items-center gap-2">
                      <div className="icon-well icon-well-sm"><Target size={18} /></div>
                      <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">Continue Learning</h2>
                    </div>
                    <Link to="/explorer" className="text-sm font-medium text-[var(--accent-primary)] hover:underline">See all</Link>
                  </div>
                  <p className="text-sm text-[var(--text-secondary)] mb-5 ml-[52px]">Your current study queue</p>
                  <div className="space-y-3">
                    <Link to="/disease/pneumonia" className="depth-layer-1 rounded-2xl p-4 flex items-center gap-4 hover:-translate-y-0.5 transition-all">
                      <div className="icon-well icon-well-sm"><BookOpen size={18} /></div>
                      <div className="flex-1 min-w-0">
                        <p className="font-semibold text-sm text-[var(--text-primary)]">Pneumonia</p>
                        <p className="text-xs text-[var(--text-secondary)]">Continue reading · Chapter 3 of 8</p>
                      </div>
                      <ChevronRight size={16} className="text-[var(--text-tertiary)]" />
                    </Link>
                    <Link to="/case/1" className="depth-layer-1 rounded-2xl p-4 flex items-center gap-4 hover:-translate-y-0.5 transition-all">
                      <div className="icon-well icon-well-sm"><Brain size={18} /></div>
                      <div className="flex-1 min-w-0">
                        <p className="font-semibold text-sm text-[var(--text-primary)]">Respiratory Infection Case</p>
                        <p className="text-xs text-[var(--text-secondary)]">Draft diagnosis incomplete</p>
                      </div>
                      <ChevronRight size={16} className="text-[var(--text-tertiary)]" />
                    </Link>
                  </div>
                </div>
              </div>
            </AnimatedSection>

            {/* LEFT 2-COL GRID */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

              <AnimatedSection>
                <div className="card-neumorphic p-6 h-full">
                  <div className="flex items-center gap-2 mb-4">
                    <div className="icon-well icon-well-sm"><CheckCircle2 size={18} /></div>
                    <h2 className="font-display text-base font-bold text-[var(--text-primary)]">Today's Plan</h2>
                  </div>
                  <div className="space-y-2">
                    {[
                      { title: 'Review Pneumonia', done: true },
                      { title: 'Finish Thyroiditis', done: false },
                      { title: 'Practice 10 Flashcards', done: false },
                      { title: 'Take Quiz', done: false },
                    ].map((t, i) => (
                      <div key={i} className="depth-layer-2 rounded-xl p-3 flex items-center gap-3 text-sm">
                        <div className={`w-5 h-5 rounded-full flex items-center justify-center shrink-0 ${t.done ? 'bg-[var(--accent-primary)]' : 'border-2 border-[var(--text-tertiary)]'}`}>
                          {t.done && <CheckCircle2 size={14} className="text-white" />}
                        </div>
                        <span className={t.done ? 'line-through text-[var(--text-secondary)]' : 'text-[var(--text-primary)]'}>{t.title}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </AnimatedSection>

              <AnimatedSection>
                <div className="card-neumorphic p-6 h-full">
                  <div className="flex items-center gap-2 mb-4">
                    <div className="icon-well icon-well-sm"><Award size={18} /></div>
                    <h2 className="font-display text-base font-bold text-[var(--text-primary)]">Achievements</h2>
                  </div>
                  <div className="grid grid-cols-2 gap-2">
                    {[
                      { icon: Flame, label: '5-Day Streak', color: 'text-orange-500' },
                      { icon: Award, label: '10 Cases Solved', color: 'text-amber-500' },
                      { icon: Brain, label: 'Quiz Master', color: 'text-purple-500' },
                      { icon: Sparkles, label: 'Flashcard Novice', color: 'text-[var(--text-primary)]', faded: true },
                    ].map((b, i) => (
                      <div key={i} className={`text-center p-3 rounded-2xl ${b.faded ? 'opacity-50' : ''} depth-layer-3`}>
                        <b.icon size={20} className={`mx-auto mb-1 ${b.color}`} />
                        <p className="text-[11px] font-medium text-[var(--text-primary)]">{b.label}</p>
                      </div>
                    ))}
                  </div>
                  <p className="text-xs text-[var(--text-tertiary)] mt-3 text-center">XP: 2,450 · <span className="text-[var(--accent-primary)]">550 to next level</span></p>
                </div>
              </AnimatedSection>

              <AnimatedSection>
                <div className="card-neumorphic p-6 h-full">
                  <div className="flex items-center gap-2 mb-4">
                    <div className="icon-well icon-well-sm"><Clock size={18} /></div>
                    <h2 className="font-display text-base font-bold text-[var(--text-primary)]">Recently Viewed</h2>
                  </div>
                  <div className="space-y-2">
                    {[
                      { label: 'Pneumonia', meta: '2 hours ago' },
                      { label: 'Respiratory Case', meta: 'Yesterday' },
                      { label: 'Asthma', meta: '3 days ago' },
                    ].map((r, i) => (
                      <Link key={i} to="/explorer" className="depth-layer-1 rounded-xl p-3 flex items-center justify-between text-sm hover:-translate-y-0.5 transition-all">
                        <span className="text-[var(--text-primary)]">{r.label}</span>
                        <span className="text-xs text-[var(--text-tertiary)]">{r.meta}</span>
                      </Link>
                    ))}
                  </div>
                </div>
              </AnimatedSection>

              <AnimatedSection>
                <div className="card-neumorphic p-6 h-full">
                  <div className="flex items-center gap-2 mb-4">
                    <div className="icon-well icon-well-sm"><Bookmark size={18} /></div>
                    <h2 className="font-display text-base font-bold text-[var(--text-primary)]">Bookmarks</h2>
                  </div>
                  <div className="space-y-2">
                    {['Pneumonia', 'Hypertension', 'Diabetes'].map((b, i) => (
                      <Link key={i} to="/explorer" className="depth-layer-1 rounded-xl p-3 flex items-center gap-3 text-sm hover:-translate-y-0.5 transition-all">
                        <CheckCircle2 size={14} className="text-[var(--accent-secondary)]" />
                        <span className="text-[var(--text-primary)]">{b}</span>
                      </Link>
                    ))}
                    <Link to="/explorer" className="text-xs text-[var(--accent-primary)] hover:underline mt-2 inline-flex items-center gap-1">
                      View all bookmarks <ChevronRight size={12} />
                    </Link>
                  </div>
                </div>
              </AnimatedSection>
            </div>
          </div>

          {/* RIGHT COLUMN (1/3) */}
          <div className="space-y-6">

            <AnimatedSection>
              <div className="card-neumorphic p-6 relative overflow-hidden">
                <div className="absolute -top-10 -right-10 w-32 h-32 rounded-full bg-gradient-to-bl from-[var(--accent-primary)]/8 to-transparent" />
                <div className="relative z-10">
                  <div className="flex items-center gap-2 mb-3">
                    <div className="icon-well icon-well-sm"><Sparkles size={18} /></div>
                    <h2 className="font-display text-base font-bold text-[var(--text-primary)]">AI Coach</h2>
                  </div>
                  <p className="text-sm text-[var(--text-secondary)] mb-4 leading-relaxed">
                    Review <strong className="text-[var(--text-primary)]">Pneumonia</strong> today — your retention dropped from 85% to 72% this week.
                  </p>
                  <div className="flex gap-2">
                    <Link to="/explorer" className="btn-neumorphic-primary py-2 px-4 text-sm flex-1 text-center">Review Now</Link>
                    <button className="btn-neumorphic-secondary py-2 px-4 text-sm">Dismiss</button>
                  </div>
                </div>
              </div>
            </AnimatedSection>

            <AnimatedSection>
              <div className="card-neumorphic p-6">
                <div className="flex items-center gap-2 mb-4">
                  <div className="icon-well icon-well-sm"><BarChart3 size={18} /></div>
                  <h2 className="font-display text-base font-bold text-[var(--text-primary)]">Weekly Progress</h2>
                </div>
                <div className="space-y-4">
                  {[
                    { label: 'Study Minutes', current: '285', total: '420', pct: 68 },
                    { label: 'Diseases', current: '3', total: '5', pct: 60 },
                    { label: 'Quiz Accuracy', current: '78', total: '100', pct: 78 },
                  ].map((m) => (
                    <div key={m.label}>
                      <div className="flex justify-between text-xs mb-1">
                        <span className="text-[var(--text-secondary)]">{m.label}</span>
                        <span className="font-semibold text-[var(--text-primary)]">{m.current}/{m.total}</span>
                      </div>
                      <div className="h-2 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_6px_var(--shadow-dark),inset_-2px_-2px_6px_var(--shadow-light)] overflow-hidden">
                        <div className="h-full rounded-full bg-gradient-to-r from-[var(--accent-primary)] to-emerald-300" style={{ width: `${m.pct}%` }} />
                      </div>
                    </div>
                  ))}
                </div>
                <div className="mt-4 pt-3 border-t border-[var(--shadow-dark)]">
                  <p className="text-xs text-[var(--text-tertiary)]">10 of 15 activities this week</p>
                </div>
              </div>
            </AnimatedSection>

            <AnimatedSection>
              <div className="card-neumorphic p-6">
                <div className="flex items-center gap-2 mb-3">
                  <div className="icon-well icon-well-sm"><Zap size={18} /></div>
                  <h2 className="font-display text-base font-bold text-[var(--text-primary)]">Quick Actions</h2>
                </div>
                <div className="space-y-2">
                  {[
                    { to: '/explorer', icon: BookOpen, label: 'Browse Diseases' },
                    { to: '/symptom-checker', icon: Brain, label: 'Symptom Checker' },
                    { to: '/cases', icon: Target, label: 'Clinical Cases' },
                    { to: '/dashboard', icon: Sparkles, label: 'Generate Quiz' },
                  ].map((a, i) => (
                    <Link key={i} to={a.to} className="depth-layer-1 rounded-2xl p-3 flex items-center gap-3 text-sm hover:-translate-y-0.5 transition-all">
                      <a.icon size={16} className="text-[var(--accent-primary)]" />
                      <span className="text-[var(--text-primary)]">{a.label}</span>
                      <ChevronRight size={14} className="ml-auto text-[var(--text-tertiary)]" />
                    </Link>
                  ))}
                </div>
              </div>
            </AnimatedSection>

            <AnimatedSection>
              <div className="card-neumorphic p-6">
                <div className="flex items-center gap-2 mb-3">
                  <div className="icon-well icon-well-sm"><Plus size={18} /></div>
                  <h2 className="font-display text-base font-bold text-[var(--text-primary)]">Draft Diseases</h2>
                </div>
                <p className="text-xs text-[var(--text-tertiary)] mb-3">As a Student, browse published diseases.</p>
                <Link to="/explorer" className="btn-neumorphic-primary py-2 px-4 text-sm text-center w-full inline-flex items-center justify-center gap-2">
                  Browse Published <ChevronRight size={14} />
                </Link>
              </div>
            </AnimatedSection>
          </div>
        </div>
      </div>
    </div>
  );
}
