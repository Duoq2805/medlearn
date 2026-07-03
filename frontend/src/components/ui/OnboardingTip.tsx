import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Lightbulb } from 'lucide-react';

interface OnboardingTipProps {
  tipKey: string;
  children: React.ReactNode;
}

export default function OnboardingTip({ tipKey, children }: OnboardingTipProps) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const dismissed = localStorage.getItem(`onboarding:${tipKey}`);
    if (!dismissed) {
      const timer = setTimeout(() => setVisible(true), 600);
      return () => clearTimeout(timer);
    }
  }, [tipKey]);

  const dismiss = () => {
    setVisible(false);
    localStorage.setItem(`onboarding:${tipKey}`, '1');
  };

  return (
    <AnimatePresence>
      {visible && (
        <motion.div
          initial={{ opacity: 0, y: -8, scale: 0.96 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: -8, scale: 0.96 }}
          transition={{ duration: 0.3, ease: [0.25, 0.1, 0.25, 1] }}
          className="card-neumorphic-sm p-4 flex items-start gap-3 mb-6"
        >
          <div className="w-8 h-8 rounded-full bg-[var(--accent-primary)]/10 flex items-center justify-center shrink-0 mt-0.5">
            <Lightbulb size={16} className="text-[var(--accent-primary)]" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm text-[var(--text-primary)]">{children}</p>
          </div>
          <button
            onClick={dismiss}
            className="shrink-0 text-[var(--text-tertiary)] hover:text-[var(--text-primary)] transition-colors p-1"
            aria-label="Dismiss tip"
          >
            <X size={14} />
          </button>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
