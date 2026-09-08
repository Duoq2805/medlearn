import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  ShieldClose, ArrowLeft, Send, AlertTriangle, FileText,
  ThumbsUp, ThumbsDown, AlertCircle, Loader2, CheckCircle2
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';
import { diseaseApi } from '../../api/disease';
import { diseaseSectionApi } from '../../api/diseaseSection';
import type { DiseaseVersionResponse } from '../../types/diseaseVersion';
import type { DiseaseSectionResponse } from '../../types/diseaseSection';

export default function ReviewerReviewPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();

  const [decision, setDecision] = useState<'approve' | 'changes' | 'reject' | null>(null);
  const [authorComments, setAuthorComments] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [version, setVersion] = useState<DiseaseVersionResponse | null>(null);
  const [disease, setDisease] = useState<any>(null);
  const [sections, setSections] = useState<DiseaseSectionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadReviewData = async () => {
      if (!id) {
        setError('Version ID missing');
        setLoading(false);
        return;
      }
      setLoading(true);
      setError(null);
      try {
        const versionId = parseInt(id, 10);
        const versionRes = await diseaseApi.getVersionById(versionId);
        setVersion(versionRes);

        const diseaseRes = await diseaseApi.fetchDisease(versionRes.diseaseId);
        setDisease(diseaseRes);

        const sectionsRes = await diseaseSectionApi.getSectionsByVersion(versionId);
        setSections(sectionsRes || []);
      } catch (err: any) {
        console.error('Failed to load version review data', err);
        setError(err.response?.data?.message || 'Failed to load version details');
      } finally {
        setLoading(false);
      }
    };

    loadReviewData();
  }, [id]);

  if (role !== 'REVIEWER' && role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Reviewer access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const handleSubmitReview = async () => {
    if (!decision) {
      alert('Please select a decision (Approve, Request Changes, or Reject)');
      return;
    }
    if (!version?.id) return;

    setIsSubmitting(true);
    try {
      if (decision === 'approve') {
        await diseaseApi.approveVersion(version.id, authorComments.trim() || null);
        alert('Version approved successfully!');
      } else {
        await diseaseApi.rejectVersion(version.id, authorComments.trim() || 'Changes requested');
        alert('Version review submitted (rejected/changes requested)!');
      }
      navigate('/reviewer/queue');
    } catch (err: any) {
      console.error('Failed to submit review decision', err);
      alert(err.response?.data?.message || 'Failed to submit review');
    } finally {
      setIsSubmitting(false);
    }
  };

  const checklist = [
    { label: 'Definition & Overview accuracy', key: 'def' },
    { label: 'Etiology & Symptoms completeness', key: 'sym' },
    { label: 'Diagnosis & Treatment guidelines compliance', key: 'dx' },
    { label: 'No grammatical or structural issues', key: 'grammar' },
    { label: 'References & Citations validity', key: 'ref' },
  ];

  if (loading) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6 text-center">
        <Loader2 size={36} className="animate-spin mx-auto mb-4 text-[var(--accent-primary)]" />
        <p className="text-[var(--text-secondary)]">Loading version details for review...</p>
      </div>
    );
  }

  if (error || !version) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6 text-center">
        <AlertTriangle size={48} className="mx-auto mb-4 text-red-500" />
        <p className="text-[var(--text-error)] text-lg mb-4">{error || 'Version not found'}</p>
        <Link to="/reviewer/queue" className="btn-neumorphic-primary py-2 px-6">Back to Queue</Link>
      </div>
    );
  }

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
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">
                  {disease?.name || 'Disease Version Review'}
                </h1>
                <p className="text-sm text-[var(--text-secondary)]">
                  Author: <strong className="text-[var(--accent-primary)]">User #{version.createdById}</strong> • Category: {disease?.categoryName || 'Uncategorized'} • Version {version.versionNumber} • Status: <span className="font-semibold text-blue-600">{version.status}</span>
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs px-3 py-1 rounded-full bg-purple-500/10 text-purple-600 font-medium">Version ID: {version.id}</span>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* LEFT: Actual Disease Sections */}
            <div className="lg:col-span-2 space-y-6">
              {sections.length === 0 ? (
                <div className="card-neumorphic p-8 text-center text-[var(--text-tertiary)]">
                  No section content provided for this version.
                </div>
              ) : (
                sections.map((section) => (
                  <div key={section.id} className="card-neumorphic p-6">
                    <div className="flex items-center justify-between mb-3 border-b border-[var(--shadow-dark)] pb-2">
                      <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">
                        {section.title || section.sectionType || 'Section'}
                      </h2>
                    </div>
                    <div className="prose prose-sm text-[var(--text-secondary)] whitespace-pre-wrap leading-relaxed">
                      {section.content || <span className="italic text-[var(--text-tertiary)]">Empty section content</span>}
                    </div>
                  </div>
                ))
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

              <div className="card-neumorphic p-6">
                <h2 className="font-display text-base font-bold text-[var(--text-primary)] mb-4">Comments to Author</h2>
                <textarea
                  value={authorComments}
                  onChange={(e) => setAuthorComments(e.target.value)}
                  placeholder="Provide detailed feedback or rejection notes for author..."
                  className="input-neumorphic w-full min-h-[100px] resize-none text-xs p-3"
                />
              </div>

              {/* Decision */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-base font-bold text-[var(--text-primary)] mb-4">Decision</h2>
                <div className="space-y-2">
                  <button
                    onClick={() => setDecision('approve')}
                    className={`py-2 px-4 w-full text-sm flex items-center justify-center gap-2 rounded-xl transition-all ${
                      decision === 'approve' ? 'bg-emerald-500 text-white shadow-md' : 'btn-neumorphic-secondary'
                    }`}
                  >
                    <ThumbsUp size={14} /> Approve Version
                  </button>

                  <button
                    onClick={() => setDecision('changes')}
                    className={`py-2 px-4 w-full text-sm flex items-center justify-center gap-2 rounded-xl transition-all ${
                      decision === 'changes' ? 'bg-amber-500 text-white shadow-md' : 'btn-neumorphic-secondary'
                    }`}
                  >
                    <AlertCircle size={14} /> Request Changes
                  </button>

                  <button
                    onClick={() => setDecision('reject')}
                    className={`py-2 px-4 w-full text-sm flex items-center justify-center gap-2 rounded-xl transition-all ${
                      decision === 'reject' ? 'bg-red-500 text-white shadow-md' : 'btn-neumorphic-secondary'
                    }`}
                  >
                    <ThumbsDown size={14} /> Reject Version
                  </button>
                </div>
              </div>

              {/* Submit */}
              <button
                onClick={handleSubmitReview}
                disabled={isSubmitting || !decision}
                className="btn-neumorphic-primary py-3 px-4 w-full flex items-center justify-center gap-2 disabled:opacity-50"
              >
                {isSubmitting ? <Loader2 size={16} className="animate-spin" /> : <Send size={14} />} Submit Review
              </button>
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
