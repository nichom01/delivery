import { apiClient } from '../apiClient';
import type { DriverDto } from '../types';

export interface CreateDriverRequest {
  name: string;
  contact?: string;
  licenceNumber?: string;
  licenceExpiry?: string;
  shiftInfo?: string;
  depotId: string;
}

export interface UpdateDriverRequest {
  name?: string;
  contact?: string;
  licenceNumber?: string;
  licenceExpiry?: string;
  shiftInfo?: string;
  status?: string;
}

export const driverService = {
  getDrivers: async (depotId?: string): Promise<DriverDto[]> => {
    const params: Record<string, string> = {};
    if (depotId) {
      params.depotId = depotId;
    }
    
    const response = await apiClient.get<DriverDto[]>('/drivers', params);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch drivers');
    }
    
    return response.data;
  },
  
  createDriver: async (request: CreateDriverRequest): Promise<DriverDto> => {
    const response = await apiClient.post<DriverDto>('/drivers', request);
    
    if (!response.success) {
      const errorMsg = response.message || 'Failed to create driver';
      console.error('Create driver error:', errorMsg, response);
      throw new Error(errorMsg);
    }
    
    if (!response.data) {
      throw new Error('No data returned from server');
    }
    
    return response.data;
  },
  
  updateDriver: async (driverId: string, request: UpdateDriverRequest): Promise<DriverDto> => {
    const response = await apiClient.put<DriverDto>(`/drivers/${driverId}`, request);
    
    if (!response.success) {
      const errorMsg = response.message || 'Failed to update driver';
      console.error('Update driver error:', errorMsg, response);
      throw new Error(errorMsg);
    }
    
    if (!response.data) {
      throw new Error('No data returned from server');
    }
    
    return response.data;
  }
};
