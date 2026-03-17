import { apiClient } from '../apiClient';
import type { DepotDto, RouteDto, CreateDepotRequest, UpdateDepotRequest, DayPlanDto } from '../types';

export const depotService = {
  getAllDepots: async (): Promise<DepotDto[]> => {
    const response = await apiClient.get<DepotDto[]>('/depots');
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch depots');
    }
    
    return response.data;
  },
  
  getRoutesByDepot: async (depotId: string): Promise<RouteDto[]> => {
    const response = await apiClient.get<RouteDto[]>(`/depots/${depotId}/routes`);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch routes');
    }
    
    return response.data;
  },
  
  createDepot: async (request: CreateDepotRequest): Promise<DepotDto> => {
    const response = await apiClient.post<DepotDto>('/depots', request);
    
    if (!response.success) {
      const errorMsg = response.message || 'Failed to create depot';
      console.error('Create depot error:', errorMsg, response);
      throw new Error(errorMsg);
    }
    
    if (!response.data) {
      throw new Error('No data returned from server');
    }
    
    return response.data;
  },
  
  updateDepot: async (depotId: string, request: UpdateDepotRequest): Promise<DepotDto> => {
    const response = await apiClient.put<DepotDto>(`/depots/${depotId}`, request);

    if (!response.success) {
      const errorMsg = response.message || 'Failed to update depot';
      console.error('Update depot error:', errorMsg, response);
      throw new Error(errorMsg);
    }

    if (!response.data) {
      throw new Error('No data returned from server');
    }

    return response.data;
  },

  getDayPlan: async (depotId: string, date?: string): Promise<DayPlanDto> => {
    const params: Record<string, string> = {};
    if (date) {
      params.date = date;
    }

    const response = await apiClient.get<DayPlanDto>(`/depots/${depotId}/day-plan`, params);

    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch day plan');
    }

    return response.data;
  },
};
