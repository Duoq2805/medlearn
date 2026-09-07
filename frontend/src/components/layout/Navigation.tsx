import { useState, useRef, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useTheme } from '../../context/ThemeContext';
import { useAuth } from '../../hooks/useAuth';
import { MedvoraLogo } from '../MedvoraLogo';
import { NotificationBell } from './NotificationBell';
import {
  Menu, Moon, Sun, LogOut, Settings, Bell, User, BookOpen,
  Brain, Sparkles, BarChart3, Search,
  Bookmark, LayoutDashboard, ChevronDown, X, Command, FileText, Clock, ClipboardCheck,
  ShieldCheck, Shield, Activity, Database, Users, Eye, CheckCircle2,
  FileWarning, MessageSquare, LineChart, FolderKanban,
  Wrench, Monitor, Globe, HardDrive, Mail, Tags,
  Pill, Stethoscope, ChevronRight
} from 'lucide-react';

type NavItem = { to: string; icon: any; label: string; children?: NavItem[] };

const NAV: Record<string, NavItem[]> = {
  USER: [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/explorer', icon: BookOpen, label: 'Diseases' },
    { to: '/flashcards', icon: Sparkles, label: 'Flashcards' },
    { to: '/quiz', icon: ClipboardCheck, label: 'Quiz' },
    { to: '/cases', icon: Stethoscope, label: 'Cases' },
    { to: '/symptom-checker', icon: Brain, label: 'Symptom Checker' },
  ],
  REVIEWER: [
    { to: '/reviewer/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/reviewer/queue', icon: FolderKanban, label: 'Review Queue' },
    { to: '/reviewer/review/1', icon: FileText, label: 'Assigned Reviews' },
    { to: '/reviewer/history', icon: Clock, label: 'History' },
    { to: '/reviewer/reports', icon: BarChart3, label: 'Reports' },
  ],
  ADMIN: [
    { to: '/admin/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/admin/users', icon: Users, label: 'Users' },
    { to: '/admin/diseases', icon: BookOpen, label: 'Content', children: [
      { to: '/admin/diseases', icon: BookOpen, label: 'Diseases' },
      { to: '/admin/categories', icon: Tags, label: 'Categories' },
      { to: '/admin/symptoms', icon: Pill, label: 'Symptoms' },
      { to: '/admin/cases', icon: Activity, label: 'Cases' },
    ] },
    { to: '/admin/reports', icon: BarChart3, label: 'Reports' },
    { to: '/admin/audit-logs', icon: FileText, label: 'System', children: [
      { to: '/admin/audit-logs', icon: FileText, label: 'Audit Logs' },
      { to: '/settings', icon: Settings, label: 'Settings' },
    ] },
  ],
};

const NAV_GUEST: NavItem[] = [
  { to: '/explorer', icon: BookOpen, label: 'Diseases' },
  { to: '/symptom-checker', icon: Brain, label: 'Symptom Checker' },
];

const AVATAR_MENU: Record<string, NavItem[]> = {
  USER: [
    { to: '/profile', icon: User, label: 'Profile' },
    { to: '/bookmarks', icon: Bookmark, label: 'Bookmarks' },
    { to: '/drafts', icon: FileText, label: 'My Drafts' },
    { to: '/progress', icon: BarChart3, label: 'Progress' },
    { to: '/history', icon: Clock, label: 'History' },
    { to: '/settings', icon: Settings, label: 'Settings' },
  ],
  REVIEWER: [
    { to: '/profile', icon: User, label: 'Profile' },
    { to: '/reviewer/queue', icon: FolderKanban, label: 'Review Queue' },
    { to: '/reviewer/history', icon: Clock, label: 'Review History' },
    { to: '/reviewer/reports', icon: BarChart3, label: 'My Reports' },
  ],
  ADMIN: [
    { to: '/admin/dashboard', icon: Shield, label: 'Admin Dashboard' },
    { to: '/admin/users', icon: Users, label: 'User Management' },
    { to: '/admin/reports', icon: BarChart3, label: 'Reports' },
    { to: '/admin/audit-logs', icon: FileText, label: 'Audit Logs' },
    { to: '/settings', icon: Settings, label: 'Platform Settings' },
  ],
};

