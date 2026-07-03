// Permission-based UI helpers for the moderation workflow.
// USER  → creates drafts, edits own drafts, submits for review
// REVIEWER → can approve/reject/publish
// ADMIN  → everything

export type Role = 'USER' | 'REVIEWER' | 'ADMIN';

export interface Permissions {
  // Draft operations (USER + REVIEWER + ADMIN)
  canCreateDraft: boolean;
  canEditOwnDraft: boolean;
  canDeleteOwnDraft: boolean;
  canSubmitForReview: boolean;
  canUploadImages: boolean;
  canImportDocument: boolean;
  canAIGenerate: boolean;
  // Moderation (REVIEWER + ADMIN)
  canApprove: boolean;
  canReject: boolean;
  canPublish: boolean;
  canEditAnyDisease: boolean;
  canComment: boolean;
  // Admin only
  canManageUsers: boolean;
  canManageRoles: boolean;
  canManageSystem: boolean;
  canViewAnalytics: boolean;
  canManageCategories: boolean;
  canManageCases: boolean;
  canViewModeration: boolean;
  canViewAuditLogs: boolean;
}

const ROLE_PERMISSIONS: Record<Role, Permissions> = {
  USER: {
    canCreateDraft: true,
    canEditOwnDraft: true,
    canDeleteOwnDraft: true,
    canSubmitForReview: true,
    canUploadImages: true,
    canImportDocument: true,
    canAIGenerate: true,
    canApprove: false,
    canReject: false,
    canPublish: false,
    canEditAnyDisease: false,
    canComment: false,
    canManageUsers: false,
    canManageRoles: false,
    canManageSystem: false,
    canViewAnalytics: false,
    canManageCategories: false,
    canManageCases: false,
    canViewModeration: false,
    canViewAuditLogs: false,
  },
  REVIEWER: {
    canCreateDraft: true,
    canEditOwnDraft: true,
    canDeleteOwnDraft: true,
    canSubmitForReview: true,
    canUploadImages: true,
    canImportDocument: true,
    canAIGenerate: true,
    canApprove: true,
    canReject: true,
    canPublish: true,
    canEditAnyDisease: true,
    canComment: true,
    canManageUsers: false,
    canManageRoles: false,
    canManageSystem: false,
    canViewAnalytics: false,
    canManageCategories: false,
    canManageCases: false,
    canViewModeration: true,
    canViewAuditLogs: false,
  },
  ADMIN: {
    canCreateDraft: true,
    canEditOwnDraft: true,
    canDeleteOwnDraft: true,
    canSubmitForReview: true,
    canUploadImages: true,
    canImportDocument: true,
    canAIGenerate: true,
    canApprove: true,
    canReject: true,
    canPublish: true,
    canEditAnyDisease: true,
    canComment: true,
    canManageUsers: true,
    canManageRoles: true,
    canManageSystem: true,
    canViewAnalytics: true,
    canManageCategories: true,
    canManageCases: true,
    canViewModeration: true,
    canViewAuditLogs: true,
  },
};

export function getPermissions(role?: string): Permissions {
  const normalized = (role || 'USER').toUpperCase() as Role;
  return ROLE_PERMISSIONS[normalized] || ROLE_PERMISSIONS.USER;
}

export function can(permission: keyof Permissions, role?: string): boolean {
  return getPermissions(role)[permission];
}

export function isRole(role?: string, target?: Role): boolean {
  if (!role) return false;
  return role.toUpperCase() === target;
}
