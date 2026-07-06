import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Plus, ChevronRight, FileText, Save, Eye, Send, Clock, TrendingUp, UploadCloud, Sparkles, BookOpen, Trash2, AlertCircle, GraduationCap, Search, Filter, SortAsc, SortDesc, CheckCircle2, Bookmark, LayoutDashboard, ChevronDown, X, Command, MessageSquare, Calendar, Users, Shield, Activity, Pill } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { getPermissions } from '../../hooks/usePermissions';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';
import Skeleton from '../../components/ui/Skeleton';
import SourceViewer from '../../components/disease/SourceViewer';
import type { DraftGenerationResponse, DraftCreationMetadata, GeneratedSection } from '../../types/diseaseDraft';

// Reusable Neumorphic Input Components
const NeumorphicInput = ({ label, value, onChange, placeholder, type = 'text', required = false, className = '', ...props }: {
  label: string; value: string; onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  placeholder: string; type?: string; required?: boolean; className?: string;
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
        <textarea value={value} onChange={onChange} placeholder={focused ? placeholder : ''}
          onFocus={() => setFocused(true)} onBlur={() => setFocused(value !== '' ? true : false)}
          className="input-neumorphic w-full min-h-[100px] resize-none pt-5 text-sm" {...props} />
      ) : (
        <input type={type} value={value} onChange={onChange} placeholder={focused ? placeholder : ''}
          onFocus={() => setFocused(true)} onBlur={() => setFocused(value !== '' ? true : false)}
          className="input-neumorphic w-full pt-5 text-sm" {...props} />
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
  const [status] = useState('Draft');
  const [version] = useState(1);
  const [lastEdited] = useState('Just now');
  const completionPct = 50;

  const sections = [
    { label: 'Definition', key: 'definition' },
    { label: 'Etiology', key: 'etiology' },
    { label: 'Symptoms', key: 'symptoms' },
    { label: 'Diagnosis', key: 'diagnosis' },
    { label: 'Treatment', key: 'treatment' },
    { label: 'Complications', key: 'complications' },
    { label: 'Prevention', key: 'prevention' },
    { label: 'References', key: 'references' },
  ];

  const getSectionContent = (key: string): string => {
    if (!draftResponse) return '';
    return ((draftResponse as any)[key] as GeneratedSection | undefined)?.content ?? '';
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
                      <> — file: <strong className="text-[var(--text-primary)]">{draftMetadata.originalFilename}</strong></>
                    )}
                  </span>
                </div>
              )}

              {/* Header Meta */}
              <div className="card-neumorphic p-8">
                <NeumorphicInput label="Disease Title" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="e.g. Pneumonia" required />
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                  <NeumorphicInput label="Category" value={category} onChange={(e) => setCategory(e.target.value)} placeholder="e.g. Infectious Diseases" />
                  <NeumorphicInput label="ICD Code" value={icd} onChange={(e) => setIcd(e.target.value)} placeholder="e.g. J18.9" />
                </div>
                <div className="mt-6">
                  <label className="block text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider mb-2">Tags</label>
                  <input type="text" placeholder="Add tags (e.g., Respiratory, Bacterial...)" className="input-neumorphic w-full" />
                </div>
              </div>

              {/* Sections */}
              {sections.map((s) => {
                const sectionData = getSectionSources(s.key);
                return (
                <div key={s.key} className={`card-neumorphic p-6 ${s.key === 'definition' || s.key === 'treatment' ? 'col-span-full' : ''}`}>
                  <div className="flex items-center justify-between mb-3">
                    <h2 className="font-display text-xl font-bold text-[var(--text-primary)]">{s.label}</h2>
                    <button className="text-xs text-[var(--text-tertiary)] hover:text-[var(--accent-primary)]">Collapse</button>
                  </div>
                  <textarea
                    placeholder={`Enter ${s.label.toLowerCase()}...`}
                    defaultValue={getSectionContent(s.key)}
                    className="input-neumorphic w-full min-h-[120px] resize-none text-sm leading-relaxed"
                  />
                  {sectionData?.sources && sectionData.sources.length > 0 && (
                    <SourceViewer sources={sectionData.sources} />
                  )}
                </div>
              )})}
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
                <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2 opacity-50 cursor-not-allowed mb-2"><FileText size={14} /> Attachments</button>
                <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2 opacity-50 cursor-not-allowed"><Sparkles size={14} /> AI Assistant</button>
              </div>

              {/* AI Card Placeholder */}
              <div className="card-neumorphic p-6">
                <div className="flex items-center gap-2 mb-4"><Sparkles size={16} className="text-[var(--accent-primary)]" /><h3 className="font-display text-base font-bold">AI Assistant</h3></div>
                <div className="space-y-2 text-sm">
                  <button disabled className="btn-neumorphic-secondary py-2 w-full text-xs opacity-50 cursor-not-allowed">Generate Definition</button>
                  <button disabled className="btn-neumorphic-secondary py-2 w-full text-sm opacity-50 cursor-not-allowed">Improve Etiology</button>
                  <button disabled className="btn-neumorphic-secondary py-2 w-full text-sm opacity-50 cursor-not-allowed">Simplify Symptoms</button>
                </div>
              </div>
            </AnimatedSection>
          </aside>
        </div>
      </div>

      {/* Sticky Bottom Action Bar */}
      <div className="fixed bottom-0 left-0 right-0 z-40 px-6 py-3">
        <div className="max-w-4xl mx-auto">
          <div className="rounded-2xl p-4 bg-[var(--bg-primary)] shadow-[inset_4px_4px_10px_var(--shadow-dark),inset_-4px_-4px_10px_var(--shadow-light)] flex items-center justify-between">
            <div className="flex items-center gap-2 text-xs text-[var(--text-tertiary)]"><Clock size={14} />{autoSave}</div>
            <div className="flex items-center gap-3">
              <button className="btn-neumorphic-secondary py-2 px-5 text-sm inline-flex items-center gap-2"><Eye size={14} />Preview</button>
              <button disabled className="btn-neumorphic-secondary py-2 px-5 text-sm opacity-50 cursor-not-allowed"><Save size={14} />Save Draft</button>
              <button className="btn-neumorphic-primary py-2 px-5 text-sm inline-flex items-center gap-2"><Send size={14} />Submit for Review</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
