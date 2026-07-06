import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Brain, History, Bookmark, FileText, Download,
  Sparkles, Target, Search, ChevronRight, Star, Clock, AlertCircle, ClipboardCheck
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function SymptomCheckerPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [selectedSymptoms, setSelectedSymptoms] = useState<string[]>([]);

  const toggleSymptom = (symptom: string) => {
    setSelectedSymptoms(prev =>
      prev.includes(symptom) ? prev.filter(s => s !== symptom) : [...prev, symptom]
    );
  };

  const handleAnalyze = () => {
    // Guests can analyze, but might be prompted to log in for results or specific features later.
    // For now, the analysis itself is permitted.
    console.log('Analyzing symptoms:', selectedSymptoms);
    // TODO: Implement actual analysis API call
  };

  const symptoms = ['Cough', 'Fever', 'Dyspnea', 'Fatigue', 'Chest Pain', 'Nausea', 'Headache', 'Myalgia'];
  const recentSearches = user ? [
    'Cough + Fever + Dyspnea',
    'Chest Pain + Nausea',
    'Headache + Fatigue'
  ] : [];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><Brain size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Symptom Checker</h1>
              <p className="text-sm text-[var(--text-secondary)]">Analyze symptoms and explore differential diagnoses</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left: Main Checker */}
            <div className="lg:col-span-2 space-y-6">
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Select Symptoms</h2>
                <div className="flex flex-wrap gap-2 mb-6">
                  {symptoms.map((symptom) => (
                    <button
                      key={symptom}
                      onClick={() => toggleSymptom(symptom)}
                      className={`py-2 px-4 rounded-full text-sm font-medium transition-all ${
                        selectedSymptoms.includes(symptom)
                          ? 'bg-[var(--accent-primary)] text-white shadow-md'
                          : 'bg-[var(--surface-primary)] shadow-[inset_3px_3px_6px_var(--shadow-dark),inset_-3px_-3px_6px_var(--shadow-light)] text-[var(--text-primary)] hover:bg-[var(--surface-hover)]'
                      }`}
                    >
                      {symptom}
                    </button>
                  ))}
                </div>
                <button
                  onClick={handleAnalyze}
                  disabled={selectedSymptoms.length === 0}
                  className={`btn-neumorphic-primary py-3 px-8 w-full flex items-center justify-center gap-2 ${
                    selectedSymptoms.length === 0 ? 'opacity-50 cursor-not-allowed' : ''
                  }`}
                >
                  <Search size={16} /> Analyze Symptoms
                </button>
              </div>

              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Differential Diagnosis</h2>
                {selectedSymptoms.length === 0 ? (
                  <div className="text-center py-8">
                    <Brain size={40} className="mx-auto mb-3 text-[var(--text-tertiary)]" />
                    <p className="text-sm text-[var(--text-secondary)]">Select symptoms above to see potential diagnoses</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {[...Array(3)].map((_, i) => (
                      <div key={i} className="depth-layer-1 rounded-2xl p-4 flex items-center justify-between">
                        <div>
                          <p className="font-semibold text-sm text-[var(--text-primary)]">Potential Diagnosis {i+1}</p>
                          <p className="text-xs text-[var(--text-secondary)]">Confidence: {90 - i * 15}%</p>
                        </div>
                        <Link to="/explorer" className="text-xs text-[var(--accent-primary)] hover:underline">View</Link>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Right: Guest sees Recent Searches + Actions only if logged in */}
            {user && (
              <div className="space-y-6">
                <div className="card-neumorphic p-6">
                  <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4 flex items-center gap-2">
                    <History size={18} /> Recent Searches
                  </h2>
                  <div className="space-y-2">
                    {recentSearches.map((search, i) => (
                      <button key={i} className="w-full depth-layer-1 rounded-xl p-3 text-xs text-left hover:bg-[var(--surface-hover)] transition-all">
                        <p className="text-[var(--text-primary)]">{search}</p>
                        <p className="text-[var(--text-tertiary)] flex items-center gap-1 mt-1"><Clock size={10} /> {i + 1}h ago</p>
                      </button>
                    ))}
                    <Link to="/history" className="text-xs text-[var(--accent-primary)] hover:underline mt-2 inline-block">View full history</Link>
                  </div>
                </div>

                <div className="card-neumorphic p-6">
                  <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Actions</h2>
                  <div className="space-y-2">
                    <button className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2">
                      <FileText size={14} /> Generate Disease Draft
                    </button>
                    <button className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2">
                      <Sparkles size={14} /> Generate Case
                    </button>
                    <button className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2">
                      <Download size={14} /> Export PDF
                    </button>
                    <Link to="/bookmarks" className="btn-neumorphic-primary py-2 px-4 w-full text-sm flex items-center justify-center gap-2">
                      <Bookmark size={14} /> Saved Reports
                    </Link>
                  </div>
                </div>
              </div>
            )}
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
