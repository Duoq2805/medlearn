import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, Plus, ChevronRight, FileText, Save, Eye, Send, Clock, TrendingUp, UploadCloud, Sparkles, BookOpen, Trash2, AlertCircle, GraduationCap, Search, Filter, SortAsc, SortDesc, CheckCircle2, Bookmark, LayoutDashboard, ChevronDown, X, Command, MessageSquare, Calendar, Users, Shield, Activity, Pill } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { getPermissions } from '../../hooks/usePermissions';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';
import Skeleton from '../../components/ui/Skeleton';
import SourceViewer from '../../components/disease/SourceViewer';
import type { DraftGenerationResponse, DraftCreationMetadata, GeneratedSection } from '../../types/diseaseDraft';
import { diseaseApi } from '../../api/disease';
import { createDiseaseDraft } from '../../api/diseaseDraft';
import { diseaseSectionApi } from '../../api/diseaseSection';

// Reusable Neumorphic Input Components
const NeumorphicInput = ({ label, value, onChange, placeholder, type = 'text', required = false, className = '', disabled = false, ...props }: {
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
          disabled={disabled}
          className="input-neumorphic w-full min-h-[100px] resize-none rounded-xl px-4 py-3 text-sm"
          {...props}
        />
      ) : (
        <input
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          disabled={disabled}
          className="input-neumorphic w-full rounded-xl px-4 py-3 text-sm"
          {...props}
        />
      )}
    </div>
  );
};

