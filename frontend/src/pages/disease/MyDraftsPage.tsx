import { useState } from 'react';
import { Link } from 'react-router-dom';
import { BookOpen, ChevronRight, Plus, Trash2, Eye, Send, Clock, FileText, AlertTriangle } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';
import DraftCreationFlow from '../../components/disease/DraftCreationFlow';

export default function MyDraftsPage() {
  const { user } = useAuth();
  const [flowOpen, setFlowOpen] = useState(false);

  const mockDrafts = [
    { id: '1', title: 'Pneumonia', status: 'Draft', lastEdited: '2h ago', version: 3, reviewerComments: null },
    { id: '2', title: 'Hypertension', status: 'Pending Review', lastEdited: '1 day ago', version: 2, reviewerComments: null },
    { id: '3', title: 'Diabetes Type 2', status: 'Needs Changes', lastEdited: '3 days ago', version: 4, reviewerComments: 'Please add more details on lifestyle factors and reference the latest guidelines.' },
  ];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well"><BookOpen size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">My Drafts</h1>
                <p className="text-sm text-[var(--text-secondary)]">Manage your disease drafts</p>
              </div>
            </div>
            {user && (
              <button onClick={() => setFlowOpen(true)} className="btn-neumorphic-primary py-3 px-6 flex items-center gap-2">
                <Plus size={18} /> New Draft
              </button>
            )}
          </div>

          <DraftCreationFlow open={flowOpen} onClose={() => setFlowOpen(false)} />

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {mockDrafts.map((draft) => (
              <div key={draft.id} className="card-neumorphic p-6 hover:shadow-lg transition-all flex flex-col">
                <div className="flex items-start justify-between mb-3">
                  <h3 className="font-display text-lg font-bold text-[var(--text-primary)]">{draft.title}</h3>
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                    draft.status === 'Draft' ? 'bg-blue-500/10 text-blue-600' :
                    draft.status === 'Pending Review' ? 'bg-amber-500/10 text-amber-600' :
                    'bg-red-500/10 text-red-600'
                  }`}>
                    {draft.status}
                  </span>
                </div>
                <p className="text-xs text-[var(--text-secondary)] mb-4">
                  Last edited: {draft.lastEdited} • v{draft.version}
                </p>
                {draft.status === 'Needs Changes' && draft.reviewerComments && (
                  <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/20">
                    <div className="flex items-center gap-2 mb-1">
                      <AlertTriangle size={14} className="text-red-600" />
                      <p className="text-sm font-semibold text-red-600">Reviewer Comments</p>
                    </div>
                    <p className="text-xs text-red-600">{draft.reviewerComments}</p>
                  </div>
                )}
                <div className="mt-auto pt-3 border-t border-[var(--shadow-dark)] flex gap-2">
                  <Link to={`/disease/${draft.id}/edit`} className="btn-neumorphic-secondary py-2 px-3 text-sm flex-1 flex items-center justify-center gap-1">
                    Edit <ChevronRight size={14} />
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
            ))}
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
