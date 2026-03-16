import { apiClient } from '../apiClient';
import type { VehicleDto } from '../types';

export interface CreateVehicleRequest {
  registration: string;
  make?: string;
  model?: string;
  capacity?: string;
  motDate?: string;
  nextServiceDue?: string;
  depotId: string;
}

export interface UpdateVehicleRequest {
  registration?: string;
  make?: string;
  model?: string;
  capacity?: string;
  motDate?: string;
  nextServiceDue?: string;
  status?: string;
}

export const vehicleService = {
  getVehicles: async (depotId?: string): Promise<VehicleDto[]> => {
    const params: Record<string, string> = {};
    if (depotId) {
      params.depotId = depotId;
    }
    
    const response = await apiClient.get<VehicleDto[]>('/vehicles', params);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch vehicles');
    }
    
    return response.data;
  },
  
  createVehicle: async (request: CreateVehicleRequest): Promise<VehicleDto> => {
    const response = await apiClient.post<VehicleDto>('/vehicles', request);
    
    if (!response.success) {
      const errorMsg = response.message || 'Failed to create vehicle';
      console.error('Create vehicle error:', errorMsg, response);
      throw new Error(errorMsg);
    }
    
    if (!response.data) {
      throw new Error('No data returned from server');
    }
    
    return response.data;
  },
  
  updateVehicle: async (vehicleId: string, request: UpdateVehicleRequest): Promise<VehicleDto> => {
    const response = await apiClient.put<VehicleDto>(`/vehicles/${vehicleId}`, request);
    
    if (!response.success) {
      const errorMsg = response.message || 'Failed to update vehicle';
      console.error('Update vehicle error:', errorMsg, response);
      throw new Error(errorMsg);
    }
    
    if (!response.data) {
      throw new Error('No data returned from server');
    }
    
    return response.data;
  }
};
