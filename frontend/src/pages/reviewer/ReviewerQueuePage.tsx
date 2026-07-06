import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  ShieldClose, FolderKanban, Search, Filter, Eye,
  ChevronRight, Clock, User, FileText, AlertCircle,
  CheckCircle2, X, Check
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function ReviewerQueuePage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('All');
  const [statusFilter, setStatusFilter] = useState('All');

  if (role !== 'REVIEWER' && role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Reviewer access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const queue = [
    { id: 1, title: 'Rheumatoid Arthritis', author: 'Dr. Chen', cat: 'Autoimmune', ver: 2, priority: 'High', submitted: '2h ago', status: 'Pending', reviewer: '-' },
    { id: 2, title: 'Chronic Kidney Disease', author: 'Dr. Wilson', cat: 'Nephrology', ver: 1, priority: 'Medium', submitted: '4h ago', status: 'In Review', reviewer: 'You' },
    { id: 3, title: 'Pediatric Asthma Protocol', author: 'Dr. Park', cat: 'Respiratory', ver: 3, priority: 'High', submitted: '6h ago', status: 'Pending', reviewer: '-' },
    { id: 4, title: 'Post-COVID Syndrome', author: 'Dr. Rivera', cat: 'Infectious', ver: 1, priority: 'Low', submitted: '8h ago', status: 'Needs Revision', reviewer: 'You' },
    { id: 5, title: 'Migraine Prophylaxis', author: 'Dr. Chen', cat: 'Neurology', ver: 2, priority: 'Medium', submitted: '12h ago', status: 'Pending', reviewer: '-' },
    { id: 6, title: 'COPD Management', author: 'Dr. Lee', cat: 'Respiratory', ver: 1, priority: 'High', submitted: '1d ago', status: 'Pending', reviewer: '-' },
    { id: 7, title: 'Heart Failure Guidelines', author: 'Dr. Smith', cat: 'Cardiovascular', ver: 4, priority: 'Medium', submitted: '2d ago', status: 'Approved', reviewer: 'Dr. Wilson' },
    { id: 8, title: 'UTI Treatment Protocol', author: 'Dr. Jones', cat: 'Infectious', ver: 1, priority: 'Low', submitted: '3d ago', status: 'Rejected', reviewer: 'Dr. Rivera' },
  ];

  const filtered = queue.filter(item => {
    const match = item.title.toLowerCase().includes(search.toLowerCase()) || item.author.toLowerCase().includes(search.toLowerCase());
    const typeOk = typeFilter === 'All' || item.cat === typeFilter;
    const statusOk = statusFilter === 'All' || item.status === statusFilter;
    return match && typeOk && statusOk;
  });

  const statusColor = (s: string) =>
    s === 'Approved' ? 'text-emerald-600 bg-emerald-500/10' :
    s === 'Rejected' ? 'text-red-600 bg-red-500/10' :
    s === 'In Review' ? 'text-blue-600 bg-blue-500/10' :
    s === 'Needs Revision' ? 'text-amber-600 bg-amber-500/10' :
    'text-gray-600 bg-gray-500/10';

  const priorityColor = (p: string) =>
    p === 'High' ? 'text-red-600' : p === 'Medium' ? 'text-amber-600' : 'text-blue-600';

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><FolderKanban size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Review Queue</h1>
              <p className="text-sm text-[var(--text-secondary)]">{queue.length} items</p>
            </div>
          </div>

          <div className="card-neumorphic p-4 mb-6 flex flex-wrap items-center gap-4">
            <div className="relative flex-1 min-w-[200px]">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
              <input type="text" placeholder="Search title or author..." value={search} onChange={(e) => setSearch(e.target.value)} className="input-neumorphic pl-9 w-full text-sm" />
            </div>
            <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)} className="input-neumorphic py-2 px-3 text-sm">
              <option value="All">All Categories</option>
              <option value="Autoimmune">Autoimmune</option>
              <option value="Nephrology">Nephrology</option>
              <option value="Respiratory">Respiratory</option>
              <option value="Infectious">Infectious</option>
              <option value="Neurology">Neurology</option>
              <option value="Cardiovascular">Cardiovascular</option>
            </select>
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="input-neumorphic py-2 px-3 text-sm">
              <option value="All">All Status</option>
              <option value="Pending">Pending</option>
              <option value="In Review">In Review</option>
              <option value="Needs Revision">Needs Revision</option>
              <option value="Approved">Approved</option>
              <option value="Rejected">Rejected</option>
            </select>
          </div>

          <div className="card-neumorphic overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-[var(--shadow-dark)]">
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Disease</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Author</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Category</th>
                  <th className="text-center p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Ver</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Priority</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Submitted</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Status</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Reviewer</th>
                  <th className="text-right p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((item) => (
                  <tr key={item.id} className="border-b border-[var(--shadow-dark)] last:border-0 hover:bg-[var(--surface-hover)] transition-colors">
                    <td className="p-4 font-medium text-[var(--text-primary)]">{item.title}</td>
                    <td className="p-4 text-[var(--text-secondary)]">{item.author}</td>
                    <td className="p-4 text-[var(--text-secondary)]">{item.cat}</td>
                    <td className="p-4 text-center text-[var(--text-secondary)]">v{item.ver}</td>
                    <td className="p-4"><span className={`text-xs font-semibold ${priorityColor(item.priority)}`}>{item.priority}</span></td>
                    <td className="p-4 text-[var(--text-secondary)]">{item.submitted}</td>
                    <td className="p-4"><span className={`text-xs px-2 py-0.5 rounded-full font-medium ${statusColor(item.status)}`}>{item.status}</span></td>
                    <td className="p-4 text-[var(--text-secondary)]">{item.reviewer}</td>
                    <td className="p-4 text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Link to={`/reviewer/review/${item.id}`} className="btn-neumorphic-secondary p-1.5 text-xs"><Eye size={12} /></Link>
                        <button className="btn-neumorphic-secondary p-1.5 text-xs text-emerald-600"><Check size={12} /></button>
                        <button className="btn-neumorphic-secondary p-1.5 text-xs text-red-600"><X size={12} /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
