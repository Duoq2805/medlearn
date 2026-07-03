import { useParams, Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import {
  ArrowLeft, Bookmark, Share2, Brain, Clock, GraduationCap,
  ChevronRight, MessageSquare, FileText, AlertCircle, CheckCircle2,
  Stethoscope, Activity, Pill
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function CaseStudyDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('case');
  const [isBookmarked, setIsBookmarked] = useState(false);

  const caseData = {
    id: id || '1',
    title: 'Acute Respiratory Infection',
    specialty: 'Pulmonology',
    difficulty: 'Medium',
    estimatedTime: '30 min',
    progress: 45,
  };

  const tabs = [
    { id: 'case', label: 'Case', icon: Brain },
    { id: 'history', label: 'History', icon: Clock },
    { id: 'exam', label: 'Exam', icon: Stethoscope },
    { id: 'investigation', label: 'Investigation', icon: Activity },
    { id: 'diagnosis', label: 'Diagnosis', icon: FileText },
    { id: 'management', label: 'Management', icon: Pill },
    { id: 'discussion', label: 'Discussion', icon: MessageSquare },
  ];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-5xl mx-auto">
        <AnimatedSection>
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <button onClick={() => navigate('/cases')} className="flex items-center gap-2 text-[var(--text-secondary)] hover:text-[var(--text-primary)]">
              <ArrowLeft size={18} /> Back to Cases
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
            <h1 className="font-display text-4xl font-bold text-[var(--text-primary)] mb-2">{caseData.title}</h1>
            <p className="text-sm text-[var(--text-secondary)] mb-4">Patient case study for clinical reasoning practice</p>
            <div className="flex flex-wrap items-center gap-4 text-sm">
              <span className="stat-pill">
                <GraduationCap size={14} className="text-[var(--accent-primary)]" />
                {caseData.difficulty}
              </span>
              <span className="stat-pill">
                <Clock size={14} className="text-[var(--accent-primary)]" />
                {caseData.estimatedTime}
              </span>
              <span className="stat-pill">
                <Brain size={14} className="text-[var(--accent-primary)]" />
                {caseData.specialty}
              </span>
            </div>
          </div>

          {/* Progress */}
          <div className="card-neumorphic p-6 mb-8">
            <div className="flex items-center justify-between mb-3">
              <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">Case Progress</h2>
              <span className="text-sm font-semibold text-[var(--accent-primary)]">{caseData.progress}%</span>
            </div>
            <div className="w-full h-3 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_6px_var(--shadow-dark),inset_-2px_-2px_6px_var(--shadow-light)] overflow-hidden">
              <div className="h-full bg-gradient-to-r from-[var(--accent-primary)] to-emerald-300 rounded-full transition-all" style={{ width: `${caseData.progress}%` }}></div>
            </div>
          </div>

          {/* Tabs */}
          <div className="mb-8 overflow-x-auto">
            <div className="card-neumorphic p-2 flex gap-1 min-w-min">
              {tabs.map((tab) => {
                const Icon = tab.icon;
                return (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`py-2 px-4 rounded-lg flex items-center gap-2 text-sm font-medium transition-all whitespace-nowrap ${
                      activeTab === tab.id
                        ? 'bg-[var(--accent-primary)] text-white'
                        : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                    }`}
                  >
                    <Icon size={14} /> {tab.label}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Content */}
          <div className="card-neumorphic p-8 mb-8">
            {activeTab === 'case' && (
              <div>
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Case Presentation</h2>
                <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
                  A 45-year-old male patient presents to the emergency department with a 3-day history of cough, fever, and dyspnea. 
                  The patient reports productive cough with greenish sputum and chest discomfort that worsens with deep breathing.
                </p>
                <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
                  Past Medical History: Hypertension (controlled), former smoker (quit 5 years ago).
                  Current Medications: Lisinopril 10mg daily.
                </p>
                <p className="text-[var(--text-secondary)] leading-relaxed">
                  On examination, the patient appears ill and in mild respiratory distress. Vital signs: HR 102 bpm, RR 24/min, 
                  BP 145/92, Temp 38.5°C, O2 sat 94% on room air.
                </p>
              </div>
            )}
            {activeTab === 'history' && (
              <div>
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Patient History</h2>
                <ul className="space-y-2 text-[var(--text-secondary)]">
                  <li><strong>Chief Complaint:</strong> Cough, fever, dyspnea x 3 days</li>
                  <li><strong>HPI:</strong> Gradual onset of dry cough progressing to productive cough</li>
                  <li><strong>PMHx:</strong> HTN, former smoker</li>
                  <li><strong>PSHx:</strong> Appendectomy 20 years ago</li>
                  <li><strong>Allergies:</strong> NKDA</li>
                </ul>
              </div>
            )}
            {activeTab === 'exam' && (
              <div>
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Physical Examination</h2>
                <ul className="space-y-2 text-[var(--text-secondary)]">
                  <li><strong>General:</strong> Ill-appearing, mild respiratory distress</li>
                  <li><strong>Lungs:</strong> Crackles RLL, bronchial breath sounds RLL</li>
                  <li><strong>Cardiovascular:</strong> Tachycardia, regular rhythm, no murmurs</li>
                  <li><strong>Abdomen:</strong> Soft, non-tender, non-distended</li>
                </ul>
              </div>
            )}
            {activeTab === 'investigation' && (
              <div>
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Investigations</h2>
                <p className="text-[var(--text-secondary)] mb-2"><strong>Lab Results:</strong></p>
                <ul className="space-y-1 text-sm text-[var(--text-secondary)] mb-4">
                  <li>WBC: 14.5 K/uL (elevated)</li>
                  <li>CRP: 8.2 mg/dL (elevated)</li>
                  <li>Procalcitonin: 1.2 ng/mL (mildly elevated)</li>
                </ul>
                <p className="text-[var(--text-secondary)]"><strong>Imaging:</strong> Chest X-ray shows consolidation RLL consistent with pneumonia</p>
              </div>
            )}
            {activeTab === 'diagnosis' && (
              <div>
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Working Diagnosis</h2>
                <p className="text-[var(--text-secondary)] leading-relaxed">
                  Based on clinical presentation, examination findings, and investigations, the working diagnosis is:
                </p>
                <p className="text-lg font-semibold text-[var(--accent-primary)] mt-4">Community-Acquired Pneumonia (CAP)</p>
              </div>
            )}
            {activeTab === 'management' && (
              <div>
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Management Plan</h2>
                <ul className="space-y-2 text-[var(--text-secondary)]">
                  <li><strong>Antibiotics:</strong> Amoxicillin-clavulanate 875/125 mg PO BID for 7 days</li>
                  <li><strong>Supportive:</strong> Fluid intake, rest, antipyretics as needed</li>
                  <li><strong>Follow-up:</strong> Chest X-ray in 6-8 weeks to confirm resolution</li>
                  <li><strong>Vaccination:</strong> Discuss pneumococcal vaccination</li>
                </ul>
              </div>
            )}
            {activeTab === 'discussion' && (
              <div>
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Discussion</h2>
                <p className="text-[var(--text-secondary)] leading-relaxed">
                  This case demonstrates the typical presentation of community-acquired pneumonia. The combination of respiratory symptoms, 
                  fever, and imaging findings are diagnostic. Empiric antibiotic therapy is warranted while awaiting culture results.
                </p>
              </div>
            )}
          </div>

          {/* Continue CTA */}
          <div className="card-neumorphic p-6 flex items-center justify-between">
            <div>
              <p className="font-semibold text-[var(--text-primary)]">You are {45}% through this case</p>
              <p className="text-xs text-[var(--text-secondary)]">Keep working to complete your clinical reasoning</p>
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
