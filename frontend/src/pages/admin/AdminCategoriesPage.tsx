import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { categoryApi } from '../../api/category';
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '../../types/category';
import { ShieldClose, Tags, Search, Plus, Pencil, Trash2 } from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminCategoriesPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [form, setForm] = useState<{ id?: number; name: string; description: string }>({ name: '', description: '' });
  const [formOpen, setFormOpen] = useState(false);
  const { data: categories, isLoading } = useQuery<Category[]>({ queryKey: ['categories'], queryFn: categoryApi.list, staleTime: 60000 });
  const refresh = () => queryClient.invalidateQueries({ queryKey: ['categories'] });
  const save = useMutation({
    mutationFn: (value: typeof form) => value.id
      ? categoryApi.update(value.id, { name: value.name, description: value.description } satisfies UpdateCategoryRequest)
      : categoryApi.create({ name: value.name, description: value.description } satisfies CreateCategoryRequest),
    onSuccess: () => { setFormOpen(false); setForm({ name: '', description: '' }); refresh(); },
  });
  const remove = useMutation({ mutationFn: categoryApi.remove, onSuccess: refresh });

  if (role !== 'ADMIN') return <div className="min-h-screen pt-32 pb-24 px-6 text-center"><ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" /><p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p><Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link></div>;
  const cats = Array.isArray(categories) ? categories : [];
  const filtered = cats.filter(c => c.name?.toLowerCase().includes(search.toLowerCase()));
  return <div className="min-h-screen pt-32 pb-24 px-6"><div className="max-w-7xl mx-auto"><AnimatedSection>
    <div className="flex items-center justify-between mb-8"><div className="flex items-center gap-3"><div className="icon-well"><Tags size={22} /></div><div><h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Category Management</h1><p className="text-sm text-[var(--text-secondary)]">{cats.length} categories</p></div></div><button onClick={() => { setForm({ name: '', description: '' }); setFormOpen(true); }} className="btn-neumorphic-secondary py-2 px-4 text-sm flex items-center gap-2"><Plus size={14} /> New Category</button></div>
    <div className="relative mb-6 max-w-md"><Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" /><input type="text" placeholder="Search categories..." value={search} onChange={e => setSearch(e.target.value)} className="input-neumorphic pl-9 w-full text-sm" /></div>
    {formOpen && <form onSubmit={e => { e.preventDefault(); if (form.name.trim()) save.mutate({ ...form, name: form.name.trim() }); }} className="card-neumorphic p-5 mb-6 space-y-3"><input required placeholder="Name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} className="input-neumorphic w-full" /><textarea placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} className="input-neumorphic w-full" /><div className="flex gap-2"><button className="btn-neumorphic-primary px-4 py-2" disabled={save.isPending}>Save</button><button type="button" onClick={() => setFormOpen(false)} className="btn-neumorphic-secondary px-4 py-2">Cancel</button></div></form>}
    {isLoading ? <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">Loading...</div> : <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">{filtered.length === 0 ? <div className="col-span-full text-center py-12 text-[var(--text-tertiary)]">No categories found</div> : filtered.map((cat: Category) => <div key={cat.id} className="card-neumorphic p-5"><div className="flex justify-between"><div><p className="font-semibold text-[var(--text-primary)]">{cat.name}</p></div><div className="flex gap-2"><button aria-label="Edit category" onClick={() => { setForm({ id: cat.id, name: cat.name, description: cat.description || '' }); setFormOpen(true); }}><Pencil size={14} /></button><button aria-label="Delete category" onClick={() => window.confirm(`Delete ${cat.name}?`) && remove.mutate(cat.id)}><Trash2 size={14} /></button></div></div>{cat.description && <p className="text-xs text-[var(--text-secondary)] mt-1">{cat.description}</p>}</div>)}</div>}
  </AnimatedSection></div></div>;
}