export default function CreateDiseasePage() {
  const { user } = useAuth();
  const location = useLocation();
  const draftResponse = (location.state as { draftResponse?: DraftGenerationResponse; draftMetadata?: DraftCreationMetadata })?.draftResponse;
  const draftMetadata = (location.state as { draftResponse?: DraftGenerationResponse; draftMetadata?: DraftCreationMetadata })?.draftMetadata;

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
  const [isSaving, setIsSaving] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isPreviewing, setIsPreviewing] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [diseaseId, setDiseaseId] = useState<number | null>(null);
  const [versionId, setVersionId] = useState<number | null>(null);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [previewData, setPreviewData] = useState<any>(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewError, setPreviewError] = useState<string | null>(null);

  // Track unsaved changes
  useEffect(() => {
    if (diseaseId !== null) {
      // We have a saved disease, track changes from initial saved state
      // We'll compare with initial values stored in refs
      // For simplicity, we'll set hasUnsavedChanges to true whenever any input changes
      // and reset to false after save.
      // We'll rely on the setters to set hasUnsavedChanges.
    }
  }, [diseaseId]);

  useEffect(() => {
    if (draftResponse) {
      const sections = [
         { key: 'definition', label: 'Definition' },
         { key: 'causes', label: 'Etiology' },
         { key: 'symptoms', label: 'Symptoms' },
         { key: 'diagnosis', label: 'Diagnosis' },
         { key: 'treatment', label: 'Treatment' },
         { key: 'complications', label: 'Complications' },
         { key: 'prevention', label: 'Prevention' },
         { key: 'reference', label: 'References' }
       ];
      const newContents: Record<string, string> = {};
      sections.forEach(sectionInfo => {
        const section = (draftResponse as any)[sectionInfo.key];
        if (section && typeof section === 'object' && 'content' in section) {
          newContents[sectionInfo.key] = section.content;
        } else {
          newContents[sectionInfo.key] = '';
        }
      });
      setSectionContents(newContents);
      // If we have a draftResponse from location state, we might not have diseaseId yet
      // We'll set diseaseId and versionId from the draftResponse if available
      // Assuming draftResponse includes id and version? Not sure.
      // We'll leave it as is; the user can save to get actual IDs.
    }
  }, [draftResponse]);

  useEffect(() => {
    const fetchCategories = async () => {
      setCategoriesLoading(true);
      try {
        const response = await diseaseApi.getCategories();
        setCategories(response);
      } catch (error: any) {
        console.error('Failed to fetch categories', error);
      } finally {
        setCategoriesLoading(false);
      }
    };

    fetchCategories();
  }, []);

  useEffect(() => {
    const fetchSectionTypes = async () => {
      setSectionTypesLoading(true);
      try {
        const response = await diseaseSectionApi.getAllSectionTypes();
        const sectionTypesMap: Record<string, number> = {};
        response.forEach((st: { id: number; name: string }) => {
          sectionTypesMap[st.name] = st.id;
        });
        setSectionTypesMap(sectionTypesMap);
      } catch (error: any) {
        console.error('Failed to fetch section types', error);
      } finally {
        setSectionTypesLoading(false);
      }
    };

    fetchSectionTypes();
  }, []);

  // Handle saving draft (just create/update draft, do not submit)
  const handleSaveDraft = async () => {
    if (!title.trim()) {
      alert('Please enter a disease title');
      return;
    }

    setIsSaving(true);
    setSaveError(null);
    try {
      // Generate slug from title with fallback to prevent empty slug
      let slug = title
        .toLowerCase()
        .replace(/[^\w\s-]/g, '') // remove non-word, non-space, non-hyphen
        .replace(/[\s_-]+/g, '-') // replace spaces and underscores with hyphen
        .trim();

      // Fallback to prevent empty slug (e.g., when title contains only special characters)
      if (!slug) {
        slug = 'draft-' + Math.random().toString(36).substr(2, 9);
      }

      // Find categoryId by category name (case-insensitive)
      const categoryObj = categories.find(cat => cat.name === category || cat.name.toLowerCase() === category.toLowerCase());
      const categoryId = categoryObj ? categoryObj.id : null;

      // Prepare section requests
      const sections = [
        { key: 'definition', label: 'Definition' },
        { key: 'causes', label: 'Etiology' },
        { key: 'symptoms', label: 'Symptoms' },
        { key: 'diagnosis', label: 'Diagnosis' },
        { key: 'treatment', label: 'Treatment' },
        { key: 'complications', label: 'Complications' },
        { key: 'prevention', label: 'Prevention' },
        { key: 'reference', label: 'References' }
      ];
      const sectionRequests = sections.map((s) => {
        const sectionTypeId = sectionTypesMap[s.key];
        if (!sectionTypeId) {
          console.warn(`Section type not found for key: ${s.key}`);
          // We'll still include it, but the backend will validate
        }
        return {
          sectionTypeId: sectionTypeId || null, // Let backend handle validation if null
          title: s.label, // Use the label as the title
          content: sectionContents[s.key] || '',
          orderIndex: sections.findIndex(sec => sec.key === s.key) // Use index as order
        };
      });

      const response = await createDiseaseDraft({
        name: title,
        slug: slug,
        categoryId: categoryId || undefined,
        sections: sectionRequests,
      });
      // Response is DiseaseResponse, which includes disease id
      const savedDiseaseId = response.id;
      setDiseaseId(savedDiseaseId);

      if (categoryId && savedDiseaseId) {
        try {
          await diseaseApi.assignCategory(savedDiseaseId, categoryId);
        } catch (cErr) {
          console.warn('Failed to assign category', cErr);
        }
      }

      // After saving, fetch the latest draft version to get versionId and updated data
      const versionResponse = await diseaseApi.getLatestDraftVersion(savedDiseaseId);
      const savedVersionId = versionResponse.id;
      setVersionId(savedVersionId);
      // Update state with the saved data (to reflect any backend changes like slug)
      setTitle(title);
      setCategory(category);
      setIcd(icd);
      setStatus(versionResponse.status || 'Draft');
      setVersion(versionResponse.versionNumber || 1);
      setLastEdited(versionResponse.updatedAt ? new Date(versionResponse.updatedAt).toLocaleString() : 'Just now');
      // Reload sections for this version
      const sectionsResponse = await diseaseSectionApi.getSectionsByVersion(savedVersionId);
      const sectionsData = sectionsResponse;
      const newSectionContents: Record<string, string> = {};
      sectionsData.forEach((section: any) => {
        let key = '';
        const text = [section.type, section.sectionTypeName, section.title].filter(Boolean).join(' ').toLowerCase();
        if (text.includes('definit')) key = 'definition';
        else if (text.includes('etiolog') || text.includes('cause')) key = 'causes';
        else if (text.includes('symptom')) key = 'symptoms';
        else if (text.includes('diagnos')) key = 'diagnosis';
        else if (text.includes('treat')) key = 'treatment';
        else if (text.includes('complicat')) key = 'complications';
        else if (text.includes('prevent')) key = 'prevention';
        else if (text.includes('referenc')) key = 'reference';

        if (key) {
          newSectionContents[key] = section.content || '';
        }
      });
      setSectionContents(newSectionContents);
      setHasUnsavedChanges(false);
      alert('Draft saved successfully!');
      // Optionally, we could navigate to edit page to show the saved version
      // navigate(`/disease/${savedDiseaseId}/edit`);
    } catch (error: any) {
      console.error('Failed to save draft', error);
      let errorMessage = 'Failed to save draft. Please try again.';
      if (error.response && error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
      } else if (error.response && error.response.data && error.response.data.errors) {
        const validationErrors = error.response.data.errors;
        if (validationErrors && Array.isArray(validationErrors)) {
          errorMessage = validationErrors.map(err => err.defaultMessage || err.message).join('\n');
        } else if (typeof validationErrors === 'object') {
          errorMessage = Object.values(validationErrors).join('\n');
        }
      }
      setSaveError(errorMessage);
      alert(errorMessage);
    } finally {
      setIsSaving(false);
    }
  };

  // Handle submitting draft for review
  const handleSubmitForReview = async () => {
    if (!title.trim()) {
      alert('Please enter a disease title');
      return;
    }

    setIsSubmitting(true);
    setSubmitError(null);
    try {
      // Generate slug from title with fallback to prevent empty slug
      let slug = title
        .toLowerCase()
        .replace(/[^\w\s-]/g, '') // remove non-word, non-space, non-hyphen
        .replace(/[\s_-]+/g, '-') // replace spaces and underscores with hyphen
        .trim();

      // Fallback to prevent empty slug (e.g., when title contains only special characters)
      if (!slug) {
        slug = 'draft-' + Math.random().toString(36).substr(2, 9);
      }

      // Find categoryId by category name
      const categoryObj = categories.find(cat => cat.name === category);
      const categoryId = categoryObj ? categoryObj.id : null;

      const sections = [
         { key: 'definition', label: 'Definition' },
         { key: 'causes', label: 'Etiology' },
         { key: 'symptoms', label: 'Symptoms' },
         { key: 'diagnosis', label: 'Diagnosis' },
         { key: 'treatment', label: 'Treatment' },
         { key: 'complications', label: 'Complications' },
         { key: 'prevention', label: 'Prevention' },
         { key: 'reference', label: 'References' }
       ];
      const sectionRequests = sections.map((s) => {
        const sectionTypeId = sectionTypesMap[s.key];
        if (!sectionTypeId) {
          console.warn(`Section type not found for key: ${s.key}`);
        }
        return {
          sectionTypeId: sectionTypeId || null,
          title: s.label,
          content: sectionContents[s.key] || '',
          orderIndex: sections.findIndex(sec => sec.key === s.key)
        };
      });

      // Step 1: Create the draft
      const createResponse = await createDiseaseDraft({
        name: title,
        slug: slug,
        categoryId: categoryId,
        sections: sectionRequests,
      });
      const diseaseId = createResponse.id;

      // Step 2: Get the latest draft version for this disease
      const versionResponse = await diseaseApi.getLatestDraftVersion(diseaseId);
      const versionId = versionResponse.id;

      // Step 3: Submit the version for review
      await diseaseApi.submitVersionForReview(versionId);

      alert('Draft submitted for review successfully!');
      // Optionally, redirect to disease detail or my drafts
      // navigate(`/disease/${diseaseId}`);
    } catch (error: any) {
      console.error('Failed to submit draft for review', error);
      let errorMessage = 'Failed to submit draft for review. Please try again.';
      if (error.response && error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
      } else if (error.response && error.response.data && error.response.data.errors) {
        const validationErrors = error.response.data.errors;
        if (validationErrors && Array.isArray(validationErrors)) {
          errorMessage = validationErrors.map(err => err.defaultMessage || err.message).join('\n');
        } else if (typeof validationErrors === 'object') {
          errorMessage = Object.values(validationErrors).join('\n');
        }
      }
      setSubmitError(errorMessage);
      alert(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle preview toggle
  const handleTogglePreview = () => {
    setIsPreviewing(!isPreviewing);
    if (isPreviewing) {
      // Load preview data when opening preview
      loadPreviewData();
    } else {
      // Clear preview data when closing preview
      setPreviewData(null);
      setPreviewLoading(false);
      setPreviewError(null);
    }
  };

  const loadPreviewData = async () => {
    if (diseaseId === null) {
      // No saved disease yet, preview will show current state (unsaved)
      setPreviewData(null);
      setPreviewLoading(false);
      setPreviewError(null);
      return;
    }
    setPreviewLoading(true);
    setPreviewError(null);
    try {
      // Fetch latest draft version for this disease
      const versionResponse = await diseaseApi.getLatestDraftVersion(diseaseId);
      const version = versionResponse;
      // Fetch disease metadata separately to get name, category, etc.
      const diseaseResponse = await diseaseApi.fetchDisease(diseaseId.toString());
      const disease = diseaseResponse;
      // Fetch sections for this version
      const sectionsResponse = await diseaseSectionApi.getSectionsByVersion(version.id);
      const sections = sectionsResponse;
      // Build preview data object
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
          sectionTypeName: section.type,
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

  const defaultCategoryList = ['Infectious Diseases', 'Cardiovascular', 'Respiratory', 'Neurology', 'Endocrine', 'Gastroenterology', 'Nephrology', 'Dermatology', 'Oncology', 'Pediatrics', 'Psychiatry', 'Orthopedics', 'Other'];
  const categoryOptions = categories.length > 0 ? categories.map(c => c.name) : defaultCategoryList;

  const sections = [
    { label: 'Definition', key: 'definition', hint: 'Giải thích bệnh là gì, cơ chế chính và phạm vi ảnh hưởng.', placeholder: 'Mô tả định nghĩa, bản chất và đặc điểm cốt lõi của bệnh...' },
    { label: 'Etiology', key: 'causes', hint: 'Nêu nguyên nhân, yếu tố nguy cơ và tác nhân liên quan.', placeholder: 'Liệt kê nguyên nhân, tác nhân và yếu tố nguy cơ...' },
    { label: 'Symptoms', key: 'symptoms', hint: 'Mô tả triệu chứng điển hình, thời điểm khởi phát và mức độ.', placeholder: 'Mô tả triệu chứng thường gặp, dấu hiệu cảnh báo...' },
    { label: 'Diagnosis', key: 'diagnosis', hint: 'Trình bày tiêu chuẩn, xét nghiệm và chẩn đoán phân biệt.', placeholder: 'Nêu quy trình chẩn đoán, xét nghiệm và chẩn đoán phân biệt...' },
    { label: 'Treatment', key: 'treatment', hint: 'Ghi hướng điều trị, thuốc, liều dùng và theo dõi.', placeholder: 'Trình bày mục tiêu điều trị, phương pháp và theo dõi...' },
    { label: 'Complications', key: 'complications', hint: 'Nêu biến chứng có thể xảy ra và dấu hiệu cần xử trí.', placeholder: 'Mô tả biến chứng, mức độ nguy hiểm và xử trí...' },
    { label: 'Prevention', key: 'prevention', hint: 'Hướng dẫn phòng bệnh, giảm nguy cơ và tái phát.', placeholder: 'Nêu biện pháp phòng ngừa, sàng lọc và thay đổi lối sống...' },
    { label: 'References', key: 'reference', hint: 'Ghi nguồn tài liệu y khoa dùng để xây dựng nội dung.', placeholder: 'Nhập tài liệu tham khảo, DOI hoặc URL nguồn...' },
  ];

  const getSectionContent = (key: string): string => {
    if (!draftResponse) return sectionContents[key] ?? '';
    return ((draftResponse as any)[key] as GeneratedSection | undefined)?.content ?? sectionContents[key] ?? '';
  };

  const getSectionSources = (key: string): GeneratedSection | undefined => {
    if (!draftResponse) return undefined;
    return (draftResponse as any)[key] as GeneratedSection | undefined;
  };

  const completedSections = sections.filter(({ key }) => (getSectionContent(key) ?? '').trim().length > 0).length;
  const completionPct = Math.round((completedSections / sections.length) * 100);

  return (
    <div className="min-h-screen pt-28 pb-24 relative z-10">
      <div className="max-w-7xl mx-auto px-6">
        <div className="flex gap-8 items-start">
          {/* EDITOR (70%) */}
          <main className="flex-1 max-w-[70ch] min-w-0 space-y-8">
            <AnimatedSection>
              <div className="mb-4">
                <Link to="/explorer" className="inline-flex items-center gap-2 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)]">
                  <ArrowLeft size={16} /> Back to Diseases
                </Link>
              </div>

              {draftMetadata && (
                <div className="card-neumorphic p-4 mb-6 flex items-center gap-3 text-sm">
                  <Sparkles size={16} className="text-[var(--accent-primary)] shrink-0" />
                  <span className="text-[var(--text-secondary)]">
                    Draft generated from <strong className="text-[var(--text-primary)]">{draftMetadata.sourceLabel}</strong>
                    {draftMetadata.method === 'upload' && draftMetadata.originalFilename && (
                      <> • file: <strong className="text-[var(--text-primary)]">{draftMetadata.originalFilename}</strong> </>
                    )}
                  </span>
                </div>
              )}

              {/* Header Meta */}
              <div className="card-neumorphic p-8 mb-8">
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
                  <div>
                    <label className="block text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider mb-2">Category</label>
                    <select
                      value={category}
                      onChange={(e) => {
                        setCategory(e.target.value);
                        setHasUnsavedChanges(true);
                      }}
                      disabled={isPreviewing}
                      className="input-neumorphic w-full text-sm py-3 px-4 rounded-xl"
                    >
                      <option value="">Select Category...</option>
                      {categoryOptions.map((catName) => (
                        <option key={catName} value={catName}>
                          {catName}
                        </option>
                      ))}
                    </select>
                  </div>
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
                  const sectionData = getSectionSources(s.key);
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
                            setSectionContents(prev => ({
                              ...prev,
                              [s.key]: e.target.value
                            }));
                            setHasUnsavedChanges(true);
                          }
                        }}
                        className="input-neumorphic min-h-[150px] w-full resize-y rounded-xl px-4 py-3 text-sm leading-relaxed"
                        disabled={isPreviewing}
                      />
                      {sectionData?.sources && sectionData.sources.length > 0 && (
                        <SourceViewer sources={sectionData.sources} />
                      )}
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
                    <span className="text-[var(--accent-primary)]">{completedSections}/{sections.length} sections • {completionPct}%</span>
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
        <div className="mt-6 p-4 border rounded-lg bg-[var(--bg-primary)] shadow-[inset_4px_4px_10px_var(--shadow-dark),inset_-4px_-4px_10px_var(--shadow-light)]">
          {previewLoading ? (
            <div className="text-center py-4">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
              <p className="mt-2 text-[var(--text-secondary)]">Loading preview...</p>
            </div>
          ) : previewError ? (
            <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-lg">
              <p className="text-[var(--text-error)]">{previewError}</p>
            </div>
          ) : previewData ? (
            <>
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">Preview (Saved Draft)</h2>
                {hasUnsavedChanges && (
                  <span className="text-xs text-[var(--text-warning)]">* You have unsaved changes. Preview shows the last saved version.</span>
                )}
              </div>
              <div className="space-y-6">
                <div className="card-neumorphic p-4">
                  <h3 className="font-display text-base font-bold text-[var(--text-primary)] mb-2">{previewData.disease.name}</h3>
                  <p className="text-sm text-[var(--text-secondary)]">{previewData.disease.description || ''}
                  </p>
                  <div className="flex flex-wrap items-center gap-4 text-sm">
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
                  {previewData.sections.map((section: any, index: number) => (
                    <div key={section.id || index} className="card-neumorphic p-4">
                      <h3 className="font-display text-lg font-bold text-[var(--text-primary)] mb-2">{section.sectionTypeName || section.title || 'Section'}</h3>
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
              </div>
            </>
          ) : (
            <>
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">Preview (Unsaved Changes)</h2>
              </div>
              <div className="space-y-6">
                <div className="card-neumorphic p-4">
                  <h3 className="font-display text-base font-bold text-[var(--text-primary)] mb-2">{title}</h3>
                  <p className="text-sm text-[var(--text-secondary)]">{icd}</p>
                  <div className="flex flex-wrap items-center gap-4 text-sm">
                    <span className="stat-pill">
                      <GraduationCap size={14} className="text-[var(--accent-primary)]" />
                      {version ? `v${version}` : 'N/A'}
                    </span>
                    <span className="stat-pill">
                      <Clock size={14} className="text-[var(--accent-primary)]" />
                      {lastEdited !== 'Just now' ? lastEdited : 'N/A'}
                    </span>
                    <span className="stat-pill">
                      <BookOpen size={14} className="text-[var(--accent-primary)]" />
                      {category || 'N/A'}
                    </span>
                  </div>
                  {sections.map((s) => (
                    <div key={s.key} className="card-neumorphic p-4">
                      <h3 className="font-display text-lg font-bold text-[var(--text-primary)] mb-2">{s.label}</h3>
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
              </div>
            </>
          )}
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
              {isPreviewing ? (
                <>
                  <button 
                    className="btn-neumorphic-secondary py-2 px-5 text-sm inline-flex items-center gap-2"
                    onClick={handleTogglePreview}
                  >
                    <Eye size={14} />Exit Preview
                  </button>
                  <button 
                    className="btn-neumorphic-secondary py-2 px-5 text-sm inline-flex items-center gap-2" 
                    onClick={handleSaveDraft}
                    disabled={isSaving}
                  >
                    <Save size={14} />Save Draft
                  </button>
                  {!isSaving && !isSubmitting && (
                    <button 
                      className="btn-neumorphic-primary py-2 px-5 text-sm inline-flex items-center gap-2"
                      onClick={handleSubmitForReview}
                    >
                      <Send size={14} />Submit for Review
                    </button>
                  )}
                </>
              ) : (
                <>
                  <button 
                    className="btn-neumorphic-secondary py-2 px-5 text-sm inline-flex items-center gap-2"
                    onClick={handleTogglePreview}
                  >
                    <Eye size={14} />Preview
                  </button>
                  <button 
                    className="btn-neumorphic-secondary py-2 px-5 text-sm inline-flex items-center gap-2" 
                    onClick={handleSaveDraft}
                    disabled={isSaving}
                  >
                    <Save size={14} />Save Draft
                  </button>
                  <button 
                    className="btn-neumorphic-primary py-2 px-5 text-sm inline-flex items-center gap-2"
                    onClick={handleSubmitForReview}
                    disabled={isSubmitting}
                  >
                    <Send size={14} />Submit for Review
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

