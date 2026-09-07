import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { aiAdminApi } from '../../api/aiAdmin';
import { subscribeAiStream } from '../../api/aiStream';
import type { PromptTemplateResponse, ModelInfo, AiStreamChunkResponse } from '../../types/aiAdmin';
import { ShieldClose, Cpu, Plus, Trash2, Radio, CheckCircle, AlertCircle, RefreshCw } from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function AdminAiPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const queryClient = useQueryClient();

  const [activeTab, setActiveTab] = useState<'prompts' | 'models' | 'sse'>('prompts');
  const [formOpen, setFormOpen] = useState(false);
  const [promptForm, setPromptForm] = useState<{
    code: string;
    name: string;
    systemPrompt: string;
    userPromptTemplate: string;
  }>({
    code: '',
    name: '',
    systemPrompt: '',
    userPromptTemplate: '',
  });

  // SSE State
  const [sseConnected, setSseConnected] = useState(false);
  const [sseConnecting, setSseConnecting] = useState(false);
  const [sseChunks, setSseChunks] = useState<AiStreamChunkResponse[]>([]);
  const [sseError, setSseError] = useState<string | null>(null);

  // Queries
  const { data: prompts, isLoading: promptsLoading } = useQuery<PromptTemplateResponse[]>({
    queryKey: ['aiPrompts'],
    queryFn: aiAdminApi.listPrompts,
    enabled: role === 'ADMIN',
  });

  const { data: modelsMap, isLoading: modelsLoading } = useQuery<Record<string, ModelInfo[]>>({
    queryKey: ['aiModels'],
    queryFn: () => aiAdminApi.listModels(),
    enabled: role === 'ADMIN' && activeTab === 'models',
  });

  const refreshPrompts = () => queryClient.invalidateQueries({ queryKey: ['aiPrompts'] });

  const createMutation = useMutation({
    mutationFn: (dto: Partial<PromptTemplateResponse>) => aiAdminApi.createPrompt(dto),
    onSuccess: () => {
      setFormOpen(false);
      setPromptForm({ code: '', name: '', systemPrompt: '', userPromptTemplate: '' });
      refreshPrompts();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => aiAdminApi.deletePrompt(id),
    onSuccess: refreshPrompts,
  });

  // SSE test handler
  const handleToggleSse = () => {
    if (sseConnected || sseConnecting) {
      setSseConnected(false);
      setSseConnecting(false);
      return;
    }

    setSseConnecting(true);
    setSseError(null);

    const unsubscribe = subscribeAiStream({
      onConnected: () => {
        setSseConnecting(false);
        setSseConnected(true);
      },
      onChunk: (chunk) => {
        setSseChunks((prev) => [chunk, ...prev.slice(0, 19)]);
      },
      onError: (err) => {
        setSseConnecting(false);
        setSseConnected(false);
        setSseError(err.message);
      },
      onClose: () => {
        setSseConnecting(false);
        setSseConnected(false);
      },
    });

    return unsubscribe;
  };

  useEffect(() => {
    let cleanup: (() => void) | undefined;
    if (activeTab === 'sse' && sseConnected) {
      cleanup = handleToggleSse();
    }
    return () => {
      if (cleanup) cleanup();
    };
  }, [activeTab]);

  if (role !== 'ADMIN') {
    return (
      <div className="min-h-screen pt-32 pb-24 px-6 text-center">
        <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
        <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
        <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">
          Go to Dashboard
        </Link>
      </div>
    );
  }

  const promptList = Array.isArray(prompts) ? prompts : [];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well">
                <Cpu size={22} />
              </div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">
                  AI Gateway & Prompts
                </h1>
                <p className="text-sm text-[var(--text-secondary)]">
                  Manage prompt templates, discover AI models, and inspect SSE streams
                </p>
              </div>
            </div>
            {activeTab === 'prompts' && (
              <button
                onClick={() => setFormOpen(true)}
                className="btn-neumorphic-secondary py-2 px-4 text-sm flex items-center gap-2"
              >
                <Plus size={14} /> New Prompt
              </button>
            )}
          </div>

          {/* Navigation Tabs */}
          <div className="flex gap-2 mb-6 border-b border-black/10 dark:border-white/10 pb-3">
            <button
              onClick={() => setActiveTab('prompts')}
              className={`py-2 px-4 rounded-xl text-xs font-semibold transition-all ${
                activeTab === 'prompts'
                  ? 'btn-neumorphic-primary'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              }`}
            >
              Prompt Templates ({promptList.length})
            </button>
            <button
              onClick={() => setActiveTab('models')}
              className={`py-2 px-4 rounded-xl text-xs font-semibold transition-all ${
                activeTab === 'models'
                  ? 'btn-neumorphic-primary'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              }`}
            >
              Gateway Models
            </button>
            <button
              onClick={() => setActiveTab('sse')}
              className={`py-2 px-4 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5 ${
                activeTab === 'sse'
                  ? 'btn-neumorphic-primary'
                  : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
              }`}
            >
              <Radio size={14} className={sseConnected ? 'text-green-500 animate-pulse' : ''} />
              Live SSE Stream
            </button>
          </div>

          {/* Tab 1: Prompts */}
          {activeTab === 'prompts' && (
            <div>
              {formOpen && (
                <form
                  onSubmit={(e) => {
                    e.preventDefault();
                    if (promptForm.code.trim() && promptForm.name.trim()) {
                      createMutation.mutate({
                        code: promptForm.code.trim(),
                        name: promptForm.name.trim(),
                        systemPrompt: promptForm.systemPrompt,
                        userPromptTemplate: promptForm.userPromptTemplate,
                        active: true,
                      });
                    }
                  }}
                  className="card-neumorphic p-5 mb-6 space-y-4"
                >
                  <h3 className="text-sm font-bold text-[var(--text-primary)]">
                    Create New Prompt Template
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <input
                      required
                      placeholder="Template Code (e.g. DISEASE_SUMMARY)"
                      value={promptForm.code}
                      onChange={(e) => setPromptForm({ ...promptForm, code: e.target.value })}
                      className="input-neumorphic w-full text-xs"
                    />
                    <input
                      required
                      placeholder="Template Name"
                      value={promptForm.name}
                      onChange={(e) => setPromptForm({ ...promptForm, name: e.target.value })}
                      className="input-neumorphic w-full text-xs"
                    />
                  </div>
                  <textarea
                    placeholder="System Prompt..."
                    value={promptForm.systemPrompt}
                    onChange={(e) => setPromptForm({ ...promptForm, systemPrompt: e.target.value })}
                    className="input-neumorphic w-full text-xs min-h-[80px]"
                  />
                  <textarea
                    placeholder="User Prompt Template (e.g. {{diseaseName}})"
                    value={promptForm.userPromptTemplate}
                    onChange={(e) => setPromptForm({ ...promptForm, userPromptTemplate: e.target.value })}
                    className="input-neumorphic w-full text-xs min-h-[80px]"
                  />
                  <div className="flex gap-2">
                    <button
                      type="submit"
                      className="btn-neumorphic-primary px-4 py-2 text-xs"
                      disabled={createMutation.isPending}
                    >
                      {createMutation.isPending ? 'Saving...' : 'Save Template'}
                    </button>
                    <button
                      type="button"
                      onClick={() => setFormOpen(false)}
                      className="btn-neumorphic-secondary px-4 py-2 text-xs"
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              )}

              {promptsLoading ? (
                <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">
                  Loading prompt templates...
                </div>
              ) : promptList.length === 0 ? (
                <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">
                  No prompt templates registered yet
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {promptList.map((p) => (
                    <div key={p.id} className="card-neumorphic p-5 space-y-3">
                      <div className="flex items-center justify-between">
                        <div>
                          <span className="text-[10px] font-mono uppercase bg-black/10 dark:bg-white/10 px-2 py-0.5 rounded text-[var(--accent-primary)]">
                            {p.code}
                          </span>
                          <h4 className="font-bold text-sm text-[var(--text-primary)] mt-1">{p.name}</h4>
                        </div>
                        <button
                          aria-label="Delete prompt template"
                          onClick={() => window.confirm(`Delete ${p.code}?`) && deleteMutation.mutate(p.id)}
                          className="p-1.5 rounded text-red-500 hover:bg-red-500/10"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                      {p.description && (
                        <p className="text-xs text-[var(--text-secondary)]">{p.description}</p>
                      )}
                      {p.systemPrompt && (
                        <div className="text-[11px] bg-black/5 dark:bg-white/5 p-2 rounded text-[var(--text-tertiary)] font-mono line-clamp-2">
                          SYS: {p.systemPrompt}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Tab 2: Gateway Models */}
          {activeTab === 'models' && (
            <div>
              {modelsLoading ? (
                <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">
                  Loading available AI models...
                </div>
              ) : !modelsMap || Object.keys(modelsMap).length === 0 ? (
                <div className="card-neumorphic p-8 text-center text-sm text-[var(--text-tertiary)]">
                  No models returned by gateway
                </div>
              ) : (
                <div className="space-y-6">
                  {Object.entries(modelsMap).map(([provider, models]) => (
                    <div key={provider} className="card-neumorphic p-5 space-y-3">
                      <h3 className="font-bold text-sm uppercase text-[var(--accent-primary)] border-b border-black/10 dark:border-white/10 pb-2">
                        Provider: {provider}
                      </h3>
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                        {models.map((m) => (
                          <div key={m.id} className="p-3 rounded-lg bg-black/5 dark:bg-white/5 space-y-1 text-xs">
                            <div className="font-semibold text-[var(--text-primary)]">{m.name || m.id}</div>
                            <div className="text-[10px] text-[var(--text-tertiary)]">Context: {m.contextWindow} tokens</div>
                            <div className="flex gap-2 pt-1 text-[10px]">
                              {m.supportsStreaming && <span className="text-green-600">Streaming</span>}
                              {m.supportsFunctions && <span className="text-blue-600">Functions</span>}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Tab 3: SSE Stream */}
          {activeTab === 'sse' && (
            <div className="card-neumorphic p-6 space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-bold text-base text-[var(--text-primary)]">AI Realtime SSE Stream</h3>
                  <p className="text-xs text-[var(--text-secondary)]">
                    Target: <code className="font-mono text-[var(--accent-primary)]">GET /api/ai/stream</code>
                  </p>
                </div>
                <button
                  onClick={handleToggleSse}
                  className={`py-2 px-5 text-xs font-semibold rounded-xl flex items-center gap-2 transition-all ${
                    sseConnected
                      ? 'bg-red-500/20 text-red-600 border border-red-500/30 hover:bg-red-500/30'
                      : 'btn-neumorphic-primary'
                  }`}
                >
                  {sseConnecting ? (
                    <RefreshCw size={14} className="animate-spin" />
                  ) : sseConnected ? (
                    <AlertCircle size={14} />
                  ) : (
                    <Radio size={14} />
                  )}
                  {sseConnecting ? 'Connecting...' : sseConnected ? 'Disconnect SSE' : 'Connect SSE'}
                </button>
              </div>

              {/* Status indicator */}
              <div className="flex items-center gap-2 text-xs">
                Status:{' '}
                {sseConnected ? (
                  <span className="flex items-center gap-1 text-green-600 font-semibold">
                    <CheckCircle size={12} /> Connected (ai-stream-connected)
                  </span>
                ) : sseError ? (
                  <span className="text-red-600 font-semibold">{sseError}</span>
                ) : (
                  <span className="text-[var(--text-tertiary)]">Disconnected</span>
                )}
              </div>

              {/* Chunks Output */}
              <div className="space-y-2">
                <label className="block text-xs font-semibold text-[var(--text-tertiary)] uppercase tracking-wider">
                  Received Stream Chunks ({sseChunks.length})
                </label>
                <div className="min-h-[160px] max-h-[300px] overflow-y-auto p-3 rounded-lg bg-black/10 dark:bg-white/5 font-mono text-xs text-[var(--text-primary)] space-y-2">
                  {sseChunks.length === 0 ? (
                    <div className="text-[var(--text-tertiary)] italic">No stream chunks received yet</div>
                  ) : (
                    sseChunks.map((c, i) => (
                      <div key={i} className="p-2 rounded bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5">
                        <div className="text-[10px] text-[var(--text-tertiary)] flex justify-between">
                          <span>Chunk #{sseChunks.length - i}</span>
                          <span>{c.done ? 'DONE' : 'IN_PROGRESS'}</span>
                        </div>
                        <div className="mt-1">{c.chunk}</div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>
          )}
        </AnimatedSection>
      </div>
    </div>
  );
}
