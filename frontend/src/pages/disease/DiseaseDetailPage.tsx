import { useParams, Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import {
  ArrowLeft, Bookmark, Share2, BookOpen, Clock, GraduationCap,
  ChevronRight, Sparkles, Target, Brain, FileText, MessageSquare, AlertCircle
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/motion/MotionWrappers';

export default function DiseaseDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [isBookmarked, setIsBookmarked] = useState(true);
  const [readingProgress, setReadingProgress] = useState(65);

  const disease = {
    id: id || '1',
    name: 'Pneumonia',
    description: 'Pneumonia is an infection that inflames the air sacs in one or both lungs.',
    category: 'Infectious Diseases',
    difficulty: 'Medium',
    estimatedReadTime: '15 min',
    progress: 65,
    lastUpdated: '2 days ago',
  };

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-4xl mx-auto">
        <AnimatedSection>
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <button onClick={() => navigate('/explorer')} className="flex items-center gap-2 text-[var(--text-secondary)] hover:text-[var(--text-primary)]">
              <ArrowLeft size={18} /> Back to Diseases
            </button>
            <div className="flex items-center gap-3">
              <button onClick={() => setIsBookmarked(!isBookmarked)} className="p-2 rounded-lg hover:bg-[var(--surface-hover)]">
                {isBookmarked ? <Bookmark fill="var(--accent-primary)" size={20} className="text-[var(--accent-primary)]" /> : <Bookmark size={20} />}
              </button>
              <button className="p-2 rounded-lg hover:bg-[var(--surface-hover)]">
                <Share2 size={20} />
              </button>
            </div>
          </div>

          {/* Title Section */}
          <div className="card-neumorphic p-8 mb-8">
            <h1 className="font-display text-4xl font-bold text-[var(--text-primary)] mb-2">{disease.name}</h1>
            <p className="text-sm text-[var(--text-secondary)] mb-4">{disease.description}</p>
            <div className="flex flex-wrap items-center gap-4 text-sm">
              <span className="stat-pill">
                <GraduationCap size={14} className="text-[var(--accent-primary)]" />
                {disease.difficulty}
              </span>
              <span className="stat-pill">
                <Clock size={14} className="text-[var(--accent-primary)]" />
                {disease.estimatedReadTime}
              </span>
              <span className="stat-pill">
                <BookOpen size={14} className="text-[var(--accent-primary)]" />
                {disease.category}
              </span>
            </div>
          </div>

          {/* Reading Progress */}
          <div className="card-neumorphic p-6 mb-8">
            <div className="flex items-center justify-between mb-3">
              <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">Your Progress</h2>
              <span className="text-sm font-semibold text-[var(--accent-primary)]">{readingProgress}%</span>
            </div>
            <div className="w-full h-3 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_6px_var(--shadow-dark),inset_-2px_-2px_6px_var(--shadow-light)] overflow-hidden">
              <div className="h-full bg-gradient-to-r from-[var(--accent-primary)] to-emerald-300 rounded-full transition-all" style={{ width: `${readingProgress}%` }}></div>
            </div>
            <p className="text-xs text-[var(--text-secondary)] mt-3">Last read: 2 hours ago</p>
          </div>

          {/* Main Content Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
            {/* Left: Content */}
            <div className="lg:col-span-2 space-y-6">
              <AnimatedSection>
                <div className="card-neumorphic p-8">
                  <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Definition</h2>
                  <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
                    Pneumonia is an infection that inflames the air sacs (alveoli) in one or both lungs. The air sacs may fill with fluid or pus (purulent material), causing a cough with phlegm or pus, fever, chills, and difficulty breathing. Bacterial, viral, or fungal infection can cause pneumonia.
                  </p>
                  <p className="text-[var(--text-secondary)] leading-relaxed">
                    Pneumonia can range in severity from mild "walking pneumonia" that can be treated on an outpatient basis to severe pneumonia requiring hospital admission and mechanical ventilation.
                  </p>
                </div>
              </AnimatedSection>

              <AnimatedSection>
                <div className="card-neumorphic p-8">
                  <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Etiology</h2>
                  <ul className="space-y-3 text-[var(--text-secondary)]">
                    {[
                      'Bacterial: Streptococcus pneumoniae, Haemophilus influenzae, Legionella',
                      'Viral: Influenza, RSV, SARS-CoV-2, Coronavirus',
                      'Fungal: Histoplasma, Coccidioides, Cryptococcus (immunocompromised)',
                      'Aspiration: From gastric contents or foreign bodies'
                    ].map((cause, i) => (
                      <li key={i} className="flex gap-3">
                        <span className="text-[var(--accent-primary)]">•</span>
                        <span>{cause}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </AnimatedSection>

              <AnimatedSection>
                <div className="card-neumorphic p-8">
                  <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Clinical Presentation</h2>
                  <div className="space-y-4">
                    <div>
                      <h3 className="font-semibold text-[var(--text-primary)] mb-2">Common Symptoms</h3>
                      <p className="text-[var(--text-secondary)] text-sm">Cough, fever, dyspnea, chest pain, fatigue, malaise</p>
                    </div>
                    <div>
                      <h3 className="font-semibold text-[var(--text-primary)] mb-2">Examination Findings</h3>
                      <p className="text-[var(--text-secondary)] text-sm">Crackles, consolidation, bronchial breath sounds, egophony, dullness to percussion</p>
                    </div>
                  </div>
                </div>
              </AnimatedSection>
            </div>

            {/* Right: Sidebar */}
            <div className="lg:col-span-1 space-y-6">
              {/* Quick Actions */}
              <div className="card-neumorphic p-6 space-y-2">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Generate</h2>
                <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full flex items-center justify-center gap-2 opacity-50 cursor-not-allowed text-sm">
                  <Sparkles size={14} /> Flashcards
                </button>
                <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full flex items-center justify-center gap-2 opacity-50 cursor-not-allowed text-sm">
                  <Target size={14} /> Quiz
                </button>
                <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full flex items-center justify-center gap-2 opacity-50 cursor-not-allowed text-sm">
                  <Brain size={14} /> Case Study
                </button>
                <Link to="/disease/1/edit" className="btn-neumorphic-primary py-2 px-4 w-full flex items-center justify-center gap-2 text-sm">
                  <FileText size={14} /> Edit
                </Link>
              </div>

              {/* Learning Notes */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">My Notes</h2>
                <textarea placeholder="Add personal notes..." className="input-neumorphic w-full min-h-[120px] resize-none text-sm" />
                <button className="btn-neumorphic-primary py-2 px-4 mt-3 text-sm">Save Notes</button>
              </div>

              {/* Related Content */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Related Diseases</h2>
                <div className="space-y-2">
                  {['Bronchitis', 'Tuberculosis', 'ARDS'].map((related, i) => (
                    <Link key={i} to="/explorer" className="depth-layer-1 rounded-lg p-2 text-sm flex items-center justify-between hover:bg-[var(--surface-hover)]">
                      <span className="text-[var(--text-primary)]">{related}</span>
                      <ChevronRight size={14} className="text-[var(--text-tertiary)]" />
                    </Link>
                  ))}
                </div>
              </div>

              {/* Discussion */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4 flex items-center gap-2">
                  <MessageSquare size={18} /> Discussion
                </h2>
                <p className="text-xs text-[var(--text-secondary)] mb-3">Comments disabled (coming soon)</p>
                <button disabled className="btn-neumorphic-secondary py-2 px-4 w-full opacity-50 cursor-not-allowed text-sm">Add Comment</button>
              </div>
            </div>
          </div>

          {/* Continue Reading CTA */}
          <div className="card-neumorphic p-6 flex items-center justify-between">
            <div>
              <p className="font-semibold text-[var(--text-primary)]">Continue where you left off</p>
              <p className="text-xs text-[var(--text-secondary)]">You have 65% of this disease completed</p>
            </div>
            <button className="btn-neumorphic-primary py-3 px-8 flex items-center gap-2">
              Continue <ChevronRight size={18} />
            </button>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
