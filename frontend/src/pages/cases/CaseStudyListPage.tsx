import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Target, Plus, Bookmark, Clock, GraduationCap,
  ChevronRight, Search, Filter, AlertCircle, Brain,
  CheckCircle2, BookOpen
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';
import Skeleton from '../../components/ui/Skeleton';

export default function CaseStudyListPage() {
  const { user } = useAuth();
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('All');
  const [isLoading] = useState(false);

  const mockCases = [
    { id: '1', title: 'Respiratory Infection', specialty: 'Pulmonology', difficulty: 'Medium', status: 'In Progress', progress: 45, lastViewed: '2h ago' },
    { id: '2', title: 'Acute Coronary Syndrome', specialty: 'Cardiology', difficulty: 'Hard', status: 'Completed', progress: 100, lastViewed: '3 days ago' },
    { id: '3', title: 'Diabetic Ketoacidosis', specialty: 'Endocrinology', difficulty: 'Medium', status: 'Saved', progress: 60, lastViewed: '1h ago' },
    { id: '4', title: 'Septic Shock', specialty: 'Critical Care', difficulty: 'Hard', status: 'Not Started', progress: 0, lastViewed: 'Never' },
  ];

  const filtered = mockCases.filter(c =>
    (filterStatus === 'All' || c.status === filterStatus) &&
    c.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well"><Target size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Clinical Cases</h1>
                <p className="text-sm text-[var(--text-secondary)]">Learn through real-world case studies</p>
              </div>
            </div>
            <Link to="/cases" className="btn-neumorphic-primary py-2 px-4 flex items-center gap-2 text-sm">
              <Plus size={14} /> New Case
            </Link>
          </div>

          {/* Toolbar */}
          <div className="card-neumorphic p-6 mb-6 flex flex-wrap items-center gap-4">
            <div className="relative flex-1 min-w-[200px]">
              <input
                type="text"
                placeholder="Search cases..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input-neumorphic w-full !pl-11 py-2 text-sm"
              />
              <Search size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)] pointer-events-none" />
            </div>
            <div className="flex gap-2">
              {['All', 'In Progress', 'Completed', 'Saved'].map((status) => (
                <button
                  key={status}
                  onClick={() => setFilterStatus(status)}
                  className={`py-2 px-3 text-sm rounded-lg transition-all ${
                    filterStatus === status
                      ? 'bg-[var(--accent-primary)] text-white'
                      : 'bg-[var(--surface-primary)] shadow-[inset_3px_3px_6px_var(--shadow-dark)] text-[var(--text-primary)]'
                  }`}
                >
                  {status}
                </button>
              ))}
            </div>
          </div>

          {/* Cases Grid */}
          {isLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="card-neumorphic p-6">
                  <Skeleton className="h-5 w-3/4 mb-4" />
                  <Skeleton className="h-4 w-1/2 mb-2" />
                  <Skeleton className="h-4 w-full mb-4" />
                  <Skeleton className="h-8 w-24" />
                </div>
              ))}
            </div>
          ) : filtered.length === 0 ? (
            <div className="card-neumorphic p-12 text-center max-w-md mx-auto">
              <AlertCircle size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
              <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No cases found</p>
              <p className="text-sm text-[var(--text-secondary)] mb-6">Try adjusting your search or filter.</p>
              <button onClick={() => { setSearchTerm(''); setFilterStatus('All'); }} className="btn-neumorphic-primary py-3 px-8 text-sm">Clear Filters</button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filtered.map((caseStudy) => (
                <div key={caseStudy.id} className="card-neumorphic p-6 hover:shadow-lg transition-all flex flex-col">
                  <div className="flex items-start justify-between mb-3">
                    <h3 className="font-display text-lg font-bold text-[var(--text-primary)]">{caseStudy.title}</h3>
                    <Bookmark size={16} className="text-[var(--text-tertiary)] hover:text-[var(--accent-primary)]" />
                  </div>
                  <p className="text-xs text-[var(--text-secondary)] mb-3">{caseStudy.specialty}</p>
                  <div className="flex items-center gap-2 text-xs text-[var(--text-tertiary)] mb-3">
                    <GraduationCap size={12} /> {caseStudy.difficulty}
                    <span className="ml-auto">{caseStudy.status}</span>
                  </div>
                  <div className="flex items-center gap-2 mb-4">
                    <div className="flex-1 h-2 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_4px_var(--shadow-dark)] overflow-hidden">
                      <div className="h-full bg-[var(--accent-primary)] rounded-full" style={{ width: `${caseStudy.progress}%` }} />
                    </div>
                    <span className="text-xs font-semibold">{caseStudy.progress}%</span>
                  </div>
                  <div className="mt-auto pt-3 border-t border-[var(--shadow-dark)]">
                    <p className="text-[10px] text-[var(--text-tertiary)] mb-2">Last viewed: {caseStudy.lastViewed}</p>
                    <Link to={`/case/${caseStudy.id}`} className="btn-neumorphic-primary py-2 px-4 text-sm w-full text-center flex items-center justify-center gap-1">
                      {caseStudy.status === 'Completed' ? 'Review' : 'Continue'} <ChevronRight size={14} />
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </AnimatedSection>
      </div>
    </div>
  );
}
