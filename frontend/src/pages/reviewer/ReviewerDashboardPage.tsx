import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Shield, ShieldCheck, ShieldClose } from 'lucide-react';

import Skeleton from '../../components/ui/Skeleton';
import { useAuth } from '../../hooks/useAuth';

interface Review {
  id: string;
  title: string;
  status: 'pending' | 'approved' | 'rejected';
  submittedBy: string;
  submittedAt: string;
}

interface ReviewsResponse {
  data: Review[];
  total: number;
}

const ITEMS_PER_PAGE = 10;

export default function ReviewerDashboardPage() {
  const { user } = useAuth();
  const [page, setPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');

  const {
    data: reviewsData,
    isLoading: isLoadingReviews,
    error: reviewsError,
    refetch: refetchReviews
  } = useQuery<ReviewsResponse>({
    queryKey: ['content-reviews', page, searchTerm],
    queryFn: async () => {
      await new Promise(resolve => setTimeout(resolve, 800));
      return {
        data: [
          { id: '1', title: 'Diabetes Review', status: 'pending', submittedBy: 'Dr. Smith', submittedAt: '2023-10-26' },
          { id: '2', title: 'Hypertension Guide', status: 'approved', submittedBy: 'Dr. Jones', submittedAt: '2023-10-25' },
        ],
        total: 2,
      };
    },
  });

  const totalPages = reviewsData ? Math.ceil(reviewsData.total / ITEMS_PER_PAGE) : 0;

  if (!user || (user.role !== 'reviewer' && user.role !== 'admin')) {
    return (
      <div className="min-h-screen bg-[var(--bg-primary)] pt-32 pb-24">
        <div className="max-w-4xl mx-auto px-4 md:px-8 text-center py-20">
          <div className="card-neumorphic-sm max-w-md mx-auto p-8">
            <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
            <p className="text-[var(--text-secondary)] text-lg mb-4">Reviewer access required</p>
            <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6 inline-flex items-center gap-2">Go to Dashboard</Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[var(--bg-primary)] pt-32 pb-24">
      <div className="max-w-7xl mx-auto px-4 md:px-8">
        <div className="mb-10">
          <p className="text-sm font-medium text-[var(--accent-primary)] mb-1">Review</p>
          <h1 className="font-display text-3xl md:text-4xl font-bold text-[var(--text-primary)]">Content Queue</h1>
        </div>

        {isLoadingReviews ? (
          <div className="space-y-3">
            {[1, 2, 3].map(i => <Skeleton key={i} className="h-16 w-full rounded-xl" />)}
          </div>
        ) : reviewsError ? (
          <div className="card-neumorphic-sm p-8 text-center">
            <p className="text-[var(--text-secondary)] mb-4">Failed to load reviews.</p>
            <button onClick={() => refetchReviews()} className="btn-neumorphic-primary py-2 px-6">Retry</button>
          </div>
        ) : reviewsData && reviewsData.data.length > 0 ? (
          <>
            <div className="space-y-3">
              {reviewsData.data.map((review) => (
                <div key={review.id} className="card-neumorphic-sm p-4 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-[var(--text-primary)]">{review.title}</p>
                    <p className="text-xs text-[var(--text-tertiary)]">by {review.submittedBy} · {review.submittedAt}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                      review.status === 'approved' ? 'bg-[var(--accent-primary)]/10 text-[var(--accent-primary)]' :
                      review.status === 'rejected' ? 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400' :
                      'bg-[#f59e0b]/10 text-[#92400e]'
                    }`}>
                      {review.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
            {totalPages > 1 && (
              <div className="mt-8 flex justify-center gap-4">
                <button onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1} className="btn-neumorphic-secondary py-2 px-5 text-sm disabled:opacity-40">← Previous</button>
                <span className="text-sm text-[var(--text-secondary)] self-center">Page {page} of {totalPages}</span>
                <button onClick={() => setPage(p => p + 1)} disabled={page === totalPages} className="btn-neumorphic-secondary py-2 px-5 text-sm disabled:opacity-40">Next →</button>
              </div>
            )}
          </>
        ) : (
          <div className="text-center py-20">
            <div className="max-w-md mx-auto">
              <ShieldCheck size={48} className="mx-auto mb-4 text-[var(--accent-primary)]" />
              <h3 className="font-display text-xl font-bold text-[var(--text-primary)] mb-2">All caught up</h3>
              <p className="text-sm text-[var(--text-secondary)]">No pending content reviews.</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
