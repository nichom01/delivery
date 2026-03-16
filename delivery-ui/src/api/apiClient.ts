import { storage } from '@/utils/storage';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export interface ApiError {
  success: false;
  message: string;
  data?: any;
}

export interface ApiSuccess<T> {
  success: true;
  message?: string;
  data: T;
}

export type ApiResponse<T> = ApiSuccess<T> | ApiError;

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    const token = storage.getToken();
    
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const url = `${this.baseUrl}${endpoint}`;
    
    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      if (!response.ok) {
        if (response.status === 401) {
          // Unauthorized - clear token
          storage.removeToken();
          // Don't redirect here - let the calling code handle it
          return {
            success: false,
            message: 'Unauthorized',
          };
        }
        
        let errorMessage = `HTTP error! status: ${response.status}`;
        try {
          const errorData = await response.json();
          console.error('API error response:', errorData);
          // Backend returns ApiResponse with 'message' field
          if (errorData.message) {
            errorMessage = errorData.message;
          } else if (errorData.error) {
            errorMessage = errorData.error;
          }
        } catch (e) {
          // If JSON parsing fails, try to get text
          try {
            const text = await response.text();
            console.error('Non-JSON error response:', text);
            if (text) {
              errorMessage = text;
            }
          } catch {
            // Ignore text parse errors
          }
        }
        
        return {
          success: false,
          message: errorMessage,
        };
      }

      const data = await response.json();
      
      // Handle ApiResponse wrapper from backend
      if (data.success !== undefined) {
        // Backend returns {success: boolean, message?: string, data?: T}
        // If success is false, return error response
        if (!data.success) {
          return {
            success: false,
            message: data.message || 'Operation failed',
          };
        }
        return data as ApiResponse<T>;
      }
      
      // If response doesn't have success field, wrap it
      return {
        success: true,
        data: data as T,
      };
    } catch (error) {
      // Network error or fetch failed
      if (error instanceof TypeError && error.message.includes('fetch')) {
        return {
          success: false,
          message: 'Unable to connect to server. Please check if the backend is running.',
        };
      }
      if (error instanceof Error) {
        return {
          success: false,
          message: error.message,
        };
      }
      return {
        success: false,
        message: 'An unexpected error occurred',
      };
    }
  }

  async get<T>(endpoint: string, params?: Record<string, string>): Promise<ApiResponse<T>> {
    let url = endpoint;
    if (params) {
      const searchParams = new URLSearchParams(params);
      url += `?${searchParams.toString()}`;
    }
    return this.request<T>(url, { method: 'GET' });
  }

  async post<T>(endpoint: string, body?: any): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  async put<T>(endpoint: string, body?: any): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }
}

export const apiClient = new ApiClient(API_BASE_URL);
