import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Plus, ChevronRight, FileText, Save, Eye, Send, Clock, Sparkles, BookOpen, AlertTriangle, GraduationCap } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';
import SourceViewer from '../../components/disease/SourceViewer';
import type { GeneratedSection } from '../../types/diseaseDraft';
import { diseaseApi } from '../../api/disease';
import { diseaseSectionApi } from '../../api/diseaseSection';

// Reusable Neumorphic Input Components
const NeumorphicInput = ({ label, value, onChange, placeholder, type = 'text', required = false, className = '', ...props }: {
  label: string; 
  value: string; 
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  placeholder: string; 
  type?: string; 
  required?: boolean; 
  className?: string;
  disabled?: boolean;
}) => {
  return (
    <div className={`relative ${className}`}>
      <label className="mb-2 block text-xs font-semibold uppercase tracking-wider text-[var(--text-secondary)]">
        {label}{required && <span className="text-red-500"> *</span>}
      </label>
      {type === 'textarea' ? (
        <textarea
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          className="input-neumorphic w-full min-h-[100px] resize-none rounded-xl px-4 py-3 text-sm"
          {...props}
        />
      ) : (
        <input
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          className="input-neumorphic w-full rounded-xl px-4 py-3 text-sm"
          {...props}
        />
      )}
    </div>
  );
};

