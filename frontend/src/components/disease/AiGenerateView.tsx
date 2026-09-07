import { useState } from 'react';
import { ArrowLeft, Sparkles, AlertCircle, Loader2, Info } from 'lucide-react';
import { aiApi } from '../../api/ai';
import type { DraftSectionType } from '../../types/ai';
import type { DiseaseDraftResponse } from '../../types/diseaseDraft';

interface AiGenerateViewProps {
  onBack: () => void;
  onDraftGenerated: (response: DiseaseDraftResponse) => void;
}

const ALL_SECTIONS: DraftSectionType[] = [
  'OVERVIEW',
  'DEFINITION',
  'CAUSES',
  'SYMPTOMS',
  'DIAGNOSIS',
  'TREATMENT',
  'PROGNOSIS',
  'COMPLICATIONS',
  'PREVENTION',
  'EPIDEMIOLOGY',
  'PATHOPHYSIOLOGY',
  'RISK_FACTORS',
  'CLINICAL_FEATURES',
  'INVESTIGATIONS',
  'MANAGEMENT',
  'DIFFERENTIAL_DIAGNOSIS',
  'REFERENCE',
];

const DEFAULT_SECTIONS: DraftSectionType[] = [
  'OVERVIEW',
  'DEFINITION',
  'CAUSES',
  'SYMPTOMS',
  'DIAGNOSIS',
  'TREATMENT',
];

export default function AiGenerateView({ onBack, onDraftGenerated }: AiGenerateViewProps) {
  const [title, setTitle] = useState('');
  const [documentIdStr, setDocumentIdStr] = useState('');
  const [selectedSections, setSelectedSections] = useState<DraftSectionType[]>(DEFAULT_SECTIONS);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const toggleSection = (sec: DraftSectionType) => {
    setSelectedSections((prev) =>
      prev.includes(sec) ? prev.filter((s) => s !== sec) : [...prev, sec]
    );
  };

  const handleGenerate = async () => {
    if (!title.trim() || selectedSections.length === 0) return;

    setGenerating(true);
    setError(null);

    try {
      const documentId = documentIdStr.trim() ? parseInt(documentIdStr.trim(), 10) : undefined;
      const response = await aiApi.generateAiDraft({
        title: title.trim(),
        sections: selectedSections,
        ...(documentId && !isNaN(documentId) ? { documentId } : {}),
      });
      onDraftGenerated(response);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Generation failed');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <button onClick={onBack} className="btn-neumorphic-secondary p-2 rounded-xl">
          <ArrowLeft size={16} />
        </button>
        <div>
          <h3 className="font-display text-lg font-bold text-[var(--text-primary)]">
            AI Generate Draft
          </h3>
          <p className="text-xs text-[var(--text-secondary)]">
            Generate a structured disease draft using AI
          </p>
        </div>
      </div>

      {/* Notice */}
      <div className="flex items-start gap-2 p-3 rounded-lg bg-amber-500/5 border border-amber-500/10">
        <Info size={14} className="text-amber-600 mt-0.5 shrink-0" />
        <p className="text-[11px] text-amber-700 leading-relaxed">
          Specify a draft title and select sections to generate. Provide an optional Document ID for grounding.
        </p>
      </div>

      {/* Inputs */}
      <div className="card-neumorphic p-4 space-y-4">
        <div>
          <label className="block text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider mb-2">
            Draft Title *
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Pneumonia"
            className="input-neumorphic w-full text-sm"
            disabled={generating}
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider mb-2">
            Document ID (Optional)
          </label>
          <input
            type="number"
            value={documentIdStr}
            onChange={(e) => setDocumentIdStr(e.target.value)}
            placeholder="e.g. 1"
            className="input-neumorphic w-full text-sm"
            disabled={generating}
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider mb-2">
            Sections to Generate * ({selectedSections.length} selected)
          </label>
          <div className="grid grid-cols-2 gap-2 max-h-48 overflow-y-auto p-2 bg-black/5 dark:bg-white/5 rounded-lg">
            {ALL_SECTIONS.map((sec) => (
              <label key={sec} className="flex items-center gap-2 text-xs cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={selectedSections.includes(sec)}
                  onChange={() => toggleSection(sec)}
                  disabled={generating}
                  className="rounded text-[var(--accent-primary)] focus:ring-0"
                />
                <span className="text-[var(--text-primary)]">{sec}</span>
              </label>
            ))}
          </div>
        </div>

        <button
          onClick={handleGenerate}
          disabled={!title.trim() || selectedSections.length === 0 || generating}
          className="btn-neumorphic-primary py-2.5 px-5 w-full text-sm flex items-center justify-center gap-2 disabled:opacity-50"
        >
          {generating ? (
            <Loader2 size={14} className="animate-spin" />
          ) : (
            <Sparkles size={14} />
          )}
          {generating ? 'Generating...' : 'Generate Draft'}
        </button>
      </div>

      {/* Error */}
      {error && (
        <div className="flex items-start gap-2 p-3 rounded-lg bg-red-500/10 border border-red-500/20">
          <AlertCircle size={14} className="text-red-600 mt-0.5 shrink-0" />
          <div>
            <p className="text-xs font-semibold text-red-600">Generation Error</p>
            <p className="text-[11px] text-red-600/80 mt-0.5">{error}</p>
          </div>
        </div>
      )}
    </div>
  );
}
