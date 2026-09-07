import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { adminApi } from '../../api/admin';
import { diseaseApi } from '../../api/disease';
import { caseApi } from '../../api/case';
import { symptomApi } from '../../api/symptom';
import { ShieldClose, BarChart3, TrendingUp, TrendingDown, Download,
  Users, BookOpen, Activity, FolderKanban, CheckCircle2, Globe, Clock } from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminReportsPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [timeRange, setTimeRange] = useState('7d');

  const { data: analytics } = useQuery({
    queryKey: ['admin', 'analytics'],
    queryFn: () => adminApi.fetchAnalytics(),
    staleTime: 60000,
  });

  const { data: diseasesData } = useQuery({
    queryKey: ['diseases', 0, 1],
    queryFn: () => diseaseApi.fetchDiseases(undefined, undefined, undefined, 0, 1),
    staleTime: 60000,
    select: (d: any) => d?.totalElements ?? '—',
  });

  const { data: casesTotal } = useQuery({
    queryKey: ['cases', 0, 1],
    queryFn: () => caseApi.fetchCases(0, 1),
    staleTime: 60000,
    select: (d: any) => d?.totalElements ?? '—',
  });

  const { data: symptoms } = useQuery({
    queryKey: ['symptoms'],
    queryFn: () => symptomApi.fetchSymptoms(),
    staleTime: 60000,
    select: (d: any) => Array.isArray(d) ? d.length : '—',
  });

  if (role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const timeOptions = ['24h', '7d', '30d', '90d', '1y'];

  const statCards = [
    { icon: Users, label: 'Total Users', value: analytics?.totalUsers ?? '—', color: 'text-blue-500' },
    { icon: BookOpen, label: 'Diseases', value: analytics?.totalDiseases ?? diseasesData, color: 'text-emerald-500' },
    { icon: Activity, label: 'Case Studies', value: analytics?.totalCases ?? casesTotal, color: 'text-cyan-500' },
    { icon: FolderKanban, label: 'Pending Reviews', value: analytics?.totalPendingReviews ?? '—', color: 'text-amber-500' },
    { icon: Globe, label: 'Symptoms', value: analytics?.totalSymptoms ?? symptoms, color: 'text-purple-500' },
    { icon: CheckCircle2, label: 'Reviewers', value: analytics?.totalReviewers ?? '—', color: 'text-rose-500' },
  ];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well"><BarChart3 size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Reports</h1>
                <p className="text-sm text-[var(--text-secondary)]">Platform statistics from analytics API</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <div className="flex gap-1">
                {timeOptions.map((t) => (
                  <button key={t} onClick={() => setTimeRange(t)}
                    className={`px-3 py-1.5 text-xs rounded-lg transition-all ${timeRange === t ? 'bg-[var(--accent-primary)] text-white' : 'btn-neumorphic-secondary'}`}>{t}</button>
                ))}
              </div>
              <button className="btn-neumorphic-secondary py-2 px-3 text-sm flex items-center gap-2"><Download size={14} /> Export</button>
            </div>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
            {statCards.map((s) => (
              <div key={s.label} className="card-neumorphic p-4">
                <s.icon size={18} className={`${s.color} mb-2`} />
                <p className="text-xl font-bold text-[var(--text-primary)]">{s.value}</p>
                <p className="text-xs text-[var(--text-secondary)]">{s.label}</p>
              </div>
            ))}
          </div>

          <div className="card-neumorphic p-6">
            <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-2">Analytics Data</h2>
            <p className="text-xs text-[var(--text-tertiary)] mb-4">
              {analytics ? 'Using /admin/analytics endpoint' : 'Using per-entity counts from existing APIs'}
            </p>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {statCards.map((s) => (
                <div key={s.label} className="depth-layer-1 rounded-xl p-4 flex items-center gap-3">
                  <s.icon size={20} className={s.color} />
                  <div>
                    <p className="text-xs text-[var(--text-tertiary)]">{s.label}</p>
                    <p className="text-2xl font-bold text-[var(--text-primary)]">{s.value}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
