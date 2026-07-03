import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Bookmark, BookOpen, Target, Sparkles, ClipboardCheck,
  Search, Filter, ChevronRight, AlertCircle, Trash2
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function BookmarkPage() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('diseases');
  const [searchTerm, setSearchTerm] = useState('');

  const mockBookmarks = {
    diseases: [
      { id: '1', title: 'Pneumonia', category: 'Respiratory', saved: '2 days ago', progress: 70 },
      { id: '2', title: 'Hypertension', category: 'Cardiovascular', saved: '1 week ago', progress: 30 },
      { id: '3', title: 'Diabetes Mellitus', category: 'Endocrine', saved: '3 days ago', progress: 60 },
    ],
    cases: [
      { id: '1', title: 'Respiratory Infection', specialty: 'Pulmonology', saved: '5 days ago', status: 'In Progress' },
      { id: '2', title: 'Acute Coronary Syndrome', specialty: 'Cardiology', saved: '1 week ago', status: 'Completed' },
    ],
    flashcards: [
      { id: '1', title: 'Pneumonia Deck', count: 24, saved: '2 days ago', progress: 70 },
      { id: '2', title: 'Hypertension Essentials', count: 15, saved: '4 days ago', progress: 40 },
    ],
    quizzes: [
      { id: '1', title: 'Respiratory Quiz', questions: 10, saved: '6 days ago', accuracy: 80 },
    ],
  };

  const tabs = [
    { id: 'diseases', label: 'Diseases', icon: BookOpen, count: mockBookmarks.diseases.length },
    { id: 'cases', label: 'Cases', icon: Target, count: mockBookmarks.cases.length },
    { id: 'flashcards', label: 'Flashcards', icon: Sparkles, count: mockBookmarks.flashcards.length },
    { id: 'quizzes', label: 'Quizzes', icon: ClipboardCheck, count: mockBookmarks.quizzes.length },
  ];

  const currentItems = mockBookmarks[activeTab as keyof typeof mockBookmarks];
  const filteredItems = currentItems.filter(item => 
    item.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><Bookmark size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Bookmarks</h1>
              <p className="text-sm text-[var(--text-secondary)]">Your saved learning materials</p>
            </div>
          </div>

          {/* Tabs */}
          <div className="mb-8 overflow-x-auto">
            <div className="card-neumorphic p-2 flex gap-1 min-w-min">
              {tabs.map((tab) => {
                const Icon = tab.icon;
                return (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`py-2 px-4 rounded-lg flex items-center gap-2 text-sm font-medium transition-all whitespace-nowrap ${
                      activeTab === tab.id
                        ? 'bg-[var(--accent-primary)] text-white'
                        : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                    }`}
                  >
                    <Icon size={14} /> {tab.label}
                    <span className={`ml-1 px-2 py-0.5 rounded-full text-xs font-semibold ${activeTab === tab.id ? 'bg-white/20' : 'bg-[var(--surface-hover)]'}`}>
                      {tab.count}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Search */}
          <div className="mb-6">
            <div className="relative">
              <input
                type="text"
                placeholder={`Search ${activeTab}...`}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input-neumorphic w-full pl-10 py-3"
              />
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
            </div>
          </div>

          {/* Items */}
          {filteredItems.length === 0 ? (
            <div className="card-neumorphic p-12 text-center">
              <Bookmark size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
              <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No bookmarks yet</p>
              <p className="text-sm text-[var(--text-secondary)] mb-6">Start bookmarking {activeTab} to build your collection.</p>
              <Link to={activeTab === 'diseases' ? '/explorer' : activeTab === 'cases' ? '/cases' : '/dashboard'} className="btn-neumorphic-primary py-3 px-8 inline-flex items-center gap-2">
                Browse {activeTab} <ChevronRight size={16} />
              </Link>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredItems.map((item) => (
                <div key={item.id} className="card-neumorphic p-6 hover:shadow-lg transition-all flex flex-col">
                  <div className="flex items-start justify-between mb-3">
                    <h3 className="font-display text-lg font-bold text-[var(--text-primary)] flex-1">{item.title}</h3>
                    <button className="p-1 rounded hover:bg-[var(--surface-hover)] text-[var(--text-tertiary)] hover:text-red-500 transition-colors">
                      <Trash2 size={16} />
                    </button>
                  </div>
                  <p className="text-xs text-[var(--text-secondary)] mb-3">
                    {('category' in item) ? item.category : ('specialty' in item) ? item.specialty : ('count' in item) ? `${item.count} items` : `${item.questions} questions`}
                  </p>
                  <p className="text-[10px] text-[var(--text-tertiary)] mb-4">Saved {item.saved}</p>
                  <div className="mt-auto pt-3 border-t border-[var(--shadow-dark)]">
                    {'progress' in item && (
                      <div className="flex items-center gap-2 mb-3">
                        <div className="flex-1 h-2 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_4px_var(--shadow-dark)] overflow-hidden">
                          <div className="h-full bg-[var(--accent-primary)]" style={{ width: `${item.progress}%` }} />
                        </div>
                        <span className="text-xs font-semibold">{item.progress}%</span>
                      </div>
                    )}
                    <Link to={activeTab === 'diseases' ? `/disease/${item.id}` : activeTab === 'cases' ? `/case/${item.id}` : '#'} className="btn-neumorphic-primary py-2 px-4 text-sm w-full text-center flex items-center justify-center gap-1">
                      Open <ChevronRight size={14} />
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
