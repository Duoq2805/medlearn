import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, BookOpen, ChevronRight, Plus, Trash2, Eye, Send, Clock, FileText, AlertTriangle, Loader2, X } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';
import DraftCreationFlow from '../../components/disease/DraftCreationFlow';
import { diseaseApi } from '../../api/disease';
import { diseaseSectionApi } from '../../api/diseaseSection';

export default function MyDraftsPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [flowOpen, setFlowOpen] = useState(false);
  type Draft = { id: string; title: string; status: string; lastEdited: string; version: number; reviewerComments: string | null; diseaseId: number; _raw: unknown };
  const [drafts, setDrafts] = useState<Draft[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [previewingDraftId, setPreviewingDraftId] = useState<string | null>(null);
  const [previewData, setPreviewData] = useState<any>(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewError, setPreviewError] = useState<string | null>(null);

  useEffect(() => {
    const loadUserDrafts = async () => {
      console.log('Loading user drafts, user:', user);
      if (!user) {
        console.log('No user, setting loading to false');
        setLoading(false);
        return;
      }
      setLoading(true);
      setError(null);
      try {
        console.log('Calling diseaseApi.getMyDrafts');
        const draftsResponse = await diseaseApi.getMyDrafts(0, 100);
        console.log('Full API response:', draftsResponse);
        console.log('API response type:', typeof draftsResponse);
        console.log('API response keys:', Object.keys(draftsResponse || {}));
        console.log('API response content:', draftsResponse?.content);
        console.log('API response data:', draftsResponse?.data);
        console.log('API response items:', draftsResponse?.items);
        console.log('API response results:', draftsResponse?.results);
        console.log('API response drafts:', draftsResponse?.drafts);
        console.log('API response diseases:', draftsResponse?.diseases);

        // Handle both direct array response and paginated response
        let draftsArray: any[] = [];
        if (Array.isArray(draftsResponse)) {
          draftsArray = draftsResponse;
          console.log('Using direct array response');
        } else if (draftsResponse && draftsResponse.content !== undefined) {
          draftsArray = draftsResponse.content || [];
          console.log('Using paginated response with content property');
        } else if (draftsResponse && draftsResponse.data !== undefined) {
          draftsArray = Array.isArray(draftsResponse.data) ? draftsResponse.data : (draftsResponse.data.content || []);
          console.log('Using response with data property');
        } else if (draftsResponse && draftsResponse.items !== undefined) {
          draftsArray = draftsResponse.items || [];
          console.log('Using response with items property');
        } else if (draftsResponse && draftsResponse.results !== undefined) {
          draftsArray = draftsResponse.results || [];
          console.log('Using response with results property');
        } else if (draftsResponse && draftsResponse.drafts !== undefined) {
          draftsArray = draftsResponse.drafts || [];
          console.log('Using response with drafts property');
        } else if (draftsResponse && draftsResponse.diseases !== undefined) {
          draftsArray = draftsResponse.diseases || [];
          console.log('Using response with diseases property');
        } else {
          console.log('Unknown response format, treating as empty array');
        }

        console.log('Final drafts array:', draftsArray);
        console.log('Drafts array length:', draftsArray.length);

        const draftsResults = draftsArray.map((draft: { id: number; diseaseName?: string; title?: string; status: string; updatedAt?: string; versionNumber?: number; reviewNote?: string; diseaseId: number }) => ({
          id: draft.id.toString(),
          title: draft.diseaseName || draft.title || 'Untitled draft',
          status: draft.status,
          lastEdited: draft.updatedAt ? new Date(draft.updatedAt).toLocaleString() : 'Just now',
          version: draft.versionNumber || 1,
          reviewerComments: draft.reviewNote || null,
          diseaseId: draft.diseaseId,
          // Preserve the full draft object for potential use
          _raw: draft
        }));
        console.log('Processed drafts results:', draftsResults);
        setDrafts(draftsResults);
      } catch (err: any) {
        console.error('Failed to load user drafts', err);
        setError(err.response?.data?.message || 'Failed to load drafts');
      } finally {
        setLoading(false);
      }
    };

    loadUserDrafts();
  }, [user]);

  const handlePreviewDraft = async (draftId: string) => {
    setPreviewingDraftId(draftId);
    setPreviewLoading(true);
    setPreviewError(null);
    setPreviewData(null);
    
    try {
      // Find the draft to get its diseaseId
      const draft = drafts.find(d => d.id === draftId);
      if (!draft || !draft.diseaseId) {
        throw new Error('Draft not found or missing disease ID');
      }

      // Get the latest draft version for this disease
      const versionResponse = await diseaseApi.getLatestDraftVersion(draft.diseaseId);
      const versionData = versionResponse;
      const versionId = versionData.id;

      if (!versionId) {
        throw new Error('Could not retrieve version ID for draft');
      }

      // Get sections for this version
      const sectionsResponse = await diseaseSectionApi.getSectionsByVersion(versionId);
      const sections = sectionsResponse;

      // Get disease details for metadata
      const diseaseResponse = await diseaseApi.fetchDisease(draft.diseaseId.toString());
      const disease = diseaseResponse;

      // Process sections into a readable format
      const processedSections = sections.map((section: any) => ({
        title: section.title || section.sectionTypeName || 'Section',
        content: section.content || '',
        order: section.orderIndex || 0
      })).sort((a, b) => a.order - b.order);

      setPreviewData({
        disease: {
          id: disease.id,
          name: disease.name,
          categoryName: disease.categoryName,
        },
        version: {
          versionNumber: versionData.versionNumber,
          status: versionData.status,
          updatedAt: versionData.updatedAt
        },
        sections: processedSections
      });
    } catch (err: any) {
      console.error('Failed to load preview data', err);
      setPreviewError(err.response?.data?.message || 'Failed to load preview data');
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleSubmitDraft = async (draftId: string) => {
    const draft = drafts.find(d => d.id === draftId);
    if (!draft || !draft.diseaseId) return;

    try {
      // Get the latest draft version for this disease to get the version ID
      const versionResponse = await diseaseApi.getLatestDraftVersion(draft.diseaseId);
      const versionData = versionResponse;
      const versionId = versionData.id;

      if (!versionId) {
        throw new Error('Could not retrieve version ID for draft');
      }

      await diseaseApi.submitVersionForReview(versionId);
      
      // Update the draft status in our local state
      setDrafts(prevDrafts =>
        prevDrafts.map(d =>
          d.id === draftId ? { ...d, status: 'PENDING_REVIEW' } : d
        )
      );
      
      alert('Draft submitted for review successfully!');
    } catch (err: any) {
      console.error('Failed to submit draft for review', err);
      const errorMessage = err.response?.data?.message || 'Failed to submit draft for review';
      setError(errorMessage);
      alert(errorMessage);
    }
  };

  const handleDeleteDraft = async (draftId: string) => {
    if (!window.confirm('Are you sure you want to delete this draft? This action cannot be undone.')) {
      return;
    }

    const draft = drafts.find(d => d.id === draftId);
    if (!draft || !draft.id) return;

    try {
      // TODO: Implement draft deletion API call
      // For now, we'll simulate it by removing from local state
      // In a real implementation, you would call an API endpoint like:
      // await diseaseApi.deleteDraft(draft.id);
      
      // Remove the draft from our local state
      setDrafts(prevDrafts => prevDrafts.filter(d => d.id !== draftId));
      
      alert('Draft deleted successfully!');
    } catch (err: any) {
      console.error('Failed to delete draft', err);
      const errorMessage = err.response?.data?.message || 'Failed to delete draft';
      setError(errorMessage);
      alert(errorMessage);
    }
  };

  const handleCloseFlow = () => {
    setFlowOpen(false);
  };

  const handleClosePreview = () => {
    setPreviewingDraftId(null);
    setPreviewData(null);
    setPreviewError(null);
  };

  if (loading) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6">
        <div className="max-w-7xl mx-auto">
          <AnimatedSection>
            <div className="text-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
              <p className="mt-4 text-[var(--text-secondary)]">Loading your drafts...</p>
            </div>
          </AnimatedSection>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6">
        <div className="max-w-7xl mx-auto">
          <AnimatedSection>
            <div className="text-center py-12">
              <p className="text-[var(--text-error)]">{error}</p>
              <Link to="/disease" className="mt-4 inline-flex items-center px-4 py-2 border border-[var(--border-primary)] text-[var(--text-primary)] rounded-md hover:bg-[var(--surface-primary)]">
                <ArrowLeft size={16} /> Back to Diseases
              </Link>
            </div>
          </AnimatedSection>
        </div>
      </div>
    );
  }

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

          <DraftCreationFlow open={flowOpen} onClose={handleCloseFlow} />

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {drafts.map((draft) => (
              <div key={draft.id} className="card-neumorphic p-6 hover:shadow-lg transition-all flex flex-col">
                <div className="flex items-start justify-between mb-3">
                  <h3 className="font-display text-lg font-bold text-[var(--text-primary)]">{draft.title}</h3>
                  <span className={`px-3 py-1 rounded-full text-xs font-medium $
                    draft.status === 'DRAFT' ? 'bg-blue-500/10 text-blue-600' :
                    draft.status === 'PENDING_REVIEW' ? 'bg-amber-500/10 text-amber-600' :
                    draft.status === 'REJECTED' ? 'bg-red-500/10 text-red-600' :
                    draft.status === 'NEEDS_CHANGES' ? 'bg-yellow-500/10 text-yellow-600' :
                    'bg-gray-500/10 text-gray-600'
                  `}>
                    {draft.status}
                  </span>
                </div>
                <p className="text-xs text-[var(--text-secondary)] mb-4">
                  Last edited: {draft.lastEdited} • v{draft.version}
                </p>
                {draft.status === 'REJECTED' && draft.reviewerComments && (
                  <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/20">
                    <div className="flex items-center gap-2 mb-1">
                      <AlertTriangle size={14} className="text-red-600" />
                      <p className="text-sm font-semibold text-red-600">Reviewer Comments</p>
                    </div>
                    <p className="text-xs text-[var(--text-error)]">{draft.reviewerComments}</p>
                  </div>
                )}
                <div className="mt-auto pt-3 border-t border-[var(--shadow-dark)] flex gap-2">
                  <Link to={`/disease/${draft.diseaseId}/edit`} className="btn-neumorphic-secondary py-2 px-3 text-sm flex-1 flex items-center justify-center gap-1">
                    Edit <ChevronRight size={14} />
                  </Link>
                  <button 
                    className="btn-neumorphic-secondary py-2 px-3 text-sm"
                    onClick={() => handlePreviewDraft(draft.id)}
                  >
                    <Eye size={14} />
                  </button>
                  {draft.status === 'DRAFT' && (
                    <button 
                      className="btn-neumorphic-primary py-2 px-3 text-sm flex items-center gap-1"
                      onClick={() => handleSubmitDraft(draft.id)}
                    >
                      <Send size={14} /> Submit
                    </button>
                  )}
                  <button 
                    className="btn-neumorphic-secondary py-2 px-3 text-sm text-red-600 hover:bg-red-500/10"
                    onClick={() => handleDeleteDraft(draft.id)}
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </AnimatedSection>
      </div>

      {/* Preview Modal */}
      {previewingDraftId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="relative w-full max-w-4xl mx-4 max-h-[90vh] overflow-y-auto">
            {/* Modal Backdrop */}
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={handleClosePreview} />
            {/* Modal Content */}
            <div className="relative z-10 bg-[var(--bg-primary)] rounded-2xl p-8 shadow-2xl">
              {/* Header */}
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                  <div className="p-3 rounded-xl bg-purple-500/10 text-purple-600">
                    <FileText size={24} />
                  </div>
                  <div>
                    <h2 className="font-display text-2xl font-bold text-[var(--text-primary)]">
                      {previewData?.disease?.name || 'Untitled Draft'}
                    </h2>
                    <p className="text-sm text-[var(--text-secondary)]">
                      {previewData?.disease?.categoryName || 'Uncategorized'} • {' '}
                      {previewData?.disease?.icdCode || 'No ICD Code'} • {' '}
                      v{previewData?.version?.versionNumber || '1'}
                    </p>
                  </div>
                </div>
                <button 
                  className="btn-neumorphic-secondary p-2 rounded-xl"
                  onClick={handleClosePreview}
                >
                  <X size={20} />
                </button>
              </div>

              {/* Description */}
              {previewData?.disease?.description && (
                <div className="mb-6">
                  <p className="text-[var(--text-secondary)] leading-relaxed">
                    {previewData.disease.description}
                  </p>
                </div>
              )}

              {/* Sections */}
              <div className="space-y-6">
                {previewData?.sections?.map((section: any, index: number) => {
                  return (
                    <div key={section.title || index} className="border-b border-[var(--shadow-dark)] pb-6 last:border-b-0">
                      <h3 className="font-display text-xl font-bold text-[var(--text-primary)] mb-4">
                        {section.title}
                      </h3>
                      <div className="prose prose-lg text-[var(--text-secondary)] leading-relaxed whitespace-pre-wrap">
                        {section.content ? (
                          <>
                            {section.content.split('\n\n').map((paragraph: string, paraIndex: number) => (
                              <p key={paraIndex} className="mb-4 last:mb-0">{paragraph}</p>
                            ))}
                          </>
                        ) : (
                          <p className="text-[var(--text-tertiary)] italic">No content available</p>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Footer Info */}
              <div className="mt-8 pt-6 border-t border-[var(--shadow-dark)] text-xs text-[var(--text-tertiary)] flex items-center justify-between">
                <span>
                  <Clock size={14} className="mr-2" /> 
                  Last edited: {new Date(previewData?.version?.updatedAt || Date.now()).toLocaleString()}
                </span>
                <span>
                  <FileText size={14} className="mr-2" /> 
                  Version {previewData?.version?.versionNumber || '1'} • {' '}
                  {previewData?.version?.status || 'Draft'}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}


