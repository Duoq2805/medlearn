export interface ApiResponse<T> { 
  success: boolean; 
  message?: string; 
  data: T; 
  timestamp?: string; 
}

export interface PageMetadata { 
  page: number; 
  size: number; 
  totalElements: number; 
  totalPages: number; 
}

export interface PageResponse<T> { 
  content: T[]; 
  page: number; 
  size: number; 
  totalElements: number; 
  totalPages: number; 
  last?: boolean; 
  first?: boolean; 
}

export interface ApiError { 
  message: string; 
  code?: string; 
  details?: Record<string, unknown>; 
}
