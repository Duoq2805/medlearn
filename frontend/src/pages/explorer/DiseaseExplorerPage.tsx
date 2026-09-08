import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import {
  Search, SortAsc, SortDesc, BookOpen, ChevronRight,
  Bookmark, AlertCircle, GraduationCap, Loader2, Plus, FileText
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useBookmarks } from '../../hooks/useBookmarks';
import { AnimatedSection } from '../../components/motion/MotionWrappers';
import { diseaseApi } from '../../api/disease';
import DraftCreationFlow from '../../components/disease/DraftCreationFlow';

export interface DisplayDisease {
  id: string;
  name: string;
  description: string;
  category: string;
  difficulty: string;
  updatedAt?: string;
}

const DEFAULT_MOCK: DisplayDisease[] = [
  { id: '1', name: 'Pneumonia', description: 'An infection that inflames air sacs in one or both lungs.', category: 'Infectious Diseases', difficulty: 'Medium' },
  { id: '2', name: 'Hypertension', description: 'A condition where blood pressure is high.', category: 'Cardiovascular', difficulty: 'Easy' },
  { id: '3', name: 'Diabetes Mellitus Type 2', description: 'A chronic condition affecting blood sugar levels.', category: 'Endocrine', difficulty: 'Medium' },
  { id: '4', name: 'Asthma', description: 'A chronic respiratory disease.', category: 'Respiratory', difficulty: 'Easy' },
  { id: '5', name: 'Migraine', description: 'A neurological condition causing headaches.', category: 'Neurology', difficulty: 'Medium' },
];

