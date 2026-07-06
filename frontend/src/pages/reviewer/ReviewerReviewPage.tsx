import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  ShieldClose, ArrowLeft, CheckCircle2, XCircle, MessageSquare,
  Eye, AlertTriangle, FileText, ClipboardCheck, ThumbsUp,
  ThumbsDown, Send, ChevronDown, ChevronRight, BookOpen, AlertCircle
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function ReviewerReviewPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [decision, setDecision] = useState<'approve' | 'changes' | 'reject' | null>(null);
  const [reviewerNotes, setReviewerNotes] = useState('');
  const [internalNotes, setInternalNotes] = useState('');
  const [authorComments, setAuthorComments] = useState('');

  if (role !== 'REVIEWER' && role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Reviewer access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const disease = {
    id, name: 'Pneumonia', author: 'Dr. Emily Chen', submitted: '2h ago',
    version: 3, category: 'Infectious Diseases',
    sections: [
      { label: 'Definition', content: 'Pneumonia is an infection that inflames air sacs in the lungs.', ok: true },
      { label: 'Etiology', content: 'Bacterial, viral, or fungal infection', ok: true },
      { label: 'Symptoms', content: 'Cough, fever, dyspnea, chest pain', ok: true },
      { label: 'Diagnosis', content: 'Chest X-ray, blood cultures, PCR', ok: false, issue: 'Missing reference to CURB-65 scoring' },
      { label: 'Treatment', content: 'Antibiotics, supportive care', ok: true },
      { label: 'References', content: '5 references provided', ok: false, issue: '2 references outdated (>5 years)' },
    ],
    aiFlags: ['Possible duplicate: similar to "Community-Acquired Pneumonia"'],
  };

  const checklist = [
    { label: 'Definition is accurate', key: 'def' },
    { label: 'Symptoms are complete', key: 'sym' },
    { label: 'Diagnosis includes recent guidelines', key: 'dx' },
    { label: 'Treatment follows standard protocols', key: 'tx' },
    { label: 'References are current (<5 years)', key: 'ref' },
    { label: 'No grammatical errors', key: 'grammar' },
    { label: 'No duplicate content detected', key: 'dup' },
  ];

  return (
    <div className="min-h-screen pt-28 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <Link to="/reviewer/queue" className="flex items-center gap-2 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] mb-4">
            <ArrowLeft size={16} /> Back to Queue
          </Link>

          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-3">
              <div className="icon-well"><FileText size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">{disease.name}</h1>
                <p className="text-sm text-[var(--text-secondary)]">by {disease.author} • v{disease.version} • {disease.category} • Submitted {disease.submitted}</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs px-2 py-1 rounded-full bg-blue-500/10 text-blue-600">Review ID: {id}</span>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* LEFT: Disease Content */}
            <div className="lg:col-span-2 space-y-4">
              {disease.sections.map((s) => (
                <div key={s.label} className={`card-neumorphic p-6 ${!s.ok ? 'border-l-4 border-amber-500' : ''}`}>
                  <div className="flex items-center justify-between mb-2">
                    <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">{s.label}</h2>
                    {!s.ok && <AlertCircle size={16} className="text-amber-500" />}
                  </div>
                  <p className="text-sm text-[var(--text-secondary)]">{s.content}</p>
                  {s.issue && <p className="text-xs text-amber-600 mt-2 flex items-center gap-1"><AlertTriangle size={12} /> {s.issue}</p>}
                </div>
              ))}

              {disease.aiFlags.length > 0 && (
                <div className="card-neumorphic p-6 border-l-4 border-purple-500">
                  <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-2">AI Flags</h2>
                  {disease.aiFlags.map((f, i) => (
                    <p key={i} className="text-xs text-purple-600 flex items-center gap-1"><AlertCircle size={12} /> {f}</p>
                  ))}
                </div>
              )}
            </div>

            {/* RIGHT: Review Panel */}
            <div className="space-y-6">
              {/* Checklist */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-base font-bold text-[var(--text-primary)] mb-4">Review Checklist</h2>
                <div className="space-y-2">
                  {checklist.map((item) => (
                    <label key={item.key} className="flex items-center gap-2 cursor-pointer">
                      <input type="checkbox" className="rounded border-[var(--shadow-dark)] text-[var(--accent-primary)] focus:ring-[var(--accent-primary)]" />
                      <span className="text-xs text-[var(--text-primary)]">{item.label}</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Reviewer Notes */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-base font-bold text-[var(--text-primary)] mb-4">Internal Notes</h2>
                <textarea value={internalNotes} onChange={(e) => setInternalNotes(e.target.value)} placeholder="Private notes (not visible to author)..." className="input-neumorphic w-full min-h-[80px] resize-none text-xs" />
              </div>

              <div className="card-neumorphic p-6">
                <h2 className="font-display text-base font-bold text-[var(--text-primary)] mb-4">Comments to Author</h2>
                <textarea value={authorComments} onChange={(e) => setAuthorComments(e.target.value)} placeholder="Feedback for the author..." className="input-neumorphic w-full min-h-[80px] resize-none text-xs" />
              </div>

              {/* Decision */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-base font-bold text-[var(--text-primary)] mb-4">Decision</h2>
                <div className="space-y-2">
                  <button onClick={() => setDecision('approve')} className={`py-2 px-4 w-full text-sm flex items-center justify-center gap-2 rounded-xl transition-all ${
                    decision === 'approve' ? 'bg-emerald-500 text-white shadow-md' : 'btn-neumorphic-secondary'
                  }`}><ThumbsUp size={14} /> Approve</button>

                  <button onClick={() => setDecision('changes')} className={`py-2 px-4 w-full text-sm flex items-center justify-center gap-2 rounded-xl transition-all ${
                    decision === 'changes' ? 'bg-amber-500 text-white shadow-md' : 'btn-neumorphic-secondary'
                  }`}><AlertCircle size={14} /> Request Changes</button>

                  <button onClick={() => setDecision('reject')} className={`py-2 px-4 w-full text-sm flex items-center justify-center gap-2 rounded-xl transition-all ${
                    decision === 'reject' ? 'bg-red-500 text-white shadow-md' : 'btn-neumorphic-secondary'
                  }`}><ThumbsDown size={14} /> Reject</button>
                </div>
              </div>

              {/* Submit */}
              <button className="btn-neumorphic-primary py-3 px-4 w-full flex items-center justify-center gap-2">
                <Send size={14} /> Submit Review
              </button>
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
