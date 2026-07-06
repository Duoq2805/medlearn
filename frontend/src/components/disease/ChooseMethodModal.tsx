import { X, FileText, Upload, Link, Sparkles } from 'lucide-react';
import type { DraftCreationMethod } from '../../types/diseaseDraft';

interface ChooseMethodModalProps {
  open: boolean;
  onClose: () => void;
  onSelect: (method: DraftCreationMethod) => void;
}

const methods: {
  method: DraftCreationMethod;
  icon: typeof FileText;
  label: string;
  description: string;
  disabled?: boolean;
  disabledReason?: string;
}[] = [
  {
    method: 'manual',
    icon: FileText,
    label: 'Write Manually',
    description: 'Start with a blank editor and write your draft from scratch',
  },
  {
    method: 'upload',
    icon: Upload,
    label: 'Upload Medical Document',
    description: 'Upload PDF, DOCX, TXT, or Markdown to extract content',
  },
  {
    method: 'import-url',
    icon: Link,
    label: 'Import Article URL',
    description: 'Import content from WHO, CDC, NCBI, Mayo Clinic, Medscape',
  },
  {
    method: 'ai-generate',
    icon: Sparkles,
    label: 'AI Generate',
    description: 'Generate a structured draft from source documents',
    disabled: true,
    disabledReason: 'TODO: Waiting for AI draft generation endpoint',
  },
];

export default function ChooseMethodModal({
  open,
  onClose,
  onSelect,
}: ChooseMethodModalProps) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="relative w-full max-w-lg mx-4">
        <div className="card-neumorphic p-8">
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="font-display text-xl font-bold text-[var(--text-primary)]">
                New Draft
              </h2>
              <p className="text-sm text-[var(--text-secondary)] mt-1">
                How do you want to create this draft?
              </p>
            </div>
            <button
              onClick={onClose}
              className="btn-neumorphic-secondary p-2 rounded-xl"
            >
              <X size={18} />
            </button>
          </div>

          {/* Method Cards */}
          <div className="space-y-3">
            {methods.map((m) => {
              const Icon = m.icon;
              return (
                <button
                  key={m.method}
                  onClick={() => !m.disabled && onSelect(m.method)}
                  disabled={m.disabled}
                  className={`w-full text-left card-neumorphic p-4 transition-all ${
                    m.disabled
                      ? 'opacity-50 cursor-not-allowed'
                      : 'hover:-translate-y-0.5 cursor-pointer'
                  }`}
                >
                  <div className="flex items-start gap-4">
                    <div
                      className={`p-2.5 rounded-xl ${
                        m.method === 'ai-generate'
                          ? 'bg-purple-500/10 text-purple-600'
                          : 'bg-[var(--accent-primary)]/10 text-[var(--accent-primary)]'
                      }`}
                    >
                      <Icon size={20} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-sm text-[var(--text-primary)]">
                          {m.label}
                        </span>
                        {m.disabled && m.disabledReason && (
                          <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-600 font-medium">
                            Coming Soon
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-[var(--text-secondary)] mt-0.5">
                        {m.description}
                      </p>
                      {m.disabled && m.disabledReason && (
                        <p className="text-[10px] text-amber-600 mt-1 italic">
                          {m.disabledReason}
                        </p>
                      )}
                    </div>
                    {!m.disabled && (
                      <div className="text-[var(--text-tertiary)] mt-1">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="m9 18 6-6-6-6"/></svg>
                      </div>
                    )}
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
