import { useState } from 'react';
import { ArrowLeft, Sparkles, AlertCircle, Loader2, FileText, Info } from 'lucide-react';

interface AiGenerateViewProps {
  onBack: () => void;
  onDraftGenerated: (response: any) => void;
}

export default function AiGenerateView({ onBack, onDraftGenerated }: AiGenerateViewProps) {
  const [diseaseName, setDiseaseName] = useState('');
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleGenerate = async () => {
    if (!diseaseName.trim()) return;

    setGenerating(true);
    setError(null);

    try {
      // 🔄 TODO: Replace with real backend call when endpoint is ready
      // const response = await generateAiDraft({
      //   diseaseName: diseaseName.trim(),
      //   documentIds: [],
      // });
      // onDraftGenerated(response);

      throw new Error(
        'TODO: POST /api/ai/draft/generate not implemented on backend. ' +
        'Backend should accept source text/document IDs, ' +
        'generate structured draft with sources, and return DraftGenerationResponse.'
      );
    } catch (err: any) {
      setError(err.message || 'Generation failed');
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
            Generate a structured disease draft from source documents
          </p>
        </div>
      </div>

      {/* Grounding Notice */}
      <div className="flex items-start gap-2 p-3 rounded-lg bg-amber-500/5 border border-amber-500/10">
        <Info size={14} className="text-amber-600 mt-0.5 shrink-0" />
        <p className="text-[11px] text-amber-700 leading-relaxed">
          This is a <strong>source-grounded draft assistant</strong>, not a medical expert.
          Upload documents first — the AI only generates content from your provided sources.
          It never uses general knowledge or fabricates medical information.
        </p>
      </div>

      {/* Disease Name */}
      <div className="card-neumorphic p-4">
        <label className="block text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider mb-2">
          Disease Name
        </label>
        <input
          type="text"
          value={diseaseName}
          onChange={(e) => setDiseaseName(e.target.value)}
          placeholder="e.g. Pneumonia"
          className="input-neumorphic w-full text-sm"
          disabled={generating}
        />

        <div className="mt-4 p-3 rounded-lg bg-blue-500/5 border border-blue-500/10">
          <div className="flex items-center gap-2 mb-2">
            <FileText size={12} className="text-[var(--accent-primary)]" />
            <span className="text-xs font-semibold text-[var(--text-primary)]">
              RAG Workflow (Future)
            </span>
          </div>
          <ol className="space-y-1 text-[11px] text-[var(--text-secondary)] list-decimal list-inside">
            <li>Upload medical document</li>
            <li>Backend extracts and chunks text</li>
            <li>Backend generates embeddings</li>
            <li>Backend retrieves relevant chunks</li>
            <li>AI generates draft with source citations</li>
          </ol>
        </div>

        <button
          onClick={handleGenerate}
          disabled={!diseaseName.trim() || generating}
          className="btn-neumorphic-primary py-2.5 px-5 w-full mt-4 text-sm flex items-center justify-center gap-2 disabled:opacity-50"
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