export default function DiseaseExplorerPage() {
  const { user } = useAuth();
  const { isBookmarked, toggleBookmark } = useBookmarks();
  const [flowOpen, setFlowOpen] = useState(false);

  const [searchQuery, setSearchQuery] = useState('');
  const [filterCategory, setFilterCategory] = useState('All');
  const [sortOrder, setSortOrder] = useState('asc');
  const [sortBy, setSortBy] = useState('name');

  const [realDiseases, setRealDiseases] = useState<DisplayDisease[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadDiseases = async () => {
      setIsLoading(true);
      try {
        const response = await diseaseApi.fetchDiseases(undefined, undefined, undefined, 0, 100);
        const content = response?.content || [];

        const loaded: DisplayDisease[] = content.map((d: any) => ({
          id: d.id.toString(),
          name: d.name,
          description: d.description || `Medical knowledge resource for ${d.name}.`,
          category: d.categoryName || d.category || 'General',
          difficulty: 'Medium',
          updatedAt: d.updatedAt ? new Date(d.updatedAt).toLocaleDateString() : undefined,
        }));

        setRealDiseases(loaded);

        // Fetch categories list
        try {
          const cats = await diseaseApi.getCategories();
          if (Array.isArray(cats) && cats.length > 0) {
            setCategories(cats.map((c: any) => c.name));
          }
        } catch (e) {
          console.warn('Failed to fetch categories list', e);
        }
      } catch (err) {
        console.error('Failed to load diseases from backend API', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadDiseases();
  }, []);

  // Merge real diseases with mock items if list is small
  const allDiseases = useMemo(() => {
    const realIds = new Set(realDiseases.map(d => d.id));
    const mocks = DEFAULT_MOCK.filter(m => !realIds.has(m.id));
    return [...realDiseases, ...mocks];
  }, [realDiseases]);

  const filteredDiseases = useMemo(() => {
    return allDiseases.filter(disease => {
      const matchesCategory = filterCategory === 'All' || disease.category.toLowerCase() === filterCategory.toLowerCase();
      const matchesSearch = disease.name.toLowerCase().includes(searchQuery.toLowerCase()) || disease.description.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesCategory && matchesSearch;
    });
  }, [allDiseases, searchQuery, filterCategory]);

  const sortedDiseases = useMemo(() => {
    return [...filteredDiseases].sort((a, b) => {
      if (sortBy === 'name') {
        return sortOrder === 'asc' ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name);
      }
      return 0;
    });
  }, [filteredDiseases, sortBy, sortOrder]);

  const handleBookmark = (disease: DisplayDisease) => {
    if (!user) { alert('Please log in to manage bookmarks.'); return; }
    toggleBookmark({
      id: disease.id,
      type: 'diseases',
      title: disease.name,
      category: disease.category,
      link: `/disease/${disease.id}`
    });
  };

  const categoryOptions = categories.length > 0
    ? categories
    : ['Infectious Diseases', 'Cardiovascular', 'Respiratory', 'Neurology', 'Endocrine', 'Gastroenterology', 'Nephrology', 'Dermatology', 'Oncology', 'Pediatrics', 'Psychiatry', 'Orthopedics'];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-3">
              <div className="icon-well"><BookOpen size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Diseases</h1>
                <p className="text-sm text-[var(--text-secondary)]">Explore medical diseases and clinical knowledge</p>
              </div>
            </div>
            {user && (
              <div className="flex items-center gap-3">
                <Link
                  to="/drafts"
                  className="btn-neumorphic py-2 px-4 text-sm flex items-center gap-2 text-[var(--text-primary)]"
                >
                  <FileText size={16} />
                  <span>My Drafts</span>
                </Link>
                <button
                  onClick={() => setFlowOpen(true)}
                  className="btn-neumorphic-primary py-2 px-4 text-sm flex items-center gap-2"
                >
                  <Plus size={16} />
                  <span>New Draft</span>
                </button>
              </div>
            )}
          </div>

          {/* Compact Search & Filter Toolbar */}
          <div className="card-neumorphic p-4 mb-6 flex flex-wrap items-center justify-between gap-3">
            <div className="relative flex-1 min-w-[200px] max-w-md">
              <input
                type="text"
                placeholder="Search diseases..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="input-neumorphic !pl-11 pr-4 py-2 w-full text-sm"
              />
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)] pointer-events-none" size={16} />
            </div>

            <div className="flex flex-wrap items-center gap-3">
              <select
                value={filterCategory}
                onChange={(e) => setFilterCategory(e.target.value)}
                className="input-neumorphic py-2 px-3 text-sm"
              >
                <option value="All">All Categories</option>
                {categoryOptions.map(c => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>

              <select
                value={`${sortBy}-${sortOrder}`}
                onChange={(e) => {
                  const [b, o] = e.target.value.split('-');
                  setSortBy(b);
                  setSortOrder(o);
                }}
                className="input-neumorphic py-2 px-3 text-sm"
              >
                <option value="name-asc">Name (A-Z)</option>
                <option value="name-desc">Name (Z-A)</option>
              </select>
            </div>
          </div>

          {/* Content */}
          {isLoading ? (
            <div className="card-neumorphic p-12 text-center text-[var(--text-secondary)]">
              <Loader2 size={32} className="animate-spin mx-auto mb-4 text-[var(--accent-primary)]" />
              <p>Loading diseases...</p>
            </div>
          ) : sortedDiseases.length === 0 ? (
            <div className="card-neumorphic p-12 text-center">
              <AlertCircle size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
              <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No diseases found</p>
              <button onClick={() => { setSearchQuery(''); setFilterCategory('All'); }} className="btn-neumorphic-primary py-3 px-8 text-sm">Clear Filters</button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {sortedDiseases.map(disease => (
                <div key={disease.id} className="card-neumorphic p-6 flex flex-col hover:shadow-lg transition-all">
                  <div className="flex justify-between items-start mb-3">
                    <Link to={`/disease/${disease.id}`} className="flex-1">
                      <h2 className="font-display text-lg font-bold text-[var(--text-primary)] hover:underline">{disease.name}</h2>
                    </Link>
                    {user && (
                      <button onClick={() => handleBookmark(disease)} className="p-1 rounded-md hover:bg-[var(--surface-hover)]" title="Bookmark">
                        {isBookmarked(disease.id, 'diseases') ? <Bookmark fill="var(--accent-primary)" size={18} className="text-[var(--accent-primary)]" /> : <Bookmark size={18} className="text-[var(--text-tertiary)]" />}
                      </button>
                    )}
                  </div>
                  <p className="text-xs text-[var(--text-secondary)] mb-4 flex-grow">{disease.description}</p>
                  <div className="mt-auto flex items-center justify-between pt-4 border-t border-[var(--shadow-dark)] text-xs text-[var(--text-tertiary)]">
                    <span><GraduationCap size={14} className="inline text-[var(--accent-primary)] mr-1" />{disease.category}</span>
                    <Link to={`/disease/${disease.id}`} className="btn-neumorphic-primary py-1.5 px-3 text-xs flex items-center gap-1">
                      View <ChevronRight size={12} />
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </AnimatedSection>
      </div>

      <DraftCreationFlow open={flowOpen} onClose={() => setFlowOpen(false)} />
    </div>
  );
}
