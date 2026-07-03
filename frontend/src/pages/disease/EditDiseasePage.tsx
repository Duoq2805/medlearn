import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, ChevronRight, FileText, Save, Eye, Send, Clock, TrendingUp, UploadCloud, Sparkles, BookOpen, Trash2, AlertCircle, GraduationCap, Search, Filter, SortAsc, SortDesc, CheckCircle2, Bookmark, LayoutDashboard, ChevronDown, X, Command, MessageSquare, Calendar, Users, Shield, Activity, Pill, Stethoscope, Brain } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { getPermissions } from '../../hooks/usePermissions';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';
import Skeleton from '../../components/ui/Skeleton';

// Reusable Neumorphic Input Components
const FloatingInput = ({ label, value, onChange, placeholder, type = 'text', required = false, className = '' }: {
  label: string; value: string; onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  placeholder: string; type?: string; required?: boolean; className?: string;
}) => {
  const [focused, setFocused] = useState(false);
  const active = focused || value.length > 0;
  return (
    <div className={`relative ${className}`}>
      <label className={`absolute left-3 top-2 text-xs font-semibold uppercase tracking-wider transition-all pointer-events-none ${active ? '-top-2 left-2 bg-[var(--bg-primary)] px-1 text-[var(--accent-primary)]' : 'text-[var(--text-tertiary)]'}`}>
        {label}{required && <span className="text-red-500">*</span>}
      </label>
      {type === 'textarea' ? (
        <textarea value={value} onChange={onChange} placeholder={focused ? placeholder : ''}
          onFocus={() => setFocused(true)} onBlur={() => setFocused(false)}
          className="input-neumorphic w-full min-h-[100px] resize-none pt-5 text-sm" />
      ) : (
        <input type={type} value={value} onChange={onChange} placeholder={focused ? placeholder : ''}
          onFocus={() => setFocused(true)} onBlur={() => setFocused(false)}
          className="input-neumorphic w-full pt-5 text-sm" {...props} />
      )}
    </div>
  );
};

export default function CreateDiseasePage() {
  const { user } = useAuth();
  const [autoSave, setAutoSave] = useState('All changes saved');
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('');
  const [icd, setIcd] = useState('');
  const [status] = useState('Draft');
  const [version] = useState(1);
  const [lastEdited] = useState('Just now');
  const [reviewerComments] = useState('None');

  const editorRef = useRef<HTMLDivElement>(null);
  const sidebarRef = useRef<HTMLDivElement>(null);
  const bottomBarRef = useRef<HTMLDivElement>(null);

  const sections = [
    { label: 'Definition', key: 'def' },
    { label: 'Etiology', key: 'et' },
    { label: 'Symptoms', key: 'sym' },
    { label: 'Diagnosis', key: 'dx' },
    { label: 'Treatment', key: 'tx' },
    { label: 'Complications', key: 'comp' },
    { label: 'Prevention', key: 'prev' },
    { label: 'References', key: 'ref' },
  ];

  const completionPercentage = 50; // Placeholder

  return (
    <div className="min-h-screen pt-28 pb-24 relative z-10">
      <div className="max-w-7xl mx-auto px-6">
        <div className="flex gap-8 items-start">
          {/* EDITOR */}
          <main ref={editorRef} className="flex-1 max-w-[70ch] min-w-0 space-y-8">
            <AnimatedSection>
              <button onClick={() => navigate('/drafts')} className="flex items-center gap-2 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] mb-4">
                <ArrowLeft size={16} /> Back to Drafts
              </button>
              <div className="card-neumorphic p-8 mb-6">
                <FloatingInput label="Disease Title" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="e.g. Pneumonia" required />
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                  <FloatingInput label="Category" value={category} onChange={(e) => setCategory(e.target.value)} placeholder="e.g. Infectious Diseases" />
                  <FloatingInput label="ICD Code" value={icd} onChange={(e) => setIcd(e.target.value)} placeholder="e.g. J18.9" />
                </div>
                <div className="mt-6">
                  <label className="block text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider mb-2">Tags</label>
                  <input type="text" placeholder="Add tags (e.g., Respiratory, Bacterial...)" className="input-neumorphic w-full" />
                </div>
              </div>

              {/* Sections */}
              {sections.map((s) => (
                <div key={s.key} className={`card-neumorphic p-6 ${s.key === 'definition' || s.key === 'treatment' ? 'col-span-full' : ''}`}>
                  <div className="flex items-center justify-between mb-3">
                    <h2 className="font-display text-xl font-bold text-[var(--text-primary)]">{s.label}</h2>
                    <button className="text-xs text-[var(--text-tertiary)] hover:text-[var(--accent-primary)]">Collapse</button>
                  </div>
                  <textarea placeholder={`Enter ${s.label.toLowerCase()}...`} className="input-neumorphic w-full min-h-[120px] resize-none text-sm leading-relaxed" />
                </div>
              ))}
            </AnimatedSection>
          </main>

          {/* SIDEBAR */}
          <aside ref={sidebarRef} className="w-[30%] min-w-[260px] sticky top-28 space-y-6">
            <AnimatedSection>
              {/* Draft Status */}
              <div className="card-neumorphic p-6">
                <h3 className="font-display text-base font-bold text-[var(--text-primary)] mb-4">Draft Status</h3>
                <div className="space-y-3 text-sm">
                  <div className="flex justify-between"><span className="text-[var(--text-secondary)]">Status</span><span className={`px-2 py-0.5 rounded text-xs font-medium ${
                    status === 'Draft' ? 'bg-blue-500/10 text-blue-600' :
                    status === 'Pending Review' ? 'bg-amber-500/10 text-amber-600' :
                    'bg-red-500/10 text-red-600'
                  }`}>{status}</span></div>
                  <div className="flex justify-between"><span className="text-[var(--text-secondary)]">Completion</span><span className="font-semibold text-[var(--accent-primary)]">{completionPercentage}%</span></div>
                  <div className="flex justify-between"><span className="text-[var(--text-secondary)]">Version</span><span>v{version}</span></div>
                  <div className="flex justify-between"><span className="text-[var(--text-secondary)]">Last Edited</span><span>{lastEdited}</span></div>
                  <div className="flex justify-between"><span className="text-[var(--text-secondary)]">Reviewer Status</span><span>{reviewerComments}</span></div>
                </div>
                <button disabled className="btn-neumorphic-secondary py-2 w-full text-sm flex items-center justify-center gap-2 mt-4 opacity-50 cursor-not-allowed"><FileText size={14} /> Attachments</button>
                <button disabled className="btn-neumorphic-secondary py-2 w-full text-sm flex items-center justify-center gap-2 mt-2 opacity-50 cursor-not-allowed"><Sparkles size={14} /> AI Assistant</button>
              </div>

              {/* AI Card Placeholder */}
              <div className="card-neumorphic p-6">
                <div className="flex items-center gap-2 mb-3"><Sparkles size={16} className="text-[var(--accent-primary)]" /><h3 className="font-display text-base font-bold">AI Assistant</h3></div>
                <div className="space-y-2 text-sm">
                  {[...Array(3)].map((_, i) => (
                    <button key={i} disabled className="btn-neumorphic-secondary py-2 w-full text-xs opacity-50 cursor-not-allowed">Generate Content {i+1}</button>
                  ))}
                </div>
              </div>
            </AnimatedSection>
          </aside>
        </div>
      </div>

      {/* Sticky Bottom Action Bar */}
      <div className="fixed bottom-0 left-0 right-0 z-40 px-6 py-3">
        <div className="max-w-7xl mx-auto">
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
