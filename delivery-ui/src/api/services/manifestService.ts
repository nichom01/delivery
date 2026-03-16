import { apiClient } from '../apiClient';
import type { ManifestDto, CreateManifestRequest, UpdateManifestRequest, ManifestStopDto } from '../types';

export const manifestService = {
  getManifests: async (depotId?: string, date?: string): Promise<ManifestDto[]> => {
    const params: Record<string, string> = {};
    if (depotId) {
      params.depotId = depotId;
    }
    if (date) {
      params.date = date;
    }
    
    const response = await apiClient.get<ManifestDto[]>('/manifests', params);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch manifests');
    }
    
    return response.data;
  },
  
  getRouteStops: async (routeId: string): Promise<ManifestStopDto[]> => {
    const response = await apiClient.get<ManifestStopDto[]>(`/manifests/routes/${routeId}/stops`);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch route stops');
    }
    
    return response.data;
  },
  
  createManifest: async (request: CreateManifestRequest): Promise<ManifestDto> => {
    const response = await apiClient.post<ManifestDto>('/manifests', request);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to create manifest');
    }
    
    return response.data;
  },
  
  confirmManifest: async (manifestId: string): Promise<ManifestDto> => {
    const response = await apiClient.put<ManifestDto>(`/manifests/${manifestId}/confirm`);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to confirm manifest');
    }
    
    return response.data;
  },
  
  updateManifest: async (manifestId: string, request: UpdateManifestRequest): Promise<ManifestDto> => {
    const response = await apiClient.put<ManifestDto>(`/manifests/${manifestId}`, request);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to update manifest');
    }
    
    return response.data;
  },
  
  removeStopFromManifest: async (manifestId: string, orderId: string): Promise<ManifestDto> => {
    const response = await apiClient.delete<ManifestDto>(`/manifests/${manifestId}/stops/${orderId}`);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to remove stop from manifest');
    }
    
    return response.data;
  }
};
