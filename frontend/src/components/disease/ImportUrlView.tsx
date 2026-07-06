import { useState } from 'react';
import { Link, ArrowLeft, AlertCircle, Loader2, Globe } from 'lucide-react';

interface ImportUrlViewProps {
  onBack: () => void;
  onContentImported: (content: string, source: string) => void;
}

const TRUSTED_SOURCES = [
  { label: 'WHO', domain: 'who.int' },
  { label: 'CDC', domain: 'cdc.gov' },
  { label: 'NCBI', domain: 'ncbi.nlm.nih.gov' },
  { label: 'Mayo Clinic', domain: 'mayoclinic.org' },
  { label: 'Medscape', domain: 'medscape.com' },
];

export default function ImportUrlView({ onBack, onContentImported }: ImportUrlViewProps) {
  const [url, setUrl] = useState('');
  const [importing, setImporting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleImport = async () => {
    if (!url.trim()) return;

    setImporting(true);
    setError(null);

    try {
      // 🔄 TODO: Replace with real backend call when endpoint is ready
      // const response = await importUrl(url.trim());
      // onContentImported(response.content, response.source);

      throw new Error(
        'TODO: POST /api/documents/import-url not implemented on backend. ' +
        'Backend should fetch URL, extract article content, and return UrlImportResponse.'
      );
    } catch (err: any) {
      setError(err.message || 'Import failed');
    } finally {
      setImporting(false);
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
            Import Article URL
          </h3>
          <p className="text-xs text-[var(--text-secondary)]">
            Paste a URL from trusted medical sources
          </p>
        </div>
      </div>

      {/* Trusted sources */}
      <div className="flex flex-wrap gap-2">
        {TRUSTED_SOURCES.map((s) => (
          <span
            key={s.domain}
            className="px-2.5 py-1 text-[10px] font-medium rounded-full bg-[var(--accent-primary)]/10 text-[var(--accent-primary)]"
          >
            {s.label}
          </span>
        ))}
      </div>

      {/* URL Input */}
      <div className="card-neumorphic p-4">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-600 shrink-0">
            <Globe size={18} />
          </div>
          <div className="relative flex-1">
            <input
              type="url"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              placeholder="https://www.who.int/news-room/..."
              className="input-neumorphic w-full text-sm pl-9"
              disabled={importing}
              onKeyDown={(e) => e.key === 'Enter' && handleImport()}
            />
            <Link size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-[var(--text-tertiary)]" />
          </div>
        </div>

        <button
          onClick={handleImport}
          disabled={!url.trim() || importing}
          className="btn-neumorphic-primary py-2.5 px-5 w-full text-sm flex items-center justify-center gap-2 disabled:opacity-50"
        >
          {importing ? (
            <Loader2 size={14} className="animate-spin" />
          ) : (
            <Globe size={14} />
          )}
          {importing ? 'Importing...' : 'Import Article'}
        </button>
      </div>

      {/* Error */}
      {error && (
        <div className="flex items-start gap-2 p-3 rounded-lg bg-red-500/10 border border-red-500/20">
          <AlertCircle size={14} className="text-red-600 mt-0.5 shrink-0" />
          <div>
            <p className="text-xs font-semibold text-red-600">Import Error</p>
            <p className="text-[11px] text-red-600/80 mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Info */}
      <div className="p-3 rounded-lg bg-blue-500/5 border border-blue-500/10">
        <p className="text-[11px] text-[var(--text-secondary)] leading-relaxed">
          Only the article content will be extracted. Backend handles all web scraping.
          Frontend never accesses external URLs directly.
        </p>
      </div>
    </div>
  );
}
