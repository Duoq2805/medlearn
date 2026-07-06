import { Sparkles, Clock, Cpu } from 'lucide-react';
import type { DraftProvenance } from '../../types/diseaseDraft';

interface ProvenanceBadgeProps {
  provenance?: DraftProvenance | null;
}

export default function ProvenanceBadge({ provenance }: ProvenanceBadgeProps) {
  if (!provenance) return null;

  const hasAny =
    provenance.generatedBy ||
    provenance.model ||
    provenance.generatedAt ||
    provenance.promptVersion;

  if (!hasAny) return null;

  return (
    <div className="flex flex-wrap items-center gap-3 rounded-lg bg-[var(--bg-secondary)]/50 px-3 py-2 text-xs text-[var(--text-tertiary)]">
      {provenance.generatedBy && (
        <span className="flex items-center gap-1">
          <Sparkles size={11} />
          {provenance.generatedBy}
        </span>
      )}
      {provenance.model && (
        <span className="flex items-center gap-1">
          <Cpu size={11} />
          {provenance.model}
        </span>
      )}
      {provenance.generatedAt && (
        <span className="flex items-center gap-1">
          <Clock size={11} />
          {new Date(provenance.generatedAt).toLocaleString()}
        </span>
      )}
      {provenance.promptVersion && (
        <span className="text-[var(--text-tertiary)]">
          Prompt v{provenance.promptVersion}
        </span>
      )}
    </div>
  );
}
