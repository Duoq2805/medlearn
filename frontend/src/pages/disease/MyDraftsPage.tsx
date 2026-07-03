import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  BookOpen, ChevronRight, Clock, CheckCircle2, AlertCircle, Plus, FileText, Save, Eye, Send, Trash2, Search
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { getPermissions } from '../../hooks/usePermissions';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function MyDraftsPage() {
  const { user } = useAuth();
  const perm = user ? getPermissions(user.roles?.[0]) : null;
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('All');

  const mockDrafts = [
    { id: '1', title: 'Pneumonia', status: 'Draft', lastEdited: '2h ago', version: 3, comments: 'Needs more info on prevention.' },
    { id: '2', title: 'Hypertension', status: 'Pending Review', lastEdited: '1 day ago', version: 2, comments: null },
    { id: '3', title: 'Diabetes Type 2', status: 'Needs Changes', lastEdited: '3 days ago', version: 4, comments: 'Please clarify treatment dosages.' },
  ];

  const filteredDrafts = mockDrafts.filter(draft => (
    (filterStatus === 'All' || draft.status === filterStatus) &&
    draft.title.toLowerCase().includes(searchTerm.toLowerCase())
  ));

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well"><BookOpen size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">My Drafts</h1>
                <p className="text-sm text-[var(--text-secondary)]">Manage your disease drafts for review</p>
              </div>
            </div>
            <Link to="/diseases/new" className="btn-neumorphic-primary py-3 px-6 flex items-center gap-2">
              <Plus size={18} /> New Draft
            </Link>
          </div>

          <div className="mb-6">
            <div className="relative">
              <input
                type="text"
                placeholder="Search drafts..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input-neumorphic w-full pl-10 py-3"
              />
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
            </div>
          </div>
          <div className="flex flex-wrap gap-2 mb-6">
            {['All', 'Draft', 'Pending Review', 'Needs Changes'].map(status => (
              <button
                key={status}
                onClick={() => setFilterStatus(status)}
                className={`py-2 px-4 rounded-lg text-sm font-medium transition-all ${
                  filterStatus === status
                    ? 'bg-[var(--accent-primary)] text-white'
                    : 'bg-[var(--surface-primary)] shadow-[inset_3px_3px_6px_var(--shadow-dark)] text-[var(--text-primary)]'
                }`}
              >
                {status}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredDrafts.length === 0 ? (
              <div className="card-neumorphic p-12 text-center col-span-full">
                <BookOpen size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
                <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No drafts yet</p>
                <p className="text-sm text-[var(--text-secondary)] mb-6">Create a new disease draft to start documenting.</p>
                <Link to="/diseases/new" className="btn-neumorphic-primary py-3 px-6 inline-flex items-center gap-2">
                  Create New Draft <ChevronRight size={16} />
                </Link>
              </div>
            ) : (
              filteredDrafts.map((draft) => (
                <div key={draft.id} className="card-neumorphic p-6 hover:shadow-lg transition-all flex flex-col">
                  <div className="flex items-start justify-between mb-3">
                    <h3 className="font-display text-lg font-bold text-[var(--text-primary)]">{draft.title}</h3>
                    <span className={`text-xs font-semibold px-2 py-1 rounded-full ${
                      draft.status === 'Draft' ? 'bg-blue-500/10 text-blue-600' :
                      draft.status === 'Pending Review' ? 'bg-amber-500/10 text-amber-600' :
                      'bg-red-500/10 text-red-600'
                    }`}>{draft.status}</span>
                  </div>
                  <p className="text-xs text-[var(--text-secondary)] mb-4">Last edited: {draft.lastEdited} • v{draft.version}</p>
                  {draft.status === 'Needs Changes' && (
                    <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/20">
                      <p className="text-xs text-red-600 flex items-center gap-1"><AlertCircle size={14} /> {draft.comments}</p>
                    </div>
                  )}
                  <div className="mt-auto pt-3 border-t border-[var(--shadow-dark)] flex gap-2">
                    <Link to={`/disease/${draft.id}/edit`} className="btn-neumorphic-secondary py-2 px-3 text-sm flex-1 flex items-center justify-center gap-1">
                      <FileText size={14} /> Edit
                    </Link>
                    <button className="btn-neumorphic-secondary py-2 px-3 text-sm">
                      <Eye size={14} />
                    </button>
                    {draft.status === 'Draft' && (
                      <button className="btn-neumorphic-primary py-2 px-3 text-sm flex items-center gap-1">
                        <Send size={14} /> Submit
                      </button>
                    )}
                    <button className="btn-neumorphic-secondary py-2 px-3 text-sm text-red-600 hover:bg-red-500/10">
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
