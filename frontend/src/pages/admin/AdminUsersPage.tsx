import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { adminApi } from '../../api/admin';
import {
  Users, Search, Shield, ShieldClose, Mail, Clock,
  CheckCircle2, X, ChevronLeft, ChevronRight
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminUsersPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [page, setPage] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const pageSize = 10;

  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'users', page, pageSize, searchTerm],
    queryFn: () => adminApi.fetchUsers(page, pageSize),
    staleTime: 30000,
  });

  if (role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const users = (data as any)?.content || [];
  const totalPages = (data as any)?.totalPages || 1;

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well"><Users size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">User Management</h1>
                <p className="text-sm text-[var(--text-secondary)]">{data?.totalElements ?? 0} users</p>
              </div>
            </div>
          </div>

          <div className="card-neumorphic p-4 mb-6">
            <div className="relative max-w-md">
              <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)] pointer-events-none" />
              <input type="text" placeholder="Search users..." value={searchTerm} onChange={(e) => { setSearchTerm(e.target.value); setPage(0); }}
                className="input-neumorphic !pl-11 w-full text-sm" />
            </div>
          </div>

          {isLoading ? (
            <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">Loading users...</div>
          ) : (
            <div className="card-neumorphic overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-[var(--shadow-dark)]">
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">User</th>
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Role</th>
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Status</th>
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Created</th>
                  </tr>
                </thead>
                <tbody>
                  {users.length === 0 ? (
                    <tr><td colSpan={4} className="p-8 text-center text-[var(--text-tertiary)]">No users found</td></tr>
                  ) : users.map((u: any) => (
                    <tr key={u.id} className="border-b border-[var(--shadow-dark)] last:border-0 hover:bg-[var(--surface-hover)] transition-colors">
                      <td className="p-4">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-full bg-[var(--accent-primary)]/10 flex items-center justify-center text-sm font-medium text-[var(--accent-primary)]">
                            {(u.fullName || u.username)?.[0]?.toUpperCase() || '?'}
                          </div>
                          <div>
                            <p className="font-medium text-[var(--text-primary)]">{u.fullName || u.username}</p>
                            <p className="text-xs text-[var(--text-tertiary)]">{u.email}</p>
                          </div>
                        </div>
                      </td>
                      <td className="p-4">
                        <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                          u.roles?.[0] === 'ADMIN' ? 'bg-purple-500/10 text-purple-600' :
                          u.roles?.[0] === 'REVIEWER' ? 'bg-blue-500/10 text-blue-600' :
                          'bg-gray-500/10 text-gray-600'
                        }`}>{u.roles?.[0] || 'USER'}</span>
                      </td>
                      <td className="p-4">
                        <span className={`text-xs flex items-center gap-1 ${u.status === 'ACTIVE' || u.isVerified ? 'text-emerald-600' : 'text-amber-600'}`}>
                          {u.status === 'ACTIVE' || u.isVerified ? <CheckCircle2 size={12} /> : <Clock size={12} />}
                          {u.status === 'ACTIVE' ? 'Active' : u.status || 'Pending'}
                        </span>
                      </td>
                      <td className="p-4 text-[var(--text-secondary)] text-xs">
                        {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-4 p-4 border-t border-[var(--shadow-dark)]">
                  <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
                    className="btn-neumorphic-secondary py-1.5 px-3 text-xs disabled:opacity-40 flex items-center gap-1"><ChevronLeft size={14} /> Prev</button>
                  <span className="text-xs text-[var(--text-secondary)]">Page {page + 1} of {totalPages}</span>
                  <button onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}
                    className="btn-neumorphic-secondary py-1.5 px-3 text-xs disabled:opacity-40 flex items-center gap-1">Next <ChevronRight size={14} /></button>
                </div>
              )}
            </div>
          )}
        </AnimatedSection>
      </div>
    </div>
  );
}
