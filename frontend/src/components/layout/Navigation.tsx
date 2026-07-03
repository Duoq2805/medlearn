import { useState, useRef, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useTheme } from '../../context/ThemeContext';
import { useAuth } from '../../hooks/useAuth';
import { getPermissions } from '../../hooks/usePermissions';
import {
  Menu, Moon, Sun, LogOut, Settings, Bell, User, BookOpen,
  Brain, Zap, Target, Sparkles, BarChart3, Search,
  Bookmark, LayoutDashboard, ChevronDown, X, Command, FileText, Clock, ClipboardCheck
} from 'lucide-react';

const NAV_AUTH = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/explorer', icon: BookOpen, label: 'Diseases' },
  { to: '/flashcards', icon: Sparkles, label: 'Flashcards' },
  { to: '/quiz', icon: ClipboardCheck, label: 'Quiz' },
  { to: '/cases', icon: Target, label: 'Cases' },
  { to: '/symptom-checker', icon: Brain, label: 'Symptom Checker' },
] as const;

const NAV_GUEST = [
  { to: '/explorer', icon: BookOpen, label: 'Diseases' },
  { to: '/cases', icon: Target, label: 'Cases' },
] as const;

export const Navigation: React.FC = () => {
  const { theme, toggleTheme } = useTheme();
  const { user, logout } = useAuth();
  const [isMobileOpen, setIsMobileOpen] = useState(false);
  const [isAvatarOpen, setIsAvatarOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const avatarRef = useRef<HTMLDivElement>(null);
  const notifRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const location = useLocation();

  const perm = user ? getPermissions(user.roles?.[0]) : null;
  const isActive = (path: string) => location.pathname.startsWith(path);

  useEffect(() => {
    const handle = (e: MouseEvent) => {
      if (avatarRef.current && !avatarRef.current.contains(e.target as Node)) setIsAvatarOpen(false);
      if (notifRef.current && !notifRef.current.contains(e.target as Node)) setIsNotifOpen(false);
    };
    document.addEventListener('mousedown', handle);
    return () => document.removeEventListener('mousedown', handle);
  }, []);

  useEffect(() => {
    const handle = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setIsSearchOpen(true);
      }
    };
    document.addEventListener('keydown', handle);
    return () => document.removeEventListener('keydown', handle);
  }, []);

  const userInitial = user?.fullName?.[0]?.toUpperCase() || user?.username?.[0]?.toUpperCase() || '?';
  const navItems = user ? NAV_AUTH : NAV_GUEST;

  const handleLogout = () => {
    logout();
    setIsAvatarOpen(false);
    navigate('/');
  };

  return (
    <header className="fixed top-0 left-0 right-0 z-50 px-4 md:px-8 py-4">
      <div className="max-w-7xl mx-auto">
        <div className="rounded-2xl px-6 py-3 border backdrop-blur-xl flex justify-between items-center bg-white dark:bg-[var(--surface-primary)] border-[var(--border)]" style={{ boxShadow: 'var(--shadow-sm)' }}>
          {/* Logo */}
          <Link to={user ? '/dashboard' : '/'} className="font-display font-bold text-xl text-[var(--text-primary)]">Medvora</Link>

          {/* Desktop Nav */}
          <nav className="hidden md:flex gap-1">
            {user && (
              <button onClick={() => setIsSearchOpen(true)} className="flex items-center gap-2 px-3 py-1.5 text-sm rounded-lg text-[var(--text-secondary)] hover:bg-[var(--surface-hover)]">
                <Search size={16} />
                <kbd className="text-xs px-1 py-0.5 rounded border border-[var(--border)]">⌘K</kbd>
              </button>
            )}
            {navItems.map((link) => (
              <Link key={link.to} to={link.to} className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-all flex items-center gap-1.5 ${
                isActive(link.to) ? 'bg-[var(--accent-primary)]/10 text-[var(--accent-primary)]' : 'text-[var(--text-secondary)] hover:bg-[var(--surface-hover)]'
              }`}>
                <link.icon size={16} /> {link.label}
              </Link>
            ))}
          </nav>

          {/* Right */}
          <div className="flex items-center gap-2">
            <button onClick={toggleTheme} className="p-2.5 rounded-full border border-[var(--border)] hover:bg-[var(--surface-hover)]">
              {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            </button>

            {user ? (
              <>
                {/* Notifications */}
                <div ref={notifRef} className="relative">
                  <button onClick={() => setIsNotifOpen(!isNotifOpen)} className="p-2.5 rounded-full border border-[var(--border)] hover:bg-[var(--surface-hover)] relative">
                    <Bell size={18} />
                    <span className="absolute -top-0.5 -right-0.5 w-4 h-4 bg-red-500 rounded-full text-[10px] flex items-center justify-center text-white font-bold">3</span>
                  </button>
                  {isNotifOpen && (
                    <div className="absolute right-0 mt-2 w-72 rounded-xl border border-[var(--border)] bg-[var(--surface-primary)] shadow-lg z-50">
                      <div className="p-3 border-b"><p className="text-sm font-semibold">Notifications</p></div>
                      <div className="p-2 space-y-1 max-h-64 overflow-y-auto">
                        {[...Array(3)].map((_, i) => (
                          <div key={i} className="flex gap-2 p-2 rounded hover:bg-[var(--surface-hover)]">
                            <span className="w-2 h-2 mt-1 rounded-full bg-[var(--accent-primary)] shrink-0" />
                            <div className="text-xs">
                              <p className="text-[var(--text-primary)]">Draft approved</p>
                              <p className="text-[var(--text-tertiary)]">5m ago</p>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>

                {/* Avatar */}
                <div ref={avatarRef} className="relative">
                  <button onClick={() => setIsAvatarOpen(!isAvatarOpen)} className="flex items-center gap-1.5 px-2 py-1 rounded-full border border-[var(--border)] bg-[var(--accent-primary)]/10 hover:bg-[var(--accent-primary)]/20">
                    <div className="w-7 h-7 rounded-full bg-[var(--accent-primary)] flex items-center justify-center text-white text-xs font-bold">{userInitial}</div>
                    <ChevronDown size={14} />
                  </button>
                  {isAvatarOpen && (
                    <div className="absolute right-0 mt-2 w-56 rounded-xl border border-[var(--border)] bg-[var(--surface-primary)] shadow-lg z-50">
                      <div className="p-3 border-b">
                        <p className="text-sm font-semibold">{user.fullName || user.username}</p>
                        <p className="text-xs text-[var(--text-secondary)]">{user.email}</p>
                      </div>
                      <div className="p-1.5 space-y-0.5">
                        <Link to="/profile" className="flex gap-2 px-3 py-2 text-sm rounded hover:bg-[var(--surface-hover)]"><User size={16} /> Profile</Link>
                        <Link to="/bookmarks" className="flex gap-2 px-3 py-2 text-sm rounded hover:bg-[var(--surface-hover)]"><Bookmark size={16} /> Bookmarks</Link>
                        {perm?.canCreateDraft && <Link to="/drafts" className="flex gap-2 px-3 py-2 text-sm rounded hover:bg-[var(--surface-hover)]"><FileText size={16} /> My Drafts</Link>}
                        <Link to="/progress" className="flex gap-2 px-3 py-2 text-sm rounded hover:bg-[var(--surface-hover)]"><BarChart3 size={16} /> Progress</Link>
                        <Link to="/history" className="flex gap-2 px-3 py-2 text-sm rounded hover:bg-[var(--surface-hover)]"><Clock size={16} /> History</Link>
                        <Link to="/settings" className="flex gap-2 px-3 py-2 text-sm rounded hover:bg-[var(--surface-hover)]"><Settings size={16} /> Settings</Link>
                        <hr className="border-[var(--shadow-dark)] my-1" />
                        <button onClick={handleLogout} className="w-full flex gap-2 px-3 py-2 text-sm text-red-600 rounded hover:bg-red-500/10"><LogOut size={16} /> Logout</button>
                      </div>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm px-4 py-2 rounded">Login</Link>
                <Link to="/register" className="text-sm px-4 py-2 rounded bg-[var(--accent-primary)] text-white hover:bg-[var(--accent-light)]">Start Free</Link>
              </>
            )}

            <button onClick={() => setIsMobileOpen(!isMobileOpen)} className="md:hidden p-2.5 rounded-full border border-[var(--border)]">
              {isMobileOpen ? <X size={18} /> : <Menu size={18} />}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {isMobileOpen && (
          <div className="md:hidden mt-3 p-4 rounded-xl border border-[var(--border)] bg-[var(--surface-primary)]">
            <nav className="flex flex-col gap-2">
              {navItems.map((link) => (
                <Link key={link.to} to={link.to} onClick={() => setIsMobileOpen(false)} className="flex gap-2 text-sm p-2">
                  <link.icon size={16} /> {link.label}
                </Link>
              ))}
            </nav>
          </div>
        )}
      </div>

      {/* Search Modal */}
      {isSearchOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4" onClick={() => setIsSearchOpen(false)}>
          <div className="absolute inset-0 bg-black/30 backdrop-blur-sm" />
          <div className="relative w-full max-w-2xl rounded-2xl border border-[var(--border)] bg-[var(--surface-primary)] shadow-xl p-6" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center gap-3 mb-4">
              <Search size={18} />
              <input autoFocus placeholder="Search Diseases, Cases, Flashcards..." className="flex-1 bg-transparent border-none outline-none text-sm" />
              <kbd className="text-xs px-1.5 py-0.5 rounded border">⌘K</kbd>
            </div>
            <div className="space-y-2">
              {['/explorer', '/cases', '/flashcards', '/quiz', '/bookmarks', '/history'].map((to, i) => (
                <Link key={i} to={to} onClick={() => setIsSearchOpen(false)} className="flex gap-3 p-2 rounded hover:bg-[var(--surface-hover)] text-sm">
                  <BookOpen size={16} />
                  <div><p>Result {i + 1}</p><p className="text-xs text-[var(--text-secondary)]">Navigate to {to}</p></div>
                </Link>
              ))}
            </div>
          </div>
        </div>
      )}
    </header>
  );
};

export default Navigation;
