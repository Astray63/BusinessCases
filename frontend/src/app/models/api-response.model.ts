export type ApiResponseStatus = 'SUCCESS' | 'ERROR';

export interface ApiResponse<T = any> {
  result: ApiResponseStatus;
  message?: string;
  data?: T;
  timestamp?: string;
  error?: {
    code: string;
    details?: any;
  };
}

export interface PaginatedResponse<T> extends ApiResponse<T[]> {
  pagination: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}