import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Plus, ChevronRight, FileText, Save, Eye, Send, Clock, TrendingUp, UploadCloud, Sparkles, BookOpen, Trash2, AlertCircle, GraduationCap, Search, Filter, SortAsc, SortDesc, CheckCircle2, Bookmark, LayoutDashboard, ChevronDown, X, Command, MessageSquare, Calendar, Users, Shield, Activity, Pill } from 'lucide-react';
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
  const [focused, setFocused] = useState(false);
  const active = focused || value.length > 0;
  return (
    <div className={`relative ${className}`}>
      <label
        className={`absolute left-3 top-2 text-xs font-semibold uppercase tracking-wider transition-all duration-300 pointer-events-none ${active ? '-top-2 left-2 bg-[var(--bg-primary)] px-1 text-[var(--accent-primary)]' : 'text-[var(--text-tertiary)]'}`}
      >
        {label}{required && <span className="text-red-500">*</span>}
      </label>
      {type === 'textarea' ? (
        <textarea 
          value={value} 
          onChange={onChange} 
          placeholder={focused ? placeholder : ''}
          onFocus={() => setFocused(true)} 
          onBlur={() => setFocused(value !== '' ? true : false)}
          className="input-neumorphic w-full min-h-[100px] resize-none pt-5 text-sm" 
          disabled={disabled}
          {...props} 
        />
      ) : (
        <input 
          type={type} 
          value={value} 
          onChange={onChange} 
          placeholder={focused ? placeholder : ''}
          onFocus={() => setFocused(true)} 
          onBlur={() => setFocused(value !== '' ? true : false)}
          className="input-neumorphic w-full pt-5 text-sm" 
          disabled={disabled}
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
  const completionPct = 50;

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
      } catch (error) {
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
      } catch (error) {
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

      // Find categoryId by category name
      const categoryObj = categories.find(cat => cat.name === category);
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
        categoryId: categoryId,
        sections: sectionRequests,
      });
      // Response is DiseaseResponse, which includes disease id
      const savedDiseaseId = response.id;
      setDiseaseId(savedDiseaseId);
      // After saving, fetch the latest draft version to get versionId and updated data
      const versionResponse = await diseaseApi.getLatestDraftVersion(savedDiseaseId);
      const savedVersionId = versionResponse.id;
      setVersionId(savedVersionId);
      // Update state with the saved data (to reflect any backend changes like slug)
      setTitle(versionResponse.disease?.name || title);
      setCategory(versionResponse.disease?.categoryName || category);
      setIcd(versionResponse.disease?.icdCode || icd);
      setStatus(versionResponse.status || 'Draft');
      setVersion(versionResponse.versionNumber || 1);
      setLastEdited(versionResponse.updatedAt ? new Date(versionResponse.updatedAt).toLocaleString() : 'Just now');
      // Reload sections for this version
      const sectionsResponse = await diseaseSectionApi.getSectionsByVersion(savedVersionId);
      const sectionsData = sectionsResponse;
      const newSectionContents: Record<string, string> = {};
      sectionsData.forEach((section: any) => {
        // Map section type name to our section key
        const labelMap: Record<string, string> = {
          'Definition': 'definition',
          'Etiology': 'causes',
          'Symptoms': 'symptoms',
          'Diagnosis': 'diagnosis',
          'Treatment': 'treatment',
          'Complications': 'complications',
          'Prevention': 'prevention',
          'References': 'reference',
        };
        const key = labelMap[section.type] || '';
        if (key) {
          newSectionContents[key] = section.content || '';
        }
      });
      setSectionContents(newSectionContents);
      setHasUnsavedChanges(false);
      alert('Draft saved successfully!');
      // Optionally, we could navigate to edit page to show the saved version
      // navigate(`/disease/${savedDiseaseId}/edit`);
    } catch (error) {
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
    } catch (error) {
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
          icdCode: disease.icdCode,
          description: disease.description,
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

  const sections = [
    { label: 'Definition', key: 'definition' },
    { label: 'Etiology', key: 'causes' },
    { label: 'Symptoms', key: 'symptoms' },
    { label: 'Diagnosis', key: 'diagnosis' },
    { label: 'Treatment', key: 'treatment' },
    { label: 'Complications', key: 'complications' },
    { label: 'Prevention', key: 'prevention' },
    { label: 'References', key: 'reference' },
  ];

  const getSectionContent = (key: string): string => {
    if (!draftResponse) return sectionContents[key] ?? '';
    return ((draftResponse as any)[key] as GeneratedSection | undefined)?.content ?? sectionContents[key] ?? '';
  };

  const getSectionSources = (key: string): GeneratedSection | undefined => {
    if (!draftResponse) return undefined;
    return (draftResponse as any)[key] as GeneratedSection | undefined;
  };

  return (
    <div className="min-h-screen pt-28 pb-24 relative z-10">
      <div className="max-w-7xl mx-auto px-6">
        <div className="flex gap-8 items-start">
          {/* EDITOR (70%) */}
          <main className="flex-1 max-w-[70ch] min-w-0 space-y-8">
            <AnimatedSection>
              {draftMetadata && (
                <div className="card-neumorphic p-4 flex items-center gap-3 text-sm">
                  <Sparkles size={16} className="text-[var(--accent-primary)] shrink-0" />
                  <span className="text-[var(--text-secondary)]">
                    Draft generated from <strong className="text-[var(--text-primary)]">{draftMetadata.sourceLabel}</strong>
                    {draftMetadata.method === 'upload' && draftMetadata.originalFilename && (
                      <> â€?file: <strong className="text-[var(--text-primary)]">{draftMetadata.originalFilename}</strong> </>
                    )}
                  </span>
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
                <div className="mt-6">
                  <label className="block text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider mb-2">Tags</label>
                  <input 
                    type="text" 
                    placeholder="Add tags (e.g., Respiratory, Bacterial...)" 
                    className="input-neumorphic w-full" 
                    onChange={(e) => {
                      setHasUnsavedChanges(true);
                    }} 
                    disabled={isPreviewing}
                  />
                </div>
              </div>

              {/* Sections */}
              {sections.map((s) => {
                const sectionData = getSectionSources(s.key);
                return (
                  <div 
                    key={s.key} 
                    className={`card-neumorphic p-6 ${s.key === 'definition' || s.key === 'treatment' ? 'col-span-full' : ''}`}
                  >
                    <div className="flex items-center justify-between mb-3">
                      <h2 className="font-display text-xl font-bold text-[var(--text-primary)]">{s.label}</h2>
                      <button className="text-xs text-[var(--text-tertiary)] hover:text-[var(--accent-primary)]">Collapse</button>
                    </div>
                    <textarea
                      placeholder={`Enter ${s.label.toLowerCase()}...`}
                      value={getSectionContent(s.key)}
                      onChange={(e) => {
                        if (!isPreviewing) {
                          setSectionContents(prev => ({
                            ...prev,
                            [s.key]: e.target.value
                          }));
                          setHasUnsavedChanges(true);
                        }
                      }}
                      className="input-neumorphic w-full min-h-[120px] resize-none text-sm leading-relaxed"
                      disabled={isPreviewing}
                    />
                    {sectionData?.sources && sectionData.sources.length > 0 && (
                      <SourceViewer sources={sectionData.sources} />
                    )}
                  </div>
                );
              })}
            </AnimatedSection>
          </main>

          {/* SIDEBAR (30%) */}
          <aside className="w-[30%] min-w-[300px] sticky top-28 self-start space-y-6">
            <AnimatedSection>
              {/* Draft Status */}
              <div className="card-neumorphic p-6">
                <h3 className="font-display text-base font-bold text-[var(--text-primary)] mb-4">Draft Details</h3>
                <div className="space-y-3 text-sm">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-semibold text-[var(--text-primary)]">Status</p>
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${status === 'Draft' ? 'bg-blue-500/10 text-blue-600' : status === 'Pending Review' ? 'bg-amber-500/10 text-amber-600' : 'bg-red-500/10 text-red-600'}`}>{status}</span>
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
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-semibold text-[var(--text-primary)]">Reviewer Status</p>
                    <span className="text-sm text-[var(--text-secondary)]">None</span>
                  </div>
                </div>
                <button 
                  disabled={isPreviewing || isSaving}
                  className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2 opacity-50 cursor-not-allowed mb-2"
                >
                  <FileText size={14} /> Attachments
                </button>
                <button 
                  disabled={isPreviewing || isSaving}
                  className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2 opacity-50 cursor-not-allowed"
                >
                  <Sparkles size={14} /> AI Assistant
                </button>
              </div>

              {/* AI Card Placeholder */}
              <div className="card-neumorphic p-6">
                <div className="flex items-center gap-2 mb-4">
                  <Sparkles size={16} className="text-[var(--accent-primary)]" />
                  <h3 className="font-display text-base font-bold">AI Assistant</h3>
                </div>
                <div className="space-y-2 text-sm">
                  <button 
                    disabled={isPreviewing || isSaving}
                    className="btn-neumorphic-secondary py-2 w-full text-sm opacity-50 cursor-not-allowed"
                  >
                    Generate Definition
                  </button>
                  <button 
                    disabled={isPreviewing || isSaving}
                    className="btn-neumorphic-secondary py-2 w-full text-sm opacity-50 cursor-not-allowed"
                  >
                    Improve Etiology
                  </button>
                  <button 
                    disabled={isPreviewing || isSaving}
                    className="btn-neumorphic-secondary py-2 w-full text-sm opacity-50 cursor-not-allowed"
                  >
                    Simplify Symptoms
                  </button>
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

