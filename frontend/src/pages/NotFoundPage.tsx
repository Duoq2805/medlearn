import { Link } from 'react-router-dom';
import { BookOpen, ChevronRight } from 'lucide-react';
import { AnimatedSection } from '../components/motion/MotionWrappers';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen flex items-center justify-center pt-32 pb-24 px-4">
      <div className="card-neumorphic p-12 text-center max-w-md mx-auto">
        <div className="icon-well mx-auto mb-4"><BookOpen size={28} /></div>
        <h1 className="font-display text-3xl font-bold text-[var(--text-primary)] mb-2">Page not found</h1>
        <p className="text-sm text-[var(--text-secondary)] mb-6">The page you're looking for doesn't exist.</p>
        <Link to="/dashboard" className="btn-neumorphic-primary py-3 px-8 inline-flex items-center gap-2">
          Go to Dashboard <ChevronRight size={18} />
        </Link>
      </div>
    </div>
  );
}
