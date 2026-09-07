import { useState, useRef } from 'react';
import { documentApi } from '../../api/document';
import { Upload, FileText, X, AlertCircle, ArrowLeft, Loader2 } from 'lucide-react';

interface UploadDocumentViewProps {
  onBack: () => void;
  onDocumentUploaded: (document: import("../../api/document").Document) => void;
}

const ACCEPTED_TYPES = [
  'application/pdf',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'text/plain',
  'text/markdown',
];

const ACCEPTED_EXTENSIONS = '.pdf,.docx,.txt,.md';

export default function UploadDocumentView({
  onBack,
  onDocumentUploaded,
}: UploadDocumentViewProps) {
  const [file, setFile] = useState<File | null>(null);
  const [dragOver, setDragOver] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const validateFile = (f: File): string | null => {
    if (!ACCEPTED_TYPES.includes(f.type) && !f.name.match(/\.(pdf|docx|txt|md)$/i)) {
      return 'Unsupported file type. Accepted: PDF, DOCX, TXT, Markdown';
    }
    if (f.size > 50 * 1024 * 1024) {
      return 'File too large. Maximum size: 50MB';
    }
    return null;
  };

  const handleFile = (f: File) => {
    setError(null);
    const err = validateFile(f);
    if (err) {
      setError(err);
      return;
    }
    setFile(f);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    const f = e.dataTransfer.files[0];
    if (f) handleFile(f);
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    setError(null);

    try {
      // 🔄 TODO: Replace with real backend call when endpoint is ready
      // const response = await uploadDocument(file);
      // onTextExtracted(extracted.text, extracted.filename);

      // For now, simulate text extraction failure to signal missing backend
      throw new Error(
        'TODO: POST /api/documents/upload not implemented on backend. ' +
        'Backend should accept multipart upload, validate type, store file locally, ' +
        'extract text, and return ExtractedTextResponse.'
      );
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const removeFile = () => {
    setFile(null);
    setError(null);
    if (inputRef.current) inputRef.current.value = '';
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
            Upload Medical Document
          </h3>
          <p className="text-xs text-[var(--text-secondary)]">
            Supported: PDF, DOCX, TXT, Markdown
          </p>
        </div>
      </div>

      {/* Dropzone */}
      {!file && (
        <div
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleDrop}
          onClick={() => inputRef.current?.click()}
          className={`card-neumorphic p-10 text-center cursor-pointer transition-all ${
            dragOver ? 'ring-2 ring-[var(--accent-primary)]' : ''
          }`}
        >
          <input
            ref={inputRef}
            type="file"
            accept={ACCEPTED_EXTENSIONS}
            className="hidden"
            onChange={(e) => {
              const f = e.target.files?.[0];
              if (f) handleFile(f);
            }}
          />
          <div className="flex flex-col items-center gap-3">
            <div className="p-3 rounded-xl bg-[var(--accent-primary)]/10 text-[var(--accent-primary)]">
              <Upload size={24} />
            </div>
            <div>
              <p className="text-sm font-semibold text-[var(--text-primary)]">
                Drop your document here
              </p>
              <p className="text-xs text-[var(--text-secondary)] mt-1">
                or click to browse files
              </p>
            </div>
            <div className="flex gap-2 text-[10px] text-[var(--text-tertiary)]">
              <span className="px-2 py-0.5 rounded bg-[var(--bg-secondary)]">PDF</span>
              <span className="px-2 py-0.5 rounded bg-[var(--bg-secondary)]">DOCX</span>
              <span className="px-2 py-0.5 rounded bg-[var(--bg-secondary)]">TXT</span>
              <span className="px-2 py-0.5 rounded bg-[var(--bg-secondary)]">MD</span>
            </div>
          </div>
        </div>
      )}

      {/* Selected file */}
      {file && (
        <div className="card-neumorphic p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3 min-w-0">
              <div className="p-2 rounded-lg bg-blue-500/10 text-blue-600 shrink-0">
                <FileText size={18} />
              </div>
              <div className="min-w-0">
                <p className="text-sm font-semibold text-[var(--text-primary)] truncate">
                  {file.name}
                </p>
                <p className="text-xs text-[var(--text-secondary)]">
                  {(file.size / 1024).toFixed(1)} KB
                </p>
              </div>
            </div>
            <button
              onClick={removeFile}
              className="btn-neumorphic-secondary p-1.5 rounded-lg shrink-0"
              disabled={uploading}
            >
              <X size={14} />
            </button>
          </div>

          {/* Action */}
          <button
            onClick={handleUpload}
            disabled={uploading}
            className="btn-neumorphic-primary py-2.5 px-5 w-full mt-4 text-sm flex items-center justify-center gap-2"
          >
            {uploading ? (
              <Loader2 size={14} className="animate-spin" />
            ) : (
              <Upload size={14} />
            )}
            {uploading ? 'Uploading...' : 'Upload & Extract Text'}
          </button>
        </div>
      )}

      {/* Error */}
      {error && (
        <div className="flex items-start gap-2 p-3 rounded-lg bg-red-500/10 border border-red-500/20">
          <AlertCircle size={14} className="text-red-600 mt-0.5 shrink-0" />
          <div>
            <p className="text-xs font-semibold text-red-600">Upload Error</p>
            <p className="text-[11px] text-red-600/80 mt-0.5">{error}</p>
          </div>
        </div>
      )}
    </div>
  );
}



