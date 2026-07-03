import { useState } from 'react';
import { Settings, Moon, Sun, Bell, Lock, Globe, Eye, Volume2, Accessibility } from 'lucide-react';
import { useTheme } from '../../context/ThemeContext';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function ThemingDashboardPage() {
  const { theme, toggleTheme } = useTheme();
  const { user } = useAuth();
  const [settings, setSettings] = useState({
    notifications: true,
    emailUpdates: false,
    soundEnabled: true,
    darkMode: theme === 'dark',
    reduceMotion: false,
    largeText: false,
    highContrast: false,
  });

  const handleToggle = (key: keyof typeof settings) => {
    setSettings(prev => ({ ...prev, [key]: !prev[key] }));
    if (key === 'darkMode') {
      toggleTheme();
    }
  };

  const settingGroups = [
    {
      title: 'Appearance',
      items: [
        { key: 'darkMode', label: 'Dark Mode', icon: Moon, description: 'Use dark theme' },
        { key: 'reduceMotion', label: 'Reduce Motion', icon: Eye, description: 'Minimize animations' },
        { key: 'largeText', label: 'Large Text', icon: Globe, description: 'Increase font size' },
        { key: 'highContrast', label: 'High Contrast', icon: Accessibility, description: 'Enhanced contrast' },
      ],
    },
    {
      title: 'Notifications',
      items: [
        { key: 'notifications', label: 'Push Notifications', icon: Bell, description: 'Desktop notifications' },
        { key: 'emailUpdates', label: 'Email Updates', icon: Bell, description: 'Weekly summary emails' },
        { key: 'soundEnabled', label: 'Sound Effects', icon: Volume2, description: 'Enable audio feedback' },
      ],
    },
    {
      title: 'Privacy & Security',
      items: [
        { key: 'security', label: 'Change Password', icon: Lock, description: 'Update your password' },
      ],
    },
  ];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-4xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><Settings size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Settings</h1>
              <p className="text-sm text-[var(--text-secondary)]">Customize your learning experience</p>
            </div>
          </div>

          <div className="space-y-6">
            {settingGroups.map((group) => (
              <div key={group.title} className="card-neumorphic p-8">
                <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-6">{group.title}</h2>
                <div className="space-y-4">
                  {group.items.map((item) => {
                    const Icon = item.icon;
                    const isButton = item.key === 'security';
                    return (
                      <div key={item.key} className="flex items-center justify-between p-4 rounded-xl bg-[var(--surface-primary)] shadow-[inset_3px_3px_6px_var(--shadow-dark),inset_-3px_-3px_6px_var(--shadow-light)]">
                        <div className="flex items-center gap-3 flex-1">
                          <Icon size={18} className="text-[var(--accent-primary)]" />
                          <div>
                            <p className="font-semibold text-sm text-[var(--text-primary)]">{item.label}</p>
                            <p className="text-xs text-[var(--text-secondary)]">{item.description}</p>
                          </div>
                        </div>
                        {isButton ? (
                          <button className="btn-neumorphic-secondary py-2 px-4 text-sm">Change</button>
                        ) : (
                          <button
                            onClick={() => handleToggle(item.key as keyof typeof settings)}
                            className={`w-12 h-6 rounded-full transition-all ${
                              settings[item.key as keyof typeof settings]
                                ? 'bg-[var(--accent-primary)]'
                                : 'bg-[var(--surface-hover)]'
                            }`}
                          >
                            <div
                              className={`w-5 h-5 rounded-full bg-white shadow-md transition-transform ${
                                settings[item.key as keyof typeof settings] ? 'translate-x-6' : 'translate-x-0.5'
                              }`}
                            />
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}

            <div className="card-neumorphic p-8">
              <h2 className="font-display text-2xl font-bold text-[var(--text-primary)] mb-4">Danger Zone</h2>
              <p className="text-sm text-[var(--text-secondary)] mb-4">Permanent actions that cannot be undone</p>
              <button className="btn-neumorphic-secondary py-3 px-6 text-red-600 hover:bg-red-500/10">
                Delete Account
              </button>
            </div>

            <div className="flex gap-3">
              <button className="btn-neumorphic-primary py-3 px-8 flex-1">Save Settings</button>
              <button className="btn-neumorphic-secondary py-3 px-8 flex-1">Reset to Defaults</button>
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
