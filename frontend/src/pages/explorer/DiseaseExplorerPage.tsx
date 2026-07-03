import React, { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import {
  Search, Filter, SortAsc, SortDesc, BookOpen, ChevronRight,
  FileText, Plus, UploadCloud, Sparkles, Clock, Bookmark,
  CheckCircle2, AlertCircle, GraduationCap
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { getPermissions } from '../../hooks/usePermissions';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';
import Skeleton from '../../components/ui/Skeleton';

const MOCK_DISEASES = [
  { id: '1', name: 'Pneumonia', description: 'An infection that inflames air sacs in one or both lungs.', category: 'Infectious Diseases', difficulty: 'Medium', progress: 70, bookmarked: true },
  { id: '2', name: 'Hypertension', description: 'A condition where blood pressure is high.', category: 'Cardiovascular', difficulty: 'Easy', progress: 30, bookmarked: false },
  { id: '3', name: 'Diabetes Mellitus Type 2', description: 'A chronic condition affecting blood sugar levels.', category: 'Endocrine', difficulty: 'Medium', progress: 90, bookmarked: true },
  { id: '4', name: 'Asthma', description: 'A chronic respiratory disease.', category: 'Respiratory', difficulty: 'Easy', progress: 50, bookmarked: false },
  { id: '5', name: 'Migraine', description: 'A neurological condition causing headaches.', category: 'Neurology', difficulty: 'Medium', progress: 80, bookmarked: false },
] as const;

export default function DiseaseExplorerPage() {
  const { user } = useAuth();
  const perm = user ? getPermissions(user.roles?.[0]) : null;

  const [searchQuery, setSearchQuery] = useState('');
  const [filterCategory, setFilterCategory] = useState('All');
  const [sortOrder, setSortOrder] = useState('asc'); // asc or desc
  const [sortBy, setSortBy] = useState('name'); // name, difficulty, progress

  const isLoading = false; // Replace with actual loading state later

  const filteredDiseases = useMemo(() => {
    return MOCK_DISEASES.filter(disease => {
      const matchesCategory = filterCategory === 'All' || disease.category === filterCategory;
      const matchesSearch = disease.name.toLowerCase().includes(searchQuery.toLowerCase()) || disease.description.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesCategory && matchesSearch;
    });
  }, [searchQuery, filterCategory]);

  const sortedDiseases = useMemo(() => {
    return filteredDiseases.sort((a, b) => {
      if (sortBy === 'name') {
        return sortOrder === 'asc' ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name);
      } else if (sortBy === 'difficulty') {
        const difficultyOrder = { Easy: 1, Medium: 2, Hard: 3 };
        return sortOrder === 'asc' ? difficultyOrder[a.difficulty] - difficultyOrder[b.difficulty] : difficultyOrder[b.difficulty] - difficultyOrder[a.difficulty];
      } else if (sortBy === 'progress') {
        return sortOrder === 'asc' ? a.progress - b.progress : b.progress - a.progress;
      }
      return 0;
    });
  }, [filteredDiseases, sortBy, sortOrder]);

  const handleBookmark = (id: string) => {
    console.log(`Bookmarked disease ${id}`);
    // Implement bookmark logic
  };

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          {/* Toolbar */}
          <div className="mb-8 sticky top-28 z-40">
            <div className="card-neumorphic-lg p-6 flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className="icon-well"><BookOpen size={22} /></div>
                <h1 className="font-display text-2xl font-bold text-[var(--text-primary)]">Diseases</h1>
              </div>
              <div className="flex flex-wrap items-center gap-4">
                <div className="relative">
                  <input
                    type="text"
                    placeholder="Search diseases..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="input-neumorphic pl-10 w-64"
                  />
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" size={16} />
                </div>
                <select value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)} className="input-neumorphic py-2 px-3">
                  <option value="All">All Categories</option>
                  <option value="Infectious Diseases">Infectious Diseases</option>
                  <option value="Cardiovascular">Cardiovascular</option>
                  <option value="Endocrine">Endocrine</option>
                  <option value="Respiratory">Respiratory</option>
                  <option value="Neurology">Neurology</option>
                </select>
                <button onClick={() => setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')} className="btn-neumorphic-secondary py-2 px-3 flex items-center gap-1">
                  {sortBy === 'name' && (
                    <>{sortOrder === 'asc' ? <SortAsc size={16} /> : <SortDesc size={16} />} Name</>
                  )}
                  {sortBy === 'difficulty' && (
                    <>{sortOrder === 'asc' ? <SortAsc size={16} /> : <SortDesc size={16} />} Difficulty</>
                  )}
                  {sortBy === 'progress' && (
                    <>{sortOrder === 'asc' ? <SortAsc size={16} /> : <SortDesc size={16} />} Progress</>
                  )}
                </button>
              </div>
            </div>
          </div>

          {/* Main Workspace: Toolbar Actions + Disease Cards */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

            {/* Toolbar Actions */}
            <div className="lg:col-span-1 space-y-6">
              <div className="card-neumorphic p-6 space-y-4">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">Actions</h2>
                <div className="space-y-2">
                  <Link to="/diseases/new" className="btn-neumorphic-primary py-2 px-4 w-full flex items-center justify-center gap-2">
                    <Plus size={16} /> New Draft
                  </Link>
                  <Link to="/drafts" className="btn-neumorphic-secondary py-2 px-4 w-full flex items-center justify-center gap-2">
                    <FileText size={16} /> My Drafts
                  </Link>
                  <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full flex items-center justify-center gap-2 opacity-50 cursor-not-allowed">
                    <UploadCloud size={16} /> Import Document
                  </button>
                  <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full flex items-center justify-center gap-2 opacity-50 cursor-not-allowed">
                    <Sparkles size={16} /> AI Generate
                  </button>
                </div>
              </div>

              <div className="card-neumorphic p-6 space-y-4">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">Filters</h2>
                <div className="space-y-2">
                  <div className="flex flex-col gap-2">
                    <label className="text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider">Category</label>
                    <select value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)} className="input-neumorphic py-2 px-3">
                      <option value="All">All Categories</option>
                      <option value="Infectious Diseases">Infectious Diseases</option>
                      <option value="Cardiovascular">Cardiovascular</option>
                      <option value="Endocrine">Endocrine</option>
                      <option value="Respiratory">Respiratory</option>
                      <option value="Neurology">Neurology</option>
                    </select>
                  </div>
                  <div className="flex flex-col gap-2">
                    <label className="text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider">Sort By</label>
                    <div className="flex gap-2">
                      <button onClick={() => setSortBy('name')} className={`flex-1 btn-neumorphic-secondary py-2 px-3 text-sm ${sortBy === 'name' && 'btn-neumorphic-primary'}`}>Name</button>
                      <button onClick={() => setSortBy('difficulty')} className={`flex-1 btn-neumorphic-secondary py-2 px-3 text-sm ${sortBy === 'difficulty' && 'btn-neumorphic-primary'}`}>Difficulty</button>
                      <button onClick={() => setSortBy('progress')} className={`flex-1 btn-neumorphic-secondary py-2 px-3 text-sm ${sortBy === 'progress' && 'btn-neumorphic-primary'}`}>Progress</button>
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="card-neumorphic p-6 space-y-4">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">History</h2>
                <div className="space-y-2">
                  {[...Array(3)].map((_, i) => (
                    <Link key={i} to="/explorer" className="depth-layer-1 rounded-xl p-3 flex items-center justify-between text-sm hover:-translate-y-0.5 transition-all">
                      <span className="text-[var(--text-primary)]">Pneumonia</span>
                      <span className="text-xs text-[var(--text-tertiary)]">2 hours ago</span>
                    </Link>
                  ))}
                  <Link to="/history" className="text-xs text-[var(--accent-primary)] hover:underline mt-2">View Full History</Link>
                </div>
              </div>
            </div>

            {/* Disease Cards */}
            <div className="lg:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-6">
              {isLoading ? (
                [...Array(6)].map((_, i) => (
                  <div key={i} className="card-neumorphic p-6">
                    <Skeleton className="h-5 w-3/4 mb-4" />
                    <Skeleton className="h-4 w-full mb-2" />
                    <Skeleton className="h-4 w-1/2" />
                    <div className="flex items-center justify-between mt-4">
                      <Skeleton className="h-8 w-24" />
                      <Skeleton className="h-8 w-8" />
                    </div>
                  </div>
                ))
              ) : sortedDiseases.length === 0 ? (
                <div className="card-neumorphic p-12 text-center col-span-full">
                  <AlertCircle size={40} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
                  <p className="text-lg font-semibold text-[var(--text-primary)] mb-2">No diseases found</p>
                  <p className="text-sm text-[var(--text-secondary)] mb-6">Try adjusting your search or filters.</p>
                  <button onClick={() => { setSearchQuery(''); setFilterCategory('All'); }} className="btn-neumorphic-primary py-3 px-8">Clear Filters</button>
                </div>
              ) : (
                sortedDiseases.map(disease => (
                  <AnimatedSection key={disease.id} className="card-neumorphic p-6 flex flex-col hover:shadow-lg transition-all duration-300">
                    <div className="flex justify-between items-start mb-4">
                      <Link to={`/disease/${disease.id}`} className="flex-1">
                        <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-1 hover:underline">{disease.name}</h2>
                      </Link>
                      <button onClick={() => handleBookmark(disease.id)} className="p-1 rounded-md hover:bg-[var(--surface-hover)]">
                        {disease.bookmarked ? <Bookmark fill="var(--accent-primary)" size={18} className="text-[var(--accent-primary)]" /> : <Bookmark size={18} className="text-[var(--text-tertiary)]" />}
                      </button>
                    </div>
                    <p className="text-xs text-[var(--text-secondary)] mb-3 flex-grow">{disease.description.substring(0, 100)}...</p>
                    <div className="mt-auto flex items-center justify-between pt-4 border-t border-[var(--shadow-dark)]">
                      <div className="text-xs text-[var(--text-tertiary)] flex items-center gap-1">
                        {disease.difficulty === 'Easy' && <GraduationCap size={14} className="text-[var(--accent-primary)]" />}
                        {disease.difficulty === 'Medium' && <GraduationCap size={14} className="text-[var(--accent-primary)]" />}
                        {disease.difficulty}
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-20 h-2 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_4px_var(--shadow-dark)] overflow-hidden">
                          <div className="h-full bg-[var(--accent-primary)] rounded-full transition-all" style={{ width: `${disease.progress}%` }}></div>
                        </div>
                        <span className="text-xs font-semibold text-[var(--text-primary)]">{disease.progress}%</span>
                      </div>
                    </div>
                  </AnimatedSection>
                ))
              )}
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
