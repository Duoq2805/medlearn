import { useState } from 'react';
import { Check, X, Eye, FileText, Sparkles } from 'lucide-react';
import SourceViewer from './SourceViewer';
import ProvenanceBadge from './ProvenanceBadge';
import { DRAFT_SECTIONS } from '../../types/diseaseDraft';
import type { DraftGenerationResponse, DraftCreationMetadata } from '../../types/diseaseDraft';

interface DraftPreviewProps {
  response: DraftGenerationResponse;
  metadata: DraftCreationMetadata;
  onAccept: () => void;
  onReject: () => void;
}

export default function DraftPreview({
  response,
  metadata,
  onAccept,
  onReject,
}: DraftPreviewProps) {
  const [expandedSections, setExpandedSections] = useState<Record<string, boolean>>(
    () => {
      const init: Record<string, boolean> = {};
      DRAFT_SECTIONS.forEach((s) => { init[s.key] = true; });
      return init;
    }
  );

  const toggleSection = (key: string) => {
    setExpandedSections((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const totalSections = DRAFT_SECTIONS.length;
  const populatedSections = DRAFT_SECTIONS.filter(
    (s) => (response as any)[s.key]?.content
  ).length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="card-neumorphic p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-purple-500/10 text-purple-600">
              <Sparkles size={20} />
            </div>
            <div>
              <h2 className="font-display text-xl font-bold text-[var(--text-primary)]">
                Preview Generated Draft
              </h2>
              <p className="text-xs text-[var(--text-secondary)] mt-0.5">
                Review before accepting into editor
              </p>
            </div>
          </div>
        </div>

        {/* Creation method badge */}
        <div className="flex items-center gap-2 mb-3">
          <span className="text-[10px] px-2 py-0.5 rounded-full bg-[var(--accent-primary)]/10 text-[var(--accent-primary)] font-medium flex items-center gap-1">
            <FileText size={10} />
            {metadata.sourceLabel}
          </span>
          <span className="text-xs text-[var(--text-tertiary)]">
            {populatedSections}/{totalSections} sections
          </span>
        </div>

        {/* Provenance */}
        <ProvenanceBadge provenance={response.provenance ?? null} />
      </div>

      {/* Section Preview */}
      {DRAFT_SECTIONS.map((section) => {
        const data = (response as any)[section.key] as
          | { content?: string; sources?: any[] }
          | undefined;
        const content = data?.content ?? '';
        const sources = data?.sources ?? [];

        if (!content) return null;

        return (
          <div key={section.key} className="card-neumorphic p-5">
            <button
              onClick={() => toggleSection(section.key)}
              className="flex items-center justify-between w-full text-left"
            >
              <h3 className="font-display text-base font-bold text-[var(--text-primary)]">
                {section.label}
              </h3>
              <span className="text-xs text-[var(--text-tertiary)]">
                {expandedSections[section.key] ? 'Collapse' : 'Expand'}
              </span>
            </button>

            {expandedSections[section.key] && (
              <>
                <div className="mt-3 text-sm text-[var(--text-secondary)] leading-relaxed whitespace-pre-wrap">
                  {content}
                </div>
                {sources.length > 0 && <SourceViewer sources={sources} />}
              </>
            )}
          </div>
        );
      })}

      {/* Empty state */}
      {populatedSections === 0 && (
        <div className="card-neumorphic p-8 text-center">
          <p className="text-sm text-[var(--text-tertiary)]">
            No supporting information found.
          </p>
          <p className="text-xs text-[var(--text-tertiary)] mt-1">
            The uploaded source did not contain enough information to generate sections.
          </p>
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center gap-3">
        <button
          onClick={onAccept}
          disabled={populatedSections === 0}
          className="btn-neumorphic-primary py-3 px-6 flex-1 text-sm flex items-center justify-center gap-2 disabled:opacity-50"
        >
          <Check size={16} />
          Accept Draft
        </button>
        <button
          onClick={onReject}
          className="btn-neumorphic-secondary py-3 px-6 text-sm flex items-center justify-center gap-2"
        >
          <X size={16} />
          Discard
        </button>
      </div>
    </div>
  );
}
