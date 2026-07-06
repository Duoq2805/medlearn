import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { diseaseApi } from '../../api/disease';
import { ShieldClose, Tags, Search, Plus } from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminCategoriesPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [search, setSearch] = useState('');

  const { data: categories, isLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: () => diseaseApi.getCategories(),
    staleTime: 60000,
  });

  if (role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const cats = Array.isArray(categories) ? categories : [];
  const filtered = cats.filter((c: any) => c.name?.toLowerCase().includes(search.toLowerCase()) || c.slug?.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well"><Tags size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Category Management</h1>
                <p className="text-sm text-[var(--text-secondary)]">{cats.length} categories</p>
              </div>
            </div>
            {/* Create: diseaseApi.createDisease is for diseases; categories use separate endpoint if available — UI shell */}
            <button className="btn-neumorphic-secondary py-2 px-4 text-sm flex items-center gap-2 opacity-60" disabled><Plus size={14} /> New Category (API)</button>
          </div>

          <div className="relative mb-6 max-w-md">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
            <input type="text" placeholder="Search categories..." value={search} onChange={(e) => setSearch(e.target.value)}
              className="input-neumorphic pl-9 w-full text-sm" />
          </div>

          {isLoading ? (
            <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">Loading...</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {filtered.length === 0 ? (
                <div className="col-span-full text-center py-12 text-[var(--text-tertiary)]">No categories found</div>
              ) : filtered.map((cat: any) => (
                <div key={cat.id || cat.slug} className="card-neumorphic p-5">
                  <p className="font-semibold text-[var(--text-primary)]">{cat.name}</p>
                  <p className="text-xs text-[var(--text-tertiary)]">/{cat.slug}</p>
                  {cat.description && <p className="text-xs text-[var(--text-secondary)] mt-1">{cat.description}</p>}
                </div>
              ))}
            </div>
          )}
        </AnimatedSection>
      </div>
    </div>
  );
}
