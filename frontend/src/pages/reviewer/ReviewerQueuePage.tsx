import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  ArrowLeft, ShieldClose, FolderKanban, Search, Filter, Eye,
  ChevronRight, Clock, User, FileText, AlertCircle,
  CheckCircle2, X, Check
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';
import { diseaseApi } from '../../api/disease';

import type { DiseaseVersionResponse } from '../../types/diseaseVersion';

type QueueItem = DiseaseVersionResponse & { diseaseName: string; diseaseCategory: string; diseaseSlug: string };

export default function ReviewerQueuePage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('All');
  const [statusFilter, setStatusFilter] = useState('All');
  const [queue, setQueue] = useState<QueueItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadReviewerQueue = async () => {
      if (!user) {
        setLoading(false);
        return;
      }
      setLoading(true);
      setError(null);
      try {
        const response = await diseaseApi.getPendingReviewVersions(0, 20);
        const versions = response.content || [];
        const queueWithDetails = await Promise.all(
          versions.map(async (version: DiseaseVersionResponse): Promise<QueueItem> => {
            try {
              const diseaseResponse = await diseaseApi.fetchDisease(version.diseaseId.toString());
              const disease = diseaseResponse;
              return {
                ...version,
                diseaseName: disease.name,
                diseaseCategory: disease.categoryName,
                // We'll also get the disease slug for linking if needed
                diseaseSlug: disease.slug,
              };
            } catch (err) {
              console.warn(`Failed to fetch disease for version ${version.id}`, err);
              return {
                ...version,
                diseaseName: 'Unknown Disease',
                diseaseCategory: 'Unknown',
                diseaseSlug: '',
              };
            }
          })
        );
        setQueue(queueWithDetails);
      } catch (err: any) {
        console.error('Failed to load reviewer queue', err);
        setError(err.response?.data?.message || 'Failed to load review queue');
      } finally {
        setLoading(false);
      }
    };

    loadReviewerQueue();
  }, [user]);

  const handleApprove = async (versionId: number) => {
    try {
      // We'll ask for a note in a real app, but for now we'll pass null
      await diseaseApi.approveVersion(versionId, null);
      alert('Version approved successfully!');
      // Refresh the queue
      // We could refetch, but for simplicity we'll just remove the item from the queue
      setQueue(prev => prev.filter(item => item.id !== versionId));
    } catch (err: any) {
      console.error('Failed to approve version', err);
      alert(err.response?.data?.message || 'Failed to approve version');
    }
  };

  const handleReject = async (versionId: number) => {
    try {
      // We'll ask for a note in a real app, but for now we'll pass null
      await diseaseApi.rejectVersion(versionId, null);
      alert('Version rejected successfully!');
      // Refresh the queue
      setQueue(prev => prev.filter(item => item.id !== versionId));
    } catch (err: any) {
      console.error('Failed to reject version', err);
      alert(err.response?.data?.message || 'Failed to reject version');
    }
  };

  if (role !== 'REVIEWER' && role !== 'ADMIN') {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6 text-center">
        <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
        <p className="text-[var(--text-secondary)] text-lg mb-4">Reviewer access required</p>
        <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6">
        <div className="max-w-7xl mx-auto">
          <AnimatedSection>
            <div className="text-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
              <p className="mt-4 text-[var(--text-secondary)]">Loading review queue...</p>
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
              <Link to="/dashboard" className="mt-4 inline-flex items-center px-4 py-2 border border-[var(--border-primary)] text-[var(--text-primary)] rounded-md hover:bg-[var(--surface-primary)]">
                <ArrowLeft size={16} /> Back to Dashboard
              </Link>
            </div>
          </AnimatedSection>
        </div>
      </div>
    );
  }

  const filtered = queue.filter((item: any) => {
    const match = 
      item.diseaseName.toLowerCase().includes(search.toLowerCase()) || 
      (item.diseaseId && item.diseaseId.toString().includes(search)) ||
      (item.createdById && item.createdById.toString().includes(search));
    const typeOk = typeFilter === 'All' || item.diseaseCategory === typeFilter;
    const statusOk = statusFilter === 'All' || item.status === statusFilter;
    return match && typeOk && statusOk;
  });

  const statusColor = (s: string) =>
    s === 'APPROVED' ? 'text-emerald-600 bg-emerald-500/10' :
    s === 'REJECTED' ? 'text-red-600 bg-red-500/10' :
    s === 'PENDING_REVIEW' ? 'text-blue-600 bg-blue-500/10' :
    s === 'ARCHIVED' ? 'text-gray-600 bg-gray-500/10' :
    'text-gray-600 bg-gray-500/10';

  // We don't have priority in the version data, so we'll use a default or derive from something else.
  // For now, we'll set all to Medium.
  const priorityColor = () => 'text-amber-600';

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><FolderKanban size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Review Queue</h1>
              <p className="text-sm text-[var(--text-secondary)]">{queue.length} items</p>
            </div>
          </div>

          <div className="card-neumorphic p-4 mb-6 flex flex-wrap items-center gap-4">
            <div className="relative flex-1 min-w-[200px]">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
              <input type="text" placeholder="Search disease title or ID..." value={search} onChange={(e) => setSearch(e.target.value)} className="input-neumorphic pl-9 w-full text-sm" />
            </div>
            <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)} className="input-neumorphic py-2 px-3 text-sm">
              <option value="All">All Categories</option>
              {/* We don't have a list of categories from the queue, so we'll hardcode some common ones or leave empty */}
              <option value="Infectious Diseases">Infectious Diseases</option>
              <option value="Cardiovascular">Cardiovascular</option>
              <option value="Respiratory">Respiratory</option>
              <option value="Neurology">Neurology</option>
              <option value="Nephrology">Nephrology</option>
              <option value="Autoimmune">Autoimmune</option>
            </select>
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="input-neumorphic py-2 px-3 text-sm">
              <option value="All">All Status</option>
              <option value="PENDING_REVIEW">Pending Review</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="ARCHIVED">Archived</option>
              <option value="DRAFT">Draft</option>
            </select>
          </div>

          <div className="card-neumorphic overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-[var(--shadow-dark)]">
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Disease</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Category</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Version</th>
                  <th className="text-center p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Status</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Submitted</th>
                  <th className="text-left p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Reviewer</th>
                  <th className="text-right p-4 text-[var(--text-tertiary)] font-semibold text-xs uppercase">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((item: any) => (
                  <tr key={item.id} className="border-b border-[var(--shadow-dark)] last:border-0 hover:bg-[var(--surface-hover)] transition-colors">
                    <td className="p-4 font-medium text-[var(--text-primary)]">{item.diseaseName}</td>
                    <td className="p-4 text-[var(--text-secondary)]">{item.diseaseCategory}</td>
                    <td className="p-4 text-center text-[var(--text-secondary)]">v{item.versionNumber}</td>
                    <td className="p-4"><span className={`text-xs px-2 py-0.5 rounded-full font-medium ${statusColor(item.status)}`}>{item.status}</span></td>
                    <td className="p-4 text-[var(--text-secondary)]">
                      {item.createdAt ? new Date(item.createdAt).toLocaleString() : 'Unknown'}
                    </td>
                    <td className="p-4 text-[var(--text-secondary)]">
                      {item.reviewedById ? `Reviewer ID: ${item.reviewedById}` : 'Pending'}
                    </td>
                    <td className="p-4 text-right">
                      <div className="flex items-center justify-end gap-1">
                        {/* Link to review page - we need to know the review page route */}
                        <Link to={`/reviewer/review/${item.id}`} className="btn-neumorphic-secondary p-1.5 text-xs">
                          <Eye size={12} />
                        </Link>
                        {/* Approve button */}
                        <button 
                          className="btn-neumorphic-secondary p-1.5 text-xs text-emerald-600"
                          onClick={() => handleApprove(item.id)}
                        >
                          <Check size={12} />
                        </button>
                        {/* Reject button */}
                        <button 
                          className="btn-neumorphic-secondary p-1.5 text-xs text-red-600"
                          onClick={() => handleReject(item.id)}
                        >
                          <X size={12} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}

