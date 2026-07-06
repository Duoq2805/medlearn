import { useState } from 'react';
import { Link } from 'react-router-dom';
import { User, ChevronRight, Camera, Save, AlertCircle, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function ProfilePage() {
  const { user } = useAuth();
  const [saved, setSaved] = useState(false);

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-3xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><User size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Profile</h1>
              <p className="text-sm text-[var(--text-secondary)]">Manage your personal information</p>
            </div>
          </div>

          <div className="space-y-6">
            {saved && (
              <div className="flex items-center gap-2 p-3 rounded-lg bg-[var(--accent-primary)]/10 text-sm text-[var(--accent-primary)]" role="alert">
                <CheckCircle2 size={16} /> Profile saved successfully.
              </div>
            )}

            <div className="card-neumorphic p-8">
              <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-6">Personal Information</h2>
              <div className="flex items-center gap-6 mb-6">
                <div className="w-20 h-20 rounded-full bg-[var(--accent-primary)]/10 flex items-center justify-center relative">
                  <User size={36} className="text-[var(--accent-primary)]" />
                  <button className="absolute -bottom-1 -right-1 w-8 h-8 rounded-full bg-[var(--accent-primary)] text-white flex items-center justify-center shadow-sm">
                    <Camera size={14} />
                  </button>
                </div>
                <div>
                  <p className="font-semibold text-[var(--text-primary)]">{user?.fullName || 'User'}</p>
                  <p className="text-xs text-[var(--text-secondary)]">{user?.email}</p>
                  <p className="text-xs text-[var(--text-tertiary)]">{(user?.roles?.[0] || 'USER').toUpperCase()}</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {[
                  { label: 'Full Name', value: user?.fullName || '' },
                  { label: 'Username', value: user?.username || '' },
                  { label: 'Email', value: user?.email || '' },
                  { label: 'University', value: '' },
                  { label: 'Medical Year', value: '' },
                  { label: 'Timezone', value: 'UTC+7 (Asia/Ho_Chi_Minh)' },
                ].map((field) => (
                  <div key={field.label} className="space-y-1">
                    <label className="text-xs font-medium text-[var(--text-secondary)]">{field.label}</label>
                    <input
                      defaultValue={field.value}
                      placeholder={`Enter your ${field.label.toLowerCase()}`}
                      className="input-neumorphic"
                    />
                  </div>
                ))}
              </div>

              <div className="mt-6 space-y-1">
                <label className="text-xs font-medium text-[var(--text-secondary)]">Bio</label>
                <textarea
                  placeholder="Tell us about yourself..."
                  className="input-neumorphic min-h-[100px] resize-none"
                />
              </div>

              <button onClick={handleSave} className="btn-neumorphic-primary py-3 px-8 inline-flex items-center gap-2 mt-6">
                <Save size={16} /> Save Changes
              </button>
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
