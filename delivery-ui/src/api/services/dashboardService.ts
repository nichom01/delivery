import { apiClient } from '../apiClient';
import type { DashboardDto } from '../types';

export const dashboardService = {
  getDashboard: async (depotId?: string, date?: string): Promise<DashboardDto> => {
    const params: Record<string, string> = {};
    if (depotId) {
      params.depotId = depotId;
    }
    if (date) {
      params.date = date;
    }
    
    const response = await apiClient.get<DashboardDto>('/dashboard', params);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch dashboard');
    }
    
    return response.data;
  }
};
