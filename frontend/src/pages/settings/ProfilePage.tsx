import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { User, ChevronRight, Camera, Save, AlertCircle, CheckCircle2, Lock, Calendar, Edit3, FileText, CloudUpload, Sparkles } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function ProfilePage() {
  const { user } = useAuth();
  const [saved, setSaved] = useState(false);

  // Mock data for unsupported fields, will be disabled/hidden
  const [profileData, setProfileData] = useState({
    fullName: user?.fullName || '',
    username: user?.username || '',
    email: user?.email || '',
    bio: '', // Mock: Not in backend DTO
    university: '', // Mock: Not in backend DTO
    medicalYear: '', // Mock: Not in backend DTO
    learningGoal: '', // Mock: Not in backend DTO
    timezone: 'UTC+7 (Asia/Ho_Chi_Minh)', // Mock: Not in backend DTO
  });

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
                  {/* Avatar upload is NOT supported by backend */}
                  <button disabled className="absolute -bottom-1 -right-1 w-8 h-8 rounded-full bg-[var(--surface-hover)] text-[var(--text-tertiary)] opacity-50 cursor-not-allowed flex items-center justify-center shadow-sm">
                    <Camera size={14} />
                  </button>
                </div>
                <div>
                  <p className="font-semibold text-[var(--text-primary)]">{profileData.fullName}</p>
                  <p className="text-xs text-[var(--text-secondary)]">{profileData.email}</p>
                  <p className="text-xs text-[var(--text-tertiary)]">{(user?.roles?.[0] || 'USER').toUpperCase()}</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-grid-cols-2 gap-4">
                {/* Full Name, Username, Email */}
                <div className="space-y-1">
                  <label className="text-xs font-medium text-[var(--text-secondary)]">Full Name</label>
                  <input defaultValue={profileData.fullName} placeholder="Enter your full name" className="input-neumorphic" />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-medium text-[var(--text-secondary)]">Username</label>
                  <input defaultValue={profileData.username} placeholder="Enter your username" className="input-neumorphic" />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-medium text-[var(--text-secondary)]">Email</label>
                  <input defaultValue={profileData.email} placeholder="Enter your email" className="input-neumorphic" />
                </div>

                {/* Unsupported fields - DISABLED */}
                <div className="space-y-1">
                  <label className="text-xs font-medium text-[var(--text-tertiary)]">Bio</label>
                  <textarea placeholder="Tell us about yourself..." defaultValue={profileData.bio} disabled className="input-neumorphic min-h-[100px] resize-none opacity-50 cursor-not-allowed w-full" />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-medium text-[var(--text-tertiary)]">University</label>
                  <input placeholder="Enter your university" defaultValue={profileData.university} disabled className="input-neumorphic opacity-50 cursor-not-allowed" />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-medium text-[var(--text-tertiary)]">Medical Year</label>
                  <input placeholder="Enter your medical year" defaultValue={profileData.medicalYear} disabled className="input-neumorphic opacity-50 cursor-not-allowed" />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-medium text-[var(--text-tertiary)]">Learning Goal</label>
                  <input placeholder="Enter your learning goal" defaultValue={profileData.learningGoal} disabled className="input-neumorphic opacity-50 cursor-not-allowed" />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-medium text-[var(--text-tertiary)]">Timezone</label>
                  <input placeholder="Enter your timezone" defaultValue={profileData.timezone} disabled className="input-neumorphic opacity-50 cursor-not-allowed" />
                </div>
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
