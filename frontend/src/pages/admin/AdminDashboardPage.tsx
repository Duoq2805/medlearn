import { useQuery, useMutation } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Shield, ShieldCheck, ShieldClose } from 'lucide-react';

import { adminApi } from "../../api/admin";
import { User } from "../../types";
import { useAuth } from "../../hooks/useAuth";
import Skeleton from "../../components/ui/Skeleton";

export default function AdminDashboardPage() {
  const { user } = useAuth();

  const {
    data: users,
    isLoading: isLoadingUsers,
    error: usersError,
    refetch: refetchUsers
  } = useQuery<User[]>({
    queryKey: ['admin', 'users'],
    queryFn: () => adminApi.fetchUsers(),
    enabled: !!user && user.role === 'admin',
  });

  const updateUserRoleMutation = useMutation({
    mutationFn: ({ userId, role }: { userId: string; role: 'student' | 'reviewer' | 'admin' }) =>
      adminApi.updateUserRole(userId, role),
    onSuccess: () => refetchUsers(),
  });

  if (!user || user.role !== 'admin') {
    return (
      <div className="min-h-screen bg-[var(--bg-primary)] pt-32 pb-24">
        <div className="max-w-4xl mx-auto px-4 md:px-8 text-center py-20">
          <div className="card-neumorphic-sm max-w-md mx-auto p-8">
            <ShieldClose size={48} className="mx-auto mb-4 text-[var(--text-tertiary)]" />
            <p className="text-[var(--text-secondary)] text-lg mb-4">Admin access required</p>
            <Link to="/dashboard" className="btn-neumorphic-primary py-2 px-6 inline-flex items-center gap-2">Go to Dashboard</Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[var(--bg-primary)] pt-32 pb-24">
      <div className="max-w-7xl mx-auto px-4 md:px-8">
        <div className="mb-10">
          <p className="text-sm font-medium text-[var(--accent-primary)] mb-1">Administration</p>
          <h1 className="font-display text-3xl md:text-4xl font-bold text-[var(--text-primary)]">User Management</h1>
        </div>

        {isLoadingUsers ? (
          <div className="space-y-3">
            {[1, 2, 3].map(i => <Skeleton key={i} className="h-16 w-full rounded-xl" />)}
          </div>
        ) : usersError ? (
          <div className="card-neumorphic-sm p-8 text-center">
            <p className="text-[var(--text-secondary)] mb-4">Failed to load users.</p>
            <button onClick={() => refetchUsers()} className="btn-neumorphic-primary py-2 px-6">Retry</button>
          </div>
        ) : (
          <div className="space-y-3">
            {users?.map((u) => (
              <div key={u.id} className="card-neumorphic-sm p-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-full bg-[var(--accent-primary)]/10 flex items-center justify-center text-sm font-medium text-[var(--accent-primary)]">
                    {u.name?.[0]?.toUpperCase() || '?'}
                  </div>
                  <div>
                    <p className="text-sm font-medium text-[var(--text-primary)]">{u.name}</p>
                    <p className="text-xs text-[var(--text-tertiary)]">{u.email}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                    u.role === 'admin' ? 'bg-[var(--accent-primary)]/10 text-[var(--accent-primary)]' :
                    u.role === 'reviewer' ? 'bg-[#3b82f6]/10 text-[#2563eb]' :
                    'bg-[var(--text-tertiary)]/10 text-[var(--text-tertiary)]'
                  }`}>
                    {u.role}
                  </span>
                  {['admin', 'reviewer', 'user'].filter(r => r !== u.role).map(role => (
                    <button
                      key={role}
                      onClick={() => updateUserRoleMutation.mutate({ userId: String(u.id), role: role as 'student' | 'reviewer' | 'admin' })}
                      className="text-xs text-[var(--text-tertiary)] hover:text-[var(--accent-primary)] transition-colors px-2 py-1 rounded-lg hover:bg-[var(--surface-hover)]"
                    >
                      Make {role}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
