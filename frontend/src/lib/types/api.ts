// ─── API Response Wrapper ─────────────────────────────────
export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  error?: ApiError;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string>;
  timestamp: string;
  path: string;
  requestId: string;
}

export class ApiException extends Error {
  code: string;
  details?: Record<string, string>;
  constructor(err: ApiError) {
    super(err.message);
    this.code = err.code;
    this.details = err.details;
    this.name = 'ApiException';
  }
}
