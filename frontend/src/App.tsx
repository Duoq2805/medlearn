import { Outlet, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { ThemeProvider } from './context/ThemeContext';
import Navigation from './components/layout/Navigation';
import { MedvoraLogo } from './components/MedvoraLogo';
import PageTransition from './components/layout/PageTransition';
import ScrollToTop from './components/ui/ScrollToTop';
import CommandPalette from './components/CommandPalette';
import { useAuth } from './hooks/useAuth'; // Assuming useAuth is available
import { Bot, TrendingUp, Sparkles, Calendar } from 'lucide-react';

function App() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const sampleCommands = [
    { id: 'home', name: 'Go to Home', description: 'Navigate to the homepage', run: () => navigate('/') },
    { id: 'explorer', name: 'Explore Diseases', description: 'Browse the disease knowledge base', shortcut: 'Ctrl+E', run: () => navigate('/explorer') },
    { id: 'checker', name: 'Symptom Checker', description: 'Check symptoms for possible conditions', shortcut: 'Ctrl+S', run: () => navigate('/symptom-checker') },
    { id: 'cases', name: 'Case Studies', description: 'Practice clinical reasoning', shortcut: 'Ctrl+C', run: () => navigate('/cases') },
    ...(user ? [{ id: 'dashboard', name: 'Go to Dashboard', description: 'Access your learning progress', shortcut: 'Ctrl+D', run: () => navigate('/dashboard') }] : []),
  ];

  useEffect(() => {
    let lenis: { destroy: () => void } | null = null;

    import('lenis').then(({ default: Lenis }) => {
      const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      if (reduced) return;

      const instance = new Lenis({
        duration: 1.2,
        easing: (t: number) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
        smoothWheel: true,
        wheelMultiplier: 1,
        touchMultiplier: 1.5,
      });

      lenis = instance;

      const raf = (time: number) => {
        instance.raf(time);
        requestAnimationFrame(raf);
      };
      requestAnimationFrame(raf);
    });

    return () => {
      lenis?.destroy();
    };
  }, []);

  return (
    <ThemeProvider>
      <div className="min-h-screen" style={{ backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)' }}>
        {/* Global Navigation */}
        <Navigation />
        <ScrollToTop />
        
        {/* Main Content */}
        <main className="relative">
          <PageTransition>
            <Outlet />
          </PageTransition>
        </main>

        <CommandPalette commands={sampleCommands} />

        {/* Global Footer */}
        <footer className="mt-32 py-16" style={{ borderTop: '1px solid var(--shadow-dark)' }}>
          <div className="max-w-7xl mx-auto px-4 md:px-8">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
              <div>
                <div className="mb-4">
                  <MedvoraLogo variant="full" className="h-8 w-8" showDepth={true} />
                </div>
                <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>
                  AI-powered medical learning platform for modern clinicians.
                </p>
              </div>

              <div>
                <h4 className="font-semibold text-sm mb-4" style={{ color: 'var(--text-primary)' }}>
                  Company
                </h4>
                <ul className="space-y-2 text-sm" style={{ color: 'var(--text-secondary)' }}>
                  <li><a href="#" className="hover:text-[var(--accent-primary)] transition-colors">About</a></li>
                  <li><a href="#" className="hover:text-[var(--accent-primary)] transition-colors">Careers</a></li>
                  <li><a href="#" className="hover:text-[var(--accent-primary)] transition-colors">Blog</a></li>
                </ul>
              </div>

              <div>
                <h4 className="font-semibold text-sm mb-4" style={{ color: 'var(--text-primary)' }}>
                  Product
                </h4>
                <ul className="space-y-2 text-sm" style={{ color: 'var(--text-secondary)' }}>
                  <li><a href="#" className="hover:text-[var(--accent-primary)] transition-colors">Features</a></li>
                  <li><a href="#" className="hover:text-[var(--accent-primary)] transition-colors">Pricing</a></li>
                  <li><a href="#" className="hover:text-[var(--accent-primary)] transition-colors">Security</a></li>
                </ul>
              </div>

              <div>
                <h4 className="font-semibold text-sm mb-4" style={{ color: 'var(--text-primary)' }}>
                  Legal
                </h4>
                <ul className="space-y-2 text-sm" style={{ color: 'var(--text-secondary)' }}>
                  <li><a href="#" className="hover:text-[var(--accent-primary)] transition-colors">Privacy</a></li>
                  <li><a href="#" className="hover:text-[var(--accent-primary)] transition-colors">Terms</a></li>
                  <li><a href="#" className="hover:text-[var(--accent-primary)] transition-colors">Cookies</a></li>
                </ul>
              </div>
            </div>

            <div className="pt-8 text-center text-sm" style={{ borderTop: '1px solid var(--shadow-dark)', color: 'var(--text-secondary)' }}>
              © 2026 Medvora. All rights reserved.
            </div>
          </div>
        </footer>
      </div>
    </ThemeProvider>
  );
}

export default App;
