import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  ShieldClose, BarChart3, TrendingUp, TrendingDown,
  CheckCircle2, XCircle, Clock, ThumbsUp, ThumbsDown,
  AlertCircle, Activity, Calendar
} from 'lucide-react';
import { AnimatedSection } from '../../components/motion/MotionWrappers';

export default function ReviewerReportsPage() {
  const { user } = useAuth();
  const role = (user?.roles?.[0] || user?.role || '').toUpperCase();
  const [timeRange, setTimeRange] = useState('7d');

  if (role !== 'REVIEWER' && role !== 'ADMIN') return (
    <div className="min-h-screen pt-32 pb-24 px-6 text-center">
      <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
      <p className="text-[var(--text-secondary)] text-lg mb-4">Reviewer access required</p>
      <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6">Go to Dashboard</Link>
    </div>
  );

  const timeOptions = ['24h', '7d', '30d', '90d'];

  const statCards = [
    { icon: CheckCircle2, label: 'Reviews Completed', value: '89', change: '+12%', up: true, color: 'text-emerald-500' },
    { icon: ThumbsUp, label: 'Approved', value: '67', change: '+8%', up: true, color: 'text-green-500' },
    { icon: AlertCircle, label: 'Changes Requested', value: '15', change: '-5%', up: true, color: 'text-amber-500' },
    { icon: ThumbsDown, label: 'Rejected', value: '7', change: '-2%', up: true, color: 'text-red-500' },
    { icon: Clock, label: 'Avg Review Time', value: '12m', change: '-1.5m', up: true, color: 'text-cyan-500' },
    { icon: TrendingUp, label: 'Approval Rate', value: '75%', change: '+5%', up: true, color: 'text-blue-500' },
  ];

  const weeklyData = [
    { day: 'Mon', count: 12 },
    { day: 'Tue', count: 8 },
    { day: 'Wed', count: 15 },
    { day: 'Thu', count: 10 },
    { day: 'Fri', count: 18 },
    { day: 'Sat', count: 6 },
    { day: 'Sun', count: 3 },
  ];

  const recentDecisions = [
    { disease: 'Pneumonia', author: 'Dr. Smith', decision: 'Approved', time: '30m ago' },
    { disease: 'Hypertension', author: 'Dr. Park', decision: 'Changes Requested', time: '1h ago' },
    { disease: 'Diabetes Type 2', author: 'Dr. Jones', decision: 'Approved', time: '2h ago' },
    { disease: 'Asthma', author: 'Dr. Lee', decision: 'Approved', time: '3h ago' },
    { disease: 'Migraine Protocol', author: 'Dr. Rivera', decision: 'Rejected', time: '4h ago' },
  ];

  const maxCount = Math.max(...weeklyData.map(d => d.count));

  return (
    <div className="min-h-screen pt-32 pb-24 px-6">
      <div className="max-w-7xl mx-auto">
        <AnimatedSection>
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="icon-well"><BarChart3 size={22} /></div>
              <div>
                <h1 className="font-display text-3xl font-bold text-[var(--text-primary)]">Reports</h1>
                <p className="text-sm text-[var(--text-secondary)]">Your review performance</p>
              </div>
            </div>
            <div className="flex gap-1">
              {timeOptions.map((t) => (
                <button key={t} onClick={() => setTimeRange(t)}
                  className={`px-3 py-1.5 text-xs rounded-lg transition-all ${timeRange === t ? 'bg-[var(--accent-primary)] text-white' : 'btn-neumorphic-secondary'}`}>{t}</button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
            {statCards.map((s) => (
              <div key={s.label} className="card-neumorphic p-4">
                <s.icon size={18} className={`${s.color} mb-2`} />
                <p className="text-xl font-bold text-[var(--text-primary)]">{s.value}</p>
                <p className="text-xs text-[var(--text-secondary)]">{s.label}</p>
                <p className={`text-[10px] mt-1 ${s.up ? 'text-emerald-500' : 'text-red-500'}`}>{s.change}</p>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
            {/* Weekly Reviews Chart */}
            <div className="card-neumorphic p-6">
              <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Weekly Reviews</h2>
              <div className="flex items-end justify-around gap-2 h-32">
                {weeklyData.map((d) => (
                  <div key={d.day} className="flex flex-col items-center gap-1 flex-1">
                    <div className="w-full rounded-t" style={{
                      height: `${(d.count / maxCount) * 100}%`,
                      background: 'var(--accent-primary)',
                      opacity: 0.5 + (d.count / maxCount) * 0.5,
                      minHeight: '8px'
                    }} />
                    <span className="text-xs text-[var(--text-tertiary)]">{d.day}</span>
                    <span className="text-[10px] font-semibold text-[var(--text-primary)]">{d.count}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Decision Breakdown */}
            <div className="card-neumorphic p-6">
              <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Decision Breakdown</h2>
              <div className="space-y-4">
                {[
                  { label: 'Approved', value: 67, total: 89, color: 'bg-emerald-500' },
                  { label: 'Changes Requested', value: 15, total: 89, color: 'bg-amber-500' },
                  { label: 'Rejected', value: 7, total: 89, color: 'bg-red-500' },
                ].map((item) => (
                  <div key={item.label}>
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-[var(--text-primary)]">{item.label}</span>
                      <span className="text-[var(--text-secondary)]">{Math.round(item.value / item.total * 100)}%</span>
                    </div>
                    <div className="w-full h-2 rounded-full bg-[var(--surface-primary)] shadow-[inset_2px_2px_4px_var(--shadow-dark)] overflow-hidden">
                      <div className={`h-full ${item.color} rounded-full`} style={{ width: `${item.value / item.total * 100}%` }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Recent Decisions */}
          <div className="card-neumorphic p-6">
            <h2 className="font-display text-lg font-bold text-[var(--text-primary)] mb-4">Recent Decisions</h2>
            <div className="space-y-2">
              {recentDecisions.map((d, i) => (
                <div key={i} className="depth-layer-1 rounded-xl p-3 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-[var(--text-primary)]">{d.disease}</p>
                    <p className="text-xs text-[var(--text-secondary)]">by {d.author} • {d.time}</p>
                  </div>
                  <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                    d.decision === 'Approved' ? 'text-emerald-600 bg-emerald-500/10' :
                    d.decision === 'Rejected' ? 'text-red-600 bg-red-500/10' :
                    'text-amber-600 bg-amber-500/10'
                  }`}>{d.decision}</span>
                </div>
              ))}
            </div>
          </div>
        </AnimatedSection>
      </div>
    </div>
  );
}
