export interface RoleRequest {
  roleName: string;
}

export interface UserStatusRequest {
  isActive: boolean;
}

export interface AnalyticsResponse {
  message?: string;
  [key: string]: any;
}
