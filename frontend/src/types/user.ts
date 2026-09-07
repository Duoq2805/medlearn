export interface UserProfileResponse {
  id: number;
  username: string;
  email: string;
  fullName?: string;
  avatarUrl?: string;
  phoneNumber?: string;
  status: string;
  isVerified: boolean;
  roles: string[];
  lastLoginAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UpdateProfileRequest {
  username?: string;
  email?: string;
  fullName?: string;
  avatarUrl?: string;
  phoneNumber?: string;
}
