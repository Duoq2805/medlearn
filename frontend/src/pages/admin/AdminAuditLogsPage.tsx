import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { ShieldClose, FileText } from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminAuditLogsPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();

  if (role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><FileText size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Audit Logs</h1>
              <p className="text-sm text-[var(--text-secondary)]">System activity log</p>
            </div>
          </div>

          <div className="card-neumorphic p-12 text-center">
            <FileText size={48} className="mx-auto mb-4 text-[var(--text-tertiary)] opacity-40" />
            <h3 className="font-display text-xl font-bold text-[var(--text-primary)] mb-2">Backend endpoint not available</h3>
            <p className="text-sm text-[var(--text-secondary)] max-w-md mx-auto">
              The audit log API endpoint is not yet implemented on the backend.
              This page will automatically display system activity logs once the
              <code className="text-xs bg-[var(--surface-hover)] px-1.5 py-0.5 rounded mx-1">GET /admin/audit-logs</code>
              endpoint is available.
            </p>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
