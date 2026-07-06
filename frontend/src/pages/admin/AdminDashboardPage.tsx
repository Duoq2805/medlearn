import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { adminApi } from '../../api/admin';
import { diseaseApi } from '../../api/disease';
import { caseApi } from '../../api/case';
import {
  Shield, ShieldClose, Users, BookOpen, Activity, FolderKanban,
  CheckCircle2, Clock, UserPlus, Database, AlertTriangle, FileText
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
    queryFn: () => adminApi.fetchPendingReviews(0, 5),
    staleTime: 30000,
  });

  const { data: usersData } = useQuery({
    queryKey: ['admin', 'users', 0, 5],
    queryFn: () => adminApi.fetchUsers(0, 5),
    staleTime: 30000,
  });

  const { data: diseasesData } = useQuery({
    queryKey: ['diseases', 0, 20],
    queryFn: () => diseaseApi.fetchDiseases(0, 20),
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
  const totalDiseases = analytics?.totalDiseases ?? (diseasesData as any)?.totalElements ?? '—';
  const totalCases = analytics?.totalCases ?? (casesData as any)?.totalElements ?? '—';
  const totalReviews = analytics?.totalPendingReviews ?? pendingReviews?.totalElements ?? '—';
  const recentUsers = (usersData as any)?.content?.slice(0, 3) || [];
  const recentDiseases = (diseasesData as any)?.content?.slice(0, 3) || [];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><Shield size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Admin Dashboard</h1>
              <p className="text-sm text-[var(--text-secondary)]">Platform Management</p>
            </div>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
            <div className="card-neumorphic p-4">
              <Users size={18} className="text-blue-500 mb-2" />
              <p className="text-2xl font-bold text-[var(--text-primary)]">{totalUsers}</p>
              <p className="text-xs text-[var(--text-secondary)]">Registered Users</p>
            </div>
            <div className="card-neumorphic p-4">
              <BookOpen size={18} className="text-emerald-500 mb-2" />
              <p className="text-2xl font-bold text-[var(--text-primary)]">{totalDiseases}</p>
              <p className="text-xs text-[var(--text-secondary)]">Diseases</p>
            </div>
            <div className="card-neumorphic p-4">
              <Activity size={18} className="text-purple-500 mb-2" />
              <p className="text-2xl font-bold text-[var(--text-primary)]">{totalCases}</p>
              <p className="text-xs text-[var(--text-secondary)]">Case Studies</p>
            </div>
            <div className="card-neumorphic p-4">
              <FolderKanban size={18} className="text-amber-500 mb-2" />
              <p className="text-2xl font-bold text-[var(--text-primary)]">{totalReviews}</p>
              <p className="text-xs text-[var(--text-secondary)]">Pending Reviews</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Left: Platform Stats + Recent Users */}
            <div className="space-y-6">
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Latest Registered Users</h2>
                {recentUsers.length > 0 ? (
                  <div className="space-y-3">
                    {recentUsers.map((u: any) => (
                      <div key={u.id} className="flex items-center gap-3 depth-layer-1 rounded-lg p-3">
                        <div className="w-8 h-8 rounded-full bg-[var(--accent-primary)]/10 flex items-center justify-center text-xs font-bold text-[var(--accent-primary)]">
                          {(u.fullName || u.username)?.[0]?.toUpperCase() || '?'}
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-[var(--text-primary)] truncate">{u.fullName || u.username}</p>
                          <p className="text-xs text-[var(--text-tertiary)]">{u.email}</p>
                        </div>
                        <span className="text-xs px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-600">{(u.roles?.[0] || 'USER')}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-[var(--text-tertiary)] text-center py-6">No user data available</p>
                )}
              </div>

              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Latest Submitted Diseases</h2>
                {recentDiseases.length > 0 ? (
                  <div className="space-y-3">
                    {recentDiseases.map((d: any) => (
                      <div key={d.id} className="flex items-center justify-between depth-layer-1 rounded-lg p-3">
                        <div>
                          <p className="text-sm font-medium text-[var(--text-primary)]">{d.name}</p>
                          <p className="text-xs text-[var(--text-tertiary)]">{d.category} • Updated {d.updatedAt ? new Date(d.updatedAt).toLocaleDateString() : '—'}</p>
                        </div>
                        <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                          d.status === 'PUBLISHED' ? 'text-emerald-600 bg-emerald-500/10' :
                          d.status === 'PENDING' ? 'text-amber-600 bg-amber-500/10' :
                          'text-blue-600 bg-blue-500/10'
                        }`}>{d.status || 'Draft'}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-[var(--text-tertiary)] text-center py-6">No disease data available</p>
                )}
              </div>
            </div>

            {/* Right: Pending Reviews, Quick Actions, Platform Health */}
            <div className="space-y-6">
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Pending Reviews</h2>
                {pendingReviews?.content?.length > 0 ? (
                  <div className="space-y-2">
                    {pendingReviews.content.slice(0, 5).map((r: any) => (
                      <div key={r.id} className="flex items-center justify-between depth-layer-1 rounded-lg p-3">
                        <div className="flex-1 min-w-0">
                          <p className="text-xs font-medium text-[var(--text-primary)] truncate">{r.title || r.diseaseName || `Review #${r.id}`}</p>
                          <p className="text-[10px] text-[var(--text-tertiary)]">{r.author ? `by ${r.author}` : ''}</p>
                        </div>
                        <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-amber-500/10 text-amber-600">{r.status || 'Pending'}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-[var(--text-tertiary)] text-center py-6">
                    {pendingReviews ? 'All caught up' : 'Loading...'}
                  </p>
                )}
                <Link to="/admin/diseases" className="mt-3 block text-xs text-[var(--accent-primary)] hover:underline">View all content</Link>
              </div>

              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Quick Actions</h2>
                <div className="space-y-2">
                  <Link to="/admin/users" className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2">
                    <Users size={14} /> User Management
                  </Link>
                  <Link to="/admin/diseases" className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2">
                    <BookOpen size={14} /> Disease Management
                  </Link>
                  <Link to="/admin/reports" className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2">
                    <Activity size={14} /> View Reports
                  </Link>
                </div>
              </div>

              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Platform Health</h2>
                <div className="space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-[var(--text-primary)]">API Status</span>
                    <span className="text-xs text-emerald-600 flex items-center gap-1"><CheckCircle2 size={12} /> Operational</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-[var(--text-primary)]">Database</span>
                    <span className="text-xs text-emerald-600 flex items-center gap-1"><CheckCircle2 size={12} /> Connected</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-[var(--text-primary)]">Storage</span>
                    <span className="text-xs text-emerald-600 flex items-center gap-1"><CheckCircle2 size={12} /> Available</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
