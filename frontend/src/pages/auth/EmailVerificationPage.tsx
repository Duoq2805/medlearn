import { Link } from 'react-router-dom';
import { ChevronRight, MailCheck } from 'lucide-react';
import { MedvoraLogo } from '../../components/MedvoraLogo';

export default function EmailVerificationPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-[var(--bg-primary)] pt-32">
      <div className="w-full max-w-md card-neumorphic p-8 text-center space-y-8">
        <div className="inline-flex justify-center">
          <MedvoraLogo className="h-16 w-16" variant="icon" />
        </div>

        <div className="space-y-3">
          <h1 className="font-display text-4xl font-bold text-[var(--text-primary)]">
            Verify Your Email
          </h1>
          <p className="text-[var(--text-secondary)]">
            A confirmation link has been sent to your inbox.
          </p>
        </div>

        <button className="btn-neumorphic-primary w-full py-3 inline-flex items-center justify-center gap-2 group">
          Open Email App
          <ChevronRight size={20} className="group-hover:translate-x-1 transition-transform" />
        </button>

        <p className="text-sm text-[var(--text-secondary)]">
          Didn't receive the email?{' '}
          <a href="#" className="font-medium text-[var(--accent-primary)] hover:text-[#a8c66a] transition-colors">
            Resend Email
          </a>
        </p>

        <Link to="/login" className="block text-sm text-[var(--text-secondary)]">
          Back to{' '}
          <span className="font-medium text-[var(--accent-primary)] hover:text-[#a8c66a] transition-colors">
            Sign in
          </span>
        </Link>
      </div>
    </div>
  );
}
