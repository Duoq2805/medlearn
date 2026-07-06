import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { diseaseApi } from '../../api/disease';
import {
  ShieldClose, BookOpen, Search, Eye, Edit3, Trash2,
  ChevronLeft, ChevronRight, Globe
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminDiseasesPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const pageSize = 15;

  const { data, isLoading } = useQuery({
    queryKey: ['diseases', page, pageSize],
    queryFn: () => diseaseApi.fetchDiseases(page, pageSize),
    staleTime: 30000,
  });

  if (role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const diseases = (data as any)?.content || [];
  const totalPages = (data as any)?.totalPages || 1;

  const statusColor = (s: string) =>
    s === 'PUBLISHED' ? 'text-emerald-600 bg-emerald-500/10' :
    s === 'PENDING' ? 'text-amber-600 bg-amber-500/10' :
    s === 'DRAFT' ? 'text-blue-600 bg-blue-500/10' :
    'text-red-600 bg-red-500/10';

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><BookOpen size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Disease Management</h1>
              <p className="text-sm text-[var(--text-secondary)]">{data?.totalElements ?? 0} diseases</p>
            </div>
          </div>

          <div className="card-neumorphic p-4 mb-6">
            <div className="relative max-w-md">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
              <input type="text" placeholder="Search diseases..." value={search} onChange={(e) => setSearch(e.target.value)}
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
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Disease</th>
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Category</th>
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Status</th>
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Updated</th>
                    <th className="text-right p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {diseases.length === 0 ? (
                    <tr><td colSpan={5} className="p-8 text-center text-[var(--text-tertiary)]">No diseases found</td></tr>
                  ) : diseases.map((d: any) => (
                    <tr key={d.id} className="border-b border-[var(--shadow-dark)] last:border-0 hover:bg-[var(--surface-hover)] transition-colors">
                      <td className="p-4 font-medium text-[var(--text-primary)]">{d.name}</td>
                      <td className="p-4 text-[var(--text-secondary)]">{d.category || '—'}</td>
                      <td className="p-4"><span className={`text-xs px-2 py-1 rounded-full font-medium ${statusColor(d.status || 'DRAFT')}`}>{d.status || 'Draft'}</span></td>
                      <td className="p-4 text-[var(--text-secondary)] text-xs">{d.updatedAt ? new Date(d.updatedAt).toLocaleDateString() : '—'}</td>
                      <td className="p-4 text-right">
                        <div className="flex items-center justify-end gap-1">
                          <Link to={`/disease/${d.id}`} className="btn-neumorphic-secondary p-1.5 text-xs"><Eye size={12} /></Link>
                          {d.status !== 'PUBLISHED' && (
                            <>
                              <Link to={`/disease/${d.id}/edit`} className="btn-neumorphic-secondary p-1.5 text-xs"><Edit3 size={12} /></Link>
                              {/* delete: diseaseApi.deleteDisease exists but needs confirmation */}
                            </>
                          )}
                        </div>
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
