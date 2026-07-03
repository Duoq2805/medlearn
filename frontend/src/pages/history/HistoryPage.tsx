import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Clock, BookOpen, Target, Sparkles, ClipboardCheck,
  Search, Filter, ChevronRight, AlertCircle
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function HistoryPage() {
  const { user } = useAuth();
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('All');

  const mockHistory = [
    { id: '1', type: 'disease', title: 'Pneumonia', action: 'Viewed', time: '2h ago', date: 'Today' },
    { id: '2', type: 'case', title: 'Respiratory Infection Case', action: 'Started', time: '4h ago', date: 'Today' },
    { id: '3', type: 'quiz', title: 'Pneumonia Quiz', action: 'Completed', time: '6h ago', date: 'Today' },
    { id: '4', type: 'disease', title: 'Hypertension', action: 'Bookmarked', time: '1 day ago', date: 'Yesterday' },
    { id: '5', type: 'search', title: 'Cough + Fever', action: 'Searched', time: '2 days ago', date: '2 days ago' },
    { id: '6', type: 'case', title: 'Acute Coronary Syndrome', action: 'Completed', time: '3 days ago', date: '3 days ago' },
  ];

  const types = [
    { id: 'All', label: 'All Activity' },
    { id: 'disease', label: 'Diseases' },
    { id: 'case', label: 'Cases' },
    { id: 'quiz', label: 'Quizzes' },
    { id: 'search', label: 'Searches' },
  ];

  const filtered = mockHistory.filter(item =>
    (filterType === 'All' || item.type === filterType) &&
    item.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const groupedByDate = filtered.reduce((acc: any, item) => {
    if (!acc[item.date]) acc[item.date] = [];
    acc[item.date].push(item);
    return acc;
  }, {});

  const getIcon = (type: string) => {
    switch (type) {
      case 'disease': return BookOpen;
      case 'case': return Target;
      case 'quiz': return ClipboardCheck;
      case 'search': return Search;
      default: return Clock;
    }
  };

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-4xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><Clock size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">History</h1>
              <p className="text-sm text-[var(--text-secondary)]">Your learning activity timeline</p>
            </div>
          </div>

          {/* Filters */}
          <div className="mb-8 space-y-4">
            <div className="relative">
              <input
                type="text"
                placeholder="Search history..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input-neumorphic w-full pl-10 py-3"
              />
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
            </div>
            <div className="flex flex-wrap gap-2">
              {types.map((type) => (
                <button
                  key={type.id}
                  onClick={() => setFilterType(type.id)}
                  className={`py-2 px-4 rounded-lg text-sm font-medium transition-all ${
                    filterType === type.id
                      ? 'bg-[var(--accent-primary)] text-white'
                      : 'bg-[var(--surface-primary)] shadow-[inset_3px_3px_6px_var(--shadow-dark)] text-[var(--text-primary)]'
                  }`}
                >
                  {type.label}
                </button>
              ))}
            </div>
          </div>

          {/* Timeline */}
          {filtered.length === 0 ? (
            <div className="card-neumorphic p-12 text-center">
              <Clock size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
              <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No activity yet</p>
              <p className="text-sm text-[var(--text-secondary)] mb-6">Your learning activity will appear here.</p>
            </div>
          ) : (
            <div className="space-y-6">
              {Object.entries(groupedByDate).map(([date, items]: [string, any]) => (
                <div key={date}>
                  <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4 pl-4 border-l-2 border-[var(--accent-primary)]">{date}</h2>
                  <div className="space-y-3">
                    {items.map((item: any, i: number) => {
                      const IconComponent = getIcon(item.type);
                      return (
                        <Link
                          key={i}
                          to={item.type === 'disease' ? `/disease/${item.id}` : item.type === 'case' ? `/case/${item.id}` : '#'}
                          className="card-neumorphic p-4 flex items-center justify-between hover:shadow-lg transition-all group"
                        >
                          <div className="flex items-center gap-4 flex-1">
                            <div className="icon-well icon-well-sm group-hover:bg-[var(--accent-primary)]/10">
                              <IconComponent size={18} className="text-[var(--accent-primary)]" />
                            </div>
                            <div className="flex-1 min-w-0">
                              <p className="font-semibold text-[var(--text-primary)] text-sm">{item.title}</p>
                              <p className="text-xs text-[var(--text-secondary)]">{item.action}</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-3">
                            <span className="text-xs text-[var(--text-tertiary)]">{item.time}</span>
                            <ChevronRight size={16} className="text-[var(--text-tertiary)]" />
                          </div>
                        </Link>
                      );
                    })}
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
