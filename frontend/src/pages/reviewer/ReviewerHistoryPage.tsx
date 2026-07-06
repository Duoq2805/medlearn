import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  ShieldClose, Clock, CheckCircle2, XCircle, MessageSquare,
  Eye, Search, Filter, ChevronRight, AlertCircle, ThumbsUp, ThumbsDown
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function ReviewerHistoryPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [search, setSearch] = useState('');
  const [decisionFilter, setDecisionFilter] = useState('All');

  if (role !== 'REVIEWER' && role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Reviewer access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const history = [
    { id: 1, title: 'Pneumonia', author: 'Dr. Smith', decision: 'Approved', comment: 'Well-structured, accurate. Approved.', time: '30m ago', version: 2 },
    { id: 2, title: 'Hypertension', author: 'Dr. Park', decision: 'Changes Requested', comment: 'Please expand differential diagnosis.', time: '1h ago', version: 3 },
    { id: 3, title: 'Diabetes Type 2', author: 'Dr. Jones', decision: 'Approved', comment: 'Excellent references. Approved.', time: '2h ago', version: 1 },
    { id: 4, title: 'Asthma', author: 'Dr. Lee', decision: 'Approved', comment: 'Minor corrections made. Approved.', time: '3h ago', version: 2 },
    { id: 5, title: 'Migraine Protocol', author: 'Dr. Rivera', decision: 'Rejected', comment: 'Insufficient references. Rejected.', time: '4h ago', version: 1 },
    { id: 6, title: 'COPD Management', author: 'Dr. Chen', decision: 'Approved', comment: 'Comprehensive. Approved.', time: '1d ago', version: 4 },
    { id: 7, title: 'Heart Failure', author: 'Dr. Wilson', decision: 'Changes Requested', comment: 'Update to latest ESC guidelines.', time: '2d ago', version: 2 },
  ];

  const filtered = history.filter(h => {
    const match = h.title.toLowerCase().includes(search.toLowerCase()) || h.author.toLowerCase().includes(search.toLowerCase());
    return decisionFilter === 'All' ? match : match && h.decision === decisionFilter;
  });

  const decisionColor = (d: string) =>
    d === 'Approved' ? 'text-emerald-600 bg-emerald-500/10' :
    d === 'Rejected' ? 'text-red-600 bg-red-500/10' :
    'text-amber-600 bg-amber-500/10';

  const decisionIcon = (d: string) =>
    d === 'Approved' ? CheckCircle2 : d === 'Rejected' ? ThumbsDown : AlertCircle;

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><Clock size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Review History</h1>
              <p className="text-sm text-[var(--text-secondary)]">{history.length} reviews performed</p>
            </div>
          </div>

          <div className="card-neumorphic p-4 mb-6 flex flex-wrap items-center gap-4">
            <div className="relative flex-1 min-w-[200px]">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
              <input type="text" placeholder="Search title or author..." value={search} onChange={(e) => setSearch(e.target.value)} className="input-neumorphic pl-9 w-full text-sm" />
            </div>
            <select value={decisionFilter} onChange={(e) => setDecisionFilter(e.target.value)} className="input-neumorphic py-2 px-3 text-sm">
              <option value="All">All Decisions</option>
              <option value="Approved">Approved</option>
              <option value="Changes Requested">Changes Requested</option>
              <option value="Rejected">Rejected</option>
            </select>
          </div>

          <div className="space-y-3">
            {filtered.map((h) => {
              const Icon = decisionIcon(h.decision);
              return (
                <div key={h.id} className="card-neumorphic p-5 flex items-center justify-between hover:shadow-md transition-all">
                  <div className="flex items-center gap-4 flex-1">
                    <Icon size={20} className={`${h.decision === 'Approved' ? 'text-emerald-500' : h.decision === 'Rejected' ? 'text-red-500' : 'text-amber-500'}`} />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <p className="text-sm font-semibold text-[var(--text-primary)]">{h.title}</p>
                        <span className="text-[10px] text-[var(--text-tertiary)]">v{h.version}</span>
                      </div>
                      <p className="text-xs text-[var(--text-secondary)]">by {h.author}</p>
                      <p className="text-xs text-[var(--text-tertiary)] mt-0.5">"{h.comment}"</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${decisionColor(h.decision)}`}>{h.decision}</span>
                    <span className="text-xs text-[var(--text-tertiary)]">{h.time}</span>
                    <Link to={`/reviewer/review/${h.id}`} className="btn-neumorphic-secondary p-1.5 text-xs"><Eye size={12} /></Link>
                  </div>
                </div>
              );
            })}
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
