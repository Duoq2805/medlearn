import { useState } from 'react';
import { ChevronDown, ChevronUp, FileText, ExternalLink } from 'lucide-react';
import type { SectionSource } from '../../types/diseaseDraft';

interface SourceViewerProps {
  sources: SectionSource[];
}

export default function SourceViewer({ sources }: SourceViewerProps) {
  const [expanded, setExpanded] = useState(false);

  if (!sources || sources.length === 0) return null;

  return (
    <div className="mt-3 border-t border-[var(--shadow-dark)] pt-2">
      <button
        onClick={() => setExpanded(!expanded)}
        className="flex items-center gap-1.5 text-xs text-[var(--text-tertiary)] hover:text-[var(--accent-primary)] transition-colors"
      >
        <FileText size={12} />
        <span>View Sources ({sources.length})</span>
        {expanded ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
      </button>

      {expanded && (
        <div className="mt-2 space-y-1.5">
          {sources.map((s, i) => (
            <div
              key={i}
              className="flex items-center justify-between rounded-lg bg-[var(--bg-secondary)]/50 px-2.5 py-1.5 text-xs"
            >
              <div className="flex items-center gap-2 min-w-0">
                <FileText size={10} className="shrink-0 text-[var(--accent-primary)]" />
                <span className="truncate text-[var(--text-secondary)]">
                  {s.document}
                </span>
              </div>
              <div className="flex items-center gap-3 shrink-0 ml-2">
                {s.page != null && (
                  <span className="text-[var(--text-tertiary)]">
                    p.{s.page}
                  </span>
                )}
                {s.paragraph != null && (
                  <span className="text-[var(--text-tertiary)]">
                    ¶{s.paragraph}
                  </span>
                )}
                {s.confidence != null && (
                  <span
                    className={`font-medium ${
                      s.confidence >= 0.8
                        ? 'text-green-600'
                        : s.confidence >= 0.5
                        ? 'text-amber-600'
                        : 'text-red-600'
                    }`}
                  >
                    {Math.round(s.confidence * 100)}%
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
