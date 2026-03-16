import { apiClient } from '../apiClient';
import type { UserDto } from '../types';

export const userService = {
  getUsers: async (): Promise<UserDto[]> => {
    // Note: This endpoint may need to be created in the backend
    // For now, return empty array if endpoint doesn't exist
    try {
      const response = await apiClient.get<UserDto[]>('/users');
      if (!response.success) {
        return [];
      }
      return response.data;
    } catch {
      return [];
    }
  }
};
