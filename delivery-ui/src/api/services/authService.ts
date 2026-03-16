import { apiClient } from '../apiClient';
import type { CurrentUserDto, LoginRequest, LoginResponse } from '../types';
import { storage } from '@/utils/storage';

export const authService = {
  login: async (username: string, password: string): Promise<LoginResponse> => {
    const request: LoginRequest = { username, password };
    const response = await apiClient.post<LoginResponse>('/auth/login', request);
    
    if (!response.success) {
      throw new Error(response.message || 'Login failed');
    }
    
    // Store token
    storage.setToken(response.data.token);
    
    return response.data;
  },
  
  getCurrentUser: async (): Promise<CurrentUserDto> => {
    const response = await apiClient.get<CurrentUserDto>('/auth/me');
    
    if (!response.success) {
      // If it's a network error, throw a more user-friendly message
      if (response.message.includes('connect to server')) {
        throw new Error('Unable to connect to server. Please ensure the backend is running.');
      }
      throw new Error(response.message || 'Failed to get current user');
    }
    
    return response.data;
  },
  
  logout: (): void => {
    // Token removal is handled by apiClient on 401
    // This is just a convenience method
  }
};
