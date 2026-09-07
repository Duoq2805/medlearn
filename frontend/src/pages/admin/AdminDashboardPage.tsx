import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { adminApi } from '../../api/admin';
import { diseaseApi } from '../../api/disease';
import { caseApi } from '../../api/case';
import {
  ShieldClose, Users, BookOpen, Activity, FolderKanban,
  CheckCircle2, Clock, UserPlus, Database, AlertTriangle, FileText, Cpu
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminDashboardPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();

  const { data: analytics } = useQuery({
    queryKey: ['admin', 'analytics'],
    queryFn: () => adminApi.fetchAnalytics(),
    staleTime: 60000,
  });

  const { data: pendingReviews } = useQuery({
    queryKey: ['admin', 'pending-reviews'],
    queryFn: () => diseaseApi.getPendingReviewVersions(0, 5),
    staleTime: 30000,
  });

  const { data: usersData } = useQuery({
    queryKey: ['admin', 'users', 0, 5],
    queryFn: () => adminApi.fetchUsers(0, 5),
    staleTime: 30000,
  });

  const { data: diseasesData } = useQuery({
    queryKey: ['diseases', 0, 20],
    queryFn: () => diseaseApi.fetchDiseases(undefined, undefined, undefined, 0, 20),
    staleTime: 60000,
  });

  const { data: casesData } = useQuery({
    queryKey: ['cases', 0, 20],
    queryFn: () => caseApi.fetchCases(0, 20),
    staleTime: 60000,
  });

  if (role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const totalUsers = analytics?.totalUsers ?? usersData?.totalElements ?? '—';
  const totalDiseases = analytics?.totalDiseases ?? diseasesData?.totalElements ?? '—';
  const totalCases = analytics?.totalCases ?? casesData?.totalElements ?? '—';
  const totalReviews = analytics?.totalPendingReviews ?? pendingReviews?.totalElements ?? '—';
  const recentUsers = usersData?.content?.slice(0, 3) || [];
  const recentDiseases = diseasesData?.content?.slice(0, 3) || [];

  return (
    <div className="min-h-screen pt-24 pb-24 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <AnimatedSection className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-extrabold text-[var(--text-primary)]">Admin Control Center</h1>
          <p className="text-sm text-[var(--text-secondary)] mt-1">Platform-wide overview, content moderation & user management</p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/admin/ai" className="btn-neumorphic text-xs font-semibold py-2 px-4 flex items-center gap-1.5 text-[var(--text-primary)]">
            <Cpu size={14} /> AI Gateway
          </Link>
          <Link to="/admin/users" className="btn-neumorphic text-xs font-semibold py-2 px-4 flex items-center gap-1.5 text-[var(--text-primary)]">
            <Users size={14} /> Manage Users
          </Link>
          <Link to="/admin/diseases" className="btn-neumorphic-primary text-xs font-semibold py-2 px-4 flex items-center gap-1.5">
            <BookOpen size={14} /> Manage Content
          </Link>
        </div>
      </AnimatedSection>

      {/* Metrics Row */}
      <AnimatedSection className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="card-neumorphic p-5 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl depth-layer-1 flex items-center justify-center text-[var(--accent-primary)] shrink-0">
            <Users size={22} />
          </div>
          <div>
            <p className="text-2xl font-bold font-display text-[var(--text-primary)]">{totalUsers}</p>
            <p className="text-xs text-[var(--text-tertiary)] uppercase tracking-wider font-semibold">Total Users</p>
          </div>
        </div>

        <div className="card-neumorphic p-5 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl depth-layer-1 flex items-center justify-center text-[var(--accent-primary)] shrink-0">
            <BookOpen size={22} />
          </div>
          <div>
            <p className="text-2xl font-bold font-display text-[var(--text-primary)]">{totalDiseases}</p>
            <p className="text-xs text-[var(--text-tertiary)] uppercase tracking-wider font-semibold">Diseases</p>
          </div>
        </div>

        <div className="card-neumorphic p-5 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl depth-layer-1 flex items-center justify-center text-amber-500 shrink-0">
            <Clock size={22} />
          </div>
          <div>
            <p className="text-2xl font-bold font-display text-[var(--text-primary)]">{totalReviews}</p>
            <p className="text-xs text-[var(--text-tertiary)] uppercase tracking-wider font-semibold">Pending Reviews</p>
          </div>
        </div>

        <div className="card-neumorphic p-5 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl depth-layer-1 flex items-center justify-center text-[var(--accent-primary)] shrink-0">
            <FolderKanban size={22} />
          </div>
          <div>
            <p className="text-2xl font-bold font-display text-[var(--text-primary)]">{totalCases}</p>
            <p className="text-xs text-[var(--text-tertiary)] uppercase tracking-wider font-semibold">Case Studies</p>
          </div>
        </div>
      </AnimatedSection>

      {/* Main Grid: Content & User Activity */}
      <AnimatedSection className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: User & Disease Lists */}
        <div className="lg:col-span-2 space-y-6">
          {/* Users Card */}
          <div className="card-neumorphic p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-display text-lg font-bold text-[var(--text-primary)] flex items-center gap-2">
                <Users size={18} className="text-[var(--accent-primary)]" /> Recent Users
              </h2>
              <Link to="/admin/users" className="text-xs font-semibold text-[var(--accent-primary)] hover:underline">
                View all ({totalUsers})
              </Link>
            </div>
            {recentUsers.length > 0 ? (
              <div className="space-y-3">
                {recentUsers.map((u) => (
                  <div key={u.id} className="flex items-center justify-between depth-layer-1 rounded-xl p-3">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full depth-layer-2 flex items-center justify-center text-xs font-bold text-[var(--accent-primary)] uppercase">
                        {u.username?.[0] || 'U'}
                      </div>
                      <div>
                        <p className="text-sm font-semibold text-[var(--text-primary)]">{u.username}</p>
                        <p className="text-xs text-[var(--text-tertiary)]">{u.email}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] px-2 py-0.5 rounded-full font-medium depth-layer-2 text-[var(--text-secondary)]">
                        {u.roles?.[0] || 'USER'}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-[var(--text-tertiary)] text-center py-6">No user data available</p>
            )}
          </div>

          {/* Diseases Card */}
          <div className="card-neumorphic p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-display text-lg font-bold text-[var(--text-primary)] flex items-center gap-2">
                <BookOpen size={18} className="text-[var(--accent-primary)]" /> Disease Records
              </h2>
              <Link to="/admin/diseases" className="text-xs font-semibold text-[var(--accent-primary)] hover:underline">
                View all ({totalDiseases})
              </Link>
            </div>
            {recentDiseases.length > 0 ? (
              <div className="space-y-3">
                {recentDiseases.map((d) => (
                  <div key={d.id} className="flex items-center justify-between depth-layer-1 rounded-xl p-3">
                    <div>
                      <p className="text-sm font-semibold text-[var(--text-primary)]">{d.name}</p>
                      <p className="text-xs text-[var(--text-tertiary)]">Slug: {d.slug} • Updated {d.updatedAt ? new Date(d.updatedAt).toLocaleDateString() : '—'}</p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-[var(--text-tertiary)] text-center py-6">No disease data available</p>
            )}
          </div>
        </div>

        {/* Right: Pending Reviews */}
        <div className="space-y-6">
          <div className="card-neumorphic p-6">
            <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Pending Reviews</h2>
            {pendingReviews?.content && pendingReviews.content.length > 0 ? (
              <div className="space-y-2">
                {pendingReviews.content.slice(0, 5).map((r) => (
                  <div key={r.id} className="flex items-center justify-between depth-layer-1 rounded-lg p-3">
                    <div className="flex-1 min-w-0">
                      <p className="text-xs font-medium text-[var(--text-primary)] truncate">Version v{r.versionNumber} (Disease #{r.diseaseId})</p>
                      <p className="text-[10px] text-[var(--text-tertiary)]">Version v{r.versionNumber}</p>
                    </div>
                    <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-amber-500/10 text-amber-600">{r.status}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-[var(--text-tertiary)] text-center py-6">
                {pendingReviews ? 'All caught up' : 'Loading...'}
              </p>
            )}
            <Link to="/reviewer/queue" className="mt-3 block text-xs text-[var(--accent-primary)] hover:underline font-semibold">View moderation queue</Link>
          </div>
        </div>
      </AnimatedSection>
    </div>
  );
}