export default function EditDiseasePage() {
  const { user } = useAuth();
  const { id: diseaseIdStr } = useParams<{ id: string }>();
  const diseaseId = diseaseIdStr ? parseInt(diseaseIdStr, 10) : null;
  const navigate = useNavigate();

  const [autoSave] = useState('All changes saved');
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('');
  const [icd, setIcd] = useState('');
  const [status, setStatus] = useState('Draft');
  const [version, setVersion] = useState(1);
  const [lastEdited, setLastEdited] = useState('Just now');
  const [sectionContents, setSectionContents] = useState<Record<string, string>>({});
  const [categories, setCategories] = useState<Array<{ id: number; name: string }>>([]);
  const [categoriesLoading, setCategoriesLoading] = useState(false);
  const [sectionTypesMap, setSectionTypesMap] = useState<Record<string, number>>({});
  const [sectionTypesLoading, setSectionTypesLoading] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isPreviewing, setIsPreviewing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [diseaseVersionId, setDiseaseVersionId] = useState<number | null>(null); // version ID of the draft we are editing
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [previewData, setPreviewData] = useState<any>(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [reviewerComments, setReviewerComments] = useState<string | null>(null);
  const sections = [
    { label: 'Definition', key: 'definition', hint: 'Giải thích bệnh là gì, cơ chế chính và phạm vi ảnh hưởng.', placeholder: 'Mô tả định nghĩa, bản chất và đặc điểm cốt lõi của bệnh...' },
    { label: 'Etiology', key: 'etiology', hint: 'Nêu nguyên nhân, yếu tố nguy cơ và tác nhân liên quan.', placeholder: 'Liệt kê nguyên nhân, tác nhân và yếu tố nguy cơ...' },
    { label: 'Symptoms', key: 'symptoms', hint: 'Mô tả triệu chứng điển hình, thời điểm khởi phát và mức độ.', placeholder: 'Mô tả triệu chứng thường gặp, dấu hiệu cảnh báo...' },
    { label: 'Diagnosis', key: 'diagnosis', hint: 'Trình bày tiêu chuẩn, xét nghiệm và chẩn đoán phân biệt.', placeholder: 'Nêu quy trình chẩn đoán, xét nghiệm và chẩn đoán phân biệt...' },
    { label: 'Treatment', key: 'treatment', hint: 'Ghi hướng điều trị, thuốc, liều dùng và theo dõi.', placeholder: 'Trình bày mục tiêu điều trị, phương pháp và theo dõi...' },
    { label: 'Complications', key: 'complications', hint: 'Nêu biến chứng có thể xảy ra và dấu hiệu cần xử trí.', placeholder: 'Mô tả biến chứng, mức độ nguy hiểm và xử trí...' },
    { label: 'Prevention', key: 'prevention', hint: 'Hướng dẫn phòng bệnh, giảm nguy cơ và tái phát.', placeholder: 'Nêu biện pháp phòng ngừa, sàng lọc và thay đổi lối sống...' },
    { label: 'References', key: 'references', hint: 'Ghi nguồn tài liệu y khoa dùng để xây dựng nội dung.', placeholder: 'Nhập tài liệu tham khảo, DOI hoặc URL nguồn...' },
  ];

  const completedSections = sections.filter(({ key }) => (sectionContents[key] ?? '').trim().length > 0).length;
  const completionPct = Math.round((completedSections / sections.length) * 100);
  useEffect(() => {
    if (!diseaseId) {
      navigate('/disease');
      return;
    }
    const loadData = async () => {
      setIsLoading(true);
      setError(null);
      try {
        // Fetch disease details
        const diseaseResponse = await diseaseApi.fetchDisease(diseaseId.toString());
        // Fetch categories for the dropdown
        const categoriesResponse = await diseaseApi.getCategories();
        setCategories(categoriesResponse);
        // Fetch section types
        const sectionTypesResponse = await diseaseSectionApi.getAllSectionTypes();
        const sectionTypesMap: Record<string, number> = {};
        const sectionTypesData = sectionTypesResponse;
        sectionTypesData.forEach((st: { id: number; name: string }) => {
          sectionTypesMap[st.name] = st.id;
        });
        setSectionTypesMap(sectionTypesMap);
        // Fetch latest draft version for this disease
        const versionResponse = await diseaseApi.getLatestDraftVersion(diseaseId);
        const versionData = versionResponse;
        setDiseaseVersionId(versionData.id);
        // Fetch sections for this version
        const sectionsResponse = await diseaseSectionApi.getSectionsByVersion(versionData.id);
        const sections = sectionsResponse;
        // Initialize section contents
        const initialContents: Record<string, string> = {};
        sections.forEach((section: any) => {
          const labelMap: Record<string, string> = {
            'Definition': 'definition',
            'Etiology': 'etiology',
            'Symptoms': 'symptoms',
            'Diagnosis': 'diagnosis',
            'Treatment': 'treatment',
            'Complications': 'complications',
            'Prevention': 'prevention',
            'References': 'references',
          };
          const label = section.sectionTypeName || section.type;
          const key = labelMap[label];
          if (key) {
            initialContents[key] = section.content || '';
          }
        });
        setSectionContents(initialContents);
        // Set disease metadata
        setTitle(diseaseResponse?.name || diseaseResponse.name || '');
        setCategory(diseaseResponse?.categoryName || '');
        setIcd('');
        setStatus(versionData.status || 'Draft');
        setVersion(versionData.versionNumber || 1);
        setLastEdited(versionData.updatedAt ? new Date(versionData.updatedAt).toLocaleString() : 'Just now');
        setReviewerComments(versionData.moderationNote || null);
        setHasUnsavedChanges(false);
      } catch (err: any) {
        console.error('Failed to load disease draft for editing', err);
        setError(err.response?.data?.message || 'Failed to load disease draft');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [diseaseId, navigate]);

  // Handle saving draft (update)
  const handleSaveDraft = async () => {
    if (!title.trim()) {
      alert('Please enter a disease title');
      return;
    }

    if (!diseaseId || !diseaseVersionId) {
      alert('Invalid disease or version ID');
      return;
    }

    setIsSaving(true);
    setError(null);
    try {
      // Generate slug from title with fallback to prevent empty slug
      let slug = title
        .toLowerCase()
        .replace(/[^\\w\\s-]/g, '') // remove non-word, non-space, non-hyphen
        .replace(/[\\s_-]+/g, '-') // replace spaces and underscores with hyphen
        .trim();

      // Fallback to prevent empty slug (e.g., when title contains only special characters)
      if (!slug) {
        slug = 'draft-' + Math.random().toString(36).substr(2, 9);
      }

      // Find categoryId by category name
      const categoryObj = categories.find(cat => cat.name === category);
      const categoryId = categoryObj ? categoryObj.id : null;

      // Prepare section requests for creation (we will delete and recreate)
      const sectionRequests = Object.entries(sectionContents).map(([key, content]) => {
        // Map our section key to section type label
        const labelMap: Record<string, string> = {
          'definition': 'Definition',
          'etiology': 'Etiology',
          'symptoms': 'Symptoms',
          'diagnosis': 'Diagnosis',
          'treatment': 'Treatment',
          'complications': 'Complications',
          'prevention': 'Prevention',
          'references': 'References',
        };
        const label = labelMap[key];
        // Mapping section key -> fallback DB SectionType ID if API lookup fails
        const FALLBACK_SECTION_TYPE_MAP: Record<string, number> = {
          'definition': 1,
          'etiology': 2,
          'symptoms': 3,
          'diagnosis': 4,
          'treatment': 5,
          'complications': 6,
          'prevention': 7,
          'references': 8,
        };

        // Case-insensitive lookup for section type ID
        const sectionTypeEntry = Object.entries(sectionTypesMap).find(([name]) => 
          name.toLowerCase() === label.toLowerCase()
        );
        const sectionTypeId = sectionTypeEntry ? sectionTypeEntry[1] : (FALLBACK_SECTION_TYPE_MAP[key] ?? 1);
        const orderIndex = ['definition', 'etiology', 'symptoms', 'diagnosis', 'treatment', 'complications', 'prevention', 'references'].indexOf(key);
        return {
          sectionTypeId: sectionTypeId,
          title: label,
          content: content,
          orderIndex: orderIndex >= 0 ? orderIndex : 0,
        };
      });

      // 1. Update disease metadata
      await diseaseApi.updateDiseaseMetadata(diseaseId.toString(), {
        name: title,
        categoryId: categoryId || undefined,
      });

      // 2. Delete all existing sections for this version
      const existingSectionsResponse = await diseaseSectionApi.getSectionsByVersion(diseaseVersionId);
      const existingSections = existingSectionsResponse;
      for (const section of existingSections) {
        await diseaseSectionApi.deleteSection(section.id);
      }

      // 3. Create new sections
      for (const sectionReq of sectionRequests) {
        await diseaseSectionApi.createSection(diseaseVersionId, sectionReq);
      }

      // Reload data from backend to ensure synchronization
      const versionResponse = await diseaseApi.getLatestDraftVersion(diseaseId);
      const versionData = versionResponse;
      setDiseaseVersionId(versionData.id);

      const sectionsResponse = await diseaseSectionApi.getSectionsByVersion(versionData.id);
      const sections = sectionsResponse;
      const reloadedContents: Record<string, string> = {};
      sections.forEach((section: any) => {
        const labelMap: Record<string, string> = {
          'Definition': 'definition',
          'Etiology': 'etiology',
          'Symptoms': 'symptoms',
          'Diagnosis': 'diagnosis',
          'Treatment': 'treatment',
          'Complications': 'complications',
          'Prevention': 'prevention',
          'References': 'references',
        };
        const label = section.sectionTypeName || section.type;
        const key = labelMap[label];
        if (key) {
          reloadedContents[key] = section.content || '';
        }
      });
      setSectionContents(reloadedContents);
      setStatus(versionData.status || 'Draft');
      setVersion(versionData.versionNumber || 1);
      setLastEdited(versionData.updatedAt ? new Date(versionData.updatedAt).toLocaleString() : 'Just now');
      setHasUnsavedChanges(false);

      alert('Draft updated successfully!');
    } catch (err: any) {
      console.error('Failed to update disease draft', err);
      let errorMessage = 'Failed to update draft. Please try again.';
      if (err.response && err.response.data && err.response.data.message) {
        errorMessage = err.response.data.message;
      } else if (err.response && err.response.data && err.response.data.errors) {
        const validationErrors = err.response.data.errors;
        if (validationErrors && Array.isArray(validationErrors)) {
          errorMessage = validationErrors.map(err => err.defaultMessage || err.message).join('\n');
        } else if (typeof validationErrors === 'object') {
          errorMessage = Object.values(validationErrors).join('\n');
        }
      }
      setError(errorMessage);
      alert(errorMessage);
    } finally {
      setIsSaving(false);
    }
  };

  // Handle submitting draft for review
  const handleSubmitForReview = async () => {
    if (!diseaseVersionId) {
      alert('Invalid version ID');
      return;
    }

    setIsSubmitting(true);
    setError(null);
    try {
      // First save any unsaved changes to make sure reviewer sees latest content
      if (hasUnsavedChanges) {
        await handleSaveDraft();
      }

      await diseaseApi.submitVersionForReview(diseaseVersionId);

      // Reload version details
      if (diseaseId) {
        const versionResponse = await diseaseApi.getLatestDraftVersion(diseaseId);
        const versionData = versionResponse;
        setStatus(versionData.status || 'PENDING_REVIEW');
      }

      alert('Draft submitted for review successfully!');
      navigate('/disease');
    } catch (err: any) {
      console.error('Failed to submit draft for review', err);
      let errorMessage = 'Failed to submit draft for review. Please try again.';
      if (err.response && err.response.data && err.response.data.message) {
        errorMessage = err.response.data.message;
      }
      setError(errorMessage);
      alert(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle preview toggle
  const handleTogglePreview = () => {
    setIsPreviewing(!isPreviewing);
    if (!isPreviewing) {
      loadPreviewData();
    } else {
      setPreviewData(null);
      setPreviewLoading(false);
      setPreviewError(null);
    }
  };

  const loadPreviewData = async () => {
    if (!diseaseId || !diseaseVersionId) return;
    setPreviewLoading(true);
    setPreviewError(null);
    try {
      const diseaseResponse = await diseaseApi.fetchDisease(diseaseId.toString());
      const disease = diseaseResponse;
      const versionResponse = await diseaseApi.getLatestDraftVersion(diseaseId);
      const version = versionResponse;
      const sectionsResponse = await diseaseSectionApi.getSectionsByVersion(diseaseVersionId);
      const sections = sectionsResponse;

      const preview = {
        disease: {
          id: disease.id,
          name: disease.name,
          slug: disease.slug,
          categoryName: disease.categoryName,
        },
        currentVersion: {
          id: version.id,
          versionNumber: version.versionNumber,
          status: version.status,
          moderationNote: version.moderationNote,
          createdAt: version.createdAt,
          updatedAt: version.updatedAt,
        },
        sections: sections.map((section: any) => ({
          id: section.id,
          title: section.title,
          content: section.content,
          sectionTypeName: section.type || section.sectionTypeName,
          orderIndex: section.orderIndex,
        }))
      };
      setPreviewData(preview);
    } catch (err: any) {
      console.error('Failed to load preview data', err);
      setPreviewError(err.response?.data?.message || 'Failed to load preview data');
    } finally {
      setPreviewLoading(false);
    }
  };

  const getSectionContent = (key: string): string => {
    return sectionContents[key] ?? '';
  };

  if (isLoading) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6 text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
        <p className="mt-4 text-[var(--text-secondary)]">Loading draft details...</p>
      </div>
    );
  }

  if (error && !title) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6 text-center">
        <AlertTriangle size={48} className="mx-auto mb-4 text-[var(--text-error)]" />
        <p className="text-[var(--text-error)] text-lg mb-4">{error}</p>
        <Link to="/disease" className="btn-neumorphic-primary py-2 px-6">Back to Diseases</Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen pt-28 pb-24 relative z-10">
      <div className="max-w-7xl mx-auto px-6">
        <div className="flex gap-8 items-start">
          {/* EDITOR (70%) */}
          <main className="flex-1 max-w-[70ch] min-w-0 space-y-8">
            <AnimatedSection>
              <div className="mb-4">
                <Link to="/disease" className="inline-flex items-center gap-2 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)]">
                  <ArrowLeft size={16} /> Back to Diseases
                </Link>
              </div>

              {/* Status Header for rejected drafts */}
              {status === 'REJECTED' && reviewerComments && (
                <div className="card-neumorphic p-6 border-l-4 border-red-500 bg-red-500/5">
                  <div className="flex items-center gap-2 mb-2">
                    <AlertTriangle size={18} className="text-red-500" />
                    <h3 className="font-display font-bold text-red-600">Draft Rejected by Reviewer</h3>
                  </div>
                  <p className="text-sm text-[var(--text-error)] font-medium">Comments: {reviewerComments}</p>
                  <p className="text-xs text-[var(--text-secondary)] mt-2">Please address the feedback above, save your changes, and submit again.</p>
                </div>
              )}

              {/* Header Meta */}
              <div className="card-neumorphic p-8">
                <NeumorphicInput 
                  label="Disease Title" 
                  value={title} 
                  onChange={(e) => {
                    setTitle(e.target.value);
                    setHasUnsavedChanges(true);
                  }} 
                  placeholder="e.g. Pneumonia" 
                  required 
                  disabled={isPreviewing}
                />
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                  <NeumorphicInput 
                    label="Category" 
                    value={category} 
                    onChange={(e) => {
                      setCategory(e.target.value);
                      setHasUnsavedChanges(true);
                    }} 
                    placeholder="e.g. Infectious Diseases" 
                    disabled={isPreviewing}
                  />
                  <NeumorphicInput 
                    label="ICD Code" 
                    value={icd} 
                    onChange={(e) => {
                      setIcd(e.target.value);
                      setHasUnsavedChanges(true);
                    }} 
                    placeholder="e.g. J18.9" 
                    disabled={isPreviewing}
                  />
                </div>
              </div>

              {/* Sections */}
              <div className="space-y-5">
                {sections.map((s, index) => {
                  const content = getSectionContent(s.key);
                  const state = content.trim() ? 'Complete' : 'Not started';
                  return (
                    <section key={s.key} className="card-neumorphic p-6 border border-[var(--shadow-dark)]/40">
                      <div className="flex items-start gap-4 mb-4">
                        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[var(--accent-primary)] text-sm font-bold text-white">
                          {index + 1}
                        </div>
                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <h2 className="font-display text-xl font-bold text-[var(--text-primary)]">{s.label}</h2>
                            <span className={
                              content.trim()
                                ? 'rounded-full px-3 py-1 text-xs font-semibold bg-emerald-500/10 text-emerald-600'
                                : 'rounded-full px-3 py-1 text-xs font-semibold bg-slate-500/10 text-[var(--text-tertiary)]'
                            }>
                              {state}
                            </span>
                          </div>
                          <p className="mt-1 text-sm leading-relaxed text-[var(--text-secondary)]">{s.hint}</p>
                        </div>
                      </div>
                      <textarea
                        aria-label={`${s.label} content`}
                        placeholder={s.placeholder}
                        value={content}
                        onChange={(e) => {
                          if (!isPreviewing) {
                            setSectionContents(prev => ({ ...prev, [s.key]: e.target.value }));
                            setHasUnsavedChanges(true);
                          }
                        }}
                        className="input-neumorphic min-h-[150px] w-full resize-y rounded-xl px-4 py-3 text-sm leading-relaxed"
                        disabled={isPreviewing}
                      />
                      <div className="mt-2 flex justify-between text-xs text-[var(--text-tertiary)]">
                        <span>{content.trim() ? 'Nội dung đã được ghi nhận' : 'Bắt đầu nhập nội dung section'}</span>
                        <span>{content.length} ký tự</span>
                      </div>
                    </section>
                  );
                })}
              </div>
            </AnimatedSection>
          </main>

          {/* SIDEBAR (30%) */}
          <aside className="w-[30%] min-w-[300px] sticky top-28 self-start space-y-6">
            <AnimatedSection>
              {/* Draft Status */}
              <div className="card-neumorphic p-6">
                <h3 className="font-display text-base font-bold text-[var(--text-primary)] mb-4">Draft Details</h3>
                <div className="mb-5 rounded-xl bg-[var(--accent-primary)]/5 p-4">
                  <div className="mb-2 flex items-center justify-between text-xs font-semibold">
                    <span className="text-[var(--text-secondary)]">Writing progress</span>
                    <span className="text-[var(--accent-primary)]">{completedSections}/{sections.length} sections 路 {completionPct}%</span>
                  </div>
                  <div className="h-2 overflow-hidden rounded-full bg-[var(--shadow-dark)]/30">
                    <div className="h-full rounded-full bg-[var(--accent-primary)] transition-all" style={{ width: `${completionPct}%` }} />
                  </div>
                </div>
                <div className="space-y-3 text-sm">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-semibold text-[var(--text-primary)]">Status</p>
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                      status === 'DRAFT' || status === 'Draft' ? 'bg-blue-500/10 text-blue-600' : 
                      status === 'PENDING_REVIEW' ? 'bg-amber-500/10 text-amber-600' : 
                      status === 'APPROVED' ? 'bg-emerald-500/10 text-emerald-600' : 
                      status === 'REJECTED' ? 'bg-red-500/10 text-red-600' : 
                      'bg-gray-500/10 text-gray-600'
                    }`}>{status}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-semibold text-[var(--text-primary)]">Completion</p>
                    <span className="text-sm font-semibold text-[var(--accent-primary)]">{completionPct}%</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-semibold text-[var(--text-primary)]">Version</p>
                    <span className="text-sm text-[var(--text-secondary)]">v{version}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-semibold text-[var(--text-primary)]">Last Edited</p>
                    <span className="text-sm text-[var(--text-secondary)]">{lastEdited}</span>
                  </div>
                </div>
              </div>
            </AnimatedSection>
          </aside>
        </div>
      </div>

    {/* Preview Pane */}
    {isPreviewing && (
      <div className="mt-8 max-w-7xl mx-auto px-6">
        <div className="p-6 card-neumorphic bg-[var(--bg-primary)]">
          {previewLoading ? (
            <div className="text-center py-4">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary mx-auto"></div>
              <p className="mt-2 text-[var(--text-secondary)]">Loading preview...</p>
            </div>
          ) : previewError ? (
            <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-lg">
              <p className="text-[var(--text-error)]">{previewError}</p>
            </div>
          ) : previewData ? (
            <>
              <div className="flex items-center justify-between mb-4 border-b border-[var(--shadow-dark)] pb-4">
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)]">Preview (Saved Draft)</h2>
                {hasUnsavedChanges && (
                  <span className="text-xs text-amber-600 font-semibold">* Unsaved changes. Preview shows the last saved version.</span>
                )}
              </div>
              <div className="space-y-6">
                <div className="card-neumorphic p-6">
                  <h3 className="font-display text-3xl font-bold text-[var(--text-primary)] mb-2">{previewData.disease.name}</h3>
                  <div className="flex flex-wrap items-center gap-4 text-sm mt-3">
                    <span className="stat-pill">
                      <GraduationCap size={14} className="text-[var(--accent-primary)]" />
                      {previewData.currentVersion?.versionNumber ? `v${previewData.currentVersion.versionNumber}` : 'N/A'}
                    </span>
                    <span className="stat-pill">
                      <Clock size={14} className="text-[var(--accent-primary)]" />
                      {previewData.currentVersion?.updatedAt ? new Date(previewData.currentVersion.updatedAt).toLocaleDateString() : 'N/A'}
                    </span>
                    <span className="stat-pill">
                      <BookOpen size={14} className="text-[var(--accent-primary)]" />
                      {previewData.disease.categoryName || 'N/A'}
                    </span>
                  </div>
                </div>
                {previewData.sections.map((section: any, index: number) => (
                  <div key={section.id || index} className="card-neumorphic p-6">
                    <h3 className="font-display text-xl font-bold text-[var(--text-primary)] mb-3">{section.sectionTypeName || section.title || 'Section'}</h3>
                    <div className="prose prose-sm text-[var(--text-secondary)] leading-relaxed">
                      {section.content ? (
                        <>
                          {section.content.split('\n\n').map((paragraph: string, paraIndex: number) => (
                            <p key={paraIndex} className="mb-4">{paragraph}</p>
                          ))}
                        </>
                      ) : (
                        <p className="text-[var(--text-secondary)] italic">No content available</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <>
              <div className="flex items-center justify-between mb-4 border-b border-[var(--shadow-dark)] pb-4">
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)]">Preview (Unsaved Changes)</h2>
              </div>
              <div className="space-y-6">
                <div className="card-neumorphic p-6">
                  <h3 className="font-display text-3xl font-bold text-[var(--text-primary)] mb-2">{title || 'Untitled Disease'}</h3>
                  <div className="flex flex-wrap items-center gap-4 text-sm mt-3">
                    <span className="stat-pill">
                      <GraduationCap size={14} className="text-[var(--accent-primary)]" />
                      v{version}
                    </span>
                    <span className="stat-pill">
                      <Clock size={14} className="text-[var(--accent-primary)]" />
                      {lastEdited}
                    </span>
                    <span className="stat-pill">
                      <BookOpen size={14} className="text-[var(--accent-primary)]" />
                      {category || 'Uncategorized'}
                    </span>
                  </div>
                </div>
                {sections.map((s) => (
                  <div key={s.key} className="card-neumorphic p-6">
                    <h3 className="font-display text-xl font-bold text-[var(--text-primary)] mb-3">{s.label}</h3>
                    <div className="prose prose-sm text-[var(--text-secondary)] leading-relaxed">
                      {getSectionContent(s.key) ? (
                        <>
                          {getSectionContent(s.key).split('\n\n').map((paragraph: string, paraIndex: number) => (
                            <p key={paraIndex} className="mb-4">{paragraph}</p>
                          ))}
                        </>
                      ) : (
                        <p className="text-[var(--text-secondary)] italic">No content available</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    )}

    {/* Sticky Bottom Action Bar */}
    <div className="fixed bottom-0 left-0 right-0 z-40 px-6 py-3">
      <div className="max-w-4xl mx-auto">
        <div className="rounded-2xl p-4 bg-[var(--bg-primary)] shadow-[inset_4px_4px_10px_var(--shadow-dark),inset_-4px_-4px_10px_var(--shadow-light)] flex items-center justify-between">
          <div className="flex items-center gap-2 text-xs text-[var(--text-tertiary)]">
            <Clock size={14} />{autoSave}
          </div>
          <div className="flex items-center gap-3">
            <button 
              className="btn-neumorphic-secondary py-2 px-5 text-sm inline-flex items-center gap-2"
              onClick={handleTogglePreview}
            >
              <Eye size={14} />{isPreviewing ? 'Exit Preview' : 'Preview'}
            </button>
            <button 
              className="btn-neumorphic-secondary py-2 px-5 text-sm inline-flex items-center gap-2" 
              onClick={handleSaveDraft}
              disabled={isSaving || status === 'PENDING_REVIEW'}
            >
              <Save size={14} />Save Draft
            </button>
            <button
              className="btn-neumorphic-primary py-2 px-5 text-sm inline-flex items-center gap-2"
              onClick={handleSubmitForReview}
              disabled={isSubmitting || status === 'PENDING_REVIEW'}
            >
              <Send size={14} />Submit for Review
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  );
}

// Stub function to compile clean
const getSectionSources = (key: string): GeneratedSection | undefined => {
  return undefined;
};



