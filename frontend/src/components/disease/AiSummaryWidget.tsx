import { useState, useEffect } from 'react';
import { Sparkles, Loader2, AlertCircle, FileText } from 'lucide-react';
import { aiApi } from '../../api/ai';
import type { SummaryResponse, SummaryType } from '../../types/ai';

interface AiSummaryWidgetProps {
  diseaseId: number;
}

const SUMMARY_TYPES: { type: SummaryType; label: string; desc: string }[] = [
  { type: 'STUDENT', label: 'Student', desc: 'Comprehensive conceptual overview for learning' },
  { type: 'CLINICAL', label: 'Clinical', desc: 'Practical diagnostic and management key points' },
  { type: 'EXAM', label: 'Exam Prep', desc: 'High-yield facts and board exam highlights' },
  { type: 'QUICK_REVISION', label: 'Quick Revision', desc: 'Ultra-concise bullet-point summary' },
];

export default function AiSummaryWidget({ diseaseId }: AiSummaryWidgetProps) {
  const [summaries, setSummaries] = useState<SummaryResponse[]>([]);
  const [selectedType, setSelectedType] = useState<SummaryType>('STUDENT');
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeSummary, setActiveSummary] = useState<SummaryResponse | null>(null);

  const fetchSummaries = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await aiApi.listSummaries(diseaseId);
      setSummaries(data);
      if (data.length > 0) {
        setActiveSummary(data[0]);
      }
    } catch (err: any) {
      setSummaries([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (diseaseId) {
      fetchSummaries();
    }
  }, [diseaseId]);

  const handleGenerate = async () => {
    setGenerating(true);
    setError(null);
    try {
      const summary = await aiApi.generateSummary(diseaseId, { summaryType: selectedType });
      setSummaries((prev) => [summary, ...prev.filter((s) => s.id !== summary.id)]);
      setActiveSummary(summary);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to generate summary');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="card-neumorphic p-5 space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Sparkles className="text-[var(--accent-primary)]" size={18} />
          <h3 className="font-display font-bold text-base text-[var(--text-primary)]">
            AI Summaries
          </h3>
        </div>
      </div>

      {/* Summary Type Selector & Generate Action */}
      <div className="space-y-3">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
          {SUMMARY_TYPES.map((t) => (
            <button
              key={t.type}
              type="button"
              onClick={() => setSelectedType(t.type)}
              disabled={generating}
              className={`p-2.5 rounded-xl border text-left transition-all ${
                selectedType === t.type
                  ? 'border-[var(--accent-primary)] bg-[var(--accent-primary)]/10 text-[var(--text-primary)]'
                  : 'border-transparent bg-black/5 dark:bg-white/5 text-[var(--text-secondary)] hover:bg-black/10'
              }`}
            >
              <div className="text-xs font-semibold">{t.label}</div>
              <div className="text-[10px] text-[var(--text-tertiary)] line-clamp-1 mt-0.5">{t.desc}</div>
            </button>
          ))}
        </div>

        <button
          onClick={handleGenerate}
          disabled={generating}
          className="btn-neumorphic-primary py-2 px-4 w-full text-xs flex items-center justify-center gap-2 disabled:opacity-50"
        >
          {generating ? <Loader2 size={14} className="animate-spin" /> : <Sparkles size={14} />}
          {generating ? 'Generating Summary...' : `Generate ${selectedType} Summary`}
        </button>
      </div>

      {error && (
        <div className="flex items-center gap-2 p-3 rounded-lg bg-red-500/10 border border-red-500/20 text-xs text-red-600">
          <AlertCircle size={14} className="shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Summary Display */}
      {loading ? (
        <div className="flex items-center justify-center py-6 text-xs text-[var(--text-tertiary)] gap-2">
          <Loader2 size={14} className="animate-spin" />
          Loading summaries...
        </div>
      ) : activeSummary ? (
        <div className="p-4 rounded-xl bg-black/5 dark:bg-white/5 space-y-3">
          <div className="flex items-center justify-between text-xs border-b border-black/10 dark:border-white/10 pb-2">
            <span className="font-semibold text-[var(--accent-primary)] uppercase tracking-wider">
              {activeSummary.summaryType} Summary (v{activeSummary.version})
            </span>
            <span className="text-[10px] text-[var(--text-tertiary)]">
              Model: {activeSummary.model || 'Default'} ({activeSummary.latencyMs}ms)
            </span>
          </div>

          <div className="text-xs leading-relaxed text-[var(--text-primary)] whitespace-pre-wrap">
            {activeSummary.content}
          </div>

          {summaries.length > 1 && (
            <div className="pt-2 flex items-center gap-1.5 overflow-x-auto text-[11px]">
              <span className="text-[var(--text-tertiary)] shrink-0">Other summaries:</span>
              {summaries.map((s) => (
                <button
                  key={s.id}
                  onClick={() => setActiveSummary(s)}
                  className={`px-2 py-0.5 rounded text-[10px] ${
                    activeSummary.id === s.id
                      ? 'bg-[var(--accent-primary)] text-white'
                      : 'bg-black/10 dark:bg-white/10 text-[var(--text-secondary)]'
                  }`}
                >
                  {s.summaryType} (v{s.version})
                </button>
              ))}
            </div>
          )}
        </div>
      ) : (
        <div className="text-center py-6 text-xs text-[var(--text-tertiary)]">
          <FileText size={20} className="mx-auto mb-1 opacity-50" />
          No summary generated yet for this disease.
        </div>
      )}
    </div>
  );
}
