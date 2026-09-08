import { useParams, Link, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import {
  ArrowLeft, Bookmark, Share2, BookOpen, Clock, GraduationCap,
  ChevronRight, Sparkles, Target, Brain, FileText, MessageSquare, AlertCircle
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useBookmarks } from '../../hooks/useBookmarks';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';
import { diseaseApi } from '../../api/disease';
import { diseaseSectionApi } from '../../api/diseaseSection';
import { flashcardApi } from '../../api/flashcard';
import AiSummaryWidget from '../../components/disease/AiSummaryWidget';

export default function DiseaseDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { isBookmarked, toggleBookmark } = useBookmarks();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [diseaseData, setDiseaseData] = useState<any>(null);
  const [sections, setSections] = useState<any[]>([]);

  useEffect(() => {
    const loadDiseaseDetail = async () => {
      if (!id) {
        setError('Disease ID not provided');
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError(null);
      try {
        // Fetch disease info
        const disease = await diseaseApi.fetchDisease(id);
        
        // Fetch current version
        const currentVersion = await diseaseApi.getCurrentVersion(Number(disease.id));
        
        // Fetch sections for current version
        const sections = await diseaseSectionApi.getSectionsByVersion(currentVersion.id);
        
        // Set disease data
        setDiseaseData({ disease, currentVersion, sections });
      } catch (err: any) {
        console.error('Failed to load disease detail', err);
        setError(err.response?.data?.message || 'Failed to load disease detail');
      } finally {
        setIsLoading(false);
      }
    };

    loadDiseaseDetail();
  }, [id, navigate]);

  if (isLoading) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6">
        <div className="max-w-4xl mx-auto">
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            <p className="mt-4 text-[var(--text-secondary)]">Loading disease details...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6">
        <div className="max-w-4xl mx-auto">
          <div className="text-center py-12">
            <p className="text-[var(--text-error)]">{error}</p>
            <Link to="/explorer" className="mt-4 inline-flex items-center px-4 py-2 border border-[var(--border-primary)] text-[var(--text-primary)] rounded-md hover:bg-[var(--surface-primary)]">
              <ArrowLeft size={16} /> Back to Diseases
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (!diseaseData) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6">
        <div className="max-w-4xl mx-auto">
          <div className="text-center py-12">
            <p className="text-[var(--text-secondary)]">No disease data found</p>
            <Link to="/explorer" className="mt-4 inline-flex items-center px-4 py-2 border border-[var(--border-primary)] text-[var(--text-primary)] rounded-md hover:bg-[var(--surface-primary)]">
              <ArrowLeft size={16} /> Back to Diseases
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const disease = diseaseData.disease;
  const currentVersion = diseaseData.currentVersion;
  const diseaseSections = diseaseData.sections;

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-4xl mx-auto">
        <AnimatedSection>
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <button onClick={() => navigate('/explorer')} className="flex items-center gap-2 text-[var(--text-secondary)] hover:text-[var(--text-primary)]">
              <ArrowLeft size={18} /> Back to Diseases
            </button>
            <div className="flex items-center gap-3">
              <button
                onClick={() => {
                  if (!id) return;
                  toggleBookmark({
                    id,
                    type: 'diseases',
                    title: disease.name,
                    category: disease.categoryName,
                    link: `/disease/${id}`
                  });
                }}
                className="p-2 rounded-lg hover:bg-[var(--surface-hover)]"
                title="Bookmark disease"
              >
                {id && isBookmarked(id, 'diseases') ? <Bookmark fill="var(--accent-primary)" size={20} className="text-[var(--accent-primary)]" /> : <Bookmark size={20} />}
              </button>
              <button className="p-2 rounded-lg hover:bg-[var(--surface-hover)]">
                <Share2 size={20} />
              </button>
            </div>
          </div>

          {/* Title Section */}
          <div className="card-neumorphic p-8 mb-8">
            <h1 className="font-display text-4xl font-bold text-[var(--text-primary)] mb-2">{disease.name}</h1>
            <p className="text-sm text-[var(--text-secondary)] mb-4">{disease.description || ''}</p>
            <div className="flex flex-wrap items-center gap-4 text-sm">
              <span className="stat-pill">
                <GraduationCap size={14} className="text-[var(--accent-primary)]" />
                {currentVersion?.versionNumber ? `v${currentVersion.versionNumber}` : 'N/A'}
              </span>
              <span className="stat-pill">
                <Clock size={14} className="text-[var(--accent-primary)]" />
                {currentVersion?.updatedAt ? new Date(currentVersion.updatedAt).toLocaleDateString() : 'N/A'}
              </span>
              <span className="stat-pill">
                <BookOpen size={14} className="text-[var(--accent-primary)]" />
                {disease.categoryName || 'N/A'}
              </span>
            </div>
          </div>

          {/* Reading Progress and Last Read (removed mock data) */}
          {user && (
            <div className="card-neumorphic p-6 mb-8">
              <div className="flex items-center justify-between mb-3">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">Your Progress</h2>
                <span className="text-sm font-semibold text-[var(--accent-primary)]">not tracked</span>
              </div>
              <p className="text-xs text-[var(--text-secondary)] mt-3">Last read: not tracked</p>
            </div>
          )}

          {/* Main Content Grid */}
          <div className={`grid gap-6 mb-8 ${user ? 'grid-cols-1 lg:grid-cols-3' : 'grid-cols-1'}`}>
            {/* Left: Content (always full width for guests, 2/3 for users) */}
            <div className={user ? 'lg:col-span-2 space-y-6' : 'space-y-6'}>
              <AnimatedSection>
                {diseaseSections.map((section: any, index: number) => (
                  <div key={section.id || index} className="card-neumorphic p-8">
                    <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">{section.sectionTypeName || section.title || 'Section'}</h2>
                    <div className="prose prose-sm text-[var(--text-secondary)] leading-relaxed">
                      {section.content ? (
                        section.content.split('\n\n').map((paragraph: string, paraIndex: number) => (
                          <p key={paraIndex} className="mb-4">{paragraph}</p>
                        ))
                      ) : (
                        <p className="text-[var(--text-secondary)] italic">No content available</p>
                      )}
                    </div>
                  </div>
                ))}
              </AnimatedSection>
            </div>

            {/* Right: Sidebar (only for logged-in users) */}
            {user && (
              <div className="lg:col-span-1 space-y-6">
                {/* AI Summaries */}
                <AiSummaryWidget diseaseId={Number(disease.id)} />

                {/* Quick Actions */}
                <div className="card-neumorphic p-6 space-y-2">
                  <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Generate</h2>
                  <button
                    onClick={async () => {
                      try {
                        await flashcardApi.generate({
                          diseaseId: Number(disease.id),
                          title: `${disease.name} Flashcards`,
                          count: 10,
                        });
                        navigate('/flashcards');
                      } catch (err: any) {
                        alert(err.response?.data?.message || err.message || 'Failed to generate flashcards');
                      }
                    }}
                    className="btn-neumorphic-secondary py-2 px-4 w-full flex items-center justify-center gap-2 text-sm"
                  >
                    <Sparkles size={14} /> Generate Flashcards
                  </button>
                  <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full flex items-center justify-center gap-2 opacity-50 cursor-not-allowed text-sm">
                    <Target size={14} /> Quiz
                  </button>
                  <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full flex items-center justify-center gap-2 opacity-50 cursor-not-allowed text-sm">
                    <Brain size={14} /> Case Study
                  </button>
                  <Link to={`/disease/${disease.id}/edit`} className="btn-neumorphic-primary py-2 px-4 w-full flex items-center justify-center gap-2 text-sm">
                    <FileText size={14} /> Edit
                  </Link>
                </div>

                {/* Learning Notes */}
                <div className="card-neumorphic p-6">
                  <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">My Notes</h2>
                  <textarea placeholder="Add personal notes..." className="input-neumorphic w-full min-h-[120px] resize-none text-sm" />
                  <button className="btn-neumorphic-primary py-2 px-4 mt-3 text-sm">Save Notes</button>
                </div>

                {/* Related Diseases (removed mock data) */}
                <div className="card-neumorphic p-6">
                  <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Related Diseases</h2>
                  <p className="text-[var(--text-secondary)]">not available</p>
                </div>

                {/* Discussion */}
                <div className="card-neumorphic p-6">
                  <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4 flex items-center gap-2">
                    <MessageSquare size={18} /> Discussion
                  </h2>
                  <p className="text-xs text-[var(--text-secondary)] mb-3">Comments disabled (coming soon)</p>
                  <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full opacity-50 cursor-not-allowed text-sm">Add Comment</button>
                </div>
              </div>
            )}
          </div>

          {/* Continue Reading CTA (only for logged-in users) */}
          {user && (
            <div className="card-neumorphic p-6 flex items-center justify-between">
              <div>
                <p className="font-semibold text-[var(--text-primary)]">Continue reading</p>
                <p className="text-xs text-[var(--text-secondary)]">Reading progress not tracked</p>
              </div>
              <button className="btn-neumorphic-primary py-3 px-8 flex items-center gap-2">
                Continue <ChevronRight size={18} />
              </button>
            </div>
          )}
        </AnimatedSection>
      </div>
    </div>
  );
}
