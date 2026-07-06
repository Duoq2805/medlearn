import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  ShieldCheck, ShieldClose, FolderKanban, Clock, AlertCircle,
  MessageSquare, Eye, X, Check, ChevronRight, FileText,
  CheckCircle2, TrendingUp, TrendingDown, UserCheck, Activity
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function ReviewerDashboardPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();

  if (role !== 'REVIEWER' && role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Reviewer access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const overviewCards = [
    { icon: FolderKanban, label: 'Pending Reviews', value: '23', change: '+5', up: false, color: 'text-amber-500' },
    { icon: UserCheck, label: 'Assigned To Me', value: '8', change: '+2', up: false, color: 'text-blue-500' },
    { icon: AlertCircle, label: 'Waiting Revision', value: '15', change: '-3', up: true, color: 'text-red-500' },
    { icon: CheckCircle2, label: 'Approved Today', value: '12', change: '+4', up: true, color: 'text-emerald-500' },
    { icon: X, label: 'Rejected Today', value: '3', change: '-1', up: true, color: 'text-purple-500' },
    { icon: Clock, label: 'Avg Review Time', value: '2.4h', change: '-0.3h', up: true, color: 'text-cyan-500' },
  ];

  const pendingItems = [
    { id: 1, title: 'Rheumatoid Arthritis', author: 'Dr. Emily Chen', type: 'Disease', priority: 'High', time: '2h ago' },
    { id: 2, title: 'Chronic Kidney Disease', author: 'Dr. James Wilson', type: 'Case Study', priority: 'Medium', time: '4h ago' },
    { id: 3, title: 'Pediatric Asthma Protocol', author: 'Dr. Lisa Park', type: 'Disease', priority: 'High', time: '6h ago' },
    { id: 4, title: 'Post-COVID Syndrome', author: 'Dr. Mark Rivera', type: 'Case Study', priority: 'Low', time: '8h ago' },
    { id: 5, title: 'Migraine Prophylaxis', author: 'Dr. Sarah Chen', type: 'Disease', priority: 'Medium', time: '12h ago' },
  ];

  const recentActivity = [
    { text: 'Approved: "Pneumonia" by Dr. Smith', time: '30m ago', icon: CheckCircle2, color: 'text-emerald-500' },
    { text: 'Requested changes: "Hypertension"', time: '1h ago', icon: MessageSquare, color: 'text-amber-500' },
    { text: 'Approved: "Diabetes Type 2" by Dr. Jones', time: '2h ago', icon: CheckCircle2, color: 'text-emerald-500' },
    { text: 'New submission: "Asthma" by Dr. Lee', time: '3h ago', icon: FileText, color: 'text-blue-500' },
    { text: 'Rejected: "Migraine Protocol" - insufficient refs', time: '4h ago', icon: X, color: 'text-red-500' },
  ];

  const notifications = [
    { text: 'New submission: "COPD Management"', type: 'submission' },
    { text: '@mention in "Pneumonia" review', type: 'mention' },
    { text: 'New version: "Diabetes Type 2" v3', type: 'version' },
    { text: 'Deadline approaching: "Hypertension"', type: 'deadline' },
  ];

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center gap-3 mb-8">
            <div className="icon-well"><ShieldCheck size={22} /></div>
            <div>
              <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Reviewer Dashboard</h1>
              <p className="text-sm text-[var(--text-secondary)]">Content Moderation</p>
            </div>
          </div>

          {/* Overview Cards */}
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
            {overviewCards.map((c) => (
              <div key={c.label} className="card-neumorphic p-4">
                <c.icon size={18} className={`${c.color} mb-2`} />
                <p className="text-xl font-bold text-[var(--text-primary)]">{c.value}</p>
                <p className="text-xs text-[var(--text-secondary)]">{c.label}</p>
                <p className={`text-[10px] mt-1 ${c.up ? 'text-emerald-500' : 'text-red-500'}`}>{c.change}</p>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-8">
              {/* Pending Reviews Queue */}
              <div className="card-neumorphic p-6">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="font-display text-lg font-bold text-[var(--text-primary)]">Pending Reviews</h2>
                  <div className="flex items-center gap-3">
                    <Link to="/reviewer/queue" className="text-xs text-[var(--accent-primary)] hover:underline">View All</Link>
                    <span className="text-xs px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-600 font-medium">{pendingItems.length}</span>
                  </div>
                </div>
                <div className="space-y-2">
                  {pendingItems.map((item) => (
                    <Link key={item.id} to={`/reviewer/review/${item.id}`} className="depth-layer-1 rounded-xl p-4 flex items-center justify-between hover:bg-[var(--surface-hover)] transition-all group cursor-pointer">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <p className="text-sm font-semibold text-[var(--text-primary)] truncate">{item.title}</p>
                          <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-medium ${
                            item.priority === 'High' ? 'bg-red-500/10 text-red-600' :
                            item.priority === 'Medium' ? 'bg-amber-500/10 text-amber-600' :
                            'bg-blue-500/10 text-blue-600'
                          }`}>{item.priority}</span>
                          <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-blue-500/10 text-blue-600">{item.type}</span>
                        </div>
                        <p className="text-xs text-[var(--text-secondary)]">by {item.author} • {item.time}</p>
                      </div>
                      <ChevronRight size={16} className="text-[var(--text-tertiary)] ml-2" />
                    </Link>
                  ))}
                </div>
              </div>

              {/* Recent Activity */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Recent Activity</h2>
                <div className="space-y-3">
                  {recentActivity.map((a, i) => (
                    <div key={i} className="flex items-start gap-3">
                      <a.icon size={14} className={`${a.color} mt-0.5`} />
                      <div>
                        <p className="text-xs text-[var(--text-primary)]">{a.text}</p>
                        <p className="text-[10px] text-[var(--text-tertiary)]">{a.time}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Right Sidebar */}
            <div className="space-y-6">
              {/* Quick Review */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Quick Review</h2>
                <div className="space-y-2">
                  <Link to={`/reviewer/review/${pendingItems[0]?.id || 1}`} className="btn-neumorphic-primary py-2 px-4 w-full text-sm flex items-center justify-center gap-2">
                    <Eye size={14} /> Review Next
                  </Link>
                  <Link to="/reviewer/queue" className="btn-neumorphic-secondary py-2 px-4 w-full text-sm flex items-center justify-center gap-2">
                    <FolderKanban size={14} /> View Queue
                  </Link>
                </div>
              </div>

              {/* Reviewer Stats */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">My Stats</h2>
                <div className="space-y-3">
                  {[
                    { label: 'Approval Rate', value: '78%', up: true },
                    { label: 'Review Accuracy', value: '94%', up: true },
                    { label: 'Reviews This Week', value: '28', up: true },
                    { label: 'Avg Time Per Review', value: '12m', up: false },
                  ].map((s) => (
                    <div key={s.label} className="flex items-center justify-between">
                      <span className="text-xs text-[var(--text-secondary)]">{s.label}</span>
                      <div className="flex items-center gap-1">
                        <span className="text-sm font-semibold text-[var(--text-primary)]">{s.value}</span>
                        {s.up ? <TrendingUp size={12} className="text-emerald-500" /> : <TrendingDown size={12} className="text-red-500" />}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Notifications */}
              <div className="card-neumorphic p-6">
                <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Notifications</h2>
                <div className="space-y-2">
                  {notifications.map((n, i) => (
                    <div key={i} className={`flex items-center gap-2 p-2 rounded-lg text-xs ${
                      n.type === 'submission' ? 'bg-blue-500/10 text-blue-700' :
                      n.type === 'mention' ? 'bg-purple-500/10 text-purple-700' :
                      n.type === 'version' ? 'bg-emerald-500/10 text-emerald-700' :
                      'bg-amber-500/10 text-amber-700'
                    }`}>
                      {n.text}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