export const Navigation: React.FC = () => {
  const { theme, toggleTheme } = useTheme();
  const { user, logout } = useAuth();
  const [isMobileOpen, setIsMobileOpen] = useState(false);
  const [isAvatarOpen, setIsAvatarOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [openDropdown, setOpenDropdown] = useState<string | null>(null);
  const avatarRef = useRef<HTMLDivElement>(null);
  const notifRef = useRef<HTMLDivElement>(null);
  const dropdownRefs = useRef<Record<string, HTMLDivElement | null>>({});
  const searchInputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();
  const location = useLocation();

  const role = (user?.roles?.[0] || user?.role || 'USER').toUpperCase();
  const isActive = (path: string) => location.pathname.startsWith(path);

  useEffect(() => {
    const handleMouseClick = (e: MouseEvent) => {
      if (avatarRef.current && !avatarRef.current.contains(e.target as Node)) setIsAvatarOpen(false);
      if (notifRef.current && !notifRef.current.contains(e.target as Node)) setIsNotifOpen(false);
      Object.entries(dropdownRefs.current).forEach(([key, ref]) => {
        if (ref && !ref.contains(e.target as Node) && openDropdown === key) setOpenDropdown(null);
      });
    };
    document.addEventListener('mousedown', handleMouseClick);
    return () => document.removeEventListener('mousedown', handleMouseClick);
  }, [openDropdown]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') { e.preventDefault(); setIsSearchOpen(true); }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, []);

  const userInitial = user?.fullName?.[0]?.toUpperCase() || user?.username?.[0]?.toUpperCase() || '?';
  const navItems = user ? (NAV[role] || NAV.USER) : NAV_GUEST;
  const avatarItems = AVATAR_MENU[role] || AVATAR_MENU.USER;

  const handleLogout = () => { logout(); setIsAvatarOpen(false); navigate('/'); };

  const renderNavLink = (item: NavItem, isChild = false) => (
    <Link key={item.to + item.label} to={item.to}
      className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-all flex items-center gap-1.5 whitespace-nowrap ${
        isActive(item.to)
          ? 'bg-[var(--accent-primary)]/10 text-[var(--accent-primary)]'
          : 'text-[var(--text-secondary)] hover:bg-[var(--surface-hover)]'
      }`}
      onClick={() => setOpenDropdown(null)}
    >
      <item.icon size={16} /> {item.label}
    </Link>
  );

  const renderDesktopNav = () => (
    <nav className="hidden md:flex gap-1 items-center">
      {user && (
        <button onClick={() => setIsSearchOpen(true)}
          className="flex items-center gap-2 px-3 py-1.5 text-sm rounded-lg text-[var(--text-secondary)] hover:bg-[var(--surface-hover)] transition-colors" aria-label="Search">
          <Search size={16} />
          <span className="text-xs text-[var(--text-tertiary)] border border-[var(--border)] rounded px-1.5 py-0.5"><Command size={10} className="inline" />K</span>
        </button>
      )}
      {navItems.map((item) => {
        if (item.children) {
          return (
            <div key={item.label} ref={(el) => { dropdownRefs.current[item.label] = el; }} className="relative">
              <button onClick={() => setOpenDropdown(openDropdown === item.label ? null : item.label)}
                className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-all flex items-center gap-1.5 ${
                  item.children.some(c => isActive(c.to))
                    ? 'bg-[var(--accent-primary)]/10 text-[var(--accent-primary)]'
                    : 'text-[var(--text-secondary)] hover:bg-[var(--surface-hover)]'
                }`}>
                <item.icon size={16} /> {item.label} <ChevronDown size={12} className={`transition-transform ${openDropdown === item.label ? 'rotate-180' : ''}`} />
              </button>
              {openDropdown === item.label && (
                <div className="absolute top-full left-0 mt-1 w-44 rounded-xl border border-[var(--border)] bg-[var(--surface-primary)] backdrop-blur-xl shadow-lg z-50 overflow-hidden depth-layer-1 p-1.5">
                  {item.children.map((child) => (
                    <Link key={child.to + child.label} to={child.to}
                      className={`flex items-center gap-2 px-3 py-2 text-sm rounded-lg transition-colors ${
                        isActive(child.to)
                          ? 'bg-[var(--accent-primary)]/10 text-[var(--accent-primary)]'
                          : 'text-[var(--text-secondary)] hover:bg-[var(--surface-hover)]'
                      }`}
                      onClick={() => setOpenDropdown(null)}>
                      <child.icon size={14} /> {child.label}
                    </Link>
                  ))}
                </div>
              )}
            </div>
          );
        }
        return renderNavLink(item);
      })}
    </nav>
  );

  const renderMobileNav = () => navItems.flatMap((item) =>
    item.children
      ? [item, ...item.children.map(c => ({ ...c, icon: c.icon || item.icon }))]
      : [item]
  ).map((link) => (
    <Link key={link.to + link.label} to={link.to} onClick={() => setIsMobileOpen(false)}
      className={`flex items-center gap-2 text-sm font-medium ${isActive(link.to) ? 'text-[var(--accent-primary)]' : 'text-[var(--text-secondary)]'}`}>
      <link.icon size={16} /> {link.label}
    </Link>
  ));

  return (
    <header className="fixed top-0 left-0 right-0 z-50 px-4 md:px-8 py-4">
      <div className="max-w-7xl mx-auto">
        <div className="rounded-2xl px-6 py-3 border backdrop-blur-xl flex justify-between items-center bg-white dark:bg-[var(--surface-primary)] border-[var(--border)]" style={{ boxShadow: 'var(--shadow-sm)' }}>
          <Link to={user ? '/admin/dashboard' : '/'} className="flex items-center gap-2.5 group">
            <MedvoraLogo className="h-9 w-9" variant="icon" />
            <span className="font-display font-bold text-lg hidden md:inline bg-gradient-to-r from-[var(--text-primary)] to-[var(--text-secondary)] dark:from-[var(--accent-primary)] dark:to-[var(--accent-light)] bg-clip-text text-transparent">Medvora</span>
          </Link>

          {renderDesktopNav()}

          <div className="flex items-center gap-3">
            <button onClick={toggleTheme} className="p-2.5 rounded-full border border-[var(--border)] bg-[--surface-white] dark:bg-[var(--surface-secondary)] text-[var(--text-primary)] hover:bg-gray-100 dark:hover:bg-[var(--surface-hover)] transition-colors" aria-label="Toggle theme">
              {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            </button>

            {user ? (
              <>
                <NotificationBell />
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm font-medium px-4 py-2 rounded-lg text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors">Login</Link>
                <Link to="/register" className="text-sm font-medium px-4 py-2 rounded-lg bg-[var(--accent-primary)] hover:bg-[var(--accent-light)] text-white transition-all shadow-md hover:shadow-lg active:scale-95">Start Free</Link>
              </>
            )}

            <button onClick={() => setIsMobileOpen(!isMobileOpen)} className="md:hidden p-2.5 rounded-full border border-[var(--border)] bg-[--surface-white] dark:bg-[var(--surface-secondary)] text-[var(--text-primary)] hover:bg-gray-100 dark:hover:bg-[var(--surface-hover)] transition-colors" aria-label="Menu">
              {isMobileOpen ? <X size={18} /> : <Menu size={18} />}
            </button>
          </div>
        </div>

        {isMobileOpen && (
          <div className="md:hidden mt-3 p-4 rounded-xl border border-[var(--border)] bg-[--surface-white] dark:bg-[var(--surface-secondary)] backdrop-blur-xl" style={{ boxShadow: 'var(--shadow-sm)' }}>
            <nav className="flex flex-col gap-3">
              {renderMobileNav()}
            </nav>
          </div>
        )}

        {isSearchOpen && (
          <div className="fixed inset-0 z-[100] flex items-center justify-center p-4" onClick={() => setIsSearchOpen(false)}>
            <div className="absolute inset-0 bg-black/30 backdrop-blur-sm" />
            <div className="relative w-full max-w-2xl rounded-2xl border border-[var(--border)] bg-[var(--surface-primary)] backdrop-blur-lg shadow-xl overflow-hidden p-6" onClick={(e) => e.stopPropagation()}>
              <div className="flex items-center gap-3 mb-4">
                <Search size={18} className="text-[var(--text-tertiary)]" />
                <input ref={searchInputRef} type="text" autoFocus placeholder="Search..." className="flex-1 bg-transparent border-none outline-none text-sm placeholder:text-[var(--text-tertiary)]" />
                <kbd className="text-xs px-1.5 py-0.5 rounded border border-[var(--border)]">⌘ K</kbd>
              </div>
            </div>
          </div>
        )}
      </div>
    </header>
  );
};

export default Navigation;
