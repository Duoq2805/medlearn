import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { symptomApi } from '../../api/symptom';
import {
  ShieldClose, Pill, Search, Plus, Edit3, Trash2,
  ChevronLeft, ChevronRight, CheckCircle2, Clock
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminSymptomsPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [search, setSearch] = useState('');

  const { data: symptoms, isLoading } = useQuery({
    queryKey: ['symptoms'],
    queryFn: () => symptomApi.fetchSymptoms(),
    staleTime: 60000,
  });

  if (role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const syms = Array.isArray(symptoms) ? symptoms : [];
  const filtered = syms.filter((s: any) => s.name?.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well"><Pill size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Symptom Management</h1>
                <p className="text-sm text-[var(--text-secondary)]">{syms.length} symptoms</p>
              </div>
            </div>
            {/* createSymptom API exists, but needs modal/form — UI shell */}
            <button className="btn-neumorphic-secondary py-2 px-4 text-sm flex items-center gap-2 opacity-60" disabled><Plus size={14} /> New Symptom (UI)</button>
          </div>

          <div className="relative mb-6 max-w-md">
            <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)] pointer-events-none" />
            <input type="text" placeholder="Search symptoms..." value={search} onChange={(e) => setSearch(e.target.value)}
              className="input-neumorphic !pl-11 w-full text-sm" />
          </div>

          {isLoading ? (
            <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">Loading...</div>
          ) : (
            <div className="card-neumorphic overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-[var(--shadow-dark)]">
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Symptom</th>
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Description</th>
                    <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Created</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.length === 0 ? (
                    <tr><td colSpan={3} className="p-8 text-center text-[var(--text-tertiary)]">No symptoms found</td></tr>
                  ) : filtered.map((s: any) => (
                    <tr key={s.id} className="border-b border-[var(--shadow-dark)] last:border-0 hover:bg-[var(--surface-hover)] transition-colors">
                      <td className="p-4 font-medium text-[var(--text-primary)]">{s.name}</td>
                      <td className="p-4 text-[var(--text-secondary)] text-xs">{s.description || '—'}</td>
                      <td className="p-4 text-[var(--text-secondary)] text-xs">{s.createdAt ? new Date(s.createdAt).toLocaleDateString() : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </AnimatedSection>
      </div>
    </div>
  );
}
