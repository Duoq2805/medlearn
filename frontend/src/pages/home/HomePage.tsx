import { motion } from 'framer-motion';
import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import {
  ChevronRight, Brain, Stethoscope, BookOpen, Activity,
  BrainCircuit, ClipboardCheck, Microscope, GraduationCap,
  ClipboardList, GitBranch, TimerReset
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import {
  StaggerContainer, StaggerItem, FadeIn, Float,
  AnimatedSection, AnimatedButton, scaleInVariants
} from '../../components/motion/MotionWrappers';


const FEATURES = [
  { icon: BrainCircuit, title: 'AI Flashcards', desc: 'Generate personalized flashcards using spaced repetition.' },
  { icon: ClipboardCheck, title: 'Quiz Generation', desc: 'Create adaptive quizzes targeting weak areas.' },
  { icon: Microscope, title: 'Disease Explorer', desc: 'Browse 500+ diseases with detailed pathophysiology.' },
  { icon: Stethoscope, title: 'Symptom Checker', desc: 'Input symptoms and get AI-powered differentials.' },
  { icon: GraduationCap, title: 'Learning Dashboard', desc: 'Track progress and retention rates.' },
  { icon: ClipboardList, title: 'Clinical Cases', desc: 'Practice with 5,000+ real-world cases.' },
  { icon: GitBranch, title: 'Version Control', desc: 'All content continuously updated.' },
  { icon: TimerReset, title: 'Rapid Recall', desc: 'Interleaved practice and active recall.' },
];

export default function HomePage() {
  const [activePrompt, setActivePrompt] = useState(0);
  const [selectedSymptom, setSelectedSymptom] = useState('Cough');
  const navigate = useNavigate();
  const { user } = useAuth();

  // Redirect authenticated users to dashboard
  if (user) {
    const role = (user.roles?.[0] || user.role || 'USER').toUpperCase();
    if (role === 'ADMIN') navigate('/admin/dashboard', { replace: true });
    else if (role === 'REVIEWER') navigate('/reviewer/dashboard', { replace: true });
    else navigate('/dashboard', { replace: true });
    return null;
  }

  const prompts = [
    { question: 'Which imaging finding supports pneumonia?', answer: 'Focal infiltrate on chest X-ray.' },
    { question: 'What lab pattern suggests hypothyroidism?', answer: 'Elevated TSH with low free T4.' },
    { question: 'What pain migration is classic for appendicitis?', answer: 'Periumbilical pain shifting to RLQ.' },
  ];

  const symptomMatches: Record<string, string[]> = {
    Cough: ['Pneumonia', 'Asthma', 'Bronchitis'],
    Fever: ['COVID-19', 'Influenza', 'UTI'],
    Fatigue: ['Hypothyroidism', 'Anemia', 'Depression'],
  };

  const handleFeatureClick = (path: string) => {
    if (user) {
      navigate(path);
    } else {
      navigate('/login', { state: { from: path } });
    }
  };

  return (
    <div className="relative" style={{ backgroundColor: 'var(--bg-primary)' }}>
      {/* Ambient decorative blobs */}
      <div className="ambient-blob ambient-blob-1" />
      <div className="ambient-blob ambient-blob-2" />
      <div className="ambient-blob ambient-blob-3" />

      <div className="relative z-10">

        {/* ===== SCENE 1: WATCH ===== */}
        <AnimatedSection className="min-h-screen flex items-center py-12 px-4 md:px-8">
          <div className="max-w-7xl mx-auto w-full">
            <div className="grid lg:grid-cols-2 gap-16 items-center">
              {/* Left */}
              <StaggerContainer staggerChildren={0.15} delayChildren={0.2} className="space-y-10">
                <div className="space-y-6">
                  <FadeIn>
                    <motion.div
                      className="inline-flex items-center gap-3 px-5 py-2.5 rounded-full"
                      style={{ backgroundColor: 'var(--surface-primary)', boxShadow: 'var(--shadow-sm)' }}
                      variants={scaleInVariants}
                    >
                      <motion.span
                        className="w-2.5 h-2.5 rounded-full"
                        style={{ backgroundColor: 'var(--accent-primary)' }}
                        animate={{ scale: [1, 1.3, 1], opacity: [1, 0.7, 1] }}
                        transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
                      />
                      <span className="text-sm font-medium" style={{ color: 'var(--text-primary)' }}>AI-Powered Medical Learning</span>
                    </motion.div>
                  </FadeIn>

                  <FadeIn direction="up"><h1 className="font-display text-5xl md:text-7xl font-bold" style={{ color: 'var(--text-primary)' }}>
                      Learn medicine the way{' '}
                      <motion.span
                        style={{ color: 'var(--accent-primary)', display: 'inline-block' }}
                        whileHover={{ scale: 1.02 }}
                      >
                        clinicians think
                      </motion.span>
                    </h1>
                  </FadeIn>

                  <FadeIn direction="up"><p className="text-lg max-w-xl leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
                      Medvora transforms medical education with interconnected knowledge, clinical reasoning practice, and intelligent retention tools.
                    </p>
                  </FadeIn>
                </div>

                {/* CTA */}
                <FadeIn>
                  <div className="flex flex-col sm:flex-row gap-4">
                    <AnimatedButton>
                      <Link to="/explorer" className="btn-neumorphic-primary px-8 py-4 text-base gap-2 inline-flex items-center group">
                        Explore Knowledge
                        <ChevronRight size={20} className="group-hover:translate-x-1 transition-transform" />
                      </Link>
                    </AnimatedButton>
                    <AnimatedButton>
                      <Link to="/symptom-checker" className="btn-neumorphic-secondary px-8 py-4 text-base inline-flex items-center justify-center">
                        Try Symptom Checker
                      </Link>
                    </AnimatedButton>
                  </div>
                </FadeIn>

                {/* Stats */}
                <FadeIn>
                  <div className="pt-6 flex items-center gap-8" style={{ borderTop: '1px solid var(--shadow-dark)' }}>
                    <motion.div whileHover={{ y: -2 }} transition={{ duration: 0.2 }}>
                      <div className="text-2xl font-bold" style={{ color: 'var(--accent-primary)' }}>500+</div>
                      <div className="text-sm" style={{ color: 'var(--text-secondary)' }}>Diseases</div>
                    </motion.div>
                    <div className="w-px h-10" style={{ backgroundColor: 'var(--shadow-dark)' }} />
                    <motion.div whileHover={{ y: -2 }} transition={{ duration: 0.2 }}>
                      <div className="text-2xl font-bold" style={{ color: 'var(--accent-primary)' }}>10K+</div>
                      <div className="text-sm" style={{ color: 'var(--text-secondary)' }}>Clinical Cases</div>
                    </motion.div>
                  </div>
                </FadeIn>
              </StaggerContainer>

              {/* Right: floating visual stack */}
              <div className="hidden lg:flex items-center justify-center relative" style={{ minHeight: '480px' }}>
                <motion.div
                  className="card-neumorphic w-full max-w-sm flex flex-col items-center justify-center p-10 text-center relative z-10 animate-float"
                  style={{ minHeight: '340px' }}
                  initial={{ opacity: 0, y: 40 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.8, delay: 0.5, ease: 'easeOut' }}
                >
                  <motion.div
                    className="icon-well mb-5"
                    whileHover={{ rotate: 5, scale: 1.05 }}
                    transition={{ type: 'spring', stiffness: 300 }}
                  >
                    <Brain size={28} />
                  </motion.div>
                  <h3 className="font-display font-bold text-xl mb-3" style={{ color: 'var(--text-primary)' }}>
                    Medical Knowledge Graph
                  </h3>
                  <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>
                    Connected, hierarchical learning mapped the way clinicians think
                  </p>
                  <motion.div
                    className="absolute -top-4 -right-4 w-24 h-24 rounded-full"
                    style={{ backgroundColor: 'var(--surface-primary)', opacity: 0.7, boxShadow: 'var(--shadow-sm)' }}
                    animate={{ y: [0, -8, 0] }}
                    transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }}
                  />
                </motion.div>

                <Float duration={5} delay={0.5} className="absolute top-0 -right-8 z-20">
                  <motion.div
                    className="w-20 h-20 card-neumorphic-sm flex items-center justify-center"
                    whileHover={{ scale: 1.1, rotate: 10 }}
                    transition={{ type: 'spring', stiffness: 300 }}
                  >
                    <Activity size={24} style={{ color: 'var(--accent-primary)' }} />
                  </motion.div>
                </Float>
                <Float duration={6} delay={1.5} className="absolute bottom-12 -left-8 z-20">
                  <motion.div
                    className="w-20 h-20 card-neumorphic-sm flex items-center justify-center"
                    whileHover={{ scale: 1.1, rotate: -10 }}
                    transition={{ type: 'spring', stiffness: 300 }}
                  >
                    <Stethoscope size={24} style={{ color: 'var(--accent-primary)' }} />
                  </motion.div>
                </Float>
                <Float duration={4} delay={2.5} className="absolute top-1/2 -right-16 z-20">
                  <motion.div
                    className="w-16 h-16 card-neumorphic-sm flex items-center justify-center"
                    whileHover={{ scale: 1.1, rotate: 10 }}
                    transition={{ type: 'spring', stiffness: 300 }}
                  >
                    <BookOpen size={20} style={{ color: 'var(--accent-primary)' }} />
                  </motion.div>
                </Float>
              </div>
            </div>
          </div>
        </AnimatedSection>

        {/* ===== SCENE 2: EXPLORE ===== */}
        <AnimatedSection className="min-h-screen flex items-center py-12 px-4 md:px-8 relative">
          <div className="absolute inset-0" style={{
            background: 'radial-gradient(ellipse at 50% 0%, rgba(140,191,142,0.04) 0%, transparent 60%)',
            pointerEvents: 'none'
          }} />
          <div className="max-w-7xl mx-auto relative z-10 w-full">
            <StaggerContainer staggerChildren={0.15} delayChildren={0.2}>
              <FadeIn>
                <h2 className="font-display text-4xl md:text-5xl font-bold text-center mb-6" style={{ color: 'var(--text-primary)' }}>
                  Learn the way doctors think
                </h2>
              </FadeIn>
              <FadeIn>
                <p className="text-center text-lg max-w-2xl mx-auto mb-16" style={{ color: 'var(--text-secondary)' }}>
                  Every condition presented with the clinical reasoning framework used in medical practice
                </p>
              </FadeIn>
            </StaggerContainer>

            <StaggerContainer>
              <div className="grid gap-8 md:grid-cols-3">
                {['pneumonia', 'hypothyroidism', 'appendicitis'].map((id) => (
                  <StaggerItem key={id}>
                    <motion.div
                      className="card-neumorphic group cursor-pointer"
                      whileHover={{ y: -6, scale: 1.01 }}
                      transition={{ type: 'spring', stiffness: 200, damping: 15 }}
                    >
                      <h3 className="font-display font-bold text-2xl mb-5" style={{ color: 'var(--text-primary)' }}>
                        <span>{id === 'pneumonia' ? 'Pneumonia' : id === 'hypothyroidism' ? 'Hypothyroidism' : 'Appendicitis'}</span>
                      </h3>
                      <div className="space-y-4 mb-6">
                        <div>
                          <p className="text-xs font-semibold uppercase tracking-wider mb-1" style={{ color: 'var(--accent-primary)' }}>Symptoms</p>
                          <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>
                            {id === 'pneumonia' ? 'Fever, cough, chest pain' : id === 'hypothyroidism' ? 'Fatigue, cold intolerance' : 'RLQ pain, nausea'}
                          </p>
                        </div>
                        <div>
                          <p className="text-xs font-semibold uppercase tracking-wider mb-1" style={{ color: 'var(--accent-primary)' }}>Diagnosis</p>
                          <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>
                            {id === 'pneumonia' ? 'Chest X-ray' : id === 'hypothyroidism' ? 'TSH test' : 'CT scan'}
                          </p>
                        </div>
                        <div>
                          <p className="text-xs font-semibold uppercase tracking-wider mb-1" style={{ color: 'var(--accent-primary)' }}>Treatment</p>
                          <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>
                            {id === 'pneumonia' ? 'Antibiotics' : id === 'hypothyroidism' ? 'Levothyroxine' : 'Surgery'}
                          </p>
                        </div>
                      </div>
                      <div className="flex gap-3 pt-6" style={{ borderTop: '1px solid var(--shadow-dark)' }}>
                        <motion.div
                          className="flex-1 card-neumorphic-inset text-center text-xs py-3 cursor-pointer inline-flex items-center justify-center gap-2"
                          whileHover={{ y: -2, scale: 1.02 }}
                          transition={{ type: 'spring', stiffness: 300 }}
                        >
                          <BrainCircuit size={14} style={{ color: 'var(--accent-primary)' }} />
                          Flashcards
                        </motion.div>
                        <motion.div
                          className="flex-1 card-neumorphic-inset text-center text-xs py-3 cursor-pointer inline-flex items-center justify-center gap-2"
                          whileHover={{ y: -2, scale: 1.02 }}
                          transition={{ type: 'spring', stiffness: 300 }}
                        >
                          <ClipboardCheck size={14} style={{ color: 'var(--accent-primary)' }} />
                          Quiz
                        </motion.div>
                      </div>
                    </motion.div>
                  </StaggerItem>
                ))}
              </div>
            </StaggerContainer>
          </div>
        </AnimatedSection>

        {/* ===== SCENE 3: INTERACT ===== */}
        <AnimatedSection className="min-h-screen flex items-center py-12 px-4 md:px-8">
          <div className="max-w-7xl mx-auto w-full">
            <StaggerContainer staggerChildren={0.15} delayChildren={0.2}>
              <FadeIn>
                <h2 className="font-display text-4xl md:text-5xl font-bold text-center mb-6" style={{ color: 'var(--text-primary)' }}>
                  Try features live
                </h2>
              </FadeIn>
              <FadeIn>
                <p className="text-center text-lg max-w-2xl mx-auto mb-16" style={{ color: 'var(--text-secondary)' }}>
                  Experience the tools before you sign up
                </p>
              </FadeIn>
            </StaggerContainer>

            <StaggerContainer staggerChildren={0.15}>
              <div className="grid gap-8 lg:grid-cols-3">

                <StaggerItem>
                  <motion.div className="card-neumorphic" whileHover={{ y: -4 }} transition={{ type: 'spring', stiffness: 200, damping: 15 }}>
                    <p className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: 'var(--accent-primary)' }}>Flashcard Builder</p>
                    <h3 className="font-display font-bold text-xl mb-5" style={{ color: 'var(--text-primary)' }}>Generate Recall</h3>
                    <div className="card-neumorphic-inset mb-5 p-5">
                      <p className="text-sm font-semibold mb-3" style={{ color: 'var(--text-primary)' }}>{prompts[activePrompt].question}</p>
                      <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>{prompts[activePrompt].answer}</p>
                    </div>
                    <div className="flex gap-2">
                      {prompts.map((_, idx) => (
                        <motion.button
                          key={idx}
                          onClick={() => setActivePrompt(idx)}
                          className="h-2.5 flex-1 rounded-full"
                          style={{ backgroundColor: idx === activePrompt ? 'var(--accent-primary)' : 'var(--shadow-dark)' }}
                          whileHover={{ scale: 1.1 }}
                          whileTap={{ scale: 0.95 }}
                          transition={{ type: 'spring', stiffness: 400 }}
                        />
                      ))}
                    </div>
                  </motion.div>
                </StaggerItem>

                <StaggerItem>
                  <motion.div className="card-neumorphic" whileHover={{ y: -4 }} transition={{ type: 'spring', stiffness: 200, damping: 15 }}>
                    <p className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: 'var(--accent-primary)' }}>Symptom Checker</p>
                    <h3 className="font-display font-bold text-xl mb-5" style={{ color: 'var(--text-primary)' }}>See Likely Paths</h3>
                    <div className="flex flex-wrap gap-2 mb-5">
                      {Object.keys(symptomMatches).map((symptom) => (
                        <motion.button
                          key={symptom}
                          onClick={() => setSelectedSymptom(symptom)}
                          className={`px-4 py-2 text-xs font-medium rounded-lg transition-all ${
                            selectedSymptom === symptom ? 'shadow-neumorphic-inset-sm' : 'shadow-neumorphic-ext-sm'
                          }`}
                          style={{
                            backgroundColor: selectedSymptom === symptom ? 'var(--surface-primary)' : 'var(--surface-elevated)',
                            color: 'var(--accent-primary)',
                          }}
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                          transition={{ type: 'spring', stiffness: 400 }}
                        >
                          {symptom}
                        </motion.button>
                      ))}
                    </div>
                    <div className="space-y-3">
                      {symptomMatches[selectedSymptom].map((match) => (
                        <motion.div
                          key={match}
                          className="card-neumorphic-sm flex justify-between items-center"
                          initial={{ opacity: 0, x: -10 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ duration: 0.3 }}
                        >
                          <span className="text-sm font-medium" style={{ color: 'var(--text-primary)' }}>{match}</span>
                          <span className="text-xs font-semibold" style={{ color: 'var(--accent-primary)' }}>{92 - Math.floor(Math.random() * 13)}%</span>
                        </motion.div>
                      ))}
                    </div>
                  </motion.div>
                </StaggerItem>

                <StaggerItem>
                  <motion.div className="card-neumorphic" whileHover={{ y: -4 }} transition={{ type: 'spring', stiffness: 200, damping: 15 }}>
                    <p className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: 'var(--accent-primary)' }}>Clinical Case</p>
                    <h3 className="font-display font-bold text-xl mb-5" style={{ color: 'var(--text-primary)' }}>Reason Through</h3>
                    <div className="card-neumorphic-inset mb-5 p-5">
                      <p className="text-sm leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
                        42-year-old with fever, productive cough, pleuritic pain, and focal crackles.
                      </p>
                    </div>
                    <div className="space-y-3">
                      {['Order chest X-ray', 'Start levothyroxine', 'Schedule colonoscopy'].map((choice, idx) => (
                        <motion.div
                          key={choice}
                          className="card-neumorphic-sm flex items-center px-4 py-3 text-sm font-medium cursor-pointer"
                          style={{
                            backgroundColor: idx === 0 ? 'var(--surface-elevated)' : 'var(--surface-primary)',
                            color: idx === 0 ? 'var(--accent-primary)' : 'var(--text-secondary)',
                          }}
                          whileHover={{ x: 4, scale: 1.01 }}
                          whileTap={{ scale: 0.98 }}
                          transition={{ type: 'spring', stiffness: 300 }}
                        >
                          <span
                            className="w-5 h-5 rounded-full flex items-center justify-center text-xs mr-3"
                            style={{
                              backgroundColor: idx === 0 ? 'var(--accent-primary)' : 'var(--surface-primary)',
                              color: idx === 0 ? 'white' : 'var(--text-tertiary)',
                              boxShadow: 'inset 0 1px 2px rgba(0,0,0,0.1)',
                            }}
                          >
                            {idx + 1}
                          </span>
                          {choice}
                        </motion.div>
                      ))}
                    </div>
                  </motion.div>
                </StaggerItem>

              </div>
            </StaggerContainer>
          </div>
        </AnimatedSection>

        {/* ===== SCENE 4: FEATURES ===== */}
        <AnimatedSection className="min-h-screen flex items-center py-12 px-4 md:px-8 relative" id="features">
          <div className="absolute inset-0" style={{
            background: 'radial-gradient(ellipse at 50% 100%, rgba(140,191,142,0.04) 0%, transparent 60%)',
            pointerEvents: 'none',
          }} />
          <div className="max-w-7xl mx-auto relative z-10 w-full">
            <StaggerContainer staggerChildren={0.15} delayChildren={0.2}>
              <FadeIn>
                <h2 className="font-display text-4xl md:text-5xl font-bold text-center mb-6" style={{ color: 'var(--text-primary)' }}>
                  Built for focused study
                </h2>
              </FadeIn>
              <FadeIn>
                <p className="text-center text-lg max-w-2xl mx-auto mb-16" style={{ color: 'var(--text-secondary)' }}>
                  Everything you need to master medical knowledge
                </p>
              </FadeIn>
            </StaggerContainer>

            <StaggerContainer staggerChildren={0.08}>
              <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                {FEATURES.map((feature, idx) => {
                  const Icon = feature.icon;
                  const routes: Record<string, string> = {
                    'AI Flashcards': '/explorer',
                    'Quiz Generation': '/explorer',
                    'Disease Explorer': '/explorer',
                    'Symptom Checker': '/symptom-checker',
                    'Learning Dashboard': '/dashboard',
                    'Clinical Cases': '/cases',
                    'Version Control': '/explorer',
                    'Rapid Recall': '/explorer',
                  };
                  const path = routes[feature.title] || '/explorer';

                  return (
                    <StaggerItem key={idx}>
                      <motion.div
                        className="card-neumorphic-sm p-6 h-full flex flex-col group cursor-pointer"
                        whileHover={{ y: -4 }}
                        transition={{ duration: 0.2, ease: 'easeOut' }}
                        onClick={() => handleFeatureClick(path)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter' || e.key === ' ') {
                            e.preventDefault();
                            handleFeatureClick(path);
                          }
                        }}
                        tabIndex={0}
                        role="button"
                        aria-label={`Learn more about ${feature.title}`}
                      >
                        {/* Icon badge */}
                        <div
                          className="w-11 h-11 rounded-xl flex items-center justify-center mb-4 group-hover:-translate-y-0.5 transition-transform duration-200"
                          style={{ backgroundColor: 'var(--surface-primary)', boxShadow: 'var(--shadow-sm)' }}
                        >
                          <Icon
                            size={20}
                            className="transition-colors duration-200"
                            style={{ color: 'var(--text-secondary)' }}
                          />
                        </div>

                        {/* Title */}
                        <h3
                          className="font-display font-bold text-lg mb-2 transition-colors duration-200"
                          style={{ color: 'var(--text-primary)' }}
                        >
                          {feature.title}
                        </h3>

                        {/* Description */}
                        <p className="text-sm flex-grow mb-4" style={{ color: 'var(--text-secondary)' }}>
                          {feature.desc}
                        </p>

                        {/* Learn more */}
                        <div
                          className="text-xs font-semibold inline-flex items-center gap-1 transition-all duration-200"
                          style={{ color: 'var(--accent-primary)' }}
                        >
                          <span>Learn more</span>
                          <ChevronRight size={12} className="group-hover:translate-x-1 transition-transform duration-200" />
                        </div>
                      </motion.div>
                    </StaggerItem>
                  );
                })}
              </div>
            </StaggerContainer>
          </div>
        </AnimatedSection>

        {/* ===== SCENE 5: DECIDE ===== */}
        <AnimatedSection className="min-h-screen flex items-center py-12 px-4 md:px-8 text-center">
          <div className="max-w-3xl mx-auto">
            <StaggerContainer staggerChildren={0.2}>
              <FadeIn>
                <h2 className="font-display text-4xl md:text-5xl font-bold mb-6" style={{ color: 'var(--text-primary)' }}>
                  Ready to learn smarter?
                </h2>
              </FadeIn>
              <FadeIn>
                <p className="text-xl mb-10 max-w-2xl mx-auto" style={{ color: 'var(--text-secondary)' }}>
                  Join thousands of medical students and professionals mastering medicine with AI-powered learning.
                </p>
              </FadeIn>
              <FadeIn>
                <div className="flex flex-col sm:flex-row gap-4 justify-center">
                  <AnimatedButton>
                    <Link to="/register" className="btn-neumorphic-primary px-10 py-4 text-base">Get Started Free</Link>
                  </AnimatedButton>
                  <AnimatedButton>
                    <Link to="/explorer" className="btn-neumorphic-secondary px-10 py-4 text-base">Explore Now</Link>
                  </AnimatedButton>
                </div>
              </FadeIn>
            </StaggerContainer>
          </div>
        </AnimatedSection>

      </div>
    </div>
  );
}
