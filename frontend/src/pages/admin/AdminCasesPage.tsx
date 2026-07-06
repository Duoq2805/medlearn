import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { caseApi } from '../../api/case';
import {
  ShieldClose, Activity, Search, Eye, ChevronLeft, ChevronRight
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminCasesPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const pageSize = 15;

  const { data, isLoading } = useQuery({
    queryKey: ['cases', page, pageSize],
    queryFn: () => caseApi.fetchCases(page, pageSize),
    staleTime: 30000,
  });

  if (role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const cases = (data as any)?.content || [];
  const totalPages = (data as any)?.totalPages || 1;

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><Activity size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Case Study Management</h1>
              <p className="text-sm text-[var(--text-secondary)]">{data?.totalElements ?? 0} case studies</p>
            </div>
          </div>

          <div className="card-neumorphic p-4 mb-6">
            <div className="relative max-w-md">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
              <input type="text" placeholder="Search case studies..." value={search} onChange={(e) => setSearch(e.target.value)}
                className="input-neumorphic pl-9 w-full text-sm" />
            </div>
          </div>

          {isLoading ? (
            <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">Loading...</div>
          ) : (
            <div className="card-neumorphic overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-[var(--shadow-dark)]">
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Title</th>
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Updated</th>
                    <th className="text-right p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {cases.length === 0 ? (
                    <tr><td colSpan={3} className="p-8 text-center text-[var(--text-tertiary)]">No case studies found</td></tr>
                  ) : cases.map((c: any) => (
                    <tr key={c.id} className="border-b border-[var(--shadow-dark)] last:border-0 hover:bg-[var(--surface-hover)] transition-colors">
                      <td className="p-4 font-medium text-[var(--text-primary)]">{c.title}</td>
                      <td className="p-4 text-[var(--text-secondary)] text-xs">{c.updatedAt ? new Date(c.updatedAt).toLocaleDateString() : '—'}</td>
                      <td className="p-4 text-right">
                        <Link to={`/case/${c.id}`} className="btn-neumorphic-secondary p-1.5 text-xs inline-flex"><Eye size={12} /></Link>
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
